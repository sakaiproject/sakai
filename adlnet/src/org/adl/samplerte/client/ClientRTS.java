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

package org.adl.samplerte.client;

import java.applet.Applet;
import java.net.URL;

import org.adl.api.ecmascript.APIErrorCodes;
import org.adl.api.ecmascript.APIErrorManager;
import org.adl.api.ecmascript.SCORM13APIInterface;
import org.adl.datamodels.DMErrorCodes;
import org.adl.datamodels.DMInterface;
import org.adl.datamodels.DMProcessingInfo;
import org.adl.datamodels.SCODataManager;
import org.adl.datamodels.nav.SCORM_2004_NAV_DM;
import org.adl.samplerte.server.LMSCMIServletRequest;
import org.adl.samplerte.server.LMSCMIServletResponse;
import org.adl.util.MessageCollection;

import netscape.javascript.JSObject;

/**
 * <strong>Filename:</strong>ClientRTS<br><br>
 *
 * <strong>Description:</strong><br>
 *
 * This class implements the ADL Sharable Content Object Reference Model (SCORM)
 * Version 2004 Sharable Content Object (SCO) to Learning Management System
 * (LMS) communication API defined by the Institute of Electrical and
 * Electronics Engineers (IEEE).  It is intended to be an example only and as
 * such several simplifications have been made.  It is not intended to be a
 * complete LMS implementation.<br><br>
 * 
 * This class is implemented as an applet running in a web-based client/server
 * LMS.  The applet runs within the context of the LMS provided client.  It
 * was developed and tested using IE5 or IE6 and the Sun Java Runtime
 * Environment Standard Edition Version 1.4.<br>
 *
 * The applet interacts with a server-side component.  The server component is
 * implemented as a Java Servlet and handles persistence of the data model.<br>
 *
 * Currently available web technologies provide many ways in which an LMS could
 * be implemented to be compliant with the communication mechanisms described
 * in the SCORM.  This is just one example.<br>
 *
 * Since the intended usage of this API is via LiveConnect from ECMAScript
 * (JavaScript), values are returned from the public LMS functions
 * to the caller as String objects.  The ECMAScript caller will see the
 * return values as JavaScript String objects.<br><br>
 *
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the SCORM Version 2004 3rd Edition.
 * Sample RTE. <br> <br>
 *
 * <strong>Implementation Issues:</strong><br>
 * This is a faceless Applet.  No user interface is provided<br><br>
 *
 * <strong>Known Problems:</strong><br>
 * 1. In several instances, the parameters to the API functions are checked
 * for a value of "null" because the Java Plug-in converts an empty string ("")
 * to <code>null</code>.  This is only a workaround.  The expected parameters
 * are "" where stated in SCORM and not <code>null</code>.<br><br>
 *
 * <strong>Side Effects:</strong><br><br>
 *
 * <strong>References:</strong><br>
 * <ul>
 *     <li>IMS Simple Sequencing Specification</li>
 *     <li>SCORM 2004 3rd Edition</li>
 *     <li>IEEE P1484.11.1 Draft 1/WD 13 Draft Standard for Learning Technology
 *            Data Model for Content Object Communication.
 *            Available at: http://ltsc.ieee.org/</li>
 *     <li>IEEE P1484.11.2 Draft 4 Standard for Learning Technology
 *            ECMAScript Application Programming Interface for Content to
 *            Runtime Services Communication.
 *            Available at: http://ltsc.ieee.org/</li>
 * </ul>
 *
 * @author ADL Technical Team
 */
public class ClientRTS extends Applet implements SCORM13APIInterface
{

   /**
    * The public version attribute of the SCORM API.
    */
   public static final String version = "1.0";
   
   /**
    * String value for the cmi.completion_status data model element.
    */
   private static final String COMPLETION_STATUS = "cmi.completion_status";

   /**
    * String value for the cmi.success_status data model element.
    */
   private static final String SUCCESS_STATUS = "cmi.success_status";

   /**
    * String value for the cmi.completion_threshold data model element.
    */
   private static final String COMPLETION_THRESHOLD = "cmi.completion_threshold";

   /**
    * String value for the cmi.progress_measure data model element.
    */
   private static final String PROGRESS_MEASURE = "cmi.progress_measure";

   /**
    * String value for the cmi.scaled_passing_score data model element.
    */
   private static final String SCALED_PASSING_SCORE = "cmi.scaled_passing_score";

   /**
    * String value for the cmi.scaled_score data model element.
    */
   private static final String SCORE_SCALED = "cmi.score.scaled";
   
   /**
    * String value of FALSE for JavaScript returns.
    */
   private static final String STRING_FALSE = "false";

   /**
    * String value of TRUE for JavaScript returns.
    */
   private static final String STRING_TRUE = "true";

   /**
    * This controls display of log messages to the java console.
    */
   private static boolean _Debug = false;
   
   /**
    * Indicates if the SCO is in an 'initialized' state.
    */
   private static boolean mInitializedState = false;
   
   /**
    * Public flag for API and Datamodel Logging.
    */
   public boolean logging_on = false;
   
   /**
    * Provides all LMS Error reporting.
    */
   private APIErrorManager mLMSErrorManager = null;

   /**
    * The current run-time data model values.
    */
   private SCODataManager mSCOData = null;

   /**
    * Indicates if the SCO is in a 'terminated' state.
    */
   private boolean mTerminatedState = false;

   /**
    * Indicates if the SCO is in a 'terminated' state.
    */
   private boolean mTerminateCalled = false;

   /**
    * URL to the location of the <code>LMSCMIServlet</code>.
    */
   private URL mServletURL = null;

   /**
    * ID of the activity associated with the currently launched content.
    */
   private String mActivityID = null;

   /**
    * ID of the activity's associated run-time data.
    */
   private String mStateID = null;

   /**
    * ID of the student experiencing the currently launched content.
    */
   private String mUserID = null;

   /**
    * Name of the student experiencing the currently launched content.
    */
   private String mUserName = null;

   /**
    * ID of the course of which the currently experienced activity is a part.
    */
   private String mCourseID = null;

   /**
    * tempURL of the SSP server side code.
    */
   private String mSSPURL = null;

   /**
    * Indicates number of the current attempt.
    */
   private long mNumAttempts = 0L;

   /**
    * Indicates if the current SCO is SCORM 2004 3rd Edition Version 1.0.
    */
   private boolean mScoVer2 = false;

   /**
    * Indicates if the Suspend button was pushed.
    */
   private boolean mLMSSuspendAllPushed = false;

   /**
    * Initializes the applet's state.
    */
   public void init()
   {
      // We assume at this point that the user has successfully logged in
      // to the LMS.

      if( _Debug )
      {
         System.out.println("In API::init()(the applet Init method)");
      }

      mTerminatedState = false;
      mInitializedState = false;
      mTerminateCalled = false;

      mLMSErrorManager = new APIErrorManager(APIErrorManager.SCORM_2004_API);

      mScoVer2 = false;

      URL codebase = getCodeBase();
      String host = codebase.getHost();
      String protocol = codebase.getProtocol();
      int port = codebase.getPort();

      if( _Debug )
      {
         System.out.println("codebase url is " + codebase.getPath().toString());
      }

      try
      {
         // Set the URL location of the SSP server side code
         mSSPURL = protocol + "://" + host + ":" + port + "/adl/SSPServer";

         mServletURL = new URL(protocol + "://" + host + ":" + port + "/adl/lmscmi");

         if( _Debug )
         {
            System.out.println("servlet url is " + mServletURL.toString());
         }
      }
      catch( Exception e )
      {
         if( _Debug )
         {
            System.out.println("ERROR in INIT");
         }
         e.printStackTrace();

         // We shouldn't proceed if we catch an exception here...
         stop();
      }

   }

   /**
    * Provides a string describing the the API applet class.
    *
    * @return API Applet information string.
    */
   public String getAppletInfo()
   {
      return "Title: Sample RTE Client Component \nAuthor: ADL TT\n"
         + "This Applet contains an example implementation of the SCORM 2004 3rd Edition API.";
   }

   /**
    * Provides information about this applet's parameters.
    *
    * @return String containing information about the applet's parameters.
    */
   public String[][] getParameterInfo()
   {
      String[][] info = { { "None", "", "This applet requires no parameters." } };

      return info;
   }

   /**
    * Confirms that the communication session has been initialized
    * (<code>LMSInitialize </code> or <code>Initialize</code> has been called).
    *
    * @return <code>true</code> if <code>LMSInitialize</code> or
    * <code>Initialize</code> has been called otherwise <code>false</code>.
    */
   private boolean isInitialized()
   {
      if( ( !mInitializedState ) && ( mScoVer2 ) )
      {

         mLMSErrorManager.setCurrentErrorCode(DMErrorCodes.GEN_GET_FAILURE);
      }

      return mInitializedState;
   }

   /**
    * Initiates the communication session.
    * It is used by SCORM 2004 3rd Edition SCOs.  The LMSCMIServlet is contacted
    * via LMSCMIServletRequest, ServletProxy and ServletWriter objects.
    * The LMSCMIServlet opens or initializes data model files and returns a
    * copy of any initialized data model elements to the applet.
    *
    * @param iParam  ("") - empty characterstring.
    * An empty characterstring shall be passed as a parameter.
    *
    * @return The function can return one of two values.  The return value
    * shall be represented as a characterstring.<br>
    * <ul>
    *    <li><code>true</code> - The characterstring "true" shall be returned 
    *    if communication session initialization, as determined by the LMS, was 
    *    successful.</li>
    *    <li><code>false</code> - The characterstring "false" shall be returned
    *    if communication session initialization, as determined by the LMS, was
    *    unsuccessful.</li>
    * </ul>
    */
   public String Initialize(String iParam)
   {
      // This function must be called by a SCO before any other
      // API calls are made.   It can not be called more than once
      // consecutively unless Terminate is called.

      if( _Debug )
      {
         System.out.println("*********************");
         System.out.println("In API::Initialize");
         System.out.println("*********************");
         System.out.println("");
      }
      String paramVal = new String();
      String evalVal = new String();
      if( logging_on )
      {
         paramVal = "Called Initialize ";
         evalVal = "display_log(\"" + paramVal + "\");";
         jsCall(evalVal);
      }

      // Assume failure
      String result = STRING_FALSE;

      if( mTerminatedState )
      {
         mLMSErrorManager.setCurrentErrorCode(APIErrorCodes.CONTENT_INSTANCE_TERMINATED);
         if( logging_on )
         {

            paramVal = "Initialize Returned Error Code " + APIErrorCodes.CONTENT_INSTANCE_TERMINATED;
            evalVal = "display_log(\"" + paramVal + "\");";
            jsCall(evalVal);
         }

         return result;
      }

      mTerminatedState = false;
      mTerminateCalled = false;

      mScoVer2 = false;

      // Make sure param is empty string "" - as per the API spec
      // Check for "null" is a workaround described in "Known Problems"
      // in the header.
      String tempParm = String.valueOf(iParam);

      if( ( tempParm == null || tempParm.equals("") ) != true )
      {
         mLMSErrorManager.setCurrentErrorCode(DMErrorCodes.GEN_ARGUMENT_ERROR);
      }

      // If the SCO is already initialized set the appropriate error code
      else if( mInitializedState )
      {
         mLMSErrorManager.setCurrentErrorCode(APIErrorCodes.ALREADY_INITIALIZED);
      }
      else
      {
         
         mLMSSuspendAllPushed = false;        
         
         LMSCMIServletRequest request = new LMSCMIServletRequest();

         // Build the local LMSCMIServlet Request Object to serialize
         // across the socket
         request.mActivityID = mActivityID;
         request.mStateID = mStateID;
         request.mStudentID = mUserID;
         request.mUserName = mUserName;
         request.mCourseID = mCourseID;
         request.mRequestType = LMSCMIServletRequest.TYPE_INIT;

         Long longObj = new Long(mNumAttempts);
         request.mNumAttempt = longObj.toString();
         request.mSSPServerLocation = mSSPURL;

         if( _Debug )
         {
            System.out.println("Trying to get SCO Data from servlet...");
            System.out.println("LMSCMIServlet Request contains: ");
            System.out.println("Activity ID: " + request.mActivityID);
            System.out.println("State ID: " + request.mStateID);
            System.out.println("User ID: " + request.mStudentID);
            System.out.println("Course ID: " + request.mCourseID);
         }

         ServletProxy proxy = new ServletProxy(mServletURL);

         LMSCMIServletResponse response = proxy.postLMSRequest(request);

         // Get the SCODataManager from the servlet response object
         mSCOData = response.mActivityData;

         SCORM_2004_NAV_DM navDM = (SCORM_2004_NAV_DM)mSCOData.getDataModel("adl");

         navDM.setValidRequests(response.mValidRequests);

         mInitializedState = true;

         // No errors were detected
         mLMSErrorManager.clearCurrentErrorCode();

         result = STRING_TRUE;
      }
      if( logging_on )
      {

         paramVal = "Initialize Returned Error Code " + mLMSErrorManager.getCurrentErrorCode();
         evalVal = "display_log(\"" + paramVal + "\");";
         jsCall(evalVal);
      }

      if( _Debug )
      {
         System.out.println("");
         System.out.println("*******************************");
         System.out.println("Done Processing Initialize()");
         System.out.println("*******************************");
      }

      return result;
   }

   /**
    * Terminates the communication session.  It is used
    * by a SCORM Version 2004 3rd Edition SCO when the SCO has determined that it no longer
    * needs to communicate with the LMS.  The Terminiate() function also shall
    * cause the persistence of any data (i.e., an implicit Commit("") call) set
    * by the SCO since the last successful call to Initialize("") or Commit(""),
    * whichever occurred most recently.  This guarantees to the SCO that all
    * data set by the SCO has been persisted by the LMS.
    * If the SCO has set a nav.event, the navigation event is communicated
    * to the Web browser through LiveConnect.
    *
    * @param iParam ("") - empty characterstring.  An empty characterstring
    * shall be passed as a parameter.
    *
    * @return The method can return one of two values.  The return value shall
    * be represented as a characterstring.
    * <ul>
    *    <li><code>true</code> - The characterstring "true" shall be returned 
    *    if termination of the communication session, as determined by the 
    *    LMS, was successful.</li>
    *    <li><code>false</code> - The characterstring "false" shall be returned 
    *    if termination of the communication session, as determined by the LMS,
    *    was unsuccessful.
    * </ul>
    */
   public String Terminate(String iParam)
   {
      if( _Debug )
      {
         System.out.println("*****************");
         System.out.println("In API::Terminate");
         System.out.println("*****************");
         System.out.println("");
      }
      String paramVal = new String();
      String evalVal = new String();
      if( logging_on )
      {

         paramVal = "Called Terminate ";
         evalVal = "display_log(\"" + paramVal + "\");";
         jsCall(evalVal);
      }
      mTerminateCalled = true;
      // Assume failure
      String result = STRING_FALSE;

      // already terminated
      if( mTerminatedState )
      {
         mLMSErrorManager.setCurrentErrorCode(APIErrorCodes.TERMINATE_AFTER_TERMINATE);
         if( logging_on )
         {
            paramVal = "Terminate Returned Error Code " + APIErrorCodes.TERMINATE_AFTER_TERMINATE;
            evalVal = "display_log(\"" + paramVal + "\");";
            jsCall(evalVal);
         }
         return result;
      }
      if( !isInitialized() )
      {
         mLMSErrorManager.setCurrentErrorCode(APIErrorCodes.TERMINATE_BEFORE_INIT);
         if( logging_on )
         {
            paramVal = "Terminate Returned Error Code " + APIErrorCodes.TERMINATE_BEFORE_INIT;
            evalVal = "display_log(\"" + paramVal + "\");";
            jsCall(evalVal);
         }
         
         return result;
      }

      // Make sure param is empty string "" - as per the API spec
      // Check for "null" is a workaround described in "Known Problems"
      // in the header.
      String tempParm = String.valueOf(iParam);
      if( ( tempParm == null || tempParm.equals("") ) != true )

      {
         mLMSErrorManager.setCurrentErrorCode(DMErrorCodes.GEN_ARGUMENT_ERROR);
      }
      else
      {
         //check if adl.nav.request is equal to suspend all, or if the suspend button was pushed, set cmi.exit equal to suspend.
         SCORM_2004_NAV_DM navDM = (SCORM_2004_NAV_DM)mSCOData.getDataModel("adl");
         String event = navDM.getNavRequest();

         if( event.equals("7") || mLMSSuspendAllPushed )
         {
            // Process 'SET' on cmi.exit
            DMInterface.processSetValue("cmi.exit", "suspend", true, mSCOData);
         }

         if( !( event.equals("5") || event.equals("6") ) )
         {
            result = Commit("");
         }
         else
         {
            // The attempt has been abandoned, so don't persist the data
            result = STRING_TRUE;
         }

         mTerminatedState = true;

         if( !result.equals(STRING_TRUE) )
         {
            if( _Debug )
            {
               System.out.println("Commit failed causing " + "Terminate to fail.");
            }
            // General Commit Failure
            mLMSErrorManager.setCurrentErrorCode(APIErrorCodes.GENERAL_COMMIT_FAILURE);
         }
         else
         {
            mInitializedState = false;

            // get value of "exit"
            DMProcessingInfo dmInfo = new DMProcessingInfo();
            int dmErrorCode = 0;
            dmErrorCode = DMInterface.processGetValue("cmi.exit", true, true, mSCOData, dmInfo);
            String exitValue = dmInfo.mValue;
            String tempEvent = "_none_";
            boolean isChoice = false;
            String evalValue = null;         

            if( dmErrorCode == APIErrorCodes.NO_ERROR )
            {
               exitValue = dmInfo.mValue;
            }
            else
            {
               exitValue = new String("");
            }

            if( exitValue.equals("time-out") )
            {
               tempEvent = "exitAll";
            }
            else if( exitValue.equals("logout") )
            {
               tempEvent = "exitAll";
            }
            else
            {
               if( event != null )
               { // SeqNavRequests.NAV_CONTINUE
                  if( event.equals("3") )
                  {
                     tempEvent = "next";
                  }
                  // SeqNavRequests.NAV_PREVIOUS
                  else if( event.equals("4") )
                  {
                     tempEvent = "previous";
                  }
                  // SeqNavRequests.NAV_ABANDON
                  else if( event.equals("5") )
                  {
                     tempEvent = "abandon";
                  }
                  // SeqNavRequests.NAV_ABANDONALL
                  else if( event.equals("6") )
                  {
                     tempEvent = "abandonAll";
                  }
                  // SeqNavRequests.NAV_EXIT
                  else if( event.equals("8") )
                  {
                     tempEvent = "exit";
                  }
                  // SeqNavRequests.NAV_EXITALL
                  else if( event.equals("9") )
                  {
                     tempEvent = "exitAll";
                  }
                  // SeqNavRequests.NAV_SUSPENDALL 
                  else if( event.equals("7") )
                  {
                     tempEvent = "suspendAll";
                  }
                  // SeqNavRequests.NAV_NONE
                  else if( event.equals("0") )
                  {
                     tempEvent = "_none_";
                  }
                  else
                  {
                     // This must be a target for choice
                     tempEvent = event;
                     isChoice = true;
                  }
               }
            }
            
            if ( !mLMSSuspendAllPushed )
            {
               // handle if sco set nav.request
               if( !tempEvent.equals("_none_") )
               {
   
                  if( _Debug )
                  {
                     System.out.println("in finish - navRequest was set");
                     System.out.println("request " + tempEvent);
   
                  }
                  if( isChoice )
                  {
                     evalValue = "doChoiceEvent(\"" + tempEvent + "\");";
                     if( _Debug )
                     {
                        System.out.println("choice nav event  " + evalValue);
                     }
                  }
                  else
                  {
                     evalValue = "doNavEvent(\"" + tempEvent + "\");";
                  }
                  jsCall(evalValue);
   
               }
            }
         }
      }

      if( logging_on )
      {
         paramVal = "Terminate Returned Error Code " + mLMSErrorManager.getCurrentErrorCode();
         evalVal = "display_log(\"" + paramVal + "\");";
         jsCall(evalVal);
      }

      if( _Debug )
      {
         System.out.println("");
         System.out.println("***************************");
         System.out.println("Done Processing Terminate()");
         System.out.println("***************************");
      }      
      return result;
   }

   /**
    * Insert a backward slash (\) before each double quote (") or
    * backslash (\) to allow the character to be displayed in the 
    * data model log.  Receives the value and returns the newly formatted
    * value
    */   
   public String formatValue(String baseString)
   {
	  int indexQuote = baseString.indexOf("\"");
	  int indexSlash = baseString.indexOf("\\");

	  if(indexQuote >= 0 || indexSlash >= 0)
	  {
         int index= 0;
		 String temp = new String();
		 String strFirst = new String();
		 String strLast = new String();
		 char insertValue = '\\';

		 while(index < baseString.length())
		 {
            if((baseString.charAt(index) == '\"') || (baseString.charAt(index) == '\\'))
			{
			   strFirst = baseString.substring(0 , index);
			   strLast = baseString.substring(index , baseString.length()); 
               baseString = strFirst.concat(Character.toString(insertValue)).concat(strLast);               
			   index += 2;
			}
			else
			{
			   index++;			  
			}
		 }
	  }   
	  return baseString;
   }
   
   /**
    * The function requests information from an LMS.  It permits the SCO to
    * request information from the LMS to determine among other things:
    * <ul>
    *    <li>Values for data model elements supported by the LMS.</li>
    *    <li>Version of the data model supported by the LMS.</li>
    *    <li>Whether or not specific data model elements are supported.</li>
    * </ul>
    * Retrieves the current value of the specified data model element
    * for a SCORM 2004 3rd Edition SCO.  The  values are locally cached except for
    * nav.event_permitted, which requires a call to LMSCMIServlet to get the
    * current value.<br><br>
    *
    * @param iDataModelElement The parameter represents the complete
    * identification of a data model element within a data model.<br><br>
    *
    * @return The method can return one of two values.  If there is not error,
    * the return value shall be represented as a characterstring containing
    * the value associated with the parameter.  If an error occurs, then the
    * API Instance shall set an error code to a value specific to the error and
    * return an empty characterstring ("").
    */
   public String GetValue(String iDataModelElement)
   {
      if( _Debug )
      {
         System.out.println("*******************");
         System.out.println("In API::GetValue");
         System.out.println("*******************");
         System.out.println("");
      }
      String paramVal = new String();
      String evalVal = new String();
      if( logging_on )
      {
         paramVal = "Called GetValue( " + iDataModelElement + ")";
         evalVal = "display_log(\"" + paramVal + "\");";
         jsCall(evalVal);
      }
      String result = "";

      // already terminated
      if( mTerminatedState )
      {
         mLMSErrorManager.setCurrentErrorCode(APIErrorCodes.GET_AFTER_TERMINATE);
         if( logging_on )
         {
            paramVal = "GetValue Returned Error Code " + APIErrorCodes.GET_AFTER_TERMINATE;
            evalVal = "display_log(\"" + paramVal + "\");";
            jsCall(evalVal);
         }
         return result;
      }
      if( iDataModelElement.length() == 0 )
      {
         mLMSErrorManager.setCurrentErrorCode(DMErrorCodes.GEN_GET_FAILURE);
         if( logging_on )
         {
            paramVal = "GetValue Returned Error Code " + DMErrorCodes.GEN_GET_FAILURE;
            evalVal = "display_log(\"" + paramVal + "\");";
            jsCall(evalVal);
         }
         return result;
      }

      if( isInitialized() )
      {

         if( _Debug )
         {
            System.out.println("Request being processed: GetValue(" + iDataModelElement + ")");
         }

         DMProcessingInfo dmInfo = new DMProcessingInfo();
         int dmErrorCode = 0;
         boolean done = false;

         // Clear current error codes
         mLMSErrorManager.clearCurrentErrorCode();

         // Compare for cmi.completion_status
         if( iDataModelElement.equals(COMPLETION_STATUS) )
         {
            double completionThreshold = 0.0;
            double progressMeasure = 0.0;
            dmErrorCode = DMInterface.processGetValue(COMPLETION_THRESHOLD, false, mSCOData, dmInfo);
            if( dmErrorCode != DMErrorCodes.NOT_INITIALIZED )
            {
               done = true;

               completionThreshold = Double.parseDouble(dmInfo.mValue);
               dmErrorCode = DMInterface.processGetValue(PROGRESS_MEASURE, false, mSCOData, dmInfo);
               if( dmErrorCode != DMErrorCodes.NOT_INITIALIZED )
               {
                  progressMeasure = Double.parseDouble(dmInfo.mValue);
                  if( progressMeasure >= completionThreshold )
                  {
                     result = "completed";
                  }
                  else
                  {
                     result = "incomplete";
                  }
               }
               else
               {
                  result = "unknown";
               }

               dmErrorCode = APIErrorCodes.NO_ERROR;
            }
         }
         // Compare for cmi.success_status
         if( iDataModelElement.equals(SUCCESS_STATUS) )
         {
            double scaledPassingScore = 0.0;
            double scoreScaled = 0.0;
            dmErrorCode = DMInterface.processGetValue(SCALED_PASSING_SCORE, false, mSCOData, dmInfo);
            if( dmErrorCode != DMErrorCodes.NOT_INITIALIZED )
            {
               done = true;
               scaledPassingScore = Double.parseDouble(dmInfo.mValue);
               dmErrorCode = DMInterface.processGetValue(SCORE_SCALED, false, mSCOData, dmInfo);
               if( dmErrorCode != DMErrorCodes.NOT_INITIALIZED )
               {
                  scoreScaled = Double.parseDouble(dmInfo.mValue);
                  if( scoreScaled >= scaledPassingScore )
                  {
                     result = "passed";
                  }
                  else
                  {
                     result = "failed";
                  }
               }
               else
               {
                  result = "unknown";
               }

               dmErrorCode = APIErrorCodes.NO_ERROR;
            }
         }

         if( !done )
         {
            // Process 'GET'
            dmErrorCode = DMInterface.processGetValue(iDataModelElement, false, mSCOData, dmInfo);

            if( dmErrorCode == APIErrorCodes.NO_ERROR )
            {			   
               result = dmInfo.mValue;			   
			}
         }

         // Set the LMS Error Manager from the Data Model Error Manager
         mLMSErrorManager.setCurrentErrorCode(dmErrorCode);

         if( dmErrorCode == APIErrorCodes.NO_ERROR )
         {
            if( _Debug )
            {
               System.out.println("GetValue() found!");
               System.out.println("Returning: " + dmInfo.mValue);
            }
         }
         else
         {
            if( _Debug )
            {
               System.out.println("Found the element, but the value was null");
            }
            result = new String("");
         }

         if( logging_on )
         {
            // Convert quotes and backslashes in result to \" and \\ 
       	    // to allow for correct output
			String resultFormatted = new String(formatValue(result));
            paramVal = "GetValue Returned the value " + resultFormatted;
            evalVal = "display_log(\"" + paramVal + "\");";
            jsCall(evalVal);
         }

      }
      // not initialized
      else
      {
         mLMSErrorManager.setCurrentErrorCode(APIErrorCodes.GET_BEFORE_INIT);
      }

      if( _Debug )
      {
         System.out.println("");
         System.out.println("************************************");
         System.out.println("Processing done for API::LMSGetValue");
         System.out.println("************************************");
      }
      if( logging_on )
      {

         paramVal = "GetValue Returned Error Code " + mLMSErrorManager.getCurrentErrorCode();
         evalVal = "display_log(\"" + paramVal + "\");";
         jsCall(evalVal);
      }

      return result;
   }

      
   /**
    * Request the transfer to the LMS of the value of
    * iValue for the data element specified as iDataModelElement.  This method
    * allows the SCO to send information to the LMS for storage.
    * Used by SCORM 2004 3rd Edition SCOs.  The values are locally cached
    * until <code>Commit()</code> or <code>Terminate()</code> is called.
    *
    * @param iDataModelElement - The complete identification of a data model
    *        element within a data model to be set.
    * @param iValue  - The intended value of the CMI or Navigation
    *        datamodel element.  The value shall be a characterstring that shall
    *        be convertible to the data type defined for the data model element
    *        identified in iDataModelElement.<br><br>
    *
    * @return The method can return one of two values.  The return value shall
    *         be represented as a characterstring.
    * <ul>
    *    <li><code>true</code> - The characterstring "true" shall be returned 
    *    if the LMS accepts the content of iValue to set the value of 
    *    iDataModelElement.</li>
    *    <li><code>false</code> - The characterstring "false" shall be returned 
    *    if the LMS encounters an error in setting the contents of 
    *    iDataModelElement with the value of iValue.</li>
    * </ul>
    */

   public String SetValue(String iDataModelElement, String iValue)
   {
      // Assume failure
      String result = STRING_FALSE;
      
      if( _Debug )
      {
         System.out.println("*******************");
         System.out.println("In API::SetValue");
         System.out.println("*******************");
         System.out.println("");
      }
      String paramVal = new String();
      String evalVal = new String();

	  // Convert quotes and backslashes in iValue to \" and \\ 
	  // to allow for correct output
      String iValueFormatted = new String(formatValue(iValue));	  
	  
      if( logging_on )
      {
         paramVal = "Called SetValue(" + iDataModelElement + ", " + iValueFormatted + ")";                    
         evalVal = "display_log(\"" + paramVal + "\");";
	     jsCall(evalVal);
      }

      // already terminated
      if( mTerminatedState )
      {
         mLMSErrorManager.setCurrentErrorCode(APIErrorCodes.SET_AFTER_TERMINATE);
         if( logging_on )
         {
            paramVal = "SetValue Returned Error Code " + APIErrorCodes.SET_AFTER_TERMINATE;
            evalVal = "display_log(\"" + paramVal + "\");";
            jsCall(evalVal);
         }

         return result;
      }

      // Clear any existing error codes
      mLMSErrorManager.clearCurrentErrorCode();

      if( !isInitialized() )
      {
         // not initialized
         mLMSErrorManager.setCurrentErrorCode(APIErrorCodes.SET_BEFORE_INIT);
         if( logging_on )
         {
            paramVal = "SetValue Returned Error Code " + APIErrorCodes.SET_BEFORE_INIT;
            evalVal = "display_log(\"" + paramVal + "\");";
            jsCall(evalVal);
         }
         return result;
      }
      
      String setValue = null;

      // Check for "null" is a workaround described in "Known Problems"
      // in the header.
      String tempValue = String.valueOf(iValue);
      if( tempValue == null )
      {
         setValue = new String("");
      }
      else
      {
         setValue = tempValue;
      }

      // Construct the request
      String theRequest = iDataModelElement + "," + setValue;

      if( _Debug )
      {
         System.out.println("Request being processed: SetValue(" + theRequest + ")");
         System.out.println("Looking for the element " + iValue);
      }

      // Send off

	 
      // Process 'SET'
      int dmErrorCode = 0;
      dmErrorCode = DMInterface.processSetValue(iDataModelElement, iValue, false, mSCOData);

	 

      // Set the LMS Error Manager from the DataModel Manager
      mLMSErrorManager.setCurrentErrorCode(dmErrorCode);

      if( mLMSErrorManager.getCurrentErrorCode().equals("0") )
      {
         // Successful Set
         result = STRING_TRUE;
      }

      //clear MessageCollection
      MessageCollection mc = MessageCollection.getInstance();
      mc.clear();

      if( logging_on )
      {
         paramVal = "SetValue Returned Error Code " + mLMSErrorManager.getCurrentErrorCode();
         evalVal = "display_log(\"" + paramVal + "\");";
         jsCall(evalVal);
      }
      if( _Debug )
      {
         System.out.println("");
         System.out.println("************************************");
         System.out.println("Processing done for API::SetValue");
         System.out.println("************************************");
      }

      return result;
   }

   /**
    * Toggles the state of the LMS provided UI controls.
    *
    * @param iState <code>true</code> if the controls should be enabled, or
    *               <code>false</code> if the controls should be disabled.
    */
   private void setUIState(boolean iState)
   {
      if( _Debug )
      {
         System.out.println(" ::Toggling UI State::-> " + iState);
      }

      String evalCmd = "setUIState(" + iState + ");";
      jsCall(evalCmd);
   }

   /**
    * Requests forwarding to the persistent data store any data
    * from the SCO that may have been cached by the API Implementation since
    * the last call to <code>Initialize()</code> or <code>Commit()</code>, 
    * whichever occurred most recently.  Used by SCORM 2004 3rd Edition SCOs.
    *
    * @param iParam ("") - empty characterstring.  An empty characterstring
    * shall be passed as a parameter.<br><br>
    *
    * @return The method can return one of two values.  The return value shall
    *         be represented as a characterstring.
    * <ul>
    *    <li><code>true</code> - The characterstring "true" shall be returned 
    *    if the data was successfully persisted to a long-term data store.</li>
    *    <li><code>false</code> - The characterstring "false" shall be returned 
    *    if the data was unsuccessfully persisted to a long-term data 
    *    store.</li>
    * </ul>
    * 
    * The API Instance shall set the error code to a value specific to
    * the error encountered.
    */
   public String Commit(String iParam)
   {
      if( _Debug )
      {
         System.out.println("*************************");
         System.out.println("Processing API::Commit");
         System.out.println("*************************");
         System.out.println("");
      }

      String paramVal = new String();
      String evalVal = new String();
      if( logging_on )
      {
         paramVal = "Called Commit";
         evalVal = "display_log(\"" + paramVal + "\");";
         jsCall(evalVal);
      }
      // Assume failure
      String result = STRING_FALSE;

      // already terminated
      if( mTerminatedState )
      {
         mLMSErrorManager.setCurrentErrorCode(APIErrorCodes.COMMIT_AFTER_TERMINATE);
         if( logging_on )
         {
            paramVal = "Commit Returned Error Code " + APIErrorCodes.COMMIT_AFTER_TERMINATE;
            evalVal = "display_log(\"" + paramVal + "\");";
            jsCall(evalVal);
         }
         return result;
      }

      // Disable UI Controls
      setUIState(false);

      // Make sure param is empty string "" - as per the API spec
      // Check for "null" is a workaround described in "Known Problems"
      // in the header.
      String tempParm = String.valueOf(iParam);

      if( ( tempParm == null || tempParm.equals("") ) != true )
      {
         mLMSErrorManager.setCurrentErrorCode(DMErrorCodes.GEN_ARGUMENT_ERROR);
      }
      else
      {
         if( !isInitialized() )
         {
            //LMS is not initialized
            mLMSErrorManager.setCurrentErrorCode(APIErrorCodes.COMMIT_BEFORE_INIT);
            if( logging_on )
            {
               paramVal = "Commit Returned Error Code " + APIErrorCodes.COMMIT_BEFORE_INIT;
               evalVal = "display_log(\"" + paramVal + "\");";
               jsCall(evalVal);
            }
            return result;
         }
         else if( mTerminatedState )
         {
            //LMS is terminated
            mLMSErrorManager.setCurrentErrorCode(APIErrorCodes.COMMIT_AFTER_TERMINATE);
            if( logging_on )
            {
               paramVal = "Commit Returned Error Code " + APIErrorCodes.COMMIT_AFTER_TERMINATE;
               evalVal = "display_log(\"" + paramVal + "\");";
               jsCall(evalVal);
            }
            return result;
         }
         else
         {
            // Prepare the request before it goes across the socket
            LMSCMIServletRequest request = new LMSCMIServletRequest();

            request.mActivityData = mSCOData;
            request.mIsFinished = mTerminateCalled;
            request.mRequestType = LMSCMIServletRequest.TYPE_SET;
            request.mCourseID = mCourseID;
            request.mStudentID = mUserID;
            request.mUserName = mUserName;
            request.mStateID = mStateID;
            request.mActivityID = mActivityID;
            Long longObj = new Long(mNumAttempts);
            request.mNumAttempt = longObj.toString();

            ServletProxy proxy = new ServletProxy(mServletURL);

            LMSCMIServletResponse response = proxy.postLMSRequest(request);

            if( !response.mError.equals("OK") )
            {

               mLMSErrorManager.setCurrentErrorCode(APIErrorCodes.GENERAL_EXCEPTION);

               if( _Debug )
               {
                  System.out.println("'SET' to server was NOT successful!");
               }
            }
            else
            {
               mLMSErrorManager.clearCurrentErrorCode();

               result = STRING_TRUE;

               SCORM_2004_NAV_DM navDM = (SCORM_2004_NAV_DM)mSCOData.getDataModel("adl");

               // Update the ADLValidRequests object from the servlet
               // response object.
               navDM.setValidRequests(response.mValidRequests);

               if( _Debug )
               {
                  System.out.println("'SET' to server succeeded!");
               }
            }

         }
      }

      // Enable UI Controls
      setUIState(true);

      // Refresh the Menu frame
      jsCall("refreshMenu()");

      if( logging_on )
      {
         paramVal = " Commit Returned Error Code " + mLMSErrorManager.getCurrentErrorCode();
         evalVal = "display_log(\"" + paramVal + "\");";
         jsCall(evalVal);
      }

      if( _Debug )
      {
         System.out.println("");
         System.out.println("**********************************");
         System.out.println("Processing done for API::Commit");
         System.out.println("**********************************");
      }

      return result;
   }

   /**

  /**
    * This method requests the error code for the current error state of the
    * API Instance.  Used by SCORM 2004 3rd Edition SCOs.
    *
    * <br><br>NOTE: Session and Data-Transfer API functions set or
    * clear the error code.<br><br>
    *
    * @return The API Instance shall return the error code reflecting the
    *         current error state of the API Instance.  The return value
    *         shall be a characterstring (convertible to an integer in the
    *         range from 0 to 65536 inclusive) representing the error code
    *         of the last error encountered.
    */
   public String GetLastError()
   {
      if( _Debug )
      {
         System.out.println("In API::GetLastError()");
      }
      String paramVal = "Called GetLastError() ";
      String evalVal = "display_log(\"" + paramVal + "\");";
      if( logging_on )
      {
         jsCall(evalVal);
      }
      paramVal = "GetLastError() Returned  " + mLMSErrorManager.getCurrentErrorCode();
      evalVal = "display_log(\"" + paramVal + "\");";
      if( logging_on )
      {
         jsCall(evalVal);
      }

      return mLMSErrorManager.getCurrentErrorCode();
   }

   /**
    * The GetErrorString() function can be used to retrieve a textual
    * description of the current error state.  The function is used by a
    * SCO to request the textual description for the error code specified
    * by the value of the parameter.  This call has no effect on the current
    * error state; it simply returns the requested information.  Used by
    * SCORM 2004 3rd Edition SCOs.
    *
    * @param iErrorCode Represents the characterstring of the error code
    *        (integer value) corresponding to an error message.<br><br>
    *
    * @return The method shall return a textual message containing a
    *         description of the error code specified by the value of the
    *         parameter.  The following requirements shall be adhered to for
    *         all return values:
    * <ul>
    *    <li>The return value shall be a characterstring that has a maximum
    *        length of 256 bytes (including null terminator).</li>
    *    <li>The SCORM makes no requirement on what the text of the
    *        characterstring shall contain.  The error codes themselves are
    *        explicitly and exclusively defined.  The textual description
    *        for the error code is LMS specific.</li>
    *    <li>If the requested error code is unknown by the LMS, an empty
    *        characterstring ("") shall be returned  This is the only time
    *        that an empty characterstring shall be returned.</li>
    * </ul>
    */
   public String GetErrorString(String iErrorCode)
   {
      if( _Debug )
      {
         System.out.println("In API::GetErrorString()");
      }
      String paramVal = "Called GetErrorString(" + iErrorCode + ") ";
      String evalVal = "display_log(\"" + paramVal + "\");";
      if( logging_on )
      {
         jsCall(evalVal);
      }

      String errorCode = mLMSErrorManager.getErrorDescription(iErrorCode);
      paramVal = "GetErrorString Returned " + errorCode;
      evalVal = "display_log(\"" + paramVal + "\");";
      if( logging_on )
      {
         jsCall(evalVal);
      }

      return errorCode;
   }

   /**
    * The GetDiagnostic() function exists for LMS specific use.  It  allows
    * the LMS to define additional diagnostic information through the API
    * Instance.  This call has no effect on the current error state; it
    * simply returns the requested information.  Used by SCORM 2004 3rd 
    * Edition SCOs.
    *
    * @param iErrorCode  An implementer-specific value for diagnostics.  The
    *        maximum length of the parameter value shall be 256 bytes
    *        (including null terminator).  The value of the parameter may be
    *        an error code, but is not limited to just error codes.<br><br>
    *
    * @return The API Instance shall return a characterstring representing
    *         the diagnostic information.  The maximum length of the
    *         characterstring returned shall be 256 bytes
    *         (including null terminator).
    */
   public String GetDiagnostic(String iErrorCode)
   {
      if( _Debug )
      {
         System.out.println("In API::GetDiagnostic()");
      }

      String paramVal = "Called GetDiagnostic(" + iErrorCode + ") ";
      String evalVal = "display_log(\"" + paramVal + "\");";
      if( logging_on )
      {
         jsCall(evalVal);
      }

      String diagnostic = mLMSErrorManager.getErrorDiagnostic(iErrorCode);
      paramVal = "GetDiagnostic Returned " + diagnostic;
      evalVal = "display_log(\"" + paramVal + "\");";
      if( logging_on )
      {
         jsCall(evalVal);
      }

      return diagnostic;
   }

   /**
    * Sets the ID of the activity associated with the currently delivered 
    * content.
    *
    * @param iActivityID  The activity ID.
    */
   public void setActivityID(String iActivityID)
   {
      mActivityID = iActivityID;
   }

   /**
    * Sets the ID of the course with which the currently launched content is
    * associated.
    *
    * @param iCourseID  The course ID.
    */
   public void setCourseID(String iCourseID)
   {
      mCourseID = iCourseID;
   }

   /**
    * Sets the ID of the run-time data state of the currently launched content.
    *
    * @param iStateID  The run-time data state ID.
    */
   public void setStateID(String iStateID)
   {
      mStateID = iStateID;
   }

   /**
    * Sets the ID of the student experiencing the currently launched content.
    *
    * @param iUserID  The student ID.
    */
   public void setUserID(String iUserID)
   {
      mUserID = iUserID;
   }

   /**
    * Sets the name of the student experiencing the currently launched content.
    *
    * @param iUserName  The student Name.
    */
   public void setUserName(String iUserName)
   {
      mUserName = iUserName;
   }

   /**
    * Sets the number of the current attempt.
    *
    * @param iNumAttempts  The number of the current attempt.
    */
   public void setNumAttempts(long iNumAttempts)
   {
      mNumAttempts = iNumAttempts;
   }

   /**
    * Sets the number of the current attemptfrom a String parameter.
    *
    * @param iNumAttempts  The number of the current attempt.
    */
   public void setNumAttempts(String iNumAttempts)
   {
      Long tempLong = new Long(iNumAttempts);
      mNumAttempts = tempLong.longValue();
   }

   /**
    * Clears error codes and sets mInitialedState and mTerminated State to
    * default values.
    */
   public void clearState()
   {
      mInitializedState = false;
      mTerminatedState = false;
      mTerminateCalled = false;
      mLMSErrorManager.clearCurrentErrorCode();
   }

   /**
    * This method implements the interface with the Java Script running on 
    * the client side of the Sample RTE.     
    * <br><br> 
    * <br><br>
    *
    * @param  message The String that is evaluated by the Java Script eval 
    * command--usually it is a Java Script function name.<br><br>    
    *         
    *         
    */
   public void jsCall(String message)
   {	   
      JSObject.getWindow(this).eval(message);
   }

   /**
    * This method indicates that the suspend all button was pushed.
    */
   public void suspendButtonPushed()
   {      
      if ( isInitialized() )
      {
         mLMSSuspendAllPushed = true;
         Terminate("");
         
         String evalValue = "doNavEvent(\"suspendAll\");";
         jsCall(evalValue);

      }      
   }

   /**
    * This method resets the boolean value of the variable that controls 
    * logging of API and Datamodel calls.     
    */
   public void resetLoggingVariable()
   {
      logging_on = !logging_on;
   }

} // ClientRTS
