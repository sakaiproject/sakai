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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Stack;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import lombok.Setter;
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
import org.sakaiproject.announcement.api.ViewableFilter;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ContentExistsAware;
import org.sakaiproject.entity.api.ContextObserver;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityNotDefinedException;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
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
import org.sakaiproject.message.api.MessageChannelEdit;
import org.sakaiproject.message.api.MessageEdit;
import org.sakaiproject.message.api.MessageHeader;
import org.sakaiproject.message.api.MessageHeaderEdit;
import org.sakaiproject.message.util.BaseMessage;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteRemovalAdvisor;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.api.LinkMigrationHelper;
import org.sakaiproject.util.MergedList;
import org.sakaiproject.util.MergedListEntryProviderBase;
import org.sakaiproject.util.MergedListEntryProviderFixedListWrapper;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * <p>
 * BaseAnnouncementService extends the BaseMessage for the specifics of Announcement.
 * </p>
 */
@Slf4j
public abstract class BaseAnnouncementService extends BaseMessage implements AnnouncementService, ContextObserver,
		EntityTransferrer, ContentExistsAware, SiteRemovalAdvisor
{
	
	/** Messages, for the http access. */
	protected static ResourceLoader rb = new ResourceLoader("annc-access");
	
	// XML DocumentBuilder and Transformer for RSS Feed
	private DocumentBuilder docBuilder = null;
	private Transformer docTransformer = null;
	
	@Setter private SiteEmailNotificationAnnc siteEmailNotificationAnnc;
	@Setter private FunctionManager functionManager;
	@Setter private AliasService aliasService;
	@Setter private ToolManager toolManager;
	@Setter private PreferencesService preferencesService;
	@Resource(name="org.sakaiproject.util.api.LinkMigrationHelper")
	private LinkMigrationHelper linkMigrationHelper;


	/**********************************************************************************************************************************************************************************************************************************************************
	 * Constructors, Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/


	/** Dependency: NotificationService. */
	@Setter protected NotificationService notificationService = null;

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
			NotificationEdit edit = notificationService.addTransientNotification();

			// set functions
			edit.setFunction(eventId(SECURE_ADD));
			edit.addFunction(eventId(SECURE_UPDATE_OWN));
			edit.addFunction(eventId(SECURE_UPDATE_ANY));

			// set the filter to any announcement resource (see messageReference())
			edit.setResourceFilter(getAccessPoint(true) + Entity.SEPARATOR + REF_TYPE_MESSAGE);

			// set the action
			edit.setAction(siteEmailNotificationAnnc);

			// register functions
			functionManager.registerFunction(eventId(SECURE_READ), true);
			functionManager.registerFunction(eventId(SECURE_ADD), true);
			functionManager.registerFunction(eventId(SECURE_REMOVE_ANY), true);
			functionManager.registerFunction(eventId(SECURE_REMOVE_OWN), true);
			functionManager.registerFunction(eventId(SECURE_UPDATE_ANY), true);
			functionManager.registerFunction(eventId(SECURE_UPDATE_OWN), true);
			functionManager.registerFunction(eventId(SECURE_ALL_GROUPS), true);

			// Sakai v2.4: UI end says hidden, 'under the covers' says draft
			// Done so import from old sites causes drafts to 'become' hidden in new sites
			functionManager.registerFunction(eventId(SECURE_READ_DRAFT), true);

			// entity producer registration
			entityManager.registerEntityProducer(this, REFERENCE_ROOT);

			// create DocumentBuilder for RSS Feed
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(true);  // ignore comments
			factory.setNamespaceAware(true);		// namespace aware should be true
			factory.setValidating(false);		   // we're not validating
			docBuilder = factory.newDocumentBuilder();
			
			TransformerFactory tFactory = TransformerFactory.newInstance();
			docTransformer = tFactory.newTransformer();

			siteService.addSiteRemovalAdvisor(this);

			log.info("init()");
		}
		catch (Throwable t)
		{
			log.warn("init(): ", t);
		}

	} // init

	/**
	 * Destroy
	 */
	public void destroy()
	{
		siteService.removeSiteRemovalAdvisor(this);
		super.destroy();
		log.info("destroy()");
	}

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
				if (!siteService.siteExists(context))
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
				if (!siteService.siteExists(context))
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
		
		return  entityManager.newReference( refString.toString() );
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
			ToolConfiguration tool = siteService.getSite(context).getToolForCommonId(SAKAI_ANNOUNCEMENT_TOOL_ID);
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
		rssUrlString.append( serverConfigurationService.getAccessUrl() );
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

	@Override
	public Optional<List<String>> getTransferOptions() {
		return Optional.of(Arrays.asList(new String[] { EntityTransferrer.COPY_PERMISSIONS_OPTION }));
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
			Site site = siteService.getSite(rssRef.getContext());
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
			StringBuilder siteUrl = new StringBuilder( serverConfigurationService.getServerUrl() );
			siteUrl.append( serverConfigurationService.getString("portalPath") );
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
					Reference msgRef = entityManager.newReference( msg.getReference() );
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
							+ formattedText.escapeHtml(hdr.getSubject())
							+ "</title>" + "</head>\n<body>");

			out.println("<h1>" + rb.getString("announcement") + "</h1>");

			// header
			out.println("<table><tr><td><b>" + rb.getString("from_colon") + "</b></td><td>"
					+ formattedText.escapeHtml(hdr.getFrom().getDisplayName()) + "</td></tr>");
			out.println("<tr><td><b>" + rb.getString("date_colon") + "</b></td><td>" + formattedText.escapeHtml(hdr.getDate().toStringLocalFull())
					+ "</td></tr>");
			out.println("<tr><td><b>" + rb.getString("subject_colon") + "</b></td><td>" + formattedText.escapeHtml(hdr.getSubject()) + "</td></tr></table>");

			// body
			out.println("<p>" + formattedText.escapeHtmlFormattedText(msg.getBody()) + "</p>");

			// attachments
			List attachments = hdr.getAttachments();
			if (attachments.size() > 0)
			{
				out.println("<p><b>" + rb.getString("attachments_colon") + "</b></p><p>");
				for (Iterator iAttachments = attachments.iterator(); iAttachments.hasNext();)
				{
					Reference attachment = (Reference) iAttachments.next();
					out.println("<a href=\"" + formattedText.escapeHtml(attachment.getUrl()) + "\">"
							+ formattedText.escapeHtml(attachment.getUrl()) + "</a><br />");
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
	 * @param ascending
	 *        Order of messages, ascending if true, descending if false
	 * @param merged
	 * 		  flag to include merged channel messages, true returns ALL messages including merged sites/channels
	 * @return a list of Message objects or specializations of Message objects (may be empty).
	 * @exception IdUnusedException
	 *            If this name is not defined for a announcement channel, or the channel references a site that does not exist.
	 * @exception PermissionException
	 *            if the user does not have read permission to the channel.
	 * @exception NullPointerException
	 */
	public List<AnnouncementMessage> getMessages(String channelReference, Filter filter, boolean ascending, boolean merged) throws IdUnusedException, PermissionException, NullPointerException {

		List<AnnouncementMessage> messageList = new ArrayList<>();

		filter = new PrivacyFilter(filter); // filter out drafts this user cannot see
		Site site = null;
		String initMergeList = null;

		try {
			site = siteService.getSite(getAnnouncementChannel(channelReference).getContext());

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
								messageList.addAll(siteChannel.getMessages(filter, ascending));
					}
				}
			}

			//sort messages
			Collections.sort(messageList);
			if (!ascending)
			{
				Collections.reverse(messageList);
			}
		}
		catch (NullPointerException e) {
			log.warn(e.getMessage());
		}

		String currentUserId = sessionManager.getCurrentSessionUserId();
		RoleAccessFilter roleFilter = new RoleAccessFilter(currentUserId);
		return messageList.stream()
			.filter(roleFilter::accept)
			.collect(Collectors.toList());
	} // getMessages

	public List<AnnouncementMessage> getVisibleMessagesOfTheDay(Time afterDate, int numberOfAnnouncements, boolean ascending) {

		int safeLimit = Math.max(0, numberOfAnnouncements);
		if (safeLimit == 0) {
			return Collections.emptyList();
		}

		String motdChannelReference = getSummarizableReference(null, MOTD_TOOL_ID);
		List<AnnouncementMessage> motdMessages;
		try {
			motdMessages = getMessages(
				motdChannelReference,
				new ViewableFilter(null, afterDate, Integer.MAX_VALUE, this),
				ascending,
				false);
		} catch (IdUnusedException | PermissionException pe) {
			MessageChannel motdChannel = getChannelPublic(motdChannelReference);
			if (motdChannel == null) {
				return Collections.emptyList();
			}
			motdMessages = ((List<AnnouncementMessage>) motdChannel.getMessagesPublic(
				new ViewableFilter(null, afterDate, Integer.MAX_VALUE, this),
				ascending));
		}

		return motdMessages.stream()
			.filter(this::isMessageViewable)
			.limit(safeLimit)
			.collect(Collectors.toList());
	}

	private class AnnouncementChannelReferenceMaker implements MergedList.ChannelReferenceMaker {

		public String makeReference(String siteId) {
			return channelReference(siteId, SiteService.MAIN_CONTAINER);
		}
	}

	/**
	 * Used by callback to convert channel references to channels.
	 */
	private class AnnouncementReferenceToChannelConverter implements
			MergedListEntryProviderFixedListWrapper.ReferenceToChannelConverter
	{
		public Object getChannel(final String channelReference)
		{
			SecurityAdvisor advisor = getChannelAdvisor(channelReference);
			try {
				securityService.pushAdvisor(advisor);
				return getAnnouncementChannel(channelReference);
			}
			catch (IdUnusedException e)
			{
				return null;
			}
			catch (PermissionException e)
			{
				log.warn("Permission denied for '{}' on '{}'", sessionManager.getCurrentSessionUserId(), channelReference);
				return null;
			} finally {
				securityService.popAdvisor(advisor);
			}
		}
	}

	/**
	 * Used to provide a interface to the MergedList class that is shared with the calendar action.
	 */
	class EntryProvider extends MergedListEntryProviderBase {

		/** announcement channels from hidden sites */
		private List<String> hiddenSites = new ArrayList<>();

		public EntryProvider() {
			this(false);
		}

		public EntryProvider(boolean includeHiddenSites) {

			if (includeHiddenSites) {
				List<String> excludedSiteIds = getExcludedSitesFromTabs();
				if (excludedSiteIds != null) {
					for (String siteId : excludedSiteIds) {
						hiddenSites.add(channelReference(siteId, SiteService.MAIN_CONTAINER));
					}
				}
			}
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.sakaiproject.util.MergedListEntryProviderBase#makeReference(java.lang.String)
		 */
		public Object makeObjectFromSiteId(String id) {

			String channelReference = channelReference(id, SiteService.MAIN_CONTAINER);
			Object channel = null;

			if (channelReference != null) {
				try {
					channel = getChannel(channelReference);
				} catch (IdUnusedException e) {
					// The channel isn't there.
				} catch (PermissionException e) {
					// We can't see the channel
				}
			}

			return channel;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.chefproject.actions.MergedEntryList.EntryProvider#allowGet(java.lang.Object)
		 */
		public boolean allowGet(String ref) {

			SecurityAdvisor advisor = getChannelAdvisor(ref);
			try {
				securityService.pushAdvisor(advisor);
				return (!hiddenSites.contains(ref) && allowGetChannel(ref));
			} finally {
				securityService.popAdvisor(advisor);
			}

		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.chefproject.actions.MergedEntryList.EntryProvider#getContext(java.lang.Object)
		 */
		public String getContext(Object obj) {

			if (obj == null) {
				return "";
			}

			AnnouncementChannel channel = (AnnouncementChannel) obj;
			return channel.getContext();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.chefproject.actions.MergedEntryList.EntryProvider#getReference(java.lang.Object)
		 */
		public String getReference(Object obj) {

			if (obj == null) {
				return "";
			}

			AnnouncementChannel channel = (AnnouncementChannel) obj;
			return channel.getReference();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.chefproject.actions.MergedEntryList.EntryProvider#getProperties(java.lang.Object)
		 */
		public ResourceProperties getProperties(Object obj) {

			if (obj == null) {
				return null;
			}

			AnnouncementChannel channel = (AnnouncementChannel) obj;
			return channel.getProperties();
		}
	}

	/**
	 * Pulls excluded site ids from Tabs preferences
	 */
	private List<String> getExcludedSitesFromTabs() {

	    Preferences prefs = preferencesService.getPreferences(sessionManager.getCurrentSessionUserId());
	    ResourceProperties props = prefs.getProperties(PreferencesService.SITENAV_PREFS_KEY);
	    List<String> l = props.getPropertyList("exclude");
	    return l;
	}

	@Override
	public Filter getMaxAgeInDaysAndAmountFilter(Integer maxAgeInDays, Integer amount) {

		if (maxAgeInDays == null) maxAgeInDays = 10;

		if (amount == null) amount = 100;

		ViewableFilter viewableFilter = new ViewableFilter(null, null, amount, this);
		long now = Instant.now().toEpochMilli();
		Time afterDate = timeService.newTime(now - (maxAgeInDays * 24 * 60 * 60 * 1000));
		viewableFilter.setFilter(new MessageSelectionFilter(afterDate, null, false));
		return viewableFilter;
	}

	public List<AnnouncementMessage> getChannelMessages(String channelReference, Filter filter, boolean ascending,
			String mergedChannelDelimitedList, boolean allUsersSites, boolean isSynopticTool, String siteId, Integer maxAgeInDays) throws PermissionException {

		if (filter == null && maxAgeInDays != null) {
			filter = getMaxAgeInDaysAndAmountFilter(maxAgeInDays, Integer.MAX_VALUE);
		}

		List<AnnouncementMessage> messageList = new ArrayList<>();

		MergedList mergedAnnouncementList = new MergedList();

		// TODO - MERGE FIX
		String[] channelArrayFromConfigParameterValue = new String[0];

		String currentUserId = sessionManager.getCurrentSessionUserId();

		// Figure out the list of channel references that we'll be using.
		// If we're on the workspace tab, we get everything.
		// Don't do this if we're the super-user, since we'd be
		// overwhelmed.

		//loading merged announcement channel reference, for Synoptic Announcement Tool-SAK-5865
		if (isSynopticTool) {

			// If siteId is null, we assume that this call is for the user's home site
			if (siteId == null) {
				siteId = siteService.getUserSiteId(currentUserId);
				channelReference = siteId;
			}

			Site site = null;
			String initMergeList = null;
			try {
				site = siteService.getSite(siteId);
				ToolConfiguration tc = site.getToolForCommonId(SAKAI_ANNOUNCEMENT_TOOL_ID);
				if (tc != null){
					initMergeList = tc.getPlacementConfig().getProperty(PORTLET_CONFIG_PARM_MERGED_CHANNELS);
				}

				if (allUsersSites && !securityService.isSuperUser()) {
					String[] channelArrayFromConfigParameterValueBefore = null;

					channelArrayFromConfigParameterValueBefore
						= mergedAnnouncementList.getAllPermittedChannels(new AnnouncementChannelReferenceMaker());
					if (channelArrayFromConfigParameterValueBefore !=null) {
						int sizeBefore = channelArrayFromConfigParameterValueBefore.length;
						List<String> channelIdStrArray = new ArrayList<>();
						for (int q = 0; q < sizeBefore; q++) {
							String channeIDD = channelArrayFromConfigParameterValueBefore[q];
							String contextt = null;
							Site siteDD = null;

							try {
								AnnouncementChannel annChannell = getAnnouncementChannel(channeIDD);
								if (annChannell != null ) {
									contextt = annChannell.getContext();
								}
								if (contextt != null) {
									siteDD = siteService.getSite(contextt);
								}
								if ( siteDD != null && siteDD.isPublished()) {
									channelIdStrArray.add(channeIDD);
								}
							} catch(IdUnusedException e) {
								log.debug("No announcement channel for ID: {}", channeIDD);
							} catch(PermissionException e) {
								log.debug("Permission exception for channelID: {}", channeIDD, e);
							} catch(Exception e) {
								log.warn("ChannelID: {}", channeIDD, e);
							}
						}
						if (channelIdStrArray.size() > 0) {
							channelArrayFromConfigParameterValue = channelIdStrArray.toArray(new String[0]);
						}
					}
					mergedAnnouncementList.loadChannelsFromDelimitedString(allUsersSites,
							new MergedListEntryProviderFixedListWrapper(new EntryProvider(false), channelReference, channelArrayFromConfigParameterValue, new AnnouncementReferenceToChannelConverter()),
							StringUtil.trimToZero(currentUserId),
							channelArrayFromConfigParameterValue,
							securityService.isSuperUser(),
							siteId);
				}
				else
				{
					channelArrayFromConfigParameterValue = mergedAnnouncementList
						.getChannelReferenceArrayFromDelimitedString(channelReference, initMergeList);

					mergedAnnouncementList
						.loadChannelsFromDelimitedString(allUsersSites,
							new MergedListEntryProviderFixedListWrapper(
								new EntryProvider(), channelReference, channelArrayFromConfigParameterValue,
								new AnnouncementReferenceToChannelConverter()),
							StringUtil.trimToZero(currentUserId), channelArrayFromConfigParameterValue, securityService.isSuperUser(),
							siteId);
				}

			} catch (IdUnusedException e1) {
				// TODO Auto-generated catch block
			}
		} else {
			if (allUsersSites && !securityService.isSuperUser()) {
				channelArrayFromConfigParameterValue = mergedAnnouncementList
						.getAllPermittedChannels(new AnnouncementChannelReferenceMaker());
			} else {
				channelArrayFromConfigParameterValue = mergedAnnouncementList
						.getChannelReferenceArrayFromDelimitedString(channelReference, mergedChannelDelimitedList);
			}
		}

		mergedAnnouncementList.loadChannelsFromDelimitedString(
				allUsersSites,
				new MergedListEntryProviderFixedListWrapper(
					new EntryProvider(),
					channelReference,
					channelArrayFromConfigParameterValue,
					new AnnouncementReferenceToChannelConverter() ),
				StringUtils.trimToEmpty(sessionManager.getCurrentSessionUserId()),
				channelArrayFromConfigParameterValue,
				securityService.isSuperUser(),
				siteId);

		Iterator channelsIt = mergedAnnouncementList.iterator();

		while (channelsIt.hasNext()) {

			MergedList.MergedEntry curEntry = (MergedList.MergedEntry) channelsIt.next();

			// If this entry should not be merged, skip to the next one.
			if (!curEntry.isMerged()) {
				continue;
			}

			SecurityAdvisor advisor = getChannelAdvisor(curEntry.getReference());
			try {
				securityService.pushAdvisor(advisor);
				AnnouncementChannel curChannel = (AnnouncementChannel) getChannel(curEntry.getReference());
				if (curChannel != null) {
					if (allowGetChannel(curChannel.getReference())) {
						messageList.addAll(((List<AnnouncementMessage>) curChannel.getMessages(filter, ascending)).stream().map(m -> {

								m.setOriginChannel(curEntry.getReference());
								m.setOriginSite(curChannel.getContext());
								return m;
							}).collect(Collectors.toList()));
					}
				}
			} catch (IdUnusedException e) {
				log.debug("{}.getMessages()", this, e);
			} catch (PermissionException e) {
				log.debug("{}.getMessages()", this, e);
			} finally {
				securityService.popAdvisor(advisor);
			}
		}

		RoleAccessFilter roleFilter = new RoleAccessFilter(currentUserId);
		return messageList.stream()
			.filter(roleFilter::accept)
			.collect(Collectors.toList());
	}

	/**
	 * Gets a security advisor for reading announcements.
	 * If <code>announcement.merge.visibility.strict</code> is set to <code>false</code>that allows messages from
	 * other channels to be read when the current user
	 * doesn't have permission. This is used to allow messages from merged sites to appear without the
	 * current user having to be a member.
	 * @param channelReference The entity reference of the channel in another site.
	 * @return A security advisor that allows the current user access to that content.
	 */
	public SecurityAdvisor getChannelAdvisor(String channelReference) {

		if (serverConfigurationService.getBoolean("announcement.merge.visibility.strict", false)) {
			return (userId, function, reference) -> SecurityAdvisor.SecurityAdvice.PASS;
		} else {
			return (userId, function, reference) -> {
				if (userId.equals(userDirectoryService.getCurrentUser().getId()) &&
						AnnouncementService.SECURE_ANNC_READ.equals(function) &&
						channelReference.equals(reference)) {
					return SecurityAdvisor.SecurityAdvice.ALLOWED;
				} else {
					return SecurityAdvisor.SecurityAdvice.PASS;
				}
			};
		}
	}
	
	
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

	protected String getSummaryFromHeader(Message item, MessageHeader header) {

		String newText;
		if (header instanceof AnnouncementMessageHeader) {
			AnnouncementMessageHeader hdr = (AnnouncementMessageHeader) header;
			newText = hdr.getSubject();
		} else {
			newText = item.getBody();
			if (newText.length() > 50) newText = newText.substring(1, 49);
		}
		newText = newText + ", " + header.getFrom().getDisplayName() + ", " + header.getDate().toStringLocalFull();
		return newText;
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> resourceIds, List<String> options) {

		Map<String, String> transversalMap = new HashMap<>();

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
						String newBody = oMessage.getBody();
						newBody = ltiService.fixLtiLaunchUrls(newBody, fromContext, toContext, transversalMap);
						newBody = linkMigrationHelper.migrateOneLink(fromContext, toContext, newBody);
						nMessage.setBody(newBody);
						// message header
						AnnouncementMessageHeaderEdit nMessageHeader = (AnnouncementMessageHeaderEdit) nMessage.getHeaderEdit();
						nMessageHeader.setDate(oMessageHeader.getDate());
						nMessageHeader.setMessage_order(oMessageHeader.getMessage_order());
						// when importing, refer to property to determine draft status
						if (!serverConfigurationService.getBoolean("import.importAsDraft", true))
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
						List nAttachments = entityManager.newReferenceList();
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
									nAttachments.add(entityManager.newReference(attachment.getReference()));
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
														getToolTitle(),
														oAttachment.getContentType(),
														oAttachment.getContent(),
														oAttachment.getProperties());
												// add to attachment list
												nAttachments.add(entityManager.newReference(attachment.getReference()));
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
												nAttachments.add(entityManager.newReference(attachment.getReference()));
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

		return transversalMap;
	}

	@Override
	public List<Map<String, String>> getEntityMap(String fromContext) {

		// get the channel associated with this site
		String oChannelRef = channelReference(fromContext, SiteService.MAIN_CONTAINER);
		try {
			AnnouncementChannel oChannel = (AnnouncementChannel) getChannel(oChannelRef);
			return ((List<AnnouncementMessage>) oChannel.getMessages(null, true)).stream()
				.map(ann -> Map.of("id", ann.getId(), "title", ann.getAnnouncementHeader().getSubject())).collect(Collectors.toList());
		} catch (Exception e) {
			log.warn("Failed to get channel for ref {}", e.toString());
		}
		return Collections.EMPTY_LIST;
	}


	@Override
	public String getToolPermissionsPrefix() {
		return SECURE_ANNC_ROOT;
	}

	@Override
	public boolean hasContent(String siteId) {

		// get the channel associated with this site
		String oChannelRef = channelReference(siteId, SiteService.MAIN_CONTAINER);
		try {
			AnnouncementChannel oChannel = (AnnouncementChannel) getChannel(oChannelRef);
			return !oChannel.getMessages(null, true).isEmpty();
		} catch (Exception e) {
			log.warn("Failed to get channel for ref {}", e.toString());
		}

		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void updateEntityReferences(String toContext, Map<String, String> transversalMap){
		if(transversalMap != null && transversalMap.size() > 0){
			try
			{
				Set<Entry<String, String>> entrySet = (Set<Entry<String, String>>) transversalMap.entrySet();
				
				String channelId = serverConfigurationService.getString(ANNOUNCEMENT_CHANNEL_PROPERTY, null);

				String toSiteId = toContext;

				if (channelId == null)
				{
					channelId = channelReference(toSiteId, SiteService.MAIN_CONTAINER);
					try
					{
						AnnouncementChannel aChannel = getAnnouncementChannel(channelId);
						//need to clear the cache to grab the newly saved messages
						threadLocalManager.set(aChannel.getReference() + ".msgs", null);
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
								String targetContextRef = entry.getValue();
								if(msgBody.contains(fromContextRef)){
									updated = true;
								}
								msgBody = linkMigrationHelper.migrateOneLink(fromContextRef, targetContextRef, msgBody);
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

	/* We use the archive() and merge() methods from the BaseMessage class and override these methods to achieve the behavior we need. */

	@Override
	public boolean importAsDraft() {
		return true; // Always import as a draft
	}

	@Override
	public boolean approveMessageSender(String userId) {
		return true; // Always approve the sender
	}

	/**
	 * This method is used to check if a message can be merged - it is called with an element from the XML that represents the message
	 *
	 * 	<message body="T3BljAwIEFNIEVTVC4=" body-html="PHA+T3BjAwIEFNIEVTVC48L3A+">
	 *		<header access="channel" date="20250217200655521" draft="true" from="" id="ad7bcb9a-f335-46a7-ad01-907778fb9df5" message_order="2"
	 *		 subject="Assignment: Open Date for ''HW2''"/>
	 *		<properties>
	 *			<property enc="BASE64" list="list" name="noti_history" value="MjAyNSaXzA=" B64Decoded="2025-02-17T20:06:55.538289588Z_0"/>
	 *			<property enc="BASE64" name="assignmentReference" value="L2Fzc2e8/5fab8f09-e029-4672-b478-43cfa3e17d64"/>
	 *		</properties>
	 *	</message>
	 */
	@Override
	public boolean checkAllowMergeElement(Element element) {
		NodeList propertyList = element.getElementsByTagName("property");
		for (int i = 0; i < propertyList.getLength(); i++) {
			Element propertyElement = (Element) propertyList.item(i);
			String name = propertyElement.getAttribute("name");
			if (name.equals("assignmentReference")) {
				log.debug("Assignment announcement found, not merging");
				return false;
			}
		}
		return true;
	}

	@Override
	public String getToolTitle(String url) {
		return getToolTitle();
	}

	public String getToolTitle() {
		return toolManager.getTool(AnnouncementService.SAKAI_ANNOUNCEMENT_TOOL_ID).getTitle();
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
		@Override
		public AnnouncementMessage getAnnouncementMessage(String messageId) throws IdUnusedException, PermissionException
		{
			AnnouncementMessage msg = (AnnouncementMessage) getMessage(messageId);

			// Apply the privacy filter to check draft permissions
			PrivacyFilter filter = new PrivacyFilter(null);
			if (!filter.accept(msg)) {
				throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_READ, msg.getReference());
			}

			// Check group access permissions: empty list returned means no permission to view
			if (filterGroupAccess(List.of(msg)).isEmpty()) {
				throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_READ, msg.getReference());
			}

			String currentUserId = sessionManager.getCurrentSessionUserId();
			RoleAccessFilter roleFilter = new RoleAccessFilter(currentUserId);
			if (!roleFilter.accept(msg)) {
				throw new PermissionException(currentUserId, SECURE_READ, msg.getReference());
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
			boolean releaseDateFirst = serverConfigurationService.getBoolean("sakai.announcement.release_date_first", true);
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
		private String originChannel;

		private String originSite;

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

		public void setOriginChannel(String originChannel) {
			this.originChannel = originChannel;
		}

		public String getOriginChannel() {
			return this.originChannel;
		}

		public void setOriginSite(String originSite) {
			this.originSite = originSite;
		}

		public String getOriginSite() {
			return this.originSite;
		}

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
			String encodedSubject = el.getAttribute("subject");
			m_subject = StringUtils.isEmpty(encodedSubject)
					? encodedSubject
					: formattedText.decodeNumericCharacterReferences(encodedSubject);

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
		@Override
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
			String encodedSubject = formattedText.encodeUnicode(getSubject());
			header.setAttribute("subject", encodedSubject);
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
		@Override
		public boolean accept(Object o)
		{
			// first if o is a announcement message that's a draft from another user, reject it
			if (o instanceof AnnouncementMessage)
			{
				AnnouncementMessage msg = (AnnouncementMessage) o;
				String currentUserId = sessionManager.getCurrentSessionUserId();

				// Check draft visibility
				if ((msg.getAnnouncementHeader()).getDraft())
				{
					// Allow if user is the creator of the message
					if (msg.getHeader().getFrom().getId().equals(currentUserId)) {
						return true;
					}
					
					// Allow if user has READ_DRAFT permission (this also covers superusers)
					if (unlockCheck(SECURE_READ_DRAFT, msg.getReference())) {
						return true;
					}
					
					// Allow if user has site.upd permission (instructor)
					String siteId = entityManager.newReference(msg.getReference()).getContext();
					String siteRef = siteService.siteReference(siteId);
					if (securityService.unlock(SiteService.SECURE_UPDATE_SITE, siteRef)) {
						log.debug("PrivacyFilter: Allowing draft message {} to be viewed by instructor with site.upd in site {}", 
							msg.getId(), siteId);
						return true;
					}
					
					log.debug("PrivacyFilter: Rejecting draft message {} for user {} in site {}", 
						msg.getId(), currentUserId, siteId);
					return false;
				}

				// Check release/retract date visibility
				// If not super-user and not the message creator, check if viewable by dates
				if (!securityService.isSuperUser()
						&& !msg.getHeader().getFrom().getId().equals(currentUserId)
						&& !isMessageViewable(msg))
				{
					return false;
				}
			}

			// now, use the real filter, if present
			if (m_filter != null) return m_filter.accept(o);

			return true;

		} // accept

	} // PrivacyFilter

	/**
	 * Filter to control access to messages based on custom roles.
	 * Only users whose role is included in the message's "selectedRoles" property,
	 * the message owner, or superusers will be able to access the message.
	 */
	protected class RoleAccessFilter implements Filter {

		private final String currentUserId;

		/**
		 * Constructs a RoleAccessFilter for the specified user.
		 *
		 * @param currentUserId the ID of the user whose access is being checked
		 */
		public RoleAccessFilter(String currentUserId) {
			this.currentUserId = currentUserId;
		}

		/**
		 * Determines if the given object (announcement message) is accessible to the current user.
		 *
		 * @param o the object to check (should be an AnnouncementMessage)
		 * @return true if the user can access the message, false otherwise
		 */
		@Override
		public boolean accept(Object o) {
			if (o instanceof AnnouncementMessage) {
				AnnouncementMessage msg = (AnnouncementMessage) o;
				List<String> selectedRoles = msg.getProperties().getPropertyList("selectedRoles");
				boolean isOwner = currentUserId.equals(msg.getAnnouncementHeader().getFrom().getId());
				if (selectedRoles == null || isOwner || securityService.isSuperUser()) {
					return true;
				} else {
					String messageSiteId = entityManager.newReference(msg.getReference()).getContext();
					try {
						Site msgSite = siteService.getSite(messageSiteId);
						Member member = msgSite.getMember(currentUserId);
						if (member == null) {
							log.warn("User {} is not a member of site {}", currentUserId, messageSiteId);
							return false;
						}
						String userRole = member.getRole().getId();
						return selectedRoles.contains(userRole);
					} catch (IdUnusedException idue) {
						log.warn("No site found with id {}", messageSiteId);
						return false;
					}
				}
			}
			return true;
		}
	}

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

	public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> options, boolean cleanup) {

		try
		{
			if(cleanup == true)
			{
				String channelId = serverConfigurationService.getString(ANNOUNCEMENT_CHANNEL_PROPERTY, null);
				
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
		transferCopyEntities(fromContext, toContext, ids, null);
		return null;
	} 

	public void clearMessagesCache(String channelRef){
		threadLocalManager.set(channelRef + ".msgs", null);
	}

	public Optional<String> getEntityUrl(Reference r, Entity.UrlType urlType) {

		//Reference r = getReference(ref);
		if (Entity.UrlType.PORTAL == urlType) {
			if (r != null) {
				String siteId = r.getContext();
				Site site;
				try {
					site = siteService.getSite(siteId);
					ToolConfiguration tc = site.getToolForCommonId("sakai.announcements");
					if (tc != null) {
						return Optional.of(serverConfigurationService.getPortalUrl() + "/directtool/" + tc.getId()
							+ "?itemReference=" + r.getReference() + "&sakai_action=doShowmetadata");
					} else {
						log.error("No announcements tool in site {}", siteId);
					}
				} catch (IdUnusedException iue) {
					log.error("Failed to get site site for id {}", siteId);
				}
			} else {
				log.error("Failed to get reference for {}", r.getReference());
			}
		}

		return Optional.of(super.getEntityUrl(r));
	}

	@Override
	public void removed(Site site){
		String siteId = site.getId();

		List<String> ids = getChannelIds(siteId);
		for (String id : ids){
			String ref = channelReference(siteId, id);
			try{
				AnnouncementChannel announcementChannel = getAnnouncementChannel(ref);
				List<Message> messages = announcementChannel.getMessages(null, true, null);
				for(Message message : messages){
					try{
						announcementChannel.removeAnnouncementMessage(message.getId());
					}catch (PermissionException e) {
						log.error("The current user does not have permission to remove message  for context: {}", siteId, e);
					}
				}
				MessageChannelEdit edit = editChannel(ref);
				removeChannel(edit);
			} catch (IdUnusedException e1) {
				log.warn("No AnnouncementChannel found for site: " + siteId);
			} catch (PermissionException e2) {
				log.error("The current user does not have permission to access AnnouncementChannel for context: {}", siteId, e2);
			} catch (InUseException e3) {
				log.error("InUseException exception occurred for message channel for site: {}", siteId, e3);
			}catch (Exception e) {
				log.error("Unknown exception occurred in announcement service  for site: {}", siteId, e);
			}
		}
		// remove any alias
		try {
			aliasService.removeTargetAliases("/announcement/announcement/" + siteId);
		} catch (PermissionException e) {
			log.error("The current user does not have permission to remove announcementChannel aliases for context: {}", siteId, e);
		}
	}
}
