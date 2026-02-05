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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.announcement.tool;

import java.time.Duration;
import java.time.Instant;
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
import java.util.Optional;
import java.util.Properties;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementChannelEdit;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageEdit;
import org.sakaiproject.announcement.api.AnnouncementMessageHeaderEdit;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.announcement.tool.MenuBuilder.ActiveTab;
import org.sakaiproject.announcement.tool.AnnouncementActionState;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.ControllerState;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PagedResourceActionII;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.cover.ContentTypeImageService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.message.api.Message;
import org.sakaiproject.message.api.MessageHeader;
import org.sakaiproject.message.api.MessageHeaderEdit;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.MergedList;
import org.sakaiproject.util.MergedListEntryProviderBase;
import org.sakaiproject.util.MergedListEntryProviderFixedListWrapper;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.SortedIterator;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.event.api.Event;

/**
 * AnnouncementAction is an implementation of Announcement service, which provides the complete function of announcements. User could check the announcements, create own new and manage all the announcement items, under certain permission check.
 */
@Slf4j
public class AnnouncementAction extends PagedResourceActionII
{
	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("announcement");

	public static final String CONTEXT_ENABLED_MENU_ITEM_EXISTS = "EnabledMenuItemExists";

	public static final String CONTEXT_ENABLE_ITEM_CHECKBOXES = "EnableItemCheckBoxes";

	public static final String ENABLED_MENU_ITEM_EXISTS = CONTEXT_ENABLED_MENU_ITEM_EXISTS;

	private static final String NOT_SELECTED_FOR_REVISE_STATUS = "noSelectedForRevise";

	private static final String FINISH_BULK_OPERATION_STATUS = "FinishDeleting";

	private static final String DELETE_ANNOUNCEMENT_STATUS = "deleteAnnouncement";

	private static final String PUBLISH_STATUS = "publish";

	private static final String UNPUBLISH_STATUS = "unpublish";

	private static final String POST_STATUS = "post";

	private static final String CANCEL_STATUS = "cancel";

	private static final String MERGE_STATUS = "merge";
	
	private static final String REORDER_STATUS = "reorder";

	private static final String OPTIONS_STATUS = "options";

	private static final String LIST_STATUS = "view";

	private static final String VIEW_STATUS = "showMetadata";

	private static final String ADD_STATUS = "new";

	private static final String EDIT_STATUS = "goToReviseAnnouncement";

	private static final String BACK_TO_EDIT_STATUS = "backToReviseAnnouncement";

	private static final String DELETE_STATUS = "deleteAnnouncement";

	private static final String SSTATE_NOTI_VALUE = "noti_value";

	private static final String SSTATE_PUBLICVIEW_VALUE = "public_view_value";

	public static final String SORT_DATE = "date";
	
	public static final String SORT_MESSAGE_ORDER = "message_order";
	
	public static final String SORT_RELEASEDATE = "releasedate";
	
	public static final String SORT_RETRACTDATE = "retractdate";

	public static final String SORT_PUBLIC = "public";

	public static final String SORT_FROM = "from";

	public static final String SORT_SUBJECT = "subject";

	public static final String SORT_CHANNEL = "channel";

	public static final String SORT_FOR = "for";


	private static final String CONTEXT_VAR_DISPLAY_OPTIONS = "displayOptions";

	private static final String VELOCITY_DISPLAY_OPTIONS = CONTEXT_VAR_DISPLAY_OPTIONS;

	public static final String PERMISSIONS_BUTTON_HANDLER = "doPermissions";
	
	public static final String REORDER_BUTTON_HANDLER = "doReorder";
	
	public static final String REFRESH_BUTTON_HANDLER = "doCancel";

	public static final String MERGE_BUTTON_HANDLER = "doMerge";

	private static final String SSTATE_ATTRIBUTE_MERGED_CHANNELS = "mergedChannels";

	private static final String VELOCITY_MERGED_CHANNEL_LIST = "mergedAnnouncementsCollection";

	/** state attribute names. */
	private static final String STATE_CHANNEL_REF = "channelId";
	
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
    private static final String VIEW_MODE_BYROLE  = "view.byrole";
    private static final String VIEW_MODE_MYGROUPS = "view.mygroups";

    /** The number of days, by default, before retraction. */
    private static final long FUTURE_DAYS = 7;

    private static final String HIDDEN = "hidden";
    private static final String SPECIFY_DATES  = "specify";

    private static final String SYNOPTIC_ANNOUNCEMENT_TOOL = "sakai.synoptic.announcement";

    public static final String SAK_PROP_ANNC_REORDER = "sakai.announcement.reorder";
    public static final boolean SAK_PROP_ANNC_REORDER_DEFAULT = true;
	
	private final String TAB_EXCLUDED_SITES = "exclude";

    private ContentHostingService contentHostingService = null;

    private EventTrackingService eventTrackingService = null;

    private SecurityService m_securityService = null;

    private EntityBroker entityBroker;

    private AliasService aliasService;

    private AnnouncementService announcementService;

    private UserDirectoryService userDirectoryService;

    private ServerConfigurationService serverConfigurationService;

    private FormattedText formattedText;

    private enum BulkOperation {

        DELETE("delete"),
        PUBLISH("publish"),
        UNPUBLISH("unpublish");

        public final String label;

        private BulkOperation(String label) {
            this.label = label;
        }
    }
   
    private static final String DEFAULT_TEMPLATE="announcement/chef_announcements";

    private static final String SELECTED_ROLES_PROPERTY = "selectedRoles";
    private static final String ROLES_CONSTANT = "roles";

    public AnnouncementAction() {
        super();
        aliasService = ComponentManager.get(AliasService.class);
        announcementService = ComponentManager.get(AnnouncementService.class);
        userDirectoryService = ComponentManager.get(UserDirectoryService.class);
        serverConfigurationService = ComponentManager.get(ServerConfigurationService.class);
        formattedText = ComponentManager.get(FormattedText.class);
    }

	public String getCurrentOrder() {
		
		boolean enableReorder=serverConfigurationService.getBoolean(SAK_PROP_ANNC_REORDER, SAK_PROP_ANNC_REORDER_DEFAULT);
		String sortCurrentOrder = SORT_DATE;
		if (enableReorder){
			sortCurrentOrder=SORT_MESSAGE_ORDER;
		}
		return sortCurrentOrder;
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
	SecurityAdvisor getChannelAdvisor(final String channelReference)
	{
		if (serverConfigurationService.getBoolean("announcement.merge.visibility.strict", false))
		{
			return (userId, function, reference) -> SecurityAdvisor.SecurityAdvice.PASS;
		}
		else
		{
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

	/**
	 * Used by callback to convert channel references to channels.
	 */
	private final class AnnouncementReferenceToChannelConverter implements
			MergedListEntryProviderFixedListWrapper.ReferenceToChannelConverter
	{
		public Object getChannel(final String channelReference)
		{
			SecurityAdvisor advisor = getChannelAdvisor(channelReference);
			try {
				m_securityService.pushAdvisor(advisor);
				return announcementService.getAnnouncementChannel(channelReference);
			}
			catch (IdUnusedException e)
			{
				return null;
			}
			catch (PermissionException e)
			{
				log.warn("Permission denied for '{}' on '{}'", SessionManager.getCurrentSessionUserId(), channelReference);
				return null;
			} finally {
				m_securityService.popAdvisor(advisor);
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
			return announcementService.channelReference(siteId, SiteService.MAIN_CONTAINER);
		}
	}

	/**
	 * Used to provide a interface to the MergedList class that is shared with the calendar action.
	 */
	class EntryProvider extends MergedListEntryProviderBase
	{
		/** announcement channels from hidden sites */
		private final List<String> hiddenSites = new ArrayList<>();

		public EntryProvider() {
			this(false);
		}

		public EntryProvider(boolean includeHiddenSites) {
			if (includeHiddenSites) {
				List<String> excludedSiteIds = getExcludedSitesFromTabs();
				if (excludedSiteIds != null) {
					for (String siteId : excludedSiteIds) {
						hiddenSites.add(announcementService.channelReference(siteId, SiteService.MAIN_CONTAINER));
					}
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.util.MergedListEntryProviderBase#makeReference(java.lang.String)
		 */
		public Object makeObjectFromSiteId(String id)
		{
			String channelReference = announcementService.channelReference(id, SiteService.MAIN_CONTAINER);
			Object channel = null;

			if (channelReference != null)
			{
				try
				{
					channel = announcementService.getChannel(channelReference);
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
			SecurityAdvisor advisor = getChannelAdvisor(ref);
			try {
				m_securityService.pushAdvisor(advisor);
				return (!hiddenSites.contains(ref) && announcementService.allowGetChannel(ref));
			} finally {
				m_securityService.popAdvisor(advisor);
			}

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
	 * get announcement range information
	 */
	static String getAnnouncementRange(AnnouncementMessage a)
	{
		if (a.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW) != null
				&& a.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW).equals(Boolean.TRUE.toString()))
		{
			return rb.getString("gen.public");
		}
		else if (a.getProperties().getPropertyList(SELECTED_ROLES_PROPERTY) != null)
		{
			String allRolesString = "";
			ArrayList<String> selectedRolesList = new ArrayList<String>(a.getProperties().getPropertyList("selectedRoles"));
			String[] selectedRoles = selectedRolesList.toArray(new String[selectedRolesList.size()]);
			int count = 0;
			for (String selectedRole : selectedRoles) {
				count++;
				allRolesString += (count==1? "" : ", ") + selectedRole;
			}
			return allRolesString;
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
		// Add resource bundle to velocity context
		context.put("tlang", rb);

		MergedList mergedAnnouncementList = new MergedList();

		mergedAnnouncementList.loadChannelsFromDelimitedString(
		        isOnWorkspaceTab(), new EntryProvider(true), 
		        StringUtils.trimToEmpty(SessionManager.getCurrentSessionUserId()), 
		        mergedAnnouncementList.getChannelReferenceArrayFromDelimitedString(state.getChannelId(), 
		                portlet.getPortletConfig().getInitParameter(
		                        getPortletConfigParameterNameForLoadOnly(portlet))), m_securityService.isSuperUser(), 
		                        ToolManager.getCurrentPlacement().getContext());

		// Place this object in the context so that the velocity template
		// can get at it.
		context.put(VELOCITY_MERGED_CHANNEL_LIST, mergedAnnouncementList);
		sstate.setAttribute(SSTATE_ATTRIBUTE_MERGED_CHANNELS, mergedAnnouncementList);

		String template = (String) getContext(runData).get("template");
		return template + "-merge";
	}
	
	/**
	 * Build the context for showing merged view
	 */
	public String buildReorderContext(VelocityPortlet portlet, Context context, RunData runData, AnnouncementActionState state,
			SessionState sstate)
	{
		// Add resource bundle to velocity context
		context.put("tlang", rb);		

		String template = (String) getContext(runData).get("template");
		return template + "-reorder";
	}

	/**
	 * This is a cover to return the right config parameter name, regardless of whether the parameter is using an older, deprecated name or the newer version.
	 */
	private String getPortletConfigParameterNameForLoadOnly(VelocityPortlet portlet)
	{
		// Check to see if the older non-merged parameter is present.
		// This is really the "merged" parameter, but it was incorrectly
		// named. This is for backward compatibility.
		String configParameter = StringUtils.trimToNull(portlet.getPortletConfig().getInitParameter(
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
		// Add resource bundle to velocity context
		context.put("tlang", rb);

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
			log.debug(this+".buildMainPanelContext ", e);
		}
		catch (NullPointerException e)
		{
			log.error(this+".buildMainPanelContext ", e);
		}

		// get the current channel ID from state object or prolet initial parameter
		String channelId = state.getChannelId();
		if (channelId == null)
		{
			// try the portlet parameter
			channelId = StringUtils.trimToNull(portlet.getPortletConfig().getInitParameter("channel"));
			if (channelId == null)
			{
				// form based on the request's site's "main" channel
				channelId = announcementService.channelReference(ToolManager.getCurrentPlacement().getContext(),
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
		boolean menu_reorder = true;
		
		// check the state status to decide which vm to render
		String statusName = state.getStatus();
		if (statusName != null)
		{
			template = getTemplate(portlet, context, rundata, sstate, state, template);
		}
		
		try
		{
			if (announcementService.allowGetChannel(channelId) && isOkayToDisplayMessageMenu(state))
			{
				// get the channel name throught announcement service API
				channel = announcementService.getAnnouncementChannel(channelId);

				if (DEFAULT_TEMPLATE.equals(template))
				{
					// only query for messages for the list view
					if (channel.allowGetMessages())
					{
						// this checks for any possibility of an add, channel or any site group
						menu_new = channel.allowAddMessage();
	
						List<AnnouncementWrapper> messages = null;
	
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
							else if (VIEW_MODE_BYROLE.equals(view))
							{
								messages = getMessagesByRoles(site, channel, null, true, state, portlet);
							}
						}
						else
						{
							messages = getMessages(channel, null, true, state, portlet);
						}

						//readResourcesPage expects messages to be in session, so put the entire messages list in the session
						sstate.setAttribute("messages", messages);
						//readResourcesPage just orders the list correctly, so we can trim a correct list
						messages = readResourcesPage(sstate, 1, messages.size() + 1);
						//this will trim the list for us to put into the session
						messages = trimListToMaxNumberOfAnnouncements(messages, state.getDisplayOptions());
						//now put it back into the session so we can prepare the page with a correctly sorted and trimmed message list
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
							if (message.isEditable() && channel.allowRemoveMessage(message))
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
			log.error(this+".buildMainPanelContext ", error);
		}
		catch (IdUnusedException error)
		{
			if (announcementService.allowAddChannel(channelId))
			{
				try
				{
					AnnouncementChannelEdit edit = announcementService.addAnnouncementChannel(channelId);
					announcementService.commitChannel(edit);
					channel = edit;
				}
				catch (IdUsedException err)
				{
					log.debug(this+".buildMainPanelContext ", err);
				}
				catch (IdInvalidException err)
				{
				}
				catch (PermissionException err)
				{
				}
				
				if (channel!=null){
					menu_new = channel.allowAddMessage();
				}
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
		
		AnnouncementActionState.DisplayOptions displayOptions = state.getDisplayOptions();
		
		if(VIEW_STATUS.equals(statusName) && channel != null)
		{
			String messageReference = state.getMessageReference();
			AnnouncementMessage message;
			try {
				message = channel.getAnnouncementMessage(this.getMessageIDFromReference(messageReference));
				menu_new = channel.allowAddMessage();
				menu_delete = channel.allowRemoveMessage(message);
				menu_revise = channel.allowEditMessage(message.getId());
			} catch (IdUnusedException | PermissionException e) {
				log.error(e.getMessage());
			}

		}
				
		//Check for MOTD, if yes then is not ok to show permissions button
		boolean showMerge = !isMotd(channelId) && isOkToShowMergeButton(statusName);
		boolean showPermissions = !isMotd(channelId) && isOkToShowPermissionsButton(statusName);
		boolean showOptions = isOkToShowOptionsButton();
		context.put("showOptionsButton", showOptions);

		ActiveTab activeTab = ActiveTab.LIST;
		if(statusName != null) switch(statusName) {
			case VIEW_STATUS:
				activeTab = ActiveTab.VIEW;
				break;
			case LIST_STATUS:
				activeTab = ActiveTab.LIST;
				break;
			case CANCEL_STATUS:
				activeTab = ActiveTab.LIST;
				break;
			case ADD_STATUS:
				activeTab = ActiveTab.ADD;
				break;
			case MERGE_STATUS:
				activeTab = ActiveTab.MERGE;
				break;
			case REORDER_STATUS:
				activeTab = ActiveTab.REORDER;
				break;
			case OPTIONS_STATUS:
				activeTab = ActiveTab.OPTIONS;
				break;
			case EDIT_STATUS:
				activeTab = ActiveTab.EDIT;
				break;
			case BACK_TO_EDIT_STATUS:
				activeTab = ActiveTab.EDIT;
				break;
			case DELETE_STATUS:
				activeTab = ActiveTab.DELETE;
				break;
		}
		
		// So, when reload after save/cancel permission actions, default page will be shown
		if(MODE_PERMISSIONS.equals(statusName)) {
			state.setStatus(LIST_STATUS);
		}

		// "View" announcement menu bar has already been built by this point (buildShowMetadataContext)
		if( !ActiveTab.VIEW.equals(activeTab)) {
			MenuBuilder.buildMenuForGeneral(portlet, rundata, activeTab, rb, context, menu_new, showMerge, showPermissions, showOptions, displayOptions);
		}

		// added by zqian for toolbar
		context.put("allow_new", Boolean.valueOf(menu_new));
		context.put("allow_delete", Boolean.valueOf(menu_delete));
		context.put("allow_revise", Boolean.valueOf(menu_revise));

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

				if (currentSortedBy != null)
				{
					// sort in announcement list view
					buildSortedContext(portlet, context, rundata, sstate);
				}

			} // if allowGetMessages()
		}

		context.put ("service", announcementService);
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
			Collection<Group> groups = channel.getGroupsAllowGetMessage();
			if (groups != null && groups.size() > 0)
			{
				//context.put("groups", groups);
				Collection<Group> sortedGroups = new ArrayList<>();

				for (Iterator<Group> i = new SortedIterator(groups.iterator(), new AnnouncementGroupComparator(AnnouncementGroupComparator.Criteria.TITLE, true)); i.hasNext();)
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

		return template;

	} // buildMainPanelContext

	public void buildSortedContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState sstate)
	{
		// Add resource bundle to velocity context
		context.put("tlang", rb);

		//SAK-21532: making one list of messages in order to allow uniform sorting
		Vector<AnnouncementWrapper> messageList = new Vector<>();
		Vector showMessagesList = new Vector();

		List<AnnouncementWrapper> messages = prepPage(sstate);
		for (int i = 0; i < messages.size(); i++)
		{
			final AnnouncementWrapper m = messages.get(i);
			messageList.addElement(m);
		}

		AnnouncementActionState state = (AnnouncementActionState) getState(portlet, rundata, AnnouncementActionState.class);

		SortedIterator<AnnouncementWrapper> sortedMessageIterator;
		//For Announcement in User's MyWorkspace, the sort order for announcement is by date SAK-22667
		if (isOnWorkspaceTab()){
			sortedMessageIterator = new SortedIterator<>(messageList.iterator(), new AnnouncementWrapperComparator(SORT_DATE, state.getCurrentSortAsc()));
		} else {
			sortedMessageIterator = new SortedIterator<>(messageList.iterator(), new AnnouncementWrapperComparator(state
					.getCurrentSortedBy(), state.getCurrentSortAsc()));
		}
		
		while (sortedMessageIterator.hasNext())
			showMessagesList.add((AnnouncementMessage) sortedMessageIterator.next());
	
		context.put("showMessagesList", showMessagesList.iterator());
		context.put("messageListVector", showMessagesList);
		context.put("showMessagesList2", showMessagesList.iterator());
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

		if (sstate.getAttribute("updating_sort") == Boolean.TRUE) {
			state.setCurrentSortedBy(SORT_MESSAGE_ORDER);
			state.setCurrentSortAsc(false);
			sstate.setAttribute(STATE_CURRENT_SORTED_BY, SORT_MESSAGE_ORDER);
			sstate.setAttribute(STATE_CURRENT_SORT_ASC, Boolean.FALSE);
			sstate.setAttribute("updating_sort", Boolean.FALSE);
		}

	} // buildSortedContext

	public String getTemplate(VelocityPortlet portlet, Context context, RunData rundata, SessionState sstate,
			AnnouncementActionState state, String value)
	{
		String template = value;
		String statusName = state.getStatus();

		if (statusName.equals(DELETE_ANNOUNCEMENT_STATUS))
		{
			template = buildBulkOperationContext(portlet, context, rundata, state, BulkOperation.DELETE);
		}
		else if (statusName.equals(PUBLISH_STATUS)) {
			template = buildBulkOperationContext(portlet, context, rundata, state, BulkOperation.PUBLISH);
        }
		else if (statusName.equals(UNPUBLISH_STATUS)) {
			template = buildBulkOperationContext(portlet, context, rundata, state, BulkOperation.UNPUBLISH);
        }
		else if (statusName.equals(VIEW_STATUS))
		{
			template = buildShowMetadataContext(portlet, context, rundata, state, sstate);
		}
		else if (StringUtils.equalsAnyIgnoreCase(statusName, EDIT_STATUS, BACK_TO_EDIT_STATUS, ADD_STATUS, "stayAtRevise"))
		{
			template = buildReviseAnnouncementContext(portlet, context, rundata, state, sstate);
		}
		else if (statusName.equals("revisePreviw"))
		{
			template = buildPreviewContext(portlet, context, rundata, state);
		}
		else if ((statusName.equals(CANCEL_STATUS)) || (statusName.equals(POST_STATUS)) || (statusName.equals(FINISH_BULK_OPERATION_STATUS)))
		{
			template = buildCancelContext(portlet, context, rundata, state);
		}
		else if (statusName.equals("noSelectedForDeletion") || (statusName.equals(NOT_SELECTED_FOR_REVISE_STATUS)))
		{
			addAlert(sstate, rb.getString("java.alert.youhave"));
		}
		else if (statusName.equals("moreThanOneSelectedForRevise"))
		{
			addAlert(sstate, rb.getString("java.alert.pleasechoose"));
		}
		else if (statusName.equals("noPermissionToRevise"))
		{
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
		else if (statusName.equals(REORDER_STATUS))
		{
			template = buildReorderContext(portlet, context, rundata, state, sstate);
		}
		else if (statusName.equals(MODE_PERMISSIONS))
		{
			template = build_permissions_context(portlet, context, rundata, sstate);
		}
		
		return template;

	} // getTemplate

	/**
	 * Setup for the options panel.
	 */
	public String buildOptionsPanelContext(VelocityPortlet portlet, Context context, RunData runData, SessionState state)
	{
		// Add resource bundle to velocity context
		context.put("tlang", rb);
		context.put("showOptionsButton", isOkToShowOptionsButton());

		// retrieve the state from state object
		AnnouncementActionState actionState = (AnnouncementActionState) getState(portlet, runData, AnnouncementActionState.class);
		context.put(CONTEXT_VAR_DISPLAY_OPTIONS, actionState.getDisplayOptions());

		String channelId = actionState.getChannelId();
		Reference channelRef = EntityManager.newReference(channelId);
		context.put("description", rb.getString("java.setting")// "Setting options for Announcements in worksite "
				+ SiteService.getSiteDisplay(channelRef.getContext()));
				
		Reference anncRef = announcementService.getAnnouncementReference(ToolManager.getCurrentPlacement().getContext());
		List aliasList =	aliasService.getAliases( anncRef.getReference() );
		if ( ! aliasList.isEmpty() )
		{
			String alias[] = ((Alias)aliasList.get(0)).getId().split("\\.");
			context.put("rssAlias", formattedText.escapeHtmlFormattedTextSupressNewlines(alias[0]) );
		}

		// Add Announcement RSS URL
	 
		context.put("rssUrl", announcementService.getRssUrl( anncRef ) );

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
	private boolean isOkToShowOptionsButton()
	{
		return SiteService.allowUpdateSite(ToolManager.getCurrentPlacement().getContext()) && !isOnWorkspaceTab();
	}

	/**
	 * Returns true if it is okay to show the merge button in the menu.
	 */
	private boolean isOkToShowMergeButton(String statusName)
	{
		String displayMerge = serverConfigurationService.getString("announcement.merge.display", "1");
		
		if(displayMerge != null && !displayMerge.equals("1"))
			return false;
		
		return SiteService.allowUpdateSite(ToolManager.getCurrentPlacement().getContext()) && !isOnWorkspaceTab();
	}

	/**
	 * Returns true if it is okay to show the permissions button in the menu.
	 */
	private boolean isOkToShowPermissionsButton(String statusName)
	{
		return SiteService.allowUpdateSite(ToolManager.getCurrentPlacement().getContext());
	}

	/**
	 * This should be the single point for getting lists of announcements in this action. It collects together all the announcements and wraps the real announcements in a decorator object that adds extra properties for use in the VM template.
	 * 
	 * @throws PermissionException
	 */
	private List<AnnouncementWrapper> getMessages(AnnouncementChannel defaultChannel, Filter filter, boolean ascending, AnnouncementActionState state,
			VelocityPortlet portlet) throws PermissionException
	{
		List<AnnouncementWrapper> wrappedMessageList = new ArrayList<>();

        String siteId = ToolManager.getCurrentPlacement().getContext();

		List<AnnouncementMessage> messageList = announcementService.getChannelMessages(state.getChannelId(), filter, ascending, portlet.getPortletConfig().getInitParameter(
								getPortletConfigParameterNameForLoadOnly(portlet)), isOnWorkspaceTab(), isSynopticTool(), siteId, null);

		messageList = getViewableMessages(messageList, siteId);

		wrappedMessageList.addAll(AnnouncementWrapper.wrapList(messageList, defaultChannel, state.getDisplayOptions()));

		// Do an overall sort. We couldn't do this earlier since each merged channel
		Collections.sort(wrappedMessageList);

		// Reverse if we're not ascending.
		if (!ascending)
		{
			Collections.reverse(wrappedMessageList);
		}
		
		return wrappedMessageList;
	}

	/**
	 * This get the whole list of announcement, find their groups, and list them based on group attribute
	 * 
	 * @throws PermissionException
	 */
	private List<AnnouncementWrapper> getMessagesByGroups(Site site, AnnouncementChannel defaultChannel, Filter filter, boolean ascending,
			AnnouncementActionState state, VelocityPortlet portlet) throws PermissionException
	{
		List<AnnouncementWrapper> messageList = getMessages(defaultChannel, filter, ascending, state, portlet);
		List<AnnouncementWrapper> rv = new Vector<>();

		for (int i = 0; i < messageList.size(); i++)
		{
			AnnouncementWrapper aMessage = messageList.get(i);
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
	 * This get the whole list of announcement, find their roles, and list them based on role attribute
	 * 
	 * @throws PermissionException
	 */
	private List<AnnouncementWrapper> getMessagesByRoles(Site site, AnnouncementChannel defaultChannel, Filter filter, boolean ascending,
			AnnouncementActionState state, VelocityPortlet portlet) throws PermissionException
	{
		List<AnnouncementWrapper> messageList = getMessages(defaultChannel, filter, ascending, state, portlet);
		List<AnnouncementWrapper> finalMessages = new ArrayList<>();

		for (AnnouncementWrapper aMessage : messageList)
		{
			String pubview = aMessage.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW);
			if (pubview != null && Boolean.valueOf(pubview).booleanValue()) {
				// public announcements
				aMessage.setRange(rb.getString("range.public"));
			} else {
				Collection selectedRoles = ((AnnouncementMessage) aMessage).getProperties().getPropertyList(SELECTED_ROLES_PROPERTY);
				if (selectedRoles == null || selectedRoles.size() == 0) {
					// site announcements
					aMessage.setRange(rb.getString("range.allgroups"));
				} else {
					// announcement by role
					finalMessages.add(aMessage);
				}
			}
		}

		return finalMessages;

	} // getMessagesByRoles

	/**
	 * This get the whole list of announcement, find their groups, and list them based on group attribute
	 * 
	 * @throws PermissionException
	 */
	private List<AnnouncementWrapper> getMessagesPublic(Site site, AnnouncementChannel defaultChannel, Filter filter, boolean ascending,
			AnnouncementActionState state, VelocityPortlet portlet) throws PermissionException
	{
		List<AnnouncementWrapper> messageList = getMessages(defaultChannel, filter, ascending, state, portlet);
		List<AnnouncementWrapper> rv = new Vector<>();

		for (int i = 0; i < messageList.size(); i++)
		{
			AnnouncementWrapper aMessage = messageList.get(i);
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
	private List<AnnouncementWrapper> trimListToMaxNumberOfAnnouncements(List<AnnouncementWrapper> messageList, AnnouncementActionState.DisplayOptions options)
	{
		if (options !=null && options.isEnforceNumberOfAnnouncementsLimit() && !isOnWorkspaceTab())
		{
			int numberOfAnnouncements = options.getNumberOfAnnouncements();
			ArrayList<AnnouncementWrapper> destList = new ArrayList<>();

			// We need to go backwards through the list, limiting it to the number
			// of announcements that we're allowed to display.
			for (int i = 0, curAnnouncementCount = 0; i < messageList.size() && curAnnouncementCount < numberOfAnnouncements; i++)
			{
				AnnouncementWrapper message = messageList.get(i);

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
	private List<AnnouncementMessage> getViewableMessages(List<AnnouncementMessage> messageList, String siteId) {

		final List<AnnouncementMessage> filteredMessages = new ArrayList<>();
		
		for (AnnouncementMessage message : messageList) {
			
			// for synoptic tool or if in MyWorkspace, 
			// only display if not hidden AND
			// between release and retract dates (if set)
			if (isSynopticTool() || isOnWorkspaceTab()) {
				if (!isHidden(message) && announcementService.isMessageViewable(message)) {
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
				else if (announcementService.isMessageViewable(message)) {
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
		return SYNOPTIC_ANNOUNCEMENT_TOOL.equals(ToolManager.getCurrentTool().getId());
	}
	
	/**
	 * Determines if use has draft (UI: hidden) permission or site.upd
	 * If so, they will be able to view messages that are hidden
	 */
	private boolean canViewHidden(AnnouncementMessage msg, String siteId) 
	{

		// if we are in a roleswapped state, we want to ignore the creator check since it would not necessarily reflect an alternate role
		String[] refs = StringUtil.split(siteId, Entity.SEPARATOR);
		String roleswap = null;
		for (int i = 0; i < refs.length; i++)
		{
			
			roleswap = (String)SessionManager.getCurrentSession().getAttribute("roleswap/site/" + refs[i]);
			if (roleswap!=null)
				break;
		}

		boolean b = m_securityService.unlock(AnnouncementService.SECURE_ANNC_READ_DRAFT, msg.getReference())
							 || m_securityService.unlock(SiteService.SECURE_UPDATE_SITE, "/site/"+ siteId);
		if (roleswap==null)
		{
			b = b || msg.getHeader().getFrom().getId().equals(SessionManager.getCurrentSessionUserId()) ; 
		} 
		
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
		// Add resource bundle to velocity context
		context.put("tlang", rb);

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
		context.put("user", userDirectoryService.getCurrentUser());
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
			log.error(e1.getMessage());
		}
		}
		
		String[] annToRoles = state.getTempAnnounceToRoles();
		if (annToRoles != null) {
			String rolesString = "";
			int count = 0;
			for (String role : annToRoles) {
				count++;
				rolesString += (count==1? "" : ", ") + role;
			}
			context.put("annToRoles", rolesString);
		}
		
		// Set date
		AnnouncementMessageEdit edit = state.getEdit();

		//set release date
		if (tempReleaseDate != null)
		{
			context.put("releaseDate", tempReleaseDate);
		}
		else
		{
			Time releaseDate = null;
			try {
				releaseDate = edit.getProperties().getTimeProperty(AnnouncementService.RELEASE_DATE);
				context.put("releaseDate", releaseDate);
			} 
			catch (Exception e) {
				// not set so set switch appropriately
				context.put("releaseDate", TimeService.newTime());
			} 
		}
		
		//set retract date
		if (tempRetractDate != null)
		{
			context.put("retractDate", tempRetractDate);
		}
		else
		{
			Time retractDate = null;
			try {
				retractDate = edit.getProperties().getTimeProperty(AnnouncementService.RETRACT_DATE);
				context.put("retractDate", retractDate);
			} 
			catch (Exception e) {
				// not set so set switch appropriately
				context.put("retractDate", TimeService.newTime());
			} 
		}
		
		//set modified date
		Time modDate = null;
		try {
			modDate = edit.getProperties().getTimeProperty(AnnouncementService.MOD_DATE);
			context.put("modDate", modDate);
		} 
		catch (Exception e) {
			// not set so set switch appropriately
			context.put("modDate", TimeService.newTime());
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
		String notification = (String) sstate.getAttribute(SSTATE_NOTI_VALUE);
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
		// Add resource bundle to velocity context
		context.put("tlang", rb);

		context.put("service", contentHostingService);
		String siteId = ToolManager.getCurrentPlacement().getContext();
		try {
			Site site = SiteService.getSite(siteId);
			Role[] siteRoles = site.getRoles().toArray(new Role[site.getRoles().size()]);
			List<String> siteRolesIds = new ArrayList<String>();
			for (Role role: siteRoles){
				if (site.getUsersHasRole(role.getId()).size() > 0) {
					siteRolesIds.add(role.getId());
				}
			}
			Collections.sort(siteRolesIds); 
			context.put("siteRolesIds", siteRolesIds);
		} catch (IdUnusedException ex) {
			log.error("Failed to get site from id {}", siteId);
		}
		// to get the content Type Image Service
		context.put("contentTypeImageService", ContentTypeImageService.getInstance());
		context.put("dateFormat", getDateFormatString());

		final String channelId = state.getChannelId();

		// find the channel and channel information through the service
		AnnouncementChannel channel = null;
		try
		{
			if (channelId != null && announcementService.allowGetChannel(channelId))
			{
				// get the channel name throught announcement service API
				channel = announcementService.getAnnouncementChannel(channelId);

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
					else {
						List selectedRolesList = state.getEdit().getProperties().getPropertyList("selectedRoles");
						ArrayList<String> selectedRolesArray = null;
						if (selectedRolesList != null) {
							selectedRolesArray = new ArrayList<String>(selectedRolesList);
							String[] selectedRoles = selectedRolesArray.toArray(new String[selectedRolesArray.size()]);
							state.setTempAnnounceToRoles(selectedRoles);
						}
					}
				}
				AnnouncementMessageEdit edit = state.getEdit();

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
					Collection sortedGroups = new Vector();
					for (Iterator i = new SortedIterator(groups.iterator(), new AnnouncementGroupComparator(AnnouncementGroupComparator.Criteria.TITLE, true)); i.hasNext();)
					{
						sortedGroups.add(i.next());
					}
					context.put("groups", sortedGroups);
				}
			}
		}
		catch (Exception ignore)
		{
			log.debug(ignore.getMessage());
		}

		List attachments = state.getAttachments();

		// if this a new annoucement, get the subject and body from temparory record
		if (state.getStatus().equals(ADD_STATUS))
		{
			context.put("new", "true");
			context.put("tempSubject", state.getTempSubject());
			context.put("tempBody", state.getTempBody());
			
			context.put("pubviewset", isChannelPublic(channelId));
			Placement placement = ToolManager.getCurrentPlacement();
			//SAK-19516, default motd to pubview so it shows up in rss
			context.put("motd", isMotd(channelId));
			
			// output the sstate saved public view options
			final boolean pubview = Boolean.valueOf((String) sstate.getAttribute(SSTATE_PUBLICVIEW_VALUE)).booleanValue();
			if (pubview)
				context.put("pubview", Boolean.TRUE);
			else
				context.put("pubview", Boolean.FALSE);

			// TODO: Track any usage of these Time objects and convert to java.time
			// Set initial release date to today
			final Time currentTime = TimeService.newTime();
			context.put(AnnouncementService.RELEASE_DATE, currentTime);
			
			final Time futureTime = defaultRetractTime();

			context.put(AnnouncementService.RETRACT_DATE, futureTime);
			
			// output the notification options
			String notification = (String) sstate.getAttribute(SSTATE_NOTI_VALUE);
			// "r", "o" or "n"
			// Get default notification
			if (notification == null) {
				notification = serverConfigurationService.getString("announcement.default.notification", "n");
			}
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
			context.put("pubviewset", isChannelPublic(channelId));
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
				retractDate = defaultRetractTime();
			}

			context.put(AnnouncementService.RETRACT_DATE, retractDate);

			context.put(SPECIFY_DATES, specify);
			context.put(HIDDEN, edit.getHeader().getDraft());
			// there is no chance to get the notification setting at this point
		
			//output notification history
			if (state.getEdit()!= null){
				List<String> notiHistory= state.getEdit().getProperties().getPropertyList("noti_history");
				if (notiHistory!=null){
					List<Collection<String>> noti_history = new ArrayList<>();
					for(String notification: notiHistory){
						ArrayList<String> splittedNoti = new ArrayList<>(Arrays.asList(notification.split("_")));
						if (splittedNoti.size() == 2) {
							splittedNoti.add("");
						}
						noti_history.add(splittedNoti);
					}			
					context.put("notiHistory", noti_history);
				}
			}
			

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
				retractDate = defaultRetractTime();

				context.put("useRetractDate", Boolean.valueOf(false));
				}
			} 
			catch (Exception e) 
			{
				retractDate = defaultRetractTime();
			}

			context.put(AnnouncementService.RETRACT_DATE, retractDate);
			
			if(state.getTempHidden()!=null)
			{
				context.put(HIDDEN,state.getTempHidden());
			} 
			
			context.put("pubviewset", isChannelPublic(channelId));

			final boolean pubview = Boolean.valueOf((String) sstate.getAttribute(SSTATE_PUBLICVIEW_VALUE)).booleanValue();
			if (pubview)
				context.put("pubview", Boolean.TRUE);
			else
				context.put("pubview", Boolean.FALSE);

			// output the notification options
			String notification = (String) sstate.getAttribute(SSTATE_NOTI_VALUE);
			// "r", "o" or "n"
			context.put("noti", notification);
			
			//output notification history
			if (state.getEdit()!= null){
				List notiHistory= state.getEdit().getProperties().getPropertyList("noti_history");
				if (notiHistory!=null){
					List noti_history=new ArrayList();
					for(Iterator it = notiHistory.iterator(); it.hasNext();){
						noti_history.add(it.next().toString().split("_"));
					}			
					context.put("notiHistory", noti_history);
				}
			}

		}

		context.put("attachments", attachments);
		context.put("newAnn", (state.getIsNewAnnouncement()) ? "true" : "else");

		context.put("announceToGroups", state.getTempAnnounceToGroups());
		context.put("highlight", state.getTempHighlight());
		context.put("announceToRoles", state.getTempAnnounceToRoles());

		context.put("publicDisable", sstate.getAttribute(PUBLIC_DISPLAY_DISABLE_BOOLEAN));

		String template = (String) getContext(rundata).get("template");
		return template + "-revise";

	} // buildReviseAnnouncementContext

	/**
	 * Calculate the default retract date from now.
	 *
	 * The duration in days is set in {@link #FUTURE_DAYS}.
	 * @deprecated Migrate away from Time and use @link{defaultRetractDate}
	 * @return a Time in the future when the message will be retracted.
	 **/
	@Deprecated
	private Time defaultRetractTime() {
		return TimeService.newTime(defaultRetractDate().toEpochMilli());
	}

	/** Calculate the default retract date from now.
	 *
	 * The duration in days is set in {@link #FUTURE_DAYS}.
	 * @return an instant in the future when the message will be retracted.
	 **/
	private Instant defaultRetractDate() {
		return Instant.now().plus(Duration.ofDays(FUTURE_DAYS));
	}

	/**
	 * Build the context for viewing announcement content
	 */
	protected String buildShowMetadataContext(VelocityPortlet portlet, Context context, RunData rundata,
			AnnouncementActionState state, SessionState sstate)
	{
		// Add resource bundle to velocity context
		context.put("tlang", rb);

		context.put("conService", contentHostingService);

		// to get the content Type Image Service
		context.put("contentTypeImageService", ContentTypeImageService.getInstance());

		// get the channel and message id information from state object
		String messageReference = state.getMessageReference();

		// get the message object through service
		SecurityAdvisor channelAdvisor = getChannelAdvisor(getChannelIdFromReference(messageReference));
		SecurityAdvisor messageAdvisor = getChannelAdvisor(messageReference);
		try
		{
			m_securityService.pushAdvisor(channelAdvisor);
			m_securityService.pushAdvisor(messageAdvisor);
			// get the channel id throught announcement service
			AnnouncementChannel channel = announcementService.getAnnouncementChannel(this
					.getChannelIdFromReference(messageReference));

			// get the message object through service
			AnnouncementMessage message = channel.getAnnouncementMessage(this.getMessageIDFromReference(messageReference));

			// put release date into context if set.
			try {
				Time releaseDate = message.getProperties().getTimeProperty(AnnouncementService.RELEASE_DATE);
				context.put("releaseDate", releaseDate);
			} 
			catch (Exception e) {
				// no release date, ignore
				if (log.isDebugEnabled()) {
					log.debug("buildShowMetadataContext releaseDate is empty for message id {}", message.getId());
				}
			}
			
			try {
				Time retractDate = message.getProperties().getTimeProperty(AnnouncementService.RETRACT_DATE);
				context.put("retractDate", retractDate);
			} 
			catch (Exception e) {
				// no retract date, ignore
				if (log.isDebugEnabled()) {
					log.debug("buildShowMetadataContext retractDate is empty for message id {}", message.getId());
				}
			}

			
			try {
				Time modDate = message.getProperties().getTimeProperty(AnnouncementService.MOD_DATE);
				context.put("modDate", modDate);
			} 
			catch (Exception e) {
				// no modified date is available
				// this can happen as SAK-21071 added the MOD_DATE property
				// as long as the release date is not set, it is safe to grab the date from the msg header
				Time releaseDate = null;
				try {
					releaseDate = message.getProperties().getTimeProperty(AnnouncementService.RELEASE_DATE);
				}
				catch (Exception ee) {
					// this means there is no release date, so it is safe to get the modDate from the message header
					context.put("modDate", message.getHeader().getDate());
				}
			}
			
			context.put("message", message);

			// find out about pubview
			context.put("pubviewset", isChannelPublic(channel.getId()));
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
			
			// this block of code is to check to see if we display a direct link back to an assignment
			String assignmentReference = message.getProperties().getProperty(AnnouncementService.ASSIGNMENT_REFERENCE);
			boolean assignmentCheck = assignmentReference != null && ! "".equals(assignmentReference);
			if (assignmentCheck)
			{
			    Map<String, Object> assignData = new HashMap<String, Object>();
			    Map<String, Object> params = new HashMap<String, Object>();
		            params.put("messageId", message.getId());
	                    // pass in the assignment reference to get the assignment data we need
                            ActionReturn ret; 
                            try {
                              ret = entityBroker.executeCustomAction(assignmentReference, "annc", params, null);
                            } 
                            catch (EntityNotFoundException e) {
                                ret = null;
						        log.info("Assignment {} not found {}", assignmentReference, e.getMessage());
                            }
							catch (SecurityException e) {
								ret = null;
								if (log.isDebugEnabled()) {
									log.debug("Assignment {} not found {}", assignmentReference, e.getMessage());
								}
							}
                            if (ret != null && ret.getEntityData() != null) {
                                Object returnData = ret.getEntityData().getData();
                                assignData = (Map<String, Object>)returnData;
                            }
                            context.put("assignmenturl", assignData.get("assignmentUrl"));
                            context.put("assignmenttitle", assignData.get("assignmentTitle"));
			}

			ActiveTab activeTab = ActiveTab.VIEW;
			if (EDIT_STATUS.equals(state.getStatus())) {
				activeTab = ActiveTab.EDIT;
			}
			MenuBuilder.buildMenuForMetaDataView(portlet, rundata, state.getDisplayOptions(), activeTab, rb, context, menu_new, menu_revise, menu_delete);

			context.put("allow_new", menu_new);
			context.put("allow_delete", menu_delete);
			context.put("allow_revise", menu_revise);

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
			// SAK-23566 indicate the announcement was viewed
			eventTrackingService.post(eventTrackingService.newEvent(AnnouncementService.SECURE_ANNC_READ, message.getReference(), false));
		}
		catch (IdUnusedException e)
		{
			log.debug("Unable to find announcement for metadata display", e);
		}
		catch (PermissionException e)
		{
			log.debug("User doesn't have permission to view announcement", e);
			addAlert(sstate, rb.getFormattedMessage("java.youmess.pes", e.toString()));
		}
		finally {
			m_securityService.popAdvisor(messageAdvisor);
			m_securityService.popAdvisor(channelAdvisor);
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
			AnnouncementChannel channel = announcementService.getAnnouncementChannel(this
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
			addAlert(sstate, rb.getFormattedMessage("java.alert.youacc.pes", e.toString()));
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
		String channelId = announcementService.channelReference(ref.getContext(), ref.getContainer());
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
		state.setStatus(VIEW_STATUS);

	} // doShowMetadata

	/**
	 * Build the context for cancelling the operation and going back to list view
	 */
	protected String buildCancelContext(VelocityPortlet portlet, Context context, RunData rundata, AnnouncementActionState state)
	{
		// Add resource bundle to velocity context
		context.put("tlang", rb);

		// buildNormalContext(portlet, context, rundata);

		String template = (String) getContext(rundata).get("template");
		return template;

	} // buildCancelContext

	/**
	 * Build the context for asking for the delete confirmation
	 */
	protected String buildBulkOperationContext(VelocityPortlet portlet, Context context, RunData rundata,
			AnnouncementActionState state, BulkOperation operation)
	{
		// Add resource bundle to velocity context
		context.put("tlang", rb);

		Collection<AnnouncementMessage> messages = state.getDeleteMessages();
		context.put("delete_messages", messages.iterator());

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

        String title = "";
        String submitName = "";
        String submitLabel = "";
        String confirmMessage = "";

        switch (operation) {
            case DELETE:
                title = rb.getString("del.deleting");
                submitName = "eventSubmit_doDelete";
                submitLabel = rb.getString("gen.delete");
                confirmMessage = rb.getString("del.areyou");
                break;
            case PUBLISH:
                title = rb.getString("pub.publishing");
                submitName = "eventSubmit_doPublish";
                submitLabel = rb.getString("gen.publish");
                confirmMessage = rb.getString("pub.areyou");
                break;
            case UNPUBLISH:
                title = rb.getString("unpub.unpublishing");
                submitName = "eventSubmit_doUnpublish";
                submitLabel = rb.getString("gen.unpublish");
                confirmMessage = rb.getString("unpub.areyou");
                break;
            default:
        }

        context.put("title", title);
        context.put("submitName", submitName);
        context.put("submitLabel", submitLabel);
        context.put("confirmMessage", confirmMessage);

		context.put("channelAccess", MessageHeader.MessageAccess.CHANNEL);

		String template = (String) getContext(rundata).get("template");
		return template + "-bulk-operation";

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
		state.setDeleteMessages(Collections.EMPTY_LIST);
		state.setIsNewAnnouncement(true);
		state.setTempBody("");
		state.setTempSubject("");
		state.setStatus(ADD_STATUS);
		state.setTempHighlight(false);

		sstate.setAttribute(AnnouncementAction.SSTATE_PUBLICVIEW_VALUE, null);
		sstate.setAttribute(AnnouncementAction.SSTATE_NOTI_VALUE, null);

	} // doNewannouncement

	/**
	 * Dispatcher function for various actions on add/revise announcement page (excluding attachments)
	 */
	public void doAnnouncement_form(RunData data, Context context)
	{
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}

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
		final String subjectRaw = params.getString("subject");
		final String subject = subjectRaw == null ? "" : subjectRaw;
		final String normalizedSubject = formattedText.unEscapeHtml(subject);
		boolean highlight = params.getBoolean("highlight"); 
		// read in the body input
		String body = params.getString("body");
		body = processFormattedTextFromBrowser(sstate, body);

		state.setTempSubject(normalizedSubject);
		state.setTempBody(body);
		state.setTempHighlight(highlight);

		if (checkForm)
		{
			if (subject.length() == 0)
			{
				addAlert(sstate, rb.getString("java.alert.youneed"));
			}
			else if (body == null ||body.replaceAll("<br>", "").replaceAll("<br/>","").replaceAll("&nbsp;", "").replaceAll("&lt;br type=&quot;_moz&quot; /&gt;", "").trim().equals("")  || body.length() == 0 ||  
					formattedText.escapeHtml(body,false).equals("&lt;br type=&quot;_moz&quot; /&gt;"))
			{
				body="";
				addAlert(sstate, rb.getString("java.alert.youfill"));
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
			if ((use_start_date && use_end_date) && retractDate!=null && retractDate.before(releaseDate))
			{
				addAlert(sstate, rb.getString("java.alert.baddates"));
			}
		}
		// set hidden property just in case saved
		state.setTempHidden(params.getBoolean(HIDDEN));

		// announce to public?
		String announceTo = params.getString("announceTo");
		state.setTempAnnounceTo(announceTo);
		if (announceTo != null && announceTo.equals("groups"))
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

		if (announceTo != null && ROLES_CONSTANT.equals(announceTo)) {
			String[] rolesChoice = params.getStrings(SELECTED_ROLES_PROPERTY);
			if (rolesChoice == null || rolesChoice.length == 0) {
				state.setTempAnnounceToRoles(null);
				if (checkForm) {
					addAlert(sstate, rb.getString("java.alert.youchooserole"));
				}
			} else {
				state.setTempAnnounceToRoles(rolesChoice);
			}
		} else {
			state.setTempAnnounceToRoles(null);
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
		// tracking changes
		boolean titleChanged = false;
		boolean accessChanged = false;
		boolean availabilityChanged = false;
		
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
		final boolean tempHighlight = state.getTempHighlight();
		
		// announce to public?
		final String announceTo = state.getTempAnnounceTo();

		// there is any error message caused by empty subject or body
		if (sstate.getAttribute(STATE_MESSAGE) != null)
		{
			state.setIsListVM(false);
			state.setStatus("stayAtRevise");
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
					channel = announcementService.getAnnouncementChannel(channelId);
					msg = channel.addAnnouncementMessage();
				}
				else
				{
					// get the message object through service
					// AnnouncementMessageEdit msg = channel.editAnnouncementMessage( messageId );
					msg = state.getEdit();

					// get the channel id throught announcement service
					channel = announcementService.getAnnouncementChannel(this.getChannelIdFromReference(msg.getReference()));
				}

				msg.setBody(body);
				AnnouncementMessageHeaderEdit header = msg.getAnnouncementHeaderEdit();
				String oSubject = header.getSubject();
				header.setSubject(subject);
				if (StringUtils.trimToNull(oSubject) != null && StringUtils.trimToNull(subject) != null && !oSubject.equals(subject))
				{
					// announcement title changed
					titleChanged = true;
				}
//				header.setDraft(!post);
				// v2.4: Hidden in UI becomes Draft 'behind the scenes'
				boolean oDraft = header.getDraft();
				if (oDraft != tempHidden)
				{
					// draft status changed
					availabilityChanged = true;
				}
				header.setDraft(tempHidden);
				header.replaceAttachments(state.getAttachments());
				header.setFrom(userDirectoryService.getCurrentUser());

				// values stored here if saving from Add/Revise page
				ParameterParser params = rundata.getParameters();
				
				// Adding the highlight to the properties
				msg.getPropertiesEdit().addProperty("highlight", String.valueOf(tempHighlight));

				// get release/retract dates
				final String specify = params.getString(HIDDEN);
				final boolean use_start_date = params.getBoolean("use_start_date");
				final boolean use_end_date = params.getBoolean("use_end_date");
				Time releaseDate = null;
				Time retractDate = null;
				String oReleaseDate = msg.getPropertiesEdit().getProperty(AnnouncementService.RELEASE_DATE);
				String oRetractDate = msg.getPropertiesEdit().getProperty(AnnouncementService.RETRACT_DATE);
				
				if(use_start_date && SPECIFY_DATES.equals(specify))
				{
					int begin_year = params.getInt("release_year");
					int begin_month = params.getInt("release_month");
					int begin_day = params.getInt("release_day");
					int begin_hour = hourAmPmConvert(params, "release_hour", "release_ampm");
					int begin_min = params.getInt("release_minute");
					
					releaseDate = TimeService.newTimeLocal(begin_year, begin_month, begin_day, begin_hour, begin_min, 0, 0);

					// in addition to setting release date property, also set Date to release date so properly sorted
					msg.getPropertiesEdit().addProperty(AnnouncementService.RELEASE_DATE, releaseDate.toString());
					// this date is important as the message-api will pick up on it and create a delayed event if in future
					// the delayed event will then notify() to send the message at the proper time
					header.setDate(releaseDate);
				}
				else if (tempReleaseDate != null) // saving from Preview page
				{
					// in addition to setting release date property, also set Date to release date so properly sorted
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

					// since revised, set Date to current Date aka date modified, to maintain Date sort.
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
					// they are not using retract date so remove
					if (msg.getProperties().getProperty(AnnouncementService.RETRACT_DATE) != null) 
					{
							msg.getPropertiesEdit().removeProperty(AnnouncementService.RETRACT_DATE);
					}
				}
				
				// release and retract date changed?
				availabilityChanged = stringChanged(availabilityChanged, oReleaseDate, msg.getPropertiesEdit().getProperty(AnnouncementService.RELEASE_DATE));
				availabilityChanged = stringChanged(availabilityChanged, oRetractDate, msg.getPropertiesEdit().getProperty(AnnouncementService.RETRACT_DATE));
				
				//modified date
				msg.getPropertiesEdit().addProperty(AnnouncementService.MOD_DATE, TimeService.newTime().toString());
				
				//announceTo
				Placement placement = ToolManager.getCurrentPlacement();
				// If the channel into which we are saving is public mark the item as public so it shows up in the
				// RSS feed.
				if(isChannelPublic(channelId))
				{
					msg.getPropertiesEdit().addProperty(ResourceProperties.PROP_PUBVIEW, Boolean.TRUE.toString());
					header.clearGroupAccess();
				}
				
				// get the existing access and group settings
				String oPubView = msg.getPropertiesEdit().getProperty(ResourceProperties.PROP_PUBVIEW);
				String oAccess = header.getAccess().toString();
				Collection<Group> oGroups = header.getGroupObjects();
				
				try
				{
					Site site = SiteService.getSite(channel.getContext());
					
					if (announceTo != null && announceTo.equals("pubview")
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
					
					// pubview changed?
					accessChanged = stringChanged(accessChanged, oPubView, msg.getPropertiesEdit().getProperty(ResourceProperties.PROP_PUBVIEW));
					
					// announce to site?
					if (announceTo != null && announceTo.equals("site"))
					{
						header.clearGroupAccess();
					}
					else if (announceTo != null && announceTo.equals("groups"))
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
					if (announceTo != null && ROLES_CONSTANT.equals(announceTo)) {
						msg.getPropertiesEdit().removeProperty(SELECTED_ROLES_PROPERTY);
						for (String role : state.getTempAnnounceToRoles()) {
							msg.getPropertiesEdit().addPropertyToList(SELECTED_ROLES_PROPERTY, role);
						}
						header.clearGroupAccess();
					} else {
						msg.getPropertiesEdit().removeProperty(SELECTED_ROLES_PROPERTY);
					}
					
					// site/grouop changed?
					accessChanged = stringChanged(accessChanged, oAccess, header.getAccess().toString());
					if (!accessChanged)
					{
						Collection<Group> groups = header.getGroupObjects();
						if (oGroups != null && groups != null
							&& !(oGroups.containsAll(groups) && groups.containsAll(oGroups)))
						{
							// group changed
							accessChanged = true;
						}
					}
				}
				catch (PermissionException e)
				{
					addAlert(sstate, rb.getFormattedMessage("java.alert.youpermi.subject", subject));

					state.setIsListVM(false);
					state.setStatus("stayAtRevise");
					return;
				}
				catch (Exception ignore)
				{
					// No site available.
				}

				// save notification level if this is a future notification message
				Time now = TimeService.newTime();
				
				int notiLevel=noti;
				if (msg.getAnnouncementHeaderEdit().getDraft()){
					notiLevel=3; //Set notilevel as 3 if it a hidden announcement, as no notification is sent regardless of the notification option
				}
				
				if (releaseDate != null && now.before(releaseDate))// && noti != NotificationService.NOTI_NONE)
				{
					msg.getPropertiesEdit().addProperty("notificationLevel", notification);
					msg.getPropertiesEdit().addPropertyToList("noti_history", now.toStringLocalFull()+"_"+notiLevel+"_"+releaseDate.toStringLocalFull());
				}
				else {
					msg.getPropertiesEdit().addPropertyToList("noti_history", now.toStringLocalFull()+"_"+notiLevel);
				}
				
				channel.commitMessage(msg, noti, "org.sakaiproject.announcement.impl.SiteEmailNotificationAnnc");

				setMotdAttachmentsPublic(header, channelId);

				if (!state.getIsNewAnnouncement())
				{
					state.setEdit(null);
					if (titleChanged)
					{
						// title changed
						eventTrackingService.post(eventTrackingService.newEvent(AnnouncementService.EVENT_ANNC_UPDATE_TITLE, msg.getReference(), true));
					}
					if (accessChanged)
					{
						// access changed
						eventTrackingService.post(eventTrackingService.newEvent(AnnouncementService.EVENT_ANNC_UPDATE_ACCESS, msg.getReference(), true));
					}
					if (availabilityChanged)
					{
						// availablity changed
						eventTrackingService.post(eventTrackingService.newEvent(AnnouncementService.EVENT_ANNC_UPDATE_AVAILABILITY, msg.getReference(), true));


						//check if an delay might exist
						if ((StringUtils.isNotEmpty(oReleaseDate) && releaseDate == null) || (StringUtils.isNotEmpty(oReleaseDate) && releaseDate != null && !oReleaseDate.equals(releaseDate.toString()))) {

							eventTrackingService.cancelDelays(msg.getReference(), AnnouncementService.EVENT_AVAILABLE_ANNC);

							//check if new date has passed already
							if(msg.getHeader().getInstant().isAfter(Instant.now())){
								Event event = eventTrackingService.newEvent(org.sakaiproject.announcement.api.AnnouncementService.EVENT_AVAILABLE_ANNC, msg.getReference(), true);
								eventTrackingService.delay(event,msg.getHeader().getInstant());
							}
						}
					}
				}

				//Create delay
				Instant date = msg.getHeader().getInstant();
				if (date.isAfter(Instant.now())) {
					// track event
					Event event = eventTrackingService.newEvent(org.sakaiproject.announcement.api.AnnouncementService.EVENT_AVAILABLE_ANNC, msg.getReference(), true);
					eventTrackingService.delay(event,date);
				}
			}
			catch (IdUnusedException e)
			{
				if (log.isDebugEnabled()) log.debug("{}doPost()", this, e);
			}
			catch (PermissionException e)
			{
				if (log.isDebugEnabled()) log.debug("{}doPost()", this, e);
				addAlert(sstate, rb.getFormattedMessage("java.alert.youpermi.subject", subject));
			}

			state.setIsListVM(true);
			state.setAttachments(null);
			state.setSelectedAttachments(null);
			state.setDeleteMessages(Collections.EMPTY_LIST);
			state.setStatus(POST_STATUS);
			state.setMessageReference("");
			state.setTempAnnounceTo(null);
			state.setTempAnnounceToGroups(null);
			state.setTempAnnounceToRoles(null);
			state.setCurrentSortedBy(getCurrentOrder());
			//state.setCurrentSortAsc(Boolean.TRUE.booleanValue());
			sstate.setAttribute(STATE_CURRENT_SORTED_BY, getCurrentOrder());
			sstate.setAttribute(STATE_CURRENT_SORT_ASC, state.getCurrentSortAsc());
		}
	} // postOrSaveDraft

	/**
	 * Sets the public visibilty of attachments for MOTD messages,
	 * the visibilty is based on the messages draft state.
	 *
	 * @param messageHeader
	 * @param channelId
	 */
	private void setMotdAttachmentsPublic(MessageHeaderEdit messageHeader, String channelId) {
		if (messageHeader != null && channelId != null) {
			List<Reference> attachments = messageHeader.getAttachments();
			if (attachments != null && isMotd(channelId)) {
				attachments.stream().map(Reference::getId)
						.filter(StringUtils::isNotBlank)
						.forEach(id -> contentHostingService.setPubView(id, !messageHeader.getDraft()));
			}
		}
	}

	/**
	 * detect string chagne.
	 * @param startValue
	 * @param s1
	 * @param s2
	 * @return
	 */
	private boolean stringChanged (boolean startValue, String s1, String s2)
	{
		boolean rv = startValue;
		if (!startValue)
		{
			if (s1 == null && s2 != null)
			{
				rv = true;
			}
			else if (s1 != null && s2 == null)
			{
				rv = true;
			}
			else if (s1 != null && s2 != null && !s1.equals(s2))
			{
				rv = true;
			}
		}
		return rv;
	}

	/**
	 * Action is to use when doPreviewrevise requested from preview status corresponding to chef_announcements-preview "eventSubmit_doPreviewrevise"
	 */
	public void doPreviewrevise(RunData rundata, Context context)
	{

		// retrieve the state from state object
		AnnouncementActionState state = (AnnouncementActionState) getState(context, rundata, AnnouncementActionState.class);

		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState sstate = ((JetspeedRunData) rundata).getPortletSessionState(peid);

		state.setStatus(BACK_TO_EDIT_STATUS);

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

		for (AnnouncementMessage message : state.getDeleteMessages()) {
			try
			{

				// get the channel id throught announcement service
				AnnouncementChannel channel = announcementService.getAnnouncementChannel(this.getChannelIdFromReference(message
						.getReference()));

				if (channel.allowRemoveMessage(message))
				{
					// remove message from channel
					//AnnouncementMessageEdit edit = channel.editAnnouncementMessage(message.getId());
					//channel.removeMessage(edit); 
					channel.removeAnnouncementMessage(message.getId());


					//Delete possible delay
					if (message.getHeader().getInstant().isAfter(Instant.now())) {
						eventTrackingService.cancelDelays(message.getReference(), org.sakaiproject.announcement.api.AnnouncementService.EVENT_AVAILABLE_ANNC);
					}
				}
				else
				{
					addAlert(sstate, rb.getString("java.alert.youdel"));
				}
			}
			catch (IdUnusedException e)
			{
				if (log.isDebugEnabled()) log.debug("{}.doDeleteannouncement()", this, e);
			}
			catch (PermissionException e)
			{
				if (log.isDebugEnabled()) log.debug("{}.doDeleteannouncement()", this, e);
			}
			catch (NoSuchElementException e)
			{
				if (log.isDebugEnabled()) log.debug("{}.doDeleteannouncement()", this, e);
			}
		}

		state.setIsListVM(true);
		state.setStatus(FINISH_BULK_OPERATION_STATUS);
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
				Collection<AnnouncementMessage> messages = new ArrayList<>();
				for (String ref : messageReferences) {
					// get the message object through service
					try
					{
						// get the channel id throught announcement service
						AnnouncementChannel channel = announcementService.getAnnouncementChannel(this
								.getChannelIdFromReference(ref));
						// get the message object through service
						AnnouncementMessage message = channel.getAnnouncementMessage(this
								.getMessageIDFromReference(ref));

						messages.add(message);
					}
					catch (IdUnusedException e)
					{
						if (log.isDebugEnabled()) log.debug("{}.doDeleteannouncement()", this, e);
						// addAlert(sstate, e.toString());
					}
					catch (PermissionException e)
					{
						if (log.isDebugEnabled()) log.debug("{}.doDeleteannouncement()", this, e);
						addAlert(sstate, rb.getFormattedMessage("java.alert.youdelann.ref", ref));
					}
				}

				// record the items to be deleted
				state.setDeleteMessages(messages);
				state.setIsListVM(false);
				state.setStatus(DELETE_ANNOUNCEMENT_STATUS);
			}
			else
			{
				state.setIsListVM(true);
				state.setStatus("noSelectedForDeletion");
			}

		}
		// if not in main screen
		else
		{
			state.setIsNewAnnouncement(false);
			Collection<AnnouncementMessage> messages = new ArrayList<>();

			// get the message object through service
			try
			{
				// get the channel id throught announcement service
				AnnouncementChannel channel = announcementService.getAnnouncementChannel(this
						.getChannelIdFromReference(messageReference));
				// get the message object through service
				AnnouncementMessage message = channel.getAnnouncementMessage(this.getMessageIDFromReference(messageReference));

				messages.add(message);
			}
			catch (IdUnusedException e)
			{
				if (log.isDebugEnabled()) log.debug("{}doDeleteannouncement()", this, e);
				// addAlert(sstate, e.toString());
			}
			catch (PermissionException e)
			{
				if (log.isDebugEnabled()) log.debug("{}doDeleteannouncement()", this, e);
				addAlert(sstate, rb.getString("java.alert.youdelann2"));
			}

			state.setDeleteMessages(messages);

			state.setIsListVM(false);
			if (sstate.getAttribute(STATE_MESSAGE) == null)
			{
				// add folder sucessful
				state.setStatus(DELETE_ANNOUNCEMENT_STATUS);
			}
		}

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
		Collection<AnnouncementMessage> messages = new ArrayList<>();

		// get the message object through service
		try
		{
			// get the channel id throught announcement service
			AnnouncementChannel channel = announcementService.getAnnouncementChannel(this
					.getChannelIdFromReference(messageReference));
			// get the message object through service
			messages.add(channel.getAnnouncementMessage(this.getMessageIDFromReference(messageReference)));
		} catch (PermissionException e) {
			log.warn("No permission to delete announcement {} : {}",  messageReference, e.toString());
			addAlert(sstate, rb.getString("java.alert.youdelann2"));
		} catch (Exception e) {
			log.error("Failed delete announcement {}: {}", messageReference, e.toString());
		}

		state.setDeleteMessages(messages);

		if (sstate.getAttribute(STATE_MESSAGE) == null)
		{
			state.setStatus(DELETE_ANNOUNCEMENT_STATUS);
		}
	} // doDelete_announcement_link
 
	private void loadSelectedAnnouncements(RunData rundata, Context context) {

		// retrieve the state from state object
		AnnouncementActionState state = (AnnouncementActionState) getState(context, rundata, AnnouncementActionState.class);

		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState sstate = ((JetspeedRunData) rundata).getPortletSessionState(peid);

		// get the channel and message id information from state object
		String messageReference = state.getMessageReference();

		// then, read in the selected announcment items
		String[] messageReferences = rundata.getParameters().getStrings("selectedMembers");

		if (messageReferences == null) {
			state.setDeleteMessages(Collections.EMPTY_LIST);
			return;
		}

		Collection<AnnouncementMessage> messages = new ArrayList<>();

		for (String ref :  messageReferences) {
			// get the message object through service
			try {
				// get the channel id throught announcement service
				AnnouncementChannel channel = announcementService.getAnnouncementChannel(this
						.getChannelIdFromReference(ref));
				// get the message object through service
				AnnouncementMessage message = channel.getAnnouncementMessage(this
						.getMessageIDFromReference(ref));

				messages.add(message);
			} catch (PermissionException e) {
				log.warn("No permission to load announcement {} : {}",  ref, e.toString());
				addAlert(sstate, rb.getFormattedMessage("java.alert.youdelann.ref", ref));
			} catch (Exception e) {
				log.error("Failed load announcement for publishing {}: {}",ref, e.toString());
			}
		}

		state.setDeleteMessages(messages);
	}

	private void publishOrUnpublishSelectedAnnouncements(RunData rundata, Context context, boolean unpublish) {

		AnnouncementActionState state = (AnnouncementActionState) getState(context, rundata, AnnouncementActionState.class);

		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState sstate = ((JetspeedRunData) rundata).getPortletSessionState(peid);

        for (AnnouncementMessage message : state.getDeleteMessages()) {

			try {
				AnnouncementChannel channel = announcementService.getAnnouncementChannel(this.getChannelIdFromReference(message.getReference()));
			    AnnouncementMessageEdit edit = channel.editAnnouncementMessage(message.getId());

				MessageHeaderEdit header = edit.getHeaderEdit();
                header.setDraft(unpublish);
                channel.commitMessage(edit, 0);
				setMotdAttachmentsPublic(header, channel.getId());
			} catch (Exception e) {
				log.error("Failed to publish announcement {}: {}", message.getId(), e.toString());
			}
		}

		state.setStatus(FINISH_BULK_OPERATION_STATUS);
	}

	public void doPublishannouncement(RunData rundata, Context context) {

		AnnouncementActionState state = (AnnouncementActionState) getState(context, rundata, AnnouncementActionState.class);
		loadSelectedAnnouncements(rundata, context);
		state.setStatus(PUBLISH_STATUS);
	}
 
    public void doPublish(RunData rundata, Context context) {

		publishOrUnpublishSelectedAnnouncements(rundata, context, false);
	}

	public void doUnpublishannouncement(RunData rundata, Context context) {

		AnnouncementActionState state = (AnnouncementActionState) getState(context, rundata, AnnouncementActionState.class);
		loadSelectedAnnouncements(rundata, context);
		state.setStatus(UNPUBLISH_STATUS);
	}
 
    public void doUnpublish(RunData rundata, Context context) {

		publishOrUnpublishSelectedAnnouncements(rundata, context, true);
	}

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
			AnnouncementChannel channel = announcementService.getAnnouncementChannel(this
					.getChannelIdFromReference(messageReference));
			// get the message object through service
			// AnnouncementMessage message = channel.getAnnouncementMessage( messageId );

			AnnouncementMessageEdit edit = channel.editAnnouncementMessage(this.getMessageIDFromReference(messageReference));
			state.setEdit(edit);

			state.setTempAnnounceToGroups(edit.getAnnouncementHeader().getGroups());

			// ReferenceVector attachmentList = (message.getHeader()).getAttachments();
			List attachmentList = (edit.getHeader()).getAttachments();
			state.setAttachments(attachmentList);
		}
		catch (IdUnusedException e)
		{
			if (log.isDebugEnabled()) log.debug("{}announcementRevise", this, e);
			// addAlert(sstate, e.toString());
		}
		catch (PermissionException e)
		{
			if (log.isDebugEnabled()) log.debug("{}announcementRevise", this, e);
			state.setStatus(VIEW_STATUS);
		}
		catch (InUseException err)
		{
			if (log.isDebugEnabled()) log.debug("{}.doReviseannouncementfrommenu", this, err);
			addAlert(sstate, rb.getString("java.alert.thisitem"));
			// "This item is being edited by another user. Please try again later.");
			state.setStatus(VIEW_STATUS);
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
						AnnouncementChannel channel = announcementService.getAnnouncementChannel(this
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
						if (log.isDebugEnabled()) log.debug("{}announcementReviseFromMenu", this, e);
					}
					catch (PermissionException e)
					{
						if (log.isDebugEnabled()) log.debug("{}announcementReviseFromMenu", this, e);
						addAlert(sstate, rb.getFormattedMessage("java.alert.youacc.pes", e.toString()));
					}
					// %%% -ggolden catch(InUseException err)
					catch (InUseException err)
					{
						if (log.isDebugEnabled())
							log.debug("{}.doReviseannouncementfrommenu", this, err);
						addAlert(sstate, rb.getString("java.alert.thisis"));
						state.setIsListVM(false);
						state.setStatus(VIEW_STATUS);
					}
				}
			}
			else
			{
				state.setIsListVM(true);
				state.setStatus(NOT_SELECTED_FOR_REVISE_STATUS);
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
				AnnouncementChannel channel = announcementService.getAnnouncementChannel(this
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
				if (log.isDebugEnabled()) log.debug("{}announcementReviseFromMenu", this, e);
				// addAlert(sstate, e.toString());
			}
			catch (PermissionException e)
			{
				if (log.isDebugEnabled()) log.debug("{}announcementReviseFromMenu", this, e);
				addAlert(sstate, rb.getFormattedMessage("java.alert.youacc.pes", e.toString()));
			}
			catch (InUseException err)
			{
				if (log.isDebugEnabled()) log.debug("{}.doReviseannouncementfrommenu", this, err);
				addAlert(sstate, rb.getString("java.alert.thisis"));
				state.setIsListVM(false);
				state.setStatus(VIEW_STATUS);
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

	} // doRevisepreview

	public void doAttachments(RunData data, Context context)
	{
		AnnouncementActionState actionState = (AnnouncementActionState) getState(context, data, AnnouncementActionState.class);
		if (isMotd(actionState.getChannelId())){
			ToolSession session = SessionManager.getCurrentToolSession();
			session.setAttribute(FilePickerHelper.FILE_PICKER_ATTACH_LINKS, new Boolean(true).toString());
		}
        
		// get into helper mode with this helper tool
		startHelper(data.getRequest(), "sakai.filepicker");

		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		AnnouncementActionState myState = (AnnouncementActionState) getState(context, data, AnnouncementActionState.class);


		state.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, myState.getAttachments());

		myState.setStatus(BACK_TO_EDIT_STATUS);
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
		state.setDeleteMessages(Collections.EMPTY_LIST);
		state.setStatus(CANCEL_STATUS);
		state.setTempAnnounceTo(null);
		state.setTempAnnounceToGroups(null);
		state.setTempAnnounceToRoles(null);
		state.setCurrentSortedBy(getCurrentOrder());
		//state.setCurrentSortAsc(Boolean.TRUE.booleanValue());
		sstate.setAttribute(STATE_CURRENT_SORTED_BY, getCurrentOrder());
		//sstate.setAttribute(STATE_CURRENT_SORT_ASC, Boolean.FALSE);

		sstate.setAttribute(STATE_CURRENT_SORT_ASC, state.getCurrentSortAsc());
		
		// we are done with customization... back to the main (list) mode
		sstate.removeAttribute(STATE_MODE);

		try
		{
			if (state.getEdit() != null)
			{
				// get the channel id throught announcement service
				AnnouncementChannel channel = announcementService.getAnnouncementChannel(this.getChannelIdFromReference(state
						.getEdit().getReference()));

				channel.cancelMessage(state.getEdit());
				state.setEdit(null);
			}
		}
		catch (IdUnusedException e)
		{
			if (log.isDebugEnabled()) log.debug("{}doCancel()", this, e);
		}
		catch (PermissionException e)
		{
			if (log.isDebugEnabled()) log.debug("{}doCancel()", this, e);
		}

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
		state.setDeleteMessages(Collections.EMPTY_LIST);
		state.setStatus(CANCEL_STATUS);

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
			// if the messages are not already sorted by field, reset the sort sequence to be ascending
			state.setCurrentSortedBy(field);
			state.setCurrentSortAsc(true);
			sstate.setAttribute(STATE_CURRENT_SORT_ASC, Boolean.TRUE);
		}

		resetPaging(sstate);
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

	// ********* ending for sorting *********

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
		
		if (entityBroker == null)
		{
			entityBroker = (EntityBroker) ComponentManager.get("org.sakaiproject.entitybroker.EntityBroker");
		}
		
		if (eventTrackingService == null)
		{
			eventTrackingService = (EventTrackingService) ComponentManager.get("org.sakaiproject.event.api.EventTrackingService");
		}

		if (m_securityService == null)
		{
			m_securityService = (SecurityService) ComponentManager.get("org.sakaiproject.authz.api.SecurityService");
		}


		// retrieve the state from state object
		AnnouncementActionState annState = (AnnouncementActionState) getState(portlet, rundata, AnnouncementActionState.class);

		// get the current channel ID from state object or prolet initial parameter
		String channelId = annState.getChannelId();
		if (channelId == null)
		{
			// try the portlet parameter
			channelId = StringUtils.trimToNull(portlet.getPortletConfig().getInitParameter("channel"));
			if (channelId == null)
			{
				// form based on the request's context's "main" channel
				channelId = announcementService.channelReference(ToolManager.getCurrentPlacement().getContext(),
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

		if (state.getAttribute(STATE_INITED) == null)
		{
			state.setAttribute(STATE_INITED, STATE_INITED);

			MergedList mergedAnnouncementList = new MergedList();

			String[] channelArrayFromConfigParameterValue = null;

			// TODO - MERGE FIX
			// Figure out the list of channel references that we'll be using.
			// If we're on the workspace tab, we get everything.
			// Don't do this if we're the super-user, since we'd be
			// overwhelmed.
			if (isOnWorkspaceTab() && !m_securityService.isSuperUser())
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
					new EntryProvider(false), annState.getChannelId(), channelArrayFromConfigParameterValue,
					new AnnouncementReferenceToChannelConverter()),
					StringUtil.trimToZero(SessionManager.getCurrentSessionUserId()), channelArrayFromConfigParameterValue,
					m_securityService.isSuperUser(), ToolManager.getCurrentPlacement().getContext());
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
			String[] disableStrgs = serverConfigurationService.getStrings("prevent.public.announcements");
			if (disableStrgs != null)
			{
				for (int i = 0; i < disableStrgs.length; i++)
				{
					if ((StringUtil.trimToZero(disableStrgs[i])).equals(site.getType()))
						state.setAttribute(PUBLIC_DISPLAY_DISABLE_BOOLEAN, Boolean.TRUE);
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

	/**
	 * Check if the channel (in effect the site) is public. This is useful for restricting the access options
	 * for announcements in a public site.
	 * @param channelId The channel ID.
	 */
	private boolean isChannelPublic(String channelId) {
		return m_securityService.unlock(userDirectoryService.getAnonymousUser(), AnnouncementService.SECURE_ANNC_READ, channelId);
	}


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
	 * Fire up the permissions editor
	 */
	public void doPermissions(RunData runData, Context context)
	{
		AnnouncementActionState state = (AnnouncementActionState) getState(context, runData, AnnouncementActionState.class);
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState sstate = ((JetspeedRunData) runData).getPortletSessionState(peid);

		state.setStatus(MODE_PERMISSIONS);

		sstate.setAttribute(STATE_TOOL_KEY, "annc");
		sstate.setAttribute(STATE_BUNDLE_KEY, "announcement");
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

		state.setStatus(MERGE_STATUS);
	} // doMerge
	
	/**
	 * Handle the "Reorder" button on the toolbar
	 */
	public void doReorder(RunData runData, Context context)
	{
		AnnouncementActionState state = (AnnouncementActionState) getState(context, runData, AnnouncementActionState.class);
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState sstate = ((JetspeedRunData) runData).getPortletSessionState(peid);

		state.setStatus(REORDER_STATUS);
	} // doMerge

	/**
	 * Handles the user clicking on the save button on the page to specify which calendars will be merged into the present schedule.
	 */
	public void doUpdate(RunData runData, Context context)
	{
		AnnouncementActionState state = (AnnouncementActionState) getState(context, runData, AnnouncementActionState.class);
		
		if (!SiteService.allowUpdateSite(ToolManager.getCurrentPlacement().getContext()))  // SAK-18202
		{	
			log.debug("{}.doUpdate - Do not have permission to update", this);
			state.setStatus(CANCEL_STATUS); 
			return;
		}

		if (state.getStatus().equals(MERGE_STATUS))
		{
			doMergeUpdate(runData, context);
		}
		else if (state.getStatus().equals(OPTIONS_STATUS))
		{
			doOptionsUpdate(runData, context);
		}
		else if (state.getStatus().equals(REORDER_STATUS))
		{
			doReorderUpdate(runData, context);
		}
		else
		{
			log.debug("{}.doUpdate - Unexpected status", this);
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
			if (log.isDebugEnabled())
			{
				log.debug("{}.doUpdate mergedChannelList == null", this);
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

		state.setStatus(null);

		sstate.removeAttribute(STATE_MODE);
		
		state.setStatus(CANCEL_STATUS); //SAK-14001	It goes to the main page after saving the merge options.
		
	}
	
	
	/**
	 * This handles the "doUpdate" if we're processing an update from the "reorder" page.
	 */
	public void doReorderUpdate(RunData rundata, Context context)
	{
		// retrieve the state from state object
		AnnouncementActionState state = (AnnouncementActionState) getState(context, rundata, AnnouncementActionState.class);

		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState sstate = ((JetspeedRunData) rundata).getPortletSessionState(peid);

		// set updating_sort state so state.getCurrentSortedBy() and state.getCurrentSortAsc() can be reset after buildSortedContext() finishes
		sstate.setAttribute("updating_sort", Boolean.TRUE);

		// Storing the re-ordered sequence of the announcements
		if (state.getIsListVM())
		{
			// then, read in the selected announcment items
			String[] messageReferences2 = rundata.getParameters().getStrings("selectedMembers2");
			if (messageReferences2 != null)
			{
				
				
				try {
				//grab all messages before the order changes:	
				List<AnnouncementMessage> allMessages = announcementService.getChannel(state.getChannelId()).getMessages(null, true);
				int msgCount =  allMessages.size(); //used to find msg order number
				//store the updated message ids so we know which ones didn't get updated
				List<String> updatedMessageIds = new ArrayList<String>();
				Vector v2 = new Vector();
				
				//find starting message index (0 - x) based on number of messages displayed per page
				int j= allMessages.size();
				if ((sstate.getAttribute(STATE_TOP_PAGE_MESSAGE) != null) && (sstate.getAttribute(STATE_PAGESIZE) != null))
				{
					j = ((Integer) sstate.getAttribute(STATE_TOP_PAGE_MESSAGE)).intValue();
				}
				
				for (int i = 0; i < messageReferences2.length; i++, j++)
				{
					// get the updated/reordered message object through service
					try
					{
						// get the channel id throught announcement service
						AnnouncementChannel channel2 = announcementService.getAnnouncementChannel(this
								.getChannelIdFromReference(messageReferences2[i]));
						// get the message object through service
						AnnouncementMessage message2 = channel2.getAnnouncementMessage(this
								.getMessageIDFromReference(messageReferences2[i]));
						AnnouncementMessageEdit msg =(AnnouncementMessageEdit)message2;
						AnnouncementMessageHeaderEdit header2 = msg.getAnnouncementHeaderEdit();
						header2.setMessage_order(msgCount - j);
						channel2.commitMessage_order(msg);
						updatedMessageIds.add(msg.getId());
						//v2.addElement(message2);
					}
					catch (IdUnusedException e)
					{
						if (log.isDebugEnabled()) log.debug("{}.doDeleteannouncement()", this, e);
						// addAlert(sstate, e.toString());
					}
					catch (PermissionException e)
					{
						if (log.isDebugEnabled()) log.debug("{}.doDeleteannouncement()", this, e);
						addAlert(sstate, rb.getFormattedMessage("java.alert.youdelann.ref", messageReferences2[i]));
					}
				}
				if(allMessages.size() > messageReferences2.length){
					//need to update the message order of the remaining untouched messages (only sorts the top 10)
				
					//order by message order:
					Comparator<AnnouncementMessage> comparing = Comparator.comparing(o -> (o.getAnnouncementHeader().getMessage_order()));
					SortedIterator<AnnouncementMessage> messagesSorted = new SortedIterator<>(allMessages.iterator(), comparing);
					//start at last message and increment up
					int messageOrder = 1;
					while(messagesSorted.hasNext()){
						Message message = messagesSorted.next();
						if(!updatedMessageIds.contains(message.getId())){
							//since this list is ordered, we can assign the message order in order:
							AnnouncementChannel channel2 = announcementService.getAnnouncementChannel(this
									.getChannelIdFromReference(message.getReference()));
							// get the message object through service
							AnnouncementMessage message2 = channel2.getAnnouncementMessage(this
									.getMessageIDFromReference(message.getReference()));
							AnnouncementMessageEdit msg =(AnnouncementMessageEdit)message2;
							AnnouncementMessageHeaderEdit header2 = msg.getAnnouncementHeaderEdit();
							header2.setMessage_order(messageOrder);						
							channel2.commitMessage_order(msg);
						}
						messageOrder++;
					}
				}
				} catch (PermissionException | IdUnusedException e1) {
					log.error(e1.getMessage());
				}
			}
		}

		state.setStatus(null);

		sstate.removeAttribute(STATE_MODE);
		
		state.setStatus(CANCEL_STATUS); 

	} // doReorderUpdate
	

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
			String alias = StringUtils.trimToNull(runData.getParameters().getString("rssAlias"));
			
			//server check to ensure the length of alias SAK-18178
			//due to the trim above this may be null
			if (alias != null && alias.length()>99){
				addAlert(sstate, rb.getString("java.alert.length"));
				state.setStatus(OPTIONS_STATUS);
				return;
			}
			
			// SAK-17786 Check for XSS
			alias = formattedText.processFormattedText(alias, null, null);
			
			Reference anncRef = announcementService.getAnnouncementReference(ToolManager.getCurrentPlacement().getContext());
		
			List aliasList =	aliasService.getAliases( anncRef.getReference() );
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
				aliasService.removeTargetAliases(anncRef.getReference());
					
            alias += ".rss";
				aliasService.setAlias(alias, anncRef.getReference());
			}
		}
		catch (IdUsedException ue)
		{
			addAlert(sstate, rb.getString("java.alert.dupalias"));
		}
		catch (Exception e)
		{
			addAlert(sstate, rb.getString("java.alert.unknown"));
			log.error("{}.doOptionsUpdate", this, e);
		}
		
		// We're omitting processing of the "showAnnouncementBody" since these
		// options are currently mutually exclusive.

		// commit the change
		saveOptions();

		sstate.removeAttribute(STATE_MODE);
		
		/*
		 * SAK-13116 If we are in the synoptic view, we want some validation so
		 * that is not possible to set the Number of Announcements to greater 
		 * than 20, since only 20 will be displayed no matter what.
		 */
		if (isSynopticTool() && state.getDisplayOptions().getNumberOfAnnouncements() > 20) 
		{
			addAlert(sstate, rb.getFormattedMessage("java.alert.customsize.number", new Object[] { 20 }));
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
		if (isMotd(state.getChannelId()))
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
        return formattedText.processFormattedText(strFromBrowser, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.cheftool.PagedResourceActionII#readResourcesPage(org.sakaiproject.service.framework.session.SessionState, int, int)
	 */
	protected List<AnnouncementWrapper> readResourcesPage(SessionState state, int first, int last)
	{
		List<AnnouncementWrapper> rv = (List) state.getAttribute("messages");
		if (rv == null) return new Vector<>();

		String sortedBy = "";
		if (state.getAttribute(STATE_CURRENT_SORTED_BY) != null) sortedBy = state.getAttribute(STATE_CURRENT_SORTED_BY).toString();

		boolean asc = false;
		if (state.getAttribute(STATE_CURRENT_SORT_ASC) != null)
			asc = ((Boolean) state.getAttribute(STATE_CURRENT_SORT_ASC)).booleanValue();

		if ((sortedBy == null) || sortedBy.equals(""))
		{
			sortedBy = isOnWorkspaceTab() ? SORT_DATE : getCurrentOrder();
			asc = false;
		}
		SortedIterator<AnnouncementWrapper> rvSorted = new SortedIterator<>(rv.iterator(), new AnnouncementWrapperComparator(sortedBy, asc));

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
			log.warn("{}.getState(): portlet null", this);
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
			log.warn("{}.getState(): peid null", this);
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
			log.error("{}.getState", this, e);
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
			log.error("", e);
		}

	} // releaseState

	// ******* end of copy from VelocityPortletStateAction

	/**
	 * Pulls excluded site ids from Tabs preferences
	 */
	private List<String> getExcludedSitesFromTabs() {
	    PreferencesService m_pre_service = (PreferencesService) ComponentManager.get(PreferencesService.class.getName());
	    final Preferences prefs = m_pre_service.getPreferences(SessionManager.getCurrentSessionUserId());
	    final ResourceProperties props = prefs.getProperties(PreferencesService.SITENAV_PREFS_KEY);
	    final List<String> l = props.getPropertyList(TAB_EXCLUDED_SITES);
	    return l;
	}

	/**
	 * Is the channel the message of the day.
	 * @param channelId The channel ID.
	 * @return <code>true</code> if the channel is the Message Of The Day (MOTD).
	 */
	private boolean isMotd(String channelId) {
		return channelId.endsWith("motd");
	}

}
