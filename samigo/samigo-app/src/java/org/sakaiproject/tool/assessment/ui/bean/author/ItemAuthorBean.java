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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTagIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.facade.TypeFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.PublishedItemService;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.MimeTypesLocator;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;

/**
 * Backing bean for Item Authoring, uses ItemBean for UI
 * $Id$
 */
@Slf4j
public class ItemAuthorBean
  implements Serializable
{

  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 8266438770394956874L;

  private final static int MAX_DECIMAL_PLACES = 10;
  public final static String FROM_QUESTIONPOOL= "questionpool";
  public final static String FROM_ASSESSMENT= "assessment";
  private String assessTitle;
  private String sectionIdent;
  private List assessmentSectionIdents;
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
  private List trueFalseAnswerSelectList;
  private ItemDataIfc item;
  private ItemBean currentItem;
  private ItemFacade itemToDelete;
  private ItemFacade itemToPreview;
  private List attachmentList;
  private Set<ItemTagIfc> tagsList;
  private String tagsListToJson;
  private String tagsTempListToJson;
  private boolean deleteTagsAllowed;
  private boolean multiTagsSingleQuestion;
  private boolean multiTagsSingleQuestionCheck;

  private String showTagsStyle;


  private String language;

  public String getLanguage() {
    Locale loc = new ResourceLoader().getLocale();
    this.language = loc.getLanguage();
    return this.language;
  }


  // for questionpool
  private String qpoolId;
  private String target;
  private List poolListSelectItems;

  // for item editing

  private boolean[] choiceCorrectArray;
  private String maxRecordingTime;
  private String maxNumberRecordings;
  private String scaleName;
  private String[] matches;
  private String[] matchAnswers;
  private String[] matchFeedbackList;
  private String[] answerFeedbackList;
  private Boolean allowMinScore;
  // for navigation
  private String outcome;
  
  // for EMI attachments
  private AnswerBean currentAnswer;
  
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
     this.tagsList = item.getItemTagSet();
   }

    private String tagListToJsonString(Set<ItemTagIfc> tagsListToConvert){


      String tagsListToJson = "[";
      if (tagsListToConvert!=null) {
        Iterator<ItemTagIfc> i = tagsListToConvert.iterator();
        Boolean more = false;
        while (i.hasNext()) {
          if (more) {
            tagsListToJson += ",";
          }
          ItemTagIfc tagToShow = (ItemTagIfc) i.next();
          String tagId = tagToShow.getTagId();
          String tagLabel = tagToShow.getTagLabel();
          String tagCollectionName = tagToShow.getTagCollectionName();
          tagsListToJson += "{\"tagId\":\"" + tagId + "\",\"tagLabel\":\"" + tagLabel + "\",\"tagCollectionName\":\"" + tagCollectionName + "\"}";
          more = true;
        }
      }
      tagsListToJson += "]";
      return tagsListToJson;
    }


  public String getTagsListToJson(){
    return this.tagsListToJson;
    }

  public void setTagsListToJson(String tagsListToJson)
    {
        this.tagsListToJson = tagsListToJson;
    }

  public String getTagsTempListToJson() {
    return tagsTempListToJson;
  }

  public void setTagsTempListToJson(String tagsTempListToJson) {
    this.tagsTempListToJson = tagsTempListToJson;
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
  public List getAssessmentSectionIdents()
  {
    return assessmentSectionIdents;
  }

  /**
   * @param list
   */
  public void setAssessmentSectionIdents(List list)
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

  public List getTrueFalseAnswerSelectList() {
    List list = new ArrayList();

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

  public List<SelectItem> getDecimalPlaceList() {
	  List<SelectItem> options = new ArrayList<SelectItem>();
	  for (int i = 0; i <= MAX_DECIMAL_PLACES; i++) {
		  SelectItem item = new SelectItem(i+"", i+"", "");
		  options.add(item);
	  }
	  return options;
  }
// TODO use sectionBean.getsectionNumberList when its ready

  public List getSectionSelectList() {
    List list = new ArrayList();
    
    ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AuthorMessages");
    AssessmentBean assessbean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
    List<SectionContentsBean> sectionSet = assessbean.getSections();
    Iterator<SectionContentsBean> iter = sectionSet.iterator();
    int i =0;
    while (iter.hasNext()){
      i = i + 1;
      SectionContentsBean part = iter.next();
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

  public List getSelectRelativeWidthList() {
	  List<SelectItem> list = new ArrayList();
	  ResourceLoader rb = new ResourceLoader(
		"org.sakaiproject.tool.assessment.bundle.AuthorMessages");
	  
	  final String[] widthLists = {
			  rb.getString("matrix_width_list_default"),
			  rb.getString("matrix_width_list_1"),
			  rb.getString("matrix_width_list_2"),
			  rb.getString("matrix_width_list_3"),
			  rb.getString("matrix_width_list_4"),
			  rb.getString("matrix_width_list_5"),
			  rb.getString("matrix_width_list_6"),
			  rb.getString("matrix_width_list_7"),
			  rb.getString("matrix_width_list_8"),
			  rb.getString("matrix_width_list_9")};
	  
	  for (int i=0; i<widthLists.length;i++)
	  {
		  SelectItem selectItem = new SelectItem();
		  selectItem.setLabel(widthLists[i]);
		  selectItem.setValue(Integer.toString(i));
		  list.add(selectItem);
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
		List<SectionContentsBean> sectionSet = assessbean.getSections();
		Iterator<SectionContentsBean> iter = sectionSet.iterator();
		int i = 0;
		while (iter.hasNext()) {
			i = i + 1;
			SectionContentsBean part = iter.next();

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
  public List getPoolSelectList() {

    poolListSelectItems = new ArrayList();


    QuestionPoolService delegate = new QuestionPoolService();
    List<QuestionPoolFacade> qplist = delegate.getBasicInfoOfAllPools(AgentFacade.getAgentString());
    Iterator<QuestionPoolFacade> iter = qplist.iterator();

    try {
      while(iter.hasNext())
      {
        QuestionPoolFacade pool = (QuestionPoolFacade) iter.next();
        
        // SAM-2269 - if the parent pool ID is greater than 0 (question pool IDs start at 1), get the parent pool
        Long parentPoolID = pool.getParentPoolId();
        QuestionPoolFacade parent = null;
        if (parentPoolID > 0) {
            for (QuestionPoolFacade qp : qplist) {
                if (parentPoolID.equals(qp.getQuestionPoolId())) {
                    parent = qp;
                    break;
                }
            }
        }
        
        // SAM-2269 - add the appropriate string to the list
        String original = pool.getDisplayName() + " (" + delegate.getCountItems(pool.getQuestionPoolId()) + ")";
        if (parent != null) {
            poolListSelectItems.add(new SelectItem(pool.getQuestionPoolId().toString(), FormattedText.convertFormattedTextToPlaintext(parent.getDisplayName() + ": " + original)));
        } else {
            poolListSelectItems.add(new SelectItem(pool.getQuestionPoolId().toString(), FormattedText.convertFormattedTextToPlaintext(original)));
        }
      }

    }
    catch (Exception e){
		throw new RuntimeException(e);
    }
    Collections.sort(poolListSelectItems, new ItemComparator());
    return poolListSelectItems;
  }

  class ItemComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			SelectItem i1 = (SelectItem) o1;
			SelectItem i2 = (SelectItem) o2;
			RuleBasedCollator collator_ini = (RuleBasedCollator)Collator.getInstance();
			try {
				RuleBasedCollator collator= new RuleBasedCollator(collator_ini.getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
				return collator.compare(i1.getLabel(), i2.getLabel());
			} catch (ParseException e) {}
			return Collator.getInstance().compare(i1.getLabel(), i2.getLabel());
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

  public Set<ItemTagIfc> getTagsList() {
    return tagsList;
  }


  /**
   * @param tagsList
   */
  public void setTagsList(Set<ItemTagIfc> tagsList)
  {
    this.tagsList = tagsList;
    setTagsListToJson(tagListToJsonString(tagsList));
  }

  public String getShowTagsStyle() {
    if (ServerConfigurationService.getBoolean("samigo.author.usetags", Boolean.FALSE)){
      return "";
    }else{
      return "display:none;";
    }
  }

  public void setShowTagsStyle(String showTagsStyle) {
    this.showTagsStyle = showTagsStyle;
  }



  public String addAttachmentsRedirect() {
    // 1. load resources into session for resources mgmt page
    //    then redirect to resources mgmt page
	
	// not EMI item (ItemText) attachment  
	setCurrentAnswer(null);
 
    try	{
      prepareMCcorrAnswers();
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

  private ItemService loadItemService(boolean isEditPendingAssessmentFlow) {
    if (isEditPendingAssessmentFlow) {
      return new ItemService();
    }
    else {
      return new PublishedItemService();
    }
  }

  private ItemFacade loadItem(boolean isEditPendingAssessmentFlow) {
    ItemService service = loadItemService(isEditPendingAssessmentFlow);
    ItemFacade itemData = null;
    // itemId == null => new questiion
    if (this.itemId!=null){
      try{
        itemData = service.getItem(this.itemId);
      }
      catch(Exception e){
        log.warn(e.getMessage());
      }
    }
    return itemData;
  }
  
  /* called by SamigoJsfTool.java on exit from file picker */
  public void setItemAttachment(){
	AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
	boolean isEditPendingAssessmentFlow = author.getIsEditPendingAssessmentFlow();

    // list returns contains modified list of attachments, i.e. new 
    // and old attachments. This list will be 
    // persisted to DB if user hit Save on the Item Modifying page.
    List list = prepareItemAttachment(loadItem(isEditPendingAssessmentFlow), isEditPendingAssessmentFlow);
    setAttachmentList(list);
  }

  private List prepareReferenceList(List attachmentList){
    List list = new ArrayList();
    if (attachmentList == null){
      return list;
    }
    AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
    final ItemFacade item = loadItem(author.getIsEditPendingAssessmentFlow());
    boolean itemEdited = false;
    Iterator<AttachmentIfc> i = attachmentList.iterator();
    while ( i.hasNext() ) {
      ContentResource cr = null;
      AttachmentIfc attach = i.next();
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
          item.removeItemAttachmentById(attach.getAttachmentId());
          i.remove();
          itemEdited = true;
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
    if ( itemEdited ) {
      final ItemService itemService = loadItemService(author.getIsEditPendingAssessmentFlow());
      itemService.saveItem(item);
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
      Map map = getResourceIdHash(attachmentSet);
      List newAttachmentList = new ArrayList();
      
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

  private Map getResourceIdHash(Set attachmentSet){
    Map map = new HashMap();
    if (attachmentSet !=null ){
      Iterator iter = attachmentSet.iterator();
      while (iter.hasNext()){
        ItemAttachmentIfc attach = (ItemAttachmentIfc) iter.next();
        map.put(attach.getResourceId(), attach);
      }
    }
    return map;
  }

  private Map resourceHash = new HashMap();
  public Map getResourceHash() {
      return resourceHash;
  }

  public void setResourceHash(Map resourceHash)
  {
      this.resourceHash = resourceHash;
  }
  
  private void prepareMCcorrAnswers() {
	  if (Long.valueOf(currentItem.getItemType()).equals(TypeFacade.MULTIPLE_CORRECT) || Long.valueOf(currentItem.getItemType()).equals(TypeFacade.MULTIPLE_CORRECT_SINGLE_SELECTION)) {
		  List multipleChoiceAnswers = currentItem.getMultipleChoiceAnswers();
		  if (multipleChoiceAnswers == null) {
			  return;
		  }
		  int corrsize = multipleChoiceAnswers.size();
		  String[] corrChoices = new String[corrsize];
		  int counter=0;
		  boolean isCorrectChoice = false;
		  String label="";
		  ItemAddListener itemAddListener = new ItemAddListener();
		  for (int i = 0; i < corrsize; i++){
			  AnswerBean answerbean = (AnswerBean) multipleChoiceAnswers.get(i);
			  label = answerbean.getLabel();
			  isCorrectChoice = itemAddListener.isCorrectChoice(currentItem, label);
			  if(isCorrectChoice){
				  corrChoices[counter]=label;
				  counter++;
			  }
		  }
		  currentItem.setCorrAnswers(corrChoices);  
	  }
  }

  public static final String REFERENCE_ROOT = Entity.SEPARATOR + "samigoDocs";

  public String getPrivateCollection() {
		String collectionId = Entity.SEPARATOR + "private" + REFERENCE_ROOT + Entity.SEPARATOR + ToolManager.getCurrentPlacement().getContext() + Entity.SEPARATOR;
		
		try {
			AssessmentService.getContentHostingService().checkCollection(collectionId);
		}catch(IdUnusedException e){
			try {
				ResourcePropertiesEdit resourceProperties = AssessmentService.getContentHostingService().newResourceProperties();
				resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, ToolManager.getCurrentPlacement().getContext());
				resourceProperties.addProperty(ResourceProperties.PROP_HIDDEN_WITH_ACCESSIBLE_CONTENT, "true");
				
				ContentCollectionEdit edit = (ContentCollectionEdit)AssessmentService.getContentHostingService().addCollection(collectionId, resourceProperties);
				
				AssessmentService.getContentHostingService().setPubView(collectionId,true);
			}catch(Exception ee){
				log.warn(ee.getMessage());
			}
		}
		catch(Exception e){
			log.warn(e.getMessage());
		}

		try {
			if(!"true".equals(AssessmentService.getContentHostingService().getProperties(Entity.SEPARATOR + "private"+ Entity.SEPARATOR).get(ResourceProperties.PROP_HIDDEN_WITH_ACCESSIBLE_CONTENT)) || !AssessmentService.getContentHostingService().isPubView(collectionId))
			{
			
				ContentCollectionEdit edit = AssessmentService.getContentHostingService().editCollection(collectionId);
				ResourcePropertiesEdit resourceProperties = edit.getPropertiesEdit();
				resourceProperties.addProperty(ResourceProperties.PROP_HIDDEN_WITH_ACCESSIBLE_CONTENT, "true");
				edit.setPublicAccess();

				AssessmentService.getContentHostingService().commitCollection(edit);
				
			}
		}
		catch(Exception e){
			log.warn(e.getMessage());
		}
		
	    return  collectionId + "uploads" + Entity.SEPARATOR;
	  }
	  
	  /**
	   * This method is used by jsf/author/imageMapQuestion.jsp
	   *   <corejsf:upload
	   *     target="jsf/upload_tmp/assessment#{assessmentBean.assessmentId}/question#{itemauthor.currentItem.itemId}/#{person.eid}"
	   *     valueChangeListener="#{itemauthor.addImageToQuestion}" />
	   */
	  public void addImageToQuestion(javax.faces.event.ValueChangeEvent e)
	  {

	    String mediaLocation = (String) e.getNewValue();
	    
		if (!mediaIsValid()) {
			setOutcome(null);
	        return;
	    }
	    PersonBean person = (PersonBean) ContextUtil.lookupBean("person");
	    String agent = person.getId();

	    // 3. get the questionId (which is the PublishedItemData.itemId)
	    int questionIndex = mediaLocation.indexOf("question");
	    int agentIndex = mediaLocation.indexOf("/", questionIndex + 8);
	    int myfileIndex = mediaLocation.lastIndexOf("/");
	    //cwen
	    if(agentIndex < 0 )
	    {
	      agentIndex = mediaLocation.indexOf("\\", questionIndex + 8);
	    }
	    String questionId = mediaLocation.substring(questionIndex + 8, agentIndex);
	    log.debug("***3a. addImageToQuestion, questionId =" + questionId);
	    if (agent == null){
	      String agentId = mediaLocation.substring(agentIndex, myfileIndex -1);
	      log.debug("**** agentId="+agentId);
	      agent = agentId;
	    }
	    log.debug("***3b. addImageToQuestion, agent =" + agent);

	    saveMedia(agent, mediaLocation);
	    setOutcome(null);
	  }

	  public void saveMedia(String agent, String mediaLocation){
		try {
			
			SecurityService.pushAdvisor(new SecurityAdvisor() {
				public SecurityAdvice isAllowed(String userId, String function, String reference) {
					return SecurityAdvice.ALLOWED;
				}
			});
			File media = new File(mediaLocation);
			byte[] mediaByte = getMediaStream(mediaLocation);
			String mimeType = MimeTypesLocator.getInstance().getContentType(media);
			
			String fullname = media.getName().trim();
			String collectionId = getPrivateCollection();
			currentItem.setImageMapSrc("/access/content"+collectionId+fullname);
		
			ResourcePropertiesEdit resourceProperties = AssessmentService.getContentHostingService().newResourceProperties();
			resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, fullname);
			
			AssessmentService.getContentHostingService().addResource(collectionId+fullname, mimeType, mediaByte, resourceProperties, NotificationService.NOTI_NONE);
		}catch(Exception e)	{
			log.warn(e.getMessage(), e);
		}
		finally {
			SecurityService.popAdvisor();
		}
	  }
	   
	  private byte[] getMediaStream(String mediaLocation)
	  {
	    FileInputStream mediaStream = null;
	    FileInputStream mediaStream2 = null;
	    byte[] mediaByte = new byte[0];
	    try
	    {
	      int i;
	      int size = 0;
	      mediaStream = new FileInputStream(mediaLocation);
	      if (mediaStream != null)
	      {
	        while ( (i = mediaStream.read()) != -1)
	        {
	          size++;
	        }
	      }
	      mediaStream2 = new FileInputStream(mediaLocation);
	      mediaByte = new byte[size];
	      if (mediaStream2 != null) {
	    	  mediaStream2.read(mediaByte, 0, size);
	      }
	    }
	    catch (FileNotFoundException ex)
	    {
	      log.error("file not found=" + ex.getMessage());
	    }
	    catch (IOException ex)
	    {
	      log.error("io exception=" + ex.getMessage());
	    }
	    finally
	    {
	      if (mediaStream != null) {
	    	  try
	    	  {
	    		  mediaStream.close();
	    	  }
	    	  catch (IOException ex1)
	    	  {
	    		  log.warn(ex1.getMessage());
	    	  }
	      }
	    if (mediaStream2 != null) {
	  	  try
	  	  {
	  		  mediaStream2.close();
	  	  }
	  	  catch (IOException ex1)
	  	  {
	  		  log.warn(ex1.getMessage());
	  	  }
	    }
	  }
	    return mediaByte;
	  }

	  public boolean mediaIsValid()
	  {
	    boolean returnValue =true;
	    // check if file is too big
	    FacesContext context = FacesContext.getCurrentInstance();
	    ExternalContext external = context.getExternalContext();
	    Long fileSize = (Long)((ServletContext)external.getContext()).getAttribute("TEMP_FILEUPLOAD_SIZE");
	    Long maxSize = (Long)((ServletContext)external.getContext()).getAttribute("FILEUPLOAD_SIZE_MAX");

	    ((ServletContext)external.getContext()).removeAttribute("TEMP_FILEUPLOAD_SIZE");
	    if (fileSize!=null){
	      float fileSize_float = fileSize.floatValue()/1024;
	      int tmp = Math.round(fileSize_float * 10.0f);
	      fileSize_float = (float)tmp / 10.0f;
	      float maxSize_float = maxSize.floatValue()/1024;
	      int tmp0 = Math.round(maxSize_float * 10.0f);
	      maxSize_float = (float)tmp0 / 10.0f;

	      String err1=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "file_upload_error");
	      String err2=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "file_uploaded");
	      String err3=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "max_size_allowed");
	      String err4=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.DeliveryMessages", "upload_again");
	      String err = err2 + fileSize_float + err3 + maxSize_float + err4;
	      context.addMessage("file_upload_error",new FacesMessage(err1));
	      context.addMessage("file_upload_error",new FacesMessage(err));
	      returnValue = false;
	    }
	    return returnValue;
	  }
  
   public AnswerBean getCurrentAnswer() {
		return currentAnswer;
   }

  public void setCurrentAnswer(AnswerBean currentAnswer) {
	this.currentAnswer = currentAnswer;
  }
  public Boolean getAllowMinScore() {
	  if(allowMinScore == null){
		  allowMinScore = ServerConfigurationService.getBoolean("samigo.allowMinScore", Boolean.FALSE);
	  }
      log.debug("Allow min score: {}", allowMinScore);
	  return allowMinScore;
  }

  public void setAllowMinScore(Boolean allowMinScore) {
	  this.allowMinScore = allowMinScore;
  }

  public boolean getDeleteTagsAllowed() {
    AuthorizationBean authorizationBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
    if (authorizationBean.isSuperUser()) {
      return true;
    }else {
      return ServerConfigurationService.getBoolean("samigo.author.allowDeleteTags", true);
    }
  }

    public boolean getMultiTagsSingleQuestion() {
            return ServerConfigurationService.getBoolean("samigo.author.multitag.singlequestion", false);
    }

    public boolean getMultiTagsSingleQuestionCheck() {
        AuthorizationBean authorizationBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
        if (authorizationBean.isSuperUser()) {
            return true;
        }else {
            return (ServerConfigurationService.getBoolean("samigo.author.multitag.singlequestion.check", false) && !getMultiTagsSingleQuestion());
        }
    }

}
