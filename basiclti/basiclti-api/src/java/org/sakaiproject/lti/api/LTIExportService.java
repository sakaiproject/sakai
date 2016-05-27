package org.sakaiproject.lti.api;

import java.io.OutputStream;

public interface LTIExportService {

	public static enum ExportType {CSV, EXCEL};
	
	public abstract void export(OutputStream out, String siteId, ExportType exportType, String filterId);

}