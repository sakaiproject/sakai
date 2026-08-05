/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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
package org.sakaiproject.lessonbuildertool.resolver;

import jakarta.servlet.ServletContext;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
public class MultiCommonsMultipartResolver implements MultipartResolver {

    private final StandardServletMultipartResolver delegate = new StandardServletMultipartResolver();

    public MultiCommonsMultipartResolver() {
    }

    public MultiCommonsMultipartResolver(ServletContext servletContext) {
    }

    @Override
    public boolean isMultipart(HttpServletRequest request) {
        return delegate.isMultipart(request);
    }

    @Override
    public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
        return delegate.resolveMultipart(request);
    }

    @Override
    public void cleanupMultipart(MultipartHttpServletRequest request) {
        delegate.cleanupMultipart(request);
    }
}
