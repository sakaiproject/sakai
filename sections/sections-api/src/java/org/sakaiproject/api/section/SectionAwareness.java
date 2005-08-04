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

import org.sakaiproject.api.section.coursemanagement.CourseSection;
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
     * Gets the primary section (typically the main lecture for a course) associated
     * with this site context.  Only one primary section is allowed per site.
     * 
     * @param contextId
     * @return
     */
    public CourseSection getPrimarySection(String contextId);

    /**
     * Gets a list of secondary sections for a given site context.  These are
     * sub-groupings of a course, typically labs or discussions.
     * 
     * @param contextId
     * @return A List of CourseSections.
     */
    public List getSecondarySections(String contextId);

    /**
     * Determines whether the section is the "primary" section that is associated
     * with the course offering.
     * 
     * @param sectionId
     * @return
     */
    public boolean isSectionPrimary(String sectionId);

    /**
     * Gets the list of section categories.  In sakai 2.1, there will be only a
     * single set of categories.  They will not be configurable on a per-course
     * or per-context bases.
     */
    public List getSectionCategories();

    /**
     * Gets the site membership for a given context.
     * 
     * @param contextId
     * @return
     */
    public List getSiteMembersInRole(String contextId, Role role);

    /**
     * Finds site members in the given context and role with a matching name or
     * display id.  Pattern matching is TBD, but will probably match in any of
     * the following cases:
     * 
     * Display Name = pattern*
     * Sort Name = pattern*
     * Display Id (installation defined, either Email or enterprise id) = pattern*
     * 
     * @param contextId
     * @param role
     * @param pattern
     * @return
     */
    public List findSiteMembersInRole(String contextId, Role role, String pattern);
    
    /**
     * Checks whether a user plays a particular role in a given site context.
     * 
     * @param contextId
     * @param personId
     * @param role
     * @return
     */
    public boolean isMemberInRole(String contextId, String personId, Role role);

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
     * @param categoryId
     * @return A List of CourseSections
     */
    public List getSectionsInCategory(String contextId, String categoryId);

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
     * are the students who are not enrolled in a lab?'
     */
    public List getUnsectionedMembers(String primarySectionId, String category, Role role);
}


/**********************************************************************************
 * $Id: $
 *********************************************************************************/
