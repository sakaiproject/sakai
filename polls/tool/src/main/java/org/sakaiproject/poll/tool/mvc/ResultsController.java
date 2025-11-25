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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.poll.api.model.Option;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.model.Vote;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.sakaiproject.poll.api.PollConstants.PERMISSION_ADD;

@Controller
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class ResultsController {

    private final PollsService pollsService;
    private final SessionManager sessionManager;
    private final SecurityService securityService;
    private final SiteService siteService;
    private final ToolManager toolManager;
    private final MessageSource messageSource;

    @GetMapping("/voteResults")
    public String showResults(@RequestParam("pollId") String pollId,
                              Model model,
                              Locale locale,
                              RedirectAttributes redirectAttributes) {
        Optional<Poll> poll = pollsService.getPollById(pollId);
        String currentUserId = sessionManager.getCurrentSessionUserId();
        if (!pollsService.isAllowedViewResults(poll.get(), currentUserId)) {
            redirectAttributes.addFlashAttribute("alert", messageSource.getMessage("poll.noviewresult", null, locale));
            return "redirect:/votePolls";
        }

        List<Option> options = poll.get().getOptions();
        if (poll.get().getMinOptions() == 0) {
            Option noVote = new Option();
            noVote.setText(messageSource.getMessage("result_novote", null, locale));
            poll.get().addOption(noVote);
        }

        List<Vote> votes = pollsService.getAllVotesForPoll(poll.get().getId());
        int totalVotes = votes.size();
        int distinctVoters = pollsService.getDistinctVotersForPoll(poll.get());
        String siteId = toolManager.getCurrentPlacement().getContext();
        int potentialVoters = pollsService.getNumberUsersCanVote(siteId);

        List<ResultRow> rows = new ArrayList<>();
        NumberFormat percentFormat = NumberFormat.getPercentInstance(locale);
        percentFormat.setMaximumFractionDigits(2);

        for (int i = 0; i < options.size(); i++) {
            Option option = options.get(i);
            long voteCount = votes.stream()
                    .filter(v -> option.getId().equals(v.getOption().getId()))
                    .count();

            double percentage = calculatePercentage(poll.get(), voteCount, totalVotes, distinctVoters);
            rows.add(new ResultRow(
                    i + 1,
                    decorateOptionText(option, locale),
                    voteCount,
                    percentFormat.format(percentage),
                    percentage
            ));
        }

        double totalPercentage = rows.stream().mapToDouble(ResultRow::getPercentageValue).sum();
        String totalPercentageLabel = percentFormat.format(totalPercentage);

        BigDecimal voterPercent = (potentialVoters <= 0 || distinctVoters == 0)
                ? BigDecimal.ZERO
                : new BigDecimal(distinctVoters)
                .divide(new BigDecimal(potentialVoters), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        String pollSizeDetails = String.format(locale, "%d / %d (%.02f %%)", distinctVoters, potentialVoters, voterPercent.setScale(2, RoundingMode.HALF_UP));
        String pollSizeMessage = messageSource.getMessage("results_poll_size",
                new Object[]{pollSizeDetails}, locale);

        String siteRef = siteService.siteReference(siteId);
        boolean isAdmin = securityService.isSuperUser();
        boolean canAdd = isAdmin || securityService.unlock(PERMISSION_ADD, siteRef);
        boolean isSiteOwner = isAdmin || securityService.unlock("site.upd", siteRef);

        model.addAttribute("poll", poll);
        model.addAttribute("rows", rows);
        model.addAttribute("totalVotes", totalVotes);
        model.addAttribute("distinctVoters", distinctVoters);
        model.addAttribute("voterPercent", voterPercent);
        model.addAttribute("pollSizeMessage", pollSizeMessage);
        model.addAttribute("totalPercentageLabel", totalPercentageLabel);
        model.addAttribute("canAdd", canAdd);
        model.addAttribute("isSiteOwner", isSiteOwner);

        return "polls/results";
    }

    private double calculatePercentage(Poll poll, long voteCount, int totalVotes, int distinctVoters) {
        if (totalVotes == 0) {
            return 0d;
        }
        if (poll.getMaxOptions() == 1) {
            return (double) voteCount / (double) totalVotes;
        }
        if (distinctVoters > 0) {
            return (double) voteCount / (double) distinctVoters;
        }
        return 0d;
    }

    private String decorateOptionText(Option option, Locale locale) {
        String text = option.getText();
        if (StringUtils.isBlank(text)) {
            text = messageSource.getMessage("result_novote", null, locale);
        }
        if (Boolean.TRUE.equals(option.getDeleted())) {
            text += messageSource.getMessage("deleted_option_tag_html", null, locale);
        }
        return text;
    }

    @Value
    public static class ResultRow {
        int order;
        String text;
        long votes;
        String percentageLabel;
        double percentageValue;
    }
}
