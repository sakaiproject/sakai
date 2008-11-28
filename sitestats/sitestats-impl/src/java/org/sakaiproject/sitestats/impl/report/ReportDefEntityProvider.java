package org.sakaiproject.sitestats.impl.report;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Exportable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Importable;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.impl.parser.DigesterUtil;


public class ReportDefEntityProvider implements AutoRegisterEntityProvider, Importable, Exportable {
	private Log						LOG								= LogFactory.getLog(ReportDefEntityProvider.class);
	public static String			PREFIX							= "sitestats-report";
	public static String			IMPORTEXPORT_CURRENT_VERSION	= "1.0";
	public static String			IMPORTEXPORT_DEFAULT_ENCODING	= "UTF-8";
	private ReportManager			M_rm;
	private DeveloperHelperService	M_dhs;
	
	
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
	
	
	// --- Importable, Exportable ----------------------------

	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Importable#importData(java.lang.String, java.io.InputStream, java.lang.String, java.util.Map)
	 */
	public String[] importData(String reference, InputStream data, String encodingKey, Map<String, Object> params) {
		LOG.info("importData(): reference="+reference+", encodingKey="+encodingKey+", params="+params);
		String[] imported = new String[0];
		if(M_dhs.entityExists(reference)) {
			String srcSiteId = M_dhs.getLocationIdFromRef(reference);
			String[] importInfo = encodingKey.split("\\|");
			try{
				byte[] bytes = new byte[data.available()];
				data.read(bytes);
				List<ReportDef> list = DigesterUtil.convertXmlToReportDefs(new String(bytes, importInfo[0]));
				String thisSiteId = M_dhs.getCurrentLocationId();
				for(ReportDef rf : list) {
					rf.setSiteId(thisSiteId);
					M_rm.saveReportDefinition(rf);
				}
			}catch(Exception e){
				LOG.error("Unable to import SiteStats reports", e);
			}
		}
		return imported;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Exportable#exportData(java.lang.String, org.sakaiproject.entitybroker.entityprovider.search.Search, java.io.OutputStream, boolean, java.util.Map)
	 */
	public String exportData(String reference, Search search, OutputStream data, boolean destructive, Map<String, Object> params) {
		LOG.info("exportData(): reference="+reference+", destructive="+destructive);
		if(M_dhs.entityExists(reference)) {
			String destSiteId = M_dhs.getLocationIdFromRef(reference);
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
					LOG.error("Unable to export SiteStats reports", e);
				}
			}
		}		
		return null;
	}

}
