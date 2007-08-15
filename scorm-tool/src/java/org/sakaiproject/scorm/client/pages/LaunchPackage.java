package org.sakaiproject.scorm.client.pages;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

public class LaunchPackage extends WebPage {
	private static final String BODY_ONLOAD_ADDTL="setMainFrameHeight( window.name );";
	
	public LaunchPackage(String fileName) {
		add(new Label("fileName", fileName));
	}
	
	public void onAttach() {
		//getBodyContainer().addOnLoadModifier(BODY_ONLOAD_ADDTL, null);
	}
	
}
