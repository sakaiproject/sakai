/*
 *  Copyright (c) 2017, University of Dayton
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.attendance.tool.pages;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.attendance.logic.SakaiProxy;
import org.sakaiproject.attendance.model.*;
import org.sakaiproject.user.api.User;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.CellType;

import java.io.*;

import java.util.*;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;


/**
 * Created by james on 5/18/17.
 */
@Slf4j
public class ExportPage extends BasePage{
    enum ExportFormat {
        XLS
    }
    private String holder = "";
    private int rowCounter = 0;
    private int userStatsCounter = 0;
    private int repeatPlaceHolder = 0;
    private static final long serialVersionUID = 1L;
    boolean includeComments = false;
    boolean blankSheet = false;
    public ExportPage() {
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        disableLink(exportLink);
        if(this.role != null && this.role.equals("Student")) {
            throw new RestartResponseException(StudentView.class);
        }

        Model<AttendanceSite> siteModel = new Model<>(attendanceLogic.getCurrentAttendanceSite());
        Form<AttendanceSite> exportForm = new Form<>("export-form", siteModel);
        add(exportForm);
        exportForm.add(new AjaxCheckBox("exportIncludeComments", Model.of(this.includeComments)) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
                ExportPage.this.includeComments = !ExportPage.this.includeComments;
                setDefaultModelObject(ExportPage.this.includeComments);
            }
        });

        exportForm.add(new AjaxCheckBox("exportBlankSheet", Model.of(this.blankSheet)) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
                ExportPage.this.blankSheet = !ExportPage.this.blankSheet;
                setDefaultModelObject(ExportPage.this.blankSheet);
            }
        });

        exportForm.add(new DownloadLink("submit-link", new LoadableDetachableModel<File>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected File load() {
                return buildExcelFile(blankSheet, includeComments);
            }
            public void onSubmit() {
                setResponsePage(new ExportPage());
            }
        }).setCacheDuration(Duration.NONE).setDeleteAfterDownload(true));

        add(new UploadForm("form"));
    }

    private File makeEmptyFile(){
        File tempFile;
        try{
            tempFile = File.createTempFile(buildFileNamePrefix(), buildFileNameSuffix());
        }catch(final IOException e) {
            throw new RuntimeException(e);
        }
        return tempFile;
    }

    private File buildExcelFile(boolean blankSheet, boolean commentsOnOff){
        File tempFile = makeEmptyFile();
        setResponsePage(new ExportPage());
        userStatsCounter = 0;
        final HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet mainSheet = wb.createSheet("Export"); // Create new sheet
        final String selectedGroup = null;
        final String siteID = sakaiProxy.getCurrentSiteId();
        final ArrayList<AttendanceEvent> eventHolder = new ArrayList<AttendanceEvent>();
        AttendanceSite attendanceSite = attendanceLogic.getAttendanceSite(siteID);
        List<String> groupIds = sakaiProxy.getAvailableGroupsForCurrentSite();
        List<AttendanceUserGroupStats> finalUserStatsList = new ArrayList<AttendanceUserGroupStats>();
        List<AttendanceUserStats> fullUserList = attendanceLogic.getUserStatsForSite(attendanceLogic.getAttendanceSite(siteID), null);
        AttendanceUserGroupStats finalUserStatsListholder = new AttendanceUserGroupStats();
        HSSFRow headerRow = mainSheet.createRow(0); //create blank header row
        HSSFCellStyle boldStyle = wb.createCellStyle(); //cell styling
        HSSFFont boldFont = wb.createFont();
        boldFont.setBold(true);
        boldFont.setUnderline(HSSFFont.U_SINGLE);
        boldStyle.setFont(boldFont);
        HSSFCell headerCell = headerRow.createCell(0, CellType.STRING); //put the labels in the header row
        headerCell.setCellStyle(boldStyle);
        headerCell.setCellValue("StudentID");
        headerCell = headerRow.createCell(1, CellType.STRING);
        headerCell.setCellStyle(boldStyle);
        headerCell.setCellValue("Student Name");
        headerCell = headerRow.createCell(2, CellType.STRING);
        headerCell.setCellStyle(boldStyle);
        headerCell.setCellValue("Section");
        List<AttendanceEvent> attendanceEventlist = attendanceLogic.getAttendanceEventsForSite(attendanceSite); //make list of events to use for header and data processing
        Collections.sort(attendanceEventlist, new Comparator<AttendanceEvent>() {   //sort the attendanceEvents by date within their array.
            @Override
            public int compare(AttendanceEvent attendanceEvent, AttendanceEvent t1) {
                if ((attendanceEvent.getStartDateTime() == null) && (t1.getStartDateTime() == null)) {
                    return 0;
                } else if (attendanceEvent.getStartDateTime() == null) {
                    return -1;
                } else if (t1.getStartDateTime() == null) {
                    return 1;
                } else {
                    return attendanceEvent.getStartDateTime().compareTo(t1.getStartDateTime());
                }
            }
        });
        int columnGetter = 3;   //separate counter for columns in case there are comments, in which case the following loop's counter will increment differently.
        for(int count=0; count<attendanceEventlist.size(); count++){   //put the actual event info in the rest of the header
            AttendanceEvent now = attendanceEventlist.get(count);   // Count iterates over the attendance events only.
            HSSFCell currentCell = headerRow.createCell(columnGetter, CellType.STRING);   //create and fill in a header cell for every event. We use ColumnGetter instead of Count to iterate because the cells may need to iterate by 2 if there are comments.
            currentCell.setCellStyle(boldStyle);
            String startDate = "";
            try {   // there may not be a date set for the event, so it could be null.
                startDate = now.getStartDateTime().toString();
            } catch (NullPointerException e) {
            }
            currentCell.setCellValue(now.getName() + '[' + startDate + "](" + now.getId().toString() + ')');
            if(commentsOnOff){  //true = has comments. if true, add the comment column for each event.
                columnGetter++;    //increment one extra, to allow for the comment column
                currentCell = headerRow.createCell(columnGetter, CellType.STRING);  //making a second header cell [for Comments] for every event
                currentCell.setCellStyle(boldStyle);
                currentCell.setCellValue(now.getName() + '[' + startDate + "]Comments(" + now.getId().toString() + ')');
            }
            columnGetter++; //increment this here because it's not part of the For loop's header.

        }
        for(int count=0; count<fullUserList.size(); count++){   //loop over the students and add each to a row
            HSSFRow studentRow = mainSheet.createRow(count+1);
            AttendanceUserStats now = fullUserList.get(count);
            User user = sakaiProxy.getUser(now.getUserID());
            String currentUserId = user.getId();
            List<AttendanceRecord> studentData = attendanceLogic.getAttendanceRecordsForUser(currentUserId, attendanceSite);
            HSSFCell currentCell = studentRow.createCell(0, CellType.STRING);   //create labels at the beginning of each student row
            currentCell.setCellValue(user.getEid());
            currentCell = studentRow.createCell(1, CellType.STRING);
            currentCell.setCellValue(user.getSortName());
            currentCell = studentRow.createCell(2, CellType.STRING);
            currentCell.setCellValue(sakaiProxy.getUserGroupWithinSite(groupIds, user.getId(), siteID));
            int columnCounter = 3;  //data columns start at 3 because of eid/name/section being first.
            for(int eventCounter=0; eventCounter< attendanceEventlist.size(); eventCounter++){  //loop over all the events in this site
                Long currentEventId = attendanceEventlist.get(eventCounter).getId();
                for(int studentDataCounter = 0; studentDataCounter<studentData.size(); studentDataCounter++){   //find the student's data for this event.
                    AttendanceRecord currentRecord = studentData.get(studentDataCounter);
                    if(currentRecord.getAttendanceEvent().getId().equals(currentEventId)){  //make a cell for every record that matches an event
                        currentCell = studentRow.createCell(columnCounter, CellType.STRING);
                        if(blankSheet){ //when the user wants a blank sheet exported
                            currentCell.setCellValue("");
                        } else if(currentRecord.getStatus() == null) {
                            currentCell.setCellValue("");
                        } else if(currentRecord.getStatus().toString().equals("PRESENT")) {    //convert the Status to a one-letter abbreviation
                            currentCell.setCellValue("P");
                        } else if (currentRecord.getStatus().toString().equals("UNEXCUSED_ABSENCE")){
                            currentCell.setCellValue("A");
                        } else if (currentRecord.getStatus().toString().equals("EXCUSED_ABSENCE")){
                            currentCell.setCellValue("E");
                        } else if (currentRecord.getStatus().toString().equals("LATE")){
                            currentCell.setCellValue("L");
                        } else if (currentRecord.getStatus().toString().equals("LEFT_EARLY")){
                            currentCell.setCellValue("LE");
                        } else {
                            currentCell.setCellValue("");   //this shows up again as part of the If structure to capture cases in which blankSheet = False but there is no data in CurrentRecord.
                        }
                        if(commentsOnOff){  //true = has comments. if true, add the comment data for each student.
                            columnCounter++;
                            currentCell = studentRow.createCell(columnCounter, CellType.STRING);
                            if(blankSheet){ //we can just stick blank Strings in if the sheet is to be blank.
                                currentCell.setCellValue("");
                            }else{
                                currentCell.setCellValue(currentRecord.getComment());
                            }
                        }
                        break;  //stop iterating when the correct data is found, after it's processed
                    }
                }
                columnCounter++; //increment this here because this counter is not in the For loop's header.
            }
        }
        try {
            return writeFile(wb, tempFile);
        } catch (IOException e) {
            log.error("Error when closing workbook: ", e);
            return null;
        }
    }

    private File writeFile(HSSFWorkbook wb, File tempFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(tempFile)){
            wb.write(fos);
        } catch (IOException e) {
            log.error("Error when closing fileOutputStream: ", e);
        } finally {
            wb.close();
        }
        return tempFile;
    }

    private String buildFileNamePrefix() {
        final String prefix = "attendance_Export-";
        return prefix;
    }

    private String buildFileNameSuffix() {
        return "." + ExportPage.ExportFormat.XLS.toString().toLowerCase();
    }

    private class UploadForm extends Form<Void> {

        FileUploadField fileUploadField;

        public UploadForm(final String id) {
            super(id);

            setMultiPart(true);
            setMaxSize(Bytes.megabytes(2));

            this.fileUploadField = new FileUploadField("upload");
            add(this.fileUploadField);

            SubmitLink submit = new SubmitLink("submitLink");
            add(submit);
        }

        @Override
        public void onSubmit(){
            // Local variables for processing
            final List<AttendanceEvent> siteEventList = new ArrayList<AttendanceEvent>(attendanceLogic.getAttendanceEventsForCurrentSite());
            final List<Long> usableIDs = getActualEventIds(siteEventList);
            List<ImportConfirmList> ICList = new ArrayList<ImportConfirmList>();
            boolean checkHeader = true;
            boolean commentsChanged = false;
            List<String> errors = new ArrayList<String>();
            // Start processing
            HSSFSheet sheet = getIterableExcelSheet();
            if(sheet != null){    //if we weren't able to get a usable sheet, it would be Null and we shouldn't process it.
                Iterator rows = sheet.rowIterator();
                if(rows.hasNext()){ //don't do anything with the Iterator unless it has something.
                    final List<String> headerRow = processHeaderRow(rows);
                    List<Long> badIds = checkHeader(siteEventList, headerRow, errors);
                    checkHeader = badIds.size()<1;  //if there are bad IDs, the header is bad [checkHeader = false]
                    while(rows.hasNext()){
                        HSSFRow currentRow = (HSSFRow) rows.next();
                        commentsChanged = processOneDataRow(currentRow, usableIDs, headerRow, ICList, commentsChanged, errors);
                    }
                }else{
                    getSession().error(getString("attendance.export.import.error.empty_file"));
                    setResponsePage(new ExportPage());
                }
            }
            if(!checkHeader){
                getSession().error(getString("attendance.export.import.error.badHeaderError.submit"));
            }
            addErrorsToSession(errors);
            setResponsePage(new ImportConfirmation(ICList ,commentsChanged));
        }

        private HSSFSheet getIterableExcelSheet(){
            final FileUpload upload = this.fileUploadField.getFileUpload();
            String extension = upload.getClientFileName().substring(upload.getClientFileName().lastIndexOf(".")+1);
            if (upload == null) {
                getSession().error(getString("attendance.export.import.error.null_file"));
                setResponsePage(new Overview());
            } else if (upload.getSize() == 0) {
                getSession().error(getString("attendance.export.import.error.empty_file"));
                setResponsePage(new ExportPage());
            }else if (!(ExportPage.ExportFormat.XLS.toString().toLowerCase()).equals(extension)) {
                getSession().error(getString("attendance.export.import.error.bad_file_format"));
                setResponsePage(new ExportPage());
            }
            HSSFSheet sheet = null;
            try{
                File temp = upload.writeToTempFile();
                FileInputStream fis = new FileInputStream(temp);
                HSSFWorkbook workbook = new HSSFWorkbook(fis);
                sheet = workbook.getSheetAt(0);
            }catch (final Exception e){
                getSession().error(getString("attendance.export.import.save.fileError"));
                setResponsePage(new ExportPage());
            }
            return sheet;
        }

        private List<String> processHeaderRow(Iterator rows){
            HSSFRow row = (HSSFRow) rows.next();
            Iterator cells = row.cellIterator();    //iterate over the cells in the current row
            List data = new ArrayList();    // container for the current row's data
            while (cells.hasNext()) {   // create the arrayList of current row's data
                HSSFCell cell = (HSSFCell) cells.next();
                data.add(cell.toString());
            }
            return data;
        }

        private List<Long> checkHeader(List<AttendanceEvent> siteEventList, List<String> headerRow, List<String> errors){
            List<Long> siteEventListIds = getActualEventIds(siteEventList);
            List<Long> badIds = new ArrayList<Long>();
            for(int count = 3; count < headerRow.size() && headerRow.get(count).length()>0; count++){  //see if every ID in idTracker is in Attendance already
                Long getIdResult = getIdFromString(headerRow.get(count), errors);
                if(!siteEventListIds.contains(getIdResult)){   //if not, this header had bad IDs (events that Attendance doesn't have)
                    badIds.add(getIdResult);   //add the bad ID into the array.
                    errors.add(getString("attendance.import.bad.event"));
                }
                if(headerRow.get(count).contains("]Comments(") && headerRow.size()%2<1){    //if there are comments in the file, there must be an odd number of columns.
                    errors.add(getString("attendance.import.missing.column"));
                }
            }
            return badIds;
        }

        private Long getIdFromString(String cellData, List<String> errors){
            int headerIndexStart = cellData.lastIndexOf("(");
            int headerIndexEnd = cellData.lastIndexOf(")");
            String idHolder = "0";
            try{
                idHolder = cellData.substring(headerIndexStart + 1, headerIndexEnd);
            }catch(StringIndexOutOfBoundsException e){
                errors.add(getString("attendance.import.bad.column"));
            }
            return Long.parseLong(idHolder);
        }

        private List<Long> getActualEventIds (List<AttendanceEvent> siteEventList){
            List<Long> usableIds = new ArrayList<Long>();
            for (int count = 0; count<siteEventList.size(); count++){
                usableIds.add(siteEventList.get(count).getId());
            }
            return usableIds;
        }

        private void addErrorsToSession(List<String> errors){
            for(int count = 0; count<4 && count<errors.size() && errors.size()>0; count++){
                getSession().error(errors.get(count));
            }
            if(errors.size()>4){
                String howManyErrors = "" + (errors.size()-4);
                getSession().error(new StringResourceModel("attendance.import.more.errors", null, new String[]{howManyErrors}).getString());
            }
        }

        private boolean processOneDataRow(HSSFRow row, List<Long> usableIds, List<String> headerRow, List<ImportConfirmList> ICList, boolean commentsChanged, List<String> errors) {
            AttendanceSite attendanceSite = attendanceLogic.getAttendanceSite(sakaiProxy.getCurrentSiteId());
            List data = new ArrayList();    // container for the current row's data
            for (int countCells=0; countCells<headerRow.size(); countCells++) {   // create the arrayList of the current Excel row's data. we'll base it on the length of the header, to avoid reaching out beyond the data's end.
                HSSFCell cell = row.getCell(countCells);
                try {data.add(cell.toString());}    //try adding the next cell's data to the array
                catch (NullPointerException e) {data.add("");}  //if it's null, we'll make it a blank
            }
            String userEID = "";
            try{
                userEID = String.valueOf(data.get(0));
            }catch(IndexOutOfBoundsException e){
                userEID = "0";
            }
            User currentUser = sakaiProxy.getUserByEID(userEID);
            if (currentUser != null) { //if it passes this Boolean condition, that should mean that Attendance has a slot for this user.
                List<AttendanceRecord> oldUserData = attendanceLogic.getAttendanceRecordsForUser(currentUser.getId());   //the student's row of data from Attendance
                for (int count = 3; count<data.size() && count<headerRow.size(); count++) {    //iterate over current student row, starting at 3 to account for id/name/section
                    Long currentID = getIdFromString(headerRow.get(count), errors); //get the eventID of the current cell
                    if (usableIds.contains(currentID)) { //if it's one of the real event IDs, and also not a comment column...
                        AttendanceRecord newData = new AttendanceRecord();  //make a blank AttendanceRecord for the new data and start filling it in
                        ImportConfirmList ICL = new ImportConfirmList();
                        if (data.get(count).equals("P") || (data.get(count).equals("PRESENT"))) {   //take data from Excel and put it in the current Attendance record. should this be a Switch statement instead?
                            newData.setStatus(Status.PRESENT);
                        } else if (data.get(count).equals("A") || (data.get(count).equals("UNEXCUSED_ABSENCE")) || (data.get(count).equals("ABSENT")) || (data.get(count).equals("UNEXCUSED ABSENCE")) || (data.get(count).equals("UNEXCUSED"))) {
                            newData.setStatus(Status.UNEXCUSED_ABSENCE);
                        } else if (data.get(count).equals("E") || (data.get(count).equals("EXCUSED_ABSENCE")) || (data.get(count).equals("EXCUSED ABSENCE")) || (data.get(count).equals("EXCUSED"))) {
                            newData.setStatus(Status.EXCUSED_ABSENCE);
                        } else if (data.get(count).equals("L") || (data.get(count).equals("LATE"))) {
                            newData.setStatus(Status.LATE);
                        } else if (data.get(count).equals("LE") || (data.get(count).equals("LEFT_EARLY")) || (data.get(count).equals("LEFT EARLY"))) {
                            newData.setStatus(Status.LEFT_EARLY);
                        } else {
                            newData.setStatus(Status.UNKNOWN);
                        }
                        Iterator<AttendanceRecord> traverseOldData = oldUserData.iterator();
                        AttendanceRecord checker;
                        while (traverseOldData.hasNext()) {   //get event/record from old data for the eventID we're currently working with
                            checker = traverseOldData.next();
                            if (checker.getAttendanceEvent().getId().equals(currentID)) {   //once we've found the right event ID, put all the data for the current record into the ICL.
                                newData.setUserID(currentUser.getId());
                                newData.setAttendanceEvent(checker.getAttendanceEvent());
                                newData.setId(checker.getId());
                                newData.setComment(checker.getComment());   //set it to the old comment here; it will be replaced if there's a new one.
                                if(!headerRow.get(count).contains("]Comments(") && count+1<headerRow.size()){
                                    if(headerRow.get(count+1).contains("]Comments(")){
                                        try{
                                            newData.setComment(data.get(count+1).toString());
                                            commentsChanged = true;
                                        }catch(IndexOutOfBoundsException e){
                                            errors.add(new StringResourceModel("attendance.import.column.format", null, new String[]{currentID.toString()}).getString());
                                        }
                                        count++;    //increment Count again to move on to the next ID when grabbing the comment.
                                    }
                                }
                                ICL.setAttendanceEvent(checker.getAttendanceEvent()); //start putting everything in the ICL.
                                ICL.setEventName(checker.getAttendanceEvent().getName() + " ");
                                ICL.setOldComment(checker.getComment());
                                ICL.setOldStatus(checker.getStatus());
                                ICL.setAttendanceSite(attendanceSite);
                                ICL.setId(checker.getId());    //Attendace event's ID
                                ICL.setUserID(currentUser.getId()); //we can't use the EID for this...it has to be the longer, hashy one
                                String startDate = "";
                                try {   //we gate the Date inside this Try block because it might be null.
                                    startDate = checker.getAttendanceEvent().getStartDateTime().toString();
                                } catch (NullPointerException e) {/*don't need to do anything here*/}
                                ICL.setEventDate(startDate);   //much of the data, like Event Date, will not change, so it can be grabbed from Checker.
                                if(newData.getComment()!=null && newData.getComment().length() > 0){    //add the newData comment to the ICL if it's not empty.
                                    ICL.setComment(newData.getComment());
                                }else{  //if it IS empty, just throw in the old comment.
                                    ICL.setComment(checker.getComment());
                                }
                                ICL.setAttendanceRecord(newData);   //the other ones that really need to be from the New Data are these last two.
                                ICL.setStatus(newData.getStatus());
                                if (!ICL.getStatus().equals(checker.getStatus()) || !StringUtils.equals(ICL.getComment(), checker.getComment())) { //make sure the new cell doesn't just have the same status/comment as the old one before processing.
                                    ICList.add(ICL);
                                }
                                break;  //stop iterating over oldUserData once we've found the matching event ID and processed its data
                            }
                        }
                    }else{
                        errors.add(new StringResourceModel("attendance.import.bad.event", null, new String[]{currentID.toString()}).getString());
                    }
                }
            }else if (data.size()>2){
                if(data.get(1).toString().length() > 0){
                    errors.add(new StringResourceModel("attendance.import.fake.student", null, new String[]{data.get(1).toString()}).getString());   //when there's a fake student in Excel that isn't in Attendance
                }else{
                    errors.add(getString("attendance.import.blank.row"));   //when there's a blank row in Excel that has no data
                }
            }else{
                errors.add(getString("attendance.import.blank.row"));   //when there's a fake/extra row in Excel that has no data
            }
            return commentsChanged;
        }
    }

    @Override
    public void renderHead(IHeaderResponse response) {
	    super.renderHead(response);
	    final String version = ServerConfigurationService.getString("portal.cdn.version", "");
	    response.render(JavaScriptHeaderItem.forUrl(String.format("javascript/exportPage.js?version=%s", version)));
    }
}
