/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.search.component.adapter.message;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.message.api.Message;
import org.sakaiproject.message.api.MessageChannel;
import org.sakaiproject.message.api.MessageHeader;
import org.sakaiproject.message.api.MessageService;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.model.SearchBuilderItem;

/**
 * @author ieb
 */
public class MessageContentProducer implements EntityContentProducer
{

	/**
	 * debug logger
	 */
	private static Log log = LogFactory.getLog(MessageContentProducer.class);

	// runtime dependency
	private String toolName = null;

	// runtime dependency
	private List addEvents = null;

	// runtime dependency
	private List removeEvents = null;

	// injected dependency
	private MessageService messageService = null;

	// runtime dependency
	private SearchService searchService = null;

	// runtime dependency
	private SearchIndexBuilder searchIndexBuilder = null;

	private EntityManager entityManager = null;

	public void init()
	{
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();

		searchService = (SearchService) load(cm, SearchService.class.getName());
		searchIndexBuilder = (SearchIndexBuilder) load(cm,
				SearchIndexBuilder.class.getName());

		entityManager = (EntityManager) load(cm, EntityManager.class.getName());

		if ("true".equals(ServerConfigurationService.getString(
				"search.experimental", "false")))
		{
			for (Iterator i = addEvents.iterator(); i.hasNext();)
			{
				searchService.registerFunction((String) i.next());
			}
			for (Iterator i = removeEvents.iterator(); i.hasNext();)
			{
				searchService.registerFunction((String) i.next());
			}
			searchIndexBuilder.registerEntityContentProducer(this);
		}
	}

	private Object load(ComponentManager cm, String name)
	{
		Object o = cm.get(name);
		if (o == null)
		{
			log.error("Cant find Spring component named " + name);
		}
		return o;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isContentFromReader(Entity cr)
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Reader getContentReader(Entity cr)
	{
		return new StringReader(getContent(cr));
	}

	/**
	 * {@inheritDoc}
	 */
	public String getContent(Entity cr)
	{
		Reference ref = entityManager.newReference(cr.getReference());
		EntityProducer ep = ref.getEntityProducer();

		if (ep instanceof MessageService)
		{
			try
			{
				MessageService ms = (MessageService) ep;
				Message m = ms.getMessage(ref);
				MessageHeader mh = m.getHeader();
				StringBuffer sb = new StringBuffer();
				Class c = mh.getClass();
				try {
					Method getSubject = c.getMethod("getSubject",new Class[] {} );
					Object o = getSubject.invoke(mh,new Object[]{});
					sb.append("Subject: ").append(o.toString()).append("\n");
				} catch ( Exception ex ) {
					// no subject, and I dont mind
					log.debug("Didnt get Subject  from "+mh,ex);
				}

				sb.append("Message Headers\n");
				sb.append("From ").append(mh.getFrom().getDisplayName())
						.append("\n");
				sb.append("Message Body\n");
				sb.append(m.getBody()).append("\n");
				log.debug("Message Content for " + cr.getReference() + " is "
						+ sb.toString());

				// resolve attachments
				List attachments = mh.getAttachments();
				for ( Iterator atti = attachments.iterator(); atti.hasNext(); ) {
					try {
						Reference attr = (Reference) atti.next();
						EntityContentProducer ecp = searchIndexBuilder.newEntityContentProducer(attr);
						String attachementDigest = ecp.getContent(attr.getEntity());
						sb.append("Attachement: \n").append(attachementDigest).append("\n");
					} catch ( Exception ex ) {
						log.info(" Failed to digest attachement "+ex.getMessage());
					}
				}
				
				
				
				
				
				
				return sb.toString();
			}
			catch (IdUnusedException e)
			{
				throw new RuntimeException(" Failed to get message content ", e);
			}
			catch (PermissionException e)
			{
				throw new RuntimeException(" Failed to get message content ", e);
			}
		}
		throw new RuntimeException(" Not a Message Entity " + cr);
	}

	/**
	 * @{inheritDoc}
	 */
	public String getTitle(Entity cr)
	{
		Reference ref = entityManager.newReference(cr.getReference());
		EntityProducer ep = ref.getEntityProducer();
		if (ep instanceof MessageService)
		{
			try
			{
				MessageService ms = (MessageService) ep;
				Message m = ms.getMessage(ref);
				MessageHeader mh = m.getHeader();
				Class c = mh.getClass();
				String subject = "Message ";
				try {
					Method getSubject = c.getMethod("getSubject",new Class[] {} );
					Object o = getSubject.invoke(mh,new Object[]{});
					subject = "Subject: "+o.toString()+" ";
				} catch ( Exception ex ) {
					log.debug("Didnt get Subject  from "+mh);
				}
				
				
				return subject+"From " + mh.getFrom().getDisplayName();
			}
			catch (IdUnusedException e)
			{
				throw new RuntimeException(" Failed to get message content ", e);
			}
			catch (PermissionException e)
			{
				throw new RuntimeException(" Failed to get message content ", e);
			}
		}
		throw new RuntimeException(" Not a Message Entity " + cr);

	}

	/**
	 * @{inheritDoc}
	 */
	public String getUrl(Entity entity)
	{
		return entity.getUrl();
	}

	/**
	 * @{inheritDoc}
	 */
	public boolean matches(Reference ref)
	{

		EntityProducer ep = ref.getEntityProducer();

		if (ep.getClass().equals(messageService.getClass()))
		{
			return true;
		}
		return false;
	}

	/**
	 * @{inheritDoc}
	 */
	public List getAllContent()
	{
		List all = new ArrayList();
		List l = messageService.getChannels();
		for (Iterator i = l.iterator(); i.hasNext();)
		{
			
			try
			{
				MessageChannel c = (MessageChannel) i.next();

				List messages = c.getMessages(null, true);
				// WARNING: I think the implementation caches on thread, if this
				// is
				// a builder
				// thread this may not work
				for (Iterator mi = messages.iterator(); mi.hasNext();)
				{
					Message m = (Message) mi.next();
					all.add(m.getReference());
				}
			}
			catch (Exception ex)
			{
				log.error("Got error on channel ",ex);

			}
		}
		return all;
	}

	/**
	 * @{inheritDoc}
	 */
	public Integer getAction(Event event)
	{
		String evt = event.getEvent();
		if (evt == null) return SearchBuilderItem.ACTION_UNKNOWN;
		for (Iterator i = addEvents.iterator(); i.hasNext();)
		{
			String match = (String) i.next();
			if (evt.equals(match))
			{
				return SearchBuilderItem.ACTION_ADD;
			}
		}
		for (Iterator i = removeEvents.iterator(); i.hasNext();)
		{
			String match = (String) i.next();
			if (evt.equals(match))
			{
				return SearchBuilderItem.ACTION_DELETE;
			}
		}
		return SearchBuilderItem.ACTION_UNKNOWN;
	}

	/**
	 * @{inheritDoc}
	 */
	public boolean matches(Event event)
	{
		Reference ref = entityManager.newReference(event.getResource());
		return matches(ref);
	}

	/**
	 * @{inheritDoc}
	 */
	public String getTool()
	{
		return toolName;
	}

	/**
	 * @return Returns the addEvents.
	 */
	public List getAddEvents()
	{
		return addEvents;
	}

	/**
	 * @param addEvents
	 *        The addEvents to set.
	 */
	public void setAddEvents(List addEvents)
	{
		this.addEvents = addEvents;
	}

	/**
	 * @return Returns the messageService.
	 */
	public MessageService getMessageService()
	{
		return messageService;
	}

	/**
	 * @param messageService
	 *        The messageService to set.
	 */
	public void setMessageService(MessageService messageService)
	{
		this.messageService = messageService;
	}

	/**
	 * @return Returns the toolName.
	 */
	public String getToolName()
	{
		return toolName;
	}

	/**
	 * @param toolName
	 *        The toolName to set.
	 */
	public void setToolName(String toolName)
	{
		this.toolName = toolName;
	}

	/**
	 * @return Returns the removeEvents.
	 */
	public List getRemoveEvents()
	{
		return removeEvents;
	}

	/**
	 * @param removeEvents
	 *        The removeEvents to set.
	 */
	public void setRemoveEvents(List removeEvents)
	{
		this.removeEvents = removeEvents;
	}

	public String getSiteId(Reference ref)
	{
		return ref.getContext();
	}

	public String getSiteId(String resourceName)
	{

		return getSiteId(entityManager.newReference(resourceName));
	}

	public List getSiteContent(String context)
	{
		List all = new ArrayList();
		List l = messageService.getChannelIds(context);
		for (Iterator i = l.iterator(); i.hasNext();)
		{
			String chanellId = (String) i.next();
			try
			{

				MessageChannel c = messageService.getChannel(messageService.channelReference(context,chanellId));

				List messages = c.getMessages(null, true);
				// WARNING: I think the implementation caches on thread, if this
				// is
				// a builder
				// thread this may not work
				for (Iterator mi = messages.iterator(); mi.hasNext();)
				{
					Message m = (Message) mi.next();
					all.add(m.getReference());
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				log.warn("Failed to get channel " + chanellId);

			}
		}
		return all;
	}

	public boolean isForIndex(Reference ref)
	{

		EntityProducer ep = ref.getEntityProducer();
		if (ep instanceof MessageService)
		{
			try
			{
				MessageService ms = (MessageService) ep;
				Message m = ms.getMessage(ref);
				if (m == null)
				{
					log.debug("Rejected null message " + ref.getReference());
					return false;
				}
			}
			catch (IdUnusedException e)
			{
				log.debug("Rejected Missing message or Collection "
						+ ref.getReference());
				return false;
			}
			catch (PermissionException e)
			{
				log.warn("Rejected private message " + ref.getReference());
				return false;
			}
			return true;
		}
		return false;
	}

	public boolean canRead(Reference ref)
	{
		EntityProducer ep = ref.getEntityProducer();
		if (ep instanceof MessageService)
		{
			try
			{
				MessageService ms = (MessageService) ep;
				ms.getMessage(ref);
				return true;
			}
			catch (Exception ex)
			{
			}
		}
		return false;
	}

	public Map getCustomProperties()
	{
		return null;
	}

	public String getCustomRDF()
	{
		return null;
	}

}
