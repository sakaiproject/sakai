package org.sakaiproject.lessonbuildertool.tool.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Collection;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.lessonbuildertool.SimplePageComment;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionResponse;
import org.sakaiproject.lessonbuildertool.SimpleStudentPage;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.GradebookIfc;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.tool.cover.SessionManager;


public class GradingBean {
	private static final Logger log = LoggerFactory.getLogger(GradingBean.class);
	public String id;
	public String points;
	public String jsId;
	public String type;
        public String csrfToken;
	
	private SimplePageToolDao simplePageToolDao;
	private GradebookIfc gradebookIfc;
	private SimplePageBean simplePageBean;
	
	public void setSimplePageToolDao(SimplePageToolDao simplePageToolDao) {
		this.simplePageToolDao = simplePageToolDao;
	}
	
	public void setGradebookIfc(GradebookIfc gradebookIfc) {
		this.gradebookIfc = gradebookIfc;
	}
	
	public void setSimplePageBean(SimplePageBean simplePageBean) {
		this.simplePageBean = simplePageBean;
	}
	
        public boolean checkCsrf() {
	    Object sessionToken = SessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
	    if (sessionToken != null && sessionToken.toString().equals(csrfToken)) {
		return true;
	    }
	    else
		return false;
	}

	public String[] getResults() {
		if(simplePageBean.getEditPrivs() != 0 || !checkCsrf()) {
			return new String[]{"failure", jsId, "-1"};
		}
		
		// Make sure they gave us a valid amount of points.
		try {
			Double.valueOf(points);
		}catch(Exception ex) {
			return new String[]{"failure", jsId, "-1"};
		}
		
		boolean r = false;
		
		if("comment".equals(type)) {
			r = gradeComment();
		}else if("student".equals(type)) {
			r = gradeStudentPage();
		}else if("question".equals(type)) {
			r = gradeQuestion();
		}
		
		if(r) {
			return new String[] {"success", jsId, String.valueOf(Double.valueOf(points))};
		}else {
			return new String[]{"failure", jsId, "-1"};
		}
	}
	
	private boolean gradeComment() {
		boolean r = false;
		
		SimplePageComment comment = simplePageToolDao.findCommentByUUID(id);
		SimplePageItem commentItem = simplePageToolDao.findItem(comment.getItemId());
		SimpleStudentPage studentPage = null;  // comments on student page only
		SimplePageItem topItem = null; // comments on student page only

		if(commentItem.getPageId() <= 0) {
		    studentPage = simplePageToolDao.findStudentPage(Long.valueOf(commentItem.getSakaiId()));
		    topItem = simplePageToolDao.findItem(studentPage.getItemId());
		    if (! simplePageBean.itemOk(topItem.getId())) {
			return false;
		    }
		} else {
		    if (! simplePageBean.itemOk(commentItem.getId())) {
			return false;
		    }
		}

		String gradebookId = null;

		Integer maxpoints = null;
		if (studentPage != null) {
		    gradebookId = topItem.getAltGradebook();
		    maxpoints = topItem.getAltPoints();
		} else {
		    gradebookId = commentItem.getGradebookId();
		    maxpoints = commentItem.getGradebookPoints();
		}

		Double newpoints = Double.valueOf(points);

		if (newpoints.equals(comment.getPoints())) {
			return true;
		}

		if (newpoints < 0.0 || newpoints > maxpoints) {
			return false;
		}

		try {
			r = gradebookIfc.updateExternalAssessmentScore(simplePageBean.getCurrentSiteId(), gradebookId, comment.getAuthor(), Double.toString(newpoints));
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
		if(r) {
			List<SimplePageComment> comments;
			if(commentItem.getPageId() > 0) {
				comments = simplePageToolDao.findCommentsOnItemByAuthor(comment.getItemId(), comment.getAuthor());
			}else {
				List<SimpleStudentPage> studentPages = simplePageToolDao.findStudentPages(studentPage.getItemId());
				List<Long> commentsItemIds = new ArrayList<Long>();
				for(SimpleStudentPage p : studentPages) {
					commentsItemIds.add(p.getCommentsSection());
				}
				
				comments = simplePageToolDao.findCommentsOnItemsByAuthor(commentsItemIds, comment.getAuthor());
			}
			
			// Make sure all of the comments by this person have the grade.
			for(SimplePageComment c : comments) {
				c.setPoints(newpoints);
				simplePageBean.update(c, false);
			}
		}
		
		return r;
	}
	
	private boolean gradeStudentPage() {
		boolean r = false;
		SimpleStudentPage page = simplePageToolDao.findStudentPage(Long.valueOf(id));
		SimplePageItem pageItem = simplePageToolDao.findItem(page.getItemId());
		Double newpoints = Double.valueOf(points);

		// the idea was to not update if there's no change in points
		// but there can be reasons to want to force grades back to the gradebook,
		// particually for group pages where the group may have changed
		//if(Double.valueOf(points).equals(page.getPoints())) {
		//  return new String[] {"success", jsId, String.valueOf(page.getPoints())};
	        //}
		
		if (page.getPageId() != simplePageBean.getCurrentPageId()) {
		    return false;
		}

		if (newpoints < 0.0 || newpoints > pageItem.getGradebookPoints()) {
			return false;
		}

		try {
		    String owner = page.getOwner();
		    String group = page.getGroup();
		    if (group == null)
			r = gradebookIfc.updateExternalAssessmentScore(simplePageBean.getCurrentSiteId(), pageItem.getGradebookId(), page.getOwner(), Double.toString(newpoints));
		    else {
			group = "/site/" + simplePageBean.getCurrentSiteId() + "/group/" + group;
			AuthzGroup g = ComponentManager.get(AuthzGroupService.class).getAuthzGroup(group);
			Set<Member> members = g.getMembers();
			// if we have more than one user, in theory some might fail and some succeed. For the
			// moment just update the grade 
			r = true;
			for (Member m: members)
			    gradebookIfc.updateExternalAssessmentScore(simplePageBean.getCurrentSiteId(), pageItem.getGradebookId(),
				       m.getUserId(), Double.toString(newpoints));
		    }
		}catch(Exception ex) {
		    log.info("Exception updating grade " + ex);
		}
		
		if(r) {
			page.setPoints(newpoints);
			simplePageBean.update(page, false);
		}
		
		return r;
	}
	
	private boolean gradeQuestion() {
		boolean r = false;
		SimplePageQuestionResponse response = simplePageToolDao.findQuestionResponse(Long.valueOf(id));
		SimplePageItem questionItem = simplePageBean.findItem(response.getQuestionId());
		Double newpoints = Double.valueOf(points);
		
		if (! simplePageBean.itemOk(questionItem.getId()))
		    return false;

		r = "true".equals(questionItem.getAttribute("questionGraded")) || questionItem.getGradebookId() != null;
		if (questionItem.getGradebookId() != null)
		    if (newpoints < 0.0 || newpoints > questionItem.getGradebookPoints()) {
			return false;
		    }
		    try {
			r = gradebookIfc.updateExternalAssessmentScore(simplePageBean.getCurrentSiteId(), questionItem.getGradebookId(), response.getUserId(), Double.toString(newpoints));
		    }catch(Exception ex) {
			log.info("Exception updating grade " + ex);
		    }
		
		if(r) {
			response.setPoints(newpoints);
			
			// Only set the answer as correct if they got the maximum number of points.
			// Unfortunately, points don't map well to the boolean correct/incorrect model,
			// but I'd rather not clutter the faculty interface with more options.
			if (questionItem.getGradebookPoints() == null || points == null)
			    return false;
			if(newpoints.equals(Double.valueOf(questionItem.getGradebookPoints()))) {
				response.setCorrect(true);
			}else {
				response.setCorrect(false);
			}
			
			response.setOverridden(true);
			simplePageBean.update(response);
		}
		
		return r;
	}
}
