package com.vegalabs.amail.server.dao;

import java.util.List;
import java.util.logging.Logger;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import com.google.inject.Inject;
import com.vegalabs.amail.server.model.Person;

public class PersonDaoImpl implements PersonDao{
	private final Logger LOG = Logger.getLogger(PersonDaoImpl.class.getName());

	private PersistenceManagerFactory pmf = null;

	@Inject
	public PersonDaoImpl(PersistenceManagerFactory pmf) {
		this.pmf = pmf;
	}

	@Override
	public Person save(Person entry) {
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
	public Person getPerson(String wavemail) {
		PersistenceManager pm = pmf.getPersistenceManager();
		Person entry =null;
		try {
			Query query = pm.newQuery(Person.class);
			//	      query.declareImports("import java.util.Date");
			query.declareParameters("String wavemail_");
			String filters = "wavemail == wavemail_";      
			query.setFilter(filters);
			List<Person> entries = (List<Person>) query.execute(wavemail);
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

