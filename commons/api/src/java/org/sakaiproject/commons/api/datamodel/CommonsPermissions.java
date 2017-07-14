package org.sakaiproject.commons.api.datamodel;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CommonsPermissions {

    private String role;
    private boolean postCreate = false;
    private boolean postReadAny = false;
    private boolean postReadOwn = false;
    private boolean postUpdateAny = false;
    private boolean postUpdateOwn = false;
    private boolean postDeleteAny = false;
    private boolean postDeleteOwn = false;
    private boolean commentCreate = false;
    private boolean commentReadAny = false;
    private boolean commentReadOwn = false;
    private boolean commentUpdateAny = false;
    private boolean commentUpdateOwn = false;
    private boolean commentDeleteAny = false;
    private boolean commentDeleteOwn = false;
}
