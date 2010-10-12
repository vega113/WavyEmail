package com.vegalabs.amail.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.vegalabs.amail.server.WaveMailRobot;
import com.vegalabs.amail.server.dao.EmailFailedEventDao;
import com.vegalabs.amail.server.model.EmailEvent;
import com.vegalabs.amail.server.model.EmailFailedEvent;
import com.vegalabs.amail.server.model.Person;

@Singleton
public class HandleFailedEmailsServlet extends HttpServlet {
	Logger LOG = Logger.getLogger(HandleFailedEmailsServlet.class.getName());
	
	 protected WaveMailRobot robot;
	 protected EmailFailedEventDao  emailFailedEventDao;
	 protected PersistenceManagerFactory pmf;
	
	@Inject
	public void HandleFailedEmailsServlet(WaveMailRobot robot, EmailFailedEventDao  emailFailedEventDao, PersistenceManagerFactory pmf){
		this.robot = robot;
		this.emailFailedEventDao = emailFailedEventDao;
		this.pmf = pmf;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
										throws ServletException, IOException {
		StringBuilder report = new StringBuilder();
		LOG.info("entering");
		List<EmailFailedEvent> failedEmailsList = emailFailedEventDao.getEmailFailedEventsByStatus("FAILED");
		for(EmailFailedEvent emailFailedEvent : failedEmailsList){
			if(emailFailedEvent.getAction() != null && !"RECEIVE".equals(emailFailedEvent.getAction())){
				continue;
			}
			report.append("\n");
			Long personId = emailFailedEvent.getPersonId();
			Long emailEventId = emailFailedEvent.getEmailEventId();
			if(personId == null || emailEventId == null){
				emailFailedEvent.setStatus("ABORTED");//TODO send bounce
				emailFailedEvent.setLastUpdated(new Date());
				emailFailedEventDao.save(emailFailedEvent);
			}else{
				//TODO if retryCount more than limit - declare as bounced
				PersistenceManager pm = pmf.getPersistenceManager();
				Person person = pm.getObjectById(Person.class, personId);
				person = pm.detachCopy(person);
				
				EmailEvent emailEvent = pm.getObjectById(EmailEvent.class, emailEventId);
				emailEvent = pm.detachCopy(emailEvent);
				String eventReport = "retry for "  + person.getWavemail() + ", Subject: " + emailEvent.getSubject() + ", retryCount: " + emailFailedEvent.getRetryCount();
				LOG.info(eventReport);
				report.append(eventReport);
				robot.receiveEmailWithRetry(person, emailEvent, emailFailedEvent);
			}
			
			
			
		}
		
		PrintWriter writer = resp.getWriter();
		writer.print("Report: " + report.toString());
		writer.flush();
	}

}
