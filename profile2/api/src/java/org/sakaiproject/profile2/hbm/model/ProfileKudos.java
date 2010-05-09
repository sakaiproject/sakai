package org.sakaiproject.profile2.hbm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Model for a kudos score for a user - persistent
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class ProfileKudos implements Serializable {

	private static final long serialVersionUID = 1L;
	private String userUuid;
	private BigDecimal score;
	private Date dateAdded;
	
	/** 
	 * Empty constructor
	 */
	public ProfileKudos() {		
	}

	public String getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}

	public BigDecimal getScore() {
		return score;
	}

	public void setScore(BigDecimal score) {
		this.score = score;
	}

	public Date getDateAdded() {
		return dateAdded;
	}

	public void setDateAdded(Date dateAdded) {
		this.dateAdded = dateAdded;
	}
	
}
