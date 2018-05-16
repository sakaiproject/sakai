/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import org.apache.commons.math3.util.Precision;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.assessment.facade.TypeFacade;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService; 
import org.sakaiproject.tool.assessment.data.dao.assessment.FavoriteColChoices;
import org.sakaiproject.tool.assessment.data.dao.assessment.FavoriteColChoicesItem;
import org.sakaiproject.util.ResourceLoader;


/**
 * UI bean for authoring an Item
 * $Id$
 */

public class ItemBean
  implements Serializable
{

  // internal use
  private static final String answerNumbers =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static boolean partialCreditEnabledChecked = false;
  private static boolean partialCreditEnabled = false;
  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 8266438770394956874L;

  // for item editing
  private String itemText;
  private String itemId;
  private String itemType;
  private double itemScore= 0.0d;
  private String itemScoreDisplayFlag= "true";
  private Double itemMinScore;
  private double itemDiscount = 0.0d;
  private String partialCreditFlag = "Defualt";
  private String[] answers;
  private String[] answerLabels;  //  such as A, B, C
  private String[] corrAnswers;  // store checkbox values(labels) for multiple correct answers, as in mcmc type
  private String corrAnswer;  // store text value for single correct answer, as in true/false , mcsc, also used for essay's model answer
  private ArrayList multipleChoiceAnswers;  // store List of answers multiple choice items, ArrayList of AnswerBean
  private String additionalChoices = "0";  // additonal multiple choice answers to be add. for the select menu
  private List<AnswerBean> emiAnswerOptions;  // ArrayList of AnswerBean - store List of possible options for an EMI question's anwers
  private String additionalEmiAnswerOptions = "3";  //Additonal options for an EMI question's answers - Jul 2010 forced to 3, no longer selected from a list
  private String leadInStatement;
  private List<AnswerBean> emiQuestionAnswerCombinations;  //store List of possible options for an EMI question's anwers
  private String additionalEmiQuestionAnswerCombinations = "3";  // additonal options for an EMI question's answers  - Jul 2010 forced to 3, no longer selected from a list
  private String emiVisibleItems = "0"; //The number of visible EMI items
  private String emiAnswerOptionsRich;
  private String emiAnswerOptionsPaste;
  private String answerOptionsRichCount = "0";
  private String answerOptionsSimpleOrRich = ItemDataIfc.ANSWER_OPTIONS_SIMPLE.toString();

  private List<ItemTagBean> itemTags;
  
  public static final int DEFAULT_MAX_NUMBER_EMI_OPTIONS_FOR_UI = 26; 
  public static final int DEFAULT_MAX_NUMBER_EMI_ITEMS_FOR_UI = 60; //Twice the actual number to allow for javascript add/delete 
  
  private int totalMCAsnwers;
  private CalculatedQuestionBean calculatedQuestion;
  
  private String requireAllOk = "false";
  private String imageMapSrc="";
  
  private boolean[] choiceCorrectArray;
  private String maxRecordingTime;
  private String maxNumberRecordings;
  private String scaleName;
  private boolean multipleCorrect = false ;
  private String multipleCorrectString;
  private String randomized = "false";
  private String rationale = "false";
  private String mcmsPartialCredit;

// for matching and calculated questions only
  private String instruction;  // matching's question text
  private ArrayList matchItemBeanList;  // store List of MatchItemBean, used for Matching only
  private MatchItemBean currentMatchPair;  // do not need this ?   store List of MatchItemBeans, used for Matching only
  
  private ArrayList imageMapItemBeanList;

// begin DELETEME
  private String[] matches;
  private String[] matchAnswers;
  private String[] matchFeedbackList;

// end DELETEME

  private String corrFeedback= "";
  private String incorrFeedback= "";
  private String generalFeedback="";

  private String objective;
  private String keyword;
  private String rubric;
  private String timeAllowed;
  private String numAttempts;
 
   
  private String outcome;
  private String poolOutcome;

  private String selectedPool;  // pool id for the item to be added to
  private String origPool;  // pool id for the item to be added to
  private String origSection;  // section id for the item to be added to
  private String selectedSection="0";  // section id for the item to be assigned to


  private boolean caseSensitiveForFib=false;
  private boolean mutuallyExclusiveForFib=false;
  private boolean ignoreSpacesForFib=false;
  private boolean caseSensitiveForFin=false;
  private boolean mutuallyExclusiveForFin=false;
  //not used now. This is used to deteremine whether 
  //we show the checkbox for mutually exclusive, 
  //depending on the answers entered in the wysiwyg editor.   
  private boolean showMutuallyExclusiveForFibCheckbox=false;  
  private boolean showMutuallyExclusiveForFinCheckbox=false;
  
  //for matrix choices survey sam-939
  private String rowChoices;
  private String columnChoices;
  private boolean newFavoriteChoice = false;
  private boolean addToFavorite=false;
  private boolean addComment=false;
  private boolean forceRanking=false;
  private int relativeWidth=0;
  private boolean newAddToFavorite = false;
  private String favoriteName="30 character limit";
  private boolean fromFavoriteSelectOneMenu = false;
  private String commentField=RB_AUTHOR_MESSAGES.getString("character_limit");
  private String currentFavorite="1";
  private boolean hasFavoriteList=false;
  private ArrayList currentFavoriteList;
  
  private static final ResourceLoader RB_AUTHOR_MESSAGES = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AuthorMessages");  
  
  /**
   * Creates a new ItemBean object.
   */
  public ItemBean()
  {

  }

  /**
   * @return
   */

 public String getOutcome()
    {
	return outcome;
    }
 
    public void setOutcome(String outcome)
    {
	this.outcome=outcome;
    }
    
 public String getPoolOutcome()
    {
	return poolOutcome;
    }
 
    public void setPoolOutcome(String poolOutcome)
    {
	this.poolOutcome=poolOutcome;
    }

  public String getItemId()
  {
    return itemId;
  }

  /**
   * @param string
   */
  public void setItemId(String string)
  {
    itemId= string;
  }


  public String getItemType()
  {
    return itemType;
  }
  
  public void setItemType(String param)
  {
    this.itemType= param;
  }


  public String getItemText()
  {
    return itemText;
  }
  
  public void setItemText(String itemText)
  {
    this.itemText = itemText;
  }

  /**
   * value of question
   * @return score it is worth
   */
  public double getItemScore()
  {
    return itemScore;
  }

  /**
   * value of question
   * @param score
   */
  public void setItemScore(double score)
  {
    this.itemScore= score;
  }

  /**
   * value of question discount
   * @return discountit is worth
   */
  public double getItemDiscount()
  {
    return itemDiscount;
  }

  /**
   * value of question discount
   * @param discount
   */
  public void setItemDiscount(double discount)
  {
    this.itemDiscount = Math.abs(discount);
  }
  
  /**
   * value of question
   * @return score it is worth
   */
  public String getAdditionalChoices()
  {
    return additionalChoices;
  }

  /**
   * value of question
   * @param score
   */
  public void setAdditionalChoices(String size)
  {
    this.additionalChoices= size;
  }


  /**
   * ordered array of answers
   * @return
   */
  public String[] getAnswerLabels()
  {
    return answerLabels;
  }


  /**
   * set ordered array of answers
   * @param answers ordered array of answers
   */
  public void setAnswerLabels(String[] answers)
  {
    this.answerLabels = answers;
  }



  /**
   * ordered array of answers
   * @return
   */
  public String[] getAnswers()
  {
    return answers;
  }


  /**
   * set ordered array of answers
   * @param answers ordered array of answers
   */
  public void setAnswers(String[] answers)
  {
    this.answers = answers;
  }

  /**
   * ordered array of correct answers
   * @return
   */
  public String getCorrAnswer()
  {
    return corrAnswer;
  }
  
  /**
   * set correct answer for True/False
   * @param answers ordered array of correct answers
   */
  public void setCorrAnswer(String answer)
  {
    this.corrAnswer = answer;
  }

  /**
   * ordered array of correct answers
   * @return
   */
  public String[] getCorrAnswers()
  {
    return corrAnswers;
  }


  /**
   * set ordered array of correct answers
   * @param answers ordered array of correct answers
   */
  public void setCorrAnswers(String[] answers)
  {
    this.corrAnswers = answers;
  }

  /**
   * get 1, 2, 3... for each answer
   * @param n
   * @return
   */
  public int[] getAnswerCounter()
  {
    int n = answers.length;
    int count[] = new int[n];
    for (int i = 0; i < n; i++)
    {
      count[i] = i;
    }
    return count;
  }

  /**
   * get the nth answer
   * @param n
   * @return the nth answer
   */
  public String getAnswer(int n)
  {
    return this.answers[n];
  }

  /**
   * set the nth answer
   * @param n
   * @param answer the nth answer
   */
  public void setAnswer(int n, String answer)
  {
    this.answers[n] = answer;
  }

  /**
   * Return the nth answer number, "A", "B", "C" etc.
   * @param n
   * @return
   */
  public String getAnswerNumber(int n)
  {
     // this accomodates REALLY large numbers of answers
    
    StringBuilder anumbuf = new StringBuilder();
    
    while (n>25)
    {
      anumbuf.append("X");
      n -= 25;
    }
    anumbuf.append(answerNumbers.substring(n));
    String anum = anumbuf.toString();
    return anum;
  }

  /**
   * This is an array of correct/not correct flags
   * @return the array of correct/not correct flags
   */
  public boolean[] getChoiceCorrectArray()
  {
    return choiceCorrectArray;
  }

  /**
   * set array of correct/not correct flags
   * @param choiceCorrectArray of correct/not correct flags
   */
  public void setChoiceCorrectArray(boolean[] choiceCorrectArray)
  {
    this.choiceCorrectArray = choiceCorrectArray;
  }

 public boolean isCorrectChoice(String label) {
    boolean returnVal = false;
    ArrayList corranswersList = ContextUtil.paramArrayValueLike(
          "mccheckboxes");
      Iterator iter = corranswersList.iterator();
      while (iter.hasNext()) {

        String currentcorrect = (String) iter.next();
        if (currentcorrect.trim().equals(label)) {
          returnVal = true;
          break;
        }
        else {
          returnVal = false;
        }
      }
      return returnVal;
 }

  /**
   * is  the nth choice correct?
   * @param n
   * @return
   */
  public boolean isCorrectChoice(int n)
  {
    return choiceCorrectArray[n];
  }

  /**
   * set the nth choice correct?
   * @param n
   * @param correctChoice true if it is
   */
  public void setCorrectChoice(int n, boolean correctChoice)
  {
    this.choiceCorrectArray[n] = correctChoice;
  }
  
  /**
   * for audio recording
   * @return maximum time for recording
   */
  public String getMaxRecordingTime()
  {
    return maxRecordingTime;
  }
  
  /**
   * for audio recording
   * @param maxRecordingTime maximum time for recording
   */
  public void setMaxRecordingTime(String maxRecordingTime)
  {
    this.maxRecordingTime = maxRecordingTime;
  }
  
  /**
   * for audio recording
   * @return maximum attempts
   */
  public String getMaxNumberRecordings()
  {
    return maxNumberRecordings;
  }

  /**
   * set audio recording maximum attempts
   * @param maxNumberRecordings
   */
  public void setMaxNumberRecordings(String maxNumberRecordings)
  {
    this.maxNumberRecordings = maxNumberRecordings;
  }

  /**
   * for survey
   * @return the scale
   */
  public String getScaleName()
  {
    return scaleName;
  }

  /**
   * set the survey scale
   * @param scaleName
   */
  public void setScaleName(String scaleName)
  {
    this.scaleName = scaleName;
  }


  /**
   * for incorrect feedback
   * @return the incorrFeedback
   */
  public String getIncorrFeedback()
  {
    return incorrFeedback;
  }

  /**
   * set the incorrectfeedback
   * @param incorrFeedback
   */
  public void setIncorrFeedback(String param)
  {
    this.incorrFeedback= param;
  }

  /**
   * for correct feedback
   * @return the scale
   */
  public String getCorrFeedback()
  {
    return corrFeedback;
  }

  /**
   * set the corrfeedback
   * @param corrfeedback
   */
  public void setCorrFeedback(String param)
  {
    this.corrFeedback= param;
  }

  /**
   * for general feedback
   * @return the scale
   */
  public String getGeneralFeedback()
  {
    return generalFeedback;
  }

  /**
   * set the generalfeedback
   * @param generalfeedback
   */
  public void setGeneralFeedback(String param)
  {
    this.generalFeedback= param;
  }



   /**
   * get keyword metadata
   */
  public String getKeyword()
  {
    return keyword;
  }

  /**
   * set metadata
   * @param param
   */
  public void setKeyword(String param)
  {
    this.keyword= param;
  }

   /**
   * get objective metadata
   */
  public String getObjective()
  {
    return objective;
  }

  /**
   * set metadata
   * @param param
   */
  public void setObjective(String param)
  {
    this.objective= param;
  }

   /**
   * get rubric metadata
   */
  public String getRubric()
  {
    return rubric;
  }

  /**
   * set metadata
   * @param param
   */
  public void setRubric(String param)
  {
    this.rubric= param;
  }


  public String getTimeAllowed()
  {
    return timeAllowed;
  }
  
  public void setTimeAllowed(String param)
  {
    this.timeAllowed= param;
  }

  public String getNumAttempts()
  {
    return numAttempts;
  }
  
  public void setNumAttempts(String param)
  {
    this.numAttempts= param;
  }

  /**
   * for multiple choice questions, multiple correct?
   * @return
   */
  public String getMultipleCorrectString()
  {
    return multipleCorrectString;
  }

  /**
   * for multiple choice questions
   * @param multipleCorrectString  multiple correct?
   */
  public void setMultipleCorrectString(String multipleCorrect)
  {
    this.multipleCorrectString = multipleCorrect;
  }
  
  public void setMultipleChoiceAnswers(ArrayList list)
  {
    this.multipleChoiceAnswers= list;
  }

  public void setMatchItemBeanList(ArrayList list)
  {
    this.matchItemBeanList= list;
  }
  
  public void setImageMapItemBeanList(ArrayList list)
  {
	  this.imageMapItemBeanList= list;
  }

  /**
   * getSelfSequenceList examines the MatchItemBean list and returns a list of SelectItemOptions that
   * correspond to beans that have a controlling sequence of "Self", meaning that they do not depend 
   * upon any other beans for their choice value.
   * @return a list of SelectItems, to be used to build a dropdown list in matching.jsp
   * @TODO - this may not belong here.  May belong in a helper class that just takes the MatchItemBean list
   */
  public List<SelectItem> getSelfSequenceList() {
	  List options = new ArrayList();
	  String selfSequence = MatchItemBean.CONTROLLING_SEQUENCE_DEFAULT;
	  String distractorSequence = MatchItemBean.CONTROLLING_SEQUENCE_DISTRACTOR;
	  
	  SelectItem selfOption = new SelectItem(selfSequence, selfSequence, selfSequence);
	  options.add(selfOption);
	  SelectItem distractorOption = new SelectItem(distractorSequence, distractorSequence, distractorSequence);
	  options.add(distractorOption);
	  
	  List<SelectItem> subOptions = new ArrayList<SelectItem>();
	  Iterator<MatchItemBean> iter = matchItemBeanList.iterator();
	  while (iter.hasNext()) {
		  MatchItemBean bean = iter.next();
		  /* 
		   * NOTE: the logic here was actually intended to check object equality and not only string equality
		   * on the second test "bean.getSequence() != this.currentMatchPair.getSequence()", this was switched
		   * over to string equality since the strings not being the same here is unlikely and because it
		   * upsets the static code checker but if weird behavior is observed related to this item and
		   * the multi-match use case (primarily the case where 2+ choices or 2+ matches have the exact same string
		   * value and also one of those choices is reused as part of a multiple match)
		   * -AZ
		   */
		  if (MatchItemBean.CONTROLLING_SEQUENCE_DEFAULT.equals(bean.getControllingSequence()) &&
				  !bean.getSequence().equals(this.currentMatchPair.getSequence())) {
			  SelectItem option = new SelectItem(bean.getSequenceStr(), bean.getSequenceStr(), bean.getSequenceStr());
			  subOptions.add(option);
		  }
	  }
	  if (subOptions.size() > 0) {
		  SelectItem[] selectItems = subOptions.toArray(new SelectItem[]{});
		  SelectItemGroup group = new SelectItemGroup("Existing");
		  group.setSelectItems(selectItems);
		  options.add(group);
	  }
	  return options;
  }

  public ArrayList getMatchItemBeanList()
  {
	return matchItemBeanList;
  }

  public void setCurrentMatchPair(MatchItemBean param)
  {
    this.currentMatchPair= param;
  }


  public MatchItemBean getCurrentMatchPair()
  {
        return currentMatchPair;
  }
  
  public ArrayList getImageMapItemBeanList()
  {
	  return imageMapItemBeanList;
  }

  /**
   * for multiple choice questions, multiple correct?
   * @return
   */
  public boolean getMultipleCorrect()
  {
    return multipleCorrect;
  }

  /**
   * for multiple choice questions
   * @param multipleCorrect multiple correct?
   */
  public void setMultipleCorrect(boolean multipleCorrect)
  {
    this.multipleCorrect = multipleCorrect;
  }

  /**
   * Is question to be randomized?
   * @return true or false
   */
  public String getRandomized() {
    return randomized;
  }

  /**
   * Is question to be randomized?
   * @param randomized true if it is
   */
  public void setRandomized(String randomized) {
    this.randomized = randomized;
  }

  /**
   * This question require all answers right to have the full score?
   * @return true or false
   */
  public String getRequireAllOk() {
    	return requireAllOk;
  }
  
  /**
   * This question require all answers right to have the full score?
   * @param requireAllOk true if it is
   */
  public void setRequireAllOk(String requireAllOk) {
    this.requireAllOk = requireAllOk;
  }
  
  /**
   * The image map Image URL
   * @return the URL as String
   */
  public String getImageMapSrc() {
    return imageMapSrc;
  }

  /**
   *  The image map Image URL
   * @param imageMapSrc. The URL as String
   */
  public void setImageMapSrc(String imageMapSrc) {
    this.imageMapSrc = imageMapSrc;
  }

  public String getInstruction() {
    return instruction;
  }
  
  public void setInstruction(String param) {
    this.instruction= param;
  }


  /**
   * has rationale ?
   * @return true or false
   */
  public String getRationale() {
    return rationale;
  }

  /**
   * @param rationale true if it is
   */
  public void setRationale(String param) {
    this.rationale= param;
  }

  /**
   * Maching only.
   * Get an array of match Strings.
   * @return array of match Strings.
   */
  public String[] getMatches() {
    return matches;
  }

  /**
   * Maching only.
   * Set array of match Strings.
   * @param matches array of match Strings.
   */
  public void setMatches(String[] matches) {
    this.matches = matches;
  }

  /**
   * Maching only.
   * Get the nth match String.
   * @param n
   * @return the nth match String
   */
  public String getMatch(int n) {
    return matches[n];
  }

  /**
   * Maching only.
   * Set the nth match String.
   * @param n
   * @param match
   */
  public void setMatch(int n, String match) {
    matches[n] = match;
  }

  /**
   * get 1, 2, 3... for each match
   * @param n
   * @return
   */
  public int[] getMatchCounter()
  {
    int n = matches.length;
    int count[] = new int[n];
    for (int i = 0; i < n; i++)
    {
      count[i] = i;
    }
    return count;
  }

/*

  public ArrayList getAnswerSelectList() {
    ArrayList list = new ArrayList();

    for (int i = 0; i < answers.length; i++) {
      SelectItem selection = new SelectItem();
      selection.setLabel(getAnswerNumber(i));
      selection.setValue(answers[i]);
      list.add(selection);
    }

    return list;
  }
*/


  /**
   * Corresponding answer number list ordered for match
   * @return answer number
   */
  public String[] getMatchAnswers() {
    return matchAnswers;
  }

  /**
   * Corresponding answer number list ordered for match
   * @param matchAnswers answer number list ordered for match
   */
  public void setMatchAnswers(String[] matchAnswers) {
    this.matchAnswers = matchAnswers;
  }

  /**
   * Corresponding answer number for nth match
   * @param n
   * @return
   */
  public String getMatchAnswer(int n) {
    return matchAnswers[n];
  }

  /**
   * set answer number for nth match
   * @param n
   * @param matchAnswer
   */
  public void setMatchAnswer(int n, String matchAnswer) {
    matchAnswers[n] = matchAnswer;
  }

  /**
   * feedback for nth match
   * @param n
   * @return     feedback for nth match

   */
  public String getMatchFeedback(int n) {
    return matchFeedbackList[n];
  }

  /**
   * set feedback for nth match
   * @param n
   * @param matchFeedback feedback for match
   */
  public void setMatchFeedback(int n, String matchFeedback) {
    this.matchFeedbackList[n] = matchFeedback;
  }

  /**
   * array of matching feeback
   * @return array of matching feeback
   */
  public String[] getMatchFeedbackList() {
     return matchFeedbackList;
  }

  /**
   * set array of matching feeback
   * @param matchFeedbackList array of matching feeback
   */
  public void setMatchFeedbackList(String[] matchFeedbackList) {
    this.matchFeedbackList = matchFeedbackList;
  }


  /**
   * String value of selected section id
   * @return String value of selected section id
   */
  public String getSelectedSection() {
    return selectedSection;
  }

  /**
   * set the String value of selected section id
   * @param selectedSection String value of selected section id
   */
  public void setSelectedSection(String selectedSection) {
    this.selectedSection = selectedSection;
  }

  /**
   * String value of selected pool id
   * @return String value of selected pool id
   */
  public String getSelectedPool() {
    return selectedPool;
  }

  /**
   * set the String value of selected pool id
   * @param selectedPool String value of selected pool id
   */
  public void setSelectedPool(String selectedPool) {
    this.selectedPool = selectedPool;
  }

  /**
   * String value of selected pool id
   * @return String value of selected pool id
   */
  public String getOrigPool() {
    return origPool;
  }

  /**
   * set the String value of selected pool id
   * @param selectedPool String value of selected pool id
   */
  public void setOrigPool(String param) {
    this.origPool = param;

  }


 /**
   * String value of selected pool id
   * @return String value of selected pool id
   */
  public String getOrigSection() {
    return origSection;
  }

  /**
   * set the String value of selected pool id
   * @param selectedSection String value of selected pool id
   */
  public void setOrigSection(String param) {
    this.origSection= param;
  }

  public ArrayList getMultipleChoiceAnswers() {
	ArrayList list = new ArrayList();
	// build a default list of 4 choices, a, b, c, d,
	if (multipleChoiceAnswers!=null) {
		return multipleChoiceAnswers;
	// for modify
 	}
	else {
	int defaultlength = 4;
	for (int i=0; i<defaultlength; i++){
		AnswerBean answerbean = new AnswerBean();
       		answerbean.setSequence( Long.valueOf(i+1));
       		answerbean.setLabel(AnswerBean.getChoiceLabels()[i]);
                
      		list.add(answerbean);
             
    	}
	
	setMultipleChoiceAnswers(list);
	}// else

    return list;
  }

  public void toggleChoiceTypes(ValueChangeEvent event) {

	//FacesContext context = FacesContext.getCurrentInstance();
	String type = (String) event.getNewValue();
	if ((type == null) || type.equals(TypeFacade.MULTIPLE_CHOICE.toString())) {
	  setItemType(TypeFacade.MULTIPLE_CHOICE.toString());
	}
	else if (type.equals(TypeFacade.MULTIPLE_CORRECT_SINGLE_SELECTION.toString())) {
	  setItemType(TypeFacade.MULTIPLE_CORRECT_SINGLE_SELECTION.toString());
	}
	else {
	  setItemType(TypeFacade.MULTIPLE_CORRECT.toString());
	}

  }

  public String addChoicesAction() {
        // build a default list of 4 choices, a, b, c, d,
     //   FacesContext context = FacesContext.getCurrentInstance();
     //   String newvalue = (String) event.getNewValue();
          String newvalue = this.getAdditionalChoices();
        ArrayList list = getMultipleChoiceAnswers(); // get existing list
        if (list!=null) {
                // add additional answer bean
           int currentsize = list.size();
           int newlength = currentsize+ new Integer(newvalue).intValue();
           if (newlength<=26){
              for (int i=currentsize; i<newlength; i++){
                  AnswerBean answerbean = new AnswerBean();
                  answerbean.setSequence( Long.valueOf(i+1));
                  answerbean.setLabel(AnswerBean.getChoiceLabels()[i]);
                  list.add(answerbean);

                }
              setMultipleChoiceAnswers(list);
              setAdditionalChoices("0");

              // if mcmc, need to set corrAnswers 
              if (TypeFacade.MULTIPLE_CORRECT.toString().equals(this.itemType) || TypeFacade.MULTIPLE_CORRECT_SINGLE_SELECTION.toString().equals(this.itemType)) {
                 ArrayList corranswersList = ContextUtil.paramArrayValueLike("mccheckboxes");
                 int corrsize = corranswersList.size();
                 int counter = 0;
                 String[] corrchoices = new String[corrsize];
                 Iterator iter = corranswersList.iterator();
                 while (iter.hasNext()) {

                   String currentcorrect = (String) iter.next();
	           corrchoices[counter]= currentcorrect; 
                   counter++;
                 }
                 this.setCorrAnswers(corrchoices);
             }
           }
           else
	       {
		   //print error
               FacesContext context=FacesContext.getCurrentInstance();
               context.addMessage(null,new FacesMessage(RB_AUTHOR_MESSAGES.getString("MCanswer_outofbound_error")));
	       }
    
       }
        return "multipleChoiceItem";
  }

  public String removeChoices() {
		String labelToRemove = ContextUtil.lookupParam("answerid");
		ArrayList corranswersList = ContextUtil.paramArrayValueLike("mccheckboxes");
		Object [] objArray = corranswersList.toArray();
		String [] corrAnswers = new String[objArray.length];
		ArrayList list = getMultipleChoiceAnswers(); // get existing list
		if (list == null) {
			return null;
		}
		Iterator iter = list.iterator();
		int currentindex = 0;
		int correctIndex = 0;
		boolean delete = false;
		while (iter.hasNext()) {
			AnswerBean answerbean = (AnswerBean) iter.next();
			if (answerbean.getLabel().equals(labelToRemove)) {
				// delete selected choices
				iter.remove();
				delete = true;
			} else {
				currentindex = currentindex + 1;
				// reset sequence and labels , shift the seq/labels after a
				// choice is deleted
				answerbean.setSequence( Long.valueOf(currentindex));
				answerbean.setLabel(AnswerBean.getChoiceLabels()[currentindex - 1]);
			}
			
			// reset correct answers
			for (int i = 0; i < objArray.length; i++) {
				if (!labelToRemove.equals(objArray[i])) {
					if ((delete && AnswerBean.getChoiceLabels()[currentindex].equals(objArray[i])) ||
							(!delete && AnswerBean.getChoiceLabels()[currentindex - 1].equals(objArray[i]))) {
						corrAnswers[correctIndex++] = AnswerBean.getChoiceLabels()[currentindex - 1];
					}
				}
			}
		}
		this.setCorrAnswers(corrAnswers);
		return null;
	}

  public String removeChoicesSingle() {
		String labelToRemove = ContextUtil.lookupParam("answeridSingle");
		ArrayList corranswersList = ContextUtil.paramArrayValueLike("mcradiobtn");
		Object [] objArray = corranswersList.toArray();
		String [] corrAnswers = new String[objArray.length];
		ArrayList list = getMultipleChoiceAnswers(); // get existing list
		if (list == null) {
			return null;
		}
		Iterator iter = list.iterator();
		int currentindex = 0;
		int correctIndex = 0;
		boolean delete = false;
		while (iter.hasNext()) {
			AnswerBean answerbean = (AnswerBean) iter.next();
			if (answerbean.getLabel().equals(labelToRemove)) {
				// delete selected choices
				iter.remove();
				delete = true;
			} else {
				currentindex = currentindex + 1;
				// reset sequence and labels , shift the seq/labels after a
				// choice is deleted
				answerbean.setSequence( Long.valueOf(currentindex));
				answerbean.setLabel(AnswerBean.getChoiceLabels()[currentindex - 1]);
			}
			
			// reset correct answers
			for (int i = 0; i < objArray.length; i++) {
				if (!labelToRemove.equals(objArray[i])) {
					if ((delete && AnswerBean.getChoiceLabels()[currentindex].equals(objArray[i])) ||
							(!delete && AnswerBean.getChoiceLabels()[currentindex - 1].equals(objArray[i]))) {
						corrAnswers[correctIndex++] = AnswerBean.getChoiceLabels()[currentindex - 1];
					}
				}
			}
		}

		this.setCorrAnswers(corrAnswers);
		if (corrAnswers.length == 0) {
			this.setCorrAnswer("");
		}
		else {
			this.setCorrAnswer(corrAnswers[0]);
		}
		return null;
	}
  
// Huong added for matching

    public boolean isMatchError(){
// need to trim, 'cuz in  mozilla, the field is printed as ^M , a new line char. 
	String choice=(currentMatchPair.getChoice().replaceAll("<^[^(img)(IMG)]*?>", "")).trim();
	String match=(currentMatchPair.getMatch().replaceAll("<^[^(img)(IMG)]*?>", "")).trim();
    
	// choice cannot be blank or null
	// match cannot be blank or null if controllingSequence is the default
	if(choice==null ||choice.equals("")|| 
			((match==null || match.equals("")) && MatchItemBean.CONTROLLING_SEQUENCE_DEFAULT.equals(currentMatchPair.getControllingSequence()))) {
	    FacesContext context=FacesContext.getCurrentInstance();
	    context.addMessage(null,new FacesMessage(RB_AUTHOR_MESSAGES.getString("match_error")));
	    return true;
	}
	return false;
    }

  public String addMatchPair() {
      if (!isMatchError()){
	    // get existing list
	    ArrayList<MatchItemBean> list = getMatchItemBeanList();
	    MatchItemBean newpair = null;
	    MatchItemBean currpair = this.getCurrentMatchPair();
	    if (!currpair.getSequence().equals( Long.valueOf(-1))) {
	      // for modify
	      int seqno =  currpair.getSequence().intValue()-1;
	      newpair= (MatchItemBean)  this.getMatchItemBeanList().get(seqno);
	      newpair.setSequence(currpair.getSequence());
	    }
	    else {
	      // for new pair
	      newpair = new MatchItemBean();
	      newpair.setSequence( Long.valueOf(list.size()+1));
	      list.add(newpair);
	    }
	    
	    // update the bean with the new values, now that is has been retrieved/created
	    newpair.setChoice(currpair.getChoice());
	    newpair.setMatch(currpair.getMatch());
	    newpair.setCorrMatchFeedback(currpair.getCorrMatchFeedback());
	    newpair.setIncorrMatchFeedback(currpair.getIncorrMatchFeedback());
	    newpair.setIsCorrect(Boolean.TRUE);
	    newpair.setControllingSequence(currpair.getControllingSequence());
	    if (MatchItemBean.CONTROLLING_SEQUENCE_DISTRACTOR.equals(newpair.getControllingSequence())) {
	  	  newpair.setMatch(MatchItemBean.CONTROLLING_SEQUENCE_DISTRACTOR);
	    } else if (!MatchItemBean.CONTROLLING_SEQUENCE_DEFAULT.equals(newpair.getControllingSequence())) {
	  	  Iterator<MatchItemBean> listIter = list.iterator();
	  	  while (listIter.hasNext()) {
	  		  MatchItemBean curBean = listIter.next();
	  		  if (newpair.getControllingSequence().equals(curBean.getSequenceStr())) {
	  			  newpair.setMatch(curBean.getMatch());
	  			  break;
	  		  }
	  	  }
	    }
		
	    this.setMatchItemBeanList(list); // get existing list
	
	    //debugging
	    /*
	    Iterator iter = list.iterator();
	    while(iter.hasNext())
	    {
	      MatchItemBean apair = (MatchItemBean) iter.next();
	    }
	    */
	    
	    MatchItemBean matchitem = new MatchItemBean();
	    this.setCurrentMatchPair(matchitem);
      }
    return "matchingItem";
  }



  public String editMatchPair() {

	String seqnostr = ContextUtil.lookupParam("sequence");
     int seqno =  Integer.valueOf(seqnostr).intValue()-1;
    // get currentmatchpair by sequence.

    MatchItemBean pairForEdit= (MatchItemBean) this.getMatchItemBeanList().get(seqno);
    this.setCurrentMatchPair(pairForEdit);
    return "matchingItem";
  }



  public String removeMatchPair() {

	String seqnostr = ContextUtil.lookupParam("sequence");
     int seqno = Integer.valueOf(seqnostr).intValue()-1;
    // get currentmatchpair by sequence.

    this.getMatchItemBeanList().remove(seqno);
    // shift seqno
    Iterator iter = this.getMatchItemBeanList().iterator();
    int i = 1;
    while(iter.hasNext())
    {
      MatchItemBean apair = (MatchItemBean) iter.next();
      apair.setSequence( Long.valueOf(i));
      i++;
    }

// debugging
    /*
    iter = this.getMatchItemBeanList().iterator();
    while(iter.hasNext())
    {
      MatchItemBean apair = (MatchItemBean) iter.next();
    }
    */

    MatchItemBean matchitem = new MatchItemBean();
    this.setCurrentMatchPair(matchitem);
    return "matchingItem";
  }

	/// IMAGEMAP
	public String getSerializedImageMap() {
		StringBuffer ret = new StringBuffer();
		List<ImageMapItemBean> list = getImageMapItemBeanList();
		for (ImageMapItemBean ib : list) {
			if (ret.length() > 0)
				ret.append("#-#");
			ret.append(ib.serialize());
		}
		return ret.toString();
	}

	public void setSerializedImageMap(String serializedString) {
		if (serializedString != null) {
			ArrayList<ImageMapItemBean> list = new ArrayList<ImageMapItemBean>();
			for (String str : serializedString.split("#-#")) {
				if (str != null && !"".equals(str)) {
					ImageMapItemBean imib = new ImageMapItemBean(str);
					imib.setSequence(Long.valueOf(list.size() + 1));
					list.add(imib);
				}
			}
			this.setImageMapItemBeanList(list);
		}
	}

  /**
   * for fib, case sensitive for grading?
   * @return
   */
  public boolean getCaseSensitiveForFib()
  {
    return caseSensitiveForFib;
  }

  /**
   * for fib questions
   * @param case sensitive for grading?
   */
  public void setCaseSensitiveForFib(boolean param)
  {
    this.caseSensitiveForFib = param;
  }

  /**
   * for fib, Mutually exclusive for multiple answers,  for grading?
   * @return
   */
  public boolean getMutuallyExclusiveForFib()
  {
    return mutuallyExclusiveForFib;
  }

  /**
   * for fib questions
   * @param, Mutually exclusive for multiple answers,  for grading?
   */
  public void setMutuallyExclusiveForFib(boolean param)
  {
    this.mutuallyExclusiveForFib = param;
  }

  /**
   * for fib, Mutually exclusive for multiple answers,  for grading?
   * @return
   */
  public boolean getShowMutuallyExclusiveForFibCheckbox()
  {
    return showMutuallyExclusiveForFibCheckbox;
  }

  /**
   * for fib questions
   * @param, Mutually exclusive for multiple answers,  for grading?
   */
  public void setShowMutuallyExclusiveForFibCheckbox(boolean param)
  {
    this.showMutuallyExclusiveForFibCheckbox= param;
  }

    /**
     * for fib, ignore spaces for grading?
     * @return
     */
    public boolean getIgnoreSpacesForFib()
    {
      return ignoreSpacesForFib;
    }

    /**
     * for fib questions
     * @param param ignore spaces for grading?
     */
    public void setIgnoreSpacesForFib(boolean param)
    {
      this.ignoreSpacesForFib = param;
    }

    /**
     * for fin, case sensitive for grading?
     * @return
     */
    public boolean getCaseSensitiveForFin()
    {
      return caseSensitiveForFin;
    }

    /**
     * for fin questions
     * @param case sensitive for grading?
     */
    public void setCaseSensitiveForFin(boolean param)
    {
      this.caseSensitiveForFin = param;
    }

    /**
     * for fin, Mutually exclusive for multiple answers,  for grading?
     * @return
     */
    public boolean getMutuallyExclusiveForFin()
    {
      return mutuallyExclusiveForFin;
    }

    /**
     * for fin questions
     * @param, Mutually exclusive for multiple answers,  for grading?
     */
    public void setMutuallyExclusiveForFin(boolean param)
    {
      this.mutuallyExclusiveForFin = param;
    }

    /**
     * for fin, Mutually exclusive for multiple answers,  for grading?
     * @return
     */
    public boolean getShowMutuallyExclusiveForFinCheckbox()
    {
      return showMutuallyExclusiveForFinCheckbox;
    }

    /**
     * for fin questions
     * @param, Mutually exclusive for multiple answers,  for grading?
     */
    public void setShowMutuallyExclusiveForFinCheckbox(boolean param)
    {
      this.showMutuallyExclusiveForFinCheckbox= param;
    }

    /**@author Mustansar Mehmood 
     * 
     */
    public void  setPartialCreditFlag(String partialCreditFlag){
    		   this.partialCreditFlag=partialCreditFlag;
    }
    
    /**
	 * @author Mustansar Mehmood
	 * 
	 * @return
	 */
	public String getPartialCreditFlag() {
		if (this.isPartialCreditEnabled()) {
			return partialCreditFlag;
		} else {
			return "false";
		}
	}

    //************ EMI Answer Options******************
    
    public void setEmiAnswerOptions(List<AnswerBean> list)
    {
    	this.emiAnswerOptions= list;
    }
    
    // Modified for Javascript Add/Remove 
    public List<AnswerBean> getEmiAnswerOptions() {
    	if (emiAnswerOptions==null) {
    		emiAnswerOptions = new ArrayList<AnswerBean>();
    	}
    	int defaultlength = DEFAULT_MAX_NUMBER_EMI_OPTIONS_FOR_UI;
    	// build or extend the list of items 26 a-z
    	// for efficiency, these will now be shown/hidden using javascript
		if (emiAnswerOptions.size() < defaultlength) {
			List<AnswerBean> list = new ArrayList<AnswerBean>();
			list.addAll(emiAnswerOptions);
			for (int i=emiAnswerOptions.size(); i<defaultlength; i++ ) {
    			AnswerBean answerbean = new AnswerBean();
           		answerbean.setSequence( Long.valueOf(i+1));
				answerbean.setLabel(AnswerBean.getChoiceLabels()[i]);
				list.add(answerbean);
			}
			emiAnswerOptions = list;
		}
		return emiAnswerOptions;
    }
    	
    public List<AnswerBean> getEmiAnswerOptionsClean() {
    	List<AnswerBean> list = new ArrayList<AnswerBean>();
    	if (emiAnswerOptions!=null) {
        	list.addAll(emiAnswerOptions);
    	}
		for (int i=list.size()-1; i>=0; i--) {
			AnswerBean answerbean = (AnswerBean)list.get(i);
			if (answerbean.getText() == null || answerbean.getText().trim().equals("")) {
				list.remove(i);
			}
			else {
				break;
			}
		}
		emiAnswerOptions = list;
		return list;
    }    
    
	public boolean isPartialCreditEnabled() {
		if (partialCreditEnabledChecked) {
			return partialCreditEnabled;
		}
		partialCreditEnabledChecked = true;
		String partialCreditEnabledString = ServerConfigurationService.getString("samigo.partialCreditEnabled");
		if (partialCreditEnabledString.equalsIgnoreCase("true")){
			partialCreditEnabled = true;
		}
		else {
			partialCreditEnabled = false;
		}
		return partialCreditEnabled;
	}
    
	public void togglePartialCredit(ValueChangeEvent event) {
		String switchEvent = (String) event.getNewValue();
	    
		if (Boolean.parseBoolean(switchEvent)) {
			setPartialCreditFlag("true");
			this.resetPartialCreditValues();
		}
		else if ("False".equalsIgnoreCase(switchEvent)) {
			setPartialCreditFlag("false");
		} else {
			setPartialCreditFlag("Default");
		}
	}

    public String populateEmiAnswerOptionsFromPasted() {
    	String pasted = getEmiAnswerOptionsPaste();
    	if (pasted == null || pasted.trim().equals("")) return "emiItem";
    	
    	ArrayList list = new ArrayList();
    	String[] pastedOptions = getEmiAnswerOptionsPaste().split("\n");
    	int labelCount = 0;
		for (int i=0; i<pastedOptions.length; i++) {
			if (pastedOptions[i]==null || pastedOptions[i].trim().equals("")) continue;
			AnswerBean answerbean = new AnswerBean();
       		answerbean.setSequence( Long.valueOf(i+1));
       		answerbean.setLabel(AnswerBean.getChoiceLabels()[labelCount++]);
       		answerbean.setText(pastedOptions[i]);
      		list.add(answerbean);
		}
    	
    	setEmiAnswerOptions(list);
    	this.setEmiAnswerOptionsPaste(null);
        return "emiItem";
    }

    //*************** EMI Lead In Statement **********************
    public String getLeadInStatement() {
    	return leadInStatement;
    }

    public void setLeadInStatement(String leadInStatement) {
    	this.leadInStatement = leadInStatement;
    }

  //*************** EMI Question-Answer Combinations **********************
    
    public void setEmiQuestionAnswerCombinations(List<AnswerBean> list)
    {
    	this.emiQuestionAnswerCombinations = list;
    }

    public void setEmiQuestionAnswerCombinationsUI(List<AnswerBean> list)
    {
    	this.emiQuestionAnswerCombinations = list;
    }

    // Modified for Javascript Add/Remove
    public List<AnswerBean> getEmiQuestionAnswerCombinationsUI() {
    	if (emiQuestionAnswerCombinations==null) {
    		emiQuestionAnswerCombinations = new ArrayList<AnswerBean>();
    	}
    	int defaultlength = DEFAULT_MAX_NUMBER_EMI_ITEMS_FOR_UI;
    	// build or extend the list of items 26 a-z
    	// for efficiency, these will now be shown/hidden using javascript
		if (emiQuestionAnswerCombinations.size() < defaultlength) {
			List<AnswerBean> list = new ArrayList<AnswerBean>();
			list.addAll(emiQuestionAnswerCombinations);
			for (int i=list.size(); i<defaultlength; i++ ) {
    			AnswerBean answerbean = new AnswerBean();
           		answerbean.setSequence(Long.valueOf(i+1));
				answerbean.setLabel(answerbean.getSequence().toString());
				list.add(answerbean);
			}
			emiQuestionAnswerCombinations=list;
		}
		return emiQuestionAnswerCombinations;
    }
    
    public List<AnswerBean> getEmiQuestionAnswerCombinationsClean() {
    	String removeLabel = "X";
    	List<AnswerBean> list = new ArrayList<AnswerBean>();
    	if (emiQuestionAnswerCombinations!=null) {
        	list.addAll(emiQuestionAnswerCombinations);
    	}
    	List<AnswerBean> cleanSortedList = new ArrayList<AnswerBean>();
    	for (int i=0; i<list.size(); i++) {
    		AnswerBean emiItem = (AnswerBean)list.get(i);
    		if (emiItem==null || emiItem.getLabel().trim().equals(removeLabel)) continue;
    		//must have either text or attachment
    		if ((emiItem.getText()==null || emiItem.getText().trim().equals(""))
    			&& !emiItem.getHasAttachment()) continue;
    		emiItem.setSequence(Long.valueOf(emiItem.getLabel()));
    		cleanSortedList.add(emiItem);
    	}
    	Collections.sort(cleanSortedList);
    	for (int i=0; i<cleanSortedList.size(); i++) {
    		AnswerBean emiItem = cleanSortedList.get(i);
    		int seq = i+1;
    		emiItem.setSequence(Long.valueOf(seq));
    		emiItem.setLabel(""+seq);
    	}
    	emiQuestionAnswerCombinations=cleanSortedList;
    	return cleanSortedList;
    }

    public void resetPartialCreditValues() {

		ArrayList answersList = this.getMultipleChoiceAnswers();
		Iterator iter = answersList.iterator();
		// information about the correct answer is not available here so
		// checking whether the answer is correct simply leads to NPE.
		while (iter.hasNext()) {
			AnswerBean answerBean = (AnswerBean) iter.next();
			if (Integer.parseInt(answerBean.getPartialCredit()) < 100) {
				answerBean.setPartialCredit("0");
			}
			else {
				answerBean.setPartialCredit("100");
			}
		}
		this.setMultipleChoiceAnswers(answersList);
	}

	public String resetToDefaultGradingLogic() {
		// String switchEvent = (String) event.getNewValue();
		partialCreditFlag = "Default";
		ArrayList answersList = this.getMultipleChoiceAnswers();
		Iterator iter = answersList.iterator();
		// information about about the correct answer is not available here so
		// checking whether the answer is correct
		// simply leads to NPE.
		while (iter.hasNext()) {
			AnswerBean answerBean = (AnswerBean) iter.next();
			answerBean.setPartialCredit("0");
		}
		this.setMultipleChoiceAnswers(answersList);
		return null;
	}

	public int gettotalMCAnswers() {
		return this.multipleChoiceAnswers.size();
	}

	public void settotalMCAnswers() {
		this.totalMCAsnwers = this.multipleChoiceAnswers.size();
	}

	public String getEmiAnwerOptionLabels() {
		String simpleOrRich = this.getAnswerOptionsSimpleOrRich();
		String emiAnswerOptionLabels = "";
		if (simpleOrRich.equals(ItemDataIfc.ANSWER_OPTIONS_SIMPLE.toString())) {
			Iterator iter = getEmiAnswerOptionsClean().iterator();
			while (iter.hasNext()) {
				AnswerBean answerBean = (AnswerBean) iter.next();
				emiAnswerOptionLabels += answerBean.getLabel();
			}
		}
		else { // ANSWER_OPTIONS_RICH
			emiAnswerOptionLabels = ItemDataIfc.ANSWER_OPTION_LABELS.substring(0, Integer.valueOf(this.getAnswerOptionsRichCount()));
		}
		return emiAnswerOptionLabels.toUpperCase();
	}
	
	public String getEmiAnswerOptionLabelsSorted() {
		String emiAnswerOptionLabels = getEmiAnwerOptionLabels();
		if (emiAnswerOptionLabels.trim() == "") return emiAnswerOptionLabels;
		// Rich Options are Generated - So will always be sorted
		if (getAnswerOptionsSimpleOrRich().equals(ItemDataIfc.ANSWER_OPTIONS_RICH.toString())) return emiAnswerOptionLabels;
		
		ArrayList optionLabels = new ArrayList();
		for (int i=0; i<emiAnswerOptionLabels.length(); i++) {
			optionLabels.add(emiAnswerOptionLabels.substring(i, i+1));
		}
		Collections.sort(optionLabels);
		String emiAnswerOptionLabelsSorted = "";
		for (int i=0; i<emiAnswerOptionLabels.length(); i++) {
			emiAnswerOptionLabelsSorted += optionLabels.get(i).toString();
		}
		return emiAnswerOptionLabelsSorted;
	}

	public String getEmiAnswerOptionsRich() {
		return emiAnswerOptionsRich;
	}

	public void setEmiAnswerOptionsRich(String emiAnswerOptionsRich) {
		this.emiAnswerOptionsRich = emiAnswerOptionsRich;
	}

	public String getEmiAnswerOptionsPaste() {
		return emiAnswerOptionsPaste;
	}

	public void setEmiAnswerOptionsPaste(String emiAnswerOptionsPaste) {
		this.emiAnswerOptionsPaste = emiAnswerOptionsPaste;
	}

	public String getAnswerOptionsSimpleOrRich() {
		return answerOptionsSimpleOrRich;
	}

	public void setAnswerOptionsSimpleOrRich(String answerOptionsSimpleOrRich) {
		this.answerOptionsSimpleOrRich = answerOptionsSimpleOrRich;
	}

	public String getAnswerOptionsRichCount() {
		return answerOptionsRichCount;
	}

	public void setAnswerOptionsRichCount(String answerOptionsRichCount) {
		this.answerOptionsRichCount = answerOptionsRichCount;
	}

	public void setEmiVisibleItems(String emiVisibleItems) {
		this.emiVisibleItems = emiVisibleItems;
	}

	public String getEmiVisibleItems() {
		return emiVisibleItems;
	}
	
	//sam-939
	public boolean getAddToFavorite()
	{
		return this.addToFavorite ;
	}

	public void setAddToFavorite(boolean param)
	{
		//value from jsp loop
		if (newAddToFavorite){

			newAddToFavorite=false;
			return;
		}

		this.addToFavorite = param;
	}
	
	public boolean getForceRanking()
	{
		return this.forceRanking;
	}
	
	public void setForceRanking(boolean param)
	{
		this.forceRanking = param;
	}

	public int getRelativeWidth()
	{
		return this.relativeWidth;
	}
	public void setRelativeWidth(int param)
	{
		this.relativeWidth = param;
	}
	
	// these give index into the select list, which is 10, 20, 30, etc. 0 for not specified
	public String getSelectedRelativeWidth() {
		return Integer.toString(this.relativeWidth / 10);
	}
	
	public void setSelectedRelativeWidth(String param) {
		this.relativeWidth= (Integer.parseInt(param) * 10);
	}

	public boolean getAddComment()
	{
		return this.addComment;
	}
	public void setAddComment(boolean param)
	{
		this.addComment= param;
	}
	public String getRowChoices() {
		return this.rowChoices;
	}
	
	public void setRowChoices(String param) {
		StringBuilder r = new StringBuilder();
		StringTokenizer t = new StringTokenizer(param, "\n");
		while (t.hasMoreTokens()) {
			r.append(t.nextToken().trim()).append(System.getProperty("line.separator"));
		}
		this.rowChoices= r.toString();
	}
	
	public String getColumnChoices() {
		return this.columnChoices;
	}
	
	public void setColumnChoices(String param) {
		// if user chose a new favorite, the value change listener set the
		// choices from the favorite. We don't want to take the values
		// submitted with the form.
		if (newFavoriteChoice){
			newFavoriteChoice=false;
			return;
		}
		StringBuilder r = new StringBuilder();
		StringTokenizer t = new StringTokenizer(param, "\n");
		while (t.hasMoreTokens()) {
			r.append(t.nextToken().trim()).append(System.getProperty("line.separator"));
		}
		this.columnChoices= r.toString();
	}

	public String getFavoriteName() {
		return this.favoriteName;
	}

	public void setFavoriteName(String param) {
		if (fromFavoriteSelectOneMenu){
			fromFavoriteSelectOneMenu=false;
			return;
		}
		this.favoriteName = param;
	}

	public boolean getFromFavoriteSelectOneMenu(){
		return false;
	}
	
	public void setFromFavoriteSelectOneMenu(boolean param){
		this.fromFavoriteSelectOneMenu = param;
	}
	
	public boolean getNewFavoriteChoice() {
		return false;
	}
	
	public void setNewFavoriteChoice(boolean param) {
		this.newFavoriteChoice = param;
	}
	
	public boolean getNewAddToFavorite() {
		return false;
	}
	
	public void setNewAddToFavorite(boolean param) {
		this.newAddToFavorite = param;
	}
	
	public String getCommentField() {
		return this.commentField;
	}
	
	public void setCommentField(String param) {
		this.commentField = param;
	}
	
	public int getCommentFieldLenght() {
		return commentField==null ? 0 : commentField.length();
	}
	
	public void toggleAddToFavorite(ValueChangeEvent event) {
		this.addToFavorite = ((Boolean)event.getNewValue()).booleanValue(); 
	}
	
	public void toggleAddComment(ValueChangeEvent event) {
		this.addComment = ((Boolean)event.getNewValue()).booleanValue(); 
	}

	public String getCurrentFavorite(){
		return this.currentFavorite;
	}
	
	public void setCurrentFavorite(String favorite){
		this.currentFavorite = favorite;
	}
	
	public boolean getHasFavoriteList(){
		//check the DB return value for that perticular agent 
		String agentId = AgentFacade.getAgentString();
		AssessmentService assessment = new AssessmentService();
		if (assessment.getFavoriteColChoicesbyAgent(agentId) != null && 
				assessment.getFavoriteColChoicesbyAgent(agentId).size() > 0 ){
			this.hasFavoriteList = true;
		}
		return this.hasFavoriteList;
	}

	public void setHasFavoriteList(boolean param){
		this.hasFavoriteList = param;
	}

	public void setCurrentFavoriteList(String[] list){
		if(!hasFavoriteList) return;
		
		if (list.length > 0)
		{
			this.hasFavoriteList = true;

			this.currentFavoriteList = new ArrayList();
			for(int i=0; i<list.length; i++ ){
				this.currentFavoriteList.add(new SelectItem(list[i]));
			}
		}

	}
	public ArrayList getCurrentFavoriteList(){
		String agentId = AgentFacade.getAgentString();
		AssessmentService assessment = new AssessmentService();
		List favorites = assessment.getFavoriteColChoicesbyAgent(agentId);
		if (favorites != null && favorites.size() > 0 ){
			this.currentFavoriteList = new ArrayList();
			Iterator iter = favorites.iterator();
			while(iter.hasNext()){
				FavoriteColChoices choices =(FavoriteColChoices)iter.next();
				SelectItem item = new SelectItem(choices.getFavoriteName());
				this.currentFavoriteList.add(item);
			}
			return this.currentFavoriteList;

		}
		return null;
	}
	
	public void setColumnChoicesFromFavorite(String strFavorite){

		StringBuffer strBuff = new StringBuffer();
		String agentId = AgentFacade.getAgentString();
		AssessmentService assessment = new AssessmentService();
		List favorites = assessment.getFavoriteColChoicesbyAgent(agentId);
		if (favorites != null && favorites.size() > 0 ){
			Iterator iter = favorites.iterator();
			while(iter.hasNext()){ 
				FavoriteColChoices choice =(FavoriteColChoices) iter.next();
				if (choice.getFavoriteName().equals(strFavorite)){
					Set choicesItem = new HashSet();
					choicesItem = choice.getFavoriteItems();
					FavoriteColChoicesItem [] itemArray= (FavoriteColChoicesItem[]) choicesItem.toArray(new FavoriteColChoicesItem[choicesItem.size()]);
					//Arrays.sort(itemArray);
					//sequence always starts from 0
					for(int j=0; j<itemArray.length; j++){
						for (int i=0; i<itemArray.length; i++){
							FavoriteColChoicesItem item = (FavoriteColChoicesItem)itemArray[i];
							if(item.getSequence().intValue()==j){
								strBuff.append(item.getChoiceText());
								strBuff.append("\n");
								break;
							}
						}
					}
					this.setColumnChoices(strBuff.toString());
					this.setAddToFavorite(true);
					this.setFavoriteName(strFavorite);
					newFavoriteChoice = true; // this has to override value submitted with form
					newAddToFavorite = true;
					fromFavoriteSelectOneMenu = true;
					return;
				}	
			} 
		}
	}

	public void setCalculatedQuestion(CalculatedQuestionBean calculatedQuestion) {
	    this.calculatedQuestion = calculatedQuestion;
	}

	public CalculatedQuestionBean getCalculatedQuestion() {
	    return this.calculatedQuestion;
	}

	public String getMcmsPartialCredit() {
		if(mcmsPartialCredit == null){
			//set default (true by default)
			mcmsPartialCredit = ServerConfigurationService.getString("samigo.mcmsPartialCredit", "true");
		}
		return mcmsPartialCredit;
	}

	public void setMcmsPartialCredit(String mcmsPartialCredit) {
		this.mcmsPartialCredit = mcmsPartialCredit;
	}

	public Double getItemMinScore() {
		return itemMinScore;
	}

	public void setItemMinScore(Double itemMinScore) {
		this.itemMinScore = itemMinScore;
	}

	public String getItemScoreDisplayFlag() {
		return itemScoreDisplayFlag;
	}

	public void setItemScoreDisplayFlag(String itemScoreDisplayFlag) {
		this.itemScoreDisplayFlag = itemScoreDisplayFlag;
	}

	public List<ItemTagBean> getItemTags() { return itemTags; }

	public void setItemTags(List<ItemTagBean> itemTags) { this.itemTags = itemTags; }
}
