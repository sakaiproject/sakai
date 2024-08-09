package org.sakaiproject.microsoft.api.data;

import lombok.Getter;

@Getter
public enum AutoConfigProcessStatus {

    START_RUNNING(0),
    CREATING_TEAM(1),
    BINDING_TEAM(2),
    END_RUNNING(3);

    private Integer code;

    private AutoConfigProcessStatus(Integer code) {
        this.code = code;
    }

    public static AutoConfigProcessStatus fromCode(Integer code) {
        for (AutoConfigProcessStatus v : AutoConfigProcessStatus.values()) {
            if (v.code == code) {
                return v;
            }
        }
        return null;
    }
}
