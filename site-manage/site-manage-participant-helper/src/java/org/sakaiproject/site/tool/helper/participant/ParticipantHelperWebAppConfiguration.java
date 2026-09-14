/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package org.sakaiproject.site.tool.helper.participant;

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

/** Bootstraps the Add Participants helper as a Spring MVC application. */
public class ParticipantHelperWebAppConfiguration implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.setServletContext(servletContext);
        rootContext.register(ParticipantHelperWebMvcConfiguration.class);

        servletContext.addListener(new ToolListener());
        servletContext.addListener(new SakaiContextLoaderListener(rootContext));

        FilterRegistration requestFilter = servletContext.addFilter("sakai.request", RequestFilter.class);
        requestFilter.addMappingForUrlPatterns(
                EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE), true, "/*");

        Dynamic servlet = servletContext.addServlet("sakai-site-manage-participant-helper", new DispatcherServlet(rootContext));
        servlet.addMapping("/");
        servlet.setLoadOnStartup(1);
    }
}
