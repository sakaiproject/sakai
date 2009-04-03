/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/portal/trunk/portal-util/util/src/java/org/sakaiproject/portal/util/PortalSiteHelper.java $
 * $Id: PortalSiteHelper.java 21708 2007-02-18 21:59:28Z ian@caret.cam.ac.uk $
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
 
package org.sakaiproject.chat2.tool;

import org.sakaiproject.chat2.model.ChatMessage;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.util.FormattedText;

public class DecoratedChatMessage {

   private ChatMessage chatMessage;
   
   private ChatTool chatTool;
   
   private Time messageTime;
   
   public DecoratedChatMessage(ChatTool chatTool, ChatMessage chatMessage)
   {
      this.chatTool = chatTool;
      this.chatMessage = chatMessage;
      if (chatMessage != null && chatMessage.getMessageDate() != null)
       {
          messageTime = TimeService.newTime(chatMessage.getMessageDate().getTime());
       }
   }
   
   public String getColor()
   {
      return chatTool.getColorMapper().getColor(chatMessage.getOwner());
   }
   
   public ChatMessage getChatMessage()
   {
      return chatMessage;
   }

   public String getDateTime()
   {
      return messageTime.toStringLocalFullZ();
   }
   
   public String getDate()
   {
      return messageTime.toStringLocalDate();
   }
   
   public String getTime()
   {
      return messageTime.toStringLocalTimeZ();
   }
   
   public String getId()
   {
      return messageTime.toString();
   }
   
   /**
    * Returns the body of the message, but limited to the number of characters 
    * specified in the tool's configuration properties
    * @return
    */
   public String getRestrictedBody() {
      String message = FormattedText.convertFormattedTextToPlaintext(chatMessage.getBody());
      int maxLength = chatTool.lookupSynopticOptions().getChars();
      int actualLength = message.length();
      if (maxLength < actualLength && maxLength >= 0) {
         message = message.substring(0, maxLength).concat("...");
      }
      return message;
   }

   public String getUnformattedBody() {
      return FormattedText.convertFormattedTextToPlaintext(chatMessage.getBody());
   }
     
   public boolean getCanRemoveMessage()
   {
      return chatTool.getCanRemoveMessage(chatMessage);
   }
   
   public String processActionDeleteMessage()
   {
      return chatTool.processActionDeleteMessageConfirm(this);
   }
   public String getOwner()
   {
      return chatTool.getMessageOwnerDisplayName(chatMessage);
   }
   
   public ChatTool getChatTool()
   {
      return chatTool;
   }
   
}
