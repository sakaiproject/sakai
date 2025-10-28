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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.VoteCollection;
import org.sakaiproject.poll.tool.model.VoteForm;
import org.sakaiproject.poll.tool.service.PollValidationException;
import org.sakaiproject.poll.tool.service.PollsUiService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/faces")
@RequiredArgsConstructor
@Slf4j
public class VoteController {

    private final PollListManager pollListManager;
    private final PollVoteManager pollVoteManager;
    private final ExternalLogic externalLogic;
    private final PollsUiService pollsUiService;
    private final MessageSource messageSource;

    @GetMapping("/voteQuestion")
    public String showVote(@RequestParam("pollId") Long pollId,
                           Model model,
                           Locale locale,
                           RedirectAttributes redirectAttributes) {
        Poll poll;
        try {
            poll = pollListManager.getPollById(pollId);
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("alert", messageSource.getMessage("vote_noperm.voteCollection", null, locale));
            return "redirect:/faces/votePolls";
        }
        if (poll == null) {
            redirectAttributes.addFlashAttribute("alert", messageSource.getMessage("poll_missing", null, locale));
            return "redirect:/faces/votePolls";
        }
        if (!pollVoteManager.pollIsVotable(poll)) {
            redirectAttributes.addFlashAttribute("alert", messageSource.getMessage("vote_noperm.voteCollection", null, locale));
            return "redirect:/faces/votePolls";
        }

        boolean multipleChoice = poll.getMaxOptions() > 1;
        List<Option> options = pollListManager.getVisibleOptionsForPoll(pollId);

        VoteForm voteForm = new VoteForm();
        voteForm.setPollId(pollId);

        model.addAttribute("poll", poll);
        model.addAttribute("options", options);
        model.addAttribute("multipleChoice", multipleChoice);
        model.addAttribute("voteForm", voteForm);
        model.addAttribute("canAdd", isAllowedPollAdd());
        model.addAttribute("isSiteOwner", isSiteOwner());
        return "polls/vote";
    }

    @PostMapping("/voteQuestion")
    public String submitVote(@ModelAttribute VoteForm voteForm,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        Poll poll;
        try {
            poll = pollListManager.getPollById(voteForm.getPollId());
        } catch (SecurityException e) {
            log.debug("User lacks permission to view poll {}", voteForm.getPollId(), e);
            redirectAttributes.addFlashAttribute("alert", messageSource.getMessage("vote_noperm.voteCollection", null, locale));
            return "redirect:/faces/votePolls";
        }
        if (poll == null) {
            redirectAttributes.addFlashAttribute("alert", messageSource.getMessage("poll_missing", null, locale));
            return "redirect:/faces/votePolls";
        }
        try {
            List<Long> optionIds = voteForm.getSelectedOptionIds() != null
                    ? new ArrayList<>(voteForm.getSelectedOptionIds())
                    : new ArrayList<>();
            VoteCollection voteCollection = pollsUiService.submitVote(voteForm.getPollId(), optionIds);
            redirectAttributes.addFlashAttribute("success", messageSource.getMessage("thanks_msg", null, locale));
            return "redirect:/faces/voteThanks?voteRef=" + voteCollection.getId();
        } catch (PollValidationException ex) {
            redirectAttributes.addFlashAttribute("alert", messageSource.getMessage(ex.getMessage(), ex.getArgs(), locale));
            return "redirect:/faces/voteQuestion?pollId=" + poll.getPollId();
        }
    }

    @GetMapping("/voteThanks")
    public String showConfirmation(@RequestParam("voteRef") String voteRef,
                                   Model model) {
        model.addAttribute("voteRef", voteRef);
        model.addAttribute("canAdd", isAllowedPollAdd());
        model.addAttribute("isSiteOwner", isSiteOwner());
        return "polls/thanks";
    }

    private boolean isAllowedPollAdd() {
        return externalLogic.isUserAdmin() || externalLogic.isAllowedInLocation(PollListManager.PERMISSION_ADD, externalLogic.getCurrentLocationReference());
    }

    private boolean isSiteOwner() {
        return externalLogic.isUserAdmin() || externalLogic.isAllowedInLocation("site.upd", externalLogic.getCurrentLocationReference());
    }
}
