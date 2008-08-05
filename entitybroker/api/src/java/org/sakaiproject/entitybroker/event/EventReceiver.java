/**
 * $Id$
 * $URL$
 * AutoRegister.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
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
