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

import java.util.*;
import org.jmock.*;
import org.jmock.integration.junit4.*;
import org.jmock.lib.legacy.*;
import org.junit.*;
import org.sakaiproject.citation.api.*;

public class NestedCitationValidatorTest  {

	private CitationValidator citationValidator = new NestedCitationValidator();
	private Mockery context = new JUnit4Mockery() {{
		setImposteriser(ClassImposteriser.INSTANCE); // Needed to mock an abstract class.
	}};
	private CitationCollection citationCollection;
	private List<CitationCollectionOrder> citationCollectionOrderList;

	@Before
	public void setUp() {
		citationCollection = context.mock(CitationCollection.class);
		context.checking(new Expectations(){{
			allowing(citationCollection).getId();
			will(returnValue("someid"));
		}});
		citationCollectionOrderList = new ArrayList<>();
	}

	@Test
	public void testValidList_H1() {
		List<CitationCollectionOrder> citationCollectionOrders = new ArrayList<>();
		CitationCollectionOrder h1 = getH1();
		citationCollectionOrders.add(h1);

		CitationCollectionOrder citationCollectionOrderTopNode = new CitationCollectionOrder();
		citationCollectionOrderTopNode.addChild(h1);
		citationCollectionOrderList = h1.flatten();

		String validMessage = citationValidator.getValidMessage(citationCollectionOrderList, citationCollectionOrderTopNode, citationCollection);
		assert(validMessage==null);
	}

	@Test
	public void testInValidList_H1_Citation_Location_Wrong() {

		List<CitationCollectionOrder> citationCollectionOrders = new ArrayList<CitationCollectionOrder>();

		// h1 with h2, h3, citation
		CitationCollectionOrder citation = new CitationCollectionOrder();
		citation.setSectiontype(CitationCollectionOrder.SectionType.CITATION);
		CitationCollectionOrder h1 = getH1();
		h1.addChild(citation);
		citationCollectionOrders.add(h1);

		CitationCollectionOrder citationCollectionOrderTopNode = new CitationCollectionOrder();
		citationCollectionOrderTopNode.addChild(h1);
		citationCollectionOrderList = h1.flatten();

		String validMessage = citationValidator.getValidMessage(citationCollectionOrderList, citationCollectionOrderTopNode, citationCollection);
		assert(validMessage.equals("Invalid nested list: The LOCATION column does not have the correct sequential order for collection with id: someid"));
	}

	@Test
	public void testValidList_H1_Citation() {

		List<CitationCollectionOrder> citationCollectionOrders = new ArrayList<CitationCollectionOrder>();

		// h1 with h2, h3, citation
		CitationCollectionOrder citation = new CitationCollectionOrder();
		citation.setSectiontype(CitationCollectionOrder.SectionType.CITATION);
		citation.setLocation(2);
		citation.setCitationid("citid");
		CitationCollectionOrder h1 = getH1();
		h1.addChild(citation);
		citationCollectionOrders.add(h1);

		CitationCollectionOrder citationCollectionOrderTopNode = new CitationCollectionOrder();
		citationCollectionOrderTopNode.addChild(h1);
		citationCollectionOrderList = h1.flatten();

		String validMessage = citationValidator.getValidMessage(citationCollectionOrderList, citationCollectionOrderTopNode, citationCollection);
		assert(null==validMessage);
	}

	@Test
	public void testInValidList_H1_Citation_NoCitationId() {

		List<CitationCollectionOrder> citationCollectionOrders = new ArrayList<CitationCollectionOrder>();

		// h1 with h2, h3, citation
		CitationCollectionOrder citation = new CitationCollectionOrder();
		citation.setSectiontype(CitationCollectionOrder.SectionType.CITATION);
		citation.setLocation(2);
		CitationCollectionOrder h1 = getH1();
		h1.addChild(citation);
		citationCollectionOrders.add(h1);

		CitationCollectionOrder citationCollectionOrderTopNode = new CitationCollectionOrder();
		citationCollectionOrderTopNode.addChild(h1);
		citationCollectionOrderList = h1.flatten();

		String validMessage = citationValidator.getValidMessage(citationCollectionOrderList, citationCollectionOrderTopNode, citationCollection);
		assert(validMessage.equals("Invalid nested list: SECTION_TYPE is 'CITATION' and CITATION_ID is null for collection with id: someid"));
	}

	@Test
	public void testInValidComplexList_WrongLocationOrder() {

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

		CitationCollectionOrder citationCollectionOrderTopNode = new CitationCollectionOrder();
		citationCollectionOrderTopNode.addChild(h1);
		citationCollectionOrderList = h1.flatten();


		String validMessage = citationValidator.getValidMessage(citationCollectionOrderList, citationCollectionOrderTopNode, citationCollection);
		assert(validMessage.equals("Invalid nested list: The LOCATION column does not have the correct sequential order for collection with id: someid"));
	}

	@Test
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

		CitationCollectionOrder citationCollectionOrderTopNode = new CitationCollectionOrder();
		citationCollectionOrderTopNode.addChild(h1);
		citationCollectionOrderList = h1.flatten();


		String validMessage = citationValidator.getValidMessage(citationCollectionOrderList, citationCollectionOrderTopNode, citationCollection);
		assert(validMessage.equals("Invalid nested list: The LOCATION column does not have the correct sequential order for collection with id: someid"));
	}

	@Test
	public void testInvalidList_Citation() {

		// h1 with invalid null h2
		CitationCollectionOrder nullH2 = getNullCitationIdAndNullSectionType(2);
		CitationCollectionOrder h1 = getH1();
		h1.addChild(nullH2);

		CitationCollectionOrder citationCollectionOrderTopNode = new CitationCollectionOrder();
		citationCollectionOrderTopNode.addChild(h1);
		citationCollectionOrderList = h1.flatten();

		String validMessage = citationValidator.getValidMessage(citationCollectionOrderList, citationCollectionOrderTopNode, citationCollection);
		assert(validMessage.equals("Invalid nested list: when checking H2 with value: null for collection with id: someid"));
	}

	@Test
	public void testInvalidList_NullCitationIdAndNullSectionType() {

		List<CitationCollectionOrder> citationCollectionOrders = new ArrayList<CitationCollectionOrder>();

		// h1 with invalid null h2
		CitationCollectionOrder nullH2 = getNullCitationIdAndNullSectionType(2);
		citationCollectionOrders.add(nullH2);
		CitationCollectionOrder h1 = getH1();
		h1.addChild(nullH2);
		citationCollectionOrders.add(h1);

		CitationCollectionOrder citationCollectionOrderTopNode = new CitationCollectionOrder();
		citationCollectionOrderTopNode.addChild(h1);
		citationCollectionOrderList = h1.flatten();

		String validMessage = citationValidator.getValidMessage(citationCollectionOrderList, citationCollectionOrderTopNode, citationCollection);
		assert(validMessage.equals("Invalid nested list: when checking H2 with value: null for collection with id: someid"));
	}

	@Test
	public void testInvalidList_H1_H3_WrongLocation() {

		List<CitationCollectionOrder> citationCollectionOrders = new ArrayList<CitationCollectionOrder>();

		// h1 with h2, h3, citation
		CitationCollectionOrder h3 = getH3();
		CitationCollectionOrder h1 = getH1();
		h1.addChild(h3);

		CitationCollectionOrder citationCollectionOrderTopNode = new CitationCollectionOrder();
		citationCollectionOrderTopNode.addChild(h1);
		citationCollectionOrderList = h1.flatten();

		String validMessage = citationValidator.getValidMessage(citationCollectionOrderList, citationCollectionOrderTopNode, citationCollection);
		assert(validMessage.equals("Invalid nested list: The LOCATION column does not have the correct sequential order for collection with id: someid"));
	}


	public void testInvalidList_H1_H3() {

		List<CitationCollectionOrder> citationCollectionOrders = new ArrayList<CitationCollectionOrder>();

		// h1 with h2, h3, citation
		CitationCollectionOrder h3 = new CitationCollectionOrder();
		h3.setSectiontype(CitationCollectionOrder.SectionType.HEADING3);
		h3.setLocation(2);
		CitationCollectionOrder h1 = getH1();
		h1.addChild(h3);

		CitationCollectionOrder citationCollectionOrderTopNode = new CitationCollectionOrder();
		citationCollectionOrderTopNode.addChild(h1);
		citationCollectionOrderList = h1.flatten();

		String validMessage = citationValidator.getValidMessage(citationCollectionOrderList, citationCollectionOrderTopNode, citationCollection);
		assert(validMessage.equals("Invalid nested list: when checking H2 with value: null for collection with id: someid"));
	}

	@Test
	public void testInvalidList_H1_H2_H3_Citation_H3_WrongLocation() {

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

		CitationCollectionOrder citationCollectionOrderTopNode = new CitationCollectionOrder();
		citationCollectionOrderTopNode.addChild(h1);
		citationCollectionOrderList = h1.flatten();

		String validMessage = citationValidator.getValidMessage(citationCollectionOrderList, citationCollectionOrderTopNode, citationCollection);
		assert(validMessage.equals("Invalid nested list: The LOCATION column does not have the correct sequential order for collection with id: someid"));
	}

	@Test
	public void testInvalidList_H1_H2_H3_Citation_H3_NoCitationId() {

		List<CitationCollectionOrder> citationCollectionOrders = new ArrayList<CitationCollectionOrder>();

		// h1 with h2, h3, citation
		CitationCollectionOrder h3 = getH3();
		CitationCollectionOrder citation = new CitationCollectionOrder();
		citation.setSectiontype(CitationCollectionOrder.SectionType.CITATION);
		citation.setLocation(4);
		citation.addChild(h3);
		CitationCollectionOrder h3_1 = getH3();
		h3_1.addChild(citation);
		CitationCollectionOrder h2 = getH2();
		h2.addChild(h3_1);
		CitationCollectionOrder h1 = getH1();
		h1.addChild(h2);
		citationCollectionOrders.add(h1);

		CitationCollectionOrder citationCollectionOrderTopNode = new CitationCollectionOrder();
		citationCollectionOrderTopNode.addChild(h1);
		citationCollectionOrderList = h1.flatten();

		String validMessage = citationValidator.getValidMessage(citationCollectionOrderList, citationCollectionOrderTopNode, citationCollection);
		assert(validMessage.equals("Invalid nested list: SECTION_TYPE is 'CITATION' and CITATION_ID is null for collection with id: someid"));
	}

	@Test
	public void testInvalidList_H1_H2_H3_Citation_H3_WrongChildren() {

		List<CitationCollectionOrder> citationCollectionOrders = new ArrayList<CitationCollectionOrder>();

		// h1 with h2, h3, citation
		CitationCollectionOrder h3 = getH3();
		CitationCollectionOrder citation = new CitationCollectionOrder();
		citation.setSectiontype(CitationCollectionOrder.SectionType.CITATION);
		citation.setLocation(4);
		citation.setCitationid("fdxg");
		citation.addChild(h3);
		CitationCollectionOrder h3_1 = getH3();
		h3_1.addChild(citation);
		CitationCollectionOrder h2 = getH2();
		h2.addChild(h3_1);
		CitationCollectionOrder h1 = getH1();
		h1.addChild(h2);
		citationCollectionOrders.add(h1);

		CitationCollectionOrder citationCollectionOrderTopNode = new CitationCollectionOrder();
		citationCollectionOrderTopNode.addChild(h1);
		citationCollectionOrderList = h1.flatten();

		String validMessage = citationValidator.getValidMessage(citationCollectionOrderList, citationCollectionOrderTopNode, citationCollection);
		assert(validMessage.equals("Invalid nested list: when checking citation (it has children!) with citation id: fdxg for collection with id: someid"));
	}

	private CitationCollectionOrder getH1() {
		CitationCollectionOrder h1CitationCollectionOrder = new CitationCollectionOrder();
		h1CitationCollectionOrder.setSectiontype(CitationCollectionOrder.SectionType.HEADING1);
		h1CitationCollectionOrder.setLocation(1);
		return h1CitationCollectionOrder;
	}

	private CitationCollectionOrder getH2() {
		CitationCollectionOrder h2CitationCollectionOrder = new CitationCollectionOrder();
		h2CitationCollectionOrder.setSectiontype(CitationCollectionOrder.SectionType.HEADING2);
		h2CitationCollectionOrder.setLocation(2);
		return h2CitationCollectionOrder;
	}

	private CitationCollectionOrder getH3() {
		CitationCollectionOrder h3CitationCollectionOrder = new CitationCollectionOrder();
		h3CitationCollectionOrder.setSectiontype(CitationCollectionOrder.SectionType.HEADING3);
		h3CitationCollectionOrder.setLocation(3);
		return h3CitationCollectionOrder;
	}

	private CitationCollectionOrder getCitation() {
		CitationCollectionOrder citation = new CitationCollectionOrder();
		citation.setSectiontype(CitationCollectionOrder.SectionType.CITATION);
		citation.setLocation(2);
		return citation;
	}

	private CitationCollectionOrder getNullCitationIdAndNullSectionType(int location) {
		CitationCollectionOrder citation = new CitationCollectionOrder();
		citation.setCollectionId("xxx");
		citation.setLocation(location);
		return citation;
	}

	private CitationCollectionOrder getDescription() {
		CitationCollectionOrder description = new CitationCollectionOrder();
		description.setSectiontype(CitationCollectionOrder.SectionType.DESCRIPTION);
		return description;
	}
}
