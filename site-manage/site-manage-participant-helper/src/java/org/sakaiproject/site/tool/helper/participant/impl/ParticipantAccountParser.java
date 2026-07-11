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
            parseNonOfficialAccounts(site, trimmedNonOfficialAccounts, entries, existingUsers, candidateEids,
                    updatedNonOfficialAccounts, messages);
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

            User user = findOfficialAccount(officialAccount, eidOnly, messages);
            if (user == null) {
                if (!hasMultipleMatchMessage(messages, officialAccount)) {
                    messages.add(new ParticipantMessage("java.username", new Object[] {officialAccount},
                            ParticipantMessage.Severity.ERROR));
                }
                continue;
            }

            if (site.getUserRole(user.getId()) != null) {
                existingUsers.add(officialAccount);
                continue;
            }
            candidateEids.add(user.getEid());
            if (!containsEid(entries, officialAccount)) {
                entries.add(new UserRoleEntry(user.getEid(), ""));
                updatedOfficialAccounts.append(currentOfficialAccount).append("\n");
            }
        }
    }

    private User findOfficialAccount(String officialAccount, List<String> eidOnly, List<ParticipantMessage> messages) {
        User user = findUserByEid(officialAccount);
        if (!officialAccount.contains(SiteAddParticipantHandler.EMAIL_CHAR) || eidOnly.contains(officialAccount)) {
            return user;
        }

        Collection<User> usersWithEmail = userDirectoryService.findUsersByEmail(officialAccount);
        if (usersWithEmail == null || usersWithEmail.isEmpty()) return user;
        if (usersWithEmail.size() == 1) return user == null ? usersWithEmail.iterator().next() : user;

        StringBuilder eids = new StringBuilder();
        StringBuilder alertEids = new StringBuilder();
        for (User matchedUser : usersWithEmail) {
            eids.append(matchedUser.getDisplayId()).append("\n");
            alertEids.append(matchedUser.getDisplayId()).append(", ");
            eidOnly.add(matchedUser.getEid());
        }
        String alert = alertEids.substring(0, alertEids.length() - 2);
        messages.add(new ParticipantMessage("java.username.multiple", new Object[] {officialAccount, alert},
                ParticipantMessage.Severity.INFO));
        return null;
    }

    private void parseNonOfficialAccounts(Site site, String nonOfficialAccounts, List<UserRoleEntry> entries,
            Set<String> existingUsers, List<String> candidateEids, StringBuilder updatedNonOfficialAccounts,
            List<ParticipantMessage> messages) {
        List<String> invalidDomains = Arrays.asList(ArrayUtils.nullToEmpty(
                serverConfigurationService.getStrings(SiteAddParticipantHandler.SAK_PROP_INVALID_EMAIL_DOMAINS)));
        for (String currentAccount : nonOfficialAccounts.split("\\r\\n")) {
            String account = StringUtils.trimToNull(currentAccount.replaceAll("[\\t\\r\\n]", ""));
            if (account == null) continue;

            String[] parts = parseAccountIntoParts(account);
            String email = parts[0];
            if (email.isEmpty()) continue;
            if (!validNonOfficialEmail(email, invalidDomains, messages)) continue;

            User user = findUserByEid(email);
            if (user == null) user = findUserByEmail(email);
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
        String[] emailParts = email.split(SiteAddParticipantHandler.EMAIL_CHAR);
        if (!email.contains(SiteAddParticipantHandler.EMAIL_CHAR)) {
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

    private User findUserByEmail(String email) {
        Collection<User> matches = userDirectoryService.findUsersByEmail(email);
        if (matches == null || matches.isEmpty()) return null;
        if (matches.size() > 1) log.warn("Found multiple users with email {}", email);
        return matches.iterator().next();
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
                    ParticipantMessage.Severity.INFO));
            if (!uniqueEids.isEmpty()) {
                messages.add(new ParticipantMessage("add.existingpart.2", null, ParticipantMessage.Severity.INFO));
            } else {
                messages.add(error("java.guest"));
            }
        }
    }

    private boolean hasMultipleMatchMessage(List<ParticipantMessage> messages, String officialAccount) {
        return messages.stream().anyMatch(message -> "java.username.multiple".equals(message.getCode())
                && message.getArgs().length > 0 && officialAccount.equals(message.getArgs()[0]));
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
}
