/**********************************************************************************
 *
 * $Header: /cvs/sakai2/sam/src/org/sakaiproject/tool/assessment/business/entity/helper/item/ItemHelper12Impl.java,v 1.43 2005/06/06 22:30:04 esmiley.stanford.edu Exp $
 *
 ***********************************************************************************/
/*
 * Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 *
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 *
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.sakaiproject.tool.assessment.business.entity.helper.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.sakaiproject.tool.assessment.business.entity.asi.Item;
import org.sakaiproject.tool.assessment.business.entity.constants.AuthoringConstantStrings;
import org.sakaiproject.tool.assessment.business.entity.constants.QTIVersion;
import org.sakaiproject.tool.assessment.business.entity.helper.AuthoringXml;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;

/**
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * <p>Version for QTI 1.2 item XML, significant differences between 1.2 and 2.0</p>
 * @author Ed Smiley esmiley@stanford.edu
 * <p>Originally ItemHelper.java</p>
 * @author Shastri, Rashmi <rshastri@iupui.edu>
 * @version $Id$
 */

public class ItemHelper12Impl extends ItemHelperBase
  implements ItemHelperIfc
{
  private static Log log = LogFactory.getLog(ItemHelper12Impl.class);
  private static final String MATCH_XPATH =
    "item/presentation/flow/response_grp/render_choice";

  private Document doc;

  protected String[] itemTypes = AuthoringConstantStrings.itemTypes;
  private AuthoringXml authoringXml;
  private List allIdents;
  private Float currentMaxScore = new Float(0);
  private float currentPerItemScore = 0;

  /**
   *
   */
  public ItemHelper12Impl()
  {
    super();
    authoringXml = new AuthoringXml(getQtiVersion());
    allIdents = new ArrayList();

    System.out.println("ItemHelper12Impl");
  }

  protected AuthoringXml getAuthoringXml()
  {
    return authoringXml;
  }

  /**
   * get the qti version
   * @return
   */
  protected int getQtiVersion()
  {
    return QTIVersion.VERSION_1_2;
  }

  /**
   * Add maximum  score to item XML.
   * @param score
   * @param itemXml
   */
  public void addMaxScore(Float score, Item itemXml)
  {
    String xPath = "item/resprocessing/outcomes/decvar/@maxvalue";
    if (score == null)
    {
      score = new Float(0);
    }
    currentMaxScore = score;
    log.debug("Setting max score in: " + xPath + " to " + score);
    updateItemXml(itemXml, xPath, "" + score.toString());
  }

  /**
   * Add minimum score to item XML
   * @param score
   * @param itemXml
   */
  public void addMinScore(Float score, Item itemXml)
  {
    String xPath = "item/resprocessing/outcomes/decvar/@minvalue";
    log.debug("Setting min score in: " + xPath + " to 0");
    updateItemXml(itemXml, xPath, "0");
  }

  /**
   * Flags an answer as correct.
   * @param correctAnswerLabel the answer that is correct
   * @param itemXml the encapsulation of the item xml
   */
  public void addCorrectAnswer(String correctAnswerLabel, Item itemXml)
  {
    this.flagAnswerCorrect(correctAnswerLabel, itemXml, true);
  }

  /**
   * Flags an answer as INCORRECT.
   * Currently, only used for true false questions.
   * @param incorrectAnswerLabel the answer that is NOT correct
   * @param itemXml the encapsulation of the item xml
   */
  public void addIncorrectAnswer(String incorrectAnswerLabel, Item itemXml)
  {
    this.flagAnswerCorrect(incorrectAnswerLabel, itemXml, false);
  }

  /**
   * Flags an answer as correct/incorrect.
   * @param correctAnswerLabel the answer that is correct
   * @param itemXml the encapsulation of the item xml
   * @param correct true, or false if not correct
   */
  public void flagAnswerCorrect(String answerLabel, Item itemXml,
                                boolean correct)
  {
    if (answerLabel == null)
    {
      answerLabel = "";
    }
    String flag;
    if (correct)
    {
      flag = "Correct";

    }
    else
    {
      flag = "InCorrect";
    }

    String respProcBaseXPath = "item/resprocessing/respcondition";
    String respProcCondXPath = "/conditionvar/varequal";
    String respProcFeedbackXPath = "/displayfeedback/@linkrefid";

    int respSize = 0;

    // now get each response and flag correct answer
    List resp = itemXml.selectNodes(respProcBaseXPath);

    if ( (resp != null) && (resp.size() > 0))
    {
      respSize = resp.size();
    }

    for (int i = 1; i <= respSize; i++)
    {
      String index = ("[" + i) + "]";
      String answerVar =
        itemXml.selectSingleValue(respProcBaseXPath + index +
                                  respProcCondXPath, "element");

      if (answerLabel.equals(answerVar)) //found right displayfeedback
      {
        String xPath = respProcBaseXPath + index + "/@title";
        String xfPath = respProcBaseXPath + index + respProcFeedbackXPath;
        log.debug("answerVar correct=" + correct + ": " + answerVar);
        log.debug("setting correct=" + correct + " xPath: " + xPath);
        updateItemXml(itemXml, xPath, flag);
        log.debug("setting correct=" + correct + " displayfeedback xPath: " +
                 xfPath);
        updateItemXml(itemXml, xfPath, flag);
        break; //done
      }
    }
  }

  /**
   * Add/update a response label entry
   * @param itemXml
   * @param xpath
   * @param itemText
   * @param isInsert
   * @param responseNo
   * @param responseLabelIdent
   */
  public void addResponseEntry(
    Item itemXml, String xpath, String value,
    boolean isInsert, String responseNo, String responseLabel)
  {
    if (isInsert)
    {
      String nextNode = "response_label[" + responseNo + "]";
      itemXml.insertElement(nextNode, xpath, "response_label");
      itemXml.add(
        xpath + "/response_label[" + responseNo + "]", "material/mattext");
    }
    else
    {
      itemXml.add(xpath, "response_label/material/mattext");
    }
    try
    {
      itemXml.update(xpath + "/response_label[" + responseNo +
                     "]/material/mattext",
                     value);
    }
    catch (Exception ex)
    {
      log.error("Cannot update value in addResponselEntry(): " + ex);
    }

    String newPath = xpath + "/response_label[" + responseNo + "]";
    itemXml.addAttribute(newPath, "ident");
    newPath = xpath + "/response_label[" + responseNo + "]/@ident";
    updateItemXml(itemXml, newPath, responseLabel);
  }

  /**
   * Add Item Feedback for a given response number.
   * @param itemXml the item xml
   * @param responseNo the numnber
   */
  private void addItemfeedback(Item itemXml, String value,
                               boolean isInsert, String responseNo,
                               String responseLabel)

  {
    String xpath = "item";

    String nextNode = "itemfeedback[" + responseNo + "]";
    log.debug("Next node for feedback: " + nextNode);
    if (isInsert)
    {
      itemXml.insertElement(nextNode, xpath, "itemfeedback");
      log.debug("Adding feedback: " + xpath +
               "/itemfeedback[" + responseNo + "]" +
               "<==/itemfeedback/flow_mat/material/mattext");
      itemXml.add(
        xpath + "/itemfeedback[" + responseNo + "]",
        "flow_mat/material/mattext");
    }
    else
    {
      log.debug("Updating feedback: " + xpath +
               "<==/itemfeedback/flow_mat/material/mattext");
//      itemXml.add(xpath, "/itemfeedback/flow_mat/material/mattext");
    }
    try
    {
      if (value == null)
      {
        value = "";
      }
      itemXml.update(xpath + "/itemfeedback[" + responseNo +
                     "]/flow_mat/material/mattext",
                     value);
      log.debug("updated value in addItemfeedback():\n" +
               xpath + "/itemfeedback[" + responseNo +
               "]/flow_mat/material/mattext" +
               " gets '" + value + "'");
    }
    catch (Exception ex)
    {
      log.error("Cannot update value in addItemfeedback(): " + ex);
    }

    String newPath = xpath + "/itemfeedback[" + responseNo + "]";
    itemXml.addAttribute(newPath, "ident");
    newPath = xpath + "/itemfeedback[" + responseNo + "]/@ident";
    String feedbackIdent = responseLabel;
    updateItemXml(itemXml, newPath, feedbackIdent);

  }

  /**
   * Get the metadata field entry XPath
   * @return the XPath
   */
  public String getMetaXPath()
  {
    String xpath = "item/itemmetadata/qtimetadata";
    return xpath;
  }

  /**
   * Get the metadata field entry XPath for a given label
   * @param fieldlabel
   * @return the XPath
   */
  public String getMetaLabelXPath(String fieldlabel)
  {
    String xpath =
      "item/itemmetadata/qtimetadata/qtimetadatafield/fieldlabel[text()='" +
      fieldlabel + "']/following-sibling::fieldentry";
    return xpath;
  }

  /**
   * Get the text for the item
   * @param itemXml
   * @return the text
   */
  public String getText(Item itemXml)
  {
    String xpath = "item/presentation/flow/material/mattext";
    String itemType = itemXml.getItemType();
    if (itemType.equals(AuthoringConstantStrings.MATCHING))
    {
      xpath = "item/presentation/flow//mattext";
    }

    return makeItemNodeText(itemXml, xpath);
  }

  /**
   * Matching only, sets each source to be matched to.
   * It also sets the the matching target.
   * @param itemTextList lvalue of matches
   * @param itemXml
   */
  private void setItemTextMatching(List itemTextList, Item itemXml)
  {
    String xpath = MATCH_XPATH;
    String randomNumber = ("" + Math.random()).substring(2);
    log.debug("In setItemTextMatching().");

    log.debug("randomNumber: " + randomNumber);
    log.debug("xpath: " + xpath);

    Iterator iter = itemTextList.iterator();
    float itSize = itemTextList.size();
    // just in case we screw up
    if (itSize > 0) currentPerItemScore = currentMaxScore.floatValue()/itSize;
    int respCondCount = 0;  //used to count the respconditions


    while (iter.hasNext())
    {
      ItemTextIfc itemText = (ItemTextIfc) iter.next();
      String text = itemText.getText();
      Long sequence = itemText.getSequence();
      log.debug("item text: " + text);
      log.debug("item sequence: " + sequence);

      String responseLabelIdent = "MS-" + randomNumber + "-" + sequence;

      List answerList = itemText.getAnswerArray();
      Iterator aiter = answerList.iterator();
      int noSources = answerList.size();
      while (aiter.hasNext())
      {
        respCondCount++;
        AnswerIfc answer = (AnswerIfc) aiter.next();
        String answerText = answer.getText();
        String label = answer.getLabel();
        Long answerSequence = answer.getSequence();
        Boolean correct = answer.getIsCorrect();
        log.debug("answerText: " + answerText);
        log.debug("label: " + label);
        log.debug("answerSequence: " + answerSequence);
        log.debug("correct: " + correct);
        String responseNo = "" + sequence;
        log.debug("responseNo: " + responseNo);
        String respIdent = "MT-" + randomNumber + "-" + label;
        log.debug("respIdent: " + respIdent);

        String respCondNo = "" + respCondCount;
        if (Boolean.TRUE.equals(correct))// addMatchingResponseLabelTarget, addMatchingRespcondition
        {
          log.debug("*** We have a match.");
          log.debug(text + "==>" + answerText);
          // put in global (ewww) ident list
          allIdents.add(respIdent);
//          itemXml.add(xpath, "response_label");

          log.debug("addMatchingResponseLabelTarget(): ");
          log.debug(" respIdent: " + respIdent);
          log.debug(" responseNo: " + responseNo);
          log.debug(" value: " + answerText);
          addMatchingResponseLabelTarget(itemXml, responseNo, respIdent, answerText);

//          itemXml.add(xpath, "response_label");
          log.debug("correct response: addMatchingRespcondition(true...");
          log.debug("correct response");
          log.debug(" respCondNo: " + respCondNo);
          log.debug(" respIdent: " + respIdent);
          log.debug(" responseLabelIdent: " + responseLabelIdent);
          addMatchingRespcondition(true, itemXml, respCondNo, respIdent,
                                 responseLabelIdent);
        }
        else // addMatchingRespcondition
        {
          log.debug("incorrect response: addMatchingRespcondition(false...");
          log.debug(" respCondNo: " + respCondNo);
          log.debug(" respIdent: " + respIdent);
          log.debug(" responseLabelIdent: " + responseLabelIdent);
          log.debug(" respCondNo: " + respCondNo);
          addMatchingRespcondition(false, itemXml, respCondNo, respIdent,
                                 responseLabelIdent);
          continue; // we skip adding the response label when false
        }
      }

      itemXml.add(xpath, "response_label");
      String responseNo = "" + sequence;

      log.debug("addMatchingResponseLabelEntry(): ");
      log.debug("responseLabelIdent: " + responseLabelIdent);
      log.debug("responseNo: " + sequence);
      log.debug("value: " + text);
      addMatchingResponseLabelSource(itemXml, responseNo, responseLabelIdent, text);
    }
    log.debug("updateAllSourceMatchGroup(): " );
    updateAllSourceMatchGroup(itemXml);
  }



  //////////////////////////////////////////////////////////////////////////////
  // FILL IN THE BLANK
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Set the item text.
   * This is only valid for FIB,a single item text separated by '{}'.
   * @param itemText text to be updated, the syntax is in the form:
   * 'roses are {} and violets are {}.' -> 'roses are ',' and violets are ','.'
   * @param itemXml
   */
  private void setItemTextFIB(String fibAns, Item itemXml)
  {
    if ( (fibAns != null) && (fibAns.trim().length() > 0))
    {
      List fibList = parseFillInBlank(fibAns);
      Map valueMap = null;
      Set newSet = null;
      String mattext = null;
      String respStr = null;
      String xpath = "item/presentation/flow/flow";
      String position = null;
      String[] responses = null;
      log.debug("fibList.size(): " + fibList.size());

      if ( (fibList != null) && (fibList.size() > 0))
      {

        List idsAndResponses = new ArrayList();
        //1. add Mattext And Responses
        for (int i = 0; i < fibList.size(); i++)
        {
          log.debug("Processing " + i);

          valueMap = (Map) fibList.get(i);
          log.debug("valueMap: " + valueMap);

          if ( (valueMap != null) && (valueMap.size() > 0))
          {
            mattext = (String) valueMap.get("text");
            log.debug("mattext: " + mattext);

            if (mattext != null)
            {
              //add mattext
              itemXml.add(xpath, "material/mattext");
              String newXpath =
                xpath + "/material[" +
                (new Integer(i + 1).toString() + "]/mattext");

              updateItemXml(itemXml, newXpath, mattext);
            }

            respStr = (String) valueMap.get("ans");
            log.debug("respStr: " + respStr);

            if (respStr != null)
            {
              //add response_str
              itemXml.add(xpath, "response_str/render_fib");
              String newXpath =
                xpath + "/response_str[" +
                (new Integer(i + 1).toString() + "]");

              itemXml.addAttribute(newXpath, "ident");
              String ident = "FIB0" + i;
              updateItemXml(
                itemXml, newXpath + "/@ident", ident);

              itemXml.addAttribute(newXpath, "rcardinality");
              updateItemXml(
                itemXml, newXpath + "/@rcardinality", "Ordered");

              newXpath = newXpath + "/render_fib";
              itemXml.addAttribute(newXpath, "fibtype");
              updateItemXml(
                itemXml, newXpath + "/@fibtype", "String");

              itemXml.addAttribute(newXpath, "prompt");
              updateItemXml(
                itemXml, newXpath + "/@prompt", "Box");

              itemXml.addAttribute(newXpath, "columns");
              updateItemXml(
                itemXml, newXpath + "/@columns",
                (new Integer(respStr.length() + 5).toString()));

              itemXml.addAttribute(newXpath, "rows");
              updateItemXml(itemXml, newXpath + "/@rows", "1");

              // we throw this into our global (ugh) list of idents
              allIdents.add(ident);
              log.debug("allIdents: " + allIdents);
            }
          }
        }
      }
    }
  }

  /**
   * Special FIB processing.
   * @param itemXml
   * @param responseCondNo
   * @param respIdent
   * @param points
   * @param responses
   * @return
   */
  private Item addFIBRespconditionNotMutuallyExclusive(
    Item itemXml, String responseCondNo,
    String respIdent, String points, String[] responses)
  {
    String xpath = "item/resprocessing";
    itemXml.add(xpath, "respcondition");
    String respCond =
      "item/resprocessing/respcondition[" + responseCondNo + "]";
    itemXml.addAttribute(respCond, "continue");
    updateItemXml(itemXml, respCond + "/@continue", "Yes");

    String or = "";

    itemXml.add(respCond, "conditionvar/or");
    or = respCond + "/conditionvar/or";

    for (int i = 0; i < responses.length; i++)
    {
      itemXml.add(or, "varequal");
      int iString = i + 1;
      String varequal = or + "/varequal[" + iString + "]";
      itemXml.addAttribute(varequal, "case");
      itemXml.addAttribute(varequal, "respident");

      updateItemXml(itemXml, varequal + "/@case", "No");

      updateItemXml(
        itemXml, varequal + "/@respident", respIdent);
      updateItemXml(itemXml, varequal, responses[i]);
    }

     //Add setvar
     itemXml.add(respCond, "setvar");
     itemXml.addAttribute(respCond + "/setvar", "action");

      updateItemXml(
      itemXml, respCond + "/setvar/@action", "Add");
      itemXml.addAttribute(respCond + "/setvar", "varname");

      updateItemXml(
      itemXml, respCond + "/setvar/@varname", "SCORE");

      updateItemXml(itemXml, respCond + "/setvar", points); // this should be minimum value

    return itemXml;
  }

  /**
   * Special FIB processing.
   * @param itemXml
   * @param responseCondNo
   * @param respIdents
   * @param points
   * @param response
   * @return
   */
  private Item addFIBRespconditionMutuallyExclusive(Item itemXml,
    String responseCondNo,
    ArrayList respIdents, String points, String response)
  {
    String xpath = "item/resprocessing";
    itemXml.add(xpath, "respcondition");
    String respCond =
      "item/resprocessing/respcondition[" + responseCondNo + "]";
    itemXml.addAttribute(respCond, "continue");

    updateItemXml(itemXml, respCond + "/@continue", "Yes");

    String or = "";
    itemXml.add(respCond, "conditionvar/or");
    or = respCond + "/conditionvar/or";

    for (int i = 0; i < respIdents.size(); i++)
    {
      int iString = i + 1;

      itemXml.add(or, "varequal");
      String varequal = or + "/varequal[" + (i + 1) + "]";
      itemXml.addAttribute(varequal, "case");
      itemXml.addAttribute(varequal, "respident");

      updateItemXml(itemXml, varequal + "/@case", "No");

      updateItemXml(
        itemXml, varequal + "/@respident", (String) respIdents.get(i));
      updateItemXml(itemXml, varequal, response);
    }

     //Add setvar
     itemXml.add(respCond, "setvar");
     itemXml.addAttribute(respCond + "/setvar", "action");

      updateItemXml(
      itemXml, respCond + "/setvar/@action", "Add");
      itemXml.addAttribute(respCond + "/setvar", "varname");

      updateItemXml(
      itemXml, respCond + "/setvar/@varname", "SCORE");

      updateItemXml(itemXml, respCond + "/setvar", points); // this should be minimum value

    return itemXml;
  }

  /**
   * Special FIB processing.
   * @param itemXml
   * @param responseCondNo
   * @return
   */
  private Item addFIBRespconditionCorrectFeedback(
    Item itemXml, String responseCondNo)
  {
    String xpath = "item/resprocessing";
    itemXml.add(xpath, "respcondition");
    String respCond =
      "item/resprocessing/respcondition[" + responseCondNo + "]";
    itemXml.addAttribute(respCond, "continue");

    updateItemXml(itemXml, respCond + "/@continue", "Yes");

    String and = "";

    itemXml.add(respCond, "conditionvar/and");
    and = respCond + "/conditionvar/and";

    for (int i = 1; i < (new Integer(responseCondNo)).intValue(); i++)
    {
      List or = itemXml.selectNodes("item/resprocessing/respcondition[" + i +
                                    "]/conditionvar/or");
      if (or != null)
      {
        itemXml.addElement(and, ( (Element) or.get(0)));
      }
    }

     //Add display feedback
     itemXml.add(respCond, "displayfeedback");
    itemXml.addAttribute(respCond + "/displayfeedback", "feedbacktype");

    updateItemXml(
      itemXml, respCond + "/displayfeedback/@feedbacktype", "Response");
    itemXml.addAttribute(respCond + "/displayfeedback", "linkrefid");

    updateItemXml(
      itemXml, respCond + "/displayfeedback/@linkrefid", "Correct");
    return itemXml;
  }

  /**
   * Special FIB processing.
   * @param itemXml
   * @param responseCondNo
   * @return
   */
  private Item addFIBRespconditionInCorrectFeedback(
    Item itemXml, String responseCondNo)
  {
    String xpath = "item/resprocessing";
    itemXml.add(xpath, "respcondition");
    String respCond =
      "item/resprocessing/respcondition[" + responseCondNo + "]";
    itemXml.addAttribute(respCond, "continue");

    updateItemXml(itemXml, respCond + "/@continue", "No");

    itemXml.add(respCond, "conditionvar/other");

//			Add display feedback
    itemXml.add(respCond, "displayfeedback");
    itemXml.addAttribute(respCond + "/displayfeedback", "feedbacktype");

    updateItemXml(
      itemXml, respCond + "/displayfeedback/@feedbacktype", "Response");
    itemXml.addAttribute(respCond + "/displayfeedback", "linkrefid");

    updateItemXml(
      itemXml, respCond + "/displayfeedback/@linkrefid", "InCorrect");
    return itemXml;
  }

  /**
   *
   * @param idsAndResponses
   * @return
   */
  private ArrayList getSimilarCorrectAnswerIDs(List idsAndResponses)
  {
    String[] compareResponse = null;
    ArrayList finalArray = new ArrayList(); // this is list of maps which contains arrayList  correct responses and ids
    ArrayList idList = new ArrayList();
    ArrayList responseList = new ArrayList();
    Map intermediaryMap = new HashMap();
    for (int l = 0; l < idsAndResponses.size(); l++)
    {
      Map idsAndResponsesMap = (Map) idsAndResponses.get(l);
      Set set = idsAndResponsesMap.keySet();
      Iterator keys = set.iterator();
      while (keys.hasNext())
      {
        String respIdent = (String) keys.next();
        String[] responses = null;
        if ( (respIdent != null) && (respIdent.length() > 0))
        {
          responses = (String[]) idsAndResponsesMap.get(respIdent);
        }

        boolean newElement = true;
        for (int i = 0; i < finalArray.size(); i++)
        {
          Map currentEntry = (Map) finalArray.get(i);
          Set entrySet = currentEntry.keySet();
          Iterator entrykeys = entrySet.iterator();
          while (entrykeys.hasNext())
          {
            compareResponse = (String[]) entrykeys.next();
            if (Arrays.equals(responses, compareResponse))
            {
              idList = (ArrayList) currentEntry.get(compareResponse);
              idList.add(respIdent);
              newElement = false;
            }
          }
        }

        if ( (finalArray.size() == 0) || (newElement))
        {
          idList = new ArrayList();
          idList.add(respIdent);
          intermediaryMap = new HashMap();
          intermediaryMap.put(responses, idList);
          finalArray.add(intermediaryMap);
        }
      }
    }
    return finalArray;
  }

  /**
   * Special FIB processing.
   * @param itemXml
   * @param idsAndResponses
   * @param allIdents
   * @param isMutuallyExclusive
   * @param points
   * @return
   */
  private Item addFIBRespconditions(Item itemXml, List idsAndResponses,
                                    List allIdents,
                                    boolean isMutuallyExclusive, String points)
  {
    if (idsAndResponses.size() > 0)
    {
      ArrayList combinationResponses = getSimilarCorrectAnswerIDs(
        idsAndResponses);
      if (combinationResponses != null && combinationResponses.size() > 0)
      {
        int respConditionNo = 1;
        for (int i = 0; i < combinationResponses.size(); i++)
        {
          Map currentEntry = (Map) combinationResponses.get(i);
          Set entrySet = currentEntry.keySet();
          Iterator entrykeys = entrySet.iterator();
          while (entrykeys.hasNext())
          {
            String[] responses = (String[]) entrykeys.next();
            ArrayList idList = (ArrayList) currentEntry.get(responses);
            if (idList != null && idList.size() > 0)
            {
              if (idList.size() == 1)
              {
                addFIBRespconditionNotMutuallyExclusive(
                  itemXml, new Integer(respConditionNo).toString(),
                  (String) idList.get(0), points, responses);
                respConditionNo = respConditionNo + 1;
              }
              else
              {
                for (int k = 0; k < responses.length; k++)
                {

                  addFIBRespconditionMutuallyExclusive(itemXml,
                    new Integer(respConditionNo).toString(), idList, points,
                    responses[k]);
                  respConditionNo = respConditionNo + 1;

                }

              }
            }
          }
        }
        // add respcondition for all correct answers
        addFIBRespconditionCorrectFeedback(itemXml,
                                           new Integer(respConditionNo).
                                           toString());
        respConditionNo = respConditionNo + 1;
        //add respcondition for all incorrect answers
        addFIBRespconditionInCorrectFeedback(itemXml,
                                             new Integer(respConditionNo).
                                             toString());
      }

    }
    return itemXml;
  }

  /**
   *  Get list of form:
   *  {ans=red, text=Roses are},
   *  {ans=blue, text=and violets are},
   *  {ans=null, text=.}
   *  From String of form "Roses are {red} and violets are {blue}."
   *
   * @param input
   * @return list of Maps
   */
  private static List parseFillInBlank(String input)
  {

    Map tempMap = null;
    List storeParts = new ArrayList();
    if (input == null)
    {
      return storeParts;
    }

    StringTokenizer st = new StringTokenizer(input, "}");
    String tempToken = "";
    String[] splitArray = null;

    while (st.hasMoreTokens())
    {
      tempToken = st.nextToken();
      tempMap = new HashMap();

      //split out text and answer parts from token
      splitArray = tempToken.trim().split("\\{", 2);
      tempMap.put("text", splitArray[0].trim());
      if (splitArray.length > 1)
      {

        tempMap.put("ans", (splitArray[1]));
      }
      else
      {
        tempMap.put("ans", null);
      }

      storeParts.add(tempMap);
    }

    return storeParts;
  }

  private static String[] getPossibleCorrectResponses(String inputStr)
  {
    String patternStr = ",";
    String[] responses = inputStr.split(patternStr);
    for (int i = 0; i < responses.length; i++)
    {
      responses[i] = responses[i].trim();
    }
    Arrays.sort(responses);

    return responses;
  }

  /**
   * Set the item text.
   * This is valid for all undelimited single item texts.
   * Not valid for matching or fill in the blank, but OK for instructional text
   * @param itemText text to be updated
   * @param itemXml
   */
  public void setItemText(String itemText, Item itemXml)
  {
    String xpath = "item/presentation/flow/material/mattext";

    List list = itemXml.selectNodes(xpath);
    log.debug("xpath size:" + list.size());
    log.debug("setting element text to: " + itemText);
    try
    {
      itemXml.update(xpath, itemText);
    }
    catch (Exception ex)
    {
      log.error(ex.getMessage(), ex);
    }
  }

  /**
   * Set the (one or more) item texts.
   * Valid for single and multiple texts.
   * @param itemXml
   * @param itemText text to be updated
   */
  public void setItemTexts(ArrayList itemTextList, Item itemXml)
  {
    if (itemTextList.size() < 1)
    {
      return;
    }

    if (itemXml.isMatching())
    {
      setItemTextMatching(itemTextList, itemXml);
      return;
    }

    String text = ( (ItemTextIfc) itemTextList.get(0)).getText();
    log.debug("item text: " + text);
    if (itemXml.isFIB())
    {
      setItemTextFIB(text, itemXml);
      return;
    }
    else
    {
      setItemText(text, itemXml);
      return;
    }

  }

  /**
   * get item type string
   * @param itemXml
   * @return type as string
   */
  public String getItemType(Item itemXml)

  {
    String type = itemXml.getFieldentry("qmd_itemtype");

    return type;
  }

  /**
   * Set the answer texts for item.
   * @param itemTextList the text(s) for item
   */

  public void setAnswers(ArrayList itemTextList, Item itemXml)
  {

    // other types either have no answer or include them in their template, or,
    // in matching, generate all in setItemTextMatching()
    if (!itemXml.isFIB() && !itemXml.isMCSC() && !itemXml.isMCMC())
    {
      return;
    }

    // OK, so now we are in business.
    String xpath =
      "item/presentation/flow/response_lid/render_choice";

    List list = itemXml.selectNodes(xpath);
    log.debug("xpath size:" + list.size());
    Iterator nodeIter = list.iterator();

    Iterator iter = itemTextList.iterator();
    Set answerSet = new HashSet();

    char label = 'A';
    int xpathIndex = 1;
    int respIdentCount = 0;
    while (iter.hasNext())
    {
      answerSet = ( (ItemTextIfc) iter.next()).getAnswerSet();
      Iterator aiter = answerSet.iterator();
      while (aiter.hasNext())
      {
        AnswerIfc answer = (AnswerIfc) aiter.next();
        if (Boolean.TRUE.equals(answer.getIsCorrect()))
        {
          this.addCorrectAnswer("" + label, itemXml);
        }
        String value = answer.getText();
        log.debug("answer: " + answer.getText());
        // if and only if FIB we do special processing
        if (itemXml.isFIB())
        {
          String[] responses =
            {
            value}; // one possible for now
          String respIdent = (String) allIdents.get(respIdentCount++);
          addFIBRespconditionNotMutuallyExclusive(
            itemXml, "" + xpathIndex, respIdent, "0", responses);
          label++;
          xpathIndex++;
          continue; //
        }
        // process into XML
        // we assume that we have equal to or more than the requisite elements
        // if we have more than the existing elements we manufacture more
        // with labels 'A', 'B'....etc.
        Node node = null;
        try
        {
          boolean isInsert = true;
          if (nodeIter.hasNext())
          {
            isInsert = false;
          }
          this.addResponseEntry(
            itemXml, xpath, value, isInsert, "" + xpathIndex, "" + label);
        }
        catch (Exception ex)
        {
          log.error("Cannot process source document.", ex);
        }

        label++;
        xpathIndex++;
      }
    }
  }

  /**
   * Set the feedback texts for item.
   * @param itemTextList the text(s) for item
   * @param itemXml
   */

  public void setFeedback(ArrayList itemTextList, Item itemXml)
  {
    log.debug("item text size: " + itemTextList.size());
    log.debug("item answer type: " + itemXml.getItemType());

    // for any answers that are now in the template, create a feedback
    String xpath =
      "item/itemfeedback/flow/response_lid/render_choice";
    int xpathIndex = 1;

    List list = itemXml.selectNodes(xpath);
    log.debug("xpath size:" + list.size());
    Iterator nodeIter = list.iterator();

    Iterator iter = itemTextList.iterator();
    Set answerSet = new HashSet();

    char label = 'A';
    boolean first = true;
    while (iter.hasNext())
    {
      ItemTextIfc itemTextIfc = (ItemTextIfc) iter.next();

      if (first) // then do once
      {
        // add in Correct and InCorrect Feedback
        String correctFeedback = itemTextIfc.getItem().getCorrectItemFeedback();
        String incorrectFeedback = itemTextIfc.getItem().
          getInCorrectItemFeedback();
        log.debug("CORRECT FEEDBACK: " + correctFeedback);
        if (correctFeedback != null)
        {
          this.addItemfeedback(
            itemXml, correctFeedback, false, "1", "" + "Correct");
        }
        log.debug("INCORRECT FEEDBACK: " + incorrectFeedback);
        if (incorrectFeedback != null)
        {
          this.addItemfeedback(
            itemXml, incorrectFeedback, false, "2", "" + "InCorrect");
        }
        xpathIndex = 3;
        first = false;
      }

      // answer feedback
      answerSet = itemTextIfc.getAnswerSet();
      Iterator aiter = answerSet.iterator();
      while (aiter.hasNext())
      {
        AnswerIfc answer = (AnswerIfc) aiter.next();
        String value = answer.getGeneralAnswerFeedback();
        log.debug("answer feedback: " + answer.getText());
        Node node = null;
        try
        {
          boolean isInsert = true;
          if (nodeIter.hasNext())
          {
            isInsert = false;
          }
          addItemfeedback(
            itemXml, value, isInsert, "" + xpathIndex, "" + label);
        }
        catch (Exception ex)
        {
          log.error("Cannot process source document.", ex);
        }

        label++;
        xpathIndex++;
      }
      // finally, add in General Feedback
      String generalFeedback = itemTextIfc.getItem().getGeneralItemFeedback();
      String itemId = itemTextIfc.getItem().getItemIdString();
      if (generalFeedback != null)
      {
        addItemfeedback(
          itemXml, generalFeedback, true, "" + xpathIndex++, itemId);
      }
    }

  }

  ////////////////////////////////////////////////////////////////
  // MATCHING
  ////////////////////////////////////////////////////////////////

  /**
   * Add the matching response label entry source.
   * @param itemXml
   * @param responseNo
   * @param responseLabelIdent
   * @param value
   */
  private void addMatchingResponseLabelTarget(
    Item itemXml, String responseNo, String respIdent, String value)
  {
    String xpath = MATCH_XPATH;

    String nextNode = "response_label[" + responseNo + "]";
    itemXml.insertElement(nextNode, xpath, "response_label");
    itemXml.add(
      xpath + "/response_label[" + responseNo + "]", "material/mattext");
    try
    {
      itemXml.update(
        xpath + "/response_label[" + responseNo + "]/material/mattext",
        value);
    }
    catch (Exception ex)
    {
      log.warn("Unable to set mattext in '" + xpath + "/response_label[" +
               responseNo + "]' to '" + value + "'");
    }

    String newPath = xpath + "/response_label[" + responseNo + "]";
    itemXml.addAttribute(newPath, "ident");
    newPath = xpath + "/response_label[" + responseNo + "]/@ident";
    updateItemXml(itemXml, newPath, respIdent);
  }

  /**
   * Add the matching response label entry source.
   * @param itemXml
   * @param responseNo
   * @param responseLabelIdent
   * @param value
   */
  private void addMatchingResponseLabelSource(
    Item itemXml, String responseNo, String responseLabelIdent, String value)
  {
    String xpath = MATCH_XPATH;

    String nextNode = "response_label[" + responseNo + "]";
    itemXml.insertElement(nextNode, xpath, "response_label");
    itemXml.add(
      xpath + "/response_label[" + responseNo + "]", "material/mattext");
    try
    {
      itemXml.update(
        xpath + "/response_label[" + responseNo + "]/material/mattext",
        value);
    }
    catch (Exception ex)
    {
      log.warn("Unable to set mattext in '" + xpath + "/response_label[" +
               responseNo + "]' to '" + value + "'");
    }

    itemXml.addAttribute(
      xpath + "/response_label[" + responseNo + "]", "match_max");
    itemXml.addAttribute(
      xpath + "/response_label[" + responseNo + "]", "match_group");

    updateItemXml(
      itemXml,
      xpath + "/response_label[" + responseNo + "]" + "/@match_max", "1");

    String newPath = xpath + "/response_label[" + responseNo + "]";
    itemXml.addAttribute(newPath, "ident");
    newPath = xpath + "/response_label[" + responseNo + "]/@ident";
    updateItemXml(itemXml, newPath, responseLabelIdent);

    //add value
//    xpath = xpath + "/response_label[" + responseNo + "]";
//    itemXml.add(xpath, "material/mattext");
//    xpath = xpath + "/material[2]/matimage";

    //Add placeholder for image
//    xpath = xpath + "/response_label[" + responseNo + "]";
//    itemXml.add(xpath, "material/matimage");
//    xpath = xpath + "/material[2]/matimage";

    //Image attributes
//    log.debug(xpath);
//    itemXml.addAttribute(xpath, "uri");
//    itemXml.addAttribute(xpath, "imagetype");
//    xpath = xpath + "/@imagetype";
//    updateItemXml(itemXml, xpath, "text/html");
  }

  /**
   * Add the matching item feedback.

   *
   * @param itemXml
   * @param feedbackIdent
   * @param responseNo
   */
  private void addMatchingItemfeedback(
    Item itemXml, String feedbackIdent, String responseNo)
  {
    String xpath = "item";

    String nextNode = "itemfeedback[" + responseNo + "]";
    itemXml.insertElement(nextNode, xpath, "itemfeedback");
    itemXml.add(
      xpath + "/itemfeedback[" + responseNo + "]", "flow_mat/material/mattext");

    String newPath = xpath + "/itemfeedback[" + responseNo + "]";
    itemXml.addAttribute(newPath, "ident");
    newPath = xpath + "/itemfeedback[" + responseNo + "]/@ident";

    updateItemXml(itemXml, newPath, feedbackIdent);

    //Add placeholder for image
    xpath = xpath + "/itemfeedback[" + responseNo + "]/flow_mat";
    itemXml.add(xpath, "material/matimage");
    xpath = xpath + "/flow_mat/material[2]/matimage";

    //Image attributes
    log.debug(xpath);
    itemXml.addAttribute(xpath, "uri");
    itemXml.addAttribute(xpath, "imagetype");
    xpath = xpath + "/@imagetype";
    updateItemXml(itemXml, xpath, "text/html");
  }

  /**
   * Add matching response condition.
   * @param itemXml
   * @param responseNo
   * @param respident
   * @param responseLabelIdent
   */
  private void addMatchingRespcondition(boolean correct,
    Item itemXml, String responseNo, String respident, String responseLabelIdent)
  {
    log.debug("inside addMatchingRespcondition()");

    String xpath = "item/resprocessing";
    itemXml.add(xpath, "respcondition/conditionvar/varequal");
    log.debug("Adding: " + xpath);

    String respCond = "item/resprocessing/respcondition[" + responseNo + "]";
    log.debug("adding attributes to respCond: " + respCond);
    itemXml.addAttribute(respCond, "continue");
    updateItemXml(itemXml, respCond + "/@continue", "No");
    itemXml.addAttribute(respCond + "/conditionvar/varequal", "case");
    updateItemXml(itemXml, respCond + "/conditionvar/varequal/@case", "Yes");
    itemXml.addAttribute(respCond + "/conditionvar/varequal", "respident");
    itemXml.addAttribute(respCond + "/conditionvar/varequal", "index");
    updateItemXml(
      itemXml, respCond + "/conditionvar/varequal/@index", responseNo);

    if (respident != null)
    {

        updateItemXml(
        itemXml, respCond + "/conditionvar/varequal/@respident", respident);
    }


    updateItemXml(
    itemXml, respCond + "/conditionvar/varequal", responseLabelIdent);

    //Add setvar
    itemXml.add(respCond, "setvar");
    itemXml.addAttribute(respCond + "/setvar", "action");
    updateItemXml(itemXml, respCond + "/setvar/@action", "Add");
    itemXml.addAttribute(respCond + "/setvar", "varname");
    log.debug("setting SCORE in: " + respCond + "/setvar/@varname");
    log.debug("selection is correct: " + correct);

    updateItemXml(itemXml, respCond + "/setvar/@varname", "SCORE");
    if (correct)
    {
      updateItemXml(itemXml, respCond + "/setvar", "" + currentPerItemScore);
    }
    else
    {
      updateItemXml(itemXml, respCond + "/setvar", "0");
    }

    //Add display feedback

    itemXml.add(respCond, "displayfeedback");
    itemXml.addAttribute(respCond + "/displayfeedback", "feedbacktype");
    log.debug("adding display feedback: " +
             respCond + "/displayfeedback/@feedbacktype");
    updateItemXml(
      itemXml, respCond + "/displayfeedback/@feedbacktype", "Response");
    itemXml.addAttribute(respCond + "/displayfeedback", "linkrefid");
    log.debug("exiting addMatchingRespcondition()");
  }

  /**
   * Update match group.
   * Uses global internal list of all target idents.
   * (... response_label[not(@match_group)]/@ident)
   * DO NOT CALL before we have all the target idents ready
   * @param itemXml
   */
  private void updateAllSourceMatchGroup( Item itemXml) {
    log.debug("Inside updateAllSourceMatchGroup.");
    String matchGroupsXpath =
      "item/presentation/flow/response_grp/render_choice/response_label[(@match_group)]";

    log.debug("allIdents.size(): " + allIdents.size());
    if (allIdents.size() > 0)
    {
      log.debug("Processing allIdents.size(0)>0");
      Iterator iter = allIdents.iterator();
      String targetIdent = null;
      String match_group = null;
      while (iter.hasNext())
      {
        targetIdent = (String) iter.next();
        log.debug("targetIdent: " + targetIdent);
        if (match_group == null)
        {
          match_group = targetIdent;
        }
        else
        {
          match_group = match_group + "," + targetIdent;
        }
        log.debug("match_group: " + match_group);
      }

      if (match_group != null)
      {
        int noOfSources = (itemXml.selectNodes(matchGroupsXpath)).size();
        log.debug("noOfSources: " + noOfSources);
        for (int i = 1; i <= noOfSources; i++)
        {
          String xpath =
            "item/presentation/flow/response_grp/render_choice/response_label[" +
            i + "]/@match_group";

          log.debug("updateItemXml(itemXml, xpath, match_group): " );
          log.debug("xpath: " + xpath);
          log.debug("match_group: " + match_group);
          updateItemXml(itemXml, xpath, match_group);
        }
      }
    }
  }

}
/**********************************************************************************
 *
 * $Header: /cvs/sakai2/sam/src/org/sakaiproject/tool/assessment/business/entity/helper/item/ItemHelper12Impl.java,v 1.43 2005/06/06 22:30:04 esmiley.stanford.edu Exp $
 *
 ***********************************************************************************/