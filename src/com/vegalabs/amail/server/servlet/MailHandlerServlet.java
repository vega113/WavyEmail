package com.vegalabs.amail.server.servlet;

import javax.servlet.http.HttpServlet;
import java.io.IOException; 
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties; 
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session; 
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage; 
import javax.mail.internet.MimeUtility;
import javax.servlet.http.*; 
import javax.xml.soap.MimeHeader;

import org.json.JSONException;

import jgravatar.Gravatar;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.wave.api.Attachment;
import com.vegalabs.amail.server.WaveMailRobot;
import com.vegalabs.amail.server.utils.MailUtils;
import com.vegalabs.amail.server.utils.MimeUtil;

@Singleton
public class MailHandlerServlet extends HttpServlet {
	 Logger LOG = Logger.getLogger(MailHandlerServlet.class.getName());
	 
	 protected WaveMailRobot robot;
	 int depth = 0;
	 
	 @Inject
	public MailHandlerServlet(WaveMailRobot robot){
		this.robot = robot;
	}
	 
	 static{
		 System.setProperty("mail.mime.charset", "UTF-8");
	 }
	 Properties props = new Properties(); 
	 Session session = Session.getDefaultInstance(props, null); 

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException { 
		LOG.info("MailHandlerServlet");
		
		PrintWriter writer = resp.getWriter();
		StringBuilder msgBody = new StringBuilder();
		
		List<Attachment> attachmentsList = new ArrayList<Attachment>();
		try {
			InputStream msgInputStream = req.getInputStream();
			MimeMessage message = new MimeMessage(session, msgInputStream);
			String subject = message.getSubject();
			
			handleMessage(msgBody, attachmentsList, message);
			
			InternetAddress[] recipientsAddresses = (InternetAddress[])message.getRecipients (RecipientType.TO);
			Set<String> recipientsSet = new LinkedHashSet<String>();
			for(InternetAddress address : recipientsAddresses){
				recipientsSet.add(address.getAddress());
			}
			
			String msgBodyStr = msgBody.toString();
			robot.recieveMail( msgBodyStr , subject,recipientsSet, message.getFrom()[0].toString(),attachmentsList, message.getSentDate());
			
			LOG.info(String.format("New email from %s with content %s", message.getFrom()[0].toString(), msgBody.toString()));
		} catch (MessagingException e) {
			LOG.log(Level.SEVERE, "exception!", e);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "exception!", e);
		}
		writer.flush();
	}


	private void handleMessage(StringBuilder msgBody,
			List<Attachment> attachmentsList, MimeMessage message)
			throws MessagingException, IOException, Exception {
		String msgContentType = message.getContentType();
		LOG.info("msgContentType: " + msgContentType);
		if(msgContentType.contains("text")){
			msgBody.append(message.getContent().toString());
		}else if(msgContentType.contains("multipart")){
			Multipart mp = (Multipart)message.getContent();
			depth = 0;
			handleMPart(msgBody, attachmentsList, mp,message.getSubject());
		}else{
			LOG.severe("no action for secnario when message content type is: " + msgContentType + "!!!");
		}
	}

	
	private void handleMPart(StringBuilder msgBody,
			List<Attachment> attachmentsList, Multipart mp, String subject) throws Exception {
		depth++;
		String msgBodyPartPlain = null;
		String msgBodyPartHtml = null;
		LOG.info("MultiPart total " + mp.getCount() + " parts, ContentType: " + mp.getContentType() + ", depth: " + depth);
		for (int i=0, n= mp.getCount(); i<n; i++) {
			Attachment attachment = null;
			BodyPart part = mp.getBodyPart(i);
			LOG.info("part#" + i + ", part: " + part);
			
			String contentType = part.getContentType();
			
			LOG.info("body contentType: " + contentType);
			String disposition = part.getDisposition();
			LOG.info("disposition: " + disposition);
			if ((disposition != null) &&  ((disposition.equals(Part.ATTACHMENT)   ))   ) {
				attachment = createAttachment(part.getFileName(),  part.getInputStream());
				
				if(attachment != null){
					attachmentsList.add(attachment);
				}
			}else if(contentType.contains("text/html")){
				msgBodyPartHtml = (String)getContent(part); 
				boolean isDetectedProblemWithHtml = msgBodyPartHtml.contains("\u0000");
				msgBodyPartHtml = msgBodyPartHtml.replaceAll("\t", " ").replaceAll("\r", "").replaceAll("\n", "").replaceAll("\b", "").replaceAll("\f", "").replaceAll("\u0000", " ");
				LOG.log(Level.INFO, "get text/html cleaned: " + msgBodyPartHtml);
				if(msgBodyPartPlain != null && msgBodyPartPlain.length() > 0 && !isDetectedProblemWithHtml){
					int sbLength = msgBodyPartPlain.length();
					int plainMsgBodyPartLength = msgBodyPartPlain.length();
					msgBody = msgBody.delete(sbLength - plainMsgBodyPartLength, sbLength);
					msgBody.append(msgBodyPartHtml);
					LOG.info("appended html");
				}
			}else if(contentType.contains("text/plain") && msgBodyPartHtml == null){
				msgBodyPartPlain = (String) getContent(part);
				msgBodyPartPlain = msgBodyPartPlain.replaceAll("\u0000", " ");
				LOG.log(Level.INFO, "appended text/plain: " + getContent(part));
				msgBody.append(msgBodyPartPlain);
			}else if(contentType.contains("message/rfc822")){
				InputStream embeddedMsgInputStream = part.getInputStream();
				MimeMessage embeddedMessage = new MimeMessage(session, embeddedMsgInputStream);
				handleMessage(msgBody, attachmentsList, embeddedMessage);
				LOG.info("handling message/rfc822");
			}else if(contentType.contains("multipart/alternative")){
				Multipart childMp = (Multipart)part.getContent();
				handleMPart(msgBody, attachmentsList, childMp,subject);
			}else{
				LOG.log(Level.SEVERE, "unhandled contentType: " + contentType);
			}
			
			
		}
	}

	private String extractTxtFromInline(BodyPart part) throws Exception {
		return (String) getContent(part);
	}


	private Attachment createAttachment(String fileName, InputStream inputStream ) throws IOException, MessagingException {
		LOG.log(Level.INFO, "creating data attachment: " + fileName);
		Attachment attachment = null;
		byte[] bytes = null;
		
		int available =  inputStream.available();
		if(available > 1){
			bytes = new byte[available];
			inputStream.read(bytes);
		}
		else{
			try {
				List<Byte> bytesList = new ArrayList<Byte>();
				byte newByte = -1;
				try {
					while( (newByte = (byte) inputStream.read()) > -1){
						bytesList.add(newByte);
					}
					if(bytesList.size() > 0){
						available = bytesList.size();
						bytes = new byte[available];
						for(int i = 0; i< available; i++){
							bytes[i] = bytesList.get(i);
						}
					}
				} catch (IOException e1) {
					LOG.log(Level.SEVERE, fileName, e1);
				}
			} catch (Exception e) {
				LOG.log(Level.WARNING, fileName, e);
				
			}
		}
		
		if(bytes != null){
			Map<String,String> properties = new HashMap<String,String>();
			properties.put(Attachment.CAPTION, fileName);
			attachment = new Attachment(properties,bytes);
		}else{
			LOG.log(Level.WARNING, "bytes are null!!! " + fileName);
		}
		return attachment;
	}
	
	
	
	protected Object getContent(BodyPart part) throws Exception{
		String content = null;
		content =  (String)MimeUtil.getContent(part);
		return content;

	}

}
