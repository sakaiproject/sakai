/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package org.sakaiproject.site.tool.helper.participant.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/** Parses requested accounts and returns the server-owned participant identities for one wizard submission. */
@Component
@Slf4j
public class ParticipantAccountParser {

    private final ServerConfigurationService serverConfigurationService;
    private final UserDirectoryService userDirectoryService;

    public ParticipantAccountParser(ServerConfigurationService serverConfigurationService,
            UserDirectoryService userDirectoryService) {
        this.serverConfigurationService = serverConfigurationService;
        this.userDirectoryService = userDirectoryService;
    }

    public Result parse(Site site, String officialAccounts, List<String> officialAccountEidOnly,
            String nonOfficialAccounts) {
        List<ParticipantMessage> messages = new ArrayList<>();
        List<UserRoleEntry> entries = new ArrayList<>();
        Set<String> existingUsers = new HashSet<>();
        List<String> candidateEids = new ArrayList<>();
        List<String> eidOnly = officialAccountEidOnly;
        StringBuilder updatedOfficialAccounts = new StringBuilder();
        StringBuilder updatedNonOfficialAccounts = new StringBuilder();

        String trimmedOfficialAccounts = StringUtils.trimToNull(officialAccounts);
        String trimmedNonOfficialAccounts = StringUtils.trimToNull(nonOfficialAccounts);
        if (trimmedOfficialAccounts == null && trimmedNonOfficialAccounts == null) {
            messages.add(error("java.guest"));
        }
        if (trimmedOfficialAccounts != null) {
            parseOfficialAccounts(site, trimmedOfficialAccounts, eidOnly, entries, existingUsers, candidateEids,
                    updatedOfficialAccounts, messages);
        }
        if (trimmedNonOfficialAccounts != null) {
            parseNonOfficialAccounts(site, trimmedNonOfficialAccounts, eidOnly, entries, existingUsers,
                    candidateEids, updatedOfficialAccounts, updatedNonOfficialAccounts, messages);
        }

        addDuplicateAndExistingMessages(candidateEids, existingUsers, messages);
        return new Result(updatedOfficialAccounts.toString(), eidOnly, updatedNonOfficialAccounts.toString(), entries,
                messages);
    }

    private void parseOfficialAccounts(Site site, String officialAccounts, List<String> eidOnly,
            List<UserRoleEntry> entries, Set<String> existingUsers, List<String> candidateEids,
            StringBuilder updatedOfficialAccounts, List<ParticipantMessage> messages) {
        for (String currentOfficialAccount : officialAccounts.split("\\r\\n")) {
            String officialAccount = StringUtils.trimToNull(currentOfficialAccount.replaceAll("[\\t\\r\\n]", ""));
            if (officialAccount == null) continue;

            AccountResolution resolution = findOfficialAccount(officialAccount, eidOnly, messages);
            if (!resolution.candidateEids().isEmpty()) {
                resolution.candidateEids().forEach(eid -> updatedOfficialAccounts.append(eid).append("\n"));
                continue;
            }

            User user = resolution.user();
            if (user == null) {
                messages.add(new ParticipantMessage("java.username", new Object[] {officialAccount},
                        ParticipantMessage.Severity.ERROR));
                continue;
            }

            if (site.getUserRole(user.getId()) != null) {
                existingUsers.add(officialAccount);
                continue;
            }
            candidateEids.add(user.getEid());
            if (!containsEid(entries, user.getEid())) {
                entries.add(new UserRoleEntry(user.getEid(), ""));
                updatedOfficialAccounts.append(currentOfficialAccount).append("\n");
            }
        }
    }

    private AccountResolution findOfficialAccount(String officialAccount, List<String> eidOnly,
            List<ParticipantMessage> messages) {
        User user = findUserByEid(officialAccount);
        if (!officialAccount.contains(ParticipantConstants.EMAIL_CHAR) || eidOnly.contains(officialAccount)) {
            return new AccountResolution(user, List.of());
        }

        return findUserByEmail(officialAccount, user, eidOnly, messages);
    }

    private AccountResolution findUserByEmail(String email, User userByEid, List<String> eidOnly,
            List<ParticipantMessage> messages) {
        Collection<User> usersWithEmail = userDirectoryService.findUsersByEmail(email);
        if (usersWithEmail == null || usersWithEmail.isEmpty()) {
            return new AccountResolution(userByEid, List.of());
        }
        if (usersWithEmail.size() == 1) {
            User resolvedUser = userByEid == null ? usersWithEmail.iterator().next() : userByEid;
            return new AccountResolution(resolvedUser, List.of());
        }

        List<String> candidateEids = new ArrayList<>();
        List<String> candidateDisplayIds = new ArrayList<>();
        addCandidate(userByEid, candidateEids, candidateDisplayIds);
        for (User matchedUser : usersWithEmail) {
            addCandidate(matchedUser, candidateEids, candidateDisplayIds);
        }
        for (String candidateEid : candidateEids) {
            if (!eidOnly.contains(candidateEid)) {
                eidOnly.add(candidateEid);
            }
        }
        messages.add(new ParticipantMessage("java.username.multiple",
                new Object[] {email, String.join(", ", candidateDisplayIds)},
                ParticipantMessage.Severity.INFO));
        return new AccountResolution(null, candidateEids);
    }

    private void addCandidate(User user, List<String> candidateEids, List<String> candidateDisplayIds) {
        if (user == null || candidateEids.contains(user.getEid())) return;

        candidateEids.add(user.getEid());
        candidateDisplayIds.add(StringUtils.defaultIfBlank(user.getDisplayId(), user.getEid()));
    }

    private void parseNonOfficialAccounts(Site site, String nonOfficialAccounts, List<String> eidOnly,
            List<UserRoleEntry> entries, Set<String> existingUsers, List<String> candidateEids,
            StringBuilder updatedOfficialAccounts, StringBuilder updatedNonOfficialAccounts,
            List<ParticipantMessage> messages) {
        List<String> invalidDomains = Arrays.asList(ArrayUtils.nullToEmpty(
                serverConfigurationService.getStrings(ParticipantConstants.INVALID_EMAIL_DOMAINS_KEY)));
        for (String currentAccount : nonOfficialAccounts.split("\\r\\n")) {
            String account = StringUtils.trimToNull(currentAccount.replaceAll("[\\t\\r\\n]", ""));
            if (account == null) continue;

            String[] parts = parseAccountIntoParts(account);
            String email = parts[0];
            if (email.isEmpty()) continue;
            if (!validNonOfficialEmail(email, invalidDomains, messages)) continue;

            User user = findUserByEid(email);
            if (user == null) {
                AccountResolution resolution = findUserByEmail(email, null, eidOnly, messages);
                if (!resolution.candidateEids().isEmpty()) {
                    resolution.candidateEids().forEach(eid -> updatedOfficialAccounts.append(eid).append("\n"));
                    continue;
                }
                user = resolution.user();
            }
            if (user != null && site.getUserRole(user.getId()) != null) {
                existingUsers.add(email);
                continue;
            }
            if (user == null && !userDirectoryService.allowAddUser()) {
                messages.add(new ParticipantMessage("java.haveadd", new Object[] {email}, ParticipantMessage.Severity.ERROR));
                continue;
            }

            String eid = user == null ? email : user.getEid();
            candidateEids.add(eid);
            if (!containsEid(entries, eid)) {
                entries.add(new UserRoleEntry(parts[2], parts[1], "", eid));
                updatedNonOfficialAccounts.append(currentAccount).append("\n");
            }
        }
    }

    private boolean validNonOfficialEmail(String email, List<String> invalidDomains, List<ParticipantMessage> messages) {
        String[] emailParts = email.split(ParticipantConstants.EMAIL_CHAR);
        if (!email.contains(ParticipantConstants.EMAIL_CHAR)) {
            messages.add(new ParticipantMessage("java.emailaddress", new Object[] {email}, ParticipantMessage.Severity.ERROR));
            return false;
        }
        if (emailParts.length != 2 || emailParts[0].isEmpty()) {
            messages.add(new ParticipantMessage("java.notemailid", new Object[] {email}, ParticipantMessage.Severity.ERROR));
            return false;
        }
        if (!EmailValidator.getInstance().isValid(email)) {
            messages.add(new ParticipantMessage("java.emailaddress", new Object[] {email}, ParticipantMessage.Severity.ERROR));
            messages.add(new ParticipantMessage("java.theemail", new Object[] {"no text"}, ParticipantMessage.Severity.ERROR));
            return false;
        }
        if (StringUtils.endsWithAny(emailParts[1], invalidDomains.toArray(new String[0]))) {
            String offendingDomain = invalidDomains.stream().filter(domain -> emailParts[1].endsWith(domain)).findAny()
                    .orElse(null);
            messages.add(new ParticipantMessage("nonOfficialAccount.invalidEmailDomain", new Object[] {offendingDomain},
                    ParticipantMessage.Severity.ERROR));
            return false;
        }
        return true;
    }

    private User findUserByEid(String eid) {
        try {
            return userDirectoryService.getUserByEid(eid);
        } catch (UserNotDefinedException e) {
            log.debug("Cannot find user with eid {}", eid, e);
            return null;
        }
    }

    private void addDuplicateAndExistingMessages(List<String> candidateEids, Set<String> existingUsers,
            List<ParticipantMessage> messages) {
        Set<String> uniqueEids = new HashSet<>();
        Set<String> duplicatedEids = new HashSet<>();
        for (String eid : candidateEids) {
            if (!uniqueEids.add(eid)) duplicatedEids.add(eid);
        }
        if (!duplicatedEids.isEmpty()) {
            String accounts = String.join(", ", duplicatedEids);
            messages.add(new ParticipantMessage(duplicatedEids.size() == 1 ? "add.duplicatedpart.single" : "add.duplicatedpart",
                    new Object[] {accounts}, ParticipantMessage.Severity.INFO));
        }
        if (!existingUsers.isEmpty()) {
            messages.add(new ParticipantMessage("add.existingpart.1", new Object[] {String.join(", ", existingUsers)},
                    ParticipantMessage.Severity.WARNING));
            if (!uniqueEids.isEmpty()) {
                messages.add(new ParticipantMessage("add.existingpart.2", null, ParticipantMessage.Severity.INFO));
            } else {
                messages.add(error("java.guest"));
            }
        }
    }

    private boolean containsEid(List<UserRoleEntry> entries, String eid) {
        return entries.stream().anyMatch(entry -> entry.getEid().equals(eid));
    }

    private String[] parseAccountIntoParts(String account) {
        String[] accountParts = account.split(",", 3);
        String email = accountParts[0].trim();
        while (email.endsWith(".")) email = email.substring(0, email.length() - 1);
        return new String[] {email, accountParts.length > 1 ? accountParts[1].trim() : "",
                accountParts.length > 2 ? accountParts[2].trim() : ""};
    }

    private ParticipantMessage error(String messageKey) {
        return new ParticipantMessage(messageKey, null, ParticipantMessage.Severity.ERROR);
    }

    public record Result(String officialAccounts, List<String> officialAccountEidOnly, String nonOfficialAccounts,
            List<UserRoleEntry> entries, List<ParticipantMessage> messages) {
    }

    private record AccountResolution(User user, List<String> candidateEids) {
    }
}
