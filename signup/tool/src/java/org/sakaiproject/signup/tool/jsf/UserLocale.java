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

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

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

	public String getLocale() {
		return (String) this.rb.getLocale().toString();
	}

	/*
	 * When user uses IE browser, due to Tomhwak 1.1.9 bug for display Chinese
	 * character in t:inputDate tag. it will return a default 'en' for following
	 * countries: china:CN, Taiwan:TW, [Japan:JP and Korea:KR too?] Once Tomhwak
	 * fixes this issue, REMOVE IT.
	 * 
	 * Just found out that it's not Tomhwak issue, it's IE browser issue, it's
	 * not working for any Drop-Down box. All other sakai tool this the same
	 * issue except Resource by using JQuery stuff.
	 */
	public String getLocaleExcludeCountryForIE() {
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext ext = context.getExternalContext();
		String userAgent = (String) ext.getRequestHeaderMap().get("User-Agent");// MSIE
		if (userAgent != null) {
			int index = userAgent.indexOf("MSIE");
			if (index >= 0 && isExcludedCountry()) {
				return rb.getLocale().US.toString();
			}
		}

		return (String) rb.getLocale().toString();
	}

	/*
	 * should exclude country like china:CN, Taiwan:TW, Japan:JP and Korea:KR
	 * due to Chinese character bug in currently Tomhwak 1.1.9 version for IE
	 * browser 7.0 etc.
	 */
	private boolean isExcludedCountry() {
		String cur_country = rb.getLocale().getCountry();
		if ("CN".equals(cur_country) || "TW".equals(cur_country)) { // JP,KR ?
			return true;
		}

		return false;
	}
	
	/**
	 * Get the date format from the locale
	 * @return
	 */
	public String getDateFormat() {
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, (new ResourceLoader()).getLocale());
		return ((SimpleDateFormat)df).toPattern();
	}
}
