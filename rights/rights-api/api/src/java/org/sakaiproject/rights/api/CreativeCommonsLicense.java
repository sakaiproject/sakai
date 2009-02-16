/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.rights.api;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Creative Commons Licenses are described by their characteristics, which come in three types:
 * 
 *	Permissions (rights granted by the license)
 *	Prohibitions (things prohibited by the license)
 *	Requirements (restrictions imposed by the license)
 *
  */
public interface CreativeCommonsLicense 
{
	public String getIdentifier();
	
	public String getUri();
	public void setUri(String uri);
	
	public String getLegalcode();
	public void setLegalcode(String url);
	
	public String getTitle();
	public String getDescription();
	
	public String getCreator();
	public void setCreator(String creator);
	
	public String getVersion();
	public void setVersion(String version);
	
	public String getJurisdiction();
	public void setJurisdiction(String jurisdiction);
	
	public String getSource();
	public void setSource(String source);
	
	public String getReplacedBy();
	public void setReplacedBy(String replacement);
	public boolean isReplaced();
	
	/*****************************************************
	 * Permissions
	 *****************************************************/
	
	/**
	 * @return
	 */
	public boolean hasPermissions();
	
	/**
	 * @return
	 */
	public Collection<String> getPermissions();
	
	/**
	 * @param permission
	 */
	public void addPermission(String permission);
	
	/**
	 * @param permissions
	 */
	public void addPermissions(Set<String> permissions);
	
	/*****************************************************
	 * Prohibitions
	 *****************************************************/

	/**
	 * @return
	 */
	public boolean hasProhibitions();
	
	/**
	 * @return
	 */
	public Collection<String> getProhibitions();
	
	/**
	 * @param prohibition
	 */
	public void addProhibition(String prohibition);
	
	/**
	 * @param prohibition
	 */
	public void addProhibitions(Set<String> prohibitions);
	
	/*****************************************************
	 * Requirements
	 *****************************************************/
	
	/**
	 * @return
	 */
	public boolean hasRequirements();
	
	/**
	 * @return
	 */
	public Collection<String> getRequirements();
	
	/**
	 * @param requirement
	 */
	public void addRequirement(String requirement);

	/**
	 * @param requirement
	 */
	public void addRequirements(Set<String> requirements);

	/*****************************************************
	 * Descriptions
	 *****************************************************/
	
	public void addDescriptions(Map<String, String> descriptions);

	public Map<String, String> getDescriptions();

	/*****************************************************
	 * Titles
	 *****************************************************/
	
	public void addTitles(Map<String, String> titles);

	public Map<String, String> getTitles();

	public String toJSON();
	
}	// interface CreativeCommonsLicense
