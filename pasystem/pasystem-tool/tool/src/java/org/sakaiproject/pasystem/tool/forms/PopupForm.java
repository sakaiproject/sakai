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

package org.sakaiproject.pasystem.tool.forms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.disk.DiskFileItem;

import org.sakaiproject.pasystem.api.Errors;
import org.sakaiproject.pasystem.api.PASystem;
import org.sakaiproject.pasystem.api.MissingUuidException;
import org.sakaiproject.pasystem.api.Popup;
import org.sakaiproject.pasystem.api.TemplateStream;
import org.sakaiproject.pasystem.tool.handlers.CrudHandler;

/**
 * Maps to and from the popup HTML form and a popup data object.
 */
@Data
@Slf4j
public class PopupForm extends BaseForm {

    private final String descriptor;
    private final boolean isOpenCampaign;
    private final List<String> assignToEids;
    private final Optional<DiskFileItem> templateItem;

    private PopupForm(String uuid,
                      String descriptor,
                      long startTime, long endTime,
                      boolean isOpenCampaign,
                      List<String> assignToEids,
                      Optional<DiskFileItem> templateItem) {
        this.uuid = uuid;
        this.descriptor = descriptor;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isOpenCampaign = isOpenCampaign;
        this.assignToEids = assignToEids;
        this.templateItem = templateItem;
    }

    public static PopupForm fromPopup(Popup existingPopup, PASystem paSystem) {
        try {
            String uuid = existingPopup.getUuid();
            List<String> assignToEids = paSystem.getPopups().getAssigneeEids(uuid);

            return new PopupForm(uuid, existingPopup.getDescriptor(),
                    existingPopup.getStartTime(),
                    existingPopup.getEndTime(),
                    existingPopup.isOpenCampaign(),
                    assignToEids,
                    Optional.empty());
        } catch (MissingUuidException e) {
            throw new RuntimeException(e);
        }
    }

    public static PopupForm fromRequest(String uuid, HttpServletRequest request) {
        String descriptor = request.getParameter("descriptor");
        boolean isOpenCampaign = "open-campaign".equals(request.getParameter("open-campaign"));

        long startTime = "".equals(request.getParameter("start_time")) ? 0 : parseTime(request.getParameter("start_time_selected_datetime"));
        long endTime = "".equals(request.getParameter("end_time")) ? 0 : parseTime(request.getParameter("end_time_selected_datetime"));

        List<String> assignToEids = new ArrayList<String>();
        if (request.getParameter("distribution") != null) {
            for (String user : request.getParameter("distribution").split("[\r\n]+")) {
                if (!user.isEmpty()) {
                    assignToEids.add(user);
                }
            }
        }

        Optional<DiskFileItem> templateItem = Optional.empty();
        DiskFileItem dfi = (DiskFileItem) request.getAttribute("template");
        if (dfi != null && dfi.getSize() > 0) {
            templateItem = Optional.of(dfi);
        }

        return new PopupForm(uuid, descriptor, startTime, endTime, isOpenCampaign, assignToEids, templateItem);
    }

    public Errors validate(CrudHandler.CrudMode mode) {
        Errors errors = new Errors();

        if (!hasValidStartTime()) {
            errors.addError("start_time", "invalid_time");
        }

        if (!hasValidEndTime()) {
            errors.addError("end_time", "invalid_time");
        }

        if (CrudHandler.CrudMode.CREATE.equals(mode) && !templateItem.isPresent()) {
            errors.addError("template", "template_was_missing");
        }

        Errors modelErrors = toPopup().validate();

        return errors.merge(modelErrors);
    }

    /**
     * If a template has been provided, return a stream for it.  On update, this can be empty.
     */
    public Optional<TemplateStream> getTemplateStream() {
        try {
            if (templateItem.isPresent() && templateItem.get().getSize() > 0) {
                return Optional.of(new TemplateStream(templateItem.get().getInputStream(),
                        templateItem.get().getSize()));
            }
        } catch (IOException e) {
            log.error("IOException while fetching template stream", e);
        }

        return Optional.empty();
    }

    public Popup toPopup() {
        return Popup.create(uuid, descriptor, startTime, endTime, isOpenCampaign);
    }
}
