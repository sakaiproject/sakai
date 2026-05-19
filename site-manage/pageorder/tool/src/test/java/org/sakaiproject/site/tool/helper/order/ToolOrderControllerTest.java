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
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.site.tool.helper.order.ToolOrderController.AccessRequest;
import org.sakaiproject.site.tool.helper.order.ToolOrderController.ReorderRequest;
import org.sakaiproject.site.tool.helper.order.impl.SitePageEditHandler;
import org.sakaiproject.util.api.LocaleService;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ToolOrderControllerTest {

    private static final Locale LOCALE = Locale.ENGLISH;

    private SitePageEditHandler pageEditHandler;
    private ToolOrderController controller;

    @Before
    public void setUp() {
        pageEditHandler = mock(SitePageEditHandler.class);
        LocaleService localeService = mock(LocaleService.class);
        MessageSource messageSource = mock(MessageSource.class);
        controller = new ToolOrderController(pageEditHandler, localeService, messageSource);

        when(localeService.getLocaleForCurrentSiteAndUser()).thenReturn(LOCALE);
        when(messageSource.getMessage(eq("access_error"), any(Object[].class), eq(LOCALE)))
                .thenReturn("access denied");
        when(messageSource.getMessage(eq("validation_error"), any(Object[].class), eq(LOCALE)))
                .thenReturn("validation failed");
        when(messageSource.getMessage(eq("status_error"), any(Object[].class), eq(LOCALE)))
                .thenReturn("server failed");
    }

    @Test
    public void accessMapsSecurityExceptionToForbidden() {
        AccessRequest request = new AccessRequest();
        request.setEnabled(Boolean.TRUE);
        when(pageEditHandler.setPageEnabled("page1", true)).thenThrow(new SecurityException("denied"));

        ResponseEntity<Map<String, Object>> response = controller.access("page1", request);

        assertErrorResponse(response, HttpStatus.FORBIDDEN, "access denied");
    }

    @Test
    public void reorderMapsIllegalArgumentExceptionToBadRequest() {
        ReorderRequest request = new ReorderRequest();
        request.setPageIds(Collections.singletonList("page1"));
        doThrow(new IllegalArgumentException("invalid order")).when(pageEditHandler).reorderPages(request.getPageIds());

        ResponseEntity<Map<String, Object>> response = controller.reorder(request);

        assertErrorResponse(response, HttpStatus.BAD_REQUEST, "validation failed");
    }

    @Test
    public void accessMapsIllegalStateExceptionToInternalServerError() {
        AccessRequest request = new AccessRequest();
        request.setEnabled(Boolean.TRUE);
        when(pageEditHandler.setPageEnabled("page1", true)).thenThrow(new IllegalStateException("save failed"));

        ResponseEntity<Map<String, Object>> response = controller.access("page1", request);

        assertErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "server failed");
    }

    @Test
    public void resetOrderMapsUnexpectedRuntimeExceptionToInternalServerError() {
        doThrow(new RuntimeException("unexpected")).when(pageEditHandler).resetOrder();

        ResponseEntity<Map<String, Object>> response = controller.resetOrder();

        assertErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "server failed");
    }

    private void assertErrorResponse(ResponseEntity<Map<String, Object>> response, HttpStatus status, String message) {
        assertEquals(status, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(Boolean.FALSE, body.get("success"));
        assertEquals(message, body.get("message"));
    }
}
