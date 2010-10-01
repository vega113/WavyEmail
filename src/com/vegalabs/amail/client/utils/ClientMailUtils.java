package com.vegalabs.amail.client.utils;

public class ClientMailUtils {
	public static String waveId2mailId(String sender, String appDomainId) {
		String[] senderSplit = sender.split("@");
	    String userId = senderSplit[0];
	    String domain = senderSplit[1];
	    String appSender = userId + "-" + domain + "@" + appDomainId + ".appspotmail.com";
		return appSender;
	}
	
	public static final String encodeBase64(String data){
//		String out = _utf8_encode(data);
//		return  encode_base64(out);
		return  encode_base64(data);
	}
	
	
	public static final String decodeBase64(String data){
		String out = decode_base64(data);
//		return  _utf8_decode(out);
		return  out;
	}
	
	private static  final native String encode_base64(String data) /*-{
		var out = "", c1, c2, c3, e1, e2, e3, e4;
		var tab = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
	    for (var i = 0; i < data.length; ) {
	       c1 = data.charCodeAt(i++);
	       c2 = data.charCodeAt(i++);
	       c3 = data.charCodeAt(i++);
	       e1 = c1 >> 2;
	       e2 = ((c1 & 3) << 4) + (c2 >> 4);
	       e3 = ((c2 & 15) << 2) + (c3 >> 6);
	       e4 = c3 & 63;
	       if (isNaN(c2))
	         e3 = e4 = 64;
	       else if (isNaN(c3))
	         e4 = 64;
	       out += tab.charAt(e1) + tab.charAt(e2) + tab.charAt(e3) + tab.charAt(e4);
	    }
	    return out;
	}-*/;
	
	
	private static final native String decode_base64(String data) /*-{
		var out = "", c1, c2, c3, e1, e2, e3, e4;
		var tab = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
	    for (var i = 0; i < data.length; ) {
	      e1 = tab.indexOf(data.charAt(i++));
	      e2 = tab.indexOf(data.charAt(i++));
	      e3 = tab.indexOf(data.charAt(i++));
	      e4 = tab.indexOf(data.charAt(i++));
	      c1 = (e1 << 2) + (e2 >> 4);
	      c2 = ((e2 & 15) << 4) + (e3 >> 2);
	      c3 = ((e3 & 3) << 6) + e4;
	      out += String.fromCharCode(c1);
	      if (e3 != 64)
	        out += String.fromCharCode(c2);
	      if (e4 != 64)
	        out += String.fromCharCode(c3);
	    }
	    return out;
	}-*/;
	
	
	
	
	
	
	// private method for UTF-8 encoding
	private static final native String _utf8_encode(String data) /*-{
		string = string.replace(/\r\n/g,"\n");
		var utftext = "";
 
		for (var n = 0; n < string.length; n++) {
 
			var c = string.charCodeAt(n);
 
			if (c < 128) {
				utftext += String.fromCharCode(c);
			}
			else if((c > 127) && (c < 2048)) {
				utftext += String.fromCharCode((c >> 6) | 192);
				utftext += String.fromCharCode((c & 63) | 128);
			}
			else {
				utftext += String.fromCharCode((c >> 12) | 224);
				utftext += String.fromCharCode(((c >> 6) & 63) | 128);
				utftext += String.fromCharCode((c & 63) | 128);
			}
 
		}
 
		return utftext;
	}-*/;
 
	// private method for UTF-8 decoding
	public static final native String _utf8_decode(String utftext) /*-{
		var string = "";
		var i = 0;
		var c = c1 = c2 = 0;
 
		while ( i < utftext.length ) {
 
			c = utftext.charCodeAt(i);
 
			if (c < 128) {
				string += String.fromCharCode(c);
				i++;
			}
			else if((c > 191) && (c < 224)) {
				c2 = utftext.charCodeAt(i+1);
				string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
				i += 2;
			}
			else {
				c2 = utftext.charCodeAt(i+1);
				c3 = utftext.charCodeAt(i+2);
				string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
				i += 3;
			}
 
		}
 
		return string;
	}-*/;
	
}
