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
 *       http://www.opensource.org/licenses/ECL-2.0
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
import java.util.Set;
import java.util.Stack;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "CHAT2_CHANNEL")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries({
        @NamedQuery(name = "findChannelsInContext", query = "from ChatChannel c WHERE c.context = :context order by c.title"),
        @NamedQuery(name = "findDefaultChannelsInContext", query = "from ChatChannel c WHERE c.context = :context and c.placement = :placement and c.placementDefaultChannel = true order by c.title")
})

@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Getter
@Setter
@ToString(exclude = "messages")
public class ChatChannel implements org.sakaiproject.entity.api.Entity {
   
   /** Message filter names */
   public static final String FILTER_BY_NUMBER = "SelectMessagesByNumber";
   public static final String FILTER_BY_TIME = "SelectMessagesByTime";
   public static final String FILTER_TODAY = "SelectTodaysMessages";
   public static final String FILTER_ALL = "SelectAllMessages";
   public static final String FILTER_NONE = "SelectNoneMessages";

   @Id
   @Column(name = "CHANNEL_ID", length = 36)
   @GeneratedValue(generator = "uuid")
   @GenericGenerator(name = "uuid", strategy = "uuid2")
   private String id;

   @Column(name = "PLACEMENT_ID", length = 99)
   private String placement;

   @Column(name = "CONTEXT", length = 99, nullable = false)
   @OrderColumn(name = "CHAT_CHANNEL_CONTEXT_I")
   private String context;

   @Column(name = "CREATION_DATE")
   @Temporal(TemporalType.TIMESTAMP)
   private Date creationDate;

   @Column(length = 64)
   private String title;

   @Column()
   private String description;

   @Column(length = 25)
   private String filterType = FILTER_ALL;

   @Column
   private int filterParam = 3;

   @Column(nullable = false)
   private int timeParam = 3;

   @Column(nullable = false)
   private int numberParam = 10;

   @Column(nullable = false)
   private boolean placementDefaultChannel = false;

   @Column(name = "ENABLE_USER_OVERRIDE", nullable = false)
   private boolean enableUserOverride = true;

   @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "chatChannel")
   @BatchSize(size = 50)
   private Set<ChatMessage> messages;

   @Column(length = 99)
   private String migratedChannelId;

   @Column(name = "START_DATE")
   @Temporal(TemporalType.TIMESTAMP)
   private Date startDate;

   @Column(name = "END_DATE")
   @Temporal(TemporalType.TIMESTAMP)
   private Date endDate;

   /**
    * Set up a new ChatChannel with the set defaults
    * @param defaults
    */
   public ChatChannel(ChatChannel defaults) {
      this.filterType = defaults.getFilterType();
      this.filterParam = defaults.getFilterParam();
      this.timeParam = defaults.getTimeParam();
      this.numberParam = defaults.getNumberParam();
      this.enableUserOverride = defaults.isEnableUserOverride();
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
   @SuppressWarnings("unchecked")
   public Element toXml(Document doc, Stack stack)
   {
      Element channel = doc.createElement("channel");

      if (stack.isEmpty())
      {
         doc.appendChild(channel);
      }
      else
      {
         ((Element) stack.peek()).appendChild(channel);
      }

      stack.push(channel);

      channel.setAttribute("context", getContext());
      channel.setAttribute("id", getId());
      channel.setAttribute("description", getDescription());
      channel.setAttribute("title", getTitle());
      channel.setAttribute("creationDate", Long.toString(getCreationDate().getTime()));
      channel.setAttribute("filterType", getFilterType());
      channel.setAttribute("filterParam", Integer.toString(getFilterParam()));
      channel.setAttribute("placementDefaultChannel", Boolean.toString(isPlacementDefaultChannel()));
      
      stack.pop();

      return channel;

   } // toXml
   
   /**
    * Converts the serialized xml back to a ChatChannel object
    * @param channelElement
    * @return the Channel
    */
   public static ChatChannel xmlToChatChannel(Element channelElement, String siteId) {
      ChatChannel tmpChannel = new ChatChannel();
      //tmpChannel.setContext(channelElement.getAttribute("context"));
      tmpChannel.setContext(siteId);
      
      if (siteId.equalsIgnoreCase(channelElement.getAttribute("context"))) {
         //If importing into the same site, keep the id.  We should get an update instead of saving a new one.
         tmpChannel.setId(channelElement.getAttribute("id"));
      }
      tmpChannel.setDescription(channelElement.getAttribute("description"));
      tmpChannel.setTitle(channelElement.getAttribute("title"));
      tmpChannel.setCreationDate(new Date(Long.parseLong(channelElement.getAttribute("creationDate"))));
      tmpChannel.setFilterType(channelElement.getAttribute("filterType"));
      tmpChannel.setFilterParam(Integer.parseInt(channelElement.getAttribute("filterParam")));
      tmpChannel.setPlacementDefaultChannel(Boolean.parseBoolean(channelElement.getAttribute("placementDefaultChannel")));
      
      return tmpChannel;
   }

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
      return ChatManager.REFERENCE_ROOT + org.sakaiproject.entity.api.Entity.SEPARATOR + ChatManager.REF_TYPE_CHANNEL + org.sakaiproject.entity.api.Entity.SEPARATOR + context + org.sakaiproject.entity.api.Entity.SEPARATOR + id;

   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entity.api.Entity#getReference(java.lang.String)
    */
   public String getReference(String rootProperty) {
      return getReference();
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entity.api.Entity#getUrl()
    */
   public String getUrl() {
      return ServerConfigurationService.getAccessUrl() + getReference();
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entity.api.Entity#getUrl(java.lang.String)
    */
   public String getUrl(String rootProperty) {
      return getUrl();
   }
}
