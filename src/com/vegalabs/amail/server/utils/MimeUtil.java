package com.vegalabs.amail.server.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

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
					throw new IOException("Invalid quoted-printableencoding", e);
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
			throw new IOException("Invalid encoding: not a valid digit (radix 16): "                    + b);
		}
		return i;
	}

//	public static Object getContent(MimeMessage message) throws
//	Exception {
//		String charset = contentType2Charset(message.getContentType(),
//				null);
//		Object content;
//		try {
//			content = message.getContent();
//		} catch (Exception e) {
//			try {
//				byte[] out = getBytes(message.getRawInputStream());
//				out = decodeQuotedPrintable(out);
//				if (charset != null) {
//					content = new String(out, charset);
//				} else {
//					content = new String(out);
//				}
//			} catch (Exception e1) {
//				throw e;
//			}
//		}
//		return content;
//	}

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
		String charset = contentType2Charset(part.getContentType(),"UTF-8");
		Object content;
		try {
			content = part.getContent();
		} catch (Exception e) {
			try {
				byte[] out = getBytes(part.getInputStream());
				out = decodeQuotedPrintable(out);
				if (charset != null) {
					content = new String(out, charset);
				} else {
					content = new String(out);
				}
			} catch (Exception e1) {
				throw e;
			}
		}
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


}
