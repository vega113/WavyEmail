
package com.vegalabs.amail.client.inject;

import com.vegalabs.amail.client.resources.GlobalResources;
import com.vegalabs.amail.client.ui.MailForm;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;

@GinModules(GinModuleImpl.class)
public interface GinjectorImpl extends Ginjector {

	GlobalResources getResources();
	MailForm getMailForm();
}
