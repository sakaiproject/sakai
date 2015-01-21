package org.sakaiproject.gradebookng.tool.pages;


import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.time.Duration;
import org.sakaiproject.gradebookng.tool.model.GradeInfo;
import org.sakaiproject.gradebookng.tool.model.StudentGradeInfo;
import org.sakaiproject.service.gradebook.shared.Assignment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
//				return new File(new ByteArrayResource("", null));
			}
		}).setCacheDuration(Duration.NONE).setDeleteAfterDownload(true));

		add(new DownloadLink("downloadFullGradebook", new LoadableDetachableModel<File>() {

			@Override
			protected File load() {
				return buildFile(true);
//				return new File(new ByteArrayResource("", null));
			}
		}).setCacheDuration(Duration.NONE).setDeleteAfterDownload(true));




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
}
