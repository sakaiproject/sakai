/**********************************************************************************
 * $URL$
 * $Id$
***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Yale University
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
package org.sakaiproject.signup.logic.messages;

import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.model.MeetingTypes;
import org.sakaiproject.time.api.Time;

/**
 * <p>
 * This is a abstract base class for Signup Email. It provides some must-have or
 * common used methods like getFooter()
 * </P>
 */
abstract public class SignupEmailBase implements SignupEmailNotification, MeetingTypes {

	private SakaiFacade sakaiFacade;

	protected static ResourceBundle rb = ResourceBundle.getBundle("emailMessage");

	public static final String newline = "<br/>\n"; // System.getProperty("line.separator");

	public static final String space = " ";

	/* footer for the email */
	protected String getFooter(String newline) {
		// tag the message - HTML version
		String rv = newline + rb.getString("separator") + newline + rb.getString("this") + space
				+ sakaiFacade.getServerConfigurationService().getString("ui.service", "Sakai") + " (<a href=\""
				+ getSiteAccessUrl() + "\">" + getSiteAccessUrl() + "</a>) " + rb.getString("forthe") + space
				+ getSiteTitle() + space + rb.getString("site") + newline + rb.getString("youcan") + newline;
		return rv;
	}

	/**
	 * get the email Header, which contains destination email address, subject
	 * etc.
	 */
	abstract public List<String> getHeader();

	/**
	 * get the main message for this email
	 */
	abstract public String getMessage();

	/**
	 * get SakaiFacade object
	 * 
	 * @return SakaiFacade object
	 */
	public SakaiFacade getSakaiFacade() {
		return sakaiFacade;
	}

	/**
	 * this is a setter
	 * 
	 * @param sakaiFacade
	 *            SakaiFacade object
	 */
	public void setSakaiFacade(SakaiFacade sakaiFacade) {
		this.sakaiFacade = sakaiFacade;
	}

	/**
	 * get current site Id
	 * 
	 * @return the current site Id
	 */
	protected String getSiteId() {
		return getSakaiFacade().getCurrentLocationId();
	}

	/* get the site name */
	protected String getSiteTitle() {
		return getSakaiFacade().getLocationTitle(getSiteId());
	}

	/* get the link to access the signup tool page in a site */
	protected String getSiteAccessUrl() {
		// TODO May have efficiency issue with getPageId
		String siteUrl = getSakaiFacade().getServerConfigurationService().getPortalUrl() + "/site/" + getSiteId()
				+ "/page/" + getSakaiFacade().getCurrentPageId();
		return siteUrl;
	}

	/**
	 * This will convert the Java date object to a Sakai's Time object, which
	 * provides all the usefull methods for output.
	 * 
	 * @param date
	 *            a Java Date object.
	 * @return a Sakai's Time object.
	 */
	protected Time getTime(Date date) {
		Time time = getSakaiFacade().getTimeService().newTime(date.getTime());
		return time;
	}

	/**
	 * Make first letter of the string to Capital letter
	 * 
	 * @param st
	 *            a string value
	 * @return a string with a first capital letter
	 */
	protected String makeFirstCapLetter(String st) {
		String temp = "";
		if (st != null && st.length() > 0)
			temp = st.substring(0, 1).toUpperCase() + st.substring(1);

		return temp;
	}
}
