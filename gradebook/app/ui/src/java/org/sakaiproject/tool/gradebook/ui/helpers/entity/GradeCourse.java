package org.sakaiproject.tool.gradebook.ui.helpers.entity;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.site.api.Site;

import lombok.Getter;
import lombok.Setter;

public class GradeCourse {
	
	@Getter
	@Setter
	private String siteId;
	
	@Getter
	@Setter
	private String siteName;
	
	@Getter
	@Setter
	private List<GradeAssignmentItem> assignments;
	
	public GradeCourse (Site site) {
		this.siteId = site.getId();
		this.siteName = site.getTitle();
		this.assignments = new ArrayList<GradeAssignmentItem>();
	}

}
