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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.api.app.syllabus.SyllabusAttachment;
import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.api.app.syllabus.SyllabusItem;
import org.sakaiproject.api.app.syllabus.SyllabusManager;
import org.sakaiproject.api.app.syllabus.SyllabusService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.EntityContentProducerEvents;
import org.sakaiproject.search.api.SearchIndexBuilder;
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
	
	static final String ATTACHMENT_NAMES = "attachment_names";

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
		// the indexer drops documents without content, which keeps drafts and unpublished items out of search
		if (syllabusData == null || !SyllabusData.ITEM_POSTED.equals(syllabusData.getStatus())) return "";
		
		String title = syllabusData.getTitle();
		String content = syllabusData.getAsset();
		StringBuilder sb = new StringBuilder();
		if (title != null) sb.append(title).append(" ");
		if (content != null) sb.append(content);
		for (SyllabusAttachment attachment : syllabusManager.getSyllabusAttachmentsForSyllabusData(syllabusData)) {
			String name = attachment.getName();
			String text = getAttachmentText(attachment.getAttachmentId());
			// the PDF digester already starts its text with the file name
			if (name != null && !text.contains(name)) sb.append(" ").append(name);
			sb.append(" ").append(text);
		}
		return sb.toString();
 	}

	/**
	 * Search results show the content, so the words of the attachment names go in a field of their own,
	 * searched but not shown.
	 */
	@Override
	public Map<String, ?> getCustomProperties(String reference) {
		Reference ref = getReference(reference);
		SyllabusData syllabusData = ref != null ? getSyllabusData(ref) : null;
		if (syllabusData == null) return null;

		List<String> names = new ArrayList<>();
		for (SyllabusAttachment attachment : syllabusManager.getSyllabusAttachmentsForSyllabusData(syllabusData)) {
			if (attachment.getName() != null) names.add(getNameWords(attachment.getName()));
		}
		return names.isEmpty() ? null : Map.of(ATTACHMENT_NAMES, names);
	}

	/**
	 * The analyzer keeps a name like Course_ReadingGuide.pdf as one word, so its parts are listed too:
	 * Course_ReadingGuide Course ReadingGuide Reading Guide
	 */
	static String getNameWords(String name) {
		String baseName = StringUtils.substringBeforeLast(name, ".");
		String parts = baseName.replaceAll("[\\p{Punct}\\s]+", " ").trim();
		String words = parts.replaceAll("(\\p{Ll})(\\p{Lu})", "$1 $2");
		return String.join(" ", new LinkedHashSet<>(List.of(baseName, parts, words)));
	}

	/**
	 * Attachments live in content hosting, so the producer registered for their references (Resources) extracts
	 * their text, with the same digesters and size limits used for site resources.
	 */
	private String getAttachmentText(String attachmentId) {
		if (attachmentId == null) return "";
		String reference = ContentHostingService.REFERENCE_ROOT + attachmentId;
		try {
			EntityContentProducer producer = searchIndexBuilder.newEntityContentProducer(reference);
			if (producer != null && producer != this) {
				String text = producer.getContent(reference);
				return text != null ? text : "";
			}
		} catch (Exception e) {
			log.debug("Could not extract the text of syllabus attachment {}: {}", reference, e.toString());
		}
		return "";
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