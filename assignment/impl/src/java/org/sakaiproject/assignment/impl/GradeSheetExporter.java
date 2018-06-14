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

import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.assignment.impl.sort.AssignmentComparator;
import org.sakaiproject.assignment.impl.sort.UserComparator;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.CandidateDetailProvider;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Exporter for the assignments grades spreadsheet. This encapsulates all the Apache POI code.
 */
@Slf4j
public class GradeSheetExporter {

    @Setter private AssignmentService assignmentService;
    @Setter private CandidateDetailProvider candidateDetailProvider;
    @Setter private SiteService siteService;
    @Setter private UserDirectoryService userDirectoryService;

    private ResourceLoader rb = new ResourceLoader("assignment");

    /**
     * Access and output the grades spreadsheet for the reference, either for an assignment or all assignments in a context.
     *
     * @param out The outputStream to stream the grades spreadsheet into.
     * @param reference The reference, either to a specific assignment, or just to an assignment context.
     * @param query The query string from the request with the following params contextString, viewString, searchString, searchFilterOnly
     *              <br/>see <b>AssignmentAction#build_instructor_report_submissions()</b>
     */
    public void getGradesSpreadsheet(final OutputStream out, final String reference, final String query) {
        Map<String, String> params = new HashMap<>();
        Pattern.compile("&").splitAsStream(query).map(p -> Arrays.copyOf(p.split("="), 2)).forEach(a -> params.put(a[0], a[1]));
        // get site title for display purpose
        String siteTitle = "";
        String sheetName = "";
        String context = params.get("contextString");
        Site site = null;
        Group group = null;
        try {
            site = siteService.getSite(context);
            siteTitle = sheetName = site.getTitle();
            if (!"all".equals(params.get("viewString"))) {
                group = site.getGroup(params.get("viewString"));
                siteTitle = siteTitle + "-" + group.getTitle();
                sheetName = group.getTitle();
            }
        } catch (Exception e) {
            log.warn("Cannot get site context: {}, {}", params.get("contextString"), e.getMessage());
            return;
        }

        // select only assignments that can be graded by the current user
        List<Assignment> downloadable = assignmentService.getAssignmentsForContext(context).stream()
                .filter(a -> !a.getDraft())
                .filter(a -> assignmentService.allowGradeSubmission(AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getReference()))
                .sorted(new AssignmentComparator())
                .collect(Collectors.toList());

        if (downloadable.isEmpty()) {
            // no assignments to download
            log.warn("No gradable assignments can be downloaded for reference: {}", reference);
        } else {
            Workbook wb = new SXSSFWorkbook();
            Sheet sheet = wb.createSheet(WorkbookUtil.createSafeSheetName(sheetName));

            // cell 0,0 - title
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue(rb.getString("download.spreadsheet.title"));

            // cell 1,0 - empty
            row = sheet.createRow(1);
            row.createCell(0).setCellValue("");

            // cell 2,0 - site title
            row = sheet.createRow(2);
            row.createCell(0).setCellValue(rb.getString("download.spreadsheet.site") + siteTitle);

            // cell 3,0 - download date
            row = sheet.createRow(3);
            row.createCell(0).setCellValue(rb.getString("download.spreadsheet.date") + assignmentService.getUsersLocalDateTimeString(Instant.now()));

            // cell 4,0 - empty
            row = sheet.createRow(4);
            row.createCell(0).setCellValue("");

            CellStyle style = wb.createCellStyle();
            Cell cell;

            // this is the header row number
            final int headerRowNumber = 5;
            // set up the header cells
            row = sheet.createRow(headerRowNumber);
            int cellColumnNum = 0;

            // user name column
            cell = row.createCell(cellColumnNum++);
            cell.setCellStyle(style);
            cell.setCellValue(rb.getString("download.spreadsheet.column.name"));

            // user enterprise id column
            cell = row.createCell(cellColumnNum);
            cell.setCellStyle(style);
            cell.setCellValue(rb.getString("download.spreadsheet.column.userid"));

            // site members excluding those who can add assignments
            // hashmap which stores the Excel row number for particular user

            String refToCheck = group == null ? site.getReference() : group.getReference();
            List<String> allowAddAnySubmissionUsers = assignmentService.allowAddAnySubmissionUsers(refToCheck);
            List<User> members = userDirectoryService.getUsers(allowAddAnySubmissionUsers);
            boolean isNotesEnabled = candidateDetailProvider != null &&
                    site != null && candidateDetailProvider.isAdditionalNotesEnabled(site);
            // For details of all the users in the site.
            Map<String, Submitter> submitterMap = new HashMap<>();
            members.sort(new UserComparator());
            for (User user : members) {
                // put user displayid and sortname in the first two cells
                Submitter submitter = new Submitter(user.getDisplayId(), user.getSortName());
                submitterMap.put(user.getId(), submitter);
                if (isNotesEnabled) {
                    Optional<List<String>> additionalNotes = candidateDetailProvider.getAdditionalNotes(user, site);
                    submitter.setNotes(additionalNotes);
                }
            }

            // We have to build a Map of the results so that we can sort them afterwards so that we don't expose data
            // by having the anonymous results in the same position as the original listing.
            Map<Submitter, List<Object>> results = new TreeMap<>();
            int index = 0;
            int assignmentSize = downloadable.size();
            // the grade data portion starts from the third column, since the first two are used for user's display id and sort name
            for (Assignment a : downloadable) {
                Assignment.GradeType assignmentType = a.getTypeOfGrade();

                int rowNum = headerRowNumber;
                row = sheet.getRow(rowNum++);
                cellColumnNum = (index + 2);
                cell = row.createCell(cellColumnNum); // since the first two column is taken by student id and name
                cell.setCellStyle(style);
                cell.setCellValue(a.getTitle());

                // begin to populate the column for this assignment, iterating through student list
                for (AssignmentSubmission submission : assignmentService.getSubmissions(a)) {
                    if (a.getIsGroup()) {
                        for (AssignmentSubmissionSubmitter submissionSubmitter : submission.getSubmitters()) {
                            String userId = submissionSubmitter.getSubmitter();
                            Submitter submitter = submitterMap.get(userId);

                            if (submitter != null) {
                                // find right row
                                // Get the user ID for this result
                                if (assignmentService.assignmentUsesAnonymousGrading(a)) {
                                    submitter = new Submitter(userId, submitter);
                                }

                                List<Object> objects = results.computeIfAbsent(submitter, k -> new ArrayList<>(Collections.nCopies(assignmentSize, null)));
                                // Create item and fill up if doesn't exist.

                                if (submission.getGraded() && submission.getGrade() != null) {
                                    // graded and released
                                    if (assignmentType == Assignment.GradeType.SCORE_GRADE_TYPE) {
                                        try {
                                            // numeric cell type?
                                            int factor = submission.getAssignment().getScaleFactor();
                                            int dec = (int) Math.log10(factor);

                                            //We get float number no matter the locale it was managed with.
                                            final NumberFormat nbFormat = FormattedText.getNumberFormat(dec, dec, null);
                                            float f = nbFormat.parse(getGrade(submissionSubmitter)).floatValue();

                                            style = wb.createCellStyle();
                                            String format = "#,##0.";
                                            for (int j = 0; j < dec; j++) {
                                                format = format.concat("0");
                                            }
                                            objects.set(index, new FloatCell(format, f));
                                        } catch (Exception e) {
                                            // TODO originally
                                            // objects.set(index, submission.getGradeForUser(userId) == null ? submission.getGrade() : submission.getGradeForUser(userId));
                                            objects.set(index, submissionSubmitter.getGrade() == null ? submission.getGrade() : submissionSubmitter.getGrade());
                                            log.warn("Cannot get grade for assignment submission={}, user={}", submission.getId(), userId);
                                        }
                                    } else {
                                        // String cell type
                                        objects.set(index, submissionSubmitter.getGrade() == null ? submission.getGrade() : submissionSubmitter.getGrade());
                                        // TODO originally
                                        // objects.set(index, submission.getGradeForUser(userId) == null ? submission.getGradeDisplay() : submission.getGradeForUser(userId));
                                    }
                                } else if (submission.getSubmitted() && submission.getDateSubmitted() != null) {
                                    // submitted, but no grade available yet
                                    objects.set(index, rb.getString("gen.nograd"));
                                }
                            } // if
                        }

                    } else {
                        AssignmentSubmissionSubmitter[] submissionSubmitters = submission.getSubmitters().toArray(new AssignmentSubmissionSubmitter[]{});

                        if (submissionSubmitters.length == 0) {
                            continue;
                        }
                        Submitter submitter = submitterMap.get(submissionSubmitters[0].getSubmitter());
                        if (submitter != null) {
	                        // Get the user ID for this result
	                        if (assignmentService.assignmentUsesAnonymousGrading(a)) {
	                            submitter = new Submitter(submission.getId() + " " + rb.getString("grading.anonymous.title"), submitter);
	                        }
                        
	                        List<Object> objects = results.computeIfAbsent(submitter, k -> new ArrayList<>(Collections.nCopies(assignmentSize, null)));
	                        // Create item and fill up if doesn't exist.
	                        // find right row
	
	                        if (submission.getGraded() && submission.getGrade() != null) {
	                            // graded and released
	                            String grade = assignmentService.getGradeDisplay(submission.getGrade(), submission.getAssignment().getTypeOfGrade(), submission.getAssignment().getScaleFactor());
	                            if (assignmentType == Assignment.GradeType.SCORE_GRADE_TYPE) {
	                                try {
	                                    // numeric cell type?
	                                    int factor = submission.getAssignment().getScaleFactor();
	                                    int dec = (int) Math.log10(factor);
	
	                                    //We get float number no matter the locale it was managed with.
	                                    NumberFormat nbFormat = FormattedText.getNumberFormat(dec, dec, null);
	                                    float f = nbFormat.parse(grade).floatValue();
	
	                                    String format = "#,##0.";
	                                    for (int j = 0; j < dec; j++) {
	                                        format = format.concat("0");
	                                    }
	                                    style.setDataFormat(wb.createDataFormat().getFormat(format));
	                                    cell.setCellStyle(style);
	                                    objects.set(index, new FloatCell(format, f));
	                                } catch (Exception e) {
	                                    objects.set(index, grade);
	                                }
	                            } else {
	                                objects.set(index, grade);
	                            }
	                        } else if (submission.getSubmitted() && submission.getDateSubmitted() != null) {
	                            objects.set(index, rb.getString("gen.nograd"));
	                        }
                    	}
                    }
                }
                index++;

                if (isNotesEnabled) {
                    // Add the notes header
                    int cellNum = (index + 2);
                    rowNum = headerRowNumber;
                    row = sheet.getRow(rowNum++);
                    cell = row.createCell(cellNum);
                    cell.setCellType(CellType.STRING);
                    cell.setCellValue(rb.getString("gen.notes"));
                }


                // The map is already sorted and so we just iterate over it and output rows.
                for (Map.Entry<Submitter, List<Object>> entry : results.entrySet()) {
                    Row sheetRow = sheet.createRow(rowNum++);
                    Submitter submitter = entry.getKey();
                    List<Object> rowValues = entry.getValue();
                    int column = 0;
                    if (submitter.anonymous) {
                        sheetRow.createCell(column++).setCellValue("");
                        sheetRow.createCell(column++).setCellValue(submitter.id);
                    } else {
                        sheetRow.createCell(column++).setCellValue(submitter.sortName);
                        sheetRow.createCell(column++).setCellValue(submitter.id);
                    }
                    for (Object rowValue : rowValues) {
                        if (rowValue instanceof FloatCell) {
                            FloatCell floatValue = (FloatCell) rowValue;
                            cell = sheetRow.createCell(column++, CellType.NUMERIC);
                            cell.setCellValue(floatValue.value);
                            style = wb.createCellStyle();
                            style.setDataFormat(wb.createDataFormat().getFormat(floatValue.format));
                            cell.setCellStyle(style);
                        } else if (rowValue != null) {
                            cell = sheetRow.createCell(column++, CellType.STRING);
                            cell.setCellValue(rowValue.toString());
                        } else {
                            cell = sheetRow.createCell(column++, CellType.STRING);
                            cell.setCellValue(rb.getString("listsub.nosub"));
                        }
                    }
                    if (isNotesEnabled) {
                        int col = column;
                        for (String note : submitter.notes) {
                            Cell noteCell = sheetRow.createCell(col++, CellType.STRING);
                            noteCell.setCellValue(note);
                        }
                    }
                }
            }

            try {
                wb.write(out);
            } catch (IOException e) {
                log.warn("Failed to write out spreadsheet:" + e.getMessage());
            }
            finally {
            	try {
            		wb.close();
            	}
            	catch(IOException e) {
            		log.warn("Failed to close spreadsheet:" + e.getMessage());
            	}
            }
        }
    }

	private String getGrade(final AssignmentSubmissionSubmitter submissionSubmitter) {
		final Assignment assignment = submissionSubmitter.getSubmission().getAssignment();
		if (StringUtils
				.isNotBlank(assignment.getProperties()
						.get(AssignmentServiceConstants.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT))
				&& assignmentService.getGradeForUserInGradeBook(assignment.getId(),
						submissionSubmitter.getSubmitter()) != null) {
			return assignmentService.getGradeForUserInGradeBook(assignment.getId(), submissionSubmitter.getSubmitter());
		}
		// TODO originally called submission.getGradeForUser(userId);
		else if (submissionSubmitter.getGrade() != null) {
			return assignmentService.getGradeDisplay(submissionSubmitter.getGrade(), assignment.getTypeOfGrade(),
					assignment.getScaleFactor());
		} else {
			return assignmentService.getGradeDisplay(submissionSubmitter.getSubmission().getGrade(),
					assignment.getTypeOfGrade(), assignment.getScaleFactor());
		}
	}

    // This small holder is so that we can hold details about a floating point number while building up the list.
    // We can't create cells when we don't know where they will go yet.
    private static class FloatCell {
        FloatCell(String format, float value) {
            this.format = format;
            this.value = value;
        }

        String format;
        float value;
    }

    /**
     * This holds details about the submitter for when we're writing out the spreadsheet.
     */
    private class Submitter implements Comparable<Submitter> {
        // Create Anonymous one.
        Submitter(String id, Submitter original) {
            this.anonymous = true;
            this.id = id;
            this.notes = original.notes;
        }

        // Create normal one
        Submitter(String id, String sortName) {
            this.anonymous = false;
            this.id = id;
            this.sortName = sortName;
        }

        boolean anonymous;
        public String id;
        String sortName;
        List<String> notes = Collections.emptyList();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Submitter submitter = (Submitter) o;

            return anonymous == submitter.anonymous && id.equals(submitter.id);

        }

        @Override
        public int hashCode() {
            int result = (anonymous ? 1 : 0);
            result = 31 * result + id.hashCode();
            return result;
        }

        @Override
        public int compareTo(Submitter o) {
            int value = Boolean.compare(this.anonymous, o.anonymous);
            if (value == 0) {
                if (anonymous) {
                    // Sort by ID for anonymous ones
                    value = this.id.compareTo(o.id);
                } else {
                    // Sort by sortName for normal ones.
                    value = this.sortName.compareTo(o.sortName);
                }
            }
            return value;
        }

        void setNotes(Optional<List<String>> notes) {
            notes.ifPresent(strings -> this.notes = strings);
        }
    }
}
