/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
