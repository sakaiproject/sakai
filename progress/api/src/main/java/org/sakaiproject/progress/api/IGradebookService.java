package org.sakaiproject.progress.api;

/**
 * Serves relevant gradebook data
 */
public interface IGradebookService {
    /**
     * returns the list of students
     * @return list of students
     */
    public String[] getStudentList();
}
