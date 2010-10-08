package com.vegalabs.amail.server.dao;

import java.util.List;

import com.vegalabs.amail.server.model.EmailFailedEvent;

public interface EmailFailedEventDao {

	List<EmailFailedEvent> getEmailFailedEventBySource(String source);

	List<EmailFailedEvent> getEmailFailedEventsByStatus(String status);

	EmailFailedEvent save(EmailFailedEvent entry);

	List<EmailFailedEvent> getEmailFailedEventBySourceAndStatus(String source,String status);

}
