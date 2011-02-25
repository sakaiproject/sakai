package au.edu.anu.portal.portlets.rss;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.ReadOnlyException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ValidatorException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.anu.portal.portlets.rss.utils.Constants;
import au.edu.anu.portal.portlets.rss.utils.Messages;


/**
 * SimpleRssPortlet
 * 
 * This is the portlet class.
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
public class SimpleRSSPortlet extends GenericPortlet{

	private final Log log = LogFactory.getLog(getClass().getName());
	
	// pages
	private String viewUrl;
	private String errorUrl;
	private String configUrl;
	
	//cache
	private Cache cache;
	private final String CACHE_NAME = "au.edu.anu.portal.portlets.cache.SimpleRSSPortletCache";
	
	public void init(PortletConfig config) throws PortletException {	   
	   super.init(config);
	   log.info("Simple RSS Portlet init()");
	   
	   //pages
	   viewUrl = config.getInitParameter("viewUrl");
	   errorUrl = config.getInitParameter("errorUrl");
	   configUrl = config.getInitParameter("configUrl");

	   //setup cache
	   CacheManager manager = new CacheManager();
	   cache = manager.getCache(CACHE_NAME);
	}
	
	/**
	 * Delegate to appropriate PortletMode.
	 */
	protected void doDispatch(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		log.info("Simple RSS doDispatch()");

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
		log.info("Simple RSS doView()");
		
		//get data
		
		//request.setAttribute("proxyContextUrl", proxy.toString());
		
		dispatch(request, response, viewUrl);
	}	
	
	
	
	/**
	 * Custom mode handler for CONFIG view
	 */
	protected void doConfig(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		log.info("Simple RSS doConfig()");

		request.setAttribute("configuredPortletTitle", getConfiguredPortletTitle(request));
		request.setAttribute("configuredFeedUrl", getConfiguredFeedUrl(request));
		
		dispatch(request, response, configUrl);
	}
	
	/**
	 * Process any portlet actions. At this stage they are all from the submission of the CONFIG mode.
	 */
	public void processAction(ActionRequest request, ActionResponse response) {
		log.info("Simple RSS processAction()");
		
		//At this stage we only ever accept actions from the CONFIG mode.
		//If in future we allow user editing, then a check needs to be done here to see
		//from what PortletMode the processAction was called (see doDispatch)
		
		boolean success = true;
		//get prefs and submitted values
		PortletPreferences prefs = request.getPreferences();
		String portletTitle = request.getParameter("portletTitle");
		String feedUrl = request.getParameter("feedUrl");
		
		//validate
		try {
			prefs.setValue("portlet_title", portletTitle);
			prefs.setValue("feed_url", feedUrl);
		} catch (ReadOnlyException e) {
			success = false;
			response.setRenderParameter("errorMessage", Messages.getString("error.form.readonly.error"));
			log.error(e);
		}
		
		//save them
		if(success) {
			try {
				prefs.store();
				response.setPortletMode(PortletMode.VIEW);
			} catch (ValidatorException e) {
				response.setRenderParameter("errorMessage", e.getMessage());
				log.error(e);
			} catch (IOException e) {
				response.setRenderParameter("errorMessage", Messages.getString("error.form.save.error"));
				log.error(e);
			} catch (PortletModeException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Gets the unique namespace for this portlet
	 * @param response
	 * @return
	 */
	private String getPortletNamespace(RenderResponse response) {
		return response.getNamespace();
	}
			
	
	/**
	 * Get the feed content
	 * @param request
	 * @param response
	 * @return Map of params or null if any required data is missing
	 */
	private Map<String,String> getFeedContent(RenderRequest request, RenderResponse response) {
		
		Map<String,String> params = new HashMap<String,String>();
		
		//check cache, otherwise get fresh
		String cacheKey = getPortletNamespace(response);
		Element element = cache.get(cacheKey);
		if(element != null) {
			log.info("Fetching data from cache for: " + cacheKey);
			params = (Map<String, String>) element.getObjectValue();
		} else {
		
			//get feed data
			
			//cache the data, must be done before signing
			log.info("Adding data to cache for: " + cacheKey);
			cache.put(new Element(cacheKey, params));
		}
		
		
		
		return params;
	}
	
	/**
	 * Get the preferred portlet title if set, or default from Constants
	 * @param request
	 * @return
	 */
	private String getConfiguredPortletTitle(RenderRequest request) {
		PortletPreferences pref = request.getPreferences();
		return pref.getValue("portlet_title", Constants.PORTLET_TITLE_DEFAULT);
	}
	
	/**
	 * Get the preferred portlet height if set, or default from Constants
	 * @param request
	 * @return
	 */
	private String getConfiguredFeedUrl(RenderRequest request) {
	      PortletPreferences pref = request.getPreferences();
	      return pref.getValue("feed_url", Constants.FEED_URL_DEFAULT);
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
		
		//message
		request.setAttribute("errorMessage", Messages.getString(messageKey));
		
		//optional heading
		if(StringUtils.isNotBlank(headingKey)){
			request.setAttribute("errorHeading", Messages.getString(headingKey));
		} else {
			request.setAttribute("errorHeading", Messages.getString("error.heading.general"));
		}
		
		//dispatch
		try {
			dispatch(request, response, errorUrl);
		} catch (Exception e) {
			e.printStackTrace();
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

	
	
	public void destroy() {
		log.info("destroy()");
	}
	
	
}
