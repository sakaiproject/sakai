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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
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
public abstract class TagSynchronizer {
	private static final Log log = LogFactory.getLog(TagSynchronizer.class);

	private TagService tagService() {
		return (TagService) ComponentManager.get(TagService.class);
	}
	
	protected abstract InputStream getTagsXmlInputStream();

	/*protected Set getChildValues(Element element) {
		Set childValues = new HashSet();
		List<Element> childElements = element.getChildren();
		for(Element childElement : childElements) {
			childValues.add(childElement.getText());
		}
		return childValues;
	}*/

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

	protected String getTagCollectionIdFromExternalSourceName(String str) {
		if(StringUtils.isBlank(str)) {
			return null;
		}
		TagCollection tagCollection = tagService().getTagCollectionForExternalSourceName(str).get();
		try {
			return tagCollection.getTagCollectionId();
		} catch (Exception e) {
			log.warn("Invalid External Source Name: " + str);
			return null;
		}
	}

	protected void updateOrCreateTagWithExternalSourceName(String externalId, String externalSourceName, String tagLabel, String description,
			String alternativeLabels, long externalCreationDate, long lastUpdateDateInExternalSystem, String parentId,
			String externalHierarchyCode, String externalType, String data) {
		String collectionID = getTagCollectionIdFromExternalSourceName(externalSourceName);
		if (externalSourceName!=null){
			if (tagService().getTagForExternalIdAndCollection(externalId,collectionID).isPresent()){
				Tag tag = tagService().getTagForExternalIdAndCollection(externalId,collectionID).get();
				tag.setTagLabel(tagLabel);
				tag.setDescription(description);
				tag.setAlternativeLabels(alternativeLabels);
				tag.setExternalUpdate(Boolean.TRUE);
				tag.setLastUpdateDateInExternalSystem(lastUpdateDateInExternalSystem);
				tag.setParentId(parentId);
				tag.setExternalHierarchyCode(externalHierarchyCode);
				tag.setExternalType(externalType);
				tag.setData(data);
				tagService().updateTag(tag);

			}else {
				Tag tag = Tag.builder()
						.tagCollectionId(collectionID)
						.tagLabel(tagLabel)
						.description(description)
						.externalId(externalId)
						.alternativeLabels(alternativeLabels)
						.externalCreation(Boolean.TRUE)
						.externalCreationDate(externalCreationDate)
						.externalUpdate(Boolean.TRUE)
						.lastUpdateDateInExternalSystem(lastUpdateDateInExternalSystem)
						.parentId(parentId)
						.externalHierarchyCode(externalHierarchyCode)
						.externalType(externalType)
						.data(data)
						.build();
				tagService().createTag(tag);
			}
		}
	}

	protected void updateOrCreateTagWithCollectionId(String externalId, String tagCollectionId, String tagLabel, String description,
									 String alternativeLabels, long externalCreationDate, long lastUpdateDateInExternalSystem, String parentId,
									 String externalHierarchyCode, String externalType, String data) {

		if (tagCollectionId!=null){
			if (tagService().getTagForExternalIdAndCollection(externalId,tagCollectionId).isPresent()){
				Tag tag = tagService().getTagForExternalIdAndCollection(externalId,tagCollectionId).get();
				tag.setTagLabel(tagLabel);
				tag.setDescription(description);
				tag.setAlternativeLabels(alternativeLabels);
				tag.setExternalUpdate(Boolean.TRUE);
				tag.setLastUpdateDateInExternalSystem(lastUpdateDateInExternalSystem);
				tag.setParentId(parentId);
				tag.setExternalHierarchyCode(externalHierarchyCode);
				tag.setExternalType(externalType);
				tag.setData(data);
				tagService().updateTag(tag);

			}else {
				Tag tag = Tag.builder()
						.tagCollectionId(tagCollectionId)
						.tagLabel(tagLabel)
						.description(description)
						.externalId(externalId)
						.alternativeLabels(alternativeLabels)
						.externalCreation(Boolean.TRUE)
						.externalCreationDate(externalCreationDate)
						.externalUpdate(Boolean.TRUE)
						.lastUpdateDateInExternalSystem(lastUpdateDateInExternalSystem)
						.parentId(parentId)
						.externalHierarchyCode(externalHierarchyCode)
						.externalType(externalType)
						.data(data)
						.build();
				tagService().createTag(tag);
			}
		}
	}

	protected void updateLabelWithId(String tagId,String externalId, String tagCollectionId, String tagLabel, String description,
													 String alternativeLabels, long externalCreationDate, long lastUpdateDateInExternalSystem, String parentId,
													 String externalHierarchyCode, String externalType, String data) {

			if (tagWithIdIsPresent(tagId)){
				Tag tag = tagService().getTag(tagId).get();
				tag.setTagCollectionId(tagCollectionId);
				tag.setTagLabel(tagLabel);
				tag.setDescription(description);
				tag.setAlternativeLabels(alternativeLabels);
				tag.setExternalCreationDate(externalCreationDate);
				tag.setExternalUpdate(Boolean.TRUE);
				tag.setExternalId(externalId);
				tag.setLastUpdateDateInExternalSystem(lastUpdateDateInExternalSystem);
				tag.setParentId(parentId);
				tag.setExternalHierarchyCode(externalHierarchyCode);
				tag.setExternalType(externalType);
				tag.setData(data);
				tagService().updateTag(tag);

			}else {
				log.warn("Not found tag with TagId: " + tagId);
			}

	}

	protected boolean tagWithIdIsPresent(String tagId){
		return tagService().getTag(tagId).isPresent();
	}


	protected void updateOrCreateTagCollection(String name, String description,
											   String externalSourceName, String externalSourceDescription,
											   long lastUpdateDateInExternalSystem){
		if (externalSourceName!=null){
			if (tagService().getTagCollectionForExternalSourceName(externalSourceName).isPresent()){
				TagCollection tagCollection = tagService().getTagCollectionForExternalSourceName(externalSourceName).get();
				tagCollection.setName(name);
				tagCollection.setExternalSourceDescription(externalSourceDescription);
				tagCollection.setLastUpdateDateInExternalSystem(lastUpdateDateInExternalSystem);
				tagCollection.setExternalUpdate(tagCollection.getExternalUpdate());
				tagService().updateTagCollection(tagCollection);
			}else {
				TagCollection tagCollection = TagCollection.builder()
						.name(name)
						.description(description)
						.externalSourceName(externalSourceName)
						.externalSourceDescription(externalSourceDescription)
						.externalUpdate(Boolean.TRUE)
						.externalCreation(Boolean.TRUE)
						.lastUpdateDateInExternalSystem(lastUpdateDateInExternalSystem)
						.build();
				tagService().createTagCollection(tagCollection);
			}
		}
	}



	protected void updateTagCollectionSynchronization(String externalSourceName, long lastUpdateDateInExternalSystem) {
		if((StringUtils.isNotBlank(externalSourceName))) {

			TagCollection tagCollection = tagService().getTagCollectionForExternalSourceName(externalSourceName).get();
			tagCollection.setExternalUpdate(Boolean.TRUE);
			tagCollection.setLastSynchronizationDate(System.currentTimeMillis());
			tagCollection.setLastUpdateDateInExternalSystem(lastUpdateDateInExternalSystem);
			try {
				tagService().updateTagCollection(tagCollection);
			} catch (Exception e) {
				log.warn("Invalid External Source Name: " + externalSourceName);
			}
		}else{
			log.warn("Invalid External Source Name: " + externalSourceName);
		}
	}

	protected void updateTagCollectionSynchronizationWithCollectionId(String tagCollectionId, long lastUpdateDateInExternalSystem) {
		if(tagService().getTagCollection(tagCollectionId).isPresent()) {
			TagCollection tagCollection = tagService().getTagCollection(tagCollectionId).get();
			tagCollection.setExternalUpdate(Boolean.TRUE);
			tagCollection.setLastSynchronizationDate(System.currentTimeMillis());
			tagCollection.setLastUpdateDateInExternalSystem(lastUpdateDateInExternalSystem);
			try {
				tagService().updateTagCollection(tagCollection);
			} catch (Exception e) {
				log.warn("Invalid CollectionId: " + tagCollectionId);
			}
		}else{
			log.warn("Invalid CollectionId: " + tagCollectionId);
		}
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
		tagService().deleteTagsOlderThanDateFromCollection(getTagCollectionIdFromExternalSourceName(externalSourceName),lastmodificationdate);
	}

	protected void deleteTagsOlderThanDateFromCollectionWithCollectionId(String tagCollectionId, long lastmodificationdate ){
		tagService().deleteTagsOlderThanDateFromCollection(tagCollectionId,lastmodificationdate);
	}

	protected void deleteTagFromExternalCollection(String externalId, String externalSourceName){
		tagService().deleteTagFromExternalCollection(externalId, getTagCollectionIdFromExternalSourceName(externalSourceName) );
	}


}
