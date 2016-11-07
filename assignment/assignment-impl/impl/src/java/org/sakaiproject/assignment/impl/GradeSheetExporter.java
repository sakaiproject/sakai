package org.sakaiproject.assignment.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.WorkbookUtil;
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentSubmission;
import org.sakaiproject.assignment.impl.sort.AssignmentComparator;
import org.sakaiproject.assignment.impl.sort.UserComparator;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.SortedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Exporter for the assignments grades spreadsheet. This encapsulates all the Apache POI code.
 */
public class GradeSheetExporter {

    public final Logger log = LoggerFactory.getLogger(GradeSheetExporter.class);

    /** the resource bundle */
    private ResourceLoader rb = new ResourceLoader("assignment");

    private AssignmentService assignmentService;

    public void setAssignmentService(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    /**
     * Access and output the grades spreadsheet for the reference, either for an assignment or all assignments in a context.
     *
     * @param out
     *        The outputStream to stream the grades spreadsheet into.
     * @param ref
     *        The reference, either to a specific assignment, or just to an assignment context.
     * @return Whether the grades spreadsheet is successfully output.
     * @throws IdUnusedException
     *         if there is no object with this id.
     * @throws PermissionException
     *         if the current user is not allowed to access this.
     */
    public boolean getGradesSpreadsheet(final OutputStream out, final String ref)
            throws IdUnusedException, PermissionException {
        boolean retVal = false;
        String typeGradesString = AssignmentService.REF_TYPE_GRADES + Entity.SEPARATOR;
        String [] parts = ref.substring(ref.indexOf(typeGradesString) + typeGradesString.length()).split(Entity.SEPARATOR);
        String idSite = (parts.length>1) ? parts[1] : parts[0];
        String context = (parts.length>1) ? SiteService.siteGroupReference(idSite, parts[3]) : SiteService.siteReference(idSite);

        // get site title for display purpose
        String siteTitle = "";
        String sheetName = "";
        try
        {
            Site site = SiteService.getSite(idSite);
            siteTitle = (parts.length>1)? site.getTitle()+" - "+ site.getGroup(parts[3]).getTitle(): site.getTitle();
            sheetName = (parts.length>1)? site.getGroup(parts[3]).getTitle(): site.getTitle();
        }
        catch (Exception e)
        {
            // ignore exception
            log.debug(this + ":getGradesSpreadsheet cannot get site context=" + idSite + e.getMessage());
        }

        // does current user allowed to grade any assignment?
        boolean allowGradeAny = false;
        List<Assignment> assignmentsList = assignmentService.getListAssignmentsForContext(idSite);
        for (Assignment assignment: assignmentsList)
        {
            if (assignmentService.allowGradeSubmission(assignment.getReference()))
            {
                allowGradeAny = true;
            }
        }

        if (!allowGradeAny)
        {
            // not permitted to download the spreadsheet
            return false;
        }
        else
        {
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
            row.createCell(0).setCellValue(
                    rb.getString("download.spreadsheet.date") + TimeService.newTime().toStringLocalFull());

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

            // user enterprise id column
            cell = row.createCell(cellColumnNum++);
            cell.setCellStyle(style);
            cell.setCellValue(rb.getString("download.spreadsheet.column.name"));

            // user name column
            cell = row.createCell(cellColumnNum++);
            cell.setCellStyle(style);
            cell.setCellValue(rb.getString("download.spreadsheet.column.userid"));

            // starting from this row, going to input user data
            Iterator assignments = new SortedIterator(assignmentsList.iterator(), new AssignmentComparator());

            // site members excluding those who can add assignments
            // hashmap which stores the Excel row number for particular user
            HashMap<String, Integer> user_row = new HashMap<>();

            List<String> allowAddAnySubmissionUsers = assignmentService.allowAddAnySubmissionUsers(context);
            List<User> members = UserDirectoryService.getUsers(allowAddAnySubmissionUsers);
            members.sort(new UserComparator());
            for (User user: members)
            {
                // create the column for user first
                row = sheet.createRow(rowNum);
                // update user_row Hashtable
                user_row.put(user.getId(), rowNum);
                rowNum++;
                // increase row
                // put user displayid and sortname in the first two cells
                row.createCell(0).setCellValue(user.getSortName());
                row.createCell(1).setCellValue(user.getDisplayId());

            }
            rowNum++;

            int index = 0;
            // the grade data portion starts from the third column, since the first two are used for user's display id and sort name
            while (assignments.hasNext())
            {
                Assignment a = (Assignment) assignments.next();

                int assignmentType = a.getContent().getTypeOfGrade();

                // for column header, check allow grade permission based on each assignment
                if (assignmentService.assignmentUsesAnonymousGrading(a)) {
                    // Skip anonymous ones as we don't want to leak grades
                }
                else if(!a.getDraft() && assignmentService.allowGradeSubmission(a.getReference()))
                {
                    // put in assignment title as the column header
                    rowNum = headerRowNumber;
                    row = sheet.getRow(rowNum++);
                    cellColumnNum = (index + 2);
                    cell = row.createCell(cellColumnNum); // since the first two column is taken by student id and name
                    cell.setCellStyle(style);
                    cell.setCellValue(a.getTitle());

                    for (int loopNum = 0; loopNum < members.size(); loopNum++)
                    {
                        // prepopulate the column with the "no submission" string
                        row = sheet.getRow(rowNum++);
                        cell = row.createCell(cellColumnNum);
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                        cell.setCellValue(rb.getString("listsub.nosub"));
                    }

                    // begin to populate the column for this assignment, iterating through student list
                    for (Iterator sIterator=assignmentService.getSubmissions(a).iterator(); sIterator.hasNext();)
                    {
                        AssignmentSubmission submission = (AssignmentSubmission) sIterator.next();

                        String userId = submission.getSubmitterId();

                        if (a.isGroup()) {

                            User[] _users = submission.getSubmitters();
                            for (int i=0; _users != null && i < _users.length; i++) {

                                userId = _users[i].getId();

                                if (user_row.containsKey(userId))
                                {
                                    // find right row
                                    row = sheet.getRow(user_row.get(userId));

                                    if (submission.getGraded() && submission.getGrade() != null)
                                    {
                                        // graded and released
                                        if (assignmentType == 3)
                                        {
                                            try
                                            {
                                                // numeric cell type?
                                                String grade = (StringUtils.trimToNull(a.getProperties().getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT))!=null)?
                                                        submission.getGradeForUserInGradeBook(userId)!=null?
                                                                submission.getGradeForUserInGradeBook(userId):submission.getGradeForUser(userId):submission.getGradeForUser(userId);
                                                if(grade == null)
                                                {
                                                    grade=submission.getGradeDisplay();
                                                }
                                                int factor = submission.getAssignment().getContent().getFactor();
                                                int dec = (int)Math.log10(factor);

                                                //We get float number no matter the locale it was managed with.
                                                NumberFormat nbFormat = FormattedText.getNumberFormat(dec,dec,null);
                                                float f = nbFormat.parse(grade).floatValue();

                                                // remove the String-based cell first
                                                cell = row.getCell(cellColumnNum);
                                                row.removeCell(cell);
                                                // add number based cell
                                                cell=row.createCell(cellColumnNum);
                                                cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                                                cell.setCellValue(f);

                                                style = wb.createCellStyle();
                                                String format ="#,##0.";
                                                for (int j=0; j<dec; j++) {
                                                    format = format.concat("0");
                                                }
                                                style.setDataFormat(wb.createDataFormat().getFormat(format));
                                                cell.setCellStyle(style);
                                            }
                                            catch (Exception e)
                                            {
                                                // if the grade is not numeric, let's make it as String type
                                                // No need to remove the cell and create a new one, as the existing one is String type.
                                                cell = row.getCell(cellColumnNum);
                                                cell.setCellType(Cell.CELL_TYPE_STRING);
                                                cell.setCellValue(submission.getGradeForUser(userId) == null ? submission.getGradeDisplay():
                                                        submission.getGradeForUser(userId));
                                            }
                                        }
                                        else
                                        {
                                            // String cell type
                                            cell = row.getCell(cellColumnNum);
                                            cell.setCellValue(submission.getGradeForUser(userId) == null ? submission.getGradeDisplay():
                                                    submission.getGradeForUser(userId));
                                        }
                                    }
                                    else if (submission.getSubmitted() && submission.getTimeSubmitted() != null)
                                    {
                                        // submitted, but no grade available yet
                                        cell = row.getCell(cellColumnNum);
                                        cell.setCellValue(rb.getString("gen.nograd"));
                                    }
                                } // if
                            }

                        }
                        else
                        {

                            if (user_row.containsKey(userId))
                            {
                                // find right row
                                row = sheet.getRow(user_row.get(userId));

                                if (submission.getGraded() && submission.getGrade() != null)
                                {
                                    // graded and released
                                    if (assignmentType == 3)
                                    {
                                        try
                                        {
                                            // numeric cell type?
                                            String grade = submission.getGradeDisplay();
                                            int factor = submission.getAssignment().getContent().getFactor();
                                            int dec = (int)Math.log10(factor);

                                            //We get float number no matter the locale it was managed with.
                                            NumberFormat nbFormat = FormattedText.getNumberFormat(dec,dec,null);
                                            float f = nbFormat.parse(grade).floatValue();

                                            // remove the String-based cell first
                                            cell = row.getCell(cellColumnNum);
                                            row.removeCell(cell);
                                            // add number based cell
                                            cell=row.createCell(cellColumnNum);
                                            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                                            cell.setCellValue(f);

                                            style = wb.createCellStyle();
                                            String format ="#,##0.";
                                            for (int j=0; j<dec; j++) {
                                                format = format.concat("0");
                                            }
                                            style.setDataFormat(wb.createDataFormat().getFormat(format));
                                            cell.setCellStyle(style);
                                        }
                                        catch (Exception e)
                                        {
                                            // if the grade is not numeric, let's make it as String type
                                            // No need to remove the cell and create a new one, as the existing one is String type.
                                            cell = row.getCell(cellColumnNum);
                                            cell.setCellType(Cell.CELL_TYPE_STRING);
                                            // Setting grade display instead grade.
                                            cell.setCellValue(submission.getGradeDisplay());
                                        }
                                    }
                                    else
                                    {
                                        // String cell type
                                        cell = row.getCell(cellColumnNum);
                                        cell.setCellValue(submission.getGradeDisplay());
                                    }
                                }
                                else if (submission.getSubmitted() && submission.getTimeSubmitted() != null)
                                {
                                    // submitted, but no grade available yet
                                    cell = row.getCell(cellColumnNum);
                                    cell.setCellValue(rb.getString("gen.nograd"));
                                }
                            } // if

                        }
                    }
                }

                index++;

            }

            // output
            try
            {
                wb.write(out);
                retVal = true;
            }
            catch (IOException e)
            {
                log.warn(" getGradesSpreadsheet Can not output the grade spread sheet for reference= " + ref);
            }

            return retVal;
        }

    } // getGradesSpreadsheet


}
