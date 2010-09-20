package com.vegalabs.amail.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.id.WaveletId;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.memcache.jsr107cache.GCacheFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.internal.ImmutableList;
import com.google.inject.servlet.RequestScoped;
import com.google.wave.api.AbstractRobot;
import com.google.wave.api.Attachment;
import com.google.wave.api.Blip;
import com.google.wave.api.BlipContentRefs;
import com.google.wave.api.Context;
import com.google.wave.api.ElementType;
import com.google.wave.api.Gadget;
import com.google.wave.api.JsonRpcResponse;
import com.google.wave.api.ParticipantProfile;
import com.google.wave.api.Tags;
import com.google.wave.api.Wavelet;
import com.google.wave.api.JsonRpcConstant.ParamsProperty;
import com.google.wave.api.event.BlipSubmittedEvent;
import com.google.wave.api.event.GadgetStateChangedEvent;
import com.google.wave.api.event.OperationErrorEvent;
import com.google.wave.api.event.WaveletSelfAddedEvent;
import com.google.wave.api.impl.DocumentModifyAction.BundledAnnotation;
import com.vegalabs.amail.server.admin.SendEmail;
import com.vegalabs.amail.server.dao.EmailEventDao;
import com.vegalabs.amail.server.dao.EmailThreadDao;
import com.vegalabs.amail.server.data.FullWaveAddress;
import com.vegalabs.amail.server.model.EmailEvent;
import com.vegalabs.amail.server.model.EmailThread;
import com.vegalabs.amail.server.utils.MailUtils;
import com.vegalabs.amail.server.utils.StringUtils;
import com.vegalabs.general.server.command.Command;
import com.vegalabs.general.server.rpc.JsonRpcRequest;
import com.vegalabs.general.server.rpc.util.Util;
import com.vegalabs.amail.server.utils.Base64Coder;
import com.vegalabs.amail.shared.ActivityType;
import com.vegalabs.amail.shared.UnicodeString;
import com.google.wave.api.Attachment;

@Singleton
public class WaveMailRobot extends AbstractRobot {
private static final Logger LOG = Logger.getLogger(WaveMailRobot.class.getName());
  
  public static final String SANDBOX_DOMAIN = "wavesandbox.com";
  public static final String PREVIEW_DOMAIN = "googlewave.com";

  public static final String PREVIEW_RPC_URL = "http://gmodules.com/api/rpc";
  public static final String SANDBOX_RPC_URL = "http://sandbox.gmodules.com/api/rpc";

  protected  String OAUTH_TOKEN = null;
  protected  String OAUTH_KEY = null;
  protected  String OAUTH_SECRET = null;
  protected  String SECURITY_TOKEN = null;
  
  protected static String DIGEST_WAVE_DOMAIN = null;
  protected static String DIGEST_WAVE_ID = null;
 
  
  protected Injector injector = null;
  protected Util util = null;
  

  private String domain = PREVIEW_DOMAIN;//SANDBOX_DOMAIN;
  
  private Cache cache;
  private EmailEventDao emailEventDao;
  private EmailThreadDao emailThreadDao;

  @Inject
  public WaveMailRobot(Injector injector, Util util, EmailEventDao emailEventDao, EmailThreadDao emailThreadDao) {
    this.injector = injector;
    this.util = util;
    this.emailEventDao = emailEventDao;
    this.emailThreadDao = emailThreadDao;

    OAUTH_TOKEN = System.getProperty("OAUTH_TOKEN");
    OAUTH_KEY = System.getProperty("OAUTH_KEY");
    OAUTH_SECRET = System.getProperty("OAUTH_SECRET");
    SECURITY_TOKEN = System.getProperty("SECURITY_TOKEN");
    DIGEST_WAVE_DOMAIN = System.getProperty("DIGEST_WAVE_DOMAIN");
    DIGEST_WAVE_ID = System.getProperty("DIGEST_WAVE_ID");
    initOauth();
    try {
    	Map<String, Integer> props = new HashMap<String, Integer>();
        props.put(GCacheFactory.EXPIRATION_DELTA, 60*60*12);
        cache = CacheManager.getInstance().getCacheFactory().createCache(props);
    } catch (CacheException e) {
        LOG.log(Level.SEVERE,"cache init",e);
    }

  }
  
  private static Map<String,String> fixStylesMap = new HashMap<String, String>(); 
  static{
	  fixStylesMap.put("background-color", "backgroundColor");
	  fixStylesMap.put("font-family", "fontFamily");
	  fixStylesMap.put("font-size", "fontSize");
	  fixStylesMap.put("font-style", "fontStyle");
	  fixStylesMap.put("font-weight", "fontWeight");
	  fixStylesMap.put("text-decoration", "textDecoration");
	  fixStylesMap.put("vertical-align", "verticalAlign");
  }
  
  public static String fixStyles(String html){
	  Set<String> keys = fixStylesMap.keySet();
	  for(String key : keys){
		  String style = fixStylesMap.get(key);
		  html.replaceAll(key, style);
	  }
	  return html;
  }
  


  public String getDomain() {
    return domain;
  }

  public void initOauth() {
    setupVerificationToken(OAUTH_TOKEN, SECURITY_TOKEN);

    if (this.domain.equals(SANDBOX_DOMAIN)) {
      setupOAuth(OAUTH_KEY, OAUTH_SECRET, SANDBOX_RPC_URL);
    }
    if (this.domain.equals(PREVIEW_DOMAIN)) {
      setupOAuth(OAUTH_KEY, OAUTH_SECRET, PREVIEW_RPC_URL);
    }

    setAllowUnsignedRequests(true);
  }

  public String getRpcServerUrl() {
    if (this.domain.equals(SANDBOX_DOMAIN)) {
      return SANDBOX_RPC_URL;
    }
    if (this.domain.equals(PREVIEW_DOMAIN)) {
      return PREVIEW_RPC_URL;
    }
    return null;
  }


  @Capability(contexts = {Context.SELF})
  @Override
  public void onWaveletSelfAdded(WaveletSelfAddedEvent event) {
	  String proxyFor = event.getBundle().getProxyingFor();
	  LOG.log(Level.INFO, "onWaveletSelfAdded proxyFor: " + proxyFor + ", wave title: " + event.getWavelet().getTitle() + ", waveId: " + event.getWavelet().getWaveId().toString());

	  Wavelet wavelet = event.getWavelet();
	  wavelet.setTitle("Create email [" + System.getProperty("APP_DOMAIN") + "]");
	  try {
		submit(wavelet, event.getBundle().getRpcServerUrl());
	} catch (IOException e) {
		LOG.log(Level.SEVERE, wavelet.getWaveId().toString(), e);
	}
  }

		
 public void recieveMail(String msgBody, String subject, Set<String> recipientsApp, String fromFullEmail, List<Attachment> attachmentsList, Date sentDate){
	//create new wave
	 
	 LOG.info("recieveMail: msgBody: " + msgBody + ", subject" + subject +  ", attachmentsList size: " + attachmentsList + ", fromFullEmail: " + fromFullEmail + ", recipientsApp: " + recipientsApp);
	 StringBuilder allRecipientsSB = new StringBuilder();
	
	 try{
		 msgBody = UnicodeString.convert(msgBody);
		 msgBody = Base64Coder.encodeString(msgBody);
		 //XXX - stupid but have to to
		 for(String rec : recipientsApp){
			 allRecipientsSB.append(rec + ","); 
		 }
		 allRecipientsSB.deleteCharAt(allRecipientsSB.lastIndexOf(","));
		 
		 for(String toWaveMailAddress : recipientsApp){
			 String toWaveAddress = MailUtils.mailId2waveId(toWaveMailAddress.toLowerCase());
			
			 if(!toWaveMailAddress.endsWith("@" + System.getProperty("APP_DOMAIN") + ".appspotmail.com"))
			 	continue;
			 
			 
			 String cleanSubject = MailUtils.cleanSubject(subject);
			//need to check if exists thread for this subject given recipient
			 EmailThread thread = emailThreadDao.getEmailThread4Wavemail(cleanSubject.hashCode(), toWaveMailAddress);
			 FullWaveAddress threadBlipAddress = null;
			 if(thread != null){
				 threadBlipAddress = new FullWaveAddress(thread.getDomain(), thread.getWaveId(), thread.getBlipId());
			 }
			 //check if this recipient already got the email
			 EmailEvent emailEvent = emailEventDao.getEmailEventByHash(subject.hashCode(),msgBody.hashCode(),sentDate );
			 FullWaveAddress newBlipFullAddress = null;
			 if(emailEvent == null){ 
				 //first time 
				 newBlipFullAddress = sendNewEmailToRecipient(msgBody, subject, fromFullEmail,toWaveMailAddress, toWaveAddress, allRecipientsSB.toString(),attachmentsList,threadBlipAddress);
				 //check if the bo
				 String fullWaveId = newBlipFullAddress.toFullWaveId();
				 
				 List<String> fromList = new ArrayList<String>();
				 fromList.add(fromFullEmail);
				 List<String> toList = new ArrayList<String>();
				 toList.add(toWaveMailAddress);
				 EmailEvent newEmailEvent = new EmailEvent("RECEIVE", subject, new Text(msgBody), fromList , toList, fromFullEmail, sentDate);
				 newEmailEvent.getFullWaveIdPerUserMap().put(toWaveMailAddress, fullWaveId);
				 emailEventDao.save(newEmailEvent);
				 
			 }else if(emailEvent != null && !emailEvent.getTo().contains(toWaveMailAddress)){
				 //email is not new - but this recipient didn't get it
				 newBlipFullAddress = sendNewEmailToRecipient(msgBody, subject, fromFullEmail,toWaveMailAddress, toWaveAddress,allRecipientsSB.toString(), attachmentsList,threadBlipAddress);
				 String fullWaveId = newBlipFullAddress.toFullWaveId();
				 emailEvent.getFullWaveIdPerUserMap().put(toWaveAddress, fullWaveId);
				 emailEventDao.save(emailEvent);
				 
			 }else if(emailEvent != null && emailEvent.getTo().contains(toWaveAddress)){
				 continue;
			 }
			 if(thread == null && threadBlipAddress == null){
				 thread = new EmailThread(cleanSubject.hashCode(), toWaveMailAddress, newBlipFullAddress);
				 emailThreadDao.save(thread);
			 }
		 }
	 }catch(Exception e){
		 LOG.log(Level.SEVERE, "", e);
	 }
	  
	 
	 
 }


private FullWaveAddress sendNewEmailToRecipient(String content, String subject,String fromFullEmail, String waveMailAddressRecipient,
				String waveAddressRecipient, String allRecipients,List<Attachment> attachmentsList, FullWaveAddress threadBlipAddress) {
	Wavelet threadWavelet = null;
	String fromMailStripped= MailUtils.stripRecipientForEmail(fromFullEmail);
	String proxyFor = fromMailStripped.replace("@", "-");
	if(threadBlipAddress == null){
		try {
			threadWavelet = newWave(domain, new LinkedHashSet<String>() ,"NEW_EMAIL_RECEIVED",proxyFor,getRpcServerUrl());
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "", e);
			throw new IllegalArgumentException(e.getMessage());
		}
	}else{
		//fetch wavelet
		threadWavelet = fetchWavelet(threadBlipAddress.getFullWaveId(), proxyFor);
	}
	
	FullWaveAddress newBlipFullAddress = null;
	
	StringBuilder subjectTitle = flattenToAscii(subject);
	threadWavelet.setTitle("Email: " + subjectTitle.toString());
	
	 subject = UnicodeString.convert(subject);
	 subject = Base64Coder.encodeString(subject);
	 //add participants to it
	//FIXME - each participant should get it's own gadget
	 threadWavelet.getParticipants().add(waveAddressRecipient);
	 threadWavelet.getParticipants().add(System.getProperty("APP_DOMAIN") + "@appspot.com");
	  
	  Blip blip = null;
	  if(threadBlipAddress == null){
		  blip = threadWavelet.getRootBlip(); 
		  newBlipFullAddress = new FullWaveAddress(threadWavelet.getWaveId().getDomain(), threadWavelet.getWaveId().getId(), blip.getBlipId());
		  appendMailGadget(blip,blip.getBlipId(),content,subject,fromFullEmail,waveMailAddressRecipient, allRecipients );
	  }else{
		  blip = threadWavelet.getBlip(threadBlipAddress.getBlipId()).continueThread();
	  }
	  
	 
	  LOG.info("Num of attachments: " + attachmentsList.size());
	  for(Attachment attachment : attachmentsList){
		  LOG.info("append attachment: " + attachment.getText());
		  blip.append(attachment);
	  }
	  
	 //submit wave
	  updateOnMailReceive(threadWavelet, ActivityType.RECEIVE, subject, fromMailStripped);
	  String blipId = null;
	  try {
		  List<JsonRpcResponse> jsonRpcResponseList = submit(threadWavelet, getRpcServerUrl());
		  if(!blip.isRoot()){
			  for(JsonRpcResponse jsonRpcResponse : jsonRpcResponseList){
				  if(jsonRpcResponse.getData().containsKey(ParamsProperty.NEW_BLIP_ID)){
					  blipId = String.valueOf(jsonRpcResponse.getData().get(ParamsProperty.NEW_BLIP_ID));
					  break;
				  }
			  }
			  appendMailGadget(blip,blipId,content,subject,fromFullEmail,waveMailAddressRecipient, allRecipients );
			  newBlipFullAddress = new FullWaveAddress(threadWavelet.getWaveId().getDomain(), threadWavelet.getWaveId().getId(), blipId);
			  submit(threadWavelet, getRpcServerUrl());
		  }
		  
	  } catch (Exception e) {
		  LOG.log(Level.WARNING, "Wavelet submition failed", e);
	  }
	  return newBlipFullAddress; //XXX return waveId and blipids
}



private StringBuilder flattenToAscii(String subject) {
	String tab = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=:,.!?-*&%@#$%^()`~;\\/'";
	  StringBuilder subjectTitle = new StringBuilder();
	  //make sure it contains only english letters
	 for(int i = 0; i < subject.length(); i++){
		  char ch = subject.charAt(i);
		  ch = ch =='\n' || ch == ' ' || tab.indexOf(ch) > -1 ? ch : '?';
		  subjectTitle.append(ch);
	  }
	return subjectTitle;
}



	
 
 
 private String convert2Utf8(String orig){
	 try{
		  byte[] utf8Bytes = orig.getBytes("UTF8");
		    String converted = new String(utf8Bytes, "UTF8");
		    return converted;
	  }catch(Exception e){
		  LOG.log(Level.SEVERE, orig, e);
	  }
	  return orig;
 }
 
 
	private void appendMailGadget(Blip blip,String blipId, String msgBody, String subject, String from, String to, String toAll) {
		String gadgetUrl = "http://" + System.getProperty("APP_DOMAIN") + ".appspot.com/wavemail/com.vegalabs.amail.client.WaveMailGadget.gadget.xml";
		LOG.info("appendMailGadget:  blipId: " + blip.getBlipId() + ", msgBody: " + msgBody);
		//check if blip already contains the add gadget. 
		BlipContentRefs gadgetRef = blip.first(ElementType.GADGET,Gadget.restrictByUrl(gadgetUrl));
		if(gadgetRef == null || Gadget.class.cast(gadgetRef.value()) == null){
			Gadget gadget = null;
			gadget = new Gadget(gadgetUrl);
			blip.at(blip.getContent().length()).insert(gadget);
			
			Map<String,String> out = new HashMap<String, String>();
		    out.put("msgBody", msgBody);
		    out.put("blipId", blipId);
		    out.put("subject", subject);
		    out.put("from", from);
		    out.put("toAll", toAll);
		    out.put("to", to);
		    out.put("mode", "READ");
		    
		    blip.first(ElementType.GADGET,Gadget.restrictByUrl(gadget.getUrl())).updateElement(out);
		}
		
	}
		



protected void submitWavelet(Wavelet wavelet) {
	 LOG.log(Level.INFO, "Entering submitWavelet");
	try {
		  submit(wavelet, getRpcServerUrl());
	  } catch (IOException e) {
//		  try{
//			  LOG.log(Level.SEVERE, "Trying to resubmit",e);
//			  submit(wavelet, getRpcServerUrl());
//		  }catch (Exception e1) {
//			  LOG.log(Level.SEVERE, "Failed after resubmitting!!!",e1);
//		}
		  LOG.log(Level.WARNING, "Wavelet submition failed", e);
	  }
}

  private boolean isNormalUser(String userId) {
    return userId != null && !(userId.endsWith("appspot.com") || userId.endsWith("gwave.com"));
  }

//  @Override
//  @Capability(contexts = {Context.ROOT, Context.SELF})
//  public void onBlipSubmitted(BlipSubmittedEvent event) {
//	  LOG.warning("Entering onBlipSubmitted");
//	  if(event.getBlip() == null || event.getBlip().getContent() == null || event.getBlip().getContent().length()  < 2){
//		  LOG.log(Level.FINE, "The content is not worthy, slipping processing");
//		  return;
//	  }
//	  // If this is from the "*-notify" proxy, skip processing.
//	  if (isNotifyProxy(event.getBundle().getProxyingFor())) {
//		  return;
//	  }
//
//
//	  // If this is unworthy wavelet (empty), skip processing.
//	  if (!isWorthy(event.getWavelet()) && event.getBlip().isRoot()) {
//		  event.getWavelet().setTitle(event.getBlip().getContent().split(" ")[0].replace("\n", ""));
//	  }else  if (!isWorthy(event.getWavelet())){
//		  return;
//	  }
//
//	  Wavelet wavelet = event.getWavelet();
//
//  }

public String makeBackStr(String forumName) {
	String back2digestWaveStr = "\nBack to \"" + forumName + "\" digest";
	return back2digestWaveStr;
}

  

  


  
  private boolean isWorthy(Wavelet wavelet) {
    return !wavelet.getTitle().trim().equals("");
  }

  

  private boolean isNotifyProxy(String proxyForString) {
    if (proxyForString != null && proxyForString.endsWith("-notify")) {
      return true;
    } else {
    	 LOG.log(Level.INFO, "proxyForString: " + proxyForString + " in isNotifyProxy");
      return false;
    }
  }


  private String getServerName() {
    return System.getProperty("APP_DOMAIN");
  }

  public String getRobotAvatarUrl() {
    return System.getProperty("DIGESTBOTTY_ICON_URL");
  }

  public String getRobotProfilePageUrl() {
	  String profileUrl = System.getProperty("PROFILE_URL");
	  return profileUrl;
  }

  @Override
  public String getRobotName() {
	  String baseName = getServerName().replace(".appspot.com", "");
    return baseName.replace("digestbotty", "DigestBotty");
  }
  
  @SuppressWarnings("unused")
@Override
  protected ParticipantProfile getCustomProfile(String name) {
	  LOG.fine("requested profile for: " + name);
	  ParticipantProfile profile = null;
	  Object o = cache.get(name);
	  profile = o != null ? ((SeriallizableParticipantProfile)o).getProfile() : null;
	  if(profile != null)
		  return profile;
	  LOG.warning("Not found profile for: " + name);
      if (1 == 2) {
    	  String digestWaveUrl = "https://wave.google.com/wave/waveref/googlewave.com/" ;
    	  profile = new ParticipantProfile("",
	    		  "" != null ? "" : "",
	    				  digestWaveUrl);
    	  
    	  // Put the value into the cache.
	      cache.put(name, new SeriallizableParticipantProfile(profile.getImageUrl(),profile.getName(), profile.getProfileUrl()));
	      return profile; 
      }else{
    	  LOG.warning("Not found even DB profile for: " + name);
    	  return new ParticipantProfile(getRobotName(),
    			  getRobotAvatarUrl(),
    			  getRobotProfilePageUrl());
      }
  }
  

  @RequestScoped
  private static class ServletHelper {
    private HttpServletRequest request = null;
    private HttpServletResponse response = null;

    @Inject
    public ServletHelper(HttpServletRequest request, HttpServletResponse response) {
      this.request = request;
      this.response = response;
    }

    public HttpServletRequest getRequest() {
      return this.request;
    }

    public HttpServletResponse getResponse() {
      return response;
    }
  }


  @Override
  public void onOperationError(OperationErrorEvent event) {
	  super.onOperationError(event);
	  LOG.severe(event.getMessage());
  }

//  @Override
//  @Capability(contexts = {Context.SELF})
//  public void onGadgetStateChanged(GadgetStateChangedEvent e) {
//	  LOG.log(Level.INFO, "entering OnGadgetStateChanged: ");
//	// If this is from the "*-digest" proxy, skip processing.
//	  boolean isDigestAdmin = true; //isDigestAdmin(e.getBundle().getProxyingFor());
//	  String[] gadgetUrls = createGadgetUrlsArr();
//	  JSONObject json = new JSONObject();
//	  Blip blip = e.getBlip();
//	  Gadget gadget = null;
//	  
//	  
////	  if (!isDigestAdmin) {
////	      LOG.info("onGadgetStateChanged: "  + e.getBundle().getProxyingFor() + " return!");
////	      return; //only gadget proxy allowed to react
////	    }else{
////	    	LOG.info("onGadgetStateChanged: "  + e.getBundle().getProxyingFor() + " process!");
////	    }
//	  LOG.info("onGadgetStateChanged: "  + e.getBundle().getProxyingFor() + " process!");
//	  int i = 0;
//	  String gadgetUrl = null;
//	  while(gadget == null && i < gadgetUrls.length){
//		  gadgetUrl = gadgetUrls[i];
//		  gadget = Gadget.class.cast(blip.first(ElementType.GADGET,Gadget.restrictByUrl(gadgetUrl)).value());
//		  if(gadget!=null){
//			  handleGadgetRequest(e, json, blip, gadget);
//		  }
//		  i++;
//	  }
//	  
//  }

public void handleGadgetRequest(GadgetStateChangedEvent e, JSONObject json,
		Blip blip, Gadget gadget) {
	try{
		  if(gadget!=null)
		  {
			  LOG.log(Level.INFO, "entering handleGadgetRequest: " + gadget.getUrl());
			  Set<String> keys = new LinkedHashSet<String>( gadget.getProperties().keySet());
			  for(String key : keys){
				  String[] split = key.split("#");
				  String postBody = gadget.getProperty(key);
				  if(split != null && split.length == 3 && split[0].equalsIgnoreCase("request") && postBody != null){
					  String responseKey = "response#" + split[1] + "#" +  split[2];
					  LOG.info("Found request: " + key + ", body: " + postBody);
					  Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
					  JsonRpcRequest jsonRpcRequest = gson.fromJson(postBody, JsonRpcRequest.class);
					  if (jsonRpcRequest != null) {
						  String method = jsonRpcRequest.getMethod();
						  if (method != null) {
							  LOG.info("processing method " + method);
							  Class<? extends Command> commandClass = com.vegalabs.amail.server.admin.CommandType.valueOfIngoreCase(method).getClazz();
							  Command command = injector.getInstance(commandClass);
							  jsonRpcRequest.getParams().put("senderId", e.getModifiedBy());
							  String projectId = e.getBundle().getProxyingFor();
							  jsonRpcRequest.getParams().put("projectId", projectId);
							  LOG.info("sender is: " + e.getModifiedBy());
							  command.setParams(jsonRpcRequest.getParams());
							  try{
								  try {
									  json = command.execute();
								  } catch (JSONException e1) {
									  json.put("error", e1.getMessage());
								  } catch (IllegalArgumentException e2) {
									  json.put("error", e2.getMessage());
								  }
							  }catch(JSONException e3){
								  json.put("error", e3.getMessage());
								  LOG.severe(e3.getMessage());
							  }
							  Map<String,String> out = new HashMap<String, String>();
							  out.put(key, null);
							  if(!split[1].equals("none")){ //if none - no reply needed
								  out.put(responseKey, json.toString());
							  }
							  blip.first(ElementType.GADGET,Gadget.restrictByUrl(gadget.getUrl())).updateElement(out);
						  }
					  }
				  }
			  }
		  }else{
			  LOG.log(Level.WARNING, "\nGadget is null: ");
		  }
	  }catch(Exception e4){
		  
		  StringWriter sw = new StringWriter();
		  PrintWriter pw = new PrintWriter(sw);
		  e4.printStackTrace(pw);
		  e.getWavelet().reply("\n" + "EXCEPTION !!!" + sw.toString() + " : " + e4.getMessage());
		  LOG.severe(sw.toString());
	  }
}

protected String[] createGadgetUrlsArr() {
	String gadgetUrl1 = "http://" + System.getProperty("APP_DOMAIN") + ".appspot.com/digestbottygadget/com.aggfi.digest.client.DigestBottyGadget.gadget.xml";
	  String gadgetUrl2 = System.getProperty("READONLYPOST_GADGET_URL");
	  String gadgetUrl3 = "http://vegalabs.appspot.com/tracker.xml";
	  String[] gadgetUrls = {gadgetUrl1, gadgetUrl2,gadgetUrl3};
	return gadgetUrls;
}




	public List<BundledAnnotation> createToAnnotation(Blip blipToUpdate, String blipId, String linkToAnnonationName) {
		String blipRef = "waveid://" + blipToUpdate.getWaveId().getDomain() + "/" + blipToUpdate.getWaveId().getId() + "/~/conv+root/" + blipId;
		  List<BundledAnnotation> baList = BundledAnnotation.listOf("link/manual",blipRef,"style/fontSize", "8pt",linkToAnnonationName,"done");
		return baList;
	}


	public void removeViewsTrackingGadget(Blip blip, String projectId) {
		LOG.info("removeViewsTrackingGadget: " + projectId + ", blipId: " + blip.getBlipId());
		String gadgetUrl = "http://" + System.getProperty("APP_DOMAIN") + ".appspot.com/tracker.xml";
		//check if blip already contains the add gadget. 
		Gadget gadget = null;
		BlipContentRefs gadgetRef = blip.first(ElementType.GADGET,Gadget.restrictByUrl(gadgetUrl));
		if(gadgetRef != null){
			gadget = Gadget.class.cast(gadgetRef.value());
			if(gadget != null){
			   blip.first(ElementType.GADGET,Gadget.restrictByUrl(gadget.getUrl())).delete();
			   gadgetRef = blip.first(ElementType.GADGET,Gadget.restrictByUrl(gadgetUrl));
			   if(gadgetRef != null){
				   gadget = Gadget.class.cast(gadgetRef.value());
				   if(gadget != null){
					   LOG.warning("Failed to remove gadget!!! blipId: " + blip + ", projectId: " + projectId );
				   }else{
					   LOG.info("Removed gadget!!! blipId: " + blip + ", projectId: " + projectId );
				   }
			   }else{
				   LOG.info("Removed gadget!!! blipId: " + blip + ", projectId: " + projectId );
			   }
			   
			 
			}
		}
		
	}
	
	public String updateOnEmailSent(Wavelet wavelet, int activityType, String recipients, String subject) {
		
		subject = flattenToAscii(subject).toString();
		String appdomain = System.getProperty("APP_DOMAIN");
		Date today = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM-dd");
		SimpleDateFormat detailedFormat = new SimpleDateFormat("dd-MMM HH:mm:ss");
		String todayStr = dateFormat.format(today);
		//fetch the wavelet
		Tags tags = wavelet.getTags();
		
		String activityTypeStr = null;
		//tag to indicate this is email
		if(!tags.contains(appdomain))
			tags.add(appdomain);
		
		String[] recArr = recipients.split(",");
		//if is reply add "replyto:<email>"  update "email.recieved.replied.no" -> "email.recieved.replied.<email>"
		if(activityType == ActivityType.REPLY){
			activityTypeStr = "REPLY";
			//indicate that email was replied
			if(!tags.contains("email.activity=reply"))
				tags.add("email.activity=reply");
			for(String recipient : recArr){
				String strippedRecpient = MailUtils.stripRecipientForEmail(recipient);
				if(!tags.contains("email.replied.to=" + recipient))
					tags.add("email.replied.to=" + strippedRecpient);
			}

			String repliedWhenStr = "email.replied.when=" + todayStr;
			if(!tags.contains(repliedWhenStr))
				tags.add(repliedWhenStr);
			
			wavelet.setTitle(subject + " [ " + detailedFormat.format(today) + " ] ");
		}
		//if send new - 
		//		add "to:<email>"
		//		email.sent
		else if(activityType == ActivityType.NEW){
			activityTypeStr = "NEW";
			if(!tags.contains("email.activity=new"))
				tags.add("email.activity=new");
			for(String recipient : recArr){
				String strippedRecpient = MailUtils.stripRecipientForEmail(recipient);
				if(!tags.contains("email.sent.to=" + recipient))
					tags.add("email.sent.to=" + strippedRecpient);
			}
			String sentWhenStr = "email.sent.when=" + todayStr;
			if(!tags.contains(sentWhenStr))
				tags.add(sentWhenStr);
			wavelet.setTitle(subject + " [Sent: " + detailedFormat.format(today) + " ] ");

		}else if(activityType == ActivityType.FORWARD){
			activityTypeStr = "FORWARD";
			if(!tags.contains("email.activity=forward"))
				tags.add("email.activity=forward");
			for(String recipient : recArr){
				String strippedRecpient = MailUtils.stripRecipientForEmail(recipient);
				if(!tags.contains("email.forwared.to=" + recipient))
					tags.add("email.forwared.to=" + strippedRecpient);
			}
			String forwardedWhenStr = "email.forwarded.when=" + todayStr;
			if(!tags.contains(forwardedWhenStr))
				tags.add(forwardedWhenStr);
			
			wavelet.setTitle( subject + " [" + detailedFormat.format(today) + " ] ");
		}
		
		
		try {
			submit(wavelet, getRpcServerUrl());
		} catch (IOException e) {
			LOG.log(Level.SEVERE, wavelet.getWaveId().toString(), e);
		}
		
		 return activityTypeStr;
	}
	
	public void updateOnMailReceive(Wavelet wavelet, int activityType, String subject, String sender){
		String appdomain = System.getProperty("APP_DOMAIN");
		Date today = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM-dd");
		SimpleDateFormat detailedFormat = new SimpleDateFormat("dd-MMM HH:mm:ss");
		String todayStr = dateFormat.format(today);
		//fetch the wavelet
		Tags tags = wavelet.getTags();
		//tag to indicate this is email
		if(!tags.contains(appdomain))
			tags.add(appdomain);
		
		if(activityType == ActivityType.RECEIVE){
			if(!tags.contains("email.activity=receive"))
				tags.add("email.activity=receive");
			
			String strippedSender = MailUtils.stripRecipientForEmail(sender);
			if(!tags.contains("email.received.from=" + strippedSender))
				tags.add("email.received.from=" + strippedSender); //XXX theoretically should be in loop
			String receivedWhenStr = "email.received.when=" + todayStr;
			if(!tags.contains(receivedWhenStr))
				tags.add(receivedWhenStr);
			
		}
	}


	
	 public Wavelet fetchWavelet(String waveId, String proxyFor){
		 String domain = waveId.split("!")[0];
		 String id = waveId.split("!")[1];
		 Wavelet wavelet = null;
		 try{
			 wavelet = fetchWavelet( new WaveId(domain, id), new WaveletId(domain, "conv+root"), proxyFor,  getRpcServerUrl());
		  }catch (IOException e) {
			  LOG.log(Level.INFO,"can happen if the robot was removed manually from the wave or if timeout. waveId: " + waveId + ", proxyFor: " + proxyFor,e);
		  }
		 return wavelet;
	 }

	

	
}