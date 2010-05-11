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
	 * @return	int or 0 if none. 
	 * 
	 * <p>This is the adjusted score, an integer out of ten.</p>
	 */
	public int getKudos(String userUuid);
	
	/**
	 * Get the kudos rating for a user
	 * @param userUuid	user to get the rating for
	 * @return	BigDecimal or null if none. 
	 * 
	 * <p>This is the more accurate score.</p>
	 */
	public BigDecimal getRawKudos(String userUuid);
	
	/**
	 * Update a user's kudos rating
	 * 
	 * @param userUuid	uuid for the user
	 * @param score		score, already calculated out of ten.
	 * @param percentage	value out of 100, more accurate.

	 * @return
	 */
	public boolean updateKudos(String userUuid, int score, BigDecimal percentage);
}
