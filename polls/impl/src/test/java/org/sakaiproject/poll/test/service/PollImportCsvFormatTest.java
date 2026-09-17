/*
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.poll.test.service;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.sakaiproject.poll.api.importformat.PollImportCsvFormat;

@RunWith(Parameterized.class)
public class PollImportCsvFormatTest {

    @Parameterized.Parameters(name = "{0}")
    public static Object[][] locales() {
        return new Object[][] {
            { "en-US", "6/1/26", "6/1/2026", "2/30/26", ',' },
            { "es-ES", "1/6/26", "1/6/2026", "30/2/26", ';' },
            { "en-GB", "01/06/26", "01/06/2026", "30/02/26", ',' },
            { "fr-FR", "01/06/26", "01/06/2026", "30/02/26", ';' },
            { "pt-BR", "01/06/26", "01/06/2026", "30/02/26", ';' },
            { "de-DE", "01.06.26", "01.06.2026", "30.02.26", ';' },
            { "ja-JP", "26/06/01", "2026/06/01", "26/02/30", ',' }
        };
    }

    private final Locale locale;
    private final String shortYearDate;
    private final String fullYearDate;
    private final String invalidDate;
    private final char separator;

    public PollImportCsvFormatTest(String localeTag, String shortYearDate, String fullYearDate,
            String invalidDate, char separator) {
        this.locale = Locale.forLanguageTag(localeTag);
        this.shortYearDate = shortYearDate;
        this.fullYearDate = fullYearDate;
        this.invalidDate = invalidDate;
        this.separator = separator;
    }

    @Test
    public void acceptsIsoAndLocalizedDates() {
        LocalDateTime expected = LocalDateTime.of(2026, 6, 1, 9, 0);
        for (String date : List.of("2026-06-01", shortYearDate, fullYearDate)) {
            Assert.assertEquals(expected, PollImportCsvFormat.parseDateTime(date, "09:00", locale));
        }
    }

    @Test
    public void usesAnExplicitCenturyForTwoDigitYears() {
        for (int year : List.of(2000, 2099)) {
            String date = shortYearDate.replace("26", year == 2000 ? "00" : "99");
            Assert.assertEquals(LocalDateTime.of(year, 6, 1, 0, 0),
                PollImportCsvFormat.parseDateTime(date, "", locale));
        }
        Assert.assertEquals(LocalDateTime.of(1999, 6, 1, 0, 0),
            PollImportCsvFormat.parseDateTime(fullYearDate.replace("2026", "1999"), "", locale));
    }

    @Test
    public void rejectsInvalidDatesAndPartialYears() {
        for (String date : List.of(invalidDate, fullYearDate.replace("2026", "026"), "2026-02-30")) {
            DateTimeParseException exception = Assert.assertThrows(DateTimeParseException.class,
                () -> PollImportCsvFormat.parseDateTime(date, "09:00", locale));
            Assert.assertEquals(date, exception.getParsedString());
        }
    }

    @Test
    public void samplePreservesCellsAndUsesTheLocaleSeparator() throws Exception {
        List<String> headers = new ArrayList<>(PollImportCsvFormat.buildColumnHeaders(PollImportCsvFormat::defaultEnglishHeader));
        headers.set(1, "Description, \"notes\";\nmore");
        String csv = PollImportCsvFormat.buildSampleCsv(headers, locale);

        try (CSVReader reader = new CSVReaderBuilder(new StringReader(csv))
                .withCSVParser(new CSVParserBuilder().withSeparator(separator).build()).build()) {
            Assert.assertArrayEquals(headers.toArray(String[]::new), reader.readNext());
            String[] sample = reader.readNext();
            Assert.assertEquals(LocalDateTime.of(2026, 5, 29, 9, 0), PollImportCsvFormat.parseDateTime(
                sample[PollImportCsvFormat.COL_OPEN_DATE], sample[PollImportCsvFormat.COL_OPEN_TIME], locale));
            Assert.assertEquals(LocalDateTime.of(2026, 5, 30, 17, 0), PollImportCsvFormat.parseDateTime(
                sample[PollImportCsvFormat.COL_CLOSE_DATE], sample[PollImportCsvFormat.COL_CLOSE_TIME], locale));
            Assert.assertNull(reader.readNext());
        }
    }
}
