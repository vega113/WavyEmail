package com.vegalabs.amail.client.service;

import java.util.Date;
import com.vegalabs.general.client.request.RequestService;
import com.vegalabs.general.client.utils.VegaUtils;
import com.vegalabs.amail.client.constants.ConstantsImpl;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class ServiceImpl implements IService {
	
	private RequestService requestService;
	private String url;
	private VegaUtils utils;
	private ConstantsImpl constants;
	
	@Inject
	public ServiceImpl(RequestService requestService, ConstantsImpl constants, VegaUtils utils){
		this.requestService = requestService;
		this.utils = utils;
		this.constants = constants;
		this.requestService = requestService;
		url = "/admin/jsonrpc" + "?cachebust=" + new Date().getTime();
	}


	@Override
	public void retrPostCounts(String projectId,
			AsyncCallback<JSONValue> asyncCallback) throws RequestException {
		com.google.gwt.json.client.JSONObject paramsJson = new JSONObject();
		com.google.gwt.json.client.JSONObject postDataJson = new JSONObject();
		
		paramsJson.put("projectId", new JSONString(projectId));
		postDataJson.put("params", paramsJson);
		postDataJson.put("method", new JSONString("GET_POST_COUNTS"));
		
		JavaScriptObject params = postDataJson.getJavaScriptObject();
		requestService.makeRequest(url,asyncCallback,params);
		
	}




	@Override
	public void sendEmail(String recipients, String subject, String msgBody,
			String sender, String senderName, int activityType, String waveId,String blipId, String uuid,String iconUrl,
			AsyncCallback<JSONValue> asyncCallback) throws RequestException {
		com.google.gwt.json.client.JSONObject paramsJson = new JSONObject();
		com.google.gwt.json.client.JSONObject postDataJson = new JSONObject();
		
		paramsJson.put("recipients", new JSONString(recipients));
		paramsJson.put("subject", new JSONString(subject));
		paramsJson.put("msgBody", new JSONString(msgBody));
		paramsJson.put("sender", new JSONString(sender));
		paramsJson.put("senderName", new JSONString(senderName));
		paramsJson.put("activityType", new JSONString(String.valueOf(activityType)));
		paramsJson.put("waveId", new JSONString(String.valueOf(waveId)));
		paramsJson.put("blipId", new JSONString(String.valueOf(blipId)));
		paramsJson.put("uuid", new JSONString(String.valueOf(uuid)));
		paramsJson.put("iconUrl", new JSONString(String.valueOf(iconUrl)));
		
		postDataJson.put("params", paramsJson);
		postDataJson.put("method", new JSONString("SEND_EMAIL"));
		
		JavaScriptObject params = postDataJson.getJavaScriptObject();
		requestService.makeRequest(url,asyncCallback,params);
		
	}
	
	
	@Override
	public void loadContacts(String userId, AsyncCallback<JSONValue> asyncCallback) throws RequestException {
		com.google.gwt.json.client.JSONObject paramsJson = new JSONObject();
		com.google.gwt.json.client.JSONObject postDataJson = new JSONObject();
		
		paramsJson.put("userId", new JSONString(userId));
		
		postDataJson.put("params", paramsJson);
		postDataJson.put("method", new JSONString("LOAD_CONTACTS"));
		
		JavaScriptObject params = postDataJson.getJavaScriptObject();
		requestService.makeRequest(url,asyncCallback,params);
		
	}


	@Override
	public void loadContactsAndContent(String personId, String emailEventId,
			AsyncCallback<JSONValue> asyncCallback) throws RequestException {
		com.google.gwt.json.client.JSONObject paramsJson = new JSONObject();
		com.google.gwt.json.client.JSONObject postDataJson = new JSONObject();
		
		
		paramsJson.put("personId", new JSONString(personId));
		paramsJson.put("emailEventId", new JSONString(emailEventId));
		
		postDataJson.put("params", paramsJson);
		postDataJson.put("method", new JSONString("LOAD_CONTACTS_AND_CONTENT"));
		
		JavaScriptObject params = postDataJson.getJavaScriptObject();
		requestService.makeRequest(url,asyncCallback,params);
		
	}

	
}
