/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.api.app.postem.data;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

public interface GradebookManager {
	public Gradebook createGradebook(String title, String creator,
			String context, List<Heading> headings, SortedSet students, Template template);

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

	public void updateGrades(Gradebook gradebook, List<Heading> headings,
			SortedSet students);

	public void updateTemplate(Gradebook gradebook, String template);

	public void deleteGradebook(final Gradebook gradebook);

	public void deleteStudentGrades(final StudentGrades student);
	
	/**
	 * 
	 * @param gradebookId
	 * @return the Gradebook object with the given id
	 * with the headings, students, and grade information populated
	 */
	public Gradebook getGradebookByIdWithHeadingsStudentsAndGrades(final Long gradebookId);
	
	/**
	 * 
	 * @param gradesLists
	 * 	list of lists parsed from the csv file for each student
	 * @return Map of username to the list of associated grades for each student
	 */
	public Map<String, List> createUsernameGradesListMap(List gradesLists);
	
	public void saveGradebook(Gradebook gradebook, List<String> headingTitles, Map usernameGradesListMap);
	
	/**
	 * 
	 * @param title
	 * @param context
	 * @return true if the given title already exists in the given context
	 */
	public boolean titleExistsInContext(String title, String context);
}
