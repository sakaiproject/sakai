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

package org.sakaiproject.poll.impl.service;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.poll.api.model.Option;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.poll.api.service.PollImportService;
import org.sakaiproject.poll.api.util.PollUtils;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.CSVReader;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PollImportServiceImpl implements PollImportService {

    private final PollsService pollsService;
    private final MessageSource messageSource;
    private final FormattedText formattedText;
    private final UserTimeService userTimeService;

    public PollImportServiceImpl(PollsService pollsService,
                                 MessageSource messageSource,
                                 FormattedText formattedText,
                                 @Qualifier("org.sakaiproject.time.api.UserTimeService") UserTimeService userTimeService) {
        this.pollsService = pollsService;
        this.messageSource = messageSource;
        this.formattedText = formattedText;
        this.userTimeService = userTimeService;
    }

    @Override
    @Transactional
    public void importFromStrings(List<String> csvContents, String siteId, String ownerId, Locale locale) {
        List<ImportedPoll> importedPolls = new ArrayList<>();
        for (String csv : csvContents) {
            importedPolls.addAll(parseImportedPolls(csv, locale));
        }

        for (ImportedPoll ip : importedPolls) {
            Poll poll = buildPoll(ip, siteId, ownerId, locale);
            pollsService.savePoll(poll);
        }
    }

    public List<ImportedPoll> parseImportedPolls(String csvContent, Locale locale) {
        List<ImportedPoll> importedPolls = new ArrayList<>();
        if (StringUtils.isBlank(csvContent)) {
            return importedPolls;
        }

        try (CSVReader reader = new CSVReader(new StringReader(csvContent))) {
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (isBlankRow(row)) {
                    continue;
                }

                String question = normalizeCell(row[0]);
                String details = cellValue(row, 1);
                String openDate = cellValue(row, 2);
                String closeDate = cellValue(row, 3);
                String minOptions = cellValue(row, 4);
                String maxOptions = cellValue(row, 5);
                String displayResult = cellValue(row, 6);
                List<String> options = new ArrayList<>();
                for (int i = 7; i < row.length; i++) {
                    String optionText = normalizeCell(row[i]);
                    if (StringUtils.isNotBlank(optionText)) {
                        options.add(optionText);
                    }
                }

                if (StringUtils.isBlank(question) || options.size() < 2) {
                    throw new IllegalArgumentException(messageSource.getMessage("poll_import_error_wrongformat", null, locale));
                }

                importedPolls.add(new ImportedPoll(
                    question,
                    details,
                    parseDateTime(openDate, locale),
                    parseDateTime(closeDate, locale),
                    parseInteger(minOptions, 1, locale),
                    parseInteger(maxOptions, 1, locale),
                    parseDisplayResult(displayResult, locale),
                    options
                ));
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException(messageSource.getMessage("poll_import_error_wrongformat", null, locale), e);
        }

        return importedPolls;
    }

    private Poll buildPoll(ImportedPoll importedPoll, String siteId, String ownerId, Locale locale) {
        Poll poll = new Poll();
        int minOptions = importedPoll.minOptions();
        int maxOptions = importedPoll.maxOptions();
        List<String> sanitizedOptions = new ArrayList<>();

        poll.setText(importedPoll.question());
        poll.setDescription(cleanupFormattedText(importedPoll.details()));
        poll.setSiteId(siteId);
        poll.setOwner(ownerId);
        poll.setDisplayResult(StringUtils.defaultIfBlank(importedPoll.displayResult(), "open"));
        poll.setMinOptions(minOptions);
        poll.setMaxOptions(maxOptions);
        poll.setLimitVoting(true);
        poll.setPublic(false);

        if (importedPoll.openDate() != null) {
            poll.setVoteOpen(importedPoll.openDate().atZone(getUserZoneId()).toInstant());
        } else {
            poll.setVoteOpen(LocalDateTime.now(getUserZoneId()).truncatedTo(ChronoUnit.MINUTES).atZone(getUserZoneId()).toInstant());
        }

        if (importedPoll.closeDate() != null) {
            poll.setVoteClose(importedPoll.closeDate().atZone(getUserZoneId()).toInstant());
        } else {
            poll.setVoteClose(LocalDateTime.now(getUserZoneId()).truncatedTo(ChronoUnit.MINUTES).plusYears(1).atZone(getUserZoneId()).toInstant());
        }

        for (String optionText : importedPoll.options()) {
            String cleanedOption = cleanupFormattedText(optionText);
            if (StringUtils.isNotBlank(cleanedOption)) {
                sanitizedOptions.add(cleanedOption);
            }
        }

        if (sanitizedOptions.size() < 2) {
            throw new IllegalArgumentException(messageSource.getMessage("poll_import_error_wrongformat", null, locale));
        }

        if (poll.getMinOptions() > poll.getMaxOptions()) {
            throw new IllegalArgumentException(messageSource.getMessage("poll_import_error_limits", null, locale));
        }

        if (poll.getVoteOpen() != null && poll.getVoteClose() != null && poll.getVoteOpen().isAfter(poll.getVoteClose())) {
            throw new IllegalArgumentException(messageSource.getMessage("poll_import_error_dates", null, locale));
        }

        if (poll.getMinOptions() > sanitizedOptions.size() || poll.getMaxOptions() > sanitizedOptions.size()) {
            throw new IllegalArgumentException(messageSource.getMessage("poll_import_error_limits", null, locale));
        }

        for (String optionText : sanitizedOptions) {
            Option option = new Option();
            option.setText(optionText);
            poll.addOption(option);
        }

        return poll;
    }

    private boolean isBlankRow(String[] row) {
        if (row == null || row.length == 0) {
            return true;
        }

        for (String cell : row) {
            if (StringUtils.isNotBlank(normalizeCell(cell))) {
                return false;
            }
        }
        return true;
    }

    private String normalizeCell(String value) {
        String normalized = StringUtils.defaultString(value);
        if (normalized.startsWith("\uFEFF")) {
            normalized = normalized.substring(1);
        }
        return StringUtils.trimToEmpty(normalized);
    }

    private String cellValue(String[] row, int index) {
        if (row == null || index < 0 || index >= row.length) {
            return StringUtils.EMPTY;
        }
        return normalizeCell(row[index]);
    }

    private LocalDateTime parseDateTime(String value, Locale locale) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        try {
            return LocalDateTime.parse(value);
        } catch (Exception e) {
            throw new IllegalArgumentException(messageSource.getMessage("poll_import_error_dates", null, locale), e);
        }
    }

    private int parseInteger(String value, int defaultValue, Locale locale) {
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }

        try {
            int parsed = Integer.parseInt(value);
            if (parsed < 1) {
                throw new IllegalArgumentException(messageSource.getMessage("poll_import_error_limits", null, locale));
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(messageSource.getMessage("poll_import_error_number", null, locale), e);
        }
    }

    private String parseDisplayResult(String value, Locale locale) {
        String displayResultCode = StringUtils.defaultIfBlank(value, "1");
        return switch (displayResultCode) {
            case "1" -> "open";
            case "2" -> "afterVoting";
            case "3" -> "afterClosing";
            case "4" -> "never";
            default -> throw new IllegalArgumentException(messageSource.getMessage("poll_import_error_display", null, locale));
        };
    }

    private String cleanupFormattedText(String text) {
        String processed = formattedText.processFormattedText(StringUtils.defaultString(text), null, true, true);
        return PollUtils.cleanupHtmlPtags(processed);
    }

    private ZoneId getUserZoneId() {
        TimeZone timeZone = userTimeService.getLocalTimeZone();
        return timeZone != null ? timeZone.toZoneId() : ZoneId.systemDefault();
    }

    public record ImportedPoll(String question, String details, LocalDateTime openDate, LocalDateTime closeDate,
                                int minOptions, int maxOptions, String displayResult,
                                List<String> options) { }

}
