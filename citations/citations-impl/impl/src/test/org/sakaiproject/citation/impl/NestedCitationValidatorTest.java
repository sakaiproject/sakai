package org.sakaiproject.citation.impl;

import org.sakaiproject.citation.api.*;

import java.util.ArrayList;
import java.util.List;

public class NestedCitationValidatorTest extends BaseCitationServiceSupport {

	private CitationValidator citationValidator = new NestedCitationValidator();

	public void testValidList_H1() {

		List<CitationCollectionOrder> citationCollectionOrders = new ArrayList<CitationCollectionOrder>();
		CitationCollectionOrder h1CitationCollectionOrder = getH1();
		citationCollectionOrders.add(h1CitationCollectionOrder);

		boolean isValid = citationValidator.isValid(citationCollectionOrders);
		assertTrue(isValid);
	}

	public void testValidList_H1_Citation() {

		List<CitationCollectionOrder> citationCollectionOrders = new ArrayList<CitationCollectionOrder>();

		// h1 with h2, h3, citation
		CitationCollectionOrder citation = getCitation();
		CitationCollectionOrder h1 = getH1();
		h1.addChild(citation);
		citationCollectionOrders.add(h1);

		boolean isValid = citationValidator.isValid(citationCollectionOrders);
		assertTrue(isValid);
	}

	public void testValidComplexList() {

		List<CitationCollectionOrder> citationCollectionOrders = new ArrayList<CitationCollectionOrder>();

		// h1 with h2, h3, citation
		CitationCollectionOrder citation = getCitation();
		CitationCollectionOrder h3 = getH3();
		h3.addChild(citation);
		CitationCollectionOrder h2 = getH2();
		h2.addChild(h3);
		CitationCollectionOrder h1 = getH1();
		h1.addChild(h2);
		citationCollectionOrders.add(h1);

		// h1 with description and h2 in it ; h2 has h3 and citation in it
		CitationCollectionOrder citation1 = getCitation();
		CitationCollectionOrder h3_1 = getH3();
		h3_1.addChild(citation1);
		CitationCollectionOrder h2_1 = getH2();
		h2_1.addChild(h3_1);
		CitationCollectionOrder h1Description = getDescription();
		CitationCollectionOrder h1_1 = getH1();
		h1_1.addChild(h2_1);
		h1_1.addChild(h1Description);
		citationCollectionOrders.add(h1_1);

		// h1 with h2, h3, citation
		CitationCollectionOrder citation2 = getCitation();
        CitationCollectionOrder h1_2 = getH1();
        h1_2.addChild(citation2);
		citationCollectionOrders.add(h1_2);


		boolean isValid = citationValidator.isValid(citationCollectionOrders);
		assertTrue(isValid);
	}

	public void testInvalidList_Citation() {

		List<CitationCollectionOrder> citationCollectionOrders = new ArrayList<CitationCollectionOrder>();

		// h1 with invalid null h2
		CitationCollectionOrder nullH2 = getNullCitationIdAndNullSectionType();
		citationCollectionOrders.add(nullH2);
		CitationCollectionOrder h1 = getH1();
		h1.addChild(nullH2);
		citationCollectionOrders.add(h1);

		boolean isValid = citationValidator.isValid(citationCollectionOrders);
		assertFalse(isValid);
	}

	public void testInvalidList_NullCitationIdAndNullSectionType() {

		List<CitationCollectionOrder> citationCollectionOrders = new ArrayList<CitationCollectionOrder>();

		CitationCollectionOrder citation = getNullCitationIdAndNullSectionType();
		citationCollectionOrders.add(citation);

		boolean isValid = citationValidator.isValid(citationCollectionOrders);
		assertFalse(isValid);
	}

	public void testInvalidList_H1_H3() {

		List<CitationCollectionOrder> citationCollectionOrders = new ArrayList<CitationCollectionOrder>();

		// h1 with h2, h3, citation
		CitationCollectionOrder h3 = getH3();
		CitationCollectionOrder h1 = getH1();
		h1.addChild(h3);

		citationCollectionOrders.add(h1);

		boolean isValid = citationValidator.isValid(citationCollectionOrders);
		assertFalse(isValid);
	}

	public void testInvalidList_H1_H2_H3_Citation_H3() {

		List<CitationCollectionOrder> citationCollectionOrders = new ArrayList<CitationCollectionOrder>();

		// h1 with h2, h3, citation
		CitationCollectionOrder h3 = getH3();
		CitationCollectionOrder citation = getCitation();
		citation.addChild(h3);
		CitationCollectionOrder h3_1 = getH3();
		h3_1.addChild(citation);
		CitationCollectionOrder h2 = getH2();
		h2.addChild(h3_1);
		CitationCollectionOrder h1 = getH1();
		h1.addChild(h2);
		citationCollectionOrders.add(h1);

		boolean isValid = citationValidator.isValid(citationCollectionOrders);
		assertFalse(isValid);
	}

	private CitationCollectionOrder getH1() {
		CitationCollectionOrder h1CitationCollectionOrder = new CitationCollectionOrder();
		h1CitationCollectionOrder.setSectiontype(CitationCollectionOrder.SectionType.HEADING1);
		return h1CitationCollectionOrder;
	}

	private CitationCollectionOrder getH2() {
		CitationCollectionOrder h2CitationCollectionOrder = new CitationCollectionOrder();
		h2CitationCollectionOrder.setSectiontype(CitationCollectionOrder.SectionType.HEADING2);
		return h2CitationCollectionOrder;
	}

	private CitationCollectionOrder getH3() {
		CitationCollectionOrder h3CitationCollectionOrder = new CitationCollectionOrder();
		h3CitationCollectionOrder.setSectiontype(CitationCollectionOrder.SectionType.HEADING3);
		return h3CitationCollectionOrder;
	}

	private CitationCollectionOrder getCitation() {
		CitationCollectionOrder citation = new CitationCollectionOrder();
		citation.setSectiontype(CitationCollectionOrder.SectionType.CITATION);
		return citation;
	}

	private CitationCollectionOrder getNullCitationIdAndNullSectionType() {
		CitationCollectionOrder citation = new CitationCollectionOrder();
		citation.setCollectionId("xxx");
		citation.setLocation(1);
		return citation;
	}

	private CitationCollectionOrder getDescription() {
		CitationCollectionOrder description = new CitationCollectionOrder();
		description.setSectiontype(CitationCollectionOrder.SectionType.DESCRIPTION);
		return description;
	}

	private CitationCollectionOrder getUnnestedList() {
		return new CitationCollectionOrder();
	}
}
