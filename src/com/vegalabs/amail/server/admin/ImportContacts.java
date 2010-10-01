package com.vegalabs.amail.server.admin;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gdata.client.contacts.ContactQuery;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gwt.json.client.JSONString;
import com.google.inject.Inject;
import com.vegalabs.amail.client.utils.ClientMailUtils;
import com.vegalabs.amail.server.WaveMailRobot;
import com.vegalabs.amail.server.dao.PersonDao;
import com.vegalabs.amail.server.dao.TokenDao;
import com.vegalabs.amail.server.model.Person;
import com.vegalabs.amail.server.model.TokenData;
import com.vegalabs.amail.server.utils.MailUtils;
import com.vegalabs.general.server.command.Command;
import com.vegalabs.general.server.rpc.util.Util;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.util.ServiceException;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.Email;


public class ImportContacts extends Command{

	static Logger LOG = Logger.getLogger(ImportContacts.class.getName());
	  private Util util = null;
	  private WaveMailRobot robot;
	  private PersonDao personDao;
	  private TokenDao tokenDao;
	  
	  @Inject
	  public ImportContacts(Util util, WaveMailRobot robot, PersonDao personDao, TokenDao tokenDao) {
	    this.util = util;
	    this.robot = robot;
	    this.personDao = personDao;
	    this.tokenDao = tokenDao;
	  }
	  
	  
	@Override
	public JSONObject execute() throws JSONException {
		String userId = this.getParam("userId");
	    if (util.isNullOrEmpty(userId)) {
	      throw new IllegalArgumentException("Missing required param: userId");
	    }
	    String wavemail = MailUtils.waveId2mailId(userId);
	    TokenData token = tokenDao.retrTokenByUser(userId);
	    if(token == null){
	    	throw new IllegalArgumentException("No token for user: " + userId);
	    }
	    Set<String> emails = null;
	    try {
			ContactFeed contactFeed = getContactList(token.getToken());
			emails = retrEmailAdresses(contactFeed);
		} catch (IOException e) {
			 LOG.log(Level.SEVERE, "", e);
		} catch (GeneralSecurityException e) {
			 LOG.log(Level.SEVERE, "", e);
		} catch (ServiceException e) {
			 LOG.log(Level.SEVERE, "", e);
		}
	    //not connect to the contacts feed and import contacts
	    
		Person person = personDao.getPerson(wavemail);
		if(person == null){
			person = new Person(wavemail);
		}
		
		for(String email : emails){
	    	String onlyMail = MailUtils.stripRecipientForEmail(email);
	    	String onlyName = MailUtils.stripRecipientForName(email);
	    	try{
	    		if(onlyMail != null && !"".equals(onlyMail)){
		    		person.getContacts().put(onlyMail, email);
		    	}
		    	if(onlyName != null && !"".equals(onlyName)){
		    		person.getContactsName().put(onlyName, email);
		    	}
	    	}catch(Exception e){
	    		LOG.log(Level.WARNING, "", e);
	    	}
	    	
	    }
		person.setUpdated(new Date());
		personDao.save(person);
	    
//	    String contacts = robot.retrContacts4User();
	    JSONObject json = new JSONObject();
	    JSONObject result = new JSONObject();
	    result.put("success", "true");
	    json.put("result", result);
		return json;
	}
	
	
	/**

     * In this api we are setting the authentication token for the particular
     * user and calling the google service
     * @param token
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws ServiceException
     */

    public ContactFeed getContactList(String token) throws IOException,GeneralSecurityException, ServiceException {
        ContactFeed contactFeed = null;
        try {
            ContactsService contactsService = new ContactsService("MailWavy-beta 0.003");
            contactsService.setAuthSubToken(token);
            contactFeed = getAllContacts(contactsService);
        } catch (Exception e) {
             LOG.log(Level.SEVERE, "", e);

        } catch (Error error) {
        	 LOG.log(Level.SEVERE, "", error);
        }
        return contactFeed;
    }

 

    /**

     * Set other information regarding the email list and get the contact
     * feed list
     * @param myService
     * @throws ServiceException
     * @throws IOException
     */

    private ContactFeed getAllContacts(ContactsService myService) throws ServiceException, IOException {
        URL feedUrl = new URL(
                "http://www.google.com/m8/feeds/contacts/default/full");
        ContactQuery contactQuery = new ContactQuery(feedUrl);
        contactQuery.setMaxResults(400);
        ContactFeed resultFeed = myService.getFeed(contactQuery, ContactFeed.class);
        return resultFeed;
    }

    public Set<String> retrEmailAdresses(ContactFeed resultFeed){
    	Set<String> emailsSet = new LinkedHashSet<String>();
    	for (int i = 0; i < resultFeed.getEntries().size(); i++) {

    		ContactEntry entry = resultFeed.getEntries().get(i);
    		String entryTitle = entry.getTitle().getPlainText();
    		LOG.info("Entry title: " + entryTitle);
    		for (Email email : entry.getEmailAddresses()) {
    			String emailAddress = email.getAddress();
    			String emailAddrs = entryTitle != null && !"".equals(entryTitle) ?  entryTitle + "<" + email.getAddress() + ">" : email.getAddress();
    			emailsSet.add(emailAddrs);
    			LOG.info("emailAddress: " + emailAddress);
    			if(email.getRel() != null){
    				if (email.getLabel() != null){
    					LOG.info("Label: " + email.getLabel());
    				}
    			}
    		}
    	}
    	return emailsSet;
    }

}
