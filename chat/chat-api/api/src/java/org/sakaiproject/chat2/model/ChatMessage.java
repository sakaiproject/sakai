package org.sakaiproject.chat2.model;


import org.sakaiproject.metaobj.shared.model.Id;
import java.util.Date;

/**
 * 
 * @author andersjb
 *
 */
public class ChatMessage {

   private Id id;
   private ChatChannel chatChannel;
   private boolean draft;
   private boolean pubView;
   private String owner;
   private Date messageDate;
   private String body;

   
   public ChatMessage() {
   }
   
   public String getBody() {
      return body;
   }
   public void setBody(String body) {
      this.body = body;
   }
   public ChatChannel getChatChannel() {
      return chatChannel;
   }
   public void setChatChannel(ChatChannel chatChannel) {
      this.chatChannel = chatChannel;
   }
   public boolean isDraft() {
      return draft;
   }
   public void setDraft(boolean draft) {
      this.draft = draft;
   }
   public Id getId() {
      return id;
   }
   public void setId(Id id) {
      this.id = id;
   }
   public Date getMessageDate() {
      return messageDate;
   }
   public void setMessageDate(Date messageDate) {
      this.messageDate = messageDate;
   }
   public String getOwner() {
      return owner;
   }
   public void setOwner(String owner) {
      this.owner = owner;
   }
   public boolean isPubView() {
      return pubView;
   }
   public void setPubView(boolean pubView) {
      this.pubView = pubView;
   }

}
