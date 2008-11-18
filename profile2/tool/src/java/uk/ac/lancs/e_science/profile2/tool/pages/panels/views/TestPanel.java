package uk.ac.lancs.e_science.profile2.tool.pages.panels.views;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;


public class TestPanel extends Panel {
	
	
	public TestPanel(String id, String message) {
		super(id);
		
		add(new Label("message", message));
	}
	
}
