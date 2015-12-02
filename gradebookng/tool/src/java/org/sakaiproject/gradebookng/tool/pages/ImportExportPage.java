package org.sakaiproject.gradebookng.tool.pages;


import org.apache.log4j.Logger;
import org.sakaiproject.gradebookng.tool.panels.importExport.GradeImportUploadStep;

/**
 * Import Export page
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class ImportExportPage extends BasePage {

	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(ImportExportPage.class);



	public ImportExportPage() {
		disableLink(this.importExportPageLink);
		
		add(new GradeImportUploadStep("wizard"));

	}


}
