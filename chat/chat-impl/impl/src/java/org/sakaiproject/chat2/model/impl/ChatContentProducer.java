/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.chat2.model.impl;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.chat2.model.ChatChannel;
import org.sakaiproject.chat2.model.ChatManager;
import org.sakaiproject.chat2.model.ChatMessage;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.api.SearchUtils;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.util.HTMLParser;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.ContextualUserDisplayService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
/**
 * @author chrismaurer
 *
 */
@Slf4j
public class ChatContentProducer implements EntityContentProducer {

   @Setter @Getter private SearchService searchService = null;
   @Setter @Getter private SearchIndexBuilder searchIndexBuilder = null;
   @Setter @Getter  private EntityManager entityManager = null;
   @Setter @Getter private ChatManager chatManager = null;
   @Setter @Getter private List<String> addEvents = new ArrayList<>();
   @Setter @Getter private List<String> removeEvents = new ArrayList<>();
   
   private ResourceLoader toolBundle;
   
   private ContextualUserDisplayService contextualUserDisplayService;
   
   @Setter private SiteService siteService;
   @Setter private UserDirectoryService userDirectoryService;


protected void init() throws Exception {
      log.info("init()");
      
      if ("true".equals(ServerConfigurationService.getString( //$NON-NLS-1$
            "search.enable", "false"))) //$NON-NLS-1$ //$NON-NLS-2$
      {
         for (Iterator<String> i = addEvents.iterator(); i.hasNext();)
         {
            getSearchService().registerFunction((String) i.next());
         }
         for (Iterator<String> i = removeEvents.iterator(); i.hasNext();)
         {
            getSearchService().registerFunction((String) i.next());
         }
         getSearchIndexBuilder().registerEntityContentProducer(this);
         
      }
      
      contextualUserDisplayService = (ContextualUserDisplayService) ComponentManager.get("org.sakaiproject.user.api.ContextualUserDisplayService");

   }
   
   /**
    * Destroy
    */
   protected void destroy()
   {
      log.info("destroy()");
   }

   
   private Reference getReference(String reference) {
      try {
          return entityManager.newReference(reference);
      } catch ( Exception ex ) {       
      }
      return null;
   }
   
   private EntityProducer getProducer(Reference ref) {
      try {
          return ref.getEntityProducer();
      } catch ( Exception ex ) {
      }
      return null;
   }
   
   private String getMessageFromBundle(String key) {
      if (toolBundle == null)
         toolBundle = new ResourceLoader("chat");
      
      return toolBundle.getString(key);
   }
   
   
   @Override
   public boolean canRead(String reference)
   {
      Reference ref = getReference(reference);
      EntityProducer ep = getProducer(ref);
      if (ep instanceof ChatEntityProducer)
      {
         try
         {
            ChatEntityProducer cep = (ChatEntityProducer) ep;
            cep.getMessage(ref);
            return true;
         }
         catch (Exception ex)
         {
         }
      }
      return false;
   }

   @Override
   public Integer getAction(Event event)
   {
      String evt = event.getEvent();
      if (evt == null) return SearchBuilderItem.ACTION_UNKNOWN;
      for (Iterator<String> i = addEvents.iterator(); i.hasNext();)
      {
         String match = (String) i.next();
         if (evt.equals(match))
         {
            return SearchBuilderItem.ACTION_ADD;
         }
      }
      for (Iterator<String> i = removeEvents.iterator(); i.hasNext();)
      {
         String match = (String) i.next();
         if (evt.equals(match))
         {
            return SearchBuilderItem.ACTION_DELETE;
         }
      }
      return SearchBuilderItem.ACTION_UNKNOWN;
   }

   @Override
   public String getContainer(String reference)
   {
      try {
         return getReference(reference).getContainer();
      } catch ( Exception ex ) {
         return "";
      }
   }
   
   protected String getMessageOwnerDisplayName(String user, String context)
   {
      User sender = null;
      try {
         sender = userDirectoryService.getUser(user);
      } catch(UserNotDefinedException e) {
         log.error(e.getMessage(), e);
         return user;
      }
      if (contextualUserDisplayService == null) {
    	  return sender.getDisplayName();
      } else {
    	  
    	  String ret = contextualUserDisplayService.getUserDisplayName(sender, siteService.siteReference(context));
    	  if (ret == null)
    		  ret = sender.getDisplayName();
    	  return ret;
      }
   }

   @Override
   public String getContent(String reference)
   {
      Reference ref = getReference(reference);
      EntityProducer ep = getProducer(ref);

      if (ep instanceof ChatEntityProducer)
      {
         try
         {
            ChatEntityProducer ms = (ChatEntityProducer) ep;
            ChatMessage m = ms.getMessage(ref);
            return HTMLParser.stripHtml(m.getBody());
         }
         catch (IdUnusedException e)
         {
            throw new RuntimeException(" Failed to get message content ", e); //$NON-NLS-1$
         }
         catch (PermissionException e)
         {
            throw new RuntimeException(" Failed to get message content ", e); //$NON-NLS-1$
         }
      }

      throw new RuntimeException(" Not a Message Entity " + reference); //$NON-NLS-1$
   }

   @Override
   public Reader getContentReader(String reference) {
      return new StringReader(getContent(reference));
   }

   @Override
   public Map<String, ?> getCustomProperties(String ref) {
      return null;
   }

   @Override
   public String getCustomRDF(String ref) {
      return null;
   }

   @Override
   public String getId(String reference)
   {
      try {
         return getReference(reference).getId();
      } catch ( Exception ex ) {
         return "";
      }
   }

   @Override
   public Iterator<String> getSiteContentIterator(final String context)
   {
      List<ChatChannel>  l = getChatManager().getContextChannels(context, false);
      final Iterator ci = l.iterator();
      return new Iterator()
      {
         Iterator mi = null;

         public boolean hasNext()
         {
            if (mi == null)
            {
               return nextIterator();
            }
            else
            {
               if (mi.hasNext())
               {
                  return true;
               }
               else
               {
                  return nextIterator();
               }
            }
         }

         private boolean nextIterator()
         {
            while (ci.hasNext())
            {

               ChatChannel c = (ChatChannel) ci.next();
               try
               {
                  Set<ChatMessage> messages = c.getMessages();
                  mi = messages.iterator();
                  if (mi.hasNext())
                  {
                     return true;
                  }
               }
               catch (Exception ex)
               {
                  log.warn("Failed to get channel {}", c.getId()); //$NON-NLS-1$
                  log.error(ex.getMessage(), ex);
               }
            }
            return false;
         }

         public Object next()
         {
            ChatMessage m = (ChatMessage) mi.next();
            return m.getReference();
         }

         public void remove()
         {
            throw new UnsupportedOperationException(
                  "Remove not implemented"); //$NON-NLS-1$
         }

      };
   }
   
   private String getSiteId(Reference ref)
   {
      return ref.getContext();
   }

   @Override
   public String getSiteId(String reference) {
      return getSiteId(entityManager.newReference(reference));
   }

   @Override
   public String getSubType(String reference)
   {
      try {
         return getReference(reference).getSubType();
      } catch ( Exception ex ) {
         return "";
      }
   }

   @Override
   public String getTitle(String reference)
   {
      Reference ref = getReference(reference);
      EntityProducer ep = getProducer(ref);
      if (ep instanceof ChatEntityProducer)
      {
         try
         {
            ChatEntityProducer ms = (ChatEntityProducer) ep;
            ChatMessage m = ms.getMessage(ref);
            String subject = getMessageFromBundle("chat_message"); //$NON-NLS-1$
            String title = subject + getMessageFromBundle("chat_from") //$NON-NLS-1$
                  + getMessageOwnerDisplayName(m.getOwner(), m.getChatChannel().getContext());
            return SearchUtils.appendCleanString(title,null).toString();

         }
         catch (IdUnusedException e)
         {
            throw new RuntimeException(" Failed to get message content ", e); //$NON-NLS-1$
         }
         catch (PermissionException e)
         {
            throw new RuntimeException(" Failed to get message content ", e); //$NON-NLS-1$
         }
      }
      throw new RuntimeException(" Not a Message Entity " + reference); //$NON-NLS-1$

   }

   @Override
   public String getTool() {
      return ChatManager.CHAT;
   }

   @Override
   public String getType(String ref) {
      try {
         return getReference(ref).getType();
      } catch ( Exception ex ) {
         return "";
      }
   }

   @Override
   public String getUrl(String reference) {
      Reference ref = getReference(reference);
      return ref.getUrl();
   }

   @Override
   public boolean isContentFromReader(String reference) {
      return false;
   }

   @Override
   public boolean isForIndex(String reference)
   {

      Reference ref = getReference(reference);
      EntityProducer ep = getProducer(ref);
      if (ep instanceof ChatEntityProducer)
      {
         try
         {
            ChatEntityProducer cep = (ChatEntityProducer) ep;
            ChatMessage m = cep.getMessage(ref);
            if (m == null)
            {
               log.debug("Rejected null message {}", ref.getReference()); //$NON-NLS-1$
               return false;
            }
         }
         catch (IdUnusedException e)
         {
            log.debug("Rejected Missing message or Collection {}", //$NON-NLS-1$
                  ref.getReference());
            return false;
         }
         catch (PermissionException e)
         {
            log.warn("Rejected private message {}", ref.getReference()); //$NON-NLS-1$
            return false;
         }
         return true;
      }
      return false;
   }

   /**
    * {@inheritDoc}
    */
   public boolean matches(String reference)
   {
      Reference ref = getReference(reference);
      EntityProducer ep = getProducer(ref);

      if (ep instanceof ChatEntityProducer)
      {
         return true;
      }
      return false;
   }

   @Override
   public boolean matches(Event event)
   {
      return matches(event.getResource());
   }

}
