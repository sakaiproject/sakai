/*
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.qti.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import org.sakaiproject.component.cover.ComponentManager;

import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.qti.asi.Item;
import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.util.api.FormattedText;

/**
 * Exports items through the QTI 1.2 templates and imports them back, checking
 * that Sakai-specific settings survive the trip.
 */
public class QtiItemRoundTripTest {

  private MockedStatic<ComponentManager> componentManagerMock;

  @Before
  public void setUp() {
    // FormattedText is only used to sanitize item text; pass strings through unchanged
    FormattedText formattedText = Mockito.mock(FormattedText.class, invocation ->
        invocation.getMethod().getReturnType() == String.class && invocation.getArguments().length > 0
            ? invocation.getArgument(0)
            : Answers.RETURNS_DEFAULTS.answer(invocation));
    componentManagerMock = Mockito.mockStatic(ComponentManager.class);
    componentManagerMock.when(() -> ComponentManager.get(FormattedText.class)).thenReturn(formattedText);
  }

  @After
  public void tearDown() {
    componentManagerMock.close();
  }

  // SAK-42532
  @Test
  public void extraCreditSurvivesRoundTrip() {
    ItemData item = newItem(TypeIfc.ESSAY_QUESTION, "Essay");
    item.setIsExtraCredit(true);
    assertTrue(roundTrip(item).getIsExtraCredit());
  }

  @Test
  public void regularItemIsNotExtraCreditAfterRoundTrip() {
    assertFalse(roundTrip(newItem(TypeIfc.ESSAY_QUESTION, "Essay")).getIsExtraCredit());
  }

  @Test
  public void fileWithoutExtraCreditFieldImportsAsNotExtraCredit() {
    ItemData item = newItem(TypeIfc.ESSAY_QUESTION, "Essay");
    item.setIsExtraCredit(true);
    Item itemXml = export(item);
    // simulate a file exported before EXTRA_CREDIT was written
    itemXml.setFieldentry("EXTRA_CREDIT", "");
    assertFalse(importItem(itemXml).getIsExtraCredit());
  }

  // SAK-40476
  @Test
  public void minScoreSurvivesRoundTrip() {
    for (Long typeId : new Long[] {TypeIfc.MULTIPLE_CHOICE, TypeIfc.TRUE_FALSE, TypeIfc.ESSAY_QUESTION, TypeIfc.FILL_IN_BLANK}) {
      ItemData item = newItem(typeId, TypeIfc.FILL_IN_BLANK.equals(typeId) ? "Roses are {red}" : "Question");
      addAnswer(item, "red", "A", true, null);
      item.setMinScore(2.5d);
      assertEquals("type " + typeId, Double.valueOf(2.5d), roundTrip(item).getMinScore());
    }
  }

  @Test
  public void missingMinScoreStaysUnset() {
    assertNull(roundTrip(newItem(TypeIfc.ESSAY_QUESTION, "Essay")).getMinScore());
  }

  // SAK-43227
  @Test
  public void singleCorrectPartialCreditSurvivesRoundTrip() {
    ItemData item = newItem(TypeIfc.MULTIPLE_CHOICE, "Pick one");
    item.setPartialCreditFlag(true);
    addAnswer(item, "right", "A", true, 100d);
    addAnswer(item, "half", "B", false, 50d);
    addAnswer(item, "wrong", "C", false, 0d);

    Map<String, Double> partialCredits = new HashMap<>();
    for (ItemTextIfc itemText : roundTrip(item).getItemTextSet()) {
      for (AnswerIfc answer : itemText.getAnswerSet()) {
        partialCredits.put(answer.getLabel(), answer.getPartialCredit());
      }
    }
    assertEquals(Map.of("A", 100d, "B", 50d, "C", 0d), partialCredits);
  }

  // SAK-34222
  @Test
  public void multipleCorrectAllOrNothingSurvivesRoundTrip() {
    for (String mcmsPartialCredit : new String[] {"false", "true"}) {
      ItemData item = newItem(TypeIfc.MULTIPLE_CORRECT, "Pick many");
      item.getItemMetaDataSet().add(new ItemMetaData(item, ItemMetaDataIfc.MCMS_PARTIAL_CREDIT, mcmsPartialCredit));
      addAnswer(item, "a", "A", true, null);
      addAnswer(item, "b", "B", true, null);
      addAnswer(item, "c", "C", false, null);
      assertEquals(mcmsPartialCredit, roundTrip(item).getItemMetaDataByLabel(ItemMetaDataIfc.MCMS_PARTIAL_CREDIT));
    }
  }

  // SAK-52973
  @Test
  public void fillInBlankMutuallyExclusiveSurvivesRoundTrip() {
    ItemData item = newItem(TypeIfc.FILL_IN_BLANK, "Name two colours: {red|blue} and {red|blue}");
    item.getItemMetaDataSet().add(new ItemMetaData(item, ItemMetaDataIfc.MUTUALLY_EXCLUSIVE_FOR_FIB, "true"));
    addAnswer(item, "red|blue", "A", true, null);
    addAnswer(item, "red|blue", "B", true, null);
    assertEquals("true", roundTrip(item).getItemMetaDataByLabel(ItemMetaDataIfc.MUTUALLY_EXCLUSIVE_FOR_FIB));
  }

  private ItemFacade roundTrip(ItemData item) {
    return importItem(export(item));
  }

  private ItemData newItem(Long typeId, String text) {
    ItemData item = new ItemData();
    item.setItemId(1L);
    item.setTypeId(typeId);
    item.setScore(10d);
    item.setDiscount(0d);
    item.setItemMetaDataSet(new HashSet<>());
    item.setItemTagSet(new HashSet<>());
    item.setItemFeedbackSet(new HashSet<>());
    item.setItemAttachmentSet(new HashSet<>());
    Set<ItemTextIfc> texts = new HashSet<>();
    texts.add(new ItemText(item, 1L, text, new LinkedHashSet<>()));
    item.setItemTextSet(texts);
    return item;
  }

  private void addAnswer(ItemData item, String text, String label, boolean correct, Double partialCredit) {
    ItemTextIfc itemText = item.getItemTextSet().iterator().next();
    long sequence = itemText.getAnswerSet().size() + 1;
    Answer answer = new Answer(itemText, text, sequence, label, correct, null, item.getScore(), partialCredit, 0d);
    answer.setAnswerFeedbackSet(new HashSet<>());
    itemText.getAnswerSet().add(answer);
  }

  private Item export(ItemData item) {
    Item itemXml = new QTIHelperFactory().getItemHelperInstance(QTIVersion.VERSION_1_2).readTypeXMLItem(item.getTypeId());
    itemXml.setIdent(item.getItemIdString());
    itemXml.update(item);
    return itemXml;
  }

  private ItemFacade importItem(Item itemXml) {
    ExtractionHelper extractionHelper = new ExtractionHelper(QTIVersion.VERSION_1_2);
    Map itemMap = extractionHelper.mapItem(itemXml);
    ItemFacade item = new ItemFacade();
    extractionHelper.updateItem(item, itemXml, itemMap);
    return item;
  }
}
