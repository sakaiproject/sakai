/**
 * Copyright (c) 2003-2021 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.event.impl;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.LearningResourceStoreService.EventWrapper;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Object;
import org.sakaiproject.event.api.XAPIFactory;

public class XAPILogout extends XAPIFactory{

   @Override
   public LRS_Object getEventObject(Event event, Map<String, EventWrapper> xAPIEvent, String url) {
        LRS_Object object = null;
        if (event != null) {
            String e = StringUtils.lowerCase(event.getEvent());
            /*
             * NOTE: use the following terms "view", "add", "edit", "delete"
             */
            
            if (xAPIEvent.containsKey(e)) {
               object = new LRS_Object (/*serverConfigurationService.getPortalUrl()*/url + "/logout", xAPIEvent.get(e).getObject());
            }
        }
        return object;
   }
}