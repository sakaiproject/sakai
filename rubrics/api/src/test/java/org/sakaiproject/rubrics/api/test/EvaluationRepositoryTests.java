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
