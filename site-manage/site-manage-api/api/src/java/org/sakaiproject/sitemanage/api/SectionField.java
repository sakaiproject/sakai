/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.sitemanage.api;

/**
 * A SectionField models an input string used to look up a Section.  Some typical
 * SectionFields might include "Department", "Course", and "Section Number".
 * From the strings in these SectionFields, one should be able to generate an EID
 * for a Section.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">jholtzman@berkeley.edu</a>
 *
 */
public interface SectionField {
	/**
	 * Gets the key to use in looking up the locale-specific label for this SectionField.
	 * @return
	 */
	public String getLabelKey();
	
	/**
	 * Gets the maximum number of characters allowed in this SectionField.
	 * @return
	 */
	public int getMaxSize();

	/**
	 * Gets the current value for this SectionField.
	 * @return
	 */
	public String getValue();
	
	/**
	 * Sets the value for this SectionField.
	 * @param value
	 */
	public void setValue(String value);
}
