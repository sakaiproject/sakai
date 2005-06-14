package org.sakaiproject.tool.assessment.facade;
import java.util.Date;
import java.util.HashSet;

import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
import org.sakaiproject.tool.assessment.data.dao.assessment.AnswerFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;

public class ItemManager {

  public ItemManager() {
  }

  public static void main(String[] args){
    ItemManager instance = new ItemManager();
    System.exit(0);
  }

  public ItemData prepareItem() {
      ItemData item = new ItemData();
      item.setInstruction("Matching game");
      item.setTypeId(new Long(9));
      item.setScore(new Float(10));
      item.setHasRationale(new Boolean("false"));
      item.setStatus(new Integer(1));
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
      System.out.println("****item in ItemManager ="+item);
      return item;
  }

  private HashSet prepareText(ItemData item) {
    HashSet textSet = new HashSet();
    ItemText text1 = new ItemText();
    text1.setItem(item);
    text1.setSequence(new Long(1));
    text1.setText("cat has");
    HashSet answerSet1 = new HashSet();
    HashSet answerFeedbackSet1 = new HashSet();
    Answer answer1 = new Answer(text1, "2 legs", new Long(1), "i",
                                new Boolean("false"), null, new Float(0));
    answerFeedbackSet1.add(new AnswerFeedback(answer1, "incorrect", "sorry"));
    answer1.setAnswerFeedbackSet(answerFeedbackSet1);
    answerSet1.add(answer1);
    answerSet1.add(new Answer(text1, "3 legs", new Long(2), "ii",
                              new Boolean("false"), null, new Float(0)));
    answerSet1.add(new Answer(text1, "4 legs", new Long(3), "iii",
                              new Boolean("true"), null, new Float(5)));
    text1.setAnswerSet(answerSet1);

    textSet.add(text1);

    ItemText text2 = new ItemText();
    text2.setItem(item);
    text2.setSequence(new Long(2));
    text2.setText("chicken has");
    HashSet answerSet2 = new HashSet();
    answerSet2.add(new Answer(text2, "2 legs", new Long(1), "i",
                              new Boolean("true"), null, new Float(5)));
    answerSet2.add(new Answer(text2, "3 legs", new Long(2), "ii",
                              new Boolean("false"), null, new Float(0)));
    answerSet2.add(new Answer(text2, "4 legs", new Long(3), "iii",
                              new Boolean("false"), null, new Float(0)));
    text2.setAnswerSet(answerSet2);
    textSet.add(text2);

    ItemText text3 = new ItemText();
    text3.setItem(item);
    text3.setSequence(new Long(3));
    text3.setText("baby has");
    HashSet answerSet3 = new HashSet();
    answerSet3.add(new Answer(text3, "2 legs", new Long(1), "i",
                              new Boolean("false"), null, new Float(0)));
    answerSet3.add(new Answer(text3, "3 legs", new Long(2), "ii",
                              new Boolean("false"), null, new Float(0)));
    answerSet3.add(new Answer(text3, "4 legs", new Long(3), "iii",
                              new Boolean("true"), null, new Float(5)));
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

  private void printItem(ItemData item) {
    System.out.println("**Id = " + item.getItemId());
    System.out.println("**score = " + item.getScore());
    System.out.println("**grade = " + item.getGrade());
    System.out.println("**CorrectFeedback is lazy = " +
                       item.getCorrectItemFeedback());
    System.out.println("**Objective not lazy = " +
                       item.getItemMetaDataByLabel("ITEM_OBJECTIVE"));
  }



  private void printIfcItem(ItemDataIfc item) {
    System.out.println("**Id = " + item.getItemId());
    System.out.println("**score = " + item.getScore());
    System.out.println("**grade = " + item.getGrade());
    System.out.println("**CorrectFeedback is lazy = " +
                       item.getCorrectItemFeedback());
    System.out.println("**Objective not lazy = " +
                       item.getItemMetaDataByLabel("ITEM_OBJECTIVE"));
    System.out.println("**createdDate = " +
                       item.getCreatedDate());
  }

  private void printFacadeItem(ItemDataIfc item) {
    ItemFacade f = new ItemFacade(item);
    System.out.println("****Id = " + f.getItemId());
    System.out.println("****score = " + f.getScore());
    System.out.println("****grade = " + f.getGrade());
    System.out.println("****CorrectFeedback is lazy = " +
                       f.getCorrectItemFeedback());
    System.out.println("****Objective not lazy = " +
                       f.getItemMetaDataByLabel("ITEM_OBJECTIVE"));
    System.out.println("****createdDate = " +
                       f.getCreatedDate());
    System.out.println("****ItemType = " +
                       f.getItemType().getKeyword());
  }

    /**
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
