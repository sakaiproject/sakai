/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.messaging.api;

import java.util.List;
import java.util.Optional;

import org.sakaiproject.event.api.Event;

/**
 * A handler of events for the user messaging service. Produces <code>UserNotificationData</code> objects for 
 * the service to add to the user notifications table
 *
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
public interface UserNotificationHandler {

    List<String> getHandledEvents();
    Optional<List<UserNotificationData>> handleEvent(Event e);
    default String getTitle(String event, String reference) {
      return "";
    }
}
