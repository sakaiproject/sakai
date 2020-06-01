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
import java.util.Locale;

import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.util.ResourceLoader;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * This class will provides methods for manage Locale issues
 * </P>
 * 
 * @author Peter Liu
 */

@Slf4j
public class UserLocale {

	private ResourceLoader rb = new ResourceLoader("messages");

	@Getter @Setter private SakaiFacade sakaiFacade;

	public String getLocale() {
		return (String) this.rb.getLocale().toString();
	}
	
	/**
	 * Get the date format from the locale
	 * @return
	 */
	public String getDateFormat() {

		Locale locale = new ResourceLoader().getLocale();
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		try {
			return ((SimpleDateFormat)df).toPattern();
		} catch (ClassCastException cce) {
			log.warn("Failed to cast DateFormat into SimpleDateFormat for locale {}", locale.toString());
			return new SimpleDateFormat().toPattern();
		}
	}

	/**
	 * Get the date format from the locale
	 * @return String representing a SimpleDateFormat for use with JSF convertDateTime
	 */
	public String getFullDateTimeFormat() {

		Locale locale = this.rb.getLocale();
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, locale);
		try {
			return ((SimpleDateFormat) df).toPattern();
		} catch (ClassCastException cce) {
			log.warn("Failed to cast DateFormat into SimpleDateFormat for locale {}", locale.toString());
			return new SimpleDateFormat().toPattern();
		}
	}
	
	/**
	 * Get the time format from the locale 
	 * @return
	 */
	public String getLocalizedTimeFormat() {

		Locale locale = this.rb.getLocale();
		DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
		try {
			return ((SimpleDateFormat) df).toPattern();
		} catch (ClassCastException cce) {
			log.warn("Failed to cast DateFormat into SimpleDateFormat for locale {}", locale.toString());
			return new SimpleDateFormat().toPattern();
		}
	}
}
