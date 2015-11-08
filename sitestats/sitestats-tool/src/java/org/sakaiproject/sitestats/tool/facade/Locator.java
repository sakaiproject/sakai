/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.facade;

import org.apache.wicket.Application;
import org.sakaiproject.sitestats.tool.wicket.SiteStatsApplication;

public class Locator {

	private static transient SakaiFacade facade;
	
	public static SakaiFacade getFacade() {
		if(facade == null) {
			facade = ((SiteStatsApplication) Application.get()).getFacade();
		}
		return facade;
	}
	
}
