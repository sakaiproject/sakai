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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.assignment.api.model.AssignmentSubmissionSubmitter;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.assignment.api.AssignmentService;
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
import java.time.Instant;
import java.util.*;

@WebService
@SOAPBinding(style= SOAPBinding.Style.RPC, use= SOAPBinding.Use.LITERAL)
@Slf4j
public class Assignments extends AbstractWebService {

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
    		log.info("assignment list requested for " + context);
    		
    		Document dom = Xml.createDocument();
    		Node all = dom.createElement("assignments");
    		dom.appendChild(all);
    		
    		for (Assignment thisA : assignmentService.getAssignmentsForContext(context)) {
    			log.debug("got " + thisA.getTitle());
    			if (!thisA.getDraft()) {
    				log.debug("about to start building xml doc");
    				Element uElement = dom.createElement("assignment");
    				uElement.setAttribute("id", thisA.getId());
    				uElement.setAttribute("title", thisA.getTitle());
    				log.debug("added title and id");
    				Integer temp = thisA.getTypeOfGrade().ordinal();
   					String gType = temp.toString();
					uElement.setAttribute("gradeType", gType);

    				/* these need to be converted to strings
    				 */
    				
    				log.debug("About to get dates");
    				
    				Instant dueTime = thisA.getDueDate();
    				Instant openTime = thisA.getOpenDate();
    				Instant closeTime = thisA.getCloseDate();
    				log.debug("got dates");
    				DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    				
    				if (openTime != null){
    					log.debug("open time is " + openTime.toString());
    					uElement.setAttribute("openTime", format.format(Date.from(openTime)));
    				}
    				if (closeTime != null) {
    					log.debug("close time is " + closeTime.toString());
    					uElement.setAttribute("closeTime", format.format(Date.from(closeTime)));
    				}
    				
    				if (dueTime != null) {
    					log.debug("due time is " + dueTime.toString());
    					uElement.setAttribute("dueTime", format.format(Date.from(dueTime)));
    				}
    				
    				log.debug("apending element to parent");
    				all.appendChild(uElement);
    			} else {
    				log.debug("this is a draft assignment");
    			}
    			
    		}
    		String retVal = Xml.writeDocumentToString(dom);
    		return retVal;
    	}
    	catch (Exception e) {
    		log.error("WS getAssignmentsForContext(): " + e.getClass().getName() + " : " + e.getMessage());
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
    		Set<AssignmentSubmission> subs = assignmentService.getSubmissions(assign);
    		
    		//build the xml
    		log.debug("about to start building xml doc");
    		Document dom = Xml.createDocument();
    		Node all = dom.createElement("submissions");
    		dom.appendChild(all);
    		
    		for (AssignmentSubmission thisSub : subs) {
    			log.debug("got submission" + thisSub);
    			Element uElement = dom.createElement("submission");
    			uElement.setAttribute("feedback-comment", thisSub.getFeedbackComment());
    			uElement.setAttribute("feedback-text", thisSub.getFeedbackText());
    			uElement.setAttribute("grade", thisSub.getGrade());
    			uElement.setAttribute("status", assignmentService.getSubmissionStatus(thisSub.getId()));
    			uElement.setAttribute("submitted-text", thisSub.getSubmittedText());
    			for (AssignmentSubmissionSubmitter submitter : thisSub.getSubmitters()) {
    				uElement.setAttribute("submitter-id", submitter.getSubmitter());
    			}

    			//Element attachments = dom.createElement("attachment");
    			for (String attachment : thisSub.getAttachments()) {
    				//Element attachments = dom.createElement("attachment");
    				Reference ref = entityManager.newReference(attachment);
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
    		log.error("WS getSubmissionsForAssignment(): " + e.getClass().getName() + " : " + e.getMessage());
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

    		log.info("User " + s.getUserEid() + " setting assignment grade/comment for " + userId + " on " + assignmentId + " to " + grade); 

    		User user = userDirectoryService.getUserByEid(userId);
    		if (user == null) 
    		{
    			return "user does not exist";
    		}
    		
    		Assignment assign = assignmentService.getAssignment(assignmentId);
    		String aReference = AssignmentReferenceReckoner.reckoner().assignment(assign).reckon().getReference();
    		
    		if (!securityService.unlock(AssignmentServiceConstants.SECURE_GRADE_ASSIGNMENT_SUBMISSION, aReference))
    		{
    			log.warn("User " + s.getUserEid() + " does not have permission to set assignment grades");
    			return "failure: no permission";
    		}
    		
    		log.info("Setting assignment grade/comment for " + userId + " on " + assignmentId + " to " + grade); 
    		
    		AssignmentSubmission sub = assignmentService.getSubmission(assignmentId, user);
    		String context = assign.getContext();

    		if (sub == null) {
    			sub = assignmentService.addSubmission(assignmentId, user.getId());
    		}
    		
    		sub.setFeedbackComment(comment);
    		sub.setGrade(grade);
    		sub.setGraded(true);
    		sub.setGradeReleased(true);
    		assignmentService.updateSubmission(sub);
    		
    		// If necessary, update the assignment grade in the Gradebook

    		String associateGradebookAssignment = assign.getProperties().get(AssignmentServiceConstants.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
    		String sReference = AssignmentReferenceReckoner.reckoner().submission(sub).reckon().getReference();
    		
    		// update grade in gradebook
    		integrateGradebook(aReference, associateGradebookAssignment, null, null, -1, null, sReference, "update", context);

    	}
    	catch (Exception e) 
    	{
    		log.error("WS setAssignmentGradeCommentforUser(): Exception while setting assignment grade/comment for " + userId + " on " + assignmentId + " to " + grade, e); 
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

    	
    	log.info("creating assignment in " + context);
    	try {
    		Session s = establishSession(sessionId);
    		Assignment assign = assignmentService.addAssignment(context);
    		
    		Instant dt = Instant.ofEpochMilli(dueTime);
    		Instant ot = Instant.ofEpochMilli(openTime);
    		Instant ct = Instant.ofEpochMilli(closeTime);

    		log.debug("time is " + dt.toString());
    		
    		//set the values for the assignemnt
    		assign.setTitle(title);
    		assign.setDraft(false);
    		assign.setDueDate(dt);
    		assign.setOpenDate(ot);
    		assign.setCloseDate(ct);
    		
    		/*
    		 *3 - points
    		 */
    		
    		//int gradeType = 3;
    		int maxGradePoints = maxPoints;
    		log.debug("max points are" + maxGradePoints);
    		/*
    		 * 1 - text
    		 * 2 - attachment
    		 */
    		assign.setTypeOfGrade(Assignment.GradeType.values()[gradeType]);
    		assign.setMaxGradePoint(maxGradePoints);
    		assign.setTypeOfSubmission(Assignment.SubmissionType.values()[subType]);
    		assign.setInstructions(instructions);
    		assign.setIndividuallyGraded(true);
    		assign.setReleaseGrades(true);
    		assignmentService.updateAssignment(assign);
    		
    		//setupo the submission
    		//AssignmentSubmissionEdit ae = as.addSubmission(context,assign.getId());
    		//clear it
    		//ae.clearSubmitters();
    		//ae.clearSubmittedAttachments();
    		//ae.clearFeedbackAttachments();
    		//as.commitEdit(ae);
    		
    		//do GB integration
    		String aReference = AssignmentReferenceReckoner.reckoner().assignment(assign).reckon().getReference();

    		integrateGradebook(aReference, null, "add", title, maxGradePoints, Date.from(dt), null, null, context);
    		
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
    			cee.setDescription("Assignment " + title + " " + "is due on " + dt.toString() + ". ");
    			cee.setDisplayName("Due "+ title);
    			cee.setType("Deadline");
    			cee.setRange(timeService.newTimeRange(dt.toEpochMilli(), 0*60*1000));
    			c.commitEvent(cee);		
    		} else {
    			log.warn("WS createAssignment(): no calendar found");
    		}
    		
    		return assign.getId();
    	}
    	catch (Exception e) {
    		log.warn("WS createAssignment(): " + e.getClass().getName() + " : " + e.getMessage());
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
        log.info("setting accept until time for assignment: " + assignmentId);
        try {
    		Session s = establishSession(sessionId);
    		Assignment assignment = assignmentService.getAssignment(assignmentId);
    		log.debug("got assignment: " + assignment.getTitle());
    		log.debug("assignment closes: " + assignment.getDueDate());
    		assignment.setCloseDate(assignment.getDueDate());
    		assignmentService.updateAssignment(assignment);
    		log.debug("edit committed");			
    	}
    	catch (Exception e) {
    		log.error("WS setAssignmentAcceptUntil(): " + e.getClass().getName() + " : " + e.getMessage()); 
            return e.getClass().getName() + " : " + e.getMessage();	
    	}
    	return "success";
    }

    @WebMethod
    @Path("/shiftAssignmentDates")
    @Produces("text/plain")
    @GET
    public String shiftAssignmentDates(
            @WebParam(name = "sessionId", partName = "sessionId") @QueryParam("sessionId") String sessionId,
            @WebParam(name = "shiftDays", partName = "shiftDays") @QueryParam("shiftDays") int shiftDays,
            @WebParam(name = "shiftHours", partName = "shiftHours") @QueryParam("shiftHours") int shiftHours,
            @WebParam(name = "assignmentId", partName = "assignmentId") @QueryParam("assignmentId") String assignmentId) {

        try {
    		Session s = establishSession(sessionId);
    		Assignment assignment = assignmentService.getAssignment(assignmentId);
    		log.debug("got assignment: " + assignment.getTitle());

    		java.util.Calendar cal = java.util.Calendar.getInstance();

    		cal.setTimeInMillis(assignment.getOpenDate().toEpochMilli());
    		cal.add(java.util.Calendar.DAY_OF_YEAR, shiftDays);
    		cal.add(java.util.Calendar.HOUR, shiftHours);
    		Date shiftedOpenDate = cal.getTime();
    		assignment.setOpenDate(shiftedOpenDate.toInstant());

    		cal.setTimeInMillis(assignment.getDueDate().toEpochMilli());
    		cal.add(java.util.Calendar.DAY_OF_YEAR, shiftDays);
    		cal.add(java.util.Calendar.HOUR, shiftHours);
    		Date shiftedDueDate = cal.getTime();
    		assignment.setDueDate(shiftedDueDate.toInstant());

    		cal.setTimeInMillis(assignment.getCloseDate().toEpochMilli());
    		cal.add(java.util.Calendar.DAY_OF_YEAR, shiftDays);
    		cal.add(java.util.Calendar.HOUR, shiftHours);
    		Date shiftedCloseDate = cal.getTime();
    		assignment.setCloseDate(shiftedCloseDate.toInstant());

    		cal.setTimeInMillis(assignment.getDropDeadDate().toEpochMilli());
    		cal.add(java.util.Calendar.DAY_OF_YEAR, shiftDays);
    		cal.add(java.util.Calendar.HOUR, shiftHours);
    		Date shiftedDropDeadDate = cal.getTime();
    		assignment.setDropDeadDate(shiftedDropDeadDate.toInstant());

    		cal.setTimeInMillis(assignment.getPeerAssessmentPeriodDate().toEpochMilli());
    		cal.add(java.util.Calendar.DAY_OF_YEAR, shiftDays);
    		cal.add(java.util.Calendar.HOUR, shiftHours);
    		Date shiftedPeerAssessmentDate = cal.getTime();
    		assignment.setPeerAssessmentPeriodDate(shiftedPeerAssessmentDate.toInstant());

    		Map<String, String> aProperties = assignment.getProperties();

    		String resubmitCloseDateString = aProperties.get(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME);
    		if (resubmitCloseDateString != null) {
    			Date resubmitCloseDate = new Date(timeService.newTime(Long.parseLong(resubmitCloseDateString)).getTime());
    			cal.setTime(resubmitCloseDate);
    			cal.add(java.util.Calendar.DAY_OF_YEAR, shiftDays);
    			cal.add(java.util.Calendar.HOUR, shiftHours);
    			aProperties.put(AssignmentConstants.ALLOW_RESUBMIT_CLOSETIME, String.valueOf(cal.getTimeInMillis()));
    		}
    		assignmentService.updateAssignment(assignment);
    		log.debug("edit committed");
    	}
    	catch (Exception e) {
    		log.error("WS shiftAssignmentDates(): " + e.getClass().getName() + " : " + e.getMessage());
    	
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
    protected void integrateGradebook( String assignmentRef, String associateGradebookAssignment, String addUpdateRemoveAssignment, String newAssignment_title, int newAssignment_maxPoints, Date newAssignment_dueTime, String submissionRef, String updateRemoveSubmission, String context)
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
    						&& a.getProperties().get(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK) != null
    						&& !a.getProperties().get(NEW_ASSIGNMENT_ADD_TO_GRADEBOOK).equals(AssignmentServiceConstants.GRADEBOOK_INTEGRATION_NO)
    						&& a.getTypeOfGrade() == Assignment.GradeType.SCORE_GRADE_TYPE)
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
    								Set<AssignmentSubmissionSubmitter> submitters = aSubmission.getSubmitters();
    								String submitterId = submitters.stream().filter(AssignmentSubmissionSubmitter::getSubmittee).findFirst().get().getSubmitter();
    								String gradeString = StringUtils.trimToNull(aSubmission.getGrade());
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
    										Set<AssignmentSubmissionSubmitter> submitters = aSubmission.getSubmitters();
    										String submitterId = submitters.stream().filter(AssignmentSubmissionSubmitter::getSubmittee).findFirst().get().getSubmitter();
    										String gradeString = StringUtils.trimToNull(aSubmission.getGrade());
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
    							Set<AssignmentSubmissionSubmitter> submitters = aSubmission.getSubmitters();
								String submitter = submitters.stream().filter(AssignmentSubmissionSubmitter::getSubmittee).findFirst().get().getSubmitter();
    							String gradeString = StringUtils.trimToNull(aSubmission.getGrade());

    							if (associateGradebookAssignment != null)
    							{
    								if (gradebookExternalAssessmentService.isExternalAssignmentDefined(gradebookUid, associateGradebookAssignment))
    								{
    									// the associated assignment is externally maintained
    									gradebookExternalAssessmentService.updateExternalAssessmentScore(gradebookUid, associateGradebookAssignment, submitter,
    											(gradeString != null && aSubmission.getGradeReleased()) ? displayGrade(gradeString) : null);
    								}
    								else if (gradebookService.isAssignmentDefined(gradebookUid, associateGradebookAssignment))
    								{
    									// the associated assignment is internal one, update records
    									gradebookService.setAssignmentScoreString(gradebookUid, associateGradebookAssignment, submitter,
    											(gradeString != null && aSubmission.getGradeReleased()) ? displayGrade(gradeString) : null, assignmentToolTitle);
    								}
    							}
    							else
    							{
    								gradebookExternalAssessmentService.updateExternalAssessmentScore(gradebookUid, assignmentRef, submitter,
    										(gradeString != null && aSubmission.getGradeReleased()) ? displayGrade(gradeString) : null);
    							}
    						}
    						catch (Exception e)
    						{
    							log.warn("Cannot find submission " + submissionRef + ": " + e.getMessage());
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
    							Set<AssignmentSubmissionSubmitter> submitters = aSubmission.getSubmitters();
    							String submitter = submitters.stream().filter(AssignmentSubmissionSubmitter::getSubmittee).findFirst().get().getSubmitter();
    							if (isExternalAssociateAssignmentDefined)
    							{
    								// if the old associated assignment is an external maintained one
    								gradebookExternalAssessmentService.updateExternalAssessmentScore(gradebookUid, associateGradebookAssignment, submitter, null);
    							}
    							else if (isAssignmentDefined)
    							{
    								gradebookService.setAssignmentScoreString(gradebookUid, associateGradebookAssignment, submitter, null, assignmentToolTitle);
    							}
    						}
    					}
    					else
    					{
    						// remove only one submission grade
    						try
    						{
    							AssignmentSubmission aSubmission = (AssignmentSubmission) assignmentService.getSubmission(submissionRef);
    							Set<AssignmentSubmissionSubmitter> submitters = aSubmission.getSubmitters();
    							String submitter = submitters.stream().filter(AssignmentSubmissionSubmitter::getSubmittee).findFirst().get().getSubmitter();
    							gradebookExternalAssessmentService.updateExternalAssessmentScore(gradebookUid, assignmentRef, submitter, null);
    						}
    						catch (Exception e)
    						{
    							log.warn("Cannot find submission " + submissionRef + ": " + e.getMessage());
    						}
    					}
    				}
    			}
    			catch (Exception e)
    			{
    				log.warn("Cannot find assignment: " + assignmentRef + ": " + e.getMessage());
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
    		//log.debug("chef", this + rb.getString("addtogradebook.alertMessage") + "\n" + e.getMessage());
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
        log.info("createSubmission( " + sessionId + ", " + context + " , " + assignmentId + " , " + userId + "," + time + ")");
        try {
    		//establish the session
    		Session s = establishSession(sessionId);
    		Assignment assign = assignmentService.getAssignment(assignmentId);
    		
    		
    		User user = userDirectoryService.getUserByEid(userId);
    		if (user == null) 
    		{
    			return "user does not exit";
    		} else {
    			log.info("Got user " + userId);
    		}
    		//s.setUserId(user.getId());
    		//s.setUserEid(userId);
    		
    		AssignmentSubmission ase = assignmentService.addSubmission(assignmentId, userDirectoryService.getUserId(userId));

    		AssignmentSubmissionSubmitter submitter = new AssignmentSubmissionSubmitter();
    		submitter.setSubmitter(user.getId());
    		submitter.setSubmittee(true);
    		ase.setSubmitted(true);
    		
    		Instant subTime = Instant.ofEpochMilli(time);
    		log.info("Setting time to " + time);
    		ase.setDateSubmitted(subTime);
    		assignmentService.updateSubmission(ase);
    		return ase.getId();
    	}
    	catch(Exception e) {
    		log.error("WS createSubmission(): " + e.getClass().getName() + " : " + e.getMessage()); 
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
    		AssignmentSubmission sub = assignmentService.getSubmission(submissionId);
    		
    		// create the attachmment
    		Base64 decode = new Base64();
    		// byte[] photoData = null;
    		// photoData = decode.decodeToByteArray(attachmentData);
    		byte[] photoData = decode.decode(attachmentData);
    		log.info("File of size: " + photoData + " found");
    		
    		byte[] content = photoData;
    				
    		ResourcePropertiesEdit rpe = contentHostingService.newResourceProperties();
    		rpe.addProperty(rpe.PROP_DISPLAY_NAME, attachmentName);
    		
    		ContentResource file = contentHostingService.addAttachmentResource(attachmentName,
    				context, "Assignments", attachmentMimeType, content, rpe);
    		log.info("attachment name is : " + attachmentName);
    		log.info("file has lenght of: " + file.getContentLength());
    		
    		Reference ref = entityManager.newReference(file.getReference());
    		sub.getAttachments().add(ref.getReference());
    		assignmentService.updateSubmission(sub);
    		return "Success!";
    	} catch (Exception e) {
    		log.error("WS addSubmissionAttachment(): " + e.getClass().getName() + " : " + e.getMessage()); 
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
    			    
    		for (Assignment ass : assignmentService.getAssignmentsForContext(context)) {
    			Map<String, String> rp = ass.getProperties();
    			
    			try {
                    Boolean deleted = ass.getDeleted();

                    log.info("Assignment {} deleted status: {}", ass.getTitle(), deleted);
    				if (deleted) {
    					log.info("undeleting" + ass.getTitle() + " for site " + context);
    					ass.setDeleted(false);
    					assignmentService.updateAssignment(ass);
    				}
    			} catch (PermissionException e) {
    				// TODO Auto-generated catch block
    				log.warn("Could not undelete assignment: {}, {}", ass.getId(), e.getMessage());
    			}

			}
    	} catch (Exception e) {
    		log.error("WS undeleteAssignments(): " + e.getClass().getName() + " : " + e.getMessage()); 
            return e.getClass().getName() + " : " + e.getMessage();
    	}
    	
    	return "success";

    }

}
