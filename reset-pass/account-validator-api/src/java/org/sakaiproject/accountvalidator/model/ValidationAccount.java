/**
 * $Id$
 * $URL$
 * 
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.accountvalidator.model;

import java.util.Date;

public class ValidationAccount {

	
	public static final Integer STATUS_SENT = 0;
	public static final Integer STATUS_RESENT = 1;
	public static final Integer STATUS_CONFIRMED = 2;
	public static final Integer STATUS_EXPIRED = 3;
	

	
	/**
	 * This is a token for a new account
	 */
	public static final int ACCOUNT_STATUS_NEW = 1;
	
	/**
	 * This is a token for an existing account
	 */
	public static final int ACCOUNT_STATUS_EXISITING = 2;
	
	/**
	 * A token for the special case of an account that existed
	 * prior to the deployment of the service
	 */
	public static final int ACCOUNT_STATUS_LEGACY = 3;
	
	/**
	 * An un-validated legacy account that does not know their password
	 */
	public static final int ACCOUNT_STATUS_LEGACY_NOPASS = 4;
	
	
	
	/**
	 * Status for a password reset
	 */
	public static final int ACCOUNT_STATUS_PASSWORD_RESET = 5;
	/**
	 * Status for requested accounts (non-mergeable)
	 **/
	public static final int ACCOUNT_STATUS_REQUEST_ACCOUNT = 6;
	/**
	 * Status for userId updation
	 */
	public static final int ACCOUNT_STATUS_USERID_UPDATE = 7;
	
	private Long id;
	private String userId;
	private Date validationSent;
	private Date validationReceived;
	private Integer validationsSent;
	private String validationToken;
	private Integer status;
	private String eid;

	private String firstName = "";
	private String surname = "";
	private Integer accountStatus;
	
	private String password;
	private String password2;
	
	// This needs to support accepting null
	private boolean terms;
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword2() {
		return password2;
	}

	public void setPassword2(String password2) {
		this.password2 = password2;
	}

	public Integer getAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(Integer accountStatus) {
		this.accountStatus = accountStatus;
	}

	public Integer getStatus() {
		return status;
	}
	
	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getFirstName() {
		//avoid NPEs
		if (firstName == null)
		{
			return "";
		}
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getSurname() {
		//avoid NPEs
		if (surname == null)
		{
			return "";
		}
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public void setId(Long i) {
		id = i;
	}
	
	public Long getId(){
		return id;
	}
	
	public void setUserId(String uid) {
		userId = uid;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setValidationSent(Date d) {
		validationSent = d;
	}
	
	public Date getValidationSent() {
		return validationSent;
		
	}
	
	public void setvalidationReceived(Date d) {
		validationReceived = d;
	}
	
	public Date getValidationReceived() {
		return validationReceived;
	}
	
	public void setValidationsSent (Integer i) {
		validationsSent = i;
	}

	public Integer getValidationsSent () {
		return validationsSent;
	}
	
	public void setValidationToken(String t) {
		validationToken = t;
	}
	
	public String getValidationToken() {
		return validationToken;
	}

	public String getEid() {
		return eid;
	}

	public void setEid(String eid) {
		this.eid = eid;
	}
	
	public Boolean getTerms() {
		return terms;
	}

	public void setTerms(Boolean terms) {
		// RSF likes to set things to null
		this.terms = (terms == null)?false:terms;
	}

 }
