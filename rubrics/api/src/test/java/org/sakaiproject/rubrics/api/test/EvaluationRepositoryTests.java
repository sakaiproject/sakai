/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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
 package org.sakaiproject.rubrics.api.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.sakaiproject.rubrics.logic.AuthenticatedRequestContext;
import org.sakaiproject.rubrics.logic.Role;
import org.sakaiproject.rubrics.logic.repository.EvaluationRepository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RubricsTestConfiguration.class})
public class EvaluationRepositoryTests {
 
    @Test
    public void testSpelExpressions() {

        Assert.assertNotNull(EvaluationRepository.EVALUEE_CONSTRAINT);

        List<String> groups = Arrays.asList(new String[] {"group1", "group2"});
        AuthenticatedRequestContext principal = new AuthenticatedRequestContext("adrian", "sakai.assignments", "site1", "site", groups);
        EvaluationContext evaluationContext = new StandardEvaluationContext(SpelContext.from(principal));

        ExpressionParser expressionParser = new SpelExpressionParser();
        Expression expression = expressionParser.parseExpression("principal.userId");
        String userIdResult = (String) expression.getValue(evaluationContext, String.class);
        Assert.assertEquals(userIdResult, "adrian");

        expression = expressionParser.parseExpression("principal.groups");
        List<String> groupsResult = expression.getValue(evaluationContext, List.class);
        Assert.assertEquals(groupsResult, groups);

        principal.addAuthority(new SimpleGrantedAuthority(Role.ROLE_EVALUEE.name()));
        Assert.assertTrue(principal.isEvaluee());
        expression = expressionParser.parseExpression("principal.isEvaluee() ? 1 : 0");
        Integer evalueeResult = expression.getValue(evaluationContext, Integer.class);
        Assert.assertTrue(evalueeResult == 1);

        principal.addAuthority(new SimpleGrantedAuthority(Role.ROLE_EVALUATOR.name()));
        Assert.assertTrue(principal.isEvaluator());
        expression = expressionParser.parseExpression("principal.isEvaluator() ? 1 : 0");
        Integer evaluatorResult = expression.getValue(evaluationContext, Integer.class);
        Assert.assertTrue(evaluatorResult == 1);
    }
}
