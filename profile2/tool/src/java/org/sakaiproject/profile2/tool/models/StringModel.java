package org.sakaiproject.profile2.tool.models;

import java.io.Serializable;

/**
 * Simple model to back a simple single text field. To be used only by the Profile2 tool.
 * 
 * <p>DO NOT USE THIS YOURSELF.</p>
 *
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 */
public class StringModel implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String string;
	
	public void setString(String string) {
		this.string = string;
	}
	public String getString() {
		return string;
	}
	
	
	/**
	 * Default constructor
	 */
	public StringModel() {
	
	}
	
	




}
