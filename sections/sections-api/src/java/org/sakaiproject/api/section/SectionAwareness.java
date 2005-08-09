/**********************************************************************************
*
* $Id: $
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
package org.sakaiproject.api.section;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.sakaiproject.api.section.coursemanagement.CourseOffering;
import org.sakaiproject.api.section.facade.Role;

/**
 * Provides section awareness to tools needing read-only access to section
 * information, such as section membership.
 * 
 * Based loosely on Ray's section awareness requirements posted at
 * http://bugs.sakaiproject.org/confluence/display/SECT/Section+Awareness+API+Requirements
 * 
 * <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public interface SectionAwareness {

	/**
	 * Gets the course offering associated with this site context.
	 * 
	 * @param siteContext The site context
	 * @return The course offering
	 */
	public CourseOffering getCourseOffering(String siteContext);
	
    /**
     * Gets the sections associated with this site context.
     * 
	 * @param siteContext The site context
     * @return The set of sections in this site
     */
    public Set getSections(String siteContext);

    /**
     * Gets the list of section categories.  In sakai 2.1, there will be only a
     * single set of categories.  They will not be configurable on a per-course
     * or per-context bases.
     */
    public List getSectionCategories();

    /**
     * Gets the site membership for a given context.
     * 
	 * @param siteContext The site context
     * @return
     */
    public List getSiteMembersInRole(String siteContext, Role role);

    /**
     * Finds site members in the given context and role with a matching name or
     * display id.  Pattern matching is TBD, but will probably match in any of
     * the following cases:
     * 
     * Display Name = pattern*
     * Sort Name = pattern*
     * Display Id (installation defined, either Email or enterprise id) = pattern*
     * 
	 * @param siteContext The site context
     * @param role The role the user must play in this context
     * @param pattern The pattern the user's name or id must match
     * @return
     */
    public List findSiteMembersInRole(String siteContext, Role role, String pattern);
    
    /**
     * Checks whether a user plays a particular role in a given site context.
     * 
	 * @param siteContext The site context
     * @param personId The user's unique id
     * @param role The role we're checking
     * @return
     */
    public boolean isMemberInRole(String siteContext, String personId, Role role);

    /**
     * Gets the full membership of the given section.
     * 
     * @param sectionId
     * @return
     */
    public List getSectionMembers(String sectionId);

    /**
     * Gets the members of a given section that play a given role in the section.
     * 
     * @param sectionId
     * @param role
     * @return
     */
    public List getSectionMembersInRole(String sectionId, Role role);

    /**
     * Checks whether a user plays a particular role in a section.
     * 
     * @param sectionId
     * @param personId
     * @param role
     * @return
     */
    public boolean isSectionMemberInRole(String sectionId, String personId, Role role);

    /**
     * Lists the sections in this context that are a member of the given category.
     * 
	 * @param siteContext The site context
     * @param categoryId
     * @return A List of CourseSections
     */
    public List getSectionsInCategory(String siteContext, String categoryId);

    /**
     * Gets the localized name of a given category.
     * 
     * @param categoryId
     * @param locale
     * @return
     */
    public String getCategoryName(String categoryId, Locale locale);
    
    /**
     * The Section Manager tool could use more specific queries on membership,
     * such as this:  getting all students in a primary section that are not
     * enrolled in any secondary sections of a given type.  For instance, 'Who
     * are the students who are not enrolled in any lab?'
     */
    public List getUnsectionedMembers(String siteContext, String category, Role role);
}


/**********************************************************************************
 * $Id: $
 *********************************************************************************/
