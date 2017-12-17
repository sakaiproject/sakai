/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.qti.helper.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.qti.asi.Item;
import org.sakaiproject.tool.assessment.qti.constants.AuthoringConstantStrings;
import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.tool.assessment.qti.helper.AuthoringXml;
import org.sakaiproject.tool.assessment.qti.util.XmlUtil;
import org.sakaiproject.tool.assessment.services.GradingService;

/**
 * <p>Version for QTI 1.2 item XML, significant differences between 1.2 and 2.0</p>
 * * @version $Id$
 * 
 * Many methods in Fill in Blank and Numerical Responses(FIN) are identical for now.  
 * This might change if we want to add random variable, parameterized calculation....
 * 
 */
@Slf4j
public class ItemHelper12Impl extends ItemHelperBase
  implements ItemHelperIfc
{
  private static final String MATCH_XPATH =
    "item/presentation/flow/response_grp/render_choice";
  private static final String NBSP = "&#160;";

  //private Document doc;

  protected String[] itemTypes = AuthoringConstantStrings.itemTypes;
  private AuthoringXml authoringXml;
  private List allIdents;
  private Double currentMaxScore =  Double.valueOf(0);
  private Double currentMinScore = Double.valueOf(0);
  private double currentPerItemScore = 0;
  private double currentPerItemDiscount = 0;

  /**
   *
   */
  public ItemHelper12Impl()
  {
    super();
    authoringXml = new AuthoringXml(getQtiVersion());
    allIdents = new ArrayList();

    log.debug("ItemHelper12Impl");
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
  public void addMaxScore(Double score, Item itemXml)
  {
    String xPath = "item/resprocessing/outcomes/decvar/@maxvalue";
    if (score == null)
    {
      score = Double.valueOf(0);
    }
    currentMaxScore = score;
    updateItemXml(itemXml, xPath, score.toString());
  }

  /**
   * Add minimum score to item XML
   * @param score
   * @param itemXml
   */
  public void addMinScore(Double discount, Item itemXml)
  {
	  String xPath = "item/resprocessing/outcomes/decvar/@minvalue";
	  if (discount == null)
	  {
		  discount = Double.valueOf(0);
	  }
	  currentMinScore = discount;
	  updateItemXml(itemXml, xPath, "" + discount.toString());
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
        updateItemXml(itemXml, xPath, flag);
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
// put CDATA around answers 
      log.debug("putting CDATA around : " + value);
      if (value == null)
      {
        value = "";
      }
      value =  XmlUtil.convertToSingleCDATA(value);
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

    if (isInsert)
    {
      itemXml.insertElement(nextNode, xpath, "itemfeedback");
      itemXml.add(
        xpath + "/itemfeedback[" + responseNo + "]", "flow_mat/material/mattext");


/*
      itemXml.add(
        xpath, "itemfeedback/flow_mat/material/mattext");
*/
    }
    else
    {
    }
    try
    {
      if (value == null)
      {
        value = "";
      }
      value =  XmlUtil.convertToSingleCDATA(value);
      itemXml.update(xpath + "/itemfeedback[" + responseNo +
                     "]/flow_mat/material/mattext",
                     value);
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
  private void setItemTextMatching(List<ItemTextIfc> itemTextList, Item itemXml)
  {
    String xpath = MATCH_XPATH;
    Map allTargets = new HashMap();
    itemXml.add(xpath, "response_label");
    String randomNumber = ("" + Math.random()).substring(2);
    Iterator iter = itemTextList.iterator();
    double itSize = itemTextList.size();

    // just in case we screw up
    if (itSize > 0)
    {
      currentPerItemScore = currentMaxScore.doubleValue() / itSize;
      currentPerItemDiscount = currentMinScore.doubleValue();
    }
    int respCondCount = 0; //used to count the respconditions

    while (iter.hasNext())
    {
      ItemTextIfc itemText = (ItemTextIfc) iter.next();
      String text = itemText.getText();
      Long sequence = itemText.getSequence();

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
        String responseFeedback = "";
        if (correct.booleanValue())
        {
          responseFeedback =
           answer.getAnswerFeedback(AnswerFeedbackIfc.CORRECT_FEEDBACK);
        }
        else
        {
          responseFeedback =
           answer.getAnswerFeedback(AnswerFeedbackIfc.INCORRECT_FEEDBACK);
        }

        if (responseFeedback == null) {
        	responseFeedback = "";
        }
        String responseNo = "" + (answerSequence.longValue() - noSources + 1);
        String respIdent = "MT-" + randomNumber + "-" + label;

        String respCondNo = "" + respCondCount;
        responseFeedback =  XmlUtil.convertStrforCDATA(responseFeedback);
        // add source (addMatchingRespcondition())
        if (Boolean.TRUE.equals(correct))
        {
          log.debug("Matching: matched.");
          if (!allIdents.contains(respIdent)) {
        	  allIdents.add(respIdent); // put in global (ewww) ident list
          }
       	  allTargets.put(respIdent, answerText);
          addMatchingRespcondition(true, itemXml, respCondNo, respIdent,
                             responseLabelIdent, responseFeedback);
        }
        else
        {
          log.debug("Matching: NOT matched.");
          addMatchingRespcondition(false, itemXml, respCondNo, respIdent,
                             responseLabelIdent, responseFeedback);
          continue; // we skip adding the response label when false
        }
      }

      String responseNo = "" + sequence;
      addMatchingResponseLabelSource(itemXml, responseNo, responseLabelIdent,
                                     text, 1);
    }

    // add targets (addMatchingResponseLabelTarget())
    for (int i = 0; i < allIdents.size(); i++)
    {
      String respIdent = (String) allIdents.get(i);
      String answerText = (String) allTargets.get(respIdent);
      String responseNo = "" + (i + 1);
      addMatchingResponseLabelTarget(itemXml, responseNo, respIdent, answerText);

    }
  }

  private void setItemTextMatrix(List<ItemTextIfc> itemTextList, Item itemXml)
  {
	  String xpath = MATCH_XPATH;

	  itemXml.add(xpath, "response_label");
	  String randomNumber = ("" + Math.random()).substring(2);
	  Iterator iter = itemTextList.iterator();
	  double itSize = itemTextList.size();

	  while (iter.hasNext())
	  {

		  ItemTextIfc itemText = (ItemTextIfc) iter.next();
		  String text = itemText.getText();
		  Long sequence = itemText.getSequence();

		  String responseIdent = "MT-" + randomNumber + "-" + sequence;

		  String responseNo = "" + sequence;
		  addMatchingResponseLabelTarget(itemXml, responseNo, responseIdent, text);
	  }

	  // add targets (addMatchingResponseLabelTarget())

	  if (itemTextList.size() > 0) {
		  ItemTextIfc itemText = (ItemTextIfc)itemTextList.get(0);
		  List answerList = itemText.getAnswerArray();
		  int numTexts = itemTextList.size();

		  int matchmax = itemTextList.size();
		  // this is a kludge. On input the only difference in the matrix format is that
		  // match_max > 1. It's valid to have a matrix with only one row, but in that case
		  // use match_max of 2 to force the input side to recognize this as a matrix. I
		  // hope this approach doesn't confuse any other CMS that may try to read the XML file.
		  if (matchmax < 2)
			  matchmax = 2;
		  for (int i = 0; i < answerList.size(); i++) {
			  AnswerIfc answer = (AnswerIfc)answerList.get(i);
			  String answerText = answer.getText();
			  String responseNo = "" + (answer.getSequence() + numTexts);
			  String responseIdent = "MS-" + randomNumber + "-" + responseNo;

			  addMatchingResponseLabelSource(itemXml, responseNo, responseIdent, answerText, matchmax);
		  }
	  }
	  updateAllSourceMatchGroup(itemXml);
  }
  
  /**
   * setItemTextCalculatedQuestion() adds the variables and formulas associated with 
   * the Calculated Question.  Variables and Formulas are both stored in sam_itemtext_t and
   * sam_answer_t table.  This function adds those variable and formula definitions to
   * the item/presentation/flow path
   * @param itemTextList list of all variables and formulas (stored as ItemTextIfc and AnswerIfc
   * objects)
   * @param itemXml XML document to be updated.  New data will be appended under "item/presentation/flow"
   */
  private void setItemTextCalculatedQuestion(List<ItemTextIfc> itemTextList, Item itemXml) {
      String xpath = "item/presentation/flow";
      itemXml.add(xpath, "variables");
      itemXml.add(xpath, "formulas");
      GradingService gs = new GradingService();
      String instructions = itemXml.getItemText();
      List<String> formulaNames = gs.extractFormulas(instructions);
      List<String> variableNames = gs.extractVariables(instructions);
      for (ItemTextIfc itemText : itemTextList) {
          if (variableNames.contains(itemText.getText())) {              
              this.addCalculatedQuestionVariable(itemText, itemXml, xpath + "/variables");
          }
          else if (formulaNames.contains(itemText.getText())){
              this.addCalculatedQuestionFormula(itemText, itemXml, xpath + "/formulas");
          } else {
              log.error("Calculated Question export failed, '" + itemText.getText() + "'" +
                      "was not identified as either a variable or formula, so there must be " +
                      "an error with the Calculated Question definition, " + 
                      "question id: " + itemText.getItem().getItemIdString());
          }
      }
  }

  /**
   * addCalculatedQuestionVariable() adds a new formula node with required subnodes 
   * into xpath location defined by the calling function
   * @param itemText - ItemText object, persisted in sam_itemtext_t, which contains 
   * the data needed for the node 
   * @param itemXml - XML object being created, with will be the result of the export
   * @param xpath - where in the XML object the formula should be added
   * always edit the last node in the array.
   */
  private void addCalculatedQuestionVariable(ItemTextIfc itemText, Item itemXml, String xpath) {
      itemXml.add(xpath, "variable");              
      String updatedXpath = xpath + "/variable[last()]";
      try {
          List<AnswerIfc> answers = itemText.getAnswerArray();
          
          // find the matching answer, since the answer list will have multiple answer objects
          // for each ItemTextIfc object
          for (AnswerIfc answer : answers) {
              if (answer.getIsCorrect()) {
                  String text = answer.getText();
                  String min = text.substring(0, text.indexOf("|"));
                  String max = text.substring(text.indexOf("|") + 1, text.indexOf(","));
                  String decimalPlaces = text.substring(text.indexOf(",") + 1);
                  
                  // add nodes
                  itemXml.add(updatedXpath, "name");
                  itemXml.update(updatedXpath + "/name", itemText.getText());
                  itemXml.add(updatedXpath, "min");
                  itemXml.update(updatedXpath + "/min", min);
                  itemXml.add(updatedXpath, "max");
                  itemXml.update(updatedXpath + "/max", max);
                  itemXml.add(updatedXpath, "decimalPlaces");
                  itemXml.update(updatedXpath + "/decimalPlaces", decimalPlaces);
                  break;
              }
          }
      } catch (Exception e) {
          log.error(e.getMessage(), e);
      }
  }
  
  /**
   * addCalculatedQuestionFormula() adds a new formula node with required subnodes 
   * into xpath location defined by the calling function
   * @param itemText - ItemText object, persisted in sam_itemtext_t, which contains 
   * the data needed for the node 
   * @param itemXml - XML object being created, with will be the result of the export
   * @param xpath - where in the XML object the formula should be added
   * always edit the last node in the array.
   */
  private void addCalculatedQuestionFormula(ItemTextIfc itemText, Item itemXml, String xpath) {
      itemXml.add(xpath, "formula");              
      String updatedXpath = xpath + "/formula[last()]";
      try {
          List<AnswerIfc> answers = itemText.getAnswerArray();
          
          // find the matching answer, since the answer list will have multiple answer objects
          // for each ItemTextIfc object
          for (AnswerIfc answer : answers) {
              if (answer.getIsCorrect()) {
                  String text = answer.getText();
                  String formula = text.substring(0, text.indexOf("|"));
                  String tolerance = text.substring(text.indexOf("|") + 1, text.indexOf(","));
                  String decimalPlaces = text.substring(text.indexOf(",") + 1);
                  
                  // add nodes
                  itemXml.add(updatedXpath, "name");
                  itemXml.update(updatedXpath + "/name", itemText.getText());
                  itemXml.add(updatedXpath, "formula");
                  itemXml.update(updatedXpath + "/formula", formula);
                  itemXml.add(updatedXpath, "tolerance");
                  itemXml.update(updatedXpath + "/tolerance", tolerance);
                  itemXml.add(updatedXpath, "decimalPlaces");
                  itemXml.update(updatedXpath + "/decimalPlaces", decimalPlaces);
                  break;
              }
          }
      } catch (Exception e) {
          log.error(e.getMessage(), e);
      }
  }
  
  
  //////////////////////////////////////////////////////////////////////////////
  // Extended Matching Items
  //////////////////////////////////////////////////////////////////////////////
  
	/**
	 * This set the options for Extended Matching Items
	 * 
	 * @param itemTextList
	 * @param itemXml
	 */
	private void setItemTextEMI(List<ItemTextIfc> itemTextList, Item itemXml) {
		//itemTextList should have only one itemText, but we check for 
		//in case someone change the code
		// add all options
		for(ItemTextIfc itemText: itemTextList){//should only be once
			if(ItemTextIfc.EMI_ANSWER_OPTIONS_SEQUENCE.equals(itemText.getSequence())){
				if(itemText.getText() != null && !itemText.getText().trim().isEmpty()){
					addEMIOptionText(itemText.getText(), itemXml);
				}
				for (AnswerIfc answer : itemText.getAnswerArraySorted()) {
					addEMIOption(answer.getLabel(), answer.getText(), itemXml);
				}
			}
		}
	}
	
	private void addEMIOptionText(String text, Item itemXml) {
		updateItemXml(itemXml, "item/presentation/flow[@class='Options']/material/mattext", 
				XmlUtil.convertToSingleCDATA(text));
	}

	private void addEMIOption(String ident, String text, Item itemXml) {
		Element response_label = createElement("response_label", itemXml);
		response_label.setAttribute("ident", ident);
		Element material = createElement("material", itemXml);
		response_label.appendChild(material);
		Element mattext = createElement("mattext", itemXml);
		material.appendChild(mattext);
		if(text != null){
			mattext.setTextContent(XmlUtil.convertToSingleCDATA(text));
		}
		itemXml.addElement("item/presentation/flow[@class='Options']/response_lid/render_choice", response_label);
	}
	
	private Element createElement(String name, Item itemXml){
		try {
			return itemXml.getDocument().createElement(name);
		} catch (Exception e) {
			log.error("Could not create element!", e);
			return null;
		}
	}
	
	private void setAnswersEMI(List<ItemTextIfc> itemTextList, Item itemXml){
		//set individual answers
		String ident = itemXml.getValueOf("item/presentation/flow/response_lid/@ident");
		for(ItemTextIfc itemText: itemTextList){
			//just check to make sure we get the right stuff
			if (itemText.isEmiQuestionItemText()) {
				addEMIItem(ident, itemText, itemXml);
			}
		}
	}
	
	private void addEMIItem(String ident, ItemTextIfc itemText, Item itemXml) {
		//main node resprocessing
		Element resprocessing = createElement("resprocessing", itemXml);
		itemXml.addElement("item", resprocessing);
		//outcomes with the scores and required count
		Element outcomes = createElement("outcomes", itemXml);
		resprocessing.appendChild(outcomes);
		//decvar for score
		Element decvarScore = createElement("decvar", itemXml);
		decvarScore.setAttribute("defaultval", "0");
		decvarScore.setAttribute("varname", "SCORE");
		decvarScore.setAttribute("vartype", "Double");
		outcomes.appendChild(decvarScore);
		//decvar for required count
		Element decvarRequired = createElement("decvar", itemXml);
		decvarRequired.setAttribute("defaultval", String.valueOf(itemText.getEmiCorrectOptionLabels().length()));
		decvarRequired.setAttribute("maxvalue", itemText.getRequiredOptionsCount().toString());
		decvarRequired.setAttribute("minvalue", "0");
		decvarRequired.setAttribute("varname", "requiredOptionsCount");
		decvarRequired.setAttribute("vartype", "Integer");
		outcomes.appendChild(decvarRequired);
		//decvar for score user set
		Element decvarScoreUserSet = createElement("decvar", itemXml);
		decvarScoreUserSet.setAttribute("varname", "scoreUserSet");
		decvarScoreUserSet.setAttribute("vartype", "String");
		outcomes.appendChild(decvarScoreUserSet);
		//Item Text
		Element interpretvar = createElement("interpretvar", itemXml);//Testing
		outcomes.appendChild(interpretvar);//Testing
		Element material = createElement("material", itemXml);//Testing
		interpretvar.appendChild(material);//Testing
		Element mattext = createElement("mattext", itemXml);//Testing
		mattext.setTextContent(XmlUtil.convertToSingleCDATA(itemText.getText()));//Testing
		material.appendChild(mattext);//Testing
		if(itemText.getHasAttachment()){
			setAttachments(itemText.getItemTextAttachmentSet(), itemXml, material);
		}
		
		double score = 0.0;
		double discount = 0.0;
		//respcondition for every correct option
		for(AnswerIfc answer: itemText.getAnswerArraySorted()){
			decvarScoreUserSet.setAttribute("defaultval", answer.getGrade());
			Element respcondition = createElement("respcondition", itemXml);
			respcondition.setAttribute("continue", "Yes");
			respcondition.setAttribute("title", answer.getIsCorrect()?"CORRECT":"INCORRECT");
			resprocessing.appendChild(respcondition);
			//conditionvar for correct answers
			Element conditionvar = createElement("conditionvar", itemXml);
			respcondition.appendChild(conditionvar);
			Element varequal = createElement("varequal", itemXml);
			varequal.setAttribute("case", "Yes");
			varequal.setAttribute("respident", ident);
			varequal.setTextContent(answer.getLabel());
			conditionvar.appendChild(varequal);
			//setvar for action
			Element setvar = createElement("setvar", itemXml);
			if(answer.getIsCorrect()){
				setvar.setAttribute("action", "Add");
				setvar.setTextContent(String.valueOf(getDouble(answer.getScore())));
				score += getDouble(answer.getScore());
			}else{
				setvar.setAttribute("action", "Subtract");
				setvar.setTextContent(String.valueOf(Math.abs(getDouble(answer.getDiscount()))));
				discount += getDouble(answer.getDiscount());
			}
			setvar.setAttribute("varname", "SCORE");
			respcondition.appendChild(setvar);
		}
		
		//set the scores
		decvarScore.setAttribute("maxvalue", String.valueOf(getDouble(score)));
		decvarScore.setAttribute("minvalue", "0");//String.valueOf(getDouble(discount)));
	}
	
	private double getDouble(Double d){
		return d==null?0.0:d.doubleValue();
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
      //Set newSet = null;
      String mattext = null;
      String respStr = null;
      String xpath = "item/presentation/flow/flow";
      //String position = null;
      //String[] responses = null;

      if ( (fibList != null) && (fibList.size() > 0))
      {

        //List idsAndResponses = new ArrayList();
        //1. add Mattext And Responses
        for (int i = 0; i < fibList.size(); i++)
        {

          valueMap = (Map) fibList.get(i);

          if ( (valueMap != null) && (valueMap.size() > 0))
          {
            mattext = (String) valueMap.get("text");
//wrap mattext with cdata
            mattext = XmlUtil.convertStrforCDATA(mattext);
  
            if (mattext != null)
            {
              //add mattext
              itemXml.add(xpath, "material/mattext");
              String newXpath =
                xpath + "/material[" +
                (Integer.toString(i + 1) + "]/mattext");

              updateItemXml(itemXml, newXpath, mattext);
            }

            respStr = (String) valueMap.get("ans");

            if (respStr != null)
            {
              //add response_str
              itemXml.add(xpath, "response_str/render_fib");
              String newXpath =
                xpath + "/response_str[" +
                ( Integer.toString(i + 1) + "]");

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
                (  Integer.toString(respStr.length() + 5)));

              itemXml.addAttribute(newXpath, "rows");
              updateItemXml(itemXml, newXpath + "/@rows", "1");

              // we throw this into our global (ugh) list of idents
              allIdents.add(ident);
            }
          }
        }
      }
    }
  }

  /**
   * we ensure that answer text between brackets is always nonempty, also that
   * starting text is nonempty, we use a non-breaking space for this purpose
   * @param fib
   * @return
   */
  private static String padFibWithNonbreakSpacesText(String fib)
  {

    if (fib.startsWith("{"))
    {
      fib = NBSP + fib;
    }
    return fib.replaceAll("\\}\\{", "}" + NBSP + "{");
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
      // need to wrap CDATA for responses[i]  .  
      String wrapcdata_response = XmlUtil.convertStrforCDATA(responses[i]);
      updateItemXml(itemXml, varequal, wrapcdata_response);
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
  
  /*
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

*/
  /**
   * Special FIB processing.
   * @param itemXml
   * @param responseCondNo
   * @return
   */
  /*
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
*/
  /**
   * Special FIB processing.
   * @param itemXml
   * @param responseCondNo
   * @return
   */
  
  /*
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
  */

//////////////////////////////////////////////////////////////////////////////
  // Numeric Response
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Set the item text.
   * This is only valid for FIN,a single item text separated by '{}'.
   * @param itemText text to be updated, the syntax is in the form:
   * 'roses are {} and violets are {}.' -> 'roses are ',' and violets are ','.'
   * @param itemXml
   */
  private void setItemTextFIN(String finAns, Item itemXml)
  {
    if ( (finAns != null) && (finAns.trim().length() > 0))
    {
      List finList = parseFillInNumeric(finAns);
      Map valueMap = null;
      Set newSet = null;
      String mattext = null;
      String respStr = null;
      String xpath = "item/presentation/flow/flow";
      String position = null;
      String[] responses = null;

      if ( (finList != null) && (finList.size() > 0))
      {


        //1. add Mattext And Responses
        for (int i = 0; i < finList.size(); i++)
        {

          valueMap = (Map) finList.get(i);

          if ( (valueMap != null) && (valueMap.size() > 0))
          {
            mattext = (String) valueMap.get("text");
//          wrap mattext with cdata
            mattext = XmlUtil.convertStrforCDATA(mattext);

            if (mattext != null)
            {
              //add mattext
              itemXml.add(xpath, "material/mattext");
              String newXpath =
                xpath + "/material[" +
                ( Integer.toString(i + 1) + "]/mattext");

              updateItemXml(itemXml, newXpath, mattext);
            }

            respStr = (String) valueMap.get("ans");

            if (respStr != null)
            {
              //add response_str
              itemXml.add(xpath, "response_str/render_fin");
              String newXpath =
                xpath + "/response_str[" +
                ( Integer.toString(i + 1) + "]");

              itemXml.addAttribute(newXpath, "ident");
              String ident = "FIN0" + i;
              updateItemXml(
                itemXml, newXpath + "/@ident", ident);

              itemXml.addAttribute(newXpath, "rcardinality");
              updateItemXml(
                itemXml, newXpath + "/@rcardinality", "Ordered");

              newXpath = newXpath + "/render_fin";
              itemXml.addAttribute(newXpath, "fintype");
              updateItemXml(
                itemXml, newXpath + "/@fintype", "String");

              itemXml.addAttribute(newXpath, "prompt");
              updateItemXml(
                itemXml, newXpath + "/@prompt", "Box");

              itemXml.addAttribute(newXpath, "columns");
              updateItemXml(
                itemXml, newXpath + "/@columns",
                (  Integer.toString(respStr.length() + 5)));

              itemXml.addAttribute(newXpath, "rows");
              updateItemXml(itemXml, newXpath + "/@rows", "1");

              // we throw this into our global (ugh) list of idents
              allIdents.add(ident);
            }
          }
        }
      }
    }
  }

  /**
   * we ensure that answer text between brackets is always nonempty, also that
   * starting text is nonempty, we use a non-breaking space for this purpose
   * @param fin
   * @return
   */
  private static String padFinWithNonbreakSpacesText(String fin)
  {

    if (fin.startsWith("{"))
    {
      fin = NBSP + fin;
    }
    return fin.replaceAll("\\}\\{", "}" + NBSP + "{");
  }

  /**
   * Special FIN processing.
   * @param itemXml
   * @param responseCondNo
   * @param respIdent
   * @param points
   * @param responses
   * @return
   */
  private Item addFINRespconditionNotMutuallyExclusive(
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
      // need to wrap CDATA for responses[i]  .  
      String wrapcdata_response = XmlUtil.convertStrforCDATA(responses[i]);

      updateItemXml(itemXml, varequal, wrapcdata_response);
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
   * Special FIN processing.
   * @param itemXml
   * @param responseCondNo
   * @param respIdents
   * @param points
   * @param response
   * @return
   */
  /*
  private Item addFINRespconditionMutuallyExclusive(Item itemXml,
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
*/
  /**
   * Special FIN processing.
   * @param itemXml
   * @param responseCondNo
   * @return
   */
  /*
  private Item addFINRespconditionCorrectFeedback(
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
*/
  /**
   * Special FIN processing.
   * @param itemXml
   * @param responseCondNo
   * @return
   */
  /*
  private Item addFINRespconditionInCorrectFeedback(
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
  
  
  */
  
  
  
  
  
  /**
   *
   * @param idsAndResponses
   * @return
   */
  
  /*
  private ArrayList getSimilarCorrectAnswerIDs(List idsAndResponses)
  {
    String[] compareResponse = null;
    ArrayList finalArray = new ArrayList(); // this is list of maps which contains arrayList  correct responses and ids
    ArrayList idList = new ArrayList();
    //ArrayList responseList = new ArrayList();
    Map intermediaryMap = new HashMap();
    for (int l = 0; l < idsAndResponses.size(); l++)
    {
      Map idsAndResponsesMap = (Map) idsAndResponses.get(l);
      
      for (Iterator it = idsAndResponsesMap.entrySet().iterator(); it.hasNext();) {
    	  Map.Entry entry = (Map.Entry) it.next();
    	  String respIdent = (String)entry.getKey();
  	      String[] responses = null ;
    	  if ( (respIdent != null) && (respIdent.length() > 0))
    	  {
    		  responses = (String[])entry.getValue();
    	  }

    	  boolean newElement = true;
    	  for (int i = 0; i < finalArray.size(); i++)
    	  {
    		  Map currentEntry = (Map) finalArray.get(i);
    		  
    		  for (Iterator it2 = currentEntry.entrySet().iterator(); it2.hasNext();) {
            	  Map.Entry entry2 = (Map.Entry) it2.next();
            	  compareResponse = (String[]) entry2.getKey();
            	  
            	  
    		  //Set entrySet = currentEntry.keySet();
    		  //Iterator entrykeys = entrySet.iterator();
    		  //while (entrykeys.hasNext())
    		  //{
    			  //compareResponse = (String[]) entrykeys.next();
    			  if (Arrays.equals(responses, compareResponse))
    			  {
    				  idList = (ArrayList) entry2.getValue(); 
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
*/
  /**
   * Special FIB processing.
   * @param itemXml
   * @param idsAndResponses
   * @param allIdents
   * @param isMutuallyExclusive
   * @param points
   * @return
   */
  
  /*
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
                    
          for (Iterator it = currentEntry.entrySet().iterator(); it.hasNext();) {
        	  Map.Entry entry = (Map.Entry) it.next();
        	  String[] responses = (String[]) entry.getKey();
        	  ArrayList idList = (ArrayList) entry.getValue();
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
  
  */
  /**
   * Special FIN processing.
   * @param itemXml
   * @param idsAndResponses
   * @param allIdents
   * @param isMutuallyExclusive
   * @param points
   * @return
   */
/*
  
  private Item addFINRespconditions(Item itemXml, List idsAndResponses,
			List allIdents, boolean isMutuallyExclusive, String points) {
		if (idsAndResponses.size() > 0) {
			ArrayList combinationResponses = getSimilarCorrectAnswerIDs(idsAndResponses);
			if (combinationResponses != null && combinationResponses.size() > 0) {
				int respConditionNo = 1;
				for (int i = 0; i < combinationResponses.size(); i++) {
					Map currentEntry = (Map) combinationResponses.get(i);

					for (Iterator it = currentEntry.entrySet().iterator(); it.hasNext();) {
						Map.Entry entry = (Map.Entry) it.next();
						String[] responses = (String[])entry.getKey();
						ArrayList idList = (ArrayList) entry.getValue();

						if (idList != null && idList.size() > 0) {
							if (idList.size() == 1) {
								addFINRespconditionNotMutuallyExclusive(
										itemXml, new Integer(respConditionNo)
										.toString(), (String) idList
										.get(0), points, responses);
								respConditionNo = respConditionNo + 1;
							} else {
								for (int k = 0; k < responses.length; k++) {

									addFINRespconditionMutuallyExclusive(
											itemXml,
											new Integer(respConditionNo)
											.toString(), idList,
											points, responses[k]);
									respConditionNo = respConditionNo + 1;

								}

							}
						}
					}
				}
				// add respcondition for all correct answers
				addFINRespconditionCorrectFeedback(itemXml, new Integer(
						respConditionNo).toString());
				respConditionNo = respConditionNo + 1;
				// add respcondition for all incorrect answers
				addFINRespconditionInCorrectFeedback(itemXml, new Integer(
						respConditionNo).toString());
			}

		}
		return itemXml;
	}
  */
  
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
    input = padFibWithNonbreakSpacesText(input);

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

  
  private static List parseFillInNumeric(String input)
  {
    input = padFinWithNonbreakSpacesText(input);

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

	public void setItemLabel(String itemLabel, Item itemXml) {
		try {
			itemXml.update("item/@label", itemLabel);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void setPresentationLabel(String presentationLabel, Item itemXml) {
		try {
			itemXml.update("item/presentation/@label", presentationLabel);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void setPresentationFlowResponseIdent(String presentationFlowResponseIdent, Item itemXml) {
		try {
			itemXml.update("item/presentation/flow/response_lid/@ident", presentationFlowResponseIdent);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
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
    setItemText(itemText, null, itemXml);
  }
  
  public void setItemText(String itemText, String flowClass, Item itemXml){
	  String xpath = "item/presentation/flow" +
	  		(flowClass==null?"":"[@class='" + flowClass + "']")
	  		+ "/material/mattext";

	    log.debug("in ItemHelper12Impl.java: setItemText() text = " + itemText);
	    itemText = XmlUtil.convertToSingleCDATA(itemText);
	    log.debug("in ItemHelperBase.java: setItemText() wrapped CDATA text is = " + itemText);

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
  public void setItemTexts(List<ItemTextIfc> itemTextList, Item itemXml)
  {
    if (itemTextList.size() < 1)
    {
      return;
    }

    if (itemXml.isMatching())
    {
      setItemTextMatching(itemTextList, itemXml);
    }
    else if (itemXml.isMXSURVEY()) {
	        setItemTextMatrix(itemTextList, itemXml);
    }
    else if (itemXml.isEMI())
    {
    	setItemTextEMI(itemTextList, itemXml);
    }
    else if (itemXml.isCalculatedQuestion()) {
        setItemTextCalculatedQuestion(itemTextList, itemXml);
        return;
    }
    else if (itemXml.isFIB())
    {
      setItemTextFIB(itemTextList.get(0).getText(), itemXml);
    }
    else if (itemXml.isFIN())
    {
        setItemTextFIN(itemTextList.get(0).getText(), itemXml);
    }
    else
    {
      setItemText(itemTextList.get(0).getText(), itemXml);
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

  public void setAnswers(List<ItemTextIfc> itemTextList, Item itemXml)
  {

    log.debug("entered setAnswers()");
    log.debug("size=" + itemTextList.size());
    if(itemXml.isEMI()){
    	setAnswersEMI(itemTextList, itemXml);
    	return;
    }
    // other types either have no answer or include them in their template, or,
    // in matching, generate all in setItemTextMatching()
    if (!itemXml.isFIB() && !itemXml.isMCSC() && !itemXml.isFIN()
        && !itemXml.isMCMC() && !itemXml.isMCMCSS() && !itemXml.isEssay() && !itemXml.isSurvey())
    {
      return;
    }

    // OK, so now we are in business.
    String xpath =
      "item/presentation/flow/response_lid/render_choice";

    List list = itemXml.selectNodes(xpath);
    Iterator nodeIter = list.iterator();

    Iterator iter = itemTextList.iterator();
    Set answerSet = new HashSet();

    char label = 'A';
    int xpathIndex = 1;
    int respIdentCount = 0;
    while (iter.hasNext())
    {
      answerSet = ( (ItemTextIfc) iter.next()).getAnswerSet();
      log.debug("answersize=" + answerSet.size());
      Iterator aiter = answerSet.iterator();
      while (aiter.hasNext())
      {
        AnswerIfc answer = (AnswerIfc) aiter.next();
        if (Boolean.TRUE.equals(answer.getIsCorrect()))
        {
          this.addCorrectAnswer("" + label, itemXml);
        }
        String value = answer.getText();
        log.debug("\n\n***The answer is: " + value);
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
        
        if (itemXml.isFIN())
        {
          String[] responses =
            {
            value}; // one possible for now
          String respIdent = (String) allIdents.get(respIdentCount++);
          addFINRespconditionNotMutuallyExclusive(
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

  public void setFeedback(List<ItemTextIfc> itemTextList, Item itemXml)
  {

    boolean hasAnswerLevelFeedback = itemXml.isMCMC() || itemXml.isMCSC()|| itemXml.isMCMCSS();

    // for any answers that are now in the template, create a feedback
    String xpath =
      "item/itemfeedback/flow/response_lid/render_choice";
    int xpathIndex = 1;

    List list = itemXml.selectNodes(xpath);
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
        addCorrectAndIncorrectFeedback(itemXml, itemTextIfc);
        xpathIndex = 1;
        first = false;
      }

      if (hasAnswerLevelFeedback)
      {
        log.debug("Setting answer level feedback");

        answerSet = itemTextIfc.getAnswerSet();
        log.debug("answerSet.size(): " + answerSet.size());

        Iterator aiter = answerSet.iterator();
        while (aiter.hasNext())
        {
          AnswerIfc answer = (AnswerIfc) aiter.next();

          String value = answer.getGeneralAnswerFeedback();
          boolean isInsert = true;
          if (nodeIter.hasNext())
          {
            isInsert = false;
          }

          if(itemXml.isMCSC()){
        	  //MC Single Correct 
        	  if(answer.getIsCorrect().booleanValue()){
        		  answer.setPartialCredit(100d);
        	  }
        	  
        	  if (answer.getItem().getPartialCreditFlag()) {
        		  Double partialCredit = 100d;
        		  try {
        			  partialCredit = Double.valueOf(((answer.getItem().getScore().doubleValue())*answer.getPartialCredit().doubleValue())/100d);
        		  }
        		  catch (Exception e) {
        			  log.error("Could not compute partial value for id: " + answer.getId());
        		  }
        		  addAnswerFeedbackPartialCredit(itemXml, value,
        				  isInsert, xpathIndex, "" + label, partialCredit); //--mustansar
        	  }
        	  else {
        		  addAnswerFeedback(itemXml, value,
            			  isInsert, xpathIndex, "" + label  );
        	  }
          }
          else 
          { // for MC Mulitiple Correct
        	  addAnswerFeedback(itemXml, value,
        			  isInsert, xpathIndex, "" + label  ); 
          }
          label++;
          xpathIndex++;
        }
      }

      addGeneralFeedback(itemXml, xpathIndex, itemTextIfc);
    }

  }

  /**
   * Adds feedback with idents of Correct and InCorrect
   * @param itemXml
   * @param itemTextIfc
   */

  private void addCorrectAndIncorrectFeedback(Item itemXml,
                                              ItemTextIfc itemTextIfc)
  {
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
  }

  /**
   * Adds feedback with ident referencing item ident
   * @param itemXml
   * @param xpathIndex
   * @param itemTextIfc
   */
  private void addGeneralFeedback(Item itemXml, int xpathIndex,
                                  ItemTextIfc itemTextIfc)
  {
    log.debug("\nDebug add in General Feedback");
    String generalFeedback = itemTextIfc.getItem().getGeneralItemFeedback();
    String itemId = itemTextIfc.getItem().getItemIdString();
    if (generalFeedback != null)
    {
      addItemfeedback(
        itemXml, generalFeedback, true, "" + xpathIndex++, itemId);
    }
  }

  /**
   * Adds feedback with ident referencing answer ident.
   *
   * @param itemXml
   * @param value
   * @param isInsert
   * @param responseNo
   * @param responseLabel
   */
  private void addAnswerFeedback(Item itemXml, String value,
                               boolean isInsert, int responseNo,
                               String responseLabel)
  {
    log.debug("addAnswerFeedback()");
    log.debug("answer feedback value: " + value);
    if (value == null) {
    	value = "<![CDATA[]]>";
    }
    else {
    	value = XmlUtil.convertStrforCDATA(value);
    }
    String respCond = "item/resprocessing/respcondition[" + responseNo + "]";
    updateItemXml(itemXml, respCond + "/setvar", "" + currentPerItemScore);
    updateItemXml(itemXml,
        respCond + "/displayfeedback[2]/@linkrefid", "AnswerFeedback");
    updateItemXml(itemXml, respCond + "/displayfeedback[2]", value);
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
    insertResponseLabelMattext(itemXml, responseNo, value, xpath);

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
    Item itemXml, String responseNo, String responseLabelIdent, String value, int matchMax)
  {
    String xpath = MATCH_XPATH;

    insertResponseLabelMattext(itemXml, responseNo, value, xpath);

    itemXml.addAttribute(
      xpath + "/response_label[" + responseNo + "]", "match_max");
    itemXml.addAttribute(
      xpath + "/response_label[" + responseNo + "]", "match_group");

    updateItemXml(
      itemXml,
      xpath + "/response_label[" + responseNo + "]" + "/@match_max", Integer.toString(matchMax));

    String newPath = xpath + "/response_label[" + responseNo + "]";
    itemXml.addAttribute(newPath, "ident");
    newPath = xpath + "/response_label[" + responseNo + "]/@ident";
    updateItemXml(itemXml, newPath, responseLabelIdent);
  }

  /**
   * utility method for addMatchingResponseLabelTarget(), addMatchingResponseLabelSource()
   * @param itemXml
   * @param responseNo
   * @param value
   * @param xpath
   */
  private void insertResponseLabelMattext(Item itemXml, String responseNo,
                                          String value, String xpath)
  {
    String nextNode = "response_label[" + responseNo + "]";
    itemXml.insertElement(nextNode, xpath, "response_label");
    itemXml.add(
      xpath + "/response_label[" + responseNo + "]", "material/mattext");
    try
    {
    	
    	log.debug("in ItemHelper12Impl.java: insertResponseLabelMattext() text = " + value);
    	value =  XmlUtil.convertStrforCDATA(value);
    	log.debug("in ItemHelperBase.java: insertResponseLabelMattext() wrapped CDATA text is = " + value);
    	
      itemXml.update(
        xpath + "/response_label[" + responseNo + "]/material/mattext",
        value);
    }
    catch (Exception ex)
    {
      log.warn("Unable to set mattext in '" + xpath + "/response_label[" +
               responseNo + "]' to '" + value + "'");
    }
  }
  
  /**
   * Add matching response condition.
   * @param itemXml
   * @param responseNo
   * @param respident
   * @param responseLabelIdent
   */
  private void addMatchingRespcondition(boolean correct,
                                        Item itemXml, String responseNo,
                                        String respident,
                                        String responseLabelIdent,
                                        String responseFeedback)
  {

    String xpath = "item/resprocessing";
    itemXml.add(xpath, "respcondition/conditionvar/varequal");

    String respCond = "item/resprocessing/respcondition[" + responseNo + "]";
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

    updateItemXml(itemXml, respCond + "/setvar/@varname", "SCORE");

    //Add display feedback

    itemXml.add(respCond, "displayfeedback");
    itemXml.addAttribute(respCond + "/displayfeedback", "feedbacktype");
    updateItemXml(
      itemXml, respCond + "/displayfeedback/@feedbacktype", "Response");
    itemXml.addAttribute(respCond + "/displayfeedback", "linkrefid");

    if (correct)
    {
      updateItemXml(itemXml, respCond + "/setvar", "" + currentPerItemScore);
      updateItemXml(itemXml,
        respCond + "/displayfeedback/@linkrefid", "CorrectMatch");
    }
    else
    {
      updateItemXml(itemXml, respCond + "/setvar", "0");
      updateItemXml(itemXml,
        respCond + "/displayfeedback/@linkrefid", "InCorrectMatch");
    }

    updateItemXml(itemXml, respCond + "/displayfeedback", responseFeedback);
  }

  /**
   * Update match group.
   * Uses global internal list of all target idents.
   * (... response_label[not(@match_group)]/@ident)
   * DO NOT CALL before we have all the target idents ready
   * @param itemXml
   */
  private void updateAllSourceMatchGroup(Item itemXml)
  {
    String matchGroupsXpath =
      "item/presentation/flow/response_grp/render_choice/response_label[(@match_group)]";

    if (allIdents.size() > 0)
    {
      Iterator iter = allIdents.iterator();
      String targetIdent = null;
      String match_group = null;
      while (iter.hasNext())
      {
        targetIdent = (String) iter.next();
        if (match_group == null)
        {
          match_group = targetIdent;
        }
        else
        {
          match_group = match_group + "," + targetIdent;
        }
      }

      if (match_group != null)
      {
        int noOfSources = (itemXml.selectNodes(matchGroupsXpath)).size();
        for (int i = 1; i <= noOfSources; i++)
        {
          String xpath =
            "item/presentation/flow/response_grp/render_choice/response_label[" +
            i + "]/@match_group";

          updateItemXml(itemXml, xpath, match_group);
        }
      }
    }
  }

  /**
   * Adds feedback with ident referencing answer ident.
   *
   * @param itemXml
   * @param value
   * @param isInsert
   * @param responseNo
   * @param responseLabel
   */
  private void addAnswerFeedbackPartialCredit(Item itemXml, String value,
		  boolean isInsert, int responseNo,
		  String responseLabel, Double partialCredit)
  {
	  log.debug("addAnswerFeedback()");
	  log.debug("answer feedback value: " + value);
	  if (value == null) {
		  value = "<![CDATA[]]>";
	  }
	  else {
		  value = XmlUtil.convertStrforCDATA(value);
	  }
	  String respCond = "item/resprocessing/respcondition[" + responseNo + "]";

	  //  updateItemXml(itemXml, respCond + "/setvar", "" + currentPerItemScore);
	  updateItemXml(itemXml, respCond + "/setvar", "" + partialCredit);
	  updateItemXml(itemXml,
			  respCond + "/displayfeedback[2]/@linkrefid", "AnswerFeedback");
	  updateItemXml(itemXml, respCond + "/displayfeedback[2]", value);
  }
  
  public void setAttachments(Set<? extends AttachmentIfc> attachmentSet, Item itemXml){
	  if(attachmentSet == null || attachmentSet.isEmpty()){
		  return;
	  }
	  List<Element> nodeList = itemXml.selectNodes("//item/presentation/flow[position()=1]/material");
	  Element material = nodeList.get(0);
	  setAttachments(attachmentSet, itemXml, material);
  }
  
  private void setAttachments(Set<? extends AttachmentIfc> attachmentSet, Item itemXml, Element material){
	  for(AttachmentIfc attach: attachmentSet){
			Element mat = null;
			if(attach.getMimeType().startsWith("text")){
				mat = createElement("mattext", itemXml);
				mat.setAttribute("texttype", attach.getMimeType());
			}else if(attach.getMimeType().startsWith("image")){
				mat = createElement("matimage", itemXml);
				mat.setAttribute("imagtype", attach.getMimeType());
			}else if(attach.getMimeType().startsWith("audio")){
				mat = createElement("mataudio", itemXml);
				mat.setAttribute("audiotype", attach.getMimeType());
			}else if(attach.getMimeType().startsWith("video")){
				mat = createElement("matvideo", itemXml);
				mat.setAttribute("videotype", attach.getMimeType());
			}else if(attach.getMimeType().startsWith("application")){
				mat = createElement("matapplication", itemXml);
				mat.setAttribute("apptype", attach.getMimeType());
			}else{
				throw new IllegalArgumentException("Don't know this Mime-type: " + attach.getMimeType());
			}
			mat.setAttribute("label", attach.getFilename());
			mat.setAttribute("size", String.valueOf(attach.getFileSize()));
			mat.setAttribute("uri", attach.getLocation());
			material.appendChild(mat);
		}
  }
}
