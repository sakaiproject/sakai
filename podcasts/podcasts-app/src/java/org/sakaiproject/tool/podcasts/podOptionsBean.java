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

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.sakaiproject.api.app.podcasts.PodcastService;

public class podOptionsBean {
  private int podOption;
  
  private PodcastService podcastService;
  
  private static final int PUBLIC = 0;
  private static final int SITE = 1;
  private static final String OPTIONS_PUBLIC = "options_public";
  private static final String OPTIONS_SITE = "options_site";
  
  private SelectItem [] displayItems = new SelectItem [] {
    new SelectItem(new Integer(PUBLIC), getMessageString(OPTIONS_PUBLIC)),
    new SelectItem(new Integer(SITE), getMessageString(OPTIONS_SITE))
  };
  
  public podOptionsBean () {
	  podOption = 0;
  }
  
  public podOptionsBean (int option) {
	  podOption = option;
  }
  
  public int getPodOption() {
	  //TODO: get option from Resources
	  return podcastService.getOptions();
  }
  
  public void setPodOption(int option) {
	  podOption = option;
  }

  public SelectItem [] getDisplayItems () {
	  return displayItems;
  }
  
  public String processOptionChange() {
	  
	  podcastService.reviseOptions(podOption == PUBLIC);
	  
	  return "cancel";
  }
  
  public String processOptionCancel() {
	  return "cancel";
  }
  
	/**
	 * Sets the Faces error message by pulling the message from the MessageBundle using
	 * the name passed in
	 * 
	 * @param key The name in the MessageBundle for the message wanted
	 * @return The string that is the value of the message
	 */
	private String getMessageString(String key) {
      String bundleName = FacesContext.getCurrentInstance().getApplication().getMessageBundle();
      Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();        
      ResourceBundle rb = ResourceBundle.getBundle(bundleName, locale);
      return rb.getString(key);
		
	}

	/**
	 * @return Returns the podcastService.
	 */
	public PodcastService getPodcastService() {
		return podcastService;
	}

	/**
	 * @param podcastService The podcastService to set.
	 */
	public void setPodcastService(PodcastService podcastService) {
		this.podcastService = podcastService;
	}

}
