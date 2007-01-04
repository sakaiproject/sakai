/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
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
package org.sakaiproject.tool.section.jsf.backingbean;

import java.util.List;

/**
 * Backing beans that are used to modify (add or edit) sections should implement
 * SectionEditor.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">jholtzman@berkeley.edu</a>
 */
public interface SectionEditor {
	/**
	 * Gets the list of sections to add / edit
	 * @return
	 */
	public List<LocalSectionModel> getSections();
	
	/**
	 * Gets the comma-separated css classes for use in the UI.
	 * @return
	 */
	public String getRowStyleClasses();
	
	/**
	 * Gets the UI element which should have the focus on page load.
	 * @return
	 */
	public String getElementToFocus();
}
