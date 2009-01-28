package uk.ac.lancs.e_science.profile2.tool.components;

import org.apache.wicket.markup.html.panel.Panel;

/*
 * BlankPanel.java
 * 
 * This does absolutely nothing except be a blank panel when an expensive one needs to be added conditionally.
 * This is added in its place to save instantiating the other one and the just hiding it
 * 
 */

public class BlankPanel extends Panel {
	
	private static final long serialVersionUID = 1L;

	public BlankPanel(String id) {
		super(id);
	}
	
	
	
}
