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

package org.sakaiproject.poll.test.service;

import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.poll.impl.service.PollImportServiceImpl;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.context.MessageSource;

public class PollImportServiceTests {

    private PollsService pollsService;
    private MessageSource messageSource;
    private FormattedText formattedText;
    private UserTimeService userTimeService;
    private PollImportServiceImpl importService;

    @Before
    public void setup() {
        pollsService = Mockito.mock(PollsService.class);
        messageSource = Mockito.mock(MessageSource.class);
        formattedText = Mockito.mock(FormattedText.class);
        userTimeService = Mockito.mock(UserTimeService.class);

        Mockito.when(messageSource.getMessage(Mockito.anyString(), Mockito.isNull(), Mockito.any(Locale.class)))
               .thenAnswer(inv -> inv.getArgument(0));
        Mockito.when(formattedText.processFormattedText(Mockito.anyString(), Mockito.isNull(), Mockito.eq(true), Mockito.eq(true)))
               .thenAnswer(inv -> inv.getArgument(0));
        Mockito.when(userTimeService.getLocalTimeZone()).thenReturn(TimeZone.getDefault());

        importService = new PollImportServiceImpl(pollsService, messageSource, formattedText, userTimeService);
    }

    @Test
    public void testImportValidCsvCreatesPolls() {
        String csv = "What is your favorite color?,,2026-06-01T09:00,2026-06-02T17:00,1,1,1,Blue,Green,Red\n";

        Mockito.when(pollsService.savePoll(Mockito.any(Poll.class))).thenAnswer(inv -> {
            Poll p = inv.getArgument(0);
            p.setId("generated-id");
            return p;
        });

        importService.importFromStrings(List.of(csv), "site-1", "owner-1", Locale.ENGLISH);

        ArgumentCaptor<Poll> captor = ArgumentCaptor.forClass(Poll.class);
        Mockito.verify(pollsService).savePoll(captor.capture());
        Poll saved = captor.getValue();
        Assert.assertEquals("What is your favorite color?", saved.getText());
        Assert.assertTrue(saved.getOptions().size() >= 2);
    }

    @Test
    public void testImportInvalidCsvThrows() {
        // only one option -> invalid
        String csv = "Question?,,2026-06-01T09:00,2026-06-02T17:00,1,1,1,OnlyOneOption\n";

        Assert.assertThrows(IllegalArgumentException.class, () ->
            importService.importFromStrings(List.of(csv), "site-1", "owner-1", Locale.ENGLISH)
        );
    }

    @Test
    public void testImportBlankRowsThrows() {
        String csv = "\uFEFF,,,\n,,,,,,,\n";

        Assert.assertThrows(IllegalArgumentException.class, () ->
            importService.importFromStrings(List.of(csv), "site-1", "owner-1", Locale.ENGLISH)
        );
        Mockito.verifyNoInteractions(pollsService);
    }

    @Test
    public void testImportQuotedDescriptionWithCommas() {
        String csv = "Question?,\"This, description, has, commas\",2026-06-01T09:00,2026-06-02T17:00,1,1,1,Opt1,Opt2\\n";

        Mockito.when(pollsService.savePoll(Mockito.any(Poll.class))).thenAnswer(inv -> inv.getArgument(0));

        importService.importFromStrings(List.of(csv), "site-1", "owner-1", Locale.ENGLISH);

        ArgumentCaptor<Poll> captor = ArgumentCaptor.forClass(Poll.class);
        Mockito.verify(pollsService).savePoll(captor.capture());
        Poll saved = captor.getValue();
        Assert.assertTrue(saved.getDescription().contains("This, description, has, commas"));
    }

    @Test
    public void testImportWithBOMAndWhitespaceTrimming() {
        String csv = "\uFEFFQuestion? , ,2026-06-01T09:00,2026-06-02T17:00,1,1,1, OptA , OptB \\n";
        Mockito.when(pollsService.savePoll(Mockito.any(Poll.class))).thenAnswer(inv -> inv.getArgument(0));

        importService.importFromStrings(List.of(csv), "site-1", "owner-1", Locale.ENGLISH);

        ArgumentCaptor<Poll> captor = ArgumentCaptor.forClass(Poll.class);
        Mockito.verify(pollsService).savePoll(captor.capture());
        Poll saved = captor.getValue();
        Assert.assertEquals("Question?", saved.getText());
        Assert.assertEquals("OptA", saved.getOptions().get(0).getText());
    }

    @Test
    public void testImportInvalidDateFormatThrows() {
        String csv = "Q?,,06/01/2026 09:00,2026-06-02T17:00,1,1,1,One,Two\\n";
        Assert.assertThrows(IllegalArgumentException.class, () ->
            importService.importFromStrings(List.of(csv), "site-1", "owner-1", Locale.ENGLISH)
        );
    }

    @Test
    public void testImportMinGreaterThanMaxThrows() {
        String csv = "Q?,,2026-06-01T09:00,2026-06-02T17:00,5,1,1,One,Two,Three\\n";
        Assert.assertThrows(IllegalArgumentException.class, () ->
            importService.importFromStrings(List.of(csv), "site-1", "owner-1", Locale.ENGLISH)
        );
    }

    @Test
    public void testImportNonNumericMinMaxThrows() {
        String csv = "Q?,,2026-06-01T09:00,2026-06-02T17:00,a,b,1,One,Two,Three\\n";
        Assert.assertThrows(IllegalArgumentException.class, () ->
            importService.importFromStrings(List.of(csv), "site-1", "owner-1", Locale.ENGLISH)
        );
    }

    @Test
    public void testImportMultipleRowsCreatesMultiplePolls() {
        String csv1 = "Q1,,2026-06-01T09:00,2026-06-02T17:00,1,1,1,A,B\\n";
        String csv2 = "Q2,,2026-07-01T09:00,2026-07-02T17:00,1,1,1,X,Y\\n";

        Mockito.when(pollsService.savePoll(Mockito.any(Poll.class))).thenAnswer(inv -> inv.getArgument(0));

        importService.importFromStrings(List.of(csv1, csv2), "site-1", "owner-1", Locale.ENGLISH);

        Mockito.verify(pollsService, Mockito.times(2)).savePoll(Mockito.any(Poll.class));
    }

    @Test
    public void testImportCloseBeforeOpenThrows() {
        String csv = "Q?,,2026-06-03T09:00,2026-06-02T17:00,1,1,1,One,Two\\n";
        Assert.assertThrows(IllegalArgumentException.class, () ->
            importService.importFromStrings(List.of(csv), "site-1", "owner-1", Locale.ENGLISH)
        );
    }
}
