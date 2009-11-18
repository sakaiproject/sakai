/**********************************************************************************
 * $URL$
 * $Id$
***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 Yale University
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
 * See the LICENSE.txt distributed with this file.
 *
 **********************************************************************************/
package org.sakaiproject.signup.logic.messages;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.model.MeetingTypes;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>
 * This is a abstract base class for Signup Email. It provides some must-have or
 * common used methods like getFooter()
 * </P>
 */
abstract public class SignupEmailBase implements SignupEmailNotification, MeetingTypes {

	private SakaiFacade sakaiFacade;
	
	protected SignupMeeting meeting;

	protected static ResourceLoader rb = new ResourceLoader("emailMessage");

	public static final String newline = "<BR>\r\n"; // System.getProperty("line.separator");\r\n

	public static final String space = " ";
	
	private static final int SITE_DESCRIPTION_DISPLAY_LENGTH=20;

	/* footer for the email */
	protected String getFooter(String newline) {
		/* tag the message - HTML version */
		if(this.meeting.getCurrentSiteId()==null)
			return getFooterWithAccessUrl(newline);
		else
			return getFooterWithNoAccessUrl(newline);
	}

	/* footer for the email */
	protected String getFooter(String newline, String targetSiteId) {
		/* tag the message - HTML version */
		Object[] params = new Object[] { getServiceName(),
				"<a href=\"" + getSiteAccessUrl(targetSiteId) + "\">" + getSiteAccessUrl(targetSiteId) + "</a>",
				getSiteTitle(targetSiteId), newline };
		String rv = newline + rb.getString("separator") + newline
				+ MessageFormat.format(rb.getString("body.footer.text"), params) + newline;

		return rv;
	}
	
	/* footer for the email */
	private String getFooterWithAccessUrl(String newline) {
		/* tag the message - HTML version */
		Object[] params = new Object[] { getServiceName(),
				"<a href=\"" + getSiteAccessUrl() + "\">" + getSiteAccessUrl() + "</a>", getSiteTitle(), newline };
		String rv = newline + rb.getString("separator") + newline
				+ MessageFormat.format(rb.getString("body.footer.text"), params) + newline;

		return rv;
	}
	
	/* footer for the email */
	private String getFooterWithNoAccessUrl(String newline) {
		/* tag the message - HTML version */
		Object[] params = new Object[] { getServiceName(),
				getSiteTitle(), newline };
		String rv = newline + rb.getString("separator") + newline
				+ MessageFormat.format(rb.getString("body.footer.text.no.access.link"), params) + newline;

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
		String siteId = getSakaiFacade().getCurrentLocationId();
		if(SakaiFacade.NO_LOCATION.equals(siteId)){
			siteId =meeting.getCurrentSiteId()!=null? this.meeting.getCurrentSiteId() : SakaiFacade.NO_LOCATION;			
		}
		
		return siteId;
	}

	/* get the site name */
	protected String getSiteTitle() {
		String title = getSakaiFacade().getLocationTitle(getSiteId());
		title +=getShortDescription(getSiteId());
		return title;
	}

	/* get the site name */
	protected String getSiteTitle(String targetSiteId) {
		return getSakaiFacade().getLocationTitle(targetSiteId) + getShortDescription(targetSiteId);
	}

	/* get the site name with a quotation mark */
	protected String getSiteTitleWithQuote() {
		return "\"" + getSiteTitle() + "\"";
	}

	/* get the site name with a quotation mark */
	protected String getSiteTitleWithQuote(String targetSiteId) {
		return "\"" + getSiteTitle(targetSiteId) + "\"";
	}

	/* get the link to access the current-site signup tool page in a site */
	protected String getSiteAccessUrl() {
		// TODO May have efficiency issue with getPageId
		String siteUrl = getSakaiFacade().getServerConfigurationService().getPortalUrl() + "/site/" + getSiteId()
				+ "/page/" + getSakaiFacade().getCurrentPageId();
		return siteUrl;
	}

	/* get the link to access corresponding site - signup tool page in a site */
	protected String getSiteAccessUrl(String targetSiteId) {
		// TODO May have efficiency issue with getPageId
		String siteUrl = getSakaiFacade().getServerConfigurationService().getPortalUrl() + "/site/" + targetSiteId
				+ "/page/" + getSakaiFacade().getSiteSignupPageId(targetSiteId);
		return siteUrl;
	}

	/**
	 * This will convert the Java date object to a Sakai's Time object, which
	 * provides all the useful methods for output.
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

	static private String myServiceName = null;

	protected String getServiceName() {
		/* first look at email bundle and then sakai.properties. 
		 * it will allow user to define different 'ui.service' value */
		if (myServiceName == null) {
			try {
				myServiceName = rb.getString("ui.service");
				int index = myServiceName.indexOf("missing key");/*return value by rb if no key defined-- hard coded!!!*/
				if (index >=0)
					myServiceName = getSakaiFacade().getServerConfigurationService().getString("ui.service",
							"Sakai Service");
			} catch (Exception e) {
				myServiceName = getSakaiFacade().getServerConfigurationService().getString("ui.service",
						"Sakai Service");
			}
		}

		return myServiceName;

	}
	
	private String getShortDescription(String siteId)
    {
		Site site;
		try {
			site = SiteService.getSite(siteId);
		} catch (IdUnusedException e) {
			return "";
		}
        String shortDescription = "";
        if (site.getShortDescription() != null && !"".equals(site.getShortDescription()))
        {
            shortDescription = site.getShortDescription();
            if (shortDescription.length() > SITE_DESCRIPTION_DISPLAY_LENGTH) shortDescription = shortDescription.substring(0, 20);
            shortDescription = ": " + shortDescription;
        }
        return shortDescription;
    }
	
	protected String getRepeatTypeMessage(SignupMeeting meeting){
		String recurFrqs ="";
		if (DAILY.equals(meeting.getRepeatType()))
			recurFrqs = rb.getString("body.meeting.repeatDaily");
		else if (WEEKDAYS.equals(meeting.getRepeatType()))
			recurFrqs = rb.getString("body.meeting.repeatWeekdays");
		else if (WEEKLY.equals(meeting.getRepeatType()))
			recurFrqs = rb.getString("body.meeting.repeatWeekly");
		else if (BIWEEKLY.equals(meeting.getRepeatType()))
			recurFrqs = rb.getString("body.meeting.repeatBiWeekly");
		else
			recurFrqs = rb.getString("body.meeting.unknown.repeatType");
		
		return recurFrqs;
	}
	
}
