package org.sakaiproject.search.tool;

import javax.servlet.http.HttpServletResponse;

public interface SherlockSearchBean
{
	/**
	 * Get the site name
	 * @return
	 */
	String getSiteName();

	/**
	 * get the search url
	 * @return
	 */
	String getSearchURL();

	/**
	 * get the update Url
	 * @return
	 */
	String getUpdateURL();

	/**
	 * get the Icon URL
	 * @return
	 */
	String getUpdateIcon();
	
	/**
	 * Stream the Icon
	 *
	 */
	void sendIcon(HttpServletResponse response);

}
