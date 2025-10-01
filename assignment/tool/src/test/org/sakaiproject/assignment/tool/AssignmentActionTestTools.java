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
package org.sakaiproject.assignment.tool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.event.api.SessionState;

/**
 * Tests for AssignmentAction
 */
@SuppressWarnings("deprecation")
@RunWith(MockitoJUnitRunner.class)
public class AssignmentActionTestTools {

    private AssignmentAction assignmentAction;

    @Mock
    private AssignmentToolUtils assignmentToolUtils;

    @Before
    public void setUp() throws Exception {
        assignmentAction = Mockito.mock(AssignmentAction.class, Mockito.withSettings().defaultAnswer(Answers.CALLS_REAL_METHODS));
        setAssignmentToolUtils(assignmentAction, assignmentToolUtils);
    }

    private void setAssignmentToolUtils(AssignmentAction action, AssignmentToolUtils utils) throws Exception {
        Field field = AssignmentAction.class.getDeclaredField("assignmentToolUtils");
        field.setAccessible(true);
        field.set(action, utils);
    }

    public Integer getScaleFactor(Integer decimals) {
        return (int) Math.pow(10.0, decimals);
    }

    @Test
    public void testScalePointGrade() throws Exception {
        when(assignmentToolUtils.scalePointGrade(anyString(), anyInt(), anyList())).thenAnswer(invocation -> {
            String point = invocation.getArgument(0);
            int factor = invocation.getArgument(1);
            List<String> alerts = invocation.getArgument(2);

            if ("1.23456789".equals(point)) {
                alerts.add("invalid");
            }

            if (factor == 100 && ".7".equals(point)) {
                return "70";
            } else if (factor == 10000 && ".7".equals(point)) {
                return "7000";
            }

            return point;
        });

        SessionState state = new SessionStateFake();

        Integer decimals = 2;
        String scaledGrade = assignmentAction.scalePointGrade(state, ".7", getScaleFactor(decimals));
        assertEquals("70", scaledGrade);
        assertEquals(null, state.getAttribute(AssignmentAction.STATE_MESSAGE));
        state.clear();

        scaledGrade = assignmentAction.scalePointGrade(state, "1.23456789", getScaleFactor(decimals));
        assertEquals("1.23456789", scaledGrade);
        assertNotEquals(null, state.getAttribute(AssignmentAction.STATE_MESSAGE));
        state.clear();

        decimals = 4;
        scaledGrade = assignmentAction.scalePointGrade(state, ".7", getScaleFactor(decimals));
        assertEquals("7000", scaledGrade);
        assertEquals(null, state.getAttribute(AssignmentAction.STATE_MESSAGE));
        state.clear();
    }
}
