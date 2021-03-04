/**
 * Copyright (c) 2010 onwards - The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.meetings.tool;

import java.io.BufferedWriter;
import java.io.Writer;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.annotation.Resource;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import org.sakaiproject.meetings.api.MeetingsService;
import org.sakaiproject.meetings.api.SakaiProxy;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.velocity.util.SLF4JLogChute;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import lombok.extern.slf4j.Slf4j;

/**
 * Bootstraps the meetings tool by rendering a Velocity template with the apps JS
 * variables prebuilt.
 *
 * @author Adrian Fish
 */
@Slf4j
public class MeetingsTool extends HttpServlet {

    private static final long serialVersionUID = 2801227086525605150L;

    @Autowired
    private SakaiProxy sakaiProxy;

    @Autowired
    private ServerConfigurationService serverConfigurationService;

    private Template bootstrapTemplate = null;

    public void init(ServletConfig config) throws ServletException {

        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this,config.getServletContext());
        log.debug("init");
        try {
            VelocityEngine vengine = new VelocityEngine();
            vengine.setApplicationAttribute(ServletContext.class.getName(), config.getServletContext());
            vengine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, new SLF4JLogChute());
            vengine.addProperty("file.resource.loader.path", config.getServletContext().getRealPath("/WEB-INF"));
            vengine.init();
            bootstrapTemplate = vengine.getTemplate("bootstrap.vm");
        } catch (Throwable t) {
            throw new ServletException("Failed to initialise MeetingsTool servlet.", t);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        log.debug("doGet()");
        // check if Sakai proxy was successfully initialized
        if (sakaiProxy == null)
            throw new ServletException("sakaiProxy MUST be initialized.");

        // check if user is logged in
        if (sakaiProxy.getCurrentUser() == null)
            throw new ServletException("You must be logged in to use this tool.");

        // check site permissions
        sakaiProxy.checkPermissions();

        // parameters
        String state = request.getParameter("state");
        String meetingId = request.getParameter("meetingId");
        if (state == null)
            state = "currentMeetings";

        String sakaiHtmlHead = (String) request.getAttribute("sakai.html.head");

        String language = sakaiProxy.getUserLanguageCode();

        VelocityContext ctx = new VelocityContext();

        // This is needed so certain trimpath variables don't get parsed.
        ctx.put("D", "$");

        ctx.put("sakaiHtmlHead", sakaiHtmlHead);
        ctx.put("isoLanguage", language);
        ctx.put("language", language);
        ctx.put("skin", sakaiProxy.getSakaiSkin());
        ctx.put("siteId", sakaiProxy.getCurrentSiteId());
        ctx.put("state", state);
        ctx.put("timezoneOffset", sakaiProxy.getUserTimezoneOffset());
        ctx.put("sakaiVersion", sakaiProxy.getSakaiVersion());
        ctx.put("maxFileSizeInBytes", sakaiProxy.getFileSizeMax());
        ctx.put("checkICalOption", serverConfigurationService.getBoolean(MeetingsService.CFG_CHECKICALOPTION, true));

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html");
        Writer writer = new BufferedWriter(response.getWriter());
        bootstrapTemplate.merge(ctx,writer);
        writer.close();
    }
}
