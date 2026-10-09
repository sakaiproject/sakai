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
package org.sakaiproject.tool.assessment.ui.facelets;

import java.io.IOException;
import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.servlet.http.HttpServletRequest;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener;
import org.sakaiproject.tool.assessment.ui.listener.author.ConfirmEditPublishedAssessmentListener;
import org.sakaiproject.tool.assessment.ui.listener.author.EditPublishedSettingsListener;
import org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.security.SamigoJsfPermissions;

/** Runs the former JSP entry-point preparation before the view is rendered. */
public class SamigoViewPhaseListener implements PhaseListener {
    private static final long serialVersionUID = 1L;

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }

    @Override
    public void afterPhase(PhaseEvent event) {
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        FacesContext context = event.getFacesContext();
        if (context.getViewRoot() == null || context.getResponseComplete()) {
            return;
        }
        String view = context.getViewRoot().getViewId();
        if (!view.endsWith(".xhtml")) {
            return;
        }
        switch (view) {
            case "/jsf/index/mainIndex.xhtml":
                AuthorizationBean authorization = (AuthorizationBean) ContextUtil.lookupBean("authorization");
                prepareIndex(authorization.getAdminPrivilege());
                break;
            case "/jsf/review/reviewIndex.xhtml":
                new SelectActionListener().processAction(null);
                break;
            case "/jsf/security/roleCheckStaticInclude.xhtml":
                prepareRole(context);
                break;
            case "/jsf/author/editLink.xhtml":
                prepareEditLink(context);
                break;
            case "/jsf/qti/xmlDisplay.xhtml":
                prepareExport(context, "exportAssessment.xml");
                break;
            case "/jsf/qti/xmlPoolDisplay.xhtml":
                prepareExport(context, "exportPool.xml");
                break;
            default:
                break;
        }
    }

    private void prepareIndex(boolean adminPrivilege) {
        if (adminPrivilege) {
            new AuthorActionListener().processAction(null);
        } else {
            new SelectActionListener().processAction(null);
        }
    }

    private void prepareRole(FacesContext context) {
        AuthorizationBean authorization = (AuthorizationBean) ContextUtil.lookupBean("authorization");
        String siteId = ToolManager.getCurrentPlacement().getContext();
        boolean adminPrivilege = SamigoJsfPermissions.get(
            (HttpServletRequest) context.getExternalContext().getRequest(),
            SessionManager.getCurrentSessionUserId(), siteId, () -> {
                authorization.addAllPrivilege(siteId);
                return authorization.getAuthzMap();
            }).contains("admin.privilege");
        prepareIndex(adminPrivilege);
        context.getApplication().getNavigationHandler().handleNavigation(
            context, null, adminPrivilege ? "author" : "select");
    }

    private void prepareExport(FacesContext context, String filename) {
        ExternalContext external = context.getExternalContext();
        external.setResponseHeader("Cache-Control", "private");
        external.setResponseHeader("Pragma", "cache");
        external.setResponseHeader("Content-Disposition", "attachment; filename=" + filename);
    }

    private void prepareEditLink(FacesContext context) {
        AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
        author.setIsEditPendingAssessmentFlow(false);
        AssessmentBean assessment = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
        assessment.setAssessmentId(ContextUtil.lookupParam("publishedAssessmentId"));
        String siteId = ToolManager.getCurrentPlacement().getContext();
        try {
            Site site = SiteService.getSite(siteId);
            ToolConfiguration tool = site.getToolForCommonId("sakai.samigo");
            AuthorizationBean authorization = (AuthorizationBean) ContextUtil.lookupBean("authorization");
            if (authorization.getAuthzMap().isEmpty()) {
                authorization.addAllPrivilege(site.getId());
            }
            new AuthorActionListener().processAction(null);
            String target;
            if (ContextUtil.lookupParam("settings") != null) {
                new EditPublishedSettingsListener().processAction(null);
                target = "publishedSettings";
            } else {
                new ConfirmEditPublishedAssessmentListener().processAction(null);
                target = "confirmEditPublishedAssessment";
            }
            context.getExternalContext().redirect("/portal/tool/" + tool.getId() + "/jsf/author/" + target);
        } catch (org.sakaiproject.exception.IdUnusedException | IOException exception) {
            throw new FacesException("Unable to open the assessment edit view for site " + siteId, exception);
        }
    }
}
