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
package org.sakaiproject.entitybroker.config;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.sakaiproject.util.SakaiContextLoaderListener;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Programmatic replacement for web.xml.
 * Registers the Spring DispatcherServlet and all filters (OAuth pre/post,
 * Sakai request) that were previously declared in web.xml.
 */
public class DirectServletInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        servletContext.addListener(new SakaiContextLoaderListener(context));
        context.register(DirectWebMvcConfiguration.class);

        DispatcherServlet dispatcherServlet = new DispatcherServlet(context);
        ServletRegistration.Dynamic servlet =
                servletContext.addServlet("sakai.entitybroker.direct", dispatcherServlet);
        servlet.setLoadOnStartup(1);
        servlet.addMapping("/*");

        // OAuth pre-filter
        FilterRegistration.Dynamic oauthPre =
                servletContext.addFilter("oauth.pre", "org.sakaiproject.oauth.filter.OAuthPreFilter");
        oauthPre.addMappingForServletNames(
                EnumSet.of(DispatcherType.REQUEST), true, "sakai.entitybroker.direct");

        // Sakai request filter with basic auth enabled
        FilterRegistration.Dynamic sakaiRequest =
                servletContext.addFilter("sakai.request", "org.sakaiproject.util.RequestFilter");
        sakaiRequest.setInitParameter("sakai.session.auth", "basic");
        sakaiRequest.addMappingForServletNames(
                EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE),
                true, "sakai.entitybroker.direct");

        // OAuth post-filter
        FilterRegistration.Dynamic oauthPost =
                servletContext.addFilter("oauth.post", "org.sakaiproject.oauth.filter.OAuthPostFilter");
        oauthPost.addMappingForServletNames(
                EnumSet.of(DispatcherType.REQUEST), true, "sakai.entitybroker.direct");
    }
}
