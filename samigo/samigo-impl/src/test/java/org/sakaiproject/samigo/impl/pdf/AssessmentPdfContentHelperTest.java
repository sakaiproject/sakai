/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.samigo.impl.pdf;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfWriter;

public class AssessmentPdfContentHelperTest {

    private static final byte[] ONE_PIXEL_PNG = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z5BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

    private AssessmentPdfContentHelper helperWithoutContentHosting() {
        return new AssessmentPdfContentHelper(mock(ContentHostingService.class));
    }

    @Test
    public void cleanTextStripsHtmlAndRemovesTables() {
        AssessmentPdfContentHelper helper = helperWithoutContentHosting();
        assertEquals("Hello world", helper.cleanText("<p>Hello <b>world</b></p><table><tr><td>x</td></tr></table>"));
    }

    @Test
    public void loadContentImageReturnsNullWhenPermissionDenied() throws Exception {
        ContentHostingService contentHostingService = mock(ContentHostingService.class);
        when(contentHostingService.getResource("/group/site/private.png"))
                .thenThrow(new org.sakaiproject.exception.PermissionException("", "", ""));

        AssessmentPdfContentHelper helper = new AssessmentPdfContentHelper(contentHostingService);
        assertFalse(helper.loadContentImage("/group/site/private.png").isPresent());
    }

    @Test
    public void addImageElementsToTableSkipsImageWhenPermissionDenied() throws Exception {
        ContentHostingService contentHostingService = mock(ContentHostingService.class);
        when(contentHostingService.getResource("/group/site/private.png"))
                .thenThrow(new org.sakaiproject.exception.PermissionException("", "", ""));

        AssessmentPdfContentHelper helper = new AssessmentPdfContentHelper(contentHostingService);
        com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(1);
        helper.addImageElementsToTable("<img src=\"/samigo/group/site/private.png\" />", table);
        assertEquals(0, table.getRows().size());
    }

    @Test
    public void buildContentAccessUrlNormalizesPrefixedResourceId() {
        assertEquals("/access/content/attachment/site/file.csv", AssessmentPdfContentHelper.buildContentAccessUrl("/access/content/attachment/site/file.csv"));
    }

    @Test
    public void resolveContentResourceIdStripsFullAccessUrl() {
        assertEquals("/attachment/TEST_003/file.csv", AssessmentPdfContentHelper.resolveContentResourceId("http://localhost:8080/access/content/attachment/TEST_003/file.csv"));
    }

    @Test
    public void resolveContentResourceIdHandlesAccessContentPath() {
        assertEquals("/group/SITE/image.png", AssessmentPdfContentHelper.resolveContentResourceId("/access/content/group/SITE/image.png"));
    }

    @Test
    public void addAttachmentListToDocumentSkipsEmptyList() throws Exception {
        AssessmentPdfContentHelper helper = helperWithoutContentHosting();
        Document document = new Document();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, output);
        document.open();
        helper.addAttachmentListToDocument(document, Collections.emptyList(), null, false);
        document.add(new com.lowagie.text.Paragraph(" "));
        document.close();
        assertTrue(output.size() > 0);
    }

    @Test
    public void resolveContentResourceIdHandlesSamigoPrefix() {
        assertEquals("/group/SITE/image.png", AssessmentPdfContentHelper.resolveContentResourceId("/samigo/group/SITE/image.png"));
    }

    @Test
    public void resolveContentResourceIdReturnsNullForBlank() {
        assertNull(AssessmentPdfContentHelper.resolveContentResourceId(" "));
    }

    @Test
    public void buildAttachmentListHtmlIncludesSamigoImageTagForPng() {
        AssessmentPdfContentHelper helper = helperWithoutContentHosting();
        AttachmentIfc attachment = mock(AttachmentIfc.class);
        when(attachment.getFilename()).thenReturn("diagram.png");
        when(attachment.getMimeType()).thenReturn("image/png");
        when(attachment.getResourceId()).thenReturn("/group/site/diagram.png");

        String html = helper.buildAttachmentListHtml(Collections.singletonList(attachment), "Attachments");

        assertTrue(html.contains("Attachments"));
        assertTrue(html.contains("diagram.png"));
        assertTrue(html.contains("<img src=\"/samigo/group/site/diagram.png\""));
    }

    @Test
    public void buildAttachmentListHtmlOmitsImageTagForNonImageMimeType() {
        AssessmentPdfContentHelper helper = helperWithoutContentHosting();
        AttachmentIfc attachment = mock(AttachmentIfc.class);
        when(attachment.getFilename()).thenReturn("notes.pdf");
        when(attachment.getMimeType()).thenReturn("application/pdf");
        when(attachment.getResourceId()).thenReturn("/group/site/notes.pdf");

        String html = helper.buildAttachmentListHtml(Collections.singletonList(attachment), "Attachments");

        assertTrue(html.contains("notes.pdf"));
        assertFalse(html.contains("<img"));
    }

    @Test
    public void isImageMimeTypeRecognizesSupportedImageTypes() {
        assertTrue(AssessmentPdfContentHelper.isImageMimeType("image/png"));
        assertTrue(AssessmentPdfContentHelper.isImageMimeType("IMAGE/JPEG"));
        assertFalse(AssessmentPdfContentHelper.isImageMimeType("application/pdf"));
    }

    @Test
    public void addAttachmentListToDocumentLoadsImageFromContentHosting() throws Exception {
        ContentHostingService contentHostingService = mock(ContentHostingService.class);
        ContentResource contentResource = mock(ContentResource.class);
        when(contentHostingService.getResource("/group/site/photo.png")).thenReturn(contentResource);
        when(contentResource.getContent()).thenReturn(ONE_PIXEL_PNG);

        AttachmentIfc attachment = mock(AttachmentIfc.class);
        when(attachment.getFilename()).thenReturn("photo.png");
        when(attachment.getMimeType()).thenReturn("image/png");
        when(attachment.getResourceId()).thenReturn("/group/site/photo.png");

        AssessmentPdfContentHelper helper = new AssessmentPdfContentHelper(contentHostingService);
        Document document = new Document();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, output);
        document.open();
        helper.addAttachmentListToDocument(document, Collections.singletonList(attachment), null, false);
        document.close();

        assertTrue(output.size() > 200);
        verify(contentHostingService).getResource("/group/site/photo.png");
    }
}
