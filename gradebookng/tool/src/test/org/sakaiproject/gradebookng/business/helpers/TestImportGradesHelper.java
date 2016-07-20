package org.sakaiproject.gradebookng.business.helpers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.model.ImportColumn;
import org.sakaiproject.gradebookng.business.model.ImportedGrade;
import org.sakaiproject.gradebookng.business.model.ImportedGradeItem;
import org.sakaiproject.gradebookng.business.model.ImportedGradeWrapper;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItemStatus;
import org.sakaiproject.gradebookng.business.util.ImportGradesHelper;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.user.api.User;

/**
 * Created by chmaurer on 1/24/15.
 */
public class TestImportGradesHelper {

	private Map<String, String> userMap() {
		final Map<String, String> userMap = new HashMap<String, String>();
		userMap.put("student1", "student1");
		userMap.put("student2", "student2");
		return userMap;
	}

	@Test
	public void testCsvImport() throws Exception {
		final InputStream is = this.getClass().getClassLoader().getResourceAsStream("grades_import.csv");
		final ImportedGradeWrapper importedGradeWrapper = ImportGradesHelper.parseCsv(is, userMap());
		is.close();

		testImport(importedGradeWrapper);
	}

	@Test
	public void testXlsImport() throws Exception {
		final InputStream is = this.getClass().getClassLoader().getResourceAsStream("grades_import.xls");
		final ImportedGradeWrapper importedGradeWrapper = ImportGradesHelper.parseXls(is, userMap());
		is.close();

		testImport(importedGradeWrapper);
	}

	private void testImport(final ImportedGradeWrapper importedGradeWrapper) {
		Assert.assertNotNull(importedGradeWrapper);

		final List<ImportedGrade> importedGrades = importedGradeWrapper.getImportedGrades();

		Assert.assertNotNull(importedGrades);
		Assert.assertEquals("unexpected list size", 2, importedGrades.size());

		Assert.assertNotNull(importedGrades.get(0).getGradeItemMap());
		Assert.assertEquals(2, importedGrades.get(0).getGradeItemMap().size());

		final ImportedGradeItem item11 = importedGrades.get(0).getGradeItemMap().get("a1");
		Assert.assertEquals("comments don't match", "graded", item11.getGradeItemComment());
		Assert.assertEquals("scores don't match", "7", item11.getGradeItemScore());

		final ImportedGradeItem item12 = importedGrades.get(0).getGradeItemMap().get("food");
		Assert.assertEquals("comments don't match", "null", item12.getGradeItemComment());
		Assert.assertEquals("scores don't match", "null", item12.getGradeItemScore());

		final ImportedGradeItem item21 = importedGrades.get(1).getGradeItemMap().get("a1");
		Assert.assertEquals("comments don't match", "interesting work", item21.getGradeItemComment());
		Assert.assertEquals("scores don't match", "3", item21.getGradeItemScore());

		final ImportedGradeItem item22 = importedGrades.get(1).getGradeItemMap().get("food");
		Assert.assertEquals("comments don't match", "I'm hungry", item22.getGradeItemComment());
		Assert.assertEquals("scores don't match", "42", item22.getGradeItemScore());
	}

	@Test
	public void testProcessImportedGrades() {
		final List<Assignment> assignments = mockAssignments();
		final List<GbStudentGradeInfo> grades = mockStudentGrades();
		final ImportedGradeWrapper importedGradeWrapper = mockImportedGrades();

		final List<ProcessedGradeItem> processedGradeItems = ImportGradesHelper.processImportedGrades(importedGradeWrapper, assignments,
				grades);

		Assert.assertNotNull(processedGradeItems);

		Assert.assertEquals("wrong number of results", 4, processedGradeItems.size());

		// assignment 1
		Assert.assertEquals("wrong status", ProcessedGradeItemStatus.STATUS_NA, processedGradeItems.get(0).getStatus().getStatusCode());
		Assert.assertEquals("wrong status", ProcessedGradeItemStatus.STATUS_NA,
				processedGradeItems.get(0).getCommentStatus().getStatusCode());

		// assignment 2
		Assert.assertEquals("wrong status", ProcessedGradeItemStatus.STATUS_UPDATE, processedGradeItems.get(1).getStatus().getStatusCode());
		Assert.assertEquals("wrong status", ProcessedGradeItemStatus.STATUS_UPDATE,
				processedGradeItems.get(1).getCommentStatus().getStatusCode());

		// assignment 3
		Assert.assertEquals("wrong status", ProcessedGradeItemStatus.STATUS_NEW, processedGradeItems.get(2).getStatus().getStatusCode());
		Assert.assertEquals("wrong status", ProcessedGradeItemStatus.STATUS_NEW,
				processedGradeItems.get(2).getCommentStatus().getStatusCode());

		// assignment ext
		final ProcessedGradeItemStatus extStatus = processedGradeItems.get(3).getStatus();
		Assert.assertEquals("wrong status", ProcessedGradeItemStatus.STATUS_EXTERNAL, extStatus.getStatusCode());
		Assert.assertEquals("wrong status name", "From a test", extStatus.getStatusValue());

	}

	/**
	 * Mock up some assignment data
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
	 * Mock up some student grade data
	 * 
	 * @return
	 */
	private List<GbStudentGradeInfo> mockStudentGrades() {
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

	private ImportedGradeWrapper mockImportedGrades() {
		final ImportedGradeWrapper importedGradeWrapper = new ImportedGradeWrapper();
		final List<ImportColumn> columns = new ArrayList<ImportColumn>();
		columns.add(new ImportColumn("Student ID", null, ImportColumn.TYPE_REGULAR));
		columns.add(new ImportColumn("Student Name", null, ImportColumn.TYPE_REGULAR));
		columns.add(new ImportColumn("Assignment 1", "10.0", ImportColumn.TYPE_ITEM_WITH_POINTS));
		columns.add(new ImportColumn("Assignment 1", "N/A", ImportColumn.TYPE_ITEM_WITH_COMMENTS));
		columns.add(new ImportColumn("Assignment 2", "10.0", ImportColumn.TYPE_ITEM_WITH_POINTS));
		columns.add(new ImportColumn("Assignment 2", "N/A", ImportColumn.TYPE_ITEM_WITH_COMMENTS));
		columns.add(new ImportColumn("Assignment 3", "100.0", ImportColumn.TYPE_ITEM_WITH_POINTS));
		columns.add(new ImportColumn("Assignment 3", "N/A", ImportColumn.TYPE_ITEM_WITH_COMMENTS));
		columns.add(new ImportColumn("Assignment Ext", "1000.0", ImportColumn.TYPE_ITEM_WITH_POINTS));

		importedGradeWrapper.setColumns(columns);

		final List<ImportedGrade> importedGrades = new ArrayList<ImportedGrade>();
		final ImportedGrade importedGrade1 = new ImportedGrade();
		importedGrade1.setStudentUuid("user1");
		importedGrade1.setStudentEid("user1");
		importedGrade1.setStudentName("User 1");
		final Map<String, ImportedGradeItem> gradeMap1 = new HashMap<>();

		gradeMap1.put("Assignment 1", new ImportedGradeItem("Assignment 1"));
		gradeMap1.get("Assignment 1").setGradeItemComment("comment 1");
		gradeMap1.get("Assignment 1").setGradeItemScore("1");
		gradeMap1.put("Assignment 2", new ImportedGradeItem("Assignment 2"));
		gradeMap1.get("Assignment 2").setGradeItemComment("comment 2");
		gradeMap1.get("Assignment 2").setGradeItemScore("2");
		importedGrade1.setGradeItemMap(gradeMap1);
		importedGrades.add(importedGrade1);

		final ImportedGrade importedGrade2 = new ImportedGrade();
		importedGrade2.setStudentEid("user2");
		importedGrade2.setStudentUuid("user2");
		importedGrade2.setStudentName("User 2");
		final Map<String, ImportedGradeItem> gradeMap2 = new HashMap<>();

		gradeMap2.put("Assignment 1", new ImportedGradeItem("Assignment 1"));
		gradeMap2.get("Assignment 1").setGradeItemComment("comment 12");
		gradeMap2.get("Assignment 1").setGradeItemScore("5");
		gradeMap2.put("Assignment 2", new ImportedGradeItem("Assignment 2"));
		gradeMap2.get("Assignment 2").setGradeItemComment("comment 222");
		gradeMap2.get("Assignment 2").setGradeItemScore("3");
		importedGrade2.setGradeItemMap(gradeMap2);
		importedGrades.add(importedGrade2);

		final ImportedGrade importedGrade3 = new ImportedGrade();
		importedGrade3.setStudentEid("user3");
		importedGrade3.setStudentUuid("user3");
		importedGrade3.setStudentName("User 3");
		final Map<String, ImportedGradeItem> gradeMap3 = new HashMap<>();

		// gradeMap3.put("Assignment 1", new ImportedGradeItem("Assignment 1", "comment 13", "5"));
		gradeMap3.put("Assignment 2", new ImportedGradeItem("Assignment 2"));
		gradeMap3.get("Assignment 2").setGradeItemComment("comment 23");
		gradeMap3.get("Assignment 2").setGradeItemScore("6");
		gradeMap3.put("Assignment 3", new ImportedGradeItem("Assignment 3"));
		gradeMap3.get("Assignment 3").setGradeItemComment("comment 233");
		gradeMap3.get("Assignment 3").setGradeItemScore("7");
		importedGrade3.setGradeItemMap(gradeMap3);
		importedGrades.add(importedGrade3);

		importedGradeWrapper.setImportedGrades(importedGrades);

		return importedGradeWrapper;
	}
}
