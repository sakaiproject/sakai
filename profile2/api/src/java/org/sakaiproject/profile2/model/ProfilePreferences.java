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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Hibernate and EntityProvider model
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class ProfilePreferences implements Serializable {

	private static final long serialVersionUID = 1L;

	@EqualsAndHashCode.Include
	private String userUuid;
	private boolean requestEmailEnabled;
	private boolean confirmEmailEnabled;
	private boolean messageNewEmailEnabled;
	private boolean messageReplyEmailEnabled;
	private boolean useOfficialImage;
	private boolean showKudos;
	private boolean showGalleryFeed;
	private boolean useGravatar;
	private boolean wallItemNewEmailEnabled;
	private boolean worksiteNewEmailEnabled;
	private boolean showOnlineStatus;

}
