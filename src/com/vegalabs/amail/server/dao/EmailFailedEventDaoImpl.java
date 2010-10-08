package com.vegalabs.amail.server.dao;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import com.google.inject.Inject;
import com.vegalabs.amail.server.model.EmailFailedEvent;

public class EmailFailedEventDaoImpl implements EmailFailedEventDao{
	private final Logger LOG = Logger.getLogger(EmailFailedEventDaoImpl.class.getName());

	private PersistenceManagerFactory pmf = null;

	@Inject
	public EmailFailedEventDaoImpl(PersistenceManagerFactory pmf) {
		this.pmf = pmf;
	}

	@Override
	public EmailFailedEvent save(EmailFailedEvent entry) {
		PersistenceManager pm = pmf.getPersistenceManager();
		try {
			entry = pm.makePersistent(entry);
			entry = pm.detachCopy(entry);
		}catch(Exception e){
			LOG.warning(e.getMessage());
		}
		finally {
			pm.close();
		}

		return entry;
	}
	
	
	@Override
	public List<EmailFailedEvent> getEmailFailedEventsByStatus(String status) {
		PersistenceManager pm = pmf.getPersistenceManager();
		List<EmailFailedEvent> entries =null;
		try {
			Query query = pm.newQuery(EmailFailedEvent.class);
			//	      query.declareImports("import java.util.Date");
			query.declareParameters("String status_");
			String filters = "status == status_";      
			query.setFilter(filters);
			entries = (List<EmailFailedEvent>) query.execute(status);   
			entries = (List<EmailFailedEvent>) pm.detachCopyAll(entries);
		} finally {
			pm.close();
		}
		return entries;
	}
	
	
	@Override
	public  List<EmailFailedEvent> getEmailFailedEventBySource(String source) {
		PersistenceManager pm = pmf.getPersistenceManager();
		List<EmailFailedEvent> entries =null;
		try {
			Query query = pm.newQuery(EmailFailedEvent.class);
			//	      query.declareImports("import java.util.Date");
			query.declareParameters("String source_");
			String filters = "source == source_";      
			query.setFilter(filters);
			entries = (List<EmailFailedEvent>) query.execute(source);   
			entries = (List<EmailFailedEvent>) pm.detachCopyAll(entries);
		} finally {
			pm.close();
		}
		return entries;
	}
	
	
	@Override
	public  List<EmailFailedEvent> getEmailFailedEventBySourceAndStatus(String source, String status) {
		PersistenceManager pm = pmf.getPersistenceManager();
		List<EmailFailedEvent> entries =null;
		try {
			Query query = pm.newQuery(EmailFailedEvent.class);
			//	      query.declareImports("import java.util.Date");
			query.declareParameters("String source_, String status_");
			String filters = "source == source_ && status == status_";      
			query.setFilter(filters);
			entries = (List<EmailFailedEvent>) query.execute(source,status);   
			entries = (List<EmailFailedEvent>) pm.detachCopyAll(entries);
		} finally {
			pm.close();
		}
		return entries;
	}
	
	

	
}