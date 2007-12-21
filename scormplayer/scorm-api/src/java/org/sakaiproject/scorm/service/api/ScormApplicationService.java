/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
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
package org.sakaiproject.scorm.service.api;

import org.sakaiproject.scorm.model.api.ScoBean;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.navigation.INavigable;
import org.sakaiproject.scorm.navigation.INavigationEvent;

public interface ScormApplicationService {

	public boolean commit(String parameter, SessionBean sessionBean, ScoBean scoBean);
	
	public String getDiagnostic(String errorCode, SessionBean sessionBean);
	
	public String getErrorString(String iErrorCode, SessionBean sessionBean);
	
	public String getLastError(SessionBean sessionBean);
	
	public String getValue(String parameter, SessionBean sessionBean, ScoBean scoBean);
	
	public boolean initialize(String parameter, SessionBean sessionBean, ScoBean scoBean);
	
	public boolean setValue(String dataModelElement, String value, SessionBean sessionBean, 
			ScoBean scoBean);
	
	public boolean terminate(String iParam, INavigationEvent navigationEvent, SessionBean sessionBean, 
			ScoBean scoBean);	
	
	public INavigationEvent newNavigationEvent();
	
	public ScoBean produceScoBean(String scoId, SessionBean sessionBean);
	
	public void discardScoBean(String scoId, SessionBean sessionBean, INavigable agent);
	
}
