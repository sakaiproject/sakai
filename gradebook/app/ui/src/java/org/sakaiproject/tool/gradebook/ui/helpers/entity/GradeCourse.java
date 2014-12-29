package org.sakaiproject.tool.gradebook.ui.helpers.entity;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import org.sakaiproject.site.api.Site;

@Data
public class GradeCourse {
	
	protected String siteId;
	protected String siteName;
	
	protected List<GradeAssignmentItem> assignments;
	
	public GradeCourse (Site site)
	{
		siteId = site.getId();
		siteName = site.getTitle();
		
		assignments = new ArrayList<GradeAssignmentItem> ();
	}

}
