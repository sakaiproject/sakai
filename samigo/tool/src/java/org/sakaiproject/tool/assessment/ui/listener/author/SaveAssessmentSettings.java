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

package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.EvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.assessment.SecuredIPAddress;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class SaveAssessmentSettings
{
  private static Log log = LogFactory.getLog(SaveAssessmentSettings.class);

  public AssessmentFacade save(AssessmentSettingsBean assessmentSettings)
  {
    // create an assessment based on the title entered and the assessment
    // template selected
    // #1 - set Assessment
    Long assessmentId = assessmentSettings.getAssessmentId();
    //log.info("**** save assessment assessmentId ="+assessmentId.toString());
    AssessmentService assessmentService = new AssessmentService();
    AssessmentFacade assessment = assessmentService.getAssessment(
        assessmentId.toString());
    //log.info("** assessment = "+assessment);
    assessment.setTitle(assessmentSettings.getTitle());
    assessment.setDescription(assessmentSettings.getDescription());
    assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.AUTHORS, assessmentSettings.getAuthors());

    // #2 - set AssessmentAccessControl
    AssessmentAccessControl control = (AssessmentAccessControl)assessment.getAssessmentAccessControl();
    if (control == null){
      control = new AssessmentAccessControl();
      // need to fix accessControl so it can take AssessmentFacade later
      control.setAssessmentBase(assessment.getData());
    }
    // a. LATER set dueDate, retractDate, startDate, releaseTo
    control.setStartDate(assessmentSettings.getStartDate());
    control.setDueDate(assessmentSettings.getDueDate());
    control.setRetractDate(assessmentSettings.getRetractDate());
    control.setFeedbackDate(assessmentSettings.getFeedbackDate());
    control.setReleaseTo(assessmentSettings.getReleaseTo());
    //log.info("control RELEASETO ="+control.getReleaseTo());
    //log.info("settings RELEASETO ="+assessmentSettings.getReleaseTo());

    // b. set Timed Assessment
    //log.info("** Time limit update to = "+assessmentSettings.getTimeLimit().intValue());
    control.setTimeLimit(assessmentSettings.getTimeLimit());
    if (assessmentSettings.getTimedAssessment())
      control.setTimedAssessment(AssessmentAccessControl.TIMED_ASSESSMENT);
    else
      control.setTimedAssessment(AssessmentAccessControl.DO_NOT_TIMED_ASSESSMENT);

    if (assessmentSettings.getAutoSubmit())
      control.setAutoSubmit(AssessmentAccessControl.AUTO_SUBMIT);
    else
      control.setAutoSubmit(AssessmentAccessControl.DO_NOT_AUTO_SUBMIT);

    // c. set Assessment Orgainzation
    if (assessmentSettings.getItemNavigation()!=null )
      control.setItemNavigation(new Integer(assessmentSettings.getItemNavigation()));
    if (assessmentSettings.getItemNumbering()!=null)
      control.setItemNumbering(new Integer(assessmentSettings.getItemNumbering()));
    if (assessmentSettings.getAssessmentFormat()!=null )
     control.setAssessmentFormat(new Integer(assessmentSettings.getAssessmentFormat()));

    // d. set Submissions
    if (assessmentSettings.getUnlimitedSubmissions()!=null){
      if (!assessmentSettings.getUnlimitedSubmissions().
          equals(AssessmentAccessControlIfc.UNLIMITED_SUBMISSIONS.toString())) {
        control.setUnlimitedSubmissions(Boolean.FALSE);
        if (assessmentSettings.getSubmissionsAllowed() != null)
          control.setSubmissionsAllowed(new Integer(assessmentSettings.
              getSubmissionsAllowed()));
        else
          control.setSubmissionsAllowed(new Integer("1"));
      }
      else {
        control.setUnlimitedSubmissions(Boolean.TRUE);
        control.setSubmissionsAllowed(null);
      }
    }
    //log.info("**unlimited submission="+assessmentSettings.getUnlimitedSubmissions());
    //log.info("**allowed="+control.getSubmissionsAllowed());

    if (assessmentSettings.getLateHandling()!=null){
      control.setLateHandling(new Integer(assessmentSettings.
                                                getLateHandling()));
    }
    if (assessmentSettings.getSubmissionsSaved()!=null){
      control.setSubmissionsSaved(new Integer(assessmentSettings.getSubmissionsSaved()));
    }
    assessment.setAssessmentAccessControl(control);

    // e. set Submission Messages
    control.setSubmissionMessage(assessmentSettings.getSubmissionMessage());
    // f. set username
    control.setUsername(assessmentSettings.getUsername());
    // g. set password
    control.setPassword(assessmentSettings.getPassword());
    // h. set finalPageUrl
    control.setFinalPageUrl(assessmentSettings.getFinalPageUrl());

    //#3 Feedback
    AssessmentFeedback feedback = (AssessmentFeedback)assessment.getAssessmentFeedback();
    if (feedback == null){
      feedback = new AssessmentFeedback();
      // need to fix feeback so it can take AssessmentFacade later
      feedback.setAssessmentBase(assessment.getData());
    }
    if (assessmentSettings.getFeedbackDelivery()!=null)
      feedback.setFeedbackDelivery(new Integer(assessmentSettings.getFeedbackDelivery()));
    feedback.setShowQuestionText(new Boolean(assessmentSettings.getShowQuestionText()));
    feedback.setShowStudentResponse(new Boolean(assessmentSettings.getShowStudentResponse()));
    feedback.setShowCorrectResponse(new Boolean(assessmentSettings.getShowCorrectResponse()));
    feedback.setShowStudentScore(new Boolean(assessmentSettings.getShowStudentScore()));
    feedback.setShowQuestionLevelFeedback(new Boolean(assessmentSettings.getShowQuestionLevelFeedback()));
    feedback.setShowSelectionLevelFeedback(new Boolean(assessmentSettings.getShowSelectionLevelFeedback()));
    feedback.setShowGraderComments(new Boolean(assessmentSettings.getShowGraderComments()));
    feedback.setShowStatistics(new Boolean(assessmentSettings.getShowStatistics()));
    assessment.setAssessmentFeedback(feedback);

    // g. set Grading
    EvaluationModel evaluation = (EvaluationModel) assessment.getEvaluationModel();
    if (evaluation == null){
      evaluation = new EvaluationModel();
      // need to fix evaluation so it can take AssessmentFacade later
      evaluation.setAssessmentBase(assessment.getData());
    }
    if (assessmentSettings.getAnonymousGrading()!=null)
      evaluation.setAnonymousGrading(new Integer(assessmentSettings.getAnonymousGrading()));
    evaluation.setToGradeBook(assessmentSettings.getToDefaultGradebook());
    if (assessmentSettings.getScoringType()!=null)
      evaluation.setScoringType(new Integer(assessmentSettings.getScoringType()));
    assessment.setEvaluationModel(evaluation);


    // h. update ValueMap: it contains value for teh checkboxes in
    // authorSettings.jsp for: hasAvailableDate, hasDueDate,
    // hasRetractDate, hasAnonymous, hasAuthenticatedUser, hasIpAddress,
    // hasUsernamePassword,
    // hasTimeAssessment,hasAutoSubmit, hasPartMetaData, hasQuestionMetaData
    HashMap h = assessmentSettings.getValueMap();
    updateMetaWithValueMap(assessment, h);

    // i. set Graphics
    assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.BGCOLOR, assessmentSettings.getBgColor());
    assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.BGIMAGE, assessmentSettings.getBgImage());

    // j. set objectives,rubrics,keywords
    assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.KEYWORDS, assessmentSettings.getKeywords());
    assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.OBJECTIVES,assessmentSettings.getObjectives());
    assessment.updateAssessmentMetaData(AssessmentMetaDataIfc.RUBRICS, assessmentSettings.getRubrics());

    // k. set ipAddresses
    HashSet ipSet = new HashSet();
    String ipAddresses = assessmentSettings.getIpAddresses();
    if (ipAddresses == null)
      ipAddresses = "";
    String[] ip = ipAddresses.split("\\n");
    for (int j=0; j<ip.length;j++){
      if (ip[j]!=null)
        ipSet.add(new SecuredIPAddress(assessment.getData(),null,ip[j]));
    }
    assessment.setSecuredIPAddressSet(ipSet);

    // l. FINALLY: save the assessment
    assessmentService.saveAssessment(assessment);
    //assessmentService.saveOrUpdate(template);
    return assessment;
  }


  private void updateMetaWithValueMap(AssessmentIfc assessment, HashMap map){
    HashMap metaMap = assessment.getAssessmentMetaDataMap();
    //log.info("** map size ="+map.size());
    if (map!=null && map.keySet()!=null){
        Iterator iter = map.keySet().iterator();
        // loop through our valueMap "can edit" & "hasXXX" properties
        while (iter.hasNext()) {
          // get label from metadata set
          String label = (String) iter.next();
          String value="";
          if (map.get(label)!=null){
            value = (String) map.get(label).toString();
            //log.info("get Label: " + label + ", Value: " + value);
          }
          assessment.updateAssessmentMetaData(label, value);
        }
    }
  }

    public boolean isUnique(String name){
	//if name=assessment name List return true else return false
        AssessmentService assessmentService = new AssessmentService();
	boolean returnValue=true;
      
	try{
	    ArrayList list = assessmentService.getBasicInfoOfAllActiveAssessments("title");
	    Iterator iter = list.iterator();
       
	    while (iter.hasNext()){
		AssessmentFacade facade =(AssessmentFacade) iter.next();
		String n=facade.getTitle();
               	
		if((name==null)||((name.trim()).equals(""))){                    
		    returnValue= false;
		    break;
		}
		else{
		    if(((name.trim()).equals(n.trim()))){
			returnValue=false; 
			break;
		    }
		   
		}
	    }
	}catch(Exception e){
    
	    e.printStackTrace();
	    throw new Error(e);
	}
	return returnValue;

    }

 public boolean isUniquePublished(String name){
	//if name=assessment name List return true else return false
        PublishedAssessmentService service = new PublishedAssessmentService();
       
	boolean returnValue=true;
      
	try{
	    ArrayList list = service.getAllActivePublishedAssessments("title");
	    Iterator iter = list.iterator();
       
	    while (iter.hasNext()){
		PublishedAssessmentFacade facade =(PublishedAssessmentFacade) iter.next();
		String n=facade.getTitle();
               	System.out.println("AssessmentName: "+n);
		if((name==null)||((name.trim()).equals(""))){                    
		    returnValue= false;
		    break;
		}
		else{
		    if(((name.trim()).equals(n.trim()))){
			returnValue=false; 
			break;
		    }
		   
		}
	    }
	}catch(Exception e){
    
	    e.printStackTrace();
	    throw new Error(e);
	}
	return returnValue;

    }

  public boolean isUnique(String id, String name){
        AssessmentService assessmentService = new AssessmentService();
	boolean returnValue=true;
        int count=0;
      
	try{
	      ArrayList list = assessmentService.getBasicInfoOfAllActiveAssessments("title");
	      // ArrayList list = assessmentService.getAllAssessments("title");
	    Iterator iter = list.iterator();
       
	    while (iter.hasNext()){
		AssessmentFacade facade =(AssessmentFacade) iter.next();
		String n=facade.getTitle();
		String i=String.valueOf(facade.getAssessmentBaseId());
		if((name==null)||((name.trim()).equals(""))){                    
		    returnValue= false;
		    break;
		}
		else{
		    if(((name.trim()).equals(n.trim()))){
                        if((i.equals(id))&& (count==0)){//itself 
			    count++; //itself
			}
			else{
			    returnValue=false;
			    break;
			}
		    }
		}	
		
	    }
	}catch(Exception e){
    
	    e.printStackTrace();
	    throw new Error(e);
	}
	return returnValue;

    }

}
