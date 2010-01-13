/**
 * Copyright (c) 2008-2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
