package org.sakaiproject.assignment.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.api.app.scheduler.DelayedInvocation;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentPeerAssessmentService;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentSubmission;
import org.sakaiproject.assignment.api.AssignmentSubmissionEdit;
import org.sakaiproject.assignment.api.model.PeerAssessmentAttachment;
import org.sakaiproject.assignment.api.model.PeerAssessmentItem;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class AssignmentPeerAssessmentServiceImpl extends HibernateDaoSupport implements AssignmentPeerAssessmentService {
	private static Log log = LogFactory.getLog(AssignmentPeerAssessmentServiceImpl.class);
	private ScheduledInvocationManager scheduledInvocationManager;
	private TimeService timeService;
	protected AssignmentService assignmentService;
	private SecurityService securityService = null;
	private SessionManager sessionManager;
	
	public void init(){
		
	}
	
	public void destroy(){
		
	}

	public void schedulePeerReview(String assignmentId){
		//first remove any previously scheduled reviews:
		removeScheduledPeerReview(assignmentId);
		//now schedule a time for the review to be setup
		Assignment assignment;
		try {
			assignment = assignmentService.getAssignment(assignmentId);
			if(!assignment.getDraft() && assignment.getAllowPeerAssessment()){
				Time assignmentCloseTime = assignment.getCloseTime();
				Time openTime = null;
				if(assignmentCloseTime != null){
					openTime = timeService.newTime(assignmentCloseTime.getTime());
				}
				// Schedule the new notification
				if (openTime != null){
					scheduledInvocationManager.createDelayedInvocation(openTime,
							"org.sakaiproject.assignment.api.AssignmentPeerAssessmentService",
							assignmentId);
				}
			}
		} catch (IdUnusedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PermissionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void removeScheduledPeerReview(String assignmentId){
		// Remove any existing notifications for this area
		DelayedInvocation[] fdi = scheduledInvocationManager.findDelayedInvocations("org.sakaiproject.assignment.api.AssignmentPeerAssessmentService", assignmentId);
		if (fdi != null && fdi.length > 0)
		{
			for (DelayedInvocation d : fdi)
			{
				scheduledInvocationManager.deleteDelayedInvocation(d.uuid);
			}
		}
	}

	/**
	 * Method called by the scheduledInvocationManager
	 */
	public void execute(String opaqueContext){
		try {
			//for group assignments, we need to have a user ID, otherwise, an exception is thrown:
			sessionManager.getCurrentSession().setUserEid("admin");
			sessionManager.getCurrentSession().setUserId("admin");
			Assignment assignment = assignmentService.getAssignment(opaqueContext);
			if(assignment.getAllowPeerAssessment() && !assignment.getDraft()){
				int numOfReviews = assignment.getPeerAssessmentNumReviews();
				List<AssignmentSubmission> submissions = (List<AssignmentSubmission>) assignmentService.getSubmissions(assignment);
				//keep a map of submission ids to look up possible existing peer assessments
				Map<String, AssignmentSubmission> submissionIdMap = new HashMap<String, AssignmentSubmission>();
				//keep track of who has been assigned an assessment
				Map<String, Map<String, PeerAssessmentItem>> assignedAssessmentsMap = new HashMap<String, Map<String, PeerAssessmentItem>>();
				//keep track of how many assessor's each student has
				Map<String, Integer> studentAssessorsMap = new HashMap<String, Integer>();
				List<User> submitterUsersList = (List<User>) assignmentService.allowAddSubmissionUsers(assignment.getReference());
				List<String> submitterIdsList = new ArrayList<String>();
				if(submitterUsersList != null){
					for(User u : submitterUsersList){
						submitterIdsList.add(u.getId());
					}
				}
				//loop through the assignment submissions and setup the maps and lists
				for(AssignmentSubmission s : submissions){
					if(s.getTimeSubmitted() != null 
							//check if the submission is submitted, if not, see if there is any submission data to review (i.e. draft was auto submitted)
							&& (s.getSubmitted() || ((s.getSubmittedText() != null && !"".equals(s.getSubmittedText().trim()) || (s.getSubmittedAttachments() != null && s.getSubmittedAttachments().size() > 0)))) 
							&& submitterIdsList.contains(s.getSubmitterId()) && !"admin".equals(s.getSubmitterId())){
						//only deal with users in the submitter's list
						submissionIdMap.put(s.getId(), s);
						assignedAssessmentsMap.put(s.getSubmitterId(), new HashMap<String, PeerAssessmentItem>());
						studentAssessorsMap.put(s.getSubmitterId(), 0);
					}
				}
				//this could be an update to an existing assessment... just make sure to grab any existing
				//review items first
				List<PeerAssessmentItem> existingItems = getPeerAssessmentItems(submissionIdMap.keySet(), assignment.getContent().getFactor());
				List<PeerAssessmentItem> removeItems = new ArrayList<PeerAssessmentItem>();
				//remove all empty items to start from scratch:
				for (Iterator iterator = existingItems.iterator(); iterator
						.hasNext();) {
					PeerAssessmentItem peerAssessmentItem = (PeerAssessmentItem) iterator.next();
					if(peerAssessmentItem.getScore() == null && (peerAssessmentItem.getComment() == null || "".equals(peerAssessmentItem.getComment().trim()))){
						removeItems.add(peerAssessmentItem);
						iterator.remove();
					}
				}
				if(removeItems.size() > 0){
					getHibernateTemplate().deleteAll(removeItems);
				}
				//loop through the items and update the map values:
				for(PeerAssessmentItem p : existingItems){
					if(submissionIdMap.containsKey(p.getSubmissionId())){
						//first, add this assessment to the AssignedAssessmentsMap
						AssignmentSubmission s = submissionIdMap.get(p.getSubmissionId());
						//Next, increment the count for studentAssessorsMap
						Integer count = studentAssessorsMap.get(s.getSubmitterId());
						if(count == null){
							//probably not possible, just check
							count = 0;
						}
						//check if the count is less than num of reviews before added another one,
						//otherwise, we need to delete this one (if it's empty)
						if(count < numOfReviews || p.getScore() != null || p.getComment() != null){
							count++;
							studentAssessorsMap.put(s.getSubmitterId(), count);
							Map<String, PeerAssessmentItem> peerAssessments = assignedAssessmentsMap.get(p.getAssessorUserId());
							if(peerAssessments == null){
								//probably not possible, but just check
								peerAssessments = new HashMap<String, PeerAssessmentItem>();
							}
							peerAssessments.put(p.getSubmissionId(), p);
							assignedAssessmentsMap.put(p.getAssessorUserId(), peerAssessments);
						}else{
							//this shoudln't happen since the code above removes all empty assessments, but just in case:
							getHibernateTemplate().delete(p);
						}
					}else{
						//this isn't realy possible since we looked up the peer assessments by submission id
						log.error("AssignmentPeerAssessmentServiceImpl: found a peer assessment with an invalid session id: " + p.getSubmissionId());
					}
				}
				
				
				//ok now that we have any existing assigned reviews accounted for, let's make sure that the number of reviews is setup properly,
				//if not, add some
				//let's get a random order of submission IDs so we can have a random assigning algorithm
				List<String> randomSubmissionIds = new ArrayList<String>(submissionIdMap.keySet());
				Collections.shuffle(randomSubmissionIds);
				List<PeerAssessmentItem> newItems = new ArrayList<PeerAssessmentItem>();
				int i = 0;
				for(String submissionId : randomSubmissionIds){
					AssignmentSubmission s = submissionIdMap.get(submissionId);
					//first find out how many existing items exist for this user:
					Integer assignedCount = studentAssessorsMap.get(s.getSubmitterId());
					//by creating a tailing list (snake style), we eliminate the issue where you can be stuck with
					//a submission and the same submission user left, making for uneven distributions of submission reviews
					List<String> snakeSubmissionList = new ArrayList<String>(randomSubmissionIds.subList(i, randomSubmissionIds.size()));
					if(i > 0){
						snakeSubmissionList.addAll(new ArrayList<String>(randomSubmissionIds.subList(0, i)));
					}
					while(assignedCount < numOfReviews){
						//we need to add more reviewers for this user's submission
						String lowestAssignedAssessor = findLowestAssignedAssessor(assignedAssessmentsMap,s.getSubmitterId(), submissionId, snakeSubmissionList, submissionIdMap);
						if(lowestAssignedAssessor != null){
							Map<String, PeerAssessmentItem> assessorsAssessmentMap = assignedAssessmentsMap.get(lowestAssignedAssessor);
							if(assessorsAssessmentMap == null){
								assessorsAssessmentMap = new HashMap<String, PeerAssessmentItem>();
							}
							PeerAssessmentItem newItem = new PeerAssessmentItem();
							newItem.setAssessorUserId(lowestAssignedAssessor);
							newItem.setSubmissionId(submissionId);
							newItem.setAssignmentId(assignment.getId());
							newItems.add(newItem);
							assessorsAssessmentMap.put(submissionId, newItem);
							assignedAssessmentsMap.put(lowestAssignedAssessor, assessorsAssessmentMap);
							//update this submission user's count:
							assignedCount++;
							studentAssessorsMap.put(submissionId, assignedCount);
						}else{
							break;
						}
					}
					i++;
				}
				if(newItems.size() > 0){
					for (PeerAssessmentItem item : newItems) {
						getHibernateTemplate().saveOrUpdate(item);
					}
				}
			}
		} catch (IdUnusedException e) {
			log.error(e.getMessage(), e);
		} catch (PermissionException e) {
			log.error(e.getMessage(), e);
		}finally{
			sessionManager.getCurrentSession().setUserEid(null);
			sessionManager.getCurrentSession().setUserId(null);
		}
	}
	
	private String findLowestAssignedAssessor(Map<String, Map<String, PeerAssessmentItem>> peerAssessments, String assesseeId, String assesseeSubmissionId, List<String> snakeSubmissionList,
			Map<String, AssignmentSubmission> submissionIdMap){//find the lowest count of assigned submissions
		String lowestAssignedAssessor = null;
		Integer lowestAssignedAssessorCount = null;
		for(String sId : snakeSubmissionList){
			AssignmentSubmission s = submissionIdMap.get(sId);
			//do not include assesseeId (aka the user being assessed)
			if(!assesseeId.equals(s.getSubmitterId()) && 
					(lowestAssignedAssessorCount == null || peerAssessments.get(s.getSubmitterId()).keySet().size() < lowestAssignedAssessorCount)){
				//check if this user already has a peer assessment for this assessee
				boolean found = false;
				for(PeerAssessmentItem p : peerAssessments.get(s.getSubmitterId()).values()){
					if(p.getSubmissionId().equals(assesseeSubmissionId)){
						found = true;
						break;
					}
				}
				if(!found){
					lowestAssignedAssessorCount = peerAssessments.get(s.getSubmitterId()).keySet().size();
					lowestAssignedAssessor = s.getSubmitterId();
				}
			}
		}
		return lowestAssignedAssessor;
	}

	public List<PeerAssessmentItem> getPeerAssessmentItems(final Collection<String> submissionsIds, Integer scaledFactor){
		List<PeerAssessmentItem> listPeerAssessmentItem = new ArrayList<>();
		if(submissionsIds == null || submissionsIds.size() == 0){
			//return an empty list
			return listPeerAssessmentItem;
		}
		HibernateCallback hcb = new HibernateCallback()
	    {
	      public Object doInHibernate(Session session) throws HibernateException,
	          SQLException
	      {
	        Query q = session.getNamedQuery("findPeerAssessmentItemsBySubmissions");
	        q.setParameterList("submissionIds", submissionsIds);
	        return q.list();
	      }
	    };
	    
	    listPeerAssessmentItem = (List<PeerAssessmentItem>) getHibernateTemplate().execute(hcb);
	        
	    for (PeerAssessmentItem item : listPeerAssessmentItem) {
	    	item.setScaledFactor(scaledFactor);
	    }
	    
	    return listPeerAssessmentItem;
	}
	
	public List<PeerAssessmentItem> getPeerAssessmentItems(final String assignmentId, final String assessorUserId, Integer scaledFactor){
		List<PeerAssessmentItem> listPeerAssessmentItem = new ArrayList<>();
		if(assignmentId == null || assessorUserId == null){
			//return an empty list
			return listPeerAssessmentItem;
		}
		HibernateCallback hcb = new HibernateCallback()
	    {
	      public Object doInHibernate(Session session) throws HibernateException,
	          SQLException
	      {
	        Query q = session.getNamedQuery("findPeerAssessmentItemsByUserAndAssignment");
	        q.setParameter("assignmentId", assignmentId);
	        q.setParameter("assessorUserId", assessorUserId);
	        return q.list();
	      }
	    };
	    
	    listPeerAssessmentItem = (List<PeerAssessmentItem>) getHibernateTemplate().execute(hcb);
	    
	    for (PeerAssessmentItem item : listPeerAssessmentItem) {
	    	item.setScaledFactor(scaledFactor);
	    }
	    
	    return listPeerAssessmentItem;
	}
	
	public List<PeerAssessmentItem> getPeerAssessmentItems(final String submissionId, Integer scaledFactor){
		List<PeerAssessmentItem> listPeerAssessmentItem = new ArrayList<>();
		if(submissionId == null || "".equals(submissionId)){
			//return an empty list
			return listPeerAssessmentItem;
		}
		HibernateCallback hcb = new HibernateCallback()
	    {
	      public Object doInHibernate(Session session) throws HibernateException,
	          SQLException
	      {
	        Query q = session.getNamedQuery("findPeerAssessmentItemsBySubmissionId");
	        q.setParameter("submissionId", submissionId);
	        return q.list();
	      }
	    };
	    
	    listPeerAssessmentItem = (List<PeerAssessmentItem>) getHibernateTemplate().execute(hcb);
	    
	    for (PeerAssessmentItem item : listPeerAssessmentItem) {
	    	item.setScaledFactor(scaledFactor);
	    }
	    
	    return listPeerAssessmentItem;
	}
	
	public List<PeerAssessmentItem> getPeerAssessmentItemsByAssignmentId(final String assignmentId, Integer scaledFactor){
		List<PeerAssessmentItem> listPeerAssessmentItem = new ArrayList<>();
		if(assignmentId == null || "".equals(assignmentId)){
			//return an empty list
			return listPeerAssessmentItem;
		}
		HibernateCallback hcb = new HibernateCallback()
	    {
	      public Object doInHibernate(Session session) throws HibernateException,
	          SQLException
	      {
	        Query q = session.getNamedQuery("findPeerAssessmentItemsByAssignmentId");
	        q.setParameter("assignmentId", assignmentId);
	        return q.list();
	      }
	    };
	    
	    listPeerAssessmentItem = (List<PeerAssessmentItem>) getHibernateTemplate().execute(hcb); 
	        
	    for (PeerAssessmentItem item : listPeerAssessmentItem) {
	    	item.setScaledFactor(scaledFactor);
	    }
	    
	    return listPeerAssessmentItem;
	}
	
	public PeerAssessmentItem getPeerAssessmentItem(final String submissionId, final String assessorUserId){
		if(submissionId == null || assessorUserId == null){
			//return an empty list
			return null;
		}
		HibernateCallback hcb = new HibernateCallback()
	    {
	      public Object doInHibernate(Session session) throws HibernateException,
	          SQLException
	      {
	        Query q = session.getNamedQuery("findPeerAssessmentItemsByUserAndSubmission");
	        q.setParameter("submissionId", submissionId);
	        q.setParameter("assessorUserId", assessorUserId);
	        return q.list();
	      }
	    };
	        
	    List<PeerAssessmentItem> results = (List<PeerAssessmentItem>) getHibernateTemplate().execute(hcb);
	    if(results != null && results.size() == 1){
	    	return results.get(0);
	    }else{
	    	return null;
	    }
	}

	public List<PeerAssessmentAttachment> getPeerAssessmentAttachments(final String submissionId, final String assessorUserId){
		if(submissionId == null || "".equals(submissionId) || assessorUserId == null || "".equals(assessorUserId)){
			//return an empty list
			return new ArrayList<PeerAssessmentAttachment>();
		}
		HibernateCallback hcb = session -> {
			Query q = session.getNamedQuery("findPeerAssessmentAttachmentsByUserAndSubmission");
			q.setParameter("submissionId", submissionId);
			q.setParameter("assessorUserId", assessorUserId);
			return q.list();
		};

		return (List<PeerAssessmentAttachment>) getHibernateTemplate().execute(hcb);
	}

	public PeerAssessmentAttachment getPeerAssessmentAttachment(final String submissionId, final String assessorUserId, final String resourceId) {
		DetachedCriteria d = DetachedCriteria.forClass(PeerAssessmentAttachment.class)
				.add(Restrictions.eq("submissionId", submissionId))
				.add(Restrictions.eq("assessorUserId", assessorUserId))
				.add(Restrictions.eq("resourceId", resourceId));
		List attachments = getHibernateTemplate().findByCriteria(d);
		if (attachments == null || attachments.isEmpty()) {
			return null;
		} else {
			return (PeerAssessmentAttachment) attachments.get(0);
		}
	}

	public void savePeerAssessmentItem(PeerAssessmentItem item){
		if(item != null && item.getAssessorUserId() != null && item.getSubmissionId() != null){
			getHibernateTemplate().saveOrUpdate(item);
			getHibernateTemplate().flush();
		}
	}

	public void savePeerAssessmentAttachments(PeerAssessmentItem item){
		if(item != null && item.getAttachmentList() != null){
			for(PeerAssessmentAttachment element : item.getAttachmentList()) {
				getHibernateTemplate().saveOrUpdate(element);
			}
			getHibernateTemplate().flush();
		}
	}

	public void removePeerAttachment(PeerAssessmentAttachment peerAssessmentAttachment) {
		getHibernateTemplate().delete(peerAssessmentAttachment);
		getHibernateTemplate().flush();
	}

	public boolean updateScore(String submissionId){
		boolean saved = false;
		SecurityAdvisor sa =  new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function, String reference)
			{
				if(AssignmentService.SECURE_GRADE_ASSIGNMENT_SUBMISSION.equals(function)
						|| AssignmentService.SECURE_UPDATE_ASSIGNMENT.equals(function)
						|| AssignmentService.SECURE_ACCESS_ASSIGNMENT.equals(function)
						|| AssignmentService.SECURE_ACCESS_ASSIGNMENT_SUBMISSION.equals(function)){
					return SecurityAdvice.ALLOWED;
				}else{
					return SecurityAdvice.PASS;
				}
			}
		};
		try {
			securityService.pushAdvisor(sa);
			//first check that submission exists and that it can be graded/override score
			AssignmentSubmission submission = assignmentService.getSubmission(submissionId);
			//only override grades that have never been graded or was last graded by this service
			//this prevents this service from overriding instructor set grades, which take precedent.
			if(submission != null && 
					(submission.getGraded() == false || submission.getGradedBy() == null || "".equals(submission.getGradedBy().trim()) 
					|| AssignmentPeerAssessmentService.class.getName().equals(submission.getGradedBy().trim()))){
				List<PeerAssessmentItem> items = getPeerAssessmentItems(submissionId, submission.getAssignment().getContent().getFactor());
				if(items != null){
					//scores are stored w/o decimal points, so a score of 3.4 is stored as 34 in the DB
					//add all the scores together and divide it by the number of scores added.  Then round.
					Integer totalScore = 0;
					int denominator = 0;
					for(PeerAssessmentItem item : items){
						if(!item.isRemoved() && item.getScore() != null){
							totalScore += item.getScore();
							denominator++;
						}
					}
					if(denominator > 0){
						totalScore = Math.round(totalScore/denominator);
					}else{
						totalScore = null;
					}
					String totleScoreStr = null;
					if(totalScore != null){
						totleScoreStr = totalScore.toString();
					}
					boolean changed = false;
					if((totleScoreStr == null || "".equals(totleScoreStr)) && (submission.getGrade() == null || "".equals(submission.getGrade()))){
						//scores are both null, nothing changed
					}else if((totleScoreStr != null && !"".equals(totleScoreStr)) && (submission.getGrade() == null || "".equals(submission.getGrade()))){
						//one score changed, update
						changed = true;
					}else if((totleScoreStr == null || "".equals(totleScoreStr)) && (submission.getGrade() != null && !"".equals(submission.getGrade()))){
						//one score changed, update
						changed = true;
					}else if(!totleScoreStr.equals(submission.getGrade())){
						changed = true;
					}
					if(changed){
						AssignmentSubmissionEdit edit = assignmentService.editSubmission(submissionId);
						edit.setGrade(totleScoreStr);
						edit.setGraded(true);
						edit.setGradedBy(AssignmentPeerAssessmentService.class.getName());
						edit.setGradeReleased(false);
						assignmentService.commitEdit(edit);
						saved = true;
					}
				}
			}
		} catch (IdUnusedException e) {
			log.error(e.getMessage(), e);
		} catch (InUseException e) {
			log.error(e.getMessage(), e);
		} catch (PermissionException e) {
			log.error(e.getMessage(), e);
		}finally
		{
			// remove advisor
			if(sa != null){
				securityService.popAdvisor(sa);
			}
		}
		return saved;
	}
	
	public void setScheduledInvocationManager(
			ScheduledInvocationManager scheduledInvocationManager)
	{
		this.scheduledInvocationManager = scheduledInvocationManager;
	}
	public void setTimeService(TimeService timeService)
	{
		this.timeService = timeService;
	}
	public void setAssignmentService(AssignmentService assignmentService){
		this.assignmentService = assignmentService;
	}

	public SecurityService getSecurityService() {
		return securityService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public SessionManager getSessionManager() {
		return sessionManager;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
}
