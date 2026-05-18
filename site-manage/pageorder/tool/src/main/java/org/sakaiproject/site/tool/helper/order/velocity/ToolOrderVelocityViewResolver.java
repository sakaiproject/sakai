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
package org.sakaiproject.site.tool.helper.order.velocity;

import java.util.Locale;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

public class ToolOrderVelocityViewResolver implements ViewResolver {

    private static final String PREFIX = "/WEB-INF/vm/";
    private static final String SUFFIX = ".vm";

    private final VelocityEngine velocityEngine;
    private final LocaleResolver localeResolver;

    public ToolOrderVelocityViewResolver(ServletContext servletContext, LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
        velocityEngine = new VelocityEngine();
        velocityEngine.setApplicationAttribute(ServletContext.class.getName(), servletContext);
        try {
            velocityEngine.init(velocityProperties());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to initialize Tool Order Velocity", e);
        }
    }

    @Override
    public View resolveViewName(String viewName, Locale locale) {
        return new ToolOrderVelocityView(velocityEngine, localeResolver, PREFIX + viewName + SUFFIX);
    }

    private Properties velocityProperties() {
        Properties properties = new Properties();
        properties.setProperty(RuntimeConstants.RESOURCE_LOADER, "webapp, classpath");
        properties.setProperty("webapp.resource.loader.class", "org.apache.velocity.tools.view.servlet.WebappLoader");
        properties.setProperty("webapp.resource.loader.path", "/");
        properties.setProperty("webapp.resource.loader.cachingOn", "true");
        properties.setProperty("classpath.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        properties.setProperty(RuntimeConstants.VM_LIBRARY, "VM_chef_library.vm");
        properties.setProperty(RuntimeConstants.INPUT_ENCODING, "UTF-8");
        properties.setProperty(RuntimeConstants.OUTPUT_ENCODING, "UTF-8");
        properties.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                "org.sakaiproject.velocity.util.SLF4JLogChute");
        return properties;
    }
}
