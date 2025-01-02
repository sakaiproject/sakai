package org.sakaiproject.microsoft.api.data;

import lombok.Getter;

@Getter
public enum CreationStatus {

    OK(2),
    PARTIAL_OK(1), //site-team is OK, but some group-channel are KO
    KO(0),
    NONE(-1);

    private Integer code;

    private CreationStatus(Integer code) {
        this.code = code;
    }

    public static CreationStatus fromCode(Integer code) {
        for (CreationStatus v : CreationStatus.values()) {
            if (v.code == code) {
                return v;
            }
        }
        return null;
    }
}
