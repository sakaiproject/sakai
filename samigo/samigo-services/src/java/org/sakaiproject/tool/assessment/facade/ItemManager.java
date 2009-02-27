/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/facade/ItemManager.java $
 * $Id: ItemManager.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.facade;
import java.util.Date;
import java.util.HashSet;

import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
import org.sakaiproject.tool.assessment.data.dao.assessment.AnswerFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ItemManager {
  private static Log log = LogFactory.getLog(ItemManager.class);

  public ItemManager() {
  }

  public static void main(String[] args){
    //ItemManager instance = new ItemManager();
    System.exit(0);
  }

  public ItemData prepareItem() {
      ItemData item = new ItemData();
      item.setInstruction("Matching game");
      item.setTypeId( Long.valueOf(9));
      item.setScore( Float.valueOf(10));
      item.setDiscount(Float.valueOf(0));
      item.setHasRationale(Boolean.FALSE);
      item.setStatus( Integer.valueOf(1));
      item.setCreatedBy("1");
      item.setCreatedDate(new Date());
      item.setLastModifiedBy("1");
      item.setLastModifiedDate(new Date());

      // prepare itemText
      item.setItemTextSet(prepareText(item));
      item.addItemText("I have",new HashSet());

      // prepare MetaData
      item.setItemMetaDataSet(prepareMetaData(item));
      item.addItemMetaData("ITEM_OBJECTIVE", "the objective is to ...");

      // prepare feedback
      item.setCorrectItemFeedback("well done!");
      item.setInCorrectItemFeedback("better luck next time!");
      log.debug("****item in ItemManager ="+item);
      return item;
  }

  private HashSet prepareText(ItemData item) {
    HashSet textSet = new HashSet();
    ItemText text1 = new ItemText();
    text1.setItem(item);
    text1.setSequence( Long.valueOf(1));
    text1.setText("cat has");
    HashSet answerSet1 = new HashSet();
    HashSet answerFeedbackSet1 = new HashSet();
    Answer answer1 = new Answer(text1, "2 legs", Long.valueOf(1), "i",
    		Boolean.FALSE, null, Float.valueOf(0), Float.valueOf(0));
    answerFeedbackSet1.add(new AnswerFeedback(answer1, "incorrect", "sorry"));
    answer1.setAnswerFeedbackSet(answerFeedbackSet1);
    answerSet1.add(answer1);
    answerSet1.add(new Answer(text1, "3 legs", Long.valueOf(2), "ii",
    		Boolean.FALSE, null, Float.valueOf(0), Float.valueOf(0)));
    answerSet1.add(new Answer(text1, "4 legs", Long.valueOf(3), "iii",
    		Boolean.TRUE, null, Float.valueOf(5), Float.valueOf(0)));
    text1.setAnswerSet(answerSet1);

    textSet.add(text1);

    ItemText text2 = new ItemText();
    text2.setItem(item);
    text2.setSequence(new Long(2));
    text2.setText("chicken has");
    HashSet answerSet2 = new HashSet();
    answerSet2.add(new Answer(text2, "2 legs", Long.valueOf(1), "i",
    		Boolean.TRUE, null, Float.valueOf(5), Float.valueOf(0)));
    answerSet2.add(new Answer(text2, "3 legs", Long.valueOf(2), "ii",
    		Boolean.FALSE, null, Float.valueOf(0), Float.valueOf(0)));
    answerSet2.add(new Answer(text2, "4 legs", Long.valueOf(3), "iii",
    		Boolean.FALSE, null, Float.valueOf(0), Float.valueOf(0)));
    text2.setAnswerSet(answerSet2);
    textSet.add(text2);

    ItemText text3 = new ItemText();
    text3.setItem(item);
    text3.setSequence(new Long(3));
    text3.setText("baby has");
    HashSet answerSet3 = new HashSet();
    answerSet3.add(new Answer(text3, "2 legs", Long.valueOf(1), "i",
    		Boolean.FALSE, null, Float.valueOf(0), Float.valueOf(0)));
    answerSet3.add(new Answer(text3, "3 legs", Long.valueOf(2), "ii",
    		Boolean.FALSE, null, Float.valueOf(0), Float.valueOf(0)));
    answerSet3.add(new Answer(text3, "4 legs", Long.valueOf(3), "iii",
    		Boolean.TRUE, null, Float.valueOf(5), Float.valueOf(0)));
    text3.setAnswerSet(answerSet3);
    textSet.add(text3);
    return textSet;
  }

  private HashSet prepareMetaData(ItemData item) {
    HashSet set = new HashSet();
    set.add(new ItemMetaData(item, "qmd_itemtype", "Matching"));
    set.add(new ItemMetaData(item, "TEXT_FORMAT", "HTML"));
    set.add(new ItemMetaData(item, "MUTUALLY_EXCLUSIVE", "True"));
    return set;
  }

  /*
  private void printItem(ItemData item) {
    log.debug("**Id = " + item.getItemId());
    log.debug("**score = " + item.getScore());
    log.debug("**grade = " + item.getGrade());
    log.debug("**CorrectFeedback is lazy = " +
                       item.getCorrectItemFeedback());
    log.debug("**Objective not lazy = " +
                       item.getItemMetaDataByLabel("ITEM_OBJECTIVE"));
  }

  private void printIfcItem(ItemDataIfc item) {
    log.debug("**Id = " + item.getItemId());
    log.debug("**score = " + item.getScore());
    log.debug("**grade = " + item.getGrade());
    log.debug("**CorrectFeedback is lazy = " +
                       item.getCorrectItemFeedback());
    log.debug("**Objective not lazy = " +
                       item.getItemMetaDataByLabel("ITEM_OBJECTIVE"));
    log.debug("**createdDate = " +
                       item.getCreatedDate());
  }

  private void printFacadeItem(ItemDataIfc item) {
    ItemFacade f = new ItemFacade(item);
    log.debug("****Id = " + f.getItemId());
    log.debug("****score = " + f.getScore());
    log.debug("****grade = " + f.getGrade());
    log.debug("****CorrectFeedback is lazy = " +
                       f.getCorrectItemFeedback());
    log.debug("****Objective not lazy = " +
                       f.getItemMetaDataByLabel("ITEM_OBJECTIVE"));
    log.debug("****createdDate = " +
                       f.getCreatedDate());
    log.debug("****ItemType = " +
                       f.getItemType().getKeyword());
  }
  */
  
    /*
  private void exportXml(ItemDataIfc item) {
    XStream xstream = new XStream();
    xstream = new XStream(new DomDriver());
    xstream.alias("item", ItemData.class);
    xstream.alias("itemText", ItemText.class);
    xstream.alias("itemFeedback", ItemFeedback.class);
    xstream.alias("itemMetaData", ItemMetaData.class);
    xstream.alias("answer", Answer.class);
    xstream.alias("answerFeedback", AnswerFeedback.class);
    String xml = xstream.toXML(item);
    byte[] b = xml.getBytes();
    try {
      FileOutputStream out = new FileOutputStream("out");
      out.write(b);
    }
    catch (FileNotFoundException ex) {
    }
    catch (IOException ex1) {
    }
  }
    */
}
