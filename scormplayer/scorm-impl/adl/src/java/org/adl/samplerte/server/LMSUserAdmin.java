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
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.adl.api.ecmascript.APIErrorManager;
import org.adl.datamodels.datatypes.LangStringValidator;
import org.adl.datamodels.datatypes.RealRangeValidator;


/**
 * The LMSUserAdmin class handles the administration of user information.  This 
 * inforamtion includes the users password and the cmi.learner_preferences 
 * data model elements, audio_level, audio_captioning, delivery_speed, and
 * language.<br><br>
 * 
 * <strong>Filename:</strong> LMSUserAdmin.java<br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the SCORM 2004 3rd Edition Sample RTE <br>
 * <br>
 * 
 * <strong>References:</strong><br>
 * <ul>
 *     <li>IMS SS Specification
 *     <li>SCORM 2004 3rd Edition 
 * </ul>
 * 
 * @author ADL Technical Team
 */
public class LMSUserAdmin extends HttpServlet
{
   
   /**
    * The int that needs passed into APIErrorManager to get the right set of
    * error codes, error messages, and error diagnostic information for SSP
    */
   private static final int SSPERRORMGR = 2;
   
   /**
    * String Constant for the display users view
    */ 
   private static final String DSP_USERS = "/admin/dsp_users.jsp";

   /**
    * String Constant for the display user information view
    */ 
   private static final String DSP_USERPROFILE = "/admin/dsp_userProfile.jsp";

   /**
    * String Constant for the new user page view
    */ 
   private static final String DSP_NEWUSER = "/admin/newUser.jsp";

   /**
    * String Constant for the ssp bucket page view
    */ 
   private static final String DSP_BUCKETVIEW = "/admin/dsp_bucketView.jsp";
   
   /**
    * String Constant for the add ssp bucket page view
    */ 
   private static final String DSP_ADDBUCKET = "/admin/dsp_addBucket.jsp";
   
   /**
    * String Constant for the success page view
    */ 
   private static final String DSP_OUTCOME = "/admin/dsp_outcome.jsp";

   /**
    * This sets up Java Logging
    */
   private Logger mLogger = Logger.getLogger("org.adl.util.debug.samplerte");
   
   /**
    * This method handles the 'POST' message sent to the servlet.  This servlet
    * will handle determining the appropriate service to invoke based on the
    * request type.  
    * 
    * @param iRequest  The request 'POST'ed to the servlet.
    * 
    * @param oResponse The response returned by the servlet.
    * 
    */
   public void doPost(HttpServletRequest iRequest, HttpServletResponse oResponse) 
   {
      mLogger.entering("---LMSAdmin", "doPost()");
      mLogger.info("POST received by LMSAdmin");      
      processRequest(iRequest, oResponse);
   }

   /**
    * This method handles the 'GET' message sent to the servlet.  This servlet
    * will handle determining the appropriate service to invoke based on the
    * request type.  
    * 
    * @param iRequest  The request - 'GET' - to the servlet.
    * 
    * @param oResponse The response returned by the servlet.
    */
   public void doGet(HttpServletRequest iRequest, 
                     HttpServletResponse oResponse)
   {	   
      processRequest(iRequest, oResponse);
   }

   /**
    * Processes the request sent to the servlet.
    * 
    * @param iRequest The request posted to the servlet
    * @param oResponse the response returned by the servlet
    */
   private void processRequest(HttpServletRequest iRequest, HttpServletResponse oResponse)
   {
      mLogger.info("LMSAdmin - Entering processRequest()");
      String reqOp = "";
      String result = "";
      UserService userService;
      UserProfile userProfile;
      SSPService sspService;
      
      try
      {
         iRequest.setCharacterEncoding("utf-8");
         //oResponse.setCharacterEncoding("utf-8");
      }
      catch (Exception e)
      {
         System.out.println("LMSUserAdmin:processRequest - encoding exception");
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
         case ServletRequestTypes.GET_PREF: 
            userService = new UserService();
            userProfile = new UserProfile();
            userProfile = userService.getUser(iRequest.getParameter("userId"));          
            
            // Send the results to the JSP view
            iRequest.setAttribute("userProfile", userProfile);
            launchView(DSP_USERPROFILE, iRequest, oResponse);
            break;

         case ServletRequestTypes.GET_USERS:
            userService = new UserService();
            Vector userProfiles = new Vector();
            userProfiles = userService.getUsers(true);
            String setProcess = iRequest.getParameter("setProcess");
            iRequest.setAttribute("setProcess",setProcess);
            iRequest.setAttribute("userProfiles", userProfiles);
            launchView(DSP_USERS, iRequest, oResponse);
            break;

          case ServletRequestTypes.NEW_USER:
             reqOp = "new_user";
             iRequest.setAttribute("result","true");
             iRequest.setAttribute("reqOp",reqOp);
             launchView(DSP_NEWUSER, iRequest, oResponse);
             break;


         case ServletRequestTypes.ADD_USERS:
            userService = new UserService();
            result = "";
            reqOp = "Add User";            
            Vector currentProfiles = new Vector();
            currentProfiles = userService.getUsers(false);                       
            boolean duplicate = false;            
            userProfile = new UserProfile();
            userProfile.mAdmin = 
                (iRequest.getParameter("admin")).equalsIgnoreCase("true");
            userProfile.mFirstName = iRequest.getParameter("firstName");
            userProfile.mLastName = iRequest.getParameter("lastName");
            userProfile.mPassword = iRequest.getParameter("password");
            userProfile.mUserID = iRequest.getParameter("userID");
            
            // Compare new user to current list of active users
            int i = 0; 
            String oldID = new String();
            UserProfile iUserProfile = new UserProfile();
            while ( ! duplicate && i < currentProfiles.size() )             
            {                                    
                iUserProfile = (UserProfile)currentProfiles.elementAt(i++);
                oldID = iUserProfile.mUserID;
                if ( oldID.equals( userProfile.mUserID) ) 
                {
                    duplicate = true;
                }
            }            
            
            if ( duplicate == true) 
            {   
                reqOp = "duplicate_user";
                iRequest.setAttribute("result","false");
                iRequest.setAttribute("reqOp",reqOp);
                iRequest.setAttribute("userProfile", userProfile);
                launchView(DSP_NEWUSER, iRequest, oResponse);
            }
            else
            {
                result = userService.addUser(userProfile);
                iRequest.setAttribute("result",result);
                iRequest.setAttribute("reqOp",reqOp);
                launchView(DSP_OUTCOME, iRequest, oResponse);
            }
            break;

         case ServletRequestTypes.UPDATE_PREF:
            boolean validationError = false;
            result = "false";
            String errorHeader = "Please correct the following fields:  ";
            String errorMsg = "";
            userProfile = new UserProfile();
            reqOp = "Update Profile";
            userProfile.mFirstName = iRequest.getParameter("firstName");
            userProfile.mLastName = iRequest.getParameter("lastName");
            userProfile.mUserID = iRequest.getParameter("userID");
            userProfile.mAudioLevel = iRequest.getParameter("audioLevel");
            userProfile.mAudioCaptioning = iRequest.getParameter("audioCaptioning");
            userProfile.mDeliverySpeed = iRequest.getParameter("deliverySpeed");
            userProfile.mLanguage = iRequest.getParameter("language");             
            userProfile.mPassword = iRequest.getParameter("password");
            String mAdminString = iRequest.getParameter("admin");
            userProfile.mAdmin = mAdminString.equals("true");
            RealRangeValidator rrv = 
                                  new RealRangeValidator(new Double(0.0), null);

            if ( (userProfile.mAudioLevel == null) || 
                 (userProfile.mAudioLevel.length() == 0) ||
                 (!(rrv.validate(userProfile.mAudioLevel) == 0)) )
            {
               validationError = true;
               errorMsg += "<br>cmi.learner_preference.audio_level must be "; 
               errorMsg += "a real number greater than or equal to 0.";  
            }

            if ( (userProfile.mDeliverySpeed == null) || 
                 (userProfile.mDeliverySpeed.length() == 0) ||
                 (!(rrv.validate(userProfile.mDeliverySpeed) == 0)) )
            {
               validationError = true;
               errorMsg += "<br>cmi.learner_preference.delivery_speed must"; 
               errorMsg += " be a real number greater than or equal to 0.";
            }

            LangStringValidator lsv = new LangStringValidator();

            if ( !(userProfile.mLanguage.trim().equals("")) &&
                 (!(lsv.validate(userProfile.mLanguage) == 0 )) )
            {
               validationError = true;
               errorMsg += "<br>cmi.learner_preference.language must be "; 
               errorMsg += "a valid SCORM 2004 3rd Edition language type or blank.";    
            }

            if ( (userProfile.mAudioCaptioning == null) || 
                 (userProfile.mAudioCaptioning.length() == 0) ||
                 ( (userProfile.mAudioCaptioning.compareTo("0")!= 0) &&
                   (userProfile.mAudioCaptioning.compareTo("1")!= 0) &&
                   (userProfile.mAudioCaptioning.compareTo("-1")!= 0) ) )
            {
               validationError = true;
               errorMsg += "<br>cmi.learner_preference.audio_captioning "; 
               errorMsg += "can only contain the values -1, 0, 1.";       
            }

            if ( (userProfile.mPassword == null) || 
                 (userProfile.mPassword.length() == 0) ||  
                 (userProfile.mPassword.trim().equals("")) )
            {
               validationError = true;
               errorMsg += "<br>Password cannot be empty";
            }

            if ( validationError )
            {
               iRequest.setAttribute("errorMsg", errorMsg);
               iRequest.setAttribute("errorHeader", errorHeader);
               iRequest.setAttribute("userProfile", userProfile);
               launchView(DSP_USERPROFILE, iRequest, oResponse);
            }
            else
            {
               userService = new UserService();
               result = userService.updateUser(userProfile);
               iRequest.setAttribute("reqOp", reqOp);
               iRequest.setAttribute("result", result);
               launchView(DSP_OUTCOME, iRequest, oResponse);
            }
            break;

         case ServletRequestTypes.DELETE_USERS:
            userService = new UserService();
            reqOp = "Delete user";
            String uID = iRequest.getParameter("userId");
            String delRes = userService.deleteUser(uID);
            iRequest.setAttribute("reqOp", reqOp);
            iRequest.setAttribute("result", delRes);
            launchView(DSP_OUTCOME, iRequest, oResponse);
            break;
            
         case ServletRequestTypes.LIST_BUCKETS:
            String lbUserId = iRequest.getParameter("userId");
            sspService = new SSPService();
            List bucketList = sspService.getBuckets(lbUserId);
            
            /*
             *  need to send userId and bucketList on to next page
             *  didn't setAttribute userId because it was already 
             *  set from the previous page
             */
            iRequest.setAttribute("userId", lbUserId);
            iRequest.setAttribute("bucketList", bucketList);
            launchView(DSP_BUCKETVIEW, iRequest, oResponse);
            break;
         
         case ServletRequestTypes.ADD_BUCKET_REQ:
            String tempUserId = iRequest.getParameter("userID");
            iRequest.setAttribute("userId", tempUserId);
            launchView(DSP_ADDBUCKET, iRequest, oResponse);
            break;
         
         case ServletRequestTypes.ADD_BUCKET:
            reqOp = "Add bucket";
            BucketProfile bucketProfile;
            sspService = new SSPService();
            
            bucketProfile = new BucketProfile();
            if ( !iRequest.getParameter("userID").equals("") )
            {
               bucketProfile.mUserID = iRequest.getParameter("userID");
            }
            if ( !iRequest.getParameter("bucketID").equals("") )
            {
               bucketProfile.mBucketID = iRequest.getParameter("bucketID");
            }
            if ( !iRequest.getParameter("requested").equals("") )
            {
               bucketProfile.mRequested = iRequest.getParameter("requested");
            }
            if ( !iRequest.getParameter("minimum").equals("") )
            {
               bucketProfile.mMinimum = iRequest.getParameter("minimum");
            }
            if ( !iRequest.getParameter("reducible").equals("") )
            {
               bucketProfile.mReducible = iRequest.getParameter("reducible");
            }
            if ( !iRequest.getParameter("persistence").equals("") )
            {
               bucketProfile.mPersistence = Integer.parseInt(iRequest.getParameter("persistence"));
            }
            if ( !iRequest.getParameter("bucketType").equals("") )
            {
               bucketProfile.mType = iRequest.getParameter("bucketType");
            }
            if ( !iRequest.getParameter("courseID").equals("") )
            {
               bucketProfile.mCourseID = iRequest.getParameter("courseID");
            }
            if ( !iRequest.getParameter("scoID").equals("") )
            {
               bucketProfile.mSCOID = iRequest.getParameter("scoID");
            }
            
            bucketProfile.mAttemptID = "1";
            
            int addBucketResult = sspService.addBucket(bucketProfile);
            String errorDescription = "";
            String errorDiagnostic = "";
            String successString = "true";
            if ( addBucketResult != 0 )
            {
               APIErrorManager erM = new APIErrorManager(SSPERRORMGR);
               errorDescription = 
                  erM.getErrorDescription(Integer.toString(addBucketResult));
               errorDiagnostic = erM.getErrorDiagnostic(Integer.toString(addBucketResult));
               successString = "false";
            }
            iRequest.setAttribute("errorCode", new Integer(addBucketResult));
            iRequest.setAttribute("errorDesc", errorDescription);
            iRequest.setAttribute("errorDiag", errorDiagnostic);
            iRequest.setAttribute("reqOp", reqOp);
            iRequest.setAttribute("result", successString);
            
            launchView(DSP_OUTCOME, iRequest, oResponse);
            break;
         
         case ServletRequestTypes.DELETE_BUCKET:
            reqOp = "Delete bucket";
         
            sspService = new SSPService();
         
            String tempActivityID = iRequest.getParameter("bID");
            
            String deleteBucketResult = sspService.deleteBucket(tempActivityID);

            iRequest.setAttribute("reqOp", reqOp);
            iRequest.setAttribute("result", deleteBucketResult);
            
            launchView(DSP_OUTCOME, iRequest, oResponse);
            break;

         default: 
            // Todo -- put in the error page.
            System.out.println("Default Case -- LMSUserAdmin.java -- Error");
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