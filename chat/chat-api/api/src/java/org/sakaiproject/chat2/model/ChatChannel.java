package org.sakaiproject.chat2.model;

import org.sakaiproject.metaobj.shared.model.Id;
import java.util.Date;

public class ChatChannel {
   private Id id;
   private String nextId = "";
   private String context;
   private Date creationDate;
   private String title;
   
   
   public ChatChannel() {
   }
   
   public String getContext() {
      return context;
   }
   public void setContext(String context) {
      this.context = context;
   }
   public Date getCreationDate() {
      return creationDate;
   }
   public void setCreationDate(Date creationDate) {
      this.creationDate = creationDate;
   }
   public Id getId() {
      return id;
   }
   public void setId(Id id) {
      this.id = id;
   }
   public String getNextId() {
      return nextId;
   }
   public void setNextId(String nextId) {
      this.nextId = nextId;
   }
   public String getTitle() {
      return title;
   }
   public void setTitle(String title) {
      this.title = title;
   }
}
