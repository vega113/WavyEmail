package com.vegalabs.amail.server.servlet;

import javax.servlet.http.HttpServlet;
import java.io.IOException; 
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties; 
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session; 
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage; 
import javax.servlet.http.*; 
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.wave.api.Attachment;
import com.vegalabs.amail.server.WaveMailRobot;

@Singleton
public class MailHandlerServlet extends HttpServlet {
	 Logger LOG = Logger.getLogger(MailHandlerServlet.class.getName());
	 
	 private WaveMailRobot robot;
	 int depth = 0;
	 
	 @Inject
	public MailHandlerServlet(WaveMailRobot robot){
		this.robot = robot;
	}

	public void doPost(HttpServletRequest req, 
			HttpServletResponse resp) 
	throws IOException { 
		LOG.info("MailHandlerServlet");
		PrintWriter writer = resp.getWriter();
		StringBuilder msgBody = new StringBuilder();
		Properties props = new Properties(); 
		Session session = Session.getDefaultInstance(props, null); 
		List<Attachment> attachmentsList = new ArrayList<Attachment>();
		try {
			MimeMessage message = new MimeMessage(session, req.getInputStream());
			String msgContentType = message.getContentType();
			LOG.info("msgContentType: " + msgContentType);
			if(msgContentType.contains("text/plain")){
				msgBody.append(message.getContent().toString());
			}else {
				Multipart mp = (Multipart)message.getContent();
				depth = 0;
				handleMPart(msgBody, attachmentsList, mp);
			}
			
			
			InternetAddress[] recipientsAddresses = (InternetAddress[])message.getRecipients (RecipientType.TO);
			Set<String> recipientsSet = new LinkedHashSet<String>();
			for(InternetAddress address : recipientsAddresses){
				recipientsSet.add(address.getAddress());
			}
			
			String subject = message.getSubject();
			
			String msgBodyStr = msgBody.toString().replace("\t", " ").replace("\r", "\n");
			robot.recieveMail( msgBodyStr , subject,recipientsSet, message.getFrom()[0].toString(),attachmentsList, message.getSentDate());
			
			LOG.info(String.format("New email from %s with content %s", message.getFrom()[0].toString(), msgBody.toString()));
		} catch (MessagingException e) {
			LOG.log(Level.SEVERE, "exception!", e);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "exception!", e);
		}
		writer.flush();
	}

	
	private void handleMPart(StringBuilder msgBody,
			List<Attachment> attachmentsList, Multipart mp)
			throws MessagingException, IOException {
		depth++;
		String msgBodyPart = null;
		String msgBodyPartHtml = null;
		LOG.info("total " + mp.getCount() + " parts, toString: " + mp.toString() + ", depth: " + depth);

		for (int i=0, n= mp.getCount(); i<n; i++) {
			Attachment attachment = null;
			Part part = mp.getBodyPart(i);
			LOG.info("part#" + i + ", part: " + part);
			
			String contentType = part.getContentType();
			String disposition = part.getDisposition();
			LOG.info("disposition: " + disposition);
			if ((disposition != null) &&  ((disposition.equals(Part.ATTACHMENT) ||  (disposition.equals(Part.INLINE)   ))   )) {
				attachment = createAttachment(part.getFileName(), part.getInputStream());
				if(attachment != null){
					attachmentsList.add( attachment);
				}
				
				String content = String.valueOf(part.getContent());
				LOG.info("after create attachment, size: " + attachmentsList.size() +  ", msgBody(?): " + content);
			}
			LOG.info("contentType: " + contentType);
			
			
			if(contentType.contains("text/html")){
				LOG.log(Level.INFO, "get text/html: " + part.getContent().toString());
				msgBodyPartHtml = part.getContent().toString();
				if(msgBodyPart != null && msgBodyPart.length() > 0){
					int sbLength = msgBodyPart.length();
					int plainMsgBodyPartLength = msgBodyPart.length();
					msgBody.delete(sbLength - plainMsgBodyPartLength, sbLength);
					msgBody.append(msgBodyPartHtml);
					LOG.info("msgBosy: " + msgBody.toString());
				}
			}else if(contentType.contains("text/plain") && msgBodyPartHtml == null){
				LOG.info(" plain contentType: " + contentType);
				LOG.log(Level.INFO, "get text/plain: " + part.getContent().toString());
				msgBodyPart =  part.getContent().toString().replace("\t", " ").replace("\r", "\n");
				msgBody.append(msgBodyPart);
				LOG.info("msgBosy: " + msgBody.toString());
			}
			if(contentType.contains("multipart/alternative")){
				try{
					Multipart childMp = (Multipart)part.getContent();
					handleMPart(msgBody, attachmentsList, childMp);
				}catch(Exception e){
					LOG.log(Level.SEVERE, "", e);
				}
			}
			
		}
	}

	private Attachment createAttachment(String fileName, InputStream inputStream) {
		LOG.log(Level.INFO, "creating attachment: " + fileName);
		Attachment attachment = null;
		byte[] bytes = null;
		try {
			int available = inputStream.available();
			if(available > 0){
				bytes = new byte[available];
				inputStream.read(bytes);
			}
		} catch (IOException e) {
			LOG.log(Level.WARNING, fileName, e);
			List<Byte> bytesList = new ArrayList<Byte>();
			byte newByte = -1;
			try {
				while( (newByte = (byte) inputStream.read()) > -1){
					bytesList.add(newByte);
				}
				if(bytesList.size() > 0){
					int available = bytesList.size();
					bytes = new byte[available];
					for(int i = 0; i< available; i++){
						bytes[i] = bytesList.get(i);
					}
				}
			} catch (IOException e1) {
				LOG.log(Level.SEVERE, fileName, e1);
			}
		}
		if(bytes != null){
			attachment = new Attachment(fileName,bytes);
		}else{
			LOG.log(Level.WARNING, "bytes are null!!! " + fileName);
		}
		return attachment;
	}
}
