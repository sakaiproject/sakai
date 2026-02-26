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
package org.sakaiproject.assignment.impl;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;

public class AssignmentServiceImplLinkMappingTest {

    @Test
    public void testAddAssignmentLinkMappingsIncludesDeepLinkPatterns() throws Exception {
        AssignmentServiceImpl assignmentService = new AssignmentServiceImpl();
        Map<String, String> transversalMap = new HashMap<>();

        String fromContext = "old-site";
        String toContext = "new-site";
        String fromAssignmentId = "old-assignment-id";
        String toAssignmentId = "new-assignment-id";

        Method method = AssignmentServiceImpl.class.getDeclaredMethod("addAssignmentLinkMappings",
                Map.class, String.class, String.class, String.class, String.class);
        method.setAccessible(true);
        method.invoke(assignmentService, transversalMap, fromContext, toContext, fromAssignmentId, toAssignmentId);

        String fromReference = AssignmentReferenceReckoner.reckoner()
                .context(fromContext).id(fromAssignmentId).reckon().getReference();
        String toReference = AssignmentReferenceReckoner.reckoner()
                .context(toContext).id(toAssignmentId).reckon().getReference();

        assertEquals("assignment/" + toAssignmentId, transversalMap.get("assignment/" + fromAssignmentId));
        assertEquals(toReference, transversalMap.get(fromReference));
        assertEquals("assignmentId=" + toAssignmentId, transversalMap.get("assignmentId=" + fromAssignmentId));
        assertEquals("assignmentReference=" + toReference,
                transversalMap.get("assignmentReference=" + fromReference));

        String encodedFromReference = URLEncoder.encode(fromReference, StandardCharsets.UTF_8);
        String encodedToReference = URLEncoder.encode(toReference, StandardCharsets.UTF_8);
        assertEquals("assignmentReference=" + encodedToReference,
                transversalMap.get("assignmentReference=" + encodedFromReference));
        assertEquals("assignmentReference%3D" + encodedToReference,
                transversalMap.get("assignmentReference%3D" + encodedFromReference));
        assertEquals("assignmentId%3D" + toAssignmentId,
                transversalMap.get("assignmentId%3D" + fromAssignmentId));
    }
}
