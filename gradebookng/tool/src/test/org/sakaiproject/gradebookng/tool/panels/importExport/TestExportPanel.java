/**
 * Copyright (c) 2003-2024 The Apereo Foundation
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
package org.sakaiproject.gradebookng.tool.panels.importExport;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.CourseGradeTransferBean;
import org.sakaiproject.grading.api.SortType;
import org.sakaiproject.component.api.ServerConfigurationService;

/**
 * Tests for ExportPanel functionality, specifically the fixes for:
 * 1. Student Number column logic in custom exports
 * 2. Double ignore column issue in empty gradebooks  
 * 3. Proper column alignment between headers and data
 *
 * To run these tests:
 * mvn test -Dtest=TestExportPanel
 * 
 * Or run all gradebook tests:
 * mvn test
 */
public class TestExportPanel {

    @Mock
    private GradebookNgBusinessService businessService;
    
    @Mock
    private ServerConfigurationService serverConfigService;
    
    private TestableExportPanel exportPanel;
    
    private static final String SITE_ID = "test-site";
    private static final String GRADEBOOK_UID = "test-gradebook";

    // Test implementation that exposes the buildFile method for testing
    private class TestableExportPanel extends ExportPanel {
        public TestableExportPanel(String id) {
            super(id);
            this.businessService = TestExportPanel.this.businessService;
            this.serverConfigService = TestExportPanel.this.serverConfigService;
            this.currentSiteId = SITE_ID;
            this.currentGradebookUid = GRADEBOOK_UID;
        }

        // Expose buildFile method for testing
        public File buildFileForTesting(boolean isCustomExport) {
            return super.buildFile(isCustomExport);
        }
        
        // Mock getString method to return test values
        @Override
        public String getString(String key) {
            switch (key) {
                case "importExport.export.csv.headers.studentId": return "Student ID";
                case "importExport.export.csv.headers.studentName": return "Name";
                case "importExport.export.csv.headers.studentNumber": return "Student Number";
                case "importExport.export.csv.headers.studentDisplayId": return "Student Display ID";
                case "column.header.section": return "Section";
                case "importExport.export.csv.headers.points": return "Total Points";
                case "importExport.export.csv.headers.lastLogDate": return "Course Grade Override Date";
                case "importExport.export.csv.headers.courseGrade": return "Course Grade";
                case "importExport.export.csv.headers.calculatedGrade": return "Calculated Grade";
                case "importExport.export.csv.headers.gradeOverride": return "Grade Override";
                case "importExport.export.csv.headers.example.points": return "Example Assignment With Points";
                case "importExport.export.csv.headers.example.nopoints": return "Example Assignment With No Points";
                case "importExport.export.csv.headers.example.pointscomments": return "Example Assignment With Points And Comments";
                case "importExport.export.csv.headers.example.ignore": return "This column will be ignored";
                case "sections.label.none": return "None";
                default: return key;
            }
        }
    }

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        exportPanel = new TestableExportPanel("testPanel");
        
        // Mock basic service calls
        when(businessService.categoriesAreEnabled(anyString(), anyString())).thenReturn(false);
        when(businessService.getGradebookAssignments(anyString(), anyString(), any(SortType.class)))
            .thenReturn(new ArrayList<>());
        when(businessService.getGradebookCategories(anyString(), anyString()))
            .thenReturn(new ArrayList<>());
        when(businessService.buildGradeMatrixForImportExport(anyString(), anyString(), any(), any()))
            .thenReturn(createMockStudentGradeInfo());
        when(businessService.isStudentNumberVisible(anyString())).thenReturn(true);
        when(serverConfigService.getBoolean(anyString(), anyBoolean())).thenReturn(false);
    }

    @Test
    public void testStudentNumberColumnInCustomExport() throws IOException {
        // Setup: Enable student number inclusion
        exportPanel.includeStudentNumber = true;
        exportPanel.includeStudentId = false;
        exportPanel.includeStudentName = false;
        
        // Test custom export includes student number when selected
        File customExportFile = exportPanel.buildFileForTesting(true);
        List<String> lines = Files.readAllLines(customExportFile.toPath());
        
        assertFalse("Custom export file should not be empty", lines.isEmpty());
        String header = lines.get(0);
        
        // Verify student number column is included in custom export
        assertTrue("Custom export should include Student Number column when selected", 
                  header.contains("Student Number"));
        
        // Verify data line includes student number
        if (lines.size() > 1) {
            String dataLine = lines.get(1);
            String[] dataCells = dataLine.split(",");
            String[] headerCells = header.split(",");
            
            // Find student number column index
            int studentNumberIndex = -1;
            for (int i = 0; i < headerCells.length; i++) {
                if (headerCells[i].contains("Student Number")) {
                    studentNumberIndex = i;
                    break;
                }
            }
            
            assertTrue("Student Number column should be found in header", studentNumberIndex >= 0);
            assertTrue("Data should have correct number of columns", dataCells.length > studentNumberIndex);
        }
        
        customExportFile.delete();
    }

    @Test
    public void testStudentNumberColumnInRegularExport() throws IOException {
        // Setup: Enable student number inclusion
        exportPanel.includeStudentNumber = true;
        
        // Test regular export includes student number
        File regularExportFile = exportPanel.buildFileForTesting(false);
        List<String> lines = Files.readAllLines(regularExportFile.toPath());
        
        assertFalse("Regular export file should not be empty", lines.isEmpty());
        String header = lines.get(0);
        
        // Verify student number column is included in regular export
        assertTrue("Regular export should include Student Number column", 
                  header.contains("Student Number"));
        
        regularExportFile.delete();
    }

    @Test
    public void testEmptyGradebookSingleIgnoreColumn() throws IOException {
        // Setup: Empty gradebook with no assignments
        when(businessService.getGradebookAssignments(anyString(), anyString(), any(SortType.class)))
            .thenReturn(new ArrayList<>());
        
        exportPanel.includeGradeItemScores = true;
        exportPanel.includeGradeItemComments = true;
        
        // Test regular export with empty gradebook
        File exportFile = exportPanel.buildFileForTesting(false);
        List<String> lines = Files.readAllLines(exportFile.toPath());
        
        assertFalse("Export file should not be empty", lines.isEmpty());
        String header = lines.get(0);
        String[] headerCells = header.split(",");
        
        // Count ignore columns (those starting with #)
        long ignoreColumnCount = Arrays.stream(headerCells)
            .filter(cell -> cell.trim().startsWith("#"))
            .count();
        
        assertEquals("Empty gradebook should have exactly one ignore column", 1, ignoreColumnCount);
        
        // Verify column alignment - header count should match data count
        if (lines.size() > 1) {
            String dataLine = lines.get(1);
            String[] dataCells = dataLine.split(",", -1); // -1 to include empty trailing cells
            
            assertEquals("Header column count should match data column count", 
                        headerCells.length, dataCells.length);
        }
        
        exportFile.delete();
    }

    @Test
    public void testGradebookWithAssignmentsSingleIgnoreColumn() throws IOException {
        // Setup: Gradebook with assignments
        List<Assignment> assignments = createMockAssignments();
        when(businessService.getGradebookAssignments(anyString(), anyString(), any(SortType.class)))
            .thenReturn(assignments);
        
        exportPanel.includeGradeItemScores = true;
        exportPanel.includeGradeItemComments = true;
        
        // Test regular export with assignments
        File exportFile = exportPanel.buildFileForTesting(false);
        List<String> lines = Files.readAllLines(exportFile.toPath());
        
        assertFalse("Export file should not be empty", lines.isEmpty());
        String header = lines.get(0);
        String[] headerCells = header.split(",");
        
        // Count ignore columns (those starting with #)
        long ignoreColumnCount = Arrays.stream(headerCells)
            .filter(cell -> cell.trim().startsWith("#"))
            .count();
        
        assertEquals("Gradebook with assignments should have exactly one ignore column", 1, ignoreColumnCount);
        
        // Verify column alignment
        if (lines.size() > 1) {
            String dataLine = lines.get(1);
            String[] dataCells = dataLine.split(",", -1);
            
            assertEquals("Header column count should match data column count", 
                        headerCells.length, dataCells.length);
        }
        
        exportFile.delete();
    }

    @Test
    public void testGradeOverrideColumnPositioning() throws IOException {
        // Setup: Custom export with course grade and grade override enabled
        exportPanel.includeCourseGrade = true;
        exportPanel.includeCalculatedGrade = true;
        exportPanel.includeGradeOverride = true;
        
        File exportFile = exportPanel.buildFileForTesting(true);
        List<String> lines = Files.readAllLines(exportFile.toPath());
        
        assertFalse("Export file should not be empty", lines.isEmpty());
        String header = lines.get(0);
        String[] headerCells = header.split(",");
        
        // Find positions of course grade and grade override columns
        int courseGradeIndex = -1;
        int gradeOverrideIndex = -1;
        
        for (int i = 0; i < headerCells.length; i++) {
            String cell = headerCells[i].trim();
            if (cell.contains("Course Grade") && !cell.contains("Override")) {
                courseGradeIndex = i;
            }
            if (cell.contains("Grade Override")) {
                gradeOverrideIndex = i;
            }
        }
        
        assertTrue("Course Grade column should be found", courseGradeIndex >= 0);
        assertTrue("Grade Override column should be found", gradeOverrideIndex >= 0);
        assertTrue("Grade Override should come after Course Grade", gradeOverrideIndex > courseGradeIndex);
        
        exportFile.delete();
    }

    @Test
    public void testCustomExportOnlyIncludesSelectedColumns() throws IOException {
        // Setup: Only enable specific columns
        exportPanel.includeStudentId = true;
        exportPanel.includeStudentName = false;
        exportPanel.includeStudentNumber = true;
        exportPanel.includeGradeItemScores = false;
        exportPanel.includeGradeItemComments = false;
        exportPanel.includeCourseGrade = true;
        exportPanel.includeGradeOverride = false;
        
        File exportFile = exportPanel.buildFileForTesting(true);
        List<String> lines = Files.readAllLines(exportFile.toPath());
        
        assertFalse("Export file should not be empty", lines.isEmpty());
        String header = lines.get(0);
        
        // Verify only selected columns are present
        assertTrue("Should include Student ID", header.contains("Student ID"));
        assertFalse("Should not include Student Name", header.contains("Name"));
        assertTrue("Should include Student Number", header.contains("Student Number"));
        assertTrue("Should include Course Grade", header.contains("Course Grade"));
        assertFalse("Should not include Grade Override", header.contains("Grade Override"));
        
        exportFile.delete();
    }

    private List<GbStudentGradeInfo> createMockStudentGradeInfo() {
        List<GbStudentGradeInfo> students = new ArrayList<>();
        
        GbUser user = new GbUser();
        user.setUserUuid("user1");
        user.setDisplayId("student1");
        user.setFirstName("John");
        user.setLastName("Doe");
        
        GbStudentGradeInfo gradeInfo = new GbStudentGradeInfo(user);
        gradeInfo.setStudentNumber("12345");
        
        CourseGradeTransferBean courseGrade = new CourseGradeTransferBean();
        courseGrade.setMappedGrade("A");
        courseGrade.setCalculatedGrade("89.5");
        courseGrade.setEnteredGrade("A-");
        courseGrade.setPointsEarned(179.0);
        
        gradeInfo.setCourseGrade(courseGrade);
        gradeInfo.setSections(Arrays.asList("Section 1"));
        
        students.add(gradeInfo);
        return students;
    }

    private List<Assignment> createMockAssignments() {
        List<Assignment> assignments = new ArrayList<>();
        
        Assignment assignment = mock(Assignment.class);
        when(assignment.getId()).thenReturn(1L);
        when(assignment.getName()).thenReturn("Test Assignment");
        when(assignment.getPoints()).thenReturn(100.0);
        when(assignment.getExternallyMaintained()).thenReturn(false);
        when(assignment.getCategoryId()).thenReturn(null);
        
        assignments.add(assignment);
        return assignments;
    }
}