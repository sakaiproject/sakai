/**
 * $Id$
 * $URL$
 * Createable.java - entity-broker - Apr 8, 2008 11:14:05 AM - azeckoski
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

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import java.util.Locale;
import java.util.Map;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * This capability is for tracking statistics of events for entities related to a tool,
 * it will be used by the site stats service for event tracking and reporting<br/>
 * Contact Nuno Fernandes (nuno@ufp.edu.pt) if you have questions<br/> 
 * This is one of the capability extensions for the {@link EntityProvider} interface<br/>
 * 
 * @author Nuno Fernandes (nuno@ufp.edu.pt)
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface Statisticable extends EntityProvider {

    /**
     * Return the associated common tool.id for this tool
     * 
     * @return the tool id (example: "sakai.messages")
     */
    public String getAssociatedToolId();

    /**
     * Return an array of all the event keys which should be tracked for statistics
     * 
     * @return an array if event keys (example: "message.new" , "message.delete")
     */
    public String[] getEventKeys();

    /**
     * OPTIONAL: return null if you do not want to implement this<br/>
     * Return the event key => event name map for a given Locale,
     * allows the author to create human readable i18n names for their event keys
     * 
     * @param locale the locale to return the names for
     * @return the map of event key => event name (example: for a 'en' locale: {"message.new","A new message"}) OR null to use the event keys
     */
    public Map<String, String> getEventNames(Locale locale);

}
