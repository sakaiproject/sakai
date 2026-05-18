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

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.vm.ActionURL;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.View;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ToolOrderVelocityView implements View {

    private static final String CONTENT_TYPE = "text/html;charset=UTF-8";
    private static final String SITE_INFO_ACTION_BASE = "siteInfoActionBase";

    private final VelocityEngine velocityEngine;
    private final LocaleResolver localeResolver;
    private final String templateName;

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(CONTENT_TYPE);

        VelocityContext context = new VelocityContext();
        model.forEach(context::put);
        context.put("contextPath", request.getContextPath());
        context.put("formattedText", ComponentManager.get(FormattedText.class));
        context.put("sakai_Validator", ComponentManager.get(FormattedText.class));
        context.put("sakai_csrf_token", getCsrfToken());
        context.put("sakai_ActionURL", new ActionURL((String) model.get(SITE_INFO_ACTION_BASE), request).setPanel("Main"));

        Object modelLocale = model.get("locale");
        Locale locale = modelLocale instanceof Locale ? (Locale) modelLocale : localeResolver.resolveLocale(request);
        ResourceLoader velocityMessages = new ResourceLoader("velocity-tool");
        velocityMessages.setContextLocale(locale);
        context.put("toolOptions", velocityMessages.getString("toolOptions"));

        Template template = velocityEngine.getTemplate(templateName, StandardCharsets.UTF_8.name());
        template.merge(context, response.getWriter());
    }

    private Object getCsrfToken() {
        SessionManager sessionManager = ComponentManager.get(SessionManager.class);
        if (sessionManager == null || sessionManager.getCurrentSession() == null) {
            return "";
        }

        Session session = sessionManager.getCurrentSession();
        Object csrfToken = session.getAttribute(UsageSessionService.SAKAI_CSRF_SESSION_ATTRIBUTE);
        return csrfToken == null ? "" : csrfToken;
    }
}
