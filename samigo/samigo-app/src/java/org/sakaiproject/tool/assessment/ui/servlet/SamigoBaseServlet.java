/*
 * Copyright (c) 2003-2023 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.ui.servlet;

import java.util.Optional;

import javax.servlet.http.HttpServlet;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.http.MediaType;

public abstract class SamigoBaseServlet extends HttpServlet {


    private static char[] ILLEGAL_FILENAME_CHARS =
            new char[] { '#', '%', '&', '{', '}', '\\', '<', '>', '*', '?', '/', '$', '!', '\"', '\'', ':', '@', '+', '`', '|', '=' };

    private SecurityService securityService = ComponentManager.get(SecurityService.class);
    private SiteService siteService = ComponentManager.get(SiteService.class);
    private UserDirectoryService userDirectoryService = ComponentManager.get(UserDirectoryService.class);

    protected static final String CONTENT_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    protected static final String CONTENT_TYPE_PDF = "application/pdf";
    protected static final String CONTENT_TYPE_JSON = MediaType.APPLICATION_JSON_VALUE;

    protected static final String FILE_EXT_XLSX = ".xlsx";
    protected static final String FILE_EXT_PDF = ".pdf";

    public static final String EXPORT_FORMAT_XLSX = "xlsx";
    public static final String EXPORT_FORMAT_PDF = "pdf";


    protected boolean hasPrivilege(String functionName, String siteId) {
        return securityService.unlock(functionName, "/site/" + siteId);
    }

    protected Optional<String> getUserId() {
        User user = userDirectoryService.getCurrentUser();

        return user != null && StringUtils.isNotEmpty(user.getId()) ? Optional.of(user.getId()) : Optional.empty();
    }

    protected Optional<Site> getSite(String siteId) {
        try {
            return Optional.of(siteService.getSite(siteId));
        } catch (IdUnusedException e) {
            return Optional.empty();
        }
    }

    protected String cleanAssessmentTitle(PublishedAssessmentIfc assessment) {
        String title = assessment.getTitle();

        // Remove SecureDeliveryModule title decoration if present
        String decoration = StringUtils.trimToNull(assessment.getAssessmentMetaDataByLabel(SecureDeliveryServiceAPI.TITLE_DECORATION));
        if (decoration != null) {
            title = StringUtils.remove(title, " " + decoration);
        }

        return title;
    }

    protected String cleanFilename(String dirtyFilename) {
        String fileName = dirtyFilename;
        fileName = StringUtils.replace(fileName, " ", "-");

        for (char illegalChar : ILLEGAL_FILENAME_CHARS) {
            fileName = StringUtils.remove(fileName, illegalChar);
        }

        return fileName;
    }
}
