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

import org.sakaiproject.entity.api.Edit;

/**
* <p>ContentResource is an editable ContentResource.</p>
*/
public interface ContentResourceEdit
	extends ContentResource, Edit
{
	/**
	* Set the content byte length.
	* @param length The content byte length.
	*/
	public void setContentLength(int length);

	/**
	* Set the resource MIME type.
	* @param type The resource MIME type.
	*/
	public void setContentType(String type);

	/**
	* Set the resource content.
	* @param content An array containing the bytes of the resource's content.
	*/
	public void setContent(byte[] content);

}	// ContentResourceEdit



