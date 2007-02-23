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
