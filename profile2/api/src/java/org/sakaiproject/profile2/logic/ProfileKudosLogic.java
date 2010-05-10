package org.sakaiproject.profile2.logic;

import java.math.BigDecimal;

/**
 * An interface for dealing with kudos in Profile2
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * 
 */
public interface ProfileKudosLogic {

	/**
	 * Get the kudos rating for a user
	 * @param userUuid	user to get the rating for
	 * @return	BigDecimal or null if none
	 */
	public BigDecimal getKudos(String userUuid);
	
	/**
	 * Update a user's kudos rating
	 * 
	 * @param userUuid	uuid for the user
	 * @param score		score, already calculated as a percentage.
	 * @return
	 */
	public boolean updateKudos(String userUuid, BigDecimal score);
}
