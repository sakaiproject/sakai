/*
 * Copyright (c) 2026 The Apereo Foundation
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://opensource.org/licenses/ecl2
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.feedback.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.OutputStream;
import java.util.List;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.junit.Test;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.feedback.exception.AttachmentsTooBigException;

public class SakaiProxyTest {

    @Test
    public void rejectsCombinedAttachmentsAtOutgoingMailLimit() throws Exception {
        assertAttachmentsRejected(1, 1, 12, 6);
    }

    @Test
    public void rejectsCombinedAttachmentsOverContentUploadLimit() throws Exception {
        assertAttachmentsRejected(1, 2, 3 * 1024 * 1024, 600_000);
    }

    @Test
    public void rejectsCombinedAttachmentsOverFeedbackLimit() throws Exception {
        assertAttachmentsRejected(2, 1, 3 * 1024 * 1024, 600_000);
    }

    @Test
    public void displayedLimitFollowsOutgoingMailLimit() {
        ServerConfigurationService config = mock(ServerConfigurationService.class);
        when(config.getInt("content.upload.max", SakaiProxy.ATTACH_MAX_DEFAULT)).thenReturn(20);
        when(config.getInt("feedback.attach.max", SakaiProxy.ATTACH_MAX_DEFAULT)).thenReturn(20);
        when(config.getInt(EmailService.MAIL_SENDFROMSAKAI_MAXSIZE, EmailService.DEFAULT_MAXSIZE)).thenReturn(10_000_000);

        SakaiProxy feedback = new SakaiProxy();
        feedback.setServerConfigurationService(config);

        assertEquals(9_999_999L, feedback.getAttachmentLimitBytes());
        // 10,000,000 bytes is about 9.5 MB, so the form must not advertise 10 MB.
        assertEquals(9, feedback.getAttachmentLimit());
    }

    private void assertAttachmentsRejected(int contentUploadMax, int feedbackAttachMax, int mailMax, int attachmentBytes)
            throws Exception {
        ServerConfigurationService config = mock(ServerConfigurationService.class);
        when(config.getInt("content.upload.max", SakaiProxy.ATTACH_MAX_DEFAULT)).thenReturn(contentUploadMax);
        when(config.getInt("feedback.attach.max", SakaiProxy.ATTACH_MAX_DEFAULT)).thenReturn(feedbackAttachMax);
        when(config.getInt(EmailService.MAIL_SENDFROMSAKAI_MAXSIZE, EmailService.DEFAULT_MAXSIZE)).thenReturn(mailMax);

        SakaiProxy feedback = new SakaiProxy();
        feedback.setServerConfigurationService(config);

        DiskFileItem first = DiskFileItem.builder().setBufferSize(attachmentBytes + 1).setFileName("first.bin").get();
        DiskFileItem second = DiskFileItem.builder().setBufferSize(attachmentBytes + 1).setFileName("second.bin").get();
        byte[] bytes = new byte[attachmentBytes];
        try (OutputStream firstStream = first.getOutputStream(); OutputStream secondStream = second.getOutputStream()) {
            firstStream.write(bytes);
            secondStream.write(bytes);
        }

        assertThrows(AttachmentsTooBigException.class, () ->
                feedback.sendEmail(null, "sender@example.com", "helpdesk@example.com", false, "site",
                        Constants.HELPDESK, "Title", "Description", List.of(first, second), true,
                        "", "", "", "", "", "", ""));
    }
}
