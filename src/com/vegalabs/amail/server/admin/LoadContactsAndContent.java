package com.vegalabs.amail.server.admin;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.memcache.jsr107cache.GCacheFactory;
import com.google.inject.Inject;
import com.vegalabs.amail.server.WaveMailRobot;
import com.vegalabs.amail.server.dao.EmailEventDao;
import com.vegalabs.amail.server.dao.PersonDao;
import com.vegalabs.amail.server.model.EmailEvent;
import com.vegalabs.amail.server.model.Person;
import com.vegalabs.amail.server.utils.MailUtils;
import com.vegalabs.general.server.command.Command;
import com.vegalabs.general.server.rpc.util.Util;

public class LoadContactsAndContent extends Command{
	
	

	static Logger LOG = Logger.getLogger(LoadContactsAndContent.class.getName());
	  private Util util = null;
	  private WaveMailRobot robot;
	  private PersonDao personDao = null;
	  private EmailEventDao emailEventDao;
	  private PersistenceManagerFactory pmf;
	  private Cache cache;
	  
	  @Inject
	  public LoadContactsAndContent(Util util, WaveMailRobot robot, PersonDao personDao,EmailEventDao emailEventDao, PersistenceManagerFactory pmf) {
	    this.util = util;
	    this.robot = robot;
	    this.personDao = personDao;
	    this.emailEventDao = emailEventDao;
	    this.pmf = pmf;
	    
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
		String personId = this.getParam("personId");
		 LOG.info("Loading LoadContactsAndContent for: " + personId);
	    if (util.isNullOrEmpty(personId)) {
	      throw new IllegalArgumentException("Missing required param: personId");
	    }
	    
	    
	    String emailEventId = this.getParam("emailEventId");
	    if (util.isNullOrEmpty(emailEventId)) {
	      throw new IllegalArgumentException("Missing required param: emailEventId");
	    }
	    
	    
	  
	    
	    PersistenceManager pm = pmf.getPersistenceManager();
	    Person person = pm.getObjectById(Person.class, Long.parseLong(personId));
	    EmailEvent emailEvent = pm.getObjectById(EmailEvent.class, Long.parseLong(emailEventId));
	    
	    
	    String contacts = robot.concatinateContacts(person).toString();
	    
	    String msgBody = emailEvent.getMsgBody().getValue();
	    String subject = emailEvent.getSubject();
	    String from = emailEvent.getFrom().get(0);
	    String toAll = robot.buildAllRecipients(emailEvent.getTo(),"To:").toString();; 
	    String to = person.getWavemail();
	    
	    
	    msgBody = robot.encode(msgBody);
	    subject = robot.encode(subject);
	    from = robot.encode(from);
	    toAll = robot.encode(toAll);
	    to = robot.encode(to);
	    
	    
	    JSONObject json = new JSONObject();
	    JSONObject result = new JSONObject();
	    result.put("contacts", org.json.simple.JSONObject.escape(contacts));
	    result.put("msgBody", org.json.simple.JSONObject.escape(msgBody));
	    result.put("subject", org.json.simple.JSONObject.escape(subject));
	    result.put("from", org.json.simple.JSONObject.escape(from));
	    result.put("toAll", org.json.simple.JSONObject.escape(toAll));
	    result.put("to", org.json.simple.JSONObject.escape(to));
	    
	    
	    json.put("result", result);
	    LOG.info("sending json to client: " + json.toString());
		return json;
	}

}
