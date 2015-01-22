package org.sakaiproject.gradebookng.tool.pages;


import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.time.Duration;
import org.sakaiproject.gradebookng.business.helpers.ImportGradesHelper;
import org.sakaiproject.gradebookng.business.model.ImportedGrade;
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
 * Import Export page
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class ImportExportPage extends BasePage {

	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(BasePage.class);

	//list of mimetypes for each category. Must be compatible with the parser
	private static final String[] XLS_MIME_TYPES={"application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};
	private static final String[] CSV_MIME_TYPES={"text/csv"};

	final List<Assignment> assignments;
	final List<StudentGradeInfo> grades;

	public ImportExportPage() {

		//get list of assignments. this allows us to build the columns and then fetch the grades for each student for each assignment from the map
		assignments = this.businessService.getGradebookAssignments();

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
					List<ImportedGrade> importedGrades = parseImportedGradeFile(upload.getInputStream(), upload.getContentType());

					//if null, the file was of the incorrect type
					//if empty there are no users
					if(importedGrades == null || importedGrades.isEmpty()) {
						error(getString("error.parse.upload"));
					} else {
						//GO TO NEXT PAGE
						System.out.println(importedGrades.size());

//						//repaint panel
//						Component newPanel = new GradeImportConfirmationStep(panelId, importedGrades);
//						newPanel.setOutputMarkupId(true);
//						UserImportUploadStep.this.replaceWith(newPanel);
//
					}


				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}
	}


	public List<ImportedGrade> parseImportedGradeFile(InputStream is, String mimetype){

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
