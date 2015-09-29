/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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
 *
 **********************************************************************************/
package org.sakaiproject.user.api;

import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.util.api.FormattedText;
import org.w3c.dom.Element;

public interface UserEditHelper {

    public void readProperties(UserEdit edit, ResourcePropertiesEdit props);

    public User getAnonymousUser();

    public String userReference(String id);

    public String getAccessPoint(boolean relative);

    public FormattedText formattedText();

    public String encodePassword(String pw);

    public boolean checkPassword(String pw1, String pw2);

    public String getSortName(UserEdit user);

    public String getUserDisplayId(UserEdit user);

    public String getUserDisplayName(UserEdit user);

    public String getDisplayName(UserEdit user);

    public String getDisplayId(UserEdit user);
    
    public String cleanId(String id);

    public String cleanEid(String eid);

    public TimeService timeService();

    public void addLiveProperties(UserEdit edit);

    public void cancelEdit(UserEdit user);

    public User getUser(String id) throws UserNotDefinedException;

    public void init();

    public void setLazyProperties(ResourcePropertiesEdit properties, User user);

    void setLazyProperties(ResourcePropertiesEdit properties, boolean lazy);

    public ResourcePropertiesEdit getResourcePropertiesEdit();

    public ResourcePropertiesEdit getResourcePropertiesEdit(Element element);
}
