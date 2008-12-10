package uk.ac.lancs.e_science.profile2.tool.models;

import java.io.Serializable;

public class UserStatus implements Serializable {

	private String message;
	private String dateStr;
		
	public String getMessage() {
		return this.message;
	}
		
	public void setMessage(String message) {
		this.message=message;
	}

	public void setDateStr(String dateStr) {
		this.dateStr = dateStr;
	}

	public String getDateStr() {
		return dateStr;
	}

	
}
