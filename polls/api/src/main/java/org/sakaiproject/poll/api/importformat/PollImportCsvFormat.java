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

package org.sakaiproject.poll.api.importformat;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

public final class PollImportCsvFormat {

    public static final int COL_QUESTION = 0;
    public static final int COL_DESCRIPTION = 1;
    public static final int COL_ACCESS = 2;
    public static final int COL_GROUPS = 3;
    public static final int COL_OPEN_DATE = 4;
    public static final int COL_OPEN_TIME = 5;
    public static final int COL_CLOSE_DATE = 6;
    public static final int COL_CLOSE_TIME = 7;
    public static final int COL_MIN_OPTIONS = 8;
    public static final int COL_MAX_OPTIONS = 9;
    public static final int COL_DISPLAY_RESULT = 10;
    public static final int COL_FIRST_OPTION = 11;

    public static final int FIXED_COLUMN_COUNT = COL_FIRST_OPTION;
    public static final int SAMPLE_OPTION_COUNT = 3;

    public static final String HEADER_QUESTION_KEY = "poll_import_header_question";
    public static final String HEADER_DESCRIPTION_KEY = "poll_import_header_description";
    public static final String HEADER_ACCESS_KEY = "poll_import_header_access";
    public static final String HEADER_GROUPS_KEY = "poll_import_header_groups";
    public static final String HEADER_OPEN_DATE_KEY = "poll_import_header_open_date";
    public static final String HEADER_OPEN_TIME_KEY = "poll_import_header_open_time";
    public static final String HEADER_CLOSE_DATE_KEY = "poll_import_header_close_date";
    public static final String HEADER_CLOSE_TIME_KEY = "poll_import_header_close_time";
    public static final String HEADER_MIN_OPTIONS_KEY = "poll_import_header_min_options";
    public static final String HEADER_MAX_OPTIONS_KEY = "poll_import_header_max_options";
    public static final String HEADER_DISPLAY_RESULT_KEY = "poll_import_header_display_result";
    public static final String HEADER_OPTION_KEY = "poll_import_header_option";

    public static final String[] FIXED_HEADER_MESSAGE_KEYS = {
        HEADER_QUESTION_KEY,
        HEADER_DESCRIPTION_KEY,
        HEADER_ACCESS_KEY,
        HEADER_GROUPS_KEY,
        HEADER_OPEN_DATE_KEY,
        HEADER_OPEN_TIME_KEY,
        HEADER_CLOSE_DATE_KEY,
        HEADER_CLOSE_TIME_KEY,
        HEADER_MIN_OPTIONS_KEY,
        HEADER_MAX_OPTIONS_KEY,
        HEADER_DISPLAY_RESULT_KEY
    };

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");

    private PollImportCsvFormat() {
    }

    public static String defaultEnglishHeader(String messageKey) {
        return switch (messageKey) {
            case HEADER_QUESTION_KEY -> "Question";
            case HEADER_DESCRIPTION_KEY -> "Description";
            case HEADER_ACCESS_KEY -> "Access";
            case HEADER_GROUPS_KEY -> "Groups";
            case HEADER_OPEN_DATE_KEY -> "Opening date";
            case HEADER_OPEN_TIME_KEY -> "Opening time";
            case HEADER_CLOSE_DATE_KEY -> "Closing date";
            case HEADER_CLOSE_TIME_KEY -> "Closing time";
            case HEADER_MIN_OPTIONS_KEY -> "Minimum options";
            case HEADER_MAX_OPTIONS_KEY -> "Maximum options";
            case HEADER_DISPLAY_RESULT_KEY -> "Results visibility";
            case HEADER_OPTION_KEY -> "Option {0}";
            default -> messageKey;
        };
    }

    public static Function<String, String> headerLabelResolver(Function<String, String> bundleLookup) {
        return key -> {
            if (bundleLookup != null) {
                String value = bundleLookup.apply(key);
                if (StringUtils.isNotBlank(value) && !key.equals(value)) {
                    return value;
                }
            }
            return defaultEnglishHeader(key);
        };
    }

    public static List<String> buildColumnHeaders(Function<String, String> messageResolver) {
        List<String> headers = new ArrayList<>(FIXED_COLUMN_COUNT + SAMPLE_OPTION_COUNT);
        for (String key : FIXED_HEADER_MESSAGE_KEYS) {
            headers.add(messageResolver.apply(key));
        }
        for (int optionNumber = 1; optionNumber <= SAMPLE_OPTION_COUNT; optionNumber++) {
            headers.add(MessageFormat.format(messageResolver.apply(HEADER_OPTION_KEY), optionNumber));
        }
        return headers;
    }

    public static String buildSampleCsv(List<String> columnHeaders) {
        if (columnHeaders == null || columnHeaders.isEmpty()) {
            throw new IllegalArgumentException("columnHeaders must not be empty");
        }

        StringBuilder csv = new StringBuilder();
        appendCsvRow(csv, columnHeaders);
        appendCsvRow(csv, List.of(sampleDataRow()));
        return csv.toString();
    }

    public static String[] sampleDataRow() {
        return new String[] {
            "What is your favorite color?",
            "",
            "site",
            "",
            "2026-05-29",
            "09:00",
            "2026-05-30",
            "17:00",
            "1",
            "1",
            "1",
            "Blue",
            "Green",
            "Red"
        };
    }

    public static boolean isValidHeaderRow(String[] row, Function<String, String> messageResolver) {
        if (row == null || row.length < COL_FIRST_OPTION + 2) {
            return false;
        }

        for (int i = 0; i < FIXED_COLUMN_COUNT; i++) {
            String messageKey = FIXED_HEADER_MESSAGE_KEYS[i];
            if (!headerCellMatches(row[i], messageKey, messageResolver)) {
                return false;
            }
        }

        int optionHeaders = 0;
        for (int i = COL_FIRST_OPTION; i < row.length; i++) {
            String cell = normalizeCell(row[i]);
            if (StringUtils.isBlank(cell)) {
                continue;
            }
            optionHeaders++;
            if (!optionHeaderCellMatches(cell, optionHeaders, messageResolver)) {
                return false;
            }
        }

        return optionHeaders >= 2;
    }

    public static String formatHeaderRow(Function<String, String> messageResolver, int optionColumnCount) {
        List<String> headers = new ArrayList<>(FIXED_COLUMN_COUNT + optionColumnCount);
        for (String key : FIXED_HEADER_MESSAGE_KEYS) {
            headers.add(messageResolver.apply(key));
        }
        for (int optionNumber = 1; optionNumber <= optionColumnCount; optionNumber++) {
            headers.add(MessageFormat.format(messageResolver.apply(HEADER_OPTION_KEY), optionNumber));
        }
        StringBuilder csv = new StringBuilder();
        appendCsvRow(csv, headers);
        return csv.toString().trim();
    }

    public static LocalDateTime parseDateTime(String dateValue, String timeValue) throws DateTimeParseException {
        if (StringUtils.isAllBlank(dateValue, timeValue)) {
            return null;
        }

        if (StringUtils.isBlank(dateValue)) {
            throw new DateTimeParseException("Date is required when a time is provided", StringUtils.defaultString(timeValue), 0);
        }

        LocalDate date = LocalDate.parse(dateValue, DATE_FORMAT);
        LocalTime time = LocalTime.MIDNIGHT;
        if (StringUtils.isNotBlank(timeValue)) {
            time = LocalTime.parse(timeValue, TIME_FORMAT);
        }
        return LocalDateTime.of(date, time);
    }

    public static String normalizeCell(String value) {
        String normalized = StringUtils.defaultString(value);
        if (normalized.startsWith("\uFEFF")) {
            normalized = normalized.substring(1);
        }
        return StringUtils.trimToEmpty(normalized);
    }

    public static String cellValue(String[] row, int index) {
        if (row == null || index < 0 || index >= row.length) {
            return StringUtils.EMPTY;
        }
        return normalizeCell(row[index]);
    }

    public static boolean isBlankRow(String[] row) {
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

    private static boolean headerCellMatches(String actual, String messageKey, Function<String, String> messageResolver) {
        String cell = normalizeCell(actual);
        return headerCellsMatch(cell, messageResolver.apply(messageKey))
                || headerCellsMatch(cell, defaultEnglishHeader(messageKey));
    }

    private static boolean optionHeaderCellMatches(String actual, int optionNumber, Function<String, String> messageResolver) {
        String cell = normalizeCell(actual);
        String expected = MessageFormat.format(messageResolver.apply(HEADER_OPTION_KEY), optionNumber);
        String expectedEnglish = MessageFormat.format(defaultEnglishHeader(HEADER_OPTION_KEY), optionNumber);
        return headerCellsMatch(cell, expected) || headerCellsMatch(cell, expectedEnglish);
    }

    private static boolean headerCellsMatch(String actual, String expected) {
        return normalizeCell(actual).equalsIgnoreCase(StringUtils.trimToEmpty(expected));
    }

    private static void appendCsvRow(StringBuilder csv, List<String> cells) {
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                csv.append(',');
            }
            csv.append(escapeCsvCell(cells.get(i)));
        }
        csv.append('\n');
    }

    private static String escapeCsvCell(String value) {
        String cell = StringUtils.defaultString(value);
        if (cell.contains(",") || cell.contains("\"") || cell.contains("\n") || cell.contains("\r")) {
            return "\"" + cell.replace("\"", "\"\"") + "\"";
        }
        return cell;
    }
}
