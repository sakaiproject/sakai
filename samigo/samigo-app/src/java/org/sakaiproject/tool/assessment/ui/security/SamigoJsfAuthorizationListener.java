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
package org.sakaiproject.tool.assessment.ui.security;

import java.io.IOException;
import java.util.Set;
import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.cover.SessionManager;

/** Enforces view permissions before postback actions and after JSF navigation. */
public class SamigoJsfAuthorizationListener implements PhaseListener {
    private static final long serialVersionUID = 1L;

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    @Override
    public void afterPhase(PhaseEvent event) {
        if (PhaseId.RESTORE_VIEW.equals(event.getPhaseId())) {
            authorize(event.getFacesContext());
        }
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        if (PhaseId.RENDER_RESPONSE.equals(event.getPhaseId())) {
            authorize(event.getFacesContext());
        }
    }

    private void authorize(FacesContext context) {
        if (context.getResponseComplete() || context.getViewRoot() == null) {
            return;
        }
        String viewId = context.getViewRoot().getViewId();
        // Direct assessment URLs can serve anonymous users without a tool placement.
        if (SamigoJsfViewAccess.isAllowed(viewId, permission -> false)) {
            return;
        }
        boolean allowed = false;
        if (ToolManager.getCurrentPlacement() != null) {
            String siteId = ToolManager.getCurrentPlacement().getContext();
            Set<String> permissions = SamigoJsfPermissions.get((ServletRequest) context.getExternalContext().getRequest(),
                SessionManager.getCurrentSessionUserId(), siteId, () -> {
                    AuthorizationBean authorization = (AuthorizationBean) ContextUtil.lookupBean("authorization");
                    authorization.addAllPrivilege(siteId);
                    return authorization.getAuthzMap();
                });
            allowed = SamigoJsfViewAccess.isAllowed(viewId, permissions::contains);
        }
        if (!allowed) {
            try {
                context.getExternalContext().responseSendError(HttpServletResponse.SC_FORBIDDEN, null);
                context.responseComplete();
            } catch (IOException e) {
                throw new FacesException(e);
            }
        }
    }
}
