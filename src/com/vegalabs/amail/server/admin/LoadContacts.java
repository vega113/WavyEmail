package com.vegalabs.amail.server.admin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.memcache.jsr107cache.GCacheFactory;
import com.google.inject.Inject;
import com.vegalabs.amail.server.WaveMailRobot;
import com.vegalabs.amail.server.utils.MailUtils;
import com.vegalabs.general.server.command.Command;
import com.vegalabs.general.server.rpc.util.Util;

public class LoadContacts extends Command{
	
	

	static Logger LOG = Logger.getLogger(LoadContacts.class.getName());
	  private Util util = null;
	  private WaveMailRobot robot;
	  private Cache cache;
	  
	  @Inject
	  public LoadContacts(Util util, WaveMailRobot robot) {
	    this.util = util;
	    this.robot = robot;
	    
	    
	    try {
	    	Map<String, Integer> props = new HashMap<String, Integer>();
	        props.put(GCacheFactory.EXPIRATION_DELTA, 60*60);
	        cache = CacheManager.getInstance().getCacheFactory().createCache(props);
	    } catch (CacheException e) {
	        LOG.log(Level.SEVERE,"cache init",e);
	    }
	    
	  }
	  
	@Override
	public JSONObject execute() throws JSONException {
		String userId = this.getParam("userId");
		 LOG.fine("Loading contacts for: " + userId);
	    if (util.isNullOrEmpty(userId)) {
	      throw new IllegalArgumentException("Missing required param: userId");
	    }
	   
	    String contacts = "";
	    Object o = cache.get("contacts#"+userId);
	    if(o != null){
	    	contacts = (String)o;
	    }else{
	    	try{
		    	contacts = robot.retrContacts4User(MailUtils.waveId2mailId(userId));
		    	cache.put("contacts#"+userId,contacts);
		    }catch(Exception e){
		    	LOG.log(Level.SEVERE, userId, e);
		    }
	    }
	    
	    JSONObject json = new JSONObject();
	    JSONObject result = new JSONObject();
	    contacts = robot.encode(contacts);
	    result.put("contacts", contacts);
	    json.put("result", result);
		return json;
	}

}
