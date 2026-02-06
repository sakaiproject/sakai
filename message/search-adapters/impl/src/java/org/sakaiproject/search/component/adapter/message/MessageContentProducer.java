/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/portal/trunk/portal-impl/impl/src/java/org/sakaiproject/portal/charon/SessionRequestHolder.java $
 * $Id: SessionRequestHolder.java 14690 2006-09-15 11:43:18Z ian@caret.cam.ac.uk $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.search.component.adapter.message;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.message.api.Message;
import org.sakaiproject.message.api.MessageChannel;
import org.sakaiproject.message.api.MessageHeader;
import org.sakaiproject.message.api.MessageService;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.EntityContentProducerEvents;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchUtils;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.util.HTMLParser;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.ContextualUserDisplayService;
import org.sakaiproject.util.ResourceLoader;

/**
 * @author ieb
 */
@Slf4j
public class MessageContentProducer implements EntityContentProducer, EntityContentProducerEvents
{

	private static final String BUNDLE_NAME = "org.sakaiproject.search.component.adapter.message.bundle.Messages"; //$NON-NLS-1$

	private static final ResourceLoader RESOURCE_BUNDLE = new ResourceLoader(BUNDLE_NAME);

	@Setter
	private String toolName;

	@Setter
	private List<String> addEvents = new ArrayList<>();

	@Setter
	private List<String> removeEvents = new ArrayList<>();

	@Setter
	private MessageService messageService;

	@Setter
	private SearchIndexBuilder searchIndexBuilder;

	@Setter
	private EntityManager entityManager;

	@Setter
	private SiteService siteService;

	// Map of events to their corresponding search index actions - built from Spring configuration
	private Map<String, Integer> eventActions = new HashMap<>();

	//ContextualDisplayService
	ContextualUserDisplayService contextualUserDisplayService;

	public void init()
	{
		// Build EVENT_ACTIONS map from Spring-configured lists
		for (String event : addEvents) {
			eventActions.put(event, SearchBuilderItem.ACTION_ADD);
		}
		for (String event : removeEvents) {
			eventActions.put(event, SearchBuilderItem.ACTION_DELETE);
		}

		searchIndexBuilder.registerEntityContentProducer(this);

		contextualUserDisplayService = (ContextualUserDisplayService) ComponentManager.get("org.sakaiproject.user.api.ContextualUserDisplayService");
	}

	@Override
	public boolean isContentFromReader(String reference)
	{
		return false;
	}

	@Override
	public Reader getContentReader(String reference)
	{
		return new StringReader(getContent(reference));
	}

	protected Reference getReference(String reference)
	{
		try
		{
			Reference r = entityManager.newReference(reference);
			if (log.isDebugEnabled())
			{
				log.debug("Message." + toolName + ".getReference" + reference + ":" + r);
			}
			return r;
		}
		catch (Exception ex)
		{
			log.debug(ex.getMessage());
		}
		return null;
	}

	private EntityProducer getProducer(Reference ref)
	{
		try
		{
			return ref.getEntityProducer();
		}
		catch (Exception ex)
		{
			log.debug(ex.getMessage());
		}
		return null;
	}

	@Override
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
					sb.append(RESOURCE_BUNDLE.getString("MessageContentProducer.5"));
					SearchUtils.appendCleanString(o.toString(), sb);
					sb.append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				catch (Exception ex)
				{
					// no subject, and I dont mind
					log.debug("Didnt get Subject  from " + mh, ex); //$NON-NLS-1$
				}

				sb.append(RESOURCE_BUNDLE.getString("MessageContentProducer.3")); //$NON-NLS-1$
				sb.append(RESOURCE_BUNDLE.getString("MessageContentProducer.4"));
				//is the user aliased in this context?
				String displayName = null;
				if (contextualUserDisplayService != null)
				{
					Reference ref1 = entityManager.newReference(m.getReference());
					String context = siteService.siteReference(ref1.getContext());
					displayName = contextualUserDisplayService.getUserDisplayName(mh.getFrom(), context);
					//the service may return a null
					if (displayName == null)
						displayName = mh.getFrom().getDisplayName();
				}
				else
				{
					displayName = mh.getFrom().getDisplayName();
				}
				SearchUtils.appendCleanString(displayName, sb); //$NON-NLS-1$
				sb.append("\n"); //$NON-NLS-1$
				sb.append(RESOURCE_BUNDLE.getString("MessageContentProducer.11")); //$NON-NLS-1$
				String mBody = m.getBody();

				for (HTMLParser hp = new HTMLParser(mBody); hp.hasNext();)
				{
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
						sb
								.append(
										RESOURCE_BUNDLE
												.getString("MessageContentProducer.23")).append(attachementDigest) //$NON-NLS-1$
								.append("\n"); //$NON-NLS-1$
					}
					catch (Exception ex)
					{
						log.info(" Failed to digest attachement " //$NON-NLS-1$
								+ ex.getMessage());
					}
				}
				String r = sb.toString();
				if (log.isDebugEnabled())
				{
					log
							.debug("Message." + toolName + ".getContent" + reference
									+ ":" + r);
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

	@Override
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
				String subject = RESOURCE_BUNDLE.getString("MessageContentProducer.2"); //$NON-NLS-1$
				try
				{
					
					
					Method getSubject = c.getMethod("getSubject", //$NON-NLS-1$
							new Class[] {});
					Object o = getSubject.invoke(mh, new Object[] {});
					subject = RESOURCE_BUNDLE.getString("MessageContentProducer.33") + o.toString() + " "; //$NON-NLS-1$ //$NON-NLS-2$
				}
				catch (Exception ex)
				{
					log.debug("Didnt get Subject  from " + mh); //$NON-NLS-1$
				}
				
				//is the user aliased in this context?
				String displayName = null;
				if (contextualUserDisplayService != null)
				{
					Reference ref1 = entityManager.newReference(m.getReference());
					String context = siteService.siteReference(ref1.getContext());
					displayName = contextualUserDisplayService.getUserDisplayName(mh.getFrom(), context);
					//the service may return a null
					if (displayName == null)
						displayName = mh.getFrom().getDisplayName();
				}
				else
				{
					displayName = mh.getFrom().getDisplayName();
				}
				
				String title = subject
						+ RESOURCE_BUNDLE.getString("MessageContentProducer.36") //$NON-NLS-1$
						+ displayName;

				String r = SearchUtils.appendCleanString(title, null).toString();
				if (log.isDebugEnabled())
				{
					log.debug("Message." + toolName + ".getTitle" + reference + ":" + r);
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

	@Override
	public String getUrl(String reference)
	{
		Reference ref = getReference(reference);
		return ref.getUrl();
	}

	@Override
	public String getUrl(String ref, Entity.UrlType urlType) {
		return entityManager.getUrl(ref, urlType).orElse("");
	}

	@Override
	public boolean matches(String reference)
	{
		Reference ref = getReference(reference);
		EntityProducer ep = getProducer(ref);

		if (ep != null && ep.getClass().equals(messageService.getClass()))
		{
			return true;
		}
		return false;
	}


	@Override
	public Integer getAction(Event event)
	{
		return eventActions.getOrDefault(event.getEvent(), SearchBuilderItem.ACTION_UNKNOWN);
	}

	@Override
	public boolean matches(Event event)
	{
		return eventActions.containsKey(event.getEvent());
	}

	@Override
	public Set<String> getTriggerFunctions()
	{
		return eventActions.keySet();
	}

	@Override
	public String getTool()
	{
		return toolName;
	}

	private String getSiteId(Reference ref)
	{
		return ref.getContext();
	}

	@Override
	public String getSiteId(String resourceName)
	{
		return getSiteId(entityManager.newReference(resourceName));
	}

	public List getSiteContent(String context)
	{
		List<String> all = new ArrayList<String>();
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
				log.warn("Failed to get channel " + chanellId); //$NON-NLS-1$

			}
		}
		return all;
	}

	@Override
	public Iterator getSiteContentIterator(final String context)
	{
		List l = messageService.getChannelIds(context);
		final Iterator ci = l.iterator();
		return new Iterator()
		{
			MessageChannel mc = null;
			int messageCount = -1;
			int nextMessage = -1;  // Index overall messages - Starts at 1
			List messages = null;
			int listPos = 0;  // Index each chunk - Starts at zero
			int chunkSize = 100;  // Retrieve 100 at a time

			public boolean hasNext()
			{
				if (mc == null)
				{
					return nextIterator();
				}
				else
				{
					if (messageCount > 1 && nextMessage <= messageCount)
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
						mc = messageService.getChannel(messageService
								.channelReference(context, chanellId));
						messageCount = mc.getCount();
						if (messageCount > 0 )
						{
							nextMessage = 1;  // Pager starts at 1
							return true;
						}
					}
					catch (Exception ex)
					{
						log.warn("Failed to get channel " + chanellId); //$NON-NLS-1$
					}
				}
				mc = null;
				nextMessage = -1;
				messageCount = -1;
				return false;
			}

			/*
			 * Loop though the messages in the channel grabbing them 
			 * in chunkSize chunks for efficiency. 
			 */
			public Object next()
			{
				if ( messages != null && listPos >= 0 && listPos < messages.size() )
				{
					Message m = (Message) messages.get(listPos);
					nextMessage = nextMessage + 1;
					listPos = listPos + 1;
					return m.getReference();
				}

				// Retrieve the next "chunk"
				PagingPosition pages = new PagingPosition(nextMessage, (nextMessage + chunkSize - 1));
				try 
				{
					messages = mc.getMessages(null, true, pages);
					if ( messages != null && messages.size() > 0 )
					{
						listPos = 0;
						Message m = (Message) messages.get(listPos);
						nextMessage = nextMessage + 1;
						listPos = listPos + 1;
						return m.getReference();
					}
				}
				catch (Exception ex)
				{
					log.warn("Failed to get message " + nextMessage); //$NON-NLS-1$
					
				}
				// We are done looping through this channel
				nextMessage = messageCount + 1;
				return null;
			}

			public void remove()
			{
				throw new UnsupportedOperationException("Remove not implemented"); //$NON-NLS-1$
			}

		};
	}

	@Override
	public boolean isForIndex(String reference)
	{
		Reference ref = getReference(reference);
		EntityProducer ep = getProducer(ref);
		if (ep instanceof MessageService)
		{
			try
			{
				Message m = messageService.getMessage(ref);

				if (m == null)
				{
					log.info("No message for reference {}", reference);
					return false;
				}
			}
			catch (IdUnusedException e)
			{
				log.info("Rejected Missing message or Collection: {}", reference);
				return false;
			}
			catch (PermissionException e)
			{
				log.warn("Rejected private message {}: {}", reference, e.toString());
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean canRead(String reference) {

		Reference ref = getReference(reference);
		EntityProducer ep = getProducer(ref);
		if (ep instanceof MessageService) {
			try {
				Message m = messageService.getMessage(ref);

                if (m == null) {
					log.info("No message for reference {}", reference);
					return false;
                }

                return messageService.isMessageViewable(m);
			} catch (Exception ex) {
                log.warn("Exception whilst checking for search item readability: {}", ex.toString());
			}
		}
		return false;
	}

	@Override
	public String getId(String reference)
	{
		try
		{
			String r = getReference(reference).getId();
			if (log.isDebugEnabled())
			{
				log.debug("Message.{}.getContainer{}:{}", toolName, reference, r);
			}
			return r;
		}
		catch (Exception ex)
		{
			return "";
		}
	}

	@Override
	public String getSubType(String reference)
	{
		try
		{
			String r = getReference(reference).getSubType();
			if (log.isDebugEnabled())
			{
				log.debug("Message.{}.getContainer{}:{}", toolName, reference, r);
			}
			return r;
		}
		catch (Exception ex)
		{
			return "";
		}
	}

	@Override
	public String getType(String reference)
	{
		try
		{
			String r = getReference(reference).getType();
			if (log.isDebugEnabled())
			{
				log.debug("Message.{}.getContainer{}:{}", toolName, reference, r);
			}
			return r;
		}
		catch (Exception ex)
		{
			return "";
		}
	}

	@Override
	public String getContainer(String reference)
	{
		try
		{
			String r = getReference(reference).getContainer();
			log.debug("Message.{}.getContainer{}:{}", toolName, reference, r);
			return r;
		}
		catch (Exception ex)
		{
			return "";
		}
	}
}
