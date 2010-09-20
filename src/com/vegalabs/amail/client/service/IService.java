package com.vegalabs.amail.client.service;


import com.google.gwt.http.client.RequestException;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IService {

	void retrPostCounts(String value, AsyncCallback<JSONValue> asyncCallback) throws RequestException;
	void sendEmail(String recipients,String subject,String msgBody,String sender, String senderName, int activityType, String waveId,String blipId, String iconUrl, AsyncCallback<JSONValue> asyncCallback) throws RequestException;
	

}
