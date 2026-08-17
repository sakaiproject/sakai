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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PollImportControllerTest {

    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");
    private static final String SITE_ID = "site-id";
    private static final String USER_ID = "user-id";

    private PollsService pollsService;
    private PollImportController controller;

    @Before
    public void setUp() {
        MessageSource messageSource = mock(MessageSource.class);
        ToolManager toolManager = mock(ToolManager.class);
        SessionManager sessionManager = mock(SessionManager.class);
        pollsService = mock(PollsService.class);
        UserDirectoryService userDirectoryService = mock(UserDirectoryService.class);
        Placement placement = mock(Placement.class);

        when(toolManager.getCurrentPlacement()).thenReturn(placement);
        when(placement.getContext()).thenReturn(SITE_ID);
        when(sessionManager.getCurrentSessionUserId()).thenReturn(USER_ID);
        when(pollsService.isAllowedPollAdd(SITE_ID)).thenReturn(true);

        controller = new PollImportController(
                messageSource,
                toolManager,
                sessionManager,
                pollsService,
                userDirectoryService);
    }

    @Test
    public void importPollsPreservesUtf8EmDash() {
        verifyImportedCsv(StandardCharsets.UTF_8);
    }

    @Test
    public void importPollsPreservesWindows1252EmDash() {
        verifyImportedCsv(WINDOWS_1252);
    }

    private void verifyImportedCsv(Charset charset) {
        String csv = "Question,Option 1,Option 2\nQuestion?,Yes — definitely,No\n";
        MockMultipartFile file = new MockMultipartFile(
                "pollUploadFile",
                "polls.csv",
                "text/csv",
                csv.getBytes(charset));

        String view = controller.importPolls(
                null,
                file,
                new RedirectAttributesModelMap(),
                Locale.ENGLISH,
                new ExtendedModelMap());

        Assert.assertEquals("redirect:/votePolls", view);
        verify(pollsService).importPollsFromCsv(eq(List.of(csv)), eq(SITE_ID), eq(USER_ID));
    }
}
