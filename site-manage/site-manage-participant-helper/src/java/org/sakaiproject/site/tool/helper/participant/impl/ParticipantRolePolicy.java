/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package org.sakaiproject.site.tool.helper.participant.impl;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Role;

/** Shared role-assignment rules for wizard validation and realm commit. */
public final class ParticipantRolePolicy {

    public enum Outcome {
        ALLOWED,
        INVALID_ROLE,
        RESTRICTED_SITE_UPD
    }

    private ParticipantRolePolicy() {
    }

    public static Outcome evaluate(String roleName, AuthzGroup realm, Set<String> allowedRoleNames,
            boolean mayUpdateRealm) {
        if (StringUtils.isBlank(roleName) || !allowedRoleNames.contains(roleName)) {
            return Outcome.INVALID_ROLE;
        }
        Role role = realm.getRole(roleName);
        if (role == null) {
            return Outcome.INVALID_ROLE;
        }
        if (!mayUpdateRealm && role.isAllowed("site.upd")) {
            return Outcome.RESTRICTED_SITE_UPD;
        }
        return Outcome.ALLOWED;
    }

    public static ParticipantMessage messageFor(Outcome outcome, String roleName, boolean wizardValidation) {
        return switch (outcome) {
            case ALLOWED -> null;
            case INVALID_ROLE -> wizardValidation
                    ? new ParticipantMessage("java.pleasechoose", null, ParticipantMessage.Severity.ERROR)
                    : new ParticipantMessage("java.roleperm", new Object[] {roleName}, ParticipantMessage.Severity.ERROR);
            case RESTRICTED_SITE_UPD -> new ParticipantMessage("java.roleperm", new Object[] {roleName},
                    ParticipantMessage.Severity.ERROR);
        };
    }
}
