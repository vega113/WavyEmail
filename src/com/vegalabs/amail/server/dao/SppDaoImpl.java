package com.vegalabs.amail.server.dao;


import java.util.List;
import java.util.logging.Logger;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import com.google.inject.Inject;
import com.vegalabs.amail.server.model.SeriallizableParticipantProfile;

public class SppDaoImpl implements SeriallizableParticipantProfileDao{
	private final Logger LOG = Logger.getLogger(SppDaoImpl.class.getName());

	private PersistenceManagerFactory pmf = null;

	@Inject
	public SppDaoImpl(PersistenceManagerFactory pmf) {
		this.pmf = pmf;
	}

	@Override
	public SeriallizableParticipantProfile save(SeriallizableParticipantProfile entry) {
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
	public SeriallizableParticipantProfile getSeriallizableParticipantProfile(String email) {
		PersistenceManager pm = pmf.getPersistenceManager();
		SeriallizableParticipantProfile entry =null;
		try {
			Query query = pm.newQuery(SeriallizableParticipantProfile.class);
			//	      query.declareImports("import java.util.Date");
			query.declareParameters("String email_");
			String filters = "email == email_";      
			query.setFilter(filters);
			List<SeriallizableParticipantProfile> entries = (List<SeriallizableParticipantProfile>) query.execute(email);
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
