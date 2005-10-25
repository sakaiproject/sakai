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
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.AgentResults;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
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
    TotalScoresBean bean = (TotalScoresBean) cu.lookupBean("totalScores");

    // we probably want to change the poster to be consistent
    String publishedId = cu.lookupParam("publishedId");
    //log.info("Got publishedId " + publishedId);

    log.info("Calling totalScores.");
    if (!totalScores(publishedId, bean, false))
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

    // we probably want to change the poster to be consistent
    String publishedId = cu.lookupParam("publishedId");
    //log.info("Got publishedId " + publishedId);


    String selectedvalue= (String) event.getNewValue();
    if ((selectedvalue!=null) && (!selectedvalue.equals("")) ){
      if (event.getComponent().getId().indexOf("sectionpicker") >-1 ) 
      {
System.out.println("changed section picker");
        bean.setSelectedSectionFilterValue(selectedvalue);   // changed section pulldown
      }
      else 
      {
System.out.println("changed submission pulldown ");
        bean.setAllSubmissions(selectedvalue);    // changed submission pulldown
      }
    }


    log.info("Calling totalScores.");
    if (!totalScores(publishedId, bean, true))
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
    String publishedId, TotalScoresBean bean, boolean isValueChange)
  {
    log.debug("totalScores()");
    try
    {
      GradingService delegate =	new GradingService();

      if (cu.lookupParam("sortBy") != null &&
          !cu.lookupParam("sortBy").trim().equals(""))
        bean.setSortType(cu.lookupParam("sortBy"));



      String which = bean.getAllSubmissions();
      if (which == null)
      {
   	// coming from authorIndex.jsp
      	String allSubmissionTparam= cu.lookupParam("allSubmissionsT");
        if (allSubmissionTparam ==null) 
		which = "3";
	else 
		which = allSubmissionTparam;
      }


      // bean.setAllSubmissions(which);
      bean.setPublishedId(publishedId);

      // get available sections 

      String pulldownid = bean.getSelectedSectionFilterValue();


      ArrayList allscores = delegate.getTotalScores(publishedId, which);

      // now we need filter by sections selected 
      ArrayList scores = new ArrayList();  // filtered list
      Iterator allscores_iter = allscores.iterator();
      while (allscores_iter.hasNext())
      {
	AssessmentGradingData data = (AssessmentGradingData) allscores_iter.next();
        String agentid =  data.getAgentId();
        
	// get the Map of all users(keyed on userid) belong to the selected sections 
	Map useridMap= bean.getUserIdMap(); 

	// now we only include scores of users belong to the selected sections
        if (useridMap.containsKey(agentid) ) {
		scores.add(data);
	  }

      }
      

      Iterator iter = scores.iterator();
      //log.info("Has this many agents: " + scores.size());
      ArrayList agents = new ArrayList();

      if (!iter.hasNext())
      {
        // this section has no students
      bean.setAgents(agents);
      bean.setTotalPeople(new Integer(bean.getAgents().size()).toString());
      return true;
      }
      Object next = iter.next();
      Date dueDate = null;

      // Okay, here we get the first result set, which has a summary of
      // information and a pointer to the graded assessment we should be
      // displaying.  We get the graded assessment.
      AssessmentGradingData data = (AssessmentGradingData) next;

      if (data.getPublishedAssessment() != null)
      {
        bean.setAssessmentName(data.getPublishedAssessment().getTitle());
// get assessmentSettings and call bean.setScoringOption()
        Integer scoringoption = data.getPublishedAssessment().getEvaluationModel().getScoringType(); 
	bean.setScoringOption(scoringoption.toString());

        // if section set is null, initialize it - daisyf , 01/31/05
        PublishedAssessmentData pub = (PublishedAssessmentData)data.getPublishedAssessment();
        HashSet sectionSet = PersistenceService.getInstance().
            getPublishedAssessmentFacadeQueries().getSectionSetForAssessment(pub);
        data.getPublishedAssessment().setSectionSet(sectionSet);

        // Set first item for question scores.  This can be complicated.
        // It's important because it simplifies Question Scores to do this
        // once and keep track of it -- the data is available here, and
        // not there.  If firstItem is "", there are no items with
        // answers, and the QuestionScores and Histograms pages don't
        // show.  This is a very weird case, but has to be handled.
        String firstitem = "";
        HashMap answeredItems = new HashMap();
        Iterator i2 = scores.iterator();
        while (i2.hasNext())
        {
          AssessmentGradingData agd = (AssessmentGradingData) i2.next();
          Iterator i3 = agd.getItemGradingSet().iterator();
          while (i3.hasNext())
          {
            ItemGradingData igd = (ItemGradingData) i3.next();
            answeredItems.put(igd.getPublishedItem().getItemId(), "true");
          }
        }
        bean.setAnsweredItems(answeredItems); // Save for QuestionScores

        boolean foundid = false;
	boolean hasRandompart = false;

        i2 = data.getPublishedAssessment().getSectionArraySorted().iterator();
        while (i2.hasNext() && !foundid)
        {
          SectionDataIfc sdata = (SectionDataIfc) i2.next();
          String authortype = sdata.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE);
          if (SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.equals(new Integer(authortype))){
            hasRandompart = true;
          }

          Iterator i3 = sdata.getItemArraySortedForGrading().iterator();
          while (i3.hasNext() && !foundid)
          {
            ItemDataIfc idata = (ItemDataIfc) i3.next();
            if (answeredItems.get(idata.getItemId()) != null)
            {
              bean.setFirstItem(idata.getItemId().toString());
              foundid = true;
            }
          }
        }

        bean.setHasRandomDrawPart(hasRandompart);
        //log.info("Rachel: Setting first item to " +
        //  bean.getFirstItem());

        try {
          bean.setAnonymous((data.getPublishedAssessment().getEvaluationModel().getAnonymousGrading().equals(EvaluationModel.ANONYMOUS_GRADING)?"true":"false"));
          //log.info("Set anonymous = " + bean.getAnonymous());
        } catch (Exception e) {
          //log.info("No evaluation model");
          bean.setAnonymous("false");
        }
        try {
          bean.setLateHandling(data.getPublishedAssessment().getAssessmentAccessControl().getLateHandling().toString());
        } catch (Exception e) {
          //log.info("No access control model.");
          bean.setLateHandling(AssessmentAccessControl.NOT_ACCEPT_LATE_SUBMISSION.toString());
        }
        try {
          bean.setDueDate(data.getPublishedAssessment().getAssessmentAccessControl().getDueDate().toString());
          dueDate = data.getPublishedAssessment().getAssessmentAccessControl().getDueDate();
        } catch (Exception e) {
          //log.info("No due date.");
          bean.setDueDate("");
          dueDate = null;
        }
// need to change for random draw parts
        try {
          bean.setMaxScore(data.getPublishedAssessment().getEvaluationModel().getFixedTotalScore().toString());
        } catch (Exception e) {
          bean.setMaxScore(data.getPublishedAssessment().getTotalScore().toString());
        }
      }

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

      // Collect a list of all the users in the scores list
      ArrayList agentUserIds = new ArrayList();
      //int z = 0;
      //while(z++ < 5000) {
      iter = scores.iterator();
      while (iter.hasNext())
      {
          AssessmentGradingData gdata = (AssessmentGradingData) iter.next();
          agentUserIds.add(gdata.getAgentId());
      }
      //}
      AgentHelper helper = IntegrationContextFactory.getInstance().getAgentHelper();
      Map userRoles = helper.getUserRolesFromContextRealm(agentUserIds);
      
      /* Dump the grading and agent information into AgentResults */
      // ArrayList agents = new ArrayList();
      iter = scores.iterator();
      while (iter.hasNext())
      {
        AgentResults results = new AgentResults();
        AssessmentGradingData gdata = (AssessmentGradingData) iter.next();
        BeanUtils.copyProperties(results, gdata);

        results.setAssessmentGradingId(gdata.getAssessmentGradingId());
        results.setTotalAutoScore(gdata.getTotalAutoScore().toString());
        results.setTotalOverrideScore(gdata.getTotalOverrideScore().toString());
        results.setFinalScore(gdata.getFinalScore().toString());
        results.setComments(gdata.getComments());

        int graded=0;
        Iterator i3 = gdata.getItemGradingSet().iterator();
          while (i3.hasNext())
          {
            ItemGradingData igd = (ItemGradingData) i3.next();
            if (igd.getAutoScore() != null)
		graded++;
          }

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

      //log.info("Sort type is " + bean.getSortType() + ".");
      bs = new BeanSort(agents, bean.getSortType());
      if (
        (bean.getSortType()).equals("assessmentGradingId") ||
        (bean.getSortType()).equals("totalAutoScore") ||
        (bean.getSortType()).equals("totalOverrideScore") ||
        (bean.getSortType()).equals("finalScore"))
      {
        bs.toNumericSort();
      } else {
        bs.toStringSort();
      }


      //bs.sort();
      //log.info("Listing agents.");
      bean.setAgents(agents);
      bean.setTotalPeople(new Integer(bean.getAgents().size()).toString());
    }

    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }

    return true;
  }

}
