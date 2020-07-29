package org.sakaiproject.elfinder;

/**
 * This is an enumeration of sakai fs types
 * In the future making this a dynamic enumeration will better support registration
 */
public enum FsType {
    ASSESSMENT("assessment"),
    ASSIGNMENT("assignment"),
    CONTENT("content"),
    DROPBOX("dropbox"),
    FORUMS_FORUM("forum"),
    FORUMS_TOPIC("topic"),
    SCORM("scorm"),
    SITE("site");

    private final String type;

    FsType(String type) {
        this.type = type;
    }

    public FsType getType() {
        return FsType.valueOf(type);
    }
}
