/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.impl.report;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.impl.parser.DigesterUtil;

@Slf4j
public class ReportDefEntityProvider implements AutoRegisterEntityProvider, CoreEntityProvider, Resolvable /*, Importable, Exportable*/ {
	public static final String		PREFIX							= "sitestats-report";
	public static final String		LABEL							= "SiteStatsReport";
	public static final String 		REFERENCE_ROOT 					= "/" + PREFIX;
	public static final String		IMPORTEXPORT_CURRENT_VERSION	= "1.0";
	public static final String		IMPORTEXPORT_DEFAULT_ENCODING	= "UTF-8";
	private ReportManager			M_rm;
	private DeveloperHelperService	M_dhs;	
	
	// --- Sakai services --------------------------------
	
	public void setReportManager(ReportManager reportManager) {
		this.M_rm = reportManager;
	}
	
	public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
		this.M_dhs = developerHelperService;
	}
	
	
	// --- AutoRegisterEntityProvider ------------------------
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.EntityProvider#getEntityPrefix()
	 */
	public String getEntityPrefix() {
		return PREFIX;
	}
	
	
	// --- CoreEntityProvider --------------------------------
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider#entityExists(java.lang.String)
	 */
	public boolean entityExists(String id) {
		long longId = 0;
		try{
			longId = Long.valueOf(id);
		}catch(NumberFormatException e){
			return false;
		}
		return M_rm.getReportDefinition(longId) != null;
	}
	
	
	// --- Resolvable ----------------------------------------
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable#getEntity(org.sakaiproject.entitybroker.EntityReference)
	 */
	public Object getEntity(EntityReference ref) {
		long longId = 0;
		try{
			longId = Long.valueOf(ref.getId());
		}catch(NumberFormatException e){
			return null;
		}
		return M_rm.getReportDefinition(longId);
	}
	
	
	// -------------------------------------------------------------------------------
	// --- THE CAPABILITIES BELOW ARE NOT IMPLEMENTED YET BY ENTITYBROKER/SITEINFO ---
	// --- See SAK-14257 for progress on this issue ----------------------------------
	// -------------------------------------------------------------------------------
	
	// --- Importable, Exportable ----------------------------

	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Importable#importData(java.lang.String, java.io.InputStream, java.lang.String, java.util.Map)
	 */
	public String[] importData(String reference, InputStream data, String encodingKey, Map<String, Object> params) {
		log.info("importData(): reference="+reference+", encodingKey="+encodingKey+", params="+params);
		String[] imported = new String[0];
		if(M_dhs.entityExists(reference)) {
			//String srcSiteId = M_dhs.getLocationIdFromRef(reference);
			String[] importInfo = encodingKey.split("\\|");
			try{
				byte[] bytes = new byte[data.available()];
				data.read(bytes);
				List<ReportDef> list = DigesterUtil.convertXmlToReportDefs(new String(bytes, importInfo[0]));
				String thisSiteId = M_dhs.getCurrentLocationId();
				for(ReportDef rf : list) {
					rf.setId(0);
					rf.setSiteId(thisSiteId);
					rf.getReportParams().setSiteId(thisSiteId);
					M_rm.saveReportDefinition(rf);
				}
			}catch(Exception e){
				log.error("Unable to import SiteStats reports", e);
			}
		}
		return imported;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Exportable#exportData(java.lang.String, org.sakaiproject.entitybroker.entityprovider.search.Search, java.io.OutputStream, boolean, java.util.Map)
	 */
	public String exportData(String reference, Search search, OutputStream data, boolean destructive, Map<String, Object> params) {
		log.info("exportData(): reference="+reference+", destructive="+destructive);
		if(M_dhs.entityExists(reference)) {
			//String destSiteId = M_dhs.getLocationIdFromRef(reference);
			String exportInfo = IMPORTEXPORT_DEFAULT_ENCODING + "|" + IMPORTEXPORT_CURRENT_VERSION;
			List<ReportDef> list = M_rm.getReportDefinitions(M_dhs.getCurrentLocationId(), false, true);
			if(list != null && !list.isEmpty()) {
				try{
					String xml = DigesterUtil.convertReportDefsToXml(list);
					data.write(xml.getBytes(IMPORTEXPORT_DEFAULT_ENCODING));
					if(destructive) {
						for(ReportDef rd : list) {
							M_rm.removeReportDefinition(rd);
						}
					}
					return exportInfo;
				}catch(Exception e){
					log.error("Unable to export SiteStats reports", e);
				}
			}
		}		
		return null;
	}

}
