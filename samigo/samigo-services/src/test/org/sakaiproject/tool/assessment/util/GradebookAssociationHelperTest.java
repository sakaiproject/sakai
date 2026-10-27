/**
 * Copyright (c) 2026 The Apereo Foundation
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.sakaiproject.grading.api.AssessmentNotFoundException;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.util.GradebookAssociationHelper.SelectedGradebookValidation;

public class GradebookAssociationHelperTest {

	@Test
	public void parseItemIds_trimsAndSkipsBlanks() {
		List<String> ids = GradebookAssociationHelper.parseItemIds(" 10 , ,20, ");
		assertEquals(2, ids.size());
		assertEquals("10", ids.get(0));
		assertEquals("20", ids.get(1));
		assertTrue(GradebookAssociationHelper.parseItemIds(null).isEmpty());
		assertTrue(GradebookAssociationHelper.parseItemIds("").isEmpty());
		assertTrue(GradebookAssociationHelper.parseItemIds("   ").isEmpty());
	}

	@Test
	public void removeItemId_removesOneAndClearsWhenEmpty() {
		assertEquals("20,30", GradebookAssociationHelper.removeItemId("10,20,30", "10"));
		assertEquals("", GradebookAssociationHelper.removeItemId("10", "10"));
		assertEquals("10", GradebookAssociationHelper.removeItemId("10", "99"));
		assertEquals("10,20", GradebookAssociationHelper.removeItemId("10,20", "2"));
	}

	@Test
	public void remapItemIds_whenMapPresent() {
		Map<String, String> map = new HashMap<>();
		map.put("gb/10", "gb/100");
		map.put("gb/20", "gb/200");
		GradingService gradingService = mockSiteAssignment("site1", 100L, false);
		when(gradingService.getGradebookUidByAssignmentById("site1", 200L)).thenReturn("site1");
		when(gradingService.getAssignment("site1", "site1", 200L)).thenReturn(new Assignment());
		assertEquals("100,200", GradebookAssociationHelper.remapItemIds("10,20", map, "site1", gradingService));
	}

	@Test
	public void remapItemIds_dropsMappedWhenConfirmedAbsent() {
		Map<String, String> map = new HashMap<>();
		map.put("gb/10", "gb/100");
		GradingService gradingService = mockSiteAssignment("site1", 20L, false);
		when(gradingService.getGradebookUidByAssignmentById(eq("site1"), eq(100L)))
				.thenThrow(new AssessmentNotFoundException("gone"));
		assertEquals("20", GradebookAssociationHelper.remapItemIds("10,20", map, "site1", gradingService));
	}

	@Test
	public void remapItemIds_partialMap_dropsUnmappedSourceItem() {
		Map<String, String> map = Map.of("gb/10", "gb/100");
		GradingService gradingService = mockSiteAssignment("target", 100L, false);
		when(gradingService.getGradebookUidByAssignmentById("target", 20L)).thenReturn("source");

		assertEquals("100", GradebookAssociationHelper.remapItemIds("10,20", map, "target", gradingService));
	}

	@Test
	public void remapItemIds_emptyMap_dropsSourceItems() {
		GradingService gradingService = mock(GradingService.class);
		when(gradingService.getGradebookUidByAssignmentById("target", 10L)).thenReturn("source");

		assertEquals("", GradebookAssociationHelper.remapItemIds("10", Map.of(), "target", gradingService));
		assertEquals("", GradebookAssociationHelper.remapItemIds("10", null, "target", gradingService));
	}

	@Test
	public void remapItemIds_emptyMap_preservesExistingTargetItem() {
		GradingService gradingService = mockSiteAssignment("target", 10L, false);

		assertEquals("10", GradebookAssociationHelper.remapItemIds("10", Map.of(), "target", gradingService));
	}

	@Test
	public void remapItemIds_preservesUnmappedItemWhenLookupFails() {
		GradingService gradingService = mock(GradingService.class);
		when(gradingService.getGradebookUidByAssignmentById("target", 10L))
				.thenThrow(new RuntimeException("timeout"));

		assertEquals("10", GradebookAssociationHelper.remapItemIds("10", Map.of(), "target", gradingService));
	}

	@Test
	public void validateSelectedGradebookItems_notSelected_ok() {
		assertEquals(SelectedGradebookValidation.OK,
				GradebookAssociationHelper.validateSelectedGradebookItems(
						EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString(), "", "site1", null));
	}

	@Test
	public void validateSelectedGradebookItems_blank_blank() {
		assertEquals(SelectedGradebookValidation.BLANK,
				GradebookAssociationHelper.validateSelectedGradebookItems(
						EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString(), "", "site1", mock(GradingService.class)));
		assertEquals(SelectedGradebookValidation.BLANK,
				GradebookAssociationHelper.validateSelectedGradebookItems(
						EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString(), "  , ", "site1", mock(GradingService.class)));
	}

	@Test
	public void validateSelectedGradebookItems_missingItem() {
		GradingService gradingService = mock(GradingService.class);
		when(gradingService.getGradebookUidByAssignmentById(anyString(), anyLong()))
				.thenThrow(new AssessmentNotFoundException("gone"));
		assertEquals(SelectedGradebookValidation.INVALID_OR_MISSING,
				GradebookAssociationHelper.validateSelectedGradebookItems(
						EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString(), "99", "site1", gradingService));
	}

	@Test
	public void validateSelectedGradebookItems_validItem() {
		GradingService gradingService = mockSiteAssignment("site1", 42L, false);
		assertEquals(SelectedGradebookValidation.OK,
				GradebookAssociationHelper.validateSelectedGradebookItems(
						EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString(), "42", "site1", gradingService));
	}

	@Test
	public void validateSelectedGradebookItems_lookupFailed() {
		GradingService gradingService = mock(GradingService.class);
		when(gradingService.getGradebookUidByAssignmentById(eq("site1"), eq(42L)))
				.thenThrow(new RuntimeException("timeout"));
		assertEquals(SelectedGradebookValidation.LOOKUP_FAILED,
				GradebookAssociationHelper.validateSelectedGradebookItems(
						EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString(), "42", "site1", gradingService));
	}

	@Test
	public void validateSelectedGradebookItems_nonNumeric() {
		GradingService gradingService = mock(GradingService.class);
		assertEquals(SelectedGradebookValidation.INVALID_OR_MISSING,
				GradebookAssociationHelper.validateSelectedGradebookItems(
						EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString(), "not-a-number", "site1", gradingService));
	}

	@Test
	public void validateSelectedGradebookItems_groupGradebook() {
		GradingService gradingService = mock(GradingService.class);
		Assignment assignment = new Assignment();
		when(gradingService.isGradebookGroupEnabled("site1")).thenReturn(true);
		when(gradingService.getGradebookUidByAssignmentById("site1", 42L)).thenReturn("group-gb");
		when(gradingService.getGradebookGroupInstancesIds("site1")).thenReturn(List.of("group-gb", "other-gb"));
		when(gradingService.getAssignment(eq("group-gb"), eq("site1"), eq(42L))).thenReturn(assignment);

		assertEquals(SelectedGradebookValidation.OK,
				GradebookAssociationHelper.validateSelectedGradebookItems(
						EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString(), "42", "site1", gradingService));
	}

	@Test
	public void filterPresentItemIds_preservesUnknown() {
		GradingService gradingService = mock(GradingService.class);
		when(gradingService.getGradebookUidByAssignmentById(eq("site1"), eq(42L)))
				.thenThrow(new RuntimeException("timeout"));
		assertEquals("42", GradebookAssociationHelper.filterPresentItemIds("42", "site1", gradingService));
	}

	@Test
	public void filterPresentItemIds_preservesItemWhenAssignmentLookupFails() {
		GradingService gradingService = mockSiteAssignment("site1", 42L, false);
		when(gradingService.getAssignment("site1", "site1", 42L)).thenThrow(new RuntimeException("timeout"));

		assertEquals("42", GradebookAssociationHelper.filterPresentItemIds("42", "site1", gradingService));
	}

	@Test
	public void validateSelectedGradebookItems_rejectsExternallyMaintainedItem() {
		GradingService gradingService = mockSiteAssignment("site1", 42L, false);
		Assignment assignment = new Assignment();
		assignment.setExternallyMaintained(true);
		when(gradingService.getAssignment("site1", "site1", 42L)).thenReturn(assignment);

		assertEquals(SelectedGradebookValidation.INVALID_OR_MISSING,
				GradebookAssociationHelper.validateSelectedGradebookItems(
						EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString(), "42", "site1", gradingService));
	}

	private GradingService mockSiteAssignment(String siteId, long assignmentId, boolean groupEnabled) {
		GradingService gradingService = mock(GradingService.class);
		Assignment assignment = new Assignment();
		when(gradingService.isGradebookGroupEnabled(siteId)).thenReturn(groupEnabled);
		when(gradingService.getGradebookUidByAssignmentById(siteId, assignmentId)).thenReturn(siteId);
		when(gradingService.getAssignment(eq(siteId), eq(siteId), eq(assignmentId))).thenReturn(assignment);
		return gradingService;
	}
}
