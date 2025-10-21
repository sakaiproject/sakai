/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2025 The Apereo Foundation
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

package org.sakaiproject.tool.assessment.ui.bean.index;

import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener;
import org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.util.Web;

/**
 * Routes the legacy Tests &amp; Quizzes landing page to the author or select view.
 */
@ManagedBean(name = "mainIndexRouter")
@RequestScoped
@Slf4j
public class MainIndexRouter {

    public void prepare(ComponentSystemEvent event) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null || context.getResponseComplete()) {
            return;
        }

        AuthorizationBean authorization =
                (AuthorizationBean) ContextUtil.lookupBean("authorization");

        if (authorization != null && authorization.getAdminPrivilege()) {
            triggerAuthorFlow();
            redirect(context, "/jsf/author/authorIndex_container.xhtml");
        } else {
            triggerSelectFlow();
            redirect(context, "/jsf/select/selectIndex_container.xhtml");
        }
    }

    private void triggerAuthorFlow() {
        try {
            new AuthorActionListener().processAction(null);
        } catch (Exception e) {
            log.warn("Unable to initialise author flow", e);
        }
    }

    private void triggerSelectFlow() {
        try {
            new SelectActionListener().processAction(null);
        } catch (Exception e) {
            log.warn("Unable to initialise select flow", e);
        }
    }

    private void redirect(FacesContext context, String targetPath) {
        ExternalContext externalContext = context.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        Object nativeUrl = request.getAttribute(Tool.NATIVE_URL);
        request.removeAttribute(Tool.NATIVE_URL);

        String portalTarget;
        try {
            portalTarget = Web.returnUrl(request, targetPath);
        } finally {
            if (nativeUrl != null) {
                request.setAttribute(Tool.NATIVE_URL, nativeUrl);
            }
        }
        try {
            externalContext.redirect(portalTarget);
        } catch (IOException e) {
            throw new FacesException(e);
        } finally {
            context.responseComplete();
        }
    }
}
