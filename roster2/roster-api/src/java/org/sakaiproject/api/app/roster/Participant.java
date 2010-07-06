/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/presence/trunk/presence-api/api/src/java/org/sakaiproject/presence/api/PresenceService.java $
 * $Id: PresenceService.java 7844 2006-04-17 13:06:02Z ggolden@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.api.app.roster;

import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.user.api.User;


/**
 * @author rshastri
 *
 */
public interface Participant 
{
  public static final String SORT_BY_ID = "displayId";
  public static final String SORT_BY_NAME = "sortName";
  public static final String SORT_BY_EMAIL = "email";
  public static final String SORT_BY_ROLE = "role";
  public static final String SORT_BY_GROUP = "group";

  
  public Profile getProfile();
  public String getRoleTitle();
  public User getUser();
  public boolean isOfficialPhotoPreferred();
  public boolean isOfficialPhotoPublicAndPreferred();
  public boolean isProfilePhotoPublic();
  public String getGroupsString();
}