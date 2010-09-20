package com.vegalabs.amail.server.admin;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import com.vegalabs.amail.server.WaveMailRobot;
import com.vegalabs.amail.server.dao.EmailEventDao;
import com.vegalabs.amail.server.dao.PersonDao;
import com.vegalabs.amail.server.model.EmailEvent;
import com.vegalabs.amail.server.model.Person;
import com.vegalabs.amail.server.utils.Base64Coder;
import com.vegalabs.amail.server.utils.MailUtils;
import com.vegalabs.amail.shared.UnicodeString;
import com.vegalabs.general.server.command.Command;
import org.json.JSONObject;
import com.vegalabs.general.server.rpc.util.Util;
import com.google.appengine.api.datastore.Text;
import com.google.inject.Inject;
import com.google.wave.api.Attachment;
import com.google.wave.api.Blip;
import com.google.wave.api.Wavelet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

  @Inject
  public SendEmail(Util util, WaveMailRobot robot, EmailEventDao emailEventDao, PersonDao personDao) {
    this.util = util;
    this.robot = robot;
    this.emailEventDao = emailEventDao;
    this.personDao = personDao;
  }

  @Override
  public JSONObject execute() throws JSONException {
	
    String recipients = this.getParam("recipients");
    if (util.isNullOrEmpty(recipients)) {
      throw new IllegalArgumentException("Missing required param: recipients");
    }
    String subject = this.getParam("subject");
    if (util.isNullOrEmpty(subject)) {
      throw new IllegalArgumentException("Missing required param: subject");
    }
    subject = Base64Coder.decodeString(subject);
    subject = UnicodeString.deconvert(subject);
    
    String msgBody = this.getParam("msgBody");
    if (util.isNullOrEmpty(msgBody)) {
      throw new IllegalArgumentException("Missing required param: msgBody");
    }
   
    msgBody = Base64Coder.decodeString(msgBody);
    msgBody = UnicodeString.deconvert(msgBody);
    
    String sender = this.getParam("sender");
    if (util.isNullOrEmpty(sender)) {
      throw new IllegalArgumentException("Missing required param: sender");
    }
    
    String waveId = this.getParam("waveId");
    if (util.isNullOrEmpty(sender)) {
      throw new IllegalArgumentException("Missing required param: waveId");
    }
    
    String blipId = this.getParam("blipId");
    if (util.isNullOrEmpty(blipId)) {
      throw new IllegalArgumentException("Missing required param: blipId");
    }
    if(blipId.equals("none")){
    	blipId = null;
    }
    LOG.info("blipId: " + blipId);
    String senderName = this.getParam("senderName");
    
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
    	blipId = wavelet.getRootBlipId();
    }
    blip = wavelet.getBlip(blipId);
	
  
    
    String activityTypeStr = robot.updateOnEmailSent(wavelet,activityType,recipients,subject);
    
    
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);

    List<String> fromList = new ArrayList<String>();
	 fromList.add(sender);
	 List<String> toList = new ArrayList<String>();
	 
	 for(String recipient : recipients.split(",")){
		 toList.add(recipient);
		 String onlyMail = MailUtils.stripRecipientForEmail(recipient);
		 String onlyName = MailUtils.stripRecipientForName(recipient);
		 person.getContacts().put(onlyMail, recipient);
		 person.getContactsName().put(onlyName, recipient);
	 }
	personDao.save(person);
	 
    EmailEvent emailEvent = new EmailEvent(activityTypeStr, subject, new Text(msgBody), fromList, toList, sender, sentDate);
	 String fullWaveId = waveId.split("!")[0] + "#" + waveId.split("!")[1] + "#" + blipId;
	 emailEvent.getFullWaveIdPerUserMap().put(sender, fullWaveId);
	 emailEventDao.save(emailEvent);
	 
	

    try {
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(sender,senderName));
        String[] splitRec = recipients.split(",");
        for(String recipient : splitRec){
        	 msg.addRecipient(Message.RecipientType.TO,
                     new InternetAddress(recipient));
        }
        msg.setSubject(subject);
        
     
        Multipart multipart = new MimeMultipart();
        
     // Create the message part 
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        // Fill the message
        messageBodyPart.setContent(msgBody, "text/html;");
        int partNum = 0;
        multipart.addBodyPart(messageBodyPart,partNum);
        partNum++;
        
        //create attachments
        LOG.info("Activity type: " + activityTypeStr);
        if(activityTypeStr.equals("FORWARD") || activityTypeStr.equals("NEW")) {
        	// Part two is attachment
            List<Attachment>  attachments = MailUtils.getAllAttachmentUrls(blip);
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
                multipart.addBodyPart(attachmentBodyPart,partNum);
                partNum++;
        	}
        }
     

        // Put parts in message
    	msg.setContent(multipart);
    	msg.saveChanges();
    	// set the Date: header
        msg.setSentDate(sentDate);
        Transport.send(msg);

    } catch (AddressException e) {
       LOG.log(Level.SEVERE, "", e);
       throw new IllegalArgumentException(e);
    } catch (javax.mail.SendFailedException e) {
    	 LOG.log(Level.SEVERE, "", e);
    	 throw new IllegalArgumentException(e);
	}catch (MessagingException e) {
    	 LOG.log(Level.SEVERE, "", e);
    	 throw new IllegalArgumentException(e);
    } catch (UnsupportedEncodingException e) {
    	 LOG.log(Level.SEVERE, "", e);
    	 throw new IllegalArgumentException(e);
	}
	
    JSONObject json = new JSONObject();
    json.put("success", "true");
    return json;
  }

}
