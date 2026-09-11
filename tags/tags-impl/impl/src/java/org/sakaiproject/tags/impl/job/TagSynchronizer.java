/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2016 The Sakai Foundation
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
package org.sakaiproject.tags.impl.job;

import org.apache.commons.lang3.StringUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import java.time.Instant;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.sakaiproject.tags.api.*;

/**
 * A quartz job to synchronize the TAGS with an
 * xml file available in sakai home.
 *
 *
 */
@Slf4j
public abstract class TagSynchronizer {
	@Setter private TagService tagService;
	
	protected abstract InputStream getTagsXmlInputStream();

	protected Date getDate(String str) {
		if(StringUtils.isBlank(str)) {
			return null;
		}
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		try {
			return df.parse(str);
		} catch (ParseException pe) {
			log.warn("Invalid date: " + str);
			return null;
		}
	}

	protected String getTagCollectionIdFromExternalSourceName(String name) {
		if (StringUtils.isBlank(name)) {
			return null;
		}
		return tagService.getTagCollectionForExternalSourceName(name)
				.orElseThrow(() -> new TagServiceException("No collection for external source " + name))
				.getTagCollectionId();
	}

	protected void updateOrCreateTagWithExternalSourceName(String externalId, String externalSourceName, String tagLabel, String description,
			String alternativeLabels, long externalCreationDate, long lastUpdateDateInExternalSystem, String parentId,
			String externalHierarchyCode, String externalType, String data) {
		updateOrCreateTagWithCollectionId(externalId, getTagCollectionIdFromExternalSourceName(externalSourceName),
				tagLabel, description, alternativeLabels, externalCreationDate, lastUpdateDateInExternalSystem,
				parentId, externalHierarchyCode, externalType, data);
	}

	protected void updateOrCreateTagWithCollectionId(String externalId, String collectionId, String tagLabel, String description,
			String alternativeLabels, long externalCreationDate, long lastUpdateDateInExternalSystem, String parentId,
			String externalHierarchyCode, String externalType, String data) {
		if (collectionId == null) {
			return;
		}
		Optional<Tag> existing = tagService.getTagForExternalIdAndCollection(externalId, collectionId);
		Tag.TagBuilder tag = existing.map(Tag::toBuilder).orElseGet(() -> Tag.builder()
				.tagCollectionId(collectionId).externalId(externalId)
				.externalCreation(true).externalCreationDate(externalCreationDate));
		tag.tagLabel(tagLabel).description(description).alternativeLabels(alternativeLabels)
				.externalUpdate(true).lastUpdateDateInExternalSystem(lastUpdateDateInExternalSystem)
				.parentId(parentId).externalHierarchyCode(externalHierarchyCode).externalType(externalType).data(data);
		if (existing.isPresent()) {
			tagService.updateTag(tag.build());
		} else {
			tagService.createTag(tag.build());
		}
	}

	protected boolean updateLabelWithId(String tagId, String externalId, String collectionId, String tagLabel, String description,
			String alternativeLabels, long externalCreationDate, long lastUpdateDateInExternalSystem, String parentId,
			String externalHierarchyCode, String externalType, String data) {
		Optional<Tag> existing = tagService.getTag(tagId);
		if (!existing.isPresent()) {
			return false;
		}
		tagService.updateTag(existing.get().toBuilder()
				.tagCollectionId(collectionId).externalId(externalId).tagLabel(tagLabel).description(description)
				.alternativeLabels(alternativeLabels).externalCreationDate(externalCreationDate)
				.externalUpdate(true).lastUpdateDateInExternalSystem(lastUpdateDateInExternalSystem)
				.parentId(parentId).externalHierarchyCode(externalHierarchyCode).externalType(externalType).data(data)
				.build());
		return true;
	}

	protected void updateOrCreateTagCollection(String name, String description, String externalSourceName,
			String externalSourceDescription, long lastUpdateDateInExternalSystem) {
		if (externalSourceName == null) {
			return;
		}
		Optional<TagCollection> existing = tagService.getTagCollectionForExternalSourceName(externalSourceName);
		TagCollection.TagCollectionBuilder collection = existing.map(TagCollection::toBuilder)
				.orElseGet(() -> TagCollection.builder().description(description)
						.externalSourceName(externalSourceName).externalUpdate(true).externalCreation(true));
		collection.name(name).externalSourceDescription(externalSourceDescription)
				.lastUpdateDateInExternalSystem(lastUpdateDateInExternalSystem);
		if (existing.isPresent()) {
			tagService.updateTagCollection(collection.build());
		} else {
			tagService.createTagCollection(collection.build());
		}
	}

	protected void updateTagCollectionSynchronization(String externalSourceName, long lastUpdateDateInExternalSystem) {
		if (StringUtils.isBlank(externalSourceName)) {
			log.warn("Missing external source name for collection synchronization");
			return;
		}
		TagCollection collection = tagService.getTagCollectionForExternalSourceName(externalSourceName)
				.orElseThrow(() -> new TagServiceException("No collection for external source " + externalSourceName));
		updateSynchronization(collection, lastUpdateDateInExternalSystem);
	}

	protected void updateTagCollectionSynchronizationWithCollectionId(String collectionId, long lastUpdateDateInExternalSystem) {
		Optional<TagCollection> collection = tagService.getTagCollection(collectionId);
		if (collection.isPresent()) {
			updateSynchronization(collection.get(), lastUpdateDateInExternalSystem);
		} else {
			log.warn("No collection with id {} for synchronization", collectionId);
		}
	}

	private void updateSynchronization(TagCollection collection, long lastUpdateDateInExternalSystem) {
		try {
			tagService.updateTagCollection(collection.toBuilder().externalUpdate(true)
					.lastSynchronizationDate(Instant.now().toEpochMilli())
					.lastUpdateDateInExternalSystem(lastUpdateDateInExternalSystem).build());
		} catch (Exception e) {
			log.warn("Could not update synchronization metadata for collection {}", collection.getTagCollectionId(), e);
		}
	}

	/** A subtree transformation may leave the reader at whitespace or at the next element. */
	protected boolean hasImportElement(XMLStreamReader reader) throws XMLStreamException {
		while (!reader.isStartElement() && !reader.isEndElement() && reader.hasNext()) {
			reader.next();
		}
		return reader.isStartElement();
	}

	protected long xmlDateToMs(Node nNode, String element) {

		try {
			Element node = (Element) nNode;
			String dateText = getString("Day", node) + "/" + getString("Month", node) + "/" + getString("Year", node);
			Date d = getDate(dateText);
			try {
				long timestamp = d.getTime();
				return timestamp;
			} catch (Exception e) {
				log.debug("The date format is not the expected at: " + element, e);
				log.debug("DateText is:" + dateText);
				return 0L;
			}
		}catch (Exception e){
			log.debug("The date format is not the expected when importing a tag or collection at: " + element, e);
			return 0L;
		}
	}

	protected String getString(String tagName, Element element) {
		NodeList list = element.getElementsByTagName(tagName);
		if (list != null && list.getLength() > 0) {
			NodeList subList = list.item(0).getChildNodes();

			if (subList != null && subList.getLength() > 0) {
				return subList.item(0).getNodeValue();
			}
		}

		return null;
	}

	protected long stringToLong(String stringToConvert, long defaultvalue) {

		try{
			return Long.parseLong(stringToConvert);
		}catch (Exception ex){
			return defaultvalue;
		}
	}

	protected void deleteTagsOlderThanDateFromCollection(String externalSourceName, long lastmodificationdate ){
		tagService.deleteTagsOlderThanDateFromCollection(getTagCollectionIdFromExternalSourceName(externalSourceName),lastmodificationdate);
	}

	protected void deleteTagsOlderThanDateFromCollectionWithCollectionId(String tagCollectionId, long lastmodificationdate ){
		tagService.deleteTagsOlderThanDateFromCollection(tagCollectionId,lastmodificationdate);
	}

	protected void deleteTagFromExternalCollection(String externalId, String externalSourceName){
		tagService.deleteTagFromExternalCollection(externalId, getTagCollectionIdFromExternalSourceName(externalSourceName) );
	}


}
