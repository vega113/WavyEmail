package com.vegalabs.amail.server.dao;

import java.util.Date;
import java.util.List;

import com.vegalabs.amail.server.model.EmailEvent;

public interface EmailEventDao {

	EmailEvent save(EmailEvent entry);


	EmailEvent getEmailEventByHash(Integer threadSubjectHash, Integer msgBodyHash, Date sentDate);

	List<EmailEvent> getEmailEventBySource(String source);

	List<EmailEvent> getEmailEventBySubjectHash(Integer subjectHash);

}
