/**
 * Copyright (c) 2008-2012 The Sakai Foundation
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
