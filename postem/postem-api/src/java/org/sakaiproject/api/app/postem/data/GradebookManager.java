package org.sakaiproject.api.app.postem.data;

import java.util.List;
import java.util.SortedSet;

public interface GradebookManager {
	public Gradebook createGradebook(String title, String creator,
			String context, List headings, SortedSet students, Template template);

	public Gradebook createEmptyGradebook(String creator, String context);

	public StudentGrades createStudentGradesInGradebook(String username,
			List grades, Gradebook gradebook);

	public StudentGrades createStudentGrades(String username, List grades);

	public Template createTemplate(String template);

	public Gradebook getGradebookByTitleAndContext(final String title,
			final String context);

	public SortedSet getGradebooksByContext(String context);

	public SortedSet getReleasedGradebooksByContext(final String context);
	
	public SortedSet getStudentGradesForGradebook(final Gradebook gradebook);

	public void saveGradebook(Gradebook gradebook);

	public void updateGrades(Gradebook gradebook, List headings,
			SortedSet students);

	public void updateTemplate(Gradebook gradebook, String template);

	public void deleteGradebook(final Gradebook gradebook);

	public void deleteStudentGrades(final StudentGrades student);
}
