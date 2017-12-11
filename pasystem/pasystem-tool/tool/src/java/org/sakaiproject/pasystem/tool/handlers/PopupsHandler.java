/**********************************************************************************
 *
 * Copyright (c) 2015 The Sakai Foundation
 *
 * Original developers:
 *
 *   New York University
 *   Payten Giles
 *   Mark Triggs
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.pasystem.tool.handlers;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.pasystem.api.PASystem;
import org.sakaiproject.pasystem.api.Popup;
import org.sakaiproject.pasystem.api.TemplateStream;
import org.sakaiproject.pasystem.tool.forms.PopupForm;

/**
 * A handler for creating and updating popups in the PA System administration tool.
 */
@Slf4j
public class PopupsHandler extends CrudHandler {

    private final PASystem paSystem;

    public PopupsHandler(PASystem pasystem) {
        this.paSystem = pasystem;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Map<String, Object> context) {
        if (request.getPathInfo().contains("/preview") && isGet(request)) {
            handlePreview(request, response, context);
        } else {
            super.handle(request, response, context);
        }
    }

    @Override
    protected void handleEdit(HttpServletRequest request, Map<String, Object> context) {
        String uuid = extractId(request);
        Optional<Popup> popup = paSystem.getPopups().getForId(uuid);

        if (popup.isPresent()) {
            showEditForm(PopupForm.fromPopup(popup.get(), paSystem), context, CrudMode.UPDATE);
        } else {
            flash("danger", "No popup found for UUID: " + uuid);
            sendRedirect("");
        }
    }

    private void handlePreview(HttpServletRequest request, HttpServletResponse response, Map<String, Object> context) {
        String uuid = extractId(request);

        context.put("layout", false);
        try {
            String content = paSystem.getPopups().getPopupContent(uuid);

            if (content.isEmpty()) {
                // Don't let the portal buffering hijack our response.
                // Include enough content to count as having returned a
                // body.
                content = "     ";
            }

            response.getWriter().write(content);
        } catch (IOException e) {
            log.warn("Write failed while previewing popup", e);
        }
    }

    private void showEditForm(PopupForm popupForm, Map<String, Object> context, CrudMode mode) {
        context.put("subpage", "popup_form");

        if (CrudMode.UPDATE.equals(mode)) {
            context.put("mode", "edit");
        } else {
            context.put("mode", "new");
        }

        context.put("popup", popupForm);
    }

    @Override
    protected void handleCreateOrUpdate(HttpServletRequest request, Map<String, Object> context, CrudMode mode) {
        String uuid = extractId(request);

        PopupForm popupForm = PopupForm.fromRequest(uuid, request);
        addErrors(popupForm.validate(mode));

        if (hasErrors()) {
            showEditForm(popupForm, context, mode);
            return;
        }

        Optional<TemplateStream> templateStream = popupForm.getTemplateStream();

        if (CrudMode.CREATE.equals(mode)) {
            paSystem.getPopups().createCampaign(popupForm.toPopup(),
                    templateStream.get(),
                    Optional.of(popupForm.getAssignToEids()));
            flash("info", "popup_created");
        } else {
            paSystem.getPopups().updateCampaign(popupForm.toPopup(),
                    templateStream,
                    popupForm.isOpenCampaign() ? Optional.empty() : Optional.of(popupForm.getAssignToEids()));
            flash("info", "popup_updated");
        }

        sendRedirect("");
    }

    @Override
    protected void showNewForm(Map<String, Object> context) {
        context.put("subpage", "popup_form");
        context.put("mode", "new");
        context.put("templateRequired", true);
    }

    @Override
    protected void handleDelete(HttpServletRequest request) {
        String uuid = extractId(request);
        paSystem.getPopups().deleteCampaign(uuid);

        flash("info", "popup_deleted");
        sendRedirect("");
    }
}
