package org.sakaiproject.lessonbuildertool.tool.beans;

import org.sakaiproject.lessonbuildertool.SimplePageComment;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.GradebookIfc;

public class CommentsGradingBean {
	public String id;
	public String points;
	public String jsId;
	
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
			return new String[]{"failure", jsId};
		}
		
		// Make sure they gave us a valid amount of points.
		try {
			Double.valueOf(points);
		}catch(Exception ex) {
			return new String[] {"failure", jsId};
		}
		
		SimplePageComment comment = simplePageToolDao.findCommentByUUID(id);
		if(Double.valueOf(points).equals(comment.getPoints())) {
			return new String[] {"success", jsId};
		}
		
		boolean r = false;
		try {
			r = gradebookIfc.updateExternalAssessmentScore(simplePageBean.getCurrentSiteId(), "lesson-builder:comment:" + comment.getItemId(), comment.getAuthor(), Double.toString(Double.valueOf(points)));
		}catch(Exception ex) {}
		
		if(r) {
			comment.setPoints(Double.valueOf(points));
			simplePageBean.update(comment, false);
			return new String[] {"success", jsId};
		}else {
			return new String[] {"failure", jsId};
		}
	}
}
