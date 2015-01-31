package org.sakaiproject.gradebookng.tool.panels;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.time.Duration;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.helpers.ImportGradesHelper;
import org.sakaiproject.gradebookng.business.model.ImportedGradeWrapper;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.tool.model.GradeInfo;
import org.sakaiproject.gradebookng.tool.model.StudentGradeInfo;
import org.sakaiproject.service.gradebook.shared.Assignment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chmaurer on 1/22/15.
 */
public class GradeImportUploadStep extends Panel {

    private static final Logger log = Logger.getLogger(GradeImportUploadStep.class);

    //list of mimetypes for each category. Must be compatible with the parser
    private static final String[] XLS_MIME_TYPES={"application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};
    private static final String[] CSV_MIME_TYPES={"text/csv"};

    private String panelId;
    final List<Assignment> assignments;
    final List<StudentGradeInfo> grades;

    @SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
    protected GradebookNgBusinessService businessService;

    public GradeImportUploadStep(String id) {
        super(id);
        this.panelId = id;

        //get list of assignments. this allows us to build the columns and then fetch the grades for each student for each assignment from the map
        assignments = businessService.getGradebookAssignments();

        //get the grade matrix
        grades = businessService.buildGradeMatrix();

        add(new DownloadLink("downloadBlankTemplate", new LoadableDetachableModel<File>() {

            @Override
            protected File load() {
                return buildFile(false);
            }
        }).setCacheDuration(Duration.NONE).setDeleteAfterDownload(true));

        add(new DownloadLink("downloadFullGradebook", new LoadableDetachableModel<File>() {

            @Override
            protected File load() {
                return buildFile(true);

            }
        }).setCacheDuration(Duration.NONE).setDeleteAfterDownload(true));


        add(new UploadForm("form"));
    }

    /**
     *
     * @param input
     * @return
     */
    private String wrapText(String input) {
        return "\"" + input + "\"";
    }


    private File buildFile(boolean includeGrades) {
        File tempFile;
        try {
            //TODO - Maybe use CSVWriter here?
            //TODO - add the site name to the file?
            tempFile = File.createTempFile("gradebookTemplate", ".csv");

            FileWriter fw = new FileWriter(tempFile);
            //Create csv header
            List<String> header = new ArrayList<String>();
            header.add(wrapText("Student ID"));
            header.add(wrapText("Student Name"));

            for (Assignment assignment : assignments) {
                header.add(wrapText(assignment.getName() + " [" + assignment.getPoints() + "]"));
                header.add(wrapText("*/ " + assignment.getName() + " Comments */"));
            }
            String headerStr = StringUtils.join(header, ",");
            fw.append(headerStr + "\n");

            List<String> line = new ArrayList<String>();

            for (StudentGradeInfo studentGradeInfo : grades) {
                line.add(wrapText(studentGradeInfo.getStudentEid()));
                line.add(wrapText(studentGradeInfo.getStudentName()));
                if (includeGrades) {
                    for (Assignment assignment : assignments) {
                        GradeInfo gradeInfo = studentGradeInfo.getGrades().get(assignment.getId());
                        line.add(wrapText(gradeInfo.getGrade()));
                        line.add(wrapText(gradeInfo.getGradeComment()));
                    }
                }
                String lineStr = StringUtils.join(line, ",");
                fw.append(lineStr + "\n");
            }


            fw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tempFile;

    }

    /*
     * Upload form
     */
    private class UploadForm extends Form<Void> {

        FileUploadField fileUploadField;

        public UploadForm(String id) {
            super(id);

            setMultiPart(true);
            setMaxSize(Bytes.megabytes(2));

            fileUploadField = new FileUploadField("upload");
            add(fileUploadField);
        }

        @Override
        public void onSubmit() {

            FileUpload upload = fileUploadField.getFileUpload();
            if (upload != null) {

                try {
                    log.debug("file upload success");
                    //turn file into list
                    ImportedGradeWrapper importedGradeWrapper = parseImportedGradeFile(upload.getInputStream(), upload.getContentType());

                    List<ProcessedGradeItem> processedGradeItems = ImportGradesHelper.processImportedGrades(importedGradeWrapper, assignments, grades);

                    //if null, the file was of the incorrect type
                    //if empty there are no users
                    if(processedGradeItems == null || processedGradeItems.isEmpty()) {
                        error(getString("error.parse.upload"));
                    } else {
                        //GO TO NEXT PAGE
                        log.debug(processedGradeItems.size());

						//repaint panel
						Component newPanel = new GradeImportConfirmationStep(panelId, processedGradeItems);
						newPanel.setOutputMarkupId(true);
						GradeImportUploadStep.this.replaceWith(newPanel);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
    }


    public ImportedGradeWrapper parseImportedGradeFile(InputStream is, String mimetype){

        //determine file type and delegate
        if(ArrayUtils.contains(CSV_MIME_TYPES, mimetype)) {
            return ImportGradesHelper.parseCsv(is);
        } else if (ArrayUtils.contains(XLS_MIME_TYPES, mimetype)) {
            return ImportGradesHelper.parseXls(is);
        } else {
            log.error("Invalid file type for grade import: " + mimetype);
        }
        return null;
    }
}
