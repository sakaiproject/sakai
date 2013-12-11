/**********************************************************************************
 * $URL: https://sakai-svn.its.yale.edu/svn/signup/branches/2-6-dev/tool/src/java/org/sakaiproject/signup/tool/jsf/UserLocale.java $
 * $Id: UserLocale.java 4350 2009-07-16 19:14:47Z gl256 $
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

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
