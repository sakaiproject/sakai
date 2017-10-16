/**********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
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

package org.sakaiproject.rubrics.security;

import org.sakaiproject.rubrics.model.BaseResource;
import org.sakaiproject.rubrics.model.Criterion;
import org.sakaiproject.rubrics.model.Evaluation;
import org.sakaiproject.rubrics.model.Rating;
import org.sakaiproject.rubrics.model.Rubric;
import org.sakaiproject.rubrics.model.ToolItemRubricAssociation;
import org.sakaiproject.rubrics.repository.BaseResourceRepository;
import org.sakaiproject.rubrics.repository.CriterionRepository;
import org.sakaiproject.rubrics.repository.EvaluationRepository;
import org.sakaiproject.rubrics.repository.RatingRepository;
import org.sakaiproject.rubrics.repository.RubricRepository;
import org.sakaiproject.rubrics.repository.ToolItemRubricAssociationRepository;
import org.sakaiproject.rubrics.security.model.AuthenticatedRequestContext;
import org.sakaiproject.rubrics.security.model.Role;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom Spring Security processor which allows for simplified authorization annotation expressions to be placed on
 * methods.
 * <p>This is a request scoped bean.</p>
 * <p>
 * Within rubrics, only <em>Editors</em> can create and modify rubric resources, with two exceptions:
 * <em>Associators</em> are the only ones who can create and modify {@link ToolItemRubricAssociation}s and
 * <em>Evaluators</em> are the only ones who can create and modify {@link Evaluation}s.</p>
 *
 * <p>In all cases, resources are associated with an owning context and the requester must be accessing from within
 * that context (or at least the integrating partner tool must create an access token representing that the bearer
 * has appropriate roles in that context).</p>
 *
 * <p>There is one special exception to the requirement that all requests must be scoped to a single owning context, and
 * that is the case where a special context ID of <code>*</code> is provided, which grants access to resources in all
 * owning contexts of the provided context type. The token exists to support administrative functions like deep copying
 * of entities in Sakai during course copies, where generating an access token to represent the edit and read rights of
 * resources in the exact two contexts for each copy would be prohibitive on the client. We stopped short of creating an
 * Admin role with all access since the required use case what we are aware of is quite constrained.</p>
 */
public class CustomMethodSecurityExpressionRoot extends SecurityExpressionRoot
        implements MethodSecurityExpressionOperations {

    private static final String DEFAULT_RESOURCE_COPY_ID = "default";

    private final Map<String, BaseResourceRepository<? extends BaseResource, Long>> repositories;

    private AuthenticatedRequestContext authenticatedRequestContext;

    public CustomMethodSecurityExpressionRoot(RubricRepository rubricRepository,
            CriterionRepository criterionRepository, RatingRepository ratingRepository,
            EvaluationRepository evaluationRepository,
            ToolItemRubricAssociationRepository toolItemRubricAssociationRepository, Authentication authentication) {
        super(authentication);
        authenticatedRequestContext = (AuthenticatedRequestContext) super.authentication.getPrincipal();
        this.repositories = new HashMap<>();
        repositories.put(Rubric.class.getSimpleName(), rubricRepository);
        repositories.put(Criterion.class.getSimpleName(), criterionRepository);
        repositories.put(Rating.class.getSimpleName(), ratingRepository);
        repositories.put(Evaluation.class.getSimpleName(), evaluationRepository);
        repositories.put(ToolItemRubricAssociation.class.getSimpleName(), toolItemRubricAssociationRepository);
    }

    /**
     *
     * @param resourceId
     * @param resourceType
     * @return
     */
    public boolean canRead(Long resourceId, String resourceType) {
        BaseResource resource = repositories.get(resourceType).findOne(resourceId);
        boolean result = resource.getMetadata().isShared()
                || isAuthorizedToAccessContextResource(resourceId, resourceType);
        if (result) {
            result = verifyResourceSpecificReadRules(resource);
        }
        return result;
    }

    public boolean canWrite(Long resourceId, String resourceType) {
        boolean result = false;
        if (resourceId == null || isAuthorizedToAccessContextResource(resourceId, resourceType)) {
            result = authenticatedRequestContext.getAuthorities().stream().anyMatch(
                    authority -> Role.valueOf(authority.getAuthority()).canCreateOrEdit(resourceType));
        };
        return result;
    }

    public <T extends BaseResource> boolean canRead(T resource) {
        return canRead(resource.getId(), resource.getClass().getSimpleName());
    }

    public <T extends BaseResource> boolean canWrite(T resource) {
        return canWrite(resource.getId(), resource.getClass().getSimpleName());
    }

    /**
     * Currently can only be used for rubrics, criterions and ratings.
     *
     * @param resourceId
     * @param resourceType
     * @return
     */
    public boolean canCopy(String resourceId, String resourceType) {
        boolean allowed = false;
        if (authenticatedRequestContext.isEditor()) {
            allowed = DEFAULT_RESOURCE_COPY_ID.equalsIgnoreCase(resourceId)
                    || authenticatedRequestContext.isSuperUser();
            if (!allowed) {
                Long id = Long.parseLong(resourceId);
                allowed = canRead(id, resourceType);
            }
        }
        return allowed;
    }

    private boolean isAuthorizedToAccessContextResource(Long resourceId, String resourceType) {
        boolean allowed = authenticatedRequestContext.isSuperUser();
        if (!allowed) {
            BaseResource resource = repositories.get(resourceType).findOne(resourceId);
            allowed = resource.getMetadata().getOwnerId().equalsIgnoreCase(
                    authenticatedRequestContext.getContextId())
                    && resource.getMetadata().getOwnerType().equalsIgnoreCase(
                    authenticatedRequestContext.getContextType())
                    || resource.getMetadata().getCreatorId().equalsIgnoreCase(authenticatedRequestContext.getUserId());
        }
        return allowed;
    }

    /**
     * Applies resource type specific read access rules, after the broader context checks have occurred.
     * @param resource
     * @return
     */
    private <T> boolean verifyResourceSpecificReadRules(T resource) {
        boolean result = false;
        if (Rubric.class.isInstance(resource)
                || Criterion.class.isInstance(resource)
                || Rating.class.isInstance(resource)) {
            // Direct read access to rubric resource types is allowed for all. The only limitation is that associations
            // may restrict previewing which rubric is associated with an item, but the rubric itself is visible to all,
            // only the knowledge of which rubric is hidden from evaluees. Given that multiple associations may
            // use the same rubric with differing preview rules, direct read access cannot be blocked for evaluees.
            result = true;
        } else if (Evaluation.class.isInstance(resource)) {
            if (authenticatedRequestContext.isEvaluator()) {
                result = true; // All evaluators in a context can view all evaluations
                // NOTE: TBD Peer evaluators will require a more limited access to only their evaluations or a sub-context/group
                // result = authenticatedRequestContext.getUserId().equalsIgnoreCase(((Evaluation)resource).getEvaluatorId());
            }
            else if (authenticatedRequestContext.isEvalueeOnly()) {
                // Can always see Evaluation that they are evaluatedId on
                result = authenticatedRequestContext.getUserId().equalsIgnoreCase(((Evaluation)resource).getEvaluatedItemOwnerId());
            }
        } else if (ToolItemRubricAssociation.class.isInstance(resource)) {
            result = true; //All roles in the context can view associations, associated rubric data may be filtered though
        }
        return result;
    }

    @Override
    public void setFilterObject(Object o) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Object getFilterObject() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setReturnObject(Object o) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Object getReturnObject() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Object getThis() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
