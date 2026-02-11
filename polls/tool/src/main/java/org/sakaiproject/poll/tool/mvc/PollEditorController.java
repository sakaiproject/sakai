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
import java.util.Optional;
import java.util.TimeZone;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.tool.model.PollForm;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.beans.factory.annotation.Qualifier;
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

import static org.sakaiproject.poll.api.PollConstants.*;

@Controller
@RequestMapping
@Slf4j
public class PollEditorController {

    private final PollsService pollsService;
    private final SecurityService securityService;
    private final SiteService siteService;
    private final SessionManager sessionManager;
    private final ToolManager toolManager;
    private final ServerConfigurationService serverConfigurationService;
    private final FormattedText formattedText;
    private final UserTimeService userTimeService;
    private final MessageSource messageSource;

    public PollEditorController(PollsService pollsService,
                                SecurityService securityService,
                                SiteService siteService,
                                SessionManager sessionManager,
                                ToolManager toolManager,
                                ServerConfigurationService serverConfigurationService,
                                @Qualifier("org.sakaiproject.time.api.UserTimeService") UserTimeService userTimeService,
                                MessageSource messageSource,
                                FormattedText formattedText) {
        this.pollsService = pollsService;
        this.securityService = securityService;
        this.siteService = siteService;
        this.sessionManager = sessionManager;
        this.toolManager = toolManager;
        this.serverConfigurationService = serverConfigurationService;
        this.userTimeService = userTimeService;
        this.messageSource = messageSource;
        this.formattedText = formattedText;
    }

    @GetMapping("/voteAdd")
    public String editPoll(@RequestParam(value = "pollId", required = false) String pollId,
                           Model model,
                           Locale locale) {
        boolean isNew = StringUtils.isEmpty(pollId);
        PollForm form;
        List options = List.of();
        boolean hasVotes = false;

        if (isNew) {
            if (!isAllowedPollAdd()) {
                model.addAttribute("alert", messageSource.getMessage("new_poll_noperms", null, locale));
                return "redirect:/votePolls";
            }
            form = newPollForm();
        } else {
            Optional<Poll> poll = pollsService.getPollById(pollId);
            if (poll.isEmpty()) {
                model.addAttribute("alert", messageSource.getMessage("poll_missing", null, locale));
                return "redirect:/votePolls";
            }

            if (!canEditPoll(poll.get())) {
                model.addAttribute("alert", messageSource.getMessage("new_poll_noperms", null, locale));
                return "redirect:/votePolls";
            }

            ZoneId zoneId = getUserZoneId();
            form = editPollForm(poll.get(), zoneId);
            options = pollsService.getVisibleOptionsForPoll(poll.get().getId());
            hasVotes = !pollsService.getAllVotesForPoll(poll.get().getId()).isEmpty();
        }

        model.addAttribute("pollForm", form);
        model.addAttribute("isNew", isNew);
        model.addAttribute("options", options);
        model.addAttribute("hasVotes", hasVotes);
        model.addAttribute("displayResultChoices", List.of(
                new DisplayOption("open", "new_poll_open"),
                new DisplayOption("afterVoting", "new_poll_aftervoting"),
                new DisplayOption("afterClosing", "new_poll_afterClosing"),
                new DisplayOption("never", "new_poll_never")
        ));

        model.addAttribute("canAdd", isAllowedPollAdd());
        model.addAttribute("isSiteOwner", isSiteOwner());
        model.addAttribute("showPublicAccess", serverConfigurationService.getBoolean("poll.allow.public.access", false));
        model.addAttribute("timezone", getUserZoneId());
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
            populateModelForEdit(model, pollForm, List.of(), false);
            return "polls/edit";
        }

        // Validate form inputs
        if (StringUtils.isBlank(pollForm.getText())) {
            bindingResult.addError(new FieldError("pollForm", "text", messageSource.getMessage("error_no_text", null, locale)));
            populateModelForEdit(model, pollForm, List.of(), false);
            return "polls/edit";
        }

        if (pollForm.getOpenDate() == null || pollForm.getCloseDate() == null) {
            bindingResult.addError(new FieldError("pollForm", "closeDate", messageSource.getMessage("close_before_open", null, locale)));
            populateModelForEdit(model, pollForm, List.of(), false);
            return "polls/edit";
        }

        if (pollForm.getOpenDate().isAfter(pollForm.getCloseDate())) {
            bindingResult.addError(new FieldError("pollForm", "closeDate", messageSource.getMessage("close_before_open", null, locale)));
            populateModelForEdit(model, pollForm, List.of(), false);
            return "polls/edit";
        }

        if (pollForm.getMinOptions() > pollForm.getMaxOptions()) {
            bindingResult.addError(new FieldError("pollForm", "maxOptions", messageSource.getMessage("min_greater_than_max", null, locale)));
            populateModelForEdit(model, pollForm, List.of(), false);
            return "polls/edit";
        }

        Poll poll = preparePollEntity(pollForm);
        if (poll == null) {
            redirectAttributes.addFlashAttribute("alert", messageSource.getMessage("poll_missing", null, locale));
            return "redirect:/votePolls";
        }

        Poll saved = pollsService.savePoll(poll);
        redirectAttributes.addFlashAttribute("success", messageSource.getMessage("poll_saved_success", null, locale));

        if ("option".equals(redirectTarget)) {
            return "redirect:/pollOption?pollId=" + saved.getId();
        }
        if ("optionBatch".equals(redirectTarget)) {
            return "redirect:/pollOptionBatch?pollId=" + saved.getId();
        }
        if (saved.getOptions().isEmpty()) {
            return "redirect:/pollOption?pollId=" + saved.getId();
        }
        return "redirect:/votePolls";
    }

    private void populateModelForEdit(Model model, PollForm pollForm, List options, boolean hasVotes) {
        model.addAttribute("options", options);
        model.addAttribute("hasVotes", hasVotes);
        model.addAttribute("displayResultChoices", List.of(
                new DisplayOption("open", "new_poll_open"),
                new DisplayOption("afterVoting", "new_poll_aftervoting"),
                new DisplayOption("afterClosing", "new_poll_afterClosing"),
                new DisplayOption("never", "new_poll_never")
        ));
        model.addAttribute("isNew", pollForm.getPollId() == null);
        model.addAttribute("canAdd", isAllowedPollAdd());
        model.addAttribute("isSiteOwner", isSiteOwner());
        model.addAttribute("showPublicAccess", serverConfigurationService.getBoolean("poll.allow.public.access", false));
        model.addAttribute("timezone", getUserZoneId());
    }

    private Poll preparePollEntity(PollForm form) {
        Poll poll;
        if (StringUtils.isNotBlank(form.getPollId())) {
            Optional<Poll> p = pollsService.getPollById(form.getPollId());
            if (p.isEmpty()) {
                return null;
            } else {
                poll = p.get();
            }
        } else {
            poll = new Poll();
        }

        poll.setText(StringUtils.trimToEmpty(form.getText()));

        // Process and sanitize HTML in description
        String sanitizedDescription = formattedText.processFormattedText(
            form.getDetails() != null ? form.getDetails() : "",
            null,
                null
        );
        poll.setDescription(org.sakaiproject.poll.api.util.PollUtils.cleanupHtmlPtags(sanitizedDescription));

        poll.setPublic(form.isPublic());
        poll.setMinOptions(form.getMinOptions());
        poll.setMaxOptions(form.getMaxOptions());
        poll.setDisplayResult(form.getDisplayResult());
        poll.setSiteId(toolManager.getCurrentPlacement().getContext());
        poll.setOwner(sessionManager.getCurrentSessionUserId());
        poll.setLimitVoting(true);

        // Convert LocalDateTime to Instant for persistence
        ZoneId zoneId = getUserZoneId();
        if (form.getOpenDate() != null) {
            poll.setVoteOpen(form.getOpenDate().atZone(zoneId).toInstant());
        }
        if (form.getCloseDate() != null) {
            poll.setVoteClose(form.getCloseDate().atZone(zoneId).toInstant());
        }

        return poll;
    }

    private PollForm newPollForm() {
        PollForm form = new PollForm();
        form.setPollId(null);
        form.setText("");
        form.setDetails("");
        form.setPublic(false);
        form.setMinOptions(1);
        form.setMaxOptions(1);
        form.setDisplayResult("open");
        form.setOpenDate(null);
        form.setCloseDate(null);
        return form;
    }

    private PollForm editPollForm(Poll poll, ZoneId zoneId) {
        PollForm form = new PollForm();
        form.setPollId(poll.getId());
        form.setText(poll.getText());
        form.setDetails(poll.getDescription());
        form.setPublic(poll.isPublic());
        form.setMinOptions(poll.getMinOptions());
        form.setMaxOptions(poll.getMaxOptions());
        form.setDisplayResult(poll.getDisplayResult());
        form.setOpenDate(truncateToMinutes(toLocalDateTime(poll.getVoteOpen(), zoneId)));
        form.setCloseDate(truncateToMinutes(toLocalDateTime(poll.getVoteClose(), zoneId)));
        return form;
    }

    private LocalDateTime toLocalDateTime(java.time.Instant instant, ZoneId zoneId) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, zoneId);
    }

    private boolean isAllowedPollAdd() {
        String siteRef = siteService.siteReference(toolManager.getCurrentPlacement().getContext());
        return securityService.isSuperUser() || securityService.unlock(PERMISSION_ADD, siteRef);
    }

    private boolean isSiteOwner() {
        String siteRef = siteService.siteReference(toolManager.getCurrentPlacement().getContext());
        return securityService.isSuperUser() || securityService.unlock("site.upd", siteRef);
    }

    private boolean canEditPoll(Poll poll) {
        if (securityService.isSuperUser()) {
            return true;
        }
        String siteRef = siteService.siteReference(toolManager.getCurrentPlacement().getContext());
        if (securityService.unlock(PERMISSION_EDIT_ANY, siteRef)) {
            return true;
        }
        return securityService.unlock(PERMISSION_EDIT_OWN, siteRef)
                && StringUtils.equals(poll.getOwner(), sessionManager.getCurrentSessionUserId());
    }

    private ZoneId getUserZoneId() {
        TimeZone tz = userTimeService.getLocalTimeZone();
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
