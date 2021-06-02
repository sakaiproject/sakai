/******************************************************************************
 * Copyright (c) 2021 Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.sakaiproject.postem.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import org.sakaiproject.api.app.postem.data.Gradebook;
import org.sakaiproject.api.app.postem.data.GradebookManager;
import org.sakaiproject.api.app.postem.data.StudentGrades;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.postem.constants.PostemToolConstants;
import org.sakaiproject.postem.helpers.CSV;
import org.sakaiproject.postem.helpers.URLConnectionReader;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class PostemSakaiService  {

    protected ArrayList<Gradebook> gradebooks;
    protected String userId;
    protected String userEid;
    protected String siteId = null;
    protected boolean ascending = true;
    protected String sortBy = Gradebook.SORT_BY_TITLE;
    protected Gradebook currentGradebook;
    private Boolean editable;
    protected TreeMap<String, String> studentMap;
    protected String csv = null;
    protected String newTemplate;

    private static final int TITLE_MAX_LENGTH = 255;
    private static final int HEADING_MAX_LENGTH = 500;

    protected static final String FILE_UPLOAD_MAX_SIZE = "20";
    protected static final String STATE_CONTENT_SERVICE = "DbContentService";

    protected boolean withHeader = true;
    char csvDelim = CSV.COMMA_DELIM;

    /** kernel api **/
    private static SecurityService securityService  = ComponentManager.get(SecurityService.class);

    private ToolSession toolSession;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private ToolManager toolManager;

    @Autowired
    private UserDirectoryService userDirectoryService;

    @Autowired
    private GradebookManager gradebookManager;

    @Autowired
    private ContentHostingService contentHostingService;

    @Autowired
    private AuthzGroupService authzGroupService;

    @Autowired
    private ServerConfigurationService serverConfigurationService;

    @Autowired
    private SiteService siteService;

    @Autowired
    private NotificationService notificationService;

    public List<Gradebook> getGradebooks(String sortBy, boolean ascending) {
        String userId = sessionManager.getCurrentSessionUserId();

        if (null != userId) {
            try {
                userEid = userDirectoryService.getUserEid(userId);
            } catch (UserNotDefinedException e) {
                log.error("UserNotDefinedException", e);
            }
        }

        Placement placement = toolManager.getCurrentPlacement();
        String currentSiteId = placement.getContext();

        siteId = currentSiteId;
        List<Gradebook> gradebooks = new ArrayList<>();
        try {
            if (checkAccess()) {
                gradebooks = new ArrayList<Gradebook>(gradebookManager
                        .getGradebooksByContext(siteId, sortBy, ascending));
            } else {
                gradebooks = new ArrayList<Gradebook>(gradebookManager
                        .getReleasedGradebooksByContext(siteId, sortBy, ascending));
            }
        } catch (Exception e) {
            gradebooks = null;
        }

        return gradebooks;
    }

    public TreeMap<String, String> processGradebookView(Long gradebookId) {
        if (isEditable()) {
            currentGradebook = gradebookManager.getGradebookByIdWithHeadings(gradebookId);
            currentGradebook.setUsernames(gradebookManager.getUsernamesInGradebook(currentGradebook));
            studentMap = currentGradebook.getStudentMap();
            return studentMap;
        }

        // otherwise, just load what we need for the current user
        currentGradebook = gradebookManager.getGradebookByIdWithHeadings(gradebookId);

        return studentMap;
    }

    public String processDelete(Long gradebookId) {
        try {
            if (!checkAccess()) {
                throw new PermissionException(sessionManager.getCurrentSessionUserId(),
                        "syllabus_access_athz", "");
            }

        } catch (PermissionException e) {
            return PostemToolConstants.RESULT_KO;
        }
        Gradebook currentGradebook = gradebookManager.getGradebookByIdWithHeadingsAndStudents(gradebookId);
        gradebookManager.deleteGradebook(currentGradebook);
        return PostemToolConstants.RESULT_OK;
    }

    public CSV processCsvDownload(Long gradebookId) {
        try {
            if (!checkAccess()) {
                throw new PermissionException(sessionManager.getCurrentSessionUserId(),
                        "syllabus_access_athz", "");
            }

        } catch (PermissionException e) {
            return null;
        }
        currentGradebook = gradebookManager.getGradebookByIdWithHeadingsAndStudents(gradebookId);

        List<List<String>> csvContents = new ArrayList<>();
        if (currentGradebook.getHeadings().size() > 0) {
            csvContents.add(currentGradebook.getHeadings());
        }
        Iterator si = currentGradebook.getStudents().iterator();
        while (si.hasNext()) {
            List<String> sgl = new ArrayList<>();
            StudentGrades sg = (StudentGrades) si.next();
            sgl.add(sg.getUsername());
            sgl.addAll(sg.getGrades());
            csvContents.add(sgl);
        }

        CSV newCsv = new CSV(csvContents, currentGradebook.getHeadings().size() > 0);
        return newCsv;
    }

    public boolean checkAccess() {
        return siteService.allowUpdateSite(toolManager.getCurrentPlacement().getContext());
    }

    public boolean isEditable() {
        if (null == editable) {
            editable = checkAccess();
        }
        return editable;
    }

    public Gradebook getGradebookById2(Long gradebookId) {
        return gradebookManager.getGradebookByIdWithHeadingsAndStudents(gradebookId);
    }

    public StudentGrades getStudentByGBAndUsername(Gradebook currentGradebook, String selectedStudent) {
        return gradebookManager.getStudentByGBAndUsername(currentGradebook, selectedStudent);
    }

    public String doDragDropUpload (MultipartFile file, HttpServletRequest request) {
        String maxFileSizeMb = serverConfigurationService.getString(ContentHostingService.SAK_PROP_MAX_UPLOAD_FILE_SIZE);
        long maxBytes = 1024L * 1024L;
        String fileName = "";
        toolSession = sessionManager.getCurrentToolSession();

        try {
            maxBytes = Long.parseLong(maxFileSizeMb) * 1024L * 1024L;
        } catch(Exception e) {
            if (null == maxFileSizeMb || maxFileSizeMb.isEmpty()) {
                maxFileSizeMb = "20"; //default MB
            }
            maxBytes = 1024L * 1024L;
        }

        if (null == file) {
            //The user submitted an empty file
            return PostemToolConstants.MISSING_CSV;
        }

        if (file.isEmpty()) {
            return PostemToolConstants.EMPTY_FILE;

        } else if (null == file.getResource().getFilename() || file.getResource().getFilename().length() == 0) {
            return PostemToolConstants.GENERIC_UPLOAD_ERROR;

        } else if (file.getResource().getFilename().length() > 0) {
            fileName = FilenameUtils.getName(file.getResource().getFilename());
            InputStream fileContentStream = null;
            try {
                fileContentStream = file.getInputStream();
            } catch (IOException e1) {
                return PostemToolConstants.GENERIC_UPLOAD_ERROR;
            }

            long contentLength = file.getSize();

            if (contentLength >= maxBytes) {
                return PostemToolConstants.FILE_TOO_BIG;

            } else if (null != fileContentStream) {
                SecurityAdvisor advisor = new SecurityAdvisor() {
                    public SecurityAdvice isAllowed(String userId, String function, String reference) {
                        return SecurityAdvice.ALLOWED;
                    }
                };
                securityService.pushAdvisor(advisor);
                try {
                    String siteId = toolManager.getCurrentPlacement().getContext();
                    String toolName = toolManager.getCurrentPlacement().getTitle();
                    String collection = Entity.SEPARATOR + "attachment" + Entity.SEPARATOR + siteId + Entity.SEPARATOR + toolName + Entity.SEPARATOR;
                    int lastIndexOf = fileName.lastIndexOf("/");
                    if (lastIndexOf != -1 && (fileName.length() > lastIndexOf + 1)) {
                        fileName = fileName.substring(lastIndexOf + 1);
                    }
                    String suffix = "";
                    String finalFileName = "";
                    lastIndexOf = fileName.lastIndexOf(".");
                    if (lastIndexOf != -1 && (fileName.length() > lastIndexOf + 1)) {
                        suffix = fileName.substring(lastIndexOf + 1);
                        finalFileName = fileName.substring(0, lastIndexOf);
                    }
                    try {
                        contentHostingService.checkCollection(collection);
                    } catch (Exception e) {
                        ContentCollectionEdit toolEdit = contentHostingService.addCollection(collection);
                        contentHostingService.commitCollection(toolEdit);
                    }
                    if (collection.length() + finalFileName.length() + suffix.length() > TITLE_MAX_LENGTH) {
                        return PostemToolConstants.NAME_FILE_TOO_LONG;
                    }
                    ContentResourceEdit edit = contentHostingService.addResource(collection, finalFileName, suffix, 99999);
                    edit.setContent(fileContentStream);
                    contentHostingService.commitResource(edit, NotificationService.NOTI_NONE);
                    toolSession.setAttribute("attachmentId", edit.getId());
                } catch (Exception e) {
                    log.error("Failed to store file.", e);
                    fileName = "";
                    toolSession.setAttribute("attachmentId", "");
                    toolSession.setAttribute("currentGradebook", null);
                    return PostemToolConstants.GENERIC_UPLOAD_ERROR;
                } finally {
                    securityService.popAdvisor(advisor);
                }

            }

        }
        return PostemToolConstants.RESULT_OK;
    }

    public Gradebook createEmptyGradebook(String creator, String context) {
        Gradebook gradebook = gradebookManager.createEmptyGradebook(creator, context);
        return gradebook;
    }

    public String processCreate(Gradebook currentGradebook, boolean isGradebookUpdate) {

        toolSession = sessionManager.getCurrentToolSession();

        try {
            if (!this.checkAccess()) {
                throw new PermissionException(sessionManager.getCurrentSessionUserId(),
                        "syllabus_access_athz", "");
            }

        } catch (PermissionException e) {
            return PostemToolConstants.PERMISSION_ERROR;
        }

        if (null != currentGradebook && null != currentGradebook.getTitle()) {
            ArrayList<Gradebook> gb = getGradebooks();
            List<Gradebook> result = gb.stream().filter(gradeb -> gradeb.getTitle().equals(currentGradebook.getTitle()))
                    .filter(gradeb -> !gradeb.getId().equals(currentGradebook.getId())).collect(Collectors.toList());
            if (result.size()>0) {
                    return PostemToolConstants.DUPLICATE_TITLE;
            }
        }

        if (null == currentGradebook || currentGradebook.getTitle().equals("")) {
            return PostemToolConstants.MISSING_TITLE;
        } else if (currentGradebook.getTitle().trim().length() > TITLE_MAX_LENGTH) {
            return PostemToolConstants.TITLE_TOO_LONG;
        }

        if (null != toolSession.getAttribute("attachmentId")) {
            try {
                //Read the data from attachment
                String attachmentId = (String) toolSession.getAttribute("attachmentId");
                ContentResource cr = contentHostingService.getResource(attachmentId);
                //Check the type
                if (ResourceProperties.TYPE_URL.equalsIgnoreCase(cr.getContentType())) {
                    //Going to need to read from a stream
                    String csvURL = new String(cr.getContent());
                    //Load the URL
                    csv = URLConnectionReader.getText(csvURL);
                    if (log.isDebugEnabled()) {
                        log.debug(csv);
                    }
                }
                else {
                    // check that file is actually a CSV file
                    if (!cr.getContentType().equalsIgnoreCase("text/csv")) {
                        return PostemToolConstants.INVALID_EXT;
                    }

                    csv = new String(cr.getContent());
                    if (log.isDebugEnabled()) {
                        log.debug(csv);
                    }
                }
                CSV grades = new CSV(csv, withHeader, csvDelim);

                if (withHeader) {
                    if (null != grades.getHeaders()) {

                        List<String> headingList = grades.getHeaders();
                        for(int col=0; col < headingList.size(); col++) {
                            String heading = (String)headingList.get(col).toString().trim();
                            if (heading.equals("") && grades.getStudents().size() == 0) {
                                return PostemToolConstants.EMPTY_FILE;
                            }
                            // Make sure there are no blank headings
                            if (null == heading || heading.equals("")) {
                                return PostemToolConstants.BLANK_HEADINGS;
                            }
                            // Make sure the headings don't exceed max limit
                            if (heading.length() > HEADING_MAX_LENGTH) {
                                return PostemToolConstants.HEADING_TOO_LONG;
                            }
                        }
                        currentGradebook.setHeadings(headingList);
                    }
                }

                if (null != grades.getStudents()) {
                    if (grades.getStudents().size() == 0) {
                      return PostemToolConstants.CSV_WITHOUT_STUDENTS;
                    }

                    String usernamesValid =  usernamesValid(grades);
                    if (null != usernamesValid) {
                        return usernamesValid;
                    }

                    if (hasADuplicateUserName(grades)) {
                      return PostemToolConstants.HAS_DUPLICATE_USERNAME;
                    }
                }

                List<String> slist = grades.getStudents();
                currentGradebook.setStudents(new TreeSet());
                Iterator si = slist.iterator();
                while (si.hasNext()) {
                    List<String> ss = (List<String>) si.next();
                    String uname = ss.remove(0).trim();
                    gradebookManager.createStudentGradesInGradebook(uname, ss,
                            currentGradebook);
                    if (currentGradebook.getStudents().size() == 1) {
                        currentGradebook.setFirstUploadedUsername(uname);
                    }
                }
            } catch (Exception exception) {
                return PostemToolConstants.GENERIC_UPLOAD_ERROR;
            }
        }

        currentGradebook.setLastUpdated(new Timestamp(new Date().getTime()));
        currentGradebook.setLastUpdater(sessionManager.getCurrentSessionUserId());

        if (isGradebookUpdate) {
            String resultDelete = processDelete(currentGradebook.getId());
            if (resultDelete.equals(PostemToolConstants.RESULT_KO)) {
                return PostemToolConstants.CSV_DELETE_FAIL;
            }
        }

        return PostemToolConstants.RESULT_OK;
    }

    public ArrayList<Gradebook> getGradebooks() {
        String userId = sessionManager.getCurrentSessionUserId();

        if (null != userId) {
            try {
                userEid = userDirectoryService.getUserEid(userId);
            } catch (UserNotDefinedException e) {
                log.error("UserNotDefinedException", e);
            }
        }

        Placement placement = toolManager.getCurrentPlacement();
        String currentSiteId = placement.getContext();

        siteId = currentSiteId;
        try {
            if (checkAccess()) {
                gradebooks = new ArrayList<Gradebook>(gradebookManager
                        .getGradebooksByContext(siteId, sortBy, ascending));
            } else {
                gradebooks = new ArrayList<Gradebook>(gradebookManager
                        .getReleasedGradebooksByContext(siteId, sortBy, ascending));
            }
        } catch (Exception e) {
            gradebooks = null;
        }

        return gradebooks;
    }

    /**
     * Establish a security advisor to allow the "embedded" azg work to occur
     * with no need for additional security permissions.
     */
    protected void enableSecurityAdvisor() {
        // put in a security advisor so we can create citationAdmin site without need
        // of further permissions
        securityService.pushAdvisor(new SecurityAdvisor() {
            public SecurityAdvice isAllowed(String userId, String function, String reference) {
                return SecurityAdvice.ALLOWED;
            }
        });
    }

    /**
     * remove all security advisors
     */
    protected void disableSecurityAdvisors() {
        // remove all security advisors
        securityService.popAdvisor();
    }

    private boolean hasADuplicateUserName(CSV studentGrades) {
        List<String> usernameList = studentGrades.getStudentUsernames();
        List<String> duplicatesList = new ArrayList<>();

        while (usernameList.size() > 0) {
            String username = (String)usernameList.get(0);
            usernameList.remove(username);
            if (usernameList.contains(username)
                    && !duplicatesList.contains(username)) {
                duplicatesList.add(username);
            }
        }

        if (duplicatesList.size() <= 0) {
            return false;
        }

        return true;
    }

    public String getDuplicateUserNames() {

        String duplicates = null;
        try {
            toolSession = sessionManager.getCurrentToolSession();
            //Read the data from attachment
            String attachmentId = (String) toolSession.getAttribute("attachmentId");
            ContentResource cr = contentHostingService.getResource(attachmentId);
            //Check the type
            if (ResourceProperties.TYPE_URL.equalsIgnoreCase(cr.getContentType())) {
                //Going to need to read from a stream
                String csvURL = new String(cr.getContent());
                //Load the URL
                csv = URLConnectionReader.getText(csvURL);
                if (log.isDebugEnabled()) {
                    log.debug(csv);
                }
            } else {
                // check that file is actually a CSV file
                if (!cr.getContentType().equalsIgnoreCase("text/csv")) {
                    return PostemToolConstants.INVALID_EXT;
                }

                csv = new String(cr.getContent());
                if (log.isDebugEnabled()) {
                    log.debug(csv);
                }
            }

            CSV grades = new CSV(csv, withHeader, csvDelim);
            List<String> userNameList = grades.getStudentUsernames();
            List<String> duplicatesList = new ArrayList<>();

            while (userNameList.size() > 0) {
                String userName = (String)userNameList.get(0);
                userNameList.remove(userName);
                if (userNameList.contains(userName)
                        && !duplicatesList.contains(userName)) {
                    duplicatesList.add(userName);
                }
            }

            if (duplicatesList.size() <= 0) {
                return duplicates;
            }

            duplicates = (String) duplicatesList.stream().collect(Collectors.joining(", "));

        } catch (Exception exception) {
            log.error("getDuplicateUserNamesException:", exception);
        }
        return duplicates;
    }

    private String usernamesValid(CSV studentGrades) {
        String usersAreValid = null;
        List<Integer> blankRows = new ArrayList<>();
        List<String> invalidUsernames = new ArrayList<>();
        int row=1;

        List<String> siteMembers = getSiteMembers();

        List<String> studentList = studentGrades.getStudentUsernames();
        Iterator studentIter = studentList.iterator();
        while (studentIter.hasNext()) {
            row++;
            String usr = (String) studentIter.next();

            if (log.isDebugEnabled()) {
                log.debug("usernamesValid : username=" + usr);
                log.debug("usernamesValid : siteMembers" + siteMembers);
            }
            if (null == usr || usr.equals("")) {
                usersAreValid = null;
                blankRows.add(new Integer(row));
            } else if (null == siteMembers || (null != siteMembers && !siteMembers.contains(getUserDefined(usr)))) {
                usersAreValid = null;
                invalidUsernames.add(usr);
            }
        }

        if (blankRows.size() >= 1) {
            usersAreValid = PostemToolConstants.BLANK_ROWS;
        }

        if (invalidUsernames.size() >= 1) {
            usersAreValid = PostemToolConstants.USER_NAME_INVALID;
        }

        return usersAreValid;
    }

    private List<String> getSiteMembers() {
        List<String> siteMembers = new ArrayList<>();
        try {
            AuthzGroup realm = authzGroupService.getAuthzGroup("/site/" + getCurrentSiteId());
            siteMembers = new ArrayList<>(realm.getUsers());
        }
        catch (GroupNotDefinedException e) {
            log.error("GroupNotDefinedException:", e);
        }

        return siteMembers;
    }

    private String getCurrentSiteId() {
        Placement placement = toolManager.getCurrentPlacement();
        return placement.getContext();
    }

    //Returns getUser and getUserByEid on the input string
    //@return Either the id of the user, or the same string if not defined
    private String getUserDefined(String usr) {
        //Set the original user id
        String userId = usr;
        User userinfo;
        try {
            userinfo = userDirectoryService.getUser(usr);
            userId = userinfo.getId();
            if (log.isDebugEnabled()) {
                log.debug("getUserDefined: username for " + usr + " is " + userId);
            }
            return userId;
        } catch (UserNotDefinedException e) {
            try {
                // try with the user eid
                userinfo = userDirectoryService.getUserByEid(usr);
                userId = userinfo.getId();
            } catch (UserNotDefinedException ee) {
                //This is mostly expected behavior, don't need to notify about it, the UI can handle it
                if (log.isDebugEnabled()) {
                    log.debug("getUserDefined: User Not Defined" + userId);
                }
            }
        }
        return userId;
    }

    public String processCreateOk(Gradebook currentGradebook) {
        try {
            gradebookManager.saveGradebook(currentGradebook);
        } catch (Exception e) {
            log.warn("processCreateOk, {}", e.toString());
            return PostemToolConstants.RESULT_KO;
        }
        return PostemToolConstants.RESULT_OK;
    }
}
