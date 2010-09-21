package com.vegalabs.amail.client;

import com.vegalabs.amail.client.inject.GinjectorImpl;
import com.vegalabs.amail.client.ui.MailForm;
import com.vegalabs.amail.shared.FieldVerifier;
import com.allen_sauer.gwt.log.client.DivLogger;
import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class WaveMailGadget implements EntryPoint {

	public void onModuleLoad() {
		GinjectorImpl ginjector = GWT.create(GinjectorImpl.class);
		MailForm widget = ginjector.getMailForm();
	    RootPanel.get("mainPanel").add(widget);
	    initRemoteLogger(RootPanel.get("logPanel"));
	}
	
	public void initRemoteLogger(AbsolutePanel panel){
		Log.setUncaughtExceptionHandler();
		if (panel != null) {
			panel.add (Log.getLogger(DivLogger.class).getWidget());
			Log.info("Logger initialized: " + Log.class.getName());
		}
	}
	
}