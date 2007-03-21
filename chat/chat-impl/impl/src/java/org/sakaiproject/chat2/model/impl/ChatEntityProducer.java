/**********************************************************************************
* $URL$
* $Id$
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

/**
 * 
 */
package org.sakaiproject.chat2.model.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.chat2.model.ChatChannel;
import org.sakaiproject.chat2.model.ChatManager;
import org.sakaiproject.chat2.model.ChatMessage;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author chrismaurer
 *
 */
public class ChatEntityProducer implements EntityProducer, EntityTransferrer {

   protected final Log logger = LogFactory.getLog(getClass());
   private EntityManager entityManager;
   private ChatManager chatManager;
   
   
   
   
   private static final String ARCHIVE_VERSION = "2.4"; // in case new features are added in future exports
   private static final String VERSION_ATTR = "version";
   private static final String CHANNEL_PROP = "channel";
   private static final String SYNOPTIC_TOOL = "synoptic_tool";
   private static final String NAME = "name";
   private static final String VALUE = "value";
   
   private static final String PROPERTIES = "properties";
   private static final String PROPERTY = "property";
   
   
   
   protected void init() throws Exception {
      logger.info("init()");
      
      try {
         getEntityManager().registerEntityProducer(this, ChatManager.REFERENCE_ROOT);
      }
      catch (Exception e) {
         logger.warn("Error registering Chat Entity Producer", e);
      }
   }
   
   /**
    * Destroy
    */
   protected void destroy()
   {
      logger.info("destroy()");
   }

   
   /**
    * {@inheritDoc}
    */
   public String[] myToolIds()
   {
      String[] toolIds = { ChatManager.CHAT_TOOL_ID };
      return toolIds;
   }
   
   public ChatMessage getMessage(Reference reference) throws IdUnusedException, PermissionException {
      return getChatManager().getMessage(reference.getId());
      //return null;
   }

   public ChatChannel getChannel(Reference reference) throws IdUnusedException, PermissionException {
      return getChatManager().getChatChannel(reference.getId());
      //return null;
   }
   
   /**
    * {@inheritDoc}
    */
   public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
   {
      //prepare the buffer for the results log
      StringBuffer results = new StringBuffer();
      int channelCount = 0;

      try 
      {
         // start with an element with our very own (service) name         
         Element element = doc.createElement(getChatManager().serviceName());
         element.setAttribute(VERSION_ATTR, ARCHIVE_VERSION);
         ((Element) stack.peek()).appendChild(element);
         stack.push(element);

         Element chat = doc.createElement(ChatManager.CHAT);
         List channelList = getChatManager().getContextChannels(siteId, true);
         if (channelList != null && !channelList.isEmpty()) 
         {
            Iterator channelIterator = channelList.iterator();
            while (channelIterator.hasNext()) 
            {
               ChatChannel channel = (ChatChannel)channelIterator.next();
               Element channelElement = channel.toXml(doc, stack);
               chat.appendChild(channelElement);
               channelCount++;
            }
            results.append("archiving " + getLabel() + ": (" + channelCount + ") channels archived successfully.\n");
            
         } 
         else 
         {
            results.append("archiving " + getLabel()
                  + ": empty chat room archived.\n");
         }
         
         // archive the chat synoptic tool options
         archiveSynopticOptions(siteId, doc, chat);

         ((Element) stack.peek()).appendChild(chat);
         stack.push(chat);

         stack.pop();
      }
      catch (Exception any)
      {
         logger.warn("archive: exception archiving service: " + getChatManager().serviceName());
      }

      stack.pop();

      return results.toString();
   }
   
   /**
    * try to add synoptic options for this tool to the archive, if they exist
    * @param siteId
    * @param doc
    * @param element
    */
   public void archiveSynopticOptions(String siteId, Document doc, Element element)
   {
      try
      {
         // archive the synoptic tool options
         Site site = SiteService.getSite(siteId);
         ToolConfiguration synTool = site.getToolForCommonId("sakai.synoptic." + getLabel());
         Properties synProp = synTool.getPlacementConfig();
         if (synProp != null && synProp.size() > 0) {
            Element synElement = doc.createElement(SYNOPTIC_TOOL);
            Element synProps = doc.createElement(PROPERTIES);

            Set synPropSet = synProp.keySet();
            Iterator propIter = synPropSet.iterator();
            while (propIter.hasNext())
            {
               String propName = (String)propIter.next();
               Element synPropEl = doc.createElement(PROPERTY);
               synPropEl.setAttribute(NAME, propName);
               synPropEl.setAttribute(VALUE, synProp.getProperty(propName));
               synProps.appendChild(synPropEl);
            }

            synElement.appendChild(synProps);
            element.appendChild(synElement);
         }
      }
      catch (Exception e)
      {
         logger.warn("archive: exception archiving synoptic options for service: " + getChatManager().serviceName());
      }
   }

   /**
    * {@inheritDoc}
    */
   public Entity getEntity(Reference ref)
   {
      // we could check that the type is one of the message services, but lets just assume it is so we don't need to know them here -ggolden

      Entity rv = null;

      try
      {
         // if this is a channel
         if (ChatManager.REF_TYPE_CHANNEL.equals(ref.getSubType()))
         {
            rv = getChatManager().getChatChannel(ref.getReference());
         }

         // otherwise a message
         else if (ChatManager.REF_TYPE_MESSAGE.equals(ref.getSubType()))
         {
            rv = getMessage(ref);
         }

         // else try {throw new Exception();} catch (Exception e) {M_log.warn("getResource(): unknown message ref subtype: " + m_subType + " in ref: " + m_reference, e);}
         else
            logger.warn("getEntity(): unknown message ref subtype: " + ref.getSubType() + " in ref: " + ref.getReference());
      }
      catch (NullPointerException e)
      {
         logger.warn("getEntity(): " + e);
      } catch (IdUnusedException e) {
         logger.warn("getEntity(): " + e);
      } catch (PermissionException e) {
         logger.warn("getEntity(): " + e);
      }
      

      return rv;
   }

   /**
    * {@inheritDoc}
    */
   public Collection getEntityAuthzGroups(Reference ref, String userId)
   {
      //TODO implement this
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public String getEntityDescription(Reference ref)
   {
      // we could check that the type is one of the message services, but lets just assume it is so we don't need to know them here -ggolden

      String rv = "Message: " + ref.getReference();

      try
      {
         // if this is a channel
         if (ChatManager.REF_TYPE_CHANNEL.equals(ref.getSubType()))
         {
            ChatChannel channel = getChannel(ref);
            rv = "Channel: " + channel.getId() + " (" + channel.getContext() + ")";
         }
      }
      catch (PermissionException e)
      {
      }
      catch (IdUnusedException e)
      {
      }
      catch (NullPointerException e)
      {
      }

      return rv;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entity.api.EntityProducer#getEntityResourceProperties(org.sakaiproject.entity.api.Reference)
    */
   public ResourceProperties getEntityResourceProperties(Reference ref) {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entity.api.EntityProducer#getEntityUrl(org.sakaiproject.entity.api.Reference)
    */
   public String getEntityUrl(Reference ref) {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entity.api.EntityProducer#getHttpAccess()
    */
   public HttpAccess getHttpAccess() {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public String getLabel() {
      return getChatManager().getLabel();
   }



   /**
    * {@inheritDoc}
    */
   public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
         Set userListAllowImport)
   {
      logger.debug("trying to merge chat");

      // buffer for the results log
      StringBuffer results = new StringBuffer();

      int count = 0;

      if (siteId != null && siteId.trim().length() > 0)
      {
         try
         {
            NodeList allChildrenNodes = root.getChildNodes();
            int length = allChildrenNodes.getLength();
            for (int i = 0; i < length; i++)
            {
               count++;
               Node siteNode = allChildrenNodes.item(i);
               if (siteNode.getNodeType() == Node.ELEMENT_NODE)
               {
                  Element chatElement = (Element) siteNode;
                  if (chatElement.getTagName().equals(ChatManager.CHAT))
                  {
                     Site site = SiteService.getSite(siteId);
                     if (site.getToolForCommonId(ChatManager.CHAT_TOOL_ID) != null) {
   
                        // add the chat rooms and synoptic tool options                
                        NodeList chatNodes = chatElement.getChildNodes();
                        int lengthChatNodes = chatNodes.getLength();
                        for (int cn = 0; cn < lengthChatNodes; cn++)
                        {
                           Node chatNode = chatNodes.item(cn);
                           if (chatNode.getNodeType() == Node.ELEMENT_NODE)
                           {
                              Element channelElement = (Element) chatNode;
                              if (channelElement.getTagName().equals(CHANNEL_PROP)) {
                                 ChatChannel channel = ChatChannel.xmlToChatChannel(channelElement, siteId);
                                 //save the channel
                                 getChatManager().updateChannel(channel, false);
                              }
                              
                              else if (channelElement.getTagName().equals(SYNOPTIC_TOOL)) 
                              {
                                 ToolConfiguration synTool = site.getToolForCommonId("sakai.synoptic.chat");
                                 Properties synProps = synTool.getPlacementConfig();

                                 NodeList synPropNodes = channelElement.getChildNodes();
                                 for (int props = 0; props < synPropNodes.getLength(); props++)
                                 {
                                    Node propsNode = synPropNodes.item(props);
                                    if (propsNode.getNodeType() == Node.ELEMENT_NODE)
                                    {
                                       Element synPropEl = (Element) propsNode;
                                       if (synPropEl.getTagName().equals(PROPERTIES))
                                       {
                                          NodeList synProperties = synPropEl.getChildNodes();
                                          for (int p = 0; p < synProperties.getLength(); p++)
                                          {
                                             Node propertyNode = synProperties.item(p);
                                             if (propertyNode.getNodeType() == Node.ELEMENT_NODE)
                                             {
                                                Element propEl = (Element) propertyNode;
                                                if (propEl.getTagName().equals(PROPERTY))
                                                {
                                                   String propName = propEl.getAttribute(NAME);
                                                   String propValue = propEl.getAttribute(VALUE);
                                                   
                                                   if (propName != null && propName.length() > 0 && propValue != null && propValue.length() > 0)
                                                   {
                                                      synProps.setProperty(propName, propValue);
                                                   }
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }        
                        }
                        SiteService.save(site);
                     }
                  }
               }
            }

            results.append("merging chat " + siteId + " (" + count
                  + ") chat items.\n");
         }
         catch (DOMException e)
         {
            logger.error(e.getMessage(), e);
            results.append("merging " + getLabel()
                  + " failed during xml parsing.\n");
         }
         catch (Exception e)
         {
            logger.error(e.getMessage(), e);
            results.append("merging " + getLabel() + " failed.\n");
         }
      }

      return results.toString();

   } // merge


   /**
    * {@inheritDoc}
    */
   public boolean parseEntityReference(String reference, Reference ref)
   {
      if (reference.startsWith(ChatManager.REFERENCE_ROOT))
      {
         String[] parts = StringUtil.split(reference, Entity.SEPARATOR);

         String id = null;
         String subType = null;
         String context = null;
         String container = null;

         // the first part will be null, then next the service, the third will be "msg" or "channel"
         if (parts.length > 2)
         {
            subType = parts[2];
            if (ChatManager.REF_TYPE_CHANNEL.equals(subType))
            {
               // next is the context id
               if (parts.length > 3)
               {
                  context = parts[3];

                  // next is the channel id
                  if (parts.length > 4)
                  {
                     id = parts[4];
                  }
               }
            }
            else if (ChatManager.REF_TYPE_MESSAGE.equals(subType))
            {
               // next three parts are context, channel (container) and mesage id
               if (parts.length > 5)
               {
                  context = parts[3];
                  container = parts[4];
                  id = parts[5];
               }
            }
            else
               logger.warn("parse(): unknown message subtype: " + subType + " in ref: " + reference);
         }

         ref.set(ChatManager.APPLICATION_ID, subType, id, container, context);

         return true;
      }

      return false;
   }

   /**
    * {@inheritDoc}
    */
   public boolean willArchiveMerge()
   {
      return true;
   }
   
   /**
    * {@inheritDoc}
    */
   public void transferCopyEntities(String fromContext, String toContext, List ids) 
   {
      try
      {           
         // retrieve all of the chat rooms
         List channels = getChatManager().getContextChannels(fromContext, true);
         if (channels != null && !channels.isEmpty()) 
         {
            Iterator channelIterator = channels.iterator();
            while (channelIterator.hasNext()) 
            {
               ChatChannel oldChannel = (ChatChannel)channelIterator.next();
               ChatChannel newChannel = getChatManager().createNewChannel(toContext, oldChannel.getTitle(), false, false);
               newChannel.setDescription(oldChannel.getDescription());
               newChannel.setFilterType(oldChannel.getFilterType());
               newChannel.setFilterParam(oldChannel.getFilterParam());
               newChannel.setContextDefaultChannel(oldChannel.isContextDefaultChannel());
               try {
                  getChatManager().updateChannel(newChannel, false);
               } 
               catch (Exception e) 
               {
                  logger.warn("Exception while creating channel: " + newChannel.getTitle() + ": " + e);
               }

            }
         }
         
         transferSynopticOptions(fromContext, toContext);    
      }

      catch (Exception any)
      {
         logger.warn(".transferCopyEntities(): exception in handling " + getChatManager().serviceName() + " : ", any);
      }
   }
   
   /**
    * Import the synoptic tool options from another site
    * 
    * @param fromContext
    * @param toContext
    */
   protected void transferSynopticOptions(String fromContext, String toContext)
   {
      try 
      {
         // transfer the synoptic tool options
         Site fromSite = SiteService.getSite(fromContext);
         ToolConfiguration fromSynTool = fromSite.getToolForCommonId("sakai.synoptic." + getLabel());
         Properties fromSynProp = fromSynTool.getPlacementConfig();

         Site toSite = SiteService.getSite(toContext);
         ToolConfiguration toSynTool = toSite.getToolForCommonId("sakai.synoptic." + getLabel());
         Properties toSynProp = toSynTool.getPlacementConfig();

         if (fromSynProp != null && !fromSynProp.isEmpty()) 
         {
            Set synPropSet = fromSynProp.keySet();
            Iterator propIter = synPropSet.iterator();
            while (propIter.hasNext())
            {
               String propName = ((String)propIter.next());
               String propValue = fromSynProp.getProperty(propName);
               if (propValue != null && propValue.length() > 0)
               {
                  toSynProp.setProperty(propName, propValue);
               }
            }

            SiteService.save(toSite);
         }
      }
      catch (PermissionException pe)
      {
         logger.warn("PermissionException transferring synoptic options for " + getChatManager().serviceName() + ':', pe);
      }
      catch (IdUnusedException e)
      {
         logger.warn("Channel " + fromContext + " cannot be found. ");
      }
      catch (Exception e)
      {
         logger.warn("transferSynopticOptions(): exception in handling " + getChatManager().serviceName() + " : ", e);
      }
   }
   
   
   
   public EntityManager getEntityManager() {
      return entityManager;
   }
   public void setEntityManager(EntityManager entityManager) {
      this.entityManager = entityManager;
   }
   public ChatManager getChatManager() {
      return chatManager;
   }
   public void setChatManager(ChatManager chatManager) {
      this.chatManager = chatManager;
   }

}
