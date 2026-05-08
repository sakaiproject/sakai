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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

@ControllerAdvice(basePackages = "org.sakaiproject.poll.tool.mvc")
@RequiredArgsConstructor
public class PollsLocaleControllerAdvice {

    private final PollsLocaleService pollsLocaleService;

    @ModelAttribute("locale")
    public Locale locale(HttpServletRequest request, HttpServletResponse response) {
        Locale locale = pollsLocaleService.getLocaleForCurrentSiteAndUser();
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        if (localeResolver != null) {
            localeResolver.setLocale(request, response, locale);
        }
        return locale;
    }
}
