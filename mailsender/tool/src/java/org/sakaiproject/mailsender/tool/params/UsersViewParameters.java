/**********************************************************************************
 * Copyright 2008-2009 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.mailsender.tool.params;

/**
 * This is a view parameters class which defines the variables that are passed from one page to
 * another
 *
 * @author Carl Hall
 */
public class UsersViewParameters extends UserGroupViewParameters
{
	public String id; // the type of group to produce

	/**
	 * Basic empty constructor
	 */
	public UsersViewParameters()
	{
	}

	/**
	 * Minimal constructor
	 *
	 * @param viewID
	 *            the target view for these parameters
	 */
	public UsersViewParameters(String viewID)
	{
		this.viewID = viewID;
	}

	/**
	 * Full power constructor
	 *
	 * @param viewID
	 * @param type
	 */
	public UsersViewParameters(String viewID, String type, String id)
	{
		this.viewID = viewID;
		this.type = type;
		this.id = id;
	}
}