package org.sakaiproject.gradebookng.business.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.sakaiproject.gradebookng.business.exception.GbImportExportException;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.model.ImportColumn;
import org.sakaiproject.gradebookng.business.model.ImportedGrade;
import org.sakaiproject.gradebookng.business.model.ImportedGradeItem;
import org.sakaiproject.gradebookng.business.model.ImportedGradeWrapper;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItemDetail;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItemStatus;
import org.sakaiproject.gradebookng.tool.model.AssignmentStudentGradeInfo;
import org.sakaiproject.service.gradebook.shared.Assignment;

import au.com.bytecode.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;

/**
 * Helper to handling parsing and processing of an imported gradebook file
 */
@Slf4j
public class ImportGradesHelper {

	// column names we know about
	final static String IMPORT_USER_ID = "Student ID";
	final static String IMPORT_USER_NAME = "Student Name";
	final static String IMPORT_COURSE_GRADE = "Course Grade";

	// patterns for detecting column headers and their types
	final static Pattern ASSIGNMENT_WITH_POINTS_PATTERN = Pattern.compile("([\\w ]+ \\[[0-9]+(\\.[0-9][0-9]?)?\\])");
	final static Pattern ASSIGNMENT_COMMENT_PATTERN = Pattern.compile("(\\* [\\w ]+)");
	final static Pattern STANDARD_HEADER_PATTERN = Pattern.compile("([\\w ]+)");
	final static Pattern POINTS_PATTERN = Pattern.compile("(\\d+)(?=]$)");
	final static Pattern IGNORE_PATTERN = Pattern.compile("(\\#[\\w ]+)");

	/**
	 * Parse a CSV into a list of ImportedGrade objects. Returns list if ok, or null if error
	 *
	 * @param is InputStream of the data to parse
	 * @return
	 */
	public static ImportedGradeWrapper parseCsv(final InputStream is, final Map<String, String> userMap) {

		// manually parse method so we can support arbitrary columns
		final CSVReader reader = new CSVReader(new InputStreamReader(is));
		String[] nextLine;
		int lineCount = 0;
		final List<ImportedGrade> list = new ArrayList<ImportedGrade>();
		Map<Integer, ImportColumn> mapping = null;

		try {
			while ((nextLine = reader.readNext()) != null) {

				if (lineCount == 0) {
					// header row, capture it
					mapping = mapHeaderRow(nextLine);
				} else {
					// map the fields into the object
					list.add(mapLine(nextLine, mapping, userMap));
				}
				lineCount++;
			}
		} catch (final Exception e) {
			log.error("Error reading imported file: " + e.getClass() + " : " + e.getMessage());
			return null;
		} finally {
			try {
				reader.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		final ImportedGradeWrapper importedGradeWrapper = new ImportedGradeWrapper();
		importedGradeWrapper.setColumns(mapping.values());
		importedGradeWrapper.setImportedGrades(list);

		return importedGradeWrapper;
	}

	/**
	 * Parse an XLS into a list of ImportedGrade objects Note that only the first sheet of the Excel file is supported.
	 *
	 * @param is InputStream of the data to parse
	 * @return
	 */
	public static ImportedGradeWrapper parseXls(final InputStream is, final Map<String, String> userMap) {

		int lineCount = 0;
		final List<ImportedGrade> list = new ArrayList<ImportedGrade>();
		Map<Integer, ImportColumn> mapping = null;

		try {
			final Workbook wb = WorkbookFactory.create(is);
			final Sheet sheet = wb.getSheetAt(0);
			for (final Row row : sheet) {

				final String[] r = convertRow(row);

				if (lineCount == 0) {
					// header row, capture it
					mapping = mapHeaderRow(r);
				} else {
					// map the fields into the object
					list.add(mapLine(r, mapping, userMap));
				}
				lineCount++;
			}

		} catch (final Exception e) {
			// TOOD this shouldn't catch everything, it should continue throwing back up the stack
			log.error("Error reading imported file: " + e.getClass() + " : " + e.getMessage());
			return null;
		}

		final ImportedGradeWrapper importedGradeWrapper = new ImportedGradeWrapper();
		importedGradeWrapper.setColumns(mapping.values());
		importedGradeWrapper.setImportedGrades(list);
		return importedGradeWrapper;
	}

	/**
	 * Takes a row of data and maps it into the appropriate ImportedGrade pieces
	 *
	 * @param line
	 * @param mapping
	 * @return
	 */
	private static ImportedGrade mapLine(final String[] line, final Map<Integer, ImportColumn> mapping, final Map<String, String> userMap) {

		final ImportedGrade grade = new ImportedGrade();

		for (final Map.Entry<Integer, ImportColumn> entry : mapping.entrySet()) {

			final int i = entry.getKey();
			final ImportColumn importColumn = entry.getValue();

			// In case there aren't enough data fields in the line to match up with the number of columns needed
			String lineVal = null;
			if (i < line.length) {
				lineVal = trim(line[i]);
			}

			// process user id column
			if (StringUtils.equals(importColumn.getColumnTitle(), IMPORT_USER_ID)) {
				grade.setStudentEid(lineVal);
				grade.setStudentUuid(userMap.get(lineVal));

			// process user name column
			} else if (StringUtils.equals(importColumn.getColumnTitle(), IMPORT_USER_NAME)) {
				grade.setStudentName(lineVal);

			// process cours egrade column
			} else if (StringUtils.equals(importColumn.getColumnTitle(), IMPORT_COURSE_GRADE)) {
				ImportedGradeItem courseGradeItem = grade.getGradeItemMap().get(IMPORT_COURSE_GRADE);
				if (courseGradeItem == null) {
					courseGradeItem = new ImportedGradeItem(IMPORT_COURSE_GRADE);
				}
				courseGradeItem.setGradeItemScore(lineVal);
				grade.getGradeItemMap().put(IMPORT_COURSE_GRADE, courseGradeItem);

			//process item with points column
			} else if (ImportColumn.TYPE_ITEM_WITH_POINTS == importColumn.getType()) {
				final String assignmentName = importColumn.getColumnTitle();
				ImportedGradeItem importedGradeItem = grade.getGradeItemMap().get(assignmentName);
				if (importedGradeItem == null) {
					importedGradeItem = new ImportedGradeItem(assignmentName);
				}
				importedGradeItem.setGradeItemScore(lineVal);
				grade.getGradeItemMap().put(assignmentName, importedGradeItem);

			//process comments
			} else if (ImportColumn.TYPE_ITEM_WITH_COMMENTS == importColumn.getType()) {
				final String assignmentName = importColumn.getColumnTitle();
				ImportedGradeItem importedGradeItem = grade.getGradeItemMap().get(assignmentName);
				if (importedGradeItem == null) {
					importedGradeItem = new ImportedGradeItem(assignmentName);
				}
				importedGradeItem.setGradeItemComment(lineVal);
				grade.getGradeItemMap().put(assignmentName, importedGradeItem);

			} else {

			}
		}

		return grade;
	}

	public static List<ProcessedGradeItem> processImportedGrades(final ImportedGradeWrapper importedGradeWrapper,
			final List<Assignment> assignments, final List<GbStudentGradeInfo> currentGrades) {
		final List<ProcessedGradeItem> processedGradeItems = new ArrayList<ProcessedGradeItem>();
		final Map<String, Assignment> assignmentNameMap = new HashMap<String, Assignment>();
		final Map<String, ProcessedGradeItem> assignmentProcessedGradeItemMap = new HashMap<String, ProcessedGradeItem>();

		final Map<Long, AssignmentStudentGradeInfo> transformedGradeMap = transformCurrentGrades(currentGrades);

		// Map the assignment name back to the Id
		for (final Assignment assignment : assignments) {
			assignmentNameMap.put(assignment.getName(), assignment);
		}

		for (final ImportColumn column : importedGradeWrapper.getColumns()) {
			boolean needsAdded = false;
			final String assignmentName = StringUtils.trim(column.getColumnTitle()); // trim whitespace so we can match properly

			ProcessedGradeItem processedGradeItem = assignmentProcessedGradeItemMap.get(assignmentName);
			if (processedGradeItem == null) {
				processedGradeItem = new ProcessedGradeItem();
				needsAdded = true;
			}

			final Assignment assignment = assignmentNameMap.get(assignmentName);
			final ProcessedGradeItemStatus status = determineStatus(column, assignment, importedGradeWrapper, transformedGradeMap);

			if (column.getType() == ImportColumn.TYPE_ITEM_WITH_POINTS) {
				processedGradeItem.setItemTitle(assignmentName);
				processedGradeItem.setItemPointValue(column.getPoints());
				processedGradeItem.setStatus(status);
			} else if (column.getType() == ImportColumn.TYPE_ITEM_WITH_COMMENTS) {

				// internal only, no longer displayed, but used to link up the columns as it is unique
				processedGradeItem.setCommentLabel(assignmentName + " Comments");
				processedGradeItem.setCommentStatus(status);
			} else {
				// Just get out
				log.warn("Bad column type - " + column.getType() + ".  Skipping.");
				continue;
			}

			if (assignment != null) {
				processedGradeItem.setItemId(assignment.getId());
			}

			final List<ProcessedGradeItemDetail> processedGradeItemDetails = new ArrayList<>();
			for (final ImportedGrade importedGrade : importedGradeWrapper.getImportedGrades()) {
				final ImportedGradeItem importedGradeItem = importedGrade.getGradeItemMap().get(assignmentName);
				if (importedGradeItem != null) {
					final ProcessedGradeItemDetail processedGradeItemDetail = new ProcessedGradeItemDetail();
					processedGradeItemDetail.setStudentEid(importedGrade.getStudentEid());
					processedGradeItemDetail.setStudentUuid(importedGrade.getStudentUuid());
					processedGradeItemDetail.setGrade(importedGradeItem.getGradeItemScore());
					processedGradeItemDetail.setComment(importedGradeItem.getGradeItemComment());
					processedGradeItemDetails.add(processedGradeItemDetail);
				}

			}
			processedGradeItem.setProcessedGradeItemDetails(processedGradeItemDetails);

			if (needsAdded) {
				processedGradeItems.add(processedGradeItem);
				assignmentProcessedGradeItemMap.put(assignmentName, processedGradeItem);
			}
		}

		return processedGradeItems;

	}

	private static ProcessedGradeItemStatus determineStatus(final ImportColumn column, final Assignment assignment,
			final ImportedGradeWrapper importedGradeWrapper,
			final Map<Long, AssignmentStudentGradeInfo> transformedGradeMap) {
		ProcessedGradeItemStatus status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_UNKNOWN);
		if (assignment == null) {
			status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_NEW);
		} else if (assignment.getExternalId() != null) {
			status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_EXTERNAL, assignment.getExternalAppName());
		} else {
			for (final ImportedGrade importedGrade : importedGradeWrapper.getImportedGrades()) {
				final AssignmentStudentGradeInfo assignmentStudentGradeInfo = transformedGradeMap.get(assignment.getId());
				final ImportedGradeItem importedGradeItem = importedGrade.getGradeItemMap().get(column.getColumnTitle());

				String actualScore = null;
				String actualComment = null;

				if (assignmentStudentGradeInfo != null) {
					final GbGradeInfo actualGradeInfo = assignmentStudentGradeInfo.getStudentGrades().get(importedGrade.getStudentEid());

					if (actualGradeInfo != null) {
						actualScore = actualGradeInfo.getGrade();
						actualComment = actualGradeInfo.getGradeComment();
					}
				}
				String importedScore = null;
				String importedComment = null;

				if (importedGradeItem != null) {
					importedScore = importedGradeItem.getGradeItemScore();
					importedComment = importedGradeItem.getGradeItemComment();
				}

				if (column.getType() == ImportColumn.TYPE_ITEM_WITH_POINTS) {
					final String trimmedImportedScore = StringUtils.removeEnd(importedScore, ".0");
					final String trimmedActualScore = StringUtils.removeEnd(actualScore, ".0");
					if (trimmedImportedScore != null && !trimmedImportedScore.equals(trimmedActualScore)) {
						status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_UPDATE);
						break;
					}
				} else if (column.getType() == ImportColumn.TYPE_ITEM_WITH_COMMENTS) {
					if (importedComment != null && !importedComment.equals(actualComment)) {
						status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_UPDATE);
						break;
					}
				}
			}
			// If we get here, must not have been any changes
			if (status.getStatusCode() == ProcessedGradeItemStatus.STATUS_UNKNOWN) {
				status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_NA);
			}

			// TODO - What about if a user was added to the import file?
			// That probably means that actualGradeInfo from up above is null...but what do I do?

		}
		return status;
	}

	private static Map<Long, AssignmentStudentGradeInfo> transformCurrentGrades(final List<GbStudentGradeInfo> currentGrades) {
		final Map<Long, AssignmentStudentGradeInfo> assignmentMap = new HashMap<Long, AssignmentStudentGradeInfo>();

		for (final GbStudentGradeInfo studentGradeInfo : currentGrades) {
			for (final Map.Entry<Long, GbGradeInfo> entry : studentGradeInfo.getGrades().entrySet()) {
				final Long assignmentId = entry.getKey();
				AssignmentStudentGradeInfo assignmentStudentGradeInfo = assignmentMap.get(assignmentId);
				if (assignmentStudentGradeInfo == null) {
					assignmentStudentGradeInfo = new AssignmentStudentGradeInfo();
					assignmentStudentGradeInfo.setAssignmemtId(assignmentId);
					assignmentMap.put(assignmentId, assignmentStudentGradeInfo);
				}
				assignmentStudentGradeInfo.addGrade(studentGradeInfo.getStudentEid(), entry.getValue());
			}

		}

		return assignmentMap;
	}

	/**
	 * Takes a row of String[] data to determine the position of the columns so that we can correctly parse any arbitrary delimited file.
	 * This is required because when we iterate over the rest of the lines, we need to know what the column header is, so we can take the appropriate action.
	 *
	 * @param line the already split line
	 * @return
	 */
	private static Map<Integer, ImportColumn> mapHeaderRow(final String[] line) throws GbImportExportException {
		final Map<Integer, ImportColumn> mapping = new LinkedHashMap<Integer, ImportColumn>();
		for (int i = 0; i < line.length; i++) {

			final ImportColumn column = parseHeaderForImportColumn(trim(line[i]));
			if (column != null) {
				mapping.put(i, column);
			}
		}

		return mapping;
	}

	/**
	 * Helper to parse the header row into an {@link ImportColumn}
	 * @param headerValue
	 * @return the mapped column or null if ignoring.
	 * @throws GbImportExportException if columns didn't match any known pattern
	 */
	private static ImportColumn parseHeaderForImportColumn(final String headerValue) throws GbImportExportException {
		final ImportColumn importColumn = new ImportColumn();

		System.out.println("headerValue: " + headerValue + "%%%");

		// assignment with points header
		final Matcher m1 = ASSIGNMENT_WITH_POINTS_PATTERN.matcher(headerValue);
		if (m1.matches()) {

			// extract title and score
			final Matcher titleMatcher = STANDARD_HEADER_PATTERN.matcher(headerValue);
			final Matcher pointsMatcher = POINTS_PATTERN.matcher(headerValue);

			if (titleMatcher.find()) {
				importColumn.setColumnTitle(trim(titleMatcher.group()));
			}
			if (pointsMatcher.find()) {
				importColumn.setPoints(pointsMatcher.group());
			}

			importColumn.setType(ImportColumn.TYPE_ITEM_WITH_POINTS);

			return importColumn;
		}

		final Matcher m2 = ASSIGNMENT_COMMENT_PATTERN.matcher(headerValue);
		if (m2.matches()) {

			// extract title
			final Matcher titleMatcher = STANDARD_HEADER_PATTERN.matcher(headerValue);

			if (titleMatcher.find()) {
				importColumn.setColumnTitle(trim(titleMatcher.group()));
			}
			importColumn.setType(ImportColumn.TYPE_ITEM_WITH_COMMENTS);

			return importColumn;
		}

		final Matcher m3 = STANDARD_HEADER_PATTERN.matcher(headerValue);
		if (m3.matches()) {

			importColumn.setColumnTitle(headerValue);
			importColumn.setType(ImportColumn.TYPE_REGULAR);

			return importColumn;
		}

		final Matcher m4 = IGNORE_PATTERN.matcher(headerValue);
		if (m4.matches()) {
			log.info("Found header: " + headerValue + " but ignoring it as it is prefixed with a #.");
			return null;
		}

		// if we got here, couldn't parse the column header, throw an error
		throw new GbImportExportException("Invalid column header, skipping: " + headerValue);

	}

	/**
	 * Helper to map an Excel {@link Row} to a String[] so we can use the same methods to process it as the CSV
	 *
	 * @param row
	 * @return
	 */
	private static String[] convertRow(final Row row) {

		final int numCells = row.getPhysicalNumberOfCells();
		final String[] s = new String[numCells];

		int i = 0;
		for (final Cell cell : row) {
			// force cell to String
			cell.setCellType(Cell.CELL_TYPE_STRING);
			s[i] = trim(cell.getStringCellValue());
			i++;
		}

		return s;
	}

	/**
	 * Helper to trim a string to null
	 *
	 * @param s
	 * @return
	 */
	private static String trim(final String s) {
		return StringUtils.trimToNull(s);
	}
}
