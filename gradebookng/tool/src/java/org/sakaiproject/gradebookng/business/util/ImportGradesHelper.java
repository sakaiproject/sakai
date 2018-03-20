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
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItemDetail;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItemStatus;
import org.sakaiproject.gradebookng.tool.model.AssignmentStudentGradeInfo;
import org.sakaiproject.service.gradebook.shared.Assignment;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVParser;
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
	final static Pattern ASSIGNMENT_WITH_POINTS_PATTERN = Pattern.compile("^(.*) \\[([0-9]+([\\.,][0-9][0-9]?)?)\\] *$");

	// list of mimetypes for each category. Must be compatible with the parser
	private static final String[] XLS_MIME_TYPES = { "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" };
	private static final String[] XLS_FILE_EXTS = { ".xls", ".xlsx" };
	private static final String[] CSV_MIME_TYPES = { "text/csv", "text/plain", "text/comma-separated-values", "application/csv" };
	private static final String[] CSV_FILE_EXTS = { ".csv", ".txt" };

	private static final char CSV_SEMICOLON_SEPARATOR = ';';

	/**
	 * Helper to parse the imported file into an {@link ImportedSpreadsheetWrapper} depending on its type
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
	public static ImportedSpreadsheetWrapper parseImportedGradeFile(final InputStream is, final String mimetype, final String filename, final Map<String, String> userMap) throws GbImportExportInvalidColumnException, GbImportExportInvalidFileTypeException, GbImportExportDuplicateColumnException, IOException, InvalidFormatException {
				return parseImportedGradeFile(is, mimetype, filename, userMap, "");
	}

	/**
	 * Helper to parse the imported file into an {@link ImportedSpreadsheetWrapper} depending on its type
	 * 
	 * @param is
	 * @param mimetype
	 * @param userMap
	 * @param userCSVSeparator
	 * @return
	 * @throws GbImportExportInvalidColumnException
	 * @throws GbImportExportInvalidFileTypeException
	 * @throws GbImportExportDuplicateColumnException
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	public static ImportedSpreadsheetWrapper parseImportedGradeFile(final InputStream is, final String mimetype, final String filename,
			final Map<String, String> userMap, String userCSVSeparator) throws GbImportExportInvalidColumnException, GbImportExportInvalidFileTypeException,
			GbImportExportDuplicateColumnException, IOException, InvalidFormatException {

		ImportedSpreadsheetWrapper rval = null;

		// It would be great if we could depend on the browser mimetype, but Windows + Excel will always send an Excel mimetype
		if (StringUtils.endsWithAny(filename, CSV_FILE_EXTS) || ArrayUtils.contains(CSV_MIME_TYPES, mimetype)) {
			rval = ImportGradesHelper.parseCsv(is, userMap, userCSVSeparator);
		} else if (StringUtils.endsWithAny(filename, XLS_FILE_EXTS) || ArrayUtils.contains(XLS_MIME_TYPES, mimetype)) {
			rval = ImportGradesHelper.parseXls(is, userMap, userCSVSeparator);
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
	private static ImportedSpreadsheetWrapper parseCsv(final InputStream is, final Map<String, String> userMap, String userCSVSeparator) throws GbImportExportInvalidColumnException, IOException, GbImportExportDuplicateColumnException {

		// manually parse method so we can support arbitrary columns
		CSVReader reader = null;
		if(StringUtils.isEmpty(userCSVSeparator)){
			reader = new CSVReader(new InputStreamReader(is));
		}else{
			reader = new CSVReader(new InputStreamReader(is), ".".equals(userCSVSeparator) ? CSVParser.DEFAULT_SEPARATOR : CSV_SEMICOLON_SEPARATOR);
		}
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
					final ImportedRow importedRow = mapLine(nextLine, mapping, userMap, userCSVSeparator);
					if(importedRow != null) {
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
	private static ImportedSpreadsheetWrapper parseXls(final InputStream is, final Map<String, String> userMap, String userCSVSeparator) throws GbImportExportInvalidColumnException, InvalidFormatException, IOException, GbImportExportDuplicateColumnException {

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
				final ImportedRow importedRow = mapLine(r, mapping, userMap, userCSVSeparator);
				if(importedRow != null) {
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
	 * Takes a row of data and maps it into the appropriate {@link ImportedRow} pieces.
	 * If a row contains data for a student that does not exist in the site, that row will be skipped
	 *
	 * @param line
	 * @param mapping
	 * @return
	 */
	private static ImportedRow mapLine(final String[] line, final Map<Integer, ImportedColumn> mapping, final Map<String, String> userMap, String userCSVSeparator) {

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

			if (column.getType() == ImportedColumn.Type.USER_ID) {

				//skip blank lines
				if(StringUtils.isBlank(lineVal)) {
					log.debug("Skipping empty row");
					return null;
				}

				// check user is in the map (ie in the site)
				// if not, skip the row
				final String studentUuid = userMap.get(lineVal);
				if(StringUtils.isBlank(studentUuid)){
					log.debug("Student was found in file but not in site. The row will be skipped: " + lineVal);
					return null;
				}
				row.setStudentEid(lineVal);
				row.setStudentUuid(studentUuid);

			} else if (column.getType() == ImportedColumn.Type.USER_NAME) {
				row.setStudentName(lineVal);

			} else if (column.getType() == ImportedColumn.Type.GB_ITEM_WITH_POINTS) {
				//Fix the separator for the comparison with the current values
				if(",".equals(userCSVSeparator) && StringUtils.isNotEmpty(lineVal)){
					lineVal = lineVal.replace(",",".");
				}

				cell.setScore(lineVal);
				row.getCellMap().put(columnTitle, cell);

			} else if (column.getType() == ImportedColumn.Type.GB_ITEM_WITHOUT_POINTS) {
				//Fix the separator for the comparison with the current values
				if(",".equals(userCSVSeparator) && StringUtils.isNotEmpty(lineVal)){
					lineVal = lineVal.replace(",",".");
				}
				cell.setScore(lineVal);
				row.getCellMap().put(columnTitle, cell);

			} else if (column.getType() == ImportedColumn.Type.COMMENTS) {
				cell.setComment(lineVal);
				row.getCellMap().put(columnTitle, cell);
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
		// TODO this will ensure dupes can't be added. Provide a report to the user that dupes were added. There would need to be a step before this though
		// this retains order of the columns in the imported file
		final Map<String, ProcessedGradeItem> assignmentProcessedGradeItemMap = new LinkedHashMap<>();

		// process grades
		final Map<Long, AssignmentStudentGradeInfo> transformedGradeMap = transformCurrentGrades(currentGrades);

		// Map assignment name to assignment
		final Map<String, Assignment> assignmentNameMap = assignments.stream().collect(Collectors.toMap(Assignment::getName, a -> a));

		// maintain a list of comment columns so we can check they have a corresponding item
		final List<String> commentColumns = new ArrayList<>();

		//for every column, setup the data
		for (final ImportedColumn column : spreadsheetWrapper.getColumns()) {
			boolean needsToBeAdded = false;

			// skip the ignorable columns
			if(column.isIgnorable()) {
				continue;
			}

			final String columnTitle = StringUtils.trim(column.getColumnTitle()); // trim whitespace so we can match properly

			//setup a new one unless it already exists (ie there were duplicate columns)
			ProcessedGradeItem processedGradeItem = assignmentProcessedGradeItemMap.get(columnTitle);
			if (processedGradeItem == null) {
				processedGradeItem = new ProcessedGradeItem();
				needsToBeAdded = true;

				//default to gb_item
				//overridden if a comment type
				processedGradeItem.setType(ProcessedGradeItem.Type.GB_ITEM);
			}

			final Assignment assignment = assignmentNameMap.get(columnTitle);
			final ProcessedGradeItemStatus status = determineStatus(column, assignment, spreadsheetWrapper, transformedGradeMap);


			if (column.getType() == ImportedColumn.Type.GB_ITEM_WITH_POINTS) {
				log.debug("GB Item: " + columnTitle + ", status: " + status.getStatusCode());
				processedGradeItem.setItemTitle(columnTitle);
				processedGradeItem.setItemPointValue(column.getPoints());
				processedGradeItem.setStatus(status);
			} else if (column.getType() == ImportedColumn.Type.COMMENTS) {
				log.debug("Comments: " + columnTitle + ", status: " + status.getStatusCode());
				processedGradeItem.setType(ProcessedGradeItem.Type.COMMENT);
				processedGradeItem.setCommentStatus(status);
				commentColumns.add(columnTitle);
			} else if (column.getType() == ImportedColumn.Type.GB_ITEM_WITHOUT_POINTS) {
				log.debug("Regular: " + columnTitle + ", status: " + status.getStatusCode());
				processedGradeItem.setItemTitle(columnTitle);
				processedGradeItem.setStatus(status);
			} else {
				// skip
				//TODO could return this but as a skip status?
				log.warn("Bad column. Type: " + column.getType() + ", header: " + columnTitle + ".  Skipping.");
				continue;
			}

			if (assignment != null) {
				processedGradeItem.setItemId(assignment.getId());
			}

			final List<ProcessedGradeItemDetail> processedGradeItemDetails = new ArrayList<>();
			for (final ImportedRow row : spreadsheetWrapper.getRows()) {
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

			// add to list
			if (needsToBeAdded) {
				assignmentProcessedGradeItemMap.put(columnTitle, processedGradeItem);
			}
		}

		// get just a list
		final List<ProcessedGradeItem> processedGradeItems = new ArrayList<>(assignmentProcessedGradeItemMap.values());

		// comment columns must have an associated gb item column
		// this ensures we have a processed grade item for each one
		commentColumns.forEach(c -> {
			final boolean matchingItemExists = processedGradeItems.stream().filter(p -> StringUtils.equals(c, p.getItemTitle())).findFirst().isPresent();

			if(!matchingItemExists) {
				throw new GbImportCommentMissingItemException("The comment column '" + c + "' does not have a corresponding gradebook item.");
			}
		});

		return processedGradeItems;

	}

	/**
	 * Determine the status of a column
	 * @param column
	 * @param assignment
	 * @param importedGradeWrapper
	 * @param transformedGradeMap
	 * @return
	 */
	private static ProcessedGradeItemStatus determineStatus(final ImportedColumn column, final Assignment assignment,
			final ImportedSpreadsheetWrapper importedGradeWrapper,
			final Map<Long, AssignmentStudentGradeInfo> transformedGradeMap) {

		//TODO - really? an arbitrary value? How about null... Remove this
		ProcessedGradeItemStatus status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_UNKNOWN);

		if (assignment == null) {
			status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_NEW);
		} else if (assignment.getExternalId() != null) {
			status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_EXTERNAL, assignment.getExternalAppName());
		} else if (column.getType() == ImportedColumn.Type.GB_ITEM_WITH_POINTS && assignment.getPoints().compareTo(NumberUtils.toDouble(column.getPoints())) != 0) {
			status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_MODIFIED);
		} else {
			for (final ImportedRow row : importedGradeWrapper.getRows()) {
				final AssignmentStudentGradeInfo assignmentStudentGradeInfo = transformedGradeMap.get(assignment.getId());
				final ImportedCell importedGradeItem = row.getCellMap().get(column.getColumnTitle());

				String actualScore = null;
				String actualComment = null;

				if (assignmentStudentGradeInfo != null) {
					final GbGradeInfo actualGradeInfo = assignmentStudentGradeInfo.getStudentGrades().get(row.getStudentEid());

					if (actualGradeInfo != null) {
						actualScore = actualGradeInfo.getGrade();
						actualComment = actualGradeInfo.getGradeComment();
					}
				}
				String importedScore = null;
				String importedComment = null;

				if (importedGradeItem != null) {
					importedScore = importedGradeItem.getScore();
					importedComment = importedGradeItem.getComment();
				}

				if (column.getType() == ImportedColumn.Type.GB_ITEM_WITH_POINTS) {
					final String trimmedImportedScore = StringUtils.removeEnd(importedScore, ".0");
					final String trimmedActualScore = StringUtils.removeEnd(actualScore, ".0");
					if (trimmedImportedScore != null && !trimmedImportedScore.equals(trimmedActualScore)) {
						status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_UPDATE);
						break;
					}
				} else if (column.getType() == ImportedColumn.Type.COMMENTS) {
					if (importedComment != null && !importedComment.equals(actualComment)) {
						status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_UPDATE);
						break;
					}
				} else if (column.getType() == ImportedColumn.Type.GB_ITEM_WITHOUT_POINTS) {
					//must be NA if it isn't new
					status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_NA);
					break;
				}

			}
			// If we get here, must not have been any changes
			if (status.getStatusCode() == ProcessedGradeItemStatus.STATUS_UNKNOWN) {
				status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_NA);
			}

			// TODO - What about if a user was added to the import file?
			// That probably means that actualGradeInfo from up above is null...but what do I do?
			// SS - this is now caught.

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
	 * Note that some columns are determined positionally
	 *
	 * @param line the already split line
	 * @return LinkedHashMap to retain order
	 * @throws GbImportExportInvalidColumnException if a column doesn't map to any known format
	 * @throws GbImportExportDuplicateColumnException if there are duplicate column headers
	 */
	private static Map<Integer, ImportedColumn> mapHeaderRow(final String[] line) throws GbImportExportInvalidColumnException, GbImportExportDuplicateColumnException {

		// retain order
		final Map<Integer, ImportedColumn> mapping = new LinkedHashMap<Integer, ImportedColumn>();

		for (int i = 0; i < line.length; i++) {

			ImportedColumn column = new ImportedColumn();

			log.debug("i: " + i);
			log.debug("line[i]: " + line[i]);

			if(i == USER_ID_POS) {
				column.setType(ImportedColumn.Type.USER_ID);
			} else if(i == USER_NAME_POS) {
				column.setType(ImportedColumn.Type.USER_NAME);
			} else {
				column = parseHeaderToColumn(trim(line[i]));
			}

			// check for duplicates
			if(mapping.values().contains(column)) {
				throw new GbImportExportDuplicateColumnException("Duplicate column header: " + column.getColumnTitle());
			}

			mapping.put(i, column);
		}

		return mapping;
	}

	/**
	 * Helper to parse the header row into an {@link ImportedColumn}
	 * @param headerValue
	 * @return the mapped column or null if ignoring.
	 * @throws GbImportExportInvalidColumnException if columns didn't match any known pattern
	 */
	private static ImportedColumn parseHeaderToColumn(final String headerValue) throws GbImportExportInvalidColumnException {

		if(StringUtils.isBlank(headerValue)) {
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
	 * @param headerValue
	 * @param title
	 * @param column
	 */
	private static void columnSetColumnTitle(String headerValue, String title, ImportedColumn column)
	{
		title = trim(title);
		if(title == null)
		{
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
