/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.signup.tool.jsf;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>
 * This class will provides methods for manage Locale issues
 * </P>
 * 
 * @author Peter Liu
 */

public class UserLocale {

	private ResourceLoader rb = new ResourceLoader("messages");
	private SakaiFacade sakaiFacade;

	public String getLocale() {
		return (String) this.rb.getLocale().toString();
	}

	public SakaiFacade getSakaiFacade() {
		return sakaiFacade;
	}

	public void setSakaiFacade(SakaiFacade sakaiFacade) {
		this.sakaiFacade = sakaiFacade;
	}
	
	/**
	 * Get the date format from the locale
	 * @return
	 */
	public String getDateFormat() {
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, (new ResourceLoader()).getLocale());
		return ((SimpleDateFormat)df).toPattern();
	}

	/**
	 * Get the date format from the locale with short timezone at end
	 * @return String representing a SimpleDateFormat for use with JSF convertDateTime
	 */
	public String getFullDateTimeFormat() {
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, this.rb.getLocale());
		return ((SimpleDateFormat)df).toPattern();
	}
	
	/**
	 * Get the time format from the locale 
	 * @return
	 */
	public String getLocalizedTimeFormat() {
		DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, this.rb.getLocale());
		String dfPattern = ((SimpleDateFormat)df).toPattern();

		TimeZone userTimeZone = sakaiFacade.getTimeService().getLocalTimeZone();
		TimeZone serverTimeZone = TimeZone.getDefault();

		// If the user is in a different zone, it would be super helpful to show them the timezone info
		if (userTimeZone != null && !userTimeZone.hasSameRules(serverTimeZone)) {
			dfPattern += " z";
		}

		return dfPattern;
	}
}
