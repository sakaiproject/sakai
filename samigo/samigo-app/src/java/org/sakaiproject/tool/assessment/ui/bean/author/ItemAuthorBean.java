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
 *       http://www.osedu.org/licenses/ECL-2.0
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.context.ExternalContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.PublishedItemService;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;

import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;



//import org.osid.shared.*;


/**
 * Backing bean for Item Authoring, uses ItemBean for UI
 * $Id$
 */
public class ItemAuthorBean
  implements Serializable
{
  private static Log log = LogFactory.getLog(ItemAuthorBean.class);

  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 8266438770394956874L;

  public final static String FROM_QUESTIONPOOL= "questionpool";
  public final static String FROM_ASSESSMENT= "assessment";
  private String assessTitle;
  private String sectionIdent;
  private ArrayList assessmentSectionIdents;
  private String insertPosition;
  private String insertToSection;
  private String insertType;
  private String assessmentID;
  private String currentSection;
  private String itemId;
  private String itemNo;
  private String itemType;
  private String itemTypeString;  // used for inserting a question
  private String showMetadata;
  private String showOverallFeedback;
  private String showQuestionLevelFeedback;
  private String showSelectionLevelFeedback;
  private String showFeedbackAuthoring;
  private ArrayList trueFalseAnswerSelectList;
  private ItemDataIfc item;
  private ItemBean currentItem;
  private ItemFacade itemToDelete;
  private ItemFacade itemToPreview;
  private List attachmentList;

  // for questionpool
  private String qpoolId;
  private String target;
  private ArrayList poolListSelectItems;

  // for item editing

  private boolean[] choiceCorrectArray;
  private String maxRecordingTime;
  private String maxNumberRecordings;
  private String scaleName;
  private String[] matches;
  private String[] matchAnswers;
  private String[] matchFeedbackList;
  private String[] answerFeedbackList;

  // for navigation
  private String outcome;
  /**
   * Creates a new ItemAuthorBean object.
   */
  public ItemAuthorBean()
  {

  }

  public void setItem(ItemDataIfc item)
   {
     this.item=item;
     this.attachmentList = item.getItemAttachmentList();
   }

  public ItemDataIfc getItem()
  {
    return item;
  }

  public void setCurrentItem(ItemBean item)
   {
	this.currentItem=item;
   }

  public ItemBean getCurrentItem()
  {
    return currentItem;
  }

  /**
   * @return
   */
  public String getAssessTitle()
  {
    return assessTitle;
  }


  /**
   * @param string
   */
  public void setAssessTitle(String string)
  {
    assessTitle = string;
  }


  /**
   * @return
   */
  public String getSectionIdent()
  {
    return sectionIdent;
  }

  /**
   * @param string
   */
  public void setSectionIdent(String string)
  {
    sectionIdent = string;
  }

  /**
   * @return
   */
  public ArrayList getAssessmentSectionIdents()
  {
    return assessmentSectionIdents;
  }

  /**
   * @param list
   */
  public void setAssessmentSectionIdents(ArrayList list)
  {
    assessmentSectionIdents = list;
  }

  /**
   * @return
   */
  public String getInsertPosition()
  {
    return insertPosition;
  }

  /**
   * @param string
   */
  public void setInsertPosition(String string)
  {
    insertPosition = string;
  }
  /**
   * @return
   */
  public String getInsertToSection()
  {
    return insertToSection;
  }

  /**
   * @param string
   */
  public void setInsertToSection(String string)
  {
    insertToSection= string;
  }


  /**
   * @return
   */
  public String getInsertType()
  {
    return insertType;
  }

  /**
   * @param string
   */
  public void setInsertType(String string)
  {
    insertType= string;
  }

  /**
   * @return
   */
  public String getAssessmentID()
  {
    return assessmentID;
  }

  /**
   * @param string
   */
  public void setAssessmentID(String string)
  {
    assessmentID = string;
  }

  /**
   * @return
   */
  public String getCurrentSection()
  {
    return currentSection;
  }

  /**
   * @param string
   */
  public void setCurrentSection(String string)
  {
    currentSection = string;
  }
  /**
   * @return
   */
  public String getItemNo()
  {
    return itemNo;
  }


  /**
   * @param string
   */
  public void setItemNo(String string)
  {
    itemNo = string;
  }


  public String getItemId()
  {
    return itemId;
  }

  public void setItemId(String string)
  {
    itemId = string;
  }


  /**
   * @return
   */
  public String getItemType()
  {
    return itemType;
  }

  /**
   * @param string
   */
  public void setItemType(String string)
  {
    itemType = string;
  }

  /**
   * @return
   */
  public String getItemTypeString()
  {
    return itemTypeString;
  }

  /**
   * @param string
   */
  public void setItemTypeString(String string)
  {
    itemTypeString = string;
  }

  /**
   * @return
   */
  public String getShowMetadata()
  {
    return showMetadata;
  }

  /**
   * @param string
   */
  public void setShowMetadata(String string)
  {
    showMetadata = string;
  }

  /**
   * @return
   */
  public String getShowOverallFeedback()
  {
    return showOverallFeedback;
  }

  /**
   * @return
   */
  public String getShowQuestionLevelFeedback()
  {
    return showQuestionLevelFeedback;
  }

  /**
   * @return
   */
  public String getShowSelectionLevelFeedback()
  {
    return showSelectionLevelFeedback;
  }

  /**
   * @param string
   */
  public void setShowOverallFeedback(String string)
  {
    showOverallFeedback = string;
  }

  /**
   * @param string
   */
  public void setShowQuestionLevelFeedback(String string)
  {
    showQuestionLevelFeedback = string;
  }

  /**
   * @param string
   */
  public void setShowSelectionLevelFeedback(String string)
  {
    showSelectionLevelFeedback = string;
  }

  /**
   * @param string
   */
  public void setQpoolId(String string)
  {
    qpoolId= string;
  }

  /**
   * @return
   */
  public String getQpoolId()
  {
    return qpoolId;
  }


  /**
   * @param string
   */
  public void setItemToDelete(ItemFacade param)
  {
    itemToDelete= param;
  }

  /**
   * @return
   */
  public ItemFacade getItemToDelete()
  {
    return itemToDelete;
  }

  /**
   * @param string
   */
  public void setItemToPreview(ItemFacade param)
  {
    itemToPreview= param;
  }

  /**
   * @return
   */
  public ItemFacade getItemToPreview()
  {
    return itemToPreview;
  }

  /**
   * @param string
   */
  public void setTarget(String string)
  {
    target= string;
  }

  /**
   * @return
   */
  public String getTarget()
  {
    return target;
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

  public String getOutcome()
  {
    return outcome;
  }

  /**
   * set the survey scale
   * @param param
   */
  public void setOutcome(String param)
  {
    this.outcome= param;
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

  /**
   * Derived property.
   * @return ArrayList of model SelectItems
   */

  public ArrayList getTrueFalseAnswerSelectList() {
    ArrayList list = new ArrayList();

    String trueprop= ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","true_msg");
    String falseprop= ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","false_msg");
    String[] answerValues = {"true", "false"};  // not to be displayed in the UI
    String[] answerLabelText= {trueprop, falseprop};
    currentItem.setAnswers(answerValues);
    currentItem.setAnswerLabels(answerLabelText);

    for (int i = 0; i < answerValues.length; i++) {
      SelectItem selection = new SelectItem();
      selection.setLabel(answerLabelText[i]);
      selection.setValue(answerValues[i]);
      list.add(selection);
    }

    return list;
  }

   /* Derived property.
   * @return ArrayList of model SelectItems
   */

// TODO use sectionBean.getsectionNumberList when its ready

  public ArrayList getSectionSelectList() {
    ArrayList list = new ArrayList();
    
    ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AuthorMessages");
    AssessmentBean assessbean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
    ArrayList sectionSet = assessbean.getSections();
    Iterator iter = sectionSet.iterator();
    int i =0;
    while (iter.hasNext()){
      i = i + 1;
      SectionContentsBean part = (SectionContentsBean) iter.next();
      SelectItem selection = new SelectItem();

      // need to filter out all the random draw parts
      if (part.getSectionAuthorType().equals(SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL)) {
        // skip random draw parts, cannot add items to this part manually
      }
      else {
        if ("".equals(part.getTitle())) {
          selection.setLabel(rb.getString("p")+" "+ i );
        }
        else {
          selection.setLabel(rb.getString("p")+" " + i + " - " + FormattedText.convertFormattedTextToPlaintext(part.getTitle()));
        }
        selection.setValue(part.getSectionId());
        list.add(selection);
      }

    }


    Collections.reverse(list);
    // create a new part if there are no non-randomDraw parts available
    if (list.size() <1) {
      i = i + 1;
      SelectItem temppart = new SelectItem();
      temppart.setLabel(rb.getString("p")+" "+ i );
      temppart.setValue("-1");  // use -1 to indicate this is a temporary part. if the user decides to cancel the operation, this part will not be created
      list.add(temppart);
    } 
    

    return list;
  }

  /**
	 * Returns a generic Map of section options (for use by helpers that won't
	 * be in the same class loader and would thus get class cast issues from
	 * using SelectItem)
	 */
	public Map getSectionList() {
		Map items = new Hashtable();

		ResourceLoader rb = new ResourceLoader(
				"org.sakaiproject.tool.assessment.bundle.AuthorMessages");
		AssessmentBean assessbean = (AssessmentBean) ContextUtil
				.lookupBean("assessmentBean");
		ArrayList sectionSet = assessbean.getSections();
		Iterator iter = sectionSet.iterator();
		int i = 0;
		while (iter.hasNext()) {
			i = i + 1;
			SectionContentsBean part = (SectionContentsBean) iter.next();

			// need to filter out all the random draw parts
			if (part.getSectionAuthorType().equals(
					SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL)) {
				// skip random draw parts, cannot add items to this part
				// manually
			} else {
				if ("".equals(part.getTitle())) {
					items.put(rb.getString("p") + " " + i, part.getSectionId());
				} else {
					items.put(rb.getString("p") + " " + i + " - "
							+ part.getTitle(), part.getSectionId());
				}
			}
		}

		// create a new part if there are no non-randomDraw parts available
		if (items.size() < 1) {
			i = i + 1;
			items.put(rb.getString("p") + " " + i, "-1"); // use -1 to
															// indicate this is
															// a temporary part.
															// if the user
															// decides to cancel
															// the operation,
															// this part will
															// not be created
		}

		return items;
	}

  /**
	 * Derived property.
	 * 
	 * @return ArrayList of model SelectItems
	 */
  public ArrayList getPoolSelectList() {

    poolListSelectItems = new ArrayList();


    QuestionPoolService delegate = new QuestionPoolService();
    ArrayList qplist = delegate.getBasicInfoOfAllPools(AgentFacade.getAgentString());
    Iterator iter = qplist.iterator();

    try {
      while(iter.hasNext())
      {
        QuestionPoolFacade pool = (QuestionPoolFacade) iter.next();
        poolListSelectItems.add(new SelectItem((pool.getQuestionPoolId().toString()), FormattedText.convertFormattedTextToPlaintext(pool.getDisplayName())));

      }

    }
    catch (Exception e){
		throw new RuntimeException(e);
    }
    Collections.sort(poolListSelectItems, new itemComparator());
    return poolListSelectItems;
  }

  class itemComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			SelectItem i1 = (SelectItem) o1;
			SelectItem i2 = (SelectItem) o2;
			return i1.getLabel().compareToIgnoreCase(i2.getLabel());
		}
	}
  
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


///////////////////////////////////////////////////////////////////////////
//         ACTION METHODS
///////////////////////////////////////////////////////////////////////////


  public String[] getAnswerFeedbackList()
  {
    return answerFeedbackList;
  }


  public void setAnswerFeedbackList(String[] answerFeedbackList)
  {
    this.answerFeedbackList = answerFeedbackList;
  }


    public String doit() {
		if ("searchQuestionBank".equals(outcome)) {
			try {
				ExternalContext context = FacesContext.getCurrentInstance()
						.getExternalContext();
				context
						.redirect("sakai.questionbank.client.helper/authorIndex");
			} catch (Exception e) {
				log.error("fail to redirect to question bank: "
						+ e.getMessage());
			}
		}

		// navigation for ItemModifyListener
		return outcome;
	}

    /**
	 * Launch the print helper
	 */
	public String print() {

		try {
			AssessmentBean assessmentBean = (AssessmentBean) ContextUtil
					.lookupBean("assessmentBean");
			ToolSession currentToolSession = SessionManager
					.getCurrentToolSession();
			currentToolSession.setAttribute("QB_assessemnt_id", assessmentBean
					.getAssessmentId());
			ExternalContext context = FacesContext.getCurrentInstance()
					.getExternalContext();
			context
					.redirect("sakai.questionbank.printout.helper/printAssessment?assessmentId="
							+ assessmentBean.getAssessmentId()
							+ "&actionString=previewAssessment");
		} catch (Exception e) {
			log.error("fail to redirect to assessment print out: "
					+ e.getMessage());
		}

		return outcome;
	}

/**
 * delete specified Item
 */
  public String deleteItem() {
	  ItemService delegate = new ItemService();
	  Long deleteId= this.getItemToDelete().getItemId();
	  ItemFacade itemf = delegate.getItem(deleteId, AgentFacade.getAgentString());
	  // save the currSection before itemf.setSection(null), used to reorder question sequences
	  SectionFacade  currSection = (SectionFacade) itemf.getSection();
	  Integer  currSeq = itemf.getSequence();

	  QuestionPoolService qpdelegate = new QuestionPoolService();
	  if ((qpdelegate.getPoolIdsByItem(deleteId.toString()) ==  null) ||
           (qpdelegate.getPoolIdsByItem(deleteId.toString()).isEmpty() )){
		  // if no reference to this item at all, ie, this item is created in 
		  // assessment but not assigned to any pool
		  delegate.deleteItem(deleteId, AgentFacade.getAgentString());
	  }
	  else {
		  if (currSection == null) {
			  // if this item is created from question pool
			  QuestionPoolBean  qpoolbean= (QuestionPoolBean) ContextUtil.lookupBean("questionpool");
	          ItemFacade itemfacade= delegate.getItem(deleteId, AgentFacade.getAgentString());
	          ArrayList items = new ArrayList();
	          items.add(itemfacade);
	          qpoolbean.setItemsToDelete(items);
			  qpoolbean.removeQuestionsFromPool();
			  return "editPool";
		  }
		  else {
			  // 
			  // if some pools still reference to this item, ie, this item is 
			  // created in assessment but also assigned a a pool
			  // then just set section = null
			  itemf.setSection(null);
			  delegate.saveItem(itemf);
		  }
	  }
	  EventTrackingService.post(EventTrackingService.newEvent("sam.assessment.revise", "removed itemId=" + deleteId, true));
	  
	  AssessmentService assessdelegate = new AssessmentService();
      // reorder item numbers

	  SectionFacade sectfacade = assessdelegate.getSection(currSection.getSectionId().toString());
	  Set itemset = sectfacade.getItemFacadeSet();
	  // should be size-1 now.
	  Iterator iter = itemset.iterator();
	  while (iter.hasNext()) {
		  ItemFacade  itemfacade = (ItemFacade) iter.next();
		  Integer itemfacadeseq = itemfacade.getSequence();
		  if (itemfacadeseq.compareTo(currSeq) > 0 ){
			  itemfacade.setSequence( Integer.valueOf(itemfacadeseq.intValue()-1) );
			  delegate.saveItem(itemfacade);
		  }
	  }

	  //  go to editAssessment.jsp, need to first reset assessmentBean
	  AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean(
                                          "assessmentBean");
	  AssessmentFacade assessment = assessdelegate.getAssessment(assessmentBean.getAssessmentId());
	  assessmentBean.setAssessment(assessment);
	  assessdelegate.updateAssessmentLastModifiedInfo(assessment);

	  return "editAssessment";
  }


 public String confirmDeleteItem(){

        ItemService delegate = new ItemService();
        String itemId= ContextUtil.lookupParam("itemid");

        ItemFacade itemf = delegate.getItem( Long.valueOf(itemId), AgentFacade.getAgentString());
        setItemToDelete(itemf);

        return "removeQuestion";
  }


  public void selectItemType(ValueChangeEvent event) {

        //FacesContext context = FacesContext.getCurrentInstance();
        String type = (String) event.getNewValue();
          setItemType(type);

  }


  /**
   * @return
   */
  public String getShowFeedbackAuthoring()
  {
    return showFeedbackAuthoring;
  }

  /**
   * @param string
   */
  public void setShowFeedbackAuthoring(String string)
  {
    showFeedbackAuthoring= string;
  }

  public List getAttachmentList() {
    return attachmentList;
  }


  /**
   * @param list
   */
  public void setAttachmentList(List attachmentList)
  {
    this.attachmentList = attachmentList;
  }

  public boolean getHasAttachment(){
    if (attachmentList != null && attachmentList.size() >0)
      return true;
    else
      return false;    
  }

  public String addAttachmentsRedirect() {
    // 1. load resources into session for resources mgmt page
    //    then redirect to resources mgmt page
    try	{
      List filePickerList = prepareReferenceList(attachmentList);
      ToolSession currentToolSession = SessionManager.getCurrentToolSession();
      currentToolSession.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, filePickerList);
      ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
      context.redirect("sakai.filepicker.helper/tool");
    }
    catch(Exception e){
      log.error("fail to redirect to attachment page: " + e.getMessage());
    }
    return getOutcome();
  }

  /* called by SamigoJsfTool.java on exit from file picker */
  public void setItemAttachment(){
	AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
	boolean isEditPendingAssessmentFlow =  author.getIsEditPendingAssessmentFlow();
	ItemService service = null;
	if (isEditPendingAssessmentFlow) {
		service = new ItemService();
	}
	else {
		service = new PublishedItemService();
	}
    ItemDataIfc itemData = null;
    // itemId == null => new questiion
    if (this.itemId!=null){
      try{
        itemData = service.getItem(this.itemId);
      }
      catch(Exception e){
        log.warn(e.getMessage());
      }
    }

    // list returns contains modified list of attachments, i.e. new 
    // and old attachments. This list will be 
    // persisted to DB if user hit Save on the Item Modifying page.
    List list = prepareItemAttachment(itemData, isEditPendingAssessmentFlow);
    setAttachmentList(list);
  }

  private List prepareReferenceList(List attachmentList){
    List list = new ArrayList();
    if (attachmentList == null){
      return list;
    }
    for (int i=0; i<attachmentList.size(); i++){
      ContentResource cr = null;
      AttachmentIfc attach = (AttachmentIfc) attachmentList.get(i);
      try{
        log.debug("*** resourceId="+attach.getResourceId());
        cr = AssessmentService.getContentHostingService().getResource(attach.getResourceId());
      }
      catch (PermissionException e) {
    	  log.warn("ContentHostingService.getResource() throws PermissionException="+e.getMessage());
      }
      catch (IdUnusedException e) {
    	  log.warn("ContentHostingService.getResource() throws IdUnusedException="+e.getMessage());
          // <-- bad sign, some left over association of question and resource, 
          // use case: user remove resource in file picker, then exit modification without
          // proper cancellation by clicking at the left nav instead of "cancel".
          // Also in this use case, any added resource would be left orphan.
          AssessmentService assessmentService = new AssessmentService();
          assessmentService.removeItemAttachment(attach.getAttachmentId().toString());
      }
      catch (TypeException e) {
    	  log.warn("ContentHostingService.getResource() throws TypeException="+e.getMessage());
      }
      if (cr!=null){
    	Reference ref = EntityManager.newReference(cr.getReference());
        log.debug("*** ref="+ref);
        if (ref !=null ) list.add(ref);
      }
    }
    return list;
  }

  private List prepareItemAttachment(ItemDataIfc item, boolean isEditPendingAssessmentFlow){
    ToolSession session = SessionManager.getCurrentToolSession();
    if (session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) {

      Set attachmentSet = new HashSet();
      if (item != null){
        attachmentSet = item.getItemAttachmentSet();
      }
      HashMap map = getResourceIdHash(attachmentSet);
      ArrayList newAttachmentList = new ArrayList();
      
      AssessmentService assessmentService = new AssessmentService();
      String protocol = ContextUtil.getProtocol();

      List refs = (List)session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
      if (refs!=null && refs.size() > 0){
        Reference ref;

        for(int i=0; i<refs.size(); i++) {
          ref = (Reference) refs.get(i);
          String resourceId = ref.getId();
          if (map.get(resourceId) == null){
            // new attachment, add 
            log.debug("**** ref.Id="+ref.getId());
            log.debug("**** ref.name="+ref.getProperties().getProperty(
                       ref.getProperties().getNamePropDisplayName()));
            ItemAttachmentIfc newAttach = assessmentService.createItemAttachment(
                                          item,
                                          ref.getId(), ref.getProperties().getProperty(
                                                       ref.getProperties().getNamePropDisplayName()),
                                        protocol, isEditPendingAssessmentFlow);
            newAttachmentList.add(newAttach);
          }
          else{ 
            // attachment already exist, let's add it to new list and
	    // check it off from map
            newAttachmentList.add((ItemAttachmentIfc)map.get(resourceId));
            map.remove(resourceId);
          }
        }
      }

      session.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
      session.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
      return newAttachmentList;
    }
    else if (item == null) {
    	return new ArrayList();
    }
    else return item.getItemAttachmentList();
  }

  private HashMap getResourceIdHash(Set attachmentSet){
    HashMap map = new HashMap();
    if (attachmentSet !=null ){
      Iterator iter = attachmentSet.iterator();
      while (iter.hasNext()){
        ItemAttachmentIfc attach = (ItemAttachmentIfc) iter.next();
        map.put(attach.getResourceId(), attach);
      }
    }
    return map;
  }

  private HashMap resourceHash = new HashMap();
  public HashMap getResourceHash() {
      return resourceHash;
  }

  public void setResourceHash(HashMap resourceHash)
  {
      this.resourceHash = resourceHash;
  }

}
