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

package org.sakaiproject.poll.tool.mvc;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.poll.api.model.Option;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.model.Vote;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.poll.tool.service.PollResultsService;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.context.support.StaticMessageSource;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PollResultsServiceTest {

    private PollsService pollsService;
    private PollResultsService pollResultsService;

    @Before
    public void setUp() {
        pollsService = mock(PollsService.class);
        FormattedText formattedText = mock(FormattedText.class);
        when(formattedText.escapeHtml(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(formattedText.convertFormattedTextToPlaintext(anyString())).thenAnswer(invocation ->
                ((String) invocation.getArgument(0))
                        .replace("&nbsp;", " ")
                        .replaceAll("<[^>]+>", ""));

        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("result_novote", Locale.US, "No vote");
        messageSource.addMessage("deleted_option_tag_html", Locale.US, "&nbsp;<i>(deleted)</i>");

        pollResultsService = new PollResultsService(pollsService, formattedText, messageSource);
    }

    @Test
    public void multiSelectPercentagesUseDistinctVoters() {
        Poll poll = new Poll();
        poll.setId("poll-1");
        poll.setMaxOptions(2);
        Option first = option(1L, "First", false);
        Option second = option(2L, "Second", false);
        poll.setOptions(List.of(first, second));

        List<Vote> votes = List.of(
                vote(first, "user1"),
                vote(second, "user1"),
                vote(first, "user2")
        );

        PollResultsService.PollResults results = buildResults(poll, votes, 2, 2);
        List<PollResultsService.ResultRow> rows = results.getRows();

        Assert.assertEquals(1.0d, rows.get(0).getPercentageValue(), 0.001d);
        Assert.assertEquals("100%", rows.get(0).getPercentageLabel());
        Assert.assertEquals(0.5d, rows.get(1).getPercentageValue(), 0.001d);
        Assert.assertEquals("50%", rows.get(1).getPercentageLabel());
    }

    @Test
    public void optionalPollIncludesNoVoteRow() {
        Poll poll = new Poll();
        poll.setId("poll-1");
        poll.setMinOptions(0);
        Option first = option(1L, "First", false);
        poll.setOptions(List.of(first));

        List<Vote> votes = List.of(vote(first, "user1"), vote(first, "user2"));

        PollResultsService.PollResults results = buildResults(poll, votes, 2, 3);
        List<PollResultsService.ResultRow> rows = results.getRows();

        Assert.assertEquals(2, rows.size());
        Assert.assertEquals("No vote", rows.get(1).getText());
        Assert.assertEquals(1L, rows.get(1).getVotes());
        Assert.assertEquals(0.333d, rows.get(1).getPercentageValue(), 0.001d);
        Assert.assertEquals("33.33%", rows.get(1).getPercentageLabel());
    }

    @Test
    public void noVotePercentageUsesPotentialVotersWhenAllVotersAbstain() {
        Poll poll = new Poll();
        poll.setId("poll-1");
        poll.setMinOptions(0);
        poll.setOptions(List.of(option(1L, "First", false)));

        PollResultsService.PollResults results = buildResults(poll, List.of(), 0, 3);
        List<PollResultsService.ResultRow> rows = results.getRows();

        Assert.assertEquals(2, rows.size());
        Assert.assertEquals("No vote", rows.get(1).getText());
        Assert.assertEquals(3L, rows.get(1).getVotes());
        Assert.assertEquals(1.0d, rows.get(1).getPercentageValue(), 0.001d);
        Assert.assertEquals("100%", rows.get(1).getPercentageLabel());
    }

    @Test
    public void deletedOptionsHavePlaintextChartLabels() {
        Poll poll = new Poll();
        poll.setId("poll-1");
        Option option = option(1L, "Deleted option", true);
        poll.setOptions(List.of(option));

        PollResultsService.PollResults results = buildResults(poll, List.of(vote(option, "user1")), 1, 1);
        List<PollResultsService.ResultRow> rows = results.getRows();

        Assert.assertTrue(rows.get(0).isDeleted());
        Assert.assertEquals("Deleted option (deleted)", rows.get(0).getChartLabel());
    }

    private PollResultsService.PollResults buildResults(Poll poll, List<Vote> votes, int distinctVoters, int potentialVoters) {
        when(pollsService.getAllVotesForPoll(poll.getId())).thenReturn(votes);
        when(pollsService.getDistinctVotersForPoll(poll)).thenReturn(distinctVoters);
        when(pollsService.getNumberUsersCanVote("site-1")).thenReturn(potentialVoters);
        return pollResultsService.buildResults(poll, "site-1", Locale.US);
    }

    private Option option(Long id, String text, boolean deleted) {
        Option option = new Option();
        option.setId(id);
        option.setText(text);
        option.setDeleted(deleted);
        return option;
    }

    private Vote vote(Option option, String userId) {
        Vote vote = new Vote();
        vote.setOption(option);
        vote.setUserId(userId);
        vote.setSubmissionId(userId);
        vote.setVoteDate(Instant.now());
        return vote;
    }
}
