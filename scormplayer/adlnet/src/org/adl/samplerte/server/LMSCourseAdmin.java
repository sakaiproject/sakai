/*******************************************************************************
 **
 ** Advanced Distributed Learning Co-Laboratory (ADL Co-Lab) Hub grants you 
 ** ("Licensee") a non-exclusive, royalty free, license to use, modify and 
 ** redistribute this software in source and binary code form, provided that 
 ** i) this copyright notice and license appear on all copies of the software; 
 ** and ii) Licensee does not utilize the software in a manner which is 
 ** disparaging to ADL Co-Lab Hub.
 **
 ** This software is provided "AS IS," without a warranty of any kind.  ALL 
 ** EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING 
 ** ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE 
 ** OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED.  ADL Co-Lab Hub AND ITS LICENSORS 
 ** SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF 
 ** USING, MODIFYING OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES.  IN NO 
 ** EVENT WILL ADL Co-Lab Hub OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, 
 ** PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, 
 ** INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE 
 ** THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE 
 ** SOFTWARE, EVEN IF ADL Co-Lab Hub HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH 
 ** DAMAGES.
 **
 *******************************************************************************/

package org.adl.samplerte.server;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <strong>Filename: </strong> LMSCourseAdmin <br>
 * <br>
 * 
 * <strong>Description: </strong> <br>
 * This servlet handles course administration features. <br>
 * <br>
 * 
 * <strong>Design Issues: </strong> <br>
 * This implementation is intended to be used by the SCORM Sample RTE <br>
 * <br>
 * 
 * <strong>References: </strong> <br>
 * <ul>
 * <li>IMS Simple Sequencing Specification
 * <li>SCORM 2004
 * </ul>
 * 
 * @author ADL Technical Team
 */
public class LMSCourseAdmin extends HttpServlet
{
   /**
    * String Constant for the display courses view
    */
   private static final String DSP_COURSES = "/admin/dsp_courses.jsp";

   /**
    * String Constant for the display scos view
    */
   private static final String DSP_SCOS = "/admin/dsp_scos.jsp";

   /**
    * String Constant for the display scos view
    */
   private static final String DSP_SCO = "/admin/dsp_comments.jsp";

   /**
    * String Constant for the success page view
    */
   private static final String DSP_OUTCOME = "/admin/dsp_outcome.jsp";

   /**
    * String Constant for the register course page view
    */
   private static final String DSP_REG_COURSE = "/runtime/dsp_regCourse.jsp";

   /**
    * String Constant for the view course page view
    */
   private static final String DSP_VIEWCOURSE = "/runtime/dsp_viewCourse.jsp";

   /**
    * String Constant for the select course page view
    */
   private static final String DSP_SELECTCOURSE = "/runtime/dsp_selectCourse.jsp";

   /**
    * String Constant for the view status page view
    */
   private static final String DSP_VIEWSTATUS = "/runtime/dsp_viewStatus.jsp";

   /**
    * String Constant for the create objective page view
    */
   private static final String DSP_CREATEOBJ = "/admin/dsp_createObjective.jsp";

   /**
    * String Constant for the select user page view
    */
   private static final String DSP_SELECTUSER = "/admin/dsp_selectUser.jsp";

   /**
    * String Constant for the select course obj page view
    */
   private static final String DSP_SELECTCOURSEOBJ = "/admin/dsp_selectCourseObj.jsp";

   /**
    * String Constant for the select course obj page view
    */
   private static final String DSP_OBJECTIVEADMIN = "/admin/dsp_objectivesAdmin.jsp";

   /**
    * String path of the chosen import status page
    */
   private String mDspImportStatus = "";

   /**
    * Handles the 'POST' message sent to the servlet. This servlet will handle
    * deterimining the appropriate service to invoke based on the request type.
    * 
    * @param iRequest
    *            The request 'POST'ed to the servlet.
    * 
    * @param oResponse
    *            The response returned by the servlet.
    */
   public void doPost(HttpServletRequest iRequest,
                      HttpServletResponse oResponse)
   {
      processRequest(iRequest, oResponse);
   }

   /**
    * Handles the 'GET' message sent to the servlet. This servlet will handle
    * deterimining the appropriate service to invoke based on the request type.
    * 
    * @param iRequest
    *            The request - 'GET' - to the servlet.
    * 
    * @param oResponse
    *            The response returned by the servlet.
    * 
    */
   public void doGet(HttpServletRequest iRequest, HttpServletResponse oResponse)
   {
      processRequest(iRequest, oResponse);
   }

   /**
    * Handles determining the type of request and invoking the appropriate
    * service.
    * 
    * @param iRequest
    *            The request - 'GET' - to the servlet.
    * 
    * @param oResponse
    *            The response returned by the servlet.
    * 
    */
   public void processRequest(HttpServletRequest iRequest,
                              HttpServletResponse oResponse) 
   {
      String userID = "";
      String courseID = "";
      String user = "";
      String reqOp = "";
      String result = "";
      
      CourseService courseService;
      UserService userService;
      ObjectivesData objData;
      Vector courses;
      
      try
      {
         iRequest.setCharacterEncoding("utf-8");
         //oResponse.setCharacterEncoding("utf-8");
      }
      catch (Exception e)
      {
         System.out.println("LMSCourseAdmin:processRequest - encoding exception");
         e.printStackTrace();
      }
      
      // Determine the request type coming into the Servlet
      String sType = iRequest.getParameter("type");
      if ( sType == null )
      {
         sType = "999";
      }
      int type = Integer.parseInt(sType);
  
      switch ( type )
      {
         case ServletRequestTypes.GET_COURSES:
            courseService = new CourseService();
            courses = new Vector();
            String setProcess = iRequest.getParameter("setProcess");
            courses = courseService.getCourses();
            iRequest.setAttribute("setProcess", setProcess);
            iRequest.setAttribute("courses", courses);
            launchView(DSP_COURSES, iRequest, oResponse);
            break;

         case ServletRequestTypes.GET_SCOS:
            courseService = new CourseService();
            Vector scos = new Vector();
            scos = courseService.getSCOs(iRequest.getParameter("courseID"));
            iRequest.setAttribute("scos", scos);
            launchView(DSP_SCOS, iRequest, oResponse);
            break;

         case ServletRequestTypes.GET_COMMENTS:
            courseService = new CourseService();
            SCOData sco = new SCOData();
            int id = Integer.parseInt(iRequest.getParameter("scoID"));
            sco = courseService.getSCO(id);
            iRequest.setAttribute("sco", sco);
            launchView(DSP_SCO, iRequest, oResponse);
            break;

         case ServletRequestTypes.UPDATE_SCO:
            courseService = new CourseService();
            result = "false";
            reqOp = "Updating Comments from LMS";
            int act = Integer.parseInt(iRequest.getParameter("scoID"));
            String txt = iRequest.getParameter("comments");
            String update = iRequest.getParameter("update");
            String locations = iRequest.getParameter("locations");
            result = courseService.updateSCO(act, txt, update, locations);
            iRequest.setAttribute("reqOp", reqOp);
            iRequest.setAttribute("result", result);
            launchView(DSP_OUTCOME, iRequest, oResponse);
            break;

         case ServletRequestTypes.DELETE_COURSE:
            courseService = new CourseService();
            result = "false";
            reqOp = "Delete course";
            courses = new Vector();
            String[] arrCourses = iRequest.getParameterValues("chkCourse");

            for (int i = 0; i < arrCourses.length; i++)
            {
               courses.add(arrCourses[i]);
            }
            result = courseService.deleteCourse(courses);
            iRequest.setAttribute("reqOp", reqOp);
            iRequest.setAttribute("result", result);
            launchView(DSP_OUTCOME, iRequest, oResponse);
            break;

         case ServletRequestTypes.REG_COURSE:
            courseService = new CourseService();
            userID = iRequest.getParameter("userId");
            courses = new Vector();
            String userCourses = new String();
            courses = courseService.getCourses();
            userCourses = courseService.getRegCourses(userID);
            iRequest.setAttribute("courses", courses);
            iRequest.setAttribute("userCourses", userCourses);
            launchView(DSP_REG_COURSE, iRequest, oResponse);
            break;

         case ServletRequestTypes.PROC_REG_COURSE:
            result = "false";
            reqOp = "Register Course";
            courseService = new CourseService();
            Vector courseList = new Vector();
            Enumeration enumCourses = iRequest.getParameterNames();
            String path = iRequest.getParameter("path");
            userID = iRequest.getParameter("userID");
            while (enumCourses.hasMoreElements())
            {
               courseList.add(enumCourses.nextElement());
            }
            result = courseService.updateRegCourses(courseList, path, userID);
            iRequest.setAttribute("reqOp", reqOp);
            iRequest.setAttribute("result", result);
            launchView(DSP_OUTCOME, iRequest, oResponse);
            break;

         case ServletRequestTypes.VIEW_REG_COURSE:
            courseService = new CourseService();
            userID = iRequest.getParameter("userId");
            courses = new Vector();
            courses = courseService.getCourses(userID);
            iRequest.setAttribute("courses", courses);
            launchView(DSP_VIEWCOURSE, iRequest, oResponse);
            break;

         case ServletRequestTypes.SELECT_MY_COURSE:
            courseService = new CourseService();
            userID = iRequest.getParameter("userId");
            courses = new Vector();
            courses = courseService.getCourses(userID);
            iRequest.setAttribute("courses", courses);
            iRequest.setAttribute("userId", userID);
            launchView(DSP_SELECTCOURSE, iRequest, oResponse);
            break;

         case ServletRequestTypes.VIEW_MY_STATUS:
            courseService = new CourseService();
            courseID = iRequest.getParameter("courseID");
            userID = iRequest.getParameter("userID");
            CourseData cd = courseService.showCourseStatus(courseID, userID);
            String name = courseService.getName(userID);
            iRequest.setAttribute("name", name);
            iRequest.setAttribute("status", cd);
            launchView(DSP_VIEWSTATUS, iRequest, oResponse);
            break;

         case ServletRequestTypes.CLEAR_DB:
            courseService = new CourseService();
            reqOp = "Clear Database";
            path = iRequest.getParameter("path");
            result = courseService.clearDatabase(path);
            iRequest.setAttribute("result", result);
            iRequest.setAttribute("reqOp", reqOp);
            launchView(DSP_OUTCOME, iRequest, oResponse);
            break;

         case ServletRequestTypes.NEW_OBJ:
            userService = new UserService();
            Vector users = userService.getUsers(true);
            String objErr = "";
            iRequest.setAttribute("objErr", objErr);
            iRequest.setAttribute("users", users);
            launchView(DSP_CREATEOBJ, iRequest, oResponse);
            break;

         case ServletRequestTypes.USER_OBJ:
            userService = new UserService();
            users = userService.getUsers(true);
            iRequest.setAttribute("users", users);
            launchView(DSP_SELECTUSER, iRequest, oResponse);
            break;

         case ServletRequestTypes.ADD_OBJ:
            reqOp = "Add Objective";
            userService = new UserService();
            courseService = new CourseService();
            objData = new ObjectivesData();
            objData.mUserID = iRequest.getParameter("userID");
            objData.mObjectiveID = iRequest.getParameter("objectiveID");
            objData.mSatisfied = iRequest.getParameter("satisfied");
            objData.mMeasure = iRequest.getParameter("measure");
            result = courseService.addObj(objData);
            users = userService.getUsers(true);
            if ( result.equalsIgnoreCase("true") )
            {
               iRequest.setAttribute("result", result);
               iRequest.setAttribute("reqOp", reqOp);
               launchView(DSP_OUTCOME, iRequest, oResponse);
            }
            else
            {
               iRequest.setAttribute("users", users);
               iRequest.setAttribute("userID", objData.mUserID);
               iRequest.setAttribute("objID", objData.mObjectiveID);
               iRequest.setAttribute("satisfied", objData.mSatisfied);
               iRequest.setAttribute("measure", objData.mMeasure);
               iRequest.setAttribute("objErr", "dupobjid");
               launchView(DSP_CREATEOBJ, iRequest, oResponse);
            }
            break;

         case ServletRequestTypes.COURSE_OBJ:
            user = iRequest.getParameter("user");
            courseService = new CourseService();
            courses = new Vector();
            courses = courseService.getCourses(user);
            iRequest.setAttribute("courses", courses);
            iRequest.setAttribute("user", user);
            launchView(DSP_SELECTCOURSEOBJ, iRequest, oResponse);
            break;

         case ServletRequestTypes.OBJ_ADMIN:
            String course = iRequest.getParameter("course");
            user = iRequest.getParameter("user");
            courseService = new CourseService();
            Vector objectives = courseService.getObjs(course, user);
            objectives = courseService.getGlobalObjs(user, objectives);
            iRequest.setAttribute("objs", objectives);
            launchView(DSP_OBJECTIVEADMIN, iRequest, oResponse);
            break;

         case ServletRequestTypes.EDIT_OBJ:
            reqOp = "Edit Objectives";
            Vector requestList = new Vector();
            Enumeration requestNames = iRequest.getParameterNames();
            while (requestNames.hasMoreElements())
            {
               String paramName = (String)requestNames.nextElement();
               // If the parameter is not the submit button
               if ( !(paramName.equals("submit")) &&
                    !(paramName.equals("type")) )
               {
                  String paramValue = iRequest.getParameter(paramName);
                  String param = paramName + ":" + paramValue;
                  requestList.add(param);
               }
            }
            courseService = new CourseService();
            result = courseService.editObjs(requestList);
            iRequest.setAttribute("result", result);
            iRequest.setAttribute("reqOp", reqOp);
            launchView(DSP_OUTCOME, iRequest, oResponse);
            break;

         case ServletRequestTypes.IMPORT_COURSE:
            String sessionID = iRequest.getParameter("sessID");
           
            String webPath = 
               this.getServletConfig().getServletContext().getRealPath("/");
            courseService = new CourseService();
            ValidationResults validationResult = 
                     courseService.importCourse(iRequest, webPath, sessionID);
            mDspImportStatus = validationResult.getRedirectView();
            iRequest.setAttribute("result", validationResult);
            launchView(mDspImportStatus, iRequest, oResponse);
            break;

         default:
            // Todo -- put in the error page.
            System.out.println("Default Case -- LMSCourseAdmin.java -- Error");
      }
   }

   /**
    * Private method used to centralize the jsp dispatch code
    * 
    * @param iJsp the path to the view to be displayed
    * @param iRequest the request object
    * @param iResponse the response object
    *
    * */
   private void launchView(String iJsp, HttpServletRequest iRequest, 
                           HttpServletResponse iResponse)
   {
      try
      {
         RequestDispatcher rd = getServletContext().getRequestDispatcher(iJsp);
         rd.forward(iRequest,iResponse);
      }
      catch(ServletException se)
      {
         System.out.println("LMSCourseAdmin:launchView - servlet exception");
         se.printStackTrace();
      }
      catch(IOException ioe)
      {
         System.out.println("LMSCourseAdmin:launchView - io exception");
         ioe.printStackTrace();
      }
   }
}