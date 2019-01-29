/**
 * Copyright (c) 2005-2018 The Apereo Foundation
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

package org.sakaiproject.tool.assessment.util;

import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.sakaiproject.rubrics.logic.RubricsConstants;

public class ParameterUtil {
    /**
     * Get parameters for a specified rubric association
     * @param entityId Entity ID of a rubric association
     * @return Map of parameters key and parameters value
     */
    public Map<String,String> getRubricConfigurationParameters(String entityId, String evaluatedItemId) {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> requestParams = context.getExternalContext().getRequestParameterMap();
        Map<String, String> list = new HashMap<>();

        String entity = RubricsConstants.RBCS_PREFIX;
        if (evaluatedItemId != null && !evaluatedItemId.isEmpty()) {
            entity += evaluatedItemId + "-";
        }
        if (entityId != null && !entityId.isEmpty()) {
            entity += entityId + "-";
        }
        final String startsWith = entity;

        requestParams.forEach((key, value) -> {
            if (key.startsWith(startsWith)) {
                list.put(key, value);
            }
        });
        return list;
    }
}
