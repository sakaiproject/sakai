/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.entity.api;

import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * Entity is the generic interface for all information units modeled in Sakai.
 * </p>
 */
public interface Entity
{
	/** The character used to separate names in the region address path */
	static final String SEPARATOR = "/";

	/**
	 * Access the URL which can be used to access the entity.
	 * 
	 * @return The URL which can be used to access the entity.
	 */
	String getUrl();

	/**
	 * Access the internal reference which can be used to access the entity from within the system.
	 * 
	 * @return The the internal reference which can be used to access the entity from within the system.
	 */
	String getReference();

	/**
	 * Access the alternate URL which can be used to access the entity.
	 * 
	 * @param rootProperty
	 *        The name of the entity property whose value controls which alternate reference URL is requested. If null, the native 'raw' URL is requested.
	 * @return The alternate URL which can be used to access the entity.
	 */
	String getUrl(String rootProperty);

	/**
	 * Access the alternate internal reference which can be used to access the entity from within the system.
	 * 
	 * @param rootProperty
	 *        The name of the entity property whose value controls which alternate reference is requested. If null, the native 'raw' reference is requested.
	 * @return The the alternate internal reference which can be used to access the entity from within the system.
	 */
	String getReference(String rootProperty);

	/**
	 * Access the id of the entity.
	 * 
	 * @return The id.
	 */
	String getId();

	/**
	 * Access the entity's properties.
	 * 
	 * @return The entity's properties.
	 */
	ResourceProperties getProperties();

	/**
	 * Serialize the entity into XML, adding an element to the doc under the top of the stack element.
	 * 
	 * @param doc
	 *        The DOM doc to contain the XML (or null for a string return).
	 * @param stack
	 *        The DOM elements, the top of which is the containing element of the new "entity" element.
	 * @return The newly added element.
	 */
	Element toXml(Document doc, Stack<Element> stack);
}
