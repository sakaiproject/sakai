package org.sakaiproject.lti.api;

import java.io.OutputStream;

public interface LTIExportService {

	public static final int TYPE_CSV = 0;
	public static final int TYPE_EXCEL = 1;
	
	public abstract void export(OutputStream out, String siteId, int exportType, String filterId);

}