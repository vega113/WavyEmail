package com.vegalabs.amail.client.resources;

import com.vegalabs.amail.client.resources.css.GlobalCSS;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface GlobalResources extends ClientBundle {

	@Source("css/globalCSS.css")
	GlobalCSS globalCSS();
	
	@Source("images/remove.png")
	ImageResource remove();
	
	@Source("images/close.jpg")
	ImageResource close();
	
	@Source("images/images3.jpg")
	ImageResource tooltip();

	@Source("images/spinner.1.gif")
	ImageResource spinner();

	@Source("images/contactsgroup.png")
	ImageResource contacts();
}
