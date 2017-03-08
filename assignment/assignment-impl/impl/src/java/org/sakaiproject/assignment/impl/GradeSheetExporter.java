package org.sakaiproject.assignment.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.WorkbookUtil;
import org.sakaiproject.assignment.api.AssignmentEntity;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.impl.sort.AssignmentComparator;
import org.sakaiproject.assignment.impl.sort.UserComparator;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.user.api.CandidateDetailProvider;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.SortedIterator;

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
    @Setter private TimeService timeService;
    @Setter private UserDirectoryService userDirectoryService;

    private ResourceLoader rb = new ResourceLoader("assignment");

    /**
     * Access and output the grades spreadsheet for the reference, either for an assignment or all assignments in a context.
     *
     * @param out The outputStream to stream the grades spreadsheet into.
     * @param ref The reference, either to a specific assignment, or just to an assignment context.
     * @return Whether the grades spreadsheet is successfully output.
     * @throws IdUnusedException   if there is no object with this id.
     * @throws PermissionException if the current user is not allowed to access this.
     */
    public boolean getGradesSpreadsheet(final OutputStream out, final String ref) throws IdUnusedException, PermissionException {
        boolean retVal = false;
        String typeGradesString = AssignmentServiceConstants.REF_TYPE_GRADES + Entity.SEPARATOR;
        String[] parts = ref.substring(ref.indexOf(typeGradesString) + typeGradesString.length()).split(Entity.SEPARATOR);
        String idSite = (parts.length > 1) ? parts[1] : parts[0];
        String context = (parts.length > 1) ? siteService.siteGroupReference(idSite, parts[3]) : siteService.siteReference(idSite);

        // get site title for display purpose
        String siteTitle = "";
        String sheetName = "";
        Site site = null;
        try {
            site = siteService.getSite(idSite);
            siteTitle = (parts.length > 1) ? site.getTitle() + " - " + site.getGroup(parts[3]).getTitle() : site.getTitle();
            sheetName = (parts.length > 1) ? site.getGroup(parts[3]).getTitle() : site.getTitle();
        } catch (Exception e) {
            // ignore exception
            log.debug(this + ":getGradesSpreadsheet cannot get site context=" + idSite + e.getMessage());
        }

        // does current user allowed to grade any assignment?
        boolean allowGradeAny = false;
        List<Assignment> assignmentsList = assignmentService.getListAssignmentsForContext(idSite);
        for (Assignment assignment : assignmentsList) {
            if (assignmentService.allowGradeSubmission(new AssignmentEntity(assignment).getReference())) {
                allowGradeAny = true;
                break;
            }
        }

        if (!allowGradeAny) {
            // not permitted to download the spreadsheet
            return false;
        } else {
            int rowNum = 0;

            HSSFWorkbook wb = new HSSFWorkbook();

            HSSFSheet sheet = wb.createSheet(WorkbookUtil.createSafeSheetName(sheetName));

            // Create a row and put some cells in it. Rows are 0 based.
            HSSFRow row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(rb.getString("download.spreadsheet.title"));

            // empty line
            row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("");

            // site title
            row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rb.getString("download.spreadsheet.site") + siteTitle);

            // download time
            row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rb.getString("download.spreadsheet.date") + timeService.newTime().toStringLocalFull());

            // empty line
            row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("");

            HSSFCellStyle style = wb.createCellStyle();
            HSSFCell cell;

            // this is the header row number
            int headerRowNumber = rowNum;
            // set up the header cells
            row = sheet.createRow(rowNum++);
            int cellColumnNum = 0;

            // user name column
            cell = row.createCell(cellColumnNum++);
            cell.setCellStyle(style);
            cell.setCellValue(rb.getString("download.spreadsheet.column.name"));

            // user enterprise id column
            cell = row.createCell(cellColumnNum++);
            cell.setCellStyle(style);
            cell.setCellValue(rb.getString("download.spreadsheet.column.userid"));

            // site members excluding those who can add assignments
            // hashmap which stores the Excel row number for particular user

            List<String> allowAddAnySubmissionUsers = assignmentService.allowAddAnySubmissionUsers(context);
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
            rowNum++;

            // We have to build a Map of the results so that we can sort them afterwards so that we don't expose data
            // by having the anonymous results in the same position as the original listing.
            Map<Submitter, List<Object>> results = new TreeMap<>();
            int index = 0;
            int assignmentSize = assignmentsList.size();
            // the grade data portion starts from the third column, since the first two are used for user's display id and sort name
            for (Iterator<Assignment> assignments = new SortedIterator(assignmentsList.iterator(), new AssignmentComparator()); assignments.hasNext(); ) {
                {
                    Assignment a = assignments.next();

                    Assignment.GradeType assignmentType = a.getTypeOfGrade();


                    // for column header, check allow grade permission based on each assignment
                    if (!a.getDraft() && assignmentService.allowGradeSubmission(new AssignmentEntity(a).getReference())) {
                        // put in assignment title as the column header
                        rowNum = headerRowNumber;
                        row = sheet.getRow(rowNum++);
                        cellColumnNum = (index + 2);
                        cell = row.createCell(cellColumnNum); // since the first two column is taken by student id and name
                        cell.setCellStyle(style);
                        cell.setCellValue(a.getTitle());

                        // begin to populate the column for this assignment, iterating through student list
                        for (Iterator sIterator = assignmentService.getSubmissions(a).iterator(); sIterator.hasNext(); ) {
                            AssignmentSubmission submission = (AssignmentSubmission) sIterator.next();


                            if (a.getIsGroup()) {

                                String[] users = submission.getSubmitters().toArray(new String[]{});
                                for (String userId : users) {
                                    Submitter submitter = submitterMap.get(userId);

                                    if (submitter != null) {
                                        // find right row
                                        // Get the user ID for this result
                                        if (assignmentService.assignmentUsesAnonymousGrading(a)) {
                                            submitter = new Submitter(userId, submitter);
                                        }

                                        List<Object> objects = results.get(submitter);
                                        if (objects == null) {
                                            // Create item and fill up if doesn't exist.
                                            objects = new ArrayList<>(Collections.nCopies(assignmentSize, null));
                                            results.put(submitter, objects);
                                        }

                                        if (submission.getGraded() && submission.getGrade() != null) {
                                            // graded and released
                                            if (assignmentType == Assignment.GradeType.SCORE_GRADE_TYPE) {
                                                try {
                                                    // numeric cell type?
                                                    String grade = (StringUtils.trimToNull(a.getProperties().get(AssignmentServiceConstants.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT)) != null) ?
                                                            submission.getGradeForUserInGradeBook(userId) != null ?
                                                                    submission.getGradeForUserInGradeBook(userId) : submission.getGradeForUser(userId) : submission.getGradeForUser(userId);
                                                    if (grade == null) {
                                                        grade = submission.getGrade();
                                                    }
                                                    int factor = submission.getAssignment().getScaleFactor();
                                                    int dec = (int) Math.log10(factor);

                                                    //We get float number no matter the locale it was managed with.
                                                    NumberFormat nbFormat = FormattedText.getNumberFormat(dec, dec, null);
                                                    float f = nbFormat.parse(grade).floatValue();

                                                    style = wb.createCellStyle();
                                                    String format = "#,##0.";
                                                    for (int j = 0; j < dec; j++) {
                                                        format = format.concat("0");
                                                    }
                                                    objects.set(index, new FloatCell(format, f));
                                                } catch (Exception e) {
                                                    objects.set(index, submission.getGradeForUser(userId) == null ? submission.getGrade() :
                                                            submission.getGradeForUser(userId));


                                                }
                                            } else {
                                                // String cell type
                                                objects.set(index, submission.getGradeForUser(userId) == null ? submission.getGradeDisplay() :
                                                        submission.getGradeForUser(userId));
                                            }
                                        } else if (submission.getSubmitted() && submission.getTimeSubmitted() != null) {
                                            // submitted, but no grade available yet
                                            objects.set(index, rb.getString("gen.nograd"));
                                        }
                                    } // if
                                }

                            } else {
                                Submitter submitter = submitterMap.get(submission.getSubmitterId());
                                // If the user doesn't have permission to submit in this site.
                                if (submitter == null) {
                                    continue;
                                }
                                // Get the user ID for this result
                                if (assignmentService.assignmentUsesAnonymousGrading(a)) {
                                    submitter = new Submitter(submission.getAnonymousSubmissionId(), submitter);
                                }

                                List<Object> objects = results.get(submitter);
                                if (objects == null) {
                                    // Create item and fill up if doesn't exist.
                                    objects = new ArrayList<>(Collections.nCopies(assignmentSize, null));
                                    results.put(submitter, objects);
                                }

                                if (submitter != null) {
                                    // find right row

                                    if (submission.getGraded() && submission.getGrade() != null) {
                                        // graded and released
                                        if (assignmentType == Assignment.SCORE_GRADE_TYPE) {
                                            try {
                                                // numeric cell type?
                                                String grade = submission.getGradeDisplay();
                                                int factor = submission.getAssignment().getContent().getFactor();
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
                                                objects.set(index, submission.getGradeDisplay());
                                            }
                                        } else {
                                            objects.set(index, submission.getGradeDisplay());
                                        }
                                    } else if (submission.getSubmitted() && submission.getDateSubmitted() != null) {
                                        objects.set(index, rb.getString("gen.nograd"));
                                    }
                                } // if

                            }
                        }
                    }

                    index++;

                }

                if (isNotesEnabled) {
                    // Add the notes header
                    int cellNum = (index + 2);
                    rowNum = headerRowNumber;
                    row = sheet.getRow(rowNum++);
                    cell = row.createCell(cellNum);
                    cell.setCellType(1);
                    cell.setCellValue(rb.getString("gen.notes"));
                }


                // The map is already sorted and so we just iterate over it and output rows.
                for (Iterator<Map.Entry<Submitter, List<Object>>> resultsIt = results.entrySet().iterator(); resultsIt.hasNext(); ) {
                    Map.Entry<Submitter, List<Object>> entry = resultsIt.next();
                    HSSFRow sheetRow = sheet.createRow(rowNum++);
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
                            cell = sheetRow.createCell(column++, Cell.CELL_TYPE_NUMERIC);
                            cell.setCellValue(floatValue.value);
                            style = wb.createCellStyle();
                            style.setDataFormat(wb.createDataFormat().getFormat(floatValue.format));
                            cell.setCellStyle(style);
                        } else if (rowValue != null) {
                            cell = sheetRow.createCell(column++, Cell.CELL_TYPE_STRING);
                            cell.setCellValue(rowValue.toString());
                        } else {
                            cell = sheetRow.createCell(column++, Cell.CELL_TYPE_STRING);
                            cell.setCellValue(rb.getString("listsub.nosub"));
                        }
                    }
                    if (isNotesEnabled) {
                        final int startColumn = column;
                        submitter.notes.ifPresent(notes -> {
                            int col = startColumn;
                            for (String note : notes) {
                                Cell noteCell = sheetRow.createCell(col++, Cell.CELL_TYPE_STRING);
                                noteCell.setCellValue(note);
                            }
                        });
                    }

                }

            }

            try {
                wb.write(out);
                return true;
            } catch (IOException e) {
                log.warn("Failed to write out spreadsheet:" + e.getMessage());
            }


            return false;

        } // getGradesSpreadsheet

    }

    // This small holder is so that we can hold details about a floating point number while building up the list.
    // We can't create cells when we don't know where they will go yet.
    private static class FloatCell {
        public FloatCell(String format, float value) {
            this.format = format;
            this.value = value;
        }

        public String format;
        public float value;
    }

    /**
     * This holds details about the submitter for when we're writing out the spreadsheet.
     */
    private class Submitter implements Comparable<Submitter> {
        // Create Anonymous one.
        public Submitter(String id, Submitter original) {
            this.anonymous = true;
            this.id = id;
            this.notes = original.notes;
        }

        // Create normal one
        public Submitter(String id, String sortName) {
            this.anonymous = false;
            this.id = id;
            this.sortName = sortName;
        }

        public boolean anonymous;
        public String id;
        public String sortName;
        public Optional<List<String>> notes = Optional.empty();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Submitter submitter = (Submitter) o;

            if (anonymous != submitter.anonymous) return false;
            return id.equals(submitter.id);

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

        public void setNotes(Optional<List<String>> notes) {
            this.notes = notes;
        }
    }


}
