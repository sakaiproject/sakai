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
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
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

import static org.sakaiproject.poll.api.PollConstants.*;

@Controller
@RequestMapping
@Slf4j
public class PollController {

    private final PollsService pollsService;
    private final SecurityService securityService;
    private final SiteService siteService;
    private final SessionManager sessionManager;
    private final ToolManager toolManager;
    private final MessageSource messageSource;
    private final UserTimeService userTimeService;

    public PollController(PollsService pollsService,
                          SecurityService securityService,
                          SiteService siteService,
                          SessionManager sessionManager,
                          ToolManager toolManager,
                          MessageSource messageSource,
                          @Qualifier("org.sakaiproject.time.api.UserTimeService") UserTimeService userTimeService) {
        this.pollsService = pollsService;
        this.securityService = securityService;
        this.siteService = siteService;
        this.sessionManager = sessionManager;
        this.toolManager = toolManager;
        this.messageSource = messageSource;
        this.userTimeService = userTimeService;
    }

    @GetMapping({"/", "/votePolls"})
    public String listPolls(Locale locale, Model model) {
        String siteId = toolManager.getCurrentPlacement().getContext();
        if (siteId == null) {
            log.warn("Unable to resolve current site when listing polls");
            model.addAttribute("polls", List.of());
            model.addAttribute("canAdd", Boolean.FALSE);
            model.addAttribute("isSiteOwner", Boolean.FALSE);
            model.addAttribute("renderDelete", Boolean.FALSE);
            return "polls/list";
        }

        List<Poll> polls = new ArrayList<>(pollsService.findAllPolls(siteId));
        Locale resourceLocale = new ResourceLoader().getLocale();
        Locale effectiveLocale = normaliseLocale(resourceLocale != null ? resourceLocale
                : (locale != null ? locale : Locale.getDefault()));

        DateTimeFormatter sortFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .withLocale(Locale.US)
                .withZone(ZoneOffset.UTC);

        List<PollRow> rows = new ArrayList<>();
        boolean renderDelete = false;
        for (Poll poll : polls) {
            boolean canVote = pollsService.pollIsVotable(poll);
            boolean canEdit = pollCanEdit(poll);
            boolean canDelete = pollsService.userCanDeletePoll(poll);
            renderDelete = renderDelete || canDelete;

            int optionCount = poll.getOptions() != null ? poll.getOptions().size() : 0;
            if (!canVote && optionCount == 0) {
                optionCount = poll.getOptions().size();
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

            boolean canViewResults = pollsService.isAllowedViewResults(poll, sessionManager.getCurrentSessionUserId());
            rows.add(new PollRow(
                    poll.getId(),
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
    public String handleBulkAction(@RequestParam(name = "deleteIds", required = false) List<String> deleteIds,
                                   @RequestParam(name = "action") String action,
                                   RedirectAttributes redirectAttributes,
                                   Locale locale) {
        if (deleteIds == null || deleteIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("alert",
                    messageSource.getMessage("poll_list_delete_tooltip", null, locale));
            return "redirect:/votePolls";
        }

        switch (action) {
            case "delete" -> {
                pollsService.deletePolls(deleteIds);
                redirectAttributes.addFlashAttribute("success",
                        messageSource.getMessage("poll_deleted_success", null, locale));
            }
            case "reset" -> {
                pollsService.resetPollVotes(deleteIds);
                redirectAttributes.addFlashAttribute("success",
                        messageSource.getMessage("poll_votes_reset_success", null, locale));
            }
            default -> redirectAttributes.addFlashAttribute("alert",
                    messageSource.getMessage("poll_list_delete_tooltip", null, locale));
        }
        return "redirect:/votePolls";
    }

    private boolean isAllowedPollAdd() {
        String siteRef = siteService.siteReference(toolManager.getCurrentPlacement().getContext());
        return securityService.isSuperUser() || securityService.unlock(PERMISSION_ADD, siteRef);
    }

    private boolean isSiteOwner() {
        String siteRef = siteService.siteReference(toolManager.getCurrentPlacement().getContext());
        return securityService.isSuperUser() || securityService.unlock("site.upd", siteRef);
    }

    private boolean pollCanEdit(Poll poll) {
        if (securityService.isSuperUser()) {
            return true;
        }
        String siteRef = siteService.siteReference(toolManager.getCurrentPlacement().getContext());
        if (securityService.unlock(PERMISSION_EDIT_ANY, siteRef)) {
            return true;
        }
        return securityService.unlock(PERMISSION_EDIT_OWN, siteRef)
                && Objects.equals(poll.getOwner(), sessionManager.getCurrentSessionUserId());
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
        String id;
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
