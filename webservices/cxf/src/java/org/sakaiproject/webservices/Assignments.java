/**
 * Copyright (c) 2005 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.webservices;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentContent;
import org.sakaiproject.assignment.api.AssignmentContentEdit;
import org.sakaiproject.assignment.api.AssignmentEdit;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentSubmission;
import org.sakaiproject.assignment.api.AssignmentSubmissionEdit;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@WebService
@SOAPBinding(style= SOAPBinding.Style.RPC, use= SOAPBinding.Use.LITERAL)
public class Assignments extends AbstractWebService {

	private static final Log LOG = LogFactory.getLog(Assignments.class);

    /** The maximum trial number to get an uniq assignment title in gradebook */
    private static final int MAXIMUM_ATTEMPTS_FOR_UNIQUENESS = 100;
    private static final String NEW_ASSIGNMENT_ADD_TO_GRADEBOOK = "new_assignment_add_to_gradebook";

    @WebMethod
    @Path("/getAssignmentsForContext")
    @Produces("text/plain")
    @GET
    public String getAssignmentsForContext(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "context", partName = "context") @QueryParam("context") String context) {

        try {
    		//establish the session
    		Session s = establishSession(sessionid);
    		
    		//ok will this give me a list of assignments for the course
    		LOG.info("assignment list requested for " + context);
    		
    		Iterator assignments = assignmentService.getAssignmentsForContext(context);
    		Document dom = Xml.createDocument();
    		Node all = dom.createElement("assignments");
    		dom.appendChild(all);
    		
    		while (assignments.hasNext()) {
    			Assignment thisA = (Assignment)assignments.next();
    			LOG.debug("got " + thisA.getTitle());
    			if (!thisA.getDraft()) {
    				AssignmentContent asCont = thisA.getContent();
    				
    				LOG.debug("about to start building xml doc");	
    				Element uElement = dom.createElement("assignment");
    				uElement.setAttribute("id", thisA.getId());
    				uElement.setAttribute("title", thisA.getTitle());
    				LOG.debug("added title and id");
    				if (asCont != null) 
    				{
    					Integer temp = new Integer(asCont.getTypeOfGrade());
    					String gType = temp.toString();
    					uElement.setAttribute("gradeType", gType);
    				}
    				
    				/* these need to be converted to strings
    				 */
    				
    				LOG.debug("About to get dates");
    				
    				Time dueTime = thisA.getDueTime();
    				Time openTime = thisA.getOpenTime();
    				Time closeTime = thisA.getCloseTime();
    				LOG.debug("got dates");
    				DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    				
    				if (openTime != null){
    					LOG.debug("open time is " + openTime.toString());
    					uElement.setAttribute("openTime", format.format(new Date(openTime.getTime())) );
    				}
    				if (closeTime != null) {
    					LOG.debug("close time is " + closeTime.toString());
    					uElement.setAttribute("closeTime", format.format(new Date(closeTime.getTime())) );
    				}
    				
    				if (dueTime != null) {
    					LOG.debug("due time is " + dueTime.toString());
    					uElement.setAttribute("dueTime", format.format(new Date(dueTime.getTime())) );
    				}
    				
    				LOG.debug("apending element to parent");
    				all.appendChild(uElement);
    			} else {
    				LOG.debug("this is a draft assignment");
    			}
    			
    		}
    		String retVal = Xml.writeDocumentToString(dom);
    		return retVal;
    	}
    	catch (Exception e) {
    		LOG.error("WS getAssignmentsForContext(): " + e.getClass().getName() + " : " + e.getMessage());
    	}
    	
    	return "<assignments/ >";
    }


    @WebMethod
    @Path("/getSubmissionsForAssignment")
    @Produces("text/plain")
    @GET
    public String getSubmissionsForAssignment(
            @WebParam(name = "sessionId", partName = "sessionId") @QueryParam("sessionId") String sessionId,
            @WebParam(name = "assignmentId", partName = "assignmentId") @QueryParam("assignmentId") String assignmentId) {
        try {
    		
    		Session s = establishSession(sessionId);
    		Assignment assign = assignmentService.getAssignment(assignmentId);
    		List subs = assignmentService.getSubmissions(assign);
    		
    		//build the xml
    		LOG.debug("about to start building xml doc");
    		Document dom = Xml.createDocument();
    		Node all = dom.createElement("submissions");
    		dom.appendChild(all);
    		
    		for (int i = 0; i < subs.size(); i++) {
    			
    			AssignmentSubmission thisSub = (AssignmentSubmission) subs.get(i);
    			LOG.debug("got submission" + thisSub);
    			Element uElement = dom.createElement("submission");
    			uElement.setAttribute("feedback-comment", thisSub.getFeedbackComment());
    			uElement.setAttribute("feedback-text", thisSub.getFeedbackText());
    			uElement.setAttribute("grade", thisSub.getGrade());
    			uElement.setAttribute("status", thisSub.getStatus());
    			uElement.setAttribute("submitted-text", thisSub.getSubmittedText());
    			List submitters = thisSub.getSubmitterIds();
    			for (int q = 0; q< submitters.size();q++) {
    				uElement.setAttribute("submitter-id", (String)submitters.get(q));
    			}
    			
    			List submissions = thisSub.getSubmittedAttachments();
    			//Element attachments = dom.createElement("attachment");
    			for (int q = 0; q< submissions.size();q++) {
    				//Element attachments = dom.createElement("attachment");
    				Reference ref = (Reference)submissions.get(q);
    				Entity ent = ref.getEntity();
    				uElement.setAttribute("attachment-url", ent.getUrl());
    				//all.appendChild();
    			}
    			
    			all.appendChild(uElement);
    				
    		}
    		String retVal = Xml.writeDocumentToString(dom);
    		return retVal;
    	}
    	catch (Exception e){
    		LOG.error("WS getSubmissionsForAssignment(): " + e.getClass().getName() + " : " + e.getMessage());
    	}	
    	
    	return "<submissions />";
    	
    }

    @WebMethod
    @Path("/setAssignmentGradeCommentforUser")
    @Produces("text/plain")
    @GET
    public String setAssignmentGradeCommentforUser(
            @WebParam(name = "sessionId", partName = "sessionId") @QueryParam("sessionId") String sessionId,
            @WebParam(name = "assignmentId", partName = "assignmentId") @QueryParam("assignmentId") String assignmentId,
            @WebParam(name = "userId", partName = "userId") @QueryParam("userId") String userId,
            @WebParam(name = "comment", partName = "comment") @QueryParam("comment") String comment,
            @WebParam(name = "grade", partName = "grade") @QueryParam("grade") String grade) {
        //establish the session
    	
    	try 
    	{		
    		Session s = establishSession(sessionId);

    		LOG.info("User " + s.getUserEid() + " setting assignment grade/comment for " + userId + " on " + assignmentId + " to " + grade); 

    		User user = userDirectoryService.getUserByEid(userId);
    		if (user == null) 
    		{
    			return "user does not exist";
    		}
    		
    		Assignment assign = assignmentService.getAssignment(assignmentId);
    		String aReference = assign.getReference();
    		
    		if (!securityService.unlock(AssignmentService.SECURE_GRADE_ASSIGNMENT_SUBMISSION, aReference))
    		{
    			LOG.warn("User " + s.getUserEid() + " does not have permission to set assignment grades");
    			return "failure: no permission";
    		}
    		
    		LOG.info("Setting assignment grade/comment for " + userId + " on " + assignmentId + " to " + grade); 
    		
    		AssignmentSubmission sub = assignmentService.getSubmission(assignmentId, user);
    		AssignmentSubmissionEdit asEdit =  null;
    		String context = assign.getContext();

    		if (sub == null) {
    			asEdit = assignmentService.addSubmission(context, assignmentId, user.getId());
    		} else {			
    			asEdit = assignmentService.editSubmission(sub.getReference());
    		}
    		
    		asEdit.setFeedbackComment(comment);
    		asEdit.setGrade(grade);
    		asEdit.setGraded(true);
    		asEdit.setGradeReleased(true);
    		assignmentService.commitEdit(asEdit);
    		
    		// If necessary, update the assignment grade in the Gradebook

    		String sReference = asEdit.getReference();

    		String associateGradebookAssignment = StringUtils.trimToNull(assign.getProperties().getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT));
    		
    		// update grade in gradebook
    		integrateGradebook(aReference, associateGradebookAssignment, null, null, -1, null, sReference, "update", context);

    	}
    	catch (Exception e) 
    	{
    		LOG.error("WS setAssignmentGradeCommentforUser(): Exception while setting assignment grade/comment for " + userId + " on " + assignmentId + " to " + grade, e); 
            return e.getClass().getName() + " : " + e.getMessage();	

    	}
    	
    	return "success";
    }

    @WebMethod
    @Path("/createAssignment")
    @Produces("text/plain")
    @GET
    public String createAssignment(
            @WebParam(name = "sessionId", partName = "sessionId") @QueryParam("sessionId") String sessionId,
            @WebParam(name = "context", partName = "context") @QueryParam("context") String context,
            @WebParam(name = "title", partName = "title") @QueryParam("title") String title,
            @WebParam(name = "dueTime", partName = "dueTime") @QueryParam("dueTime") long dueTime,
            @WebParam(name = "openTime", partName = "openTime") @QueryParam("openTime") long openTime,
            @WebParam(name = "closeTime", partName = "closeTime") @QueryParam("closeTime") long closeTime,
            @WebParam(name = "maxPoints", partName = "maxPoints") @QueryParam("maxPoints") int maxPoints,
            @WebParam(name = "gradeType", partName = "gradeType") @QueryParam("gradeType") int gradeType,
            @WebParam(name = "instructions", partName = "instructions") @QueryParam("instructions") String instructions,
            @WebParam(name = "subType", partName = "subType") @QueryParam("subType") int subType) {

    	
    	LOG.info("creating assignment in " + context);
    	try {
    		Session s = establishSession(sessionId);
    		AssignmentEdit assign = assignmentService.addAssignment(context);
    		
    		Time dt = timeService.newTime(dueTime);
    		Time ot = timeService.newTime(openTime);
    		Time ct = timeService.newTime(closeTime);

    		LOG.debug("time is " + dt.toStringGmtFull());
    		
    		//set the values for the assignemnt
    		assign.setTitle(title);
    		assign.setDraft(false);
    		assign.setDueTime(dt);
    		assign.setOpenTime(ot);
    		assign.setCloseTime(ct);
    		
    		//we need a contentedit for the actual contents of the assignment - this will do for now
    		
    		AssignmentContentEdit asCont = assignmentService.addAssignmentContent(context);
    		assign.setContent(asCont);
    		/*
    		 *3 - points
    		 */
    		
    		//int gradeType = 3;
    		int maxGradePoints = maxPoints;
    		LOG.debug("max points are" + maxGradePoints);
    		/*
    		 * 1 - text
    		 * 2 - attachment
    		 */
    		int typeofSubmission = subType; 
    		asCont.setTitle(title);
    		asCont.setTypeOfGrade(gradeType);
    		asCont.setMaxGradePoint(maxGradePoints);
    		asCont.setTypeOfSubmission(typeofSubmission);
    		asCont.setInstructions(instructions);
    		asCont.setIndividuallyGraded(true);
    		asCont.setReleaseGrades(true);
    		assignmentService.commitEdit(asCont);
    		
    		//setupo the submission
    		//AssignmentSubmissionEdit ae = as.addSubmission(context,assign.getId());
    		//clear it
    		//ae.clearSubmitters();
    		//ae.clearSubmittedAttachments();
    		//ae.clearFeedbackAttachments();
    		//as.commitEdit(ae);
    		
    		assignmentService.commitEdit(assign);
    		//do GB integration 
    		String aReference = assign.getReference();
    		
    		integrateGradebook(aReference, null, "add", title, maxGradePoints, dt, null, null, context);
    		
    		Calendar c = null;
    		try {
    			c = calendarService.getCalendar("/calendar/calendar/" + context + "/main");
    		} 
    		catch(Exception e) {
    			c = null;
    		}
    		
    		if (c != null) 
    		{
    			CalendarEventEdit cee = c.addEvent();
    			cee.setDescription("Assignment " + title + " " + "is due on " + dt.toStringLocalFull () + ". ");
    			cee.setDisplayName("Due "+ title);
    			cee.setType("Deadline");
    			cee.setRange(timeService.newTimeRange(dt.getTime (), 0*60*1000));
    			c.commitEvent(cee);		
    		} else {
    			LOG.warn("WS createAssignment(): no calendar found");
    		}
    		
    		return assign.getId();
    	}
    	catch (Exception e) {
    		LOG.warn("WS createAssignment(): " + e.getClass().getName() + " : " + e.getMessage());
            return e.getClass().getName() + " : " + e.getMessage();	
    	}
    	
    }

    @WebMethod
    @Path("/setAssignmentAcceptUntil")
    @Produces("text/plain")
    @GET
    public String setAssignmentAcceptUntil(
            @WebParam(name = "sessionId", partName = "sessionId") @QueryParam("sessionId") String sessionId,
            @WebParam(name = "assignmentId", partName = "assignmentId") @QueryParam("assignmentId") String assignmentId) {
        LOG.info("setting accept until time for assignment: " + assignmentId);
        try {
    		Session s = establishSession(sessionId);
    		AssignmentEdit assignment = assignmentService.editAssignment(assignmentId);
    		LOG.debug("got assignment: " + assignment.getTitle());
    		LOG.debug("assignment closes: " + assignment.getDueTime());
    		assignment.setCloseTime(assignment.getDueTime());
    		assignmentService.commitEdit(assignment);
    		LOG.debug("edit committed");			
    	}
    	catch (Exception e) {
    		LOG.error("WS setAssignmentAcceptUntil(): " + e.getClass().getName() + " : " + e.getMessage()); 
            return e.getClass().getName() + " : " + e.getMessage();	
    	}
    	return "success";
    }

    // This is a copy of the code in AssignmentAction.java

    /**
     *
     * @param assignmentRef
     * @param associateGradebookAssignment
     * @param addUpdateRemoveAssignment
     * @param newAssignment_title
     * @param newAssignment_maxPoints
     * @param newAssignment_dueTime
     * @param submissionRef
     * @param updateRemoveSubmission
     * @param context
     */
    protected void integrateGradebook( String assignmentRef, String associateGradebookAssignment, String addUpdateRemoveAssignment, String newAssignment_title, int newAssignment_maxPoints, Time newAssignment_dueTime, String submissionRef, String updateRemoveSubmission, String context)
    {
    	//add or remove external grades to gradebook
    	// a. if Gradebook does not exists, do nothing, 'cos setting should have been hidden
    	// b. if Gradebook exists, just call addExternal and removeExternal and swallow any exception. The
    	//    exception are indication that the assessment is already in the Gradebook or there is nothing
    	//    to remove.
    	String gradebookUid = context;
    	boolean gradebookExists = isGradebookDefined(context);
    	
    	String assignmentToolTitle = "Assignments";
    	
    	if (gradebookExists)
    	{
    		boolean isExternalAssignmentDefined=gradebookExternalAssessmentService.isExternalAssignmentDefined(gradebookUid, assignmentRef);
    		boolean isExternalAssociateAssignmentDefined = gradebookExternalAssessmentService.isExternalAssignmentDefined(gradebookUid, associateGradebookAssignment);
    		boolean isAssignmentDefined = gradebookService.isAssignmentDefined(gradebookUid, associateGradebookAssignment);

    		if (addUpdateRemoveAssignment != null)
    		{
    			if (addUpdateRemoveAssignment.equals("add") || ( addUpdateRemoveAssignment.equals("update") && !gradebookService.isAssignmentDefined(gradebookUid, newAssignment_title)))
    			{
    				// add assignment into gradebook
    				try
    				{
    					// add assignment to gradebook
    					gradebookExternalAssessmentService.addExternalAssessment(gradebookUid,
    							assignmentRef, 
    							null,
    							newAssignment_title,
    							newAssignment_maxPoints/10,
    							new Date(newAssignment_dueTime.getTime()),
    					"Assignment");
    				}
    				catch (AssignmentHasIllegalPointsException e) 
    				{
    					//addAlert(state, rb.getString("addtogradebook.illegalPoints"));
    				}
    				catch(ConflictingAssignmentNameException e)
    				{
    					// try to modify assignment title, make sure there is no such assignment in the gradebook, and insert again
    					boolean trying = true;
    					int attempts = 1;
    					String titleBase = newAssignment_title;
    					while(trying && attempts < MAXIMUM_ATTEMPTS_FOR_UNIQUENESS) 	// see end of loop for condition that enforces attempts <= limit)
    					{
    						String newTitle = titleBase + "-" + attempts;
    						
    						if(!gradebookService.isAssignmentDefined(gradebookUid, newTitle))
    						{
    							try
    							{
    								// add assignment to gradebook
    								gradebookExternalAssessmentService.addExternalAssessment(gradebookUid,
    										assignmentRef, 
    										null,
    										newTitle,
    										newAssignment_maxPoints/10,
    										new Date(newAssignment_dueTime.getTime()),
    								"Assignment");
    								trying = false;
    							}
    							catch(Exception ee)
    							{
    								// try again, ignore the exception
    							}
    						}
    						
    						if (trying)
    						{
    							attempts++;
    							if(attempts >= MAXIMUM_ATTEMPTS_FOR_UNIQUENESS)
    							{
    								// add alert prompting for change assignment title
    								//addAlert(state, rb.getString("addtogradebook.nonUniqueTitle"));
    							}
    						}
    					}
    				}
    				catch (ConflictingExternalIdException e)
    				{
    					// ignore
    				}
    				catch (GradebookNotFoundException e)
    				{
    					// ignore
    				}
    				catch (Exception e)
    				{
    					// ignore
    				}

    			}  // (addUpdateRemoveAssignment.equals("add") || ( addUpdateRemoveAssignment.equals("update") && !g.isAssignmentDefined(gradebookUid, newAssignment_title)))  
    			
    		}	// addUpdateRemoveAssignment != null
    		
    		if (updateRemoveSubmission != null)
    		{
    			try
    			{
    				Assignment a = assignmentService.getAssignment(assignmentRef);

    				if (updateRemoveSubmission.equals("update")
    						&& a.getProperties().getProperty(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK) != null
    						&& !a.getProperties().getProperty(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK).equals(AssignmentService.GRADEBOOK_INTEGRATION_NO)
    						&& a.getContent().getTypeOfGrade() == Assignment.SCORE_GRADE_TYPE)
    				{
    					if (submissionRef == null)
    					{
    						// bulk add all grades for assignment into gradebook
    						Iterator submissions = assignmentService.getSubmissions(a).iterator();

    						Map m = new HashMap();

    						// any score to copy over? get all the assessmentGradingData and copy over
    						while (submissions.hasNext())
    						{
    							AssignmentSubmission aSubmission = (AssignmentSubmission) submissions.next();
    							if (aSubmission.getGradeReleased())
    							{
    								User[] submitters = aSubmission.getSubmitters();
    								String submitterId = submitters[0].getId();
    								String gradeString = StringUtils.trimToNull(aSubmission.getGrade(false));
    								Double grade = gradeString != null ? Double.valueOf(displayGrade(gradeString)) : null;
    								m.put(submitterId, grade);
    							}
    						}

    						// need to update only when there is at least one submission
    						if (m.size()>0)
    						{
    							if (associateGradebookAssignment != null)
    							{
    								if (isExternalAssociateAssignmentDefined)
    								{
    									// the associated assignment is externally maintained
    									gradebookExternalAssessmentService.updateExternalAssessmentScores(gradebookUid, associateGradebookAssignment, m);
    								}
    								else if (isAssignmentDefined)
    								{
    									// the associated assignment is internal one, update records one by one
    									submissions = assignmentService.getSubmissions(a).iterator();
    									while (submissions.hasNext())
    									{
    										AssignmentSubmission aSubmission = (AssignmentSubmission) submissions.next();
    										User[] submitters = aSubmission.getSubmitters();
    										String submitterId = submitters[0].getId();
    										String gradeString = StringUtils.trimToNull(aSubmission.getGrade(false));
    										String grade = (gradeString != null && aSubmission.getGradeReleased()) ? displayGrade(gradeString) : null;
    										gradebookService.setAssignmentScoreString(gradebookUid, associateGradebookAssignment, submitterId, grade, assignmentToolTitle);
    									}
    								}
    							}
    							else if (isExternalAssignmentDefined)
    							{
    								gradebookExternalAssessmentService.updateExternalAssessmentScores(gradebookUid, assignmentRef, m);
    							}
    						}
    					}
    					else
    					{
    						try
    						{
    							// only update one submission
    							AssignmentSubmission aSubmission = (AssignmentSubmission) assignmentService.getSubmission(submissionRef);
    							User[] submitters = aSubmission.getSubmitters();
    							String gradeString = StringUtils.trimToNull(aSubmission.getGrade(false));

    							if (associateGradebookAssignment != null)
    							{
    								if (gradebookExternalAssessmentService.isExternalAssignmentDefined(gradebookUid, associateGradebookAssignment))
    								{
    									// the associated assignment is externally maintained
    									gradebookExternalAssessmentService.updateExternalAssessmentScore(gradebookUid, associateGradebookAssignment, submitters[0].getId(),
    											(gradeString != null && aSubmission.getGradeReleased()) ? displayGrade(gradeString) : null);
    								}
    								else if (gradebookService.isAssignmentDefined(gradebookUid, associateGradebookAssignment))
    								{
    									// the associated assignment is internal one, update records
    									gradebookService.setAssignmentScoreString(gradebookUid, associateGradebookAssignment, submitters[0].getId(),
    											(gradeString != null && aSubmission.getGradeReleased()) ? displayGrade(gradeString) : null, assignmentToolTitle);
    								}
    							}
    							else
    							{
    								gradebookExternalAssessmentService.updateExternalAssessmentScore(gradebookUid, assignmentRef, submitters[0].getId(),
    										(gradeString != null && aSubmission.getGradeReleased()) ? displayGrade(gradeString) : null);
    							}
    						}
    						catch (Exception e)
    						{
    							LOG.warn("Cannot find submission " + submissionRef + ": " + e.getMessage());
    						}
    					} // submissionref != null

    				}
    				else if (updateRemoveSubmission.equals("remove"))
    				{
    					if (submissionRef == null)
    					{
    						// remove all submission grades (when changing the associated entry in Gradebook)
    						Iterator submissions = assignmentService.getSubmissions(a).iterator();

    						// any score to copy over? get all the assessmentGradingData and copy over
    						while (submissions.hasNext())
    						{
    							AssignmentSubmission aSubmission = (AssignmentSubmission) submissions.next();
    							User[] submitters = aSubmission.getSubmitters();
    							if (isExternalAssociateAssignmentDefined)
    							{
    								// if the old associated assignment is an external maintained one
    								gradebookExternalAssessmentService.updateExternalAssessmentScore(gradebookUid, associateGradebookAssignment, submitters[0].getId(), null);
    							}
    							else if (isAssignmentDefined)
    							{
    								gradebookService.setAssignmentScoreString(gradebookUid, associateGradebookAssignment, submitters[0].getId(), null, assignmentToolTitle);
    							}
    						}
    					}
    					else
    					{
    						// remove only one submission grade
    						try
    						{
    							AssignmentSubmission aSubmission = (AssignmentSubmission) assignmentService.getSubmission(submissionRef);
    							User[] submitters = aSubmission.getSubmitters();
    							gradebookExternalAssessmentService.updateExternalAssessmentScore(gradebookUid, assignmentRef, submitters[0].getId(), null);
    						}
    						catch (Exception e)
    						{
    							LOG.warn("Cannot find submission " + submissionRef + ": " + e.getMessage());
    						}
    					}
    				}
    			}
    			catch (Exception e)
    			{
    				LOG.warn("Cannot find assignment: " + assignmentRef + ": " + e.getMessage());
    			}
    		} // updateRemoveSubmission != null

    		
    	}	// if gradebook exists
    	
    }	// integrateGradebook

    protected boolean isGradebookDefined(String context)
    {
    	boolean  rv = false;
    	try
    	{
    		if (gradebookService.isGradebookDefined(context))
    		{
    			return true;
    		}
    	}
    	catch (Exception e)
    	{
    		//LOG.debug("chef", this + rb.getString("addtogradebook.alertMessage") + "\n" + e.getMessage());
    	}
    	
    	return false;
    	
    }	// isGradebookDefined()


    @WebMethod
    @Path("/createSubmission")
    @Produces("text/plain")
    @GET
    public String createSubmission(
            @WebParam(name = "sessionId", partName = "sessionId") @QueryParam("sessionId") String sessionId,
            @WebParam(name = "context", partName = "context") @QueryParam("context") String context,
            @WebParam(name = "assignmentId", partName = "assignmentId") @QueryParam("assignmentId") String assignmentId,
            @WebParam(name = "userId", partName = "userId") @QueryParam("userId") String userId,
            @WebParam(name = "time", partName = "time") @QueryParam("time") long time) {
        LOG.info("createSubmission( " + sessionId + ", " + context + " , " + assignmentId + " , " + userId + "," + time + ")");
        try {
    		//establish the session
    		Session s = establishSession(sessionId);
    		Assignment assign = assignmentService.getAssignment(assignmentId);
    		
    		
    		User user = userDirectoryService.getUserByEid(userId);
    		if (user == null) 
    		{
    			return "user does not exit";
    		} else {
    			LOG.info("Got user " + userId);
    		}
    		//s.setUserId(user.getId());
    		//s.setUserEid(userId);
    		
    		AssignmentSubmissionEdit ase = assignmentService.addSubmission(context,assignmentId, userDirectoryService.getUserId(userId));
    		
    		ase.clearSubmitters();		
    		ase.addSubmitter(user);
    		ase.setSubmitted(true);
    		
    		Time subTime = timeService.newTime(time);
    		LOG.info("Setting time to " + time);
    		ase.setTimeSubmitted(subTime);
    		assignmentService.commitEdit(ase);
    		return ase.getId();	
    		
    	}
    	catch(Exception e) {
    		LOG.error("WS createSubmission(): " + e.getClass().getName() + " : " + e.getMessage()); 
            return e.getClass().getName() + " : " + e.getMessage();
    	}
    }


    @WebMethod
    @Path("/addSubmissionAttachment")
    @Produces("text/plain")
    @GET
    public String addSubmissionAttachment(
            @WebParam(name = "sessionId", partName = "sessionId") @QueryParam("sessionId") String sessionId,
            @WebParam(name = "context", partName = "context") @QueryParam("context") String context,
            @WebParam(name = "submissionId", partName = "submissionId") @QueryParam("submissionId") String submissionId,
            @WebParam(name = "attachmentName", partName = "attachmentName") @QueryParam("attachmentName") String attachmentName,
            @WebParam(name = "attachmentMimeType", partName = "attachmentMimeType") @QueryParam("attachmentMimeType") String attachmentMimeType,
            @WebParam(name = "attachmentData", partName = "attachmentData") @QueryParam("attachmentData") String attachmentData) {


        try {
    		// establish the session
    		Session s = establishSession(sessionId);
    		AssignmentSubmissionEdit sub = assignmentService.editSubmission(submissionId);
    		
    		// create the attachmment
    		Base64 decode = new Base64();
    		// byte[] photoData = null;
    		// photoData = decode.decodeToByteArray(attachmentData);
    		byte[] photoData = decode.decode(attachmentData);
    		LOG.info("File of size: " + photoData + " found");
    		
    		byte[] content = photoData;
    				
    		ResourcePropertiesEdit rpe = contentHostingService.newResourceProperties();
    		rpe.addProperty(rpe.PROP_DISPLAY_NAME, attachmentName);
    		
    		ContentResource file = contentHostingService.addAttachmentResource(attachmentName,
    				context, "Assignments", attachmentMimeType, content, rpe);
    		LOG.info("attachment name is : " + attachmentName);
    		LOG.info("file has lenght of: " + file.getContentLength());
    		
    		Reference ref = entityManager.newReference(file.getReference());
    		sub.addSubmittedAttachment(ref);
    		assignmentService.commitEdit(sub);
    		return "Success!";
    	} catch (Exception e) {
    		LOG.error("WS addSubmissionAttachment(): " + e.getClass().getName() + " : " + e.getMessage()); 
            return e.getClass().getName() + " : " + e.getMessage();
    	}
    	
    }


    /**
     * display grade properly - copied from AssignmentAction
     */
    private String displayGrade(String grade)
    {
    	if (grade != null && (grade.length() >= 1))
    	{
    		if (grade.indexOf(".") != -1)
    		{
    			if (grade.startsWith("."))
    			{
    				grade = "0".concat(grade);
    			}
    			else if (grade.endsWith("."))
    			{
    				grade = grade.concat("0");
    			}
    		}
    		else
    		{
    			try
    			{
    				Integer.parseInt(grade);
    				grade = grade.substring(0, grade.length() - 1) + "." + grade.substring(grade.length() - 1);
    			}
    			catch (NumberFormatException e)
    			{
    				// ignore
    			}
    		}
    	}
    	else
    	{
    		grade = "";
    	}
    	
    	return grade;

    } // displayGrade


    @WebMethod
    @Path("/undeleteAssignments")
    @Produces("text/plain")
    @GET
    public String undeleteAssignments(
            @WebParam(name = "sessionId", partName = "sessionId") @QueryParam("sessionId") String sessionId,
            @WebParam(name = "context", partName = "context") @QueryParam("context") String context) {
        try {
    		//establish the session
    		Session s = establishSession(sessionId);
    			    
    		Iterator assingments = assignmentService.getAssignmentsForContext(context);
    		while (assingments.hasNext()) {
    			Assignment ass =  (Assignment)assingments.next();
    			ResourceProperties rp = ass.getProperties();
    			
    			try {
                    String deleted = rp.getProperty(ResourceProperties.PROP_ASSIGNMENT_DELETED);

                    LOG.info("Assignment " + ass.getTitle()+ " deleted status: " + deleted);
    				if (deleted != null) {
    					AssignmentEdit ae = assignmentService.editAssignment(ass.getId());
    					ResourcePropertiesEdit rpe = ae.getPropertiesEdit();
    					LOG.info("undeleting" + ass.getTitle() + " for site " + context);
    					rpe.removeProperty(ResourceProperties.PROP_ASSIGNMENT_DELETED);
    				
    					assignmentService.commitEdit(ae);
    					
    				}
    			} catch (IdUnusedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} catch (PermissionException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} catch (InUseException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    			
    		}
    	} catch (Exception e) {
    		LOG.error("WS undeleteAssignments(): " + e.getClass().getName() + " : " + e.getMessage()); 
            return e.getClass().getName() + " : " + e.getMessage();
    	}
    	
    	return "success";

    }

}
