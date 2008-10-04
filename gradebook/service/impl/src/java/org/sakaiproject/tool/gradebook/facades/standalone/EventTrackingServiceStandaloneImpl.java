/*******************************************************************************
 * Copyright (c) 2006, 2007 Sakai Foundation, the MIT Corporation
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

package org.sakaiproject.tool.gradebook.facades.standalone;

import org.sakaiproject.tool.gradebook.facades.EventTrackingService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Author:Louis Majanja <louis@media.berkeley.edu>
 * Date: Feb 22, 2007
 * Time: 3:36:22 PM
 *
 *
 * an implementation of the EventTrackingService facade for the standalalone gradebook
 */
public class EventTrackingServiceStandaloneImpl implements EventTrackingService {

    private static final Log log = LogFactory.getLog(EventTrackingServiceStandaloneImpl.class);

    /**
     *
     * @param message
     * @param referenceObject
     */
    public void postEvent(String message, String referenceObject) {
        if(log.isDebugEnabled()) log.debug("action: "+message + "  object reference:"+referenceObject);
    }
}
