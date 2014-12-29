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
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Hibernate model
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileFriend implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long id;
	private String userUuid;
	private String friendUuid;
	private int relationship;
	private Date requestedDate;
	private boolean confirmed;
	private Date confirmedDate;

	
	/**
	 * Additional constructor that should be used when requesting a friend as it has preinitialised values
	 */
	public ProfileFriend(String userUuid, String friendUuid, int relationship){
		this.userUuid = userUuid;
		this.friendUuid = friendUuid;
		this.relationship = relationship;
		this.requestedDate = new Date();
		this.confirmed = false;
		this.confirmedDate = null;
	}
	
}
