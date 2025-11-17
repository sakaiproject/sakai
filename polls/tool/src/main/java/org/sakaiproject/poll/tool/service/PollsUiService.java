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
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.poll.api.logic.ExternalLogic;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.poll.api.model.Option;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.model.Vote;
import org.sakaiproject.poll.api.model.VoteCollection;
import org.sakaiproject.poll.tool.util.OptionsFileConverterUtil;
import org.sakaiproject.poll.api.util.PollUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class PollsUiService {

    public static final String HANDLE_DELETE_OPTION_DO_NOTHING = "do-nothing";
    public static final String HANDLE_DELETE_OPTION_RETURN_VOTES = "return-votes";

    private final PollsService pollsService;
    private final ExternalLogic externalLogic;

    public void deletePolls(Collection<String> pollIds) {
        for (String pollId : pollIds) {
            Optional<Poll> poll = pollsService.getPollById(pollId);
            if (poll.isEmpty()) {
                log.warn("Poll {} not found during bulk delete", pollId);
                continue;
            }
            try {
                pollsService.deletePoll(poll.get());
            } catch (SecurityException e) {
                log.warn("User {} is not permitted to delete poll {}", externalLogic.getCurrentUserId(), pollId);
            }
        }
    }

    public void resetPollVotes(Collection<String> pollIds) {
        for (String pollId : pollIds) {
            Optional<Poll> poll = pollsService.getPollById(pollId);
            if (poll.isEmpty()) {
                log.warn("Poll {} not found during bulk vote reset", pollId);
                continue;
            }
            if (pollsService.userCanDeletePoll(poll.get())) {
                List<Vote> votes = pollsService.getAllVotesForPoll(poll.get().getId());
                pollsService.deleteAll(votes);
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

        if (poll.getId() != null) {
            Optional<Poll> existing = pollsService.getPollById(poll.getId());
            if (existing.isEmpty()) {
                throw new PollValidationException("poll_not_found");
            }
            if (poll.getCreationDate() == null) {
                poll.setCreationDate(existing.get().getCreationDate());
            }
        } else {
            poll.setCreationDate(new Date());
        }

        poll.setDescription(PollUtils.cleanupHtmlPtags(externalLogic.processFormattedText(poll.getDescription(), new StringBuilder())));

        boolean isNew = poll.getId() == null;
        pollsService.savePoll(poll);

        if (!isNew) {
            List<Option> pollOptions = pollsService.getOptionsForPoll(poll);
            poll.setOptions(pollOptions);
        }

        return poll;
    }

    public Option saveOption(Option option) {
        option.setText(PollUtils.cleanupHtmlPtags(externalLogic.processFormattedText(option.getText(), new StringBuilder(), true, true)));
        if (StringUtils.isBlank(option.getText())) {
            throw new PollValidationException("option_empty");
        }

        if (option.getId() == null) {
            // New option - poll relationship should already be set
            Poll poll = option.getPoll();
            if (poll == null) {
                throw new IllegalArgumentException("Poll must be set for new option");
            }
            option.setOptionOrder(pollsService.getOptionsForPoll(poll).size());
        } else {
            Optional<Option> existing = pollsService.getOptionById(option.getId());
            if (existing.isEmpty()) {
                throw new IllegalArgumentException("Option not found");
            }
            option.setOptionOrder(existing.get().getOptionOrder());
            option.setPoll(existing.get().getPoll());
        }
        pollsService.saveOption(option);
        return option;
    }

    public void saveOptionsBatch(String pollId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new PollValidationException("error_batch_options");
        }

        List<String> optionTexts = extractOptions(file);
        if (optionTexts.isEmpty()) {
            throw new PollValidationException("error_batch_options");
        }

        Optional<Poll> poll = pollsService.getPollById(pollId);
        if (poll.isEmpty()) {
            throw new IllegalArgumentException("Poll not found: " + pollId);
        }
        int nextOrder = pollsService.getOptionsForPoll(poll.get()).size();
        for (String optionText : optionTexts) {
            Option option = new Option();
            option.setPoll(poll.get());
            option.setText(PollUtils.cleanupHtmlPtags(optionText));
            option.setOptionOrder(nextOrder++);
            pollsService.saveOption(option);
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
        Optional<Option> option = pollsService.getOptionById(optionId);
        if (option.isEmpty()) {
            throw new IllegalArgumentException("Option not found");
        }

        Poll poll = option.get().getPoll();
        if (poll == null) {
            throw new IllegalArgumentException("Option has no associated poll");
        }
        List<Vote> votes = pollsService.getAllVotesForOption(option.get());

        if (votes != null && !votes.isEmpty()) {
            if (HANDLE_DELETE_OPTION_RETURN_VOTES.equals(orphanVoteHandling)) {
                Set<String> userIds = new TreeSet<>();
                pollsService.deleteOption(option.get().getId());
                for (Vote vote : votes) {
                    if (vote.getUserId() != null) {
                        String userEid = externalLogic.getUserEidFromId(vote.getUserId());
                        if (StringUtils.isNotBlank(userEid)) {
                            userIds.add(userEid);
                        }
                    }
                    pollsService.deleteVote(vote);
                }
                externalLogic.notifyDeletedOption(new ArrayList<>(userIds), externalLogic.getSiteTile(poll.getSiteId()), poll.getText());
            } else {
                pollsService.getOptionById(optionId)
                        .ifPresent(o -> pollsService.deleteOption(o.getId(), true));
            }
        } else {
            pollsService.deleteOption(option.get().getId());
        }

        return option.get().getPoll();
    }

    public VoteCollection submitVote(String pollId, List<Long> selectedOptionIds) {
        Optional<Poll> poll = pollsService.getPollById(pollId);

        if (poll.isPresent() && !pollsService.pollIsVotable(poll.get())) {
            throw new PollValidationException("vote_noperm.voteCollection");
        }

        if (poll.isPresent() && poll.get().isLimitVoting() && pollsService.userHasVoted(pollId)) {
            throw new PollValidationException("vote_hasvoted.voteCollection");
        }

        List<Long> votesToProcess = new ArrayList<>();
        if (selectedOptionIds != null) {
            votesToProcess.addAll(selectedOptionIds);
        }

        if (votesToProcess.isEmpty() && poll.get().getMinOptions() == 0) {
            votesToProcess.add(0L);
        }

        // Validate option membership and uniqueness (ignore sentinel 0L for zero-min polls)
        if (!(votesToProcess.size() == 1 && votesToProcess.get(0) == 0L)) {
            Set<Long> uniqueIds = new TreeSet<>(votesToProcess);
            if (uniqueIds.size() != votesToProcess.size()) {
                throw new PollValidationException("invalid_option_selection");
            }
            List<Option> allowedOptions = pollsService.getVisibleOptionsForPoll(pollId);
            Set<Long> allowedIds = allowedOptions.stream()
                    .map(Option::getId)
                    .collect(Collectors.toSet());
            if (!allowedIds.containsAll(votesToProcess)) {
                throw new PollValidationException("invalid_option_selection");
            }
        }

        validateVoteSelection(poll.get(), votesToProcess);

        VoteCollection voteCollection = new VoteCollection();
        voteCollection.setPollId(pollId);

        List<Vote> votesToSave = new ArrayList<>();
        for (Long optionId : votesToProcess) {
            Optional<Option> option = pollsService.getOptionById(optionId);
            Vote vote = pollsService.createVote(poll.get(), option.get(), voteCollection.getId());
            votesToSave.add(vote);
        }

        pollsService.saveVoteList(votesToSave);
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
