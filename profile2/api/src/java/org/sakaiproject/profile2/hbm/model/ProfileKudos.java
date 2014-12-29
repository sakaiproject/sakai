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
