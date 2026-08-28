/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.vm;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Properties;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * Replacement for the deprecated VelocityViewServlet.
 */
public class VelocityServlet extends HttpServlet {

    private VelocityEngine velocityEngine;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        try {
            velocityEngine = new VelocityEngine();

            Properties props = loadConfiguration(config);

            for (String key : props.stringPropertyNames()) {
                velocityEngine.setProperty(key, props.getProperty(key));
            }

            velocityEngine.init();

        } catch (Exception e) {
            throw new ServletException("Unable to initialize VelocityEngine", e);
        }
    }

    /**
     * Compatible with the previous implementation.
     */
    protected Properties loadConfiguration(ServletConfig config) throws IOException {

        Properties props = new Properties();

        String configPath = config.getInitParameter("properties");

        if (configPath != null && !configPath.isBlank()) {

            if (!configPath.startsWith("/")) {
                configPath = "/" + configPath;
            }

            try (InputStream in = getServletContext().getResourceAsStream(configPath)) {
                if (in != null) {
                    props.load(in);
                }
            }
        }

        String root = config.getServletContext().getRealPath("/");

        if (root == null) {
            root = "/";
        }

        props.put(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, root);

        if (props.containsKey("runtime.log")) {
            props.put("runtime.log", root + props.getProperty("runtime.log"));
        }

        props.putIfAbsent(RuntimeConstants.RESOURCE_LOADER, "file");
        props.putIfAbsent("resource.loader.file.class",
                "org.apache.velocity.runtime.resource.loader.FileResourceLoader");

        return props;
    }

    @Override
    protected void service(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        VelocityContext context = new VelocityContext();

        Enumeration<String> names = request.getAttributeNames();

        while (names.hasMoreElements()) {

            String name = names.nextElement();

            context.put(escapeVmName(name), request.getAttribute(name));
        }

        Template template = handleRequest(request, response, context);

        if (template == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("text/html;charset=UTF-8");

        try (Writer writer = response.getWriter()) {
            template.merge(context, writer);
        }
    }

    protected Template handleRequest(HttpServletRequest request,
                                     HttpServletResponse response,
                                     VelocityContext context) {

        String templatePath =
                (String) request.getAttribute("jakarta.servlet.include.servlet_path");

        if (templatePath == null) {
            templatePath = (String) request.getAttribute("jakarta.servlet.include.servlet_path");
        }

        if (templatePath == null) {
            templatePath = (String) request.getAttribute("sakai.vm.path");
        }

        if (templatePath == null) {
            templatePath = request.getServletPath();
        }

        try {
            return velocityEngine.getTemplate(templatePath);
        } catch (ParseErrorException | ResourceNotFoundException e) {
            log("Unable to load template " + templatePath, e);
        } catch (Exception e) {
            log("Unexpected error loading template " + templatePath, e);
        }

        return null;
    }

    protected String escapeVmName(String name) {

        char[] chars = name.toCharArray();

        if (!Character.isLetter(chars[0])) {
            chars[0] = 'X';
        }

        for (int i = 1; i < chars.length; i++) {

            char c = chars[i];

            if (!(Character.isLetterOrDigit(c)
                    || c == '_'
                    || c == '-')) {

                chars[i] = '_';
            }
        }

        return new String(chars);
    }
}