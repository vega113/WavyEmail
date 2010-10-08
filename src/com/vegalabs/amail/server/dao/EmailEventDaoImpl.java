package com.vegalabs.amail.server.dao;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import com.google.inject.Inject;
import com.vegalabs.amail.server.model.EmailEvent;

public class EmailEventDaoImpl implements EmailEventDao{
	private final Logger LOG = Logger.getLogger(EmailEventDaoImpl.class.getName());

	private PersistenceManagerFactory pmf = null;

	@Inject
	public EmailEventDaoImpl(PersistenceManagerFactory pmf) {
		this.pmf = pmf;
	}

	@Override
	public EmailEvent save(EmailEvent entry) {
		PersistenceManager pm = pmf.getPersistenceManager();
		try {
			entry = pm.makePersistent(entry);
			entry = pm.detachCopy(entry);
		}catch(Exception e){
			LOG.warning(e.getMessage() + ", " + e.getClass().getName());
			//should send back abort here in case the object is too large (over 1MB)//TODO
		}
		finally {
			pm.close();
		}

		return entry;
	}
	
	
	@Override
	public List<EmailEvent> getEmailEventBySubjectHash(Integer subjectHash) {
		PersistenceManager pm = pmf.getPersistenceManager();
		List<EmailEvent> entries =null;
		try {
			Query query = pm.newQuery(EmailEvent.class);
			//	      query.declareImports("import java.util.Date");
			query.declareParameters("Integer threadSubjectHash_");
			String filters = "subjectHash == subjectHash_";      
			query.setFilter(filters);
			entries = (List<EmailEvent>) query.execute(subjectHash);   
			entries = (List<EmailEvent>) pm.detachCopyAll(entries);
		} finally {
			pm.close();
		}
		return entries;
	}
	
	@Override
	public EmailEvent getEmailEventByHash(Integer subjectHash, Integer msgBodyHash, Date sentDate) {
		PersistenceManager pm = pmf.getPersistenceManager();
		EmailEvent entry =null;
		try {
			Query query = pm.newQuery(EmailEvent.class);
				      query.declareImports("import java.util.Date");
			query.declareParameters("Integer threadSubjectHash_, Integer msgBodyHash_, Date sentDate_");
			String filters = "subjectHash == subjectHash_ && msgBodyHash == msgBodyHash_ && sentDate == sentDate_";      
			query.setFilter(filters);
			List<EmailEvent> entries = (List<EmailEvent>) query.execute(subjectHash, msgBodyHash,sentDate);   
			if(entries.size() > 0){
				entry = entries.get(0);
				entry = pm.detachCopy(entry);
			}
		} finally {
			pm.close();
		}
		return entry;
	}
	
	
	@Override
	public  List<EmailEvent> getEmailEventBySource(String source) {
		PersistenceManager pm = pmf.getPersistenceManager();
		List<EmailEvent> entries =null;
		try {
			Query query = pm.newQuery(EmailEvent.class);
			//	      query.declareImports("import java.util.Date");
			query.declareParameters("String source_");
			String filters = "source == source_";      
			query.setFilter(filters);
			entries = (List<EmailEvent>) query.execute(source);   
			entries = (List<EmailEvent>) pm.detachCopyAll(entries);
		} finally {
			pm.close();
		}
		return entries;
	}
	
	

	
}