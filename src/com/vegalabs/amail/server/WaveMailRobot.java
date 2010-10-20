package com.vegalabs.amail.server;

import it.sauronsoftware.base64.Base64; 

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManagerFactory;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jgravatar.Gravatar;
import jgravatar.GravatarDefaultImage;
import jgravatar.GravatarRating;

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
import com.google.wave.api.event.GadgetStateChangedEvent;
import com.google.wave.api.event.OperationErrorEvent;
import com.google.wave.api.event.WaveletSelfAddedEvent;
import com.google.wave.api.impl.DocumentModifyAction.BundledAnnotation;
import com.vegalabs.amail.server.dao.EmailEventDao;
import com.vegalabs.amail.server.dao.EmailFailedEventDao;
import com.vegalabs.amail.server.dao.EmailThreadDao;
import com.vegalabs.amail.server.dao.PersonDao;
import com.vegalabs.amail.server.dao.SeriallizableParticipantProfileDao;
import com.vegalabs.amail.server.data.FullWaveAddress;
import com.vegalabs.amail.server.model.EmailEvent;
import com.vegalabs.amail.server.model.EmailFailedEvent;
import com.vegalabs.amail.server.model.EmailThread;
import com.vegalabs.amail.server.model.Person;
import com.vegalabs.amail.server.model.SeriallizableParticipantProfile;
import com.vegalabs.amail.server.utils.MailUtils;
import com.vegalabs.general.server.command.Command;
import com.vegalabs.general.server.rpc.JsonRpcRequest;
import com.vegalabs.general.server.rpc.util.Util;
import com.vegalabs.amail.shared.ActivityType;
import com.vegalabs.amail.shared.UnicodeString;

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
  private PersonDao personDao;
  private SeriallizableParticipantProfileDao sppDao;
  private EmailFailedEventDao  emailFailedEventDao;

  @Inject
  public WaveMailRobot(Injector injector, Util util, EmailEventDao emailEventDao, EmailThreadDao emailThreadDao, PersonDao personDao,
		  SeriallizableParticipantProfileDao sppDao, EmailFailedEventDao  emailFailedEventDao) {
    this.injector = injector;
    this.util = util;
    this.emailEventDao = emailEventDao;
    this.emailThreadDao = emailThreadDao;
    this.personDao = personDao;
    this.sppDao = sppDao;
    this.emailFailedEventDao = emailFailedEventDao;

    OAUTH_TOKEN = System.getProperty("OAUTH_TOKEN");
    OAUTH_KEY = System.getProperty("OAUTH_KEY");
    OAUTH_SECRET = System.getProperty("OAUTH_SECRET");
    SECURITY_TOKEN = System.getProperty("SECURITY_TOKEN");
    DIGEST_WAVE_DOMAIN = System.getProperty("DIGEST_WAVE_DOMAIN");
    DIGEST_WAVE_ID = System.getProperty("DIGEST_WAVE_ID");
    initOauth();
    try {
    	Map<String, Integer> props = new HashMap<String, Integer>();
        props.put(GCacheFactory.EXPIRATION_DELTA, 60*60);
        cache = CacheManager.getInstance().getCacheFactory().createCache(props);
    } catch (CacheException e) {
        LOG.log(Level.SEVERE,"cache init",e);
    }

  }
  
  private static Map<String,String> fixStylesMap = new HashMap<String, String>();

public static final String GADGET_URL = "http://" + System.getProperty("APP_DOMAIN") + ".appspot.com/wavemail/com.vegalabs.amail.client.WaveMailGadget.gadget.xml"; 
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
    }else{
    	LOG.severe("Are you crazy? domain should be googlewave.com!!!");
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
	  LOG.log(Level.INFO, "onWaveletSelfAdded proxyFor: " + proxyFor + ", wave title: " + event.getWavelet().getTitle() + ", waveId: " + event.getWavelet().getWaveId().toString() + ", blipId: " + event.getBlip().getBlipId() + ", isRoot: " + event.getBlip().isRoot());

	  updateWaveletOnEmailSent(event.getWavelet(),-1, "", "");//just tag as email
	  Wavelet wavelet = event.getWavelet();
	 
	  wavelet.setTitle("Create Wavy eMail [" + System.getProperty("APP_DOMAIN") + "]");
	  LOG.info("Modified by: " + event.getModifiedBy());
	  //fetch the wavelet
	  if(event.getBlip().isRoot()){
		  appendMailGadget(event.getBlip(), event.getBlip().getBlipId(), "", "", "", "", event.getModifiedBy(),"", "NEW");
	  }else{
		  appendAttachmentInstructions(event.getBlip());
	  }
  }

		
 public void recieveMail(String msgBody,String subject, String realRecipient,  Set<String> recipientsApp,Set<String> ccApp,Set<String> bccApp, String fromFullEmail, List<Attachment> attachmentsList, Date sentDate){
	//create new wave
	 
	 LOG.fine("recieveMail: msgBody: " + msgBody + ", subject" + subject +  ", attachmentsList size: " + attachmentsList + ", fromFullEmail: " + fromFullEmail + ", recipientsApp: " + recipientsApp);
	 
	 String onlyMail = MailUtils.stripRecipientForEmail(fromFullEmail);
 	 String onlyName = MailUtils.stripRecipientForName(fromFullEmail);
 	if(onlyName != null){
		//let's check if if have this profile
		updateCreateProfile(onlyMail,onlyName);
	}
 	
 	//attach the HTML content as attachment - I will use it as source for frame that will display the content in gadget //TODO
 	Attachment contentAttachment;
	try {
//		contentAttachment = createStrAttachment(WaveMailRobot.filterNonISO(subject).trim() + ".html", msgBody.toString());
//		if(contentAttachment != null){
//			attachmentsList.add( contentAttachment);
//		}
	} catch (Exception e1) {
		LOG.warning("Failed to create html content attachment : " + WaveMailRobot.filterNonISO(subject).trim());
	}
		
	handleDeliveryToOne(fromFullEmail, realRecipient, recipientsApp,ccApp, subject, msgBody, sentDate, attachmentsList);
 }
 
 
 private void handleDeliveryToOne(String fromFullEmail, String toWaveMailAddress, Set<String> recipientsApp,Set<String> ccApp,String subject, String msgBody, Date sentDate, List<Attachment> attachmentsList){
	 if(!toWaveMailAddress.endsWith("@" + System.getProperty("APP_DOMAIN") + ".appspotmail.com"))
		 	return;
	 toWaveMailAddress = toWaveMailAddress.toLowerCase().trim();
		 
		 String toWaveAddress = MailUtils.mailId2waveId(toWaveMailAddress);
		 //get person
		 Person person = null;
		 person = personDao.getPerson(toWaveMailAddress);
		 if(person == null){
			 person = new Person(toWaveMailAddress);
			 personDao.save(person);
		 }
		 
		 //check if this recipient already got the email
		 EmailEvent emailEvent = emailEventDao.getEmailEventByHash(subject.hashCode(),msgBody.hashCode(),sentDate );
		 if(emailEvent == null){ 
			 //first time 
			 List<String> fromList = new ArrayList<String>();
			 fromList.add(fromFullEmail);
			 List<String> toList = new ArrayList<String>();
			 for(String rec : recipientsApp){
				 toList.add(rec);
			 }
			 List<String> ccList = new ArrayList<String>();
			 for(String rec : ccApp){
				 ccList.add(rec);
			 }
			
			 Text dbText = new Text(msgBody);
			 EmailEvent newEmailEvent = new EmailEvent("RECEIVE", subject, dbText, fromList , toList,ccList, fromFullEmail, sentDate,attachmentsList);
			 newEmailEvent = emailEventDao.save(newEmailEvent);
			 
			 
			 //---------------
			 receiveEmailWithRetry(person,newEmailEvent, null);
		 }else if(emailEvent != null && !emailEvent.getTo().contains(toWaveMailAddress)){
			 //email is not new - but this recipient didn't get it
			 receiveEmailWithRetry(person,emailEvent, null);
		 }else if(emailEvent != null && emailEvent.getTo().contains(toWaveAddress)){
			 return;
		 }
 }

 private Attachment createStrAttachment(String fileName, String htmlContent) throws Exception {
		LOG.log(Level.INFO, "creating str attachment: " + fileName);
		htmlContent = "<html><head><meta charset=\"UTF-8\"></head><body>" + htmlContent + "</body></html>";
		Attachment attachment = null;
		byte[] bytes = htmlContent.getBytes("UTF-8");
		
		if(bytes != null){
			Map<String,String> properties = new HashMap<String,String>();
			properties.put(Attachment.CAPTION, fileName);
			properties.put(Attachment.MIME_TYPE, "text/html");
			
			attachment = new Attachment(properties,bytes);
		}else{
			LOG.log(Level.WARNING, "bytes are null!!! " + fileName);
		}
		return attachment;
	}


public void receiveEmailWithRetry(Person person, EmailEvent newEmailEvent, EmailFailedEvent emailFailedEvent) {
	FullWaveAddress newBlipFullAddress;
	//need to check if exists thread for this subject given recipient
	 String cleanSubject = MailUtils.cleanSubject(newEmailEvent.getSubject());
	 
	 EmailThread thread = emailThreadDao.getEmailThread4Wavemail(cleanSubject.hashCode(), person.getWavemail().toLowerCase());
	 FullWaveAddress threadBlipAddress = null;
	 if(thread != null){
		 threadBlipAddress = new FullWaveAddress(thread.getDomain(), thread.getWaveId(), thread.getBlipId());
	 }
	try {
		newBlipFullAddress = sendNewEmailToRecipient(newEmailEvent.getMsgBody().getValue(), newEmailEvent.getSubject(), newEmailEvent.getSource(),
				 person.getWavemail(), newEmailEvent.getTo(),newEmailEvent.getCc(),newEmailEvent.getAttachments(),threadBlipAddress);
//		newBlipFullAddress = sendNewEmailToRecipientVer2(person.getId().toString(), newEmailEvent.getId().toString(), newEmailEvent.getSubject(), newEmailEvent.getSource(),
//				 person.getWavemail(), MailUtils.mailId2waveId(person.getWavemail().toLowerCase()), newEmailEvent.getTo(),newEmailEvent.getAttachments(),threadBlipAddress);
		 //check if the bo
		 String fullWaveId = newBlipFullAddress.toFullWaveId();
		 newEmailEvent.getFullWaveIdPerUserMap().put(person.getWavemail().toLowerCase(), fullWaveId);
		 emailEventDao.save(newEmailEvent);
		 if( thread == null){
			 thread = new EmailThread(cleanSubject.hashCode(), person.getWavemail().toLowerCase(), newBlipFullAddress);
		 }else{
			 thread.setBlipsCount(thread.getBlipsCount() + 1);
		 }
		 emailThreadDao.save(thread);
	} 
	catch (Exception e) {
		 LOG.log(Level.SEVERE, "EmailEvent id: " + newEmailEvent.getId() + ", person: " + person.toString() + ", EmailEvent: " + newEmailEvent.toString(), e);
		 // TODO schedule to receive again or send bounce
		 StringWriter exceptionStackTraceWriter = new StringWriter();
		 e.printStackTrace(new PrintWriter(exceptionStackTraceWriter));
		 if(emailFailedEvent == null){
			 emailFailedEvent = new EmailFailedEvent(person.getWavemail(), person.getId(), newEmailEvent.getId(), e.getMessage(), new Text(exceptionStackTraceWriter.toString()));
			 emailFailedEvent.setAction("RECEIVE");
			 emailFailedEventDao.save(emailFailedEvent);
		 }else{
			 emailFailedEvent.setRetryCount(emailFailedEvent.getRetryCount() + 1);
			 emailFailedEvent.setExceptionMsg(e.getMessage());
			 emailFailedEvent.setExceptionStackTrace(new Text(exceptionStackTraceWriter.toString()));
			 emailFailedEvent.setLastUpdated(new Date());
			 emailFailedEventDao.save(emailFailedEvent);
		 }
		 return;
	}
	if(emailFailedEvent != null){
		emailFailedEvent.setRetryCount(emailFailedEvent.getRetryCount() + 1);
		emailFailedEvent.setStatus("SUCCESS");
		emailFailedEvent.setLastUpdated(new Date());
		emailFailedEventDao.save(emailFailedEvent);
	}
}



public StringBuilder buildAllRecipients(Iterable<String> recipientsApp, String prefix) {
	StringBuilder allRecipientsSB = new StringBuilder();
	 for(String rec : recipientsApp){
		 allRecipientsSB.append("[" + prefix + rec + "]" + ","); 
	 }
	 if(allRecipientsSB.lastIndexOf(",") != -1)
		 allRecipientsSB.deleteCharAt(allRecipientsSB.lastIndexOf(","));
	return allRecipientsSB;
}


private FullWaveAddress sendNewEmailToRecipient(String content, String subject,String fromFullEmail, String waveMailAddressRecipient,
		List<String> toList,List<String> ccList,List<Attachment> attachmentsList, FullWaveAddress threadBlipAddress) throws IOException {
	String waveAddressRecipient = MailUtils.mailId2waveId(waveMailAddressRecipient.toLowerCase());
	String allRecipients = buildAllRecipients(toList, "To:").toString();
	allRecipients += buildAllRecipients(ccList, "Cc:").toString();
	Wavelet threadWavelet = null;
	fromFullEmail = MailUtils.decodeEmailAddress(fromFullEmail);
	String fromMailStripped= MailUtils.stripRecipientForEmail(filterNonASCII(fromFullEmail));
	if(!fromMailStripped.contains("@")){
		LOG.severe("fromMailStripped: " + fromMailStripped + ", fromFullEmail: " + fromFullEmail);
	}
	String proxyFor = fromMailStripped.replace("@", "-").replace("<", "").replace(">", "").trim();
	proxyFor = removeWordInParenthesis(proxyFor);
	LOG.info("proxyFor: " + proxyFor + ", fromFullEmail: " + fromFullEmail);
	if(threadBlipAddress == null){
		threadWavelet = newWave(domain, new LinkedHashSet<String>() ,"NEW_EMAIL_RECEIVED",proxyFor,getRpcServerUrl());
	}else{
		//fetch wavelet
		threadWavelet = fetchWavelet(threadBlipAddress.getFullWaveId(), null);
		if(!threadWavelet.getParticipants().contains(proxyFor)){
			threadWavelet.getParticipants().add(System.getProperty("APP_DOMAIN") + "+" + proxyFor + "@appspot.com");
		}
	}
	
	FullWaveAddress newBlipFullAddress = null;
	String subjectTitle = subject;
	subjectTitle = filterNonASCII(subject);
//	subjectTitle = MailUtils.toUTF8(subjectTitle, null);
	threadWavelet.setTitle("eMail: " + subjectTitle);
	 
	 //add participants to it
	 threadWavelet.getParticipants().add(waveAddressRecipient);
	 threadWavelet.getParticipants().add(System.getProperty("APP_DOMAIN") + "@appspot.com");
	 
	  String blipId = null;
	  Blip blip = null;
	  if(threadBlipAddress == null){
		  blip = threadWavelet.getRootBlip(); 
		  blipId = threadWavelet.getRootBlipId();
		  newBlipFullAddress = new FullWaveAddress(threadWavelet.getWaveId().getDomain(), threadWavelet.getWaveId().getId(), blip.getBlipId());
		  appendMailGadget(blip,blipId,content,subject,fromFullEmail,waveMailAddressRecipient,waveAddressRecipient, allRecipients,"READ" );
		  
	  }else{
		  blip = threadWavelet.getBlip(threadBlipAddress.getBlipId()).continueThread();
	  }
	  
	 
	 //submit wave
	
	  List<JsonRpcResponse> jsonRpcResponseList = null;
	  jsonRpcResponseList = submit(threadWavelet, getRpcServerUrl());
	 
	  if(!blip.isRoot()){
		  for(JsonRpcResponse jsonRpcResponse : jsonRpcResponseList){
			  if(jsonRpcResponse.getData().containsKey(ParamsProperty.NEW_BLIP_ID)){
				  blipId = String.valueOf(jsonRpcResponse.getData().get(ParamsProperty.NEW_BLIP_ID));
				  break;
			  }
		  }
		  appendMailGadget(blip,blipId,content,subject,fromFullEmail,waveMailAddressRecipient,waveAddressRecipient, allRecipients ,"READ" );
		  newBlipFullAddress = new FullWaveAddress(threadWavelet.getWaveId().getDomain(), threadWavelet.getWaveId().getId(), blipId);
	  }
	  updateOnMailReceive(threadWavelet, ActivityType.RECEIVE, subject, fromMailStripped);
	  appendAttachments(attachmentsList, blip);
	  submit(threadWavelet, getRpcServerUrl());
	  return newBlipFullAddress;
}




private String removeWordInParenthesis(String proxyFor) {
	String out = proxyFor;
	if(out.contains("(")){
		int start = out.indexOf("(");
		out = out.substring(0,start );
	}
	return out;
}



private void appendAttachments(List<Attachment> attachmentsList, Blip blip) {
	LOG.info("Num of attachments: " + attachmentsList.size());
		for(Attachment attachment : attachmentsList){
			byte[] data = attachment.getData();
			int dataSize = data != null ? data.length : 0;
			LOG.fine("append attachment: " + attachment.getCaption() + ", size: " + dataSize + " bytes");
			blip.append(attachment);
		}
}




public String decode(String str2Decode) {
	String out = str2Decode;
	out = UnicodeString.deconvert(str2Decode);
	LOG.fine("in decode, before: " + str2Decode );
	LOG.fine("in decode, after: " + out );
	return out;
}

public String encode(String str2Encode) {
	String out = str2Encode;
	out = UnicodeString.convert(out);
	LOG.fine("in utf-8 encode, before: " + str2Encode );
	LOG.fine("in encode, after: " + out );
	
	return out;
}
public Map<String,String> encode(Map<String,String> map2Enc){
	for(String key : map2Enc.keySet()){
		String value = map2Enc.get(key);
		value = encode(value);
		map2Enc.put(key,value);
		LOG.fine("key: " + key);
	}
	return map2Enc;
}




public static String filterNonISO(String inString) {
	// Create the encoder and decoder for the character encoding
	Charset charset = Charset.forName("ISO-8859-1");
	CharsetDecoder decoder = charset.newDecoder();
	CharsetEncoder encoder = charset.newEncoder();
	// This line is the key to removing "unmappable" characters.
	encoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
	String result = inString;

	try {
		// Convert a string to bytes in a ByteBuffer
		ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(inString));

		// Convert bytes in a ByteBuffer to a character ByteBuffer and then to a string.
		CharBuffer cbuf = decoder.decode(bbuf);
		result = cbuf.toString();
	} catch (CharacterCodingException cce) {
		String errorMessage = "Exception during character encoding/decoding: " + cce.getMessage();
		LOG.log(Level.FINE,errorMessage, cce);
	}

	return result;	
}
public static String filterNonASCII(String inString) {
	// Create the encoder and decoder for the character encoding
	Charset charset = Charset.forName("US-ASCII");
	CharsetDecoder decoder = charset.newDecoder();
	CharsetEncoder encoder = charset.newEncoder();
	// This line is the key to removing "unmappable" characters.
	encoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
	String result = inString;

	try {
		// Convert a string to bytes in a ByteBuffer
		ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(inString));

		// Convert bytes in a ByteBuffer to a character ByteBuffer and then to a string.
		CharBuffer cbuf = decoder.decode(bbuf);
		result = cbuf.toString();
	} catch (CharacterCodingException cce) {
		String errorMessage = "Exception during character encoding/decoding: " + cce.getMessage();
		LOG.log(Level.SEVERE,errorMessage, cce);
	}

	return result;	
}




 
 private void appendMailGadget(Blip blip, String blipId,String msgBody, String subject, String from, String to,String waveUserId, String toAll, String mode) {
		LOG.fine("appendMailGadget:  blipId: " + blipId + ", subject: " + subject);
		//check if blip already contains the add gadget. 
		Gadget gadget = extractGadgetFromBlip(GADGET_URL,blip);
		if(gadget == null){
			if(to == null || "".equals(to)){
				to = MailUtils.waveId2mailId(waveUserId);
			}
			String contacts = retrContacts4User(to);
			
			gadget = new Gadget(GADGET_URL);
			blip.at(blip.getContent().length()).insert(gadget);
			
			Map<String,String> out = new HashMap<String, String>();
		    out.put("msgBody", msgBody);
		    out.put("blipId", blipId);
		    out.put("subject", subject);
		    out.put("from", from);
		    out.put("toAll", toAll);
		    out.put("to", to);
		    out.put("contacts#" + waveUserId, contacts);
		    encode(out);
		    
		    out.put("mode", mode);
		    updateGadgetState(blip, GADGET_URL, out);
		    appendAttachmentInstructions(blip);
		    
		}
		
	}



public void appendAttachmentInstructions(Blip blip) {
	List<BundledAnnotation> baList = BundledAnnotation.listOf("style/fontSize", "8pt");
	blip.at(blip.getContent().length()).insert(baList,"\nTo send attachments - jus attach the file/s below, i.e. switch to \"Edit\" mode, and then attach files to the blip by clicking on the staple icon on the wave's top toolsbar. Please note to set appropriate file extensions, i.e. myimage.jpg." + 
			"\nAlso note - maximum email size is 1MB due to Google AppEngine restrictions." + 
			" You can check ");
	List<BundledAnnotation> baList1 = BundledAnnotation.listOf("style/fontSize", "8pt", "link/manual", "http://code.google.com/appengine/docs/python/mail/overview.html#Attachments");
	blip.at(blip.getContent().length()).insert(baList1,"here");
	blip.at(blip.getContent().length()).insert(baList," list of attachment types that are allowed by AppEngine\n");
}
	
	public void updateGadgetState(Blip blip, String gadgetUrl,Map<String, String> out) {
		Gadget gadget = extractGadgetFromBlip(gadgetUrl,blip);
		gadget.getProperties().putAll(out);
		blip.first(ElementType.GADGET,Gadget.restrictByUrl(gadget.getUrl())).updateElement(gadget.getProperties());
	}
	
	private Gadget extractGadgetFromBlip(String gadgetUrl, Blip blip){
		BlipContentRefs gadgetRef = blip.first(ElementType.GADGET,Gadget.restrictByUrl(gadgetUrl));
		if(gadgetRef == null || Gadget.class.cast(gadgetRef.value()) == null){
			return null;
		}else{
			return Gadget.class.cast(gadgetRef.value());
		}
	}



	public String retrContacts4User(String to) {
		//load contacts
		Person person = personDao.getPerson(MailUtils.stripRecipientForEmail(to));
		if(person == null){
			LOG.warning("No person for wavemail: " + to);
		}
		StringBuilder contactsSb = concatinateContacts(person);
		return contactsSb.toString();
	}



	public StringBuilder concatinateContacts(Person person) {
		StringBuilder contactsSb = new StringBuilder();
		if(person != null && person.getContacts() != null){
			for(String key : person.getContacts().keySet()){
				String value = person.getContacts().get(key);
				if(!value.equals(key)){
					contactsSb.append(value + "#" + key + "#");
				}else{
					contactsSb.append(value + "#");
				}
			}
		}
		return contactsSb;
	}
	
	public String retrContacts4User(Person person) {
		StringBuilder contactsSb = concatinateContacts(person);
		return contactsSb.toString();
	}
		



protected void submitWavelet(Wavelet wavelet) {
	 LOG.log(Level.INFO, "Entering submitWavelet");
	try {
		  submit(wavelet, getRpcServerUrl());
	  } catch (IOException e) {
		  LOG.log(Level.WARNING, "Wavelet submition failed", e);
	  }
}


public String makeBackStr(String forumName) {
	String back2digestWaveStr = "\nBack to \"" + forumName + "\" digest";
	return back2digestWaveStr;
}


  public String getRobotAvatarUrl() {
	  String robotIconUrl = System.getProperty("ROBOT_ICON_URL");
	  LOG.info("in getRobotAvatarUrl name: " + robotIconUrl);
    return robotIconUrl; 
  }

  public String getRobotProfilePageUrl() {
	  String profileUrl = System.getProperty("PROFILE_URL");
	  LOG.info("RobotProfilePageUrl name: " + profileUrl);
	  return profileUrl;
  }

  @Override
  public String getRobotName() {
	  String baseName = "Wavy eMail beta";
	  LOG.info("in getRobotName name: " + baseName);
    return baseName;
  }
  
  @SuppressWarnings("unused")
@Override
  protected ParticipantProfile getCustomProfile(String name) {
	  LOG.fine("requested profile for: " + name);
	  ParticipantProfile profile = null;
	  Object o = cache.get("SeriallizableParticipantProfile1#"+name);
	  profile = o != null ? ((SeriallizableParticipantProfile)o).getProfile() : null;
	  if(profile != null){
		  if(profile.getImageUrl() == null || "".equals(profile.getImageUrl()) ){
			  Gravatar gravatar = new Gravatar();
			  String email = switch2email(name);
			  if(email != null){
				  updateProfileWithGravatar(email,((SeriallizableParticipantProfile)o));
				  profile = ((SeriallizableParticipantProfile)o).getProfile();
			  }
		  }
		  LOG.fine("Found profile in cache: " + printProfile(profile));
		  return profile;
	  }
	  LOG.fine("Not found profile in cache for: " + name);
	 
	 
	  
	  
	  String email = switch2email(name);
	  //now let's check in DB
	  if(email != null){
		  SeriallizableParticipantProfile dbProfile = sppDao.getSeriallizableParticipantProfile(email);
		  LOG.fine("Found profile in DB: " + dbProfile);
		  if(dbProfile != null){
			  if(dbProfile.getImageUrl() == null){ 
				  updateProfileWithGravatar( email,dbProfile);
				  sppDao.save(dbProfile);
				  LOG.info("Found profile in DB: " + printProfile(dbProfile.getProfile()));
				  cache.put("SeriallizableParticipantProfile1#"+name, dbProfile);
			  }
			  
			  return dbProfile.getProfile();
		  }else{
			  //no profile at all - create
			  SeriallizableParticipantProfile newProfile = new SeriallizableParticipantProfile(null,email,null, null);
			  updateProfileWithGravatar( email,newProfile);
			  sppDao.save(newProfile);
			  cache.put("SeriallizableParticipantProfile1#"+name, newProfile);
			  return newProfile.getProfile();
		  }
	  }
	  
      if (1 == 2) {
    	  String digestWaveUrl = "https://wave.google.com/wave/waveref/googlewave.com/" ;
    	  profile = new ParticipantProfile("",
	    		  "" != null ? "" : "",
	    				  digestWaveUrl);
    	  
    	  // Put the value into the cache.
	      cache.put(name, new SeriallizableParticipantProfile(profile.getImageUrl(),null,profile.getName(), profile.getProfileUrl()));
	      return profile; 
      }else{
    	  LOG.fine("Not found even DB profile for: " + name);
    	  return new ParticipantProfile(getRobotName(),
    			  getRobotAvatarUrl(),
    			  getRobotProfilePageUrl());
      }
  }



public void updateProfileWithGravatar(String email,
		SeriallizableParticipantProfile newProfile) {
	Gravatar gravatar = new Gravatar();
	Map<String, String> gravatarProfile = new HashMap<String, String>();
	try {
			gravatarProfile = gravatar.getProfile(email);
		} catch (IOException e) {
			LOG.log(Level.WARNING, email, e);
		} catch (JSONException e) {
			LOG.log(Level.WARNING, email, e);
		}
	  newProfile.updateWith(gravatarProfile);
	  LOG.fine("updated: " + newProfile.toString());
}



private String printProfile(ParticipantProfile profile) {
	String profileStr= "ParticipantProfile [ name:" + profile.getName() + ", imageUrl:" +  profile.getImageUrl()  + "]";
	return profileStr;
}



private String switch2email(String name) {
	int lst = name.lastIndexOf("-");
	  String email = null;
	  if(lst > -1){
		  email = name.substring(0, lst) + "@" + name.substring(lst+1,name.length());
	  }
	return email;
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


	public List<BundledAnnotation> createToAnnotation(Blip blipToUpdate, String blipId, String linkToAnnonationName) {
		String blipRef = "waveid://" + blipToUpdate.getWaveId().getDomain() + "/" + blipToUpdate.getWaveId().getId() + "/~/conv+root/" + blipId;
		  List<BundledAnnotation> baList = BundledAnnotation.listOf("link/manual",blipRef,"style/fontSize", "8pt",linkToAnnonationName,"done");
		return baList;
	}


	public String updateWaveletOnEmailSent(Wavelet wavelet, int activityType, String recipients, String subject) {
		
		subject = filterNonASCII(subject);
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
			
			if(tags.contains("email.activity=draft"))
				tags.remove("email.activity=draft");
			
			//indicate that email was replied
			if(!tags.contains("email.activity=reply"))
				tags.add("email.activity=reply");
			for(String recipient : recArr){
				String strippedRecpient = MailUtils.stripRecipientForEmail(recipient);
				if(!tags.contains("email.replied.to=" + strippedRecpient))
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
			
			if(tags.contains("email.activity=draft"))
				tags.remove("email.activity=draft");
			
			if(!tags.contains("email.activity=new"))
				tags.add("email.activity=new");
			for(String recipient : recArr){
				String strippedRecpient = MailUtils.stripRecipientForEmail(recipient.trim());
				if(strippedRecpient != null && !strippedRecpient.equals("") && !tags.contains("email.sent.to=" + strippedRecpient))
					tags.add("email.sent.to=" + strippedRecpient);
			}
			String sentWhenStr = "email.sent.when=" + todayStr;
			if(!tags.contains(sentWhenStr))
				tags.add(sentWhenStr);
			wavelet.setTitle(subject + " [Sent: " + detailedFormat.format(today) + " ] ");

		}else if(activityType == ActivityType.FORWARD){
			activityTypeStr = "FORWARD";
			
			if(tags.contains("email.activity=draft"))
				tags.remove("email.activity=draft");
			
			if(!tags.contains("email.activity=forward"))
				tags.add("email.activity=forward");
			for(String recipient : recArr){
				String strippedRecpient = MailUtils.stripRecipientForEmail(recipient);
				if(!"".equals(strippedRecpient) && !tags.contains("email.forwared.to=" + strippedRecpient))
					tags.add("email.forwared.to=" + strippedRecpient);
			}
			String forwardedWhenStr = "email.forwarded.when=" + todayStr;
			if(!tags.contains(forwardedWhenStr))
				tags.add(forwardedWhenStr);
			
			wavelet.setTitle( subject + " [" + detailedFormat.format(today) + " ] ");
		}else if(activityType == ActivityType.DRAFT){
			activityTypeStr = "DRAFT";
			if(!tags.contains("email.activity=draft"))
				tags.add("email.activity=draft");
		}
		
		 return activityTypeStr;
	}
	
	public void updateOnMailReceive(Wavelet wavelet, int activityType, String subject, String sender){
		String appdomain = System.getProperty("APP_DOMAIN");
		Date today = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM-dd");
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
			  LOG.log(Level.WARNING,"can happen if the robot was removed manually from the wave or if timeout. waveId: " + waveId + ", proxyFor: " + proxyFor,e);
			  //FIXME - cannot continue from here - need to abort and resubmit
		  }
		 return wavelet;
	 }



	public SeriallizableParticipantProfile updateCreateProfile(String email, String name) {
		 Object o = cache.get("SeriallizableParticipantProfile1#"+email.replace("@", "-"));
		 SeriallizableParticipantProfile profile = null;
		 if(o != null){
			 profile = ((SeriallizableParticipantProfile)o);
			
		 }else{
			 profile = sppDao.getSeriallizableParticipantProfile(email);
			 
		 }
		 if(profile != null){
			 String profileName = profile.getName();
			 if(profileName == null){
				 profile.setName(name);
				 sppDao.save(profile);
			 }
		 }else{
			 profile = new SeriallizableParticipantProfile(null, email, name, null);
			 sppDao.save(profile);
		 }
		 return profile;
	}
	
	public long calcAttachmentsSize(List<Attachment> attachments) {
		long totalSize = 0;
		for(Attachment attachment : attachments){
			totalSize += attachment.getData().length;
		}
		return totalSize;
	}
	
	public static void main(String[] args) {
		String add = "Yuri Zelikov";
		String email = MailUtils.stripRecipientForEmail(add);
		try {
			System.out.println(MimeUtility.decodeWord(add));
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
