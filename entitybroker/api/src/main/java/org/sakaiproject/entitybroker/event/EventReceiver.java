/**
 * $Id$
 * $URL$
 * AutoRegister.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2007, 2008 The Sakai Foundation
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
 **/

package org.sakaiproject.entitybroker.event;

/**
 * Allows a developer to create a method which will be called when specific events occur by
 * implementing this interface, this uses the Sakai event services
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public interface EventReceiver {

   /**
    * This defines the events that you want to know about by event name,
    * {@link #receiveEvent(String, String)} will be called whenever an event occurs which has a name
    * which begins with any of the strings this method returns, simply return empty array if you do
    * not want to match events this way<br/> <br/> <b>Note:</b> Can be used with
    * {@link #getResourcePrefix()}
    * 
    * @return an arrays of event name prefixes
    */
   public String[] getEventNamePrefixes();

   /**
    * This defines the events that you want to know about by event resource (reference),
    * {@link #receiveEvent(String, String)} will be called whenever an event occurs which has a
    * resource which begins with the string this method returns, simply return empty string to match
    * no events this way<br/> <br/> <b>Note:</b> Can be used with {@link #getEventNamePrefixes()}
    * 
    * @return a string with a resource (reference) prefix
    */
   public String getResourcePrefix();

   /**
    * This defines what should happen when an event occurs that you want to know about
    * 
    * @param eventName
    *           a string which represents the name of the event (e.g. announcement.create)
    * @param id
    *           the local id of the entity
    */
   public void receiveEvent(String eventName, String id);

}
