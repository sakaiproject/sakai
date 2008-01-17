/**********************************************************************************
 * $URL$
 * $Id: podOptionsBean.java 14691 2006-09-15 12:36:27Z josrodri@iupui.edu$
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

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.sakaiproject.api.app.podcasts.PodcastService;
import org.sakaiproject.util.ResourceLoader;

public class podOptionsBean {
  private int podOption;

  /** Used to acccess podcast service functions */
  private PodcastService podcastService;
  
  /** Used to access message bundle */
  private static ResourceLoader rb;

  private static final int PUBLIC = 0;
  private static final int SITE = 1;
  private static final String OPTIONS_PUBLIC = "options_public";
  private static final String OPTIONS_SITE = "options_site";
  private static final String CHANGE_TO_SITE = "option_change_confirm";
  
  private SelectItem [] displayItems = new SelectItem [] {
    new SelectItem(new Integer(PUBLIC), getMessageString(OPTIONS_PUBLIC)),
    new SelectItem(new Integer(SITE), getMessageString(OPTIONS_SITE))
  };
  
  public podOptionsBean() {
		podOption = 0;
	}

  	/** Returns whether podcast folder is PUBLIC (0) or SITE (1) **/
	public int getPodOption() {
		return podcastService.getOptions();
	}

	/** Set whether the podcast folder is PUBLIC (0) or SITE (1) **/
	public void setPodOption(int option) {
		podOption = option;
	}

	/** Returns the options of what the podcast folder can be set to **/
	public SelectItem[] getDisplayItems() {
		return displayItems;
	}

	/**
	 * Set the podcast folder to either:
	 * 	0	Display to non-members (PUBLIC)
	 *  1	Display to Site (SITE)
	 * @return String
	 * 			Used for navigation to go back to main page
	 */
	public String processOptionChange() {

		podcastService.reviseOptions(podOption == PUBLIC);

		if (podOption == SITE) {
			// Set the display message because changed to Display to Site
			setErrorMessage(CHANGE_TO_SITE);

		}

		return "cancel";
	}

	/** Returns back to main page with no changes **/
	public String processOptionCancel() {
		return "cancel";
	}

	/**
	 * Returns the message pulled from the MessageBundle using
	 * the name passed in
	 * 
	 * @param key 
	 * 			The name in the MessageBundle for the message wanted
	 * @return String
	 * 			The string that is the value of the message
	 */
	private String getMessageString(String key) {
		if (rb == null) {
	          String bundle = FacesContext.getCurrentInstance().getApplication().getMessageBundle();
	          rb = new ResourceLoader(bundle);
		}
		return rb.getString(key);
	}

	/**
	 * @param podcastService The podcastService to set.
	 */
	public void setPodcastService(PodcastService podcastService) {
		this.podcastService = podcastService;
	}

	/**
	 * Passes an error message to the Spring framework to display on page.
	 * 
	 * @param alertMsg The key to get the message from the message bundle 
	 */
	private void setErrorMessage(String alertMsg) {
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage("Alert: " + getMessageString(alertMsg)));
	}

}
