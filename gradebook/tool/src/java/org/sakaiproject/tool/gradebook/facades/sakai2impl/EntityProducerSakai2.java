/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.facades.sakai2impl;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.service.legacy.entity.Entity;
import org.sakaiproject.service.legacy.entity.EntityProducer;
import org.sakaiproject.service.legacy.entity.HttpAccess;
import org.sakaiproject.service.legacy.entity.Reference;
import org.sakaiproject.service.legacy.entity.ResourceProperties;
import org.sakaiproject.service.legacy.resource.cover.EntityManager;
import org.sakaiproject.service.legacy.site.Site;

import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;

/**
 * Implements the Sakai 2.1 EntityProducer approach to integration of tool-specific
 * storage with site management.
 */
public class EntityProducerSakai2 implements EntityProducer {
    private static final Log log = LogFactory.getLog(EntityProducerSakai2.class);

	private GradebookService gradebookService;
	private String toolId;

	public void init() {
		EntityManager.registerEntityProducer(this, null);
	}

	public void syncWithSiteChange(Site site, ChangeType change) {
		String gradebookUid = site.getId();
		boolean gradebookExists = gradebookService.gradebookExists(gradebookUid);

		if ((change == EntityProducer.ChangeType.ADD) || (change == EntityProducer.ChangeType.UPDATE)) {
			// See if this tool is now in the site.
			String[] toolsToSearchFor = {getToolId()};
			Collection matchingTools = site.getTools(toolsToSearchFor);
			if (matchingTools.isEmpty() && gradebookExists) {
				// We've been directed to leave Gradebook data in place when
				// the tool is removed from a site.
				if (log.isInfoEnabled()) log.info("Gradebook being removed from site " + gradebookUid + " but associated data will remain until site deletion");
			} else if (!matchingTools.isEmpty() && !gradebookExists) {
				if (log.isInfoEnabled()) log.info("Gradebook being added to site " + gradebookUid);
				gradebookService.addGradebook(gradebookUid, gradebookUid);
			}
		} else if ((change == EntityProducer.ChangeType.REMOVE) && gradebookExists) {
			try {
				gradebookService.deleteGradebook(gradebookUid);
			} catch (GradebookNotFoundException e) {
				if (log.isWarnEnabled()) log.warn(e);
			}
		}
	}

	// The following EntityProducer methods are currently unused.
	public String getLabel() {
		return null;
	}
	public boolean willArchiveMerge() {
		return false;
	}
	public boolean willImport() {
		return false;
	}
	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments) {
		return null;
	}
	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans, Set userListAllowImport) {
		return null;
	}
	public void importEntities(String fromContext, String toContext, List ids) {
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
	public Collection getEntityAuthzGroups(Reference ref) {
		return null;
	}

	public void setGradebookService(GradebookService gradebookService) {
		if (log.isDebugEnabled()) log.debug("setGradebookService " + gradebookService);
		this.gradebookService = gradebookService;
	}

	public String getToolId() {
		return toolId;
	}

	/**
	 * Since this string is otherwise used only in XML configuration
	 * files, we get it from one too.
	 */
	public void setToolId(String toolId) {
		this.toolId = toolId;
	}

	public HttpAccess getHttpAccess() {
		return null;
	}
}
