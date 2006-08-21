/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/content/trunk/content-tool/tool/src/java/org/sakaiproject/content/tool/ResourcesAction.java $
 * $Id: ResourcesAction.java 13885 2006-08-21 16:03:28Z jimeng@umich.edu $
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

package org.sakaiproject.rights.impl;

import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.rights.api.Copyright;
import org.sakaiproject.rights.api.CopyrightService;
import org.sakaiproject.rights.api.RightsAssignment;
import org.sakaiproject.rights.api.RightsAssignment;


public class BaseCopyrightService implements CopyrightService {

	/**
	 * @param entityRef
	 * @return
	 */
	public RightsAssignment addRightsAssignment(String entityRef)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public RightsAssignment getRightsAssignment(String entityRef) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void setRightsAssignment(String entityRef, RightsAssignment rights) 
	{
		// TODO Auto-generated method stub

	}
	
	/**
	 * @param rights
	 */
	public void save(RightsAssignment rights)
	{
		// TODO 
	}
	
	public class BasicCopyright implements Copyright 
	{

		public void setOwner(String owner) 
		{
			// TODO Auto-generated method stub

		}

		public void setYear(String year) 
		{
			// TODO Auto-generated method stub

		}

		public String getOwner() 
		{
			// TODO Auto-generated method stub
			return null;
		}

		public String getYear() 
		{
			// TODO Auto-generated method stub
			return null;
		}

		public Element toXml(Document doc, Stack stack) 
		{
			// TODO Auto-generated method stub
			return null;
		}

	}	// class BasicCopyright

}	// class BaseCopyrightService
