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
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import lombok.Setter;

public class ReportDefEntityProducer implements EntityProducer, EntityTransferrer, ContentExistsAware {

	@Setter private EntityManager entityManager;
	@Setter private ReportManager reportManager;
		
	public void init() {
		entityManager.registerEntityProducer(this, ReportDefEntityProvider.REFERENCE_ROOT);
	}
	
	public String[] myToolIds() {
		return new String[]{StatsManager.SITESTATS_TOOLID};
	}

	public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> transferOptions) {
		return transferCopyEntities(fromContext, toContext, ids, transferOptions, false);
	}

	public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> transferOptions, boolean cleanup) {
		// determine report definitions to copy
		List<ReportDef> list = null;
		if(ids != null && ids.size() > 0) {
			list = new ArrayList<ReportDef>();
			for(String id : (List<String>) ids) {
				ReportDef rd = reportManager.getReportDefinition(Long.valueOf(id));
				if(rd != null) {
					list.add(rd);
				}
			}
		}else{
			list = reportManager.getReportDefinitions(fromContext, false, true);
		}

		// cleanup existing reports on destination site before copying
		if(cleanup) {
			List<ReportDef> listToCleanUp = reportManager.getReportDefinitions(toContext, false, true);
			for(ReportDef rd : listToCleanUp) {
				reportManager.removeReportDefinition(rd);
			}
		}
		
		// copy to destination
		for(ReportDef rd : list) {
			rd.setId(0);
			rd.setSiteId(toContext);
			rd.getReportParams().setSiteId(toContext);
			reportManager.saveReportDefinition(rd);
		}

        return null;
	}
	
	
	public String getLabel() {
		return ReportDefEntityProvider.LABEL;
	}

	public boolean willArchiveMerge() {
		return false;
	}
	
	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments) {
		return null;
	}

	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans, Set userListAllowImport) {
		return null;
	}

	public boolean parseEntityReference(String reference, Reference ref) {
		return false;
	}

	public String getEntityDescription(Reference ref) {
		return null;
	}

	public ResourceProperties getEntityResourceProperties(Reference ref) {
		return null;
	}

	public Entity getEntity(Reference ref) {
		return null;
	}

	public String getEntityUrl(Reference ref) {
		return null;
	}

	public Collection getEntityAuthzGroups(Reference ref, String userId) {
		return null;
	}

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
		List<ReportDef> existingReportDefinitions = reportManager.getReportDefinitions(siteId, false, true);
		return !existingReportDefinitions.isEmpty();
	}

}
