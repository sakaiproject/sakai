/**********************************************************************************
 * Copyright (c) 2025 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.poll.tool.mvc;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.tool.service.PollsUiService;
import org.sakaiproject.time.api.UserTimeService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.sakaiproject.util.ResourceLoader;

@Controller
@RequestMapping("/faces")
@Slf4j
public class PollController {

    private final PollListManager pollListManager;
    private final PollVoteManager pollVoteManager;
    private final ExternalLogic externalLogic;
    private final PollsUiService pollsUiService;
    private final MessageSource messageSource;
    private final UserTimeService userTimeService;

    public PollController(PollListManager pollListManager,
                          PollVoteManager pollVoteManager,
                          ExternalLogic externalLogic,
                          PollsUiService pollsUiService,
                          MessageSource messageSource,
                          @Qualifier("org.sakaiproject.time.api.UserTimeService") UserTimeService userTimeService) {
        this.pollListManager = pollListManager;
        this.pollVoteManager = pollVoteManager;
        this.externalLogic = externalLogic;
        this.pollsUiService = pollsUiService;
        this.messageSource = messageSource;
        this.userTimeService = userTimeService;
    }

    @GetMapping({"/", "/votePolls"})
    public String listPolls(Locale locale, Model model) {
        String siteId = externalLogic.getCurrentLocationId();
        if (siteId == null) {
            log.warn("Unable to resolve current site when listing polls");
            model.addAttribute("polls", List.of());
            model.addAttribute("canAdd", Boolean.FALSE);
            model.addAttribute("isSiteOwner", Boolean.FALSE);
            model.addAttribute("renderDelete", Boolean.FALSE);
            return "polls/list";
        }

        List<Poll> polls = new ArrayList<>(pollListManager.findAllPolls(siteId));
        Locale resourceLocale = new ResourceLoader().getLocale();
        Locale effectiveLocale = normaliseLocale(resourceLocale != null ? resourceLocale
                : (locale != null ? locale : Locale.getDefault()));

        DateTimeFormatter sortFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .withLocale(Locale.US)
                .withZone(ZoneOffset.UTC);

        List<PollRow> rows = new ArrayList<>();
        boolean renderDelete = false;
        for (Poll poll : polls) {
            boolean canVote = pollVoteManager.pollIsVotable(poll);
            boolean canEdit = pollCanEdit(poll);
            boolean canDelete = pollListManager.userCanDeletePoll(poll);
            renderDelete = renderDelete || canDelete;

            int optionCount = poll.getOptions() != null ? poll.getOptions().size() : 0;
            if (!canVote && optionCount == 0) {
                optionCount = pollListManager.getOptionsForPoll(poll.getPollId()).size();
            }

            String voteOpenDisplay = null;
            String voteOpenSortKey = null;
            if (poll.getVoteOpen() != null) {
                voteOpenDisplay = userTimeService.shortLocalizedTimestamp(poll.getVoteOpen().toInstant(), effectiveLocale);
                voteOpenSortKey = sortFormatter.format(poll.getVoteOpen().toInstant());
            }

            String voteCloseDisplay = null;
            String voteCloseSortKey = null;
            if (poll.getVoteClose() != null) {
                voteCloseDisplay = userTimeService.shortLocalizedTimestamp(poll.getVoteClose().toInstant(), effectiveLocale);
                voteCloseSortKey = sortFormatter.format(poll.getVoteClose().toInstant());
            }

            boolean canViewResults = pollListManager.isAllowedViewResults(poll, externalLogic.getCurrentUserId());
            rows.add(new PollRow(
                    poll.getPollId(),
                    poll.getText(),
                    canVote,
                    canEdit,
                    canDelete,
                    canViewResults,
                    voteOpenDisplay,
                    voteOpenSortKey,
                    voteCloseDisplay,
                    voteCloseSortKey,
                    optionCount
            ));
        }

        rows.sort(Comparator.comparing(PollRow::getVoteCloseSortKey, Comparator.nullsLast(Comparator.reverseOrder())));

        model.addAttribute("polls", rows);
        model.addAttribute("canAdd", isAllowedPollAdd());
        model.addAttribute("isSiteOwner", isSiteOwner());
        model.addAttribute("renderDelete", renderDelete);
        model.addAttribute("siteId", siteId);
        return "polls/list";
    }

    @PostMapping("/polls/bulk")
    public String handleBulkAction(@RequestParam(name = "deleteIds", required = false) List<Long> deleteIds,
                                   @RequestParam(name = "action") String action,
                                   RedirectAttributes redirectAttributes,
                                   Locale locale) {
        if (deleteIds == null || deleteIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("alert",
                    messageSource.getMessage("poll_list_delete_tooltip", null, locale));
            return "redirect:/faces/votePolls";
        }

        switch (action) {
            case "delete" -> {
                pollsUiService.deletePolls(deleteIds);
                redirectAttributes.addFlashAttribute("success",
                        messageSource.getMessage("poll_list_delete", null, locale));
            }
            case "reset" -> {
                pollsUiService.resetPollVotes(deleteIds);
                redirectAttributes.addFlashAttribute("success",
                        messageSource.getMessage("poll_list_reset", null, locale));
            }
            default -> redirectAttributes.addFlashAttribute("alert",
                    messageSource.getMessage("poll_list_delete_tooltip", null, locale));
        }
        return "redirect:/faces/votePolls";
    }

    private boolean isAllowedPollAdd() {
        return externalLogic.isUserAdmin() || externalLogic.isAllowedInLocation(PollListManager.PERMISSION_ADD, externalLogic.getCurrentLocationReference());
    }

    private boolean isSiteOwner() {
        return externalLogic.isUserAdmin() || externalLogic.isAllowedInLocation("site.upd", externalLogic.getCurrentLocationReference());
    }

    private boolean pollCanEdit(Poll poll) {
        if (externalLogic.isUserAdmin()) {
            return true;
        }
        if (externalLogic.isAllowedInLocation(PollListManager.PERMISSION_EDIT_ANY, externalLogic.getCurrentLocationReference())) {
            return true;
        }
        return externalLogic.isAllowedInLocation(PollListManager.PERMISSION_EDIT_OWN, externalLogic.getCurrentLocationReference())
                && Objects.equals(poll.getOwner(), externalLogic.getCurrentUserId());
    }

    private Locale normaliseLocale(Locale locale) {
        if (locale == null) {
            return Locale.getDefault();
        }
        if ("en".equals(locale.getLanguage()) && "ZA".equals(locale.getCountry())) {
            return Locale.UK;
        }
        return locale;
    }

    @Value
    public static class PollRow {
        Long id;
        String text;
        boolean votable;
        boolean editable;
        boolean deletable;
        boolean resultsVisible;
        String voteOpenDisplay;
        String voteOpenSortKey;
        String voteCloseDisplay;
        String voteCloseSortKey;
        int optionCount;
    }
}
