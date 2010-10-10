package com.vegalabs.amail.shared;

//import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.vegalabs.amail.server.WaveMailRobot;

/**
 * Method convert(String str)
 * Implemented as a class method.
 * 
 * Takes one argument: a string that may contain international characters.
 * Returns: a string with the international characters converted to hex-encoded unicode.
 * A hex-encoded unicode character has this format: \ u x x x x (spaces added for emphasis)
 * where xxxx is the hex-encoded value of the character. The xxxx part is
 * always 4 digits, 0 filled to make 4 digits.
 * 
 * Example input/output:
 * 		Input string:   Constitución
 * 		Encoded output: Constituci\u00f3n
 * 
 * Example call: String term = unicodeString.convert("Constitución"); 
 */

;

public class UnicodeString
{
	private static final Logger LOG = Logger.getLogger(UnicodeString.class.getName());
	public static String convert(String str)
	{
		
		StringBuffer ostr = new StringBuffer();

		for(int i=0; i<str.length(); i++) 
		{
			char ch = str.charAt(i);
			if(ch == '\n' || ch == '\r' || ch == '\t')
				continue;

			if ((ch >= 0x0020) && (ch <= 0x007e))	// Does the char need to be converted to unicode? 
			{
				ostr.append(ch);					// No.
			}else
			{

				StringBuilder newChar = new StringBuilder("\\u");
//				ostr.append("\\u") ;				// standard unicode format.
//				String hex = Integer.toHexString(str.charAt(i) & 0xFFFF);	// Get hex value of the char. 
				String hex = UnicodeFormatter.charToHex(str.charAt(i));	// Get hex value of the char. 
				hex = hex.trim();
				if(hex.length() == 0 || ((int)str.charAt(i)) == 0){
					LOG.warning("toHexString for" + ((int)str.charAt(i)) + " is " + hex);
					continue;
				}
				for(int j=0; j<4-hex.length(); j++){	// Prepend zeros because unicode requires 4 digits
					newChar.append("0");
				}
				
				newChar.append(hex.toLowerCase());		// standard unicode format.
				
				if(newChar.length() != 6){
					LOG.warning("newChar " + newChar);
					continue;
				}
				if(newChar.toString().contains ("u0000")){
					LOG.warning("convert " + str.charAt(i) + " : " + newChar);
					continue;
				}
				ostr.append(newChar);
			
				
				
			}
		}
		return ostr.toString();		//Return the stringbuffer cast as a string.
	}
	
	
	public static String deconvert(String str){
		if(str == null){
			return "";
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.indexOf("\\u") > -1){
			int start = sb.indexOf("\\u");
			int end = start + 6;
			Character ch = null;
			String uni = sb.substring(start, end);
			String hStr = "0x" + uni.substring(2,6);
			Integer hint = Integer.decode(hStr);
			ch = (char)hint.intValue();
			int startUni = sb.indexOf(uni);
			int endUni = startUni + 6;
			sb.delete(startUni, endUni);
			sb.insert(startUni, ch);
		}
		return sb.toString();
	}


	public static List<String> convert(Iterable<String> fromList) {
		List<String> list = new ArrayList<String>();
		for(String from : fromList){
			list.add(convert(from));
		}
		return list;
	}
	
	public static Map<Character,String> hebrewUniCharsMap = initHebrewUniCharsMap();
	public static Map<String,Character> uniHebrewCharsMap = initUniHebrewCharsMap();
	
	public static Map<Character,String>  initHebrewUniCharsMap(){
		Map<Character,String> map = new HashMap<Character,String>();
		map.put('א', "\\u05d0");
		map.put('ב', "\\u05d1");
		map.put('ג', "\\u05d2");
		map.put('ד', "\\u05d3");
		map.put('ה', "\\u05d4");
		map.put('ו', "\\u05d5");
		map.put('ז', "\\u05d6");
		map.put('ח', "\\u05d7");
		map.put('ט', "\\u05d8");
		map.put('י', "\\u05d9");
		map.put('ך', "\\u05da");
		map.put('כ', "\\u05db");
		map.put('ל', "\\u05dc");
		map.put('ם', "\\u05dd");
		map.put('מ', "\\u05de");
		map.put('ן', "\\u05df");
		map.put('נ', "\\u05e0");
		map.put('ס', "\\u05e1");
		map.put('ע', "\\u05e2");
		map.put('ף', "\\u05e3");
		map.put('פ', "\\u05e4");
		map.put('ץ', "\\u05e5");
		map.put('צ', "\\u05e6");
		map.put('ק', "\\u05e7");
		map.put('ר', "\\u05e8");
		map.put('ש', "\\u05e9");
		map.put('ת', "\\u05ea");
		
		return map;
	}
	
	
	public static Map<String,Character>  initUniHebrewCharsMap(){
		Map<String,Character> map = new HashMap<String,Character>();
		map.put("\\u05d0",'א');
		map.put("\\u05d1", 'ב');
		map.put("\\u05d2",'ג');
		map.put("\\u05d3",'ד');
		map.put("\\u05d4",'ה');
		map.put("\\u05d5",'ו');
		map.put("\\u05d6",'ז');
		map.put("\\u05d7",'ח');
		map.put("\\u05d8",'ט');
		map.put("\\u05d9",'י');
		map.put("\\u05da",'ך');
		map.put("\\u05db",'כ');
		map.put("\\u05dc",'ל');
		map.put("\\u05dd",'ם');
		map.put("\\u05de",'מ');
		map.put("\\u05df",'ן');
		map.put("\\u05e0",'נ');
		map.put("\\u05e1",'ס');
		map.put("\\u05e2",'ע');
		map.put("\\u05e3",'ף');
		map.put("\\u05e4",'פ');
		map.put("\\u05e5",'ץ');
		map.put("\\u05e6",'צ');
		map.put("\\u05e7",'ק');
		map.put("\\u05e8",'ר');
		map.put("\\u05e9",'ש');
		map.put("\\u05ea",'ת');
		
		return map;
	}

}
