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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This is the model for a user's profile
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */

@Data
@NoArgsConstructor
public class UserProfile implements Serializable {

	private static final long serialVersionUID = 1L;

	private String userUuid;
	private String displayName;
	private String nickname;
	private String email;
	private String mobilephone;
	private String personalSummary;
	private String phoneticPronunciation;
	private String pronounsSelected;
	private String pronounsInput;
	private String pronouns;
	
	private boolean locked;
	
	private SocialNetworkingInfo socialInfo;
	
	/* 
	 * This is an EntityBroker URL that can be used to get directly to a user's profile image. URL is open, but privacy is still checked.
	 */
	private String imageUrl; 
	private String imageThumbUrl; 
	
	private Map<String, String> props;

	/* Additional methods for setting/getting properties in the map */
	public void setProperty(String key, String value) {
        if (props == null) {
            props = new HashMap<String, String>();
        }
        props.put(key, value);
    }
    public String getProperty(String key) {
        if (props == null) {
            return null;
        }
        return props.get(key);
    }
	
}
