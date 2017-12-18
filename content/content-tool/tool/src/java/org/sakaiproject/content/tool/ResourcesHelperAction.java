/**********************************************************************************
 * $URL:	 $
 * $Id:	$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.content.tool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.util.FileItem;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.*;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.api.NotificationEdit;

@Slf4j
public class ResourcesHelperAction extends VelocityPortletPaneledAction 
{
	 private static final ResourceConditionsHelper conditionsHelper = new ResourceConditionsHelper();
	 
	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("types");
	private static ResourceLoader metaLang = new ResourceLoader("metadata");
	private static ResourceLoader contentResourceBundle = new ResourceLoader("content");
	
	protected  static final String ACCESS_HTML_TEMPLATE = "resources/sakai_access_html";

	protected  static final String ACCESS_TEXT_TEMPLATE = "resources/sakai_access_text";

	protected  static final String ACCESS_UPLOAD_TEMPLATE = "resources/sakai_access_upload";
	protected  static final String ACCESS_URL_TEMPLATE = "resources/sakai_access_url";
	
	/** copyright path -- MUST have same value as AccessServlet.COPYRIGHT_PATH */
	public static final String COPYRIGHT_PATH = Entity.SEPARATOR + "copyright";
	private static final String COPYRIGHT_ALERT_URL = ServerConfigurationService.getAccessUrl() + COPYRIGHT_PATH;
	
	protected  static final String CREATE_FOLDERS_TEMPLATE = "resources/sakai_create_folders";
	protected  static final String CREATE_HTML_TEMPLATE = "resources/sakai_create_html";
	protected  static final String CREATE_TEXT_TEMPLATE = "resources/sakai_create_text";
	protected  static final String CREATE_UPLOAD_TEMPLATE = "resources/sakai_create_upload";
	protected  static final String CREATE_UPLOADS_TEMPLATE = "resources/sakai_create_uploads";
	protected  static final String CREATE_URL_TEMPLATE = "resources/sakai_create_url";
	protected static final String CREATE_URLS_TEMPLATE = "resources/sakai_create_urls";
	
	public static final String MODE_MAIN = "main";
	protected static final String PREFIX = "ResourceTypeHelper.";
	
	protected  static final String REVISE_HTML_TEMPLATE = "resources/sakai_revise_html";
	protected  static final String REVISE_TEXT_TEMPLATE = "resources/sakai_revise_text";
	protected  static final String REVISE_UPLOAD_TEMPLATE = "resources/sakai_revise_upload";
	protected  static final String REVISE_URL_TEMPLATE = "resources/sakai_revise_url";
	
	protected static final String REPLACE_CONTENT_TEMPLATE = "resources/sakai_replace_file";
	
	protected static final String MAKE_SITE_PAGE_TEMPLATE = "content/sakai_make_site_page";

    protected static final String ERROR_PAGE_TEMPLATE = "resources/sakai_error_page";

	/** The content type image lookup service in the State. */
	private static final String STATE_CONTENT_TYPE_IMAGE_SERVICE = PREFIX + "content_type_image_service";
	
	private static final String STATE_COPYRIGHT_FAIRUSE_URL = PREFIX + "copyright_fairuse_url";

	private static final String STATE_COPYRIGHT_NEW_COPYRIGHT = PREFIX + "new_copyright";
	
	/** copyright related info */
	private static final String STATE_COPYRIGHT_TYPES = PREFIX + "copyright_types";

	private static final String STATE_DEFAULT_COPYRIGHT = PREFIX + "default_copyright";
	
	private static final String STATE_DEFAULT_COPYRIGHT_ALERT = PREFIX + "default_copyright_alert";
	
	/** state attribute for the maximum size for file upload */
	static final String STATE_FILE_UPLOAD_MAX_SIZE = PREFIX + "file_upload_max_size";
	
	/** The user copyright string */
	private static final String	STATE_MY_COPYRIGHT = PREFIX + "mycopyright";
	
	private static final String STATE_NEW_COPYRIGHT_INPUT = PREFIX + "new_copyright_input";
  
	/** state attribute indicating whether users in current site should be denied option of making resources public */
	private static final String STATE_PREVENT_PUBLIC_DISPLAY = PREFIX + "prevent_public_display";
	
	/** state attribute indicating whether we're using the Creative Commons dialog instead of the "old" copyright dialog */
	protected static final String STATE_USING_CREATIVE_COMMONS = PREFIX + "usingCreativeCommons";
	
	/** name of state attribute for the default retract time */
	protected static final String STATE_DEFAULT_RETRACT_TIME = PREFIX + "default_retract_time";
	
	/** The title of the new page to be created in the site */
	protected static final String STATE_PAGE_TITLE = PREFIX + "page_title";
	
	/** We need to send a single email with every D&D upload reported in it */
	private static final String DRAGNDROP_FILENAME_REFERENCE_LIST = "dragndrop_filename_reference_list";	

	private NotificationService notificationService = (NotificationService) ComponentManager.get(NotificationService.class);	
	private EventTrackingService eventTrackingService = (EventTrackingService) ComponentManager.get(EventTrackingService.class);

	public String buildAccessContext(VelocityPortlet portlet,
			Context context,
			RunData data,
			SessionState state)
	{
		log.debug("{}.buildAccessContext()", this);
		String template = ACCESS_TEXT_TEMPLATE;
		return template;
	}


	
	public String buildCreateContext(VelocityPortlet portlet,
			Context context,
			RunData data,
			SessionState state)
	{
		log.debug("{}.buildCreateContext()", this);
		String template = CREATE_UPLOAD_TEMPLATE;
		
		ToolSession toolSession = SessionManager.getCurrentToolSession();

		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);

		context.put(ResourcesAction.PIPE_INIT_ID, pipe.getInitializationId());

		//Reference reference = (Reference) toolSession.getAttribute(ResourceToolAction.COLLECTION_REFERENCE);
		String typeId = pipe.getAction().getTypeId();

		ListItem parent = new ListItem(pipe.getContentEntity());
		if(parent.isDropbox)
		{
			String dropboxNotificationsProperty = getDropboxNotificationsProperty();
			context.put("dropboxNotificationAllowed", Boolean.valueOf(ResourcesAction.DROPBOX_NOTIFICATIONS_ALLOW.equals(dropboxNotificationsProperty)));
		}
		
		if(ResourceType.TYPE_TEXT.equals(typeId))
		{
			template = CREATE_TEXT_TEMPLATE;
		}
		else if(ResourceType.TYPE_HTML.equals(typeId))
		{
			template = CREATE_HTML_TEMPLATE;
		}
		else if(ResourceType.TYPE_URL.equals(typeId))
		{
			template = CREATE_URL_TEMPLATE;
		}
		else // assume ResourceType.TYPE_UPLOAD
		{
			template = CREATE_UPLOAD_TEMPLATE;
		}
		
		int requestStateId = ResourcesAction.preserveRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST});
		context.put("requestStateId", requestStateId);

		return template;
	}

	public String buildMainPanelContext(VelocityPortlet portlet,
			Context context,
			RunData data,
			SessionState state)
	{
		log.debug("{}.buildMainPanelContext()", this);
		context.put("tlang", rb);
		context.put("metaLang", metaLang);

		context.put("validator", new Validator());
		context.put("copyright_alert_url", COPYRIGHT_ALERT_URL);
		context.put("DOT", ListItem.DOT);
		context.put("calendarMap", new HashMap());

                String ezproxy = ServerConfigurationService.getString("content.ezproxy.prefix", "");

                if (ezproxy != null && ezproxy != "") {
                    context.put("ezproxyPrefix", ezproxy);
                } else {
                    context.put("ezproxyPrefix", false);
                }
		
		context.put("dateFormat", getDateFormatString());
		
		String mode = (String) state.getAttribute(ResourceToolAction.STATE_MODE);

		if (mode == null)
		{
			initHelper(portlet, context, data, state);
		}

		if(state.getAttribute(ResourcesAction.STATE_MESSAGE) != null)
		{
			context.put("itemAlertMessage", state.getAttribute(ResourcesAction.STATE_MESSAGE));
			state.removeAttribute(ResourcesAction.STATE_MESSAGE);
		}
		
		ContentTypeImageService contentTypeImageService = (ContentTypeImageService) state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE);
		context.put("contentTypeImageService", contentTypeImageService);
		
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		if(pipe == null)
		{
			String attributes = "ResourcesHelperAction.buildMainPanelContext() SAK-8449 dump of state.attributes:\n";
			List<String> attrNames = state.getAttributeNames();
			for(String attrName : attrNames)
			{
				Object val = state.getAttribute(attrName);
				if(val instanceof Collection)
				{
					int i = 0;
					for(Object obj : (Collection) val)
					{
						attributes += "\t" + attrName + "[" + i + "] ==> " + obj + "\n";
						i++;
					}
				}
				else
				{
					attributes += "\t" + attrName + " ==> " + val + "\n";
				}
			}
			attributes += "ResourcesHelperAction.buildMainPanelContext() SAK-8449 dump of toolSession.attributes:\n";
			Enumeration toolNames = toolSession.getAttributeNames();
			while(toolNames.hasMoreElements())
			{
				String name = (String) toolNames.nextElement();
				Object val = toolSession.getAttribute(name);
				if(val instanceof Collection)
				{
					int i = 0;
					for(Object obj : (Collection) val)
					{
						attributes += "\t" + name + "[" + i + "] ==> " + obj + "\n";
						i++;
					}
				}
				else
				{
					attributes += "\t" + name + " ==> " + val + "\n";
				}
			}
			log.debug(attributes, new Throwable());
            return ERROR_PAGE_TEMPLATE;
		}
		if(pipe.isActionCompleted())
		{
			return null;
		}
		
		context.put(ResourcesAction.PIPE_INIT_ID, pipe.getInitializationId());

		String actionId = pipe.getAction().getId();
		
		context.put("GROUP_ACCESS", AccessMode.GROUPED);
		context.put("SITE_ACCESS", AccessMode.SITE);
		context.put("INHERITED_ACCESS", AccessMode.INHERITED);
		
		context.put("TYPE_FOLDER", ResourceType.TYPE_FOLDER);
		context.put("TYPE_HTML", ResourceType.TYPE_HTML);
		context.put("TYPE_TEXT", ResourceType.TYPE_TEXT);
		context.put("TYPE_UPLOAD", ResourceType.TYPE_UPLOAD);
		context.put("TYPE_URL", ResourceType.TYPE_URL);

		String template = "";

		switch(pipe.getAction().getActionType())
		{
		case CREATE:
			template = buildCreateContext(portlet, context, data, state);
			break;
		case REVISE_CONTENT:
			template = buildReviseContext(portlet, context, data, state);
			break;
		case REPLACE_CONTENT:
			template = buildReplaceContext(portlet, context, data, state);
			break;
		case NEW_UPLOAD:
			template = buildUploadFilesContext(portlet, context, data, state);
			break;
		case NEW_FOLDER:
			template = buildNewFoldersContext(portlet, context, data, state);
			break;
		case NEW_URLS:
			template = buildNewUrlsContext(portlet, context, data, state);
			break;
		default:
			template = buildMakeSitePageContext(portlet, context, data, state);
			break;
		}
		
		return template;
	}

	public String buildMakeSitePageContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		log.debug("{}.buildMakeSitePage()", this);

		int requestStateId = ResourcesAction.preserveRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST});
		context.put("requestStateId", requestStateId);
		
		context.put("page", state.getAttribute(STATE_PAGE_TITLE));
		
		return MAKE_SITE_PAGE_TEMPLATE;
	}
	

	protected String buildNewUrlsContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	 {
		log.debug("{}.buildNewUrlsContext()", this);
		context.put("site_id", ToolManager.getCurrentPlacement().getContext());
		ToolSession toolSession = SessionManager.getCurrentToolSession();

		MultiFileUploadPipe pipe = (MultiFileUploadPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		
		context.put(ResourcesAction.PIPE_INIT_ID, pipe.getInitializationId());

		List<ResourceToolActionPipe> pipes = pipe.getPipes();
		
		Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
		if(defaultRetractDate == null)
		{
			defaultRetractDate = TimeService.newTime();
			state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
		}

		Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
		if(preventPublicDisplay == null)
		{
			preventPublicDisplay = Boolean.FALSE;
			state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
		}
		
		ListItem parent = new ListItem(pipe.getContentEntity());
		parent.setPubviewPossible(! preventPublicDisplay);
		ListItem model = new ListItem(pipe, parent, defaultRetractDate);
		model.initMetadataGroups();
		// model.setPubviewPossible(! preventPublicDisplay);
				
		context.put("model", model);
		context.put("type", model.getResourceTypeDef());
		
		context.put("pipes", pipes);
		
		if(ContentHostingService.isAvailabilityEnabled())
		{
			context.put("availability_is_enabled", Boolean.TRUE);
		}
		
		if(model.isDropbox)
		{
			String dropboxNotificationsProperty = getDropboxNotificationsProperty();
			context.put("dropboxNotificationAllowed", Boolean.valueOf(ResourcesAction.DROPBOX_NOTIFICATIONS_ALLOW.equals(dropboxNotificationsProperty)));
		}

                String ezproxy = ServerConfigurationService.getString("content.ezproxy.prefix", "");
                
                if (ezproxy != null && ezproxy != "") {
                    context.put("ezproxyPrefix", ezproxy);
                } else {
                    context.put("ezproxyPrefix", false);
                }
		
		ResourcesAction.copyrightChoicesIntoContext(state, context);
		ResourcesAction.publicDisplayChoicesIntoContext(state, context);
		ResourceConditionsHelper.buildConditionContext(context, state);
		
		int requestStateId = ResourcesAction.preserveRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST});
		context.put("requestStateId", requestStateId);
		
		// Get default notification ("r", "o" or "n") 
		context.put("noti", ServerConfigurationService.getString("content.default.notification", "n"));
		
		return CREATE_URLS_TEMPLATE;
	 }



	/**
	 * @param portlet
	 * @param context
	 * @param data
	 * @param state
	 * @return
	 */
	private String buildNewFoldersContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		log.debug("{}.buildNewFoldersContext()", this);
		context.put("site_id", ToolManager.getCurrentPlacement().getContext());
		ToolSession toolSession = SessionManager.getCurrentToolSession();

		MultiFileUploadPipe pipe = (MultiFileUploadPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		
		context.put(ResourcesAction.PIPE_INIT_ID, pipe.getInitializationId());

		List<ResourceToolActionPipe> pipes = pipe.getPipes();

		Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
		if(defaultRetractDate == null)
		{
			defaultRetractDate = TimeService.newTime();
			state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
		}

		Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
		if(preventPublicDisplay == null)
		{
			preventPublicDisplay = Boolean.FALSE;
			state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
		}
		
		ListItem parent = new ListItem(pipe.getContentEntity());
		parent.setPubviewPossible(! preventPublicDisplay);
		ListItem model = new ListItem(pipe, parent, defaultRetractDate);
		model.initMetadataGroups();
		// model.setPubviewPossible(! preventPublicDisplay);
		
		context.put("model", model);
		context.put("type", model.getResourceTypeDef());
		
		context.put("pipes", pipes);
				
		if(ContentHostingService.isAvailabilityEnabled())
		{
			context.put("availability_is_enabled", Boolean.TRUE);
		}	

		ResourcesAction.publicDisplayChoicesIntoContext(state, context);
		ResourceConditionsHelper.buildConditionContext(context, state);

		int requestStateId = ResourcesAction.preserveRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST});
		context.put("requestStateId", requestStateId);

		// Get default notification ("r", "o" or "n") 
		context.put("noti", ServerConfigurationService.getString("content.default.notification", "n"));
		
		return CREATE_FOLDERS_TEMPLATE;
	}



	/**
	 * @param portlet
	 * @param context
	 * @param data
	 * @param state
	 * @return
	 */
	protected String buildReplaceContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		log.debug("{}.buildReplaceContext()", this);
		ToolSession toolSession = SessionManager.getCurrentToolSession();

		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		context.put(ResourcesAction.PIPE_INIT_ID, pipe.getInitializationId());

		Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
		if(preventPublicDisplay == null)
		{
			preventPublicDisplay = Boolean.FALSE;
			state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
		}
		
		ListItem item = new ListItem(pipe.getContentEntity());
		item.setPubviewPossible(! preventPublicDisplay);
		
		if(item.isDropbox)
		{
			String dropboxNotificationsProperty = getDropboxNotificationsProperty();
			context.put("dropboxNotificationAllowed", Boolean.valueOf(ResourcesAction.DROPBOX_NOTIFICATIONS_ALLOW.equals(dropboxNotificationsProperty)));
		}
		
		context.put("item", item);
		
		int requestStateId = ResourcesAction.preserveRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST});
		context.put("requestStateId", requestStateId);

		// Get default notification ("r", "o" or "n") 
		context.put("noti", ServerConfigurationService.getString("content.default.notification", "n"));
		
		return REPLACE_CONTENT_TEMPLATE;
	}



	public String buildReviseContext(VelocityPortlet portlet,
			Context context,
			RunData data,
			SessionState state)
	{
		log.debug("{}.buildReviseContext()", this);
		String template = REVISE_TEXT_TEMPLATE;
		ToolSession toolSession = SessionManager.getCurrentToolSession();

		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);

		context.put(ResourcesAction.PIPE_INIT_ID, pipe.getInitializationId());

		//Reference reference = (Reference) toolSession.getAttribute(ResourceToolAction.COLLECTION_REFERENCE);
		String typeId = pipe.getAction().getTypeId();
		String mimetype = pipe.getMimeType();
		
		ListItem item = new ListItem(pipe.getContentEntity());
		context.put("item", item);
		
		// context.put("inDropbox", ContentHostingService.isInDropbox(pipe.getContentEntity().getId()));
		ResourceTypeRegistry registry = (ResourceTypeRegistry) ComponentManager.get(ResourceTypeRegistry.class);
		if(registry != null)
		{
			ResourceType typedef = registry.getType(typeId);
			if(typedef != null)
			{
				context.put("hasNotificationDialog", typedef.hasNotificationDialog());
			}
		}
		
		if(item.isDropbox)
		{
			String dropboxNotificationsProperty = getDropboxNotificationsProperty();
			context.put("dropboxNotificationAllowed", Boolean.valueOf(ResourcesAction.DROPBOX_NOTIFICATIONS_ALLOW.equals(dropboxNotificationsProperty)));
		}
		
		context.put("pipe", pipe);

		if(ResourceType.TYPE_TEXT.equals(typeId))
		{
			template = REVISE_TEXT_TEMPLATE;
		}
		else if(ResourceType.TYPE_HTML.equals(typeId))
		{
			template = REVISE_HTML_TEMPLATE;
		}
		else if(ResourceType.TYPE_URL.equals(typeId))
		{
			String decodedUrl = pipe.getContentstring();
			try {
				decodedUrl = URLDecoder.decode(pipe.getContentstring(), "UTF-8");
			} catch (Exception e){
				//cant decode, continue anyway with original string
			}
			context.put("decodedUrl", decodedUrl);
			template = REVISE_URL_TEMPLATE;
		}
		else if(ResourceType.TYPE_UPLOAD.equals(typeId) && mimetype != null && ResourceType.MIME_TYPE_HTML.equals(mimetype))
		{
			template = REVISE_HTML_TEMPLATE;
		}
		else if(ResourceType.TYPE_UPLOAD.equals(typeId) && mimetype != null && ResourceType.MIME_TYPE_TEXT.equals(mimetype))
		{
			template = REVISE_TEXT_TEMPLATE;
		}
		else // assume ResourceType.TYPE_UPLOAD
		{
			template = REVISE_UPLOAD_TEMPLATE;
		}
		
		int requestStateId = ResourcesAction.preserveRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST});
		context.put("requestStateId", requestStateId);

		// Get default notification ("r", "o" or "n") 
		context.put("noti", ServerConfigurationService.getString("content.default.notification", "n"));
		
		return template;
	}

	/**
	 * @param portlet
	 * @param context
	 * @param data
	 * @param state
	 * @return
	 */
	protected String buildUploadFilesContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		log.debug("{}.buildUploadFilesContext()", this);
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		context.put("site_id", ToolManager.getCurrentPlacement().getContext());
		
		String max_file_size_mb = (String) state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE);
		if(max_file_size_mb == null)
		{
			max_file_size_mb = "20";
		}
		context.put("uploadMaxSize", max_file_size_mb);
		
		String uploadMax = ServerConfigurationService.getString(ResourcesConstants.SAK_PROP_MAX_UPLOAD_FILE_SIZE);
		String instr_uploads = rb.getFormattedMessage("instr.uploads", new String[]{ uploadMax });
		context.put("instr_uploads", instr_uploads);

		String uploadWarning = rb.getFormattedMessage("label.overwrite.warning");
		context.put("label_overwrite_warning",uploadWarning);
		String instr_dnd_uploads = rb.getFormattedMessage("instr.dnd.uploads", new String[]{ uploadMax });
		context.put("instr_dnd_uploads", instr_dnd_uploads);

//		int max_bytes = 1024 * 1024;
//		try
//		{
//			max_bytes = Integer.parseInt(max_file_size_mb) * 1024 * 1024;
//		}
//		catch(Exception e)
//		{
//			// if unable to parse an integer from the value
//			// in the properties file, use 1 MB as a default
//			max_file_size_mb = "1";
//			max_bytes = 1024 * 1024;
//		}

		
		MultiFileUploadPipe pipe = (MultiFileUploadPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		
		context.put(ResourcesAction.PIPE_INIT_ID, pipe.getInitializationId());

		List<ResourceToolActionPipe> pipes = pipe.getPipes();
		
		Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
		if(defaultRetractDate == null)
		{
			defaultRetractDate = TimeService.newTime();
			state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
		}

		Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
		if(preventPublicDisplay == null)
		{
			preventPublicDisplay = Boolean.FALSE;
			state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
		}
		
		ListItem parent = new ListItem(pipe.getContentEntity());
		parent.setPubviewPossible(! preventPublicDisplay);
		ListItem model = new ListItem(pipe, parent, defaultRetractDate);
		model.initMetadataGroups();
		// model.setPubviewPossible(! preventPublicDisplay);
				
		context.put("model", model);
		context.put("type", model.getResourceTypeDef());
		
		context.put("pipes", pipes);
		
		if(ContentHostingService.isAvailabilityEnabled())
		{
			context.put("availability_is_enabled", Boolean.TRUE);
			context.put("upload_visibility_hidden", ServerConfigurationService.getBoolean("content.dnd.visibility.hidden", false));
		}
		
		if(model.isDropbox)
		{
			String dropboxNotificationsProperty = getDropboxNotificationsProperty();
			context.put("dropboxNotificationAllowed", Boolean.valueOf(ResourcesAction.DROPBOX_NOTIFICATIONS_ALLOW.equals(dropboxNotificationsProperty)));
		}
		
		ResourcesAction.copyrightChoicesIntoContext(state, context);
		ResourcesAction.publicDisplayChoicesIntoContext(state, context);
		
		String defaultCopyrightStatus = (String) state.getAttribute(STATE_DEFAULT_COPYRIGHT);
		if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
		{
			defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
			state.setAttribute(STATE_DEFAULT_COPYRIGHT, defaultCopyrightStatus);
		}

		context.put("defaultCopyrightStatus", defaultCopyrightStatus);
		
		ResourceConditionsHelper.buildConditionContext(context, state);
	
		int requestStateId = ResourcesAction.preserveRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST});
		context.put("requestStateId", requestStateId);

		// Get default notification ("r", "o" or "n") 
		context.put("noti", ServerConfigurationService.getString("content.default.notification", "n"));
		
		return CREATE_UPLOADS_TEMPLATE;
	}
	

	public void doFinishUpload (RunData data){
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);

		if(pipe != null)
		{
			pipe.setActionCanceled(false);
			pipe.setErrorEncountered(false);
			pipe.setActionCompleted(true);
		}
		
		log.debug("{}.doFinishUpload() finished action", this);
		toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
	}	
	

	public void doCancel(RunData data)
	{
		log.debug("{}.doCancel()", this);
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		
		int requestStateId = params.getInt("requestStateId", 0);
		ResourcesAction.restoreRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST}, requestStateId);
		
		//Tool tool = ToolManager.getCurrentTool();
		//String url = (String) toolSession.getAttribute(tool.getId() + Tool.HELPER_DONE_URL);
		//toolSession.removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);
		
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);

		if(pipe != null)
		{
			String pipe_init_id = pipe.getInitializationId();
			String response_init_id = params.getString(ResourcesAction.PIPE_INIT_ID);
			if(pipe_init_id == null || response_init_id == null || ! response_init_id.equalsIgnoreCase(pipe_init_id))
			{
				pipe.setErrorEncountered(true);
				pipe.setActionCanceled(false);
			}
			else
			{
				pipe.setErrorEncountered(false);
				pipe.setActionCanceled(true);
			}
			
			pipe.setActionCompleted(false);
	
			toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
		
		}
		
		
	}
	
	public void doContinue(RunData data)
	{
		log.debug("{}.doContinue()", this);
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		int requestStateId = params.getInt("requestStateId", 0);
		ResourcesAction.restoreRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST}, requestStateId);
		
		String content = params.getString("content");
		if(content == null)
		{
			addAlert(state, rb.getString("text.notext"));
			return;
		}
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		
//		Tool tool = ToolManager.getCurrentTool();
//		String url = (String) toolSession.getAttribute(tool.getId() + Tool.HELPER_DONE_URL);
//		toolSession.removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);

		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		if(pipe == null)
		{
			return;
		}
		
		if(pipe != null)
		{
			String pipe_init_id = pipe.getInitializationId();
			String response_init_id = params.getString(ResourcesAction.PIPE_INIT_ID);
			if(pipe_init_id == null || response_init_id == null || ! response_init_id.equalsIgnoreCase(pipe_init_id))
			{
					// in this case, prevent upload to wrong folder
					pipe.setErrorMessage(rb.getString("alert.try-again"));
					pipe.setActionCanceled(false);
					pipe.setErrorEncountered(true);
					pipe.setActionCompleted(false);
					return;
			}
				
			toolSession.setAttribute(ResourceToolAction.ACTION_PIPE, pipe);

		}
		
		String resourceType = pipe.getAction().getTypeId();
		String mimetype = pipe.getMimeType();
		
		ListItem item = new ListItem(pipe.getContentEntity());
		// notification
		int noti = determineNotificationPriority(params, item.isDropbox);

		pipe.setRevisedMimeType(pipe.getMimeType());
		if(ResourceType.TYPE_TEXT.equals(resourceType) || ResourceType.MIME_TYPE_TEXT.equals(mimetype))
		{
			pipe.setRevisedMimeType(ResourceType.MIME_TYPE_TEXT);
			pipe.setRevisedResourceProperty(ResourceProperties.PROP_CONTENT_ENCODING, ResourcesAction.UTF_8_ENCODING);
			pipe.setNotification(noti);

		}
		else if(ResourceType.TYPE_HTML.equals(resourceType) || ResourceType.MIME_TYPE_HTML.equals(mimetype))
		{
			StringBuilder alertMsg = new StringBuilder();
			content = FormattedText.processHtmlDocument(content, alertMsg);
			pipe.setRevisedMimeType(ResourceType.MIME_TYPE_HTML);
			pipe.setRevisedResourceProperty(ResourceProperties.PROP_CONTENT_ENCODING, ResourcesAction.UTF_8_ENCODING);
			pipe.setNotification(noti);
			if (alertMsg.length() > 0)
			{
				addAlert(state, alertMsg.toString());
				return;
			}
		}
		else if(ResourceType.TYPE_URL.equals(resourceType))
		{
			
			// SAK-23587 - properly escape the URL where required
			try {
				URL url = new URL(content);
				URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
				content = uri.toString();
			} catch (Exception e) {
				//ok to ignore, just use the original url
				log.debug("URL can not be encoded: {}:{}", e.getClass(), e.getCause());
			}
			
			pipe.setRevisedMimeType(ResourceType.MIME_TYPE_URL);
			pipe.setNotification(noti);
		}
		else if(ResourceType.TYPE_FOLDER.equals(resourceType))
		{
			MultiFileUploadPipe mfp = (MultiFileUploadPipe) pipe;
			int count = params.getInt("folderCount");
			mfp.setFileCount(count);
			
			List<ResourceToolActionPipe> pipes = mfp.getPipes();
			for(int i = 0; i < pipes.size(); i++)
			{
				ResourceToolActionPipe fp = pipes.get(i);
				String folderName = params.getString("folder" + (i + 1));
				fp.setFileName(folderName);
				fp.setNotification(noti);
			}
		}
		
		try 
		{
			pipe.setRevisedContent(content.getBytes(ResourcesAction.UTF_8_ENCODING));
			pipe.setActionCanceled(false);
			pipe.setErrorEncountered(false);
			pipe.setActionCompleted(true);
		} 
		catch (UnsupportedEncodingException e) 
		{
			log.warn("{}: {}", this, e.toString());
			addAlert(state, rb.getString("alert.utf8encoding"));
			pipe.setActionCanceled(false);
			pipe.setErrorEncountered(true);
			pipe.setActionCompleted(false);
		}
		
		toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);

	}
	
	public void doCreateFolders(RunData data)
	{
		log.debug("{}.doCreateFolders()", this);
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		ToolSession toolSession = SessionManager.getCurrentToolSession();
		
		int requestStateId = params.getInt("requestStateId", 0);
		ResourcesAction.restoreRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST}, requestStateId);
		
		MultiFileUploadPipe pipe = (MultiFileUploadPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		if(pipe == null)
		{
			return;
		}
		
		String pipe_init_id = pipe.getInitializationId();
		String response_init_id = params.getString(ResourcesAction.PIPE_INIT_ID);
		
		if(pipe_init_id == null || response_init_id == null || ! response_init_id.equalsIgnoreCase(pipe_init_id))
		{
			// in this case, prevent upload to wrong folder
			pipe.setErrorMessage(rb.getString("alert.try-again"));
			pipe.setActionCanceled(false);
			pipe.setErrorEncountered(true);
			pipe.setActionCompleted(false);
			return;
		}
		
		String resourceType = pipe.getAction().getTypeId();
		
		int count = params.getInt("fileCount");
		pipe.setFileCount(count);
		
		int lastIndex = params.getInt("lastIndex");
		
		ContentEntity entity = pipe.getContentEntity();
		ListItem parent = null;
		if(entity != null && entity instanceof ContentCollection)
		{
			ContentCollection containingCollection = (ContentCollection) entity;
			
			Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
			if(preventPublicDisplay == null)
			{
				preventPublicDisplay = Boolean.FALSE;
				state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
			}
			
			parent = new ListItem(entity);
			parent.setPubviewPossible(! preventPublicDisplay);
		}

		List<ResourceToolActionPipe> pipes = pipe.getPipes();
		
		int actualCount = 0;
		for(int i = 0; i <= lastIndex && actualCount < count; i++)
		{
			String exists = params.getString("exists" + ListItem.DOT + i);
			if(exists == null || "".equals(exists))
			{
				continue;
			}
			ResourceToolActionPipe fp = pipes.get(actualCount);
			String folderName = params.getString("content" + ListItem.DOT + i);
			if(folderName == null || folderName.trim().equals(""))
			{
				continue;
			}
			
			fp.setFileName(folderName);
			
			ListItem newFolder = (ListItem) fp.getRevisedListItem();
			if(newFolder == null)
			{
				if(parent == null)
				{
					newFolder = new ListItem(folderName);
				}
				else
				{
					Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
					if(defaultRetractDate == null)
					{
						defaultRetractDate = TimeService.newTime();
						state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
					}

					newFolder = new ListItem(fp, parent, defaultRetractDate);
					newFolder.setName(folderName);
					newFolder.setId(folderName);
				}
			}
			if(ListItem.isOptionalPropertiesEnabled())
			{
				newFolder.initMetadataGroups();
			}

			// capture properties
			newFolder.captureProperties(params, ListItem.DOT + i);
			if (newFolder.numberFieldIsInvalid) {
				addAlert(state, rb.getString("conditions.invalid.condition.argument"));
				return;
			}
			if (newFolder.numberFieldIsOutOfRange) {
				addAlert(state, rb.getFormattedMessage("conditions.condition.argument.outofrange", new String[] { newFolder.getConditionAssignmentPoints() }));
				return;
			}
			if(!"".equals(newFolder.metadataValidationFails)) {
				addAlert(state, metaLang.getFormattedMessage("metadata.validation.error", newFolder.metadataValidationFails));
				return;
			}
			//Control if groups are selected
			if (!ResourcesAction.checkGroups(params)) {
				addAlert(state, rb.getString("alert.youchoosegroup")); 
				return;
			}

			fp.setRevisedListItem(newFolder);
			
			ResourceConditionsHelper.saveCondition(newFolder, params, state, i);
			
			actualCount++;
		}

		if(actualCount > 0)
		{
			pipe.setActionCanceled(false);
			pipe.setErrorEncountered(false);
			pipe.setActionCompleted(true);
			
			toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
		}
		else
		{
			addAlert(state, rb.getString("alert.nofldr"));
		}

	}
	
	public void doReplace(RunData data)
	{
		log.debug("{}.doReplace()", this);
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		
		int requestStateId = params.getInt("requestStateId", 0);
		ResourcesAction.restoreRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST}, requestStateId);
		
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		if(pipe == null)
		{
			return;
		}
		
		String pipe_init_id = pipe.getInitializationId();
		String response_init_id = params.getString(ResourcesAction.PIPE_INIT_ID);
			
		if(pipe_init_id == null || response_init_id == null || ! response_init_id.equalsIgnoreCase(pipe_init_id))
		{
			// in this case, prevent upload to wrong folder
			pipe.setErrorMessage(rb.getString("alert.try-again"));
			pipe.setActionCanceled(false);
			pipe.setErrorEncountered(true);
			pipe.setActionCompleted(false);
			log.debug("{}.doReplace() setting error on pipe", this);
			String uploadMax = ServerConfigurationService.getString(ResourcesConstants.SAK_PROP_MAX_UPLOAD_FILE_SIZE);
			addAlert(state, rb.getFormattedMessage("alert.over-per-upload-quota", new Object[]{uploadMax}));
			return;
		}
		
		FileItem fileitem = null;
		try
		{
			fileitem = params.getFileItem("content");
		}
		catch(Exception e)
		{
			log.warn("Exception ", e);
		}
		
		if(fileitem == null)
		{
			String max_file_size_mb = (String) state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE);
			int max_bytes = 1024 * 1024;
			try
			{
				max_bytes = Integer.parseInt(max_file_size_mb) * 1024 * 1024;
			}
			catch(Exception e)
			{
				// if unable to parse an integer from the value
				// in the properties file, use 1 MB as a default
				max_file_size_mb = "1";
				max_bytes = 1024 * 1024;
			}
			
			String max_bytes_string = ResourcesAction.getFileSizeString(max_bytes, rb);
			// "The user submitted a file to upload but it was too big!"
			addAlert(state, rb.getFormattedMessage("size.exceeded", new Object[]{ max_bytes_string }));
			//max_file_size_mb + "MB " + rb.getString("exceeded2"));
		}
		else if (fileitem.getFileName() == null || fileitem.getFileName().length() == 0)
		{
			addAlert(state, rb.getString("choosefile7"));
		}
		else if (fileitem.getFileName().length() > 0)
		{
			String filename = Validator.getFileName(fileitem.getFileName());
			InputStream stream = fileitem.getInputStream();
			pipe.setRevisedContentStream(stream);
			String contentType = fileitem.getContentType().replaceAll("\"", "");
			pipe.setRevisedMimeType(contentType);
			pipe.setFileName(filename);

			if (ResourceType.MIME_TYPE_HTML.equals(contentType) || ResourceType.MIME_TYPE_TEXT.equals(contentType)) {
				pipe.setRevisedResourceProperty(ResourceProperties.PROP_CONTENT_ENCODING, ResourcesAction.UTF_8_ENCODING);
			} else if (pipe.getPropertyValue(ResourceProperties.PROP_CONTENT_ENCODING) != null) {
				pipe.setRevisedResourceProperty(ResourceProperties.PROP_CONTENT_ENCODING, (String) pipe.getPropertyValue(ResourceProperties.PROP_CONTENT_ENCODING));
			}

			ListItem newFile = new ListItem(pipe.getContentEntity());
			// notification
			int noti = determineNotificationPriority(params, newFile.isDropbox);
			newFile.setNotification(noti);

			pipe.setRevisedListItem(newFile);

			pipe.setActionCanceled(false);
			pipe.setErrorEncountered(false);
			pipe.setActionCompleted(true);

			toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
		}


	}
	
	public void doAddUrls(RunData data)
	{
		log.debug("{}.soAddUrls()", this);
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		
		int requestStateId = params.getInt("requestStateId", 0);
		ResourcesAction.restoreRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST}, requestStateId);
		
		MultiFileUploadPipe mfp = (MultiFileUploadPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		if(mfp == null)
		{
			return;
		}
		
		String pipe_init_id = mfp.getInitializationId();
		String response_init_id = params.getString(ResourcesAction.PIPE_INIT_ID);
	
		if(pipe_init_id == null || response_init_id == null || ! response_init_id.equalsIgnoreCase(pipe_init_id))
		{
			// in this case, prevent upload to wrong folder
			mfp.setErrorMessage(rb.getString("alert.try-again"));
			mfp.setActionCanceled(false);
			mfp.setErrorEncountered(true);
			mfp.setActionCompleted(false);
			return;
		}
		
		int count = params.getInt("fileCount");
		mfp.setFileCount(count);
		
		int lastIndex = params.getInt("lastIndex");
		
		ContentEntity entity = mfp.getContentEntity();
		ListItem parent = null;
		if(entity != null && entity instanceof ContentCollection)
		{
			ContentCollection containingCollection = (ContentCollection) entity;
			
			Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
			if(preventPublicDisplay == null)
			{
				preventPublicDisplay = Boolean.FALSE;
				state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
			}
			
			parent = new ListItem(entity);
			parent.setPubviewPossible(! preventPublicDisplay);
		}
		
		List<String> alerts = new ArrayList<String>();
		
		List<ResourceToolActionPipe> pipes = mfp.getPipes();
		
		int actualCount = 0;
		for(int i = 0; i <= lastIndex && actualCount < count; i++)
		{
			String exists = params.getString("exists" + ListItem.DOT + i);
			if(exists == null || "".equals(exists))
			{
				continue;
			}
			
			ResourceToolActionPipe pipe = pipes.get(actualCount);
			
			String url = params.getString("content" + ListItem.DOT + i );
			if(url == null)
			{
				continue;
			}
			else
			{
				try
				 {
					 url = ResourcesAction.validateURL(url);
				 }
				 catch (MalformedURLException e)
				 {
					 addAlert(state, rb.getFormattedMessage("url.invalid", new String[]{url}));
					 continue;
				 }
				
				 try {
					 pipe.setRevisedContent(url.getBytes(ResourcesAction.UTF_8_ENCODING));
				 } catch (UnsupportedEncodingException e) {
					 pipe.setRevisedContent(url.getBytes());
				 }
			}
			// SAK-11816 - allow much longer URLs by correcting a long basename, make sure no URL resource id exceeds 36 chars
			// Make the URL a length of 32 chars. This is because the basename registered in CR has to be the same than the basename in resources 
			if (url != null) {
			    // url with a mininum of 18 chars.
                while (url.length() < 18) {
                	url = url.concat(ListItem.DOT);
                }
                
                // max of 18 chars from the URL itself
                url = url.substring(0, 18);
                
                // add a timestamp to differentiate it (+14 chars)
                Format f= new SimpleDateFormat("yyyyMMddHHmmss");
                url += f.format(new Date());
                // total new length of 32 chars
            }
            // SAK-11816 - END
			
			pipe.setFileName(Validator.escapeResourceName(url));
			pipe.setRevisedMimeType(ResourceType.MIME_TYPE_URL);
				
			ListItem newFile = (ListItem) pipe.getRevisedListItem();
			if(newFile == null)
			{
				if(parent == null)
				{
					newFile = new ListItem(pipe.getFileName());
				}
				else
				{
					Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
					if(defaultRetractDate == null)
					{
						defaultRetractDate = TimeService.newTime();
						state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
					}

					newFile = new ListItem(mfp, parent, defaultRetractDate);
					newFile.setName(new String(pipe.getRevisedContent()));
					newFile.setId(pipe.getFileName());
				}
			}
			
			if(ListItem.isOptionalPropertiesEnabled())
			{
				newFile.initMetadataGroups();
			}
			
			// capture properties
			newFile.captureProperties(params, ListItem.DOT + i);
			if (newFile.numberFieldIsInvalid) {
				addAlert(state, rb.getString("conditions.invalid.condition.argument"));
				return;
			}
			if (newFile.numberFieldIsOutOfRange) {
				addAlert(state, rb.getFormattedMessage("conditions.condition.argument.outofrange", new String[] { newFile.getConditionAssignmentPoints() }));
				return;
			}
			if(!"".equals(newFile.metadataValidationFails)) {
				addAlert(state, metaLang.getFormattedMessage("metadata.validation.error", newFile.metadataValidationFails));
				return;
			}
			//Control if groups are selected
			if (!ResourcesAction.checkGroups(params)) {
				addAlert(state, rb.getString("alert.youchoosegroup")); 
				return;
			}
			
			// notification
			int noti = determineNotificationPriority(params, newFile.isDropbox);
			newFile.setNotification(noti);
			
			//alerts.addAll(newFile.checkRequiredProperties());
							
			pipe.setRevisedListItem(newFile);
			
			// capture properties
			newFile.captureProperties(params, ListItem.DOT + i);
			if (newFile.numberFieldIsInvalid) {
				addAlert(state, contentResourceBundle.getString("conditions.invalid.condition.argument"));
				return;
			}
			if (newFile.numberFieldIsOutOfRange) {
			    addAlert(state, contentResourceBundle.getFormattedMessage("conditions.condition.argument.outofrange", new String[] { newFile.getConditionAssignmentPoints() }));
				return;
			}
			if(!"".equals(newFile.metadataValidationFails)) {
				addAlert(state, metaLang.getFormattedMessage("metadata.validation.error", newFile.metadataValidationFails));
				return;
			}
			ResourceConditionsHelper.saveCondition(newFile, params, state, i);
				
			actualCount++;
			
		}
		if(! alerts.isEmpty())
		{
			for(String alert: alerts)
			{
				addAlert(state, alert);
			}
		}

		if(actualCount < 1)
		{
			addAlert(state, rb.getString("url.noinput"));
			return;
		}

		mfp.setActionCanceled(false);
		mfp.setErrorEncountered(false);
		mfp.setActionCompleted(true);
		
		toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);

	}

	/**
	 * @return
	 */
	protected String getDropboxNotificationsProperty()
	{
		Placement placement = ToolManager.getCurrentPlacement();
		Properties props = placement.getPlacementConfig();
		String dropboxNotifications = props.getProperty(ResourcesAction.DROPBOX_NOTIFICATIONS_PROPERTY);
		if(dropboxNotifications == null)
		{
			dropboxNotifications = ResourcesAction.DROPBOX_NOTIFICATIONS_DEFAULT_VALUE;
		}
		
		log.debug("{}.getDropboxNotificationsProperty() dropboxNotifications == {}", this, dropboxNotifications);

		return dropboxNotifications;
	}

	/**
	 * @param params
	 */
	protected int determineNotificationPriority(ParameterParser params, boolean contextIsDropbox) 
	{
		int noti = NotificationService.NOTI_NONE;
		// %%STATE_MODE_RESOURCES%%
		if (contextIsDropbox)
		{
			boolean notification = false;
			String notifyDropbox = getDropboxNotificationsProperty();
			
			if(ResourcesAction.DROPBOX_NOTIFICATIONS_ALWAYS.equals(notifyDropbox))
			{
				noti = NotificationService.NOTI_REQUIRED;
			}
			else if(ResourcesAction.DROPBOX_NOTIFICATIONS_ALLOW.equals(notifyDropbox))
			{
				notification = params.getBoolean("notify_dropbox");
				if(notification)
				{
					noti = NotificationService.NOTI_OPTIONAL;
				}
			}
			log.debug("{}.determineNotificationPriority() noti == {}", this, noti);
		}
		else
		{
			// read the notification options
			String notification = params.getString("notify");
			if ("r".equals(notification))
			{
				noti = NotificationService.NOTI_REQUIRED;
			}
			else if ("o".equals(notification))
			{
				noti = NotificationService.NOTI_OPTIONAL;
			}
		}
		return noti;
	}
	
	
	public void doUpload(RunData data)
	{
		log.debug("{}.doUpload()", this);
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		
		int requestStateId = params.getInt("requestStateId", 0);
		ResourcesAction.restoreRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST}, requestStateId);
		
		MultiFileUploadPipe mfp = (MultiFileUploadPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		if(mfp == null)
		{
			log.debug("{}.doUpload() mfp is null", this);
			return;
		}
		
		String pipe_init_id = mfp.getInitializationId();
		String response_init_id = params.getString(ResourcesAction.PIPE_INIT_ID);
		
		if(pipe_init_id == null || response_init_id == null || ! response_init_id.equalsIgnoreCase(pipe_init_id))
		{
			// in this case, prevent upload to wrong folder
			mfp.setErrorMessage(rb.getString("alert.try-again"));
			mfp.setActionCanceled(false);
			mfp.setErrorEncountered(true);
			mfp.setActionCompleted(false);
			log.debug("{}.doUpload() setting error on pipe", this);
			String uploadMax = ServerConfigurationService.getString(ResourcesConstants.SAK_PROP_MAX_UPLOAD_FILE_SIZE);
			addAlert(state, rb.getFormattedMessage("alert.over-per-upload-quota", new Object[]{uploadMax}));
			return;
		}
		
		log.debug(" after doUpload() setting error on pipe");
		int count = params.getInt("fileCount");
		mfp.setFileCount(count);
		if(count < 1)
		{
			mfp.setFileCount(1);
		}
		
		int lastIndex = params.getInt("lastIndex");
		
		List<String> allAlerts = new ArrayList<String>();
		
		ContentEntity entity = mfp.getContentEntity();
		ListItem parent = null;
		if(entity != null && entity instanceof ContentCollection)
		{
			ContentCollection containingCollection = (ContentCollection) entity;
			
			Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
			if(preventPublicDisplay == null)
			{
				preventPublicDisplay = Boolean.FALSE;
				state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
			}
			
			parent = new ListItem(entity);
			parent.setPubviewPossible(! preventPublicDisplay);
		}
		
		List<ResourceToolActionPipe> pipes = mfp.getPipes();
	
		log.debug("{}.doUpload() iterating through pipes", this);

		int uploadCount = 0;
		
		for(int i = 0, c = 0; i <= lastIndex && c < count; i++)
		{
			String exists = params.getString("exists" + ListItem.DOT + i);
			if(exists == null || "".equals(exists))
			{
				continue;
			}
			
			ResourceToolActionPipe pipe = pipes.get(c);
			
			FileItem fileitem = null;
			try
			{
				fileitem = params.getFileItem("content" + ListItem.DOT + i );
			}
			catch(Exception e)
			{
				log.warn("Exception ", e);
			}
			
			if(fileitem == null)
			{
				String max_file_size_mb = (String) state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE);
				int max_bytes = 1024 * 1024;
				try
				{
					max_bytes = Integer.parseInt(max_file_size_mb) * 1024 * 1024;
				}
				catch(Exception e)
				{
					// if unable to parse an integer from the value
					// in the properties file, use 1 MB as a default
					max_file_size_mb = "1";
					max_bytes = 1024 * 1024;
				}
				
				String max_bytes_string = ResourcesAction.getFileSizeString(max_bytes, rb);
				// "The user submitted a file to upload but it was too big!"
				addAlert(state, rb.getFormattedMessage("size.exceeded", new Object[]{ max_bytes_string }));
				//max_file_size_mb + "MB " + rb.getString("exceeded2"));
			}
			else if (fileitem.getFileName() == null || fileitem.getFileName().length() == 0)
			{
				// no file selected -- skip this one
			}
			else if (fileitem.getFileName().length() > 0)
			{
				String filename = Validator.getFileName(fileitem.getFileName());
				pipe.setRevisedContentStream( fileitem.getInputStream() );
				String contentType = fileitem.getContentType().replaceAll("\"", "");
				pipe.setRevisedMimeType(contentType);
				
				// If no encoding specified, default to UTF-8 encoding
				if ( (ResourceType.MIME_TYPE_HTML.equals(contentType) || ResourceType.MIME_TYPE_TEXT.equals(contentType)) &&
						pipe.getPropertyValue(ResourceProperties.PROP_CONTENT_ENCODING) == null)
				{
						pipe.setRevisedResourceProperty(ResourceProperties.PROP_CONTENT_ENCODING, ResourcesAction.UTF_8_ENCODING);
				}
				
				pipe.setFileName(filename);
					 
				ListItem newFile = (ListItem) pipe.getRevisedListItem();
				if(newFile == null)
				{
					if(parent == null)
					{
						newFile = new ListItem(filename);
					}
					else
					{
						Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
						if(defaultRetractDate == null)
						{
							defaultRetractDate = TimeService.newTime();
							state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
						}

						newFile = new ListItem(pipe, parent, defaultRetractDate);
						newFile.setName(filename);
						newFile.setId(filename);
					}
				}

				if(ListItem.isOptionalPropertiesEnabled())
				{
					newFile.initMetadataGroups();
				}

				// capture properties
				newFile.captureProperties(params, ListItem.DOT + i);
				
				// notification
				int noti = determineNotificationPriority(params, newFile.isDropbox);
				newFile.setNotification(noti);
				// allAlerts.addAll(newFile.checkRequiredProperties());
				
				pipe.setRevisedListItem(newFile);
				
				// capture properties
				newFile.captureProperties(params, ListItem.DOT + i);
				if (newFile.numberFieldIsInvalid) {
					addAlert(state, contentResourceBundle.getString("conditions.invalid.condition.argument"));
					return;
				}
				if (newFile.numberFieldIsOutOfRange) {
				    addAlert(state, contentResourceBundle.getFormattedMessage("conditions.condition.argument.outofrange", new String[] { newFile.getConditionAssignmentPoints() }));
					return;
				}
				if(!"".equals(newFile.metadataValidationFails)) {
					addAlert(state, metaLang.getFormattedMessage("metadata.validation.error", newFile.metadataValidationFails));
					return;
				}
				//Control if groups are selected
				if (!ResourcesAction.checkGroups(params)) {
					addAlert(state, rb.getString("alert.youchoosegroup")); 
					return;
				}
				
				ResourceConditionsHelper.saveCondition(newFile, params, state, i);
				
				uploadCount++;
				
			}
			c++;
			
		}
		log.debug("{}.doUpload() checking upload count", this);
		
		if(uploadCount < 1 && state.getAttribute(ResourcesAction.STATE_MESSAGE) == null)
		{
			log.debug("{}.doUpload() no files uploaded", this);

			HttpServletRequest req = data.getRequest();
			String status = (String) req.getAttribute("upload.status");
			log.debug("Printing out upload.status: {}", status);
			if(status == null)
			{
				log.warn("No files uploaded; upload.status == null");
			}
			else if("ok".equals(status))
			{
				log.warn("No files uploaded; upload.status == ok");
			}
			else if("size_limit_exceeded".equals(status))
			{
				String max_file_size_mb = (String) state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE);
				int max_bytes = 1024 * 1024;
				try
				{
					max_bytes = Integer.parseInt(max_file_size_mb) * 1024 * 1024;
				}
				catch(Exception e)
				{
					// if unable to parse an integer from the value
					// in the properties file, use 1 MB as a default
					max_file_size_mb = "1";
					max_bytes = 1024 * 1024;
				}
				
				String max_bytes_string = ResourcesAction.getFileSizeString(max_bytes, rb);
				
				addAlert(state, rb.getFormattedMessage("size.exceeded", new Object[]{ max_bytes_string }));
			}
			else if("exception".equals(status))
			{
				log.warn("No files uploaded; upload.status == exception");
				addAlert(state, rb.getString("choosefile7"));
			}
		}
		log.debug("{}.doUpload() checking allAlerts", this);
		if(! allAlerts.isEmpty())
		{
			for(String alert: allAlerts)
			{
				addAlert(state, alert);
			}
		}

		log.debug("{}.doUpload() checking messages", this);
		if(state.getAttribute(ResourcesAction.STATE_MESSAGE) == null)
		{
			mfp.setActionCanceled(false);
			mfp.setErrorEncountered(false);
			mfp.setActionCompleted(true);

			log.debug("{}.doUpload() no error messages", this);

			toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
		}

	}
	
	protected void initHelper(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		log.debug("{}.initHelper()", this);
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		//toolSession.setAttribute(ResourceToolAction.STARTED, Boolean.TRUE);
		//state.setAttribute(ResourceToolAction.STATE_MODE, MODE_MAIN);
		if(state.getAttribute(STATE_USING_CREATIVE_COMMONS) == null)
		{
			String usingCreativeCommons = ServerConfigurationService.getString("copyright.use_creative_commons");
			if( usingCreativeCommons != null && usingCreativeCommons.equalsIgnoreCase(Boolean.TRUE.toString()))
			{
				state.setAttribute(STATE_USING_CREATIVE_COMMONS, Boolean.TRUE.toString());
			}
			else
			{
				state.setAttribute(STATE_USING_CREATIVE_COMMONS, Boolean.FALSE.toString());
			}
		}

		if (state.getAttribute(STATE_COPYRIGHT_TYPES) == null)
		{
			if (ServerConfigurationService.getStrings("copyrighttype") != null)
			{
				state.setAttribute(STATE_COPYRIGHT_TYPES, new ArrayList(Arrays.asList(ServerConfigurationService.getStrings("copyrighttype"))));
			}
		}

		if (state.getAttribute(STATE_DEFAULT_COPYRIGHT) == null)
		{
			if (ServerConfigurationService.getString("default.copyright") != null)
			{
				state.setAttribute(STATE_DEFAULT_COPYRIGHT, ServerConfigurationService.getString("default.copyright"));
			}
		}

		if (state.getAttribute(STATE_DEFAULT_COPYRIGHT_ALERT) == null)
		{
			if (ServerConfigurationService.getString("default.copyright.alert") != null)
			{
				state.setAttribute(STATE_DEFAULT_COPYRIGHT_ALERT, ServerConfigurationService.getString("default.copyright.alert"));
			}
		}

		if (state.getAttribute(STATE_NEW_COPYRIGHT_INPUT) == null)
		{
			if (ServerConfigurationService.getString("newcopyrightinput") != null)
			{
				state.setAttribute(STATE_NEW_COPYRIGHT_INPUT, ServerConfigurationService.getString("newcopyrightinput"));
			}
		}

		if (state.getAttribute(STATE_COPYRIGHT_FAIRUSE_URL) == null)
		{
			if (ServerConfigurationService.getString("fairuse.url") != null)
			{
				state.setAttribute(STATE_COPYRIGHT_FAIRUSE_URL, ServerConfigurationService.getString("fairuse.url"));
			}
		}

		if (state.getAttribute(STATE_COPYRIGHT_NEW_COPYRIGHT) == null)
		{
			if (ServerConfigurationService.getString("copyrighttype.new") != null)
			{
				state.setAttribute(STATE_COPYRIGHT_NEW_COPYRIGHT, ServerConfigurationService.getString("copyrighttype.new"));
			}
		}

		if (state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE) == null)
		{
			String uploadMax = ServerConfigurationService.getString(ResourcesConstants.SAK_PROP_MAX_UPLOAD_FILE_SIZE);
			String uploadCeiling = ServerConfigurationService.getString("content.upload.ceiling");
			
			if(uploadMax == null && uploadCeiling == null)
			{
				state.setAttribute(STATE_FILE_UPLOAD_MAX_SIZE, ResourcesConstants.DEFAULT_MAX_FILE_SIZE_STRING );
			}
			else if(uploadCeiling == null)
			{
				state.setAttribute(STATE_FILE_UPLOAD_MAX_SIZE, uploadMax);
			}
			else if(uploadMax == null)
			{
				state.setAttribute(STATE_FILE_UPLOAD_MAX_SIZE, null);
			}
			else
			{
				int maxNum = Integer.MAX_VALUE;
				int ceilingNum = Integer.MAX_VALUE;
				try
				{
					maxNum = Integer.parseInt(uploadMax);
				}
				catch(Exception e)
				{
				}
				try
				{
					ceilingNum = Integer.parseInt(uploadCeiling);
				}
				catch(Exception e)
				{
				}

				if(ceilingNum < maxNum)
				{
					state.setAttribute(STATE_FILE_UPLOAD_MAX_SIZE, uploadCeiling);
				}
				else
				{
					state.setAttribute(STATE_FILE_UPLOAD_MAX_SIZE, uploadMax);
				}
			}
			
		}
		
		state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, Boolean.FALSE);
		String[] siteTypes = ServerConfigurationService.getStrings("prevent.public.resources");
		String siteType = null;
		Site site;
		try
		{
			site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
			siteType = site.getType();
			if(siteTypes != null)
			{
				for(int i = 0; i < siteTypes.length; i++)
				{
					if ((StringUtils.trimToNull(siteTypes[i])).equals(siteType))
					{
						state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, Boolean.TRUE);
						break;
					}
				}
			}
		}
		catch (IdUnusedException e)
		{
			// allow public display
		}
		catch(NullPointerException e)
		{
			// allow public display
		}

		state.setAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE, org.sakaiproject.content.cover.ContentTypeImageService.getInstance());
	}
	
	protected void toolModeDispatch(String methodBase, String methodExt, HttpServletRequest req, HttpServletResponse res)
		throws ToolException
	{
		log.debug("{}.toolModeDispatch()", this);
		SessionState sstate = getState(req);
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		
		//String mode = (String) sstate.getAttribute(ResourceToolAction.STATE_MODE);
		//Object started = toolSession.getAttribute(ResourceToolAction.STARTED);
		Object done = toolSession.getAttribute(ResourceToolAction.DONE);
		
		if (done != null)
		{
			toolSession.removeAttribute(ResourceToolAction.STARTED);
			Tool tool = ToolManager.getCurrentTool();
		
			String url = (String) SessionManager.getCurrentToolSession().getAttribute(tool.getId() + Tool.HELPER_DONE_URL);
			log.debug("{}.toolModeDispatch() url == {}", this, url);
		
			SessionManager.getCurrentToolSession().removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);
		
			try
			{
				res.sendRedirect(url);
			}
			catch (IOException e)
			{
				log.warn("{}.toolModeDispatch() IOException {}", this, e);
			}
			log.debug("{}.toolModeDispatch() returning", this);
			return;
		}
		log.debug("{}.toolModeDispatch() calling super.toolModeDispatch({}, methodExt, , )", this, methodBase, req, res);
		
		super.toolModeDispatch(methodBase, methodExt, req, res);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
	{
		String fullPath = request.getParameter("fullPath");
		String action = request.getParameter("sakai_action");
		boolean hidden = Boolean.valueOf(request.getParameter("hidden"));
		log.debug("Received action: {} for file: {} with visibilty {}", action, fullPath, Boolean.toString(!hidden));
		
		// set up rundata, in case we're called from RSF
		checkRunData(request);

		if(fullPath != null)
		{
			Long fileSize = Long.parseLong(request.getHeader("content-length"));
			Long uploadMax;
			Long siteQuota;
			
			try
			{
				uploadMax = Long.parseLong( ServerConfigurationService.getString( ResourcesConstants.SAK_PROP_MAX_UPLOAD_FILE_SIZE, 
																					ResourcesConstants.DEFAULT_MAX_FILE_SIZE_STRING ) );
			}
			catch( NumberFormatException ex )
			{
				log.debug( "sakai.property '{}' does not contain an integer {}", ResourcesConstants.SAK_PROP_MAX_UPLOAD_FILE_SIZE, ex);
				uploadMax = ResourcesConstants.DEFAULT_MAX_FILE_SIZE;
			}
			
			try
			{
				siteQuota = Long.parseLong( ServerConfigurationService.getString( ResourcesConstants.SAK_PROP_MASTER_SITE_QUOTA, 
																					ResourcesConstants.DEFAULT_SITE_QUOTA_STRING ) );
			}
			catch( NumberFormatException ex )
			{
				log.debug( "sakai.property '{}' does not contain an integer {}", ResourcesConstants.SAK_PROP_MASTER_SITE_QUOTA, ex);
				siteQuota = ResourcesConstants.DEFAULT_SITE_QUOTA;
			}
			
			// Get the site type specific site quota
			String siteTypeQuotaProp = "";
			Long siteTypeQuota = null;
			try
			{
				Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
				siteTypeQuotaProp = ResourcesConstants.SAK_PROP_MASTER_SITE_QUOTA + "." + site.getType();
				siteTypeQuota = Long.parseLong( ServerConfigurationService.getString( siteTypeQuotaProp ) );
			}
			catch( IdUnusedException ex )
			{
				log.warn( "Can't find site: ", ex );
			}
			catch( NumberFormatException ex )
			{
				log.debug( "sakai.property '{}' does not contain an integer {}", siteTypeQuotaProp, ex);
			}
			
			// Determine which site quota to use (if the site specific quota is set, it over-rides the global one)
			if( siteTypeQuota != null )
			{
				siteQuota = siteTypeQuota;
			}

			// Determine if the site quota is unlimited
			boolean siteQuotaUnlimited = siteQuota == 0;
			
			// If the file size exceeds the max uploaded file size, post error message
			Long fileSizeKB = fileSize / 1024L;
			Long fileSizeMB = fileSize / 1024L / 1024L;
			if( fileSizeMB > uploadMax )
			{
				addAlert(getState(request), rb.getFormattedMessage("alert.over-per-upload-quota", new Object[]{uploadMax}));
			}
			// If the file size exceeds the max quota for the site, post error message
			else if( !siteQuotaUnlimited && fileSizeKB > siteQuota )
			{
				addAlert(getState(request), rb.getFormattedMessage("alert.over-site-upload-quota", new Object[]{siteQuota}));
			}
			// Otherwise, continue with upload process
			else
			{
				JetspeedRunData rundata = (JetspeedRunData) request.getAttribute(ATTR_RUNDATA);
				if (checkCSRFToken(request,rundata,action)) {
					doDragDropUpload(request, response, fullPath, hidden);
				}
			}
		}
		else
		{
			if (action!=null)
			{
			    JetspeedRunData rundata = (JetspeedRunData) request.getAttribute(ATTR_RUNDATA);
			    if (checkCSRFToken(request,rundata,action)) {
				if (action.equals("doFinishUpload"))
				{
					notifyDragAndDropCompleted(request);
				}
				super.doPost(request, response);
			    }
			}
			else
			{
				//Action NULL - Here we have a folder uploaded in a browser that does not support it.
				log.warn("Action null and file null in ResourcesHelperAction");
				
				//RequestFilter throws an Exception when a folder is uploaded from a not valid browser (anyone but Chrome 21+):
				//org.apache.commons.fileupload.FileUploadBase$IOFileUploadException: Processing of multipart/form-data request failed. Stream ended unexpectedly
				
				//No way to handle this in server side, unless we modify RequestFilter or create our own FileUpload handler.
				//The easiest fix is to handle not valid folders in client side.
				//It is done, so this piece of code should never be reached.
			}
		}
	}


	private synchronized void doDragDropUpload(HttpServletRequest request, HttpServletResponse response, String fullPath, boolean hidden)
	{
		//Feel free to sent comments/warnings/advices to me at daniel.merino AT unavarra.es
		
		//Full method must be synchronized because a collection can edited and committed in two concurrent requests
		//Dropzone allows multiple parallel uploads so if all this process is not synchronized, InUseException is thrown
		//Maybe a more sophisticated concurrence can be set in the future
		
		SessionState state = getState(request);
		ToolSession toolSession = SessionManager.getCurrentToolSession();

		ContentResourceEdit resource = null;
		ContentCollectionEdit collection= null;

		String uploadFileName=null;
		String collectionName=null;
		String resourceId = null;
		String overwrite = request.getParameter("overwrite");
		
		String resourceGroup = toolSession.getAttribute("resources.request.create_wizard_collection_id").toString();

		if (!("undefined".equals(fullPath) || "".equals(fullPath)))
		{
			//Received a file that is inside an uploaded folder 
			//Try to create a collection with this folder and to add the file inside it after
			File myfile = new File(fullPath);
			String fileName = myfile.getName();
			collectionName=resourceGroup+myfile.getParent();

			//AFAIK it is not possible to check undoubtedly if a collection exists
			//isCollection() only tests if name is valid and checkCollection() returns void type
			//So the procedure is to create the collection and capture thrown Exceptions
			collection = createCollectionIfNotExists(collectionName);
			
			if (collection==null)
			{
				addAlert(state,contentResourceBundle.getFormattedMessage("dragndrop.collection.error",new Object[]{collectionName,fileName}));
				return;
			}
		}

		try
		{
			//Now upload the received file
			//Test that file has been sent in request 
			org.apache.commons.fileupload.FileItem uploadFile = (org.apache.commons.fileupload.FileItem) request.getAttribute("file");
			
			if(uploadFile != null)
			{
				String contentType = uploadFile.getContentType();
				uploadFileName=uploadFile.getName();
				
				String extension = "";
				String basename = uploadFileName.trim();
				if (uploadFileName.contains(".")) {
					String[] parts = uploadFileName.split("\\.");
					basename = parts[0];
					if (parts.length > 1) {
						extension = parts[parts.length - 1];
					}
					for (int i = 1; i < parts.length - 1; i++) {
						basename += "." + parts[i];
					}
				}

				if (collection!=null)
				{
					//get the resourceId by using collectionName and uploadFileName
					resourceId = collectionName +"/"+ uploadFileName;
					try{
						//check if resource in collection exists
						ContentHostingService.getResource(resourceId);
						//if user has chosen to overwrite existing resource save the new copy
						if(overwrite != null && overwrite.equals("true")){
							resource = ContentHostingService.editResource(resourceId);
						}
						//if no overwrite then create a new resource in the collection
						else{
							resource = ContentHostingService.addResource(collection.getId(), Validator.escapeResourceName(basename),Validator.escapeResourceName(extension), ResourcesAction.MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
						}
					}
					//if this is a new resource add to the collection.
					catch(IdUnusedException idUnusedException) {
						log.debug("Adding resource {} in collection {}", uploadFileName, collection.getId());
						resource = ContentHostingService.addResource(collection.getId(), Validator.escapeResourceName(basename),Validator.escapeResourceName(extension), ResourcesAction.MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
					}
				}
				else
				{
					//Method getUniqueFileName was added to change external name of uploaded resources if they exist already in the collection, just the same way that their internal id.
					//However, that is not the way Resources tool works. Internal id is changed but external name is the same for every copy of the same file.
					//So I disable this method call, though it can be enabled again if desired.
					
					//String resourceName = getUniqueFileName(uploadFileName, resourceGroup);
					resourceId = resourceGroup + uploadFileName;
					try{
						//check if resource exists
						ContentHostingService.getResource(resourceId);
						//if it does and overwrite is true save the latest copy
						if(overwrite != null && overwrite.equals("true")){
							resource = ContentHostingService.editResource(resourceId);
						}
						// if no overwrite then simply create a new resource
						else{
							resource = ContentHostingService.addResource(resourceGroup, Validator.escapeResourceName(basename), Validator.escapeResourceName(extension), ResourcesAction.MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
						}
					}
					// if new resource then save
					catch(IdUnusedException idUnusedException) {
						log.debug("Adding resource {} in current folder ({})", uploadFileName, resourceGroup);
						resource = ContentHostingService.addResource(resourceGroup, Validator.escapeResourceName(basename), Validator.escapeResourceName(extension), ResourcesAction.MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
					}
				}

				if (resource != null)
				{
					if (contentType!=null) resource.setContentType(contentType);
					
					ResourcePropertiesEdit resourceProps = resource.getPropertiesEdit();
					resourceProps.addProperty(ResourcePropertiesEdit.PROP_DISPLAY_NAME, uploadFileName);
					resource.setContent(uploadFile.getInputStream());
					resource.setContentType(contentType);
					resource.setAvailability(hidden, null, null);
					ContentHostingService.commitResource(resource, NotificationService.NOTI_NONE);
					
					if (collection != null){
						collection.setAvailability(hidden, null, null);
						ContentHostingService.commitCollection(collection);
						log.debug("Collection commited: {}", collection.getId());
					}
				}
				else
				{
					addAlert(state, contentResourceBundle.getFormattedMessage("dragndrop.upload.error",new Object[]{uploadFileName}));
					return;
				}
			}
			else
			{
				addAlert(state, contentResourceBundle.getFormattedMessage("dragndrop.upload.error",new Object[]{uploadFileName}));
				return;
			}
		}
		catch (IdUniquenessException e)
		{
			addAlert(state,contentResourceBundle.getFormattedMessage("dragndrop.duplicated.error",new Object[]{uploadFileName,ResourcesAction.MAXIMUM_ATTEMPTS_FOR_UNIQUENESS}));
			return;
		}
		catch (OverQuotaException e) {
			addAlert(state, rb.getString("alert.over-site-upload-quota"));
			return;
		}
		catch (ServerOverloadException e) {
			addAlert(state,contentResourceBundle.getFormattedMessage("dragndrop.overload.error",new Object[]{uploadFileName}));
			return;
		}
		catch (IdLengthException e) {
			addAlert(state, contentResourceBundle.getFormattedMessage("dragndrop.length.error", e.getReference(), e.getLimit()));
			return;
		}
		catch (Exception e) {
			addAlert(state, contentResourceBundle.getFormattedMessage("dragndrop.upload.error",new Object[]{uploadFileName}));
			log.warn("Drag and drop upload failed: {}", e);
			return;
		}
		
		try
		{
			//Set an i18n OK message for successfully uploaded files. This message is captured by client side and written in the files of Dropzone.
			response.setContentType("text/plain");
			response.setStatus(200);
			PrintWriter pw = response.getWriter();
			pw.write(contentResourceBundle.getString("dragndrop.success.upload"));
			pw.flush();
			pw.close();
		}
		catch (Exception e)
		{
			log.error("Exception writing response in ResourcesHelperAction");
			return;
		}
		
		addFilenameReferenceToList(state, resource.getReference());
		toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
	}

	private ContentCollectionEdit createCollectionIfNotExists(String collectionName)
	{
		//Try to get an existing collection or create it if it does not exist
		//Weird thing: To get existing collections a File.separator must finish the collection's name or a Permissions exception is thrown
		//This does not happen when creating the collection
		
		ContentCollectionEdit cc = null;
		try
		{
			log.debug("Looking for collection {}", collectionName+File.separator);
			if (ContentHostingService.getCollection(collectionName+File.separator)!=null)
			{
				//As Sakai usual behaviour in Resources tool, Collections internal ids do not use non-ASCII chars, so for example folder "Videos" and "Vídeos" are asigned to the same id.
				//So id must be always used. Using collection's name can fail.
				cc=ContentHostingService.editCollection(ContentHostingService.getCollection(collectionName+File.separator).getId());
				log.debug("Editing collection found with id: {}", cc.getId());
			}
		}
		catch (IdUnusedException e)
		{
			log.debug("Collection {} does not exist, proceed to create it.", collectionName+File.separator);

			//It does not exist, so create the folder
			String carpetaName = collectionName.substring(collectionName.lastIndexOf(File.separator)+1,collectionName.length()); //Deepest folder name

			try
			{
				cc = ContentHostingService.addCollection(collectionName);
				ResourcePropertiesEdit m_oPropEditSub = cc.getPropertiesEdit();
				m_oPropEditSub.addProperty(ResourceProperties.PROP_DISPLAY_NAME, carpetaName);

				if (cc!=null)
					ContentHostingService.commitCollection(cc);
			}
			catch (IdUsedException e2)
			{ 
				log.warn("IdUsedException {}", e2.toString());
				return null;
			}
			catch (Exception e2)
			{
				log.warn("Exception on exception: {}", e2.toString());
				return null;
			}
		}
		catch (InUseException e)
		{
			//This exception can be thrown for concurrence issues with multiple parallel uploads.
			//Synchronizing doDragDropUpload method avoids it
			log.warn("InUseException: {}", e.toString());
			log.error(e.getMessage(), e);
			return null;
		}
		catch (Exception e)
		{
			log.warn("Exception: {}", e.toString());
			return null;
		}
		log.debug("Returning collection: {}", cc.getId());
		return cc;
	}
	
	
	public void notifyDragAndDropCompleted(HttpServletRequest request)
	{
		/*
		 * This method uses a new class SiteEmailNotificationDragAndDrop which extends SiteEmailNotification 
		 * and uses some modified code of SiteEmailNotificationContent and DropboxNotification classes.
		 * Current Content notifications are managed with information of every uploaded content entity. However, this does not work with a group of entities uploaded through D&D.
		 * Resources D&D notifications can be decided using containing folder as reference instead each uploaded file.
		 * Dropbox D&D notifications are trickier. When a maintain/Instructor sends one notification to access/student, dropbox containing folder can be used as well.
		 * But when a student/access sends one notification to maintains/Instructors, the folder owner is compared with the user who last modificated the uploaded item.
		 * This is not valid in D&D, so I have changed it and folder owner is compared with current user in order to send notifications.
		 */
		JetspeedRunData rundata = (JetspeedRunData) request.getAttribute(ATTR_RUNDATA);
		ParameterParser params = rundata.getParameters();
		
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		ListItem item = new ListItem(pipe.getContentEntity());
		
		int notificationPriority = determineNotificationPriority(params, item.isDropbox());
		
		SessionState state = getState(request);
		
		try
		{
			Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
			
			NotificationEdit ne = notificationService.addTransientNotification();
			
			String eventResource;
			if (item.isDropbox)
			{
				eventResource=org.sakaiproject.content.api.ContentHostingService.EVENT_RESOURCE_AVAILABLE;
				ne.setResourceFilter(ContentHostingService.REFERENCE_ROOT+org.sakaiproject.content.api.ContentHostingService.COLLECTION_DROPBOX);
			}
			else
			{
				eventResource=org.sakaiproject.content.api.ContentHostingService.EVENT_RESOURCE_ADD;
				ne.setResourceFilter(ContentHostingService.REFERENCE_ROOT+org.sakaiproject.content.api.ContentHostingService.COLLECTION_SITE);
			}
			
			ne.setFunction(eventResource);
			SiteEmailNotificationDragAndDrop sendnd = new SiteEmailNotificationDragAndDrop(site.getId());
			sendnd.setDropboxFolder(item.isDropbox());
			sendnd.setFileList((ArrayList<String>)(state.getAttribute(DRAGNDROP_FILENAME_REFERENCE_LIST)));
			// Notify when files were successfully added
			if (sendnd.getFileList() != null && !sendnd.getFileList().isEmpty()) {			
				ne.setAction(sendnd);
				sendnd.notify(ne,eventTrackingService.newEvent(eventResource, ContentHostingService.REFERENCE_ROOT+item.getId(), true, notificationPriority));			
			}
			state.setAttribute(DRAGNDROP_FILENAME_REFERENCE_LIST, null);
			sendnd.setFileList(null);
		} catch (IdUnusedException e) {
			log.warn("Somehow we couldn't find the site.", e);
		}
	}

	private void addFilenameReferenceToList(SessionState state, String ref)
	{
		ArrayList<String> soFar = (ArrayList<String>) state.getAttribute(DRAGNDROP_FILENAME_REFERENCE_LIST);
		if (soFar == null) soFar = new ArrayList<String>();			
		soFar.add(ref);
		state.setAttribute(DRAGNDROP_FILENAME_REFERENCE_LIST, soFar);

	} // addAlert
	
/*
	private String getUniqueFileName(String uploadFileName, String resourceGroup) throws org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.TypeException
	{
		String resourceId = "";
		boolean isNameUnique = false;
		String fileName = uploadFileName;
		int attempt = 0;
		while (!isNameUnique)
		{
			try
			{
				resourceId = resourceGroup + fileName;
				ContentResource tempEdit = ContentHostingService.getResource(resourceId);
				if(tempEdit != null)
				{
					attempt++;
					StringBuffer fileNameBuffer = new StringBuffer();
					if(attempt > 1)
					{
						fileNameBuffer.append(fileName.substring(0, fileName.lastIndexOf("-")));
					}
					else
					{
						fileNameBuffer.append(fileName.substring(0, fileName.lastIndexOf(".")));
					}
					fileNameBuffer.append("-");
					fileNameBuffer.append(attempt);
					fileNameBuffer.append(fileName.substring(fileName.lastIndexOf("."), fileName.length()));
					fileName = fileNameBuffer.toString();
				}
			}
			catch (IdUnusedException e)
			{
				isNameUnique = true;
			}
		}
		return fileName;
	}
*/

}
