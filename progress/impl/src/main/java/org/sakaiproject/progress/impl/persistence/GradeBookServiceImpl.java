package org.sakaiproject.progress.impl.persistence;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.progress.api.IGradebookService;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import
import org.sakaiproject.posapi.app.postem.data.GradebookManager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class GradeBookServiceImpl implements IGradebookService {

    protected GradebookManager gradebookManager;
    protected String userId;
    protected String userEid;
    protected String siteId = null;
    protected ArrayList gradebooks;

    private ArrayList getGradebooks() {
        if (userId == null) {
            userId = SessionManager.getCurrentSessionUserId();

            if (userId != null) {
                try {
                    userEid = UserDirectoryService.getUserEid(userId);
                } catch (UserNotDefinedException e) {
                    log.error("UserNotDefinedException", e);
                }
            }
        }

        Placement placement = ToolManager.getCurrentPlacement();
        String currentSiteId = placement.getContext();

        siteId = currentSiteId;
        try {
            if (checkAccess()) {
                // logger.info("**** Getting by context!");
                gradebooks = new ArrayList(gradebookManager
                        .getGradebooksByContext(siteId, sortBy, ascending));
            } else {
                // logger.info("**** Getting RELEASED by context!");
                gradebooks = new ArrayList(gradebookManager
                        .getReleasedGradebooksByContext(siteId, sortBy, ascending));
            }
        } catch (Exception e) {
            gradebooks = null;
        }

        if (gradebooks != null && gradebooks.size() > 0)
            gradebooksExist = true;
        else
            gradebooksExist = false;

        return gradebooks;

    }

    private boolean checkAccess() {
        // return true;
        return SiteService.allowUpdateSite(ToolManager.getCurrentPlacement()
                .getContext());
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public void setTitle(String title) {

    }

    @Override
    public String getCreator() {
        return null;
    }

    @Override
    public void setCreator(String creator) {

    }

    @Override
    public String getCreatorEid() {
        return null;
    }

    @Override
    public void setCreatorEid(String creatorUserId) {

    }

    @Override
    public Timestamp getCreated() {
        return null;
    }

    @Override
    public void setCreated(Timestamp created) {

    }

    @Override
    public String getLastUpdater() {
        return null;
    }

    @Override
    public void setLastUpdater(String lastUpdater) {

    }

    @Override
    public String getLastUpdaterEid() {
        return null;
    }

    @Override
    public void setLastUpdaterEid(String lastUpdaterUserId) {

    }

    @Override
    public String getUpdatedDateTime() {
        return null;
    }

    @Override
    public Timestamp getLastUpdated() {
        return null;
    }

    @Override
    public void setLastUpdated(Timestamp lastUpdated) {

    }

    @Override
    public String getContext() {
        return null;
    }

    @Override
    public void setContext(String context) {

    }

    @Override
    public Set getStudents() {
        return null;
    }

    @Override
    public void setStudents(Set students) {

    }

    @Override
    public void setFileReference(String fileReference) {

    }

    @Override
    public String getFileReference() {
        return null;
    }

    @Override
    public List getHeadings() {
        return null;
    }

    @Override
    public void setHeadings(List headings) {

    }

    @Override
    public Long getId() {
        return null;
    }

    @Override
    public void setId(Long id) {

    }

    @Override
    public Boolean getReleased() {
        return null;
    }

    @Override
    public void setReleased(Boolean released) {

    }

    @Override
    public String getHeadingsRow() {
        return null;
    }

    @Override
    public TreeMap getStudentMap() {
        return null;
    }

    @Override
    public boolean hasStudent(String username) {
        return false;
    }

    @Override
    public boolean getRelease() {
        return false;
    }

    @Override
    public void setRelease(boolean release) {

    }

    @Override
    public Boolean getReleaseStatistics() {
        return null;
    }

    @Override
    public void setReleaseStatistics(Boolean releaseStatistics) {

    }

    @Override
    public boolean getReleaseStats() {
        return false;
    }

    @Override
    public void setReleaseStats(boolean releaseStats) {

    }

    @Override
    public String getProperWidth(int column) {
        return null;
    }

    @Override
    public List getRawData(int column) {
        return null;
    }

    @Override
    public List getAggregateData(int column) throws Exception {
        return null;
    }

    @Override
    public String getFirstUploadedUsername() {
        return null;
    }

    @Override
    public void setFirstUploadedUsername(String username) {

    }

    @Override
    public List getUsernames() {
        return null;
    }

    @Override
    public void setUsernames(List<String> usernames) {

    }
}
