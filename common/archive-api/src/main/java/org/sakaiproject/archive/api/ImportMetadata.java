/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.archive.api;

/**
 * ...
 */
public interface ImportMetadata
{
	/**
	 * @return
	 */
	public String getId();

	/**
	 * @param id
	 */
	public void setId(String id);

	/**
	 * @return Returns the fileName.
	 */
	public String getFileName();

	/**
	 * @param fileName
	 *        The fileName to set.
	 */
	public void setFileName(String fileName);

	/**
	 * @return Returns the legacyTool.
	 */
	public String getLegacyTool();

	/**
	 * @param legacyTool
	 *        The legacyTool to set.
	 */
	public void setLegacyTool(String legacyTool);

	/**
	 * @return Returns the mandatory.
	 */
	public boolean isMandatory();

	/**
	 * @param mandatory
	 *        The mandatory to set.
	 */
	public void setMandatory(boolean mandatory);

	/**
	 * @return Returns the sakaiServiceName.
	 */
	public String getSakaiServiceName();

	/**
	 * @param sakaiServiceName
	 *        The sakaiServiceName to set.
	 */
	public void setSakaiServiceName(String sakaiServiceName);

	/**
	 * @return Returns the sakaiTool.
	 */
	public String getSakaiTool();

	/**
	 * @param sakaiTool
	 *        The sakaiTool to set.
	 */
	public void setSakaiTool(String sakaiTool);
}
