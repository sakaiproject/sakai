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

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

import java.io.IOException;
import java.io.InputStream; 
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;

import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.exception.GbImportExportInvalidFileTypeException;
import org.sakaiproject.gradebookng.business.importExport.CommentValidationReport;
import org.sakaiproject.gradebookng.business.importExport.CommentValidator;
import org.sakaiproject.gradebookng.business.importExport.GradeValidationReport;
import org.sakaiproject.gradebookng.business.importExport.GradeValidator;
import org.sakaiproject.gradebookng.business.importExport.HeadingValidationReport;
import org.sakaiproject.gradebookng.business.importExport.UserIdentificationReport;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.business.model.ImportedCell;
import org.sakaiproject.gradebookng.business.model.ImportedColumn;
import org.sakaiproject.gradebookng.business.model.ImportedRow;
import org.sakaiproject.gradebookng.business.model.ImportedSpreadsheetWrapper;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem.Status;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItemDetail;
import org.sakaiproject.gradebookng.tool.model.AssignmentStudentGradeInfo;
import org.sakaiproject.gradebookng.tool.model.ImportWizardModel;
import org.sakaiproject.gradebookng.tool.pages.ImportExportPage;
import org.sakaiproject.service.gradebook.shared.Assignment;

/**
 * Helper to handling parsing and processing of an imported gradebook file
 */
@Slf4j
public class ImportGradesHelper {

	// column positions we care about. 0 is first column.
	public final static int USER_ID_POS = 0;
	public final static int USER_NAME_POS = 1;

	// patterns for detecting column headers and their types
	final static Pattern ASSIGNMENT_PATTERN = Pattern.compile("([^\\[]+)(\\[(\\d+([\\.,]\\d+)?)\\])?");
	final static Pattern COMMENT_PATTERN = Pattern.compile("\\* (.+)");
	final static Pattern IGNORE_PATTERN = Pattern.compile("(\\#.+)");

	// list of mimetypes for each category. Must be compatible with the parser
	public static final String[] XLS_MIME_TYPES = { "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" };
	public static final String[] XLS_FILE_EXTS = { ".xls", ".xlsx" };
	public static final String[] CSV_MIME_TYPES = { "text/csv", "text/plain", "text/comma-separated-values", "application/csv" };
	public static final String[] CSV_FILE_EXTS = { ".csv", ".txt" };

	private static final char CSV_SEMICOLON_SEPARATOR = ';';

	/**
	 * Helper to parse the imported file into an {@link ImportedSpreadsheetWrapper} depending on its type
	 *
	 * @param is
	 * @param mimetype
	 * @param filename
	 * @param businessService
	 * @return
	 * @throws GbImportExportInvalidFileTypeException
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	public static ImportedSpreadsheetWrapper parseImportedGradeFile(final InputStream is, final String mimetype, final String filename,
			final GradebookNgBusinessService businessService) throws GbImportExportInvalidFileTypeException, IOException, InvalidFormatException {
				return parseImportedGradeFile(is, mimetype, filename, businessService, "");
	}

	/**
	 * Helper to parse the imported file into an {@link ImportedSpreadsheetWrapper} depending on its type
	 *
	 * @param is
	 * @param mimetype
	 * @param filename
	 * @param businessService
	 * @param userDecimalSeparator
	 * @return
	 * @throws GbImportExportInvalidFileTypeException
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	public static ImportedSpreadsheetWrapper parseImportedGradeFile(final InputStream is, final String mimetype, final String filename,
			final GradebookNgBusinessService businessService, String userDecimalSeparator) throws GbImportExportInvalidFileTypeException, IOException, InvalidFormatException {

		ImportedSpreadsheetWrapper rval = null;

		// It would be great if we could depend on the browser mimetype, but Windows + Excel will always send an Excel mimetype
		if (StringUtils.endsWithAny(filename, CSV_FILE_EXTS) || ArrayUtils.contains(CSV_MIME_TYPES, mimetype)) {
			rval = ImportGradesHelper.parseCsv(is, businessService, userDecimalSeparator);
		} else if (StringUtils.endsWithAny(filename, XLS_FILE_EXTS) || ArrayUtils.contains(XLS_MIME_TYPES, mimetype)) {
			rval = ImportGradesHelper.parseXls(is, businessService, userDecimalSeparator);
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
	private static ImportedSpreadsheetWrapper parseCsv(final InputStream is, final GradebookNgBusinessService businessService, String userDecimalSeparator)
			throws IOException {

		// manually parse method so we can support arbitrary columns
		CSVReader reader;
		if(StringUtils.isEmpty(userDecimalSeparator)){
			reader = new CSVReader(new InputStreamReader(is));
		}else{
			reader = new CSVReader(new InputStreamReader(is), ".".equals(userDecimalSeparator) ? CSVParser.DEFAULT_SEPARATOR : CSV_SEMICOLON_SEPARATOR);
		}
		String[] nextLine;
		int lineCount = 0;
		final List<ImportedRow> list = new ArrayList<>();
		Map<Integer, ImportedColumn> mapping = new LinkedHashMap<>();
		Map<String, GbUser> userEidMap = businessService.getUserEidMap();
		final ImportedSpreadsheetWrapper importedGradeWrapper = new ImportedSpreadsheetWrapper();

		try {
			while ((nextLine = reader.readNext()) != null) {

				if (lineCount == 0) {
					// header row, capture it
					mapping = mapHeaderRow(nextLine, importedGradeWrapper.getHeadingReport());
				} else {
					// map the fields into the object
					final ImportedRow importedRow = mapLine(nextLine, mapping, userEidMap, userDecimalSeparator);
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

		importedGradeWrapper.setColumns(new ArrayList<>(mapping.values()));
		importedGradeWrapper.setRows(list, userEidMap);
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
	private static ImportedSpreadsheetWrapper parseXls(final InputStream is, final GradebookNgBusinessService businessService, String userDecimalSeparator)
			throws InvalidFormatException, IOException {

		int lineCount = 0;
		final List<ImportedRow> list = new ArrayList<>();
		Map<Integer, ImportedColumn> mapping = new LinkedHashMap<>();
		Map<String, GbUser> userEidMap = businessService.getUserEidMap();
		final ImportedSpreadsheetWrapper importedGradeWrapper = new ImportedSpreadsheetWrapper();

		final Workbook wb = WorkbookFactory.create(is);
		final Sheet sheet = wb.getSheetAt(0);
		for (final Row row : sheet) {

			final String[] r = convertRow(row);

			if (lineCount == 0) {
				// header row, capture it
				mapping = mapHeaderRow(r, importedGradeWrapper.getHeadingReport());
			} else {
				// map the fields into the object
				final ImportedRow importedRow = mapLine(r, mapping, userEidMap, userDecimalSeparator);
				if (importedRow != null) {
					list.add(importedRow);
				}
			}
			lineCount++;
		}

		importedGradeWrapper.setColumns(new ArrayList<>(mapping.values()));
		importedGradeWrapper.setRows(list, userEidMap);
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
	private static ImportedRow mapLine(final String[] line, final Map<Integer, ImportedColumn> mapping, final Map<String, GbUser> userMap, String userDecimalSeparator) {

		final ImportedRow row = new ImportedRow();

		for (final Map.Entry<Integer, ImportedColumn> entry : mapping.entrySet()) {

			final int i = entry.getKey();
			final ImportedColumn column = entry.getValue();

			// In case there aren't enough data fields in the line to match up with the number of columns needed
			String lineVal = null;
			if (i < line.length) {
				lineVal = StringUtils.trimToNull(line[i]);
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
					GbUser user = userMap.get(lineVal);
					if(user != null) {
						row.setStudentUuid(user.getUserUuid());
						row.setStudentEid(user.getDisplayId());
					} else {
						row.setStudentEid(lineVal);
					}
					break;
				case USER_NAME:
					row.setStudentName(lineVal);
					break;
				case GB_ITEM_WITH_POINTS:
					// fall into next case (same impl)
				case GB_ITEM_WITHOUT_POINTS:
					// fix the separator for the comparison with the current values
					if (StringUtils.isNotBlank(lineVal)) {
						cell.setRawScore(lineVal);
						cell.setScore(",".equals(userDecimalSeparator) ? lineVal.replace(userDecimalSeparator, ".") : lineVal);
					}
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
	 * Validates and processes the spreadsheet provided by importWizardModel.getSpreadsheetWrapper(); prepares an ImportWizardModel appropriate for the selection step
	 * @param sourcePage the ImportExportPage
	 * @param sourcePanel panel on which to invoke error(), etc. with localized messages should we encounter any issues
	 * @param importWizardModel
	 * @param businessService
	 * @param target
	 * @return true if the model was successfully set up without errors; in case of errors, the feedback panels will be updated and false will be returned
	 */
	public static boolean setupImportWizardModelForSelectionStep(ImportExportPage sourcePage, Panel sourcePanel, ImportWizardModel importWizardModel,
																		GradebookNgBusinessService businessService, AjaxRequestTarget target) {
		ImportedSpreadsheetWrapper spreadsheetWrapper = importWizardModel.getSpreadsheetWrapper();
		if (spreadsheetWrapper == null) {
			sourcePanel.error(MessageHelper.getString("importExport.error.unknown"));
			sourcePage.updateFeedback(target);
			return false;
		}

		// If there are duplicate headings, tell the user now
		boolean hasValidationErrors = false;
		HeadingValidationReport headingReport = spreadsheetWrapper.getHeadingReport();
		SortedSet<String> duplicateHeadings = headingReport.getDuplicateHeadings();
		if (!duplicateHeadings.isEmpty()) {
			String duplicates = StringUtils.join(duplicateHeadings, ", ");
			sourcePanel.error(MessageHelper.getString("importExport.error.duplicateColumns", duplicates));
			hasValidationErrors = true;
		}

		// If there are invalid headings, tell the user now
		SortedSet<String> invalidHeadings = headingReport.getInvalidHeadings();
		if (!invalidHeadings.isEmpty()) {
			String invalids = StringUtils.join(invalidHeadings, ", ");
			sourcePanel.error(MessageHelper.getString("importExport.error.invalidColumns", invalids));
			hasValidationErrors = true;
		}

		// If there are blank headings, tell the user now
		int blankHeadings = headingReport.getBlankHeaderTitleCount();
		if (blankHeadings > 0) {
			sourcePanel.error(MessageHelper.getString("importExport.error.blankHeadings", blankHeadings));
			hasValidationErrors = true;
		}

		// If there are duplicate student entries, tell the user now (we can't make the decision about which entry takes precedence)
		UserIdentificationReport userReport = spreadsheetWrapper.getUserIdentifier().getReport();
		SortedSet<GbUser> duplicateStudents = userReport.getDuplicateUsers();
		if (!duplicateStudents.isEmpty()) {
			String duplicates = StringUtils.join(duplicateStudents, ", ");
			sourcePanel.error(MessageHelper.getString("importExport.error.duplicateStudents", duplicates));
			hasValidationErrors = true;
		}

		// Perform grade validation; present error message with invalid grades on current page
		List<ImportedColumn> columns = spreadsheetWrapper.getColumns();
		List<ImportedRow> rows = spreadsheetWrapper.getRows();
		GradeValidationReport gradeReport = new GradeValidator(businessService).validate(rows, columns);
		// maps columnTitle -> (userEid -> grade)
		SortedMap<String, SortedMap<String, String>> invalidGradesMap = gradeReport.getInvalidNumericGrades();
		if (!invalidGradesMap.isEmpty()) {
			Collection<SortedMap<String, String>> invalidGrades = invalidGradesMap.values();
			List<String> badGradeEntries = new ArrayList<>(invalidGrades.size());
			for (SortedMap<String, String> invalidGradeEntries : invalidGrades) {
				badGradeEntries.add(StringUtils.join(invalidGradeEntries.entrySet(), ", "));
			}

			String badGrades = StringUtils.join(badGradeEntries, ", ");
			sourcePanel.error(MessageHelper.getString("importExport.error.invalidGradeData", MessageHelper.getString("grade.notifications.invalid"), badGrades));
			hasValidationErrors = true;
		}

		// Perform comment validation; present error message with invalid gradebook items and corresponding student identifiers on current page
		CommentValidationReport commentReport = new CommentValidator().validate(rows, columns);
		SortedMap<String, List<String>> invalidCommentsMap = commentReport.getInvalidComments();
		if (!invalidCommentsMap.isEmpty()) {
			List<String> badCommentEntries = new ArrayList<>();
			for (String columnTitle : invalidCommentsMap.keySet()) {
				for (String student : invalidCommentsMap.get(columnTitle)) {
					badCommentEntries.add(columnTitle + ":" + student);
				}
			}

			String badComments = StringUtils.join(badCommentEntries, ", ");
			sourcePanel.error(MessageHelper.getString("importExport.error.invalidComments", CommentValidator.MAX_COMMENT_LENGTH, badComments));
			hasValidationErrors = true;
		}

		// get existing data
		final List<Assignment> assignments = businessService.getGradebookAssignments();
		final List<GbStudentGradeInfo> grades = businessService.buildGradeMatrixForImportExport(assignments, null);

		// process file
		List<ProcessedGradeItem> processedGradeItems = processImportedGrades(spreadsheetWrapper, assignments, grades);

		// If the file has orphaned comment columns, tell the user now
		SortedSet<String> orphanedCommentColumns = headingReport.getOrphanedCommentHeadings();
		if (!orphanedCommentColumns.isEmpty()) {
			String invalids = StringUtils.join(orphanedCommentColumns, ", ");
			sourcePanel.error(MessageHelper.getString("importExport.error.orphanedComments", invalids));
			hasValidationErrors = true;
		}

		// if the file has no valid users, tell the user now
		if (userReport.getIdentifiedUsers().isEmpty()) {
			hasValidationErrors = true;
			sourcePanel.error(MessageHelper.getString("importExport.error.noValidStudents"));
		}

		// if empty there are no grade columns, tell the user now
		if (processedGradeItems.isEmpty() && !userReport.getIdentifiedUsers().isEmpty()) {
			hasValidationErrors = true;
			sourcePanel.error(MessageHelper.getString("importExport.error.noValidGrades"));
		}

		boolean hasChanges = false;
		for (ProcessedGradeItem item : processedGradeItems) {
			if (item.getStatus() == ProcessedGradeItem.Status.MODIFIED || item.getStatus() == ProcessedGradeItem.Status.NEW ||
					item.getStatus() == ProcessedGradeItem.Status.UPDATE) {
				hasChanges = true;
				break;
			}
		}

		if ((!hasChanges && !processedGradeItems.isEmpty()) && !userReport.getIdentifiedUsers().isEmpty()) {
			hasValidationErrors = true;
			sourcePanel.error(MessageHelper.getString("importExport.error.noChanges"));
		}

		// Return errors before processing further
		if (hasValidationErrors) {
			sourcePage.updateFeedback(target);
			return false;
		}

		// No validation errors were encountered; clear out previous errors and continue to the next step in the wizard
		sourcePage.clearFeedback();
		sourcePage.updateFeedback(target);

		// Setup and return the model
		importWizardModel.setProcessedGradeItems(processedGradeItems);
		importWizardModel.setUserReport(userReport);
		return true;
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

			log.debug("Column name: {}, type: {}, status: {}", columnTitle, column.getType(), status);

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
					log.warn("Bad column. Type: {}, header: {}.  Skipping.", column.getType(), columnTitle);
					break;
			}

			// process the data
			final List<ProcessedGradeItemDetail> processedGradeItemDetails = new ArrayList<>();
			for (final ImportedRow row : spreadsheetWrapper.getRows()) {
				log.debug("row: {}", row.getStudentEid());
				log.debug("columnTitle: {}", columnTitle);

				final ImportedCell cell = row.getCellMap().get(columnTitle);

				if (cell != null) {
					// Only process the grade item if the user is valid (present in the site/gradebook)
					if (row.getUser().isValid()) {
						final ProcessedGradeItemDetail processedGradeItemDetail = new ProcessedGradeItemDetail();
						processedGradeItemDetail.setUser(row.getUser());
						processedGradeItemDetail.setGrade(cell.getScore());
						processedGradeItemDetail.setComment(cell.getComment());
						processedGradeItemDetails.add(processedGradeItemDetail);
					}
				}
			}
			processedGradeItem.setProcessedGradeItemDetails(processedGradeItemDetails);

			processedGradeItems.add(processedGradeItem);
		}

		// comment columns must have an associated gb item column
		// this ensures we have a processed grade item for each one
		HeadingValidationReport report = spreadsheetWrapper.getHeadingReport();
		List<String> itemTitles = processedGradeItems.stream()
				.filter(item -> item.getType() == ProcessedGradeItem.Type.GB_ITEM)
				.map(ProcessedGradeItem::getItemTitle).collect(Collectors.toList());
		commentColumns.forEach(c -> {
			final boolean matchingItemExists = itemTitles.stream().anyMatch(c::contains);

			if (!matchingItemExists) {
				report.addOrphanedCommentHeading(c);
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

		log.debug("Determining status for column: {}, type: {}", column.getColumnTitle(), column.getType());

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

		// for grade items, only need to check if we dont already have a status, as grade items are always imported for NEW and MODIFIED items
		// for comments we always check unless external as we might have a NEW item but with no data which means SKIP
		SortedSet<GbUser> usersInGradebook = importedGradeWrapper.getUserIdentifier().getReport().getIdentifiedUsers();
		if ((column.isGradeItem() && status == null) || (column.isComment() && status != Status.EXTERNAL)) {
			for (final ImportedRow row : importedGradeWrapper.getRows()) {

				// if the user is not a member of the site/gradebook, we don't need to consider the data for the column's status
				if (!usersInGradebook.contains(row.getUser())) {
					continue;
				}

				// imported data setup
				final ImportedCell importedCell = row.getCellMap().get(column.getColumnTitle());

				log.debug("Checking cell data: {}", importedCell);

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

					log.debug("Comparing data, importedScore: {}, existingScore: {}", importedScore, existingScore);

					if (StringUtils.isNotBlank(importedScore) && !StringUtils.equals(importedScore, existingScore)) {
						status = Status.UPDATE;
						break;
					}
				}

				// handle comments
				if (column.isComment()) {
					log.debug("Comparing data, importedComment: {}, existingComment: {}", importedComment, existingComment);

					// has a value, could be NEW or an UPDATE. Preserve NEW if we already had it
					if (status != Status.NEW) {
						boolean importContainsNewComment = (StringUtils.isNotBlank(importedComment) && !StringUtils.equals(importedComment, existingComment));
						boolean importClearsExistingComment = (StringUtils.isBlank(importedComment) && StringUtils.isNotBlank(existingComment));

						if (importContainsNewComment || importClearsExistingComment) {
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

		log.debug("Status: {}", status);

		return status;
	}

	private static Map<Long, AssignmentStudentGradeInfo> transformCurrentGrades(final List<GbStudentGradeInfo> currentGrades) {
		final Map<Long, AssignmentStudentGradeInfo> assignmentMap = new HashMap<>();

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
	 * @param headingReport the heading validation report to use for duplicated, invalid and blank columns
	 * @return LinkedHashMap to retain order
	 */
	private static Map<Integer, ImportedColumn> mapHeaderRow(final String[] line, HeadingValidationReport headingReport) {

		// retain order
		final Map<Integer, ImportedColumn> mapping = new LinkedHashMap<>();

		for (int i = 0; i < line.length; i++) {

			ImportedColumn column;

			log.debug("i: {}", i);
			log.debug("line[i]: {}", line[i]);

			if (i == USER_ID_POS) {
				column = new ImportedColumn();
				column.setType(ImportedColumn.Type.USER_ID);
			} else if (i == USER_NAME_POS) {
				column = new ImportedColumn();
				column.setType(ImportedColumn.Type.USER_NAME);
			} else {
				column = parseHeaderToColumn(StringUtils.trimToNull(line[i]), headingReport);
			}

			// check for duplicates
			if (mapping.values().contains(column)) {
				String columnTitle = column.getColumnTitle();
				headingReport.addDuplicateHeading(columnTitle);
				continue;
			}

			if (column != null) {
				mapping.put(i, column);
			}
		}

		return mapping;
	}

	/**
	 * Helper to parse the header row into an {@link ImportedColumn}
	 *
	 * @param headerValue
	 * @param headingReport the heading validation report to use for duplicated, invalid and blank columns
	 * @return the mapped column or null if ignoring.
	 */
	private static ImportedColumn parseHeaderToColumn(final String headerValue, HeadingValidationReport headingReport) {

		if (StringUtils.isBlank(headerValue)) {
			headingReport.incrementBlankHeaderTitleCount();
			return null;
		}

		log.debug("headerValue: {}", headerValue);
		final ImportedColumn column = new ImportedColumn();

		Matcher m = IGNORE_PATTERN.matcher(headerValue);
		if (m.matches()) {
			log.debug("Found header: {} but ignoring it as it is prefixed with a #.", headerValue);
			column.setType(ImportedColumn.Type.IGNORE);
			return column;
		}

		m = COMMENT_PATTERN.matcher(headerValue);
		if (m.matches()) {
			column.setColumnTitle(StringUtils.trimToNull(m.group(1)));
			column.setType(ImportedColumn.Type.COMMENTS);
			return column;
		}

		m = ASSIGNMENT_PATTERN.matcher(headerValue);
		if (m.matches()) {
			column.setColumnTitle(StringUtils.trimToNull(m.group(1)));
			String points = m.group(3);
			if (StringUtils.isNotBlank(points)) {
				column.setPoints(points);
				column.setType(ImportedColumn.Type.GB_ITEM_WITH_POINTS);
			} else {
				column.setType(ImportedColumn.Type.GB_ITEM_WITHOUT_POINTS);
			}

			return column;
		}

		// None of the patterns match, it must be invalid/formatted improperly
		headingReport.addInvalidHeading(headerValue);
		return null;
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
			s[i] = StringUtils.trimToNull(cell.getStringCellValue());
			i++;
		}

		return s;
	}
}
