package org.sakaiproject.gradebookng.tool.model;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.user.api.User;

public class StudentGrades implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Getter
	private String studentUuid;
	
	@Getter
	private String studentName;
	
	@Getter
	private String studentEid;
	
	@Getter @Setter
	private String courseGrade;
	
	@Getter @Setter
	private List<String> assignments;
	
	public StudentGrades(){
	}
	
	public StudentGrades(User u) {
		this.studentUuid = u.getId();
		this.studentEid = u.getEid();
		this.studentName = u.getDisplayName();
	}
	
	
	
}
