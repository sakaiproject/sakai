/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/



package org.sakaiproject.tool.assessment.ui.bean.cms;

import java.util.ArrayList;
import java.io.Serializable;
import org.sakaiproject.tool.assessment.facade.AgentFacade;

/**
 * <p>Course Management Bean </p>
 * <p>Stub for now. </p>
 * <p>Copyright: Copyright (c) 2004 Sakai</p>
 * <p> </p>
 * @author Ed Smiley
 * @version $Id$
 */

public class CourseManagementBean implements Serializable
{
  private String courseName;
  private String courseIdString;
  private String instructor;
  private String instructorIdString;
  private ArrayList sectionList;
  private ArrayList groupList;
  private String syllabus;

  public CourseManagementBean(){
    courseName = AgentFacade.getCurrentSiteName();
    courseIdString = AgentFacade.getCurrentSiteId();
    instructor = "Rachel Gollub";
    instructorIdString = "rgollub";
    syllabus = "To boldly go where no assessment manager has gone before.";
    sectionList = new ArrayList();
    sectionList.add("section 1");
    sectionList.add("section 2");
    sectionList.add("section 3");
    groupList = new ArrayList();
    groupList.add("Group One");
    groupList.add("Group Two");
  }
  public String getCourseName()
  {
    return courseName;
  }
  public void setCourseName(String courseName)
  {
    this.courseName = courseName;
  }
  public String getCourseIdString()
  {
    return courseIdString;
  }
  public void setCourseIdString(String courseIdString)
  {
    this.courseIdString = courseIdString;
  }
  public String getInstructor()
  {
    return instructor;
  }
  public void setInstructor(String instructor)
  {
    this.instructor = instructor;
  }
  public String getInstructorIdString()
  {
    return instructorIdString;
  }
  public void setInstructorIdString(String instructorIdStrintg)
  {
    this.instructorIdString = instructorIdStrintg;
  }
  public java.util.ArrayList getSectionList()
  {
    return sectionList;
  }
  public void setSectionList(java.util.ArrayList sectionList)
  {
    this.sectionList = sectionList;
  }
  public java.util.ArrayList getGroupList()
  {
    return groupList;
  }
  public void setGroupList(java.util.ArrayList groupList)
  {
    this.groupList = groupList;
  }
  public String getSyllabus()
  {
    return syllabus;
  }
  public void setSyllabus(String syllabus)
  {
    this.syllabus = syllabus;
  }
}
