/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.lti.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.OutputStream;
import java.text.DateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.sakaiproject.lti.api.LTIExportService;
import org.sakaiproject.lti.api.LTIExporter;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.util.ResourceLoader;

public class LTIExportServiceImpl implements LTIExportService {
	protected LTIService ltiService;
	protected ServerConfigurationService serverConfigurationService;
	protected SiteService siteService;
	
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
	 * @see impl.LTIExportService#export(java.io.OutputStream, org.sakaiproject.lti.api.LTIExportService.ExportType, java.lang.String)
	 */
	public void export(OutputStream out, String siteId, ExportType exportType, String filterId) {
		String search = null;
		//check if we need to filter the tools by tool_id
		if(StringUtils.isNotEmpty(filterId)) {
			search = "tool_id:"+filterId;
		}
		List<Map<String,Object>> contents = ltiService.getContentsDao(search, null, 0, 0, siteId, isAdmin(siteId));
		LTIExporter exporter = null;
		switch(exportType) {
			case CSV : exporter = new ExporterCSV(); break;
			case EXCEL : exporter = new ExporterExcel(); break;
		}		
		
		if(exporter != null) {
			DateFormat dateFormatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, rb.getLocale());
			
			String attribution_name = serverConfigurationService.getString(LTIService.LTI_SITE_ATTRIBUTION_PROPERTY_NAME, LTIService.LTI_SITE_ATTRIBUTION_PROPERTY_NAME_DEFAULT);
			if(StringUtils.isNotEmpty(attribution_name)) {
				//check if given property is a translation key
				String aux = rb.getString(attribution_name);
				if(StringUtils.isNotEmpty(aux)) 
					attribution_name = aux;
			}
			
			boolean isAdmin = isAdmin(siteId);
			
			//set header row
			exporter.newLine();
			exporter.addCell(rb.getString("export.title"));
			exporter.addCell(rb.getString("export.url"));
			if(isAdmin) {
				exporter.addCell(rb.getString("export.siteid"));
				exporter.addCell(rb.getString("export.sitetitle"));
			}
			exporter.addCell(rb.getString("export.createdat"));
			if(isAdmin) {
				exporter.addCell(rb.getString("export.sitecontactname"));
				exporter.addCell(rb.getString("export.sitecontactemail"));
				if(StringUtils.isNotEmpty(attribution_name)) {
					exporter.addCell(attribution_name);
				}
			}
			
			//values rows
			for(Map<String,Object> content : contents) {
				exporter.newLine();
				exporter.addCell((String)content.get("title"));
				
				String url = (String)content.get("launch");
				if(StringUtils.isEmpty(url)) {
					try {
						url = (String)(ltiService.getToolDao(new Long(content.get(LTIService.LTI_TOOL_ID).toString()), siteId).get("launch"));
					} catch(Exception e) {
						url = "-";
					}
				}
				exporter.addCell(url);
				
				if(isAdmin) {
					exporter.addCell((String)content.get("SITE_ID"));
					
					exporter.addCell((String)content.get("SITE_TITLE"));
				}
							
				try{ exporter.addCell(dateFormatter.format(content.get("created_at"))); } catch(Exception e){ exporter.addCell("-"); }
				
				if(isAdmin) {
					exporter.addCell((String)content.get("SITE_CONTACT_NAME"));
					
					exporter.addCell((String)content.get("SITE_CONTACT_EMAIL"));
					
					if(StringUtils.isNotEmpty(attribution_name)) {
						exporter.addCell((String)content.get("ATTRIBUTION"));
					}
				}
			}
			
			exporter.write(out);
		} else {
			M_log.error("Error exporting : no exporter found for "+exportType);
		}
	}
	
}
