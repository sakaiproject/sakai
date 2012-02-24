/**
 * $Id$
 * $URL$
 * EntityMember.java - entity-broker - Aug 15, 2008 2:02:20 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.providers.model;

/**
 * Represents an owner of something
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class Owner {

    private String userId;
    private String userDisplayName;
    private String userEntityURL;

    public Owner(String userId, String displayName) {
        if (userId.startsWith("/user/") && userId.length() > 6) {
            userId = userId.substring(6);
        }
        this.userId = userId;
        this.userDisplayName = displayName;
        this.userEntityURL = "/direct/user/" + userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public void setUserDisplayName(String userDisplayName) {
        this.userDisplayName = userDisplayName;
    }

    public String getUserEntityURL() {
        return userEntityURL;
    }

    public void setUserEntityURL(String userEntityURL) {
        this.userEntityURL = userEntityURL;
    }

}
