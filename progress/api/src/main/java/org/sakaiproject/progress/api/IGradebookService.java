package org.sakaiproject.progress.api;

import java.sql.Timestamp;
import java.util.List;
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

    public String getTitle();

    public void setTitle(String title);

    public String getCreator();

    public void setCreator(String creator);

    public String getCreatorEid();

    public void setCreatorEid(String creatorUserId);

    public Timestamp getCreated();

    public void setCreated(Timestamp created);

    public String getLastUpdater();

    public void setLastUpdater(String lastUpdater);

    public String getLastUpdaterEid();

    public void setLastUpdaterEid(String lastUpdaterUserId);

    public String getUpdatedDateTime();

    public Timestamp getLastUpdated();

    public void setLastUpdated(Timestamp lastUpdated);

    public String getContext();

    public void setContext(String context);

    public Set getStudents();

    public void setStudents(Set students);

    //public Template getTemplate();

    //public void setTemplate(Template template);

    public void setFileReference(String fileReference);

    public String getFileReference();

    public List getHeadings();

    public void setHeadings(List headings);

    public Long getId();

    public void setId(Long id);

    public Boolean getReleased();

    public void setReleased(Boolean released);

    public String getHeadingsRow();

    public TreeMap getStudentMap();

    public boolean hasStudent(String username);

    public boolean getRelease();

    public void setRelease(boolean release);

    public Boolean getReleaseStatistics();

    public void setReleaseStatistics(Boolean releaseStatistics);

    public boolean getReleaseStats();

    public void setReleaseStats(boolean releaseStats);

    public String getProperWidth(int column);

    public List getRawData(int column);

    /**
     * NOT IMPLEMENTED - the code in this method has never been used and is
     * currently commented out.  This method will always return an empty list
     * @param column
     * @return an empty list
     * @throws Exception
     *
     */
    public List getAggregateData(int column) throws Exception;

    // public Map getTotals(int column);

    //public StudentGrades studentGrades(String username);

    public String getFirstUploadedUsername();

    public void setFirstUploadedUsername(String username);

    public List getUsernames();
    public void setUsernames(List<String> usernames);
}
