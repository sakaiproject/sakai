package org.sakaiproject.chat2.tool;

import org.sakaiproject.chat2.model.ChatMessage;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;

public class DecoratedChatMessage {

   private ChatMessage chatMessage;
   
   private ChatTool chatTool;
   
   private Time messageTime;
   
   public DecoratedChatMessage(ChatTool chatTool, ChatMessage chatMessage)
   {
      this.chatTool = chatTool;
      this.chatMessage = chatMessage;
      messageTime = TimeService.newTime(chatMessage.getMessageDate().getTime());
   }
   
   public String getColor()
   {
      return chatTool.getColorMapper().getColor(chatMessage.getOwner());
   }
   
   public ChatMessage getChatMessage()
   {
      return chatMessage;
   }
   
   public String getBody()
   {
      return chatMessage.getBody();
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
   
   public boolean getCanRemoveMessage()
   {
      return chatTool.getCanRemoveMessage(chatMessage);
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
