package org.sakaiproject.lti.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.OutputStream;
import java.text.DateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.sakaiproject.lti.api.LTIExportService;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;

public class LTIExportServiceImpl implements LTIExportService {
	protected LTIService ltiService;
	protected ServerConfigurationService serverConfigurationService;
	protected SiteService siteService;
	
	private String csvSeparator = null;
	
	/** Resource bundle using current language locale */
	protected static ResourceLoader rb = new ResourceLoader("ltiservice");
	
	private static Log M_log = LogFactory.getLog(LTIExportServiceImpl.class);
	
	public void setLtiService(LTIService ltiService) {
		this.ltiService = ltiService;
	}
	
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void init() {
		
	}
	
	private boolean isAdmin(String siteId) {
		if ( siteId == null ) {
			throw new java.lang.RuntimeException("isAdmin() requires non-null siteId");
		}
		if (!"!admin".equals(siteId)) return false;
		return siteService.allowUpdateSite(siteId);
	}

	/* (non-Javadoc)
	 * @see impl.LTIExportService#export(java.io.OutputStream, int, java.lang.String)
	 */
	public void export(OutputStream out, String siteId, int exportType, String filterId) {
		String search = null;
		//check if we need to filter the tools by tool_id
		if(StringUtils.isNotEmpty(filterId)) {
			search = "tool_id:"+filterId;
		}
		List<Map<String,Object>> contents = ltiService.getContentsDao(search, null, 0, 0, siteId, isAdmin(siteId));
		switch(exportType) {
			case TYPE_CSV : exportCSV(out, siteId, contents); break;
			case TYPE_EXCEL : exportExcel(out, siteId, contents); break;
		}
	}
	
	private String getCSVSeparator() {
		if(csvSeparator == null) {
			csvSeparator = serverConfigurationService.getString("basiclti.export.csv.separator", ",");
		}
		return csvSeparator;
	}
	
	private void exportCSV(OutputStream out, String siteId, List<Map<String,Object>> contents) {
		
		StringBuilder result = new StringBuilder();
		FormattedText validator = new FormattedText();
		DateFormat dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, rb.getLocale());
		
		String attribution_name = serverConfigurationService.getString(LTIService.LTI_SITE_ATTRIBUTION_PROPERTY_NAME);
		if(StringUtils.isNotEmpty(attribution_name)) {
			//check if given property is a translation key
			String aux = rb.getString(attribution_name);
			if(StringUtils.isNotEmpty(aux)) 
				attribution_name = aux;
		}
		
		boolean isAdmin = isAdmin(siteId);
		
		//set header row
		result.append(rb.getString("export.title"));
		result.append(getCSVSeparator());
		result.append(rb.getString("export.url"));
		result.append(getCSVSeparator());
		if(isAdmin) {
			result.append(rb.getString("export.siteid"));
			result.append(getCSVSeparator());
			result.append(rb.getString("export.sitetitle"));
			result.append(getCSVSeparator());
		}
		result.append(rb.getString("export.createdat"));
		if(isAdmin) {
			result.append(getCSVSeparator());
			result.append(rb.getString("export.sitecontactname"));
			result.append(getCSVSeparator());
			result.append(rb.getString("export.sitecontactemail"));
			if(StringUtils.isNotEmpty(attribution_name)) {
				result.append(getCSVSeparator());
				result.append(attribution_name);
			}
		}
		result.append("\n");
		
		//values rows
		for(Map<String,Object> content : contents) {
			result.append(validator.escapeHtml((String)content.get("title"), false));
			result.append(getCSVSeparator());
			
			String url = (String)content.get("launch");
			if(StringUtils.isEmpty(url)) {
				try {
					url = (String)(ltiService.getToolDao(new Long(content.get(LTIService.LTI_TOOL_ID).toString()), siteId).get("launch"));
				} catch(Exception e) {
					url = "-";
					e.printStackTrace();
				}
			}
			result.append(validator.escapeHtml(url, false));
			result.append(getCSVSeparator());
			
			if(isAdmin) {
				result.append(validator.escapeHtml((String)content.get("SITE_ID"), false));
				result.append(getCSVSeparator());
				
				result.append(validator.escapeHtml((String)content.get("SITE_TITLE"), false));
				result.append(getCSVSeparator());
			}
						
			try{ result.append(dateFormatter.format(content.get("created_at"))); } catch(Exception e){ result.append("-"); }
			
			if(isAdmin) {
				result.append(getCSVSeparator());
				result.append(validator.escapeHtml((String)content.get("SITE_CONTACT_NAME"), false));
				result.append(getCSVSeparator());
				
				result.append(validator.escapeHtml((String)content.get("SITE_CONTACT_EMAIL"), false));
				if(StringUtils.isNotEmpty(attribution_name)) {
					result.append(getCSVSeparator());
					
					result.append(validator.escapeHtml((String)content.get("ATTRIBUTION"), false));
				}
			}
			result.append("\n");
		}
		
		try {
			out.write(result.toString().getBytes());
		} catch(Exception e) {
			M_log.error("Error exporting to CSV : "+e.getMessage());
		}
	}
	
	private void exportExcel(OutputStream out, String siteId, List<Map<String,Object>> contents) {
		String sheetName = "LTI";
		FormattedText validator = new FormattedText();
		DateFormat dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, rb.getLocale());
		
		String attribution_name = serverConfigurationService.getString(LTIService.LTI_SITE_ATTRIBUTION_PROPERTY_NAME);
		if(StringUtils.isNotEmpty(attribution_name)) {
			//check if given property is a translation key
			String aux = rb.getString(attribution_name);
			if(StringUtils.isNotEmpty(aux)) 
				attribution_name = aux;
		}
		
		boolean isAdmin = isAdmin(siteId);
		
		int rowNum = 0;
		HSSFWorkbook wb = new HSSFWorkbook();
		
		HSSFSheet sheet = wb.createSheet(WorkbookUtil.createSafeSheetName(sheetName));
		
		int cellNum = 0;		
		//set header row
		HSSFRow row = sheet.createRow(rowNum++);
		row.createCell(cellNum++).setCellValue(rb.getString("export.title"));
		row.createCell(cellNum++).setCellValue(rb.getString("export.url"));
		if(isAdmin) {
			row.createCell(cellNum++).setCellValue(rb.getString("export.siteid"));
			row.createCell(cellNum++).setCellValue(rb.getString("export.sitetitle"));
		}
		row.createCell(cellNum++).setCellValue(rb.getString("export.createdat"));
		if(isAdmin) {
			row.createCell(cellNum++).setCellValue(rb.getString("export.sitecontactname"));
			row.createCell(cellNum++).setCellValue(rb.getString("export.sitecontactemail"));
			if(StringUtils.isNotEmpty(attribution_name)) {
				row.createCell(cellNum++).setCellValue(attribution_name);
			}
		}
		
		//values rows
		for(Map<String,Object> content : contents) {
			cellNum = 0;
			row = sheet.createRow(rowNum++);
			row.createCell(cellNum++).setCellValue(validator.escapeHtml((String)content.get("title"), false));
			
			String url = (String)content.get("launch");
			if(StringUtils.isEmpty(url)) {
				try {
					url = (String)(ltiService.getToolDao(new Long(content.get(LTIService.LTI_TOOL_ID).toString()), siteId).get("launch"));
				} catch(Exception e) {
					url = "-";
				}
			}
			row.createCell(cellNum++).setCellValue(validator.escapeHtml(url, false));
			
			if(isAdmin) {
				row.createCell(cellNum++).setCellValue(validator.escapeHtml((String)content.get("SITE_ID"), false));
				
				row.createCell(cellNum++).setCellValue(validator.escapeHtml((String)content.get("SITE_TITLE"), false));
			}
			
			try { row.createCell(cellNum++).setCellValue(dateFormatter.format(content.get("created_at"))); } catch(Exception e){ row.createCell(cellNum++).setCellValue("-"); }
			
			if(isAdmin) {
				row.createCell(cellNum++).setCellValue(validator.escapeHtml((String)content.get("SITE_CONTACT_NAME"), false));
				
				row.createCell(cellNum++).setCellValue(validator.escapeHtml((String)content.get("SITE_CONTACT_EMAIL"), false));
				
				if(StringUtils.isNotEmpty(attribution_name)) {
					row.createCell(cellNum++).setCellValue(validator.escapeHtml((String)content.get("ATTRIBUTION"), false));
				}
			}
		}
		
		// output
		try
		{
			wb.write(out);
		}
		catch (Exception e)
		{
			M_log.warn("Error exporting to Excel : "+e.getMessage());
		}
		
	}
}
