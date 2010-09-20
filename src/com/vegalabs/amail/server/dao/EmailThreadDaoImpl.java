package com.vegalabs.amail.server.dao;

import java.util.List;
import java.util.logging.Logger;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import com.google.inject.Inject;
import com.vegalabs.amail.server.model.EmailThread;

public class EmailThreadDaoImpl implements EmailThreadDao{
	private final Logger LOG = Logger.getLogger(EmailThreadDaoImpl.class.getName());

	private PersistenceManagerFactory pmf = null;

	@Inject
	public EmailThreadDaoImpl(PersistenceManagerFactory pmf) {
		this.pmf = pmf;
	}
	
	@Override
	public EmailThread save(EmailThread entry) {
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
	public EmailThread getEmailThread4Wavemail(Integer threadSubjectHash, String wavemail) {
		PersistenceManager pm = pmf.getPersistenceManager();
		EmailThread entry =null;
		try {
			Query query = pm.newQuery(EmailThread.class);
			//	      query.declareImports("import java.util.Date");
			query.declareParameters("String wavemail_, Integer threadSubjectHash_");
			String filters = "wavemail == wavemail_ && threadSubjectHash == threadSubjectHash_";      
			query.setFilter(filters);
			List<EmailThread> entries = (List<EmailThread>) query.execute(wavemail,threadSubjectHash);
			if(entries.size() > 0){
				entry = entries.get(0);
				entry = pm.detachCopy(entry);
			}
			
		} finally {
			pm.close();
		}
		return entry;
	}
	
	

	
}