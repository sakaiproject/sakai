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

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import lombok.Getter;
import lombok.Setter;

/**
 * Extension of UserProfile to provide some additional fields we need during import
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */

public class ImportableUserProfile extends UserProfile {

	private static final long serialVersionUID = 1L;
	
	@Getter @Setter
	private String eid;
	
	@Getter @Setter
	private String officialImageUrl;
	
	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
