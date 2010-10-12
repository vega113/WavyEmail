package com.vegalabs.amail.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.wave.api.Blip;
import com.google.wave.api.BlipData;
import com.google.wave.api.BlipThread;
import com.google.wave.api.DataDocuments;
import com.google.wave.api.Element;
import com.google.wave.api.FormElement;
import com.google.wave.api.Gadget;
import com.google.wave.api.Image;
import com.google.wave.api.Wavelet;
import com.vegalabs.amail.server.WaveMailRobot;
import com.vegalabs.amail.server.data.FullWaveAddress;

@Singleton
public class ExportBlip2HtmlServlet extends HttpServlet {
	Logger LOG = Logger.getLogger(ExportBlip2HtmlServlet.class.getName());
	
	 protected WaveMailRobot robot;
	 protected final static String BASIC_INDENT = "   ";
	
	@Inject
	public void HandleFailedEmailsServlet(WaveMailRobot robot){
		this.robot = robot;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
										throws ServletException, IOException {
		PrintWriter writer = resp.getWriter();
		
		String domain = req.getParameter("domain");
		String waveId = req.getParameter("waveId");
		String blipId = req.getParameter("blipId");
		
		StringBuilder sb = new StringBuilder();
		
		FullWaveAddress fullWaveAddress = new FullWaveAddress(domain, waveId, blipId);
		try{
			Wavelet wavelet = robot.fetchWavelet(domain + "!" + waveId, null);
			Blip blip = wavelet.getBlip(blipId);
			sb.append("Title: " + wavelet.getTitle()+"\n");
			//append blip content
			//append inline blips - indented twice and with (inline)
			//append child blip - indented (recursive)
			//append all blips in the thread after given blip (recursive)
			sb.append("--------------------------\n");
			handleBlip(blip,wavelet,sb,"");
			List<String> theadBlipIds = blip.getThread().getBlipIds();
			boolean isAppend = false;
			for(String threadBlipId : theadBlipIds){
				if(threadBlipId.equals(blipId)){
					isAppend = true;
					continue;
				}
				if(isAppend){
					Blip threadBlip = wavelet.getBlip(threadBlipId);
					sb.append("--------------------------\n");
					handleBlip(threadBlip,wavelet,sb,"");
				}
			}
			
			writer.print("Wavelet contents:\n " + sb.toString());
			writer.flush();
		}catch(Exception e){
			e.printStackTrace(writer);
		}
		
		
		
	}

	private void handleBlip(Blip blip,Wavelet wavelet, StringBuilder sb, String indent) {
		String content = blip.getContent().substring(1);
		List<String> contributorsList = blip.getContributors();
		String byStr = join(contributorsList,",");
		byStr = removeLast(byStr,",");
		sb.append(indent  + "BlipId: " + blip.getBlipId() + " By: " + byStr);
		
		sb.append("\n" + indent + content + "\n");
		
		SortedMap<Integer, Element> elementsMap = blip.getElements();
		for(Integer key : elementsMap.keySet()){
			Element elem = elementsMap.get(key);
			String[] elemArr = handleElement(elem);
		}
		
		//now children blips
		Collection<BlipThread> inlineReplyThreads = blip.getInlineReplyThreads();
		for(BlipThread inlineReplyThread : inlineReplyThreads){
			List<Blip> inlineBlips = inlineReplyThread.getBlips();
			sb.append(indent + BASIC_INDENT +"---------inline thread, location: " + inlineReplyThread.getLocation() + "-----------------\n");
			for(Blip inlineBlip : inlineBlips){
				handleBlip(inlineBlip,wavelet,sb,indent + BASIC_INDENT);
			}
		}
		Collection<BlipThread> replyThreads = blip.getReplyThreads();
		for(BlipThread replyThread : replyThreads){
			List<Blip> replyBlips = replyThread.getBlips();
			sb.append(indent + BASIC_INDENT + "---------reply thread -----------------\n");
			for(Blip replyBlip : replyBlips){
				handleBlip(replyBlip,wavelet,sb,indent + BASIC_INDENT);
			}
		}
	}
	
	private String[] handleElement(Element elem) {
		if(elem.isAttachment()){
			
		}else if(elem.isFormElement()){
			StringBuilder sb = new StringBuilder();
			sb.append("----Form-----\n");
			FormElement formElement = (FormElement)elem;
			String formValue = formElement.getValue();
			Map<String,String> propertiesMap = formElement.getProperties();
			for(String key : propertiesMap.keySet()){
				String value = propertiesMap.get(key);
				sb.append(key + " : " + value + "\n");
			}
			LOG.info(sb.toString());
			
		}else if(elem.isGadget()){
			StringBuilder sb = new StringBuilder();
			sb.append("----Gadget-----\n");
			Gadget gadgetElement = (Gadget)elem;
			String elemValue = gadgetElement.getUrl();
			Map<String,String> propertiesMap = gadgetElement.getProperties();
			for(String key : propertiesMap.keySet()){
				String value = propertiesMap.get(key);
				sb.append(key + " : " + value + "\n");
			}
			LOG.info(sb.toString());
		}else if(elem.isImage()){
			StringBuilder sb = new StringBuilder();
			Image imageElement = (Image)elem;
			sb.append("----Image-----\n");
			String elemValue = imageElement.getUrl();
			Map<String,String> propertiesMap = imageElement.getProperties();
			for(String key : propertiesMap.keySet()){
				String value = propertiesMap.get(key);
				sb.append(key + " : " + value + "\n");
			}
			LOG.info(sb.toString());
		}else if(elem.isInlineBlip()){
			StringBuilder sb = new StringBuilder();
			sb.append("----Inline blip-----\n");
			Map<String,String> propertiesMap = elem.getProperties();
			for(String key : propertiesMap.keySet()){
				String value = propertiesMap.get(key);
				sb.append(key + " : " + value);
				LOG.info(sb.toString());
			}
		}
		
			return null;
	}

	public String join(Iterable<String> list, String ch){
		StringBuilder sb = new StringBuilder();
		for(String str : list){
			sb.append(str + ch);
		}
		return sb.toString();
	}
	
	public String removeLast(String str, String strRem){
		int start = str.lastIndexOf(strRem);
		if(start != -1){
			int length = strRem.length();
			String startStr = str.substring(0,start);
			String endStr = str.substring(start + length,str.length());
			return startStr + endStr;
		}
		return str;
	}

}
