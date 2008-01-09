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

import org.adl.datamodels.IDataManager;

public interface ScoBean extends Serializable {

	public static final int SCO_VERSION_2 = 2;
	public static final int SCO_VERSION_3 = 3;
	
	public abstract void clearState();
	
	public abstract String getScoId();
	
	public abstract boolean isInitialized();
	
	public abstract void setInitialized(boolean isInitialized);
	
	public abstract boolean isSuspended();
	
	public abstract void setSuspended(boolean isSuspended);
	
	public abstract boolean isTerminated();
	
	public abstract void setTerminated(boolean isTerminated);
	
	public abstract void setVersion(int version);
	
	public abstract IDataManager getDataManager();
	
	public abstract void setDataManager(IDataManager dataManager);
	
	/*public abstract String Commit(String parameter);

	public abstract String GetDiagnostic(String iErrorCode);

	public abstract String GetErrorString(String iErrorCode);

	public abstract String GetLastError();

	public abstract String GetValue(String parameter);

	public abstract String Initialize(String parameter);

	public abstract String SetValue(String dataModelElement, String value);

	public abstract String Terminate(String iParam);

	public abstract String Terminate(String iParam, IRefreshable agent, Object target);*/

}