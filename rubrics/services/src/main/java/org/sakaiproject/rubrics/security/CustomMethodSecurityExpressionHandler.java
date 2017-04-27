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

import org.aopalliance.intercept.MethodInvocation;
import org.sakaiproject.rubrics.repository.CriterionRepository;
import org.sakaiproject.rubrics.repository.EvaluationRepository;
import org.sakaiproject.rubrics.repository.RatingRepository;
import org.sakaiproject.rubrics.repository.RubricRepository;
import org.sakaiproject.rubrics.repository.ToolItemRubricAssociationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CustomMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

    private AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

    @Autowired
    private RubricRepository rubricRepository;

    @Autowired
    private CriterionRepository criterionRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private EvaluationRepository evaluationRepository;

    @Autowired
    private ToolItemRubricAssociationRepository toolItemRubricAssociationRepository;

    protected MethodSecurityExpressionOperations createSecurityExpressionRoot(
            Authentication authentication, MethodInvocation invocation) {
        CustomMethodSecurityExpressionRoot root =
                new CustomMethodSecurityExpressionRoot(rubricRepository, criterionRepository, ratingRepository,
                        evaluationRepository, toolItemRubricAssociationRepository, authentication);
        root.setPermissionEvaluator(getPermissionEvaluator());
        root.setTrustResolver(this.trustResolver);
        root.setRoleHierarchy(getRoleHierarchy());
        return root;
    }
}
