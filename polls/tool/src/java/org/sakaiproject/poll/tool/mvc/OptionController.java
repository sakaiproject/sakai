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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.tool.model.OptionBatchForm;
import org.sakaiproject.poll.tool.model.OptionForm;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.sakaiproject.poll.tool.service.PollsUiService.HANDLE_DELETE_OPTION_DO_NOTHING;
import static org.sakaiproject.poll.tool.service.PollsUiService.HANDLE_DELETE_OPTION_RETURN_VOTES;

@Controller
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class OptionController {

    private final PollListManager pollListManager;
    private final PollVoteManager pollVoteManager;
    private final ExternalLogic externalLogic;
    private final PollsUiService pollsUiService;
    private final MessageSource messageSource;

    @GetMapping("/pollOption")
    public String editOption(@RequestParam(value = "optionId", required = false) Long optionId,
                             @RequestParam(value = "pollId", required = false) Long pollId,
                             Model model,
                             Locale locale) {
        OptionForm form = new OptionForm();
        Poll poll;
        if (optionId != null) {
            Option option = pollListManager.getOptionById(optionId);
            if (option == null) {
                return "redirect:/votePolls";
            }
            form.setOptionId(option.getOptionId());
            form.setPollId(option.getPollId());
            form.setText(option.getText());
            poll = pollListManager.getPollById(option.getPollId());
        } else {
            if (pollId == null) {
                return "redirect:/votePolls";
            }
            form.setPollId(pollId);
            poll = pollListManager.getPollById(pollId);
        }

        model.addAttribute("poll", poll);
        model.addAttribute("optionForm", form);
        model.addAttribute("isNew", optionId == null);
        model.addAttribute("canAdd", isAllowedPollAdd());
        model.addAttribute("isSiteOwner", isSiteOwner());
        return "polls/option-edit";
    }

    @PostMapping("/pollOption")
    public String saveOption(@ModelAttribute("optionForm") OptionForm optionForm,
                             BindingResult bindingResult,
                             @RequestParam("submitAction") String submitAction,
                             RedirectAttributes redirectAttributes,
                             Locale locale,
                             Model model) {
        if (!isAllowedPollAdd()) {
            bindingResult.addError(new FieldError("optionForm", "text", messageSource.getMessage("new_poll_noperms", null, locale)));
            model.addAttribute("poll", pollListManager.getPollById(optionForm.getPollId()));
            model.addAttribute("isNew", optionForm.getOptionId() == null);
            model.addAttribute("canAdd", isAllowedPollAdd());
            model.addAttribute("isSiteOwner", isSiteOwner());
            return "polls/option-edit";
        }

        Option option = new Option();
        option.setOptionId(optionForm.getOptionId());
        option.setPollId(optionForm.getPollId());
        option.setText(optionForm.getText());

        try {
            pollsUiService.saveOption(option);
            redirectAttributes.addFlashAttribute("success", messageSource.getMessage("poll_option_added_success", null, locale));
            if ("addAnother".equals(submitAction)) {
                return "redirect:/pollOption?pollId=" + option.getPollId();
            }
            return "redirect:/voteAdd?pollId=" + option.getPollId();
        } catch (PollValidationException ex) {
            bindingResult.addError(new FieldError("optionForm", "text", messageSource.getMessage(ex.getMessage(), ex.getArgs(), locale)));
            model.addAttribute("poll", pollListManager.getPollById(optionForm.getPollId()));
            model.addAttribute("isNew", optionForm.getOptionId() == null);
            model.addAttribute("canAdd", isAllowedPollAdd());
            model.addAttribute("isSiteOwner", isSiteOwner());
            return "polls/option-edit";
        }
    }

    @GetMapping("/pollOptionBatch")
    public String batchOption(@RequestParam("pollId") Long pollId,
                              Model model) {
        Poll poll = pollListManager.getPollById(pollId);
        OptionBatchForm form = new OptionBatchForm();
        form.setPollId(pollId);
        model.addAttribute("poll", poll);
        model.addAttribute("batchForm", form);
        model.addAttribute("canAdd", isAllowedPollAdd());
        model.addAttribute("isSiteOwner", isSiteOwner());
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
        if (!isAllowedPollAdd()) {
            bindingResult.addError(new FieldError("batchForm", "file", messageSource.getMessage("new_poll_noperms", null, locale)));
            model.addAttribute("poll", pollListManager.getPollById(batchForm.getPollId()));
            model.addAttribute("canAdd", isAllowedPollAdd());
            model.addAttribute("isSiteOwner", isSiteOwner());
            return "polls/option-batch";
        }
        try {
            pollsUiService.saveOptionsBatch(batchForm.getPollId(), batchForm.getFile());
            redirectAttributes.addFlashAttribute("success", messageSource.getMessage("poll_options_batch_added_success", null, locale));
            return "redirect:/voteAdd?pollId=" + batchForm.getPollId();
        } catch (PollValidationException ex) {
            bindingResult.addError(new FieldError("batchForm", "file", messageSource.getMessage(ex.getMessage(), ex.getArgs(), locale)));
            model.addAttribute("poll", pollListManager.getPollById(batchForm.getPollId()));
            model.addAttribute("canAdd", isAllowedPollAdd());
            model.addAttribute("isSiteOwner", isSiteOwner());
            return "polls/option-batch";
        }
    }

    @GetMapping("/pollOptionDelete")
    public String deleteOption(@RequestParam("optionId") Long optionId,
                               Model model) {
        Option option = pollListManager.getOptionById(optionId);
        if (option == null) {
            return "redirect:/votePolls";
        }
        Poll poll = pollListManager.getPollById(option.getPollId());
        List<Option> pollOptions = pollListManager.getOptionsForPoll(poll);
        boolean hasVotes = !pollVoteManager.getAllVotesForOption(option).isEmpty();

        model.addAttribute("option", option);
        model.addAttribute("poll", poll);
        model.addAttribute("hasVotes", hasVotes || pollVoteManager.pollIsVotable(poll));
        model.addAttribute("handleOptions", List.of(
                new DeleteChoice(HANDLE_DELETE_OPTION_DO_NOTHING, "handle_delete_option_do_nothing_label"),
                new DeleteChoice(HANDLE_DELETE_OPTION_RETURN_VOTES, "handle_delete_option_return_votes_label")
        ));
        model.addAttribute("pollOptions", pollOptions);
        model.addAttribute("canAdd", isAllowedPollAdd());
        model.addAttribute("isSiteOwner", isSiteOwner());
        return "polls/option-delete";
    }

    @PostMapping("/pollOptionDelete")
    public String confirmDelete(@RequestParam("optionId") Long optionId,
                                @RequestParam(value = "orphanHandling", required = false) String orphanHandling,
                                RedirectAttributes redirectAttributes,
                                Locale locale) {
        if (!isAllowedPollAdd()) {
            redirectAttributes.addFlashAttribute("alert", messageSource.getMessage("new_poll_noperms", null, locale));
            return "redirect:/votePolls";
        }
        if (StringUtils.isBlank(orphanHandling)) {
            orphanHandling = HANDLE_DELETE_OPTION_DO_NOTHING;
        }

        Poll poll = pollsUiService.deleteOption(optionId, orphanHandling);
        redirectAttributes.addFlashAttribute("success", messageSource.getMessage("poll_option_deleted_success", null, locale));
        return "redirect:/voteAdd?pollId=" + poll.getPollId();
    }

    private boolean isAllowedPollAdd() {
        return externalLogic.isUserAdmin() || externalLogic.isAllowedInLocation(PollListManager.PERMISSION_ADD, externalLogic.getCurrentLocationReference());
    }

    private boolean isSiteOwner() {
        return externalLogic.isUserAdmin() || externalLogic.isAllowedInLocation("site.upd", externalLogic.getCurrentLocationReference());
    }

    public record DeleteChoice(String value, String labelKey) { }
}
