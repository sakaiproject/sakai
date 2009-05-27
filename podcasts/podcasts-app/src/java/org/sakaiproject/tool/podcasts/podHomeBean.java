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
package org.sakaiproject.tool.podcasts;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.podcasts.PodcastService;
import org.sakaiproject.api.app.podcasts.PodcastPermissionsService;
import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;

public class podHomeBean {
	// Message Bundle handles
	private static final String QUOTA_ALERT = "quota_alert";
	private static final String LENGTH_ALERT = "length_alert";
	private static final String PERMISSION_ALERT = "permission_alert";
	private static final String INTERNAL_ERROR_ALERT = "internal_error_alert";
	private static final String ID_UNUSED_ALERT = "id_unused_alert";
	private static final String ID_INVALID_ALERT = "id_invalid_alert";
	private static final String IO_ALERT = "io_alert";
	private static final String ID_USED_ALERT = "id_used_alert";

	// Patterns for Date and Number formatting
	private static final String PUBLISH_DATE_FORMAT = "publish_date_format";
	private static final String DATE_PICKER_FORMAT = "date_picker_format";
	private static final String DATE_BY_HAND_FORMAT = "date_by_hand_format";
	private static final String INTERNAL_DATE_FORMAT = "internal_date_format";

	private static final String LAST_MODIFIED_TIME_FORMAT = "hh:mm a z";
	private static final String LAST_MODIFIED_DATE_FORMAT = "MM/dd/yyyy";
	
	// error handling variables
	private boolean displayNoFileErrMsg = false;
	private boolean displayNoDateErrMsg = false;
	private boolean displayNoTitleErrMsg = false;
	private boolean displayInvalidDateErrMsg = false;

	/**
	 * Bean to store information about a single podcast.
	 * Used by main page to display and to store which
	 * podcast is to be Revised or Deleted.
	 *  
	 * @author josephrodriguez
	 */
	public class DecoratedPodcastBean {

		private String resourceId;
		private String filename;
		private long fileSize;
		private String displayDate;
		private String displayDateRevise;
		private String title;
		private String description;
		private String size;
		private String type;
		private String postedTime;
		private String postedDate;
		private String author;
		private String fileURL;
		private String newWindow;
		private String fileContentType;
		private String styleClass;
		private boolean hidden;

		public DecoratedPodcastBean() {
			styleClass = "";
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String decsription) {
			this.description = decsription;
		}

		public String getDisplayDate() {
			return displayDate;
		}

		/**
		 * @param displayDateRevise The displayDateRevise to set.
		 */
		public void setDisplayDateRevise(String displayDateRevise) {
			this.displayDateRevise = displayDateRevise;
		}

		/** Returns the revised display date for this podcast **/
		public String getDisplayDateRevise() {
			String dispDate = null;
			
			if (displayDateRevise == null) {
				dispDate = displayDate;
			}
			else {
				dispDate = displayDateRevise;
			}
				
			SimpleDateFormat formatter = new SimpleDateFormat(getErrorMessageString(DATE_BY_HAND_FORMAT));
			formatter.setTimeZone(TimeService.getLocalTimeZone());
			
			try {
				Date tempDate = convertDateString(dispDate,
						getErrorMessageString(PUBLISH_DATE_FORMAT));
				
				return formatter.format(tempDate);
			
			} catch (ParseException e) {
				// since revising, only log error if malformed date and not just blank
				if (! "".equals(dispDate)) {
					LOG.error("ParseException while rendering Revise Podcast page. ", e);
				}
			}
			
			return dispDate;

		}
		public void setDisplayDate(String displayDate) {
			this.displayDate = displayDate;
		}

		public String getFilename() {
			return filename;
		}

		public void setFilename(String filename) {
			this.filename = filename;
		}

		/** Returns the size (as a formatted String) for this podcast's file **/
		public String getSize() {
			return size;
		}

		/** Sets the size (as a formatted String) for this podcast's file **/
		public void setSize(String size) {
			this.size = size;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		/** Returns this podcast's content MIME type ***/
		public String getType() {
			return type;
		}

		/** Sets this podcast's content MIME type ***/
		public void setType(String type) {
			this.type = type;
		}

		public String getPostedTime() {
			return postedTime;
		}

		public void setPostedTime(String postedTime) {
			this.postedTime = postedTime;
		}

		public String getPostedDate() {
			return postedDate;
		}

		public void setPostedDate(String postedDate) {
			this.postedDate = postedDate;
		}

		public String getAuthor() {
			return author;
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		public String getResourceId() {
			return resourceId;
		}

		public void setResourceId(String resourceId) {
			this.resourceId = resourceId;
		}

		public long getFileSize() {
			return fileSize;
		}

		public void setFileSize(long fileSize) {
			this.fileSize = fileSize;
		}

		/**
		 * Returns the file URL for the podcast. If errors, return empty
		 * string.
		 */
		public String getFileURL() {
			try {
//				return podcastService.getPodcastFileURL(resourceId);
				/* SAK-13059: added url escaping filename due to files with spaces
				 *   and other non-friendly url characters need to be but if entire
				 *   url returned by podcastService is escaped, it is interpreted as
				 *   a relative url and not an absolute one.
				 */ 
				String fullUrl = podcastService.getPodcastFileURL(resourceId);
				String filenameEscaped = Validator.escapeUrl(filename);
				
				return fullUrl.substring(0, fullUrl.lastIndexOf(Entity.SEPARATOR) + 1) + filenameEscaped;
			} 
			catch (PermissionException e) {
				LOG.info("PermissionException getting file URL for "
						+ resourceId + "while displaying podcast file for site " + podcastService.getSiteId(), e);
				setErrorMessage(PERMISSION_ALERT);

			} 
			catch (IdUnusedException e) {
				LOG.info("IdUnusedException getting file URL for " + resourceId
						+ " while displaying podcast file for site " + podcastService.getSiteId(), e);
				setErrorMessage(ID_UNUSED_ALERT);

			}

			return "";
		}

		public void setFileURL(String fileURL) {
			this.fileURL = fileURL;
		}

		/**
		 * Returns a value to determine if file should be opened in same
		 * window or a new one. _self means open in same window, _blank
		 * means open in a new window. If this resource is not a file,
		 * it will return an empty string.
		 * 
		 * @return String
		 * 			Returns either '_self' or '_blank' ('' if not a file)
		 */
		public String getNewWindow() {
			return Validator.getResourceTarget(fileContentType);
		}

		public void setNewWindow(String newWindow) {
			this.newWindow = newWindow;
		}

		public String getFileContentType() {
			return fileContentType;
		}

		public void setFileContentType(String fileContentType) {
			this.fileContentType = fileContentType;
		}

		public String getStyleClass() {
			return styleClass;
		}

		public void setStyleClass(String styleClass) {
			this.styleClass = styleClass;
		}

		public boolean isHidden() {
			return hidden;
		}

		public void setHidden(boolean hidden) {
			this.hidden = hidden;
		}

	} // end of DecoratedPodcastBean

// ====================== End of DecoratedPodcastBean ====================== // 
	
	// podHomeBean constants
	private static final String NO_RESOURCES_ERR_MSG = "no_resource_alert";
	private static final String NO_RESOURCES_ERR_MSG2 = "no_resource_alert2";
	private static final String RESOURCEID = "resourceId";
	private static final String FEED_URL_MIDDLE = "podcasts/site/";
	private static final String MB = "MB";
	private static final String BYTES = "Bytes";
	
	private static final String MB_NUMBER_FORMAT = "#.#";
	private static final String BYTE_NUMBER_FORMAT = "#,###";

	// Permissions prefix
	private final String CONTENT = "content.";
	
	// For date String conversion
	private static final Map monStrings; 
	
	static {
		monStrings = new HashMap();
		
		monStrings.put("Jan", "January");
		monStrings.put("Feb", "February");
		monStrings.put("Mar", "March");
		monStrings.put("Apr", "April");
		monStrings.put("Jun", "June");
		monStrings.put("Jul", "July");
		monStrings.put("Aug", "August");
		monStrings.put("Sep", "September");
		monStrings.put("Oct", "October");
		monStrings.put("Nov", "November");
		monStrings.put("Dec", "December");
	}

	// configurable toolId for Resources tool check
	private String RESOURCE_TOOL_ID = ServerConfigurationService.getString("podcasts.toolid", "sakai.resources");

	/** Used to pull message bundle */
//        String bundle = FacesContext.getCurrentInstance().getApplication().getMessageBundle();
 //       toolBundle = new ResourceLoader(bundle);
	private ResourceLoader rb;

	// inject the services needed
	private PodcastService podcastService;
	private PodcastPermissionsService podcastPermissionsService;

	private Log LOG = LogFactory.getLog(podHomeBean.class);

	// variables to hold miscellanous information
	private List contents;
	private String URL;
	private DecoratedPodcastBean selectedPodcast;

	// used by podAdd.jsp for adding a podcast
	private String filename = "";
	private String date = "";
	private String title;
	private String description;
	private String email;
	private long fileSize = 0;
	private String fileContentType;
	private BufferedInputStream fileAsStream;

	// FUTURE: Will be used to implement Email notifications
	private SelectItem[] emailItems = {
			new SelectItem("none", "None - No notification"),
			new SelectItem("low", "Low - Only participants who have opted in"),
			new SelectItem("high", "High - All participants") };

	public podHomeBean() {
	}

	/**
	 * Determines if Resource tool part of the site. Needed to store podcasts.
	 */
	public boolean getResourceToolExists() {
		boolean resourceToolExists = false;

		try {
			//
			// Get a list of tool ids and see if RESOURCE_TOOL_ID is in the returned Collection			
			LOG.debug("Checking for presence of Sakai Resources tool using RESOURCE_TOOL_ID = " + RESOURCE_TOOL_ID);
			Site thisSite = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());

			Collection toolsInSite = thisSite.getTools(RESOURCE_TOOL_ID);
			
			if (!toolsInSite.isEmpty()) {
				resourceToolExists = true;
			}
		} 
		catch (IdUnusedException e) {
			LOG.error("No Site found while trying to check if site has Resources tool.", e);
			
			// Only want to display this message if they are instructors or administrators
			// so if student say it exists
			if (getCanUpdateSite()) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage("Alert: " + getErrorMessageString(NO_RESOURCES_ERR_MSG) 
												   + ToolManager.getTool(RESOURCE_TOOL_ID).getTitle()
												   + " " + getErrorMessageString(NO_RESOURCES_ERR_MSG2)));
			}
		}

		// Since multiple checks for this, only want to set the message the first time
		if (!resourceToolExists
				&& (!FacesContext.getCurrentInstance().getMessages().hasNext())) {
			
			if (getCanUpdateSite()) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage("Alert: " + getErrorMessageString(NO_RESOURCES_ERR_MSG) 
												   + ToolManager.getTool(RESOURCE_TOOL_ID).getTitle()
												   + " " + getErrorMessageString(NO_RESOURCES_ERR_MSG2)));
			}
		}

		return resourceToolExists;
	}

	/**
	 * Passes an error message to the Spring framework to display on page.
	 * 
	 * @param alertMsg
	 *            The key to get the message from the message bundle
	 */
	private void setErrorMessage(String alertMsg) {
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage("Alert: " + getErrorMessageString(alertMsg)));
	}

	/**
	 * Determines if the podcast folder exists. If it does not, it will attempt
	 * to create it.
	 * 
	 * @return boolean
	 * 				TRUE if folder exists, FALSE otherwise.
	 */
	public boolean getPodcastFolderExists() {
		boolean podcastFolderExists = false;

		if (getResourceToolExists()) {
			// we know resources tool exists, but need to know if podcast folder
			// does
			try {
				podcastFolderExists = podcastService.checkPodcastFolder();

			}
			catch (InUseException e) {
				LOG.info("InUseException while attempting to determine if podcast folder exists."
								+ " for site " + podcastService.getSiteId(), e);
				setErrorMessage(INTERNAL_ERROR_ALERT);

			}
			catch (PermissionException e) {
				LOG.warn("PermissionException while attempting to determine if podcast folder exists."
							+ " for site " + podcastService.getSiteId(), e);
				setErrorMessage(PERMISSION_ALERT);

			}
		} 
		else if (!getCanUpdateSite()) {
			// Resources tool does not exist but for students
			// say true to get "There are no podcasts..." message instead of
			// error
			podcastFolderExists = true;
		}

		return podcastFolderExists;
	}

	/**
	 * Returns the URL to point your podcatcher to in order to grab the feed.
	 * 
	 * @return String 
	 * 				The feed URL.
	 */
	public String getURL() {
		URL = ServerConfigurationService.getServerUrl() + Entity.SEPARATOR
				+ FEED_URL_MIDDLE + podcastService.getSiteId();
		return URL;
	}
	
	/**
	 * Returns String to be passed to permissions page.
	 */
	private String getPermissionsMessage() {
		return Validator.escapeUrl("Set permissions for Podcasts folder in worksite " +
		   		 SiteService.getSiteDisplay(podcastService.getSiteId()));
	}

	 /**
	  * Constructs call to permissions helper and redirects to it to display
	  * Podcasts folder permissions page.
	  */ 
	 public String processPermissions() {
			ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();

			try {
				String url = "sakai.permissions.helper.helper/tool?session." +
								PermissionsHelper.DESCRIPTION + "=" + getPermissionsMessage() +
								"&session." + PermissionsHelper.TARGET_REF + "=" + 
								podcastService.getPodcastsFolderRef() + 
								"&session." + PermissionsHelper.PREFIX + "=" + CONTENT +
								"&session." + PermissionsHelper.ROLES_REF + "=" +
								"/site/" + podcastService.getSiteId();

		        context.redirect(url);
		    }
			catch (IOException e) {
		            throw new RuntimeException("Failed to redirect to helper", e);
		    }
		
			return null;
		}

	/**
	 * Used to inject the podcast service into this bean.
	 * 
	 * @param podcastService
	 * 				The podcast service this bean needs
	 */
	public void setPodcastService(PodcastService podcastService) {
		this.podcastService = podcastService;
	}

	public void setPodcastPermissionsService(PodcastPermissionsService podcastPermissionsService) {
		this.podcastPermissionsService = podcastPermissionsService;
	}

	/**
	 * Returns whether podcast should be 'hidden' in UI. Conditions:
	 * 	Hidden property set
	 *  Release date is in future
	 *  Retract date is in past
	 *  
	 * @param podcastResource
	 * 			The actual podcast to check
	 * @param tempDate
	 * 			Release date (if exists) or display date (older version)
	 */
	private boolean hiddenInUI(ContentResource podcastResource, Date tempDate) {
		return podcastPermissionsService.isResourceHidden(podcastResource, tempDate);
	}

	/**
	 * Returns whether the Podcasts folder should be considered HIDDEN in UI
	 */
	public boolean isPodcastFolderHidden() {
		boolean hidden;
		
		try {
			return podcastService.isPodcastFolderHidden(podcastService.getSiteId());
		}
		catch (Exception e) {
			// if problems accessing, then should be hidden, so return false
			return true;
		}
	}
	
	/**
	 * Receives a particular podcast and packages it as a DecoratedPodcastBean
	 * 
	 * @param podcastProperties
	 *            Contains the ResourceProperties object of a podcast resource
	 * @param resourceId
	 *            The resource ID for the podcast
	 *            
	 * @return DecoratedPodcastBean 
	 * 			The packaged podcast or null and exception if problems
	 * 
	 * @throws EntityPropertyNotDefinedException
	 * 			The property wanted was not found
	 * @throws EntityPropertyTypeException
	 * 			The property (Date/Time) was not a valid one
	 */
	public DecoratedPodcastBean getAPodcast(ContentResource podcastResource, boolean folderHidden)
			throws EntityPropertyNotDefinedException,
			EntityPropertyTypeException {

		ResourceProperties podcastProperties = podcastResource.getProperties();

		DecoratedPodcastBean podcastInfo = null;

		// first check if hidden.
		// if instructor or has hidden property, set hidden property of decorated bean
		// if not, return null since user cannot see
		Date tempDate = null;
		final SimpleDateFormat formatter = new SimpleDateFormat(getErrorMessageString(PUBLISH_DATE_FORMAT));
		formatter.setTimeZone(TimeService.getLocalTimeZone());

		// get release/publish date - else part needed for podcasts created before release/retract dates
		// feature implemented
		if (podcastResource.getReleaseDate() == null) {
			tempDate = podcastService.getGMTdate(podcastProperties
													.getTimeProperty(PodcastService.DISPLAY_DATE).getTime());
		} 
		else {
			tempDate = new Date(podcastResource.getReleaseDate().getTime());
		}

		// store result of hidden property OR after retract date OR before release date
		final boolean uiHidden = folderHidden || hiddenInUI(podcastResource, tempDate);
		
		if (! uiHidden || getHasHidden()) {
			podcastInfo = new DecoratedPodcastBean();

			podcastInfo.setDisplayDate(formatter.format(tempDate));

			// store resourceId
			podcastInfo.setResourceId(podcastResource.getId());

			// store Title and Description
			podcastInfo.setTitle(podcastProperties
								.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME));
			podcastInfo.setDescription(podcastProperties
								.getPropertyFormatted(ResourceProperties.PROP_DESCRIPTION));

			podcastInfo.setHidden(uiHidden);

			// if podcast should be hidden, set style class to 'inactive'
			if (uiHidden) {
				podcastInfo.setStyleClass("inactive");
			}
			
			String filename = null;
			try {
				String url = podcastService.getPodcastFileURL(podcastResource.getId());
				filename = url.substring(url.lastIndexOf("/")+1);
			}
			catch (PermissionException e) {
				LOG.warn("PermissionException getting podcast with id " + podcastResource.getId() + " while constructing DecoratedPodcastBean for site "
						+ podcastService.getSiteId() + ". " + e.getMessage(), e);
			}
			catch (IdUnusedException e) {
				LOG.warn("IdUnusedException getting podcast with id " + podcastResource.getId() + " while constructing DecoratedPodcastBean for site "
						+ podcastService.getSiteId() + ". " + e.getMessage(), e);
			}
			
			// if user puts URL instead of file, this is result of retrieving filename
			if (filename == null)
				return null;

			podcastInfo.setFilename(filename);

			// get content type
			podcastInfo.setFileContentType(podcastProperties.getProperty(ResourceProperties.PROP_CONTENT_TYPE));

			// store actual and formatted file size
			// determine whether to display filesize as bytes or MB
			final long size = Long.parseLong(podcastProperties.getProperty(ResourceProperties.PROP_CONTENT_LENGTH));
			podcastInfo.setFileSize(size);

			final double sizeMB = size / (1024.0 * 1024.0);
			final DecimalFormat df = new DecimalFormat(MB_NUMBER_FORMAT);
			String sizeString;
			if (sizeMB > 0.3) {
				sizeString = df.format(sizeMB) + MB;
			} 
			else {
				df.applyPattern(BYTE_NUMBER_FORMAT);
				sizeString = "" + df.format(size) + " " + BYTES;
			}
			podcastInfo.setSize(sizeString);

			final String extn = Validator.getFileExtension(filename);
			if (extn != "") {
				podcastInfo.setType(Validator.getFileExtension(filename).toUpperCase());
			}
			else {
				podcastInfo.setType("UNK");
			}

			// get and format last modified time
			formatter.applyPattern(LAST_MODIFIED_TIME_FORMAT);

			tempDate = new Date(podcastProperties.getTimeProperty(ResourceProperties.PROP_MODIFIED_DATE).getTime());

			podcastInfo.setPostedTime(formatter.format(tempDate));

			// get and format last modified date
			formatter.applyPattern(LAST_MODIFIED_DATE_FORMAT);

			tempDate = new Date(podcastProperties.getTimeProperty(ResourceProperties.PROP_MODIFIED_DATE).getTime());

			podcastInfo.setPostedDate(formatter.format(tempDate));

			// get author
			podcastInfo.setAuthor(podcastProperties.getPropertyFormatted(ResourceProperties.PROP_CREATOR));
		}

		return podcastInfo;
	}

	/**
	 * Construct a List of DecoratedPodcastBeans for display on main page
	 * 
	 * @return List
	 * 			List of DecoratedPodcastBeans that are the podcasts
	 */
	public List getContents() {
		boolean folderHidden = false;
		
		try {
			// if entire folder set to HIDDEN and don't have site.upd or hidden perm
			// return empty list since cannot access
			// if have proper perms, set switch so all podcasts greyed out in UI
			if (podcastService.isPodcastFolderHidden(podcastService.getSiteId())) {
				if (! podcastPermissionsService.hasPerm(PodcastPermissionsService.HIDDEN_PERMISSIONS,
															podcastService.retrievePodcastFolderId(
															podcastService.getSiteId()))) {
					return new ArrayList();
				}
				else {
					folderHidden = true;
				}
			}

			contents = podcastService.getPodcasts();

			// if cannot update site (ie, student) only display published podcasts
			if (! podcastPermissionsService.canUpdateSite(podcastService.getSiteId())) {
				contents = podcastService.filterPodcasts(contents);
			}

		} 
		catch (PermissionException e) {
			LOG.warn("PermissionException getting podcasts for display in site "
						+ podcastService.getSiteId() + ". " + e.getMessage(), e);
			setErrorMessage(PERMISSION_ALERT);

		} 
		catch (InUseException e) {
			LOG.warn("InUseException while getting podcasts for display"
						+ podcastService.getSiteId() + ". " + e.getMessage(), e);
			setErrorMessage(INTERNAL_ERROR_ALERT);

		} 
		catch (IdInvalidException e) {
			LOG.error("IdInvalidException while getting podcasts for display "
						+ podcastService.getSiteId() + ". " + e.getMessage(), e);
			setErrorMessage(ID_INVALID_ALERT);

		} 
		catch (InconsistentException e) {
			LOG.error("InconsistentException while getting podcasts for display "
						+ podcastService.getSiteId() + ". " + e.getMessage(), e);
			setErrorMessage(INTERNAL_ERROR_ALERT);
			
			return null;

		} 
		catch (IdUsedException e) {
			LOG.warn("IdUsedException while gettting podcasts for display "
						+ podcastService.getSiteId() + ". " + e.getMessage(), e);
			setErrorMessage(ID_UNUSED_ALERT);

		}
		catch (IdUnusedException e) {
			LOG.warn("IdUnusedException while determining if Podcasts folder has HIDDEN permission set" 
						+ " for site " + podcastService.getSiteId() + ". " + e.getMessage(), e);
		}

		// create local List of DecoratedBeans
		ArrayList decoratedPodcasts = new ArrayList();

		if (contents != null) {
			Iterator podcastIter = contents.iterator();

			// for each bean
			while (podcastIter.hasNext()) {
				try {
					// get its properties from ContentHosting
					final ContentResource podcastResource = (ContentResource) podcastIter.next();
					ResourceProperties podcastProperties = podcastResource.getProperties();

					// Create a new decorated bean to store the info
					DecoratedPodcastBean podcastInfo = getAPodcast(podcastResource, folderHidden);

					// add it to the List to send to the page. if URL, will return null so skip it
					if (podcastInfo != null)
						decoratedPodcasts.add(podcastInfo);

				} 
				catch (EntityPropertyNotDefinedException e) {
					LOG.error("EntityPropertyNotDefinedException while creating DecoratedPodcastBean "
									+ " for site "+ podcastService.getSiteId() + ". SKIPPING..." + e.getMessage(), e);
				}
				catch (EntityPropertyTypeException e) {
					LOG.error("EntityPropertyTypeException while creating DecoratedPodcastBean "
									+ " for site "+ podcastService.getSiteId() + ". SKIPPING..." + e.getMessage(), e);
				}
			}
		}

		return decoratedPodcasts;
	}

	/**
	 * Resources/podcasts exists, but are there any actual podcasts
	 * 
	 * @return boolean
	 * 				TRUE if there are podcasts, FALSE otherwise
	 */
	public boolean getActPodcastsExist() {
		boolean actPodcastsExist = false;

		// if student on site that has Podcasts but no Resources, want "There are no..."
		if (!getResourceToolExists() && ! podcastPermissionsService.canUpdateSite()) {
			return false;
		}
		
		if (!getPodcastFolderExists()) {
			// if for some reason there is not a podcast folder
			// for example, was renamed in Resources
			actPodcastsExist = false;

		} 
		else {
			// ask the service if there is anything in the podcast folder
			try {
				actPodcastsExist = podcastService.checkForActualPodcasts();
				
			} 
			catch (PermissionException e) {
				LOG.warn("PermissionException while determining if there are files in the podcast folder "
								+ " for site " + podcastService.getSiteId() + ". " + e.getMessage(), e);
				setErrorMessage(PERMISSION_ALERT);
			}
		}

		return actPodcastsExist;
	}

	/**
	 * To set the selectedPodcast DecoratedPodcastBean when Revision and
	 * Deletion links are clicked (possibly Download also)
	 * 
	 * @param e
	 *            ActionEvent object generated by clicking on a link
	 */
	public void podMainListener(ActionEvent e) {
		FacesContext context = FacesContext.getCurrentInstance();
		Map requestParams = context.getExternalContext()
				.getRequestParameterMap();
		final String resourceId = (String) requestParams.get(RESOURCEID);

		setPodcastSelected(resourceId);
	}

	/**
	 * Does the actual filling up of the selectedPodcast bean
	 * 
	 * @param resourceId
	 *            Resource ID for the podcast whose link was selected
	 */
	public void setPodcastSelected(String resourceId) {
		Iterator podcastIter = contents.iterator();

		// for each bean
		while (podcastIter.hasNext()) {
			try {

				// get its properties from ContentHosting
				ContentResource podcastResource = (ContentResource) podcastIter.next();

				if (podcastResource.getId().equals(resourceId)) {
					selectedPodcast = getAPodcast(podcastResource, 
										podcastService.isPodcastFolderHidden(podcastService.getSiteId()));
					break; // found and filled, get out of loop
				}

			} 
			catch (EntityPropertyNotDefinedException e) {
				LOG.error("EntityPropertyNotDefinedException while attempting to fill selectedPodcast property "
								+ " for site " + podcastService.getSiteId() + ". SKIPPING..." + e.getMessage(), e);
				throw new Error(e);

			} 
			catch (EntityPropertyTypeException e) {
				LOG.error("EntityPropertyTypeException while attempting to fill selectedPodcast property "
								+ " for site " + podcastService.getSiteId() + ". SKIPPING..." + e.getMessage(), e);
				throw new Error(e);

			}
			catch (IdUnusedException e) {
				LOG.error("IdUnusedException while attempting to determine if Podcasts folder is hidden for site " 
							+ podcastService.getSiteId() + ". SKIPPING..." + e.getMessage(), e);
			}
			catch (PermissionException e) {
				LOG.error("PermissionException while attempting to determine if Podcasts folder is hidden for site " 
						+ podcastService.getSiteId() + ". SKIPPING..." + e.getMessage(),e );
			}
		}
	}

	/**
	 * Returns the current DecoratedPodcastBean set as selectedPodcast.
	 */
	public DecoratedPodcastBean getSelectedPodcast() {
		return selectedPodcast;
	}

	/**
	 * Use to set the selectedPodcast object.
	 */
	public void setSelectedPodcast(DecoratedPodcastBean selectedPodcast) {
		this.selectedPodcast = selectedPodcast;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * FUTURE: To display the possible email notification levels
	 * 
	 * @return SelectItem []
	 * 				List of possible email notification levels
	 */
	public SelectItem[] getEmailItems() {
		return emailItems;
	}

	/**
	 * FUTURE: Returns the notification level set by the user
	 * 
	 * @return String
	 * 				Returns the current notification level
	 */
	public String getemail() {
		return email;
	}

	/**
	 * FUTURE: Sets the notification level
	 * 
	 * @param email
	 * 			String representing the notification level
	 */
	public void setemail(String email) {
		this.email = email;
	}

	/**
	 * Returns boolean if user can update podcasts. Used to display modification
	 * options on main page.
	 */
	public boolean getCanUpdateSite() {
		return podcastPermissionsService.canUpdateSite();
	}
	
	public boolean getHasReadPerm() {
		if (! getResourceToolExists()){
			return false;
		}
		else {
			try {
				return podcastPermissionsService.hasPerm(PodcastPermissionsService.READ_PERMISSIONS,
												podcastService.retrievePodcastFolderId(
														podcastService.getSiteId()));
			}
			catch (PermissionException e) {
				// problem getting podcast folder id, so return false
			}
			return false;
		}
	}

	public boolean getHasNewPerm() {
		if (! getResourceToolExists()){
			return false;
		}
		else {
			try {
				return podcastPermissionsService.hasPerm(PodcastPermissionsService.NEW_PERMISSIONS,
												podcastService.retrievePodcastFolderId(
														podcastService.getSiteId()));
			}
			catch (PermissionException e) {
				// problem getting podcast folder id, so return false
			}
			return false;
		}
	}
	
	public boolean getHasReviseAnyPerm() {
		if (! getResourceToolExists()){
			return false;
		}
		else {
			try {
				return podcastPermissionsService.hasPerm(PodcastPermissionsService.REVISE_ANY_PERMISSIONS,
												podcastService.retrievePodcastFolderId(
														podcastService.getSiteId()));
			}
			catch (PermissionException e) {
				// problem getting podcast folder id, so return false
			}
			return false;
		}
	}
	
	public boolean getHasReviseOwnPerm() {
		try {
			return podcastPermissionsService.hasPerm(PodcastPermissionsService.REVISE_OWN_PERMISSIONS,
											podcastService.retrievePodcastFolderId(
													podcastService.getSiteId()));
		}
		catch (PermissionException e) {
			// problem getting podcast folder id, so return false
		}
		return false;
	}
	
	public boolean getHasDelAnyPerm() {
		try {
			return podcastPermissionsService.hasPerm(PodcastPermissionsService.DELETE_ANY_PERMISSIONS,
											podcastService.retrievePodcastFolderId(
													podcastService.getSiteId()));
		}
		catch (PermissionException e) {
			// problem getting podcast folder id, so return false
		}
		return false;
	}
	
	public boolean getHasDelOwnPerm() {
		try {
			return podcastPermissionsService.hasPerm(PodcastPermissionsService.DELETE_OWN_PERMISSIONS,
											podcastService.retrievePodcastFolderId(
													podcastService.getSiteId()));
		}
		catch (PermissionException e) {
			// problem getting podcast folder id, so return false
		}
		return false;
	}

	/**
	 * Returns TRUE if user has ALLGROUPS permission (or site.upd which supercedes),
	 * FALSE if not
	 */
	public boolean getHasAllGroups() {
		try {
			return podcastPermissionsService.hasPerm(PodcastPermissionsService.ALL_GROUPS_PERMISSIONS,
											podcastService.retrievePodcastFolderId(
													podcastService.getSiteId()));
		}
		catch (PermissionException e) {
			// problem getting podcast folder id, so return false
		}
		return false;
	}
	
	public boolean getHasHidden() {
		try {
			return podcastPermissionsService.hasPerm(PodcastPermissionsService.HIDDEN_PERMISSIONS,
											podcastService.retrievePodcastFolderId(
													podcastService.getSiteId()));
		}
		catch (PermissionException e) {
			// problem getting podcast folder id, so return false
		}
		return false;
	}
	
	public String getUserName() {
		return podcastService.getUserName();
	}
	
	public boolean isCanAccessFolder() {
		return getCanUpdateSite()
				|| ! isPodcastFolderHidden()
				|| (isPodcastFolderHidden() && getHasHidden());
//				|| (isPodcastFolderGroupRestricted() && userInGroupRestrictedTo());
	}

	/**
	 * Creates a BufferedInputStream to get ready to upload file selected. Used
	 * by Add Podcast and Revise Podcast pages.
	 * 
	 * @param event
	 *            ValueChangeEvent object generated by selecting a file to
	 *            upload.
	 *            
	 * @throws AbortProcessingException
	 * 			Internal processing error attempting to set up BufferedInputStream
	 */
	public void processFileUpload(ValueChangeEvent event)
			throws AbortProcessingException {
		UIComponent component = event.getComponent();

		Object newValue = event.getNewValue();
		Object oldValue = event.getOldValue();
		PhaseId phaseId = event.getPhaseId();
		Object source = event.getSource();
		System.out.println("processFileUpload() event: " + event
				+ " component: " + component + " newValue: " + newValue
				+ " oldValue: " + oldValue + " phaseId: " + phaseId
				+ " source: " + source);

		if (newValue instanceof String)
			return;
		if (newValue == null)
			return;

		FileItem item = (FileItem) event.getNewValue();
		String fieldName = item.getFieldName();
		filename = Validator.getFileName(item.getName());
		fileSize = item.getSize();
		fileContentType = item.getContentType();
		System.out.println("processFileUpload(): item: " + item
				+ " fieldname: " + fieldName + " filename: " + filename
				+ " length: " + fileSize);

		// Read the file as a stream (may be more memory-efficient)
		try {
			fileAsStream = new BufferedInputStream(item.getInputStream());
			
		} 
		catch (IOException e) {
			LOG.warn("IOException while attempting to set BufferedInputStream to upload "
							+ filename + " from site " + podcastService.getSiteId() + ". "
									 + e.getMessage(), e);
			setErrorMessage(INTERNAL_ERROR_ALERT);

		}

	}

	/**
	 * Converts the date string input using the FORMAT_STRING given.
	 * 
	 * @param inputDate
	 *            The string that needs to be converted.
	 * @param FORMAT_STRING
	 *            The format the data needs to conform to
	 * 
	 * @return Date
	 * 			The Date object containing the date passed in or null if invalid.
	 * 
	 * @throws ParseException
	 * 			If not a valid date compared to FORMAT_STRING given
	 */
	private Date convertDateString(final String inputDate,
			final String FORMAT_STRING) throws ParseException {

		Date convertedDate = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat(FORMAT_STRING);
		dateFormat.setTimeZone(TimeService.getLocalTimeZone());

		convertedDate = dateFormat.parse(inputDate);

		return convertedDate;
	}

	/**
	 * Performs the actual adding of a podcast. Calls PodcastService to actually
	 * add the podcast.
	 * 
	 * @return String Sent to return to main page.
	 */
	public String processAdd() {

		// if OK, need byte array to store contents of file
		byte[] fileContents = new byte[(int) fileSize];

		// If problems, stay on this page
		String whereToGo = "podcastAdd";

		// validate the input
		if (OKtoAdd()) {
			try {
				fileAsStream.read(fileContents);
				
			} 
			catch (IOException e) {
				LOG.error("IOException while attempting the actual upload file " + filename + " during processAdd "
								+ " for site " + podcastService.getSiteId() + ". " + e.getMessage(), e);
				setErrorMessage(IO_ALERT);
				
				// stay on Add podcast page
				return "podcastAdd";

			}

			try {
				Date displayDate = null;

				try {
					displayDate = convertDateString(date,
							getErrorMessageString(DATE_PICKER_FORMAT));

				} 
				catch (ParseException e) {
					// must have entered it in by hand so try again
					try {
						displayDate = convertDateString(date,
								getErrorMessageString(DATE_BY_HAND_FORMAT));

					} 
					catch (ParseException e1) {
						// Now it's invalid, so set error message and stay on page
						LOG.warn("ParseException attempting to convert " + date
								+ " both valid ways. " + e1.getMessage(), e1);

						displayInvalidDateErrMsg = true;
						return "podcastAdd";
					}

				}

				podcastService.addPodcast(title, displayDate, description,
						fileContents, filename, fileContentType);

				// We're good, no error message need be displayed
				displayNoFileErrMsg = false;
				displayNoDateErrMsg = false;
				displayNoTitleErrMsg = false;
				displayInvalidDateErrMsg = false;

				// erase data on page
				title = "";
				date = null;
				description = "";
				filename = "";
				fileAsStream = null;

				// back to main page
				whereToGo = "cancel";

			} 
			catch (OverQuotaException e) {
				LOG.warn("OverQuotaException while attempting to actually add the new podcast "
								+ " for site " + podcastService.getSiteId() + ". " + e.getMessage(), e);
				setErrorMessage(QUOTA_ALERT);

			} 
			catch (ServerOverloadException e) {
				LOG.info("ServerOverloadException while attempting to actually add the new podcast "
								+ " for site " + podcastService.getSiteId() + ". " + e.getMessage(), e);
				setErrorMessage(INTERNAL_ERROR_ALERT);

			} 
			catch (InconsistentException e) {
				LOG.error("InconsistentException while attempting to actually add the new podcast "
								+ " for site " + podcastService.getSiteId() + ". " + e.getMessage(), e);
				throw new Error(e);

			} 
			catch (IdInvalidException e) {
				LOG.error("IdInvalidException while attempting to actually add the new podcast "
								+ " for site " + podcastService.getSiteId() + ". " + e.getMessage(), e);
				setErrorMessage(ID_INVALID_ALERT);

			} 
			catch (IdLengthException e) {
				LOG.warn("IdLengthException while attempting to actually add the new podcast "
								+ " for site " + podcastService.getSiteId() + ". " + e.getMessage(), e);
				setErrorMessage(LENGTH_ALERT);

			} 
			catch (PermissionException e) {
				LOG.warn("PermissionException while attempting to actually add the new podcast "
								+ " for site " + podcastService.getSiteId() + ". " + e.getMessage(), e);
				setErrorMessage(PERMISSION_ALERT);

			} 
			catch (IdUniquenessException e) {
				LOG.error("IdUniquenessException while attempting to actually add the new podcast "
								+ " for site " + podcastService.getSiteId() + ". " + e.getMessage(), e);
				setErrorMessage(ID_USED_ALERT);

			}

			// TODO add email notification code here
			/*
			 * if (email.equalsIgnoreCase("high")) {
			 * EmailService.send("josrodri@iupui.edu", "josrodri@iupui.edu", "A
			 * podcast has been added to feed.", "A podcast has been added to
			 * the list of podcasts. It's publish date will determine when it
			 * will be available in the feed", null, null, null); } else if
			 * (email.equalsIgnoreCase("low")){ //TODO: email only those who
			 * have opted in }
			 */}
		
		return whereToGo;
	}

	/**
	 * Erases bean values since no podcast is to be added.
	 * 
	 * @return String 
	 * 				Sent to return to main page.
	 */
	public String processCancelAdd() {
		date = null;
		title = "";
		description = "";
		fileAsStream = null;
		filename = "";
		displayNoFileErrMsg = false;
		displayNoDateErrMsg = false;
		displayNoTitleErrMsg = false;
		displayInvalidDateErrMsg = false;

		return "cancel";
	}

	/**
	 * Gathers information and calls PodcastService to make changes to existing
	 * podcast.
	 */
	public String processRevisePodcast() {
		// set error messages to false so can be
		// turned on during processing
		displayNoTitleErrMsg = false;
		displayInvalidDateErrMsg = false;

		String whereToGo = "cancel";
		boolean filenameChange = false;
		byte[] fileContents = null;

		// if they blank out the title, stay here since
		// a title is a requirement
		if ("".equals(selectedPodcast.title.trim())) {
			displayNoTitleErrMsg = true;
			if ("".equals(selectedPodcast.displayDateRevise)) {
				displayInvalidDateErrMsg = true;
			}
			else {
				displayInvalidDateErrMsg = false;
			}
			
			return "revise";
		}

		// If file has changed, change it in the resource
		if (filename != null) {
			if (!filename.equals("")) {
				selectedPodcast.filename = filename;
				filenameChange = true;

				if (fileAsStream != null) {
					fileContents = new byte[(int) fileSize];

				} 
				else {
					fileContents = new byte[(int) selectedPodcast.fileSize];
				}

				try {
					fileAsStream.read(fileContents);
					
				} 
				catch (IOException e) {
					LOG.error("IOException while attempting to get file contents when revising podcast for "
									+ filename + " in site " + podcastService.getSiteId() + ". " + e.getMessage(), e);
					setErrorMessage(IO_ALERT);
					return "podcastRevise";
					
				}
			}
		}

		Date displayDate = null;
		Date displayDateRevise = null;
		try {
			try {
				// SAK-13493: SimpleDateFormat.parse() did not enforce format specified, so
				// had to call custom method to check if String was valid
				if (isValidDate(selectedPodcast.displayDateRevise)) {
					displayDateRevise = convertDateString(selectedPodcast.displayDateRevise, 
											getErrorMessageString(DATE_BY_HAND_FORMAT));
				}
				else {
					throw new ParseException("Invalid displayDate stored in selectedPodcast", 0);
				}
			}
			catch (ParseException e) {
				// must have used date picker, so try again
				if (isValidDate(selectedPodcast.displayDateRevise)) {
					displayDateRevise = convertDateString(selectedPodcast.displayDateRevise, 
											getErrorMessageString(DATE_PICKER_FORMAT));
				}
				else {
					throw new ParseException("Invalid displayDate entered while revising podcast " + selectedPodcast.filename, 0);
				}
			}

			if (filenameChange) {
				// filename has changed, so create an entirely new entry - is
				// needed since filename part of resource URL
				podcastService.addPodcast(selectedPodcast.title, displayDateRevise,
						selectedPodcast.description, fileContents, filename,
						fileContentType);

				podcastService.removePodcast(selectedPodcast.getResourceId());
			} 
			else {

				// only title, description, or date has changed, so can revise
				podcastService.revisePodcast(selectedPodcast.resourceId,
						selectedPodcast.title, displayDateRevise,
						selectedPodcast.description, fileContents,
						selectedPodcast.filename);

			}

			/* FUTURE: Enable notification
			if (email.equalsIgnoreCase("high")) {
				EmailService.send("josrodri@iupui.edu","josrodri@iupui.edu",
								"A podcast has been added to feed.",
								"A podcast has been added to the list of podcasts. "
										+ "It's publish date will determine when it will be available in the feed",
								null, null, null);
			} 
			else if (email.equalsIgnoreCase("low")) {
				// FUTURE: email only those who have opted in
			}
*/			
		} 
		catch (ParseException e1) {
			LOG.error("ParseException attempting to convert date for " + selectedPodcast.title
							+ " for site " + podcastService.getSiteId() + ". " + e1.getMessage(), e1);
			date = "";
			displayInvalidDateErrMsg = true;
			return "podcastRevise";
		}
		catch (PermissionException e) {
			LOG.error("PermissionException while revising podcast "
					+ selectedPodcast.title + " for site " + podcastService.getSiteId() + ". " + e.getMessage(), e);
			setErrorMessage(PERMISSION_ALERT);
			
		} 
		catch (InUseException e) {
			LOG.warn("InUseException while revising podcast "
					+ selectedPodcast.title + " for site " + podcastService.getSiteId() + ". " + e.getMessage(), e);
			setErrorMessage(INTERNAL_ERROR_ALERT);

		} 
		catch (OverQuotaException e) {
			LOG.warn("OverQuotaException while revising podcast "
					+ selectedPodcast.title + " for site " + podcastService.getSiteId() + ". " + e.getMessage(), e);
			setErrorMessage(QUOTA_ALERT);

		} 
		catch (ServerOverloadException e) {
			LOG.warn("ServerOverloadException while revising podcast "
					+ selectedPodcast.title + " for site " + podcastService.getSiteId() + ". " + e.getMessage(), e);
			setErrorMessage(INTERNAL_ERROR_ALERT);

		} 
		catch (IdLengthException e) {
			LOG.warn("IdLengthException while revising podcast with filename changed from "
							+ selectedPodcast.filename + " to " + filename
							+ " for site " + podcastService.getSiteId() + ". " + e.getMessage(), e);
			setErrorMessage(LENGTH_ALERT);
			return "podcastRevise";

		}
		catch (Exception e) {
			// catches	IdUnusedException	TypeException
			//			IdInvalidException	InconsistentException
			//			IdUniquenessException
			LOG.error(e.getMessage() + " while revising podcast with filename changed from "
							+ selectedPodcast.filename + " to " + filename
							+ " for site " + podcastService.getSiteId() + ". " + e.getMessage(), e);
			setErrorMessage(INTERNAL_ERROR_ALERT);

		} 

		// Reset values to continue processing
		date = null;
		title = "";
		description = "";
		fileAsStream = null;
		filename = "";
		displayNoTitleErrMsg = false;

		return whereToGo;
	}

	/**
	 * Resets selectedPodcast bean since no revision is to be made
	 */
	public String processCancelRevise() {
		selectedPodcast = null;
		date = null;
		title = "";
		description = "";
		fileAsStream = null;
		filename = "";
		displayInvalidDateErrMsg = false;
		displayNoTitleErrMsg = false;

		return "cancel";
	}

	/**
	 * Used to call podcastService to actually delete selected podcast.
	 */
	public String processDeletePodcast() {
		try {
			podcastService.removePodcast(selectedPodcast.getResourceId());
			
			return "cancel";
			
		} 
		catch (PermissionException e) {
			LOG.error("PermissionException while deleting podcast "
							+ selectedPodcast.title + " from site " + podcastService.getSiteId() 
							+ ". " + e.getMessage(), e);
			setErrorMessage(PERMISSION_ALERT);

		} 
		catch (InUseException e) {
			LOG.warn("InUseException while deleting podcast "
					+ selectedPodcast.title + " from site " + podcastService.getSiteId() 
					+ ". " + e.getMessage(), e);
			setErrorMessage(INTERNAL_ERROR_ALERT);

		} 
		catch (Exception e) {
			// For IdUnusedException and TypeException
			LOG.error(e.getMessage() + " while deleting podcast "
					+ selectedPodcast.title + " from site " + podcastService.getSiteId() 
					+ ". " + e.getMessage(), e);
			setErrorMessage(INTERNAL_ERROR_ALERT);

		}

		// If here these was an error so stay on this page
		return "podcastDelete";
	}

	/**
	 * Resets selectedPodcast bean since no deletion is to be made
	 * 
	 * @return String 
	 * 				Sent to return to main page.
	 */
	public String processCancelDelete() {
		selectedPodcast = null;
		return "cancel";		
	}

	/**
	 * Returns whether a no file selected error message is displayed 
	 */
	public boolean getDisplayNoFileErrMsg() {
		return displayNoFileErrMsg;
	}

	public void setDisplayNoFileErrMsg(boolean displayNoFileErrMsg) {
		this.displayNoFileErrMsg = displayNoFileErrMsg;
	}

	/**
	 * Returns whether a no date error message is displayed
	 */
	public boolean getDisplayNoDateErrMsg() {
		return displayNoDateErrMsg;
	}

	public void setDisplayNoDateErrMsg(boolean displayNoDateErrMsg) {
		this.displayNoDateErrMsg = displayNoDateErrMsg;
	}

	/**
	 * Returns whether a no title entered error message is displayed
	 */
	public boolean getDisplayNoTitleErrMsg() {
		return displayNoTitleErrMsg;
	}

	public void setDisplayNoTitleErrMsg(boolean displayNoTitleErrMsg) {
		this.displayNoTitleErrMsg = displayNoTitleErrMsg;
	}

	public boolean getErrorOnPage() {
		return displayNoFileErrMsg || displayNoDateErrMsg || displayNoTitleErrMsg || displayInvalidDateErrMsg;
	}
	
	/**
	 * Returns whether an invalid date error message is displayed
	 */
	public boolean getDisplayInvalidDateErrMsg() {
		return displayInvalidDateErrMsg;
	}

	/**
	 * @param displayInvalidDateErrMsg
	 *            The displayInvalidDateErrMsg to set.
	 */
	public void setDisplayInvalidDateErrMsg(boolean displayInvalidDateErrMsg) {
		this.displayInvalidDateErrMsg = displayInvalidDateErrMsg;
	}

	/**
	 * Performs validation of input when attempting to add a podcast.
	 * Checks filename, date, and title fields since those are the
	 * required ones. If errors, sets boolean flags so proper error 
	 * messages appear on the page.
	 * 
	 * @return boolean
	 * 			TRUE - input validated, FALSE -  there were errors
	 */
	private boolean OKtoAdd() {
		boolean OKtoAdd = true;

		if (filename == null) {
			displayNoFileErrMsg = true;
			OKtoAdd = false;
			
		} 
		else if (filename.trim().equals("")) {
			displayNoFileErrMsg = true;
			OKtoAdd = false;
		
		} 
		else {
			displayNoFileErrMsg = false;
		
		}

		if (date == null) {
			displayNoDateErrMsg = true;
			OKtoAdd = false;
		
		}
		else if (date.trim().equals("")) {
			displayNoDateErrMsg = true;
			OKtoAdd = false;
		
		}
		else {
			displayNoDateErrMsg = false;

			if (isValidDate(date)) {
				displayInvalidDateErrMsg = false;
			
			} 
			else {
				displayInvalidDateErrMsg = true;
				OKtoAdd = false;
			
			}

		}

		if (title == null) {
			displayNoTitleErrMsg = true;
			OKtoAdd = false;
		
		}
		else if (title.trim().equals("")) {
			displayNoTitleErrMsg = true;
			OKtoAdd = false;
		
		}
		else {
			displayNoTitleErrMsg = false;
		
		}

		return OKtoAdd;
	}

	/**
	 * Sets the Faces error message by pulling the message from the
	 * MessageBundle using the name passed in
	 * 
	 * @param key
	 *           The name in the MessageBundle for the message wanted
	 *            
	 * @return String
	 * 			The string that is the value of the message
	 */
	private String getErrorMessageString(String key) {
		if (rb == null) {
	          String bundle = FacesContext.getCurrentInstance().getApplication().getMessageBundle();
	          rb = new ResourceLoader(bundle);
		}
		return rb.getString(key);
	}

	/**
	 * Performs date validation checking. Validator object
	 * does not do bounds checking, so do that and if OK,
	 * let Validator check for errors like Feb 30, etc.
	 * 
	 * TODO: Try and find an actual validator to replace this
	 * 		 method
	 * 
	 * @param date
	 * 			The candidate String date
	 * 
	 * @return boolean
	 * 			TRUE - Conforms to a valid input date format string
	 * 			FALSE - Does not conform 
	 */
	private boolean isValidDate(String date) {
		boolean validDate = true;

		// Should contain date part, time port, AM/PM part
		String[] wholeDateSplit = date.split(" ");

		// if not in 2 parts, input error
		if (wholeDateSplit.length != 3) {
			return false;
		}

		// since date entered first, check it first
		String[] dateSplit = wholeDateSplit[0].split("/");
		
		if (dateSplit.length != 3) {
			return false;

		} 
		else {
			int month = Integer.parseInt(dateSplit[0]);
			int day = Integer.parseInt(dateSplit[1]);

			if (month < 0 || month > 12) {
				return false;
			} 
			else if (day < 0 || day > 31) {
				return false;
			} 
			else if (dateSplit[2].length() != 4) {
				return false;
			} 
			else {
				int year = Integer.parseInt(dateSplit[2]);

				validDate = Validator.checkDate(day, month, year);
			}
		}

		if (! validDate) {
			return false;
		}
		else {
			// Date's OK, now to the time
			String[] timeSplit = wholeDateSplit[1].split(":");

			// Valid times are hh:mm or hh:mm:ss, so check for either 
			if (timeSplit.length < 2 || timeSplit.length > 3) {
				return false;

			} 
			else if (timeSplit.length == 2) {
				int hour = Integer.parseInt(timeSplit[0]);
				int min = Integer.parseInt(timeSplit[1]);

				if (hour < 1 || hour > 12) {
					return false;

				} 
				else if (min < 0 || min > 59) {
					return false;

				}
			} 
			else {
				int hour = Integer.parseInt(timeSplit[0]);
				int min = Integer.parseInt(timeSplit[1]);
				int sec = Integer.parseInt(timeSplit[2]);

				if (hour < 1 || hour > 12) {
					return false;

				}
				else if (min < 0 || min > 59) {
					return false;

				} 
				else if (sec < 0 || sec > 59) {
					return false;

				}
			}
		}

		// We want a 12 hour clock, so AM/PM needs to be specified
		if ("AM".equalsIgnoreCase(wholeDateSplit[2]) || "PM".equalsIgnoreCase(wholeDateSplit[2])) {
			return true;

		}
		else {
			return false;

		}	
	}
	
	private String formatDate(long date) {
		String disTimeString = TimeService.newTime(date).toStringGmtFull();
		
		String temp = monStrings.get(disTimeString.substring(0, 3)) + disTimeString.substring(3);
		
		return temp;
		
	}

	/**
	 * Returns whether a file too large tried to be uploaded. (SAK-9822) 
	 */
	public boolean getUploadStatus() {
		LOG.debug("getUploadStatus()");
		FacesContext context = FacesContext.getCurrentInstance();
		String status = (String) ((HttpServletRequest) context.getExternalContext().getRequest()).getAttribute("upload.status");

		return "size_limit_exceeded".equals(status);
	}
	
	/**
	 * Returns the message that a file too large tried to be uploaded. (SAK-9822) 
	 */
	public String getMaxSizeExceededAlert() {
		if (rb == null) {
	          String bundle = FacesContext.getCurrentInstance().getApplication().getMessageBundle();
	          rb = new ResourceLoader(bundle);
		}

		// TODO: pull this from the ServerConfigurationService? RequestFilter?
		FacesContext context = FacesContext.getCurrentInstance();
		String uploadMaxSize = ServerConfigurationService.getString("content.upload.max");
		
		Long override = (Long) ((HttpServletRequest) context.getExternalContext().getRequest()).getAttribute("upload.limit");
		
		if (override != null && ! "".equals(override)) {
			uploadMaxSize = override.toString();
		}

		return rb.getFormattedMessage("max_size_exceeded_alert", new String [] { uploadMaxSize });
	}
}
