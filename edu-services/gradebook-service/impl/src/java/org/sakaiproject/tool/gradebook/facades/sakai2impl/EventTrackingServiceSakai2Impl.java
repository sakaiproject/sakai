/*******************************************************************************
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation, The MIT Corporation
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
 ******************************************************************************/

package org.sakaiproject.tool.gradebook.facades.sakai2impl;

import org.sakaiproject.tool.gradebook.facades.EventTrackingService;
import org.sakaiproject.event.api.Event;


/**
 * Author:Louis Majanja <louis@media.berkeley.edu>
 * Date: Feb 22, 2007
 * Time: 3:35:45 PM
 *
 * a sakai implementation of the EventTrackingService facade 
 */
public class EventTrackingServiceSakai2Impl implements EventTrackingService {

    private org.sakaiproject.event.api.EventTrackingService eventTrackingService;

    /**
     *
     * @param message
     * @param objectReference
     */
    public void postEvent(String message, String objectReference) {
        Event event = eventTrackingService.newEvent(message, objectReference, true);
        eventTrackingService.post(event);
    }

    /**
     *
     * @return
     */
    public org.sakaiproject.event.api.EventTrackingService getEventTrackingService() {
        return eventTrackingService;
    }

    /**
     *
     * @param eventTrackingService
     */
    public void setEventTrackingService(org.sakaiproject.event.api.EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }
}
