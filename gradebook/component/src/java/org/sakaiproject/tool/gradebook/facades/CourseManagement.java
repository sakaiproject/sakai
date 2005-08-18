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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.sakaiproject.service.gradebook.shared.UnknownUserException;

/**
 * Facade to external course and group services.
 *
 * Baseline Sakai 2.0 gradebook requirements are minimal.
 *
 * For full LMS capabilities, much richer notions of course offerings, sections,
 * terms, roles, and enrollments will need to be supported.
 */
public interface CourseManagement {
	/**
	 * @param gradebookUid
	 *            the UID of the given gradebook instance; typically used to
	 *            reference the equivalent course offering
	 * @return the matching Enrollment records
	 */
	public Set getEnrollments(String gradebookUid);

	/**
	 * Obtain the set of users with the specific UIDs.
	 * Called when displaying a page of grade records containing the given UIDs.
	 *
	 * Although not strictly needed to meet requirements, this gives implementors
	 * a way to optimize a frequent operation.
	 *
	 * @param gradebookUid
	 * @param userUids the String UIDs to match
	 * @return a set of matching Enrollment objects
	 */
	 public Set findEnrollmentsByUserUids(String gradebookUid, Collection userUids);

	/**
	 * Obtain the number of students included in the specified gradebook.
	 * This is the only CourseManagement information needed to calculate
	 * the most frequently displayed gradebook statistics.
	 *
	 * Although not strictly needed to meet requirements, this gives implementors
	 * a way to optimize a frequent operation.
	 *
	 * @param gradebookUid
	 * @return the number of enrollments
	 */
	public int getEnrollmentsSize(String gradebookUid);

	/**
	 * Search for enrollments whose students have names or display uids that
     * match the specified criteria. For the V2.0 Baseline Gradebook, we've said
     * the input query will be split on space-or-comma and used for an implied
     * "and" match, initial substring wildcarding for the last field and exact
     * (but case-insensitive) match for other fields. "Hamilton, Josh" would
     * become the search "+Hamilton +Josh*" (matching "Joshua Hamilton"), and
     * "Josh Hamil" would become the search "+Josh +Hamil*" (matching "Josh
	 * Hamilton").  Similarly, if the beginning (or all) of a student's display uid matches the
     * query, the student's enrollment will be included in the returned set.
     * "joshh" would match both the UID "joshham@miskotonic.edu" and the UID
     * "joshhor@miskotonic.edu".
	 *
	 * @return a set of matching enrollments or the empty set
	 */
	public Set findEnrollmentsByStudentNameOrDisplayUid(String gradebookUid, String studentNameQuery);

	/**
	 * DROPPED FROM THE SAKAI 2.0 GRADEBOOK FACADES.
	 * In this version of the gradebook applications, all of the sortable pageable
	 * tables which contain student data can also be sorted by grades or scores.
	 * When that happens, all enrollments will need to be retrieved. Rather than
	 * changing different performance profiles for the two types of sorting,
	 * we'll hold
	 We'll leave the comments around for a bit, though.
	 *
	 *
	 *
	 * Return a range of enrollments, assuming case-insensitive sorting by student
	 * sortable name (typically "Last, First M." for European names).
	 *
	 * Although not strictly needed to meet requirements, this gives implementors
	 * a way to optimize a frequent operation.
	 *
	 * @param gradebookUid
	 * @param startRange the sorted position from which to start returning enrollments
	 * @param rangeMaximum the maximum number to return (at the end of the sorted collection,
	 *                     the real number returned will usually be less)
	 * @param isAscending true if the sort should be done in ascending order; false for descending
	 * @return list of enrollments, sorted by student name
	 */
	public List findEnrollmentsPagedBySortName(String gradebookUid, int startRange, int rangeMaximum, boolean isAscending);

	/**
	 * Return a range of enrollments, assuming case-insensitive sorting by student
	 * display UID (typically a student number or an email address).
	 *
	 * Although not strictly needed to meet requirements, this gives implementors
	 * a way to optimize a frequent operation.
	 *
	 * @param gradebookUid
	 * @param startRange the sorted position from which to start returning enrollments
	 * @param rangeMaximum the maximum number to return (at the end of the sorted collection,
	 *                     the real number returned will usually be less)
	 * @param isAscending true if the sort should be done in ascending order; false for descending
	 * @return list of enrollments, sorted by student display UID
	 */
	public List findEnrollmentsPagedByDisplayUid(String gradebookUid, int startRange, int rangeMaximum, boolean isAscending);

    /**
     * Looks up a user based on their uid.
     *
     * TODO Eliminate this unnecessarily broad method. This is used only to obtain a grader's name based on their user UID.
     * It should be restricted to people who have access to the gradebook.
     * One potential issue: Administrators may have grading rights without an explicit participation record.
     * The safest way to handle this is likely an Authn method like "getUser(Object whatToAuthn)" which returns
     * the User object for the current user.
     *
     * @param userUid
     * @return
     */
    public User getUser(String userUid) throws UnknownUserException;
}



