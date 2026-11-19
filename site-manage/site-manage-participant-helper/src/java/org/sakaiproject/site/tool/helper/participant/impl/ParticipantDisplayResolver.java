/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package org.sakaiproject.site.tool.helper.participant.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/** Resolves participant identities for wizard review views. */
@Component
@Slf4j
public class ParticipantDisplayResolver {

    private final UserDirectoryService userDirectoryService;

    public ParticipantDisplayResolver(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public List<ParticipantDisplay> displays(List<UserRoleEntry> entries) {
        return entries.stream().map(this::display).collect(Collectors.toList());
    }

    private ParticipantDisplay display(UserRoleEntry entry) {
        String displayId = entry.getEid();
        String displayName = participantName(entry);
        try {
            User user = userDirectoryService.getUserByEid(entry.getEid());
            displayId = user.getDisplayId();
            displayName = user.getSortName();
        } catch (UserNotDefinedException e) {
            log.debug("Cannot find user with eid {} while preparing role assignment", entry.getEid(), e);
        }
        return new ParticipantDisplay(displayId + " (" + displayName + ")");
    }

    private String participantName(UserRoleEntry entry) {
        if (StringUtils.isBlank(entry.getLastName()) && StringUtils.isBlank(entry.getFirstName())) {
            return entry.getEid();
        }
        return StringUtils.defaultString(entry.getLastName()) + ", " + StringUtils.defaultString(entry.getFirstName());
    }

    public record ParticipantDisplay(String displayName) {
    }
}
