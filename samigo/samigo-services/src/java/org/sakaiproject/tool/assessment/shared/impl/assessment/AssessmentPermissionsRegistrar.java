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
package org.sakaiproject.tool.assessment.shared.impl.assessment;

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.samigo.util.SamigoConstants;

public class AssessmentPermissionsRegistrar {

    private FunctionManager functionManager;

    public void setFunctionManager(FunctionManager functionManager) {
        this.functionManager = functionManager;
    }

    public void init() {
        functionManager.registerFunction(SamigoConstants.AUTHZ_TAKE_ASSESSMENT, true);
        functionManager.registerFunction(SamigoConstants.AUTHZ_SUBMIT_ASSESSMENT, true);
        functionManager.registerFunction(SamigoConstants.AUTHZ_CREATE_ASSESSMENT, true);
        functionManager.registerFunction(SamigoConstants.AUTHZ_EDIT_ASSESSMENT_ANY, true);
        functionManager.registerFunction(SamigoConstants.AUTHZ_EDIT_ASSESSMENT_OWN, true);
        functionManager.registerFunction(SamigoConstants.AUTHZ_DELETE_ASSESSMENT_ANY, true);
        functionManager.registerFunction(SamigoConstants.AUTHZ_DELETE_ASSESSMENT_OWN, true);
        functionManager.registerFunction(SamigoConstants.AUTHZ_PUBLISH_ASSESSMENT_ANY, true);
        functionManager.registerFunction(SamigoConstants.AUTHZ_PUBLISH_ASSESSMENT_OWN, true);
        functionManager.registerFunction(SamigoConstants.AUTHZ_GRADE_ASSESSMENT_ANY, true);
        functionManager.registerFunction(SamigoConstants.AUTHZ_GRADE_ASSESSMENT_OWN, true);
        functionManager.registerFunction(SamigoConstants.AUTHZ_QUESTIONPOOL_CREATE, true);
        functionManager.registerFunction(SamigoConstants.AUTHZ_QUESTIONPOOL_EDIT_OWN, true);
        functionManager.registerFunction(SamigoConstants.AUTHZ_QUESTIONPOOL_DELETE_OWN, true);
        functionManager.registerFunction(SamigoConstants.AUTHZ_QUESTIONPOOL_COPY_OWN, true);
        functionManager.registerFunction(SamigoConstants.AUTHZ_TEMPLATE_CREATE, true);
        functionManager.registerFunction(SamigoConstants.AUTHZ_TEMPLATE_EDIT_OWN, true);
        functionManager.registerFunction(SamigoConstants.AUTHZ_TEMPLATE_DELETE_OWN, true);
        functionManager.registerFunction(SamigoConstants.AUTHZ_ASSESSMENT_ALL_GROUPS, true);
    }
}
