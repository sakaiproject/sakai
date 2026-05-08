/*
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
package org.sakaiproject.poll.tool.mvc;

import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PollsLocaleControllerAdviceTest {

    @Test
    public void setsSakaiResolvedLocaleIntoSessionLocaleResolver() {
        PollsLocaleService pollsLocaleService = mock(PollsLocaleService.class);
        when(pollsLocaleService.getLocaleForCurrentSiteAndUser()).thenReturn(Locale.FRANCE);

        PollsLocaleControllerAdvice advice = new PollsLocaleControllerAdvice(pollsLocaleService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addPreferredLocale(Locale.GERMANY);

        LocaleResolver localeResolver = new SessionLocaleResolver();
        request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, localeResolver);

        Locale returned = advice.locale(request, mock(HttpServletResponse.class));

        Assert.assertEquals(Locale.FRANCE, returned);
        Assert.assertEquals(Locale.FRANCE, localeResolver.resolveLocale(request));
    }
}
