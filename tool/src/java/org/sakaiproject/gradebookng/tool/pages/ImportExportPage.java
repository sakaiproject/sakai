package org.sakaiproject.gradebookng.tool.pages;


import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.util.time.Duration;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.user.api.User;

import java.io.*;
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

	public ImportExportPage() {

		//get list of assignments. this allows us to build the columns and then fetch the grades for each student for each assignment from the map
		final List<Assignment> assignments = this.businessService.getGradebookAssignments();
		final List<User> users = this.businessService.getGradeableUsers();
		
		add(new DownloadLink("downloadBlankTemplate", new LoadableDetachableModel<File>() {

			@Override
			protected File load() {
				File tempFile;
				try {
					//TODO - add the site name to the file?
					tempFile = File.createTempFile("gradebookTemplate", ".csv");
//					FileOutputStream fos = new FileOutputStream();
					FileWriter fw = new FileWriter(tempFile);
					//Create csv header
					List<String> header = new ArrayList<String>();
					header.add("studentId");
					header.add("studentName");

					for (Assignment assignment : assignments) {
						header.add(assignment.getName());
					}
					String headerStr = StringUtils.join(header, ",");
					fw.append(headerStr + "\n");

					List<String> line = new ArrayList<String>();

					for (User user : users) {
						line.add(wrapText(user.getEid()));
						line.add(wrapText(user.getSortName()));
						String lineStr = StringUtils.join(line, ",");
						fw.append(lineStr + "\n");
					}


					fw.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				return tempFile;
//				return new File(new ByteArrayResource("", null));
			}
		}).setCacheDuration(Duration.NONE).setDeleteAfterDownload(true));




	}

	private String wrapText(String input) {
		return "\"" + input + "\"";
	}
}
