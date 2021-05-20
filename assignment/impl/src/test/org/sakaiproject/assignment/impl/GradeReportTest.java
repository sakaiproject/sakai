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

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;

import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.api.FormattedText;

import org.sakaiproject.util.ResourceLoader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.Assert;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AssignmentTestConfiguration.class})
public class GradeReportTest {

    @Autowired private FormattedText formattedText;
    @Autowired private SiteService siteService;
    @Autowired private UserDirectoryService userDirectoryService;

    private AssignmentService assignmentService;
    private ResourceLoader resourceLoader;
    private Site site;
    private GradeSheetExporter exporter;
    private Assignment assOne;
    private Assignment assTwo;
    private List<Assignment> assignments;
    private String noSub = "No Submission";

    @Before
    public void setUp() {

        resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.getLocale()).thenReturn(Locale.ENGLISH);
        when(resourceLoader.getString("download.spreadsheet.column.userid")).thenReturn("User Id");
        when(resourceLoader.getString("listsub.nosub")).thenReturn(noSub);

        site = mock(Site.class);
        when(site.getTitle()).thenReturn("XYZ");
        when(site.getReference()).thenReturn("/site/xyz");

        try {
            when(siteService.getSite("xyz")).thenReturn(site);
        } catch (Exception e) {
            Assert.fail("Failed to setup site service mock");
        }

        assignments = new ArrayList<>();

        assOne = new Assignment();
        assOne.setTypeOfGrade(Assignment.GradeType.SCORE_GRADE_TYPE);
        assOne.setScaleFactor(1);
        assOne.setMaxGradePoint(10);
        assOne.setTitle("Assignment One");
        assOne.setDraft(false);
        assOne.setId("one");
        assOne.setContext("xyz");
        assOne.setDueDate(Instant.now().minus(20, ChronoUnit.HOURS));
        assignments.add(assOne);

        assTwo = new Assignment();
        assTwo.setScaleFactor(1);
        assTwo.setMaxGradePoint(10);
        assTwo.setTypeOfGrade(Assignment.GradeType.SCORE_GRADE_TYPE);
        assTwo.setTitle("Assignment Two");
        assTwo.setDraft(false);
        assTwo.setId("two");
        assTwo.setContext("xyz");
        assTwo.setDueDate(Instant.now().minus(10, ChronoUnit.HOURS));
        assignments.add(assTwo);


        assignmentService = (AssignmentService) mock(AssignmentService.class);

        exporter = new GradeSheetExporter();
        exporter.setSiteService(siteService);
        exporter.setAssignmentService(assignmentService);
        exporter.setUserDirectoryService(userDirectoryService);
        exporter.setRb(resourceLoader);
        exporter.setFormattedText(formattedText);
    }

    @Test
    public void noAssignments() {

        Optional<Workbook> optionalWorkbook = exporter.getGradesSpreadsheet("contextString=xyz&viewString=all");
        Assert.assertFalse("You should not get a workbook, if there are no assignments for the contextString", optionalWorkbook.isPresent());
    }

    @Test
    public void noSubmissions() {

        when(assignmentService.allowGradeSubmission(anyString())).thenReturn(true);

        when(assignmentService.getAssignmentsForContext("xyz")).thenReturn(assignments);

        Optional<Workbook> optionalWorkbook = exporter.getGradesSpreadsheet("contextString=xyz&viewString=all");

        Assert.assertTrue("You should still get a spreadsheet even with no submissions", optionalWorkbook.isPresent());

        Workbook workbook = optionalWorkbook.get();

        Assert.assertEquals("There should only be 1 sheet", 1, workbook.getNumberOfSheets());

        Sheet sheet = workbook.getSheetAt(0);

        Assert.assertEquals("There should be 6 rows when there are no submissions.", 6, sheet.getPhysicalNumberOfRows());

        //Name,User Id,Assignment,Grade,Scale,Submitted,Late
        Assert.assertEquals("There should be 7 columns in the header row, with two assignments", 7, sheet.getRow(5).getPhysicalNumberOfCells());
    }

    @Test
    public void oneSubmission() {

        String userSortName = "Alfred Hitchcock";
        String userDisplayId = "ahitchcock";

        when(assignmentService.allowGradeSubmission(anyString())).thenReturn(true);

        when(assignmentService.getAssignmentsForContext("xyz")).thenReturn(assignments);

        List<String> submissionUsers = new ArrayList<>();
        submissionUsers.add("user1");

        User user1 = mock(User.class);
        when(user1.getId()).thenReturn("u1");
        when(user1.getSortName()).thenReturn(userSortName);
        when(user1.getDisplayId()).thenReturn(userDisplayId);

        List<User> members = new ArrayList<>();
        members.add(user1);

        when(userDirectoryService.getUsers(anyList())).thenReturn(members);

        AssignmentSubmission user1SubOne = new AssignmentSubmission();
        user1SubOne.setId("subOne");
        user1SubOne.setGrade("34");
        user1SubOne.setGraded(true);
        user1SubOne.setAssignment(assOne);
        user1SubOne.setDateSubmitted(Instant.now().minus(30, ChronoUnit.HOURS));

        Set<AssignmentSubmission> assOneSubmissions = new HashSet<>();
        assOneSubmissions.add(user1SubOne);

        AssignmentSubmissionSubmitter user1Submitter = new AssignmentSubmissionSubmitter();
        user1Submitter.setSubmitter("u1");
        user1SubOne.getSubmitters().add(user1Submitter);

        assOne.getSubmissions().add(user1SubOne);

        when(assignmentService.getSubmissions(assOne)).thenReturn(assOneSubmissions);

        when(assignmentService.getGradeForSubmitter(user1SubOne, "u1")).thenReturn(user1SubOne.getGrade());

        Optional<Workbook> optionalWorkbook = exporter.getGradesSpreadsheet("contextString=xyz&viewString=all");

        Assert.assertTrue("You should have a spreadsheet", optionalWorkbook.isPresent());

        Sheet sheet = optionalWorkbook.get().getSheetAt(0);

        Assert.assertEquals("There should be 8 rows when there is one submission.", 8, sheet.getPhysicalNumberOfRows());

        Row submissionRow = sheet.getRow(6);

        //Name,User Id,Assignment,Grade,Scale,Submitted,Late
        Assert.assertEquals("There should be 7 columns in the submission row, with two assignments", 7, submissionRow.getPhysicalNumberOfCells());

        Cell cell = submissionRow.getCell(0);
        Assert.assertEquals(userSortName, cell.getStringCellValue());

        cell = submissionRow.getCell(1);
        Assert.assertEquals(userDisplayId, cell.getStringCellValue());

        cell = submissionRow.getCell(2);
        Assert.assertEquals(assOne.getTitle(), cell.getStringCellValue());

        cell = submissionRow.getCell(3);
        Assert.assertEquals(user1SubOne.getGrade(), cell.getStringCellValue());

        cell = submissionRow.getCell(5);
        Assert.assertEquals(Date.from(user1SubOne.getDateSubmitted()), cell.getDateCellValue());

        cell = submissionRow.getCell(6);
        Assert.assertEquals(false, cell.getBooleanCellValue());

        submissionRow = sheet.getRow(7);

        cell = submissionRow.getCell(0);
        Assert.assertEquals(userSortName, cell.getStringCellValue());

        cell = submissionRow.getCell(1);
        Assert.assertEquals(userDisplayId, cell.getStringCellValue());

        cell = submissionRow.getCell(2);
        Assert.assertEquals(assTwo.getTitle(), cell.getStringCellValue());

    }
}
