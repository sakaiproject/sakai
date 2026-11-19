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
package org.sakaiproject.lessonbuildertool.tool.beans;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.lessonbuildertool.service.AjaxServer;

public class LessonsMimeTypeTest {

    /** Relative links retain the Ajax endpoint extension fallbacks without an HTTP probe. */
    @Test
    public void relativeLinksUseExtensionFallback() {
        AjaxServer server = new AjaxServer();
        assertEquals("image/unknown", server.getMimeType("/access/content/image.png?download=1"));
        assertEquals("text/html", server.getMimeType("pages/index.html#section"));
        assertEquals("application/xhtml+xml", server.getMimeType("//example.org/index.xhtml"));
        assertEquals("", server.getMimeType("/access/content/unknown"));
    }

    /** Relative media use the kernel MIME lookup while extensionless pages retain the HTML fallback. */
    @Test
    public void relativeLinksUseMediaFallbackInPageBean() {
        // ContentTypeImageService is a kernel boundary outside Lessons.
        ContentTypeImageService contentTypes = mock(ContentTypeImageService.class);
        when(contentTypes.getContentType("png")).thenReturn("image/png");
        when(contentTypes.getContentType("mp4")).thenReturn("video/mp4");
        when(contentTypes.getContentType("xhtml")).thenReturn("application/xhtml+xml");
        when(contentTypes.getContentType("unknown")).thenReturn("application/octet-stream");
        when(contentTypes.isUnknownType("application/octet-stream")).thenReturn(true);
        SimplePageBean bean = new SimplePageBean();
        bean.setContentTypeImageService(contentTypes);
        assertEquals("image/png", bean.getTypeOfUrl("/access/content/image.png?download=1#section"));
        assertEquals("video/mp4", bean.getTypeOfUrl("media/video.mp4"));
        assertEquals("text/html", bean.getTypeOfUrl("/access/content/page"));
        assertEquals("text/html", bean.getTypeOfUrl("/access/content/file.unknown"));
        assertEquals("text/html", bean.getTypeOfUrl("pages/index.html"));
        assertEquals("application/xhtml+xml", bean.getTypeOfUrl("//example.org/index.xhtml"));
    }
}
