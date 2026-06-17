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

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.poll.api.model.Option;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.util.PollUtils;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.poll.tool.model.OptionBatchForm;
import org.sakaiproject.poll.tool.model.OptionForm;
import org.sakaiproject.poll.tool.service.PollPermissionsService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class OptionController {

    private static final String HANDLE_DELETE_OPTION_DO_NOTHING = "do-nothing";
    private static final String HANDLE_DELETE_OPTION_RETURN_VOTES = "return-votes";

    private final PollsService pollsService;
    private final FormattedText formattedText;
    private final MessageSource messageSource;
    private final PollPermissionsService pollPermissionsService;

    @GetMapping("/pollOption")
    public String editOption(@RequestParam(value = "optionId", required = false) Long optionId,
                             @RequestParam(value = "pollId", required = false) String pollId,
                             Model model,
                             Locale locale) {
        OptionForm form = new OptionForm();
        Poll poll;
        if (optionId != null) {
            Optional<Option> option = pollsService.getOptionById(optionId);
            if (option.isEmpty()) {
                return "redirect:/votePolls";
            }
            form.setOptionId(option.get().getId());
            Poll optionPoll = option.get().getPoll();
            if (optionPoll == null) {
                return "redirect:/votePolls";
            }
            form.setPollId(optionPoll.getId());
            form.setText(option.get().getText());
            poll = optionPoll;
        } else {
            if (pollId == null) {
                return "redirect:/votePolls";
            }
            form.setPollId(pollId);
            poll = pollsService.getPollById(pollId).orElse(null);
        }
        if (!pollPermissionsService.canEditPoll(poll)) {
            return "redirect:/votePolls";
        }

        model.addAttribute("poll", poll);
        model.addAttribute("optionForm", form);
        addMenuPermissions(model);
        return "polls/option-edit";
    }

    @PostMapping("/pollOption")
    public String saveOption(@ModelAttribute("optionForm") OptionForm optionForm,
                             BindingResult bindingResult,
                             @RequestParam("submitAction") String submitAction,
                             RedirectAttributes redirectAttributes,
                             Locale locale,
                             Model model) {
        Optional<Poll> submittedPoll = pollsService.getPollById(optionForm.getPollId());
        if (submittedPoll.isEmpty()) {
            bindingResult.addError(new FieldError("optionForm", "text", "Poll not found"));
            model.addAttribute("poll", null);
            addMenuPermissions(model);
            return "polls/option-edit";
        }

        Option option;
        Poll poll = submittedPoll.get();
        if (optionForm.getOptionId() != null) {
            Optional<Option> existingOption = pollsService.getOptionById(optionForm.getOptionId());
            if (existingOption.isEmpty()) {
                bindingResult.addError(new FieldError("optionForm", "text", "Option not found"));
                model.addAttribute("poll", poll);
                addMenuPermissions(model);
                return "polls/option-edit";
            }
            option = existingOption.get();
            poll = option.getPoll();
            if (poll == null) {
                bindingResult.addError(new FieldError("optionForm", "text", "Poll not found"));
                model.addAttribute("poll", null);
                addMenuPermissions(model);
                return "polls/option-edit";
            }
        } else {
            // Creating new option
            // optionOrder is managed by @OrderColumn - position in list determines order
            option = new Option();
            poll.addOption(option);
        }

        if (!pollPermissionsService.canEditPoll(poll)) {
            bindingResult.addError(new FieldError("optionForm", "text", messageSource.getMessage("new_poll_noperms", null, locale)));
            model.addAttribute("poll", poll);
            addMenuPermissions(model);
            return "polls/option-edit";
        }

        // Validate option text
        if (StringUtils.isBlank(optionForm.getText())) {
            bindingResult.addError(new FieldError("optionForm", "text", messageSource.getMessage("option_empty", null, locale)));
            model.addAttribute("poll", poll);
            addMenuPermissions(model);
            return "polls/option-edit";
        }

        // Process and sanitize HTML in option text
        String sanitizedText = formattedText.processFormattedText(
            optionForm.getText(),
            null,
            true,
            true
        );
        option.setText(PollUtils.cleanupHtmlPtags(sanitizedText));

        // Validate after processing
        if (StringUtils.isBlank(option.getText())) {
            bindingResult.addError(new FieldError("optionForm", "text", messageSource.getMessage("option_empty", null, locale)));
            model.addAttribute("poll", poll);
            addMenuPermissions(model);
            return "polls/option-edit";
        }

        pollsService.savePoll(poll);
        redirectAttributes.addFlashAttribute("success", messageSource.getMessage("poll_option_added_success", null, locale));

        if ("addAnother".equals(submitAction)) {
            return "redirect:/pollOption?pollId=" + poll.getId();
        }
        return "redirect:/voteAdd?pollId=" + poll.getId();
    }

    @GetMapping("/pollOptionBatch")
    public String batchOption(@RequestParam("pollId") String pollId,
                              Model model) {
        Optional<Poll> poll = pollsService.getPollById(pollId);
        if (poll.isEmpty()) {
            return "redirect:/votePolls";
        }
        if (!pollPermissionsService.canEditPoll(poll.get())) {
            return "redirect:/votePolls";
        }
        OptionBatchForm form = new OptionBatchForm();
        form.setPollId(pollId);
        model.addAttribute("poll", poll.get());
        model.addAttribute("batchForm", form);
        addMenuPermissions(model);
        return "polls/option-batch";
    }

    @PostMapping("/pollOptionBatch")
    public String uploadBatch(@ModelAttribute("batchForm") OptionBatchForm batchForm,
                              BindingResult bindingResult,
                              @RequestParam("file") MultipartFile file,
                              RedirectAttributes redirectAttributes,
                              Locale locale,
                              Model model) {
        batchForm.setFile(file);
        Optional<Poll> poll = pollsService.getPollById(batchForm.getPollId());
        if (poll.isEmpty()) {
            bindingResult.addError(new FieldError("batchForm", "file", "Poll not found"));
            model.addAttribute("poll", null);
            addMenuPermissions(model);
            return "polls/option-batch";
        }
        if (!pollPermissionsService.canEditPoll(poll.get())) {
            bindingResult.addError(new FieldError("batchForm", "file", messageSource.getMessage("new_poll_noperms", null, locale)));
            model.addAttribute("poll", poll.get());
            addMenuPermissions(model);
            return "polls/option-batch";
        }

        // Validate file
        if (file == null || file.isEmpty()) {
            bindingResult.addError(new FieldError("batchForm", "file", messageSource.getMessage("error_batch_options", null, locale)));
            model.addAttribute("poll", poll.get());
            addMenuPermissions(model);
            return "polls/option-batch";
        }

        // Parse file to extract option texts
        List<String> optionTexts = extractOptionsFromFile(file);
        if (optionTexts.isEmpty()) {
            bindingResult.addError(new FieldError("batchForm", "file", messageSource.getMessage("error_batch_options", null, locale)));
            model.addAttribute("poll", poll.get());
            addMenuPermissions(model);
            return "polls/option-batch";
        }

        try {
            pollsService.saveOptionsBatch(batchForm.getPollId(), optionTexts);
            redirectAttributes.addFlashAttribute("success", messageSource.getMessage("poll_options_batch_added_success", null, locale));
            return "redirect:/voteAdd?pollId=" + batchForm.getPollId();
        } catch (IllegalArgumentException ex) {
            bindingResult.addError(new FieldError("batchForm", "file", ex.getMessage()));
            model.addAttribute("poll", poll.get());
            addMenuPermissions(model);
            return "polls/option-batch";
        }
    }

    @GetMapping("/pollOptionDelete")
    public String deleteOption(@RequestParam("optionId") Long optionId,
                               Model model) {
        Optional<Option> option = pollsService.getOptionById(optionId);
        if (option.isEmpty()) {
            return "redirect:/votePolls";
        }
        Poll poll = option.get().getPoll();
        if (poll == null) {
            return "redirect:/votePolls";
        }
        if (!pollPermissionsService.canEditPoll(poll)) {
            return "redirect:/votePolls";
        }
        boolean hasVotes = !pollsService.getAllVotesForOption(option.get()).isEmpty();

        model.addAttribute("option", option.get());
        model.addAttribute("poll", poll);
        model.addAttribute("hasVotes", hasVotes || pollsService.pollIsVotable(poll));
        model.addAttribute("handleOptions", List.of(
                new DeleteChoice(HANDLE_DELETE_OPTION_DO_NOTHING, "handle_delete_option_do_nothing_label"),
                new DeleteChoice(HANDLE_DELETE_OPTION_RETURN_VOTES, "handle_delete_option_return_votes_label")
        ));
        model.addAttribute("pollOptions", poll.getOptions());
        addMenuPermissions(model);
        return "polls/option-delete";
    }

    @PostMapping("/pollOptionDelete")
    public String confirmDelete(@RequestParam("optionId") Long optionId,
                                @RequestParam(value = "orphanHandling", required = false) String orphanHandling,
                                RedirectAttributes redirectAttributes,
                                Locale locale) {
        Optional<Option> option = pollsService.getOptionById(optionId);
        if (option.isEmpty() || option.get().getPoll() == null) {
            return "redirect:/votePolls";
        }
        Poll poll = option.get().getPoll();
        if (!pollPermissionsService.canEditPoll(poll)) {
            redirectAttributes.addFlashAttribute("alert", messageSource.getMessage("new_poll_noperms", null, locale));
            return "redirect:/votePolls";
        }
        if (StringUtils.isBlank(orphanHandling)) {
            orphanHandling = HANDLE_DELETE_OPTION_DO_NOTHING;
        }

        Poll updatedPoll = pollsService.deleteOptionWithVoteHandling(optionId, orphanHandling);
        redirectAttributes.addFlashAttribute("success", messageSource.getMessage("poll_option_deleted_success", null, locale));
        return "redirect:/voteAdd?pollId=" + updatedPoll.getId();
    }

    private List<String> extractOptionsFromFile(MultipartFile file) {
        try (java.io.InputStream inputStream = file.getInputStream()) {
            return org.sakaiproject.poll.tool.util.OptionsFileConverterUtil.convertInputStreamToOptionList(inputStream);
        } catch (java.io.IOException e) {
            log.warn("Unable to parse batch options file {}", file.getOriginalFilename(), e);
            return List.of();
        }
    }

    private void addMenuPermissions(Model model) {
        model.addAttribute("canAdd", pollPermissionsService.canAddPoll());
        model.addAttribute("isSiteOwner", pollPermissionsService.isSiteOwner());
    }

    public record DeleteChoice(String value, String labelKey) { }
}
