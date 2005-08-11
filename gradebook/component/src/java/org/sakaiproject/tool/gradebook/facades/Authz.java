/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.facades;

/**
 * Facade to external role and authorization service.
 *
 * <p>
 * Baseline Sakai 2.0 gradebook requirements are minimal. There are three types of user per
 * gradebook: users who can do anything (conventionally referred to as "instructors"), users
 * who can only check their own released grades (conventionally referred to as "students"),
 * and users who can't do anything (conventionally kicked out).
 *
 * <p>
 * For full gradebook functionality and LMS capabilities, much richer notions of
 * permissions, roles, and scopes will need to be supported.
 *
 * <p>
 * This version of the API does not define methods to support these future features.
 * Instead, some of the expanded authorization requirements are described in comments
 * below. (It's generally a poor idea to commit development time to "requirements" that
 * aren't functionally required for delivery, don't have any user visibility, and
 * can't be thoroughly tested.) The bad news is that we expect we'll have to
 * change the baseline gradebook authz checks to a new interface later. The good news is
 * that gradebook authz checks are so simple that there isn't much code to
 * change. The alternative would spread more-or-less educated guesses at a proper interface
 * throughout more code, making the risk of later change more unpredictable.
 *
 * <p>
 * Roles will necessarily still play a highly visible role in administrative tools
 * and enterprise integration.
 *
 * <p>
 * But within the gradebook, the new complexity will mean moving from a simple "role"
 * query to more fine-grained but more stable "permission" queries. If "application
 * adminstrators", "instructors", and "graders" can all grade, it's cleaner to ask
 * "can this user grade here?" than "does this user play one of the following three roles?"
 * Instead, it's likely that authz will move to a more configurable
 * grants-based approach. Most permission checks will involve three arguments:
 * <pre>
 *   isAuthorized(permission, userUid, gradebookUid)</pre>
 * where "permission" includes serializable constants such as:
 * <ul>
 *   <li>MODIFY_GRADEBOOK - able to change gradebook-wide settings
 *   <li>MODIFY_ASSIGNMENTS - able to add, edit, or delete assignments
 *   <li>GRADE_COURSE - able to enter or change a final course grade
 *   <li>GRADE - permitted to enter grades (<b>NOTE</b>: If the user has been assigned
 *       any grading rights, this should return true even if no students are
 *       currently in the gradebook)
 *   <li>VIEW_ALL - readonly access to full view
 *   <li>VIEW_GRADE_REPORT_FOR_SELF - permitted to look at any released assignments
 *       and grades for self (the "student view")
 * </ul>
 *
 * At least one check will need to specify a "section". In these descriptions, the
 * term "sections" refers to named subsets of site members who can be graded. Some of these
 * subsets will likely be defined by institutional data. Other subsets will be ad hoc
 * groups created within Sakai's (eventual) group and role managment tool.
 * <pre>
 *   isAuthorized(GRADE_SECTION, userUid, sectionId)</pre>
 * (<b>NOTE</b>: If the user has been assigned grading responsibilities for a section,
 * this should return true even if that section is currently empty.)
 *
 * <p>
 * At least one four-argument check is theoretically possible:
 * <pre>
 *   isAuthorized(GRADE_STUDENT, userUid, gradebookUid, studentId)</pre>
 * However, it's also possible that this will be too fine-grained for practical
 * use, and that gradebook/Samigo users will instead rely on a combination of coarse-grained
 * authz and tracking of agents responsible for grade changes.
 *
 * <p>
 * A number of role and group related queries will also be needed. Without attempting
 * to generalize an approach, here are descriptions of a few.
 * <ul>
 *   <li>Find all enrollments for this gradebook - Used all over.
 *   <li>Find all persons who can grade in this gradebook - Used to filter the scores
 *       (see if Jane Grader is giving everyone lower grades than Jeff Grader). Used
 *       in group-and-role management (show me who I'm dealing with).
 *   <li>Find all sections for this gradebook - Used to filter the enrollments and
 *       scores (see if Lab 1 is doing ridiculously worse than Lab 2; grade a stack
 *       of papers from Lab 1). Used in group-and-role management (show me what sections
 *       graders can be assigned to).
 * </ul>
 */
public interface Authz {
	/**
	 * @param gradebookUid
	 * @param userUid
	 * @return the role played by the specified user in the specified gradebook.
	 *         For the Baseline Sakai 2.0 gradebook,there are only three roles.
	 *         A role of "instructor" means that everything except "see my own
	 *         released grades" is authorized.
	 *         A role of "student" means that only "see my own released grades"
	 *         is authorized.
	 *         A role of "none" means the user shouldn't be here.
	 */
	public Role getGradebookRole(String gradebookUid, String userUid);
}


