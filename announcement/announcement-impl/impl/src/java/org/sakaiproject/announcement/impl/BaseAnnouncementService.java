/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.announcement.impl;

import java.io.Writer;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.text.DateFormat;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementChannelEdit;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageEdit;
import org.sakaiproject.announcement.api.AnnouncementMessageHeader;
import org.sakaiproject.announcement.api.AnnouncementMessageHeaderEdit;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ContextObserver;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Summary;
import org.sakaiproject.entity.api.EntityAccessOverloadException;
import org.sakaiproject.entity.api.EntityCopyrightException;
import org.sakaiproject.entity.api.EntityNotDefinedException;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.message.api.Message;
import org.sakaiproject.message.api.MessageChannel;
import org.sakaiproject.message.api.MessageHeader;
import org.sakaiproject.message.api.MessageHeaderEdit;
import org.sakaiproject.message.impl.BaseMessageService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.cover.AliasService;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * <p>
 * BaseAnnouncementService extends the BaseMessageService for the specifics of Announcement.
 * </p>
 */
public abstract class BaseAnnouncementService extends BaseMessageService implements AnnouncementService, ContextObserver,
		EntityTransferrer
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(BaseAnnouncementService.class);

	/** Messages, for the http access. */
	protected static ResourceLoader rb = new ResourceLoader("annc-access");
	
	// XML DocumentBuilder and Transformer for RSS Feed
	private DocumentBuilder docBuilder = null;
	private Transformer docTransformer = null;
	
	private ContentHostingService contentHostingService;
	/**
	 * Dependency: contentHostingService.
	 * 
	 * @param service
	 *        The NotificationService.
	 */
	public void setContentHostingService(ContentHostingService service)
	{
		contentHostingService = service;
	}
	
	/**********************************************************************************************************************************************************************************************************************************************************
	 * Constructors, Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** Dependency: NotificationService. */
	protected NotificationService m_notificationService = null;

	/**
	 * Dependency: NotificationService.
	 * 
	 * @param service
	 *        The NotificationService.
	 */
	public void setNotificationService(NotificationService service)
	{
		m_notificationService = service;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			super.init();

			// register a transient notification for announcements
			NotificationEdit edit = m_notificationService.addTransientNotification();

			// set functions
			edit.setFunction(eventId(SECURE_ADD));
			edit.addFunction(eventId(SECURE_UPDATE_OWN));
			edit.addFunction(eventId(SECURE_UPDATE_ANY));

			// set the filter to any announcement resource (see messageReference())
			edit.setResourceFilter(getAccessPoint(true) + Entity.SEPARATOR + REF_TYPE_MESSAGE);

			// set the action
			edit.setAction(new SiteEmailNotificationAnnc());

			// register functions
			FunctionManager.registerFunction(eventId(SECURE_READ));
			FunctionManager.registerFunction(eventId(SECURE_ADD));
			FunctionManager.registerFunction(eventId(SECURE_REMOVE_ANY));
			FunctionManager.registerFunction(eventId(SECURE_REMOVE_OWN));
			FunctionManager.registerFunction(eventId(SECURE_UPDATE_ANY));
			FunctionManager.registerFunction(eventId(SECURE_UPDATE_OWN));
			FunctionManager.registerFunction(eventId(SECURE_ALL_GROUPS));

			// Sakai v2.4: UI end says hidden, 'under the covers' says draft
			// Done so import from old sites causes drafts to 'become' hidden in new sites
			FunctionManager.registerFunction(eventId(SECURE_READ_DRAFT));

			// entity producer registration
			m_entityManager.registerEntityProducer(this, REFERENCE_ROOT);

			// create DocumentBuilder for RSS Feed
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(true);  // ignore comments
			factory.setNamespaceAware(true);		// namespace aware should be true
			factory.setValidating(false);		   // we're not validating
			docBuilder = factory.newDocumentBuilder();
			
			TransformerFactory tFactory = TransformerFactory.newInstance();
			docTransformer = tFactory.newTransformer();
			
			M_log.info("init()");
		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}

	} // init

	/**********************************************************************************************************************************************************************************************************************************************************
	 * StorageUser implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Construct a new continer given just ids.
	 * 
	 * @param ref
	 *        The container reference.
	 * @return The new containe Resource.
	 */
	public Entity newContainer(String ref)
	{
		return new BaseAnnouncementChannelEdit(ref);
	}

	/**
	 * Construct a new container resource, from an XML element.
	 * 
	 * @param element
	 *        The XML.
	 * @return The new container resource.
	 */
	public Entity newContainer(Element element)
	{
		return new BaseAnnouncementChannelEdit(element);
	}

	/**
	 * Construct a new container resource, as a copy of another
	 * 
	 * @param other
	 *        The other contianer to copy.
	 * @return The new container resource.
	 */
	public Entity newContainer(Entity other)
	{
		return new BaseAnnouncementChannelEdit((MessageChannel) other);
	}

	/**
	 * Construct a new rsource given just an id.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param id
	 *        The id for the new object.
	 * @param others
	 *        (options) array of objects to load into the Resource's fields.
	 * @return The new resource.
	 */
	public Entity newResource(Entity container, String id, Object[] others)
	{
		return new BaseAnnouncementMessageEdit((MessageChannel) container, id);
	}

	/**
	 * Construct a new resource, from an XML element.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param element
	 *        The XML.
	 * @return The new resource from the XML.
	 */
	public Entity newResource(Entity container, Element element)
	{
		return new BaseAnnouncementMessageEdit((MessageChannel) container, element);
	}

	/**
	 * Construct a new resource from another resource of the same type.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param other
	 *        The other resource.
	 * @return The new resource as a copy of the other.
	 */
	public Entity newResource(Entity container, Entity other)
	{
		return new BaseAnnouncementMessageEdit((MessageChannel) container, (Message) other);
	}

	/**
	 * Construct a new continer given just ids.
	 * 
	 * @param ref
	 *        The container reference.
	 * @return The new containe Resource.
	 */
	public Edit newContainerEdit(String ref)
	{
		BaseAnnouncementChannelEdit rv = new BaseAnnouncementChannelEdit(ref);
		rv.activate();
		return rv;
	}

	/**
	 * Construct a new container resource, from an XML element.
	 * 
	 * @param element
	 *        The XML.
	 * @return The new container resource.
	 */
	public Edit newContainerEdit(Element element)
	{
		BaseAnnouncementChannelEdit rv = new BaseAnnouncementChannelEdit(element);
		rv.activate();
		return rv;
	}

	/**
	 * Construct a new container resource, as a copy of another
	 * 
	 * @param other
	 *        The other contianer to copy.
	 * @return The new container resource.
	 */
	public Edit newContainerEdit(Entity other)
	{
		BaseAnnouncementChannelEdit rv = new BaseAnnouncementChannelEdit((MessageChannel) other);
		rv.activate();
		return rv;
	}

	/**
	 * Construct a new rsource given just an id.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param id
	 *        The id for the new object.
	 * @param others
	 *        (options) array of objects to load into the Resource's fields.
	 * @return The new resource.
	 */
	public Edit newResourceEdit(Entity container, String id, Object[] others)
	{
		BaseAnnouncementMessageEdit rv = new BaseAnnouncementMessageEdit((MessageChannel) container, id);
		rv.activate();
		return rv;
	}

	/**
	 * Construct a new resource, from an XML element.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param element
	 *        The XML.
	 * @return The new resource from the XML.
	 */
	public Edit newResourceEdit(Entity container, Element element)
	{
		BaseAnnouncementMessageEdit rv = new BaseAnnouncementMessageEdit((MessageChannel) container, element);
		rv.activate();
		return rv;
	}

	/**
	 * Construct a new resource from another resource of the same type.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param other
	 *        The other resource.
	 * @return The new resource as a copy of the other.
	 */
	public Edit newResourceEdit(Entity container, Entity other)
	{
		BaseAnnouncementMessageEdit rv = new BaseAnnouncementMessageEdit((MessageChannel) container, (Message) other);
		rv.activate();
		return rv;
	}

	/**
	 * Collect the fields that need to be stored outside the XML (for the resource).
	 * 
	 * @return An array of field values to store in the record outside the XML (for the resource).
	 */
	public Object[] storageFields(Entity r)
	{
		Object[] rv = new Object[4];
		rv[0] = ((Message) r).getHeader().getDate();
		rv[1] = ((Message) r).getHeader().getFrom().getId();
		rv[2] = ((AnnouncementMessage) r).getAnnouncementHeader().getDraft() ? "1" : "0";
		rv[3] = r.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW) == null ? "0" : "1";
		// rv[3] = ((AnnouncementMessage) r).getAnnouncementHeader().getAccess() == MessageHeader.MessageAccess.PUBLIC ? "1" : "0";

		return rv;
	}

	/**
	 * Check if this resource is in draft mode.
	 * 
	 * @param r
	 *        The resource.
	 * @return true if the resource is in draft mode, false if not.
	 */
	public boolean isDraft(Entity r)
	{
		return ((AnnouncementMessage) r).getAnnouncementHeader().getDraft();
	}

	/**
	 * Access the resource owner user id.
	 * 
	 * @param r
	 *        The resource.
	 * @return The resource owner user id.
	 */
	public String getOwnerId(Entity r)
	{
		return ((Message) r).getHeader().getFrom().getId();
	}

	/**
	 * Access the resource date.
	 * 
	 * @param r
	 *        The resource.
	 * @return The resource date.
	 */
	public Time getDate(Entity r)
	{
		return ((Message) r).getHeader().getDate();
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Abstractions, etc. satisfied
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Report the Service API name being implemented.
	 */
	protected String serviceName()
	{
		return AnnouncementService.class.getName();
	}

	/**
	 * Construct a new message header from XML in a DOM element.
	 * 
	 * @param id
	 *        The message Id.
	 * @return The new message header.
	 */
	protected MessageHeaderEdit newMessageHeader(Message msg, String id)
	{
		return new BaseAnnouncementMessageHeaderEdit(msg, id);

	} // newMessageHeader

	/**
	 * Construct a new message header from XML in a DOM element.
	 * 
	 * @param el
	 *        The XML DOM element that has the header information.
	 * @return The new message header.
	 */
	protected MessageHeaderEdit newMessageHeader(Message msg, Element el)
	{
		return new BaseAnnouncementMessageHeaderEdit(msg, el);

	} // newMessageHeader

	/**
	 * Construct a new message header as a copy of another.
	 * 
	 * @param other
	 *        The other header to copy.
	 * @return The new message header.
	 */
	protected MessageHeaderEdit newMessageHeader(Message msg, MessageHeader other)
	{
		return new BaseAnnouncementMessageHeaderEdit(msg, other);

	} // newMessageHeader

	/**
	 * Form a tracking event string based on a security function string.
	 * 
	 * @param secure
	 *        The security function string.
	 * @return The event tracking string.
	 */
	protected String eventId(String secure)
	{
		return SECURE_ANNC_ROOT + secure;

	} // eventId

	/**
	 * Return the reference rooot for use in resource references and urls.
	 * 
	 * @return The reference rooot for use in resource references and urls.
	 */
	protected String getReferenceRoot()
	{
		return REFERENCE_ROOT;

	} // getReferenceRoot

	/**
	 * {@inheritDoc}
	 */
	public boolean parseEntityReference(String reference, Reference ref)
	{
		if (reference.startsWith(REFERENCE_ROOT))
		{
			String[] parts = StringUtil.split(reference, Entity.SEPARATOR);

			String id = null;
			String subType = null;
			String context = null;
			String container = null;

			// the first part will be null, then next the service, the third will be: "msg", "channel", "announcement" or "rss"
			if (parts.length > 2)
			{
				subType = parts[2];
				if (REF_TYPE_CHANNEL.equals(subType))
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
				else if (REF_TYPE_MESSAGE.equals(subType))
				{
					// next three parts are context, channel (container) and mesage id
					if (parts.length > 5)
					{
						context = parts[3];
						container = parts[4];
						id = parts[5];
					}
				}
				else if (REF_TYPE_ANNOUNCEMENT_RSS.equals(subType) || REF_TYPE_ANNOUNCEMENT.equals(subType))
				{
					// next is the context id
					if (parts.length > 3)
						context = parts[3];
				}
				else
					M_log.warn("parse(): unknown message subtype: " + subType + " in ref: " + reference);
			}

			// Translate context alias into site id (only for rss) if necessary
			if (REF_TYPE_ANNOUNCEMENT_RSS.equals(subType) &&(context != null) && (context.length() > 0))
			{
				if (!m_siteService.siteExists(context))
				{
					try
					{
						String aliasTarget = AliasService.getTarget(context);
						if (aliasTarget.startsWith(REFERENCE_ROOT)) // only support announcement aliases
						{
							parts = StringUtil.split(aliasTarget, Entity.SEPARATOR);
							if (parts.length > 3)
								context = parts[3];
						}
					}
					catch (Exception e)
					{
						M_log.debug(this+".parseEntityReference(): "+e.toString());
						return false;
					}
				}

				// if context still isn't valid, then no valid alias or site was specified
				if (!m_siteService.siteExists(context))
				{
					M_log.warn(this+".parseEntityReference() no valid site or alias: " + context);
					return false;
				}
			}

			ref.set(APPLICATION_ID, subType, id, container, context);

			return true;
		}

		return false;
	}
	
	/**
	 * @inheritDoc
	 */
	public Reference getAnnouncementReference(String context)
	{
      StringBuilder refString = new StringBuilder();
		refString.append(getAccessPoint(true));
		refString.append(Entity.SEPARATOR);
		refString.append(REF_TYPE_ANNOUNCEMENT);
		refString.append(Entity.SEPARATOR);
		refString.append(context);
		
		return  m_entityManager.newReference( refString.toString() );
	}

	/**
	 * @inheritDoc
	 */
	public String getRssUrl(Reference ref) 
	{
      String alias = null;
      List aliasList =  AliasService.getAliases( ref.getReference() );
		
      if ( ! aliasList.isEmpty() )
         alias = ((Alias)aliasList.get(0)).getId();
         
      StringBuilder rssUrlString = new StringBuilder();
		rssUrlString.append( m_serverConfigurationService.getAccessUrl() );
		rssUrlString.append(getAccessPoint(true));
		rssUrlString.append(Entity.SEPARATOR);
		rssUrlString.append(REF_TYPE_ANNOUNCEMENT_RSS);
		rssUrlString.append(Entity.SEPARATOR);

      if ( alias != null)
   		rssUrlString.append(alias);
      else
			rssUrlString.append(ref.getContext());
			
		return rssUrlString.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isMessageViewable(AnnouncementMessage message) 
	{
		final ResourceProperties messageProps = message.getProperties();

		final Time now = TimeService.newTime();
		try 
		{
			final Time releaseDate = message.getProperties().getTimeProperty(RELEASE_DATE);

			if (now.before(releaseDate)) 
			{
				return false;
			}
		}
		catch (Exception e) 
		{
			// Just not using/set Release Date
		} 

		try 
		{
			final Time retractDate = message.getProperties().getTimeProperty(RETRACT_DATE);
			
			if (now.after(retractDate)) 
			{
				return false;
			}
		}
		catch (Exception e) 
		{
			// Just not using/set Retract Date
		}
		
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void contextCreated(String context, boolean toolPlacement)
	{
		if (toolPlacement) enableMessageChannel(context);
	}

	/**
	 * {@inheritDoc}
	 */
	public void contextUpdated(String context, boolean toolPlacement)
	{
		if (toolPlacement) enableMessageChannel(context);
	}

	/**
	 * {@inheritDoc}
	 */
	public void contextDeleted(String context, boolean toolPlacement)
	{
		disableMessageChannel(context);
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] myToolIds()
	{
		String[] toolIds = { "sakai.announcements" };
		return toolIds;
	}
	
	
	/**
	 ** Generate RSS Item element for specified assignment
	 **/
	protected Element generateItemElement( Document doc, AnnouncementMessage msg, Reference msgRef )
	{
			Element item = doc.createElement("item");
			
			Element el = doc.createElement("title");
			el.appendChild(doc.createTextNode( msg.getAnnouncementHeader().getSubject() ));
			item.appendChild(el);
						
			el = doc.createElement("author");
			el.appendChild(doc.createTextNode( msg.getHeader().getFrom().getEmail() ));
			item.appendChild(el);
			
			el = doc.createElement("link");
			el.appendChild(doc.createTextNode( msgRef.getUrl() )); 
			item.appendChild(el);
			
			el = doc.createElement("description");
			el.appendChild(doc.createTextNode( msg.getBody()) );
			item.appendChild(el);
			
			el = doc.createElement("pubDate");
			el.appendChild(doc.createTextNode( msg.getHeader().getDate().toStringLocalFullZ() ));
			item.appendChild(el);
			
			// attachments
			List attachments = msg.getAnnouncementHeader().getAttachments();
			if (attachments.size() > 0)
			{
				for (Iterator iAttachments = attachments.iterator(); iAttachments.hasNext();)
				{
					Reference attachment = (Reference) iAttachments.next();
					el = doc.createElement("enclosure");
					el.setAttribute("url",attachment.getUrl());
					el.setAttribute("type",attachment.getType());
					item.appendChild(el);
				}
			}
			
			return item;
	}
	
	/**
	 ** Print all Announcements as RSS Feed
	 **/
	protected void printAnnouncementRss( OutputStream out, Reference rssRef )
	{
		try
		{
			Site site = m_siteService.getSite(rssRef.getContext());
			Document doc = docBuilder.newDocument();
			
			Element root = doc.createElement("rss");
			root.setAttribute("version","2.0");
			doc.appendChild(root);
			
			Element channel = doc.createElement("channel");
			root.appendChild(channel);
			
			// add title
			Element el = doc.createElement("title");
			el.appendChild(doc.createTextNode("Announcements for "+site.getTitle()));
			channel.appendChild(el);
			
			// add description
			el = doc.createElement("description");
			String desc = (site.getDescription()!=null)?site.getDescription():site.getTitle();
			el.appendChild(doc.createTextNode(desc));
			channel.appendChild(el);
			
			// add link
			el = doc.createElement("link");
			StringBuilder siteUrl = new StringBuilder( m_serverConfigurationService.getServerUrl() );
			siteUrl.append( m_serverConfigurationService.getString("portalPath") );
			siteUrl.append( site.getReference() );
			el.appendChild(doc.createTextNode(siteUrl.toString())); 
			channel.appendChild(el);
			
			// add lastBuildDate
			el = doc.createElement("lastBuildDate");
			String now = DateFormat.getDateInstance(DateFormat.FULL).format( new Date() );
			el.appendChild(doc.createTextNode( now )); 
			channel.appendChild(el);
			
			// add generator
			el = doc.createElement("generator");
			el.appendChild(doc.createTextNode("Sakai Announcements RSS Generator")); 
			channel.appendChild(el);
			
			// get list of public announcements
			AnnouncementChannel anncChan = (AnnouncementChannel)getChannelPublic( channelReference(rssRef.getContext(), SiteService.MAIN_CONTAINER) );
			if ( anncChan == null )
			{
				M_log.warn(this+".printAnnouncementRss invalid request "+rssRef.getContext());
				return;
			}
			List anncList = anncChan.getMessagesPublic(null,false);
			
			for ( Iterator it=anncList.iterator(); it.hasNext(); )
			{
				AnnouncementMessage msg = (AnnouncementMessage)it.next();
				if ( isMessageViewable(msg) )
				{
					Reference msgRef = m_entityManager.newReference( msg.getReference() );
					Element item = generateItemElement( doc, msg, msgRef );
					channel.appendChild(item);
				}
			}
			
			docTransformer.transform( new DOMSource(doc), new StreamResult(out) );
		}
		catch (Exception e)
		{
			M_log.warn(this+"printAnnouncementRss ", e);
		}
	}

	/**
	 ** Print specified Announcement as HTML Page
	 **/
	protected void printAnnouncementHtml( PrintWriter out, Reference ref )
		throws EntityPermissionException, EntityNotDefinedException
	{
		try
		{
			// check security on the message, if not a public message (throws if not permitted)
			if ( ref.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW) == null ||
				  !ref.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW).equals(Boolean.TRUE.toString()) )
			{
				unlock(SECURE_READ, ref.getReference());
			}
			
			AnnouncementMessage msg = (AnnouncementMessage) ref.getEntity();
			AnnouncementMessageHeader hdr = (AnnouncementMessageHeader) msg.getAnnouncementHeader();

			out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
							+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">\n"
							+ "<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
							+ "<style type=\"text/css\">body{margin:0px;padding:1em;font-family:Verdana,Arial,Helvetica,sans-serif;font-size:80%;}</style>\n"
							+ "<title>"
							+ rb.getString("announcement")
							+ ": "
							+ Validator.escapeHtml(hdr.getSubject())
							+ "</title>" + "</head>\n<body>");

			out.println("<h1>" + rb.getString("announcement") + "</h1>");

			// header
			out.println("<table><tr><td><b>" + rb.getString("from") + ":</b></td><td>"
					+ Validator.escapeHtml(hdr.getFrom().getDisplayName()) + "</td></tr>");
			out.println("<tr><td><b>" + rb.getString("date") + ":</b></td><td>" + Validator.escapeHtml(hdr.getDate().toStringLocalFull())
					+ "</td></tr>");
			out.println("<tr><td><b>" + rb.getString("subject") + ":</b></td><td>" + Validator.escapeHtml(hdr.getSubject()) + "</td></tr></table>");

			// body
			out.println("<p>" + Validator.escapeHtmlFormattedText(msg.getBody()) + "</p>");

			// attachments
			List attachments = hdr.getAttachments();
			if (attachments.size() > 0)
			{
				out.println("<p><b>" + rb.getString("attachments") + ":</b></p><p>");
				for (Iterator iAttachments = attachments.iterator(); iAttachments.hasNext();)
				{
					Reference attachment = (Reference) iAttachments.next();
					out.println("<a href=\"" + Validator.escapeHtml(attachment.getUrl()) + "\">"
							+ Validator.escapeHtml(attachment.getUrl()) + "</a><br />");
				}
				out.println("</p>");
			}

			out.println("</body></html>");
		}
		catch (PermissionException e)
		{
			throw new EntityPermissionException(e.getUser(), e.getLock(), e.getResource());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public HttpAccess getHttpAccess()
	{
		return new HttpAccess()
		{
			public void handleAccess(HttpServletRequest req, HttpServletResponse res, Reference ref, Collection copyrightAcceptedRefs) 
				throws EntityPermissionException, EntityNotDefinedException //, EntityAccessOverloadException, EntityCopyrightException
			{
				// we only access the msg & rss reference
				if ( !REF_TYPE_MESSAGE.equals(ref.getSubType()) &&
					  !REF_TYPE_ANNOUNCEMENT_RSS.equals(ref.getSubType()) ) 
						throw new EntityNotDefinedException(ref.getReference());
						
				try
				{
					if ( REF_TYPE_MESSAGE.equals(ref.getSubType()) )
					{
						res.setContentType("text/html; charset=UTF-8");
						printAnnouncementHtml( res.getWriter(), ref );
					}
					else
					{
						res.setContentType("application/xml"); 
						res.setCharacterEncoding("UTF-8");
						printAnnouncementRss( res.getOutputStream(), ref );
					}
				}
				catch ( IOException e )
				{
					throw new EntityNotDefinedException(ref.getReference());
				}
			}
		};
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AnnouncementService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Return a specific announcement channel.
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return the AnnouncementChannel that has the specified name.
	 * @exception IdUnusedException
	 *            If this name is not defined for a announcement channel.
	 * @exception PermissionException
	 *            If the user does not have any permissions to the channel.
	 */
	public AnnouncementChannel getAnnouncementChannel(String ref) throws IdUnusedException, PermissionException
	{
		return (AnnouncementChannel) getChannel(ref);

	} // getAnnouncementChannel

	/**
	 * Add a new announcement channel.
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return The newly created channel.
	 * @exception IdUsedException
	 *            if the id is not unique.
	 * @exception IdInvalidException
	 *            if the id is not made up of valid characters.
	 * @exception PermissionException
	 *            if the user does not have permission to add a channel.
	 */
	public AnnouncementChannelEdit addAnnouncementChannel(String ref) throws IdUsedException, IdInvalidException,
			PermissionException
	{
		return (AnnouncementChannelEdit) addChannel(ref);

	} // addAnnouncementChannel

	/**********************************************************************************************************************************************************************************************************************************************************
	 * ResourceService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public String getLabel()
	{
		return "announcement";
	}

        /**********************************************************************************************************************************************************************************************************************************************************
         * getSummaryFromHeader implementation
         *********************************************************************************************************************************************************************************************************************************************************/
         protected String getSummaryFromHeader(Message item, MessageHeader header)
         {
            String newText;
	    if ( header instanceof AnnouncementMessageHeader) {
		AnnouncementMessageHeader hdr = (AnnouncementMessageHeader) header;
		newText = hdr.getSubject();
	    } else {
       	      newText = item.getBody();
              if ( newText.length() > 50 ) newText = newText.substring(1,49);
            }
            newText = newText + ", " + header.getFrom().getDisplayName() + ", " + header.getDate().toStringLocalFull();
            return newText;
        }

	/**
	 * {@inheritDoc}
	 */
	public void transferCopyEntities(String fromContext, String toContext, List resourceIds)
	{
		// get the channel associated with this site
		String oChannelRef = channelReference(fromContext, SiteService.MAIN_CONTAINER);
		AnnouncementChannel oChannel = null;
		try
		{
			oChannel = (AnnouncementChannel) getChannel(oChannelRef);
			// the "to" message channel
			String nChannelRef = channelReference(toContext, SiteService.MAIN_CONTAINER);
			AnnouncementChannel nChannel = null;
			try
			{
				nChannel = (AnnouncementChannel) getChannel(nChannelRef);
			}
			catch (IdUnusedException e)
			{
				try
				{
					commitChannel(addChannel(nChannelRef));

					try
					{
						nChannel = (AnnouncementChannel) getChannel(nChannelRef);
					}
					catch (Exception eee)
					{
						// ignore
					}
				}
				catch (Exception ee)
				{
					// ignore
				}
			}

			if (nChannel != null)
			{
				// pass the DOM to get new message ids, record the mapping from old to new, and adjust attachments
				List oMessageList = oChannel.getMessages(null, true);
				AnnouncementMessage oMessage = null;
				AnnouncementMessageHeader oMessageHeader = null;
				AnnouncementMessageEdit nMessage = null;
				for (int i = 0; i < oMessageList.size(); i++)
				{
					// the "from" message
					oMessage = (AnnouncementMessage) oMessageList.get(i);
					String oMessageId = oMessage.getId();

					boolean toBeImported = true;
					if (resourceIds != null && resourceIds.size() > 0)
					{
						// if there is a list for import assignments, only import those assignments and relative submissions
						toBeImported = false;
						for (int m = 0; m < resourceIds.size() && !toBeImported; m++)
						{
							if (((String) resourceIds.get(m)).equals(oMessageId))
							{
								toBeImported = true;
							}
						}
					}

					if (toBeImported)
					{
						oMessageHeader = (AnnouncementMessageHeaderEdit) oMessage.getHeader();
						ResourceProperties oProperties = oMessage.getProperties();

						// the "to" message
						nMessage = (AnnouncementMessageEdit) nChannel.addMessage();
						nMessage.setBody(oMessage.getBody());
						// message header
						AnnouncementMessageHeaderEdit nMessageHeader = (AnnouncementMessageHeaderEdit) nMessage.getHeaderEdit();
						nMessageHeader.setDate(oMessageHeader.getDate());
						// when importing, refer to property to determine draft status
						if ("false".equalsIgnoreCase(m_serverConfigurationService.getString("import.importAsDraft")))
						{
							nMessageHeader.setDraft(oMessageHeader.getDraft());
						}
						else
						{
							nMessageHeader.setDraft(true);
						}

						nMessageHeader.setFrom(oMessageHeader.getFrom());
						nMessageHeader.setSubject(oMessageHeader.getSubject());
						// attachment
						List oAttachments = oMessageHeader.getAttachments();
						List nAttachments = m_entityManager.newReferenceList();
						for (int n = 0; n < oAttachments.size(); n++)
						{
							Reference oAttachmentRef = (Reference) oAttachments.get(n);
							String oAttachmentId = ((Reference) oAttachments.get(n)).getId();
							if (oAttachmentId.indexOf(fromContext) != -1)
							{
								// replace old site id with new site id in attachments
								String nAttachmentId = oAttachmentId.replaceAll(fromContext, toContext);
								try
								{
									ContentResource attachment = contentHostingService.getResource(nAttachmentId);
									nAttachments.add(m_entityManager.newReference(attachment.getReference()));
								}
								catch (IdUnusedException e)
								{
									try
									{
										ContentResource oAttachment = contentHostingService.getResource(oAttachmentId);
										try
										{
											if (contentHostingService.isAttachmentResource(nAttachmentId))
											{
												// add the new resource into attachment collection area
												ContentResource attachment = contentHostingService.addAttachmentResource(
														Validator.escapeResourceName(oAttachment.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME)), 
														//ToolManager.getCurrentPlacement().getContext(), 
														toContext, 	//don't use ToolManager.getCurrentPlacement()!
														ToolManager.getTool("sakai.announcements").getTitle(), 
														oAttachment.getContentType(),
														oAttachment.getContent(), 
														oAttachment.getProperties());
												// add to attachment list
												nAttachments.add(m_entityManager.newReference(attachment.getReference()));
											}
											else
											{
												// add the new resource into resource area
												ContentResource attachment = contentHostingService.addResource(
														Validator.escapeResourceName(oAttachment.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME)),
														//ToolManager.getCurrentPlacement().getContext(), 
														toContext, //don't use ToolManager.getCurrentPlacement()!
														1, 
														oAttachment.getContentType(), 
														oAttachment.getContent(), 
														oAttachment.getProperties(), 
														NotificationService.NOTI_NONE);
												// add to attachment list
												nAttachments.add(m_entityManager.newReference(attachment.getReference()));
											}
										}
										catch (Exception eeAny)
										{
											// if the new resource cannot be added
											M_log.warn(" cannot add new attachment with id=" + nAttachmentId);
										}
									}
									catch (Exception eAny)
									{
										// if cannot find the original attachment, do nothing.
										M_log.warn(" cannot find the original attachment with id=" + oAttachmentId);
									}
								}
								catch (Exception any)
								{
									M_log.info(any.getMessage());
								}
							}
							else
							{
								nAttachments.add(oAttachmentRef);
							}
						}
						nMessageHeader.replaceAttachments(nAttachments);
						// properties
						ResourcePropertiesEdit p = nMessage.getPropertiesEdit();
						p.clear();
						p.addAll(oProperties);

						// complete the edit
						nChannel.commitMessage(nMessage, NotificationService.NOTI_NONE);
					}
				}

			} // if
			
			transferSynopticOptions(fromContext, toContext);
		}
		catch (IdUnusedException e)
		{
			M_log.warn(" MessageChannel " + fromContext + " cannot be found. ");
		}
		catch (Exception any)
		{
			M_log.warn(".importResources(): exception in handling " + serviceName() + " : ", any);
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AnnouncementChannel implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseAnnouncementChannelEdit extends BaseMessageChannelEdit implements AnnouncementChannelEdit
	{
		/**
		 * Construct with a reference.
		 * 
		 * @param ref
		 *        The channel reference.
		 */
		public BaseAnnouncementChannelEdit(String ref)
		{
			super(ref);

		} // BaseAnnouncementChannelEdit

		/**
		 * Construct as a copy of another message.
		 * 
		 * @param other
		 *        The other message to copy.
		 */
		public BaseAnnouncementChannelEdit(MessageChannel other)
		{
			super(other);

		} // BaseAnnouncementChannelEdit

		/**
		 * Construct from a channel (and possibly messages) already defined in XML in a DOM tree. The Channel is added to storage.
		 * 
		 * @param el
		 *        The XML DOM element defining the channel.
		 */
		public BaseAnnouncementChannelEdit(Element el)
		{
			super(el);

		} // BaseAnnouncementChannelEdit

		/**
		 * Return a specific announcement channel message, as specified by message name.
		 * 
		 * @param messageId
		 *        The id of the message to get.
		 * @return the AnnouncementMessage that has the specified id.
		 * @exception IdUnusedException
		 *            If this name is not a defined message in this announcement channel.
		 * @exception PermissionException
		 *            If the user does not have any permissions to read the message.
		 */
		public AnnouncementMessage getAnnouncementMessage(String messageId) throws IdUnusedException, PermissionException
		{
			AnnouncementMessage msg = (AnnouncementMessage) getMessage(messageId);

			// filter out drafts not by this user (unless this user is a super user or has access_draft ability)
			if ((msg.getAnnouncementHeader()).getDraft() && (!SecurityService.isSuperUser())
					&& (!msg.getHeader().getFrom().getId().equals(SessionManager.getCurrentSessionUserId()))
					&& (!unlockCheck(SECURE_READ_DRAFT, msg.getReference())))
			{
				throw new PermissionException(SessionManager.getCurrentSessionUserId(), SECURE_READ, msg.getReference());
			}

			return msg;

		} // getAnnouncementMessage

		/**
		 * Return a list of all or filtered messages in the channel. The order in which the messages will be found in the iteration is by date, oldest first if ascending is true, newest first if ascending is false.
		 * 
		 * @param filter
		 *        A filtering object to accept messages, or null if no filtering is desired.
		 * @param ascending
		 *        Order of messages, ascending if true, descending if false
		 * @return a list on channel Message objects or specializations of Message objects (may be empty).
		 * @exception PermissionException
		 *            if the user does not have read permission to the channel.
		 */
		public List getMessages(Filter filter, boolean ascending) throws PermissionException
		{
			// filter out drafts this user cannot see
			filter = new PrivacyFilter(filter);

			return super.getMessages(filter, ascending);

		} // getMessages
		
		/**
		 * A (AnnouncementMessageEdit) cover for editMessage. Return a specific channel message, as specified by message name, locked for update. Must commitEdit() to make official, or cancelEdit() when done!
		 * 
		 * @param messageId
		 *        The id of the message to get.
		 * @return the Message that has the specified id.
		 * @exception IdUnusedException
		 *            If this name is not a defined message in this channel.
		 * @exception PermissionException
		 *            If the user does not have any permissions to read the message.
		 * @exception InUseException
		 *            if the current user does not have permission to mess with this user.
		 */
		public AnnouncementMessageEdit editAnnouncementMessage(String messageId) throws IdUnusedException, PermissionException,
				InUseException
		{
			return (AnnouncementMessageEdit) editMessage(messageId);

		} // editAnnouncementMessage
		
		
	/**
	 * A cover for removeMessage. Deletes the messages specified by the message id.
	 * 
	 * @param messageId
	 *        The id of the message to get.
	 * @exception PermissionException
	 *            If the user does not have any permissions to delete the message.
	 */
		public void removeAnnouncementMessage(String messageId) throws PermissionException
				
		{
			removeMessage(messageId);
			//return (AnnouncementMessageEdit) removeMessage(messageId);

		} // editAnnouncementMessage
		

		/**
		 * A (AnnouncementMessageEdit) cover for addMessage. Add a new message to this channel. Must commitEdit() to make official, or cancelEdit() when done!
		 * 
		 * @return The newly added message, locked for update.
		 * @exception PermissionException
		 *            If the user does not have write permission to the channel.
		 */
		public AnnouncementMessageEdit addAnnouncementMessage() throws PermissionException
		{
			return (AnnouncementMessageEdit) addMessage();

		} // addAnnouncementMessage

		/**
		 * a (AnnouncementMessage) cover for addMessage to add a new message to this channel.
		 * 
		 * @param subject
		 *        The message header subject.
		 * @param draft
		 *        The message header draft indication.
		 * @param attachments
		 *        The message header attachments, a vector of Reference objects.
		 * @param body
		 *        The message body.
		 * @return The newly added message.
		 * @exception PermissionException
		 *            If the user does not have write permission to the channel.
		 */
		public AnnouncementMessage addAnnouncementMessage(String subject, boolean draft, List attachments, String body)
				throws PermissionException
		{
			AnnouncementMessageEdit edit = (AnnouncementMessageEdit) addMessage();
			AnnouncementMessageHeaderEdit header = edit.getAnnouncementHeaderEdit();
			edit.setBody(body);
			header.replaceAttachments(attachments);
			header.setSubject(subject);
			header.setDraft(draft);

			commitMessage(edit);

			return edit;

		} // addAnnouncementMessage

	} // class BaseAnnouncementChannelEdit

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AnnouncementMessage implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseAnnouncementMessageEdit extends BaseMessageEdit implements AnnouncementMessageEdit
	{
		/**
		 * Construct.
		 * 
		 * @param channel
		 *        The channel in which this message lives.
		 * @param id
		 *        The message id.
		 */
		public BaseAnnouncementMessageEdit(MessageChannel channel, String id)
		{
			super(channel, id);

		} // BaseAnnouncementMessageEdit

		/**
		 * Construct as a copy of another message.
		 * 
		 * @param other
		 *        The other message to copy.
		 */
		public BaseAnnouncementMessageEdit(MessageChannel channel, Message other)
		{
			super(channel, other);

		} // BaseAnnouncementMessageEdit

		/**
		 * Construct from an existing definition, in xml.
		 * 
		 * @param channel
		 *        The channel in which this message lives.
		 * @param el
		 *        The message in XML in a DOM element.
		 */
		public BaseAnnouncementMessageEdit(MessageChannel channel, Element el)
		{
			super(channel, el);

		} // BaseAnnouncementMessageEdit

		/**
		 * Access the announcement message header.
		 * 
		 * @return The announcement message header.
		 */
		public AnnouncementMessageHeader getAnnouncementHeader()
		{
			return (AnnouncementMessageHeader) getHeader();

		} // getAnnouncementHeader

		/**
		 * Access the announcement message header.
		 * 
		 * @return The announcement message header.
		 */
		public AnnouncementMessageHeaderEdit getAnnouncementHeaderEdit()
		{
			return (AnnouncementMessageHeaderEdit) getHeader();

		} // getAnnouncementHeaderEdit

	} // class BasicAnnouncementMessageEdit

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AnnouncementMessageHeaderEdit implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseAnnouncementMessageHeaderEdit extends BaseMessageHeaderEdit implements AnnouncementMessageHeaderEdit
	{
		/** The subject for the announcement. */
		protected String m_subject = null;

		/**
		 * Construct.
		 * 
		 * @param id
		 *        The unique (within the channel) message id.
		 * @param from
		 *        The User who sent the message to the channel.
		 * @param attachments
		 *        The message header attachments, a vector of Reference objects.
		 */
		public BaseAnnouncementMessageHeaderEdit(Message msg, String id)
		{
			super(msg, id);

		} // BaseAnnouncementMessageHeaderEdit

		/**
		 * Construct, from an already existing XML DOM element.
		 * 
		 * @param el
		 *        The header in XML in a DOM element.
		 */
		public BaseAnnouncementMessageHeaderEdit(Message msg, Element el)
		{
			super(msg, el);

			// extract the subject
			m_subject = el.getAttribute("subject");

		} // BaseAnnouncementMessageHeaderEdit

		/**
		 * Construct as a copy of another header.
		 * 
		 * @param other
		 *        The other message header to copy.
		 */
		public BaseAnnouncementMessageHeaderEdit(Message msg, MessageHeader other)
		{
			super(msg, other);

			m_subject = ((AnnouncementMessageHeader) other).getSubject();

		} // BaseAnnouncementMessageHeaderEdit

		/**
		 * Access the subject of the announcement.
		 * 
		 * @return The subject of the announcement.
		 */
		public String getSubject()
		{
			return ((m_subject == null) ? "" : m_subject);

		} // getSubject

		/**
		 * Set the subject of the announcement.
		 * 
		 * @param subject
		 *        The subject of the announcement.
		 */
		public void setSubject(String subject)
		{
			if (StringUtil.different(subject, m_subject))
			{
				m_subject = subject;
			}

		} // setSubject

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
			// get the basic work done
			Element header = super.toXml(doc, stack);

			// add draft, subject
			header.setAttribute("subject", getSubject());
			header.setAttribute("draft", new Boolean(getDraft()).toString());

			return header;

		} // toXml

	} // BaseAnnouncementMessageHeader

	/**
	 * A filter that will reject announcement message drafts not from the current user, and otherwise use another filter, if defined, for acceptance.
	 */
	protected class PrivacyFilter implements Filter
	{
		/** The other filter to check with. May be null. */
		protected Filter m_filter = null;

		/**
		 * Construct
		 * 
		 * @param filter
		 *        The other filter we check with.
		 */
		public PrivacyFilter(Filter filter)
		{
			m_filter = filter;

		} // PrivacyFilter

		/**
		 * Does this object satisfy the criteria of the filter?
		 * 
		 * @return true if the object is accepted by the filter, false if not.
		 */
		public boolean accept(Object o)
		{
			// first if o is a announcement message that's a draft from another user, reject it
			if (o instanceof AnnouncementMessage)
			{
				AnnouncementMessage msg = (AnnouncementMessage) o;

				if ((msg.getAnnouncementHeader()).getDraft() && (!SecurityService.isSuperUser())
						&& (!msg.getHeader().getFrom().getId().equals(SessionManager.getCurrentSessionUserId()))
						&& (!unlockCheck(SECURE_READ_DRAFT, msg.getReference())))
				{
					return false;
				}
			}

			// now, use the real filter, if present
			if (m_filter != null) return m_filter.accept(o);

			return true;

		} // accept

	} // PrivacyFilter

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntitySummary#summarizableToolIds()
	 */
	public String[] summarizableToolIds()
	{
		return new String[] {
				"sakai.announcements",
				"sakai.motd"
		};
	}
	public String getSummarizableReference(String siteId, String toolIdentifier)
	{
		if ( "sakai.motd".equals(toolIdentifier) ) {
			return "/announcement/channel/!site/motd";
		} else {
			return super.getSummarizableReference(siteId, toolIdentifier);
		}
	}

	public void transferCopyEntities(String fromContext, String toContext, List ids, boolean cleanup)
	{
		try
		{
			if(cleanup == true)
			{
				String channelId = ServerConfigurationService.getString("channel", null);
				
				String toSiteId = toContext;
				
				if (channelId == null)
				{
					channelId = channelReference(toSiteId, SiteService.MAIN_CONTAINER);
					try
					{
						AnnouncementChannel aChannel = getAnnouncementChannel(channelId);
						
						List mList = aChannel.getMessages(null, true);
						
						for(Iterator iter = mList.iterator(); iter.hasNext();)
						{
							AnnouncementMessage msg = (AnnouncementMessage) iter.next();
							
							aChannel.removeMessage(msg.getId());
						}
					}
					catch(Exception e)
					{
						M_log.debug("Unable to remove Announcements " + e);
					}
				}
			}
		}
		catch (Exception e)
		{
			M_log.debug("transferCopyEntities: End removing Announcement data");
		}
		transferCopyEntities(fromContext, toContext, ids);
	
	} 

}
