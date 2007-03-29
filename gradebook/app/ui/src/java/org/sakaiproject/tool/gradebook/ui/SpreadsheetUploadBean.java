/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California
 *
 *  Licensed under the Educational Community License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ecl1.php
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/

package org.sakaiproject.tool.gradebook.ui;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingSpreadsheetNameException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Comment;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

public class SpreadsheetUploadBean extends GradebookDependentBean implements Serializable {


    private String title;
    private UploadedFile upFile;
    private static final Log logger = LogFactory.getLog(SpreadsheetUploadBean.class);
    private Spreadsheet spreadsheet;
    private Map rosterMap;
    private List assignmentList;
    private List studentRows;
    private List assignmentHeaders;
    private Map selectedAssignment;
    private List assignmentColumnSelectItems;
    private boolean saved = false;
    private String columnCount;
    private String rowCount;
    private boolean hasUnknownUser;
    private Long spreadsheetId;
    private Map scores;
    private Assignment assignment;
    private Long assignmentId;
    private Integer selectedCommentsColumnId = 0;



    public SpreadsheetUploadBean() {


        //initialize rosteMap which is map of displayid and user objects
        rosterMap = new HashMap();
        List  enrollments = getAvailableEnrollments();
        if(logger.isDebugEnabled()) logger.debug("enrollment size " +enrollments.size());

        Iterator iter;
        iter = enrollments.iterator();
        while(iter.hasNext()){
            EnrollmentRecord enr;
            enr = (EnrollmentRecord)iter.next();
            if(logger.isDebugEnabled()) logger.debug("displayid "+enr.getUser().getDisplayId() + "  userid "+enr.getUser().getUserUid());
            rosterMap.put(enr.getUser().getDisplayId(),enr.getUser());
        }

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UploadedFile getUpFile() {
        return upFile;
    }

    public void setUpFile(UploadedFile upFile) {
        if(logger.isDebugEnabled()) logger.debug("upload file name " + upFile.getName());
        this.upFile = upFile;
    }


    public Spreadsheet getSpreadsheet() {
        return spreadsheet;
    }

    public void setSpreadsheet(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    public Map getRosterMap() {
        return rosterMap;
    }

    public void setRosterMap(Map rosterMap) {
        this.rosterMap = rosterMap;
    }
    public Long getSpreadsheetId() {
        return spreadsheetId;
    }

    public void setSpreadsheetId(Long spreadsheetId) {
        this.spreadsheetId = spreadsheetId;
    }


    public List getSpreadsheets() {
        this.setPageName("spreadsheetListing");
        return getGradebookManager().getSpreadsheets(getGradebookId());
    }

    public String deleteItem(){
        return "spreadsheetRemove";
    }

    public List getAssignmentList() {
        return assignmentList;
    }

    public void setAssignmentList(List assignmentList) {
        this.assignmentList = assignmentList;
    }

    public List getStudentRows() {
        return studentRows;
    }

    public void setStudentRows(List studentRows) {
        this.studentRows = studentRows;
    }

    public List getAssignmentHeaders() {
        return assignmentHeaders;
    }

    public void setAssignmentHeaders(List assignmentHeaders) {
        this.assignmentHeaders = assignmentHeaders;
    }

    public Map getSelectedAssignment() {
        return selectedAssignment;
    }

    public void setSelectedAssignment(Map selectedAssignment) {
        this.selectedAssignment = selectedAssignment;
    }

    public List getAssignmentColumnSelectItems() {
        return assignmentColumnSelectItems;
    }

    public void setAssignmentColumnSelectItems(List assignmentColumnSelectItems) {
        this.assignmentColumnSelectItems = assignmentColumnSelectItems;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }


    public String getColumnCount() {
        return FacesUtil.getLocalizedString("upload_preview_column_count",new String[] {columnCount});
    }

    public void setColumnCount(String columnCount) {
        this.columnCount = columnCount;
    }

    public String getRowCount() {
        return FacesUtil.getLocalizedString("upload_preview_row_count",new String[] {rowCount});
    }

    public void setRowCount(String rowCount) {
        this.rowCount = rowCount;
    }


    public String getRowStyles() {
        StringBuffer sb = new StringBuffer();
        for(Iterator iter = studentRows.iterator(); iter.hasNext();){
            SpreadsheetRow row = (SpreadsheetRow)iter.next();
            if(row.isKnown()){
                sb.append("internal,");
            }else{
                sb.append("external,");
            }
        }
        if(logger.isDebugEnabled())logger.debug(sb.toString());
        return sb.toString();
    }

    public boolean getHasUnknownUser() {
        return hasUnknownUser;
    }

    public void setHasUnknownUser(boolean hasUnknownUser) {
        this.hasUnknownUser = hasUnknownUser;
    }


    public Map getScores() {
        return scores;
    }

    public void setScores(Map scores) {
        this.scores = scores;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    //view file from db
    public String viewItem(){

        if(logger.isDebugEnabled())logger.debug("loading viewItem()");

        org.sakaiproject.tool.gradebook.Spreadsheet sp = getGradebookManager().getSpreadsheet(spreadsheetId);
        StringBuffer sb = new StringBuffer();
        sb.append(sp.getContent());

        List contents = new ArrayList();

        String lineitems[] = sb.toString().split("\n");
        for(int i = 0;i<lineitems.length;i++){
            if(logger.isDebugEnabled())logger.debug("line item contents \n" + lineitems[i]);
            contents.add(lineitems[i]);
        }

        if(logger.isDebugEnabled())logger.debug(sp.toString());

        spreadsheet = new Spreadsheet();
        spreadsheet.setTitle(sp.getName());
        spreadsheet.setDate(sp.getDateCreated());
        spreadsheet.setUserId(sp.getCreator());
        spreadsheet.setLineitems(contents);

        assignmentList = new ArrayList();
        studentRows = new ArrayList();
        assignmentColumnSelectItems = new ArrayList();
        //
        // assignmentHeaders = new ArrayList();

        SpreadsheetHeader header = new SpreadsheetHeader((String) spreadsheet.getLineitems().get(0));
        assignmentHeaders = header.getHeaderWithoutUser();


        //generate spreadsheet rows
        Iterator it = spreadsheet.getLineitems().iterator();
        int rowcount = 0;
        int unknownusers = 0;
        while(it.hasNext()){
            String line = (String) it.next();
            if(rowcount > 0){
                SpreadsheetRow  row = new SpreadsheetRow(line);
                studentRows.add(row);
                //check the number of unkonw users in spreadsheet
                if(!row.isKnown())unknownusers = unknownusers + 1;
                if(logger.isDebugEnabled()) logger.debug("row added" + rowcount);
            }
            rowcount++;
        }
        rowCount = String.valueOf(rowcount - 1);
        if(unknownusers > 0){
            this.hasUnknownUser = true;
        }

        //create a numeric list of assignment headers

        if(logger.isDebugEnabled())logger.debug("creating assignment List ---------");
        for(int i = 0;i<assignmentHeaders.size();i++){
            assignmentList.add(new Integer(i));
            if(logger.isDebugEnabled()) logger.debug("col added" + i);
        }
        columnCount = String.valueOf(assignmentHeaders.size());


        for(int i = 0;i<assignmentHeaders.size();i++){
            SelectItem item = new  SelectItem(new Integer(i + 1),(String)assignmentHeaders.get(i));
            if(logger.isDebugEnabled()) logger.debug("creating selectItems "+ item.getValue());
            assignmentColumnSelectItems.add(item);
        }

        if(logger.isDebugEnabled()) logger.debug("Map initialized " +studentRows.size());
        if(logger.isDebugEnabled()) logger.debug("assignmentList " +assignmentList.size());


        return "spreadsheetPreview";
    }


    //process file and preview
    public String processFile() throws Exception {

        if(logger.isDebugEnabled()) logger.debug("check if upFile is intialized");
        if(upFile == null){
            if(logger.isDebugEnabled()) logger.debug("upFile not initialized");
            FacesUtil.addErrorMessage(getLocalizedString("upload_view_failure"));
            return null;
        }

        if(logger.isDebugEnabled()){
            logger.debug("file size " + upFile.getSize() + "file name " + upFile.getName() + "file Content Type " + upFile.getContentType() + "");
        }

        if(logger.isDebugEnabled()) logger.debug("check that the file is csv file");
        if(!upFile.getName().endsWith("csv")){
            FacesUtil.addErrorMessage(getLocalizedString("upload_view_filetype_error",new String[] {upFile.getName()}));
            return null;
        }
        /**
         logger.debug("check that file content type");

         logger.debug("check the file content type");
         if(!upFile.getContentType().equalsIgnoreCase("application/vnd.ms-excel")){
         FacesUtil.addErrorMessage(getLocalizedString("upload_view_filetype_error",new String[] {upFile.getName()}));
         return null;
         }
         **/

        InputStream inputStream = new BufferedInputStream(upFile.getInputStream());
        List contents;
        contents = csvtoArray(inputStream);
        spreadsheet = new Spreadsheet();
        spreadsheet.setDate(new Date());
        spreadsheet.setTitle(this.getTitle());
        spreadsheet.setFilename(upFile.getName());
        spreadsheet.setLineitems(contents);

        assignmentList = new ArrayList();
        studentRows = new ArrayList();
        assignmentColumnSelectItems = new ArrayList();
        //
        // assignmentHeaders = new ArrayList();

        SpreadsheetHeader header;
        try{
            header = new SpreadsheetHeader((String) spreadsheet.getLineitems().get(0));
            assignmentHeaders = header.getHeaderWithoutUser();
        }catch(IndexOutOfBoundsException ioe){
            if(logger.isDebugEnabled())logger.debug(ioe + " there is a problem with the uploaded spreadsheet");
            FacesUtil.addErrorMessage(getLocalizedString("upload_view_filecontent_error"));
            return null;
        }

        //generate spreadsheet rows
        Iterator it = spreadsheet.getLineitems().iterator();
        int rowcount = 0;
        int unknownusers = 0;
        while(it.hasNext()){
            String line = (String) it.next();
            if(rowcount > 0){
                SpreadsheetRow  row = new SpreadsheetRow(line);
                studentRows.add(row);
                //check the number of unkonw users in spreadsheet
                if(!row.isKnown())unknownusers = unknownusers + 1;
                if(logger.isDebugEnabled()) logger.debug("row added" + rowcount);
            }
            rowcount++;
        }
        rowCount = String.valueOf(rowcount - 1);
        if(unknownusers > 0){
            this.hasUnknownUser = true;
        }

        //create a numeric list of assignment headers

        if(logger.isDebugEnabled())logger.debug("creating assignment List ---------");
        for(int i = 0;i<assignmentHeaders.size();i++){
            assignmentList.add(new Integer(i));
            if(logger.isDebugEnabled()) logger.debug("col added" + i);
        }
        columnCount = String.valueOf(assignmentHeaders.size());


        for(int i = 0;i<assignmentHeaders.size();i++){
            SelectItem item = new  SelectItem(new Integer(i + 1),(String)assignmentHeaders.get(i));
            if(logger.isDebugEnabled()) logger.debug("creating selectItems "+ item.getValue());
            assignmentColumnSelectItems.add(item);
        }

        if(logger.isDebugEnabled()) logger.debug("Map initialized " +studentRows.size());
        if(logger.isDebugEnabled()) logger.debug("assignmentList " +assignmentList.size());

        if(studentRows.size() < 1){
            FacesUtil.addErrorMessage(getLocalizedString("upload_view_filecontent_error"));
            return null;
        }


        return "spreadsheetUploadPreview";
    }

    /**
     * method converts an input stream to an List consist of strings
     * representing a line
     *
     * @param inputStream
     * @return contents
     */
    private List csvtoArray(InputStream inputStream) throws IOException{

        /**
         * TODO this well probably be removed
         */

        if(logger.isDebugEnabled()) logger.debug("csvtoArray()");
        List contents = new ArrayList();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while((line = reader.readLine())!=null){
            //logger.debug("contents of line: "+line);
            contents.add(line);
        }
        return contents;

    }

    /**
     * method to save CSV to database
     *
     * @return String
     */
    public String saveFile(){

        StringBuffer sb = new StringBuffer();
        List contents =  spreadsheet.getLineitems();
        Iterator it = contents.iterator();
        while(it.hasNext()){
            String line = (String) it.next();
            sb.append(line + '\n');
        }

        String filename = spreadsheet.getFilename();

        /** temporary presistence code
         *
         */
        if(logger.isDebugEnabled())logger.debug("string to save "+sb.toString());
        try{
            getGradebookManager().createSpreadsheet(getGradebookId(),spreadsheet.getTitle(),this.getUserDirectoryService().getUserDisplayName(getUserUid()),new Date(),sb.toString());
        }catch(ConflictingSpreadsheetNameException e){
            if(logger.isDebugEnabled())logger.debug(e);
            FacesUtil.addErrorMessage(getLocalizedString("upload_preview_save_failure"));
            return null;
        }
        FacesUtil.addRedirectSafeMessage(getLocalizedString("upload_preview_save_confirmation", new String[] {filename}));

        this.setPageName("spreadsheetListing");
        return "spreadsheetListing";
    }

    public String importData(){

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();

        if(logger.isDebugEnabled())logger.debug("processFile()");
        String selectedColumn =  request.getParameter("form:assignment");
        if(logger.isDebugEnabled())logger.debug("the selected column is " + selectedColumn);

        selectedAssignment = new HashMap();
        try{
            selectedAssignment.put("Assignment", assignmentHeaders.get(Integer.parseInt(selectedColumn) - 1));
        }catch(Exception e){
            if(logger.isDebugEnabled())logger.debug("no assignment selected");
            FacesUtil.addErrorMessage(getLocalizedString("import_preview_assignment_selection_failure"));
            return null;
        }

        Iterator it = studentRows.iterator();
        if(logger.isDebugEnabled())logger.debug("number of student rows "+studentRows.size() );
         int i = 0;
         while(it.hasNext()){

             if(logger.isDebugEnabled())logger.debug("row " + i);
             SpreadsheetRow row = (SpreadsheetRow) it.next();
             List line = row.getRowcontent();

             String userid = "";
             String user = (String)line.get(0);
             try{
                 userid = ((User)rosterMap.get(line.get(0))).getUserUid();
             }catch(Exception e){
                 if(logger.isDebugEnabled())logger.debug("user "+ user + "is not known to the system");
                 userid = "";
             }
             
             String points;
             try{
            	 int index = Integer.parseInt(selectedColumn);
            	 if(line.size() > index) {
                     points = (String) line.get(index);
            	 } else {
            		 logger.info("unable to find any points for " + userid + " in spreadsheet");
            		 points = "";
            	 }
             }catch(NumberFormatException e){
                 if(logger.isDebugEnabled())logger.error(e);
                 points = "";
             }
             if(logger.isDebugEnabled())logger.debug("user "+user + " userid " + userid +" points "+points);
             if(!"".equals(points) && (!"".equals(userid))){
                 selectedAssignment.put(userid,points);
             }
             i++;
         }
        if(logger.isDebugEnabled())logger.debug("scores to import "+ i);

        spreadsheet.setSelectedAssignment(selectedAssignment);

        if(assignment == null) {
            assignment = new Assignment();
            assignment.setReleased(true);
        }

        try{
            scores =  spreadsheet.getSelectedAssignment();
            assignment.setName((String) scores.get("Assignment"));
        }catch(NullPointerException npe){
            if(logger.isDebugEnabled()) logger.debug("scores not set");
        }

        return "spreadsheetImport";
    }
    //save grades and comments
    public String saveGrades(){

        if(logger.isDebugEnabled())logger.debug("create assignment and save grades");
        if(logger.isDebugEnabled()) logger.debug("first check if all variables are numeric");

        logger.debug("********************" + scores);
        
        Iterator iter = scores.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry entry  = (Map.Entry) iter.next();
            String points =  (String) entry.getValue();
            try{
                if(logger.isDebugEnabled()) logger.debug("checking if " +points +" is a numeric value");
                if(!entry.getKey().equals("Assignment"))Double.parseDouble(points);
            }catch(NumberFormatException e){
                if(logger.isDebugEnabled()) logger.debug(points + " is not a numeric value");
                FacesUtil.addRedirectSafeMessage(getLocalizedString("import_assignment_notsupported"));

                return "spreadsheetPreview";
            }

        }


        try {
            assignmentId = getGradebookManager().createAssignment(getGradebookId(), assignment.getName(), assignment.getPointsPossible(), assignment.getDueDate(), new Boolean(assignment.isNotCounted()),new Boolean(assignment.isReleased()));
            FacesUtil.addRedirectSafeMessage(getLocalizedString("add_assignment_save", new String[] {assignment.getName()}));

            assignment = getGradebookManager().getAssignment(assignmentId);
            List gradeRecords = new ArrayList();

            //initialize comment List
            List comments = new ArrayList();
            //check if a comments column is selected for the defalt select item value is
            // 0 which mean no comments to be imported
            if(selectedCommentsColumnId!=null && selectedCommentsColumnId > 0) comments = createCommentList(assignment);

            if(logger.isDebugEnabled())logger.debug("remove title entry form map");
            scores.remove("Assignment");
            if(logger.isDebugEnabled())logger.debug("iterate through scores and and save assignment grades");

            Iterator it = scores.entrySet().iterator();
            while(it.hasNext()){

                Map.Entry entry = (Map.Entry) it.next();
                String uid = (String) entry.getKey();
                String points = (String) entry.getValue();
                AssignmentGradeRecord asnGradeRecord = new AssignmentGradeRecord(assignment,uid,Double.valueOf(points));
                gradeRecords.add(asnGradeRecord);
                if(logger.isDebugEnabled())logger.debug("added grades for " + uid + " - points scored " +points);

            }

            if(logger.isDebugEnabled())logger.debug("persist grade records to database");
            getGradebookManager().updateAssignmentGradesAndComments(assignment,gradeRecords,comments);
            getGradebookBean().getEventTrackingService().postEvent("gradebook.importItem","/gradebook/"+getGradebookId()+"/"+assignment.getName()+"/"+getAuthzLevel());
            return  "spreadsheetPreview";
        } catch (ConflictingAssignmentNameException e) {
            if(logger.isErrorEnabled())logger.error(e);
            FacesUtil.addErrorMessage(getLocalizedString("add_assignment_name_conflict_failure"));
        }


        return null;
    }


    /**
     * method creates a collection of comment
     * objects from a the saved spreadsheet and
     * selected column. requires an assignment as parameter to set the
     * gradableObject property of the comment
     *
     * @param assignmentTobeCommented
     * @return  List of comment comment objects
     */
    public List createCommentList(Assignment assignmentTobeCommented){

        List comments = new ArrayList();
        Iterator it = studentRows.iterator();
        while(it.hasNext()){
            SpreadsheetRow row = (SpreadsheetRow) it.next();
            List line = row.getRowcontent();

            String userid = "";
            String user = (String)line.get(0);
            try{
                userid = ((User)rosterMap.get(line.get(0))).getUserUid();
                String commentText = (String) line.get( selectedCommentsColumnId.intValue());
                if((!commentText.equals(""))){
                    Comment comment = new Comment(userid,commentText,assignmentTobeCommented);
                    comments.add(comment);
                }
            }catch(Exception e){
                if(logger.isDebugEnabled())logger.debug("student is required  and "+ user + "is not known to this gradebook");
            }
        }
        return comments;
    }



    public Integer getSelectedCommentsColumnId() {
        return selectedCommentsColumnId;
    }

    public void setSelectedCommentsColumnId(Integer selectedCommentsColumnId) {
        this.selectedCommentsColumnId = selectedCommentsColumnId;
    }

    /**
     *
     */

    public class Spreadsheet  implements Serializable {

        private String title;
        private Date date;
        private String userId;
        private String contents;
        private String displayName;
        private Long gradebookId;
        private String filename;
        private List lineitems;
        private Map selectedAssignment;



        public Spreadsheet(String title, Date date, String userId, String contents) {

            if(logger.isDebugEnabled())logger.debug("loading Spreadsheet()");
            this.title = title;
            this.date = date;
            this.userId = userId;
            this.contents = contents;
        }

        public Spreadsheet() {
            if(logger.isDebugEnabled())logger.debug("loading Spreadsheet()");
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContents() {
            return contents;
        }

        public void setContents(String contents) {
            this.contents = contents;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public Long getGradebookId() {
            return gradebookId;
        }

        public void setGradebookId(Long gradebookId) {
            this.gradebookId = gradebookId;
        }


        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }


        public List getLineitems() {
            return lineitems;
        }

        public void setLineitems(List lineitems) {
            this.lineitems = lineitems;
        }


        public Map getSelectedAssignment() {
            //logger.debug(this.selectedAssignment);
            return selectedAssignment;
        }

        public void setSelectedAssignment(Map selectedAssignment) {
            this.selectedAssignment = selectedAssignment;
            //logger.debug(selectedAssignment);
        }

        public String toString() {
            return "Spreadsheet{" +
                    "title='" + title + '\'' +
                    ", date=" + date +
                    ", userId='" + userId + '\'' +
                    ", contents='" + contents + '\'' +
                    ", displayName='" + displayName + '\'' +
                    ", gradebookId=" + gradebookId +
                    ", filename='" + filename + '\'' +
                    ", lineitems=" + lineitems +
                    ", selectedAssignment=" + selectedAssignment +
                    '}';
        }
    }




    /**
     * spreadsheet header class
     */

    public class SpreadsheetHeader implements Serializable{

        private List header;
        private int columnCount;

        public List getHeader() {
            return header;
        }

        public void setHeader(List header) {
            this.header = header;
        }

        public int getColumnCount() {
            return columnCount;
        }

        public void setColumnCount(int columnCount) {
            this.columnCount = columnCount;
        }


        public List getHeaderWithoutUser() {
            List head = header;
            head.remove(0);
            return head;
        }


        public SpreadsheetHeader(String source) {


            if(logger.isDebugEnabled()) logger.debug("creating header from "+source);
            header = new ArrayList();
            CSV csv = new CSV();
            header = csv.parse(source);
            columnCount = header.size();

        }

    }

    /**
     * spreadsheetRow class
     */
    public class SpreadsheetRow implements Serializable {

        private List rowcontent;
        private int columnCount;
        private String userDisplayName;
        private String userId;
        private String userUid;
        private boolean isKnown;

        public SpreadsheetRow(String source) {


            if(logger.isDebugEnabled()) logger.debug("creating row from string " + source);
            rowcontent = new ArrayList();
            CSV csv = new CSV();
            rowcontent = csv.parse(source);

            try {
                if(logger.isDebugEnabled()) logger.debug("getuser name for "+ rowcontent.get(0));
                //userDisplayName = getUserDirectoryService().getUserDisplayName(tokens[0]);
                userId = (String) rowcontent.get(0);
                userDisplayName = ((User)rosterMap.get(userId)).getDisplayName();
                userUid = ((User)rosterMap.get(userId)).getUserUid();
                isKnown  = true;
                if(logger.isDebugEnabled())logger.debug("get userid "+ rowcontent.get(0) + "username is "+userDisplayName);

            } catch (NullPointerException e) {
                logger.error("User " + rowcontent.get(0) + " is unknown to this gradebook: " + e);
                userDisplayName = "unknown student";
                userId = (String) rowcontent.get(0);
                userUid = null;
                isKnown = false;

            }

        }

        public List getRowcontent() {

            return rowcontent;
        }

        public void setRowcontent(List rowcontent) {
            this.rowcontent = rowcontent;
        }


        public int getColumnCount() {
            return columnCount;
        }

        public void setColumnCount(int columnCount) {
            this.columnCount = columnCount;
        }

        public String getUserDisplayName() {
            return userDisplayName;
        }

        public void setUserDisplayName(String userDisplayName) {
            this.userDisplayName = userDisplayName;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserUid() {
            return userUid;
        }

        public void setUserUid(String userUid) {
            this.userUid = userUid;
        }

        public boolean isKnown() {
            return isKnown;
        }

        public void setKnown(boolean known) {
            isKnown = known;
        }
    }

    //csv class to handle

    /** Parse comma-separated values (CSV), a common Windows file format.
     * Sample input: "LU",86.25,"11/4/1998","2:19PM",+4.0625
     * <p>
     * Inner logic adapted from a C++ original that was
     * Copyright (C) 1999 Lucent Technologies
     * Excerpted from 'The Practice of Programming'
     * by Brian W. Kernighan and Rob Pike.
     * <p>
     * Included by permission of the http://tpop.awl.com/ web site,
     * which says:
     * "You may use this code for any purpose, as long as you leave
     * the copyright notice and book citation attached." I have done so.
     * @author Brian W. Kernighan and Rob Pike (C++ original)
     * @author Ian F. Darwin (translation into Java and removal of I/O)
     * @author Ben Ballard (rewrote advQuoted to handle '""' and for readability)
     */
    class CSV {

        public static final char DEFAULT_SEP = ',';

        /** Construct a CSV parser, with the default separator (`,'). */
        public CSV() {
            this(DEFAULT_SEP);
        }

        /** Construct a CSV parser with a given separator.
         * @param sep The single char for the separator (not a list of
         * separator characters)
         */
        public CSV(char sep) {
            fieldSep = sep;
        }

        /** The fields in the current String */
        protected List list = new ArrayList();

        /** the separator char for this parser */
        protected char fieldSep;

        /** parse: break the input String into fields
         * @return java.util.Iterator containing each field
         * from the original as a String, in order.
         */
        public List parse(String line)
        {
            StringBuffer sb = new StringBuffer();
            list.clear();      // recycle to initial state
            int i = 0;

            if (line.length() == 0) {
                list.add(line);
                return list;
            }

            do {
                sb.setLength(0);
                if (i < line.length() && line.charAt(i) == '"')
                    i = advQuoted(line, sb, ++i);  // skip quote
                else
                    i = advPlain(line, sb, i);
                list.add(sb.toString());
                i++;
            } while (i < line.length());
            if(logger.isDebugEnabled()) {
                StringBuffer logBuffer = new StringBuffer("Parsed " + line + " as: ");
                for(Iterator iter = list.iterator(); iter.hasNext();) {
                	logBuffer.append(iter.next());
                	if(iter.hasNext()) {
                		logBuffer.append(", ");
                	}
                }
                logger.debug("Parsed source string " + line + " as " + logBuffer.toString() + ", length=" + list.size());
            }
            return list;

        }

        /** advQuoted: quoted field; return index of next separator */
        protected int advQuoted(String s, StringBuffer sb, int i)
        {
            int j;
            int len= s.length();
            for (j=i; j<len; j++) {
                if (s.charAt(j) == '"' && j+1 < len) {
                    if (s.charAt(j+1) == '"') {
                        j++; // skip escape char
                    } else if (s.charAt(j+1) == fieldSep) { //next delimeter
                        j++; // skip end quotes
                        break;
                    }
                } else if (s.charAt(j) == '"' && j+1 == len) { // end quotes at end of line
                    break; //done
                }
                sb.append(s.charAt(j));  // regular character.
            }
            return j;
        }

        /** advPlain: unquoted field; return index of next separator */
        protected int advPlain(String s, StringBuffer sb, int i)
        {
            int j;

            j = s.indexOf(fieldSep, i); // look for separator
            if (j == -1) {                 // none found
                sb.append(s.substring(i));
                return s.length();
            } else {
                sb.append(s.substring(i, j));
                return j;
            }
        }
    }
}



