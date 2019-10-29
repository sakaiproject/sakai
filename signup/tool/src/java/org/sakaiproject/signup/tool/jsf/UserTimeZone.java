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

public class UserTimeZone {

	private ResourceLoader rb = new ResourceLoader("messages");
	private SakaiFacade sakaiFacade;

	public SakaiFacade getSakaiFacade() {
		return sakaiFacade;
	}

	public void setSakaiFacade(SakaiFacade sakaiFacade) {
		this.sakaiFacade = sakaiFacade;
	}

	public TimeZone getUserTimeZone(){
		return sakaiFacade.getTimeService().getLocalTimeZone();
	}
	
	public String getUserTimeZoneStr(){
		return sakaiFacade.getTimeService().getLocalTimeZone().getID();
	}
	
}
