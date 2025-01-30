/**********************************************************************************
 *
 * Copyright (c) ${license.git.copyrightYears} ${holder}
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *             http://opensource.org/licenses/ecl2

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.jsf.convert;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

@FacesConverter("org.sakaiproject.tool.assessment.jsf.convert.UsernameConverter")
@Slf4j
public class UsernameConverter implements Converter {

    public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
        return getAsString(arg0, arg1, arg2);
    }

    public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
        String userId = (String) arg2;
        if (userId == null) return null;

        UserDirectoryService userDirectoryService = (UserDirectoryService) ComponentManager.get(UserDirectoryService.class);

        try {
            return userDirectoryService.getUser(userId).getDisplayName();
        } catch (UserNotDefinedException e) {
            log.warn("User '{}' not found, not returning the display name.", userId);
            return userId;
        }

    }

}
