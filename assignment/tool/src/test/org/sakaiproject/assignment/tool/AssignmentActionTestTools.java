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
import static org.mockito.Mockito.*;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.FormattedText;

/**
 * Tests for AssignmentAction
 */
@SuppressWarnings("deprecation")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ComponentManager.class})
public class AssignmentActionTestTools {

    private AssignmentAction assignmentAction;
    @Mock
    private AssignmentService assignmentService;

    @Before
    public void setUp() {
        BasicConfigurator.configure();
        PowerMockito.mockStatic(ComponentManager.class);
        // A mock component manager.
        when(ComponentManager.get(any(Class.class))).then(new Answer<Object>() {
            private Map<Class, Object> mocks = new HashMap<>();
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Class classToMock = (Class) invocation.getArguments()[0];
                return mocks.computeIfAbsent(classToMock, k -> mock(classToMock));
            }
        });
        
        when(ComponentManager.get(SessionManager.class).getCurrentSession()).thenReturn(mock(Session.class));
        when(FormattedText.getDecimalSeparator()).thenReturn(".");
        
        when(FormattedText.getNumberFormat()).thenReturn(NumberFormat.getInstance(Locale.ENGLISH));
        assignmentAction = new AssignmentAction();

        Mockito.when(ComponentManager.get(AssignmentService.class)).thenReturn(assignmentService);

    }

    //This probably should also be moved from AssignmentAction to a util or something
    public Integer getScaleFactor(Integer decimals) {		
        return (int)Math.pow(10.0, decimals);
    }

    @Test
    public void testScalePointGrade() {
        SessionState state = new SessionStateFake();
        //Simple state?

        Integer decimals=2;
        when(ServerConfigurationService.getInt("assignment.grading.decimals", AssignmentConstants.DEFAULT_DECIMAL_POINT)).thenReturn(decimals);
        String scaledGrade = assignmentAction.scalePointGrade(state, ".7",getScaleFactor(decimals));
        assertEquals(scaledGrade,"70");
        //Verify the state message is null
        assertEquals(state.getAttribute(AssignmentAction.STATE_MESSAGE), null);
        state.clear();
        
        /* This case is broken at the moment but it does return invalid in the state
         */
        scaledGrade = assignmentAction.scalePointGrade(state, "1.23456789",getScaleFactor(decimals));
        assertEquals(scaledGrade,"1.23456789");
        //Verify the state message isn't null (indicating an error)
        assertNotEquals(state.getAttribute(AssignmentAction.STATE_MESSAGE), null);
        state.clear();

        decimals=4;
        when(ServerConfigurationService.getInt("assignment.grading.decimals", AssignmentConstants.DEFAULT_DECIMAL_POINT)).thenReturn(decimals);
        scaledGrade = assignmentAction.scalePointGrade(state, ".7",getScaleFactor(decimals));
        assertEquals(scaledGrade,"7000");
        //Verify the state message is null
        assertEquals(state.getAttribute(AssignmentAction.STATE_MESSAGE), null);
        state.clear();
    }
}
