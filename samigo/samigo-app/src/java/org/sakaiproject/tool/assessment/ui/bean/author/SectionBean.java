/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.listener.author.SavePartAttachmentListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class SectionBean implements Serializable
{

/** Use serialVersionUID for interoperability. */
private final static long serialVersionUID = 4216587136245498157L;
private String assessmentTitle;
private String assessmentId;
private String showMetadata;
private String sectionId;
private String noOfItems;
private String sectionTitle;
private String sectionDescription;
private List assessmentSectionIdents;
private List poolsAvailable;  // selectItems for pools
private List items;
private boolean random;
private String randomPartScore;
private String randomPartDiscount;
private String removeAllQuestions; // 1=Yes, 0=No
private SectionFacade section;
private AssessmentIfc assessment;
private String destSectionId; //destinated section where questions will be moved to
private String randomizationType;
private boolean pointValueHasOverrided;
private boolean discountValueHasOverrided;

private String numberSelected;
private String selectedPool;  // pool id for the item to be added to

private String objective;
private String keyword;
private String rubric;
private String type;
private String questionOrdering;

private boolean hideRandom = false;
private boolean hideOneByOne= false;

private String outcome;


private List attachmentList;

  public void setSection(SectionFacade section) {
    try {
      if (section == null) {
    	  this.section = null;
      }
      else {
    	  this.section = section;
    	  this.assessment = section.getAssessment();
    	  this.assessmentId = assessment.getAssessmentId().toString();
    	  this.assessmentTitle = assessment.getTitle();
    	  this.sectionId = section.getSectionId().toString();
    	  this.sectionTitle = section.getTitle();
    	  this.sectionDescription = section.getDescription();
    	  this.attachmentList = section.getSectionAttachmentList();
    	  if (this.attachmentList != null && this.attachmentList.size() >0)
    		  this.hasAttachment = true;
      }
    }
    catch (Exception ex) {
      log.warn("Unable to set section. Exception thrown from setSection(): " + ex.getMessage());
    }
  }

  public SectionFacade getSection(){
    return section;
  }

  public boolean getHideRandom()
  {
    return hideRandom;
  }

  public void setHideRandom(boolean param)
  {
    hideRandom = param;
  }


  public boolean getHideOneByOne()
  {
    return hideOneByOne;
  }

  public void setHideOneByOne(boolean param)
  {
    hideOneByOne= param;
  }


  /**
   * @return
   */
  public String getAssessmentId()
  {
    return assessmentId;
  }

  /**
   * @return
   */
  public String getAssessmentTitle()
  {
    return assessmentTitle;
  }

  /**
   * @param string
   */
  public void setAssessmentId(String string)
  {
    assessmentId = string;
  }

  /**
   * @param string
   */
  public void setAssessmentTitle(String string)
  {
    assessmentTitle = string;
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
  public String getSectionId()
  {
    return sectionId;
  }

  public String getSectionIdent()
  {
    return getSectionId();
  }

  /**
   * @param string
   */
  public void setSectionId(String string)
  {
    sectionId = string;
  }

  public void setSectionIdent(String string)
  {
    setSectionId(string);
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
   * Get a numerical sequence for all parts.
   * Derived property.
   * @return String[] in format "1", "2", "3"... up to the number of parts
   */
  public List getSectionNumberList()
  {
    List list = new ArrayList();

    if (assessmentSectionIdents==null) return list;

    for (int i = 0; i < assessmentSectionIdents.toArray().length; i++) {
      SelectItem selection = new SelectItem();
      selection.setLabel("" + i);
      selection.setValue("" + i);
      list.add(selection);
    }

    return list;
  }


  public List<SelectItem> getAuthorTypeList()
  {

    List<SelectItem> list = new ArrayList<SelectItem>();
    // cannot disable only one radio button in a list, so am generating the list again

    ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AuthorMessages");

    SelectItem selection = new SelectItem();
    selection.setLabel(rb.getString("type_onebyone"));
    selection.setValue("1");
    list.add(selection);
    
    SelectItem selection1 = new SelectItem();
    boolean disabled = false;
    String label = rb.getString("random_draw_from_que");
    
    if (hideRandom)
    {
        label += " " + rb.getString("random_draw_from_que_edit_disabled");
        disabled = true;
    }
    else if (getPoolsAvailable().isEmpty())
    {
        label += " " + rb.getString("randow_draw_from_que_no_pools_available");
        disabled = true;
    }
    
    selection1.setDisabled(disabled);
    selection1.setLabel(label);
    selection1.setValue("2");
    list.add(selection1);

    return list;
  }


  /**
   * Ordinal number of current section.
   * Derived property.
   * @return String the number as a String, e.g. "3"
   */
  public String getSelectedSection(){
    return "" + assessmentSectionIdents.indexOf(sectionId);
  }

  public int getTotalSections()
  {
    return assessmentSectionIdents.size();
  }

  /**List of available question pools for random draw. 
   * returns a list of pools that have not been used by other random drawn parts 
   * @return ArrayList of QuestionPoolFacade objects
   */
  public List getPoolsAvailable()
  {
    List resultPoolList= new ArrayList();

    AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");

    QuestionPoolService delegate = new QuestionPoolService();
    
    String agentId = AgentFacade.getAgentString();
    
    List allpoollist = delegate.getBasicInfoOfAllPools(agentId);

    Map allPoolsMap= new HashMap();
    for (int i=0; i<allpoollist.size();i++){
      QuestionPoolFacade apool = (QuestionPoolFacade) allpoollist.get(i);
      allPoolsMap.put(apool.getQuestionPoolId().toString(), apool);
    }

    AssessmentService assessdelegate = null;
	  AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
	  boolean isEditPendingAssessmentFlow =  author.getIsEditPendingAssessmentFlow();

    if (isEditPendingAssessmentFlow) {
    	assessdelegate = new AssessmentService();
    }
    else {
    	assessdelegate = new PublishedAssessmentService();
    }

    
    List sectionList = assessmentBean.getSectionList();
    for (int i=0; i<sectionList.size();i++){
      SelectItem s = (SelectItem) sectionList.get(i);

      // need to remove the pools already used by random draw parts

      SectionDataIfc section= assessdelegate.getSection(s.getValue().toString());
      if( (section !=null) && (section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE)!=null) &&
 (section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE).equals(SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString()))) {
	String poolid = section.getSectionMetaDataByLabel(SectionDataIfc.POOLID_FOR_RANDOM_DRAW);
	if (allPoolsMap.containsKey(poolid) ) {
	  allPoolsMap.remove(poolid);
	}
      }
    }
    
    // SAM-2463: Fetch the count of questions for each pool in one query instead of hundreds
    Map<Long, Integer> poolQuestionCounts = delegate.getCountItemsForUser(agentId);

    Iterator pooliter = allPoolsMap.keySet().iterator();
    while (pooliter.hasNext()) {
      QuestionPoolFacade pool = (QuestionPoolFacade) allPoolsMap.get(pooliter.next());
      Long poolId = pool.getQuestionPoolId();
      int items = poolQuestionCounts.containsKey(poolId) ? poolQuestionCounts.get(poolId) : 0;
      if(items>0){
    	  resultPoolList.add(new SelectItem((poolId.toString()), getPoolTitleValueForRandomDrawDropDown(pool, items, allpoollist, delegate)));
      }
    }
    //  add pool which is currently used in current Part for modify part
    if (!("".equals(this.getSelectedPool())) && (this.getSelectedPool() !=null)){

    //now we need to get the poolid and displayName
     
	QuestionPoolFacade currPool= delegate.getPool(new Long(this.getSelectedPool()), AgentFacade.getAgentString());
    // now add the current pool used  to the list, so it's available in the pulldown 
        if (currPool!=null) {
          // if the pool still exists, it's possible that the pool has been deleted  
          int currItems = delegate.getCountItems(currPool.getQuestionPoolId());
          if(currItems>0){
              resultPoolList.add(new SelectItem((currPool.getQuestionPoolId().toString()), getPoolTitleValueForRandomDrawDropDown(currPool, currItems, allpoollist, delegate)));  
          }
        }
        else {
          // the pool has been deleted, 
        } 
    }

    Collections.sort(resultPoolList, new ItemComparator());
    return resultPoolList;
  }
  
  /**
   * Determine the correct string to display in the drop down for 'Random draw from question pool' option.
   * Format is:  [parent pool name (# of questions)]: pool name (# of questions)
   * Where the parent pool name part is only displayed if the pool is a sub-pool.
   * 
   * This is important because subpool names are not unique across parent pools, which can result in 
   * multiple pools with the same name in the dropdown UI.
   * 
   * @author bjones86 - SAM-2269
   * 
   * @param pool
   * 			the question pool
   * @param items
   * 			the number of questions in this pool
   * @param allPools
   * 			a List of all pools in the database
   * @return a string to display in the UI's drop down for the given question pool
   */
  @SuppressWarnings("deprecation")
  private String getPoolTitleValueForRandomDrawDropDown(QuestionPoolFacade pool, int items, List<QuestionPoolFacade> allPools, QuestionPoolService qps) {
 
        // Build the string in the original format
        String original = formatPoolDisplayName( pool.getDisplayName(), items );

        // If the parent pool ID is greater than 0 (question pool IDs start at 1), return the string with the parent pool name prefixed
        Long parentPoolID = pool.getParentPoolId();
        if(parentPoolID > 0) {
            for(QuestionPoolFacade qp : allPools) {
                if(parentPoolID.equals(qp.getQuestionPoolId())) {
                    return qp.getDisplayName() + ": " + original;
                }
            }
        }

        // Otherwise, it has no parent or the parent wasn't found, return the original string
        return original;
  }
  
  private String formatPoolDisplayName(String poolName, int poolCount)
  {
      ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AuthorMessages");
      String qs = (poolCount == 1) ? rb.getString("q") : rb.getString("qs");
      return FormattedText.convertFormattedTextToPlaintext(poolName) + " (" + poolCount + " " + qs.toLowerCase() + ")";
  }
  
  public String getRandomDrawMsg()
  {
      ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AuthorMessages");
      String msg = rb.getString("random_draw_from_que");
      
      if (hideRandom)
      {
          msg += " " + rb.getString("random_draw_from_que_edit_disabled");
      }
      
      return msg;
  }

  class ItemComparator implements Comparator {
	  public int compare(Object o1, Object o2) {
		  SelectItem i1 = (SelectItem)o1;
		  SelectItem i2 = (SelectItem)o2;
			try{
				RuleBasedCollator r_collator= new RuleBasedCollator(((RuleBasedCollator)Collator.getInstance()).getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
				return r_collator.compare(i1.getLabel(), i2.getLabel());
			}catch(ParseException e){
				  return Collator.getInstance().compare(i1.getLabel(),i2.getLabel());
			}
	  }
  }

  /**List of available question pools.
   * @param list ArrayList of selectItems
   */
  public void setPoolsAvailable(List list)
  {
    poolsAvailable = list;
  }

  /**
   * @return
   */
  public String getNoOfItems()
  {
    return noOfItems;
  }

  /**
   * @param string
   */
  public void setNoOfItems(String string)
  {
    noOfItems = string;
  }

  /**
 * Get a numerical sequence for all questions.
 * Derived property.
 * @return String[] in format "1", "2", "3"... up to the number of questions
 */

  public List getItemNumberList(){
    List list = new ArrayList();

    for (int i = 0; i < items.toArray().length; i++) {
      SelectItem selection = new SelectItem();
      selection.setLabel("" + i);
      selection.setValue("" + i);
      list.add(selection);
    }

    return list;
  }

  /**
   * @return the title
   */
  public String getSectionTitle()
  {
    return this.sectionTitle;
  }

  /**
   * @param string the title
   */
  public void setSectionTitle(String string)
  {
    this.sectionTitle = string;
  }
  /**
   * @return the info
   */
  public String getSectionDescription()
  {
    return sectionDescription;
  }

  /**
   * @param string the info
   */
  public void setSectionDescription(String string)
  {
    sectionDescription = string;
  }
  /**
   * @return the number selected
   */
  public String getNumberSelected()
  {
    return numberSelected;
  }

  /**
   * @param string the number selected
   */
  public void setNumberSelected(String string)
  {
    numberSelected = string;
  }

  /**
   * randomize?
   * @return boolean
   */
  public boolean getRandom()
  {
    return random;
  }

  public List getItems()
  {
    return items;
  }

  /**
   * randomize?
   * @param bool boolean
   */
  public void setRandom(boolean bool)
  {
    random = bool;
  }

  public void setItems(List items)
  {
    this.items = items;
  }

  public String getRandomPartScore()
  {
    if (randomPartScore != null)
	return randomPartScore;

    if (section == null)
	return "";

    if (section.getSectionMetaDataByLabel(SectionDataIfc.POINT_VALUE_FOR_QUESTION) != null) {
    	return section.getSectionMetaDataByLabel(SectionDataIfc.POINT_VALUE_FOR_QUESTION);
    }
    else {
    	return "";
    }
  }

  public void setRandomPartScore(String score)
  {
    randomPartScore = score;
  }
  
  public String getRandomPartDiscount()
  {
    if (randomPartDiscount != null)
       return randomPartDiscount;

    if (section == null)
       return "0";

    if (section.getSectionMetaDataByLabel(SectionDataIfc.DISCOUNT_VALUE_FOR_QUESTION) != null) {
       return section.getSectionMetaDataByLabel(SectionDataIfc.DISCOUNT_VALUE_FOR_QUESTION);
    }
    else {
       return "0";
    }
  }

  public void setRandomPartDiscount(String discount)
  {
    randomPartDiscount = discount;
  }

  /**
   * If removing part, do questions go with it?
   * @return true if questions are deleted too.
   */
  public String getRemoveAllQuestions()
  {
    return removeAllQuestions;
  }

  /**
   * If removing part, do questions go with it?
   * @param removeAllQuestions
   */
  public void setRemoveAllQuestions(String removeAllQuestions)
  {
    this.removeAllQuestions = removeAllQuestions;
  }

  public String getDestSectionId()
  {
    return destSectionId;
  }

  /**
   * @param string the title
   */
  public void setDestSectionId(String destSectionId)
  {
    this.destSectionId = destSectionId;
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


   /**
   * get type
   */
  public String getType()
  {
    return type;
  }

  /**
   * set type
   * @param param
   */
  public void setType(String param)
  {
    this.type= param;
  }


   /**
   * get questionOrdering
   */
  public String getQuestionOrdering()
  {
    return questionOrdering;
  }

  /**
   * set questionOrdering
   * @param param
   */
  public void setQuestionOrdering(String param)
  {
    this.questionOrdering= param;
  }

  public void toggleAuthorType(ValueChangeEvent event) {

// need to update metadata in db.
        //FacesContext context = FacesContext.getCurrentInstance();
        String type = (String) event.getNewValue();


        if ((type == null) || type.equals(SectionDataIfc.QUESTIONS_AUTHORED_ONE_BY_ONE.toString())) {
          setType(SectionDataIfc.QUESTIONS_AUTHORED_ONE_BY_ONE.toString());
        }
        else if (type.equals(SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString())) {
          setType(SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString());
        }
        else {
	  // shouldn't go here.
        }

  }


   /**
   * get outcome
   */
  public String getOutcome()
  {
    return outcome;
  }

  /**
   * set outcome
   * @param param
   */
  public void setOutcome(String param)
  {
    this.outcome= param;
  }

  public List getAttachmentList() {
    return attachmentList;
  }

  public void setAttachmentList(List attachmentList)
  {
    this.attachmentList = attachmentList;
  }

  private boolean hasAttachment = false;
  public boolean getHasAttachment(){
    return this.hasAttachment;
  }

  public void setHasAttachment(boolean hasAttachment){
    this.hasAttachment = hasAttachment;
  }

  public String addAttachmentsRedirect() {
    // 1. then redirect to add attachment
    try	{
      List filePickerList = new ArrayList();
      if (attachmentList != null){
        filePickerList = prepareReferenceList(attachmentList);
      }
      log.debug("**filePicker list="+filePickerList.size());
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

  public void setPartAttachment(){
	  AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
	  boolean isEditPendingAssessmentFlow =  author.getIsEditPendingAssessmentFlow();
	  SavePartAttachmentListener lis = null;
	  if (isEditPendingAssessmentFlow) {	  
		  lis = new SavePartAttachmentListener(true);
	  }
	  else {
		  lis = new SavePartAttachmentListener(false);
	  }
	  lis.processAction(null);
  }

  private List prepareReferenceList(List attachmentList){
    List list = new ArrayList();
    for (int i=0; i<attachmentList.size(); i++){
      ContentResource cr = null;
      AttachmentIfc attach = (AttachmentIfc) attachmentList.get(i);
      try{
        cr = AssessmentService.getContentHostingService().getResource(attach.getResourceId());
      }
      catch (PermissionException e) {
    	  log.warn("PermissionException from ContentHostingService:"+e.getMessage());
      }
      catch (IdUnusedException e) {
    	  log.warn("IdUnusedException from ContentHostingService:"+e.getMessage());
          // <-- bad sign, some left over association of part and resource, 
          // use case: user remove resource in file picker, then exit modification without
          // proper cancellation by clicking at the left nav instead of "cancel".
          // Also in this use case, any added resource would be left orphan. 
    	  AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
    	  boolean isEditPendingAssessmentFlow =  author.getIsEditPendingAssessmentFlow();
    	  AssessmentService assessmentService = null;
    	  if (isEditPendingAssessmentFlow) {	  
    		  assessmentService = new AssessmentService();
    	  }
    	  else {
    		  assessmentService = new PublishedAssessmentService();
    	  }
          assessmentService.removeSectionAttachment(attach.getAttachmentId().toString());
      }
      catch (TypeException e) {
    	  log.warn("TypeException from ContentHostingService:"+e.getMessage());
      }
      if (cr!=null){
        if (this.resourceHash == null) this.resourceHash = new HashMap();
        this.resourceHash.put(attach.getResourceId(),cr);
        Reference ref = EntityManager.newReference(cr.getReference());
        if (ref !=null ) list.add(ref);
      }
    }
    return list;
  }

  private Map resourceHash = new HashMap();
  public Map getResourceHash() {
      return resourceHash;
  }

  public void setResourceHash(Map resourceHash)
  {
      this.resourceHash = resourceHash;
  }
  
  public List getRandomizationTypeList(){

	    List list = new ArrayList();

	    ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AuthorMessages");
	    
	    SelectItem selection = new SelectItem();
	    selection.setLabel(rb.getString("randomized_per_submission"));
	    selection.setValue("1");
	    list.add(selection);
	    
	    selection = new SelectItem();
	    selection.setLabel(rb.getString("randomized_per_student"));
	    selection.setValue("2");
	    list.add(selection);
	    
	    return list;
	  }

  public String getRandomizationType() {
      return randomizationType;
  }

  public void setRandomizationType(String randomizationType)
  {
      this.randomizationType = randomizationType;
  }
  
  public boolean getPointValueHasOverrided() {
      return pointValueHasOverrided;
  }

  public void setPointValueHasOverrided(boolean pointValueHasOverrided)
  {
      this.pointValueHasOverrided = pointValueHasOverrided;
  }
  
  public boolean getDiscountValueHasOverrided() {
      return discountValueHasOverrided;
  }

  public void setDiscountValueHasOverrided(boolean discountValueHasOverrided)
  {
      this.discountValueHasOverrided = discountValueHasOverrided;
  }
}
