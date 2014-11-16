package org.sakaiproject.gradebookng.tool.model;

import java.io.Serializable;
import java.util.List;

public class StudentGrades implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private long id;
	private String studentName;
	private String studentEid;
	private String courseGrade;
	
	private List<String> assignments;
	
	public StudentGrades(){
	}
	
	public StudentGrades(long id, String studentName, String studentEid, String courseGrade, List<String> assignments)
	{
		this.id = id;
		this.studentName = studentName;
		this.studentEid = studentEid;
		this.courseGrade = courseGrade;
		this.assignments = assignments;
		
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFirstName() {
		return studentName;
	}

	public void setFirstName(String firstName) {
		this.studentName = firstName;
	}
	
	public String getStudentEid() {
		return studentEid;
	}

	public void setgetStudentEid(String studentEid) {
		this.studentEid = studentEid;
	}

	public String getCourseGrade() {
		return courseGrade;
	}

	public void setCourseGrade(String courseGrade) {
		this.courseGrade = courseGrade;
	}

	
	
}
