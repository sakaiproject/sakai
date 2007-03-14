/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/portal/trunk/portal-util/util/src/java/org/sakaiproject/portal/util/PortalSiteHelper.java $
 * $Id: PortalSiteHelper.java 21708 2007-02-18 21:59:28Z ian@caret.cam.ac.uk $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
 
package org.sakaiproject.chat2.model;


import java.util.Date;
import java.util.Stack;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author andersjb
 *
 */
public class ChatMessage implements Entity {

   private String id;
   private ChatChannel chatChannel;
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
   public String getId() {
      return id;
   }
   public void setId(String id) {
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
   
   
   /**
    * Serialize the resource into XML, adding an element to the doc under the top of the stack element.
    * 
    * @param doc
    *        The DOM doc to contain the XML (or null for a string return).
    * @param stack
    *        The DOM elements, the top of which is the containing element of the new "resource" element.
    * @return The newly added element.
    */
   public Element toXml(Document doc, Stack stack)
   {
      //I don't think this will get called since chat messages don't appear to be archived. - chmaurer
      
      Element message = doc.createElement("message");

      if (stack.isEmpty())
      {
         doc.appendChild(message);
      }
      else
      {
         ((Element) stack.peek()).appendChild(message);
      }

      stack.push(message);

      //m_header.toXml(doc, stack);

      //FormattedText.encodeFormattedTextAttribute(message, "body", getBody());
      message.setAttribute("body", getBody());
      message.setAttribute("owner", getOwner());
      message.setAttribute("messageDate", Long.toString(getMessageDate().getTime()));
      message.setAttribute("channelId", getChatChannel().getId());

      /*
       * // Note: the old way to set the body - CDATA is too sensitive to the characters within -ggolden Element body = doc.createElement("body"); message.appendChild(body); body.appendChild(doc.createCDATASection(getBody()));
       */

      // properties
      //m_properties.toXml(doc, stack);

      stack.pop();

      return message;

   } // toXml

   /* (non-Javadoc)
    * @see org.sakaiproject.entity.api.Entity#getProperties()
    */
   public ResourceProperties getProperties() {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entity.api.Entity#getReference()
    */
   public String getReference() {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entity.api.Entity#getReference(java.lang.String)
    */
   public String getReference(String rootProperty) {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entity.api.Entity#getUrl()
    */
   public String getUrl() {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entity.api.Entity#getUrl(java.lang.String)
    */
   public String getUrl(String rootProperty) {
      // TODO Auto-generated method stub
      return null;
   }
   
}
