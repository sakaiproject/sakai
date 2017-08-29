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
package org.sakaiproject.gradebookng.business.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.sakaiproject.gradebookng.business.exception.GbImportCommentMissingItemException;
import org.sakaiproject.gradebookng.business.exception.GbImportExportDuplicateColumnException;
import org.sakaiproject.gradebookng.business.exception.GbImportExportInvalidColumnException;
import org.sakaiproject.gradebookng.business.exception.GbImportExportInvalidFileTypeException;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.model.ImportedCell;
import org.sakaiproject.gradebookng.business.model.ImportedColumn;
import org.sakaiproject.gradebookng.business.model.ImportedRow;
import org.sakaiproject.gradebookng.business.model.ImportedSpreadsheetWrapper;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem.Status;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItemDetail;
import org.sakaiproject.gradebookng.tool.model.AssignmentStudentGradeInfo;
import org.sakaiproject.service.gradebook.shared.Assignment;

import au.com.bytecode.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;

/**
 * Helper to handling parsing and processing of an imported gradebook file
 */
@Slf4j
public class ImportGradesHelper {

	// column positions we care about. 0 is first column.
	public final static int USER_ID_POS = 0;
	public final static int USER_NAME_POS = 1;

	// patterns for detecting column headers and their types
	final static Pattern ASSIGNMENT_COMMENT_PATTERN = Pattern.compile("\\* (.*)$");
	final static Pattern ASSIGNMENT_WITH_POINTS_PATTERN = Pattern.compile("^(.*) \\[([0-9]+(\\.[0-9][0-9]?)?)\\] *$");

	// list of mimetypes for each category. Must be compatible with the parser
	private static final String[] XLS_MIME_TYPES = { "application/vnd.ms-excel",
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" };
	private static final String[] XLS_FILE_EXTS = { ".xls", ".xlsx" };
	private static final String[] CSV_MIME_TYPES = { "text/csv", "text/plain", "text/comma-separated-values", "application/csv" };
	private static final String[] CSV_FILE_EXTS = { ".csv", ".txt" };

	/**
	 * Helper to parse the imported file into an {@link ImportedSpreadsheetWrapper} depending on its type
	 * 
	 * @param is
	 * @param mimetype
	 * @param userMap
	 * @return
	 * @throws GbImportExportInvalidColumnException
	 * @throws GbImportExportInvalidFileTypeException
	 * @throws GbImportExportDuplicateColumnException
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	public static ImportedSpreadsheetWrapper parseImportedGradeFile(final InputStream is, final String mimetype, final String filename,
			final Map<String, String> userMap) throws GbImportExportInvalidColumnException, GbImportExportInvalidFileTypeException,
			GbImportExportDuplicateColumnException, IOException, InvalidFormatException {

		ImportedSpreadsheetWrapper rval = null;

		// It would be great if we could depend on the browser mimetype, but Windows + Excel will always send an Excel mimetype
		if (StringUtils.endsWithAny(filename, CSV_FILE_EXTS) || ArrayUtils.contains(CSV_MIME_TYPES, mimetype)) {
			rval = ImportGradesHelper.parseCsv(is, userMap);
		} else if (StringUtils.endsWithAny(filename, XLS_FILE_EXTS) || ArrayUtils.contains(XLS_MIME_TYPES, mimetype)) {
			rval = ImportGradesHelper.parseXls(is, userMap);
		} else {
			throw new GbImportExportInvalidFileTypeException("Invalid file type for grade import: " + mimetype);
		}
		return rval;
	}

	/**
	 * Parse a CSV into a list of {@link ImportedRow} objects.
	 *
	 * @param is InputStream of the data to parse
	 * @return
	 * @throws IOException
	 * @throws GbImportExportInvalidColumnException
	 * @throws GbImportExportDuplicateColumnException
	 */
	private static ImportedSpreadsheetWrapper parseCsv(final InputStream is, final Map<String, String> userMap)
			throws GbImportExportInvalidColumnException, IOException, GbImportExportDuplicateColumnException {

		// manually parse method so we can support arbitrary columns
		final CSVReader reader = new CSVReader(new InputStreamReader(is));
		String[] nextLine;
		int lineCount = 0;
		final List<ImportedRow> list = new ArrayList<ImportedRow>();
		Map<Integer, ImportedColumn> mapping = new LinkedHashMap<>();

		try {
			while ((nextLine = reader.readNext()) != null) {

				if (lineCount == 0) {
					// header row, capture it
					mapping = mapHeaderRow(nextLine);
				} else {
					// map the fields into the object
					final ImportedRow importedRow = mapLine(nextLine, mapping, userMap);
					if (importedRow != null) {
						list.add(importedRow);
					}
				}
				lineCount++;
			}
		} finally {
			try {
				reader.close();
			} catch (final IOException e) {
				log.warn("Error closing the reader", e);
			}
		}

		final ImportedSpreadsheetWrapper importedGradeWrapper = new ImportedSpreadsheetWrapper();
		importedGradeWrapper.setColumns(new ArrayList<>(mapping.values()));
		importedGradeWrapper.setRows(list);

		return importedGradeWrapper;
	}

	/**
	 * Parse an XLS into a list of {@link ImportedRow} objects.
	 *
	 * Note that only the first sheet of the Excel file is supported.
	 *
	 * @param is InputStream of the data to parse
	 * @return
	 * @throws IOException
	 * @throws InvalidFormatException
	 * @throws GbImportExportInvalidColumnException
	 * @Throws GbImportExportDuplicateColumnException
	 */
	private static ImportedSpreadsheetWrapper parseXls(final InputStream is, final Map<String, String> userMap)
			throws GbImportExportInvalidColumnException, InvalidFormatException, IOException, GbImportExportDuplicateColumnException {

		int lineCount = 0;
		final List<ImportedRow> list = new ArrayList<>();
		Map<Integer, ImportedColumn> mapping = new LinkedHashMap<>();

		final Workbook wb = WorkbookFactory.create(is);
		final Sheet sheet = wb.getSheetAt(0);
		for (final Row row : sheet) {

			final String[] r = convertRow(row);

			if (lineCount == 0) {
				// header row, capture it
				mapping = mapHeaderRow(r);
			} else {
				// map the fields into the object
				final ImportedRow importedRow = mapLine(r, mapping, userMap);
				if (importedRow != null) {
					list.add(importedRow);
				}
			}
			lineCount++;
		}

		final ImportedSpreadsheetWrapper importedGradeWrapper = new ImportedSpreadsheetWrapper();
		importedGradeWrapper.setColumns(new ArrayList<>(mapping.values()));
		importedGradeWrapper.setRows(list);
		return importedGradeWrapper;
	}

	/**
	 * Takes a row of data and maps it into the appropriate {@link ImportedRow} pieces. If a row contains data for a student that does not
	 * exist in the site, that row will be skipped
	 *
	 * @param line
	 * @param mapping
	 * @return
	 */
	private static ImportedRow mapLine(final String[] line, final Map<Integer, ImportedColumn> mapping, final Map<String, String> userMap) {

		final ImportedRow row = new ImportedRow();

		for (final Map.Entry<Integer, ImportedColumn> entry : mapping.entrySet()) {

			final int i = entry.getKey();
			final ImportedColumn column = entry.getValue();

			// In case there aren't enough data fields in the line to match up with the number of columns needed
			String lineVal = null;
			if (i < line.length) {
				lineVal = trim(line[i]);
			}

			final String columnTitle = column.getColumnTitle();

			ImportedCell cell = row.getCellMap().get(columnTitle);
			if (cell == null) {
				cell = new ImportedCell();
			}

			switch (column.getType()) {
				case USER_ID:
					// skip blank lines
					if (StringUtils.isBlank(lineVal)) {
						log.debug("Skipping empty row");
						return null;
					}

					// check user is in the map (ie in the site)
					// if not, skip the row
					final String studentUuid = userMap.get(lineVal);
					if (StringUtils.isBlank(studentUuid)) {
						log.debug("Student was found in file but not in site. The row will be skipped: " + lineVal);
						return null;
					}
					row.setStudentEid(lineVal);
					row.setStudentUuid(studentUuid);
					break;
				case USER_NAME:
					row.setStudentName(lineVal);
					break;
				case GB_ITEM_WITH_POINTS:
					cell.setScore(lineVal);
					row.getCellMap().put(columnTitle, cell);
					break;
				case GB_ITEM_WITHOUT_POINTS:
					cell.setScore(lineVal);
					row.getCellMap().put(columnTitle, cell);
					break;
				case COMMENTS:
					cell.setComment(lineVal);
					row.getCellMap().put(columnTitle, cell);
					break;
				case IGNORE:
					// do nothing
					break;
				default:
					break;
			}
		}

		return row;
	}

	/**
	 * Process the data.
	 *
	 * TODO enhance this to have better returns ie GbExceptions
	 *
	 * @param spreadsheetWrapper
	 * @param assignments
	 * @param currentGrades
	 *
	 * @return
	 */
	public static List<ProcessedGradeItem> processImportedGrades(final ImportedSpreadsheetWrapper spreadsheetWrapper,
			final List<Assignment> assignments, final List<GbStudentGradeInfo> currentGrades) {

		// setup
		// note that previous step checks for duplicates
		final List<ProcessedGradeItem> processedGradeItems = new ArrayList<>();

		// process grades
		final Map<Long, AssignmentStudentGradeInfo> transformedGradeMap = transformCurrentGrades(currentGrades);

		// Map assignment name to assignment
		final Map<String, Assignment> assignmentMap = assignments.stream()
				.collect(Collectors.toMap(Assignment::getName, Function.identity()));

		// maintain a list of comment columns so we can check they have a corresponding item
		final List<String> commentColumns = new ArrayList<>();

		// for every column, setup the header and data
		for (final ImportedColumn column : spreadsheetWrapper.getColumns()) {

			// skip the ignorable columns (ie username, id, any marked for ignore)
			if (column.isIgnorable()) {
				continue;
			}

			final String columnTitle = StringUtils.trim(column.getColumnTitle()); // trim whitespace so we can match properly

			final ProcessedGradeItem processedGradeItem = new ProcessedGradeItem();

			// assignment info (if available)
			final Assignment assignment = assignmentMap.get(columnTitle);
			if (assignment != null) {
				processedGradeItem.setItemId(assignment.getId());
			}

			// status
			final Status status = determineStatus(column, assignment, spreadsheetWrapper, transformedGradeMap);
			processedGradeItem.setStatus(status);
			processedGradeItem.setItemTitle(columnTitle);

			log.debug("Column name: " + columnTitle + ", type: " + column.getType() + ", status: " + status);

			// process the header as applicable
			switch (column.getType()) {
				case COMMENTS:
					processedGradeItem.setType(ProcessedGradeItem.Type.COMMENT);
					commentColumns.add(columnTitle);
					break;
				case GB_ITEM_WITHOUT_POINTS:
					processedGradeItem.setType(ProcessedGradeItem.Type.GB_ITEM);
					break;
				case GB_ITEM_WITH_POINTS:
					processedGradeItem.setType(ProcessedGradeItem.Type.GB_ITEM);
					processedGradeItem.setItemPointValue(column.getPoints());
					break;
				case IGNORE:
					// never hit
					break;
				case USER_ID:
					// never hit
					break;
				case USER_NAME:
					// never hit
					break;
				default:
					log.warn("Bad column. Type: " + column.getType() + ", header: " + columnTitle + ".  Skipping.");
					break;
			}

			// process the data
			final List<ProcessedGradeItemDetail> processedGradeItemDetails = new ArrayList<>();
			for (final ImportedRow row : spreadsheetWrapper.getRows()) {
				log.debug("row: " + row.getStudentEid());
				log.debug("columnTitle: " + columnTitle);

				final ImportedCell cell = row.getCellMap().get(columnTitle);

				if (cell != null) {
					final ProcessedGradeItemDetail processedGradeItemDetail = new ProcessedGradeItemDetail();
					processedGradeItemDetail.setStudentEid(row.getStudentEid());
					processedGradeItemDetail.setStudentUuid(row.getStudentUuid());
					processedGradeItemDetail.setGrade(cell.getScore());
					processedGradeItemDetail.setComment(cell.getComment());
					processedGradeItemDetails.add(processedGradeItemDetail);
				}

			}
			processedGradeItem.setProcessedGradeItemDetails(processedGradeItemDetails);

			processedGradeItems.add(processedGradeItem);
		}

		// comment columns must have an associated gb item column
		// this ensures we have a processed grade item for each one
		commentColumns.forEach(c -> {
			final boolean matchingItemExists = processedGradeItems.stream().filter(p -> StringUtils.equals(c, p.getItemTitle())).findFirst()
					.isPresent();

			if (!matchingItemExists) {
				throw new GbImportCommentMissingItemException(
						"The comment column '" + c + "' does not have a corresponding gradebook item.");
			}
		});

		return processedGradeItems;
	}

	/**
	 * Determine the status of a column
	 * 
	 * @param column
	 * @param assignment
	 * @param importedGradeWrapper
	 * @param transformedGradeMap
	 * @return {@link Status}
	 */
	private static Status determineStatus(final ImportedColumn column, final Assignment assignment,
			final ImportedSpreadsheetWrapper importedGradeWrapper,
			final Map<Long, AssignmentStudentGradeInfo> transformedGradeMap) {

		// default
		Status status = null;

		log.debug("Determining status for column: " + column.getColumnTitle() + ", type: " + column.getType());

		if (column.isGradeItem()) {
			if (assignment == null) {
				status = Status.NEW;
			} else if (assignment.getExternalId() != null) {
				status = Status.EXTERNAL;
			} else if (column.getType() == ImportedColumn.Type.GB_ITEM_WITH_POINTS
					&& assignment.getPoints().compareTo(NumberUtils.toDouble(column.getPoints())) != 0) {
				status = Status.MODIFIED;
			}
		}

		if (column.isComment()) {
			if (assignment == null) {
				status = Status.NEW;
			} else if (assignment.getExternalId() != null) {
				status = Status.EXTERNAL;
			}
		}

		// for grade items, only need to check if we dont already have a status, as grade items are always imported for NEW and MODIFIED
		// items
		// for comments we always check unless external as we might have a NEW item but with no data which means SKIP
		if ((column.isGradeItem() && status == null) || (column.isComment() && status != Status.EXTERNAL)) {
			for (final ImportedRow row : importedGradeWrapper.getRows()) {

				// imported data setup
				final ImportedCell importedCell = row.getCellMap().get(column.getColumnTitle());

				log.debug("Checking cell data: " + importedCell);

				String importedScore = null;
				String importedComment = null;

				if (importedCell != null) {
					importedScore = importedCell.getScore();
					importedComment = importedCell.getComment();
				}

				// get existing data
				String existingScore = null;
				String existingComment = null;
				if (assignment != null) {
					final AssignmentStudentGradeInfo assignmentStudentGradeInfo = transformedGradeMap.get(assignment.getId());

					if (assignmentStudentGradeInfo != null) {
						final GbGradeInfo existingGradeInfo = assignmentStudentGradeInfo.getStudentGrades().get(row.getStudentEid());
						if (existingGradeInfo != null) {
							existingScore = existingGradeInfo.getGrade();
							existingComment = existingGradeInfo.getGradeComment();
						}
					}
				}

				// handle grade items
				if (column.isGradeItem()) {

					importedScore = StringUtils.removeEnd(importedScore, ".0");
					existingScore = StringUtils.removeEnd(existingScore, ".0");

					log.debug("Comparing data, importedScore: " + importedScore + ", existingScore: " + existingScore);

					if (StringUtils.isNotBlank(importedScore) && !StringUtils.equals(importedScore, existingScore)) {
						status = Status.UPDATE;
						break;
					}
				}

				// handle comments
				if (column.isComment()) {

					log.debug("Comparing data, importedComment: " + importedComment + ", existingComment: " + existingComment);

					if (StringUtils.isBlank(importedComment)) {
						status = Status.SKIP;
						continue; // keep checking
					}
					// has a value, could be NEW or an UPDATE. Preserve NEW if we already had it
					if (status != Status.NEW) {
						if (StringUtils.isNotBlank(importedComment) && !StringUtils.equals(importedComment, existingComment)) {
							status = Status.UPDATE;
							break;
						}
					}
				}
			}
		}

		if (status == null) {
			status = Status.SKIP;
		}

		log.debug("Status: " + status);

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
	 * This is required because when we iterate over the rest of the lines, we need to know what the column header is, so we can take the
	 * appropriate action.
	 *
	 * Note that some columns are determined positionally
	 *
	 * @param line the already split line
	 * @return LinkedHashMap to retain order
	 * @throws GbImportExportInvalidColumnException if a column doesn't map to any known format
	 * @throws GbImportExportDuplicateColumnException if there are duplicate column headers
	 */
	private static Map<Integer, ImportedColumn> mapHeaderRow(final String[] line)
			throws GbImportExportInvalidColumnException, GbImportExportDuplicateColumnException {

		// retain order
		final Map<Integer, ImportedColumn> mapping = new LinkedHashMap<Integer, ImportedColumn>();

		for (int i = 0; i < line.length; i++) {

			ImportedColumn column = null;

			log.debug("i: " + i);
			log.debug("line[i]: " + line[i]);

			if (i == USER_ID_POS) {
				column = new ImportedColumn();
				column.setType(ImportedColumn.Type.USER_ID);
			} else if (i == USER_NAME_POS) {
				column = new ImportedColumn();
				column.setType(ImportedColumn.Type.USER_NAME);
			} else {
				column = parseHeaderToColumn(trim(line[i]));
			}

			// check for duplicates
			if (mapping.values().contains(column)) {
				throw new GbImportExportDuplicateColumnException("Duplicate column header: " + column.getColumnTitle());
			}

			mapping.put(i, column);
		}

		return mapping;
	}

	/**
	 * Helper to parse the header row into an {@link ImportedColumn}
	 * 
	 * @param headerValue
	 * @return the mapped column or null if ignoring.
	 * @throws GbImportExportInvalidColumnException if columns didn't match any known pattern
	 */
	private static ImportedColumn parseHeaderToColumn(final String headerValue) throws GbImportExportInvalidColumnException {

		if (StringUtils.isBlank(headerValue)) {
			throw new GbImportExportInvalidColumnException("Invalid column header: " + headerValue);
		}

		log.debug("headerValue: " + headerValue);

		final ImportedColumn column = new ImportedColumn();

		if (headerValue.startsWith("#")) {
			log.info("Found header: " + headerValue + " but ignoring it as it is prefixed with a #.");
			column.setType(ImportedColumn.Type.IGNORE);
			return column;
		}

		// Comment lines start with a "* "
		Matcher m = ASSIGNMENT_COMMENT_PATTERN.matcher(headerValue);
		if (m.matches()) {

			// extract title
			columnSetColumnTitle(headerValue, m.group(1), column);
			column.setType(ImportedColumn.Type.COMMENTS);

			return column;
		}

		// assignment with points header - ends with a "[nn.nn]"
		m = ASSIGNMENT_WITH_POINTS_PATTERN.matcher(headerValue);
		if (m.matches()) {

			// extract title and score
			columnSetColumnTitle(headerValue, m.group(1), column);
			column.setPoints(m.group(2));
			column.setType(ImportedColumn.Type.GB_ITEM_WITH_POINTS);

			return column;
		}

		// It's a standard columm
		columnSetColumnTitle(headerValue, headerValue, column);
		column.setType(ImportedColumn.Type.GB_ITEM_WITHOUT_POINTS);

		return column;
	}

	/**
	 * Helper to set a column title or raise an exception if empty
	 * 
	 * @param headerValue
	 * @param title
	 * @param column
	 */
	private static void columnSetColumnTitle(String headerValue, String title, ImportedColumn column) {
		title = trim(title);
		if (title == null) {
			// Empty column title is invalid
			throw new GbImportExportInvalidColumnException("Invalid column header: " + headerValue);
		}
		column.setColumnTitle(title);
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
