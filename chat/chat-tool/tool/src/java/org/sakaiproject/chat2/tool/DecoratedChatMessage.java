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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
 
package org.sakaiproject.chat2.tool;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import org.sakaiproject.chat2.model.ChatManager;
import org.sakaiproject.chat2.model.ChatMessage;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;

public class DecoratedChatMessage {

   private ChatMessage chatMessage;
   
   private ChatTool chatTool;
   
   private ChatManager chatManager;
   
   private ZonedDateTime ldt;
   ResourceLoader rl = new ResourceLoader();
   
   public DecoratedChatMessage(ChatTool chatTool, ChatMessage chatMessage, ChatManager chatManager)
   {
      this.chatTool = chatTool;
      this.chatMessage = chatMessage;
      this.chatManager = chatManager;
      if (chatMessage != null && chatMessage.getMessageDate() != null)
       {
          ldt = ZonedDateTime.ofInstant(chatMessage.getMessageDate().toInstant(), ZoneId.of(chatManager.getUserTimeZone()));
       }
   }
   
   public ChatMessage getChatMessage()
   {
      return chatMessage;
   }

   public String getDateTime()
   {
      return ldt.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.LONG).withLocale(rl.getLocale()));
   }
   
   public String getDate()
   {
      return ldt.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(rl.getLocale()));
   }
   
   public String getTime()
   {
      return ldt.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.LONG).withLocale(rl.getLocale()));
   }
   
   public String getId()
   {
      return ldt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS", rl.getLocale()));
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
