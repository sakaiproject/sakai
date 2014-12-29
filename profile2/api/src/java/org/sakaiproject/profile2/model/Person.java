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
package org.sakaiproject.profile2.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/** 
 * This the main object that represents a full person in Profile2. It is essentially a wrapper object around several other objects and data.
 * <p>See BasicPerson for the basic attributes like uuid, name, etc.
 * <p>All fields in Person will be set at instantiation time, however if any are null, this is a true error and should be handled by throwing the
 * appropriate exception.
 *  
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@NoArgsConstructor
public class Person extends BasicPerson implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Getter @Setter
	private UserProfile profile;
	
	@Getter @Setter
	private ProfilePrivacy privacy;
	
	@Getter @Setter
	private ProfilePreferences preferences;
	
}
