package org.sakaiproject.citation.impl;

import org.sakaiproject.citation.api.CitationCollectionOrder;
import org.sakaiproject.citation.api.CitationValidator;

import java.util.Arrays;
import java.util.List;

/**
 * Created by nickwilson on 9/29/15.
 */
public class NestedCitationValidator implements CitationValidator {

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
	public boolean isValid(List<CitationCollectionOrder> citationCollectionOrders) {

		for (CitationCollectionOrder h1Section : citationCollectionOrders) {
			if (hasNullCitationIdAndSectionType(h1Section) || !Arrays.asList(NESTED_CITATION_LIST.TOP_LEVEL.getAllowableTypes()).contains(
					h1Section.getSectiontype())){
				return false;
			}
			for (CitationCollectionOrder h2Section : h1Section.getChildren()) {
				if (hasNullCitationIdAndSectionType(h2Section) || !Arrays.asList(NESTED_CITATION_LIST.HEADING1.getAllowableTypes()).contains(
						h2Section.getSectiontype())){
					return false;
				}
				for (CitationCollectionOrder h3Section : h2Section.getChildren()) {
					if (hasNullCitationIdAndSectionType(h3Section) || !Arrays.asList(NESTED_CITATION_LIST.HEADING2.getAllowableTypes()).contains(
							h3Section.getSectiontype())){
						return false;
					}
					for (CitationCollectionOrder citation : h3Section.getChildren()) {
						if (hasNullCitationIdAndSectionType(citation) || !Arrays.asList(NESTED_CITATION_LIST.HEADING3.getAllowableTypes()).contains(
								citation.getSectiontype())){
							return false;
						}
						if (citation.getChildren()!=null && !citation.getChildren().isEmpty()){
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	private boolean hasNullCitationIdAndSectionType(CitationCollectionOrder citationCollectionOrder) {
		return citationCollectionOrder.getCitationid()==null && citationCollectionOrder.getSectiontype()==null;
	}
}
