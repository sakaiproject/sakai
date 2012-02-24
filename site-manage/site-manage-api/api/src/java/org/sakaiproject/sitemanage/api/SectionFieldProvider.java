/**********************************************************************************
 * $URL:  $
 * $Id: SectionFieldManager.java 22875 2007-03-19 02:31:42Z daisyf@stanford.edu $
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

import java.util.List;


/**
 * The site management applications allow installations to configure a variable
 * number of "fields" to use when looking up a Section from the
 * CourseManagementService.  The SectionFieldProvider provides method for
 * finding the number and type of fields to use in the UI, and for finding Section EIDs
 * from a number of Fields.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">jholtzman@berkeley.edu</a>
 *
 */
public interface SectionFieldProvider {
	
	/**
	 * Generates a Section EID from a List of fields.  This EID may be used to look up
	 * Sections from the CourseManagementService.  This may be implemented with
	 * simple text formatting, or an intermediary lookup may be needed to convert
	 * the human-understandable field values to a recognized EID.
	 * 
	 * @param fields
	 * @return
	 */
	public String getSectionEid(String academicSessionEid, List<SectionField> fields);
	
	/**
	 * Generates a Section title from a list of fields. 
	 * This title will be used ONLY if the section cannot be find from CourseManagementService
	 * otherwise, the title given by CourseManagementService will be used for the section
	 * @param academicSessionEid
	 * @param fields
	 * @return
	 */
	public String getSectionTitle(String academicSessionEid, List<SectionField> fields);

	/**
	 * Gets the List of SectionFields to use in the UI.
	 * 
	 * @return
	 */
	public List<SectionField> getRequiredFields();
}
