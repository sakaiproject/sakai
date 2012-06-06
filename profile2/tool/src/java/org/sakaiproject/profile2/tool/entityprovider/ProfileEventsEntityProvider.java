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
package org.sakaiproject.profile2.tool.entityprovider;

import java.util.Locale;
import java.util.Map;

import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Statisticable;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.profile2.util.ProfileConstants;

/**
 * This is the entity provider to interact with SiteStats 
 * and provided anonymous reporting of Profile2 usage.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class ProfileEventsEntityProvider extends AbstractEntityProvider implements AutoRegisterEntityProvider, Statisticable, Describeable {
	
    private final static String PREFIX = "profile-events";
    
    private final static String[] EVENT_KEYS = new String[] {
    	ProfileConstants.EVENT_PROFILE_VIEW_OWN,
		ProfileConstants.EVENT_PROFILE_VIEW_OTHER,
		ProfileConstants.EVENT_FRIEND_REQUEST,
		ProfileConstants.EVENT_FRIEND_CONFIRM,
		ProfileConstants.EVENT_FRIENDS_VIEW_OWN,
		ProfileConstants.EVENT_FRIENDS_VIEW_OTHER,
		ProfileConstants.EVENT_SEARCH_BY_NAME,
		ProfileConstants.EVENT_SEARCH_BY_INTEREST,
		ProfileConstants.EVENT_STATUS_UPDATE,
		ProfileConstants.EVENT_TWITTER_UPDATE,
		ProfileConstants.EVENT_MESSAGE_SENT,
	};

   
    @Override
	public String getEntityPrefix() {
		return PREFIX;
	}

    @Override
	public String getAssociatedToolId() {
		return ProfileConstants.TOOL_ID;
	}

    @Override
	public String[] getEventKeys() {
		return EVENT_KEYS;
	}

    @Override
	public Map<String, String> getEventNames(Locale locale) {
		return null;
	}

}
