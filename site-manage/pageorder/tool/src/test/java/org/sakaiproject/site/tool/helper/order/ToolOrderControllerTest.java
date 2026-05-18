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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.site.tool.helper.order.impl.SitePageEditHandler;
import org.sakaiproject.site.tool.helper.order.model.ToolOrderPage;
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
    public void reorderSavesValidOrder() {
        List<String> pageIds = Arrays.asList("page2", "page1");
        ToolOrderController.ReorderRequest request = new ToolOrderController.ReorderRequest();
        request.setPageIds(pageIds);
        when(messageSource.getMessage(eq("success_order_saved"), any(), eq(Locale.ENGLISH)))
                .thenReturn("Tool order was saved");
        when(pageEditHandler.getPages()).thenReturn(Collections.emptyList());

        ResponseEntity<Map<String, Object>> response = controller.reorder(request, Locale.ENGLISH);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Tool order was saved", response.getBody().get("message"));
        verify(pageEditHandler).reorderPages(pageIds);
    }

    @Test
    public void reorderRequiresRequest() {
        when(messageSource.getMessage(eq("error_pageids"), any(), eq(Locale.ENGLISH)))
                .thenReturn("A valid page order is required.");

        ResponseEntity<Map<String, Object>> response = controller.reorder(null, Locale.ENGLISH);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("A valid page order is required.", response.getBody().get("message"));
        verify(pageEditHandler, never()).reorderPages(any());
    }

    @Test
    public void reorderRequiresPageIds() {
        when(messageSource.getMessage(eq("error_pageids"), any(), eq(Locale.ENGLISH)))
                .thenReturn("A valid page order is required.");

        ResponseEntity<Map<String, Object>> response = controller.reorder(
                new ToolOrderController.ReorderRequest(), Locale.ENGLISH);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("A valid page order is required.", response.getBody().get("message"));
        verify(pageEditHandler, never()).reorderPages(any());
    }

    @Test
    public void visibilitySavesValidValue() {
        ToolOrderPage row = new ToolOrderPage();
        row.setTitle("Overview");
        ToolOrderController.VisibilityRequest request = new ToolOrderController.VisibilityRequest();
        request.setVisible(true);
        when(pageEditHandler.setPageVisible("page1", true)).thenReturn(row);
        when(messageSource.getMessage(eq("success_visible"), any(), eq(Locale.ENGLISH)))
                .thenReturn("Overview is now visible to normal users");

        ResponseEntity<Map<String, Object>> response = controller.visibility("page1", request, Locale.ENGLISH);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Overview is now visible to normal users", response.getBody().get("message"));
        assertEquals(row, response.getBody().get("row"));
        verify(pageEditHandler).setPageVisible("page1", true);
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
    public void visibilityRequiresRequest() {
        when(messageSource.getMessage(eq("error_visibility_required"), any(), eq(Locale.ENGLISH)))
                .thenReturn("Page visibility value is required.");

        ResponseEntity<Map<String, Object>> response = controller.visibility("page1", null, Locale.ENGLISH);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Page visibility value is required.", response.getBody().get("message"));
        verify(pageEditHandler, never()).setPageVisible(anyString(), anyBoolean());
    }

    @Test
    public void accessSavesValidValue() {
        ToolOrderPage row = new ToolOrderPage();
        row.setTitle("Overview");
        ToolOrderController.AccessRequest request = new ToolOrderController.AccessRequest();
        request.setEnabled(true);
        when(pageEditHandler.setPageEnabled("page1", true)).thenReturn(row);
        when(messageSource.getMessage(eq("success_enabled"), any(), eq(Locale.ENGLISH)))
                .thenReturn("Overview is now enabled for normal users");

        ResponseEntity<Map<String, Object>> response = controller.access("page1", request, Locale.ENGLISH);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Overview is now enabled for normal users", response.getBody().get("message"));
        assertEquals(row, response.getBody().get("row"));
        verify(pageEditHandler).setPageEnabled("page1", true);
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

    @Test
    public void accessRequiresRequest() {
        when(messageSource.getMessage(eq("error_access_required"), any(), eq(Locale.ENGLISH)))
                .thenReturn("Page access value is required.");

        ResponseEntity<Map<String, Object>> response = controller.access("page1", null, Locale.ENGLISH);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Page access value is required.", response.getBody().get("message"));
        verify(pageEditHandler, never()).setPageEnabled(anyString(), anyBoolean());
    }

    @Test
    public void clientErrorsReturnBadRequest() {
        when(messageSource.getMessage(eq("status_error"), any(), eq(Locale.ENGLISH)))
                .thenReturn("The change could not be saved.");
        ToolOrderController.ReorderRequest request = new ToolOrderController.ReorderRequest();
        request.setPageIds(Collections.singletonList("page1"));
        doThrow(new IllegalArgumentException("bad request")).when(pageEditHandler).reorderPages(any());

        ResponseEntity<Map<String, Object>> response = controller.reorder(request, Locale.ENGLISH);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("The change could not be saved.", response.getBody().get("message"));
    }

    @Test
    public void unexpectedErrorsReturnInternalServerError() {
        when(messageSource.getMessage(eq("internal_error"), any(), eq(Locale.ENGLISH)))
                .thenReturn("The change could not be saved because of a server error.");
        ToolOrderController.ReorderRequest request = new ToolOrderController.ReorderRequest();
        request.setPageIds(Collections.singletonList("page1"));
        doThrow(new RuntimeException("database unavailable")).when(pageEditHandler).reorderPages(any());

        ResponseEntity<Map<String, Object>> response = controller.reorder(request, Locale.ENGLISH);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("The change could not be saved because of a server error.", response.getBody().get("message"));
    }
}
