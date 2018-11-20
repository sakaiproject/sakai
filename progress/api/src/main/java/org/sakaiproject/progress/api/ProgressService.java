package org.sakaiproject.progress.api;

import org.sakaiproject.progress.model.Progress;

import java.util.Date;
import java.util.List;

/**
 * Interface that will serve as the basic contract to the persistence layer for getting and setting configurations
 * TODO: Add more methods to interact with individual progress items
 */
public interface ProgressService {
    public void getConfig();
    public void setConfig();

    public void addProgress(Progress progress);
    public void updateProgess(Progress progress);
    public Progress getProgress(String id);
    public void deleteProgress(String id);

    public List<Progress> getAllProgress();
}
