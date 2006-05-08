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

package uk.ac.cam.caret.sakai.rwiki.service.api.model;

// FIXME: Service

public interface RWikiObjectContent
{
	/**
	 * The Id of the content
	 * 
	 * @return
	 */
	String getRwikiid();

	/**
	 * The Id of the content
	 * 
	 * @param rwikiid
	 */
	void setRwikiid(String rwikiid);

	/**
	 * The record ID
	 * 
	 * @return
	 */
	String getId();

	/**
	 * The record ID
	 * 
	 * @param id
	 */
	void setId(String id);

	/**
	 * The content
	 * 
	 * @return
	 */
	String getContent();

	/**
	 * The content
	 * 
	 * @param content
	 */
	void setContent(String content);

}
