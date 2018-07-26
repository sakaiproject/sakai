/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.rubrics.logic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.sakaiproject.rubrics.logic.model.Criterion;
import org.sakaiproject.rubrics.logic.model.Evaluation;
import org.sakaiproject.rubrics.logic.model.Rating;
import org.sakaiproject.rubrics.logic.model.Rubric;
import org.sakaiproject.rubrics.logic.model.ToolItemRubricAssociation;

public enum Role {

    ROLE_EDITOR ("rubrics.editor", Arrays.asList(Rubric.class, Criterion.class, Rating.class)),
    ROLE_ASSOCIATOR ("rubrics.associator", Arrays.asList(ToolItemRubricAssociation.class)),
    ROLE_EVALUATOR ("rubrics.evaluator", Arrays.asList(Evaluation.class)),
    ROLE_EVALUEE ("rubrics.evaluee", Collections.emptyList()),
    ROLE_SUPERUSER ("rubrics.superuser", Collections.emptyList());

    private String permissionKey;
    private List<Class> authorizedToCreateOrEditResources;

    Role(String permissionKey, List<Class> authorizedToCreateOrEditResources) {
        this.permissionKey = permissionKey;
        this.authorizedToCreateOrEditResources = authorizedToCreateOrEditResources;
    }

    public String getPermissionKey() {
        return permissionKey;
    }

    public boolean canCreateOrEdit(String resourceType) {
       return this.authorizedToCreateOrEditResources.stream().anyMatch(
               r -> r.getSimpleName().equalsIgnoreCase(resourceType));
    }

    public static Role fromPermissionKey(String key) {
        for (Role roles: Role.values()) {
            if (roles.permissionKey.equals(key)) {
                return roles;
            }
        }
        throw new IllegalArgumentException(key);
    }
}
