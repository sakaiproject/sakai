/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.application.FacesMessage;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.business.entity.RecordingData;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.EvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAccessControl;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.AgentResults;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.HistogramScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.QuestionScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.evaluation.util.EvaluationListenerUtil;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.BeanSort;
import org.sakaiproject.tool.assessment.integration.helper.ifc.AgentHelper;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;


/**
 * <p>
 * This handles the selection of the Total Score entry page.
 *  </p>
 * <p>Description: Action Listener for Evaluation Total Score front door</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class TotalScoreListener
  implements ActionListener, ValueChangeListener
{
  private static Log log = LogFactory.getLog(TotalScoreListener.class);
  private static EvaluationListenerUtil util;
  private static BeanSort bs;
  private static ContextUtil cu;

  //private SectionAwareness sectionAwareness;
  // private List availableSections;

  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {

    log.info("TotalScore LISTENER.");

    DeliveryBean delivery = (DeliveryBean) cu.lookupBean("delivery");
    TotalScoresBean bean = (TotalScoresBean) cu.lookupBean("totalScores");

    // we probably want to change the poster to be consistent
    String publishedId = cu.lookupParam("publishedId");
    //log.info("Got publishedId " + publishedId);
    PublishedAssessmentService pubAssessmentService = new PublishedAssessmentService();
    PublishedAssessmentFacade pubAssessment = pubAssessmentService.
                                              getPublishedAssessment(publishedId);

    // reset scoringType based on evaluationModel,scoringType if coming from authorIndex     
    EvaluationModelIfc model = pubAssessment.getEvaluationModel();
    if (model != null && model.getScoringType()!=null)
      bean.setAllSubmissions(model.getScoringType().toString());
    else
      bean.setAllSubmissions(TotalScoresBean.LAST_SUBMISSION); 

   // checking for permission first
    FacesContext context = FacesContext.getCurrentInstance();
    AuthorBean author = (AuthorBean) cu.lookupBean("author");
    author.setOutcome("totalScores");
    if (!passAuthz(context, pubAssessment.getCreatedBy())){
      author.setOutcome("author");
      return;
    }

    // set action mode
    delivery.setActionString("gradeAssessment");

    log.info("Calling totalScores.");
    if (!totalScores(pubAssessment, bean, false))
    {
      //throw new RuntimeException("failed to call totalScores.");
    }

  }

  /**
   * Process a value change.
   */
  public void processValueChange(ValueChangeEvent event)
  {

    log.info("TotalScore CHANGE LISTENER.");
    TotalScoresBean bean = (TotalScoresBean) cu.lookupBean("totalScores");
    QuestionScoresBean questionbean = (QuestionScoresBean) cu.lookupBean("questionScores");
    HistogramScoresBean histobean = (HistogramScoresBean) cu.lookupBean("histogramScores");

    // we probably want to change the poster to be consistent
    String publishedId = cu.lookupParam("publishedId");
    PublishedAssessmentService pubAssessmentService = new PublishedAssessmentService();
    PublishedAssessmentFacade pubAssessment = pubAssessmentService.
                                              getPublishedAssessment(publishedId);

    String selectedvalue= (String) event.getNewValue();
    if ((selectedvalue!=null) && (!selectedvalue.equals("")) ){
      if (event.getComponent().getId().indexOf("sectionpicker") >-1 ) 
      {
        log.debug("changed section picker");
        bean.setSelectedSectionFilterValue(selectedvalue);   // changed section pulldown
      }
      else 
      {
        log.debug("changed submission pulldown ");
        bean.setAllSubmissions(selectedvalue);    // changed submission pulldown
      }
    }

    questionbean.setAllSubmissions(null);    // reset questionScores pulldown  
    histobean.setAllSubmissions(null);    // reset histogramScores pulldown  
    log.info("Calling totalScores.");
    if (!totalScores(pubAssessment, bean, true))
    {
      //throw new RuntimeException("failed to call totalScores.");
    }
  }

  /**
   * This will populate the TotalScoresBean with the data associated with the
   * particular versioned assessment based on the publishedId.
   *
   * @todo Some of this code will change when we move this to Hibernate persistence.
   * @param publishedId String
   * @param bean TotalScoresBean
   * @return boolean
   */
  public boolean totalScores(
    PublishedAssessmentFacade pubAssessment, TotalScoresBean bean, boolean isValueChange)
  {
    if (cu.lookupParam("sortBy") != null &&
	!cu.lookupParam("sortBy").trim().equals("")){
      bean.setSortType(cu.lookupParam("sortBy"));
    }

    log.debug("totalScores()");
    try
    {
      boolean firstTime = true;
      PublishedAssessmentData p = (PublishedAssessmentData)pubAssessment.getData();
      if ((bean.getPublishedId()).equals(p.getPublishedAssessmentId().toString()))
        firstTime = false;
      bean.setPublishedAssessment(p);

      //#1 - prepareAgentResultList prepare a list of AssesmentGradingData and set it as
      // bean.agents later in step #4
      // scores is a filtered list contains last AssessmentGradingData submitted for grade
      ArrayList scores = new ArrayList();  
      ArrayList students_not_submitted= new ArrayList();  
      Map useridMap= bean.getUserIdMap(); 
      prepareAgentResultList(bean, p, scores, students_not_submitted, useridMap);
      if (scores.size()==0) // no submission, return
        return true;

      // pass #1, proceed forward to prepare properties that set the link "Statistics"
      //#2 - the following methods are used to determine if the link "Statistics"
      // and "Questions" should be displayed in totalScore.jsp. Once set, they 
      // need not be executed everytime
      if (firstTime){
        // if section set is null, initialize it - daisyf , 01/31/05
        HashSet sectionSet = PersistenceService.getInstance().
                     getPublishedAssessmentFacadeQueries().getSectionSetForAssessment(p);
        p.setSectionSet(sectionSet);
        bean.setFirstItem(getFirstItem(p));
        bean.setHasRandomDrawPart(hasRandomPart(p));
      }
      if (firstTime || (isValueChange)){
        bean.setAnsweredItems(getAnsweredItems(scores, p)); // Save for QuestionScores
      }
      log.debug("**firstTime="+firstTime);
      log.debug("**isValueChange="+isValueChange);

      //#3 - Collect a list of all the users in the scores list
      ArrayList agentUserIds = getAgentIds(useridMap);
      AgentHelper helper = IntegrationContextFactory.getInstance().getAgentHelper();
      Map userRoles = helper.getUserRolesFromContextRealm(agentUserIds);

      //#4 - prepare agentResult list
      ArrayList agents = new ArrayList();
      prepareAgentResult(p, scores.iterator(), agents, userRoles);
      prepareNotSubmittedAgentResult(students_not_submitted.iterator(), agents, userRoles);
      bean.setAgents(agents);
      bean.setTotalPeople(new Integer(bean.getAgents().size()).toString());

      //#5 - set role & sort selection
      setRoleAndSortSelection(bean, agents);

      //#6 - this is for audio questions?
      //setRecordingData(bean);

    }

    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }

    return true;
  }


 public boolean passAuthz(FacesContext context, String ownerId){
    AuthorizationBean authzBean = (AuthorizationBean) cu.lookupBean("authorization");
    boolean hasPrivilege_any = authzBean.getGradeAnyAssessment();
    boolean hasPrivilege_own0 = authzBean.getGradeOwnAssessment();
    boolean hasPrivilege_own = (hasPrivilege_own0 && isOwner(ownerId));
    boolean hasPrivilege = (hasPrivilege_any || hasPrivilege_own);
    if (!hasPrivilege){
       String err=(String)cu.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "denied_grade_assessment_error");
       context.addMessage("authorIndexForm:grade_assessment_denied" ,new FacesMessage(err));
    }
    return hasPrivilege;
  }

  public boolean isOwner(String ownerId){
    boolean isOwner = false;
    String agentId = AgentFacade.getAgentString();
    isOwner = agentId.equals(ownerId);
    log.debug("***isOwner="+isOwner);
    return isOwner;
  }

  public Integer getScoringType(PublishedAssessmentData pub){
    Integer scoringType = EvaluationModelIfc.HIGHEST_SCORE;
    EvaluationModelIfc e = pub.getEvaluationModel();
    if ( e!=null ){
      scoringType = e.getScoringType();
    }
    return scoringType;
  }

  // Set first item for question scores.  This can be complicated.
  // It's important because it simplifies Question Scores to do this
  // once and keep track of it -- the data is available here, and
  // not there.  If firstItem is "", there are no items with
  // answers, and the QuestionScores and Histograms pages don't
  // show.  This is a very weird case, but has to be handled.
  /* daisy's  comment: Really? I don't really understand why but 
     I have rewritten this method so it is more efficient. The old method
     has trouble dealing with large class with large question set. */
  public HashMap getAnsweredItems(ArrayList scores, PublishedAssessmentData pub){
    HashMap answeredItems = new HashMap();
    HashMap h = new HashMap();

    // 0. build a Hashmap containing all the assessmentGradingId in the filtered list 
    for (int m=0; m<scores.size(); m++){
      AssessmentGradingData a = (AssessmentGradingData)scores.get(m);
      h.put(a.getAssessmentGradingId(),"");
    }

    // 1. get list of publishedItemId
    List list =PersistenceService.getInstance().
      getPublishedAssessmentFacadeQueries().getPublishedItemIds(pub.getPublishedAssessmentId());

    // 2. build a HashMap (Long publishedItemId, ArrayList assessmentGradingIds)
    HashMap itemIdHash = getPublishedItemIdHash(pub);

    // 3. go through each publishedItemId and get all the submission of 
    // assessmentGradingId for the item
    for (int i=0; i<list.size(); i++){
      Long itemId = (Long)list.get(i);
      log.debug("****publishedItemId"+itemId);
      ArrayList l = new ArrayList();
      Object o = itemIdHash.get(itemId);
      if (o != null) l = (ArrayList) o;
      // check if the assessmentGradingId submitted is among the filtered list
      for (int j=0; j<l.size(); j++){
        Long assessmentGradingId = (Long) l.get(j);
        log.debug("****assessmentGradingId"+assessmentGradingId);
        if (h.get(assessmentGradingId) != null){
          answeredItems.put(itemId, "true");
          break;    
	}
      } 
    }
    return answeredItems;
  }

  public boolean hasRandomPart(PublishedAssessmentData pub){
    return PersistenceService.getInstance().
          getPublishedAssessmentFacadeQueries().hasRandomPart(pub.getPublishedAssessmentId());
  }

  public String getFirstItem(PublishedAssessmentData pub){
    PublishedItemData item = PersistenceService.getInstance().
          getPublishedAssessmentFacadeQueries().getFirstPublishedItem(pub.getPublishedAssessmentId());
    if (item!=null)
      return item.getItemId().toString();
    else
      return "";
  }

  public void getFilteredList(TotalScoresBean bean, ArrayList allscores,ArrayList scores, 
                              ArrayList students_not_submitted, Map useridMap){
    // only do section filter if it's published to authenticated users
    if (bean.getReleaseToAnonymous()){
    // skip section filter if it's published to anonymous users
      scores.addAll(allscores);
    }
    else {
      // now we need filter by sections selected 
      ArrayList students_submitted= new ArrayList();  // arraylist of students submitted test
      Iterator allscores_iter = allscores.iterator();
      while (allscores_iter.hasNext())
      {
        AssessmentGradingData data = (AssessmentGradingData) allscores_iter.next();
        String agentid =  data.getAgentId();
        
        // get the Map of all users(keyed on userid) belong to the selected sections 
        // now we only include scores of users belong to the selected sections
        if (useridMap.containsKey(agentid) ) {
          scores.add(data); // daisyf: #1b - what is the min set of info needed for data?
          students_submitted.add(agentid);
        }
      }

      // now get the list of students that have not submitted for grades 
      Iterator useridIterator = useridMap.keySet().iterator(); 
      while (useridIterator.hasNext()) {
        String userid = (String) useridIterator.next(); 	
        if (!students_submitted.contains(userid)) {
          students_not_submitted.add(userid);
        }
      }
    }
  }

  public void prepareAgentResultList(TotalScoresBean bean, PublishedAssessmentData p,
                 ArrayList scores, ArrayList students_not_submitted, Map useridMap){ 

    // get available sections 
    String pulldownid = bean.getSelectedSectionFilterValue();

    // daisyf: #1a - place for optimization. all score contains full object of
    // AssessmentGradingData, do we need full?
    GradingService delegate = new GradingService();
    ArrayList allscores = delegate.getTotalScores(p.getPublishedAssessmentId().toString(), bean.getAllSubmissions());
    getFilteredList(bean, allscores, scores, students_not_submitted, useridMap);
    bean.setTotalPeople(scores.size()+"");
  }

  /* Dump the grading and agent information into AgentResults */
  // ArrayList agents = new ArrayList();
  public void prepareAgentResult(PublishedAssessmentData p, Iterator iter, ArrayList agents, Map userRoles){
    GradingService gradingService = new GradingService();
    while (iter.hasNext())
    {
      AgentResults results = new AgentResults();
      AssessmentGradingData gdata = (AssessmentGradingData) iter.next();
      gdata.setItemGradingSet(gradingService.getItemGradingSet(gdata.getAssessmentGradingId().toString()));
      try{
        BeanUtils.copyProperties(results, gdata);
      }
      catch(Exception e){
        log.warn(e.getMessage());
      }

      results.setAssessmentGradingId(gdata.getAssessmentGradingId());
      if(gdata.getTotalAutoScore() != null)
        results.setTotalAutoScore(gdata.getTotalAutoScore().toString());
      else
        results.setTotalAutoScore("0.0");
      results.setTotalOverrideScore(gdata.getTotalOverrideScore().toString());
      if(gdata.getFinalScore() != null)
        results.setFinalScore(gdata.getFinalScore().toString());
      else
        results.setFinalScore("0.0");
      results.setComments(gdata.getComments());

      int graded=0;
      Iterator i3 = gdata.getItemGradingSet().iterator();
      while (i3.hasNext())
      {
        ItemGradingData igd = (ItemGradingData) i3.next();
        if (igd.getAutoScore() != null)
          graded++;
      }
        
      Date dueDate = null;
      PublishedAccessControl ac = (PublishedAccessControl) p.getAssessmentAccessControl();
      if (ac!=null)
        dueDate = ac.getDueDate();
      if (dueDate == null || gdata.getSubmittedDate().before(dueDate)) {
        results.setIsLate(new Boolean(false));
        if (gdata.getItemGradingSet().size()==graded)
          results.setStatus(new Integer(2));
        else
          results.setStatus(new Integer(3));
      }
      else {
        results.setIsLate(new Boolean(true));
        results.setStatus(new Integer(4));
      }
      AgentFacade agent = new AgentFacade(gdata.getAgentId());
      //log.info("Rachel: agentid = " + gdata.getAgentId());
      results.setLastName(agent.getLastName());
      results.setFirstName(agent.getFirstName());
      if (results.getLastName() != null &&
        results.getLastName().length() > 0)
        results.setLastInitial(results.getLastName().substring(0,1));
      else if (results.getFirstName() != null &&
               results.getFirstName().length() > 0)
             results.setLastInitial(results.getFirstName().substring(0,1));
      else
        results.setLastInitial("Anonymous");
      results.setIdString(agent.getIdString());
      results.setRole((String)userRoles.get(gdata.getAgentId()));
      agents.add(results);
    }
  }

  public ArrayList getAgentIds(Map useridMap){
    ArrayList agentUserIds = new ArrayList();
    Iterator iter = useridMap.keySet().iterator();
    while (iter.hasNext())
    {
      String userid = (String)iter.next();
      agentUserIds.add(userid);
    }
    return agentUserIds;
  }


  public void setRoleAndSortSelection(TotalScoresBean bean, ArrayList agents){
    if (cu.lookupParam("roleSelection") != null)
    {
      bean.setRoleSelection(cu.lookupParam("roleSelection"));
    }

    if (bean.getSortType() == null)
    {
      if (bean.getAnonymous().equals("true"))
      {
        bean.setSortType("totalAutoScore");
      }
      else
      {
        bean.setSortType("lastName");
      }
    }
 
    String sortProperty = bean.getSortType();
    System.out.println("****Sort type is " + sortProperty);
    bs = new BeanSort(agents, sortProperty);

    if ((sortProperty).equals("lastName")) bs.toStringSort();
    if ((sortProperty).equals("idString")) bs.toStringSort();
    if ((sortProperty).equals("role")) bs.toStringSort();
    if ((sortProperty).equals("comments")) bs.toStringSort();
    if ((sortProperty).equals("submittedDate")) bs.toDateSort();
    if ((sortProperty).equals("assessmentGradingId")) bs.toNumericSort();
    if ((sortProperty).equals("status")) bs.toNumericSort();
    if ((sortProperty).equals("totalAutoScore")) bs.toNumericSort();
    if ((sortProperty).equals("totalOverrideScore")) bs.toNumericSort();
    if ((sortProperty).equals("finalScore")) bs.toNumericSort();

    agents = (ArrayList)bs.sort();
  }

    public void setRecordingData(TotalScoresBean bean){
      // recordingData encapsulates the inbeanation needed for recording.
      // set recording agent, agent assessmentId,
      // set course_assignment_context value
      // set max tries (0=unlimited), and 30 seconds max length
      String courseContext = bean.getAssessmentName() + " total ";
      // Note this is HTTP-centric right now, we can't use in Faces
      //      AuthoringHelper authoringHelper = new AuthoringHelper();
      //      authoringHelper.getRemoteUserID() needs servlet stuff
      //      authoringHelper.getRemoteUserName() needs servlet stuff

      String userId = "";
      String userName = "";
      RecordingData recordingData =
        new RecordingData( userId, userName,
        courseContext, "0", "30");
      // set this value in the requestMap for sound recorder
      bean.setRecordingData(recordingData);

    }

  //add those students that have not submitted scores, need to display them 
  // in the UI as well SAK-2234
  // students_not_submitted
  public void prepareNotSubmittedAgentResult(Iterator notsubmitted_iter,
                                             ArrayList agents, Map userRoles){
    while (notsubmitted_iter.hasNext()){
      String studentid = (String) notsubmitted_iter.next();
      AgentResults results = new AgentResults();
      AgentFacade agent = new AgentFacade(studentid);
      results.setLastName(agent.getLastName());
      results.setFirstName(agent.getFirstName());
      if (results.getLastName() != null &&
        results.getLastName().length() > 0)
      {
        results.setLastInitial(results.getLastName().substring(0,1));
      }
      else if (results.getFirstName() != null &&
               results.getFirstName().length() > 0)
      {
        results.setLastInitial(results.getFirstName().substring(0,1));
      }
      else
      {
        results.setLastInitial("Anonymous");
      }
      results.setIdString(agent.getIdString());
      results.setRole((String)userRoles.get(studentid));
      // use -1 to indicate this is an unsubmitted agent
      results.setAssessmentGradingId(new Long(-1));
      results.setTotalAutoScore("0");
      results.setTotalOverrideScore("0");
      results.setSubmittedDate(null);
      results.setFinalScore("0");
      results.setComments("");
      results.setStatus(new Integer(5));  //  no submission
      agents.add(results);
    }
  }

  // build a Hashmap (Long itemId, ArrayList assessmentGradingIds)
  // containing the last/highest item submission 
  // (regardless of users who submitted it) of a given published assessment
  public HashMap getPublishedItemIdHash(PublishedAssessmentData pub){
    HashMap publishedItemIdHash = new HashMap();
    Integer scoringType = getScoringType(pub);
    if ((scoringType).equals(EvaluationModelIfc.HIGHEST_SCORE)){
	publishedItemIdHash = PersistenceService.getInstance().
            getAssessmentGradingFacadeQueries().
            getHighestAssessmentGradingByPublishedItem(
            pub.getPublishedAssessmentId());
    }
    else{
	publishedItemIdHash = PersistenceService.getInstance().
            getAssessmentGradingFacadeQueries().
            getLastAssessmentGradingByPublishedItem(
            pub.getPublishedAssessmentId());
    }
    return publishedItemIdHash;
  }


}
