package uk.ac.lancs.e_science.profile2.hbm;

import java.io.Serializable;
import java.util.Date;



public class ProfileStatus implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String userUuid;
	private String message;
	private Date dateAdded;
	//private int cleared; //maybe to hold value if the status has been cleared
	

	public ProfileStatus(){
	}
	
	
	public ProfileStatus(String userUuid, String message, Date dateAdded){
		this.userUuid = userUuid;
		this.message = message;
		this.dateAdded = dateAdded;
	}


	public String getUserUuid() {
		return userUuid;
	}


	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public Date getDateAdded() {
		return dateAdded;
	}


	public void setDateAdded(Date dateAdded) {
		this.dateAdded = dateAdded;
	}
	
	
	
}
