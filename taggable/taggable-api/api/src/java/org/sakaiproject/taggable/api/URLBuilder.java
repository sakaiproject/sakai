package org.sakaiproject.taggable.api;

import java.util.Map;

/**
 * Convenience class for building urls out of a base url, a view and a bunch of parameters
 * @author chrismaurer
 *
 */
public interface URLBuilder {
	
	/**
	 * Builds the url from the base, view and params
	 * @return
	 */
	public String getURL();
	
	/**
	 * Set the base url to use when building the full url
	 * @param base
	 */
	public void setBaseURL(String base);
	
	/**
	 * Set the view to be used in the url
	 * @param view
	 */
	public void setView(String view);
	
	/**
	 * Sets the params that will be used in the url.  Will be put together in "key=value" form.
	 * @param params
	 */
	public void setParams(Map<String, String> params);
}
