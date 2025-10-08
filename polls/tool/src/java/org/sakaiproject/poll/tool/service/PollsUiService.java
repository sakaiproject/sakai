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

package org.sakaiproject.poll.tool.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Vote;
import org.sakaiproject.poll.model.VoteCollection;
import org.sakaiproject.poll.tool.util.OptionsFileConverterUtil;
import org.sakaiproject.poll.util.PollUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class PollsUiService {

    public static final String HANDLE_DELETE_OPTION_DO_NOTHING = "do-nothing";
    public static final String HANDLE_DELETE_OPTION_RETURN_VOTES = "return-votes";

    private final PollListManager pollListManager;
    private final PollVoteManager pollVoteManager;
    private final ExternalLogic externalLogic;

    public void deletePolls(Collection<Long> pollIds) {
        for (Long pollId : pollIds) {
            Poll poll = pollListManager.getPollById(pollId);
            if (poll == null) {
                log.warn("Poll {} not found during bulk delete", pollId);
                continue;
            }
            try {
                pollListManager.deletePoll(poll);
            } catch (SecurityException e) {
                log.warn("User {} is not permitted to delete poll {}", externalLogic.getCurrentUserId(), pollId);
            }
        }
    }

    public void resetPollVotes(Collection<Long> pollIds) {
        for (Long pollId : pollIds) {
            Poll poll = pollListManager.getPollById(pollId);
            if (poll == null) {
                log.warn("Poll {} not found during bulk vote reset", pollId);
                continue;
            }
            if (pollListManager.userCanDeletePoll(poll)) {
                List<Vote> votes = pollVoteManager.getAllVotesForPoll(poll);
                pollVoteManager.deleteAll(votes);
            }
        }
    }

    public Poll savePoll(Poll poll, Locale locale) {
        if (StringUtils.isBlank(poll.getText())) {
            throw new PollValidationException("error_no_text");
        }

        if (poll.getVoteOpen() == null || poll.getVoteClose() == null) {
            throw new PollValidationException("close_before_open");
        }

        if (poll.getVoteOpen().after(poll.getVoteClose())) {
            throw new PollValidationException("close_before_open");
        }

        if (poll.getMinOptions() > poll.getMaxOptions()) {
            throw new PollValidationException("min_greater_than_max");
        }

        if (poll.getPollId() != null) {
            Poll existing = pollListManager.getPollById(poll.getPollId(), false);
            if (poll.getCreationDate() == null) {
                poll.setCreationDate(existing.getCreationDate());
            }
        } else {
            poll.setCreationDate(new Date());
        }

        poll.setDetails(PollUtils.cleanupHtmlPtags(externalLogic.processFormattedText(poll.getDetails(), new StringBuilder())));

        boolean isNew = poll.getPollId() == null;
        pollListManager.savePoll(poll);

        if (!isNew) {
            List<Option> pollOptions = pollListManager.getOptionsForPoll(poll);
            poll.setOptions(pollOptions);
            if (pollOptions.size() < poll.getMinOptions() || pollOptions.size() > poll.getMaxOptions()) {
                throw new PollValidationException("invalid_poll_limits");
            }
        }

        return poll;
    }

    public Option saveOption(Option option) {
        option.setText(PollUtils.cleanupHtmlPtags(externalLogic.processFormattedText(option.getText(), new StringBuilder(), true, true)));
        if (StringUtils.isBlank(option.getText())) {
            throw new PollValidationException("option_empty");
        }

        if (option.getOptionId() == null) {
            option.setOptionOrder(pollListManager.getOptionsForPoll(option.getPollId()).size());
        }
        pollListManager.saveOption(option);
        return option;
    }

    public void saveOptionsBatch(Long pollId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new PollValidationException("error_batch_options");
        }

        List<String> optionTexts = extractOptions(file);
        if (optionTexts.isEmpty()) {
            throw new PollValidationException("error_batch_options");
        }

        int nextOrder = pollListManager.getOptionsForPoll(pollId).size();
        for (String optionText : optionTexts) {
            Option option = new Option();
            option.setPollId(pollId);
            option.setText(PollUtils.cleanupHtmlPtags(optionText));
            option.setOptionOrder(nextOrder++);
            pollListManager.saveOption(option);
        }
    }

    private List<String> extractOptions(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return OptionsFileConverterUtil.convertInputStreamToOptionList(inputStream);
        } catch (IOException e) {
            log.warn("Unable to parse batch options file {}", file.getOriginalFilename(), e);
            return List.of();
        }
    }

    public Poll deleteOption(Long optionId, String orphanVoteHandling) {
        Option option = pollListManager.getOptionById(optionId);
        if (option == null) {
            throw new IllegalArgumentException("Option not found");
        }

        Poll poll = pollListManager.getPollById(option.getPollId());
        List<Vote> votes = pollVoteManager.getAllVotesForOption(option);

        if (votes != null && !votes.isEmpty()) {
            if (HANDLE_DELETE_OPTION_RETURN_VOTES.equals(orphanVoteHandling)) {
                Set<String> userIds = new TreeSet<>();
                pollListManager.deleteOption(option);
                for (Vote vote : votes) {
                    if (vote.getUserId() != null) {
                        String userEid = externalLogic.getUserEidFromId(vote.getUserId());
                        if (StringUtils.isNotBlank(userEid)) {
                            userIds.add(userEid);
                        }
                    }
                    pollVoteManager.deleteVote(vote);
                }
                externalLogic.notifyDeletedOption(new ArrayList<>(userIds), externalLogic.getSiteTile(poll.getSiteId()), poll.getText());
            } else {
                Option persistentOption = pollListManager.getOptionById(optionId);
                pollListManager.deleteOption(persistentOption, true);
            }
        } else {
            pollListManager.deleteOption(option);
        }

        return pollListManager.getPollById(option.getPollId());
    }

    public VoteCollection submitVote(Long pollId, List<Long> selectedOptionIds) {
        Poll poll = pollListManager.getPollById(pollId);

        if (!pollVoteManager.pollIsVotable(poll)) {
            throw new PollValidationException("vote_noperm.voteCollection");
        }

        if (poll.getLimitVoting() && pollVoteManager.userHasVoted(pollId)) {
            throw new PollValidationException("vote_hasvoted.voteCollection");
        }

        List<Long> votesToProcess = new ArrayList<>();
        if (selectedOptionIds != null) {
            votesToProcess.addAll(selectedOptionIds);
        }

        if (votesToProcess.isEmpty() && poll.getMinOptions() == 0) {
            votesToProcess.add(0L);
        }

        validateVoteSelection(poll, votesToProcess);

        VoteCollection voteCollection = new VoteCollection();
        voteCollection.setPollId(pollId);

        List<Vote> votesToSave = new ArrayList<>();
        for (Long optionId : votesToProcess) {
            Option option = new Option(optionId);
            Vote vote = new Vote(poll, option, voteCollection.getId());
            if (vote.getIp() == null) {
                vote.setIp("Nothing");
            }
            votesToSave.add(vote);
        }

        pollVoteManager.saveVoteList(votesToSave);
        voteCollection.setVotes(votesToSave);
        return voteCollection;
    }

    private void validateVoteSelection(Poll poll, List<Long> selectedOptionIds) {
        int selectionCount = selectedOptionIds.size();
        if (poll.getMaxOptions() == poll.getMinOptions()
                && poll.getMaxOptions() == 1
                && selectionCount == 0) {
            throw new PollValidationException("error_novote.voteCollection", poll.getMinOptions());
        }

        if (selectionCount > poll.getMaxOptions()) {
            throw new PollValidationException("error_tomany_votes.voteCollection", poll.getMaxOptions());
        }

        if (selectionCount < poll.getMinOptions()) {
            throw new PollValidationException("error_tofew_votes.voteCollection", poll.getMinOptions());
        }
    }

    public Poll hydratePollForForm(Poll poll, LocalDateTime open, LocalDateTime close, ZoneId zoneId) {
        if (open != null) {
            poll.setVoteOpen(Date.from(open.atZone(zoneId).toInstant()));
        }
        if (close != null) {
            poll.setVoteClose(Date.from(close.atZone(zoneId).toInstant()));
        }
        return poll;
    }

    public LocalDateTime toLocalDateTime(Date date, ZoneId zoneId) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), zoneId);
    }
}
