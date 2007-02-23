package org.sakaiproject.chat2.model;

/**
 * Any classes that want to observer the messages in a room needs to implement this
 * and then add itself to the ChatManager
 * @author andersjb
 *
 */
public interface RoomObserver {

   /**
    * 
    * @param toolId String of the tool receiving the message
    * @param roomId String of the room receiving the masseg
    * @param message ChatMessage the message being received
    */
   public void receivedMessage(String roomId, Object message);
   

   /**
    * 
    * @param toolId
    * @param roomId
    */
   public void roomDeleted(String roomId);
   
}
