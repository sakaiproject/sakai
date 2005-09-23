/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The Regents of the University of Michigan,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.component.section.cover;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.sakaiproject.api.kernel.component.cover.ComponentManager;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.facade.Role;

public class SectionAwareness {
	private static org.sakaiproject.api.section.SectionAwareness instance;

	private static org.sakaiproject.api.section.SectionAwareness getInstance() {
		if(instance == null) {
			instance = (org.sakaiproject.api.section.SectionAwareness)ComponentManager.get(
					org.sakaiproject.api.section.SectionAwareness.class);
		}
		return instance;
	}
	
	public static final List findSiteMembersInRole(String siteContext, Role role, String pattern) {
		return getInstance().findSiteMembersInRole(siteContext, role, pattern);
	}

	public static final String getCategoryName(String categoryId, Locale locale) {
		return getInstance().getCategoryName(categoryId, locale);
	}

	public static final CourseSection getSection(String sectionUuid) {
		return getInstance().getSection(sectionUuid);
	}

	public static final List getSectionCategories() {
		return getInstance().getSectionCategories();
	}

	public static final List getSectionMembers(String sectionUuid) {
		return getInstance().getSectionMembers(sectionUuid);
	}

	public static final List getSectionMembersInRole(String sectionUuid, Role role) {
		return getInstance().getSectionMembersInRole(sectionUuid, role);
	}

	public static final Set getSections(String siteContext) {
		return getInstance().getSections(siteContext);
	}

	public static final List getSectionsInCategory(String siteContext, String categoryId) {
		return getInstance().getSectionsInCategory(siteContext, categoryId);
	}

	public static final List getSiteMembersInRole(String siteContext, Role role) {
		return getInstance().getSiteMembersInRole(siteContext, role);
	}

	public static final boolean isSectionMemberInRole(String sectionUuid, String userUid, Role role) {
		return getInstance().isSectionMemberInRole(sectionUuid, userUid, role);
	}

	public static final boolean isSiteMemberInRole(String siteContext, String userUid, Role role) {
		return getInstance().isSiteMemberInRole(siteContext, userUid, role);
	}
	
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/
