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

import javax.servlet.http.HttpServletRequest;
import lombok.Data;
import org.sakaiproject.pasystem.api.Banner;
import org.sakaiproject.pasystem.api.Errors;
import org.sakaiproject.pasystem.api.MissingUuidException;

/**
 * Maps to and from the banner HTML form and a banner data object.
 */
@Data
public class BannerForm extends BaseForm {

    private final String message;
    private final String hosts;
    private final String type;
    private final boolean active;

    private BannerForm(String uuid, String message, String hosts, long startTime, long endTime, boolean active, String type) {
        this.uuid = uuid;
        this.message = message;
        this.hosts = hosts;
        this.startTime = startTime;
        this.endTime = endTime;
        this.active = active;
        this.type = type;
    }

    public static BannerForm fromBanner(Banner existingBanner) {
        try {
            String uuid = existingBanner.getUuid();

            return new BannerForm(uuid,
                    existingBanner.getMessage(),
                    existingBanner.getHosts(),
                    existingBanner.getStartTime(),
                    existingBanner.getEndTime(),
                    existingBanner.isActive(),
                    existingBanner.getType());
        } catch (MissingUuidException e) {
            throw new RuntimeException(e);
        }
    }

    public static BannerForm fromRequest(String uuid, HttpServletRequest request) {
        String message = request.getParameter("message");
        String hosts = request.getParameter("hosts");
        String type = request.getParameter("type");

        long startTime = "".equals(request.getParameter("start_time")) ? 0 : parseTime(request.getParameter("start_time_selected_datetime"));
        long endTime = "".equals(request.getParameter("end_time")) ? 0 : parseTime(request.getParameter("end_time_selected_datetime"));

        boolean active = "on".equals(request.getParameter("active"));

        return new BannerForm(uuid, message, hosts, startTime, endTime, active, type);
    }

    public Errors validate() {
        Errors errors = new Errors();

        if (!hasValidStartTime()) {
            errors.addError("start_time", "invalid_time");
        }

        if (!hasValidEndTime()) {
            errors.addError("end_time", "invalid_time");
        }

        Errors modelErrors = toBanner().validate();

        return errors.merge(modelErrors);
    }

    public Banner toBanner() {
        return new Banner(uuid, message, hosts, active, startTime, endTime, type);
    }

}

