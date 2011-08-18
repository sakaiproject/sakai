package org.sakaiproject.lessonbuildertool.tool.beans;

import java.util.List;

import org.sakaiproject.lessonbuildertool.SimplePageComment;
import org.sakaiproject.lessonbuildertool.SimpleStudentPage;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.GradebookIfc;

public class GradingBean {
	public String id;
	public String points;
	public String jsId;
	public String type;
	
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
	
	public String[] getResults() {
		if(simplePageBean.getEditPrivs() != 0) {
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
			SimplePageComment comment = simplePageToolDao.findCommentByUUID(id);
			if(Double.valueOf(points).equals(comment.getPoints())) {
				return new String[] {"success", jsId, String.valueOf(comment.getPoints())};
			}
			
			try {
				r = gradebookIfc.updateExternalAssessmentScore(simplePageBean.getCurrentSiteId(), "lesson-builder:comment:" + comment.getItemId(), comment.getAuthor(), Double.toString(Double.valueOf(points)));
			}catch(Exception ex) {}
			
			if(r) {
				List<SimplePageComment> comments = simplePageToolDao.findCommentsOnItemByAuthor(comment.getItemId(), comment.getAuthor());
				
				// Make sure all of the comments by this person have the grade.
				for(SimplePageComment c : comments) {
					c.setPoints(Double.valueOf(points));
					simplePageBean.update(c, false);
				}
			}
		}else if("student".equals(type)) {
			SimpleStudentPage page = simplePageToolDao.findStudentPage(Long.valueOf(id));
			if(Double.valueOf(points).equals(page.getPoints())) {
				return new String[] {"success", jsId, String.valueOf(page.getPoints())};
			}
			
			try {
				r = gradebookIfc.updateExternalAssessmentScore(simplePageBean.getCurrentSiteId(), "lesson-builder:page:" + page.getItemId(), page.getOwner(), Double.toString(Double.valueOf(points)));
			}catch(Exception ex) {}
			
			if(r) {
				page.setPoints(Double.valueOf(points));
				simplePageBean.update(page, false);
			}
		}
		
		if(r) {
			return new String[] {"success", jsId, String.valueOf(Double.valueOf(points))};
		}else {
			return new String[]{"failure", jsId, "-1"};
		}
	}
}
