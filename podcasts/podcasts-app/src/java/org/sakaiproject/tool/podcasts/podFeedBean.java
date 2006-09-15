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


package org.sakaiproject.tool.podcasts;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.sakaiproject.api.app.podcasts.PodfeedService;

public class podFeedBean {

	/** Used to pull messages from the bundle **/
	public static final String PODFEED_TITLE_MSG = "feed_title";
	public static final String PODFEED_DESC1 = "feed_desc1";
	public static final String PODFEED_DESC2 = "feed_desc2";
	public static final String NO_TITLE_ALERT = "notitle";

	// inject podfeedService into here to use
	private PodfeedService podfeedService;

	// Used by podFeedRevise for global feed info
	private String podfeedTitle;
	private String podfeedDescription;

	/**
	 * Returns the global podcast title
	 * 
	 * @return String 
	 *		The global podcast title
	 */
	public String getPodfeedTitle() {
		podfeedTitle = podfeedService.getPodfeedTitle();

		return podfeedTitle;
	}

	/**
	 * Setter for global podcast title
	 * 
	 * @param podFeedTitle 
	 * 				The podFeedTitle to set.
	 */
	public void setPodfeedTitle(String podFeedTitle) {
		this.podfeedTitle = podFeedTitle;

	}

	/**
	 * Returns the global podcast description
	 * 
	 * @return String
	 * 				Returns the podFeedDescription.
	 */
	public String getPodfeedDescription() {
		podfeedDescription = podfeedService.getPodfeedDescription();

		return podfeedDescription;
	}

	/**
	 * Setter for the global podcast description.
	 * 
	 * @param podFeedDescription 
	 * 				The podFeedDescription to set.
	 */
	public void setPodfeedDescription(String podFeedDescription) {
		this.podfeedDescription = podFeedDescription;

	}

	/**
	 * Determine if revisions made to global feed info and apply changes.
	 * 
	 * @return String to control navigation
	 */
	public String processRevisePodcast() {
		String whereToGo = "cancel";
		
		final String oldTitle = podfeedService.getPodfeedTitle();
		
		if (podfeedTitle != null && !podfeedTitle.equals(oldTitle)) {
			if (podfeedTitle.equals("")) {
				setErrorMessage(NO_TITLE_ALERT);
				whereToGo = "podfeedRevise";
			}
			else {
				// Replace with this title
				podfeedService.setPodfeedTitle(podfeedTitle);
			}
		}

		final String oldDescription = podfeedService.getPodfeedDescription();
		
		if (podfeedDescription != null && !podfeedDescription.equals(oldDescription)) {
			// Replace with this description
			podfeedService.setPodfeedDescription(podfeedDescription);
		}

		return whereToGo;
	}

	/**
	 * Cancels revising global podfeed information
	 * 
	 * @return String Sent to return to the main page
	 */
	public String processCancelPodfeedRevise() {
		podfeedTitle = "";
		podfeedDescription = "";

		return "cancel";
	}

	/**
	 * @return Returns the podfeedService.
	 */
	public PodfeedService getPodfeedService() {
		return podfeedService;
	}

	/**
	 * @param podfeedService The podfeedService to set.
	 */
	public void setPodfeedService(PodfeedService podfeedService) {
		this.podfeedService = podfeedService;
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
		String bundleName = FacesContext.getCurrentInstance().getApplication()
				.getMessageBundle();
		Locale locale = FacesContext.getCurrentInstance().getViewRoot()
				.getLocale();
		ResourceBundle rb = ResourceBundle.getBundle(bundleName, locale);
		return rb.getString(key);

	}

}
