package org.sakaiproject.progress.api;

import org.sakaiproject.exception.IdUnusedException;

import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.user.api.User;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Serves relevant gradebook data
 */
public interface IGradebookService {

    final String SORT_BY_TITLE = "title";
    final String SORT_BY_CREATOR = "creator";
    final String SORT_BY_MOD_BY = "modBy";
    final String SORT_BY_MOD_DATE = "modDate";
    final String SORT_BY_RELEASED = "released";

    public void setGradebook(final String uid);

    public Gradebook getGradebook(final String uid);

    public String getTitle();

    public String getContext();

    public void setContext(String context);

    public List<User> getStudents(String siteId);

    public Long getId();

    public TreeMap getStudentMap();

    public boolean hasStudent(String username);

    public String getFirstUploadedUsername();

    public void setFirstUploadedUsername(String username);

    public List getUsernames();

    public void setUsernames(List<String> usernames);

    public Map<String, CourseGrade> getCourseGrades(String siteID);
}
