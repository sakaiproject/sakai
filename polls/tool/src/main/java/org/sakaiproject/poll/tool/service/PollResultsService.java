/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
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

package org.sakaiproject.poll.tool.service;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.poll.api.model.Option;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.model.Vote;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Service
@RequiredArgsConstructor
public class PollResultsService {

    private final PollsService pollsService;
    private final FormattedText formattedText;
    private final MessageSource messageSource;

    public PollResults buildResults(Poll poll, String siteId, Locale locale) {
        List<Vote> votes = pollsService.getAllVotesForPoll(poll.getId());
        int distinctVoters = pollsService.getDistinctVotersForPoll(poll);
        int potentialVoters = pollsService.getNumberUsersCanVote(siteId);
        return new PollResults(
                buildResultRows(poll, votes, distinctVoters, potentialVoters, locale),
                votes.size(),
                distinctVoters,
                potentialVoters
        );
    }

    private List<ResultRow> buildResultRows(Poll poll, List<Vote> votes, int distinctVoters, int potentialVoters, Locale locale) {
        int minOptions = poll.getMinOptions();
        List<Option> displayOptions = new ArrayList<>(poll.getOptions());
        if (minOptions == 0) {
            Option noVote = new Option();
            noVote.setText(messageSource.getMessage("result_novote", null, locale));
            displayOptions.add(noVote);
        }

        Map<Long, Long> voteCounts = votes.stream()
                .filter(v -> v.getOption() != null)
                .collect(Collectors.groupingBy(v -> v.getOption().getId(), Collectors.counting()));

        NumberFormat percentFormat = NumberFormat.getPercentInstance(locale);
        percentFormat.setMaximumFractionDigits(2);

        int totalVotes = votes.size();
        List<ResultRow> rows = new ArrayList<>();
        for (int i = 0; i < displayOptions.size(); i++) {
            Option option = displayOptions.get(i);
            boolean noVoteRow = isNoVoteRow(option, minOptions);
            long voteCount = getVoteCount(option, minOptions, potentialVoters, distinctVoters, voteCounts);
            double percentage = noVoteRow
                    ? calculateNoVotePercentage(voteCount, potentialVoters)
                    : calculatePercentage(poll, voteCount, totalVotes, distinctVoters);

            rows.add(new ResultRow(
                    i + 1,
                    decorateOptionText(option, locale),
                    Boolean.TRUE.equals(option.getDeleted()),
                    decorateOptionLabel(option, locale),
                    voteCount,
                    percentFormat.format(percentage),
                    percentage
            ));
        }
        return rows;
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

    private double calculateNoVotePercentage(long voteCount, int potentialVoters) {
        if (potentialVoters <= 0) {
            return 0d;
        }
        return (double) voteCount / (double) potentialVoters;
    }

    private long getVoteCount(Option option, int minOptions, int potentialVoters, int distinctVoters, Map<Long, Long> voteCounts) {
        if (isNoVoteRow(option, minOptions)) {
            return Math.max(0L, (long) potentialVoters - distinctVoters);
        }
        return voteCounts.getOrDefault(option.getId(), 0L);
    }

    private boolean isNoVoteRow(Option option, int minOptions) {
        return minOptions == 0 && option.getId() == null;
    }

    private String decorateOptionText(Option option, Locale locale) {
        return getOptionLabel(option, locale);
    }

    private String decorateOptionLabel(Option option, Locale locale) {
        return StringUtils.normalizeSpace(formattedText.convertFormattedTextToPlaintext(
                getEscapedDecoratedOptionText(option, locale)));
    }

    private String getOptionLabel(Option option, Locale locale) {
        String text = option.getText();
        if (StringUtils.isBlank(text)) {
            text = messageSource.getMessage("result_novote", null, locale);
        }
        return text;
    }

    private String getEscapedDecoratedOptionText(Option option, Locale locale) {
        return appendDeletedTag(formattedText.escapeHtml(getOptionLabel(option, locale)), option, locale);
    }

    private String appendDeletedTag(String text, Option option, Locale locale) {
        if (Boolean.TRUE.equals(option.getDeleted())) {
            text += messageSource.getMessage("deleted_option_tag_html", null, locale);
        }
        return text;
    }

    @Value
    public static class PollResults {
        List<ResultRow> rows;
        int totalVotes;
        int distinctVoters;
        int potentialVoters;
    }

    @Value
    public static class ResultRow {
        int order;
        String text;
        boolean deleted;
        String chartLabel;
        long votes;
        String percentageLabel;
        double percentageValue;
    }
}
