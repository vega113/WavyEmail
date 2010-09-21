package com.vegalabs.amail.server.admin;

import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gwt.json.client.JSONString;
import com.google.inject.Inject;
import com.vegalabs.amail.client.utils.ClientMailUtils;
import com.vegalabs.amail.server.WaveMailRobot;
import com.vegalabs.amail.server.utils.MailUtils;
import com.vegalabs.general.server.command.Command;
import com.vegalabs.general.server.rpc.util.Util;

public class LoadContacts extends Command{

	static Logger LOG = Logger.getLogger(SendEmail.class.getName());
	  private Util util = null;
	  private WaveMailRobot robot;
	  
	  @Inject
	  public LoadContacts(Util util, WaveMailRobot robot) {
	    this.util = util;
	    this.robot = robot;
	  }
	  
	  
	@Override
	public JSONObject execute() throws JSONException {
		String userId = this.getParam("userId");
	    if (util.isNullOrEmpty(userId)) {
	      throw new IllegalArgumentException("Missing required param: userId");
	    }
	    String contacts = robot.retrContacts4User(MailUtils.waveId2mailId(userId));
	    JSONObject json = new JSONObject();
	    JSONObject result = new JSONObject();
	    result.put("contacts", contacts);
	    json.put("result", result);
		return json;
	}

}
