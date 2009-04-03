/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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

import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 *
 */
public interface Copyright 
{
	public String getCopyrightId();
	
	/**
	 * @return
	 */
	public String getYear();
	
	/**
	 * @return
	 */
	public String getOwner();
	
	/**
	 * @param year
	 */
	public void setYear(String year);
	
	/**
	 * @param owner
	 */
	public void setOwner(String owner);
	
	/**
	 * @param doc
	 * @param stack
	 * @return
	 */
	public Element toXml(Document doc, Stack<Object> stack);
	
}	// interface Copyright
