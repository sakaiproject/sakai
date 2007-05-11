package org.sakaiproject.scorm.helper;

import org.sakaiproject.scorm.helper.pages.UploadContentPackage;
import org.sakaiproject.scorm.tool.ScormTool;

public class ScormHelper extends ScormTool {

	public Class getHomePage() {
		return UploadContentPackage.class;
	}
	
}
