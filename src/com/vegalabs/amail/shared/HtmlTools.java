package com.vegalabs.amail.shared;


public class HtmlTools {
	public static String escape(String text){
		//unescape
		//replace ## < >
		text = escapeXML(text);
		//trim
		//convert to unicode
		//replace \t \r \n
		return text;
	}
	
	public static String unescape(String text){
		//unescape
		//replace ## < >
		text = unescapeXML(text);
		//trim
		//convert to unicode
		//replace \t \r \n
		return text;
	}
	
	public static String escapeXML(String aText){
		String result = aText.replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll("\"","&quot;").replaceAll("\'","&#039;");
		return result;
	}
	
	
	public static String unescapeXML(String aText){
		String result = aText.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&quot;", "\"").replaceAll("&#039;", "\'").replaceAll("&amp;", "&");
		return result;
	}
}
