/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
package uk.ac.cam.caret.sakai.rwiki.tool.bean;

import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.tool.util.WikiPageAction;

/**
 * General value bean for creating history views.
 * 
 * @author andrew
 */

// FIXME: Tool
public class HistoryBean extends ViewBean
{

	/**
	 * Parameter name for version. This parameter is sent when reverting a
	 * RWikiObject. It is used to know when we have a
	 * <code>VersionException</code
	 */
	public static final String VERSION_PARAM = "version";

	/**
	 * Parameter name for requesting a left version for a diff. This parameter
	 * is sent when requesting a diff.
	 */
	public static final String LEFT_PARAM = "left";

	/**
	 * Parameter name for requesting a right version for a diff. This parameter
	 * is sent when requesting a diff.
	 */
	public static final String RIGHT_PARAM = "right";

	/**
	 * Parameter name for requesting a revision. This parameter is sent when
	 * requesting a review of a particular revision or a reversion.
	 */
	public static final String REVISION_PARAM = "revision";

	private static final String LEFT_URL_ENCODED = urlEncode(LEFT_PARAM);

	private static final String RIGHT_URL_ENCODED = urlEncode(RIGHT_PARAM);

	private static final String VERSION_URL_ENCODED = urlEncode(VERSION_PARAM);

	private static final String REVISION_URL_ENCODED = urlEncode(REVISION_PARAM);

	/**
	 * The revision number that getRevert and getViewRevision refer to
	 */
	private int interestedRevision;

	/**
	 * The current version of the rwikiObject that is being refered to as a
	 * long.
	 */
	private long time;

	public HistoryBean()
	{
		// Must have null constructor!
		super();
	}

	/**
	 * Creates a HistoryBean using the rwikiObject to fill most of it's fields
	 * 
	 * @param rwikiObject
	 *        exemplar
	 * @param defaultRealm
	 *        the defaultRealm
	 */
	public HistoryBean(RWikiObject rwikiObject, String defaultRealm)
	{
		super(rwikiObject.getName(), defaultRealm);
		this.interestedRevision = rwikiObject.getRevision().intValue();
		this.time = rwikiObject.getVersion().getTime();
	}

	/**
	 * Creates a HistoryBean
	 * 
	 * @param pageName
	 * @param defaultRealm
	 * @param defaultInterestedRevision
	 * @param time
	 */
	public HistoryBean(String pageName, String defaultRealm,
			int defaultInterestedRevision, long time)
	{
		super(pageName, defaultRealm);
		this.interestedRevision = defaultInterestedRevision;
		this.time = time;
	}

	/**
	 * Using the currently set interestedRevision returns an url that will
	 * generate a diff to the current revision from the interested revision
	 * 
	 * @return url as String
	 */
	public String getDiffToCurrentUrl()
	{
		return "?" + PAGENAME_URL_ENCODED + "=" + urlEncode(getPageName())
				+ "&" + ACTION_URL_ENCODED + "="
				+ urlEncode(WikiPageAction.DIFF_ACTION.getName()) + "&"
				+ PANEL_URL_ENCODED + "=" + MAIN_URL_ENCODED + "&"
				+ LEFT_URL_ENCODED + "=" + interestedRevision;
	}

	/**
	 * Using the currently set interestedRevision returns an url that will
	 * generate a diff from that revision to it's previous revision.
	 * 
	 * @return url as String
	 */
	public String getDiffToPreviousUrl()
	{
		return "?" + PAGENAME_URL_ENCODED + "=" + urlEncode(getPageName())
				+ "&" + ACTION_URL_ENCODED + "="
				+ urlEncode(WikiPageAction.DIFF_ACTION.getName()) + "&"
				+ PANEL_URL_ENCODED + "=" + MAIN_URL_ENCODED + "&"
				+ RIGHT_URL_ENCODED + "=" + interestedRevision;
	}

	/**
	 * Using the currently set interestedRevision returns an url that will
	 * generate a view of that revision.
	 * 
	 * @return url as String
	 */
	public String getViewRevisionUrl()
	{
		return "?" + PAGENAME_URL_ENCODED + "=" + urlEncode(getPageName())
				+ "&" + ACTION_URL_ENCODED + "="
				+ urlEncode(WikiPageAction.REVIEW_ACTION.getName()) + "&"
				+ PANEL_URL_ENCODED + "=" + MAIN_URL_ENCODED + "&"
				+ HistoryBean.REVISION_URL_ENCODED + "=" + interestedRevision;

	}

	/**
	 * Using the currently set interestedRevision returns an url that will cause
	 * a revert to that revision.
	 * 
	 * @return url as String
	 */
	public String getRevertToRevisionUrl()
	{
		return "?" + PAGENAME_URL_ENCODED + "=" + urlEncode(getPageName())
				+ "&" + ACTION_URL_ENCODED + "="
				+ urlEncode(WikiPageAction.REVERT_ACTION.getName()) + "&"
				+ PANEL_URL_ENCODED + "=" + MAIN_URL_ENCODED + "&"
				+ HistoryBean.REVISION_URL_ENCODED + "=" + interestedRevision
				+ "&" + VERSION_URL_ENCODED + "=" + urlEncode("" + time);
	}

	/**
	 * The currently set interestedRevision
	 * 
	 * @return current interestedRevision
	 */
	public int getInterestedRevision()
	{
		return interestedRevision;
	}

	/**
	 * Sets the currently interestedRevision
	 * 
	 * @param interestedRevision
	 *        to set
	 */
	public void setInterestedRevision(int interestedRevision)
	{
		this.interestedRevision = interestedRevision;
	}

}
