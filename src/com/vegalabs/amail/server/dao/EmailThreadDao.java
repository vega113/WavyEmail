package com.vegalabs.amail.server.dao;

import com.vegalabs.amail.server.model.EmailThread;

public interface EmailThreadDao {

	EmailThread save(EmailThread entry);

	EmailThread getEmailThread4Wavemail(Integer threadSubjectHash,
			String wavemail);
	
}
