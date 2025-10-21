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

package org.sakaiproject.tool.assessment.ui.bean.shared;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;

/**
 * Exposes portal-related state required by legacy Samigo authoring views.
 */
@ManagedBean(name = "authoringPortalState")
@RequestScoped
public class AuthoringPortalStateBean {

    /**
     * Returns the iframe id for the current tool panel. Mirrors the legacy JSP
     * logic used by the authoring views that relied on the {@code panel}
     * request parameter, falling back to the current placement identifier.
     *
     * @return the iframe identifier used by legacy scripts (never {@code null})
     */
    public String getPanelFrameId() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            ExternalContext externalContext = context.getExternalContext();
            if (externalContext != null) {
                String panelParam = externalContext.getRequestParameterMap().get("panel");
                if (StringUtils.isNotBlank(panelParam)) {
                    return panelParam;
                }
            }
        }

        Placement placement = ToolManager.getCurrentPlacement();
        if (placement != null) {
            return "Main" + placement.getId();
        }

        return "Main";
    }
}
