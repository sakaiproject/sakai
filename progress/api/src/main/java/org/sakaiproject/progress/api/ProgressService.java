package org.sakaiproject.progress.api;

/**
 * Interface that will serve as the basic contract to the persistence layer for getting and setting configurations
 * TODO: Modify this to meet actual requirements
 */
public interface ProgressService {
    public void getConfig();
    public void setConfig();

    public long getCourseID();
    public void setCourseID();

    public Date getDateCreated();

    public Date getDateEdited();
    public void setDateEdited();

    public string getModifiedBy();

    public ProgressItem getProgressItem();
}
