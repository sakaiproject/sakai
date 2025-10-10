/**********************************************************************************
 * Copyright (c) 2025 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.poll.tool.config;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.util.SakaiContextLoaderListener;
import org.sakaiproject.util.ToolListener;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Bootstraps the Polls tool without relying on the legacy RSF servlet.
 */
public class PollsWebApplicationInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        servletContext.setInitParameter("resourceurlbase", "/polls-tool/");

        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.setServletContext(servletContext);
        rootContext.register(PollsWebMvcConfig.class, PollsToolConfig.class);

        servletContext.addListener(new ToolListener());
        servletContext.addListener(new SakaiContextLoaderListener(rootContext));

        FilterRegistration requestFilterRegistration = servletContext.addFilter("sakai.request", RequestFilter.class);
        requestFilterRegistration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE), true, "/faces/*");
        requestFilterRegistration.setInitParameter(RequestFilter.CONFIG_UPLOAD_ENABLED, "true");

        Dynamic servlet = servletContext.addServlet("sakai.poll", new DispatcherServlet(rootContext));
        servlet.addMapping("/faces/*");
        servlet.addMapping("/");
        servlet.setLoadOnStartup(1);
    }
}
