package org.sakaiproject.poll.api;

import org.sakaiproject.entity.api.Entity;

public class PollConstants {

    public static final String APPLICATION_ID = "sakai:poll";
    public static final String REFERENCE_ROOT = Entity.SEPARATOR + "poll";

    public static final String PERMISSION_PREFIX = "poll";
    public static final String PERMISSION_VOTE = "poll.vote";
    public static final String PERMISSION_ADD = "poll.add";
    public static final String PERMISSION_DELETE_OWN = "poll.deleteOwn";
    public static final String PERMISSION_DELETE_ANY = "poll.deleteAny";
    public static final String PERMISSION_EDIT_ANY = "poll.editAny";
    public static final String PERMISSION_EDIT_OWN = "poll.editOwn";

    public static final String REF_POLL_TYPE = "poll";

    private PollConstants() {
        throw new IllegalStateException("do not instantiate this class");
    }
}
