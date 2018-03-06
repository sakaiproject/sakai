/**
 * Copyright (c) 2003-2011 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.site.tool.helper.managegroupsectionrole.rsf;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

/**
* 
* Params for the group import process
*
*/
public class GroupImportViewParameters extends SimpleViewParameters {
    
	public String status;
		
		
	public GroupImportViewParameters(String viewID, String status) {
		this.status= status;
		this.viewID = viewID;
	}
	
	
	public GroupImportViewParameters(String status) {
		super();
		this.status = status;
	}
	
	
	public GroupImportViewParameters() {
		super();
	}   
}
