/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/portal/trunk/portal-impl/impl/src/java/org/sakaiproject/portal/charon/SessionRequestHolder.java $
 * $Id: SessionRequestHolder.java 14690 2006-09-15 11:43:18Z ian@caret.cam.ac.uk $
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
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
import org.sakaiproject.search.api.SearchUtils;
import org.sakaiproject.search.component.Messages;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.util.HTMLParser;

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

		if ( "true".equals(ServerConfigurationService.getString(
				"search.enable", "false")))
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
			log.error("Cant find Spring component named " + name); //$NON-NLS-1$
		}
		return o;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isContentFromReader(String reference)
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Reader getContentReader(String reference)
	{
		return new StringReader(getContent(reference));
	}
	
	private Reference getReference(String reference) {
		try {
			Reference r = entityManager.newReference(reference);
			if (log.isDebugEnabled() ) {
					log.debug("Message."+toolName+".getReference"+reference+":"+r);
			}
			return r;		
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

	/**
	 * {@inheritDoc}
	 */
	public String getContent(String reference)
	{
		Reference ref = getReference(reference);
		EntityProducer ep = getProducer(ref);

		if (ep instanceof MessageService)
		{
			try
			{
				MessageService ms = (MessageService) ep;
				Message m = ms.getMessage(ref);
				MessageHeader mh = m.getHeader();
				StringBuilder sb = new StringBuilder();
				Class c = mh.getClass();
				try
				{
					Method getSubject = c.getMethod("getSubject", //$NON-NLS-1$
							new Class[] {});
					Object o = getSubject.invoke(mh, new Object[] {});
					sb.append(Messages.getString("MessageContentProducer.5"));
					SearchUtils.appendCleanString(o.toString(),sb);
					sb.append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				catch (Exception ex)
				{
					// no subject, and I dont mind
					log.debug("Didnt get Subject  from " + mh, ex); //$NON-NLS-1$
				}

				sb.append(Messages.getString("MessageContentProducer.3")); //$NON-NLS-1$
				sb.append(Messages.getString("MessageContentProducer.4"));
				SearchUtils.appendCleanString(mh.getFrom().getDisplayName(),sb); //$NON-NLS-1$
				sb.append("\n"); //$NON-NLS-1$
				sb.append(Messages.getString("MessageContentProducer.11")); //$NON-NLS-1$
				String mBody = m.getBody();

				for ( HTMLParser hp = new HTMLParser(mBody); hp.hasNext(); ) {
					SearchUtils.appendCleanString(hp.next(), sb);
					sb.append(" ");
				}
				
				sb.append("\n"); //$NON-NLS-1$
				log.debug("Message Content for " + ref.getReference() + " is " //$NON-NLS-1$ //$NON-NLS-2$
						+ sb.toString());

				// resolve attachments
				List attachments = mh.getAttachments();
				for (Iterator atti = attachments.iterator(); atti.hasNext();)
				{
					try
					{
						Reference attr = (Reference) atti.next();
						String areference = attr.getReference();
						EntityContentProducer ecp = searchIndexBuilder
								.newEntityContentProducer(areference);
						String attachementDigest = ecp.getContent(areference);
						sb.append(Messages.getString("MessageContentProducer.23")).append(attachementDigest) //$NON-NLS-1$
								.append("\n"); //$NON-NLS-1$
					}
					catch (Exception ex)
					{
						log.info(" Failed to digest attachement " //$NON-NLS-1$
								+ ex.getMessage());
					}
				}
				String r = sb.toString();
				if (log.isDebugEnabled() ) {
					log.debug("Message."+toolName+".getContent"+reference+":"+r);
				}
				return r;
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

	/**
	 * @{inheritDoc}
	 */
	public String getTitle(String reference)
	{
		Reference ref = getReference(reference);
		EntityProducer ep = getProducer(ref);
		if (ep instanceof MessageService)
		{
			try
			{
				MessageService ms = (MessageService) ep;
				Message m = ms.getMessage(ref);
				MessageHeader mh = m.getHeader();
				Class c = mh.getClass();
				String subject = Messages.getString("MessageContentProducer.2"); //$NON-NLS-1$
				try
				{
					Method getSubject = c.getMethod("getSubject", //$NON-NLS-1$
							new Class[] {});
					Object o = getSubject.invoke(mh, new Object[] {});
					subject = Messages.getString("MessageContentProducer.33") + o.toString() + " "; //$NON-NLS-1$ //$NON-NLS-2$
				}
				catch (Exception ex)
				{
					log.debug("Didnt get Subject  from " + mh); //$NON-NLS-1$
				}

				String title = subject + Messages.getString("MessageContentProducer.36") //$NON-NLS-1$
						+ mh.getFrom().getDisplayName();
				
				String r = SearchUtils.appendCleanString(title,null).toString();
				if (log.isDebugEnabled() ) {
					log.debug("Message."+toolName+".getTitle"+reference+":"+r);
				}
				return r;
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

	/**
	 * @{inheritDoc}
	 */
	public String getUrl(String reference)
	{
		Reference ref = getReference(reference);
		return ref.getUrl();
	}

	/**
	 * @{inheritDoc}
	 */
	public boolean matches(String reference)
	{
		Reference ref = getReference(reference);
		EntityProducer ep = getProducer(ref);

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
				log.error("Got error on channel ", ex); //$NON-NLS-1$

			}
		}
		if (log.isDebugEnabled() ) {
			log.debug("Message."+toolName+".getAllContent::"+all.size());
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
		return matches(event.getResource());
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

	private String getSiteId(Reference ref)
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

				MessageChannel c = messageService.getChannel(messageService
						.channelReference(context, chanellId));

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
				log.warn("Failed to get channel " + chanellId); //$NON-NLS-1$

			}
		}
		return all;
	}

	public Iterator getSiteContentIterator(final String context)
	{
		List l = messageService.getChannelIds(context);
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

					String chanellId = (String) ci.next();
					try
					{
						MessageChannel c = messageService
								.getChannel(messageService.channelReference(
										context, chanellId));
						List messages = c.getMessages(null, true);
						mi = messages.iterator();
						if (mi.hasNext())
						{
							return true;
						}
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
						log.warn("Failed to get channel " + chanellId); //$NON-NLS-1$

					}
				}
				return false;
			}

			public Object next()
			{
				Message m = (Message) mi.next();
				return m.getReference();
			}

			public void remove()
			{
				throw new UnsupportedOperationException(
						"Remove not implemented"); //$NON-NLS-1$
			}

		};
	}

	public boolean isForIndex(String reference)
	{

		Reference ref = getReference(reference);
		EntityProducer ep = getProducer(ref);
		if (ep instanceof MessageService)
		{
			try
			{
				MessageService ms = (MessageService) ep;
				Message m = ms.getMessage(ref);
				if (m == null)
				{
					log.debug("Rejected null message " + ref.getReference()); //$NON-NLS-1$
					return false;
				}
			}
			catch (IdUnusedException e)
			{
				log.debug("Rejected Missing message or Collection " //$NON-NLS-1$
						+ ref.getReference());
				return false;
			}
			catch (PermissionException e)
			{
				log.warn("Rejected private message " + ref.getReference()); //$NON-NLS-1$
				return false;
			}
			return true;
		}
		return false;
	}

	public boolean canRead(String reference)
	{
		Reference ref = getReference(reference);
		EntityProducer ep = getProducer(ref);
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

	public Map getCustomProperties(String ref)
	{
		return null;
	}

	public String getCustomRDF(String ref)
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.EntityContentProducer#getId(java.lang.String)
	 */
	public String getId(String reference)
	{
		try {
			String r = getReference(reference).getId();
			if (log.isDebugEnabled() ) {
				log.debug("Message."+toolName+".getContainer"+reference+":"+r);
			}
			return r;
		} catch ( Exception ex ) {
			return "";
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.EntityContentProducer#getSubType(java.lang.String)
	 */
	public String getSubType(String reference)
	{
		try {
			String r = getReference(reference).getSubType();
			if (log.isDebugEnabled() ) {
				log.debug("Message."+toolName+".getContainer"+reference+":"+r);
			}
			return r;
		} catch ( Exception ex ) {
			return "";
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.EntityContentProducer#getType(java.lang.String)
	 */
	public String getType(String reference)
	{
		try {
			String r = getReference(reference).getType();
			if (log.isDebugEnabled() ) {
				log.debug("Message."+toolName+".getContainer"+reference+":"+r);
			}
			return r;
		} catch ( Exception ex ) {
			return "";
		}
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.search.api.EntityContentProducer#getType(java.lang.String)
	 */
	public String getContainer(String reference)
	{
		try {
			String r = getReference(reference).getContainer();
			if (log.isDebugEnabled() ) {
				log.debug("Message."+toolName+".getContainer"+reference+":"+r);
			}
			return r;
		} catch ( Exception ex ) {
			return "";
		}
	}

}
