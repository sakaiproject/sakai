/**
 * Copyright 2011-2013 The Australian National University
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package au.edu.anu.portal.portlets.rss;

import java.io.IOException;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletURL;
import javax.portlet.ReadOnlyException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ValidatorException;

import com.sun.syndication.feed.synd.SyndFeed;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import au.edu.anu.portal.portlets.rss.model.Attachment;
import au.edu.anu.portal.portlets.rss.utils.Constants;
import au.edu.anu.portal.portlets.rss.utils.Messages;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;

/**
 * SimpleRssPortlet
 * 
 * This is the portlet class.
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
@Slf4j
public class SimpleRSSPortlet extends GenericPortlet{

	// pages
	private String viewUrl;
	private String editUrl;
	private String errorUrl;
	private String noContentUrl;

	//cache
	private MemoryService memoryService;
	private Cache<String, SyndFeed> feedCache;
	private Cache<String, Map<String, Attachment>> mediaCache;

	private static final String FEED_CACHE_NAME = "au.edu.anu.portal.portlets.cache.SimpleRSSPortletCache.feed";
	private static final String MEDIA_CACHE_NAME = "au.edu.anu.portal.portlets.cache.SimpleRSSPortletCache.media";

	//pref names
	private final String PREF_PORTLET_TITLE = "portlet_title";
	private final String PREF_FEED_URL = "feed_url";
	private final String PREF_MAX_ITEMS = "max_items";

	public void init(PortletConfig config) throws PortletException {	   
	   super.init(config);
	   log.info("Simple RSS Portlet init()");
	   
	   //pages
	   viewUrl = config.getInitParameter("viewUrl");
	   editUrl = config.getInitParameter("editUrl");
	   errorUrl = config.getInitParameter("errorUrl");
	   noContentUrl = config.getInitParameter("noContentUrl");

	   //setup cache
	   memoryService = ComponentManager.get(MemoryService.class);
	   feedCache = memoryService.getCache(FEED_CACHE_NAME);
	   mediaCache = memoryService.getCache(MEDIA_CACHE_NAME);
	   
	}

	/**
	 * Delegate to appropriate PortletMode.
	 */
	protected void doDispatch(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		log.debug("Simple RSS doDispatch()");

		if (StringUtils.equalsIgnoreCase(request.getPortletMode().toString(), "CONFIG")) {
			doConfig(request, response);
		}
		else {
			super.doDispatch(request, response);
		}
	}

	/**
	 * Render the main view
	 */
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		log.debug("Simple RSS doView()");
		
		//get feed URL
		String feedUrl = getConfiguredFeedUrl(request);

		//get feed data
		SyndFeed feed = getFeedContent(request, response);

		//catch - errors already handled
		if(feed == null) {
			return;
		}

		//catch and send to no content page
		if(feed.getEntries().isEmpty()) {
			dispatch(request, response, noContentUrl);
		}

		//get the media associated with the entries in this feed
		Map<String,Attachment> media = getFeedMedia(feed, feedUrl);

		//get max items (subtract 1 since it will be used in a 0 based index)
		int maxItems = getConfiguredMaxItems(request) - 1;

		request.setAttribute("SyndFeed", feed);
		request.setAttribute("Media", media);
		request.setAttribute("maxItems", maxItems);
		
		dispatch(request, response, viewUrl);
	}	
	
	/**
	 * Custom mode handler for EDIT view
	 */
	protected void doEdit(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		log.debug("Simple RSS doEdit()");

		//if we have an error message, replay the form
		String errorMessage = request.getParameter("errorMessage");

		if(StringUtils.isNotBlank(errorMessage)) {
			//PORT-672 replay data from the request so it is preserved
			request.setAttribute("portletTitle", request.getParameter("portletTitle"));
			request.setAttribute("feedUrl", request.getParameter("feedUrl"));
			request.setAttribute("maxItems", request.getParameter("maxItems"));
		} else {
			//get it from the preferences
			request.setAttribute("portletTitle", getConfiguredPortletTitle(request));
			request.setAttribute("feedUrl", getConfiguredFeedUrl(request));
			request.setAttribute("maxItems", getConfiguredMaxItems(request));
		}

		//check permissions
		request.setAttribute("feedUrlIsLocked", isPrefLocked(request, PREF_FEED_URL));
		request.setAttribute("portletTitleIsLocked", isPrefLocked(request, PREF_PORTLET_TITLE));

		//cancel url
		request.setAttribute("cancelUrl", getPortletModeUrl(response, PortletMode.VIEW));
		
		//get any error message that is in the request and pass it on
		request.setAttribute("errorMessage", request.getParameter("errorMessage"));
		
		dispatch(request, response, editUrl);
	}

	/**
	 * Custom mode handler for CONFIG view
	 * Identical to EDIT mode.
	 */
	protected void doConfig(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		log.debug("Simple RSS doConfig()");
		doEdit(request,response);
	}

	/**
	 * Process any portlet actions. 
	 */
	public void processAction(ActionRequest request, ActionResponse response) throws PortletModeException  {
		log.debug("Simple RSS processAction()");
		
		//this handles both EDIT and CONFIG modes in exactly the same way.
		//if we need to split, check PortletMode.

		boolean success = true;
		//get prefs and submitted values
		PortletPreferences prefs = request.getPreferences();
		String portletTitle = StringEscapeUtils.escapeHtml(StringUtils.trim(request.getParameter("portletTitle")));
		String maxItems = StringUtils.trim(request.getParameter("maxItems"));
		String feedUrl = StringUtils.trim(request.getParameter("feedUrl"));
		
		//portlet title could be blank, set to default
		if(StringUtils.isBlank(portletTitle)){
			portletTitle=Constants.PORTLET_TITLE_DEFAULT;
		}

		boolean feedUrlIsLocked = isPrefLocked(request, PREF_FEED_URL);
		boolean portletTitleIsLocked = isPrefLocked(request, PREF_PORTLET_TITLE);

		//check not readonly
		try {
			//only do this if we know its not locked, ie this is not a preconfigured portlet
			if(!portletTitleIsLocked) {
				prefs.setValue(PREF_PORTLET_TITLE, portletTitle);
			}
			
			//only do this if we know its not locked, ie this is not a preconfigured portlet
			if(!feedUrlIsLocked) {
				prefs.setValue(PREF_FEED_URL, feedUrl);
			}
			prefs.setValue(PREF_MAX_ITEMS, maxItems);
		} catch (ReadOnlyException e) {
			success = false;
			response.setRenderParameter("errorMessage", Messages.getString("error.form.readonly.error"));
			log.error(e.getMessage());
		}

		//validate and save
		if(success) {
			try {
				prefs.store();
				response.setPortletMode(PortletMode.VIEW);
				
			} catch (ValidatorException e) {
				//PORT-672 present entered data on the form again
				response.setRenderParameter("errorMessage", e.getMessage());
				response.setRenderParameter("portletTitle", portletTitle);
				response.setRenderParameter("maxItems", maxItems);
				
				//this will be null if locked so don't set it, we dont need it
				if(!feedUrlIsLocked) {
					response.setRenderParameter("feedUrl", feedUrl);
				}
				log.error(e.getMessage());
			} catch (IOException e) {
				response.setRenderParameter("errorMessage", Messages.getString("error.form.save.error"));
				log.error(e.getMessage());
			} catch (PortletModeException e) {
				log.error(e.getMessage(), e);
			}
		}
		
		
	}

	/**
	 * Get the feed content
	 * @param request
	 * @param response
	 * @return Map of params or null if any required data is missing
	 */
	private SyndFeed getFeedContent(RenderRequest request, RenderResponse response) {
		
		SyndFeed feed;
		
		//check cache, otherwise get fresh
		//we use the feedUrl as the cacheKey
		String feedUrl = getConfiguredFeedUrl(request);
		if(StringUtils.isBlank(feedUrl)) {
			log.debug("No feed URL configured");
			doError("error.no.config", "error.heading.config", getPortletModeUrl(response, PortletMode.EDIT), request, response);
			return null;
		}
		
		String cacheKey = feedUrl;
		
		feed = feedCache.get(cacheKey);
		if(feed != null) {
			log.debug("Fetching data from feed cache for: " + cacheKey);
		} else {
			//get from remote
			feed = getRemoteFeed(feedUrl, request, response);
		}
		
		return feed;
	}

	/**
	 * Helper to get the remote feed data and cache it
	 * @param feedUrl
	 * @param request
	 * @param response
	 * @return
	 */
	private SyndFeed getRemoteFeed(String feedUrl, RenderRequest request, RenderResponse response) {
		
		//get feed data
		SyndFeed feed = new FeedParser().parseFeed(feedUrl);
		if(feed == null) {
			log.error("No data was returned from remote server.");
			doError("error.no.remote.data", "error.heading.general", request, response);
			return null;
		}
		
		//cache the data,
		log.debug("Adding data to feed cache for: " + feedUrl);
		feedCache.put(feedUrl, feed);
		
		return feed;
	}

	/**
	 * Helper for extracting the enclosures (media) associated with entries in the feed.
	 * They are returned as a map of String to Attachment where String is the entry Uri
	 * 
	 * @param feed		The raw SyndFeed to process
	 * @param feedUrl	The url of this feed
	 * @return
	 */
	private Map<String,Attachment> getFeedMedia(SyndFeed feed, String feedUrl) {
		
		Map<String,Attachment> media;
		
		//check cache
		media = mediaCache.get(feedUrl);
		if(media != null) {
			log.debug("Fetching data from media cache for: " + feedUrl);
			return media;
		} else {
		
			//parse the enclosures for this feed
			media = FeedParser.parseFeedEnclosures(feed);
			
			//cache the data
			log.debug("Adding data to media cache for: " + feedUrl);
			mediaCache.put(feedUrl, media);
		}
		
		return media;
	}

	/**
	 * Get the preferred portlet title if set, or default from Constants
	 * @param request
	 * @return
	 */
	private String getConfiguredPortletTitle(RenderRequest request) {
		PortletPreferences pref = request.getPreferences();
		return pref.getValue(PREF_PORTLET_TITLE, Constants.PORTLET_TITLE_DEFAULT);
	}

	/**
	 * Get the preferred portlet height if set, or default from Constants
	 * @param request
	 * @return
	 */
	private String getConfiguredFeedUrl(RenderRequest request) {
	      PortletPreferences pref = request.getPreferences();
	      return pref.getValue(PREF_FEED_URL, null);
	}

	/**
	 * Get the preferred max number of items, or default from Constants
	 * @param request
	 * @return
	 */
	private int getConfiguredMaxItems(RenderRequest request) {
	      PortletPreferences pref = request.getPreferences();
	      return Integer.valueOf(pref.getValue(PREF_MAX_ITEMS, Integer.toString(Constants.MAX_ITEMS)));
	}

	/**
	 * Override GenericPortlet.getTitle() to use the preferred title for the portlet instead
	 */
	@Override
	protected String getTitle(RenderRequest request) {
		return getConfiguredPortletTitle(request);
	}
	

	/**
	 * Helper to handle error messages
	 * @param messageKey	Message bundle key
	 * @param headingKey	optional error heading message bundle key, if not specified, the general one is used
	 * @param request
	 * @param response
	 */
	private void doError(String messageKey, String headingKey, RenderRequest request, RenderResponse response){
		doError(messageKey, headingKey, null, request, response);
	}

	/**
	 * Helper to handle error messages
	 * @param messageKey	Message bundle key
	 * @param headingKey	optional error heading message bundle key, if not specified, the general one is used
	 * @param link			if the message text is to be linked, what is the href?
	 * @param request
	 * @param response
	 */
	private void doError(String messageKey, String headingKey, String link, RenderRequest request, RenderResponse response){
		
		//message
		request.setAttribute("errorMessage", Messages.getString(messageKey));
		
		//optional heading
		if(StringUtils.isNotBlank(headingKey)){
			request.setAttribute("errorHeading", Messages.getString(headingKey));
		} else {
			request.setAttribute("errorHeading", Messages.getString("error.heading.general"));
		}

		if(StringUtils.isNotBlank(link)){
			request.setAttribute("errorLink", link);
		}

		//dispatch
		try {
			dispatch(request, response, errorUrl);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	
	/**
	 * Dispatch to a JSP or servlet
	 * @param request
	 * @param response
	 * @param path
	 * @throws PortletException
	 * @throws IOException
	 */
	protected void dispatch(RenderRequest request, RenderResponse response, String path)throws PortletException, IOException {
		response.setContentType("text/html"); 
		PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher(path);
		dispatcher.include(request, response);
	}

	
	/**
	 * Helper to get the URL to take us to a portlet mode.
	 * This will end up in doDispatch.
	 * 
	 * @param response
	 * @return
	 */
	private String getPortletModeUrl(RenderResponse response, PortletMode mode) {

		PortletURL url = response.createRenderURL();
	    try {
	    	url.setPortletMode(mode);
		} catch (PortletModeException e) {
			log.error("Invalid portlet mode: " + mode);
			return null;
		}
	    
		return url.toString();
	}

	/**
	 * Helper to check if a preference is locked (ie readonly). This may be set by a channel config to restrict access.
	 * 
	 * @param request
	 * @return
	 */
	private boolean isPrefLocked(PortletRequest request, String prefName) {
		PortletPreferences prefs = request.getPreferences();
		try {
			return prefs.isReadOnly(prefName);
		} catch (IllegalArgumentException e){
			log.debug("Preference does not exist: " + prefName);
			return false;
		}
	}
	
	public void destroy() {
		log.info("Simple RSS Portlet destroy()");
		memoryService.destroyCache(FEED_CACHE_NAME);
		memoryService.destroyCache(MEDIA_CACHE_NAME);
	}

}
