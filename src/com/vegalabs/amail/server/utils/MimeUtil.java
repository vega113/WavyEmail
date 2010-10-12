package com.vegalabs.amail.server.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.QuotedPrintableCodec;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

public class MimeUtil {
	private static final Logger LOG = Logger.getLogger(MimeUtil.class.getName());

	public static MimeMessage createMimeMessage(HttpServletRequest
			request)
	throws MessagingException, IOException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		session.setDebug(true);
		MimeMessage message = new MimeMessage(session,
				request.getInputStream());
		return message;
	}

	// http://commons.apache.org/codec/xref/org/apache/commons/codec/net/Quo...
	private static final byte ESCAPE_CHAR = '=';

	public static String decodeQuotedPrintable(byte[] bytes, String
			charset)
	throws IOException {
		return new String(decodeQuotedPrintable(bytes), charset);
	}

	public static byte[] decodeQuotedPrintable(byte[] bytes) throws
	IOException {
		if (bytes == null) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (int i = 0; i < bytes.length; i++) {
			int b = bytes[i];
			if (b == ESCAPE_CHAR) {
				try {
					if (bytes[i + 1] == 10) {
						// FIX skip newline, lenient
						++i;
					} else {
						int u = digit16(bytes[++i]);
						int l = digit16(bytes[++i]);
						out.write((char) ((u << 4) + l));
					}
				} catch (Exception e) {
//					throw new IOException("Invalid quoted-printableencoding", e);
				}
			} else {
				out.write(b);
			}
		}
		return out.toByteArray();
	}

	public static int digit16(byte b) throws IOException {
		int i = Character.digit(b, 16);
		if (i == -1) {
			throw new IOException("<MimeUtil> Invalid encoding: not a valid digit (radix 16): "  + b);
		}
		return i;
	}

	private static byte[] getBytes(InputStream rawInputStream) throws IOException {
		List<Byte> bytesList = new ArrayList<Byte>();
		byte[] out = null;
		byte temp;
		while((temp = (byte)rawInputStream.read()) > -1){
			bytesList.add(temp);
		}
		int size = bytesList.size();
		out = new byte[size];
		int i = 0;
		for(Byte mybyte : bytesList){
			out[i] = mybyte;
			i++;
		}
		return out;
	}

	public static Object getContent(BodyPart part) throws Exception {
		String content = null; 
		InputStream is = part.getInputStream();
		String encoding = MimeUtility.getEncoding(part.getDataHandler());
		
		try {
			try{
				javax.mail.internet.MimeBodyPart mimeBodyPart = null;
				if(part instanceof javax.mail.internet.MimeBodyPart){
					mimeBodyPart = (javax.mail.internet.MimeBodyPart)part;
					InputStream rawInputStream = mimeBodyPart.getRawInputStream();
					
					
					String contentType = mimeBodyPart.getContentType();
					String mimeEncoding = mimeBodyPart.getEncoding();
					
					LOG.info("mimeEncoding: " + mimeEncoding);
					
					content = processRawInputStream(rawInputStream,
							contentType, mimeEncoding);

				}else{
					if(content == null || content.length() == 0 ){
						content = new String(getBytes(is));
					}
				}
			}catch(Exception e){
				LOG.log(Level.WARNING, "in catch after mime: ",e);
				if(content == null || content.length() == 0 ){
					content = new String(getBytes(is));
					LOG.warning("content from bytes: " + content);
				}
			}
			
		} catch (Exception e) {
			try {
				
				byte[] out = getBytes(is);
				out = getBytes(is);
				out = decodeQuotedPrintable(out);
				content = new String(out);
				LOG.info("in getContent:decodeQuotedPrintable encoding: " + encoding + ", content: " + content);
			} catch (Exception e1) {
				LOG.log(Level.WARNING, (String) content, e1);
				is = MimeUtility.decode(is,encoding);
				byte[] out = getBytes(is);
				content = new String(out);
				if(content == null || content.toString().length() < 2 ){
					throw e;
				}
				LOG.fine("in getContent:decodeQuotedPrintable:MimeUtility.decode content: " + content);
			}
		}
		return content;
	}

	public static String processRawInputStream(InputStream rawInputStream,
			String contentType, String mimeEncoding) throws IOException,
			UnsupportedEncodingException {
		String content;
		String charset = contentType2Charset(contentType, "UTF-8");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			int c;
			while ((c = rawInputStream.read()) != -1)
				outputStream.write(c);
			// get the character set from the content-type
			byte[] encodedBytes = outputStream.toByteArray();
			String encodedContent = new String(outputStream.toByteArray(), charset);
			LOG.fine("encoded content: " + encodedContent);
			String decodedContent = null;
			if("quoted-printable".equals(mimeEncoding.toLowerCase())){
				decodedContent = new String(decodeQuotedPrintable(encodedBytes), charset);
			}else if("base64".equals(mimeEncoding.toLowerCase())){
				
				decodedContent = new String(Base64.decodeBase64(encodedBytes),charset);
			}else{
				decodedContent = encodedContent;
			}
			LOG.fine("decoded content: " + decodedContent);
			content = Normalizer.normalize(decodedContent, Form.NFKC) ;//
		return content;
	}

	public static String contentType2Charset(String contentType,
			String defaultCharset) {
		String charset = defaultCharset;
		if (contentType.indexOf("charset=") != -1) {
			String[] split = contentType.split("charset=");
			if (split.length > 1) {
				charset = split[1];
				if (charset.indexOf(';') >= 0) {
					charset = charset.substring(0,
							charset.indexOf(';'));
				}
				charset = charset.replaceAll("\"", "");
				charset = charset.trim();
			}
		}
		return charset;
	}

	public static String getContent(InputStream rawInputStream,InputStream inputStream, String contentType, String mimeEncoding) {
		String content = null;
		try {
			content =  processRawInputStream(rawInputStream, contentType, mimeEncoding);
		} catch (UnsupportedEncodingException e) {
			LOG.log(Level.WARNING, (String) content, e);
		} catch (Exception e) {
			LOG.log(Level.WARNING, (String) content, e);
			try {
				content =  new String(getBytes(inputStream));
			} catch (IOException e1) {
				LOG.log(Level.SEVERE, (String) content, e1);
			}
		}
		
		return content;
	}


}
