package org.sakaiproject.scorm.client;

import wicket.Component;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.model.StringResourceModel;

public class ClientPage extends WebPage {
	protected static final String BODY_ONLOAD_ADDTL="setMainFrameHeight( window.name );";
	
	public void onAttach() {
		getBodyContainer().addOnLoadModifier(BODY_ONLOAD_ADDTL, null);
	}

	protected Label newResourceLabel(String id, Component component) {
		return new Label(id, new StringResourceModel(id, component, null));
	}


}
