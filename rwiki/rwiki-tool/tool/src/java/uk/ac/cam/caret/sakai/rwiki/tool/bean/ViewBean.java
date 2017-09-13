/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.tool.bean;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.tool.util.WikiPageAction;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;

/**
 * This bean is a helper bean for the view view.
 * 
 * @author andrew
 */
// FIXME: Need a better way of making urls!
public class ViewBean
{

	/**
	 * Parameter name for the parameter indicating which panel to use
	 */
	public static final String PANEL_PARAM = "panel";

	/**
	 * Value of the parameter panel that indicates the main panel
	 */
	public static final String MAIN_PANEL = "Main";

	/**
	 * Parameter name for the parameter indicating what action is required
	 */
	public static final String ACTION_PARAM = "action";

	/**
	 * Parameter name for the paramater indicating which pageName to view, edit
	 * etc.
	 */
	public static final String PAGE_NAME_PARAM = "pageName";

	/**
	 * Parameter name for the paramater indicating which pageName to view, edit
	 * etc.
	 */
	public static final String PARAM_BREADCRUMB_NAME = "breadcrumb";

	/*
	 * These are urlencoded variants of constants from files that actually do
	 * the work...
	 */
	protected static final String PAGENAME_URL_ENCODED = urlEncode(PAGE_NAME_PARAM);

	protected static final String DISBLE_BREADCRUMBS_ENCODED = urlEncode(PARAM_BREADCRUMB_NAME)
			+ "=0";

	protected static final String ACTION_URL_ENCODED = urlEncode(ACTION_PARAM);

	protected static final String PANEL_URL_ENCODED = urlEncode(PANEL_PARAM);

	protected static final String MAIN_URL_ENCODED = urlEncode(MAIN_PANEL);

	protected static final String SEARCH_URL_ENCODED = urlEncode(SearchBean.SEARCH_PARAM);

	protected static final String PAGE_URL_ENCODED = urlEncode(SearchBean.PAGE_PARAM);

	protected static final String REALM_URL_ENCODED = urlEncode(SearchBean.REALM_PARAM);

	/**
	 * The current pageName
	 */
	private String pageName;

	/**
	 * The current localSpace
	 */
	private String localSpace;

	/**
	 * The anchor to view
	 */
	private String anchor;

	/**
	 * The current search criteria
	 */
	private String search;

	/**
	 * Simple constructor that creates an empty view bean. You must set the
	 * pageName and the localSpace to make this useful
	 */
	public ViewBean()
	{
		// Beans must have null constructor!
	}

	/**
	 * Creates a ViewBean and set's the interested page name and local space
	 * 
	 * @param name
	 *        page name possibly non-globalised
	 * @param defaultSpace
	 *        default space to globalise against
	 */
	public ViewBean(String name, String defaultSpace)
	{
		this.pageName = NameHelper.globaliseName(name, defaultSpace);
		this.localSpace = defaultSpace;
	}

	private String getAnchorString()
	{
		if (anchor != null)
		{
			return "#" + urlEncode(anchor);
		}
		return "";
	}

	/**
	 * Returns a public view URL with no breadcrumbs
	 * 
	 * @return
	 */
	public String getExportUrl()
	{
		return getPageUrl(pageName, WikiPageAction.EXPORT_ACTION.getName());
	}

	/**
	 * Returns a public view URL with no breadcrumbs
	 * 
	 * @return
	 */
	public String getPublicViewUrl()
	{
		return getPublicViewUrl(false);
	}

	/**
	 * Returns a string representation of an url to perma view the current page
	 * 
	 * @return url as string
	 */
	public String getPublicViewUrl(boolean withBreadcrumbs)
	{
		return getPageUrl(pageName, WikiPageAction.PUBLICVIEW_ACTION.getName(),
				withBreadcrumbs);
	}

	/**
	 * Returns a string representation of an url to view the current page
	 * 
	 * @return url as string
	 */
	public String getViewUrl()
	{
		return getPageUrl(pageName, WikiPageAction.VIEW_ACTION.getName());
	}

	/**
	 * Returns a string representation of an url to view the passed in page
	 * 
	 * @param name
	 *        possibly non-globalised name to view
	 * @return url as string
	 */
	public String getViewUrl(String name)
	{
		return getPageUrl(NameHelper.globaliseName(name, localSpace),
				WikiPageAction.VIEW_ACTION.getName());
	}

	/**
	 * Returns a string representation of an url to edit the current page
	 * 
	 * @return url as string
	 */
	public String getEditUrl()
	{
		return getPageUrl(pageName, WikiPageAction.EDIT_ACTION.getName());
	}

	/**
	 * Returns a string representation of an url to edit the passed in page.
	 * 
	 * @param name
	 *        possibly non-globalised name
	 * @return url as string
	 */

	public String getEditUrl(String name)
	{
		return getPageUrl(NameHelper.globaliseName(name, localSpace),
				WikiPageAction.EDIT_ACTION.getName());
	}

	/**
	 * Returns a string representation of an url to view information about the
	 * current page
	 * 
	 * @return url as string
	 */
	public String getInfoUrl()
	{
		return getPageUrl(pageName, WikiPageAction.INFO_ACTION.getName());
	}

	/**
	 * Returns a string representation of an url to view information about the
	 * passed in page
	 * 
	 * @param name
	 *        possibly non-globalised name
	 * @return url as string
	 */
	public String getInfoUrl(String name)
	{
		return getPageUrl(NameHelper.globaliseName(name, localSpace),
				WikiPageAction.INFO_ACTION.getName());
	}

	public String getHistoryUrl()
	{
		return getHistoryUrl(pageName);
	}

	public String getHistoryUrl(String name)
	{
		return getPageUrl(NameHelper.globaliseName(name, localSpace),
				WikiPageAction.HISTORY_ACTION.getName());
	}

	/**
	 * Given a WikiPageAction return an url to the current page for performing
	 * that action.
	 * 
	 * @param action
	 *        WikiPageAction to perform
	 * @return url as string
	 */
	public String getActionUrl(WikiPageAction action)
	{
		return getActionUrl(action, true);
	}

	/**
	 * Given a WikiPageAction return an url to the current page (with
	 * breadcrumbs on or off) for performing that action.
	 * 
	 * @param action
	 *        WikiPageAction to perform
	 * @param breadcrumbs
	 *        false if breadcrumbs should be disabled
	 * @return url as string
	 */
	public String getActionUrl(WikiPageAction action, boolean breadcrumbs)
	{
		return getPageUrl(this.pageName, action.getName(), breadcrumbs);
	}

	/**
	 * Given a WikiPageAction return an url to the requested page (with
	 * breadcrumbs on or off) for performing that action.
	 * 
	 * @param action
	 *        WikiPageAction to perform
	 * @param breadcrumbs
	 *        false if breadcrumbs should be disabled
	 * @return url as string
	 */
	public String getActionUrl(String pageName, WikiPageAction action,
			boolean breadcrumbs)
	{
		return getPageUrl(pageName, action.getName(), breadcrumbs);
	}

	/**
	 * Given a WikiPageAction return an url to the current page with the
	 * additional parameters being set.
	 * 
	 * @param action
	 *        WikiPageAction to perform
	 * @param parameters
	 *        Additional query parameters to attach
	 * @return url as String
	 */
	public String getActionUrl(WikiPageAction action, Map parameters)
	{
		return getPageUrl(this.pageName, action.getName(), parameters);
	}

	/**
	 * Given a WikiPageAction return an url to the requested page with the
	 * additional parameters being set.
	 * 
	 * @param pageName
	 *        globalised pagename to perform action on
	 * @param action
	 *        WikiPageAction to perform
	 * @param parameters
	 *        Additional query parameters to attach
	 * @return url as String
	 */
	public String getActionUrl(String pageName, WikiPageAction action,
			Map parameters)
	{
		return getPageUrl(pageName, action.getName(), parameters);
	}

	/**
	 * Given a page name and an action return an url that represents it.
	 * 
	 * @param pageName
	 *        globalised pagename to perform action on
	 * @param action
	 *        name of action to perform
	 * @return url as string
	 */
	protected String getPageUrl(String pageName, String action)
	{
		return getPageUrl(pageName, action, true);
	}

	/**
	 * Given a page name and an action return an url that represents it.
	 * 
	 * @param pageName
	 *        globalised pagename to perform action on
	 * @param action
	 *        name of action to perform
	 * @param withBreadcrumbs
	 *        if false, breadcrumb disable is propagated
	 * @return url as string
	 */
	protected String getPageUrl(String pageName, String action,
			boolean withBreadcrumbs)
	{
		if (withBreadcrumbs)
		{
			return "?" + PAGENAME_URL_ENCODED + "=" + urlEncode(pageName) + "&"
					+ ACTION_URL_ENCODED + "=" + urlEncode(action) + "&"
					+ PANEL_URL_ENCODED + "=" + MAIN_URL_ENCODED + "&"
					+ REALM_URL_ENCODED + "=" + urlEncode(localSpace)
					+ getAnchorString();
		}
		else
		{
			return "?" + PAGENAME_URL_ENCODED + "=" + urlEncode(pageName) + "&"
					+ ACTION_URL_ENCODED + "=" + urlEncode(action) + "&"
					+ PANEL_URL_ENCODED + "=" + MAIN_URL_ENCODED + "&"
					+ DISBLE_BREADCRUMBS_ENCODED + "&" + REALM_URL_ENCODED
					+ "=" + urlEncode(localSpace) + getAnchorString();
		}

	}

	protected String getPageUrl(String pageName, String action, Map params)
	{
		StringBuffer url = new StringBuffer();
		url.append("?").append(PAGENAME_URL_ENCODED).append('=').append(
				urlEncode(pageName));
		url.append('&').append(ACTION_URL_ENCODED).append('=').append(
				urlEncode(action));
		url.append('&').append(PANEL_URL_ENCODED).append('=').append(
				MAIN_URL_ENCODED);
		url.append('&').append(REALM_URL_ENCODED).append('=').append(
				urlEncode(localSpace));

		for (Iterator<Entry<String, String>> it = params.entrySet().iterator(); it.hasNext();)
		{
			Entry<String, String> entry = it.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if (!(PAGE_NAME_PARAM.equals(key) || ACTION_PARAM.equals(key)
					|| PANEL_PARAM.equals(key) || SearchBean.REALM_PARAM
					.equals(key)))
			{
				url.append('&').append(urlEncode(key)).append('=').append(
						urlEncode(value));
			}
		}

		return url.append(getAnchorString()).toString();
	}

	/**
	 * Creates an appropriate url for searching for the given criteria. XXX this
	 * shouldn't be here!
	 * 
	 * @return url as string
	 */
	protected String getSearchUrl()
	{
		return "?" + ACTION_URL_ENCODED + "=" + SEARCH_URL_ENCODED + "&"
				+ SEARCH_URL_ENCODED + "=" + urlEncode(search) + "&"
				+ REALM_URL_ENCODED + "=" + urlEncode(localSpace) + "&"
				+ PANEL_URL_ENCODED + "=" + MAIN_URL_ENCODED;
	}

	/**
	 * The Globalised Page Name
	 * 
	 * @return globalised page name
	 */
	public String getPageName()
	{
		return pageName;
	}

	/**
	 * Set the globalised page name
	 * 
	 * @param pageName
	 *        globalised page name
	 */
	public void setPageName(String pageName)
	{
		this.pageName = pageName;
	}

	/**
	 * The page name localised against the localSpace
	 * 
	 * @return localised page name
	 */
	public String getLocalName()
	{
		return NameHelper.localizeName(this.pageName, this.localSpace);
	}

	/**
	 * The localSpace
	 * 
	 * @return localSpace as string
	 */
	public String getLocalSpace()
	{
		return localSpace;
	}

	/**
	 * Set the localSpace
	 * 
	 * @param localSpace
	 *        the new localSpace
	 */
	public void setLocalSpace(String localSpace)
	{
		this.localSpace = localSpace;
	}

	/**
	 * The space of that the page is in
	 * 
	 * @return the page's space
	 */
	public String getPageSpace()
	{
		return NameHelper.localizeSpace(pageName, localSpace);
	}

	/**
	 * Takes a string to encode and encodes it as a UTF-8 URL-Encoded string.
	 * 
	 * @param toEncode
	 *        string to encode.
	 * @return url encoded string.
	 */
	public static String urlEncode(String toEncode)
	{
		try
		{
			return URLEncoder.encode(toEncode, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new RuntimeException(
					"Not entirely sure how this happened but UTF-8 doesn't "
							+ "represent a valid encoding anymore! Weird!", e);
		}
	}

	/**
	 * Set the current anchor
	 * 
	 * @param anchor
	 *        anchor to set
	 */
	public void setAnchor(String anchor)
	{
		this.anchor = anchor;
	}

	/**
	 * Get the current anchor name
	 * 
	 * @return anchor
	 */
	public String getAnchor()
	{
		return anchor;
	}

	/**
	 * The current search criteria XXX This shouldn't be here!
	 * 
	 * @return search criteria
	 */
	public String getSearch()
	{
		return search;
	}

	/**
	 * Set the current search criteria XXX This shouldn't be here!
	 * 
	 * @param search
	 *        the search criteria
	 */
	public void setSearch(String search)
	{
		this.search = search;
	}

	public String getNewCommentURL()
	{
		return getPageUrl(pageName, WikiPageAction.NEWCOMMENT_ACTION.getName());

	}

	public String getEditCommentURL()
	{
		return getPageUrl(pageName, WikiPageAction.EDITCOMMENT_ACTION.getName());
	}

	public String getListCommentsURL()
	{
		return getPageUrl(pageName, WikiPageAction.LISTCOMMENT_ACTION.getName());
	}

	public String getListPresenceURL()
	{
		return getPageUrl(pageName, WikiPageAction.LISTPRESENCE_ACTION
				.getName());
	}

	/**
	 * @return
	 */
	public String getOpenPageChatURL()
	{
		return getPageUrl(pageName, WikiPageAction.OPENPAGECHAT_ACTION
				.getName());
	}

	/**
	 * @return
	 */
	public String getOpenSpaceChatURL()
	{
		return getPageUrl(pageName, WikiPageAction.OPENSPACECHAT_ACTION
				.getName());
	}

	/**
	 * @return
	 */
	public String getListPageChatURL()
	{
		return getPageUrl(pageName, WikiPageAction.LISTPAGECHAT_ACTION
				.getName());
	}

	/**
	 * @return
	 */
	public String getListSpaceChatURL()
	{
		return getPageUrl(pageName, WikiPageAction.LISTSPACECHAT_ACTION
				.getName());
	}

	public String getBaseAccessUrl()
	{
		// /wiki 
		return RWikiObjectService.REFERENCE_ROOT + pageName + ".";

	}
	public String getRssAccessUrl()
	{
		// /wiki 
		return RWikiObjectService.REFERENCE_ROOT + getPageSpace() + "/.20.rss";
 	}
	public String getPreferencesUrl()
	{
		return this.getPageUrl(getPageName(), WikiPageAction.PREFERENCES_ACTION
				.getName());
	}


}
