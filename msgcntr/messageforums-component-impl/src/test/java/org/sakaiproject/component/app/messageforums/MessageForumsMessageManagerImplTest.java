/**
 * Copyright (c) 2003-2021 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.component.app.messageforums;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageRecipientImpl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

public class MessageForumsMessageManagerImplTest {

    private List<PrivateMessage> messages = new ArrayList<>();

    private static final String TYPE_ID = "ASDF";
    private static final String USER_ID = "test-user";
    private static final String CONTEXT_ID = "12345";

    private enum PRIORITY {
        pvt_priority_normal,
        pvt_priority_low,
        pvt_priority_high
    }

    @Before
    public void setUp() throws Exception {
        TestUtil.setRunningTests(true);
        seedMessageData();
    }

    private void seedMessageData() {
        messages.clear();
        messages.add(createMessage(new TheDate(2020, Calendar.DECEMBER, 1), "Normal Test Message December",
                "Hi there, from December!", PRIORITY.pvt_priority_normal.name()));
        messages.add(createMessage(new TheDate(2020, Calendar.NOVEMBER, 1), "Low Test Message November",
                "Hi there, from November!", PRIORITY.pvt_priority_low.name()));
        messages.add(createMessage(new TheDate(2020, Calendar.NOVEMBER, 1), "High Test Message November",
                "Hi there, from November!  High priority.", PRIORITY.pvt_priority_high.name()));
        messages.add(createMessage(new TheDate(2020, Calendar.OCTOBER, 1), "High Test Message October",
                "Hi there, from October!", PRIORITY.pvt_priority_high.name()));

        long id = 1;
        for (PrivateMessage pm : messages) {
            pm.setId(id++);
        }
    }

    private PrivateMessage createMessage(TheDate when, String title, String body, String priority) {
        Calendar cal = Calendar.getInstance();
        cal.set(when.getYear(), when.getMonth(), when.getDay());
        Date theDate = cal.getTime();

        PrivateMessageImpl pm = new PrivateMessageImpl();
        pm.setUuid(UUID.randomUUID().toString());
        pm.setTypeUuid("privateMessageAreaType");
        pm.setCreated(theDate);
        pm.setCreatedBy(USER_ID);
        pm.setModified(theDate);
        pm.setModifiedBy(USER_ID);
        pm.setTitle(title);
        pm.setBody(body);
        pm.setAuthor(USER_ID);
        pm.setDeleted(false);
        pm.setLabel(priority);
        pm.setDraft(Boolean.FALSE);
        pm.setHasAttachments(Boolean.FALSE);
        pm.setRecipients(Collections.singletonList(
                new PrivateMessageRecipientImpl(USER_ID, TYPE_ID, CONTEXT_ID, false, false)));
        return pm;
    }

    private List<PrivateMessage> filterMessages(
            String searchText, Date fromDate, Date toDate, String label,
            boolean byText, boolean byAuthor, boolean byBody,
            boolean byLabel, boolean byDate) {

        List<PrivateMessage> filtered = new ArrayList<>();
        for (PrivateMessage pm : messages) {
            boolean match = true;

            if (byDate) {
                if (fromDate != null && pm.getCreated() != null
                        && !pm.getCreated().after(fromDate)) match = false;
                if (toDate != null && pm.getCreated() != null
                        && !pm.getCreated().before(toDate)) match = false;
            }
            if (match && byLabel && label != null
                    && !label.equals(pm.getLabel())) {
                match = false;
            }
            if (match && byText && searchText != null) {
                boolean textMatch =
                        (pm.getTitle() != null && pm.getTitle().contains(searchText))
                     || (byBody && pm.getBody() != null && pm.getBody().contains(searchText));
                if (!textMatch) match = false;
            }
            if (match && byAuthor && searchText != null) {
                if (pm.getAuthor() == null || !pm.getAuthor().contains(searchText))
                    match = false;
            }

            if (match) filtered.add(pm);
        }
        return filtered;
    }

    @Test
    public void testGetOneMessage() {
        PrivateMessage message = messages.isEmpty() ? null : messages.get(0);
        Assert.assertNotNull(message);
        Assert.assertEquals(1L, (long) message.getId());
    }

    @Test
    public void testPMSearchAllMessages() {
        SearchData sd = SearchData.builder()
                .searchText("Hi")
                .searchByText(true)
                .searchByBody(true)
                .build();

        List<PrivateMessage> results = validateResults(sd);
        Assert.assertNotNull(results);
        Assert.assertFalse(results.isEmpty());
        Assert.assertEquals(messages.size(), results.size());
    }

    @Test
    public void testPMSearchByDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.OCTOBER, 10);
        Date oct10 = cal.getTime();

        cal.set(2020, Calendar.NOVEMBER, 10);
        Date nov10 = cal.getTime();

        SearchData sd = SearchData.builder()
                .searchFromDate(oct10)
                .searchToDate(nov10)
                .searchByDate(true)
                .build();

        List<PrivateMessage> results = validateResults(sd);
        Assert.assertNotNull(results);
        Assert.assertFalse(results.isEmpty());
        Assert.assertEquals(2, results.size());
        Assert.assertEquals("High Test Message November", results.get(0).getTitle());
        Assert.assertEquals("Low Test Message November", results.get(1).getTitle());
    }

    @Test
    public void testPMSearchByDateAndLabel() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.OCTOBER, 10);
        Date oct10 = cal.getTime();

        cal.set(2020, Calendar.NOVEMBER, 10);
        Date nov10 = cal.getTime();

        SearchData sd = SearchData.builder()
                .searchFromDate(oct10)
                .searchToDate(nov10)
                .selectedLabel(PRIORITY.pvt_priority_high.name())
                .searchByDate(true)
                .searchByLabel(true)
                .build();

        List<PrivateMessage> results = validateResults(sd);
        Assert.assertNotNull(results);
        Assert.assertFalse(results.isEmpty());
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("High Test Message November", results.get(0).getTitle());
    }

    @Test
    public void testPMSearchBySubjectAndLabel() {

        SearchData sd = SearchData.builder()
                .selectedLabel(PRIORITY.pvt_priority_high.name())
                .searchByLabel(true)
                .searchByText(true)
                .searchText("Message")
                .build();

        List<PrivateMessage> results = validateResults(sd);
        Assert.assertNotNull(results);
        Assert.assertFalse(results.isEmpty());
        Assert.assertEquals(2, results.size());
        Assert.assertEquals("High Test Message November", results.get(0).getTitle());
        Assert.assertEquals("High Test Message October", results.get(1).getTitle());
    }

    private List<PrivateMessage> validateResults(SearchData sd) {
        List<PrivateMessage> list = filterMessages(
                sd.getSearchText(), sd.getSearchFromDate(), sd.getSearchToDate(),
                sd.getSelectedLabel(), sd.isSearchByText(), sd.isSearchByAuthor(),
                sd.isSearchByBody(), sd.isSearchByLabel(), sd.isSearchByDate());
        // Sort so we can have an expected ordering of the result
        list.sort(Comparator.comparing(PrivateMessage::getTitle).thenComparing(PrivateMessage::getCreated));
        return list;
    }

    @Data
    @AllArgsConstructor
    private static class TheDate implements Serializable {
        private int year;
        private int month;
        private int day;
    }

    @Builder
    @Getter
    private static class SearchData implements Serializable {
        private String searchText;
        private Date searchFromDate;
        private Date searchToDate;
        private String selectedLabel;
        private boolean searchByText;
        private boolean searchByAuthor;
        private boolean searchByBody;
        private boolean searchByLabel;
        private boolean searchByDate;
    }
}
