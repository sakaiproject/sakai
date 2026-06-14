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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.poll.tool.service.PollPermissionsService;
import org.sakaiproject.poll.tool.service.PollResultsService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class ResultsController {

    private final PollsService pollsService;
    private final SessionManager sessionManager;
    private final ToolManager toolManager;
    private final MessageSource messageSource;
    private final PollResultsService pollResultsService;
    private final PollPermissionsService pollPermissionsService;

    @GetMapping("/voteResults")
    public String showResults(@RequestParam("pollId") String pollId,
                              Model model,
                              Locale locale,
                              RedirectAttributes redirectAttributes) {
        Optional<Poll> poll = pollsService.getPollById(pollId);
        if (poll.isEmpty()) {
            redirectAttributes.addFlashAttribute("alert", messageSource.getMessage("poll_missing", null, locale));
            return "redirect:/votePolls";
        }

        Poll currentPoll = poll.get();
        String currentUserId = sessionManager.getCurrentSessionUserId();
        if (!pollsService.isAllowedViewResults(currentPoll, currentUserId)) {
            redirectAttributes.addFlashAttribute("alert", messageSource.getMessage("poll.noviewresult", null, locale));
            return "redirect:/votePolls";
        }

        String siteId = toolManager.getCurrentPlacement().getContext();
        PollResultsService.PollResults results = pollResultsService.buildResults(currentPoll, siteId, locale);

        NumberFormat percentFormat = NumberFormat.getPercentInstance(locale);
        percentFormat.setMaximumFractionDigits(2);
        double totalPercentage = results.getRows().stream().mapToDouble(PollResultsService.ResultRow::getPercentageValue).sum();
        String totalPercentageLabel = percentFormat.format(totalPercentage);

        BigDecimal voterPercent = (results.getPotentialVoters() <= 0 || results.getDistinctVoters() == 0)
                ? BigDecimal.ZERO
                : new BigDecimal(results.getDistinctVoters())
                .divide(new BigDecimal(results.getPotentialVoters()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        String pollSizeDetails = String.format(locale, "%d / %d (%.02f %%)", results.getDistinctVoters(), results.getPotentialVoters(), voterPercent.setScale(2, RoundingMode.HALF_UP));
        String pollSizeMessage = messageSource.getMessage("results_poll_size",
                new Object[]{pollSizeDetails}, locale);

        boolean canEdit = pollPermissionsService.canEditPoll(currentPoll);

        model.addAttribute("poll", currentPoll);
        model.addAttribute("canEdit", canEdit);
        model.addAttribute("rows", results.getRows());
        model.addAttribute("chartLabels", results.getRows().stream().map(PollResultsService.ResultRow::getChartLabel).collect(Collectors.toList()));
        model.addAttribute("chartVotes", results.getRows().stream().map(PollResultsService.ResultRow::getVotes).collect(Collectors.toList()));
        model.addAttribute("chartPercentages", results.getRows().stream().map(PollResultsService.ResultRow::getPercentageLabel).collect(Collectors.toList()));
        model.addAttribute("totalVotes", results.getTotalVotes());
        model.addAttribute("distinctVoters", results.getDistinctVoters());
        model.addAttribute("voterPercent", voterPercent);
        model.addAttribute("pollSizeMessage", pollSizeMessage);
        model.addAttribute("totalPercentageLabel", totalPercentageLabel);
        model.addAttribute("canAdd", pollPermissionsService.canAddPoll());
        model.addAttribute("isSiteOwner", pollPermissionsService.isSiteOwner());

        return "polls/results";
    }
}
