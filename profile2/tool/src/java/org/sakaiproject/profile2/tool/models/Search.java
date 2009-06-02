package uk.ac.lancs.e_science.profile2.tool.models;

import java.io.Serializable;

/**
 * Simple model to back a search. To be used only by the Profile2 tool.
 * 
 * <p>DO NOT USE THIS YOURSELF.</p>
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 */
public class Search implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String searchName;
	private String searchInterest;
	
	public void setSearchName(String searchName) {
		this.searchName = searchName;
	}
	public String getSearchName() {
		return searchName;
	}
	public void setSearchInterest(String searchInterest) {
		this.searchInterest = searchInterest;
	}

	public String getSearchInterest() {
		return searchInterest;
	}
	

	
	
	
	/* for the form feedback, to get around a bug in Wicket where it needs a backing model */
	private String searchNameFeedback;
	private String searchInterestFeedback;
	
	public String getSearchNameFeedback() {
		return searchNameFeedback;
	}
	public void setSearchNameFeedback(String searchNameFeedback) {
		this.searchNameFeedback = searchNameFeedback;
	}
	public void setSearchInterestFeedback(String searchInterestFeedback) {
		this.searchInterestFeedback = searchInterestFeedback;
	}
	public String getSearchInterestFeedback() {
		return searchInterestFeedback;
	}

	
	/**
	 * Default constructor
	 */
	public Search() {
	
	}
	
	




}
