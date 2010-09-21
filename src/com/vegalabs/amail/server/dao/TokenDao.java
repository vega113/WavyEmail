package com.vegalabs.amail.server.dao;

import com.vegalabs.amail.server.model.TokenData;

public interface TokenDao {

	public abstract TokenData save(TokenData tokenData);

	public abstract TokenData retrTokenByUser(String user);

}