package com.vegalabs.amail.server.admin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import com.vegalabs.amail.server.WaveMailRobot;
import com.vegalabs.amail.server.dao.EmailEventDao;
import com.vegalabs.amail.server.dao.EmailThreadDao;
import com.vegalabs.amail.server.dao.PersonDao;
import com.vegalabs.amail.server.data.FullWaveAddress;
import com.vegalabs.amail.server.model.EmailEvent;
import com.vegalabs.amail.server.model.EmailThread;
import com.vegalabs.amail.server.model.Person;
import com.vegalabs.amail.server.utils.MailUtils;
import com.vegalabs.general.server.command.Command;
import org.json.JSONObject;
import com.vegalabs.general.server.rpc.util.Util;
import com.google.appengine.api.datastore.Text;
import com.google.inject.Inject;
import com.google.wave.api.Attachment;
import com.google.wave.api.Blip;
import com.google.wave.api.Element;
import com.google.wave.api.Wavelet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

public class SendEmail extends Command {
  static Logger LOG = Logger.getLogger(SendEmail.class.getName());
  private Util util = null;
  private WaveMailRobot robot;
  private EmailEventDao emailEventDao;
  private PersonDao personDao;
  private EmailThreadDao emailThreadDao;

  @Inject
  public SendEmail(Util util, WaveMailRobot robot, EmailEventDao emailEventDao, PersonDao personDao,EmailThreadDao emailThreadDao) {
    this.util = util;
    this.robot = robot;
    this.emailEventDao = emailEventDao;
    this.personDao = personDao;
    this.emailThreadDao = emailThreadDao;
  }

  @Override
  public JSONObject execute() throws JSONException {
	
    String recipients = this.getParam("recipients");
    if (util.isNullOrEmpty(recipients)) {
      throw new IllegalArgumentException("Missing required param: recipients");
    }
    recipients = robot.decode(recipients);
    String subject = this.getParam("subject");
    if (util.isNullOrEmpty(subject)) {
    	subject = "";
    }
    subject = robot.decode(subject);
    
    String msgBody = this.getParam("msgBody");
    if (util.isNullOrEmpty(msgBody)) {
    	msgBody = "";
    }
   
    msgBody = robot.decode(msgBody);
    
    String sender = this.getParam("sender");
    if (util.isNullOrEmpty(sender)) {
      throw new IllegalArgumentException("Missing required param: sender");
    }
    sender = robot.decode(sender);
    
    String waveId = this.getParam("waveId");
    if (util.isNullOrEmpty(sender)) {
      throw new IllegalArgumentException("Missing required param: waveId");
    }
    
    String uuid = this.getParam("uuid");
    if (util.isNullOrEmpty(uuid)) {
      throw new IllegalArgumentException("Missing required param: uuid");
    }
    
    String blipId = this.getParam("blipId");
    if(blipId.equals("none")){
    	blipId = null;
    }
    LOG.info("blipId: " + blipId);
    String senderName = this.getParam("senderName");
    senderName = robot.decode(senderName);
    
    String iconUrl = this.getParam("iconUrl");//XXX save it
    if (util.isNullOrEmpty(iconUrl)) {
        throw new IllegalArgumentException("Missing required param: iconUrl");
      }
    
    
    Date sentDate = new Date();
    
    Person person = null;
    person = personDao.getPerson(sender);
    if(person == null){
    	person = new Person(sender);
    	
    }
    person.setIconUrl(iconUrl);
    if(senderName != null){
    	person.setName(senderName);
    }
    
    int activityType = Integer.parseInt(this.getParam("activityType"));
    
    //need to fetch the wavelet
    Wavelet wavelet = robot.fetchWavelet(waveId,null);
    if(wavelet == null){
    	throw new IllegalArgumentException("Cannot fetch the wavelet (probably due to timeout) " + waveId);
    }
//    check if there are attachments in the blip
    Blip blip = null;
    if(blipId == null){
    	//find the blip
    	//iterate of blips and try to find the gadget
    	blipId = findBlipIdByGadgetUuid(uuid, wavelet);
    	LOG.warning("Cannot find blip for uuid: " + uuid);
    }
    if(blipId == null){
    	blipId = wavelet.getRootBlipId();
    }
    blip = wavelet.getBlip(blipId);
    
    
    List<Attachment>  attachments = MailUtils.getAllAttachments(blip);
    
    long lengthInBytes = robot.calcAttachmentsSize(attachments);
	if(lengthInBytes + msgBody.getBytes().length > 1024*1024*1024){
		throw new IllegalArgumentException("Message size is too big, maximum size is 1MB!");
	}

    List<String> fromList = new ArrayList<String>();
    fromList.add(sender);
    List<String> toList = new ArrayList<String>();

    for(String recipient : recipients.split(",")){
    	toList.add(recipient);
    	String onlyMail = MailUtils.stripRecipientForEmail(recipient);
    	String onlyName = MailUtils.stripRecipientForName(recipient);
    	if(onlyName != null){
    		//let's check if if have this profile
    		robot.updateCreateProfile(onlyMail,onlyName);
    	}
    	person.getContacts().put(onlyMail, recipient);
    	person.getContactsName().put(onlyName, recipient);
    }
    person.setUpdated(new Date());
    personDao.save(person);
    
    String activityTypeStr = robot.updateWaveletOnEmailSent(wavelet,activityType,recipients,subject);
    
//    String contacts = robot.retrContacts4User(person);
//    HashMap<String,String> contactsUpdateMap = new HashMap<String, String>();
//    contactsUpdateMap.put("contacts", contacts);
//    robot.updateGadgetState(blip, WaveMailRobot.GADGET_URL, contactsUpdateMap);
    //XXX - turn on later
    
    try {
		robot.submit(wavelet, robot.getRpcServerUrl());
	} catch (IOException e) {
		LOG.log(Level.SEVERE, wavelet.getWaveId().toString(), e);
		// in most cases it will be submitted later
	}
	
	

    EmailEvent emailEvent = new EmailEvent(activityTypeStr, subject, new Text(msgBody), fromList, toList, sender, sentDate, attachments);
    String fullWaveId = waveId.split("!")[0] + "#" + waveId.split("!")[1] + "#" + blipId;
    emailEvent.getFullWaveIdPerUserMap().put(sender, fullWaveId);
    emailEventDao.save(emailEvent);
    
    //handle mail thread - create new if needed
    String cleanSubject = MailUtils.cleanSubject(subject);
	//need to check if exists thread for this subject given recipient
	 EmailThread thread = emailThreadDao.getEmailThread4Wavemail(cleanSubject.hashCode(), sender);
	 if( thread == null){
		 thread = new EmailThread(cleanSubject.hashCode(), sender, new FullWaveAddress(wavelet.getWaveId().getDomain(), wavelet.getWaveId().getId(), blipId));
	 }else{
		 thread.setDomain(wavelet.getWaveId().getDomain());
		 thread.setWaveId(wavelet.getWaveId().getId());
		 thread.setBlipId(blipId);
		 thread.setBlipsCount(thread.getBlipsCount() + 1);
	 }
	 emailThreadDao.save(thread);

    deliverEmailWithRetry(person, emailEvent);
	
    JSONObject json = new JSONObject();
    json.put("success", "true");
    return json;
  }


private String findBlipIdByGadgetUuid(String uuid, Wavelet wavelet) {
	Map<String,Blip> blipsMap =  wavelet.getBlips();
	for(String key : blipsMap.keySet()){
		Blip currentBlip = blipsMap.get(key);
		Map<Integer,Element> currentBlipElementsMap =  currentBlip.getElements();
		for(Integer elemKey : currentBlipElementsMap.keySet()){
			Element currElement = currentBlipElementsMap.get(elemKey);
			String elemUuid = currElement.getProperty("uuid");
			if(uuid.equals(elemUuid)){
				return currentBlip.getBlipId();
			}
		}
	}
	return null;
}

  //no retry yet
  public static void deliverEmailWithRetry(Person person, EmailEvent emailEvent) {
	  Properties props = new Properties();
	  Session session = Session.getDefaultInstance(props, null);
	  try {
		  Message msg = new MimeMessage(session);
		  msg.setFrom(new InternetAddress(person.getWavemail(),person.getName()));
		  for(String recipient : emailEvent.getTo()){
			  if(recipient.length() > 4 && recipient.contains("@")){
				  msg.addRecipient(Message.RecipientType.TO,
						  new InternetAddress(recipient));
			  }
		  }
		  msg.setSubject(emailEvent.getSubject());
		  
		  
		  Multipart multipart = new MimeMultipart();
		  
		// Create the message part 
		  MimeBodyPart messageBodyTxtPart = new MimeBodyPart();
		  // Fill the message
		  messageBodyTxtPart.setContent(emailEvent.getMsgBody().getValue(), "text/plain;");
		  multipart.addBodyPart(messageBodyTxtPart);


		  

		  // Create the message part 
		  MimeBodyPart messageBodyPart = new MimeBodyPart();
		  // Fill the message
		  messageBodyPart.setContent(emailEvent.getMsgBody().getValue(), "text/html;");
		  multipart.addBodyPart(messageBodyPart);
		  
		  

		  //create attachments
		  LOG.info("Activity type: " + emailEvent.getActivityType());
		  if(emailEvent.getActivityType().equals("FORWARD") || emailEvent.getActivityType().equals("NEW")) {
			  // Part two is attachment
			  waveAttachment2Mpart(emailEvent.getAttachments(), multipart);
		  }


		  // Put parts in message
		  msg.setContent(multipart);
		  msg.saveChanges();
		  // set the Date: header
		  msg.setSentDate(emailEvent.getSentDate());
		  Transport.send(msg);

	  } catch (AddressException e) {
		  LOG.log(Level.SEVERE, "person id: " + person.getId() + ", emailEvent id:" + emailEvent.getId() + ", msgBody: " + emailEvent.getMsgBody(), e);
		  throw new IllegalArgumentException(e);
	  } catch (javax.mail.SendFailedException e) {
		  LOG.log(Level.SEVERE, "person id: " + person.getId() + ", emailEvent id:" + emailEvent.getId() + ", msgBody: " + emailEvent.getMsgBody(), e);
		  throw new IllegalArgumentException(e);
	  }catch (MessagingException e) {
		  LOG.log(Level.SEVERE, "person id: " + person.getId() + ", emailEvent id:" + emailEvent.getId() + ", msgBody: " + emailEvent.getMsgBody(), e);
		  throw new IllegalArgumentException(e);
	  } catch (UnsupportedEncodingException e) {
		  LOG.log(Level.SEVERE, "person id: " + person.getId() + ", emailEvent id:" + emailEvent.getId() + ", msgBody: " + emailEvent.getMsgBody(), e);
		  throw new IllegalArgumentException(e);
	  }
  }

	public static void waveAttachment2Mpart(List<Attachment> attachments,
			Multipart multipart) throws MessagingException {
		for(Attachment attachment : attachments){
			String filename = attachment.getCaption();
			String mimeTmp = attachment.getMimeType().endsWith(";") ? attachment.getMimeType() : attachment.getMimeType() + ";";
			byte[] data = attachment.getData();
			LOG.info("Attachmnet name: " + filename + ", data length: " + data.length + ", mime: " + mimeTmp);
			if(filename.endsWith(".ico")){
				filename.replace(".ico", ".png");
				mimeTmp = "image/png;";
			}else if(filename.endsWith(".xml")){
				filename.replace(".xml", ".txt");
				mimeTmp = "text/plain;";
			}else if(filename.endsWith(".png")){
				mimeTmp = "image/png;";
			}else if(filename.endsWith(".html")){
				mimeTmp = "text/html;";
			}else if(filename.endsWith(".doc")){
				mimeTmp = "application/msword;";
			}else if(filename.endsWith(".pdf")){
				mimeTmp = "application/pdf;";
			}else if(filename.endsWith(".mp3")){
				mimeTmp = "audio/mpeg;";
			}else if(filename.endsWith(".gif")){
				mimeTmp = "image/gif;";
			}
	
			final String mime = mimeTmp;
			MimeBodyPart attachmentBodyPart = new MimeBodyPart(){
				@Override
				public String getContentType() {
					return mime;
				}
	
				@Override
				public String getDisposition() throws MessagingException {
					return Part.ATTACHMENT;
				}
	
			};
			ByteArrayDataSource ds = new ByteArrayDataSource(data,mime);
			DataHandler dh = new DataHandler(ds);
			attachmentBodyPart.setDataHandler(dh);
			attachmentBodyPart.setFileName(filename);
			attachmentBodyPart.setDisposition(Part.ATTACHMENT);
			LOG.info("real content type: " + attachmentBodyPart.getContentType() + ", isDisposition: " + (attachmentBodyPart.getDisposition() != null) + ", ds.contentType: " + ds.getContentType());
			multipart.addBodyPart(attachmentBodyPart);
		}
	}


}
