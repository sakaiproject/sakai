/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

public interface ResourceEditingHelper
{
	public static final String CREATE_TYPE = "sakaiproject.filepicker.resourceCreateType";

	public static final String CREATE_TYPE_FORM = "sakaiproject.filepicker.resourceCreateTypeForm";

	public static final String CREATE_SUB_TYPE = "sakaiproject.filepicker.resourceCreateSubType";

	public static final String CREATE_PARENT = "sakaiproject.filepicker.resourceParentCollection";

	/**
	 * if attachment id is missing, must be create mode.
	 */
	public static final String ATTACHMENT_ID = "sakaiproject.filepicker.resourceEditId";

	public static final String CUSTOM_CSS = "sakaiproject.filepicker.customCss";
}
