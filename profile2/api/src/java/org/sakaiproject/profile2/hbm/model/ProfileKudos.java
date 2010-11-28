package org.sakaiproject.profile2.hbm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model for a kudos score for a user - persistent
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileKudos implements Serializable {

	private static final long serialVersionUID = 1L;
	private String userUuid;
	
	/**
	 * Calculated score out of 100, more accurate if you need better reporting.
	 */
	private BigDecimal percentage;
	
	/**
	 * Adjusted score used for display, less accurate, however some items are unattainable depending on who you are
	 * so this is always rounded up and is fairer. 
	 * 
	 * <p>This value is used for display.</p>
	 */
	private int score;
	private Date dateAdded;
	
}
