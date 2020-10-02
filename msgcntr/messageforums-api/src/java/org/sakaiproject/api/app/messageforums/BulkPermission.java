package org.sakaiproject.api.app.messageforums;

import lombok.Data;

@Data
public class BulkPermission {

    private boolean changeSettings;
    private boolean deleteAny;
    private boolean deleteOwn;
    private boolean markAsRead;
    private boolean moderatePostings;
    private boolean movePostings;
    private boolean newResponse;
    private boolean newResponseToResponse;
    private boolean newTopic;
    private boolean postToGradebook;
    private boolean read;
    private boolean reviseAny;
    private boolean reviseOwn;

    public void setAllPermissions(boolean toTrueOrFalse) {
        changeSettings = deleteAny = deleteOwn = markAsRead = moderatePostings = movePostings = newTopic
                = newResponse = newResponseToResponse = postToGradebook = read = reviseAny = reviseOwn
                = toTrueOrFalse;
    }
}
