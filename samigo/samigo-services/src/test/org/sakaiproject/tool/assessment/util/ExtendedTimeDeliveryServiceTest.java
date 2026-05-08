/*
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
package org.sakaiproject.tool.assessment.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.tool.assessment.data.dao.assessment.ExtendedTime;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;

public class ExtendedTimeDeliveryServiceTest {

    @Test
    public void testResolveEntriesPrefersUserSpecificEntryOverGroupEntry() {
        ExtendedTime groupEntry = buildExtendedTime("group-a", null, 100L);
        ExtendedTime userEntry = buildExtendedTime(null, "student1", 100L);
        Set<String> siteGroupIds = new HashSet<>(Collections.singletonList("group-a"));

        Map<Long, ExtendedTime> resolvedEntries = ExtendedTimeDeliveryService.resolveEntriesByPublishedAssessment(
                Arrays.asList(groupEntry, userEntry), "student1", siteGroupIds);

        Assert.assertSame(userEntry, resolvedEntries.get(100L));
    }

    @Test
    public void testResolveEntriesDeterministicallyResolvesMatchingGroupEntry() {
        ExtendedTime firstGroupEntry = buildExtendedTime("group-a", null, 100L);
        ExtendedTime secondGroupEntry = buildExtendedTime("group-b", null, 100L);
        Set<String> siteGroupIds = new HashSet<>(Arrays.asList("group-a", "group-b"));

        Map<Long, ExtendedTime> resolvedEntries = ExtendedTimeDeliveryService.resolveEntriesByPublishedAssessment(
                Arrays.asList(firstGroupEntry, secondGroupEntry), "student1", siteGroupIds);
        Map<Long, ExtendedTime> reversedResolvedEntries = ExtendedTimeDeliveryService.resolveEntriesByPublishedAssessment(
                Arrays.asList(secondGroupEntry, firstGroupEntry), "student1", siteGroupIds);

        Assert.assertSame(secondGroupEntry, resolvedEntries.get(100L));
        Assert.assertSame(secondGroupEntry, reversedResolvedEntries.get(100L));
    }

    @Test
    public void testResolvedConstructorFallsBackToAssessmentDatesWithoutExtendedTime() {
        Date startDate = new Date(1000L);
        Date dueDate = new Date(2000L);
        Date retractDate = new Date(3000L);
        PublishedAssessmentFacade assessment = buildAssessment(100L, startDate, dueDate, retractDate);

        ExtendedTimeDeliveryService deliveryService = new ExtendedTimeDeliveryService(assessment, "student1", null);

        Assert.assertFalse(deliveryService.hasExtendedTime());
        Assert.assertEquals(startDate, deliveryService.getStartDate());
        Assert.assertEquals(dueDate, deliveryService.getDueDate());
        Assert.assertEquals(retractDate, deliveryService.getRetractDate());
        Assert.assertEquals(Integer.valueOf(0), deliveryService.getTimeLimit());
    }

    @Test
    public void testAssessmentIdConstructorRejectsNullResolvedExtendedTime() {
        try {
            new ExtendedTimeDeliveryService(100L, "student1", null);
            Assert.fail("Expected IllegalArgumentException for null resolvedExtendedTime");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("resolvedExtendedTime"));
        }
    }

    private ExtendedTime buildExtendedTime(String groupId, String userId, Long publishedAssessmentId) {
        ExtendedTime extendedTime = new ExtendedTime();
        PublishedAssessmentData publishedAssessment = new PublishedAssessmentData();
        publishedAssessment.setPublishedAssessmentId(publishedAssessmentId);
        extendedTime.setPubAssessment(publishedAssessment);
        extendedTime.setGroup(groupId);
        extendedTime.setUser(userId);
        return extendedTime;
    }

    private PublishedAssessmentFacade buildAssessment(Long publishedAssessmentId, Date startDate, Date dueDate, Date retractDate) {
        return new PublishedAssessmentFacade(publishedAssessmentId, "Assessment", "Entire Site",
                startDate, dueDate, retractDate, null, null, null, null,
                null, Boolean.TRUE, 1, null, PublishedAssessmentFacade.ACTIVE_STATUS, null, null);
    }
}
