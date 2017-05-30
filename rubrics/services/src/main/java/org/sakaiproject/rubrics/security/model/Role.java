package org.sakaiproject.rubrics.security.model;

import org.sakaiproject.rubrics.model.BaseResource;
import org.sakaiproject.rubrics.model.Criterion;
import org.sakaiproject.rubrics.model.Evaluation;
import org.sakaiproject.rubrics.model.Rating;
import org.sakaiproject.rubrics.model.Rubric;
import org.sakaiproject.rubrics.model.ToolItemRubricAssociation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Role {

    ROLE_EDITOR ("rbcs.editor", Arrays.asList(Rubric.class, Criterion.class, Rating.class)),
    ROLE_ASSOCIATOR ("rbcs.associator", Arrays.asList(ToolItemRubricAssociation.class)),
    ROLE_EVALUATOR ("rbcs.evaluator", Arrays.asList(Evaluation.class)),
    ROLE_EVALUEE ("rbcs.evaluee", Collections.emptyList());

    private String permissionKey;
    private List<Class<? extends BaseResource>> authorizedToCreateOrEditResources;

    Role(String permissionKey, List<Class<? extends BaseResource>> authorizedToCreateOrEditResources) {
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