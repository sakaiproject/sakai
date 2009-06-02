package org.sakaiproject.profile2.tool.models;

import java.io.Serializable;

/**
 * Simple model to back a simple single text field. To be used only by the Profile2 tool.
 * 
 * <p>DO NOT USE THIS YOURSELF.</p>
 *
 * TODO make this model replace the Search model
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 */
public class SimpleText implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String text;
	
	public void setText(String text) {
		this.text = text;
	}
	public String getText() {
		return text;
	}
	
	
	
	
	/* for the form feedback, to get around a bug in Wicket where it needs a backing model */
	private String textFeedback;
	
	public String getTextFeedback() {
		return textFeedback;
	}
	public void setTextFeedback(String textFeedback) {
		this.textFeedback = textFeedback;
	}
	

	
	/**
	 * Default constructor
	 */
	public SimpleText() {
	
	}
	
	




}
