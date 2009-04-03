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
