/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2010, 2011, 2012, 2013, 2014 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.content.impl;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Simple tests for fixTypeAndId in BaseContentService
 */
public class BaseContentServiceFixTypeAndIdTest {

    private BaseContentService contentHostingService;
    private String id;
    private String type;
    private Map extType;

    @Before
    public void setUp() {

        contentHostingService = new DbContentService();
        BasicContentTypeImageService mockBasicContentTypeImageService = mock(BasicContentTypeImageService.class);
        contentHostingService.setContentTypeImageService(mockBasicContentTypeImageService);

        when(mockBasicContentTypeImageService.getContentTypeExtension("audio/wav")).thenReturn("wav");
        when(mockBasicContentTypeImageService.getContentTypeExtension("image/png")).thenReturn("png");
    }

    @Test
    public void testAttachmentNoExtensionSimpleId() throws Exception {
        id = "/attachment/foobarbaz/fckeditor/e3ec10b2-62de-4492-bb39-c582249269e0/9dcf51f8-8889-4ad3-b430-f8ddaaaea461";
        type = "audio/wav";
        extType = contentHostingService.fixTypeAndId(id, type);
        assertEquals(id + ".wav", extType.get("id"));        // append extension
        assertEquals(type, extType.get("type"));
    }

    @Test
    public void testAttachmentNoExtensionDashId() throws Exception {
        id = "/attachment/foo-bar-baz/fckeditor/e3ec10b2-62de-4492-bb39-c582249269e0/9dcf51f8-8889-4ad3-b430-f8ddaaaea461";
        type = "audio/wav";
        extType = contentHostingService.fixTypeAndId(id, type);
        assertEquals(id + ".wav", extType.get("id"));        // append extension
        assertEquals(type, extType.get("type"));
    }

    @Test
    public void testAttachmentNoExtensionUnderscore() throws Exception {
        id = "/attachment/foo_bar_baz/fckeditor/e3ec10b2-62de-4492-bb39-c582249269e0/9dcf51f8-8889-4ad3-b430-f8ddaaaea461";
        type = "audio/wav";
        extType = contentHostingService.fixTypeAndId(id, type);
        assertEquals(id + ".wav", extType.get("id"));        // append extension
        assertEquals(type, extType.get("type"));

    }

    @Test
    public void testAttachmentNoExtensionDots() {
        id = "/attachment/foo.bar.baz/fckeditor/e3ec10b2-62de-4492-bb39-c582249269e0/9dcf51f8-8889-4ad3-b430-f8ddaaaea461";
        type = "audio/wav";
        extType = contentHostingService.fixTypeAndId(id, type);
        assertEquals(id + ".wav", extType.get("id"));        // append extension
        assertEquals(type, extType.get("type"));
    }

    @Test
    public void testGroupNoExtensionSimpleId() {
        id = "/group/foobarbaz/9dcf51f8-8889-4ad3-b430-f8ddaaaea461";
        type = "image/png";
        extType = contentHostingService.fixTypeAndId(id, type);
        assertEquals(id + ".png", extType.get("id"));        // append extension
        assertEquals(type, extType.get("type"));
    }

    @Test
    public void testGroupNoExtensionDashId() throws Exception {
        id = "/group/foo-bar-baz/9dcf51f8-8889-4ad3-b430-f8ddaaaea461";
        type = "image/png";
        extType = contentHostingService.fixTypeAndId(id, type);
        assertEquals(id + ".png", extType.get("id"));        // append extension
        assertEquals(type, extType.get("type"));
    }

    @Test
    public void testGroupNoExtensionUnderscore() {
        id = "/group/foo_bar_baz/9dcf51f8-8889-4ad3-b430-f8ddaaaea461";
        type = "image/png";
        extType = contentHostingService.fixTypeAndId(id, type);
        assertEquals(id + ".png", extType.get("id"));        // append extension
        assertEquals(type, extType.get("type"));
    }

    @Test
    public void testGroupNoExtensionDots() {
        id = "/group/foo.bar.baz/9dcf51f8-8889-4ad3-b430-f8ddaaaea461";
        type = "image/png";
        extType = contentHostingService.fixTypeAndId(id, type);
        assertEquals(id + ".png", extType.get("id"));        // append extension
        assertEquals(type, extType.get("type"));
    }

    @Test
    public void testAttachmentSimpleId() {
        id = "/attachment/foobarbaz/fckeditor/e3ec10b2-62de-4492-bb39-c582249269e0/9dcf51f8-8889-4ad3-b430-f8ddaaaea461.wav";
        type = "audio/wav";
        extType = contentHostingService.fixTypeAndId(id, type);
        assertEquals(id, extType.get("id"));        // don't append extension to id
        assertEquals(type, extType.get("type"));
    }

    @Test
    public void testAttachmentDashId() {
        id = "/attachment/foo-bar-baz/fckeditor/e3ec10b2-62de-4492-bb39-c582249269e0/9dcf51f8-8889-4ad3-b430-f8ddaaaea461.wav";
        type = "audio/wav";
        extType = contentHostingService.fixTypeAndId(id, type);
        assertEquals(id, extType.get("id"));        // don't append extension to id
        assertEquals(type, extType.get("type"));
    }

    @Test
    public void testAttachmentUnderscore() {
        id = "/attachment/foo_bar_baz/fckeditor/e3ec10b2-62de-4492-bb39-c582249269e0/9dcf51f8-8889-4ad3-b430-f8ddaaaea461.wav";
        type = "audio/wav";
        extType = contentHostingService.fixTypeAndId(id, type);
        assertEquals(id, extType.get("id"));        // don't append extension to id
        assertEquals(type, extType.get("type"));
    }

    @Test
    public void testAttachmentDots() {
        id = "/attachment/foo.bar.baz/fckeditor/e3ec10b2-62de-4492-bb39-c582249269e0/9dcf51f8-8889-4ad3-b430-f8ddaaaea461.wav";
        type = "audio/wav";
        extType = contentHostingService.fixTypeAndId(id, type);
        assertEquals(id, extType.get("id"));        // don't append extension to id
        assertEquals(type, extType.get("type"));
    }

    @Test
    public void testGroupSimpleId() {
        id = "/group/foobarbaz/9dcf51f8-8889-4ad3-b430-f8ddaaaea461.png";
        type = "image/png";
        extType = contentHostingService.fixTypeAndId(id, type);
        assertEquals(id, extType.get("id"));        // don't append extension to id
        assertEquals(type, extType.get("type"));
    }

    @Test
    public void testGroupDashId() {
        id = "/group/foo-bar-baz/9dcf51f8-8889-4ad3-b430-f8ddaaaea461.png";
        type = "image/png";
        extType = contentHostingService.fixTypeAndId(id, type);
        assertEquals(id, extType.get("id"));        // don't append extension to id
        assertEquals(type, extType.get("type"));
    }

    @Test
    public void testGroupUnderscore() {
        id = "/group/foo_bar_baz/9dcf51f8-8889-4ad3-b430-f8ddaaaea461.png";
        type = "image/png";
        extType = contentHostingService.fixTypeAndId(id, type);
        assertEquals(id, extType.get("id"));        // don't append extension to id
        assertEquals(type, extType.get("type"));
    }

    @Test
    public void testGroupDots() {
        id = "/group/foo.bar.baz/9dcf51f8-8889-4ad3-b430-f8ddaaaea461.png";
        type = "image/png";
        extType = contentHostingService.fixTypeAndId(id, type);
        assertEquals(id, extType.get("id"));
        assertEquals(type, extType.get("type"));
    }

    @Test
    public void testUnknownType() {
        id = "/group/siteId/file.unknown";
        type = "";
        extType = contentHostingService.fixTypeAndId(id, type);
        assertEquals(id, extType.get("id"));
        assertNull(extType.get("type"));
    }

    @Test
    public void testPathWithDots() {
        id = "/group/siteId/folder.with.dots/filename.wav";
        type = "audio/wav";
        extType = contentHostingService.fixTypeAndId(id, type);
        assertEquals(id, extType.get("id"));
        assertEquals(type, extType.get("type"));
    }

    @Test
    public void testFilenameWithDots() {
        id = "/group/siteId/filename.with.dots.wav";
        type = "audio/wav";
        extType = contentHostingService.fixTypeAndId(id, type);
        assertEquals(id, extType.get("id"));
        assertEquals(type, extType.get("type"));
    }
}