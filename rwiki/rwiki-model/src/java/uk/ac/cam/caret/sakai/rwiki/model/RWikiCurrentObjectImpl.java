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

import java.util.List;

import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiCurrentObject;

/**
 * @author ieb
 */
public class RWikiCurrentObjectImpl extends RWikiObjectImpl implements
		RWikiCurrentObject
{

	List targetSiteTypes = null;

	public void setRwikiobjectid(String rwikiobjectid)
	{
		throw new RuntimeException(
				"It is not possible to set the RWikiObject ID on the current version, it is the ID ");
	}

	public String getRwikiobjectid()
	{
		return getId();
	}

	/**
	 * @return Returns the targetSiteTypes.
	 */
	public List getTargetSiteTypes()
	{
		return targetSiteTypes;
	}

	/**
	 * @param targetSiteTypes
	 *        The targetSiteTypes to set.
	 */
	public void setTargetSiteTypes(List targetSiteTypes)
	{
		this.targetSiteTypes = targetSiteTypes;
	}
}
