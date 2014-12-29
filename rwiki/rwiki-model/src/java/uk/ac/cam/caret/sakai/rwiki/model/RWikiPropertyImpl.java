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

import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiProperty;

/**
 * Implementation of an RWiki Property
 * 
 * @author ieb
 */
public class RWikiPropertyImpl implements RWikiProperty
{
	private String id;

	private String name;

	private String value;

	/**
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.api.model.RWikiProperty#getId()
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.api.model.RWikiProperty#setId(java.lang.String)
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.api.model.RWikiProperty#getName()
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.api.model.RWikiProperty#setName(java.lang.String)
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.api.model.RWikiProperty#getValue()
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.api.model.RWikiProperty#setValue(java.lang.String)
	 */
	public void setValue(String value)
	{
		this.value = value;
	}
}
