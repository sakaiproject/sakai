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
import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.assignment.impl.sort.AssignmentComparator;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.CandidateDetailProvider;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.comparator.UserSortNameComparator;
import org.springframework.util.comparator.NullSafeComparator;

import lombok.Getter;
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
    @Setter private FormattedText formattedText;

    @Setter private ResourceLoader rb = new ResourceLoader("assignment");

    /**
    * A comparator that sorts by student sortName
    */
    private static final Comparator<Submitter> SUBMITTER_NAME_COMPARATOR = new Comparator<Submitter>() {
        Collator collator;
        {
            this.collator = Collator.getInstance();
            try {
                this.collator = new RuleBasedCollator(
                        ((RuleBasedCollator) this.collator).getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
            } catch (final ParseException e) {
                log.warn(this + " Cannot init RuleBasedCollator. Will use the default Collator instead.", e);
            }
        }

        @Override
        public int compare(final Submitter s1, final Submitter s2) {
            return new NullSafeComparator<>(collator, false).compare(s1.getSortName(), s2.getSortName());
        }
    };

    /**
     * @param reference The reference, either to a specific assignment, or just to an assignment context.
     * @param query The query string from the request with the following params contextString, viewString, searchString, searchFilterOnly
     *              <br/>see <b>AssignmentAction#build_instructor_report_submissions()</b>
     */
    public Optional<Workbook> getGradesSpreadsheet(final String query) {

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
            return Optional.empty();
        }

        // select only assignments that can be graded by the current user
        List<Assignment> downloadable = assignmentService.getAssignmentsForContext(context).stream()
                .filter(a -> !a.getDraft())
                .filter(a -> assignmentService.allowGradeSubmission(AssignmentReferenceReckoner.reckoner().assignment(a).reckon().getReference()))
                .sorted(new AssignmentComparator())
                .collect(Collectors.toList());

        if (downloadable.isEmpty()) {
            // no assignments to download
            log.warn("No gradable assignments can be downloaded for context: {}", context);
            return Optional.empty();
        } else {
            // site members excluding those who can add assignments
            // hashmap which stores the Excel row number for particular user

            String refToCheck = group == null ? site.getReference() : group.getReference();
            List<String> allowAddAnySubmissionUsers = assignmentService.allowAddAnySubmissionUsers(refToCheck);
            List<User> members = userDirectoryService.getUsers(allowAddAnySubmissionUsers);
            boolean isNotesEnabled = candidateDetailProvider != null &&
                    site != null && candidateDetailProvider.isAdditionalNotesEnabled(site);
            // For details of all the users in the site.
            Map<String, Submitter> submitterMap = new HashMap<>();
            members.sort(new UserSortNameComparator());
            for (User user : members) {
                // put user displayid and sortname in the first two cells
                Submitter submitter = new Submitter(user.getDisplayId(), user.getSortName());
                submitterMap.put(user.getId(), submitter);
                if (isNotesEnabled) {
                    Optional<List<String>> additionalNotes = candidateDetailProvider.getAdditionalNotes(user, site);
                    submitter.setNotes(additionalNotes);
                }
            }

            Workbook wb = new SXSSFWorkbook(6 + members.size());
            Sheet sheet = wb.createSheet(WorkbookUtil.createSafeSheetName(sheetName));

            int exportRowcount = 0;

            // cell 0,0 - title
            sheet.createRow(exportRowcount++).createCell(0).setCellValue(rb.getString("download.spreadsheet.title"));

            // cell 1,0 - empty
            sheet.createRow(exportRowcount++).createCell(0).setCellValue("");

            // cell 2,0 - site title
            sheet.createRow(exportRowcount++).createCell(0).setCellValue(rb.getString("download.spreadsheet.site") + siteTitle);

            // cell 3,0 - download date
            sheet.createRow(exportRowcount++).createCell(0).setCellValue(rb.getString("download.spreadsheet.date") + assignmentService.getUsersLocalDateTimeString(Instant.now()));

            // cell 4,0 - empty
            sheet.createRow(exportRowcount++).createCell(0).setCellValue("");

            CellStyle style = wb.createCellStyle();
            Cell cell;

            // this is the header row number
            // set up the header cells
            int headerRowCellCount = 0;
            Row tableHeaderRow = sheet.createRow(exportRowcount++);

            // user name column
            tableHeaderRow.createCell(headerRowCellCount++).setCellValue(rb.getString("download.spreadsheet.column.name"));

            // user enterprise id column
            tableHeaderRow.createCell(headerRowCellCount++).setCellValue(rb.getString("download.spreadsheet.column.userid"));

            // Assignment title
            tableHeaderRow.createCell(headerRowCellCount++).setCellValue(rb.getString("download.spreadsheet.column.asn.title"));

            // Grade
            tableHeaderRow.createCell(headerRowCellCount++).setCellValue(rb.getString("download.spreadsheet.column.asn.grade"));

            // Scale
            tableHeaderRow.createCell(headerRowCellCount++).setCellValue(rb.getString("download.spreadsheet.column.asn.scale"));

            // Submission Date
            tableHeaderRow.createCell(headerRowCellCount++).setCellValue(rb.getString("download.spreadsheet.column.submitted"));

            // Is submission Late
            tableHeaderRow.createCell(headerRowCellCount++).setCellValue(rb.getString("download.spreadsheet.column.late"));

            // Assignment notes
            if (isNotesEnabled) {
                tableHeaderRow.createCell(headerRowCellCount++).setCellValue(rb.getString("gen.notes"));
            }

            // We have to build a Map of the results so that we can sort them afterwards so that we don't expose data
            // by having the anonymous results in the same position as the original listing.
            Map<Submitter, SubmissionInfo> results = new TreeMap<>();
            // the grade data portion starts from the third column, since the first two are used for user's display id and sort name
            for (Assignment assignment : downloadable) {
                Assignment.GradeType assignmentTypeOfGrade = assignment.getTypeOfGrade();
                boolean isAnonymousGrading = assignmentService.assignmentUsesAnonymousGrading(assignment);
                boolean isGroupAssignment = assignment.getIsGroup();
                // begin to populate the column for this assignment, iterating through student list
                for (AssignmentSubmission submission : assignmentService.getSubmissions(assignment)) {
                    if (isGroupAssignment) {
                        for (AssignmentSubmissionSubmitter submissionSubmitter : submission.getSubmitters()) {
                            String userId = submissionSubmitter.getSubmitter();
                            Submitter submitter = submitterMap.get(userId);
                            SubmissionInfo submissionInfo = null;

                            if (submitter != null) {

                                // Set the groupId for group submissions
                                submitter.setGroupId(submission.getGroupId());

                                // find right row
                                // Get the user ID for this result
                                if (isAnonymousGrading) {
                                    submitter = new Submitter(userId, submitter);
                                }

                                if (submission.getGraded() && submission.getGrade() != null) {
                                    // graded and released
                                    if (assignmentTypeOfGrade == Assignment.GradeType.SCORE_GRADE_TYPE) {
                                        try {
                                            // numeric cell type?
                                            int factor = submission.getAssignment().getScaleFactor();
                                            int dec = (int) Math.log10(factor);

                                            //We get float number no matter the locale it was managed with.
                                            final NumberFormat nbFormat = formattedText.getNumberFormat(dec, dec, null);
                                            double f = nbFormat.parse(getGrade(submissionSubmitter)).doubleValue();

                                            style = wb.createCellStyle();
                                            String format = "#,##0.";
                                            for (int j = 0; j < dec; j++) {
                                                format = format.concat("0");
                                            }
                                            submissionInfo = new SubmissionInfo(new FloatCell(format, f), submission);
                                        } catch (Exception e) {
                                            // TODO originally
                                            submissionInfo = new SubmissionInfo(submissionSubmitter.getGrade() == null ? submission.getGrade() : submissionSubmitter.getGrade(), submission);
                                            log.warn("Cannot get grade for assignment submission={}, user={}", submission.getId(), userId);
                                        }
                                    } else {
                                        // String cell type
                                        submissionInfo = new SubmissionInfo(submissionSubmitter.getGrade() == null ? submission.getGrade() : submissionSubmitter.getGrade(), submission);
                                    }
                                } else if (submission.getSubmitted() && submission.getDateSubmitted() != null) {
                                    // submitted, but no grade available yet
                                    submissionInfo = new SubmissionInfo(rb.getString("gen.nograd"), submission);
                                }
                                results.put(submitter, submissionInfo);
                            } // if
                        }

                    } else {
                        AssignmentSubmissionSubmitter[] submissionSubmitters = submission.getSubmitters().toArray(new AssignmentSubmissionSubmitter[]{});

                        if (submissionSubmitters.length == 0) {
                            continue;
                        }
                        Submitter submitter = submitterMap.get(submissionSubmitters[0].getSubmitter());
                        SubmissionInfo submissionInfo = null;
                        if (submitter != null) {
                            // Get the user ID for this result
                            if (isAnonymousGrading) {
                                submitter = new Submitter(submission.getId() + " " + rb.getString("grading.anonymous.title"), submitter);
                            }

                            // Create item and fill up if doesn't exist.
                            // find right row
                            if ("true".equals(params.get("estimate"))) {
                                if (submission.getSubmitted() && submission.getDateSubmitted() != null && (submissionSubmitters[0].getTimeSpent() == null || StringUtils.isBlank(submissionSubmitters[0].getTimeSpent()))) {
                                    submissionInfo = new SubmissionInfo(rb.getString("gen.noestimate"), submission);
                                } else {
                                    submissionInfo = new SubmissionInfo(submissionSubmitters[0].getTimeSpent(), submission);
                                }
                            }

                            if (submission.getGraded() && submission.getGrade() != null) {
                                // graded and released
                                String grade = assignmentService.getGradeForSubmitter(submission, submissionSubmitters[0].getSubmitter());

                                if (assignmentTypeOfGrade == Assignment.GradeType.SCORE_GRADE_TYPE) {
                                    try {
                                        // numeric cell type?
                                        int factor = submission.getAssignment().getScaleFactor();
                                        int dec = (int) Math.log10(factor);

                                        //We get float number no matter the locale it was managed with.
                                        NumberFormat nbFormat = formattedText.getNumberFormat(dec, dec, null);
                                        double f = nbFormat.parse(grade).doubleValue();

                                        String format = "#,##0.";
                                        for (int j = 0; j < dec; j++) {
                                            format = format.concat("0");
                                        }
                                        style.setDataFormat(wb.createDataFormat().getFormat(format));
                                        submissionInfo = new SubmissionInfo(new FloatCell(format, f), submission);
                                    } catch (Exception e) {
                                        submissionInfo = new SubmissionInfo(grade, submission);
                                    }
                                } else {
                                    submissionInfo = new SubmissionInfo(grade, submission);
                                }
                            } else if (submission.getSubmitted() && submission.getDateSubmitted() != null) {
                                submissionInfo = new SubmissionInfo(rb.getString("gen.nograd"), submission);
                            }
                            results.put(submitter, submissionInfo);
                        }
                    }
                }

                final List<Submitter> submitters = new ArrayList<>(results.keySet());
                Collections.sort(submitters, SUBMITTER_NAME_COMPARATOR);

                for (final Submitter submitter : submitters) {
                    SubmissionInfo submissionInfo = results.get(submitter);
                    int column = 0;
                    Row sheetRow = null;
                    if (!submitter.anonymous) {
                        sheetRow = sheet.createRow(exportRowcount++);
                        // User name
                        String userName = submitter.sortName;
                        // If the assignment has group submissions, it's good to set the name of the group too.
                        if (isGroupAssignment && submitter.getGroupId() != null) {
                            Group submissionGroup = site.getGroup(submitter.getGroupId());
                            if (submissionGroup != null) {
                                userName = String.format(" %s [%s]", userName, submissionGroup.getTitle());
                            }
                        }
                        sheetRow.createCell(column++).setCellValue(userName);
                        // User Id
                        sheetRow.createCell(column++).setCellValue(submitter.id);
                        // Assignment title
                        sheetRow.createCell(column++).setCellValue(assignment.getTitle());

                        if (submissionInfo != null && submissionInfo.grade instanceof FloatCell) {
                            FloatCell floatValue = (FloatCell) submissionInfo.grade;
                            cell = sheetRow.createCell(column++, CellType.NUMERIC);
                            cell.setCellValue(floatValue.value);
                            style = wb.createCellStyle();
                            style.setDataFormat(wb.createDataFormat().getFormat(floatValue.format));
                            cell.setCellStyle(style);
                        } else {
                            cell = sheetRow.createCell(column++, CellType.STRING);
                            cell.setCellValue(submissionInfo != null && submissionInfo.grade != null ? submissionInfo.grade.toString() : rb.getString("listsub.nosub"));
                        }

                        // Scale
                        String scaleValue = "";
                        switch (assignmentTypeOfGrade) {
                            case CHECK_GRADE_TYPE:
                                scaleValue = rb.getString("check");
                                break;
                            case LETTER_GRADE_TYPE:
                                scaleValue = "A-F";
                                break;
                            case PASS_FAIL_GRADE_TYPE:
                                scaleValue = rb.getString("gen.pf");
                                break;
                            case SCORE_GRADE_TYPE:
                                scaleValue = "0-"+assignmentService.getMaxPointGradeDisplay(assignment.getScaleFactor(), assignment.getMaxGradePoint());
                                break;
                            case UNGRADED_GRADE_TYPE:
                                scaleValue = rb.getString("gen.nograd");
                                break;
                            case GRADE_TYPE_NONE:
                            default:
                                scaleValue = rb.getString("gen.notset");
                                break;
                        }
                        sheetRow.createCell(column++).setCellValue(scaleValue);

                        // Date submitted and Late.
                        CellStyle dateCellStyle = wb.createCellStyle();
                        CreationHelper createHelper = wb.getCreationHelper();
                        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("mm/dd/yyyy hh:mm"));

                        cell = sheetRow.createCell(column++, CellType.NUMERIC);
                        cell.setCellStyle(dateCellStyle);
                        if (submissionInfo != null && submissionInfo.dateSubmitted != null) {
                            cell.setCellValue(Date.from(submissionInfo.dateSubmitted));
                            cell = sheetRow.createCell(column++, CellType.BOOLEAN);
                            cell.setCellValue(submissionInfo.late);
                        } else {
                            cell.setCellStyle(null);
                            cell.setCellValue(0);
                            cell = sheetRow.createCell(column++, CellType.BOOLEAN);
                            cell.setCellValue(false);
                        }
                        if (isNotesEnabled) {
                            for (String note : submitter.notes) {
                                Cell noteCell = sheetRow.createCell(column++, CellType.STRING);
                                noteCell.setCellValue(note);
                            }
                        }
                    }
                }
            }

            return Optional.of(wb);
        }
    }

    /**
     * Access and output the grades spreadsheet for the reference, either for an assignment or all assignments in a context.
     *
     * @param out The outputStream to stream the grades spreadsheet into.
     * @param reference The reference, either to a specific assignment, or just to an assignment context.
     * @param query The query string from the request with the following params contextString, viewString, searchString, searchFilterOnly
     *              <br/>see <b>AssignmentAction#build_instructor_report_submissions()</b>
     */
    public void writeGradesSpreadsheet(final OutputStream out, final String query) {

        getGradesSpreadsheet(query).ifPresent(wb -> {

            try  {
                wb.write(out);
            } catch (IOException e) {
                log.warn("Failed to write out spreadsheet:" + e.getMessage());
            } finally {
                try {
                    wb.close();
                } catch(IOException e) {
                    log.warn("Failed to close spreadsheet:" + e.getMessage());
                }
            }
        });
    }

    private String getGrade(final AssignmentSubmissionSubmitter submissionSubmitter) {
        return assignmentService.getGradeForSubmitter(submissionSubmitter.getSubmission(), submissionSubmitter.getSubmitter());
    }

    // This small holder is so that we can hold details about a floating point number while building up the list.
    // We can't create cells when we don't know where they will go yet.
    private static class FloatCell {
        FloatCell(String format, double value) {
            this.format = format;
            this.value = value;
        }

        String format;
        double value;
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
        @Getter @Setter
        public String groupId;
        String sortName;
        List<String> notes = Collections.emptyList();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Submitter submitter = (Submitter) o;

            return anonymous == submitter.anonymous && id.equals(submitter.id) && groupId.equals(submitter.groupId);

        }

        @Override
        public int hashCode() {
            int result = (anonymous ? 1 : 0);
            result = 31 * result + id.hashCode();
            return result;
        }

        @Override
        public int compareTo(Submitter o) {
            // Sort by sortName for normal ones, but id if they're the same
            return new CompareToBuilder().append(this.sortName, o.sortName).append(this.id, o.id).append(this.groupId, o.groupId).toComparison();
        }

        void setNotes(Optional<List<String>> notes) {
            notes.ifPresent(strings -> this.notes = strings);
        }

        public String getSortName() {
            return sortName;
        }

        public void setSortName(String sortName) {
            this.sortName = sortName;
        }
    }

    private class SubmissionInfo {

        Object grade;
        Instant dateSubmitted;
        boolean late = false;

        SubmissionInfo(Object grade, AssignmentSubmission submission) {

            this.grade = grade;
            this.dateSubmitted = submission.getDateSubmitted();

            if (this.dateSubmitted != null) {
                this.late = submission.getDateSubmitted().isAfter(submission.getAssignment().getDueDate());
            }
        }
    }
}
