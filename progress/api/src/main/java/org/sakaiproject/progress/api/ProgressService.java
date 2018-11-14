package org.sakaiproject.progress.api;

import java.util.Date;

/**
 * Interface that will serve as the basic contract to the persistence layer for getting and setting configurations
 * TODO: Modify this to meet actual requirements
 */
public interface ProgressService {
    public void getConfig();
    public void setConfig();

    public long getCourseId();
    public void setCourseId();
    public Date getDateCreated();
    public Date getDateEdited();
    public void setDateEdited();
    public String getModifiedBy();
    public List<ProgressItem> getProgressItemList();
    public ProgressItem getProgressItem(long id);
}
