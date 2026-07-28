/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.tool.mvc;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.util.api.LocaleService;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

public class SiteStatsControllerAdviceTest {

    private SiteStatsControllerAdvice advice;
    private MessageSource messageSource;
    private MockHttpServletRequest request;

    @Before
    public void setUp() {
        LocaleService localeService = mock(LocaleService.class);
        messageSource = mock(MessageSource.class);
        when(localeService.getLocaleForCurrentSiteAndUser()).thenReturn(Locale.ENGLISH);
        advice = new SiteStatsControllerAdvice(
                localeService, messageSource, mock(SakaiCsrfTokens.class),
                mock(ServerConfigurationService.class), mock(UserTimeService.class));
        request = new MockHttpServletRequest();
        request.setAttribute("sakai.html.head", "<link>");
    }

    @Test
    public void csrfFailureReturnsAVisibleLocalizedForbiddenPage() {
        when(messageSource.getMessage(
                eq("sitestats_error_request_title"), isNull(), anyString(), eq(Locale.ENGLISH)))
                .thenReturn("Request verification failed");
        when(messageSource.getMessage(eq("java.badcsrftoken"), isNull(), anyString(), eq(Locale.ENGLISH)))
                .thenReturn("Refresh the tool and try again.");

        ModelAndView response = advice.invalidCsrfToken(request);

        assertEquals("error", response.getViewName());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatus());
        assertEquals("Request verification failed", response.getModel().get("errorTitle"));
        assertEquals("Refresh the tool and try again.", response.getModel().get("error"));
        assertEquals("<link>", response.getModel().get("sakaiHtmlHead"));
    }

    @Test
    public void operationFailureReturnsAVisibleLocalizedServerError() {
        when(messageSource.getMessage(
                eq("sitestats_error_operation_title"), isNull(), anyString(), eq(Locale.ENGLISH)))
                .thenReturn("Unable to complete the operation");
        when(messageSource.getMessage(
                eq("sitestats_error_report_save"), isNull(), anyString(), eq(Locale.ENGLISH)))
                .thenReturn("The report could not be saved. Try again.");
        SiteStatsOperationException exception = new SiteStatsOperationException(
                "sitestats_error_report_save", "Report 42 could not be saved for site example");

        ModelAndView response = advice.operationFailure(request, exception);

        assertEquals("error", response.getViewName());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("Unable to complete the operation", response.getModel().get("errorTitle"));
        assertEquals("The report could not be saved. Try again.", response.getModel().get("error"));
    }

    @Test
    public void invalidReportConfigurationReturnsAVisibleLocalizedBadRequest() {
        when(messageSource.getMessage(
                eq("sitestats_error_report_configuration_title"), isNull(), anyString(), eq(Locale.ENGLISH)))
                .thenReturn("Invalid report configuration");
        when(messageSource.getMessage(
                eq("sitestats_report_configuration_invalid"), isNull(), anyString(), eq(Locale.ENGLISH)))
                .thenReturn("The selected report configuration is not valid.");
        InvalidReportConfigurationException exception = new InvalidReportConfigurationException(
                "sitestats_report_configuration_invalid");

        ModelAndView response = advice.invalidReportConfiguration(request, exception);

        assertEquals("error", response.getViewName());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals("Invalid report configuration", response.getModel().get("errorTitle"));
        assertEquals("The selected report configuration is not valid.", response.getModel().get("error"));
    }
}
