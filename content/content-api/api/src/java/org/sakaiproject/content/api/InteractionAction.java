/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
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


package org.sakaiproject.content.api;

import org.sakaiproject.entity.api.Reference;

/**
 * An InteractionAction defines a kind of ResourceToolAction which involves 
 * user interaction to complete the action.  
 *
 */
public interface InteractionAction extends ResourceToolAction
{
	/**
	 * @param baseServerUrl
	 * @param reference
	 * @return
	 */
	public String getActionUrl(String baseServerUrl, Reference reference);
	
	/**
	 * @param reference
	 */
	public void finalizeAction(Reference reference);
	
	/**
	 * @param reference
	 */
	public void cancelAction(Reference reference);
	
}
