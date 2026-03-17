/**
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
package org.sakaiproject.lessonbuildertool.service;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import org.sakaiproject.scorm.api.ScormConstants;
import org.sakaiproject.scorm.model.api.ActivitySummary;
import org.sakaiproject.scorm.model.api.Attempt;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResultService;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScormEntityTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock private ScormContentService mockContentService;
    @Mock private ScormResultService mockResultService;
    @Mock private ContentPackage mockContentPackage;
    @Mock private Attempt mockAttempt;

    private ScormEntity entity;

    @Before
    public void setUp() throws Exception {
        setStaticField("scormContentService", mockContentService);
        setStaticField("scormResultService", mockResultService);

        entity = new ScormEntity(LessonEntity.TYPE_SCORM, 42L, 1);
        entity.contentPackage = mockContentPackage;

        when(mockContentPackage.getContentPackageId()).thenReturn(42L);
        when(mockAttempt.getAttemptNumber()).thenReturn(1L);
    }

    @After
    public void tearDown() throws Exception {
        setStaticField("scormContentService", null);
        setStaticField("scormResultService", null);
    }

    // notPublished: OVERDUE must be treated as published (accessible to learners)
    @Test
    public void notPublished_whenOverdue_returnsFalse() {
        when(mockContentService.getContentPackageStatus(mockContentPackage))
            .thenReturn(ScormConstants.CONTENT_PACKAGE_STATUS_OVERDUE);
        assertFalse(entity.notPublished());
    }

    @Test
    public void notPublished_whenOpen_returnsFalse() {
        when(mockContentService.getContentPackageStatus(mockContentPackage))
            .thenReturn(ScormConstants.CONTENT_PACKAGE_STATUS_OPEN);
        assertFalse(entity.notPublished());
    }

    @Test
    public void notPublished_whenClosed_returnsTrue() {
        when(mockContentService.getContentPackageStatus(mockContentPackage))
            .thenReturn(ScormConstants.CONTENT_PACKAGE_STATUS_CLOSED);
        assertTrue(entity.notPublished());
    }

    // getSubmission: all SCOs must be complete, not just any one
    @Test
    public void getSubmission_whenOnlyOneSCOCompleted_returnsNull() {
        ActivitySummary done = sco("completed", "unknown");
        ActivitySummary notDone = sco("incomplete", "unknown");

        when(mockResultService.getNewstAttempt(42L, "user1")).thenReturn(mockAttempt);
        when(mockResultService.getActivitySummaries(42L, "user1", 1L))
            .thenReturn(Arrays.asList(done, notDone));

        assertNull(entity.getSubmission("user1"));
    }

    @Test
    public void getSubmission_whenAllSCOsCompleted_returnsSubmission() {
        ActivitySummary s1 = sco("completed", "unknown");
        ActivitySummary s2 = sco("completed", "passed");

        when(mockResultService.getNewstAttempt(42L, "user1")).thenReturn(mockAttempt);
        when(mockResultService.getActivitySummaries(42L, "user1", 1L))
            .thenReturn(Arrays.asList(s1, s2));

        assertNotNull(entity.getSubmission("user1"));
    }

    // findObject: objectMap key must include leading slash to match stored sakaiId format
    @Test
    public void findObject_withMapHit_returnsRemappedRef() {
        Map<String, String> objectMap = new HashMap<>();
        objectMap.put("/scorm/42", "scorm/99");

        assertEquals("/scorm/99", entity.findObject("scorm/42/My Package", objectMap, "newSite"));
    }

    @Test
    public void findObject_withMapMiss_fallsBackToTitleSearch() {
        ContentPackage destPkg = mock(ContentPackage.class);
        when(destPkg.isDeleted()).thenReturn(false);
        when(destPkg.getTitle()).thenReturn("My Package");
        when(destPkg.getContentPackageId()).thenReturn(99L);
        when(mockContentService.getContentPackages("newSite"))
            .thenReturn(Collections.singletonList(destPkg));

        assertEquals("/scorm/99", entity.findObject("scorm/42/My Package", Collections.emptyMap(), "newSite"));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private ActivitySummary sco(String completionStatus, String successStatus) {
        ActivitySummary s = mock(ActivitySummary.class);
        when(s.getCompletionStatus()).thenReturn(completionStatus);
        when(s.getSuccessStatus()).thenReturn(successStatus);
        return s;
    }

    private static void setStaticField(String fieldName, Object value) throws Exception {
        Field f = ScormEntity.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(null, value);
    }
}
