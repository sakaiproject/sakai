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
package org.sakaiproject.coursemanagement.api;

/**
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">jholtzman@berkeley.edu</a>
 *
 */
public interface SectionCategory {
	
	/**
	 * @return
	 */
	public String getCategoryCode();
	/**
	 * @param categoryCode
	 */
	public void setCategoryCode(String categoryCode);
	/**
	 * @return
	 */
	public String getCategoryDescription();
	/**
	 * @param categoryDescription
	 */
	public void setCategoryDescription(String categoryDescription);
}
