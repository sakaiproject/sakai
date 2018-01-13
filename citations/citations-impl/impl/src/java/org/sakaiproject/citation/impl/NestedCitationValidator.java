/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package org.sakaiproject.citation.impl;

import org.apache.commons.lang.*;
import org.sakaiproject.citation.api.*;

import java.util.Arrays;
import java.util.List;
import org.sakaiproject.component.cover.*;

/**
 * Created by nickwilson on 9/29/15.
 */
public class NestedCitationValidator implements CitationValidator {

	private CitationService citationService;

	protected CitationService getCitationService() {
		if(this.citationService == null) {
			this.citationService = ComponentManager.get(CitationService.class);
		}
		return this.citationService;
	}

	// Represents a static model for a nested citation list
	// Shows the types of subsections allowed in each section
	enum NESTED_CITATION_LIST {

		TOP_LEVEL {
			@Override
			public CitationCollectionOrder.SectionType[] getAllowableTypes() {
				return new CitationCollectionOrder.SectionType[]{
						CitationCollectionOrder.SectionType.HEADING1,
						null // 'Null' here refers to the unnested list
						};
			}
		},
		HEADING1 {
			@Override
			public CitationCollectionOrder.SectionType[] getAllowableTypes() {
				return new CitationCollectionOrder.SectionType[]{
						CitationCollectionOrder.SectionType.HEADING2,
						CitationCollectionOrder.SectionType.DESCRIPTION,
						CitationCollectionOrder.SectionType.CITATION
				};
			}
		},
		HEADING2 {
			@Override
			public CitationCollectionOrder.SectionType[] getAllowableTypes() {
				return new CitationCollectionOrder.SectionType[]{
						CitationCollectionOrder.SectionType.HEADING3,
						CitationCollectionOrder.SectionType.DESCRIPTION,
						CitationCollectionOrder.SectionType.CITATION};
			}
		},
		HEADING3 {
			@Override
			public CitationCollectionOrder.SectionType[] getAllowableTypes() {
				return new CitationCollectionOrder.SectionType[]{
						CitationCollectionOrder.SectionType.DESCRIPTION,
						CitationCollectionOrder.SectionType.CITATION
				};
			}
		};

		public abstract CitationCollectionOrder.SectionType[] getAllowableTypes();
	}

	@Override
	public String getValidMessage(List<CitationCollectionOrder> citationCollectionOrderList, CitationCollectionOrder citationCollectionOrderTopNode, CitationCollection collection) {

		String errorMessage = validateCollection(collection);
		if (errorMessage!=null){
			return errorMessage;
		}

		int count = 1;
		for (CitationCollectionOrder citationCollectionOrder : citationCollectionOrderList) {

			// Check location column is in correct order, i.e. starts from 1 and increments by 1 for each row
			if (citationCollectionOrder.getLocation()!=count){
				return "Invalid nested list: The LOCATION column does not have the correct sequential order for collection with id: " + collection.getId();
			}
			count++;

			// 1. Check that if SECTION_TYPE column is 'CITATION' that CITATION_ID is not null
			if (citationCollectionOrder.getSectiontype()!=null && citationCollectionOrder.getSectiontype().equals(CitationCollectionOrder.SectionType.CITATION) && citationCollectionOrder.getCitationid()==null){
				return "Invalid nested list: SECTION_TYPE is 'CITATION' and CITATION_ID is null for collection with id: " + collection.getId();
			}
			// 2. Check that if SECTION_TYPE column is a 'HEADING' or a 'DESCRIPTION' that CITATION_ID is null
			if (citationCollectionOrder.getSectiontype()!=null &&  (citationCollectionOrder.getSectiontype().equals(CitationCollectionOrder.SectionType.HEADING1)
					|| citationCollectionOrder.getSectiontype().equals(CitationCollectionOrder.SectionType.HEADING2)
					|| citationCollectionOrder.getSectiontype().equals(CitationCollectionOrder.SectionType.HEADING3)
					|| citationCollectionOrder.getSectiontype().equals(CitationCollectionOrder.SectionType.DESCRIPTION)) && citationCollectionOrder.getCitationid()!=null){
				return "Invalid nested list: SECTION_TYPE is a 'HEADING' or a 'DESCRIPTION' and CITATION_ID is null for collection with id: " + collection.getId();
			}
			// 3. Check that if VALUE column is not null then section type column is not null
			if (citationCollectionOrder.getValue()!=null && citationCollectionOrder.getSectiontype()==null){
				return "Invalid nested list: VALUE is not null but either SECTION_TYPE is null for collection with id: " + collection.getId();
			}

		}

		for (CitationCollectionOrder h1Section : citationCollectionOrderTopNode.getChildren()) {
			if (hasNullCitationIdAndSectionType(h1Section) || !Arrays.asList(NESTED_CITATION_LIST.TOP_LEVEL.getAllowableTypes()).contains(
					h1Section.getSectiontype())){
				return "Invalid nested list: when checking H1 with value: " + h1Section.getValue() + " for collection with id: " +  collection.getId();
			}
			for (CitationCollectionOrder h2Section : h1Section.getChildren()) {
				if (hasNullCitationIdAndSectionType(h2Section) || !Arrays.asList(NESTED_CITATION_LIST.HEADING1.getAllowableTypes()).contains(
						h2Section.getSectiontype())){
					return "Invalid nested list: when checking H2 with value: " + h2Section.getValue() + " for collection with id: " +  collection.getId();
				}
				for (CitationCollectionOrder h3Section : h2Section.getChildren()) {
					if (hasNullCitationIdAndSectionType(h3Section) || !Arrays.asList(NESTED_CITATION_LIST.HEADING2.getAllowableTypes()).contains(
							h3Section.getSectiontype())){
						return "Invalid nested list: when checking H3 with value: " + h3Section.getValue() + " for collection with id: " +  collection.getId();
					}
					for (CitationCollectionOrder citation : h3Section.getChildren()) {
						if (hasNullCitationIdAndSectionType(citation) || !Arrays.asList(NESTED_CITATION_LIST.HEADING3.getAllowableTypes()).contains(
								citation.getSectiontype())){
							return "Invalid nested list: when checking citation with citation id: " + citation.getCitationid() + " for collection with id: " +  collection.getId();
						}
						if (citation.getChildren()!=null && !citation.getChildren().isEmpty()){
							return "Invalid nested list: when checking citation (it has children!) with citation id: " + citation.getCitationid() + " for collection with id: " +  collection.getId();
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public String getAddSectionErrorMessage(CitationCollectionOrder citationCollectionOrder, CitationCollection collection) {

		String errorMessage = validateCitationCollectionOrder(citationCollectionOrder, collection, "<h1>Section Title</h1>", CitationCollectionOrder.SectionType.HEADING1);
		if (errorMessage!=null){
			return errorMessage;
		}
		// Check location from HTML matches the next citation collection order id in the database
		String nextId = getCitationService().getNextCitationCollectionOrderId(citationCollectionOrder.getCollectionId());
		if (StringUtils.isBlank(nextId) || citationCollectionOrder.getLocation()!=Integer.parseInt(nextId)){
			return "Citation Collection Order to be added as an h1 for collection with id: " + collection.getId() + " has an invalid location: "
					+ citationCollectionOrder.getLocation() + " (whereas the next location for the h1 in the db is " + nextId + ")";
		}
		return null;
	}

	@Override
	public String getAddSubSectionErrorMessage(CitationCollectionOrder citationCollectionOrder, CitationCollection collection) {

		String errorMessage = validateCitationCollectionOrder(citationCollectionOrder, collection, null, null);
		if (errorMessage!=null){
			return errorMessage;
		}

		boolean isH2 = citationCollectionOrder.getSectiontype()!=null && citationCollectionOrder.getSectiontype().equals(CitationCollectionOrder.SectionType.HEADING2) &&
				citationCollectionOrder.getValue()!=null;
		boolean isH3 = citationCollectionOrder.getSectiontype()!=null && citationCollectionOrder.getSectiontype().equals(CitationCollectionOrder.SectionType.HEADING3) &&
				citationCollectionOrder.getValue()!=null;
		boolean isDescription = citationCollectionOrder.getSectiontype()!=null && citationCollectionOrder.getSectiontype().equals(CitationCollectionOrder.SectionType.DESCRIPTION) &&
				citationCollectionOrder.getValue()!=null;
		if (!isH2 && !isH3 && !isDescription){
			return "Something has gone wrong: trying to add an subsection that isn't a h2 H3 or a description for collection id:" + citationCollectionOrder.getCollectionId();
		}

		// check the previous CitationCollectionOrder is of the correct type
		List<CitationCollectionOrder> citationCollectionOrderList = getCitationService().getNestedCollectionAsList(collection.getId());
		CitationCollectionOrder previousCitationCollectionOrder = null;
		for (CitationCollectionOrder collectionOrder: citationCollectionOrderList) {
			if (collectionOrder.getLocation()==citationCollectionOrder.getLocation()-1){
				previousCitationCollectionOrder = collectionOrder;
			}
		}
		if (previousCitationCollectionOrder == null) {
			return "Failed to find previous item for collection id: "+ citationCollectionOrder.getCollectionId();
		}

			if (isH2){
			//  check previous CitationCollectionOrder is an h1 h2 h3 or description or citation
			if (!previousCitationCollectionOrder.getSectiontype().equals(CitationCollectionOrder.SectionType.HEADING1) &&
					!previousCitationCollectionOrder.getSectiontype().equals(CitationCollectionOrder.SectionType.HEADING2) &&
						!previousCitationCollectionOrder.getSectiontype().equals(CitationCollectionOrder.SectionType.HEADING3) &&
					!previousCitationCollectionOrder.getSectiontype().equals(CitationCollectionOrder.SectionType.CITATION) &&
					!previousCitationCollectionOrder.getSectiontype().equals(CitationCollectionOrder.SectionType.DESCRIPTION)){
				return "Invalid place to add subsection: trying to add an H2 to something other than an H1 H2 H3 or a DESCRIPTION or CITATION for collection id:" + citationCollectionOrder.getCollectionId();
				}
			}
			else if (isH3){
			//  check previous CitationCollectionOrder is an h2 h3 or description or citation
			if (!previousCitationCollectionOrder.getSectiontype().equals(CitationCollectionOrder.SectionType.HEADING2) &&
					!previousCitationCollectionOrder.getSectiontype().equals(CitationCollectionOrder.SectionType.HEADING3) &&
											!previousCitationCollectionOrder.getSectiontype().equals(CitationCollectionOrder.SectionType.CITATION) &&
					!previousCitationCollectionOrder.getSectiontype().equals(CitationCollectionOrder.SectionType.DESCRIPTION)){
				return "Invalid place to add subsection: trying to add an H3 to something other than an H2 H3 or DESCRIPTION or CITATION for collection id:" + citationCollectionOrder.getCollectionId();
				}
			}
			else if (isDescription){
				//  check previous CitationCollectionOrder is an h1 h2 or h3
			if (!previousCitationCollectionOrder.getSectiontype().equals(CitationCollectionOrder.SectionType.HEADING1) &&
					!previousCitationCollectionOrder.getSectiontype().equals(CitationCollectionOrder.SectionType.HEADING2) &&
					!previousCitationCollectionOrder.getSectiontype().equals(CitationCollectionOrder.SectionType.HEADING3)){
					return "Invalid place to add subsection: trying to add a description to something other than an H1 H2 or H3 for collection id:" + citationCollectionOrder.getCollectionId();
			}
		}
		return null;
	}

	@Override
	public String getRemoveSectionErrorMessage(CitationCollection collection, int locationId) {

		// Check CitationCollectionOrder exists in the database
		CitationCollectionOrder citationCollectionOrder = getCitationService().getCitationCollectionOrder(collection.getId(), locationId);
		if (citationCollectionOrder==null){
			return "Citationcollectionorder does not exist for location id: " + locationId;
		}
		return null;
	}

	@Override
	public String validateExistingDbStructure(CitationCollection collection) {
		List<CitationCollectionOrder> citationCollectionOrderList = getCitationService().getNestedCollectionAsList(collection.getId());
		CitationCollectionOrder citationCollectionOrder = getCitationService().getNestedCollection(collection.getId());

		return getValidMessage(citationCollectionOrderList, citationCollectionOrder, collection);
	}

	@Override
	public String getDragAndDropErrorMessage(List<CitationCollectionOrder> citationCollectionOrders, CitationCollection collection) {

		for (CitationCollectionOrder h1Section : citationCollectionOrders) {
			if (!Arrays.asList(NESTED_CITATION_LIST.TOP_LEVEL.getAllowableTypes()).contains(
					h1Section.getSectiontype())){
				return "Invalid nested list: when checking H1 with value: " + h1Section.getValue() + " for collection with id: " +  h1Section.getCollectionId();
			}
			for (CitationCollectionOrder h2Section : h1Section.getChildren()) {
				if (hasNullCitationIdAndSectionType(h2Section) || !Arrays.asList(NESTED_CITATION_LIST.HEADING1.getAllowableTypes()).contains(
						h2Section.getSectiontype())){
					return "Invalid nested list: when checking H2 with value: " + h2Section.getValue() + " for collection with id: " +  h2Section.getCollectionId();
				}
				for (CitationCollectionOrder h3Section : h2Section.getChildren()) {
					if (hasNullCitationIdAndSectionType(h3Section) || !Arrays.asList(NESTED_CITATION_LIST.HEADING2.getAllowableTypes()).contains(
							h3Section.getSectiontype())){
						return "Invalid nested list: when checking H3 with value: " + h3Section.getValue() + " for collection with id: " +  h3Section.getCollectionId();
					}
					for (CitationCollectionOrder citation : h3Section.getChildren()) {
						if (hasNullCitationIdAndSectionType(citation) || !Arrays.asList(NESTED_CITATION_LIST.HEADING3.getAllowableTypes()).contains(
								citation.getSectiontype())){
							return "Invalid nested list: when checking citation with citation id: " + citation.getCitationid() + " for collection with id: " +  citation.getCollectionId();
						}
						if (citation.getChildren()!=null && !citation.getChildren().isEmpty()){
							return "Invalid nested list: when checking citation (it has children!) with citation id: " + citation.getCitationid() + " for collection with id: " +  citation.getCollectionId();
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public String getUpdateSectionErrorMessage(CitationCollectionOrder citationCollectionOrder, CitationCollection collection) {

		String errorMessage = validateCitationCollectionOrder(citationCollectionOrder, collection, null, null);
		if (errorMessage!=null){
			return errorMessage;
		}

		// check CitationCollectionOrder exists in the database
		CitationCollectionOrder citationCollOrder = getCitationService().getCitationCollectionOrder(collection.getId(), citationCollectionOrder.getLocation());
		if (citationCollOrder==null){
			return "Citationcollectionorder does not exist in the database for collection id : " + citationCollectionOrder.getCollectionId() + " and for location id: " + citationCollectionOrder.getLocation();
		}
		// check CitationCollectionOrder at location is not a citation
		if (citationCollOrder.getSectiontype().equals(CitationCollectionOrder.SectionType.CITATION)){
			return "Citationcollectionorder is a citation in the database for collection id : " + citationCollectionOrder.getCollectionId() + " and for location id: " + citationCollectionOrder.getLocation();
		}

		return null;
	}

	private String validateCitationCollectionOrder(CitationCollectionOrder citationCollectionOrder, CitationCollection collection, String value, CitationCollectionOrder.SectionType sectionType) {
		if (citationCollectionOrder==null){
			return "CitationCollectionOrder is null for collection with collection id: " + collection.getId();
		}
		if (StringUtils.isBlank(citationCollectionOrder.getCollectionId())){
			return "CitationCollectionOrder has an empty collection id for collection with collection id: " + collection.getId();
		}
		if (StringUtils.isNotBlank(citationCollectionOrder.getCitationid())){
			return "CitationCollectionOrder has a non empty citation id of: " + citationCollectionOrder.getCitationid() + " for collection with collection id: " + collection.getId();
		}
		if (!(value==null || citationCollectionOrder.getValue().equals(value))){
			return "CitationCollectionOrder has an invalid value of:" + citationCollectionOrder.getValue() + " for collection with collection id: " + collection.getId();
		}
		if (!(sectionType==null || citationCollectionOrder.getSectiontype().equals(sectionType))){
			return "CitationCollectionOrder has an invalid section type of:" + citationCollectionOrder.getSectiontype() + " for collection with collection id: " + collection.getId();
		}
		return null;
	}

	private String validateCollection(CitationCollection collection) {
		// Check collection is not null
		if (collection==null){
			return "Collection is null";
		}
		// Check collection id is not null
		if (StringUtils.isBlank((collection.getId()))){
			return "Collection has an empty id";
		}
		return null;

	}

	private boolean hasNullCitationIdAndSectionType(CitationCollectionOrder citationCollectionOrder) {
		return citationCollectionOrder.getCitationid()==null && citationCollectionOrder.getSectiontype()==null;
	}
}
