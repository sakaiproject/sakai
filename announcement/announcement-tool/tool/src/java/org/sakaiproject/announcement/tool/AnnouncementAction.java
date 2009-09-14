/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.announcement.tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Stack;
import java.util.Vector;
import java.text.Collator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementChannelEdit;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageEdit;
import org.sakaiproject.announcement.api.AnnouncementMessageHeader;
import org.sakaiproject.announcement.api.AnnouncementMessageHeaderEdit;
import org.sakaiproject.announcement.cover.AnnouncementService;
import org.sakaiproject.alias.cover.AliasService;
import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.ControllerState;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PagedResourceActionII;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.api.MenuItem;
import org.sakaiproject.cheftool.menu.MenuDivider;
import org.sakaiproject.cheftool.menu.MenuEntry;
import org.sakaiproject.cheftool.menu.MenuImpl;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.cover.ContentTypeImageService;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.message.api.Message;
import org.sakaiproject.message.api.MessageHeader;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.ContextualUserDisplayService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.MergedList;
import org.sakaiproject.util.MergedListEntryProviderBase;
import org.sakaiproject.util.MergedListEntryProviderFixedListWrapper;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.SortedIterator;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * AnnouncementAction is an implementation of Announcement service, which provides the complete function of announcements. User could check the announcements, create own new and manage all the announcement items, under certain permission check.
 */
public class AnnouncementAction extends PagedResourceActionII
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(AnnouncementAction.class);
	
	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("announcement");

	private static final String CONTEXT_ENABLED_MENU_ITEM_EXISTS = "EnabledMenuItemExists";

	private static final String CONTEXT_ENABLE_ITEM_CHECKBOXES = "EnableItemCheckBoxes";

	private static final String ENABLED_MENU_ITEM_EXISTS = CONTEXT_ENABLED_MENU_ITEM_EXISTS;

	private static final String NOT_SELECTED_FOR_REVISE_STATUS = "noSelectedForRevise";

	private static final String FINISH_DELETING_STATUS = "FinishDeleting";

	private static final String DELETE_ANNOUNCEMENT_STATUS = "deleteAnnouncement";

	private static final String POST_STATUS = "post";

	private static final String CANCEL_STATUS = "cancel";

	private static final String MERGE_STATUS = "merge";

	private static final String OPTIONS_STATUS = "options";

	private static final String SSTATE_NOTI_VALUE = "noti_value";

	private static final String SSTATE_PUBLICVIEW_VALUE = "public_view_value";

	private static final String SORT_DATE = "date";
	
	private static final String SORT_RELEASEDATE = "releasedate";
	
	private static final String SORT_RETRACTDATE = "retractdate";

	private static final String SORT_PUBLIC = "public";

	private static final String SORT_FROM = "from";

	private static final String SORT_SUBJECT = "subject";

	private static final String SORT_CHANNEL = "channel";

	private static final String SORT_FOR = "for";

	private static final String SORT_GROUPTITLE = "grouptitle";

	private static final String SORT_GROUPDESCRIPTION = "groupdescription";

	private static final String CONTEXT_VAR_DISPLAY_OPTIONS = "displayOptions";

	private static final String VELOCITY_DISPLAY_OPTIONS = CONTEXT_VAR_DISPLAY_OPTIONS;

	private static final String PERMISSIONS_BUTTON_HANDLER = "doPermissions";

	private static final String MERGE_BUTTON_HANDLER = "doMerge";

	private static final String SSTATE_ATTRIBUTE_MERGED_CHANNELS = "mergedChannels";

	private static final String VELOCITY_MERGED_CHANNEL_LIST = "mergedAnnouncementsCollection";

	/** state attribute names. */
	private static final String STATE_CHANNEL_REF = "channelId";
	
	private static final String STATE_CHANNEL_PUBVIEW = "channelPubView";

	private static final String PORTLET_CONFIG_PARM_NON_MERGED_CHANNELS = "nonMergedAnnouncementChannels";

	private static final String PORTLET_CONFIG_PARM_MERGED_CHANNELS = "mergedAnnouncementChannels";

	public static final String STATE_MESSAGE = "message";

	public static final String STATE_MESSAGES = "pagedMessages";

	protected static final String STATE_INITED = "annc.state.inited";

	private static final String STATE_CURRENT_SORTED_BY = "session.state.sorted.by";

	private static final String STATE_CURRENT_SORT_ASC = "session.state.sort.asc";

	private static final String STATE_SELECTED_VIEW = "state.selected.view";

	private static final String SC_TRUE = "true";

	private static final String SC_FALSE = "false";

	private static final String PUBLIC_DISPLAY_DISABLE_BOOLEAN = "publicDisplayBoolean";
   
   private static final String VIEW_MODE_ALL      = "view.all";
   private static final String VIEW_MODE_PUBLIC   = "view.public";
   private static final String VIEW_MODE_BYGROUP  = "view.bygroup";
   private static final String VIEW_MODE_MYGROUPS = "view.mygroups";

   // hours * minutes * seconds * milliseconds
   private static final long MILLISECONDS_IN_DAY = (24 * 60 * 60 * 1000);
   private static final long FUTURE_DAYS = 7;
   
   private static final String HIDDEN = "hidden";
   private static final String SPECIFY_DATES  = "specify";
   
   private static final String SYNOPTIC_ANNOUNCEMENT_TOOL = "sakai.synoptic.announcement";
 
   private static final String UPDATE_PERMISSIONS = "site.upd";

   private ContentHostingService contentHostingService = null;
   
   private AssignmentService assignmentService = null;

	/**
	 * Used by callback to convert channel references to channels.
	 */
	private final class AnnouncementReferenceToChannelConverter implements
			MergedListEntryProviderFixedListWrapper.ReferenceToChannelConverter
	{
		public Object getChannel(String channelReference)
		{
			try
			{
				return AnnouncementService.getAnnouncementChannel(channelReference);
			}
			catch (IdUnusedException e)
			{
				return null;
			}
			catch (PermissionException e)
			{
				return null;
			}
		}
	}

	/*
	 * Callback class so that we can form references in a generic way.
	 */
	private final class AnnouncementChannelReferenceMaker implements MergedList.ChannelReferenceMaker
	{
		public String makeReference(String siteId)
		{
			return AnnouncementService.channelReference(siteId, SiteService.MAIN_CONTAINER);
		}
	}

	/**
	 * Used to provide a interface to the MergedList class that is shared with the calendar action.
	 */
	class EntryProvider extends MergedListEntryProviderBase
	{
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.util.MergedListEntryProviderBase#makeReference(java.lang.String)
		 */
		public Object makeObjectFromSiteId(String id)
		{
			String channelReference = AnnouncementService.channelReference(id, SiteService.MAIN_CONTAINER);
			Object channel = null;

			if (channelReference != null)
			{
				try
				{
					channel = AnnouncementService.getChannel(channelReference);
				}
				catch (IdUnusedException e)
				{
					// The channel isn't there.
				}
				catch (PermissionException e)
				{
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
		public boolean allowGet(String ref)
		{
			return AnnouncementService.allowGetChannel(ref);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.chefproject.actions.MergedEntryList.EntryProvider#getContext(java.lang.Object)
		 */
		public String getContext(Object obj)
		{
			if (obj == null)
			{
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
		public String getReference(Object obj)
		{
			if (obj == null)
			{
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
		public ResourceProperties getProperties(Object obj)
		{
			if (obj == null)
			{
				return null;
			}

			AnnouncementChannel channel = (AnnouncementChannel) obj;
			return channel.getProperties();
		}

	}

	/**
	 * Decorator for the "Message" class. It adds various properties to the decorated real Announcement message.
	 */
	static public class AnnouncementWrapper implements AnnouncementMessage
	{
		private boolean enforceMaxNumberOfChars;

		private AnnouncementMessage announcementMesssage;

		private boolean editable;

		private String channelDisplayName;

		private int maxNumberOfChars;

		private String range;
		
		private String authorDisplayName;
		
		public AnnouncementMessage getMessage()
		{
			return this.announcementMesssage;
		}

		/**
		 * Constructor
		 * 
		 * @param message
		 *        The message to be wrapped.
		 * @param currentChannel
		 *        The channel in which the message is contained.
		 * @param hostingChannel
		 *        The channel into which the message is being merged.
		 * @param maxNumberOfChars
		 *        The maximum number of characters that will be returned by getTrimmedBody().
		 */
		public AnnouncementWrapper(AnnouncementMessage message, AnnouncementChannel currentChannel,
				AnnouncementChannel hostingChannel, AnnouncementActionState.DisplayOptions options, String range)
		{
			if (options != null)
			{
				this.maxNumberOfChars = options.getNumberOfCharsPerAnnouncement();
				this.enforceMaxNumberOfChars = options.isEnforceNumberOfCharsPerAnnouncement();
			}
			else
			{
				// default settings from DisplayOptions class
				this.maxNumberOfChars = Integer.MAX_VALUE;
				this.enforceMaxNumberOfChars = false;
			}
			this.announcementMesssage = message;

			// This message is editable only if the site matches.
			this.editable = currentChannel.getReference().equals(hostingChannel.getReference());

			Site site = null;

			try
			{
				site = SiteService.getSite(currentChannel.getContext());
			}
			catch (IdUnusedException e)
			{
				// No site available.
			}

			if (site != null)
			{
				this.channelDisplayName = site.getTitle();
			}
			else
			{
				this.channelDisplayName = "";
			}

			// TODO Let's not retrieve the service for each and every message....
			ContextualUserDisplayService contextualUserDisplayService = (ContextualUserDisplayService) ComponentManager.get("org.sakaiproject.user.api.ContextualUserDisplayService");
			User author = message.getAnnouncementHeader().getFrom();
			if ((site != null) && (!this.editable) && (contextualUserDisplayService != null))
			{
				this.authorDisplayName = contextualUserDisplayService.getUserDisplayName(author, site.getReference());
			}
			if (this.authorDisplayName == null)
			{
				this.authorDisplayName = author.getDisplayName();
			}

			if (range != null)
			{
				this.range = range;
			}
		}

		/**
		 * Constructor
		 * 
		 * @param announcementWrapper
		 *        The message to be wrapped.
		 */
		public AnnouncementWrapper(AnnouncementWrapper mWrapper)
		{
			this.maxNumberOfChars = mWrapper.maxNumberOfChars;
			this.enforceMaxNumberOfChars = mWrapper.enforceMaxNumberOfChars;
			this.announcementMesssage = mWrapper.getMessage();
			
			this.channelDisplayName = mWrapper.channelDisplayName;
			this.range = mWrapper.range;
		}

		/**
		 * See if the given message was posted in the last N days, where N is the value of the maxDaysInPast parameter.
		 */
		private static boolean isMessageWithinLastNDays(AnnouncementMessage message, int maxDaysInPast)
		{
			long currentTime = TimeService.newTime().getTime();

			long timeDeltaMSeconds = currentTime - message.getHeader().getDate().getTime();

			long numDays = timeDeltaMSeconds / MILLISECONDS_IN_DAY;

			return (numDays <= maxDaysInPast);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.chefproject.core.Message#getHeader()
		 */
		public MessageHeader getHeader()
		{
			return announcementMesssage.getHeader();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.chefproject.core.Message#getBody()
		 */
		public String getBody()
		{
			return announcementMesssage.getBody();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.chefproject.core.Message#getBody()
		 */
		public String getTrimmedBody()
		{
			if (this.enforceMaxNumberOfChars)
			{
				// trim the body, as formatted text
				String body = announcementMesssage.getBody();
				StringBuilder buf = new StringBuilder();
				body = FormattedText.escapeHtmlFormattedTextSupressNewlines(body);
				boolean didTrim = FormattedText.trimFormattedText(body, this.maxNumberOfChars, buf);
				if (didTrim)
				{
					if (buf.toString().length() != 0)
					{
						buf.append("...");
					}
				}

				return buf.toString();
			}
			else
			{
				return announcementMesssage.getBody();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.chefproject.core.Resource#getUrl()
		 */
		public String getUrl()
		{
			return announcementMesssage.getUrl();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.chefproject.core.Resource#getReference()
		 */
		public String getReference()
		{
			return announcementMesssage.getReference();
		}

		/**
		 * @inheritDoc
		 */
		public String getReference(String rootProperty)
		{
			return getReference();
		}

		/**
		 * @inheritDoc
		 */
		public String getUrl(String rootProperty)
		{
			return getUrl();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.chefproject.core.Resource#getId()
		 */
		public String getId()
		{
			return announcementMesssage.getId();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.chefproject.core.Resource#getProperties()
		 */
		public ResourceProperties getProperties()
		{
			return announcementMesssage.getProperties();
		}
		
		/**
		 * returns the range string
		 * 
		 * @return
		 */
		public String getRange()
		{
			return range;
		}

		/**
		 * Set the range string
		 * 
		 * @return
		 */
		public void setRange(String range)
		{
			this.range = range;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.chefproject.core.Resource#toXml(org.w3c.dom.Document, java.util.Stack)
		 */
		public Element toXml(Document doc, Stack stack)
		{
			return announcementMesssage.toXml(doc, stack);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Object arg0)
		{
			return announcementMesssage.compareTo(arg0);
		}

		/**
		 * Returns true if the message is editable.
		 */
		public boolean isEditable()
		{
			return editable;
		}

		/**
		 * Returns the string that is used to show the channel to the user.
		 */
		public String getChannelDisplayName()
		{
			return channelDisplayName;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.chefproject.core.AnnouncementMessage#getAnnouncementHeader()
		 */
		public AnnouncementMessageHeader getAnnouncementHeader()
		{
			return announcementMesssage.getAnnouncementHeader();
		}
		
		public String getAuthorDisplayName()
		{
			return authorDisplayName;
		}

		/**
		 * Constructs a list of wrapped/decorated AnnouncementMessages when given a list of unwrapped/undecorated AnnouncementMessages.
		 * 
		 * @param messages
		 *        The list of messages.
		 * @param currentChannel
		 *        The current channel being processed.
		 * @param hostingChannel
		 *        The default channel of the page into which this list is being merged.
		 * @param maxNumberOfDaysInThePast
		 *        Messages over this limit will not be included in the list.
		 * @param maxCharsPerAnnouncement
		 *        The maximum number of characters that will be returned when getTrimmedBody() is called.
		 */
		static private List wrapList(List messages, AnnouncementChannel currentChannel, AnnouncementChannel hostingChannel,
				AnnouncementActionState.DisplayOptions options)
		{
			// 365 is the default in DisplayOptions
			int maxNumberOfDaysInThePast = (options != null) ? options.getNumberOfDaysInThePast() : 365;
			 

			List messageList = new ArrayList();

			Iterator it = messages.iterator();

			while (it.hasNext())
			{
				AnnouncementMessage message = (AnnouncementMessage) it.next();

				// See if the message falls within the filter window.
				// note: the default of enforceNumberOfDaysInThePastLimit is false
				if (options != null && options.isEnforceNumberOfDaysInThePastLimit() && !isMessageWithinLastNDays(message, maxNumberOfDaysInThePast))
				{
					continue;
				}

				messageList.add(new AnnouncementWrapper(message, currentChannel, hostingChannel, options,
						getAnnouncementRange(message)));
			}

			return messageList;
		}

	}

	/**
	 * get announcement range information
	 */
	private static String getAnnouncementRange(AnnouncementMessage a)
	{
		if (a.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW) != null
				&& a.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW).equals(Boolean.TRUE.toString()))
		{
			return rb.getString("gen.public");
		}
		else if (a.getAnnouncementHeader().getAccess().equals(MessageHeader.MessageAccess.CHANNEL))
		{
			return rb.getString("range.allgroups");
		}
		else
		{
			int count = 0;
			String allGroupString = "";
			try
			{
				Site site = SiteService.getSite(EntityManager.newReference(a.getReference()).getContext());
				for (Iterator i = a.getAnnouncementHeader().getGroups().iterator(); i.hasNext();)
				{
					Group aGroup = site.getGroup((String) i.next());
					if (aGroup != null)
					{
						count++;
						if (count > 1)
						{
							allGroupString = allGroupString.concat(", ").concat(aGroup.getTitle());
						}
						else
						{
							allGroupString = aGroup.getTitle();
						}
					}
				}
			}
			catch (IdUnusedException e)
			{
				// No site available.
			}
			return allGroupString;
		}
	}

	/**
	 * Enable or disable the observer
	 * 
	 * @param enable
	 *        if true, the observer is enabled, if false, it is disabled
	 */
	protected void enableObserver(SessionState sstate, boolean enable)
	{
		if (enable)
		{
			enableObservers(sstate);
		}
		else
		{
			disableObservers(sstate);
		}
	}

	/**
	 * See if the current tab is the workspace tab.
	 * 
	 * @return true if we are currently on the "My Workspace" tab.
	 */
	private boolean isOnWorkspaceTab()
	{
		// TODO: this is such a bad question... workspace? tab? revisit this. -ggolden
		// return false;
		// // we'll really answer the question - is the current request's site a user site, and not the ~admin user's site.
		String siteId = ToolManager.getCurrentPlacement().getContext();
		if (SiteService.getUserSiteId("admin").equals(siteId)) return false;
		return SiteService.isUserSite(siteId);
	}

	/**
	 * Build the context for showing merged view
	 */
	public String buildMergeContext(VelocityPortlet portlet, Context context, RunData runData, AnnouncementActionState state,
			SessionState sstate)
	{

		MergedList mergedAnnouncementList = new MergedList();

		mergedAnnouncementList.loadChannelsFromDelimitedString(isOnWorkspaceTab(), new EntryProvider(), StringUtil
				.trimToZero(SessionManager.getCurrentSessionUserId()), mergedAnnouncementList
				.getChannelReferenceArrayFromDelimitedString(state.getChannelId(), portlet.getPortletConfig().getInitParameter(
						getPortletConfigParameterNameForLoadOnly(portlet))), SecurityService.isSuperUser(), ToolManager
				.getCurrentPlacement().getContext());

		// Place this object in the context so that the velocity template
		// can get at it.
		context.put(VELOCITY_MERGED_CHANNEL_LIST, mergedAnnouncementList);
		context.put("tlang", rb);
		sstate.setAttribute(SSTATE_ATTRIBUTE_MERGED_CHANNELS, mergedAnnouncementList);

		String template = (String) getContext(runData).get("template");
		return template + "-merge";
	}

	/**
	 * This is a cover to return the right config parameter name, regardless of whether the parameter is using an older, deprecated name or the newer version.
	 */
	private String getPortletConfigParameterNameForLoadOnly(VelocityPortlet portlet)
	{
		// Check to see if the older non-merged parameter is present.
		// This is really the "merged" parameter, but it was incorrectly
		// named. This is for backward compatibility.
		String configParameter = StringUtil.trimToNull(portlet.getPortletConfig().getInitParameter(
				PORTLET_CONFIG_PARM_NON_MERGED_CHANNELS));
		String configParameterName = configParameter != null ? PORTLET_CONFIG_PARM_NON_MERGED_CHANNELS
				: PORTLET_CONFIG_PARM_MERGED_CHANNELS;
		return configParameterName;
	}
	
	/**
	 * Default is to use when Portal starts up
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState sstate)
	{
		// retrieve the state from state object
		AnnouncementActionState state = (AnnouncementActionState) getState(portlet, rundata, AnnouncementActionState.class);

		// load the saved option data
		Placement placement = ToolManager.getCurrentPlacement();
		if (placement != null)
		{
			Properties props = placement.getPlacementConfig();
			if(props.isEmpty())
				props = placement.getConfig();
			AnnouncementActionState.DisplayOptions disOptions = state.getDisplayOptions();
			if (disOptions != null)
			{
				disOptions.loadProperties(props);
			}
			context.put(VELOCITY_DISPLAY_OPTIONS, disOptions);
		}
		else
		{
			// Put our display options in the context so
			// that we can modify our list display accordingly
			context.put(VELOCITY_DISPLAY_OPTIONS, state.getDisplayOptions());
		}

		String template = (String) getContext(rundata).get("template");

		// group realted variables
		context.put("channelAccess", MessageHeader.MessageAccess.CHANNEL);
		context.put("groupAccess", MessageHeader.MessageAccess.GROUPED);
		// ********* for site column display ********
		Site site = null;
		try
		{
			site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
			context.put("site", site);
		}
		catch (IdUnusedException e)
		{
			// No site available.
			M_log.debug(this+".buildMainPanelContext ", e);
		}
		catch (NullPointerException e)
		{
			M_log.warn(this+".buildMainPanelContext ", e);
		}

		// get the current channel ID from state object or prolet initial parameter
		String channelId = state.getChannelId();
		if (channelId == null)
		{
			// try the portlet parameter
			channelId = StringUtil.trimToNull(portlet.getPortletConfig().getInitParameter("channel"));
			if (channelId == null)
			{
				// form based on the request's site's "main" channel
				channelId = AnnouncementService.channelReference(ToolManager.getCurrentPlacement().getContext(),
						SiteService.MAIN_CONTAINER);
			}

			// let the state object have the current channel id
			state.setChannelId(channelId);
			state.setIsListVM(true);
		}
		// context.put("channel_id", ((channelId == null) ? "Now null" : channelId));
		context.put("channel_id", ((channelId == null) ? rb.getString("java.nownull") : channelId));

		// set if we have notification enabled
		context.put("notification", Boolean.valueOf(notificationEnabled(state)));

		// find the channel and channel information through the service
		AnnouncementChannel channel = null;

		boolean menu_new = true;
		boolean menu_delete = true;
		boolean menu_revise = true;

		try
		{
			if (AnnouncementService.allowGetChannel(channelId) && isOkayToDisplayMessageMenu(state))
			{
				// get the channel name throught announcement service API
				channel = AnnouncementService.getAnnouncementChannel(channelId);

				if (channel.allowGetMessages() && !state.getCurrentSortedBy().equals(SORT_GROUPTITLE)
						&& !state.getCurrentSortedBy().equals(SORT_GROUPDESCRIPTION))
				{
					// this checks for any possibility of an add, channel or any site group
					menu_new = channel.allowAddMessage();

					List messages = null;

					String view = (String) sstate.getAttribute(STATE_SELECTED_VIEW);

					if (view != null)
					{
                  if (view.equals(VIEW_MODE_ALL))
						{
							messages = getMessages(channel, null, true, state, portlet);
						}
						else if (view.equals(VIEW_MODE_BYGROUP))
						{
							messages = getMessagesByGroups(site, channel, null, true, state, portlet);
						}
						else if (view.equals(VIEW_MODE_PUBLIC))
						{
							messages = getMessagesPublic(site, channel, null, true, state, portlet);
						}
					}
					else
					{
						messages = getMessages(channel, null, true, state, portlet);
					}
					
					sstate.setAttribute("messages", messages);
					messages = prepPage(sstate);
					sstate.setAttribute(STATE_MESSAGES, messages);

					menu_delete = false;
					for (int i = 0; i < messages.size(); i++)
					{
						AnnouncementWrapper message = (AnnouncementWrapper) messages.get(i);

						// If any message is allowed to be removed
						// Also check to see if the AnnouncementWrapper object thinks
						// that this message is editable from the default site.
						if (message.editable && channel.allowRemoveMessage(message))
						{
							menu_delete = true;
							break;
						}
					}

					menu_revise = false;
					for (int i = 0; i < messages.size(); i++)
					{
						// if any message is allowed to be edited
						if (channel.allowEditMessage(((Message) messages.get(i)).getId()))
						{
							menu_revise = true;
							break;
						}
					}
				}
				else
				// if the messages in this channel are not allow to be accessed
				{
					menu_new = channel.allowAddMessage();
					menu_revise = false;
					menu_delete = false;
				} // if-else
			}
			else
			// if the channel is not allowed to access
			{
				menu_new = false;
				menu_revise = false;
				menu_delete = false;
			}
		}
		catch (PermissionException error)
		{
			M_log.warn(this+".buildMainPanelContext ", error);
		}
		catch (IdUnusedException error)
		{
			if (AnnouncementService.allowAddChannel(channelId))
			{
				try
				{
					AnnouncementChannelEdit edit = AnnouncementService.addAnnouncementChannel(channelId);
					AnnouncementService.commitChannel(edit);
					channel = edit;
				}
				catch (IdUsedException err)
				{
					M_log.debug(this+".buildMainPanelContext ", err);
				}
				catch (IdInvalidException err)
				{
				}
				catch (PermissionException err)
				{
				}
				menu_new = channel.allowAddMessage();
				menu_revise = false;
				menu_delete = false;
			}
			else
			{
				menu_new = false;
				menu_revise = false;
				menu_delete = false;
			} // if-else
		} // try-catch
				
		// check the state status to decide which vm to render
		String statusName = state.getStatus();
		
		AnnouncementActionState.DisplayOptions displayOptions = state.getDisplayOptions();
		
		if(statusName=="showMetadata" && channel!=null)
		{
			String messageReference = state.getMessageReference();
			AnnouncementMessage message;
			try {
				message = channel.getAnnouncementMessage(this.getMessageIDFromReference(messageReference));
				menu_new = channel.allowAddMessage();
				menu_delete = channel.allowRemoveMessage(message);
				menu_revise = channel.allowEditMessage(message.getId());
			} catch (IdUnusedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PermissionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
				
		//Check for MOTD, if yes then is not ok to show permissions button
		if("MOTD".equals(placement.getTitle()) && (placement != null && placement.getId().contains("admin"))) {
			buildMenu(portlet, context, rundata, state, menu_new, menu_delete, menu_revise, this.isOkToShowMergeButton(statusName),
					false, this.isOkToShowOptionsButton(statusName), displayOptions);
		}
		else{
		buildMenu(portlet, context, rundata, state, menu_new, menu_delete, menu_revise, this.isOkToShowMergeButton(statusName),
				this.isOkToShowPermissionsButton(statusName), this.isOkToShowOptionsButton(statusName), displayOptions);
		}
			
		// added by zqian for toolbar
		context.put("allow_new", Boolean.valueOf(menu_new));
		context.put("allow_delete", Boolean.valueOf(menu_delete));
		context.put("allow_revise", Boolean.valueOf(menu_revise));

		if (statusName != null)
		{
			template = getTemplate(portlet, context, rundata, sstate, state, template);
		}

		if (channel != null)
		{
			// ********* for sorting *********
			if (channel.allowGetMessages() && isOkayToDisplayMessageMenu(state))
			{
				String currentSortedBy = state.getCurrentSortedBy();
				context.put("currentSortedBy", currentSortedBy);
				if (state.getCurrentSortAsc())
					context.put("currentSortAsc", "true");
				else
					context.put("currentSortAsc", "false");

				if (currentSortedBy != null && !currentSortedBy.equals(SORT_GROUPTITLE)
						&& !currentSortedBy.equals(SORT_GROUPDESCRIPTION))
				{
					// sort in announcement list view
					buildSortedContext(portlet, context, rundata, sstate);
				}

			} // if allowGetMessages()
		}

		context.put ("service", AnnouncementService.getInstance());
		context.put ("entityManager", EntityManager.getInstance());
		context.put("timeservice", TimeService.getInstance());
		
		// ********* for site column display ********

		context.put("isOnWorkspaceTab", (isOnWorkspaceTab() ? "true" : "false"));

		context.put("channel", channel);

		final Tool tool = ToolManager.getCurrentTool();
		final String toolId = tool.getId();
		context.put("toolId", toolId);

		if (channel != null)
		{
			// show all the groups in this channal that user has get message in
			Collection groups = channel.getGroupsAllowGetMessage();
			if (groups != null && groups.size() > 0)
			{
				//context.put("groups", groups);
				Collection sortedGroups = new Vector();

				for (Iterator i = new SortedIterator(groups.iterator(), new AnnouncementComparator(SORT_GROUPTITLE, true)); i.hasNext();)
					{
						sortedGroups.add(i.next());
					}
				context.put("groups", sortedGroups);
			}
		}

		if (sstate.getAttribute(STATE_SELECTED_VIEW) != null)
		{
			context.put("view", sstate.getAttribute(STATE_SELECTED_VIEW));
		}

		// inform the observing courier that we just updated the page...
		// if there are pending requests to do so they can be cleared
		justDelivered(sstate);

		return template;

	} // buildMainPanelContext

	public void buildSortedContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState sstate)
	{
		Vector drafts = new Vector();
		Vector nonDrafts = new Vector();
		Vector showMessagesList = new Vector();

		List messages = prepPage(sstate);
		for (int i = 0; i < messages.size(); i++)
		{
			final AnnouncementMessage m = (AnnouncementMessage) messages.get(i);

			if (m.getAnnouncementHeader().getDraft())
			{
				drafts.addElement(m);
			}
			else
			{
				nonDrafts.add(m);
			}
		}

		AnnouncementActionState state = (AnnouncementActionState) getState(portlet, rundata, AnnouncementActionState.class);

		SortedIterator sortedDraftIterator = new SortedIterator(drafts.iterator(), new AnnouncementComparator(state
				.getCurrentSortedBy(), state.getCurrentSortAsc()));
		SortedIterator sortedNonDraftIterator = new SortedIterator(nonDrafts.iterator(), new AnnouncementComparator(state
				.getCurrentSortedBy(), state.getCurrentSortAsc()));

		if (state.getCurrentSortAsc())
		{
			while (sortedDraftIterator.hasNext())
				showMessagesList.add((AnnouncementMessage) sortedDraftIterator.next());

			while (sortedNonDraftIterator.hasNext())
				showMessagesList.add((AnnouncementMessage) sortedNonDraftIterator.next());
		}
		else
		{
			while (sortedDraftIterator.hasNext())
				showMessagesList.add((AnnouncementMessage) sortedDraftIterator.next());

			while (sortedNonDraftIterator.hasNext())
				showMessagesList.add((AnnouncementMessage) sortedNonDraftIterator.next());
		}

		context.put("showMessagesList", showMessagesList.iterator());
		context.put("showMessagesList2", showMessagesList.iterator());
		context.put("messageListVector", showMessagesList);

		context.put("totalPageNumber", sstate.getAttribute(STATE_TOTAL_PAGENUMBER));
		context.put("formPageNumber", FORM_PAGE_NUMBER);
		context.put("prev_page_exists", sstate.getAttribute(STATE_PREV_PAGE_EXISTS));
		context.put("next_page_exists", sstate.getAttribute(STATE_NEXT_PAGE_EXISTS));
		context.put("current_page", sstate.getAttribute(STATE_CURRENT_PAGE));
		pagingInfoToContext(sstate, context);

		// SAK-9116: to use Viewing {0} - {1} of {2} items
		// find the position of the message that is the top first on the page
		if ((sstate.getAttribute(STATE_TOP_PAGE_MESSAGE) != null) && (sstate.getAttribute(STATE_PAGESIZE) != null))
		{
			int topMsgPos = ((Integer) sstate.getAttribute(STATE_TOP_PAGE_MESSAGE)).intValue() + 1;
			int btmMsgPos = topMsgPos + ((Integer) sstate.getAttribute(STATE_PAGESIZE)).intValue() - 1;
			int allMsgNumber = btmMsgPos;
			if (sstate.getAttribute(STATE_NUM_MESSAGES) != null)
			{
				allMsgNumber = ((Integer) sstate.getAttribute(STATE_NUM_MESSAGES)).intValue();
				if (btmMsgPos > allMsgNumber) btmMsgPos = allMsgNumber;
			}

			String [] viewValues = { (new Integer(topMsgPos)).toString(),
									(new Integer(btmMsgPos)).toString(),
									(new Integer(allMsgNumber)).toString() };

			context.put("announcementItemRangeArray", viewValues);
		}
		// context.put("jsfutil", JsfUtil.this);

	} // buildSortedContext

	public String getTemplate(VelocityPortlet portlet, Context context, RunData rundata, SessionState sstate,
			AnnouncementActionState state, String value)
	{
		String template = value;
		String statusName = state.getStatus();

		if (statusName.equals(DELETE_ANNOUNCEMENT_STATUS))
		{
			template = buildDeleteAnnouncementContext(portlet, context, rundata, state);
		}
		else if (statusName.equals("showMetadata"))
		{
			template = buildShowMetadataContext(portlet, context, rundata, state, sstate);
		}
		else if ((statusName.equals("goToReviseAnnouncement")) || (statusName.equals("backToReviseAnnouncement"))
				|| (statusName.equals("new")) || (statusName.equals("stayAtRevise")))
		{
			template = buildReviseAnnouncementContext(portlet, context, rundata, state, sstate);
		}
		else if (statusName.equals("revisePreviw"))
		{
			template = buildPreviewContext(portlet, context, rundata, state);
		}
		else if ((statusName.equals(CANCEL_STATUS)) || (statusName.equals(POST_STATUS)) || (statusName.equals("FinishDeleting")))
		{
			template = buildCancelContext(portlet, context, rundata, state);
		}
		else if (statusName.equals("noSelectedForDeletion") || (statusName.equals(NOT_SELECTED_FOR_REVISE_STATUS)))
		{
			// addAlert(sstate, "You have to select the announcement first!");
			addAlert(sstate, rb.getString("java.alert.youhave"));
		}
		else if (statusName.equals("moreThanOneSelectedForRevise"))
		{
			// addAlert(sstate, "Please choose only one announcement at a time to revise!");
			addAlert(sstate, rb.getString("java.alert.pleasechoose"));
		}
		else if (statusName.equals("noPermissionToRevise"))
		{
			// addAlert(sstate, "You don't have permission to revise this announcement!");
			addAlert(sstate, rb.getString("java.alert.youdont"));
		}
		else if (statusName.equals(MERGE_STATUS))
		{
			template = buildMergeContext(portlet, context, rundata, state, sstate);
		}
		else if (statusName.equals(OPTIONS_STATUS))
		{
			template = buildOptionsPanelContext(portlet, context, rundata, sstate);
		}
		return template;

	} // getTemplate

	/**
	 * Setup for the options panel.
	 */
	public String buildOptionsPanelContext(VelocityPortlet portlet, Context context, RunData runData, SessionState state)
	{
		// retrieve the state from state object
		AnnouncementActionState actionState = (AnnouncementActionState) getState(portlet, runData, AnnouncementActionState.class);
		context.put(CONTEXT_VAR_DISPLAY_OPTIONS, actionState.getDisplayOptions());

		String channelId = actionState.getChannelId();
		Reference channelRef = EntityManager.newReference(channelId);
		context.put("description", rb.getString("java.setting")// "Setting options for Announcements in worksite "
				+ SiteService.getSiteDisplay(channelRef.getContext()));
				
		Reference anncRef = AnnouncementService.getAnnouncementReference(ToolManager.getCurrentPlacement().getContext());
		List aliasList =	AliasService.getAliases( anncRef.getReference() );
		if ( ! aliasList.isEmpty() )
		{
			String alias[] = ((Alias)aliasList.get(0)).getId().split("\\.");
			context.put("rssAlias", alias[0] );
		}

		// Add Announcement RSS URL
	 
		context.put("rssUrl", AnnouncementService.getRssUrl( anncRef ) );

		// pick the "-customize" template based on the standard template name
		String template = (String) getContext(runData).get("template");
		return template + "-customize";

	} // buildOptionsPanelContext

	/**
	 * Returns true if it is okay to display the revise/delete/etc. menu buttons.
	 */
	private boolean isOkayToDisplayMessageMenu(AnnouncementActionState state)
	{
		String selectedMessageReference = state.getMessageReference();

		// If we're in the list state or if there is otherwise no message selected,
		// return true.
		if (state.getStatus() == null || state.getStatus().equals(CANCEL_STATUS) || selectedMessageReference == null
				|| selectedMessageReference.length() == 0)
		{
			return true;
		}
		if (state.getStatus().equals(MERGE_STATUS))
		{
			return false;
		}
		else
		{
			String channelID = getChannelIdFromReference(selectedMessageReference);

			boolean selectedMessageMatchesDefaultChannel = state.getChannelId().equals(channelID);

			return selectedMessageMatchesDefaultChannel;
		}
	}

	/**
	 * Returns true if it is ok to show the options button in the toolbar.
	 */
	private boolean isOkToShowOptionsButton(String statusName)
	{
		if (SiteService.allowUpdateSite(ToolManager.getCurrentPlacement().getContext()) && !isOnWorkspaceTab())
		{
			return (statusName == null || statusName.equals(CANCEL_STATUS) || statusName.equals(POST_STATUS)
					|| statusName.equals(DELETE_ANNOUNCEMENT_STATUS) || statusName.equals(FINISH_DELETING_STATUS)
					|| statusName.equals("noSelectedForDeletion") || statusName.equals(NOT_SELECTED_FOR_REVISE_STATUS));
		}
		else
		{
			return false;
		}
	}

	/*
	 * what i've done to make this tool automaticlly updated includes some corresponding imports in buildMail, tell observer just the page is just refreshed in the do() functions related to show the list, enable the obeserver in other do() functions
	 * related to not show the list, disable the obeserver in the do(), define the session sstate object, and protlet. add initState add updateObservationOfChannel() add state attribute STATE_CHANNEL_REF
	 */

	/**
	 * Returns true if it is okay to show the merge button in the menu.
	 */
	private boolean isOkToShowMergeButton(String statusName)
	{
		String displayMerge = ServerConfigurationService.getString("announcement.merge.display", "1");
		
		if(displayMerge != null && !displayMerge.equals("1"))
			return false;
		
		if (SiteService.allowUpdateSite(ToolManager.getCurrentPlacement().getContext()) && !isOnWorkspaceTab())
		{
			return (statusName == null || statusName.equals(CANCEL_STATUS) || statusName.equals(POST_STATUS)
					|| statusName.equals(DELETE_ANNOUNCEMENT_STATUS) || statusName.equals(FINISH_DELETING_STATUS)
					|| statusName.equals("noSelectedForDeletion") || statusName.equals(NOT_SELECTED_FOR_REVISE_STATUS));
		}
		else
		{
			return false;
		}
	}

	/**
	 * Returns true if it is okay to show the permissions button in the menu.
	 */
	private boolean isOkToShowPermissionsButton(String statusName)
	{
		if (SiteService.allowUpdateSite(ToolManager.getCurrentPlacement().getContext()))
		{
			return (statusName == null || statusName.equals(CANCEL_STATUS) || statusName.equals(POST_STATUS)
					|| statusName.equals(DELETE_ANNOUNCEMENT_STATUS) || statusName.equals(FINISH_DELETING_STATUS)
					|| statusName.equals("noSelectedForDeletion") || statusName.equals(NOT_SELECTED_FOR_REVISE_STATUS));
		}
		else
		{
			return false;
		}
	}

	/**
	 * This should be the single point for getting lists of announcements in this action. It collects together all the announcements and wraps the real announcements in a decorator object that adds extra properties for use in the VM template.
	 * 
	 * @throws PermissionException
	 */
	private List getMessages(AnnouncementChannel defaultChannel, Filter filter, boolean ascending, AnnouncementActionState state,
			VelocityPortlet portlet) throws PermissionException
	{
		List messageList = new ArrayList();

		MergedList mergedAnnouncementList = new MergedList();

		// TODO - MERGE FIX
		String[] channelArrayFromConfigParameterValue = null;	
		
		// Figure out the list of channel references that we'll be using.
		// If we're on the workspace tab, we get everything.
		// Don't do this if we're the super-user, since we'd be
		// overwhelmed.
		
		//loading merged announcement channel reference, for Synoptic Announcement Tool-SAK-5865
		if (SYNOPTIC_ANNOUNCEMENT_TOOL.equals(ToolManager.getCurrentTool().getId())){

			Site site = null;
			String initMergeList=null;
			try {
				site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
				ToolConfiguration tc=site.getToolForCommonId("sakai.announcements");
				if (tc!=null){
					//Properties ps= tc.getPlacementConfig();
					initMergeList = tc.getPlacementConfig().getProperty(PORTLET_CONFIG_PARM_MERGED_CHANNELS);	
				}

				if (isOnWorkspaceTab() && !SecurityService.isSuperUser())
				{
					channelArrayFromConfigParameterValue = mergedAnnouncementList
					.getAllPermittedChannels(new AnnouncementChannelReferenceMaker());
				}
				else
				{
					channelArrayFromConfigParameterValue = mergedAnnouncementList
					.getChannelReferenceArrayFromDelimitedString(state.getChannelId(), initMergeList);

				}

			} catch (IdUnusedException e1) {
				// TODO Auto-generated catch block
			}			
		}
		else {
			if (isOnWorkspaceTab() && !SecurityService.isSuperUser())
			{
				channelArrayFromConfigParameterValue = mergedAnnouncementList
						.getAllPermittedChannels(new AnnouncementChannelReferenceMaker());
			}
			else
			{
				channelArrayFromConfigParameterValue = mergedAnnouncementList
						.getChannelReferenceArrayFromDelimitedString(state.getChannelId(), portlet.getPortletConfig().getInitParameter(
								getPortletConfigParameterNameForLoadOnly(portlet)));
				
			}

		}			
		
		mergedAnnouncementList
				.loadChannelsFromDelimitedString(isOnWorkspaceTab(), new MergedListEntryProviderFixedListWrapper(
						new EntryProvider(), state.getChannelId(), channelArrayFromConfigParameterValue,
						new AnnouncementReferenceToChannelConverter()), StringUtil.trimToZero(SessionManager
						.getCurrentSessionUserId()), channelArrayFromConfigParameterValue, SecurityService.isSuperUser(),
						ToolManager.getCurrentPlacement().getContext());

		Iterator channelsIt = mergedAnnouncementList.iterator();

		while (channelsIt.hasNext())
		{
			MergedList.MergedEntry curEntry = (MergedList.MergedEntry) channelsIt.next();

			// If this entry should not be merged, skip to the next one.
			if (!curEntry.isMerged())
			{
				continue;
			}

			AnnouncementChannel curChannel = null;
			try
			{
				curChannel = (AnnouncementChannel) AnnouncementService.getChannel(curEntry.getReference());
			}
			catch (IdUnusedException e)
			{
				M_log.debug(this + ".getMessages()", e);
			}
			catch (PermissionException e)
			{
				M_log.debug(this + ".getMessages()", e);
			}

			if (curChannel != null)
			{
				if (AnnouncementService.allowGetChannel(curChannel.getReference()))
				{
					messageList.addAll(AnnouncementWrapper.wrapList(curChannel.getMessages(filter, ascending), curChannel,
							defaultChannel, state.getDisplayOptions()));
				}
			}
		}

		// Do an overall sort. We couldn't do this earlier since each merged channel
		Collections.sort(messageList);

		// Reverse if we're not ascending.
		if (!ascending)
		{
			Collections.reverse(messageList);
		}

		// Apply any necessary list truncation.
		messageList = getViewableMessages(messageList, ToolManager.getCurrentPlacement().getContext());
		
		messageList = trimListToMaxNumberOfAnnouncements(messageList, state.getDisplayOptions());

		return messageList;
	}

	/**
	 * This get the whole list of announcement, find their groups, and list them based on group attribute
	 * 
	 * @throws PermissionException
	 */
	private List getMessagesByGroups(Site site, AnnouncementChannel defaultChannel, Filter filter, boolean ascending,
			AnnouncementActionState state, VelocityPortlet portlet) throws PermissionException
	{
		List messageList = getMessages(defaultChannel, filter, ascending, state, portlet);
		List rv = new Vector();

		for (int i = 0; i < messageList.size(); i++)
		{
			AnnouncementWrapper aMessage = (AnnouncementWrapper) messageList.get(i);
			String pubview = aMessage.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW);
			if (pubview != null && Boolean.valueOf(pubview).booleanValue())
			{
				// public announcements
				aMessage.setRange(rb.getString("range.public"));
				//rv.add(new AnnouncementWrapper(aMessage));
			}
			else
			{
				if (aMessage.getAnnouncementHeader().getAccess().equals(MessageHeader.MessageAccess.CHANNEL))
				{
					// site announcements
					aMessage.setRange(rb.getString("range.allgroups"));
					//rv.add(new AnnouncementWrapper(aMessage));
				}
				else
				{
					//for (Iterator k = aMessage.getAnnouncementHeader().getGroups().iterator(); k.hasNext();)
					{
						// announcement by group
						//AnnouncementWrapper m = new AnnouncementWrapper(aMessage);
						//m.setRange(site.getGroup((String) k.next()).getTitle());
						//rv.add(m);
						rv.add(aMessage);
					}
				}

			}
		}

		return rv;

	} // getMessagesByGroups

	/**
	 * This get the whole list of announcement, find their groups, and list them based on group attribute
	 * 
	 * @throws PermissionException
	 */
	private List getMessagesPublic(Site site, AnnouncementChannel defaultChannel, Filter filter, boolean ascending,
			AnnouncementActionState state, VelocityPortlet portlet) throws PermissionException
	{
		List messageList = getMessages(defaultChannel, filter, ascending, state, portlet);
		List rv = new Vector();

		for (int i = 0; i < messageList.size(); i++)
		{
			AnnouncementMessage aMessage = (AnnouncementMessage) messageList.get(i);
			String pubview = aMessage.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW);
			if (pubview != null && Boolean.valueOf(pubview).booleanValue())
			{
				// public announcements
				rv.add(aMessage);
			}
		}

		return rv;

	} // getMessagesPublic

	/**
	 * This will limit the maximum number of announcements that is shown.
	 */
	private List trimListToMaxNumberOfAnnouncements(List messageList, AnnouncementActionState.DisplayOptions options)
	{
		if (options !=null && options.isEnforceNumberOfAnnouncementsLimit())
		{
			int numberOfAnnouncements = options.getNumberOfAnnouncements();
			ArrayList destList = new ArrayList();

			// We need to go backwards through the list, limiting it to the number
			// of announcements that we're allowed to display.
			for (int i = messageList.size() - 1, curAnnouncementCount = 0; i >= 0 && curAnnouncementCount < numberOfAnnouncements; i--)
			{
				AnnouncementMessage message = (AnnouncementMessage) messageList.get(i);

				destList.add(message);
					
				curAnnouncementCount++;
			}

			return destList;
		}
		else
		{
			return messageList;
		}
	}

	/**
	 * Filters out messages based on hidden property and release/retract dates.
	 * Only use hidden if in synoptic tool.
	 * 
	 * @param messageList
	 * 			The unfiltered message list
	 * 
	 * @return
	 * 			List of messsage this user is able to view
	 */
	private List getViewableMessages(List messageList, String siteId) {
		final List filteredMessages = new ArrayList();
		
		for (Iterator messIter = messageList.iterator(); messIter.hasNext();) {
			final AnnouncementMessage message = (AnnouncementMessage) messIter.next();
			
			// for synoptic tool or if in MyWorkspace, 
			// only display if not hidden AND
			// between release and retract dates (if set)
			if (isSynopticTool() || isOnWorkspaceTab()) {
				if (!isHidden(message) && AnnouncementService.isMessageViewable(message)) {
					filteredMessages.add(message);
				}
			}
			else {
				// on main page, if hidden but user has hidden permission
				// then display. Otherwise, if between release/retract dates
				// or they are not set
				if (isHidden(message)) {
					if (canViewHidden(message, siteId)) {
						filteredMessages.add(message);
					}
				}
				else if (AnnouncementService.isMessageViewable(message)) {
					filteredMessages.add(message);
				}
				else if (canViewHidden(message, siteId)) {
					filteredMessages.add(message);
				}
			}
		}
		
		return filteredMessages;
	}
	
	/**
	 * Returns true if the tool with the id passed in exists in the
	 * current site.
	 * 
	 * @param toolId
	 * 			The tool id to search for.
	 * 
	 * @return
	 * 			TRUE if tool exists, FALSE otherwise.
	 */
	private boolean isSynopticTool() {
		String curToolId = ToolManager.getCurrentTool().getId();

		if (SYNOPTIC_ANNOUNCEMENT_TOOL.equals(curToolId)) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Determines if use has draft (UI: hidden) permission or site.upd
	 * If so, they will be able to view messages that are hidden
	 */
	private boolean canViewHidden(AnnouncementMessage msg, String siteId) 
	{
		final boolean b = SecurityService.unlock(AnnouncementService.SECURE_READ_DRAFT, msg.getReference())
							 || SecurityService.unlock(UPDATE_PERMISSIONS, "/site/"+ siteId); 
		return b;
	}
	
	/**
	 * Determine if message is hidden (draft property set)
	 */
	private boolean isHidden(AnnouncementMessage message) 
	{
		return 	message.getHeader().getDraft();
	}

	/**
	 * Build the context for preview an attachment
	 */
	protected String buildPreviewContext(VelocityPortlet portlet, Context context, RunData rundata, AnnouncementActionState state)
	{
		context.put("conService", contentHostingService);

		// to get the content Type Image Service
		context.put("contentTypeImageService", ContentTypeImageService.getInstance());

		final String subject = state.getTempSubject();
		final String body = state.getTempBody();
		final Time tempReleaseDate = state.getTempReleaseDate();
		final Time tempRetractDate = state.getTempRetractDate();
		String annTo=state.getTempAnnounceTo();
		context.put("subject", subject);
		context.put("body", body);
		context.put("user", UserDirectoryService.getCurrentUser());
		context.put("newAnn", (state.getIsNewAnnouncement()) ? "true" : "else");
		context.put("annTo", annTo);
	
		
		String channelId=state.getChannelId();
		Collection annToGroups=state.getTempAnnounceToGroups();		
		String allGroupString="";
		
		
		if(annToGroups!=null){
		Site site=null;
		int count=0;
		try {
			site = SiteService.getSite(EntityManager.newReference(channelId).getContext());		
			for (Iterator i = annToGroups.iterator(); i.hasNext();)
			{
				Group aGroup = site.getGroup((String) i.next());
				if (aGroup != null)
				{
					count++;
					if (count > 1)
					{
						allGroupString = allGroupString.concat(", ").concat(aGroup.getTitle());
					}
					else
					{
						allGroupString = aGroup.getTitle();
					}
				}
			}
			context.put("annToGroups", allGroupString);
			
		} catch (IdUnusedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		}
		
		
		// Set date
		AnnouncementMessageEdit edit = state.getEdit();

		if (tempReleaseDate != null)
		{
			context.put("date", tempReleaseDate);
		}
		else
		{
			Time releaseDate = null;
			try {
				releaseDate = edit.getProperties().getTimeProperty(AnnouncementService.RELEASE_DATE);
				context.put("date", releaseDate);
			} 
			catch (Exception e) {
				// not set so set switch appropriately
				context.put("date", TimeService.newTime());
			} 
		}
		
		List attachments = state.getAttachments();
		context.put("attachments", attachments);

		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState sstate = ((JetspeedRunData) rundata).getPortletSessionState(peid);

		// output the public view options
		boolean pubview = Boolean.valueOf((String) sstate.getAttribute(SSTATE_PUBLICVIEW_VALUE)).booleanValue();
		if (pubview)
			context.put("IsPubView", rb.getString("java.yes"));// "Yes");
		else
			context.put("IsPubView", rb.getString("java.no"));// "No");

		if (edit == null)
			context.put(HIDDEN, false);
		else
		{
			final boolean hidden = edit.getHeader().getDraft();
			context.put(HIDDEN, hidden);
		}

		// output the notification options
		String notification = (String) sstate.getAttribute(SSTATE_NOTI_VALUE);;
		if ("r".equals(notification))
		{
			context.put("noti", rb.getString("java.NOTI_REQUIRED"));
		}
		else if ("n".equals(notification))
		{
			context.put("noti", rb.getString("java.NOTI_NONE"));
		}
		else
		{
			context.put("noti", rb.getString("java.NOTI_OPTIONAL"));
		}				

		// pick the "browse" template based on the standard template name
		String template = (String) getContext(rundata).get("template");
		return template + "-preview";

	} // buildPreviewContext

	/**
	 * Build the context for revising the announcement
	 */
	protected String buildReviseAnnouncementContext(VelocityPortlet portlet, Context context, RunData rundata,
			AnnouncementActionState state, SessionState sstate)
	{
		context.put("service", contentHostingService);

		// to get the content Type Image Service
		context.put("contentTypeImageService", ContentTypeImageService.getInstance());
		context.put("dateFormat", getDateFormatString());

		final String channelId = state.getChannelId();

		// find the channel and channel information through the service
		AnnouncementChannel channel = null;
		try
		{
			if (channelId != null && AnnouncementService.allowGetChannel(channelId))
			{
				// get the channel name throught announcement service API
				channel = AnnouncementService.getAnnouncementChannel(channelId);

				context.put("allowAddChannelMessage", new Boolean(channel.allowAddChannelMessage()));

				String announceTo = state.getTempAnnounceTo();
				if (announceTo != null && announceTo.length() != 0)
				{
					context.put("announceTo", announceTo);
				}
				else
				{
					if (state.getIsNewAnnouncement())
					{
						if (channel.allowAddChannelMessage())
						{
							// default to make site selection
							context.put("announceTo", "site");
						}
						else if (channel.getGroupsAllowAddMessage().size() > 0)
						{
							// to group otherwise
							context.put("announceTo", "groups");
						}
					}
				}
				AnnouncementMessageEdit edit = state.getEdit();

				/*This part is a repetition and not needed
				  
				 // Get/set release information
				Time releaseDate = null;
				try 
				{
					releaseDate = edit.getProperties().getTimeProperty(AnnouncementService.RELEASE_DATE);					
					context.put("useReleaseDate", Boolean.valueOf(true));
				} 
				catch (Exception e) 
				{
					// Set inital release date to creation date
					releaseDate = edit.getHeader().getDate();
				} 

				context.put(AnnouncementService.RELEASE_DATE, releaseDate);

				// Get/set retract information
				Time retractDate = null;
				try 
				{
					retractDate = edit.getProperties().getTimeProperty(AnnouncementService.RETRACT_DATE);
					context.put("useRetractDate", Boolean.valueOf(true));
				} 
				catch (Exception e) 
				{
					// Set inital retract date to approx 2 months from today
					final long futureTimeLong = TimeService.newTime().getTime() + MILLISECONDS_IN_DAY * FUTURE_DAYS;			
					retractDate = TimeService.newTime(futureTimeLong);
				}

				context.put(AnnouncementService.RETRACT_DATE, retractDate);
				*/

				// group list which user can remove message from
				// TODO: this is almost right (see chef_announcements-revise.vm)... ideally, we would let the check groups that they can add to,
				// and uncheck groups they can remove from... only matters if the user does not have both add and remove -ggolden
				final boolean own = edit == null ? true : edit.getHeader().getFrom().getId().equals(SessionManager.getCurrentSessionUserId());
				Collection groups = channel.getGroupsAllowRemoveMessage(own);
				context.put("allowedRemoveGroups", groups);
				
				// group list which user can add message to
				groups = channel.getGroupsAllowAddMessage();

				// add to these any groups that the message already has
				if (edit != null)
				{
					final Collection otherGroups = edit.getHeader().getGroupObjects();
					for (Iterator i = otherGroups.iterator(); i.hasNext();)
					{
						Group g = (Group) i.next();
						
						if (!groups.contains(g))
						{
							groups.add(g);
						}
					}					
				}

				if (groups.size() > 0)
				{
					/*String sort = (String) sstate.getAttribute(STATE_CURRENT_SORTED_BY);
					boolean asc = sstate.getAttribute(STATE_CURRENT_SORT_ASC) != null ? ((Boolean) sstate
							.getAttribute(STATE_CURRENT_SORT_ASC)).booleanValue() : true;
					if (sort == null || (!sort.equals(SORT_GROUPTITLE) && !sort.equals(SORT_GROUPDESCRIPTION)))
					{
						sort = SORT_GROUPTITLE;
						sstate.setAttribute(STATE_CURRENT_SORTED_BY, sort);
						state.setCurrentSortedBy(sort);
						//state.setCurrentSortAsc(Boolean.FALSE.booleanValue());
						state.setCurrentSortAsc(state.getCurrentSortAsc());
					}*/
					Collection sortedGroups = new Vector();
					//for (Iterator i = new SortedIterator(groups.iterator(), new AnnouncementComparator(sort, asc)); i.hasNext();)
					for (Iterator i = new SortedIterator(groups.iterator(), new AnnouncementComparator(SORT_GROUPTITLE, true)); i.hasNext();)
					{
						sortedGroups.add(i.next());
					}
					context.put("groups", sortedGroups);
				}
			}
		}
		catch (Exception ignore)
		{
		}

		List attachments = state.getAttachments();

		// if this a new annoucement, get the subject and body from temparory record
		if (state.getStatus().equals("new"))
		{
			context.put("new", "true");
			context.put("tempSubject", state.getTempSubject());
			context.put("tempBody", state.getTempBody());

			// default pubview
			context.put("pubviewset", ((sstate.getAttribute(STATE_CHANNEL_PUBVIEW) ==  null) ? Boolean.FALSE : Boolean.TRUE));

			// output the sstate saved public view options
			final boolean pubview = Boolean.valueOf((String) sstate.getAttribute(SSTATE_PUBLICVIEW_VALUE)).booleanValue();
			if (pubview)
				context.put("pubview", Boolean.TRUE);
			else
				context.put("pubview", Boolean.FALSE);

			// Set inital release date to today
			final Time currentTime = TimeService.newTime();
			context.put(AnnouncementService.RELEASE_DATE, currentTime);
			
			// Set inital retract date to 60 days from now
			final long futureTimeLong = currentTime.getTime() + MILLISECONDS_IN_DAY * FUTURE_DAYS;			
			final Time futureTime = TimeService.newTime(futureTimeLong);

			context.put(AnnouncementService.RETRACT_DATE, futureTime);
			
			// output the notification options
			String notification = (String) sstate.getAttribute(SSTATE_NOTI_VALUE);
			// "r", "o" or "n"
			context.put("noti", notification);

		}
		// if this is an existing one
		else if (state.getStatus().equals("goToReviseAnnouncement"))
		{
			// to determine if release/retract dates have been specified
			// so correct checkbox is selected
			boolean specify = false;
			
			// get the message object through service
			context.put("new", "false");
			// get the message object through service
			// AnnouncementMessage message = channel.getAnnouncementMessage( messageId );
			// context.put("message", message);

			AnnouncementMessageEdit edit = state.getEdit();
			context.put("message", edit);

			// find out about pubview
			context.put("pubviewset", ((sstate.getAttribute(STATE_CHANNEL_PUBVIEW) ==  null) ? Boolean.FALSE : Boolean.TRUE));
			context.put("pubview", Boolean.valueOf(edit.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW) != null));

			// Get/set release information
			Time releaseDate = null;
			try {
				releaseDate = edit.getProperties().getTimeProperty(AnnouncementService.RELEASE_DATE);
				
				context.put("useReleaseDate", Boolean.valueOf(true));
				specify = true;
			} 
			catch (Exception e) 
			{
				// Set inital release date to creation date
				releaseDate = edit.getHeader().getDate();
			} 

			context.put(AnnouncementService.RELEASE_DATE, releaseDate);

			// Get/set retract information
			Time retractDate = null;
			try 
			{
				retractDate = edit.getProperties().getTimeProperty(AnnouncementService.RETRACT_DATE);
				
				context.put("useRetractDate", Boolean.valueOf(true));
				specify = true;
			} 
			catch (Exception e) 
			{
				// Set inital retract date to approx 2 months from today
				final long futureTimeLong = TimeService.newTime().getTime() + MILLISECONDS_IN_DAY * FUTURE_DAYS;			
				retractDate = TimeService.newTime(futureTimeLong);
			}

			context.put(AnnouncementService.RETRACT_DATE, retractDate);

			context.put(SPECIFY_DATES, specify);
			context.put(HIDDEN, edit.getHeader().getDraft());
			// there is no chance to get the notification setting at this point
		}
		else
		// if state is "backToRevise"
		{
			context.put("new", "true");
			context.put("tempSubject", state.getTempSubject());
			context.put("tempBody", state.getTempBody());
			
			// Get/set release information
			Time releaseDate = null;
			try 
			{
				//releaseDate = edit.getProperties().getTimeProperty(RELEASE_DATE);					
				if (state.getTempReleaseDate()!=null)
				{
				releaseDate= state.getTempReleaseDate();
				context.put("useReleaseDate", Boolean.valueOf(true));
				context.put(SPECIFY_DATES, true);
				}
				else
				{
					releaseDate = TimeService.newTime();	
				context.put("useReleaseDate", Boolean.valueOf(false));
				}
			} 
			catch (Exception e) 
			{
				// Set inital release date to creation date
				releaseDate = TimeService.newTime();
			} 

			context.put(AnnouncementService.RELEASE_DATE, releaseDate);

			// Get/set retract information
			Time retractDate = null;
			try 
			{
				if (state.getTempRetractDate()!=null)
				{
				retractDate= state.getTempRetractDate();
				context.put("useRetractDate", Boolean.valueOf(true));
				context.put(SPECIFY_DATES, true);
				}
				else
				{
					// Set inital retract date to 60 days from now				
				final long futureTimeLong = TimeService.newTime().getTime() + MILLISECONDS_IN_DAY * FUTURE_DAYS;			
				retractDate = TimeService.newTime(futureTimeLong);
				context.put("useRetractDate", Boolean.valueOf(false));
				}
			} 
			catch (Exception e) 
			{
				// Set inital retract date to approx 2 months from today
				final long futureTimeLong = TimeService.newTime().getTime() + MILLISECONDS_IN_DAY * FUTURE_DAYS;			
				retractDate = TimeService.newTime(futureTimeLong);
			}

			context.put(AnnouncementService.RETRACT_DATE, retractDate);
			
			if(state.getTempHidden()!=null)
			{
				context.put(HIDDEN,state.getTempHidden());
			} 
			

			final boolean pubview = Boolean.valueOf((String) sstate.getAttribute(SSTATE_PUBLICVIEW_VALUE)).booleanValue();
			if (pubview)
				context.put("pubview", Boolean.TRUE);
			else
				context.put("pubview", Boolean.FALSE);

			// output the notification options
			String notification = (String) sstate.getAttribute(SSTATE_NOTI_VALUE);;
			// "r", "o" or "n"
			context.put("noti", notification);
		}

		context.put("attachments", attachments);
		context.put("newAnn", (state.getIsNewAnnouncement()) ? "true" : "else");

		context.put("announceToGroups", state.getTempAnnounceToGroups());

		context.put("publicDisable", sstate.getAttribute(PUBLIC_DISPLAY_DISABLE_BOOLEAN));

		String template = (String) getContext(rundata).get("template");
		return template + "-revise";

	} // buildReviseAnnouncementContext

	/**
	 * Build the context for viewing announcement content
	 */
	protected String buildShowMetadataContext(VelocityPortlet portlet, Context context, RunData rundata,
			AnnouncementActionState state, SessionState sstate)
	{
		context.put("conService", contentHostingService);

		// to get the content Type Image Service
		context.put("contentTypeImageService", ContentTypeImageService.getInstance());

		// get the channel and message id information from state object
		String messageReference = state.getMessageReference();

		// get the message object through service
		try
		{
			// get the channel id throught announcement service
			AnnouncementChannel channel = AnnouncementService.getAnnouncementChannel(this
					.getChannelIdFromReference(messageReference));

			// get the message object through service
			AnnouncementMessage message = channel.getAnnouncementMessage(this.getMessageIDFromReference(messageReference));

			// put release date into context if set. otherwise, put current date
			try {
				Time releaseDate = message.getProperties().getTimeProperty(AnnouncementService.RELEASE_DATE);
				
				context.put("date", releaseDate);
			} 
			catch (Exception e) {
				// no release date, put in current time
				context.put("date", TimeService.newTime());
			}
			
			context.put("message", message);

			// find out about pubview
			context.put("pubviewset", ((sstate.getAttribute(STATE_CHANNEL_PUBVIEW) ==  null) ? Boolean.FALSE : Boolean.TRUE));
			context.put("pubview", Boolean.valueOf(message.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW) != null));

			// show all the groups in this channal that user has get message in
			Collection groups = channel.getGroupsAllowGetMessage();
			if (groups != null)
			{
				context.put("range", getAnnouncementRange(message));
			}

			boolean menu_new = channel.allowAddMessage();
			boolean menu_delete = channel.allowRemoveMessage(message);
			boolean menu_revise = channel.allowEditMessage(message.getId());

			// Check to see if we can display an enabled menu.
			// We're currently not allowing modification of
			// announcements if they are not from this site.
			if (!this.isOkayToDisplayMessageMenu(state))
			{
				menu_new = menu_delete = menu_revise = false;
			}
			
			try // Check to see if this is an announcement associated with an assignment
			{
				Iterator i = assignmentService.getAssignmentsForContext(channel.getContext());
				String assignmentId = "";
				
				while (i.hasNext()) // if i is empty, none of this code is ran
				{
					Assignment a = (Assignment) i.next();
					
					// This is the only link we have to know if the announcement is associated with an assignment
					String announcementCheck = a.getProperties().getProperty("CHEF:assignment_opendate_announcement_message_id");
					
					// Get the titles of both for comparison
					String title = a.getTitle();
					String title2 = message.getAnnouncementHeader().getSubject();
					
					// Lots of checks to make absolutely sure this is the assignment we are looking for
					if (announcementCheck!="" && announcementCheck!=null && announcementCheck.equals(message.getId()) && title2.endsWith(title))
					{
						assignmentId = a.getId();
						context.put("assignment", a);
						if (assignmentId != null && assignmentId.length() > 0)
						{
							String assignmentContext = a.getContext(); // assignment context
							boolean allowReadAssignment = assignmentService.allowGetAssignment(assignmentContext); // check for read permission
							if (allowReadAssignment && a.getOpenTime().before(TimeService.newTime())) // this checks if we want to display an assignment link
							{
								Site site = SiteService.getSite(assignmentContext); // site id
								ToolConfiguration fromTool = site.getToolForCommonId("sakai.assignment.grades");
								boolean allowAddAssignment = assignmentService.allowAddAssignment(assignmentContext); // this checks for the asn.new permission and determines the url we present the user
								
								// Two different urls to be rendered depending on the user's permission
								if (allowAddAssignment)
								{
									context.put("assignmenturl", ServerConfigurationService.getPortalUrl() + "/directtool/" + fromTool.getId() + "?assignmentId=" + a.getReference() + "&panel=Main&sakai_action=doView_assignment");
								}
								else
								{
									context.put("assignmenturl", ServerConfigurationService.getPortalUrl() + "/directtool/" + fromTool.getId() + "?assignmentReference=" + a.getReference() + "&panel=Main&sakai_action=doView_submission");
								}
							}
						}
						break; // no need to keep iterating if we find the match
					}
				}
			}
			catch (Exception e)
			{
				// no assignment associated
			}

			// check the state status to decide which vm to render
			String statusName = state.getStatus();

			AnnouncementActionState.DisplayOptions displayOptions = state.getDisplayOptions();

			buildMenu(portlet, context, rundata, state, menu_new, menu_delete, menu_revise, this.isOkToShowMergeButton(statusName),
					this.isOkToShowPermissionsButton(statusName), this.isOkToShowOptionsButton(statusName), displayOptions);

			context.put("allow_new", Boolean.valueOf(menu_new));
			context.put("allow_delete", Boolean.valueOf(menu_delete));
			context.put("allow_revise", Boolean.valueOf(menu_revise));

			// navigation bar display control
			List msgs = (List) sstate.getAttribute(STATE_MESSAGES);
			if (msgs != null)
			{
				for (int i = 0; i < msgs.size(); i++)
				{
					if (((AnnouncementWrapper) msgs.get(i)).getId().equals(message.getId()))
					{
						boolean goPT = false;
						boolean goNT = false;
						if ((i - 1) >= 0)
						{
							goPT = true;
							context.put("prevMsg", msgs.get(i - 1));
						}
						if ((i + 1) < msgs.size())
						{
							goNT = true;
							context.put("nextMsg", msgs.get(i + 1));
						}
						context.put("goPTButton", new Boolean(goPT));
						context.put("goNTButton", new Boolean(goNT));
					}
				}
			}
		}
		catch (IdUnusedException e)
		{
			if (M_log.isDebugEnabled()) M_log.debug(this + ".buildShowMetadataContext()" + e);
		}
		catch (PermissionException e)
		{
			if (M_log.isDebugEnabled()) M_log.debug(this + ".buildShowMetadataContext()" + e);
			addAlert(sstate, rb.getString("java.youmess")+ e.toString());
		}

		String template = (String) getContext(rundata).get("template");
		return template + "-metadata";

	}

	/*
	 * Update the current message reference in the state object @param direction If going to previous message, -1; to next one, 1.
	 */
	private void indexMessage(RunData rundata, Context context, int direction)
	{
		SessionState sstate = ((JetspeedRunData) rundata).getPortletSessionState(((JetspeedRunData) rundata).getJs_peid());

		// retrieve the state from state object
		AnnouncementActionState state = (AnnouncementActionState) getState(context, rundata, AnnouncementActionState.class);

		// get the channel and message id information from state object
		String messageReference = state.getMessageReference();

		// get the message object through service
		try
		{
			// get the channel id throught announcement service
			AnnouncementChannel channel = AnnouncementService.getAnnouncementChannel(this
					.getChannelIdFromReference(messageReference));

			// get the message object through service
			AnnouncementMessage message = channel.getAnnouncementMessage(this.getMessageIDFromReference(messageReference));

			List msgs = (List) sstate.getAttribute(STATE_MESSAGES);
			for (int i = 0; i < msgs.size(); i++)
			{
				if (((AnnouncementWrapper) msgs.get(i)).getId().equals(message.getId()))
				{
					int index = i;
					// moving to the next message in the list
					if (direction == 1)
					{
						if ((index != -1) && ((index + 1) < msgs.size()))
						{
							AnnouncementWrapper next = (AnnouncementWrapper) (msgs.get(index + 1));
							state.setMessageReference(next.getReference());
						}
					}
					// moving to the previous message in the list
					else if (direction == -1)
					{
						if ((index != -1) && ((index - 1) >= 0))
						{
							AnnouncementWrapper prev = (AnnouncementWrapper) (msgs.get(index - 1));
							state.setMessageReference(prev.getReference());
						}
					}
				}
			}
		}
		catch (IdUnusedException e)
		{
			addAlert(sstate, rb.getString("java.alert.cannotfindann"));
		}
		catch (PermissionException e)
		{
			addAlert(sstate, rb.getString("java.alert.youacc"));
		}
	}

	/**
	 * Responding to the request of going to next message
	 */
	public void doNext_message(RunData rundata, Context context)
	{
		indexMessage(rundata, context, 1);

	} // doNext_message

	/**
	 * Responding to the request of going to previous message
	 */
	public void doPrev_message(RunData rundata, Context context)
	{
		indexMessage(rundata, context, -1);

	} // doPrev_message

	/**
	 * Get the message id from a message reference.
	 */
	private String getMessageIDFromReference(String messageReference)
	{
		// "crack" the reference (a.k.a dereference, i.e. make a Reference)
		// and get the event id and channel reference
		Reference ref = EntityManager.newReference(messageReference);
		return ref.getId();
	}

	/**
	 * Get the channel id from a message reference.
	 */
	private String getChannelIdFromReference(String messageReference)
	{
		// "crack" the reference (a.k.a dereference, i.e. make a Reference)
		// and get the event id and channel reference
		Reference ref = EntityManager.newReference(messageReference);
		String channelId = AnnouncementService.channelReference(ref.getContext(), ref.getContainer());
		return channelId;
	} // getChannelIdFromReference

	/**
	 * corresponding to chef_announcements doShowMetadata
	 * 
	 * @param itemId
	 *        The string used to record the announcement id
	 */
	public void doShowmetadata(RunData rundata, Context context)
	{
		// retrieve the state from state object
		AnnouncementActionState state = (AnnouncementActionState) getState(context, rundata, AnnouncementActionState.class);

		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState sstate = ((JetspeedRunData) rundata).getPortletSessionState(peid);

		String itemReference = rundata.getParameters().getString("itemReference");
		state.setMessageReference(itemReference);

		state.setIsListVM(false);
		state.setIsNewAnnouncement(false);
		state.setStatus("showMetadata");

		// disable auto-updates while in view mode
		disableObservers(sstate);

	} // doShowMetadata

	/**
	 * Build the context for cancelling the operation and going back to list view
	 */
	protected String buildCancelContext(VelocityPortlet portlet, Context context, RunData rundata, AnnouncementActionState state)
	{
		// buildNormalContext(portlet, context, rundata);

		String template = (String) getContext(rundata).get("template");
		return template;

	} // buildCancelContext

	/**
	 * Build the context for asking for the delete confirmation
	 */
	protected String buildDeleteAnnouncementContext(VelocityPortlet portlet, Context context, RunData rundata,
			AnnouncementActionState state)
	{
		Vector v = state.getDelete_messages();
		if (v == null) v = new Vector();
		context.put("delete_messages", v.iterator());

		try
		{
			Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
			context.put("site", site);
		}
		catch (IdUnusedException e)
		{
			// No site available.
		}
		catch (NullPointerException e)
		{
		}
		context.put("channelAccess", MessageHeader.MessageAccess.CHANNEL);

		String template = (String) getContext(rundata).get("template");
		return template + "-delete";

	} // buildDeleteAnnouncementContext

	/**
	 * Action is to use when doNewannouncement requested, corresponding to chef_announcements menu "New..."
	 */
	public void doNewannouncement(RunData rundata, Context context)
	{
		// retrieve the state from state object
		AnnouncementActionState state = (AnnouncementActionState) getState(context, rundata, AnnouncementActionState.class);

		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState sstate = ((JetspeedRunData) rundata).getPortletSessionState(peid);

		state.setIsListVM(false);
		state.setAttachments(null);
		state.setSelectedAttachments(null);
		state.setDeleteMessages(null);
		state.setIsNewAnnouncement(true);
		state.setTempBody("");
		state.setTempSubject("");
		state.setStatus("new");

		sstate.setAttribute(AnnouncementAction.SSTATE_PUBLICVIEW_VALUE, null);
		sstate.setAttribute(AnnouncementAction.SSTATE_NOTI_VALUE, null);

		// disable auto-updates while in view mode
		disableObservers(sstate);

	} // doNewannouncement

	/**
	 * Dispatcher function for various actions on add/revise announcement page
	 */
	public void doAnnouncement_form(RunData data, Context context)
	{

		ParameterParser params = data.getParameters();

		String option = (String) params.getString("option");
		if (option != null)
		{
			if (option.equals("post"))
			{
				// post announcement
				readAnnouncementForm(data, context, true);
				doPost(data, context);
			}
			else if (option.equals("preview"))
			{
				// preview announcement
				readAnnouncementForm(data, context, true);
				doRevisepreview(data, context);
			}
			else if (option.equals("save"))
			{
				// save announcement as draft
				readAnnouncementForm(data, context, true);
				doSavedraft(data, context);
			}
			else if (option.equals("cancel"))
			{
				// cancel
				doCancel(data, context);
			}
			else if (option.equals("attach"))
			{
				// attach
				readAnnouncementForm(data, context, false);
				doAttachments(data, context);
			}
			else if (option.equals("sortbygrouptitle"))
			{
				// sort group by title
				readAnnouncementForm(data, context, false);
				doSortbygrouptitle(data, context);
			}
			else if (option.equals("sortbygroupdescription"))
			{
				// sort group by description
				readAnnouncementForm(data, context, false);
				doSortbygroupdescription(data, context);
			}
		}
	} // doAnnouncement_form

	/**
	 * Read user inputs in announcement form
	 * 
	 * @param data
	 * @param checkForm
	 *        need to check form data or not
	 */
	protected void readAnnouncementForm(RunData rundata, Context context, boolean checkForm)
	{
		// retrieve the state from state object
		final AnnouncementActionState state = (AnnouncementActionState) getState(context, rundata, AnnouncementActionState.class);

		final String peid = ((JetspeedRunData) rundata).getJs_peid();
		final SessionState sstate = ((JetspeedRunData) rundata).getPortletSessionState(peid);
		final ParameterParser params = rundata.getParameters();
		
		// *** make sure the subject and body won't be empty
		// read in the subject input from announcements-new.vm
		final String subject = params.getString("subject");
		// read in the body input
		String body = params.getString("body");
		body = processFormattedTextFromBrowser(sstate, body);

		state.setTempSubject(subject);
		state.setTempBody(body);

		if (checkForm)
		{
			if (subject.length() == 0)
			{
				// addAlert(sstate, "You need to fill in the subject!");
				addAlert(sstate, rb.getString("java.alert.youneed"));
			}
			else if (body.replaceAll("<br>", "").replaceAll("<br/>","").replaceAll("&nbsp;", "").replaceAll("&lt;br type=&quot;_moz&quot; /&gt;", "").trim().equals("")  || body.length() == 0 || body == null || 
					FormattedText.escapeHtml(body,false).equals("&lt;br type=&quot;_moz&quot; /&gt;"))
			{
				body="";
				addAlert(sstate, rb.getString("java.alert.youfill"));// "You need to fill in the body of the announcement!");
			}
		}
		
		final String specify = params.getString(HIDDEN);
		final boolean use_start_date = params.getBoolean("use_start_date");
		final boolean use_end_date = params.getBoolean("use_end_date");
		
		// if user selected specify dates but then did not check a checkbox
		// in order to do so, return an alert
		if (checkForm)
		{
			if (specify.equals("specify") && !(use_start_date || use_end_date))
			{
				addAlert(sstate, rb.getString("java.alert.nodates"));
			}
		}
	
		Time releaseDate = null;
		Time retractDate = null;
		
		if (use_start_date && SPECIFY_DATES.equals(specify))
		{
			int begin_year = params.getInt("release_year");
			int begin_month = params.getInt("release_month");
			int begin_day = params.getInt("release_day");
			int begin_hour = hourAmPmConvert(params, "release_hour", "release_ampm");
			int begin_min = params.getInt("release_minute");
			releaseDate = TimeService.newTimeLocal(begin_year, begin_month, begin_day, begin_hour, begin_min, 0, 0);

			state.setTempReleaseDate(releaseDate);
		}
		else
		{
			state.setTempReleaseDate(null);
		}
		
		if (use_end_date && SPECIFY_DATES.equals(specify))
		{
			int end_year = params.getInt("retract_year");
			int end_month = params.getInt("retract_month");
			int end_day = params.getInt("retract_day");
			int end_hour = hourAmPmConvert(params, "retract_hour", "retract_ampm");
			int end_min = params.getInt("retract_minute");
			retractDate = TimeService.newTimeLocal(end_year, end_month, end_day, end_hour, end_min, 0, 0);

			state.setTempRetractDate(retractDate);
		}
		else
		{
			state.setTempRetractDate(null);
		}

		if (checkForm)
		{
			if ((use_start_date && use_end_date) && retractDate.before(releaseDate))
			{
				addAlert(sstate, rb.getString("java.alert.baddates"));
			}
		}
		// set hidden property just in case saved
		state.setTempHidden(params.getBoolean(HIDDEN));

		// announce to public?
		String announceTo = params.getString("announceTo");
		state.setTempAnnounceTo(announceTo);
		if (announceTo.equals("groups"))
		{
			String[] groupChoice = params.getStrings("selectedGroups");
			if (groupChoice != null)
			{
				state.setTempAnnounceToGroups(new ArrayList(Arrays.asList(groupChoice)));
			}

			if (groupChoice == null || groupChoice.length == 0)
			{
				state.setTempAnnounceToGroups(null);
				if (checkForm)
				{
					addAlert(sstate, rb.getString("java.alert.youchoosegroup"));
				}
			}
		}
		else
		{
			state.setTempAnnounceToGroups(null);
		}

		// read the public view setting and save it in session state
		String publicView = params.getString("pubview");
		if (publicView == null) publicView = "false";
		sstate.setAttribute(AnnouncementAction.SSTATE_PUBLICVIEW_VALUE, publicView);

		// read the notification options & save it in session state
		String notification = rundata.getParameters().getString("notify");
		sstate.setAttribute(AnnouncementAction.SSTATE_NOTI_VALUE, notification);

	} // readAnnouncementForm

	/**
	 * convert the am pm hour into 24-hour format
	 * @param params
	 * @param hour
	 * @return
	 */
	private int hourAmPmConvert(final ParameterParser params, String param_hour_name, String param_ampm_name) 
	{
		int hour = params.getInt(param_hour_name);
		hour %= 12;
		if ("pm".equals(params.getString(param_ampm_name))) hour += 12;
		return hour;
	}

	/**
	 * Action is to use when doPost requested, corresponding to chef_announcements-revise or -preview "eventSubmit_doPost"
	 */
	public void doPost(RunData rundata, Context context)
	{
		postOrSaveDraft(rundata, context, true);
	} // doPost

	/**
	 * post or save draft of a message?
	 */
	protected void postOrSaveDraft(RunData rundata, Context context, boolean post)
	{
		// retrieve the state from state object
		AnnouncementActionState state = (AnnouncementActionState) getState(context, rundata, AnnouncementActionState.class);

		final String peid = ((JetspeedRunData) rundata).getJs_peid();
		final SessionState sstate = ((JetspeedRunData) rundata).getPortletSessionState(peid);

		// get the channel and message id information from state object
		final String channelId = state.getChannelId();

		// these are values that will be have been set if coming
		// from Preview
		final String subject = state.getTempSubject();
		final String body = state.getTempBody();
		final Time tempReleaseDate = state.getTempReleaseDate();
		final Time tempRetractDate = state.getTempRetractDate();
		final Boolean tempHidden = state.getTempHidden();
		
		// announce to public?
		final String announceTo = state.getTempAnnounceTo();

		// there is any error message caused by empty subject or body
		if (sstate.getAttribute(STATE_MESSAGE) != null)
		{
			state.setIsListVM(false);
			state.setStatus("stayAtRevise");

			// disable auto-updates while in view mode
			disableObservers(sstate);
		}
		else
		{

			// read the notification options
			String notification = (String) sstate.getAttribute(AnnouncementAction.SSTATE_NOTI_VALUE);

			int noti = NotificationService.NOTI_OPTIONAL;
			if ("r".equals(notification))
			{
				noti = NotificationService.NOTI_REQUIRED;
			}
			else if ("n".equals(notification))
			{
				noti = NotificationService.NOTI_NONE;
			}

			try
			{
				AnnouncementChannel channel = null;
				AnnouncementMessageEdit msg = null;

				// if a new created announcement to be posted
				if (state.getIsNewAnnouncement())
				{
					// get the channel id throught announcement service
					channel = AnnouncementService.getAnnouncementChannel(channelId);
					msg = channel.addAnnouncementMessage();
				}
				else
				{
					// get the message object through service
					// AnnouncementMessageEdit msg = channel.editAnnouncementMessage( messageId );
					msg = state.getEdit();

					// get the channel id throught announcement service
					channel = AnnouncementService.getAnnouncementChannel(this.getChannelIdFromReference(msg.getReference()));
				}

				msg.setBody(body);
				AnnouncementMessageHeaderEdit header = msg.getAnnouncementHeaderEdit();
				header.setSubject(subject);
//				header.setDraft(!post);
				// v2.4: Hidden in UI becomes Draft 'behind the scenes'
				header.setDraft(tempHidden);
				header.replaceAttachments(state.getAttachments());
				header.setFrom(UserDirectoryService.getCurrentUser());

				// values stored here if saving from Add/Revise page
				ParameterParser params = rundata.getParameters();
				
				// get release/retract dates
				final String specify = params.getString(HIDDEN);
				final boolean use_start_date = params.getBoolean("use_start_date");
				final boolean use_end_date = params.getBoolean("use_end_date");
				Time releaseDate = null;
				Time retractDate = null;
				
				if(use_start_date && SPECIFY_DATES.equals("specify"))
				{
					int begin_year = params.getInt("release_year");
					int begin_month = params.getInt("release_month");
					int begin_day = params.getInt("release_day");
					int begin_hour = hourAmPmConvert(params, "release_hour", "release_ampm");
					int begin_min = params.getInt("release_minute");
					
					releaseDate = TimeService.newTimeLocal(begin_year, begin_month, begin_day, begin_hour, begin_min, 0, 0);

					// in addition to setting release date property, also set Date to release
					// date so properly sorted
					msg.getPropertiesEdit().addProperty(AnnouncementService.RELEASE_DATE, releaseDate.toString());
					header.setDate(releaseDate);
				}
				else if (tempReleaseDate != null) // saving from Preview page
				{
					// in addition to setting release date property, also set Date to release
					// date so properly sorted
					msg.getPropertiesEdit().addProperty(AnnouncementService.RELEASE_DATE, tempReleaseDate.toString());
					header.setDate(tempReleaseDate);					
				}
				else
				{
					// they are not using release date so remove
					if (msg.getProperties().getProperty(AnnouncementService.RELEASE_DATE) != null) 
					{
							msg.getPropertiesEdit().removeProperty(AnnouncementService.RELEASE_DATE);
					}

					// since revised, set Date to current date
					header.setDate(TimeService.newTime());
				}
				
				if (use_end_date && SPECIFY_DATES.equals(specify))
				{
					int end_year = params.getInt("retract_year");
					int end_month = params.getInt("retract_month");
					int end_day = params.getInt("retract_day");
					int end_hour = hourAmPmConvert(params, "retract_hour", "retract_ampm");
					int end_min = params.getInt("retract_minute");
					retractDate = TimeService.newTimeLocal(end_year, end_month, end_day, end_hour, end_min, 0, 0);

					msg.getPropertiesEdit().addProperty("retractDate", retractDate.toString());
				}
				else if (tempRetractDate != null)
				{
					msg.getPropertiesEdit().addProperty(AnnouncementService.RETRACT_DATE, tempRetractDate.toString());
				}
				else 
				{
					// they are not using release date so remove
					if (msg.getProperties().getProperty(AnnouncementService.RETRACT_DATE) != null) 
					{
							msg.getPropertiesEdit().removeProperty(AnnouncementService.RETRACT_DATE);
					}
				}
				
				// announce to?
				try
				{
					Site site = SiteService.getSite(channel.getContext());

					if (announceTo.equals("pubview")
							|| Boolean.valueOf((String) sstate.getAttribute(AnnouncementAction.SSTATE_PUBLICVIEW_VALUE))
									.booleanValue()) // if from the post in preview, get the setting from sstate object
					{
						// any setting of this property indicates pubview
						msg.getPropertiesEdit().addProperty(ResourceProperties.PROP_PUBVIEW, Boolean.TRUE.toString());
						header.clearGroupAccess();
					}
					else
					{
						// remove the property to indicate no pubview
						msg.getPropertiesEdit().removeProperty(ResourceProperties.PROP_PUBVIEW);
					}

					// announce to site?
					if (announceTo.equals("site"))
					{
						header.clearGroupAccess();
					}
					else if (announceTo.equals("groups"))
					{
						// get the group ids selected
						Collection groupChoice = state.getTempAnnounceToGroups();
						
						// make a collection of Group objects
						Collection groups = new Vector();
						for (Iterator iGroups = groupChoice.iterator(); iGroups.hasNext();)
						{
							String groupId = (String) iGroups.next();
							groups.add(site.getGroup(groupId));
						}

						// set access
						header.setGroupAccess(groups);
					}
				}
				catch (PermissionException e)
				{
					addAlert(sstate, rb.getString("java.alert.youpermi")// "You don't have permissions to create this announcement -"
							+ subject);

					state.setIsListVM(false);
					state.setStatus("stayAtRevise");

					// disable auto-updates while in view mode
					disableObservers(sstate);
					return;
				}
				catch (Exception ignore)
				{
					// No site available.
				}

				// save notification level if this is a future notification message
				Time now = TimeService.newTime();
				
				if (releaseDate != null && now.before(releaseDate) && noti != NotificationService.NOTI_NONE)
				{
					msg.getPropertiesEdit().addProperty("notificationLevel", notification);
				}
				
				channel.commitMessage(msg, noti, "org.sakaiproject.announcement.impl.SiteEmailNotificationAnnc");

				if (!state.getIsNewAnnouncement())
				{
					state.setEdit(null);
				} // if-else
			}
			catch (IdUnusedException e)
			{
				if (M_log.isDebugEnabled()) M_log.debug(this + "doPost()", e);
			}
			catch (PermissionException e)
			{
				if (M_log.isDebugEnabled()) M_log.debug(this + "doPost()", e);
				addAlert(sstate, rb.getString("java.alert.youpermi")+ subject);
			}

			state.setIsListVM(true);
			state.setAttachments(null);
			state.setSelectedAttachments(null);
			state.setDeleteMessages(null);
			state.setStatus(POST_STATUS);
			state.setMessageReference("");
			state.setTempAnnounceTo(null);
			state.setTempAnnounceToGroups(null);
			state.setCurrentSortedBy(SORT_DATE);
			//state.setCurrentSortAsc(Boolean.TRUE.booleanValue());
			sstate.setAttribute(STATE_CURRENT_SORTED_BY, SORT_DATE);
			sstate.setAttribute(STATE_CURRENT_SORT_ASC, state.getCurrentSortAsc());

			// make sure auto-updates are enabled
			enableObservers(sstate);
		}
	} // postOrSaveDraf

	/**
	 * Action is to use when doPreviewrevise requested from preview status corresponding to chef_announcements-preview "eventSubmit_doPreviewrevise"
	 */
	public void doPreviewrevise(RunData rundata, Context context)
	{

		// retrieve the state from state object
		AnnouncementActionState state = (AnnouncementActionState) getState(context, rundata, AnnouncementActionState.class);

		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState sstate = ((JetspeedRunData) rundata).getPortletSessionState(peid);

		state.setStatus("backToReviseAnnouncement");

		// disable auto-updates while in view mode
		disableObservers(sstate);

	} // doPreviewrevise

	/**
	 * Action is to use when ddoDelete requested, to perform deletion corresponding to chef_announcements-delete "eventSubmit_doDelete"
	 */
	public void doDelete(RunData rundata, Context context)
	{

		// retrieve the state from state object
		AnnouncementActionState state = (AnnouncementActionState) getState(context, rundata, AnnouncementActionState.class);

		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState sstate = ((JetspeedRunData) rundata).getPortletSessionState(peid);

		// get the messages to be deleted from state object
		Vector v = state.getDelete_messages();
		Iterator delete_messages = v.iterator();

		while (delete_messages.hasNext())
		{
			try
			{
				AnnouncementMessage message = (AnnouncementMessage) delete_messages.next();

				// get the channel id throught announcement service
				AnnouncementChannel channel = AnnouncementService.getAnnouncementChannel(this.getChannelIdFromReference(message
						.getReference()));

				if (channel.allowRemoveMessage(message))
				{
					// remove message from channel
					//AnnouncementMessageEdit edit = channel.editAnnouncementMessage(message.getId());
					//channel.removeMessage(edit); 
					channel.removeAnnouncementMessage(message.getId());

					// make sure auto-updates are enabled
					enableObservers(sstate);
				}
				else
				{
					addAlert(sstate, rb.getString("java.alert.youdel"));
					// "you don't have permission to delete the messages.");
				}
			}
			catch (IdUnusedException e)
			{
				if (M_log.isDebugEnabled()) M_log.debug(this + ".doDeleteannouncement()", e);
			}
			catch (PermissionException e)
			{
				if (M_log.isDebugEnabled()) M_log.debug(this + ".doDeleteannouncement()", e);
			}
			catch (NoSuchElementException e)
			{
				if (M_log.isDebugEnabled()) M_log.debug(this + ".doDeleteannouncement()", e);
			}
		}

		state.setIsListVM(true);
		state.setStatus("FinishDeleting");

	} // doDelete

	/**
	 * Action is to use when doDeleteannouncement requested, corresponding to chef_announcements or chef_announcements-metadata menu "Delete"
	 */
	public void doDeleteannouncement(RunData rundata, Context context)
	{
		// retrieve the state from state object
		AnnouncementActionState state = (AnnouncementActionState) getState(context, rundata, AnnouncementActionState.class);

		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState sstate = ((JetspeedRunData) rundata).getPortletSessionState(peid);

		// get the channel and message id information from state object
		String messageReference = state.getMessageReference();

		// if in main screen
		if (state.getIsListVM())
		{
			// then, read in the selected announcment items
			String[] messageReferences = rundata.getParameters().getStrings("selectedMembers");
			if (messageReferences != null)
			{
				Vector v = new Vector();
				for (int i = 0; i < messageReferences.length; i++)
				{
					// get the message object through service
					try
					{
						// get the channel id throught announcement service
						AnnouncementChannel channel = AnnouncementService.getAnnouncementChannel(this
								.getChannelIdFromReference(messageReferences[i]));
						// get the message object through service
						AnnouncementMessage message = channel.getAnnouncementMessage(this
								.getMessageIDFromReference(messageReferences[i]));

						v.addElement(message);
					}
					catch (IdUnusedException e)
					{
						if (M_log.isDebugEnabled()) M_log.debug(this + ".doDeleteannouncement()", e);
						// addAlert(sstate, e.toString());
					}
					catch (PermissionException e)
					{
						if (M_log.isDebugEnabled()) M_log.debug(this + ".doDeleteannouncement()", e);
						addAlert(sstate, rb.getString("java.alert.youdelann")	+ messageReferences[i]);
					}
				}

				// record the items to be deleted
				state.setDeleteMessages(v);
				state.setIsListVM(false);
				state.setStatus(DELETE_ANNOUNCEMENT_STATUS);

				// disable auto-updates while in view mode
				disableObservers(sstate);
			}
			else
			{
				state.setIsListVM(true);
				state.setStatus("noSelectedForDeletion");

				// make sure auto-updates are enabled
				enableObservers(sstate);
			}

		}
		// if not in main screen
		else
		{
			state.setIsNewAnnouncement(false);
			Vector v = new Vector();

			// get the message object through service
			try
			{
				// get the channel id throught announcement service
				AnnouncementChannel channel = AnnouncementService.getAnnouncementChannel(this
						.getChannelIdFromReference(messageReference));
				// get the message object through service
				AnnouncementMessage message = channel.getAnnouncementMessage(this.getMessageIDFromReference(messageReference));

				v.addElement(message);
			}
			catch (IdUnusedException e)
			{
				if (M_log.isDebugEnabled()) M_log.debug(this + "doDeleteannouncement()", e);
				// addAlert(sstate, e.toString());
			}
			catch (PermissionException e)
			{
				if (M_log.isDebugEnabled()) M_log.debug(this + "doDeleteannouncement()", e);
				addAlert(sstate, rb.getString("java.alert.youdelann2"));
			}

			// state.setDelete_messages(delete_messages);
			state.setDeleteMessages(v);

			state.setIsListVM(false);
			if (sstate.getAttribute(STATE_MESSAGE) == null)
			{
				// add folder sucessful
				state.setStatus(DELETE_ANNOUNCEMENT_STATUS);
			}
		}

		// disable auto-updates while in confirm mode
		disableObservers(sstate);

	} // doDeleteannouncement

	/**
	 * Action is to use when doDelete_announcement_link requested, corresponding to chef_announcements the link of deleting announcement item
	 */
	public void doDelete_announcement_link(RunData rundata, Context context)
	{
		// retrieve the state from state object
		AnnouncementActionState state = (AnnouncementActionState) getState(context, rundata, AnnouncementActionState.class);

		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState sstate = ((JetspeedRunData) rundata).getPortletSessionState(peid);

		String messageReference = rundata.getParameters().getString("itemReference");
		state.setMessageReference(messageReference);

		state.setIsNewAnnouncement(false);
		Vector v = new Vector();

		// get the message object through service
		try
		{
			// get the channel id throught announcement service
			AnnouncementChannel channel = AnnouncementService.getAnnouncementChannel(this
					.getChannelIdFromReference(messageReference));
			// get the message object through service
			AnnouncementMessage message = channel.getAnnouncementMessage(this.getMessageIDFromReference(messageReference));

			v.addElement(message);
		}
		catch (IdUnusedException e)
		{
			if (M_log.isDebugEnabled()) M_log.debug(this + "doDeleteannouncement()", e);
		}
		catch (PermissionException e)
		{
			if (M_log.isDebugEnabled()) M_log.debug(this + "doDeleteannouncement()", e);
			addAlert(sstate, rb.getString("java.alert.youdelann2"));
		}

		state.setDeleteMessages(v);

		if (sstate.getAttribute(STATE_MESSAGE) == null)
		{
			state.setStatus(DELETE_ANNOUNCEMENT_STATUS);
		}
	} // doDelete_announcement_link

	/**
	 * Action is to use when doReviseannouncement requested, corresponding to chef_announcements the link of any draft announcement item
	 */
	public void doReviseannouncement(RunData rundata, Context context)
	{
		// retrieve the state from state object
		AnnouncementActionState state = (AnnouncementActionState) getState(context, rundata, AnnouncementActionState.class);

		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState sstate = ((JetspeedRunData) rundata).getPortletSessionState(peid);

		state.setStatus("goToReviseAnnouncement");

		String messageReference = rundata.getParameters().getString("itemReference");
		state.setMessageReference(messageReference);

		// get the message object through service
		try
		{
			// get the channel id throught announcement service
			AnnouncementChannel channel = AnnouncementService.getAnnouncementChannel(this
					.getChannelIdFromReference(messageReference));
			// get the message object through service
			// AnnouncementMessage message = channel.getAnnouncementMessage( messageId );

			AnnouncementMessageEdit edit = channel.editAnnouncementMessage(this.getMessageIDFromReference(messageReference));
			state.setEdit(edit);

			state.setTempAnnounceToGroups(edit.getAnnouncementHeader().getGroups());

			// ReferenceVector attachmentList = (message.getHeader()).getAttachments();
			List attachmentList = (edit.getHeader()).getAttachments();
			state.setAttachments(attachmentList);

			// disable auto-updates while in confirm mode
			disableObservers(sstate);
		}
		catch (IdUnusedException e)
		{
			if (M_log.isDebugEnabled()) M_log.debug(this + "announcementRevise", e);
			// addAlert(sstate, e.toString());
		}
		catch (PermissionException e)
		{
			if (M_log.isDebugEnabled()) M_log.debug(this + "announcementRevise", e);
			state.setStatus("showMetadata");
		}
		catch (InUseException err)
		{
			if (M_log.isDebugEnabled()) M_log.debug(this + ".doReviseannouncementfrommenu", err);
			addAlert(sstate, rb.getString("java.alert.thisitem"));
			// "This item is being edited by another user. Please try again later.");
			state.setStatus("showMetadata");
		}
		state.setIsNewAnnouncement(false);

	} // doReviseannouncement

	/**
	 * Action is to use when doReviseannouncementfrommenu requested, corresponding to chef_announcements.vm and -metadata.vm menu "Revise"
	 */

	public void doReviseannouncementfrommenu(RunData rundata, Context context)
	{

		// retrieve the state from state object
		AnnouncementActionState state = (AnnouncementActionState) getState(context, rundata, AnnouncementActionState.class);

		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState sstate = ((JetspeedRunData) rundata).getPortletSessionState(peid);

		// get the channel and message id information from state object
		String messageReference = state.getMessageReference();

		// if in main screen
		if (state.getIsListVM())
		{
			// then, read in the selected announcment items
			String[] messageReferences = rundata.getParameters().getStrings("selectedMembers");
			if (messageReferences != null)
			{
				if (messageReferences.length > 1)
				{
					state.setIsListVM(true);
					state.setStatus("moreThanOneSelectedForRevise");

					// make sure auto-updates are enabled
					enableObservers(sstate);

				}
				else if (messageReferences.length == 1)
				{
					state.setIsListVM(false);
					state.setMessageReference(messageReferences[0]);
					state.setStatus("goToReviseAnnouncement");

					// get the message object through service
					try
					{
						// get the channel id throught announcement service
						AnnouncementChannel channel = AnnouncementService.getAnnouncementChannel(this
								.getChannelIdFromReference(messageReferences[0]));
						// get the message object through service
						AnnouncementMessage message = channel.getAnnouncementMessage(this
								.getMessageIDFromReference(messageReferences[0]));

						if (channel.allowEditMessage(message.getId()))
						{
							state.setAttachments(message.getHeader().getAttachments());
							AnnouncementMessageEdit edit = channel.editAnnouncementMessage(this
									.getMessageIDFromReference(messageReferences[0]));
							state.setEdit(edit);
							state.setTempAnnounceToGroups(edit.getAnnouncementHeader().getGroups());
						}
						else
						{
							state.setIsListVM(true);
							state.setStatus("noPermissionToRevise");
						}
					}
					catch (IdUnusedException e)
					{
						if (M_log.isDebugEnabled()) M_log.debug(this + "announcementReviseFromMenu", e);
					}
					catch (PermissionException e)
					{
						if (M_log.isDebugEnabled()) M_log.debug(this + "announcementReviseFromMenu", e);
						addAlert(sstate, rb.getString("java.alert.youacc")// "You don't have permissions to access the message(s) - "
								+ e.toString());
					}
					// %%% -ggolden catch(InUseException err)
					catch (InUseException err)
					{
						if (M_log.isDebugEnabled())
							M_log.debug(this + ".doReviseannouncementfrommenu", err);
						addAlert(sstate, rb.getString("java.alert.thisis"));// "This item is being edited by another user. Please try again later.");
						state.setIsListVM(false);
						state.setStatus("showMetadata");

						// make sure auto-updates are enabled
						disableObservers(sstate);
					}
				}
			}
			else
			{
				state.setIsListVM(true);
				state.setStatus(NOT_SELECTED_FOR_REVISE_STATUS);

				// make sure auto-updates are enabled
				enableObservers(sstate);
			}
		}
		// if the user is viewing a certain announcement already
		else
		{
			state.setIsListVM(false);
			state.setStatus("goToReviseAnnouncement");

			// get the message object through service
			try
			{
				// get the channel id throught announcement service
				AnnouncementChannel channel = AnnouncementService.getAnnouncementChannel(this
						.getChannelIdFromReference(messageReference));
				// get the message object through service
				AnnouncementMessage message = channel.getAnnouncementMessage(this.getMessageIDFromReference(messageReference));

				if (channel.allowEditMessage(message.getId()))
				{
					AnnouncementMessageEdit edit = channel
							.editAnnouncementMessage(this.getMessageIDFromReference(messageReference));
					state.setEdit(edit);
					state.setAttachments(message.getHeader().getAttachments());
					state.setTempAnnounceToGroups(edit.getAnnouncementHeader().getGroups());
				}
				else
				{
					state.setIsListVM(true);
					state.setStatus("noPermissionToRevise");
				}
			}
			catch (IdUnusedException e)
			{
				if (M_log.isDebugEnabled()) M_log.debug(this + "announcementReviseFromMenu", e);
				// addAlert(sstate, e.toString());
			}
			catch (PermissionException e)
			{
				if (M_log.isDebugEnabled()) M_log.debug(this + "announcementReviseFromMenu", e);
				addAlert(sstate, rb.getString("java.alert.youacc")// "You don't have permissions to access the message(s) - "
						+ e.toString());
			}
			catch (InUseException err)
			{
				if (M_log.isDebugEnabled()) M_log.debug(this + ".doReviseannouncementfrommenu", err);
				addAlert(sstate, rb.getString("java.alert.thisis"));// "This item is being edited by another user. Please try again later.");
				state.setIsListVM(false);
				state.setStatus("showMetadata");

				// disable auto-updates while in view mode
				disableObservers(sstate);
			}
		}

		state.setIsNewAnnouncement(false);
	} // doReviseannouncementfrommenu

	/**
	 * Action is to use when doRevisePreview requested, corresponding to chef_announcements-revise "eventSubmit_doRevisePreview" from revise view to preview view
	 */
	public void doRevisepreview(RunData rundata, Context context)
	{
		// retrieve the state from state object
		AnnouncementActionState state = (AnnouncementActionState) getState(context, rundata, AnnouncementActionState.class);

		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState sstate = ((JetspeedRunData) rundata).getPortletSessionState(peid);

		// there is any error message caused by empty subject or body
		if (sstate.getAttribute(STATE_MESSAGE) != null)
		{
			state.setIsListVM(false);
			state.setStatus("stayAtRevise");
		}
		else
		{
			state.setStatus("revisePreviw");
		} // if-else

		// disable auto-updates while in view mode
		disableObservers(sstate);

	} // doRevisepreview

	public void doAttachments(RunData data, Context context)
	{
		AnnouncementActionState actionState = (AnnouncementActionState) getState(context, data, AnnouncementActionState.class);
		if (actionState.getChannelId().contains("motd")){
		ToolSession session = SessionManager.getCurrentToolSession();
        session.setAttribute(FilePickerHelper.FILE_PICKER_ATTACH_LINKS, new Boolean(true).toString());
		}
        
		// get into helper mode with this helper tool
		startHelper(data.getRequest(), "sakai.filepicker");

		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		AnnouncementActionState myState = (AnnouncementActionState) getState(context, data, AnnouncementActionState.class);

		// // setup... we'll use the ResourcesAction's mode
		// state.setAttribute(ResourcesAction.STATE_MODE, ResourcesAction.MODE_HELPER);
		// state.setAttribute(ResourcesAction.STATE_RESOURCES_HELPER_MODE, ResourcesAction.MODE_ATTACHMENT_SELECT);
		// boolean show_other_sites = ServerConfigurationService.getBoolean("resources.show_all_collections.helper", ResourcesAction.SHOW_ALL_SITES_IN_FILE_PICKER);
		// /** This attribute indicates whether "Other Sites" twiggle should show */
		// state.setAttribute(ResourcesAction.STATE_SHOW_ALL_SITES, Boolean.toString(show_other_sites));
		// /** This attribute indicates whether "Other Sites" twiggle should be open */
		// state.setAttribute(ResourcesAction.STATE_SHOW_OTHER_SITES, Boolean.FALSE.toString());
		//		
		// String toolName = ToolManager.getCurrentTool().getTitle();
		// state.setAttribute(ResourcesAction.STATE_ATTACH_TOOL_NAME, toolName);
		//		
		// String subject = myState.getTempSubject();
		// String stateFromText = rb.getString("java.theann");//"the announcement";
		// if (subject != null && subject.length() > 0)
		// {
		// stateFromText = rb.getString("java.ann")//"announcement "
		// + '"' + subject + '"';
		// }
		// state.setAttribute(AttachmentAction.STATE_FROM_TEXT, stateFromText);
		//
		// List attachments = myState.getAttachments();
		// // whether there is alread an attachment //%%%zqian
		// if (attachments.size() > 0)
		// {
		// state.setAttribute(ResourcesAction.STATE_HAS_ATTACHMENT_BEFORE, Boolean.TRUE);
		// }
		// else
		// {
		// state.setAttribute(ResourcesAction.STATE_HAS_ATTACHMENT_BEFORE, Boolean.FALSE);
		// }

		state.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, myState.getAttachments());

		myState.setStatus("backToReviseAnnouncement");
	} // doAttachments

	/**
	 * Action is to use when doCancel requested, corresponding to chef_announcement "eventSubmit_doCancel"
	 */
	public void doCancel(RunData rundata, Context context)
	{

		// retrieve the state from state object
		AnnouncementActionState state = (AnnouncementActionState) getState(context, rundata, AnnouncementActionState.class);

		// Carry out the base class options cancel if we're in the
		// middle of an options page or an merge page
		if (state.getStatus().equals(OPTIONS_STATUS) || state.getStatus().equals(MERGE_STATUS))
		{
			cancelOptions();
		}

		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState sstate = ((JetspeedRunData) rundata).getPortletSessionState(peid);

		state.setIsListVM(true);
		state.setAttachments(null);
		state.setSelectedAttachments(null);
		state.setDeleteMessages(null);
		state.setStatus(CANCEL_STATUS);
		state.setTempAnnounceTo(null);
		state.setTempAnnounceToGroups(null);
		state.setCurrentSortedBy(SORT_DATE);
		//state.setCurrentSortAsc(Boolean.TRUE.booleanValue());
		sstate.setAttribute(STATE_CURRENT_SORTED_BY, SORT_DATE);
		//sstate.setAttribute(STATE_CURRENT_SORT_ASC, Boolean.FALSE);

		sstate.setAttribute(STATE_CURRENT_SORT_ASC, state.getCurrentSortAsc());
		
		// we are done with customization... back to the main (list) mode
		sstate.removeAttribute(STATE_MODE);

		// re-enable auto-updates when going back to list mode
		enableObservers(sstate);
		
		try
		{
			if (state.getEdit() != null)
			{
				// get the channel id throught announcement service
				AnnouncementChannel channel = AnnouncementService.getAnnouncementChannel(this.getChannelIdFromReference(state
						.getEdit().getReference()));

				channel.cancelMessage(state.getEdit());
				state.setEdit(null);
			}
		}
		catch (IdUnusedException e)
		{
			if (M_log.isDebugEnabled()) M_log.debug(this + "doCancel()", e);
		}
		catch (PermissionException e)
		{
			if (M_log.isDebugEnabled()) M_log.debug(this + "doCancel()", e);
		}

		// make sure auto-updates are enabled
		enableObservers(sstate);

	} // doCancel

	/**
	 * Action is to use when doLinkcancel requested, corresponding to chef_announcement "eventSubmit_doLinkcancel"
	 */
	public void doLinkcancel(RunData rundata, Context context)
	{

		// retrieve the state from state object
		AnnouncementActionState state = (AnnouncementActionState) getState(context, rundata, AnnouncementActionState.class);

		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState sstate = ((JetspeedRunData) rundata).getPortletSessionState(peid);

		state.setIsListVM(true);
		state.setAttachments(null);
		state.setSelectedAttachments(null);
		state.setDeleteMessages(null);
		state.setStatus(CANCEL_STATUS);

		// make sure auto-updates are enabled
		enableObservers(sstate);

	} // doLinkcancel

	/**
	 * Action is to use when doSavedraft requested, corresponding to chef_announcements-preview "eventSubmit_doSavedraft"
	 */
	public void doSavedraft(RunData rundata, Context context)
	{
		postOrSaveDraft(rundata, context, false);

	} // doSavedraft

	// ********* starting for sorting *********

	/**
	 * Does initialization of sort parameters in the state.
	 */
	private void setupSort(RunData rundata, Context context, String field)
	{
		// retrieve the state from state object
		AnnouncementActionState state = (AnnouncementActionState) getState(context, rundata, AnnouncementActionState.class);

		SessionState sstate = ((JetspeedRunData) rundata).getPortletSessionState(((JetspeedRunData) rundata).getJs_peid());
		sstate.setAttribute(STATE_CURRENT_SORTED_BY, field);

		if (state.getCurrentSortedBy().equals(field))
		{
			// current sorting sequence
			boolean asc = state.getCurrentSortAsc();

			// toggle between the ascending and descending sequence
			if (asc)
				asc = false;
			else
				asc = true;

			state.setCurrentSortAsc(asc);
			sstate.setAttribute(STATE_CURRENT_SORT_ASC, new Boolean(asc));
		}
		else
		{
			// if the messages are not already sorted by subject, set the sort sequence to be descending
			state.setCurrentSortedBy(field);
			state.setCurrentSortAsc(true);
			sstate.setAttribute(STATE_CURRENT_SORT_ASC, Boolean.FALSE);
		}
	} // setupSort

	/**
	 * Do sort by subject
	 */
	public void doSortbysubject(RunData rundata, Context context)
	{
		setupSort(rundata, context, SORT_SUBJECT);
	} // doSortbysubject

	/**
	 * Do sort by from - the author
	 */
	public void doSortbyfrom(RunData rundata, Context context)
	{
		setupSort(rundata, context, SORT_FROM);
	} // doSortbyfrom

	/**
	 * Do sort by public
	 */
	public void doSortbypublic(RunData rundata, Context context)
	{
		setupSort(rundata, context, SORT_PUBLIC);
	} // doSortbypublic

	/**
	 * Do sort by the date of the announcement.
	 */
	public void doSortbydate(RunData rundata, Context context)
	{
		setupSort(rundata, context, SORT_DATE);
	} // doSortbydate

	public void doSortbyreleasedate(RunData rundata, Context context)
	{
		setupSort(rundata, context, SORT_RELEASEDATE);
		
	} // doSortbyreleasedate

	public void doSortbyretractdate(RunData rundata, Context context)
	{
		setupSort(rundata, context, SORT_RETRACTDATE);
		
	} // doSortbyreleasedate

	/**
	 * Do sort by the announcement channel name.
	 */
	public void doSortbychannel(RunData rundata, Context context)
	{
		setupSort(rundata, context, SORT_CHANNEL);
	} // doSortbydate

	/**
	 * Do sort by for - grouop/site/public
	 */
	public void doSortbyfor(RunData rundata, Context context)
	{
		setupSort(rundata, context, SORT_FOR);
	} // doSortbyfor

	/**
	 * Do sort by group title
	 */
	public void doSortbygrouptitle(RunData rundata, Context context)
	{
		setupSort(rundata, context, SORT_GROUPTITLE);
	} // doSortbygrouptitle

	/**
	 * Do sort by group description
	 */
	public void doSortbygroupdescription(RunData rundata, Context context)
	{
		setupSort(rundata, context, SORT_GROUPDESCRIPTION);
	} // doSortbygroupdescription

	private class AnnouncementComparator implements Comparator
	{
		// the criteria
		String m_criteria = null;

		// the criteria - asc
		boolean m_asc = true;

		/**
		 * constructor
		 * 
		 * @param criteria
		 *        The sort criteria string
		 * @param asc
		 *        The sort order string. "true" if ascending; "false" otherwise.
		 */
		public AnnouncementComparator(String criteria, boolean asc)
		{
			m_criteria = criteria;
			m_asc = asc;

		} // constructor

		/**
		 * implementing the compare function
		 * 
		 * @param o1
		 *        The first object
		 * @param o2
		 *        The second object
		 * @return The compare result. 1 is o1 < o2; -1 otherwise
		 */
		public int compare(Object o1, Object o2)
		{
			int result = -1;

			if (m_criteria.equals(SORT_SUBJECT))
			{
				// sorted by the discussion message subject
				result = Collator.getInstance().compare(((AnnouncementMessage) o1).getAnnouncementHeader().getSubject(),
						((AnnouncementMessage) o2).getAnnouncementHeader().getSubject());
			}
			else if (m_criteria.equals(SORT_DATE))
			{
				// sorted by the discussion message date
				if (((AnnouncementMessage) o1).getAnnouncementHeader().getDate().before(
						((AnnouncementMessage) o2).getAnnouncementHeader().getDate()))
				{
					result = -1;
				}
				else
				{
					result = 1;
				}
			}
			else if (m_criteria.equals(SORT_RELEASEDATE))
			{
				Time o1releaseDate = null;
				Time o2releaseDate = null;
				
				try
				{
					o1releaseDate = ((AnnouncementMessage) o1).getProperties().getTimeProperty(AnnouncementService.RELEASE_DATE);
				}
				catch (Exception e) 
				{
					// release date not set, go on
				}

				try 
				{
					o2releaseDate = ((AnnouncementMessage) o2).getProperties().getTimeProperty(AnnouncementService.RELEASE_DATE);
				}
				catch (Exception e) 
				{
					// release date not set, go on
				}

				if (o1releaseDate != null && o2releaseDate != null) 
				{
					// sorted by the discussion message date
					if (o1releaseDate.before(o2releaseDate))
					{
						result = -1;
					}
					else
					{
						result = 1;
					}
				}
				else if (o1releaseDate == null)
				{
					return 1;
				}
				else
				{
					return -1;
				}
			}
			else if (m_criteria.equals(SORT_RETRACTDATE))
			{
				Time o1retractDate = null;
				Time o2retractDate = null;
				
				try
				{
					o1retractDate = ((AnnouncementMessage) o1).getProperties().getTimeProperty(AnnouncementService.RETRACT_DATE);
				}
				catch (Exception e) 
				{
					// release date not set, go on
				}

				try 
				{
					o2retractDate = ((AnnouncementMessage) o2).getProperties().getTimeProperty(AnnouncementService.RETRACT_DATE);
				}
				catch (Exception e) 
				{
					// release date not set, go on
				}

				if (o1retractDate != null && o2retractDate != null) 
				{
					// sorted by the discussion message date
					if (o1retractDate.before(o2retractDate))
					{
						result = -1;
					}
					else
					{
						result = 1;
					}
				}
				else if (o1retractDate == null)
				{
					return 1;
				}
				else
				{
					return -1;
				}
			}
			else if (m_criteria.equals(SORT_FROM))
			{
				// sorted by the discussion message subject
				result = Collator.getInstance().compare(((AnnouncementMessage) o1).getAnnouncementHeader().getFrom().getSortName(),
						((AnnouncementMessage) o2).getAnnouncementHeader().getFrom().getSortName());
			}
			else if (m_criteria.equals(SORT_CHANNEL))
			{
				// sorted by the channel name.
				result = Collator.getInstance().compare(((AnnouncementWrapper) o1).getChannelDisplayName(),
						((AnnouncementWrapper) o2).getChannelDisplayName());
			}
			else if (m_criteria.equals(SORT_PUBLIC))
			{
				// sorted by the public view attribute
				String factor1 = ((AnnouncementMessage) o1).getProperties().getProperty(ResourceProperties.PROP_PUBVIEW);
				if (factor1 == null) factor1 = "false";
				String factor2 = ((AnnouncementMessage) o2).getProperties().getProperty(ResourceProperties.PROP_PUBVIEW);
				if (factor2 == null) factor2 = "false";
				result = Collator.getInstance().compare(factor1,factor2);
			}
			else if (m_criteria.equals(SORT_FOR))
			{
				// sorted by the public view attribute
				String factor1 = ((AnnouncementWrapper) o1).getRange();
				String factor2 = ((AnnouncementWrapper) o2).getRange();
				result = Collator.getInstance().compare(factor1,factor2);
			}
			else if (m_criteria.equals(SORT_GROUPTITLE))
			{
				// sorted by the group title
				String factor1 = ((Group) o1).getTitle();
				String factor2 = ((Group) o2).getTitle();
				result = Collator.getInstance().compare(factor1,factor2);
			}
			else if (m_criteria.equals(SORT_GROUPDESCRIPTION))
			{
				// sorted by the group title
				String factor1 = ((Group) o1).getDescription();
				String factor2 = ((Group) o2).getDescription();
				if (factor1 == null)
				{
					factor1 = "";
				}
				if (factor2 == null)
				{
					factor2 = "";
				}
				result = Collator.getInstance().compare(factor1,factor2);
			}

			// sort ascending or descending
			if (!m_asc)
			{
				result = -result;
			}
			return result;

		} // compare

	} // AnnouncementComparator

	// ********* ending for sorting *********

	/**
	 * Action is to parse the function calls
	 */
	/*
	 * public void doParse_list_announcement(RunData data, Context context) { SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid()); ParameterParser params = data.getParameters(); String source =
	 * params.getString("source"); if (source.equalsIgnoreCase("new")) { // create new announcement doNewannouncement(data, context); } else if (source.equalsIgnoreCase("revise")) { // revise announcement doReviseannouncementfrommenu(data, context); }
	 * else if (source.equalsIgnoreCase("delete")) { // delete announcement doDeleteannouncement(data, context); } } // doParse_list_announcement
	 */

	private int seperatorMatrix(boolean a, boolean b, boolean c)
	{
		int i = 0;
		if (a) i = i + 100;
		if (b) i = i + 10;
		if (c) i = i + 1;

		if (i == 111) return 11;
		if ((i == 110) || (i == 101)) return 10;
		if (i == 11) return 1;
		if ((i == 100) || (i == 10) || (i == 1) || (i == 0)) return 0;
		return 11;
	}

	/**
	 * Build the menu.
	 */
	private void buildMenu(VelocityPortlet portlet, Context context, RunData rundata, AnnouncementActionState state,
			boolean menu_new, boolean menu_delete, boolean menu_revise, boolean menu_merge, boolean menu_permissions,
			boolean menu_options, AnnouncementActionState.DisplayOptions displayOptions)
	{
		Menu bar = new MenuImpl(portlet, rundata, "AnnouncementAction");
		boolean buttonRequiringCheckboxesPresent = false;

		//if (!displayOptions.isShowOnlyOptionsButton()) ##SAK-13434
		if (displayOptions != null && !displayOptions.isShowOnlyOptionsButton())
		{
			String statusName = state.getStatus();
			if (statusName != null)
			{
				if (statusName.equals("showMetadata"))
				{
					boolean s1 = true;
					boolean s2 = true;
					// int m = seperatorMatrix(menu_new, menu_delete, menu_revise);
					int m = seperatorMatrix(menu_new, menu_revise, menu_delete);
					if (m == 10) s2 = false; // 10
					if (m == 1) s1 = false; // 01
					if (m == 0) s1 = s2 = false; // 00

					bar.add(new MenuEntry(rb.getString("gen.new"), null, menu_new, MenuItem.CHECKED_NA, "doNewannouncement"));
					if (s1) bar.add(new MenuDivider());
					bar.add(new MenuEntry(rb.getString("gen.revise"), null, menu_revise, MenuItem.CHECKED_NA,
							"doReviseannouncementfrommenu"));
					if (s2) bar.add(new MenuDivider());
					bar.add(new MenuEntry(rb.getString("gen.delete2"), null, menu_delete, MenuItem.CHECKED_NA,
							"doDeleteannouncement"));
					buttonRequiringCheckboxesPresent = true;
				}
				else
				{
					bar.add(new MenuEntry(rb.getString("gen.new"), null, menu_new, MenuItem.CHECKED_NA, "doNewannouncement"));
					buttonRequiringCheckboxesPresent = true;

				} // if (statusName.equals("showMetadata"))
			}
			else
			{
				bar.add(new MenuEntry(rb.getString("gen.new"), null, menu_new, MenuItem.CHECKED_NA, "doNewannouncement"));
				buttonRequiringCheckboxesPresent = true;
			} // if-else (statusName != null)

			// add merge button, if allowed
			if (menu_merge)
			{
				bar.add(new MenuEntry(rb.getString("java.merge"), MERGE_BUTTON_HANDLER));
			}
		} // if-else (!displayOptions.isShowOnlyOptionsButton())

		// add options if allowed
		if (menu_options)
		{
			addOptionsMenu(bar, (JetspeedRunData) rundata);
		}

		// let the permissions button to be the last one in the toolbar
		if (displayOptions != null && !displayOptions.isShowOnlyOptionsButton())
		{
			// add permissions, if allowed
			if (menu_permissions)
			{
				bar.add(new MenuEntry(rb.getString("java.permissions"), PERMISSIONS_BUTTON_HANDLER));
			}
		}

		// Set menu state attribute
		SessionState stateForMenus = ((JetspeedRunData) rundata).getPortletSessionState(portlet.getID());
		stateForMenus.setAttribute(MenuItem.STATE_MENU, bar);

		Iterator it = bar.getItems().iterator();

		// See if we have any enabled menu items.
		boolean enabledItemExists = false;

		while (it.hasNext())
		{
			MenuItem menuItem = (MenuItem) it.next();
			if (menuItem.getIsEnabled())
			{
				enabledItemExists = true;
				break;
			}
		}

		// Set a flag in the context to indicate that at least one menu item is enabled.
		context.put(ENABLED_MENU_ITEM_EXISTS, Boolean.valueOf(enabledItemExists));

		context.put(CONTEXT_ENABLE_ITEM_CHECKBOXES, Boolean.valueOf(enabledItemExists && buttonRequiringCheckboxesPresent));
		context.put(CONTEXT_ENABLED_MENU_ITEM_EXISTS, Boolean.valueOf(enabledItemExists));

		context.put(Menu.CONTEXT_MENU, bar);
		context.put(Menu.CONTEXT_ACTION, "AnnouncementAction");
		context.put("tlang", rb);

	} // buildMenu

	/*
	 * what i've done to make this tool automaticlly updated includes some corresponding imports in buildMail, tell observer just the page is just refreshed in the do() functions related to show the list, enable the obeserver in other do() functions
	 * related to not show the list, disable the obeserver in the do(), define the session sstate object, and protlet. add initState add updateObservationOfChannel() add state attribute STATE_CHANNEL_REF
	 */

	/**
	 * Populate the state object, if needed.
	 */
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata)
	{
		super.initState(state, portlet, rundata);
		
		if (contentHostingService == null)
		{
			contentHostingService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
		}
		
		if (assignmentService == null)
		{
			assignmentService = (AssignmentService) ComponentManager.get("org.sakaiproject.assignment.api.AssignmentService");
		}

		// retrieve the state from state object
		AnnouncementActionState annState = (AnnouncementActionState) getState(portlet, rundata, AnnouncementActionState.class);

		// get the current channel ID from state object or prolet initial parameter
		String channelId = annState.getChannelId();
		if (channelId == null)
		{
			// try the portlet parameter
			channelId = StringUtil.trimToNull(portlet.getPortletConfig().getInitParameter("channel"));
			if (channelId == null)
			{
				// form based on the request's context's "main" channel
				channelId = AnnouncementService.channelReference(ToolManager.getCurrentPlacement().getContext(),
						SiteService.MAIN_CONTAINER);
			}

			// let the state object have the current channel id
			annState.setChannelId(channelId);
			annState.setIsListVM(true);
		}
		state.setAttribute(STATE_CHANNEL_REF, channelId);
		
		if (state.getAttribute(STATE_SELECTED_VIEW) == null)
		{
			state.setAttribute(STATE_SELECTED_VIEW, VIEW_MODE_ALL);
		}

		// // get the current collection ID from state object or prolet initial parameter
		// String collectionId = annState.getCollectionId();
		// if (collectionId == null)
		// {
		// // get the current channel ID for prolet initial parameter
		// collectionId = StringUtil.trimToNull(portlet.getPortletConfig().getInitParameter("collection"));
		// if (collectionId == null)
		// collectionId = ContentHostingService.getSiteCollection(PortalService.getCurrentSiteId());
		//
		// // let the state object have the current channel id
		// annState.setCollectionId(collectionId);
		// }

		// String channel = StringUtil.trimToNull(config.getInitParameter(PARAM_CHANNEL));
		// setup the observer to notify our main panel
		if (state.getAttribute(STATE_INITED) == null)
		{
			state.setAttribute(STATE_INITED, STATE_INITED);

			// check if the channel is marked public read
			if (SecurityService.unlock(UserDirectoryService.getAnonymousUser(), AnnouncementService.SECURE_ANNC_READ, channelId))
			{
				state.setAttribute(STATE_CHANNEL_PUBVIEW, STATE_CHANNEL_PUBVIEW);
			}

			// // the delivery location for this tool
			// String deliveryId = clientWindowId(state, portlet.getID());
			//
			// // the html element to update on delivery
			// String elementId = mainPanelUpdateId(portlet.getID());
			//
			// // the event resource reference pattern to watch for
			// Reference r = new Reference(channelId);
			// String pattern = AnnouncementService.messageReference(r.getContext(), r.getId(), "");
			//
			// ObservingCourier observer = new ObservingCourier(deliveryId, elementId, pattern);
			//			
			// state.setAttribute(STATE_OBSERVER, observer);

			MergedList mergedAnnouncementList = new MergedList();

			String[] channelArrayFromConfigParameterValue = null;

			// TODO - MERGE FIX
			// Figure out the list of channel references that we'll be using.
			// If we're on the workspace tab, we get everything.
			// Don't do this if we're the super-user, since we'd be
			// overwhelmed.
			if (isOnWorkspaceTab() && !SecurityService.isSuperUser())
			{
				channelArrayFromConfigParameterValue = mergedAnnouncementList
						.getAllPermittedChannels(new AnnouncementChannelReferenceMaker());
			}
			else
			{
				// Get the list of merged announcement sources.
				channelArrayFromConfigParameterValue = mergedAnnouncementList.getChannelReferenceArrayFromDelimitedString(annState
						.getChannelId(), portlet.getPortletConfig().getInitParameter(
						getPortletConfigParameterNameForLoadOnly(portlet)));
			}

			mergedAnnouncementList.loadChannelsFromDelimitedString(isOnWorkspaceTab(), new MergedListEntryProviderFixedListWrapper(
					new EntryProvider(), annState.getChannelId(), channelArrayFromConfigParameterValue,
					new AnnouncementReferenceToChannelConverter()),
					StringUtil.trimToZero(SessionManager.getCurrentSessionUserId()), channelArrayFromConfigParameterValue,
					SecurityService.isSuperUser(), ToolManager.getCurrentPlacement().getContext());
		}

		// Set up or display options if we haven't done it yet.
		if (annState.getDisplayOptions() == null)
		{
			loadDisplayOptionsFromPortletConfig(portlet, annState);
		}

		// default is to not disable the public selection - FALSE
		state.setAttribute(PUBLIC_DISPLAY_DISABLE_BOOLEAN, Boolean.FALSE);

		Site site = null;
		try
		{
			site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
			String[] disableStrgs = ServerConfigurationService.getStrings("prevent.public.announcements");
			if (disableStrgs != null)
			{
				for (int i = 0; i < disableStrgs.length; i++)
				{
					if ((StringUtil.trimToZero(disableStrgs[i])).equals(site.getType()))
						state.setAttribute(PUBLIC_DISPLAY_DISABLE_BOOLEAN, Boolean.TRUE);;
				}
			}
		}
		catch (IdUnusedException e)
		{
		}
		catch (NullPointerException e)
		{
		}

	} // initState

	// /**
	// * Adds the merged sites to the list of events that we're interested
	// * in watching.
	// */
	// private void addMergedAnnouncementsToObserver(MergedList mergedAnnouncementList, AnnouncementActionState annState, ObservingCourier observer)
	// {
	// Iterator it = mergedAnnouncementList.iterator();
	//		
	// while (it.hasNext())
	// {
	// MergedList.MergedEntry entry = (MergedList.MergedEntry) it.next();
	//
	// if ( entry.isMerged() )
	// {
	// Reference ref = new Reference(entry.getReference());
	//
	// String pattern =
	// AnnouncementService.messageReference(
	// ref.getContext(),
	// ref.getId(),
	// "");
	//	
	// // observer.addResourcePattern(pattern);
	// }
	// }
	// }

	/**
	 * Loads the display options object we save in the ActionState with the settings from the PortletConfig.
	 */
	private void loadDisplayOptionsFromPortletConfig(VelocityPortlet portlet, AnnouncementActionState annState)
	{
		AnnouncementActionState.DisplayOptions displayOptions = new AnnouncementActionState.DisplayOptions();
		annState.setDisplayOptions(displayOptions);

		//PortletConfig portletConfig = portlet.getPortletConfig();
		//displayOptions.loadProperties(portletConfig.getInitParameters());
		Properties registeredProperties = ToolManager.getCurrentTool().getRegisteredConfig();
		displayOptions.loadProperties((Map)registeredProperties);
		

	} // initState

	/**
	 * Setup our observer to be watching for change events for our channel.
	 */
	private void updateObservationOfChannel(MergedList mergedAnnouncementList, RunData runData, SessionState state,
			AnnouncementActionState annState)
	{
		// String peid = ((JetspeedRunData) runData).getJs_peid();
		//		
		// ObservingCourier observer =
		// (ObservingCourier) state.getAttribute(STATE_OBSERVER);
		//
		// addMergedAnnouncementsToObserver(mergedAnnouncementList, annState, observer);
		//
		// // the delivery location for this tool
		// String deliveryId = clientWindowId(state, peid);
		// observer.setDeliveryId(deliveryId);

	} // updateObservationOfChannel

	/**
	 * Fire up the permissions editor
	 */
	public void doPermissions(RunData data, Context context)
	{
		// get into helper mode with this helper tool
		startHelper(data.getRequest(), "sakai.permissions.helper");

		// setup the parameters for the helper
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		AnnouncementActionState stateObj = (AnnouncementActionState) getState(context, data, AnnouncementActionState.class);

		String channelRefStr = stateObj.getChannelId();
		Reference channelRef = EntityManager.newReference(channelRefStr);
		String siteRef = SiteService.siteReference(channelRef.getContext());

		// setup for editing the permissions of the site for this tool, using the roles of this site, too
		state.setAttribute(PermissionsHelper.TARGET_REF, siteRef);

		// ... with this description
		state.setAttribute(PermissionsHelper.DESCRIPTION, rb.getString("java.set")
				+ SiteService.getSiteDisplay(channelRef.getContext()));

		// ... showing only locks that are prpefixed with this
		state.setAttribute(PermissionsHelper.PREFIX, "annc.");

		// load the permissions.properties file
		ResourceLoader pRb = new ResourceLoader("permissions");
		HashMap<String, String> pRbValues = new HashMap<String, String>();
		for (Iterator iKeys = pRb.keySet().iterator();iKeys.hasNext();)
		{
		String key = (String) iKeys.next();
		pRbValues.put(key, (String) pRb.get(key));

		}
		state.setAttribute("permissionDescriptions", pRbValues);

	}

	/**
	 * Handle the "Merge" button on the toolbar
	 */
	public void doMerge(RunData runData, Context context)
	{
		AnnouncementActionState state = (AnnouncementActionState) getState(context, runData, AnnouncementActionState.class);
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState sstate = ((JetspeedRunData) runData).getPortletSessionState(peid);

		doOptions(runData, context);

		// if we didn't end up in options mode, bail out
		if (!MODE_OPTIONS.equals(sstate.getAttribute(STATE_MODE))) return;

		// Disable the observer
		enableObserver(sstate, false);

		state.setStatus(MERGE_STATUS);
	} // doMerge

	/**
	 * Handles the user clicking on the save button on the page to specify which calendars will be merged into the present schedule.
	 */
	public void doUpdate(RunData runData, Context context)
	{
		AnnouncementActionState state = (AnnouncementActionState) getState(context, runData, AnnouncementActionState.class);

		if (state.getStatus().equals(MERGE_STATUS))
		{
			doMergeUpdate(runData, context);
		}
		else if (state.getStatus().equals(OPTIONS_STATUS))
		{
			doOptionsUpdate(runData, context);
		}
		else
		{
			M_log.debug(this + ".doUpdate - Unexpected status");
		}
	}

	/**
	 * This handles the "doUpdate" if we're processing an update from the "merge" page.
	 */
	private void doMergeUpdate(RunData runData, Context context)
	{
		AnnouncementActionState state = (AnnouncementActionState) getState(context, runData, AnnouncementActionState.class);
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState sstate = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// Get the merged calendar list out of our session state
		MergedList mergedChannelList = (MergedList) sstate.getAttribute(SSTATE_ATTRIBUTE_MERGED_CHANNELS);

		if (mergedChannelList != null)
		{
			// Get the information from the run data and load it into
			// our calendar list that we have in the session state.
			mergedChannelList.loadFromRunData(runData.getParameters());
		}
		else
		{
			if (M_log.isDebugEnabled())
			{
				M_log.debug(this + ".doUpdate mergedChannelList == null");
			}
		}

		// update the tool config
		Placement placement = ToolManager.getCurrentPlacement();

		// Make sure that the older, incorrectly named paramter is gone.
		placement.getPlacementConfig().remove(PORTLET_CONFIG_PARM_NON_MERGED_CHANNELS);

		if (mergedChannelList != null)
		{
			placement.getPlacementConfig().setProperty(PORTLET_CONFIG_PARM_MERGED_CHANNELS,
					mergedChannelList.getDelimitedChannelReferenceString());
		}
		else
		{
			placement.getPlacementConfig().remove(PORTLET_CONFIG_PARM_MERGED_CHANNELS);
		}

		// commit the change
		saveOptions();

		updateObservationOfChannel(mergedChannelList, runData, sstate, state);

		// Turn the observer back on.
		enableObserver(sstate, true);

		state.setStatus(null);

		sstate.removeAttribute(STATE_MODE);
		
		state.setStatus(CANCEL_STATUS); //SAK-14001	It goes to the main page after saving the merge options.
		
	}

	/**
	 * This handles the "doUpdate" if we're in a processing an update from the options page.
	 */
	public void doOptionsUpdate(RunData runData, Context context)
	{
		AnnouncementActionState state = (AnnouncementActionState) getState(context, runData, AnnouncementActionState.class);
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState sstate = ((JetspeedRunData) runData).getPortletSessionState(peid);

		doUpdateDisplayOptions(runData, state, sstate);

		try
		{
			String alias = StringUtil.trimToNull(runData.getParameters().getString("rssAlias"));
			Reference anncRef = AnnouncementService.getAnnouncementReference(ToolManager.getCurrentPlacement().getContext());
		
			List aliasList =	AliasService.getAliases( anncRef.getReference() );
			String oldAlias = null;
			if ( ! aliasList.isEmpty() )
			{
				String aliasSplit[] = ((Alias)aliasList.get(0)).getId().split("\\.");
				oldAlias =  aliasSplit[0];
			}
		
			// Add the desired alias (if changed)
			if ( alias != null && (oldAlias == null || !oldAlias.equals(alias)) )
			{
				// first, clear any alias set to this channel
				AliasService.removeTargetAliases(anncRef.getReference());
					
            alias += ".rss";
				AliasService.setAlias(alias, anncRef.getReference());
			}
		}
		catch (IdUsedException ue)
		{
			addAlert(sstate, rb.getString("java.alert.dupalias"));
		}
		catch (Exception e)
		{
			addAlert(sstate, rb.getString("java.alert.unknown"));
			M_log.warn(this+".doOptionsUpdate", e);
		}
		
		// We're omitting processing of the "showAnnouncementBody" since these
		// options are currently mutually exclusive.

		// commit the change
		saveOptions();

		// Turn the observer back on.
		enableObserver(sstate, true);

		//state.setStatus(null);

		sstate.removeAttribute(STATE_MODE);
		
		/*
		 * SAK-13116 If we are in the synoptic view, we want some validation so
		 * that is not possible to set the Number of Announcements to greater 
		 * than 20, since only 20 will be displayed no matter what.
		 */
		if (isSynopticTool() && state.getDisplayOptions().getNumberOfAnnouncements() > 20) 
		{
			addAlert(sstate, rb.getFormattedMessage("java.alert.customsize", new Object[] { 20 }));
			state.setStatus(OPTIONS_STATUS); //If the display option is more than 20, then go back to the options page
		}
		else
		{		
		state.setStatus(CANCEL_STATUS); //SAK-14001	It goes to the main page after updating the Options.
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.chefproject.actions.VelocityPortletPaneledAction#doOptions(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
	 */
	public void doOptions(RunData runData, Context context)
	{
		super.doOptions(runData, context);

		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState sstate = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// go to option editing if no error message which means the option editing is locked successfully
		// otherwise, stay at whatever state we are at
		String msg = (String) sstate.getAttribute(STATE_MESSAGE);
		if ((msg == null) || (msg.equals("")))
		{
			// Keep track of our state
			AnnouncementActionState state = (AnnouncementActionState) getState(context, runData, AnnouncementActionState.class);
			state.setStatus(OPTIONS_STATUS);
		}
	} // doOptions

	private void doUpdateDisplayOptions(RunData runData, AnnouncementActionState state, SessionState sstate)
	{
		AnnouncementActionState.DisplayOptions displayOptions = state.getDisplayOptions();

		ParameterParser parameters = runData.getParameters();

		displayOptions.loadProperties(parameters);

		// update the tool config
		Placement placement = ToolManager.getCurrentPlacement();

		displayOptions.saveProperties(placement.getPlacementConfig());

	} // doUpdateDisplayOptions

	/**
	 * is notification enabled?
	 */
	protected boolean notificationEnabled(AnnouncementActionState state)
	{
		// if it is motd, it does not send notification to user, hence the notification option is disabled. SAK-4559
		if (state.getChannelId().contains("motd"))
		{
				return false;
		}
		return true;
	} // notificationEnabled

	/**
	 * Processes formatted text that is coming back from the browser (from the formatted text editing widget).
	 * 
	 * @param state
	 *        Used to pass in any user-visible alerts or errors when processing the text
	 * @param strFromBrowser
	 *        The string from the browser
	 * @return The formatted text
	 */
	private String processFormattedTextFromBrowser(SessionState state, String strFromBrowser)
	{
		StringBuilder alertMsg = new StringBuilder();
		try
		{
			String text = FormattedText.processFormattedText(strFromBrowser, alertMsg);
			if (alertMsg.length() > 0) addAlert(state, alertMsg.toString());
			return text;
		}
		catch (Exception e)
		{
			M_log.warn( this + ".processFormattedTextFromBrowser ", e);
			return strFromBrowser;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.cheftool.PagedResourceActionII#readResourcesPage(org.sakaiproject.service.framework.session.SessionState, int, int)
	 */
	protected List readResourcesPage(SessionState state, int first, int last)
	{
		List rv = (List) state.getAttribute("messages");
		if (rv == null) return new Vector();

		String sortedBy = "";
		if (state.getAttribute(STATE_CURRENT_SORTED_BY) != null) sortedBy = state.getAttribute(STATE_CURRENT_SORTED_BY).toString();

		boolean asc = false;
		if (state.getAttribute(STATE_CURRENT_SORT_ASC) != null)
			asc = ((Boolean) state.getAttribute(STATE_CURRENT_SORT_ASC)).booleanValue();

		if ((sortedBy == null) || sortedBy.equals(""))
		{
			sortedBy = "date";
			asc = false;
		}
		SortedIterator rvSorted = new SortedIterator(rv.iterator(), new AnnouncementComparator(sortedBy, asc));

		PagingPosition page = new PagingPosition(first, last);
		page.validate(rv.size());

		Vector subrv = new Vector();
		for (int index = 0; index < rv.size(); index++)
		{
			if ((index >= (page.getFirst() - 1)) && index < page.getLast())
				subrv.add(rvSorted.next());
			else
				rvSorted.next();
		}
		return subrv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.cheftool.PagedResourceActionII#sizeResources(org.sakaiproject.service.framework.session.SessionState)
	 */
	protected int sizeResources(SessionState state)
	{
		List rv = (List) state.getAttribute("messages");
		if (rv == null) return 0;

		return rv.size();
	}

	// toggle through different views
	public void doView(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		String viewMode = data.getParameters().getString("view");
		state.setAttribute(STATE_SELECTED_VIEW, viewMode);

		if (viewMode.equals(VIEW_MODE_ALL))
		{

		}
		else if (viewMode.equals(VIEW_MODE_PUBLIC))
		{

		}
		else if (viewMode.equals(VIEW_MODE_BYGROUP))
		{
			state.setAttribute(STATE_CURRENT_SORTED_BY, SORT_FOR);
			state.setAttribute(STATE_CURRENT_SORT_ASC, Boolean.TRUE);
		}
		else if (viewMode.equals(VIEW_MODE_MYGROUPS))
		{

		}

		// we are changing the view, so start with first page again.
		resetPaging(state);

		// clear search form
		doSearch_clear(data, context);

	} // doView

	// ********
	// ******** functions copied from VelocityPortletStateAction ********
	// ********
	/**
	 * Get the proper state for this instance (if portlet is not known, only context).
	 * 
	 * @param context
	 *        The Template Context (it contains a reference to the portlet).
	 * @param rundata
	 *        The Jetspeed (Turbine) rundata associated with the request.
	 * @param stateClass
	 *        The Class of the ControllerState to find / create.
	 * @return The proper state object for this instance.
	 */
	protected ControllerState getState(Context context, RunData rundata, Class stateClass)
	{
		return getState(((JetspeedRunData) rundata).getJs_peid(), rundata, stateClass);

	} // getState

	/**
	 * Get the proper state for this instance (if portlet is known).
	 * 
	 * @param portlet
	 *        The portlet being rendered.
	 * @param rundata
	 *        The Jetspeed (Turbine) rundata associated with the request.
	 * @param stateClass
	 *        The Class of the ControllerState to find / create.
	 * @return The proper state object for this instance.
	 */
	protected ControllerState getState(VelocityPortlet portlet, RunData rundata, Class stateClass)
	{
		if (portlet == null)
		{
			M_log.warn(this + ".getState(): portlet null");
			return null;
		}

		return getState(portlet.getID(), rundata, stateClass);

	} // getState

	/**
	 * Get the proper state for this instance (if portlet id is known).
	 * 
	 * @param peid
	 *        The portlet id.
	 * @param rundata
	 *        The Jetspeed (Turbine) rundata associated with the request.
	 * @param stateClass
	 *        The Class of the ControllerState to find / create.
	 * @return The proper state object for this instance.
	 */
	protected ControllerState getState(String peid, RunData rundata, Class stateClass)
	{
		if (peid == null)
		{
			M_log.warn(this + ".getState(): peid null");
			return null;
		}

		try
		{
			// get the PortletSessionState
			SessionState ss = ((JetspeedRunData) rundata).getPortletSessionState(peid);

			// get the state object
			ControllerState state = (ControllerState) ss.getAttribute("state");

			if (state != null) return state;

			// if there's no "state" object in there, make one
			state = (ControllerState) stateClass.newInstance();
			state.setId(peid);

			// remember it!
			ss.setAttribute("state", state);

			return state;
		}
		catch (Exception e)
		{
			M_log.warn(this+ ".getState", e);
		}

		return null;

	} // getState

	/**
	 * Release the proper state for this instance (if portlet is not known, only context).
	 * 
	 * @param context
	 *        The Template Context (it contains a reference to the portlet).
	 * @param rundata
	 *        The Jetspeed (Turbine) rundata associated with the request.
	 */
	protected void releaseState(Context context, RunData rundata)
	{
		releaseState(((JetspeedRunData) rundata).getJs_peid(), rundata);

	} // releaseState

	/**
	 * Release the proper state for this instance (if portlet is known).
	 * 
	 * @param portlet
	 *        The portlet being rendered.
	 * @param rundata
	 *        The Jetspeed (Turbine) rundata associated with the request.
	 */
	protected void releaseState(VelocityPortlet portlet, RunData rundata)
	{
		releaseState(portlet.getID(), rundata);

	} // releaseState

	/**
	 * Release the proper state for this instance (if portlet id is known).
	 * 
	 * @param peid
	 *        The portlet id being rendered.
	 * @param rundata
	 *        The Jetspeed (Turbine) rundata associated with the request.
	 */
	protected void releaseState(String peid, RunData rundata)
	{
		try
		{
			// get the PortletSessionState
			SessionState ss = ((JetspeedRunData) rundata).getPortletSessionState(peid);

			// get the state object
			ControllerState state = (ControllerState) ss.getAttribute("state");

			// recycle the state object
			state.recycle();

			// clear out the SessionState for this Portlet
			ss.removeAttribute("state");

			ss.clear();

		}
		catch (Exception e)
		{
			M_log.warn("", e);
		}

	} // releaseState

	// ******* end of copy from VelocityPortletStateAction

}
