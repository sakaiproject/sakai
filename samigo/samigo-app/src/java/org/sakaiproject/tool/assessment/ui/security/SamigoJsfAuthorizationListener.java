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
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletResponse;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.cover.ToolManager;

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
            AuthorizationBean authorization = (AuthorizationBean) ContextUtil.lookupBean("authorization");
            authorization.addAllPrivilege(ToolManager.getCurrentPlacement().getContext());
            allowed = SamigoJsfViewAccess.isAllowed(viewId, authorization::getPrivilege);
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
