/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.site.tool.helper.order;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.site.tool.helper.order.impl.SitePageEditHandler;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ToolOrderControllerTest {

    private ToolOrderController controller;
    private SitePageEditHandler pageEditHandler;
    private MessageSource messageSource;

    @Before
    public void setUp() {
        pageEditHandler = mock(SitePageEditHandler.class);
        messageSource = mock(MessageSource.class);
        controller = new ToolOrderController(pageEditHandler, messageSource);
    }

    @Test
    public void visibilityRequiresExplicitVisibleValue() {
        when(messageSource.getMessage(eq("error_visibility_required"), any(), eq(Locale.ENGLISH)))
                .thenReturn("Page visibility value is required.");

        ResponseEntity<Map<String, Object>> response = controller.visibility("page1",
                new ToolOrderController.VisibilityRequest(), Locale.ENGLISH);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Page visibility value is required.", response.getBody().get("message"));
        verify(pageEditHandler, never()).setPageVisible(anyString(), anyBoolean());
    }

    @Test
    public void accessRequiresExplicitEnabledValue() {
        when(messageSource.getMessage(eq("error_access_required"), any(), eq(Locale.ENGLISH)))
                .thenReturn("Page access value is required.");

        ResponseEntity<Map<String, Object>> response = controller.access("page1",
                new ToolOrderController.AccessRequest(), Locale.ENGLISH);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Page access value is required.", response.getBody().get("message"));
        verify(pageEditHandler, never()).setPageEnabled(anyString(), anyBoolean());
    }
}
