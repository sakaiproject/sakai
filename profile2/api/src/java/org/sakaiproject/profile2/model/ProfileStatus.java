package org.sakaiproject.profile2.model;

import java.io.Serializable;
import java.util.Date;

import org.sakaiproject.entitybroker.entityprovider.annotations.EntityId;


/**
 * Hibernate and EntityProvider model
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfileStatus implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@EntityId
	private String userUuid;
	private String message;
	private Date dateAdded;
	private String dateFormatted; //not persisted, convenience holder
	//private int cleared; //maybe to hold value if the status has been cleared
	
	/**
	 * Empty constructor
	 */
	public ProfileStatus(){
	}
	
	/** 
	 * Constructor to create a status object in one go
	 */
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

	public void setDateFormatted(String dateFormatted) {
		this.dateFormatted = dateFormatted;
	}

	public String getDateFormatted() {
		return dateFormatted;
	}
	
	
	
}
