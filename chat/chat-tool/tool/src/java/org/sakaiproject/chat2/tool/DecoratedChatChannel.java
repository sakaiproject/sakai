package org.sakaiproject.chat2.tool;

import org.sakaiproject.chat2.model.ChatChannel;

public class DecoratedChatChannel {

   private ChatChannel chatChannel;
   
   private ChatTool chatTool;
   
   public DecoratedChatChannel(ChatTool chatTool, ChatChannel chatChannel)
   {
      this.chatTool = chatTool;
      this.chatChannel = chatChannel;
   }
   
   public String processActionEnterRoom()
   {
      return chatTool.processActionEnterRoom(chatChannel);
   }
   
   public String processActionEditRoom()
   {
      return chatTool.processActionEditRoom(chatChannel);
   }
   
   public String processActionDeleteRoom()
   {
      return chatTool.processActionEditRoom(chatChannel);
   }
   
   public ChatChannel getChatChannel()
   {
      return chatChannel;
   }
   
   public ChatTool getChatTool()
   {
      return chatTool;
   }
   
}
