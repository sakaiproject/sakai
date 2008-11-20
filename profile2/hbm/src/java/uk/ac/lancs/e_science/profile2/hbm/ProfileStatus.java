package uk.ac.lancs.e_science.profile2.hbm;

import java.util.Date;

import org.apache.log4j.Logger;



public class ProfileStatus {

	private transient Logger log = Logger.getLogger(ProfileStatus.class);

	private long id;
	private String userUuid;
	private String message;
	private Date dateAdded;
	

	public ProfileStatus(){
	}
	
	
	public ProfileStatus(String userUuid, String message, Date dateAdded){
		this.userUuid = userUuid;
		this.message = message;
		this.dateAdded = dateAdded;
	}


	public long getId() {
		return id;
	}


	public void setId(long id) {
		this.id = id;
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
