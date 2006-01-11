/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
*
**********************************************************************************/

package org.sakaiproject.tool.assessment.ui.bean.author;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.TypeFacade;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;


/**
 * UI bean for authoring an Item
 * $Id$
 */
public class ItemBean
  implements Serializable
{
  private static Log log = LogFactory.getLog(ItemBean.class);

  // internal use
  private static final String answerNumbers =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 8266438770394956874L;

  // for item editing
  private String itemText;
  private String itemId;
  private String itemType;
  private String itemScore= "0";
  private String[] answers;
  private String[] answerLabels;  //  such as A, B, C
  private String[] corrAnswers;  // store checkbox values(labels) for multiple correct answers, as in mcmc type
  private String corrAnswer;  // store text value for single correct answer, as in true/false , mcsc, also used for essay's model answer
  private ArrayList multipleChoiceAnswers;  // store List of answers multiple choice items, ArrayList of AnswerBean
  private String additionalChoices = "0";  // additonal multiple choice answers to be add. for the select menu





  private boolean[] choiceCorrectArray;
  private String maxRecordingTime;
  private String maxNumberRecordings;
  private String scaleName;
  private boolean multipleCorrect = false ;
  private String multipleCorrectString;
  private String randomized = "false";
  private String rationale = "false";

// for matching only
  private String instruction;  // matching's question text
  private ArrayList matchItemBeanList;  // store List of MatchItemBean, used for Matching only
  private MatchItemBean currentMatchPair;  // do not need this ?   store List of MatchItemBeans, used for Matching only

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
  public String getItemScore()
  {
    return itemScore;
  }

  /**
   * value of question
   * @param score
   */
  public void setItemScore(String score)
  {
    this.itemScore= score;
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
    String anum = "";
    // this accomodates REALLY large numbers of answers
    while (n>25)
    {
      anum += "X";
      n -= 25;
    }
    anum += answerNumbers.substring(n);

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
       		answerbean.setSequence(new Long(i+1));
       		answerbean.setLabel(AnswerBean.choiceLabels[i]);
                
      		list.add(answerbean);
             
    	}
	
	setMultipleChoiceAnswers(list);
	}// else

    return list;
  }

  public void toggleChoiceTypes(ValueChangeEvent event) {

	FacesContext context = FacesContext.getCurrentInstance();
	String type = (String) event.getNewValue();
	if ((type == null) || type.equals(TypeFacade.MULTIPLE_CHOICE.toString())) {
	  setMultipleCorrect(false);
	  setMultipleCorrectString(TypeFacade.MULTIPLE_CHOICE.toString());
	  setItemType(TypeFacade.MULTIPLE_CHOICE.toString());
	}
	else {
	  setMultipleCorrect(true);
	  setMultipleCorrectString(TypeFacade.MULTIPLE_CORRECT.toString());
	  setItemType(TypeFacade.MULTIPLE_CORRECT.toString());
	}

  }
  public void addChoices(ValueChangeEvent event) {
        // build a default list of 4 choices, a, b, c, d,
	FacesContext context = FacesContext.getCurrentInstance();
	String newvalue = (String) event.getNewValue();
	ArrayList list = getMultipleChoiceAnswers(); // get existing list
	if (list!=null) {
		// add additional answer bean
		int currentsize = list.size();
        	int newlength = currentsize+ new Integer(newvalue).intValue();
        	for (int i=currentsize; i<newlength; i++){
                	AnswerBean answerbean = new AnswerBean();
              		answerbean.setSequence(new Long(i+1));
                	answerbean.setLabel(AnswerBean.choiceLabels[i]);
                	list.add(answerbean);

        	}
        }
	setMultipleChoiceAnswers(list);
	setAdditionalChoices("0");



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
                for (int i=currentsize; i<newlength; i++){
                        AnswerBean answerbean = new AnswerBean();
                        answerbean.setSequence(new Long(i+1));
                        answerbean.setLabel(AnswerBean.choiceLabels[i]);
                        list.add(answerbean);

                }
        }
        setMultipleChoiceAnswers(list);
        setAdditionalChoices("0");

        // if mcmc, need to set corrAnswers 
       if (getMultipleCorrect()) {
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


        return "multipleChoiceItem";
  }

  public String removeChoices() {
	String labelToRemove = ContextUtil.lookupParam("answerid");
        ArrayList list = getMultipleChoiceAnswers(); // get existing list
	Iterator iter = list.iterator();
	int currentindex = 0;
        if (list!=null) {
      	  while(iter.hasNext())
       	  {
		AnswerBean answerbean = (AnswerBean) iter.next();
		if (answerbean.getLabel().equals(labelToRemove)) {
		// delete selected choices
               		iter.remove();
                }
		else {
		currentindex=currentindex+1;
		// reset sequence and labels , shift the seq/labels after a choice is deleted
              		answerbean.setSequence(new Long(currentindex));
                	answerbean.setLabel(AnswerBean.choiceLabels[currentindex-1]);
		}
          }

        }
        setMultipleChoiceAnswers(list);

	return null;

  }


// Huong added for matching

    public boolean isMatchError(){
// need to trim, 'cuz in  mozilla, the field is printed as ^M , a new line char. 
	String choice=(currentMatchPair.getChoice().replaceAll("<.*?>", "")).trim();
	String match=(currentMatchPair.getMatch().replaceAll("<.*?>", "")).trim();
    
	if(choice==null ||choice.equals("")|| match==null || match.equals("")){
	    FacesContext context=FacesContext.getCurrentInstance();
	    ResourceBundle rb=ResourceBundle.getBundle("org.sakaiproject.tool.assessment.bundle.AuthorMessages", context.getViewRoot().getLocale());

	    context.addMessage(null,new FacesMessage((String)rb.getObject("match_error")));
	    return true;
	}
	return false;
    }

  public String addMatchPair() {
      if (!isMatchError()){

    // get existing list
    ArrayList list = getMatchItemBeanList();

    Iterator biter = this.getMatchItemBeanList().iterator();
    while(biter.hasNext())
    {
      MatchItemBean apair = (MatchItemBean) biter.next();
    }


    MatchItemBean currpair = this.getCurrentMatchPair();
    if (!currpair.getSequence().equals(new Long(-1))) {
      // for modify
      int seqno =  currpair.getSequence().intValue()-1;
      MatchItemBean newpair= (MatchItemBean)  this.getMatchItemBeanList().get(seqno);
      newpair.setSequence(currpair.getSequence());
      newpair.setChoice(currpair.getChoice());
      newpair.setMatch(currpair.getMatch());
      newpair.setCorrMatchFeedback(currpair.getCorrMatchFeedback());
      newpair.setIncorrMatchFeedback(currpair.getIncorrMatchFeedback());
      newpair.setIsCorrect(Boolean.TRUE);
    }
    else {
      // for new pair
      MatchItemBean newpair = new MatchItemBean();
      newpair.setChoice(currpair.getChoice());
      newpair.setMatch(currpair.getMatch());
      newpair.setCorrMatchFeedback(currpair.getCorrMatchFeedback());
      newpair.setIncorrMatchFeedback(currpair.getIncorrMatchFeedback());
      newpair.setIsCorrect(Boolean.TRUE);
      newpair.setSequence(new Long(list.size()+1));

    list.add(newpair);
    }



    this.setMatchItemBeanList(list); // get existing list


//debugging
    Iterator iter = list.iterator();
    while(iter.hasNext())
    {
      MatchItemBean apair = (MatchItemBean) iter.next();
    }

    MatchItemBean matchitem = new MatchItemBean();
    this.setCurrentMatchPair(matchitem);
      }
    return "matchingItem";
  }



  public String editMatchPair() {

	String seqnostr = ContextUtil.lookupParam("sequence");
     int seqno = new Integer(seqnostr).intValue()-1;
    // get currentmatchpair by sequence.

    MatchItemBean pairForEdit= (MatchItemBean) this.getMatchItemBeanList().get(seqno);
    this.setCurrentMatchPair(pairForEdit);
    return "matchingItem";
  }



  public String removeMatchPair() {

	String seqnostr = ContextUtil.lookupParam("sequence");
     int seqno = new Integer(seqnostr).intValue()-1;
    // get currentmatchpair by sequence.

    this.getMatchItemBeanList().remove(seqno);
    // shift seqno
    Iterator iter = this.getMatchItemBeanList().iterator();
    int i = 1;
    while(iter.hasNext())
    {
      MatchItemBean apair = (MatchItemBean) iter.next();
      apair.setSequence(new Long(i));
      i++;
    }

// debugging

    iter = this.getMatchItemBeanList().iterator();
    while(iter.hasNext())
    {
      MatchItemBean apair = (MatchItemBean) iter.next();
    }


    MatchItemBean matchitem = new MatchItemBean();
    this.setCurrentMatchPair(matchitem);
    return "matchingItem";
  }

  


/* not used

  public ArrayList getMultipleChoiceAnswerSelectList() {

	ArrayList list = getMultipleChoiceAnswers();
      Iterator iter = list.iterator();
      while(iter.hasNext())
      {
      SelectItem selection = new SelectItem();
	AnswerBean answerbean = (AnswerBean)iter.next();
      selection.setLabel(answerbean.getLabel());
      selection.setValue(answerbean.getLabel());
      list.add(selection);
    }

    return list;
  }

*/

   


}
