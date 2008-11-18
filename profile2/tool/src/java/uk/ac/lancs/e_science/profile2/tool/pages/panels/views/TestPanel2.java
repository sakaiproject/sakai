package uk.ac.lancs.e_science.profile2.tool.pages.panels.views;


import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public class TestPanel2 extends Panel {
	
	
	public TestPanel2(String id, String message) {
		super(id);
		
		add(new Label("message", message));
	}
	
}
