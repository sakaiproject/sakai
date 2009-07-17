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
package org.sakaiproject.signup.tool.jsf;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.sakaiproject.util.ResourceLoader;

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
}
