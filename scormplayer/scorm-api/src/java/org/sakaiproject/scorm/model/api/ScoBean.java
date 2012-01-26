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
package org.sakaiproject.scorm.model.api;

import java.io.Serializable;

public interface ScoBean extends Serializable {

	public static final int SCO_VERSION_2 = 2;
	public static final int SCO_VERSION_3 = 3;
	
	public void clearState();
	
	public String getScoId();
	
	public boolean isInitialized();
	
	public void setInitialized(boolean isInitialized);
	
	public boolean isSuspended();
	
	public void setSuspended(boolean isSuspended);
	
	public boolean isTerminated();
	
	public void setTerminated(boolean isTerminated);
	
	public void setVersion(int version);
	
	public Long getDataManagerId();
	
	public void setDataManagerId(Long id);
	
	/*public String Commit(String parameter);

	public String GetDiagnostic(String iErrorCode);

	public String GetErrorString(String iErrorCode);

	public String GetLastError();

	public String GetValue(String parameter);

	public String Initialize(String parameter);

	public String SetValue(String dataModelElement, String value);

	public String Terminate(String iParam);

	public String Terminate(String iParam, IRefreshable agent, Object target);*/

}