package com.vegalabs.amail.server.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	
	
	public static StringBuilder escapeHtmlFull(String s)
	 {
	     StringBuilder b = new StringBuilder(s.length());
	     for (int i = 0; i < s.length(); i++)
	     {
	       char ch = s.charAt(i);
	       if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9')
	       {
	         // safe
	         b.append(ch);
	       }
	       else if (Character.isWhitespace(ch))
	       {
	         // paranoid version: whitespaces are unsafe - escape
	         // conversion of (int)ch is naive
	         b.append("&#").append((int) ch).append(";");
	       }
	       else if (Character.isISOControl(ch))
	       {
	         // paranoid version:isISOControl which are not isWhitespace removed !
	         // do nothing do not include in output !
	       }
	       else if (Character.isHighSurrogate(ch))
	       {
	         int codePoint;
	         if (i + 1 < s.length() && Character.isSurrogatePair(ch, s.charAt(i + 1))
	           && Character.isDefined(codePoint = (Character.toCodePoint(ch, s.charAt(i + 1)))))
	         {
	            b.append("&#").append(codePoint).append(";");
	         }
	         else
	         {
	        	 LOG.log(Level.WARNING, "bug:isHighSurrogate");
	         }
	         i++; //in both ways move forward
	       }
	       else if(Character.isLowSurrogate(ch))
	       {
	         // wrong char[] sequence, //TODO: LOG !!!
	         LOG.log(Level.WARNING, "bug:isLowSurrogate");
	         i++; // move forward,do nothing do not include in output !
	       }
	       else
	       {
	         if (Character.isDefined(ch))
	         {
	           // paranoid version
	           // the rest is unsafe, including <127 control chars
	           b.append("&#").append((int) ch).append(";");
	         }
	         //do nothing do not include undefined in output!
	       }
	    }
	     return b;
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
	 public static List<Attachment> getAllAttachmentUrls(Blip blip) {
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
			return dirtySubject;
		}
}
