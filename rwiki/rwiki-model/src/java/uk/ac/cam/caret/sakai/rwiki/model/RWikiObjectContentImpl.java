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
package uk.ac.cam.caret.sakai.rwiki.model;

import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObjectContent;


public class RWikiObjectContentImpl implements RWikiObjectContent
{

	protected String rwikiid;

	protected String id;

	protected String content;

	public String getRwikiid()
	{
		return rwikiid;
	}

	public void setRwikiid(String rwikiid)
	{
		this.rwikiid = rwikiid;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getContent()
	{
		if ( content == null ) return ""; // SAK-20790, do not change the internal representation of the object.
		return content;
	}

	public void setContent(String content)
	{
		// SAK-2470
		if (content == null) content = "";
		this.content= content.replaceAll("[\\x00-\\x08\\x0b\\x0c\\x0e-\\x1f\\ud800-\\udfff\\uffff\\ufffe]", "");
	}

}
