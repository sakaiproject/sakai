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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.tool.model.PollForm;
import org.sakaiproject.poll.tool.service.PollValidationException;
import org.sakaiproject.poll.tool.service.PollsUiService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class PollEditorController {

    private final PollListManager pollListManager;
    private final PollVoteManager pollVoteManager;
    private final ExternalLogic externalLogic;
    private final PollsUiService pollsUiService;
    private final MessageSource messageSource;

    @GetMapping("/voteAdd")
    public String editPoll(@RequestParam(value = "pollId", required = false) Long pollId,
                           @RequestParam(value = "id", required = false) Long legacyId,
                           Model model,
                           Locale locale) {
        Long resolvedId = pollId != null ? pollId : legacyId;
        Poll poll = resolvedId != null ? pollListManager.getPollById(resolvedId) : new Poll();

        if (resolvedId != null && poll == null) {
            model.addAttribute("alert", messageSource.getMessage("poll_missing", null, locale));
            return "redirect:/votePolls";
        }

        if (resolvedId != null && !canEditPoll(poll)) {
            model.addAttribute("alert", messageSource.getMessage("new_poll_noperms", null, locale));
            return "redirect:/votePolls";
        }

        ZoneId zoneId = getUserZoneId();
        PollForm form = toForm(poll, zoneId);

        model.addAttribute("pollForm", form);
        model.addAttribute("isNew", resolvedId == null);
        model.addAttribute("options", poll.getPollId() != null ? pollListManager.getVisibleOptionsForPoll(poll.getPollId()) : List.of());
        model.addAttribute("hasVotes", poll.getPollId() != null && !pollVoteManager.getAllVotesForPoll(poll).isEmpty());
        model.addAttribute("displayResultChoices", List.of(
                new DisplayOption("open", "new_poll_open"),
                new DisplayOption("afterVoting", "new_poll_aftervoting"),
                new DisplayOption("afterClosing", "new_poll_afterClosing"),
                new DisplayOption("never", "new_poll_never")
        ));

        model.addAttribute("canAdd", isAllowedPollAdd());
        model.addAttribute("isSiteOwner", isSiteOwner());
        model.addAttribute("showPublicAccess", externalLogic.isShowPublicAccess());
        model.addAttribute("timezone", zoneId);
        return "polls/edit";
    }

    @PostMapping("/voteAdd")
    public String savePoll(@ModelAttribute("pollForm") PollForm pollForm,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           Locale locale,
                           Model model,
                           @RequestParam(value = "redirect", required = false) String redirectTarget) {

        if (!isAllowedPollAdd()) {
            bindingResult.addError(new FieldError("pollForm", "text", messageSource.getMessage("new_poll_noperms", null, locale)));
            return "polls/edit";
        }

        Poll poll = preparePollEntity(pollForm);
        if (poll == null) {
            redirectAttributes.addFlashAttribute("alert", messageSource.getMessage("poll_missing", null, locale));
            return "redirect:/votePolls";
        }
        try {
            Poll saved = pollsUiService.savePoll(poll, locale);
            redirectAttributes.addFlashAttribute("success", messageSource.getMessage("poll_saved_success", null, locale));

            if ("option".equals(redirectTarget)) {
                return "redirect:/pollOption?pollId=" + saved.getPollId();
            }
            if ("optionBatch".equals(redirectTarget)) {
                return "redirect:/pollOptionBatch?pollId=" + saved.getPollId();
            }
            if (pollListManager.getOptionsForPoll(saved).isEmpty()) {
                return "redirect:/pollOption?pollId=" + saved.getPollId();
            }
            return "redirect:/votePolls";
        } catch (PollValidationException ex) {
            String field = switch (ex.getMessage()) {
                case "close_before_open" -> "closeDate";
                case "min_greater_than_max", "invalid_poll_limits" -> "maxOptions";
                case "error_no_text" -> "text";
                default -> null;
            };
            if (field != null) {
                bindingResult.addError(new FieldError("pollForm", field, messageSource.getMessage(ex.getMessage(), ex.getArgs(), locale)));
            } else {
                bindingResult.addError(new FieldError("pollForm", "text", messageSource.getMessage(ex.getMessage(), ex.getArgs(), locale)));
            }
            Poll contextPoll = pollForm.getPollId() != null ? pollListManager.getPollById(pollForm.getPollId()) : poll;
            if (contextPoll == null) {
                redirectAttributes.addFlashAttribute("alert", messageSource.getMessage("poll_missing", null, locale));
                return "redirect:/votePolls";
            }
            model.addAttribute("poll", contextPoll);
            model.addAttribute("options", contextPoll.getPollId() != null ? pollListManager.getVisibleOptionsForPoll(contextPoll.getPollId()) : List.of());
            model.addAttribute("hasVotes", contextPoll.getPollId() != null && !pollVoteManager.getAllVotesForPoll(contextPoll).isEmpty());
            model.addAttribute("displayResultChoices", List.of(
                    new DisplayOption("open", "new_poll_open"),
                    new DisplayOption("afterVoting", "new_poll_aftervoting"),
                    new DisplayOption("afterClosing", "new_poll_afterClosing"),
                    new DisplayOption("never", "new_poll_never")
            ));
            model.addAttribute("isNew", pollForm.getPollId() == null);
            model.addAttribute("canAdd", isAllowedPollAdd());
            model.addAttribute("isSiteOwner", isSiteOwner());
            model.addAttribute("showPublicAccess", externalLogic.isShowPublicAccess());
            model.addAttribute("timezone", getUserZoneId());
            return "polls/edit";
        }
    }

    private Poll preparePollEntity(PollForm form) {
        Poll poll = form.getPollId() != null ? pollListManager.getPollById(form.getPollId()) : new Poll();
        if (poll == null) {
            return null;
        }
        poll.setText(StringUtils.trimToEmpty(form.getText()));
        poll.setDetails(form.getDetails());
        poll.setPublic(form.isPublic());
        poll.setMinOptions(form.getMinOptions());
        poll.setMaxOptions(form.getMaxOptions());
        poll.setDisplayResult(form.getDisplayResult());
        poll.setSiteId(externalLogic.getCurrentLocationId());
        poll.setOwner(externalLogic.getCurrentUserId());
        poll.setLimitVoting(true);

        ZoneId zoneId = getUserZoneId();
        LocalDateTime open = form.getOpenDate();
        LocalDateTime close = form.getCloseDate();
        pollsUiService.hydratePollForForm(poll, open, close, zoneId);
        return poll;
    }

    private PollForm toForm(Poll poll, ZoneId zoneId) {
        PollForm form = new PollForm();
        form.setPollId(poll.getPollId());
        form.setText(poll.getText());
        form.setDetails(poll.getDetails());
        form.setPublic(poll.isPublic());
        form.setMinOptions(poll.getMinOptions());
        form.setMaxOptions(poll.getMaxOptions());
        form.setDisplayResult(poll.getDisplayResult());
        form.setOpenDate(truncateToMinutes(pollsUiService.toLocalDateTime(poll.getVoteOpen(), zoneId)));
        form.setCloseDate(truncateToMinutes(pollsUiService.toLocalDateTime(poll.getVoteClose(), zoneId)));
        return form;
    }

    private boolean isAllowedPollAdd() {
        return externalLogic.isUserAdmin() || externalLogic.isAllowedInLocation(PollListManager.PERMISSION_ADD, externalLogic.getCurrentLocationReference());
    }

    private boolean isSiteOwner() {
        return externalLogic.isUserAdmin() || externalLogic.isAllowedInLocation("site.upd", externalLogic.getCurrentLocationReference());
    }

    private boolean canEditPoll(Poll poll) {
        if (externalLogic.isUserAdmin()) {
            return true;
        }
        if (externalLogic.isAllowedInLocation(PollListManager.PERMISSION_EDIT_ANY, externalLogic.getCurrentLocationReference())) {
            return true;
        }
        return externalLogic.isAllowedInLocation(PollListManager.PERMISSION_EDIT_OWN, externalLogic.getCurrentLocationReference())
                && StringUtils.equals(poll.getOwner(), externalLogic.getCurrentUserId());
    }

    private ZoneId getUserZoneId() {
        TimeZone tz = externalLogic.getLocalTimeZone();
        return tz != null ? tz.toZoneId() : ZoneId.systemDefault();
    }

    private LocalDateTime truncateToMinutes(LocalDateTime value) {
        return value != null ? value.truncatedTo(ChronoUnit.MINUTES) : null;
    }

    @Value
    public static class DisplayOption {
        String value;
        String labelKey;
    }
}
