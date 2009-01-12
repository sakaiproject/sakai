package uk.ac.lancs.e_science.profile2.tool.models;

import java.io.Serializable;

public class Search implements Serializable {
	
	private String searchName;
	
	
	public void setSearchName(String searchName) {
		this.searchName = searchName;
	}

	public String getSearchName() {
		return searchName;
	}

	

	
	
	
	/* for the form feedback, to get around a bug in Wicket where it needs a backing model */
	private String searchNameFeedback;
	public String getSearchNameFeedback() {
		return searchNameFeedback;
	}

	public void setSearchNameFeedback(String searchNameFeedback) {
		this.searchNameFeedback = searchNameFeedback;
	}
	
	
	




}
