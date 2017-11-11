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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.sakaiproject.entity.api.ContentExistsAware;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ReportDefEntityProducer implements EntityProducer, EntityTransferrer, ContentExistsAware {
	private ReportManager			M_rm;
		
	// --- Sakai services --------------------------------
	
	public void init() {
		EntityManager.registerEntityProducer(this, ReportDefEntityProvider.REFERENCE_ROOT);
	}
	
	public void setReportManager(ReportManager reportManager) {
		this.M_rm = reportManager;
	}
	
	
	// --- EntityTransferrer -----------------------------
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityTransferrer#myToolIds()
	 */
	public String[] myToolIds() {
		return new String[]{StatsManager.SITESTATS_TOOLID};
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityTransferrer#transferCopyEntities(java.lang.String, java.lang.String, java.util.List)
	 */
	public void transferCopyEntities(String fromContext, String toContext, List ids) {
		transferCopyEntities(fromContext, toContext, ids, false);		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityTransferrer#transferCopyEntities(java.lang.String, java.lang.String, java.util.List, boolean)
	 */
	public void transferCopyEntities(String fromContext, String toContext, List ids, boolean cleanup) {
		// determine report definitions to copy
		List<ReportDef> list = null;
		if(ids != null && ids.size() > 0) {
			list = new ArrayList<ReportDef>();
			for(String id : (List<String>) ids) {
				ReportDef rd = M_rm.getReportDefinition(Long.valueOf(id));
				if(rd != null) {
					list.add(rd);
				}
			}
		}else{
			list = M_rm.getReportDefinitions(fromContext, false, true);
		}

		// cleanup existing reports on destination site before copying
		if(cleanup) {
			List<ReportDef> listToCleanUp = M_rm.getReportDefinitions(toContext, false, true);
			for(ReportDef rd : listToCleanUp) {
				M_rm.removeReportDefinition(rd);
			}
		}
		
		// copy to destination
		for(ReportDef rd : list) {
			rd.setId(0);
			rd.setSiteId(toContext);
			rd.getReportParams().setSiteId(toContext);
			M_rm.saveReportDefinition(rd);			
		}
	}
	
	
	// --- EntityProducer --------------------------------

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityProducer#getLabel()
	 */
	public String getLabel() {
		return ReportDefEntityProvider.LABEL;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityProducer#willArchiveMerge()
	 */
	public boolean willArchiveMerge() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityProducer#archive(java.lang.String, org.w3c.dom.Document, java.util.Stack, java.lang.String, java.util.List)
	 */
	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityProducer#merge(java.lang.String, org.w3c.dom.Element, java.lang.String, java.lang.String, java.util.Map, java.util.Map, java.util.Set)
	 */
	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans, Set userListAllowImport) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityProducer#parseEntityReference(java.lang.String, org.sakaiproject.entity.api.Reference)
	 */
	public boolean parseEntityReference(String reference, Reference ref) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntityDescription(org.sakaiproject.entity.api.Reference)
	 */
	public String getEntityDescription(Reference ref) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntityResourceProperties(org.sakaiproject.entity.api.Reference)
	 */
	public ResourceProperties getEntityResourceProperties(Reference ref) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntity(org.sakaiproject.entity.api.Reference)
	 */
	public Entity getEntity(Reference ref) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntityUrl(org.sakaiproject.entity.api.Reference)
	 */
	public String getEntityUrl(Reference ref) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntityAuthzGroups(org.sakaiproject.entity.api.Reference, java.lang.String)
	 */
	public Collection getEntityAuthzGroups(Reference ref, String userId) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityProducer#getHttpAccess()
	 */
	public HttpAccess getHttpAccess() {
		return null;
	}

	/**
	 * This implementation simply checks if we have reports in the site. If so, consider it content.
	 * 
	 * @see org.sakaiproject.entity.api.ContentExistsAware#hasContent(String)
	 */
	@Override
	public boolean hasContent(String siteId) {
		List<ReportDef> existingReportDefinitions = M_rm.getReportDefinitions(siteId, false, true);
		return !existingReportDefinitions.isEmpty();
	}

	

}
