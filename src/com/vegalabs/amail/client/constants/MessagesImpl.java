
package com.vegalabs.amail.client.constants;

import java.util.Date;

import com.google.gwt.i18n.client.Messages;

/**
 * Interface used to provide messages with parameters from a properties file.
 *  
 * Instances of this interface can be requested/injected by Gin without an
 * explicit binding: Gin will internallt call GWT.create on the requested type.
 */
public interface MessagesImpl extends Messages {

	String infoReplySent(Date sentReplyDate, String to);
	String loadingForumsMsg(Object reportTabStr, String projectName);
	String infoForwardSent(Date sentReplyDate, String to);

}
