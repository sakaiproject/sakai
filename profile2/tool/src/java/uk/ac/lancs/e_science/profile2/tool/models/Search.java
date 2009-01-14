package uk.ac.lancs.e_science.profile2.tool.models;

import java.io.Serializable;

public class Search implements Serializable {
	
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

	
	
	
	




}
