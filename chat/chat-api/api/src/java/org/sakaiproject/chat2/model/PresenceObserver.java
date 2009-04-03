/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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
 
package org.sakaiproject.chat2.model;

/**
 * any class that wants to observer users joining and leaving a location should
 * implement this class and then open a new PresenceObserverHelper(this, "location")
 * @author andersjb
 *
 */
public interface PresenceObserver {

   /**
    * This is called by the PresenceObserverHelper when a user joins a location
    * @param location the user is joining this location
    * @param user the user joining
    */
   public void userJoined(String location, String user);
   

   /**
    * This is called by the PresenceObserverHelper when a user leaves a location
    * @param location the user is leaving this location
    * @param user the user leaving
    */
   public void userLeft(String location, String user);
   
}
