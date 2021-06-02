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

package org.sakaiproject.postem.controller;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeMap;
import javax.servlet.ServletContext;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.api.app.postem.data.Gradebook;
import org.sakaiproject.api.app.postem.data.StudentGrades;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.postem.constants.PostemToolConstants;
import org.sakaiproject.postem.form.GradebookForm;
import org.sakaiproject.postem.helpers.CSV;
import org.sakaiproject.postem.helpers.MediaTypeUtils;
import org.sakaiproject.postem.service.PostemSakaiService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Slf4j
@Controller
public class ColumnsController {

    protected ArrayList students;
    protected Gradebook currentGradebook;
    protected TreeMap studentMap;

    @Autowired
    private PostemSakaiService postemSakaiService;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    ServletContext context;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private PreferencesService preferencesService;

    @Autowired
    private ServerConfigurationService serverConfigurationService;

    @Autowired
    private UserDirectoryService userDirectoryService;

    @GetMapping(value = {"/gradebook_view/{gradebookId}"})
    public String getViewGradebook(@PathVariable("gradebookId") Long gradebookId, Model model) {
        log.debug("getViewGradebook");

        currentGradebook = postemSakaiService.getGradebookById2(gradebookId);
        students = new ArrayList(currentGradebook.getStudents());

        model.addAttribute("visible", Boolean.toString(postemSakaiService.checkAccess()));
        model.addAttribute("currentGradebook", currentGradebook);
        model.addAttribute("studentsList", students);
        return PostemToolConstants.GRADEBOOK_VIEW;
    }

    @GetMapping(value = {"/student_grades_view/{gradebookId}"})
    public String getStudentGradeView(@PathVariable("gradebookId") Long gradebookId, Model model) {
        log.debug("getStudentGradeView");

        currentGradebook = postemSakaiService.getGradebookById2(gradebookId);

        String userId = sessionManager.getCurrentSessionUserId();
        String userEid = null;
        if (userId != null) {
            try {
                userEid = userDirectoryService.getUserEid(userId);
            } catch (UserNotDefinedException e) {
                log.error("UserNotDefinedException", e);
            }
        }

        StudentGrades studentGrades = null;
        if (userEid != null) {
            studentGrades = postemSakaiService.getStudentByGBAndUsername(currentGradebook, userEid);
        }

        final Locale locale = StringUtils.isNotBlank(userId) ? preferencesService.getLocale(userId) : Locale.getDefault();

        if (null == studentGrades) {
            String literalErrorMessage = MessageFormat.format(messageSource.getMessage(PostemToolConstants.NO_GRADES_FOR_USER, null, locale), currentGradebook.getTitle());
            model.addAttribute("literalErrorMessage", literalErrorMessage);
        }

        model.addAttribute("visible", Boolean.toString(postemSakaiService.checkAccess()));
        model.addAttribute("currentGradebook", currentGradebook);
        model.addAttribute("student", studentGrades);
        return PostemToolConstants.STUDENT_GRADE_VIEW;
    }

    @GetMapping(value = {"/student_view/{gradebookId}"})
    public String getViewStudent(@PathVariable("gradebookId") Long gradebookId, Model model) {
        log.debug("getViewStudent");

        studentMap = postemSakaiService.processGradebookView(gradebookId);

        model.addAttribute("visible", Boolean.toString(postemSakaiService.checkAccess()));
        model.addAttribute("studentMap", studentMap);
        model.addAttribute("gradebookId", gradebookId);
        return PostemToolConstants.STUDENT_VIEW;
    }

    @GetMapping(value = {"/student_view_result/{gradebookId}/{student}"})
    public String getViewStudentResult(@PathVariable("gradebookId") Long gradebookId, @PathVariable("student") String selectedStudent, Model model) {
        log.debug("getViewStudentResult");

        studentMap = postemSakaiService.processGradebookView(gradebookId);
        currentGradebook = postemSakaiService.getGradebookById2(gradebookId);
        StudentGrades selStudent = null;
        String lastSelected = "";

        if (selectedStudent!=null && !selectedStudent.equals("blank")) {
            selStudent = postemSakaiService.getStudentByGBAndUsername(currentGradebook, selectedStudent);
            selStudent.setGradebook(currentGradebook);
            if (selStudent != null) {
                lastSelected = selStudent.getUsername();
            }
        }

        model.addAttribute("visible", Boolean.toString(postemSakaiService.checkAccess()));
        model.addAttribute("lastSelected", lastSelected);
        model.addAttribute("selStudent", selStudent);
        model.addAttribute("studentMap", studentMap);
        model.addAttribute("gradebookId", gradebookId);

        return PostemToolConstants.STUDENT_VIEW;
    }

    @GetMapping(value = {"/delete_confirm/{gradebookId}"})
    public String getDeleteConfirm(@PathVariable("gradebookId") Long gradebookId, Model model) {
        log.debug("getDeleteConfirm");

        currentGradebook = postemSakaiService.getGradebookById2(gradebookId);

        model.addAttribute("visible", Boolean.toString(postemSakaiService.checkAccess()));
        model.addAttribute("currentGradebook", currentGradebook);
        return PostemToolConstants.DELETE_CONFIRM;
    }

    @GetMapping(value = {"/processDelete/{gradebookId}"})
    public String processDelete(@PathVariable("gradebookId") Long gradebookId, Model model) {
        log.debug("processDelete");

        String result = postemSakaiService.processDelete(gradebookId);

        if (result.equals(PostemToolConstants.RESULT_KO)) {
            return PostemToolConstants.PERMISSION_ERROR;
        }

        model.addAttribute("visible", Boolean.toString(postemSakaiService.checkAccess()));
        return PostemToolConstants.REDIRECT_MAIN_TEMPLATE;
    }

    @GetMapping(value = {"/process_csv_download/{gradebookId}"})
    public ResponseEntity<byte[]> processCsvDownload(@PathVariable("gradebookId") Long gradebookId, Model model) {
        log.debug("processCsvDownload()");

        CSV csv = postemSakaiService.processCsvDownload(gradebookId);
        if (null == csv) {
            return ResponseEntity.noContent().build();
        }
        Gradebook currentGradebook = postemSakaiService.getGradebookById2(gradebookId);
        String fileName = "postem_" + currentGradebook.getTitle() + ".csv";
        MediaType mediaType = MediaTypeUtils.getMediaTypeForFileName(context, fileName);

        try {
            return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                .body(csv.getCsv().getBytes(StandardCharsets.UTF_8.name()));
        } catch (Exception e) {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping(value = {"/gradebook_update/{gradebookId}"})
    public String getGradebookUpdate(@PathVariable("gradebookId") Long gradebookId, Model model) {
        log.debug("getGradebookUpdate");

        currentGradebook = postemSakaiService.getGradebookById2(gradebookId);

        model.addAttribute("visible", Boolean.toString(postemSakaiService.checkAccess()));
        String fileReference = currentGradebook.getFileReference();
        String partFileReference = "";
        if (null != fileReference) {
            String[] parts = fileReference.split("/");
            if (parts.length > 0) {
                partFileReference = parts[parts.length - 1];
            }
        }

        GradebookForm gradebookForm = new GradebookForm();
        gradebookForm.setGradebookUpdate(true);
        gradebookForm.setReleased(currentGradebook.getRelease());
        gradebookForm.setTitle(currentGradebook.getTitle());
        gradebookForm.setFileReference(partFileReference);
        gradebookForm.setId(currentGradebook.getId());
        model.addAttribute("gradebookForm", gradebookForm);
        model.addAttribute("fileReference", partFileReference);
        String uploadMax = serverConfigurationService.getString(ContentHostingService.SAK_PROP_MAX_UPLOAD_FILE_SIZE);
        if (null == uploadMax || uploadMax.isEmpty()) {
            uploadMax = "20"; //default MB
        }
        model.addAttribute("uploadMax", uploadMax);

        ToolSession toolSession = sessionManager.getCurrentToolSession();
        toolSession.setAttribute("attachmentId", currentGradebook.getFileReference());
        return PostemToolConstants.ADD_ITEM;
    }
}
