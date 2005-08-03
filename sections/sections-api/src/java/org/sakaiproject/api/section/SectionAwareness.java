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

import org.sakaiproject.api.section.facade.Role;

public interface SectionAwareness {
    
    /**
     * Gets a list of sections for a given site context.
     * 
     * @param contextId
     * @return
     */
    public List getSections(String contextId);
    
    // TODO Add the rest of the section awareness methods from http://bugs.sakaiproject.org/confluence/display/SECT/Section+Awareness+API+Requirements
    
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
     * Gets the site membership playing a given role for a given context.
     * 
     * @param contextId
     * @param role
     * @return
     */
    public List getMembersInRoles(String contextId, Role role);

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
     * Gets the name of a section.
     * 
     * @param sectionId
     * @return
     */
    public String getSectionName(String sectionId);

    /**
     * Gets the category of a given section.
     * 
     * @param sectionId
     * @return
     */
    public String getSectionCategory(String sectionId);

    /**
     * Lists the sections in this context that are a member of the given category.
     * 
     * @param categoryId
     * @return
     */
    public List getSectionsInCategory(String contextId, String categoryId);

    /**
     * Gets the name of a given category.
     * 
     * @param categoryId
     * @param locale
     * @return
     */
    public String getCategoryName(String categoryId, Locale locale);
    
    /**
     * Determines whether the section is the "primary" section that is associated
     * with the course offering.
     * 
     * @param sectionId
     * @return
     */
    public boolean isSectionPrimary(String sectionId);
}


/**********************************************************************************
 * $Id: $
 *********************************************************************************/
