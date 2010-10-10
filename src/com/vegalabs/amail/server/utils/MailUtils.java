package com.vegalabs.amail.server.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import com.google.wave.api.Attachment;
import com.google.wave.api.Element;
import com.google.wave.api.Blip;



public class MailUtils {
	private static final Logger LOG = Logger.getLogger(MailUtils.class.getName());
	public static String waveId2mailId(String sender) {
		String[] senderSplit = sender.split("@");
	    String userId = senderSplit[0];
	    String domain = senderSplit[1];
	    
	    String appSender = String.format("%s-%s@%s.appspotmail.com", userId,domain,System.getProperty("APP_DOMAIN"));
		return appSender;
	}
	
	public static String mailId2waveId(String appRecipient) {
		String recipient = null;
		try{
			String[] appRecipientSplit = appRecipient.split("@");
		    String userId = appRecipientSplit[0].split("-")[0]; //FIXME - what if user/domain id also contains "-" ?
		    String domain = appRecipientSplit[0].split("-")[1];
		    
		    recipient = String.format("%s@%s", userId,domain);
		}catch(Exception e){
		}
		return recipient;
	}
	
	
	public static String stripRecipientForEmail(String fullRecipient) {
		String proxyFor;
		int strt = fullRecipient.indexOf("<");
		 int end = fullRecipient.indexOf(">");
		 if(strt > 0){
			 proxyFor = fullRecipient.substring(strt+1, end);
		 }else{
			 proxyFor = fullRecipient;
		 }
		return proxyFor.toLowerCase();
	}
	
	public static String stripRecipientForName(String fullRecipient) {
		String senderName;
		int strt = fullRecipient.indexOf("<");
		 if(strt > 0){
			 senderName = fullRecipient.substring(0,strt); 
		 }else{
			 senderName = "";
		 }
		return senderName;
	}
	 public static List<Attachment> getAllAttachments(Blip blip) {
   	  List<Attachment> attachmentsList = new ArrayList<Attachment>();
   	  Collection<com.google.wave.api.Element> elements = blip.getElements().values();
   	  for (Element element : elements) {
   	    if (element.isAttachment()) {
   	    	Attachment attachment = (Attachment)element;
   	    	attachmentsList.add(attachment);
   	    }
   	  }
   	  return attachmentsList;
   	}
	 
	 private static String[] stripArr = {"Re:", "RE:", "Fw:", "Fwd:", "FW:"};
		
		public static String cleanSubject(String dirtySubject){
			boolean isContinue = true;
			dirtySubject = dirtySubject.trim();
			while(dirtySubject.length() > 2 && isContinue){
				isContinue = false;
				for(String prefix : stripArr){
					if(dirtySubject.startsWith(prefix)){
						int prefixLength = prefix.length();
						dirtySubject = dirtySubject.substring(prefixLength).trim();
						isContinue = true;
					}
				}
			}
			return dirtySubject.trim();
		}
		
		
		static Properties props = new Properties(); 
		 static Session session = Session.getDefaultInstance(props, null); 
		public static void sendDeliveryFailedMail(String to, String subject, String reason) {
			 
			 Message msg = new MimeMessage(session);
			  try {
				msg.setFrom(new InternetAddress("delivery_failed" + "@" + System.getProperty("APP_DOMAIN") + ".appspotmail.com"));
				 msg.addRecipient(Message.RecipientType.TO,
						  new InternetAddress(to));
				  msg.setSubject("Delivery failed: " + subject);

				  Multipart multipart = new MimeMultipart();

				  // Create the message part 
				  MimeBodyPart messageBodyPart = new MimeBodyPart();
				  // Fill the message
				  messageBodyPart.setContent(reason, "text/html;");
				  multipart.addBodyPart(messageBodyPart,0);

				  //create attachments
				  // Put parts in message
				  msg.setContent(multipart);
				  msg.saveChanges();
				  // set the Date: header
				  msg.setSentDate(new Date());
				  Transport.send(msg);
			} catch (AddressException e) {
				LOG.log(Level.SEVERE, "to: " + to + ", subject: " + subject + ", reason" + reason , e);
			} catch (MessagingException e) {
				LOG.log(Level.SEVERE, "to: " + to + ", subject: " + subject + ", reason" + reason , e);
			}
		}
}
