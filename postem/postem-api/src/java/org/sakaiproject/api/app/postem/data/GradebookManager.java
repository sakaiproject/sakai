/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.api.app.postem.data;

import java.util.List;
import java.util.SortedSet;

public interface GradebookManager {
	public Gradebook createGradebook(String title, String creator,
			String context, List headings, SortedSet students, Template template, String fileReference);

	public Gradebook createEmptyGradebook(String creator, String context);

	public StudentGrades createStudentGradesInGradebook(String username,
			List grades, Gradebook gradebook);

	public StudentGrades createStudentGrades(String username, List grades);

	public Template createTemplate(String template);

	public Gradebook getGradebookByTitleAndContext(final String title,
			final String context);

	public SortedSet getGradebooksByContext(final String context, final String sortBy, final boolean ascending);

	public SortedSet getReleasedGradebooksByContext(final String context, final String sortBy, final boolean ascending);

	public SortedSet getStudentGradesForGradebook(final Gradebook gradebook);

	public void saveGradebook(Gradebook gradebook);

	public void updateGrades(Gradebook gradebook, List headings,
			SortedSet students);

	public void updateTemplate(Gradebook gradebook, String template, String fileReference);

	public void deleteGradebook(final Gradebook gradebook);

	public void deleteStudentGrades(final StudentGrades student);
	
	/**
	 * 
	 * @param gradebookId
	 * @return gradebook object with the headings and student data populated
	 */
	public Gradebook getGradebookByIdWithHeadingsAndStudents(final Long gradebookId);
	
	/**
	 * 
	 * @param gradebookId
	 * @return gradebook object with headings populated, not students
	 */
	public Gradebook getGradebookByIdWithHeadings(final Long gradebookId);
	
	/**
	 * Return the StudentGrades object associated with the given gradebook and
	 * username
	 * @param gradebook
	 * @param username
	 * @return
	 */
	public StudentGrades getStudentByGBAndUsername(final Gradebook gradebook, final String username);
	
	/**
	 * Update an individual StudentGrades object
	 * @param student
	 */
	public void updateStudent(StudentGrades student);
	
	/**
	 * 
	 * @param gradebook
	 * @return a list of all of the usernames associated with the given gradebook
	 */
	public List getUsernamesInGradebook(Gradebook gradebook);
	
}
