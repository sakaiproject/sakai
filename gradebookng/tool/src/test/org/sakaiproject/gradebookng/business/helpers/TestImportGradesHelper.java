package org.sakaiproject.gradebookng.business.helpers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.sakaiproject.gradebookng.business.exception.GbImportExportInvalidFileTypeException;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.model.ImportedCell;
import org.sakaiproject.gradebookng.business.model.ImportedColumn;
import org.sakaiproject.gradebookng.business.model.ImportedRow;
import org.sakaiproject.gradebookng.business.model.ImportedSpreadsheetWrapper;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItemStatus;
import org.sakaiproject.gradebookng.business.util.ImportGradesHelper;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.user.api.User;

import lombok.extern.slf4j.Slf4j;

/**
 * Tests for the ImportGradesHelper class.
 */
@Slf4j
public class TestImportGradesHelper {


	@Test
	public void when_textcsv_thenCsvImportSucceeds() throws Exception {
		final InputStream is = this.getClass().getClassLoader().getResourceAsStream("grades_import.csv");
		final ImportedSpreadsheetWrapper importedSpreadsheetWrapper = ImportGradesHelper.parseImportedGradeFile(is, "text/csv", "grades_import.csv", mockUserMap());
		is.close();
		testImport(importedSpreadsheetWrapper);
	}

	@Test
	public void when_textplain_thenCsvImportSucceeds() throws Exception {
		final InputStream is = this.getClass().getClassLoader().getResourceAsStream("grades_import.csv");
		final ImportedSpreadsheetWrapper importedSpreadsheetWrapper = ImportGradesHelper.parseImportedGradeFile(is, "text/plain", "grades_import.csv", mockUserMap());
		is.close();
		testImport(importedSpreadsheetWrapper);
	}

	@Test
	public void when_textcommaseparatedvalues_thenCsvImportSucceeds() throws Exception {
		final InputStream is = this.getClass().getClassLoader().getResourceAsStream("grades_import.csv");
		final ImportedSpreadsheetWrapper importedSpreadsheetWrapper = ImportGradesHelper.parseImportedGradeFile(is, "text/comma-separated-values", "grades_import.csv", mockUserMap());
		is.close();
		testImport(importedSpreadsheetWrapper);
	}

	@Test
	public void when_textapplicationcsv_thenCsvImportSucceeds() throws Exception {
		final InputStream is = this.getClass().getClassLoader().getResourceAsStream("grades_import.csv");
		final ImportedSpreadsheetWrapper importedSpreadsheetWrapper = ImportGradesHelper.parseImportedGradeFile(is, "application/csv", "grades_import.csv", mockUserMap());
		is.close();
		testImport(importedSpreadsheetWrapper);
	}

	@Test
	public void when_browser_says_applicationvndmsexcel_thenCsvImportSucceeds() throws Exception {
		final InputStream is = this.getClass().getClassLoader().getResourceAsStream("grades_import.csv");
		// Windows machine with MS Office installed is going to send this CSV with an Excel mimetype
		final ImportedSpreadsheetWrapper importedSpreadsheetWrapper = ImportGradesHelper.parseImportedGradeFile(is, "application/vnd.ms-excel", "grades_import.csv", mockUserMap());
		is.close();
		testImport(importedSpreadsheetWrapper);
	}

	@Test
	public void when_applicationvndmsexcel_thenXlsImportSucceeds() throws Exception {
		final InputStream is = this.getClass().getClassLoader().getResourceAsStream("grades_import.xls");
		final ImportedSpreadsheetWrapper importedSpreadsheetWrapper = ImportGradesHelper.parseImportedGradeFile(is, "application/vnd.ms-excel", "grades_import.xls", mockUserMap());
		is.close();
		testImport(importedSpreadsheetWrapper);
	}

	@Test
	public void when_applicationvndopenxmlformatsofficedocumentspreadsheetmlsheet_thenXlsImportSucceeds() throws Exception {
		final InputStream is = this.getClass().getClassLoader().getResourceAsStream("grades_import.xls");
		final ImportedSpreadsheetWrapper importedSpreadsheetWrapper = ImportGradesHelper.parseImportedGradeFile(is, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "grades_import.xls", mockUserMap());
		is.close();
		testImport(importedSpreadsheetWrapper);
	}

	@Test(expected=GbImportExportInvalidFileTypeException.class)
	public void when_anythingelse_thenImportFails() throws Exception {
		final InputStream is = this.getClass().getClassLoader().getResourceAsStream("grades_import.pdf");
		final ImportedSpreadsheetWrapper importedSpreadsheetWrapper = ImportGradesHelper.parseImportedGradeFile(is, "application/pdf", "grades_import.pdf", mockUserMap());
		is.close();
		testImport(importedSpreadsheetWrapper);
	}

	private void testImport(final ImportedSpreadsheetWrapper importedSpreadsheetWrapper) {
		Assert.assertNotNull(importedSpreadsheetWrapper);

		final List<ImportedRow> rows = importedSpreadsheetWrapper.getRows();

		Assert.assertNotNull(rows);
		Assert.assertEquals("unexpected list size", 2, rows.size());

		Assert.assertNotNull(rows.get(0).getCellMap());
		Assert.assertEquals(2, rows.get(0).getCellMap().size());

		final ImportedCell item11 = rows.get(0).getCellMap().get("a1");
		Assert.assertEquals("comments don't match", "graded", item11.getComment());
		Assert.assertEquals("scores don't match", "7", item11.getScore());

		final ImportedCell item12 = rows.get(0).getCellMap().get("food");
		Assert.assertEquals("comments don't match", "null", item12.getComment());
		Assert.assertEquals("scores don't match", "null", item12.getScore());

		final ImportedCell item21 = rows.get(1).getCellMap().get("a1");
		Assert.assertEquals("comments don't match", "interesting work", item21.getComment());
		Assert.assertEquals("scores don't match", "3", item21.getScore());

		final ImportedCell item22 = rows.get(1).getCellMap().get("food");
		Assert.assertEquals("comments don't match", "I'm hungry", item22.getComment());
		Assert.assertEquals("scores don't match", "42", item22.getScore());
	}

	/**
	 * This test is broken. The order of the lists needs to be adjusted. THe code is very convoluted.
	 * TODO fix this failing test
	 */
	@Ignore
	@Test
	public void testProcessImportedGrades() {
		final List<Assignment> assignments = mockAssignments();
		final List<GbStudentGradeInfo> existingGrades = mockExistingStudentGrades();
		final ImportedSpreadsheetWrapper importedSpreadsheetWrapper = mockImportedSpreadsheetData();

		final List<ProcessedGradeItem> processedGradeItems = ImportGradesHelper.processImportedGrades(importedSpreadsheetWrapper, assignments, existingGrades);

		Assert.assertNotNull(processedGradeItems);

		Assert.assertEquals("Wrong number of columns", 4, processedGradeItems.size());

		// assignment 1
		final ProcessedGradeItem item1 = processedGradeItems.get(0);
		Assert.assertEquals("Incorrect title: " + "Assignment 1", item1.getItemTitle());
		Assert.assertEquals("wrong status", ProcessedGradeItemStatus.STATUS_NA, item1.getStatus().getStatusCode());
		Assert.assertEquals("wrong status", ProcessedGradeItemStatus.STATUS_NA, item1.getCommentStatus().getStatusCode());

		// assignment 2
		final ProcessedGradeItem item2 = processedGradeItems.get(1);
		Assert.assertEquals("Incorrect title: " + "Assignment 2", item2.getItemTitle());
		Assert.assertEquals("wrong status", ProcessedGradeItemStatus.STATUS_MODIFIED, item2.getStatus().getStatusCode());
		Assert.assertEquals("wrong status", ProcessedGradeItemStatus.STATUS_UPDATE, item2.getCommentStatus().getStatusCode());

		// assignment 3
		// this does not exist in the mocked data so should be new
		final ProcessedGradeItem item3 = processedGradeItems.get(2);
		Assert.assertEquals("Incorrect title: " + "Assignment 3", item3.getItemTitle());
		Assert.assertEquals("wrong status", ProcessedGradeItemStatus.STATUS_NEW, item3.getStatus().getStatusCode());
		Assert.assertEquals("wrong status", ProcessedGradeItemStatus.STATUS_NEW, item3.getCommentStatus().getStatusCode());

		// assignment ext
		final ProcessedGradeItem item4 = processedGradeItems.get(3);
		Assert.assertEquals("Incorrect title: " + "Assignment Ext", item4.getItemTitle());
		Assert.assertEquals("wrong status", ProcessedGradeItemStatus.STATUS_EXTERNAL, item4.getStatus().getStatusCode());
		Assert.assertEquals("wrong status name", "From a test", item4.getStatus().getStatusValue());

	}

	/**
	 * Mock up some EXISTING assignment data
	 *
	 * @return List of mocked assignments
	 */
	private List<Assignment> mockAssignments() {
		final List<Assignment> assignments = new ArrayList<Assignment>();
		final Assignment assignment1 = new Assignment();
		assignment1.setId(1L);
		assignment1.setName("Assignment 1");
		assignment1.setPoints(10.0);
		assignments.add(assignment1);

		final Assignment assignment2 = new Assignment();
		assignment2.setId(2L);
		assignment2.setName("Assignment 2");
		assignment2.setPoints(100.0);
		assignments.add(assignment2);

		final Assignment assignment3 = new Assignment();
		assignment3.setId(3L);
		assignment3.setName("Assignment Ext");
		assignment3.setPoints(1000.0);
		assignment3.setExternalAppName("From a test");
		assignment3.setExternalId("ext_asdf");
		assignments.add(assignment3);

		return assignments;
	}

	/**
	 * Mock up some existing student grade data
	 *
	 * @return
	 */
	private List<GbStudentGradeInfo> mockExistingStudentGrades() {
		final List<GbStudentGradeInfo> grades = new ArrayList<GbStudentGradeInfo>();

		final User user1 = Mockito.mock(User.class);
		Mockito.when(user1.getId()).thenReturn("user1");
		Mockito.when(user1.getEid()).thenReturn("user1");
		final GbStudentGradeInfo studentGradeInfo1 = new GbStudentGradeInfo(user1);
		final GradeDefinition gradeDefinition1 = new GradeDefinition();
		gradeDefinition1.setGrade("1");
		gradeDefinition1.setGradeComment("comment 1");
		studentGradeInfo1.addGrade(1L, new GbGradeInfo(gradeDefinition1));
		grades.add(studentGradeInfo1);

		final GbStudentGradeInfo studentGradeInfo2 = new GbStudentGradeInfo(user1);
		final GradeDefinition gradeDefinition2 = new GradeDefinition();
		gradeDefinition2.setGrade("2");
		gradeDefinition2.setGradeComment("comment 2");
		studentGradeInfo2.addGrade(2L, new GbGradeInfo(gradeDefinition2));
		grades.add(studentGradeInfo2);

		final User user2 = Mockito.mock(User.class);
		Mockito.when(user2.getId()).thenReturn("user2");
		Mockito.when(user2.getEid()).thenReturn("user2");
		final GbStudentGradeInfo studentGradeInfo3 = new GbStudentGradeInfo(user2);
		final GradeDefinition gradeDefinition3 = new GradeDefinition();
		gradeDefinition3.setGrade("5");
		gradeDefinition3.setGradeComment("comment 12");
		studentGradeInfo3.addGrade(1L, new GbGradeInfo(gradeDefinition3));
		grades.add(studentGradeInfo3);

		final GbStudentGradeInfo studentGradeInfo4 = new GbStudentGradeInfo(user2);
		final GradeDefinition gradeDefinition4 = new GradeDefinition();
		gradeDefinition4.setGrade("6");
		gradeDefinition4.setGradeComment("comment 22");
		studentGradeInfo4.addGrade(2L, new GbGradeInfo(gradeDefinition4));
		grades.add(studentGradeInfo4);

		return grades;
	}

	private ImportedSpreadsheetWrapper mockImportedSpreadsheetData() {
		final ImportedSpreadsheetWrapper importedSpreadsheetWrapper = new ImportedSpreadsheetWrapper();
		final List<ImportedColumn> columns = new ArrayList<ImportedColumn>();

		// only list actual columns to be turned into the import here
		columns.add(new ImportedColumn("Student ID", null, ImportedColumn.Type.GB_ITEM_WITHOUT_POINTS));
		columns.add(new ImportedColumn("Student Name", null, ImportedColumn.Type.GB_ITEM_WITHOUT_POINTS));
		columns.add(new ImportedColumn("Assignment 1", "10.0", ImportedColumn.Type.GB_ITEM_WITH_POINTS));
		columns.add(new ImportedColumn("Assignment 1", "N/A", ImportedColumn.Type.COMMENTS));
		columns.add(new ImportedColumn("Assignment 2", "10.0", ImportedColumn.Type.GB_ITEM_WITH_POINTS));
		columns.add(new ImportedColumn("Assignment 2", "N/A", ImportedColumn.Type.COMMENTS));
		columns.add(new ImportedColumn("Assignment 3", "100.0", ImportedColumn.Type.GB_ITEM_WITH_POINTS));
		columns.add(new ImportedColumn("Assignment 3", "N/A", ImportedColumn.Type.COMMENTS));
		columns.add(new ImportedColumn("Assignment Ext", "1000.0", ImportedColumn.Type.GB_ITEM_WITH_POINTS));

		importedSpreadsheetWrapper.setColumns(columns);

		final List<ImportedRow> rows = new ArrayList<ImportedRow>();

		final ImportedRow row1 = new ImportedRow();
		row1.setStudentUuid("user1");
		row1.setStudentEid("user1");
		row1.setStudentName("User 1");
		final Map<String, ImportedCell> cellMap1 = new HashMap<>();

		cellMap1.put("Assignment 1", new ImportedCell());
		cellMap1.get("Assignment 1").setComment("comment 1");
		cellMap1.get("Assignment 1").setScore("1");
		cellMap1.put("Assignment 2", new ImportedCell());
		cellMap1.get("Assignment 2").setComment("comment 2");
		cellMap1.get("Assignment 2").setScore("2");
		row1.setCellMap(cellMap1);
		rows.add(row1);

		final ImportedRow row2 = new ImportedRow();
		row2.setStudentEid("user2");
		row2.setStudentUuid("user2");
		row2.setStudentName("User 2");
		final Map<String, ImportedCell> cellMap2 = new HashMap<>();

		cellMap2.put("Assignment 1", new ImportedCell());
		cellMap2.get("Assignment 1").setComment("comment 12");
		cellMap2.get("Assignment 1").setScore("5");
		cellMap2.put("Assignment 2", new ImportedCell());
		cellMap2.get("Assignment 2").setComment("comment 222");
		cellMap2.get("Assignment 2").setScore("3");
		row2.setCellMap(cellMap2);
		rows.add(row2);

		final ImportedRow row3 = new ImportedRow();
		row3.setStudentEid("user3");
		row3.setStudentUuid("user3");
		row3.setStudentName("User 3");
		final Map<String, ImportedCell> cellMap3 = new HashMap<>();

		cellMap3.put("Assignment 2", new ImportedCell());
		cellMap3.get("Assignment 2").setComment("comment 23");
		cellMap3.get("Assignment 2").setScore("6");
		cellMap3.put("Assignment 3", new ImportedCell());
		cellMap3.get("Assignment 3").setComment("comment 233");
		cellMap3.get("Assignment 3").setScore("7");
		row3.setCellMap(cellMap3);
		rows.add(row3);

		importedSpreadsheetWrapper.setRows(rows);

		return importedSpreadsheetWrapper;
	}

	private Map<String, String> mockUserMap() {
		final Map<String, String> userMap = new HashMap<String, String>();
		userMap.put("student1", "student name 1");
		userMap.put("student2", "student name 2");
		return userMap;
	}

}
