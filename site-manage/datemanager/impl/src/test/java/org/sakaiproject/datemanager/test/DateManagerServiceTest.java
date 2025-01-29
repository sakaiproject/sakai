package org.sakaiproject.datemanager.test;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
        when(gradingService.getAssignment(siteId, 78L)).thenReturn(assignment);

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
        when(gradingService.getAssignment(siteId, 78L)).thenReturn(assignment);

        try {
            DateManagerValidation validation = dateManagerService.validateGradebookItems(siteId, jsonArray);
            Assert.assertEquals(0, validation.getErrors().size());
            Assert.assertEquals(1, validation.getUpdates().size());
            DateManagerUpdate update = validation.getUpdates().get(0);
            Assert.assertEquals(ZonedDateTime.parse("2025-05-16T00:00:00-04:00").toInstant(), update.getDueDate());
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
