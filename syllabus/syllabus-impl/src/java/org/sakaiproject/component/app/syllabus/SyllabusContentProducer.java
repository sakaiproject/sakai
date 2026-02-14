/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003-2025 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *		 http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.component.app.syllabus;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.api.app.syllabus.SyllabusItem;
import org.sakaiproject.api.app.syllabus.SyllabusManager;
import org.sakaiproject.api.app.syllabus.SyllabusService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.EntityContentProducerEvents;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchUtils;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Content producer for syllabus search functionality
 * 
 * @author Generated for syllabus search support
 */
@Slf4j
public class SyllabusContentProducer implements EntityContentProducer, EntityContentProducerEvents {

	@Setter @Getter
	private SearchIndexBuilder searchIndexBuilder = null;
	
	@Setter @Getter
	private EntityManager entityManager = null;
	
	@Setter @Getter
	private SyllabusManager syllabusManager = null;
	
	// Map of events to their corresponding search index actions
	private static final Map<String, Integer> EVENT_ACTIONS = Map.of(
			SyllabusService.EVENT_SYLLABUS_POST_NEW, SearchBuilderItem.ACTION_ADD,
			SyllabusService.EVENT_SYLLABUS_POST_CHANGE, SearchBuilderItem.ACTION_ADD,
			SyllabusService.EVENT_SYLLABUS_DRAFT_NEW, SearchBuilderItem.ACTION_ADD,
			SyllabusService.EVENT_SYLLABUS_DRAFT_CHANGE, SearchBuilderItem.ACTION_ADD,
			SyllabusService.EVENT_SYLLABUS_DELETE_POST, SearchBuilderItem.ACTION_DELETE
	);
	
	@Setter
	private SiteService siteService;
	
	@Setter
	private SecurityService securityService;
	
	@Setter
	private ServerConfigurationService serverConfigurationService;

	public void init() {
		searchIndexBuilder.registerEntityContentProducer(this);
	}

	private Reference getReference(String reference) {
		try {
			return entityManager.newReference(reference);
		} catch (Exception ex) {
			return null;
		}
	}

	private EntityProducer getProducer(Reference ref) {
		try {
			return ref.getEntityProducer();
		} catch (Exception ex) {
			return null;
		}
	}

	@Override
	public boolean canRead(String reference) {
		Reference ref = getReference(reference);
		if (ref == null) return false;
		
		SyllabusData syllabusData = getSyllabusData(ref);
		if (syllabusData == null) return false;
		
		// Check if syllabus is posted (not draft)
		if (!SyllabusData.ITEM_POSTED.equals(syllabusData.getStatus())) {
			return false;
		}
		
		String siteId = syllabusData.getSyllabusItem().getContextId();
		try {
			// Check if user can access the site - this is how syllabus tool checks access
			siteService.getSiteVisit(siteId);
			return true;
		} catch (Exception e) {
			// User cannot access the site
			return false;
		}
	}

	@Override
	public Integer getAction(Event event) {
		return EVENT_ACTIONS.getOrDefault(event.getEvent(), SearchBuilderItem.ACTION_UNKNOWN);
	}

	@Override
	public String getContainer(String reference) {
		try {
			return getReference(reference).getContainer();
		} catch (Exception ex) {
			return "";
		}
	}

	@Override
	public String getContent(String reference) {
		Reference ref = getReference(reference);
		if (ref == null) return "";
		
		SyllabusData syllabusData = getSyllabusData(ref);
		if (syllabusData == null) return "";
		
		String content = syllabusData.getAsset();
		return content != null ? content : "";
 	}

	@Override
	public Reader getContentReader(String reference) {
		return new StringReader(getContent(reference));
	}

	@Override
	public String getId(String ref) {
		try {
			Reference reference = getReference(ref);
			if (reference != null) {
				return reference.getId();
			}
		} catch (Exception e) {
			log.debug("Error getting id for reference: {}", ref, e);
		}
		return "";
	}

	public List<String> getSiteContent(String context) {
		List<String> rv = new ArrayList<>();
		
		try {
			// Get all syllabus items for this site
			SyllabusItem syllabusItem = syllabusManager.getSyllabusItemByContextId(context);
			if (syllabusItem != null) {
				for (SyllabusData data : syllabusItem.getSyllabi()) {
					// Only index published syllabus entries
					if (SyllabusData.ITEM_POSTED.equals(data.getStatus())) {
						rv.add(getReference(context, data.getSyllabusId()));
					}
				}
			}
		} catch (Exception e) {
			log.warn("Error getting site content for context: {}", context, e);
		}
		
		return rv;
	}

	@Override
	public Iterator<String> getSiteContentIterator(String context) {
		return getSiteContent(context).iterator();
	}

	@Override
	public String getSiteId(String reference) {
		Reference ref = getReference(reference);
		if (ref == null) return null;
		
		SyllabusData syllabusData = getSyllabusData(ref);
		if (syllabusData == null) return null;
		
		return syllabusData.getSyllabusItem().getContextId();
	}

	@Override
	public String getSubType(String ref) {
		return "";
	}

	@Override
	public String getTitle(String reference) {
		Reference ref = getReference(reference);
		if (ref == null) return "";
		
		SyllabusData syllabusData = getSyllabusData(ref);
		if (syllabusData == null) return "";
		
		String title = syllabusData.getTitle();
		return title != null ? title : "";
	}

	@Override
	public String getTool() {
		return "syllabus";
	}

	@Override
	public String getType(String ref) {
		return "syllabus";
	}

	@Override
	public String getUrl(String reference) {
		Reference ref = getReference(reference);
		if (ref == null) return "";
		
		SyllabusData syllabusData = getSyllabusData(ref);
		if (syllabusData == null) return "";
		
		String siteId = syllabusData.getSyllabusItem().getContextId();
		Long syllabusId = syllabusData.getSyllabusId();
		
		// Use direct access to the syllabus entity
		try {
			Site site = siteService.getSite(siteId);
			ToolConfiguration toolConfig = site.getToolForCommonId("sakai.syllabus");
			if (toolConfig != null) {
				// Use proper portal URL from configuration
				return serverConfigurationService.getPortalUrl() + 
					   "/directtool/" + toolConfig.getId() + 
					   "?itemId=" + syllabusId + "&action=read_item";
			}
		} catch (Exception e) {
			log.debug("Error getting tool configuration for site: {}", siteId, e);
		}
		
		// Fallback to regular site URL
		return serverConfigurationService.getPortalUrl() + "/site/" + siteId + "/tool/sakai.syllabus";
	}

	@Override
	public boolean isContentFromReader(String reference) {
		return false;
	}

	@Override
	public boolean isForIndex(String reference) {
		Reference ref = getReference(reference);
		if (ref == null) return false;
		
		SyllabusData syllabusData = getSyllabusData(ref);
		if (syllabusData == null) return false;
		
		// Only index published syllabus entries
		return SyllabusData.ITEM_POSTED.equals(syllabusData.getStatus());
	}

	@Override
	public boolean matches(String reference) {
		return reference.startsWith(SyllabusService.REFERENCE_ROOT);
	}

	@Override
	public boolean matches(Event event) {
		return EVENT_ACTIONS.containsKey(event.getEvent());
	}

	/**
	 * Helper method to get SyllabusData from a reference
	 */
	private SyllabusData getSyllabusData(Reference ref) {
		try {
			String id = ref.getId();
			if (id != null) {
				return syllabusManager.getSyllabusData(id);
			}
		} catch (Exception e) {
			log.debug("Error getting SyllabusData for reference: {}", ref.getReference(), e);
		}
		return null;
	}

	/**
	 * Helper method to construct a reference string
	 */
	private String getReference(String siteId, Long syllabusId) {
		return SyllabusService.REFERENCE_ROOT + "/" + siteId + "/" + syllabusId.toString();
	}

	@Override
	public Set<String> getTriggerFunctions() {
		return EVENT_ACTIONS.keySet();
	}
} 