package com.vegalabs.amail.server.dao;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import com.vegalabs.amail.server.model.TokenData;
import com.google.inject.Inject;

public class TokenDaoImpl implements TokenDao {
	private PersistenceManagerFactory pmf = null;

	@Inject
	public TokenDaoImpl(PersistenceManagerFactory pmf) {
		this.pmf = pmf;
	}
	
	/* (non-Javadoc)
	 * @see com.vegalabs.amail.server.dao.TokenDao#save(com.vegalabs.amail.server.model.TokenData)
	 */
	public TokenData save(TokenData tokenData) {
		PersistenceManager pm = pmf.getPersistenceManager();
		try {
			tokenData = pm.makePersistent(tokenData);
			tokenData = pm.detachCopy(tokenData);
		} finally {
			pm.close();
		}
		return tokenData;
	}
	
	/* (non-Javadoc)
	 * @see com.vegalabs.amail.server.dao.TokenDao#retrTokenByUser(java.lang.String)
	 */
	public TokenData retrTokenByUser(String user){
		TokenData tokenData = null;
		PersistenceManager pm = pmf.getPersistenceManager();
		try{
			tokenData = pm.getObjectById(TokenData.class,user);
			tokenData = pm.detachCopy(tokenData);
		}catch(Exception e){
		}finally {
			pm.close();
		}
		return tokenData;
	}
}
