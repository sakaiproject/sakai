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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.announcement.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Vector;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementChannelEdit;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageEdit;
import org.sakaiproject.announcement.api.AnnouncementMessageHeader;
import org.sakaiproject.announcement.api.AnnouncementMessageHeaderEdit;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ContextObserver;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityNotDefinedException;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.EntityTransferrerRefMigrator;
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
import org.sakaiproject.message.api.MessageEdit;
import org.sakaiproject.message.api.MessageHeader;
import org.sakaiproject.message.api.MessageHeaderEdit;
import org.sakaiproject.message.util.BaseMessage;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.MergedList;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * BaseAnnouncementService extends the BaseMessage for the specifics of Announcement.
 * </p>
 */
@Slf4j
public abstract class BaseAnnouncementService extends BaseMessage implements AnnouncementService, ContextObserver,
		EntityTransferrer, EntityTransferrerRefMigrator
{
	/** private constants definitions */
	private final static String SAKAI_ANNOUNCEMENT_TOOL_ID = "sakai.announcements";
	private static final String PORTLET_CONFIG_PARM_MERGED_CHANNELS = "mergedAnnouncementChannels";

	
	/** Messages, for the http access. */
	protected static ResourceLoader rb = new ResourceLoader("annc-access");
	
	// XML DocumentBuilder and Transformer for RSS Feed
	private DocumentBuilder docBuilder = null;
	private Transformer docTransformer = null;
	
	private ContentHostingService contentHostingService;
	private SiteEmailNotificationAnnc siteEmailNotificationAnnc;

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

	public void setSiteEmailNotificationAnnc(SiteEmailNotificationAnnc siteEmailNotificationAnnc) {
		this.siteEmailNotificationAnnc = siteEmailNotificationAnnc;
	}

	private FunctionManager functionManager;
	public void setFunctionManager(FunctionManager functionManager) {
		this.functionManager = functionManager;
	}
	
	private AliasService aliasService;	
	public void setAliasService(AliasService aliasService) {
		this.aliasService = aliasService;
	}

	private ToolManager toolManager;
	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
		
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
			edit.setAction(siteEmailNotificationAnnc);

			// register functions
			functionManager.registerFunction(eventId(SECURE_READ));
			functionManager.registerFunction(eventId(SECURE_ADD));
			functionManager.registerFunction(eventId(SECURE_REMOVE_ANY));
			functionManager.registerFunction(eventId(SECURE_REMOVE_OWN));
			functionManager.registerFunction(eventId(SECURE_UPDATE_ANY));
			functionManager.registerFunction(eventId(SECURE_UPDATE_OWN));
			functionManager.registerFunction(eventId(SECURE_ALL_GROUPS));

			// Sakai v2.4: UI end says hidden, 'under the covers' says draft
			// Done so import from old sites causes drafts to 'become' hidden in new sites
			functionManager.registerFunction(eventId(SECURE_READ_DRAFT));

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
			
			log.info("init()");
		}
		catch (Throwable t)
		{
			log.warn("init(): ", t);
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
		Object[] rv = new Object[5];
		rv[0] = ((Message) r).getHeader().getDate();
		rv[1] = ((Message) r).getHeader().getFrom().getId();
		rv[2] = ((AnnouncementMessage) r).getAnnouncementHeader().getDraft() ? "1" : "0";
		rv[3] = r.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW) == null ? "0" : "1";
		rv[4] = ((Message) r).getHeader().getMessage_order();
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
	
	/**
	 * Access the resource Message Order.
	 * 
	 * @param r
	 *        The resource.
	 * @return The resource order.
	 */
	public Integer getMessage_order(Entity r)
	{
		return ((Message) r).getHeader().getMessage_order();
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
	 * @param secure The security function string.
	 *     NOTE: if this input is null or blank then event id will default to "annc.INVALID_KEY",
	 *           this is only because throwing an exception here would be very disruptive and returning
	 *           a null or blank for the event id would cause other failures (SAK-23804)
	 * @return The event tracking string.
	 */
	protected String eventId(String secure) {
		// https://jira.sakaiproject.org/browse/SAK-23804
		if (StringUtils.isBlank(secure)) {
		    try {
		        throw new IllegalArgumentException("anouncement eventId() input cannot be null or blank");
		    } catch (Exception e) {
		        secure = "INVALID_KEY";
		        log.error("Bad call to BaseAnnouncementService.eventId(String) - input string is blank, generating '{}' event name and logging trace", secure, e);
		    }
		}
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
					log.warn("parse(): unknown message subtype: {} in ref: {}", subType, reference);
			}

			// Translate context alias into site id (only for rss) if necessary
			if (REF_TYPE_ANNOUNCEMENT_RSS.equals(subType) &&(context != null) && (context.length() > 0))
			{
				if (!m_siteService.siteExists(context))
				{
					try
					{
						String aliasTarget = aliasService.getTarget(context);
						if (aliasTarget.startsWith(REFERENCE_ROOT)) // only support announcement aliases
						{
							parts = StringUtil.split(aliasTarget, Entity.SEPARATOR);
							if (parts.length > 3)
								context = parts[3];
						}
					}
					catch (Exception e)
					{
						log.debug(this+".parseEntityReference(): {}", e.toString());
						return false;
					}
				}

				// if context still isn't valid, then no valid alias or site was specified
				if (!m_siteService.siteExists(context))
				{
					log.warn(this+".parseEntityReference() no valid site or alias: {}", context);
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
	@Override
	public String channelReference(String context, String id)
	{
	    /* SAK-19516: for MOTD and Admin Workspace Announcements, the channel reference 
	     * is not calculated based on the context and id, but pulled from SAKAI_TOOL_PROPERTY 'channel'.
	     */
		String channelRef = null;
		try {
			ToolConfiguration tool = m_siteService.getSite(context).getToolForCommonId(SAKAI_ANNOUNCEMENT_TOOL_ID);
			if (tool != null) {
				channelRef = tool.getConfig().getProperty(ANNOUNCEMENT_CHANNEL_PROPERTY, null);
			}
		} catch (IdUnusedException e) {
		    // ignore the error, continue with the default method
		    log.debug("Could not find channelRef in channel property, falling back to default method...");
		}
		
		if (channelRef == null || channelRef.trim().length() == 0) {
			channelRef = super.channelReference(context, id);
		}
		return channelRef;

	} // channelReference

	/**
	 * @inheritDoc
	 */
	public String getRssUrl(Reference ref) 
	{
      String alias = null;
      List aliasList =  aliasService.getAliases( ref.getReference() );
		
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

		final Time now = m_timeService.newTime();
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
			Date date = new Date(msg.getHeader().getDate().getTime());
			String pubDate = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.ENGLISH).format(date);
			el.appendChild(doc.createTextNode(pubDate));
			item.appendChild(el);
			
			el = doc.createElement("message_order");
			el.appendChild(doc.createTextNode( msg.getHeader().getMessage_order().toString()));
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
			String now = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.ENGLISH).format(new Date());
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
				log.warn(this+".printAnnouncementRss invalid request {}", rssRef.getContext());
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
			log.warn(this+"printAnnouncementRss ", e);
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
			out.println("<table><tr><td><b>" + rb.getString("from_colon") + "</b></td><td>"
					+ Validator.escapeHtml(hdr.getFrom().getDisplayName()) + "</td></tr>");
			out.println("<tr><td><b>" + rb.getString("date_colon") + "</b></td><td>" + Validator.escapeHtml(hdr.getDate().toStringLocalFull())
					+ "</td></tr>");
			out.println("<tr><td><b>" + rb.getString("subject_colon") + "</b></td><td>" + Validator.escapeHtml(hdr.getSubject()) + "</td></tr></table>");

			// body
			out.println("<p>" + Validator.escapeHtmlFormattedText(msg.getBody()) + "</p>");

			// attachments
			List attachments = hdr.getAttachments();
			if (attachments.size() > 0)
			{
				out.println("<p><b>" + rb.getString("attachments_colon") + "</b></p><p>");
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
	
	/**
	 * Return a list of messages from the provided channel (merged flag returns merged messages)
	 * @param channelReference
	 *        Channel's reference String
	 * @param filter
	 *        A filtering object to accept messages, or null if no filtering is desired.
	 * @param order
	 *        Order of messages, ascending if true, descending if false
	 * @param merged
	 * 		  flag to include merged channel messages, true returns ALL messages including merged sites/channels
	 * @return a list of Message objects or specializations of Message objects (may be empty).
	 * @exception IdUnusedException
	 *            If this name is not defined for a announcement channel.
	 * @exception PermissionException
	 *            if the user does not have read permission to the channel.
	 * @exception NullPointerException
	 */
	public List getMessages(String channelReference,Filter filter, boolean order, boolean merged) throws IdUnusedException, PermissionException, NullPointerException
	{
		List<Message> messageList = new Vector();	
		filter = new PrivacyFilter(filter);  		// filter out drafts this user cannot see
		Site site = null;
		String initMergeList = null;
	
		try{
			site = m_siteService.getSite(getAnnouncementChannel(channelReference).getContext());

			ToolConfiguration tc=site.getToolForCommonId(SAKAI_ANNOUNCEMENT_TOOL_ID);
			if (tc!=null){
				initMergeList = tc.getPlacementConfig().getProperty(PORTLET_CONFIG_PARM_MERGED_CHANNELS);	
			}
			
			MergedList mergedAnnouncementList = new MergedList();
			String[] channelArrayFromConfigParameterValue = null;	
			
			//get array of associated channels: similar logic as found in AnnouncementAction.getMessages() for viewing
			channelArrayFromConfigParameterValue = mergedAnnouncementList.getChannelReferenceArrayFromDelimitedString(channelReference, initMergeList);
			
			//get messages for each channel
			for(int i=0; i<channelArrayFromConfigParameterValue.length;i++)
			{
				MessageChannel siteChannel = getAnnouncementChannel(channelArrayFromConfigParameterValue[i]);
				if (siteChannel != null)
				{
					if (allowGetChannel(siteChannel.getReference()))
					{
						//merged flag = true then add all channel's messages
						//merged flag = false only add the calling channel's messages
						if(merged || siteChannel.getContext().equals(site.getId()))
								messageList.addAll(siteChannel.getMessages(filter,order));
					}
				}
			}
			
			//sort messages
			Collections.sort(messageList);
			if (!order)
			{
				Collections.reverse(messageList);
			}			
		} catch (IdUnusedException e) {
			log.warn(e.getMessage());
		}
		catch (PermissionException e) {
			log.warn(e.getMessage());
		}
		catch (NullPointerException e) {
			log.warn(e.getMessage());
		}
		return messageList;

	} // getMessages
	
	
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
		transferCopyEntitiesRefMigrator(fromContext, toContext, resourceIds);
	}

	public Map<String, String> transferCopyEntitiesRefMigrator(String fromContext, String toContext, List resourceIds)
	{
	//	Map<String, String> transversalMap = new HashMap<String, String>();
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
						toBeImported = false;
						for (int m = 0; m < resourceIds.size() && !toBeImported; m++)
						{
							if (((String) resourceIds.get(m)).equals(oMessageId))
							{
								toBeImported = true;
							}
						}
					}
					
					// not to import any assignment-generated announcement
					String assignmentReference = StringUtils.trimToNull(oMessage.getProperties().getProperty(AnnouncementService.ASSIGNMENT_REFERENCE));
					if (toBeImported && assignmentReference != null)
					{
						toBeImported = false;
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
						nMessageHeader.setMessage_order(oMessageHeader.getMessage_order());
						// when importing, refer to property to determine draft status
						if (!m_serverConfigurationService.getBoolean("import.importAsDraft", true))
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
														//toolManager.getCurrentPlacement().getContext(), 
														toContext,    //don't use toolManager.getCurrentPlacement()!
														toolManager.getTool("sakai.announcements").getTitle(),
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
														//toolManager.getCurrentPlacement().getContext(), 
														toContext, //don't use toolManager.getCurrentPlacement()!
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
											log.warn(" cannot add new attachment with id={}", nAttachmentId);
										}
									}
									catch (Exception eAny)
									{
										// if cannot find the original attachment, do nothing.
										log.warn(" cannot find the original attachment with id={}", oAttachmentId);
									}
								}
								catch (Exception any)
								{
									log.info(any.getMessage());
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
						nChannel.commitMessage(nMessage, NotificationService.NOTI_IGNORE);
						
//						transversalMap.put(oMessage.getReference(), nMessage.getReference());
					}
				}

			} // if
			
			transferSynopticOptions(fromContext, toContext);
		}
		catch (IdUnusedException e)
		{
			log.warn(" MessageChannel {} cannot be found. ", fromContext);
		}
		catch (Exception any)
		{
			log.warn(".importResources(): exception in handling {} : {}", serviceName(), any);
		}
		
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void updateEntityReferences(String toContext, Map<String, String> transversalMap){
		if(transversalMap != null && transversalMap.size() > 0){
			try
			{
				Set<Entry<String, String>> entrySet = (Set<Entry<String, String>>) transversalMap.entrySet();
				
				String channelId = m_serverConfigurationService.getString(ANNOUNCEMENT_CHANNEL_PROPERTY, null);

				String toSiteId = toContext;

				if (channelId == null)
				{
					channelId = channelReference(toSiteId, SiteService.MAIN_CONTAINER);
					try
					{
						AnnouncementChannel aChannel = getAnnouncementChannel(channelId);
						//need to clear the cache to grab the newly saved messages
						m_threadLocalManager.set(aChannel.getReference() + ".msgs", null);
						List mList = aChannel.getMessages(null, true);

						for(Iterator iter = mList.iterator(); iter.hasNext();)
						{
							AnnouncementMessage msg = (AnnouncementMessage) iter.next();
							String msgBody = msg.getBody();
							boolean updated = false;
							Iterator<Entry<String, String>> entryItr = entrySet.iterator();
							while(entryItr.hasNext()) {
								Entry<String, String> entry = (Entry<String, String>) entryItr.next();
								String fromContextRef = entry.getKey();
								if(msgBody.contains(fromContextRef)){									
									msgBody = msgBody.replace(fromContextRef, entry.getValue());
									updated = true;
								}								
							}	
							if(updated){
								AnnouncementMessageEdit editMsg = aChannel.editAnnouncementMessage(msg.getId());
								editMsg.setBody(msgBody);
								aChannel.commitMessage(editMsg, NotificationService.NOTI_IGNORE);
							}
						}
					}
					catch(Exception e)
					{
						log.debug("Unable to remove Announcements ", e.getMessage(), e);
					}
				}

			}
			catch (Exception e)
			{
				log.debug("transferCopyEntities: End removing Announcement data");
			}
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AnnouncementChannel implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseAnnouncementChannelEdit extends BaseMessageChannelEdit<AnnouncementMessageEdit> implements AnnouncementChannelEdit
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
			if ((msg.getAnnouncementHeader()).getDraft() && (!m_securityService.isSuperUser())
					&& (!msg.getHeader().getFrom().getId().equals(m_sessionManager.getCurrentSessionUserId()))
					&& (!unlockCheck(SECURE_READ_DRAFT, msg.getReference())))
			{
				throw new PermissionException(m_sessionManager.getCurrentSessionUserId(), SECURE_READ, msg.getReference());
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
		
		//Intercept this commit to update the order and set unreleased to the top
		public void commitMessage(MessageEdit edit, int priority, String invokee) {
			int currentMax = setMessageOrderMax(edit);
			setMessageUnreleasedMax(currentMax);
			super.commitMessage(edit, priority, invokee);
		}
		
		/**
		 * used to set unreleased messages higher than other messages
		 * 
		 * @param currentMax
		 *        The value of the current max
		 */
		private void setMessageUnreleasedMax(int currentMax) {
			boolean releaseDateFirst = m_serverConfigurationService.getBoolean("sakai.announcement.release_date_first", true);
			//Don't run this if the property is not set
			if (releaseDateFirst == false) {
				return;
			}
			try {
				//Get all messages in this channel
				List<MessageEdit> msglist = (List<MessageEdit>) this.getMessages(null, false);

				//Go through all the messages and move all the ones that aren't yet released higher in the order. Just ignore any errors
				for (MessageEdit me:msglist) {
					Date releaseDate = null;
					try {
						releaseDate = me.getProperties().getDateProperty(AnnouncementService.RELEASE_DATE);
					} catch (EntityPropertyNotDefinedException e) {
						if (log.isDebugEnabled()) {
							log.debug("Exception moving an unreleased item.",e);
						}
						continue;
					} catch (EntityPropertyTypeException e) {
						if (log.isDebugEnabled()) {
							log.debug("Exception moving an unreleased item.",e);
						}
						continue;
					}
					//releaseDate of this item is after current date, so set it later than max
					if (releaseDate.compareTo(new Date()) > 0) {
						if (log.isDebugEnabled()) {
							log.debug("Placing unreleased announcement to top of list {}", me.getId());
						}
						//Try to set the current max of these other messages
						try {
							AnnouncementMessageEdit em = editAnnouncementMessage(me.getId());
							em.getHeaderEdit().setMessage_order(++currentMax);
							super.commitMessage(em, NotificationService.NOTI_IGNORE, "");
						} catch (InUseException e) {
							if (log.isDebugEnabled()) {
								log.debug("Exception moving an unreleased item.",e);
							}
							continue;
						}
						catch (IdUnusedException e) {
							if (log.isDebugEnabled()) {
								log.debug("Exception moving an unreleased item.",e);
							}
							continue;
						}
						//Commit this update directly
					}
				}
			} catch (PermissionException ex) {
				log.error(ex.getMessage());
			}
		}
		
		/**
		 * Go through all of the messages to find the max, put this message at the Maximum + 1
		 * 
		 * @param msg
		 *        The message to edit
		 * @return The currentMax value determined (To save on future execution)
		 */
		private int setMessageOrderMax(MessageEdit msg) {
			int currentMax = 0;
			try {
				List<MessageEdit> msglist = (List<MessageEdit>) this.getMessages(null, false);
				for (MessageEdit me:msglist) {
					if (me.getHeaderEdit().getMessage_order()>currentMax)
						currentMax = me.getHeaderEdit().getMessage_order();
				}
				msg.getHeaderEdit().setMessage_order(++currentMax);
			} catch (PermissionException ex) {
				log.error(ex.getMessage());
			}
			return currentMax;
		}

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

				if ((msg.getAnnouncementHeader()).getDraft() && (!m_securityService.isSuperUser())
						&& (!msg.getHeader().getFrom().getId().equals(m_sessionManager.getCurrentSessionUserId()))
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
		transferCopyEntitiesRefMigrator(fromContext, toContext, ids, cleanup);
	}

	public Map<String, String> transferCopyEntitiesRefMigrator(String fromContext, String toContext, List ids, boolean cleanup)
	{
//		Map<String, String> transversalMap = new HashMap<String, String>();
		try
		{
			if(cleanup == true)
			{
				String channelId = m_serverConfigurationService.getString(ANNOUNCEMENT_CHANNEL_PROPERTY, null);
				
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
						log.debug("Unable to remove Announcements {}", e.getMessage(), e);
					}
				}
			}
		}
		catch (Exception e)
		{
			log.debug("transferCopyEntities: End removing Announcement data");
		}
		transferCopyEntitiesRefMigrator(fromContext, toContext, ids);
		return null;
	} 

	public void clearMessagesCache(String channelRef){
		m_threadLocalManager.set(channelRef + ".msgs", null);
	}
}
