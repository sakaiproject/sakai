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

package org.sakaiproject.util;

import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.w3c.dom.Element;

/**
 * <p>
 * BaseResourceProperties is the base class for ResourcePropertiesEdit implementations.
 * </p>
 */
public class BaseResourcePropertiesEdit extends BaseResourceProperties implements ResourcePropertiesEdit
{
	/**
	 * Construct.
	 */
	public BaseResourcePropertiesEdit()
	{
		super();

	} // BaseResourcePropertiesEdit

	/**
	 * Construct from XML.
	 * 
	 * @param el
	 *        The XML DOM element.
	 */
	public BaseResourcePropertiesEdit(Element el)
	{
		super(el);

	} // BaseResourcePropertiesEdit

} // BaseResourcePropertiesEdit

