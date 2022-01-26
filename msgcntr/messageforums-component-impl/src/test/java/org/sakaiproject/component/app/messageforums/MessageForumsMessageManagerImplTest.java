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


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.common.type.Type;
import org.sakaiproject.api.common.type.TypeManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageRecipientImpl;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MsgcntrTestConfiguration.class})
public class MessageForumsMessageManagerImplTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    @Qualifier("org.sakaiproject.api.app.messageforums.MessageForumsMessageManager")
    private MessageForumsMessageManager messageForumsMessageManager;

    @Autowired
    @Qualifier("org.sakaiproject.tool.api.ToolManager")
    private ToolManager toolManager;

    @Autowired
    @Qualifier("org.sakaiproject.api.common.type.TypeManager")
    private TypeManager typeManager;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private IdManager idManager;

    private static final String TYPE_ID = "ASDF";
    private static final String USER_ID = "test-user";
    private static final String CONTEXT_ID = "12345";

    List<PrivateMessage> messages = new ArrayList<>();

    private enum PRIORITY {
        pvt_priority_normal,
        pvt_priority_low,
        pvt_priority_high
    }

    @Before
    public void setUp() throws Exception {
        TestUtil.setRunningTests(true);

        //Setup type manager stuff
        Type type = mock(Type.class);
        when(typeManager.getType(any(), any(), any())).thenReturn(type);
        when(type.getUuid()).thenReturn(TYPE_ID);

        Placement placement = mock(Placement.class);
        when(placement.getContext()).thenReturn(CONTEXT_ID);
        when(toolManager.getCurrentPlacement()).thenReturn(placement);

        Tool tool = mock(Tool.class);
        when(tool.getId()).thenReturn("asdf");
        when(toolManager.getCurrentTool()).thenReturn(tool);

        when(idManager.createUuid()).thenReturn(UUID.randomUUID().toString());
        when(sessionManager.getCurrentSessionUserId()).thenReturn(USER_ID);

        seedMessageData();
    }

    private void seedMessageData() {
        messages.add(createMessage(new TheDate(2020, Calendar.DECEMBER, 1), "Normal Test Message December",
                "Hi there, from December!", PRIORITY.pvt_priority_normal.name()));
        messages.add(createMessage(new TheDate(2020, Calendar.NOVEMBER, 1), "Low Test Message November",
                "Hi there, from November!", PRIORITY.pvt_priority_low.name()));
        messages.add(createMessage(new TheDate(2020, Calendar.NOVEMBER, 1), "High Test Message November",
                "Hi there, from November!  High priority.", PRIORITY.pvt_priority_high.name()));
        messages.add(createMessage(new TheDate(2020, Calendar.OCTOBER, 1), "High Test Message October",
                "Hi there, from October!", PRIORITY.pvt_priority_high.name()));

        for (PrivateMessage pm : messages) {
            String result = messageForumsMessageManager.saveMessage(pm);
            Assert.assertNotNull(result);
        }
    }

    private PrivateMessage createMessage(TheDate when, String title, String body, String priority) {
        Calendar cal = Calendar.getInstance();
        cal.set(when.getYear(), when.getMonth(), when.getDay());
        Date theDate = cal.getTime();
        PrivateMessage pm = messageForumsMessageManager.createPrivateMessage();
        pm.setBody(body);
        pm.setCreated(theDate);
        pm.setModified(theDate);
        pm.setModifiedBy(USER_ID);
        pm.setTitle(title);
        pm.setAuthor(USER_ID);
        pm.setDeleted(false);
        pm.setLabel(priority);

        pm.setRecipients(Collections.singletonList(new PrivateMessageRecipientImpl(USER_ID, TYPE_ID, CONTEXT_ID, false, false)));

        return pm;
    }

    @Test
    public void testGetOneMessage() {
        Message message = messageForumsMessageManager.getMessageById(1L);
        Assert.assertNotNull(message);
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
        List list = messageForumsMessageManager.findPvtMsgsBySearchText(TYPE_ID, sd.getSearchText(), sd.getSearchFromDate(),
                sd.getSearchToDate(), sd.getSelectedLabel(), sd.isSearchByText(), sd.isSearchByAuthor(), sd.isSearchByBody(),
                sd.isSearchByLabel(), sd.isSearchByDate());
        // Sort so we can have an expected ordering of the results
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
