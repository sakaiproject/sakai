/**
 * $Id$
 * $URL$
 * LearningTrackingProvider.java - entity-broker - 22 Apr 2013 20:01:11 - azeckoski
 **************************************************************************
 * Copyright (c) 2013 The Sakai Foundation
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

package org.sakaiproject.entitybroker.entityprovider.extension;

/**
 * This provides for tracking learning events (LRS statements) related to an entity (by prefix)<br/> 
 * If more advanced control is needed then use of the full LRS_Statement in then LearningResourceStoreService.
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
public interface LearningTrackingProvider {

   /**
    * Send a simple learning activity (LRS) statement with an optional result
    * 
    * Statements are the bread and butter of Experience API (a.k.a. TinCanAPI).
    * They dictate the format for the specific moments in a stream of activity. 
    * They convey an experience which has occurred, or may be occurring, 
    * and typically can be stated in clear language, for instance, 
    * "Bob completed 'Truck Driving Training Level 1'".
    * 
    * NOTE: OPTIONAL params can be null, all other MUST be set and NOT empty strings
    * 
    * @param prefix the string which represents a type of entity handled by an entity provider
    * @param actorEmail the user email address, "I"
    * @param verbStr a string indicating the action, "did"
    * @param objectURI URI indicating the object of the statement, "this"
    * @param resultSuccess [OPTIONAL] true if the result was successful (pass) or false if not (fail), "well"
    * @param resultScaledScore [OPTIONAL] Score from -1.0 to 1.0 where 0=0% and 1.0=100%
    * @throws IllegalArgumentException if the prefix is not set or other required fields are left blank or null
    * @see #LearningResourceStoreService.registerStatement(LRS_Statement statement, String origin)
    */
   public void registerStatement(String prefix, String actorEmail, String verbStr, String objectURI, Boolean resultSuccess, Float resultScaledScore);

}
