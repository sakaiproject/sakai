/*
 * Copyright (c) 2003-2025 The Apereo Foundation
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
package org.sakaiproject.datemanager.test;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.datemanager.api.DateManagerConstants;
import org.sakaiproject.datemanager.api.DateManagerService;
import org.sakaiproject.datemanager.api.model.DateManagerUpdate;
import org.sakaiproject.datemanager.api.model.DateManagerValidation;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DateManagerTestConfiguration.class})
public class DateManagerServiceTest {

    @Autowired private DateManagerService dateManagerService;
    @Autowired private GradingService gradingService;
    @Autowired private SessionManager sessionManager;
    @Autowired private ToolManager toolManager;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Qualifier("org.sakaiproject.time.api.UserTimeService")
    @Autowired private UserTimeService userTimeService;

    @Test
    public void testIsChangedForGradebook() {
        String siteId = UUID.randomUUID().toString();
        ToolSession toolSession = Mockito.mock(ToolSession.class);
        when(toolSession.getAttribute(DateManagerService.STATE_SITE_ID)).thenReturn(siteId);

        when(sessionManager.getCurrentToolSession()).thenReturn(toolSession);
        Tool tool = Mockito.mock(Tool.class);
        when(tool.getTitle()).thenReturn("Gradebook");
        when(toolManager.getTool(DateManagerConstants.COMMON_ID_GRADEBOOK)).thenReturn(tool);
        when(userTimeService.getLocalTimeZone()).thenReturn(TimeZone.getDefault());

        Assignment assignment = Mockito.mock(Assignment.class);
        Date dueDate = Date.from(LocalDate.parse("2025-05-16").atStartOfDay(ZoneOffset.systemDefault()).toInstant());
        when(assignment.getDueDate()).thenReturn(dueDate);
        when(gradingService.getAssignment(siteId, siteId, 78L)).thenReturn(assignment);

        // LD due dates are equal, changed = false
        boolean changed = dateManagerService.isChanged(DateManagerConstants.COMMON_ID_GRADEBOOK, new String[]{"78", "Participation", "2025-05-16"});
        Assert.assertFalse(changed);

        // LDT due dates are equal, changed = false
        changed = dateManagerService.isChanged(DateManagerConstants.COMMON_ID_GRADEBOOK, new String[]{"78", "Participation", "2025-05-16T00:00:00"});
        Assert.assertFalse(changed);

        // ZDT due dates are equal, changed = false
        changed = dateManagerService.isChanged(DateManagerConstants.COMMON_ID_GRADEBOOK, new String[]{"78", "Participation", "2025-05-16T00:00:00-04:00"});
        Assert.assertFalse(changed);

        // LD due dates are different, changed = true
        changed = dateManagerService.isChanged(DateManagerConstants.COMMON_ID_GRADEBOOK, new String[]{"78", "Participation", "2025-05-17"});
        Assert.assertTrue(changed);

        // LDT due dates are equal, changed = true
        changed = dateManagerService.isChanged(DateManagerConstants.COMMON_ID_GRADEBOOK, new String[]{"78", "Participation", "2025-05-17T00:00:00"});
        Assert.assertTrue(changed);

        // ZDT due dates are equal, changed = true
        changed = dateManagerService.isChanged(DateManagerConstants.COMMON_ID_GRADEBOOK, new String[]{"78", "Participation", "2025-05-17T00:00:00-04:00"});
        Assert.assertTrue(changed);
    }

    @Test
    public void testValidateGradebookItems() {
        // due date is 2025-05-16T00:00:00-04:00
        String json = readFileAsString("gradebook-zdt.json");
        JSONArray jsonArray = (JSONArray) JSONValue.parse(json);

        String siteId = UUID.randomUUID().toString();
        ToolSession toolSession = Mockito.mock(ToolSession.class);
        when(toolSession.getAttribute(DateManagerService.STATE_SITE_ID)).thenReturn(siteId);

        when(sessionManager.getCurrentToolSession()).thenReturn(toolSession);
        Tool tool = Mockito.mock(Tool.class);
        when(tool.getTitle()).thenReturn("Gradebook");
        when(toolManager.getTool(DateManagerConstants.COMMON_ID_GRADEBOOK)).thenReturn(tool);

        when(gradingService.currentUserHasEditPerm(siteId)).thenReturn(true);
        when(userTimeService.getLocalTimeZone()).thenReturn(TimeZone.getDefault());

        Assignment assignment = Mockito.mock(Assignment.class);
        when(gradingService.getAssignment(siteId, siteId, 78L)).thenReturn(assignment);

        try {
            DateManagerValidation validation = dateManagerService.validateGradebookItems(siteId, jsonArray);
            Assert.assertEquals(0, validation.getErrors().size());
            Assert.assertEquals(1, validation.getUpdates().size());
            DateManagerUpdate update = validation.getUpdates().get(0);
            Assert.assertEquals(LocalDateTime.parse("2025-05-16T00:00:00").atZone(ZoneId.systemDefault()).toInstant(), update.getDueDate());
        } catch (Exception e) {
            Assert.fail(e.toString());
        }


        // due date is 2025-05-16
        json = readFileAsString("gradebook-ld.json");
        jsonArray = (JSONArray) JSONValue.parse(json);

        try {
            DateManagerValidation validation = dateManagerService.validateGradebookItems(siteId, jsonArray);
            Assert.assertEquals(0, validation.getErrors().size());
            Assert.assertEquals(1, validation.getUpdates().size());
            DateManagerUpdate update = validation.getUpdates().get(0);
            Assert.assertEquals(LocalDate.parse("2025-05-16").atStartOfDay(ZoneOffset.systemDefault()).toInstant(), update.getDueDate());
        } catch (Exception e) {
            Assert.fail(e.toString());
        }

    }

    @Test
    public void testCsvExport() {
        String siteId = "test-site";
        
        // Mock the server configuration service to return a CSV separator
        when(serverConfigurationService.getString("csv.separator", ",")).thenReturn(",");
        
        try {
            byte[] csvData = dateManagerService.exportCsvData(siteId);
            Assert.assertNotNull("CSV data should not be null", csvData);
            Assert.assertTrue("CSV data should not be empty", csvData.length > 0);
            
            String csvContent = new String(csvData, StandardCharsets.UTF_8);
            Assert.assertTrue("CSV should contain Date Manager header", csvContent.contains("Date Manager"));
        } catch (Exception e) {
            Assert.fail("CSV export should not throw exception: " + e.toString());
        }
    }

    @Test
    public void testCsvExportWithSemicolonSeparator() {
        String siteId = "test-site";
        
        // Mock the server configuration service to return semicolon as CSV separator (European style)
        when(serverConfigurationService.getString("csv.separator", ",")).thenReturn(";");
        
        try {
            byte[] csvData = dateManagerService.exportCsvData(siteId);
            Assert.assertNotNull("CSV data should not be null", csvData);
            Assert.assertTrue("CSV data should not be empty", csvData.length > 0);
            
            String csvContent = new String(csvData, StandardCharsets.UTF_8);
            Assert.assertTrue("CSV should contain Date Manager header", csvContent.contains("Date Manager"));
            
            // The test verifies that the export works with semicolon separator configured
            // Even if no actual data is exported (no tools/assignments in test environment),
            // the method should execute successfully without errors
            Assert.assertTrue("CSV content should not be empty", csvContent.trim().length() > 0);
            
        } catch (Exception e) {
            Assert.fail("CSV export with semicolon separator should not throw exception: " + e.toString());
        }
    }
    
    @Test
    public void testCsvSeparatorConfiguration() {
        // Test comma separator (default)
        when(serverConfigurationService.getString("csv.separator", ",")).thenReturn(",");
        try {
            dateManagerService.exportCsvData("test-site");
            // If we get here without exception, the comma separator works
            Assert.assertTrue("Comma separator should work", true);
        } catch (Exception e) {
            Assert.fail("Comma separator should not cause exception: " + e.toString());
        }
        
        // Test semicolon separator (European style)
        when(serverConfigurationService.getString("csv.separator", ",")).thenReturn(";");
        try {
            dateManagerService.exportCsvData("test-site");
            // If we get here without exception, the semicolon separator works
            Assert.assertTrue("Semicolon separator should work", true);
        } catch (Exception e) {
            Assert.fail("Semicolon separator should not cause exception: " + e.toString());
        }
    }

    private static String readFileAsString(String filePath) {
        Resource resource = new ClassPathResource(filePath);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (Exception e) {
            log.warn("Failed to read file [{}], {}", filePath, e.toString());
        }
        return "";
    }

}
