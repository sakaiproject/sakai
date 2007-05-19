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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.adl.datamodels.DMErrorCodes;
import org.adl.datamodels.DMInterface;
import org.adl.datamodels.DMProcessingInfo;
import org.adl.datamodels.SCODataManager;
import org.adl.samplerte.util.RTEFileHandler;
import org.adl.sequencer.ADLObjStatus;
import org.adl.sequencer.ADLSequencer;
import org.adl.sequencer.ADLValidRequests;
import org.adl.sequencer.ISeqActivity;
import org.adl.sequencer.ISeqActivityTree;
import org.adl.sequencer.ISequencer;
import org.adl.sequencer.SeqActivity;
import org.adl.sequencer.SeqActivityTree;
import org.adl.sequencer.SeqObjective;

/**
 * <strong>Filename:</strong> LMSCMIServletjava<br>
 * <br>
 * <strong>Description:</strong><br>
 * The LMSCMIServlet class handles the server side data model communication of
 * the Sample RTE.<br>
 * This servlet handles persistence of the SCORM Run-Time Environment Data Model
 * elements. Persistence is being handled via flat files and java object
 * serialization rather than through a database.<br>
 * <br>
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the SCORM Sample RTE 1.3. <br>
 * <br>
 * <strong>Implementation Issues:</strong><br>
 * This servlet works in conjunction with the <code>LMSRTEClient</code> applet
 * in the <code>org.adl.lms.client</code> package.<br>
 * <br>
 * <strong>Known Problems:</strong><br>
 * <br>
 * <strong>Side Effects:</strong><br>
 * <br>
 * <strong>References:</strong><br>
 * <ul>
 * <li>IMS SS Specification</li>
 * <li>SCORM 2004</li>
 * </ul>
 * 
 * @author ADL Technical Team
 */
public class LMSCMIServlet extends HttpServlet
{
   /**
    * This is the value used for the primary objective ID
    */
   private final String mPRIMARY_OBJ_ID = null;

   /**
    * This string contains the name of the SampleRTEFiles directory.
    */
   private final String SRTEFILESDIR = "SCORM3rdSampleRTE10Files";

   /**
    * This sets up Java Logging
    */
   private Logger mLogger = Logger.getLogger("org.adl.util.debug.samplerte");

   /**
    * The name of the target persisted run-time data model file.
    */
   private String mSCOFile = null;

   /**
    * The ID of the learner associated with the persisted run-time data.
    */
   private String mUserID = null;

   /**
    * The Name of the learner associated with the persisted run-time data.
    */
   private String mUserName = null;

   /**
    * The ID of the course associated with the persisted run-time data.
    */
   private String mCourseID = null;

   /**
    * The ID of the SCO associated with the persisted run-time data.
    */
   private String mSCOID = null;

   /**
    * The attempt number associated with the persisted run-time data.
    */
   private String mNumAttempt = null;

   /**
    * The ID of the activity associated with the persisted run-time data.
    */
   private String mActivityID = null;

   /**
    * The request issued by the LMS Client.
    */
   private LMSCMIServletRequest mRequest = null;

   /**
    * The response of this servlet.
    */
   private LMSCMIServletResponse mResponse = null;

   /**
    * The active set of run-time data.
    */
   private SCODataManager mSCOData = null;

   /**
    * The location of the SSP Servlet. This variable is used, don't delete it.
    */
   private String mSSPServerLocation = null;

   /**
    * This method handles the 'POST' message sent to the servlet. This servlet
    * will handle <code>LMSServletRequest</code> objects and respond with a
    * <code>LMSServletResponse</code> object.
    * 
    * @param iRequest The request 'POST'ed to the servlet.
    * @param oResponse The response returned by the servlet.<br>
    *           <br>
    * @exception ServletException
    * @exception IOException <br>
    *               <br>
    * @see org.adl.samplerte.server#LMSServletRequest
    * @see org.adl.samplerte.server#LMSServletResponse
    */
   public void doPost(HttpServletRequest iRequest, HttpServletResponse oResponse) throws ServletException, IOException
   {
      mLogger.entering("---LMSCMIServlet", "doPost()");
      mLogger.info("POST received by LMSCMIServlet");

      try
      {
         mLogger.info("Requested session: " + iRequest.getRequestedSessionId());
         mLogger.info("query string: " + iRequest.getQueryString());
         mLogger.info("header string: " + iRequest.getContextPath());

         for( Enumeration e = iRequest.getHeaderNames(); e.hasMoreElements(); )
         {
            mLogger.info(e.nextElement().toString());
         }

         // Retrieve the current session ID
         HttpSession session = iRequest.getSession(false);
         if( session == null )
         {
            mLogger.severe("  ERROR - No session ID in LMSCMIServlet.");
         }
         else
         {
            mLogger.info("Session ID is: " + session.getId());
         }

         mLogger.info("Checking attributes");

         ObjectInputStream in = new ObjectInputStream(iRequest.getInputStream());

         mLogger.info("Created REQUEST object INPUT stream successfully");

         ObjectOutputStream out = new ObjectOutputStream(oResponse.getOutputStream());

         mLogger.info("Created RESPONSE object OUTPUT stream successfully");

         // Read the LMSCMIServletRequest object
         mRequest = (LMSCMIServletRequest)in.readObject();

         // Set servlet state
         mSCOID = mRequest.mStateID;
         mActivityID = mRequest.mActivityID;
         mCourseID = mRequest.mCourseID;
         mUserID = mRequest.mStudentID;
         mNumAttempt = mRequest.mNumAttempt;
         mUserName = mRequest.mUserName;
         mSSPServerLocation = mRequest.mSSPServerLocation;

         mLogger.info("ScoID: " + mSCOID);

         // Set the run-time data model path
         if( mNumAttempt != null )
         {
            mSCOFile = File.separator + SRTEFILESDIR + File.separator + mUserID + File.separator + mCourseID
               + File.separator + mSCOID + "__" + mNumAttempt;
         }
         else
         {
            mLogger.fine("  ERROR: NULL # attempt");

            mSCOFile = File.separator + SRTEFILESDIR + File.separator + mUserID + File.separator + mCourseID
               + File.separator + mSCOID;
         }

         mLogger.info("Data model path:  " + mSCOFile);

         FileInputStream fi = null;
         ObjectInputStream fileIn = null;

         // Handle the request
         switch( mRequest.mRequestType )
         {

            case LMSCMIServletRequest.TYPE_INIT:

               mLogger.info("CMI Servlet - doPost() - entering case INIT ");

               mLogger.info("Processing 'init' request");

               // create response object to return
               mResponse = new LMSCMIServletResponse();

               // Serialize the users activity tree for the selected course

               SeqActivityTree mSeqActivityTree = new SeqActivityTree();

               String mTreePath = File.separator + SRTEFILESDIR + File.separator + mUserID + File.separator + mCourseID
                  + File.separator + "serialize.obj";

               FileInputStream mFileIn = new FileInputStream(mTreePath);
               ObjectInputStream mObjectIn = new ObjectInputStream(mFileIn);
               mSeqActivityTree = (SeqActivityTree)mObjectIn.readObject();
               mObjectIn.close();
               mFileIn.close();

               boolean newFile = true;
               RTEFileHandler fileHandler = new RTEFileHandler();

               // Try to open the state file
               try
               {
                  fi = new FileInputStream(mSCOFile);
                  newFile = false;

               }
               catch( FileNotFoundException fnfe )
               {
                  mLogger.info("State file does not exist...");

                  // data model file does not exist so initialize values

                  mLogger.info("Created file handler");
                  mLogger.info("About to create file");

                  fileHandler.initializeStateFile(mNumAttempt, mUserID, mUserName, mCourseID, mSCOID, mSCOID,
                     mSSPServerLocation);

                  mLogger.info("after initialize state file");
                  mLogger.info("State File Created");

                  fi = new FileInputStream(mSCOFile);
               }

               mLogger.info("Created LMSSCODataFile File input stream " + "successfully");

               fileIn = new ObjectInputStream(fi);

               mLogger.info("Created OBJECT input stream successfully");

               // Initialize the new attempt
               mSCOData = (SCODataManager)fileIn.readObject();

               fileIn.close();
               fi.close();

               // Create the sequencer and set the tree
               ISequencer mSequencer = new ADLSequencer();
               ADLValidRequests mState = new ADLValidRequests();
               ISeqActivity mSeqActivity = mSeqActivityTree.getActivity(mSCOID);
               mSequencer.setActivityTree(mSeqActivityTree);

               // get UIState
               mSequencer.getValidRequests(mState);
               mResponse.mValidRequests = mState;
               mLogger.info("continue  " + mResponse.mValidRequests.mContinue);
               mLogger.info("previous  " + mResponse.mValidRequests.mPrevious);

               Vector mStatusVector = new Vector();

               mStatusVector = mSequencer.getObjStatusSet(mSCOID);

               ADLObjStatus mObjStatus = new ADLObjStatus();

               // Temporary variables for obj initialization
               int err = 0;
               String obj = new String();

               // Initialize Objectives based on global objectives
               if( mStatusVector != null )
               {
                  if( newFile )
                  {
                     for( int i = 0; i < mStatusVector.size(); i++ )
                     {
                        // initialize objective status from sequencer
                        mObjStatus = (ADLObjStatus)mStatusVector.get(i);

                        // Set the objectives id
                        obj = "cmi.objectives." + i + ".id";

                        err = DMInterface.processSetValue(obj, mObjStatus.mObjID, true, mSCOData);

                        // Set the objectives success status
                        obj = "cmi.objectives." + i + ".success_status";

                        if( mObjStatus.mStatus.equalsIgnoreCase("satisfied") )
                        {
                           err = DMInterface.processSetValue(obj, "passed", true, mSCOData);
                        }
                        else if( mObjStatus.mStatus.equalsIgnoreCase("notSatisfied") )
                        {
                           err = DMInterface.processSetValue(obj, "failed", true, mSCOData);
                        }

                        // Set the objectives scaled score
                        obj = "cmi.objectives." + i + ".score.scaled";

                        if( mObjStatus.mHasMeasure )
                        {
                           Double norm = new Double(mObjStatus.mMeasure);
                           err = DMInterface.processSetValue(obj, norm.toString(), true, mSCOData);
                        }
                     }
                  }
                  else
                  {
                     for( int i = 0; i < mStatusVector.size(); i++ )
                     {
                        int idx = -1;

                        // initialize objective status from sequencer
                        mObjStatus = (ADLObjStatus)mStatusVector.get(i);

                        // get the count of current objectives
                        DMProcessingInfo pi = new DMProcessingInfo();
                        int result = DMInterface.processGetValue("cmi.objectives._count", true, mSCOData, pi);

                        int objCount = ( new Integer(pi.mValue) ).intValue();

                        // Find the current index for this objective
                        for( int j = 0; j < objCount; j++ )
                        {
                           pi = new DMProcessingInfo();
                           obj = "cmi.objectives." + j + ".id";

                           result = DMInterface.processGetValue(obj, true, mSCOData, pi);

                           
                           if( pi.mValue.equals(mObjStatus.mObjID) )
                           {
                              
                              idx = j;
                              break;
                           }
                        }

                        if( idx != -1 )
                        {
                                            
                           // Set the objectives success status
                           obj = "cmi.objectives." + idx + ".success_status";

                           if( mObjStatus.mStatus.equalsIgnoreCase("satisfied") )
                           {
                              err = DMInterface.processSetValue(obj, "passed", true, mSCOData);
                           }
                           else if( mObjStatus.mStatus.equalsIgnoreCase("notSatisfied") )
                           {
                              err = DMInterface.processSetValue(obj, "failed", true, mSCOData);
                           }

                           // Set the objectives scaled score
                           obj = "cmi.objectives." + idx + ".score.scaled";

                           if( mObjStatus.mHasMeasure )
                           {
                              Double norm = new Double(mObjStatus.mMeasure);
                              err = DMInterface.processSetValue(obj, norm.toString(), true, mSCOData);
                           }
                        }
                        else
                        {
                           System.out.println("  OBJ NOT FOUND --> " + mObjStatus.mObjID);
                        }

                     }
                  }
               }

               mResponse.mActivityData = mSCOData;

               // Need to return time tracking information
               // -+- TODO -+-

               out.writeObject(mResponse);
               mLogger.info("LMSCMIServlet processed init");

               break;

            case LMSCMIServletRequest.TYPE_GET:

               mLogger.info("Processing 'get' request");

               mResponse = new LMSCMIServletResponse();

               // Try to open the state file
               try
               {
                  fi = new FileInputStream(mSCOFile);

                  mLogger.info("Created SCO data file input stream " + "successfully");

                  fileIn = new ObjectInputStream(fi);

                  mLogger.info("Created OBJECT input stream successfully");

                  mResponse.mActivityData = (SCODataManager)fileIn.readObject();

               }
               catch( FileNotFoundException fnfe )
               {
                  mLogger.fine("ERROR == State data not created");

                  mResponse.mError = "NO DATA";
               }

               fileIn.close();
               fi.close();

               out.writeObject(mResponse);

               mLogger.info("LMSCMIServlet processed get for SCO Data\n");

               break;

            case LMSCMIServletRequest.TYPE_SET:

               mLogger.info("Processing 'set' request");

               handleData(mRequest.mActivityData);

               out.writeObject(mResponse);

               mLogger.info("LMSCMIServlet processed set.");

               break;

            case LMSCMIServletRequest.TYPE_TIMEOUT:

               mLogger.info("Processing 'timeout' request");

               // -+- TODO -+-

               mLogger.info("LMSCMIServlet processed 'timeout'");

               break;

            default:

               mLogger.severe("ERROR:  Bad Request Type.");

               break;
         }

         // Close the input and output streams
         in.close();
         out.close();

      }
      catch( Exception e )
      {
         mLogger.severe(" :: doPost :: EXCEPTION");
         mLogger.severe(e.toString());
         e.printStackTrace();
      }
   }

   /**
    * This method handles processing of the core data being sent from the client
    * to the LMS. The data needs to be processed and made persistent.
    * 
    * @param iSCOData The run-time data to be processed.
    */
   private void handleData(SCODataManager iSCOData)
   {
      mLogger.info("LMSCMIServlet - Entering handleData()");
      mResponse = new LMSCMIServletResponse();

      String sampleRTERoot = File.separator + SRTEFILESDIR;
      String userDir = sampleRTERoot + File.separator + mUserID + File.separator + mCourseID;
      boolean setPrimaryObjScore = false;

      boolean suspended = false;

      try
      {
         String completionStatus = null;
         String SCOEntry = null;
         double normalScore = -1.0;
         String masteryStatus = null;
         String sessionTime = null;
         String score = null;

         SCODataManager scoData = mRequest.mActivityData;

         // call terminate on the sco data
         scoData.terminate();

         int err = 0;
         DMProcessingInfo dmInfo = new DMProcessingInfo();

         // Get the current completion_status
         err = DMInterface.processGetValue("cmi.completion_status", true, scoData, dmInfo);
         completionStatus = dmInfo.mValue;

         if( completionStatus.equals("not attempted") )
         {
            completionStatus = "incomplete";
         }

         // Get the current success_status
         err = DMInterface.processGetValue("cmi.success_status", true, scoData, dmInfo);
         masteryStatus = dmInfo.mValue;

         // Get the current entry
         err = DMInterface.processGetValue("cmi.entry", true, true, scoData, dmInfo);
         SCOEntry = dmInfo.mValue;

         // Get the current scaled score
         err = DMInterface.processGetValue("cmi.score.scaled", true, scoData, dmInfo);

         if( err == DMErrorCodes.NO_ERROR )
         {
            mLogger.info("Got score, with no error");
            score = dmInfo.mValue;
         }
         else
         {
            mLogger.info("Failed getting score, got err: " + err);
            score = "";
         }

         // Get the current session time
         err = DMInterface.processGetValue("cmi.session_time", true, scoData, dmInfo);
         if( err == DMErrorCodes.NO_ERROR )
         {
            sessionTime = dmInfo.mValue;
         }

         mLogger.info("Saving Data to the File ...  PRIOR TO SAVE");
         mLogger.info("The SCO Data Manager for the current SCO contains the " + "following:");

         // Open the Activity tree flat file associated with the
         // logged in user
         String theWebPath = getServletConfig().getServletContext().getRealPath("/");

         String actFile = userDir + File.separator + "serialize.obj";

         // Only perform data mapping on Terminate
         if( mRequest.mIsFinished )
         {
            mLogger.info("About to get and update activity tree");

            FileInputStream fi;
            try
            {
               fi = new FileInputStream(actFile);
            }
            catch( FileNotFoundException fnfe )
            {
               mLogger.severe("Can not open Activity tree file");

               fi = new FileInputStream(actFile);
            }

            mLogger.info("Created Activity FILE input stream successfully");

            ObjectInputStream fileIn = new ObjectInputStream(fi);

            mLogger.info("Created Activity Tree OBJECT input stream " + "successfully");

            SeqActivityTree theTree = (SeqActivityTree)fileIn.readObject();
            fileIn.close();
            fi.close();

            mLogger.info("(*********DUMPING ActivityTree***********)");
            if( theTree == null )
            {

               mLogger.info("The activity tree is NULL");
            }
            else
            {
               theTree.dumpState();
            }

            if( theTree != null )
            {
               // Create the sequencer and set the tree
               ADLSequencer theSequencer = new ADLSequencer();
               theSequencer.setActivityTree(theTree);

               SeqActivity act = theTree.getActivity(mActivityID);

               // Only modify the TM if the activity is tracked
               if( act.getIsTracked() )
               {

                  // Update the activity's status
                  mLogger.info(act.getID() + " is TRACKED -- ");
                  mLogger.info("Performing default mapping to TM");

                  String primaryObjID = null;
                  boolean foundPrimaryObj = false;
                  boolean setPrimaryObjSuccess = false;
                  boolean sesPrimaryObjScore = false;

                  // Find the primary objective ID
                  Vector objs = (Vector)act.getObjectives();

                  if( objs != null )
                  {
                     for( int j = 0; j < objs.size(); j++ )
                     {
                        SeqObjective obj = (SeqObjective)objs.elementAt(j);
                        if( obj.mContributesToRollup )
                        {
                           if( obj.mObjID != null )
                           {
                              primaryObjID = obj.mObjID;
                           }
                           break;
                        }
                     }
                  }

                  // Get the activities objective list
                  // Map the DM to the TM
                  err = DMInterface.processGetValue("cmi.objectives._count", true, scoData, dmInfo);
                  Integer size = new Integer(dmInfo.mValue);
                  int numObjs = size.intValue();

                  // Loop through objectives updating TM
                  for( int i = 0; i < numObjs; i++ )
                  {
                     mLogger.info("CMISerlet - IN MAP OBJ LOOP");
                     String objID = new String("");
                     String objMS = new String("");
                     String objScore = new String("");
                     String obj = new String("");

                     // Get this objectives id
                     obj = "cmi.objectives." + i + ".id";
                     err = DMInterface.processGetValue(obj, true, scoData, dmInfo);

                     objID = dmInfo.mValue;

                     if( primaryObjID != null && objID.equals(primaryObjID) )
                     {
                        foundPrimaryObj = true;
                     }
                     else
                     {
                        foundPrimaryObj = false;
                     }

                     // Get this objectives mastery
                     obj = "cmi.objectives." + i + ".success_status";
                     err = DMInterface.processGetValue(obj, true, scoData, dmInfo);
                     objMS = dmInfo.mValue;

                     // Report the success status
                     if( objMS.equals("passed") )
                     {
                        theSequencer.setAttemptObjSatisfied(mActivityID, objID, "satisfied");
                        if( foundPrimaryObj )
                        {
                           setPrimaryObjSuccess = true;
                        }
                     }
                     else if( objMS.equals("failed") )
                     {
                        theSequencer.setAttemptObjSatisfied(mActivityID, objID, "notSatisfied");

                        if( foundPrimaryObj )
                        {
                           setPrimaryObjSuccess = true;
                        }
                     }
                     else
                     {
                        theSequencer.setAttemptObjSatisfied(mActivityID, objID, "unknown");
                     }

                     // Get this objectives measure
                     obj = "cmi.objectives." + i + ".score.scaled";
                     err = DMInterface.processGetValue(obj, true, scoData, dmInfo);
                     if( err == DMErrorCodes.NO_ERROR )
                     {
                        objScore = dmInfo.mValue;
                     }

                     // Report the measure
                     if( !objScore.equals("") && !objScore.equals("unknown") )
                     {
                        try
                        {
                           normalScore = ( new Double(objScore) ).doubleValue();
                           theSequencer.setAttemptObjMeasure(mActivityID, objID, normalScore);

                           if( foundPrimaryObj )
                           {
                              setPrimaryObjScore = true;
                           }
                        }
                        catch( Exception e )
                        {
                           mLogger.severe("  ::--> ERROR: Invalid score");
                           mLogger.severe("  ::  " + normalScore);

                           mLogger.severe(e.toString());
                           e.printStackTrace();
                        }
                     }
                     else
                     {
                        theSequencer.clearAttemptObjMeasure(mActivityID, objID);
                     }
                  }

                  // Report the completion status
                  theSequencer.setAttemptProgressStatus(mActivityID, completionStatus);

                  if( SCOEntry.equals("resume") )
                  {
                     theSequencer.reportSuspension(mActivityID, true);
                  }
                  else
                  {

                     // Clean up session level SSP buckets
                     RTEFileHandler fileHandler = new RTEFileHandler();
                     fileHandler.deleteAttemptSSPData(mUserID, mCourseID, mSCOID);

                     theSequencer.reportSuspension(mActivityID, false);
                  }

                  // Report the success status
                  if( masteryStatus.equals("passed") )
                  {
                     theSequencer.setAttemptObjSatisfied(mActivityID, mPRIMARY_OBJ_ID, "satisfied");
                  }
                  else if( masteryStatus.equals("failed") )
                  {
                     theSequencer.setAttemptObjSatisfied(mActivityID, mPRIMARY_OBJ_ID, "notSatisfied");
                  }
                  else
                  {
                     if( !setPrimaryObjSuccess )
                     {
                        theSequencer.setAttemptObjSatisfied(mActivityID, mPRIMARY_OBJ_ID, "unknown");
                     }
                  }

                  // Report the measure
                  if( !score.equals("") && !score.equals("unknown") )
                  {
                     try
                     {
                        normalScore = ( new Double(score) ).doubleValue();
                        theSequencer.setAttemptObjMeasure(mActivityID, mPRIMARY_OBJ_ID, normalScore);
                     }
                     catch( Exception e )
                     {
                        mLogger.severe("  ::--> ERROR: Invalid score");
                        mLogger.severe("  ::  " + normalScore);

                        mLogger.severe(e.toString());
                        e.printStackTrace();
                     }
                  }
                  else
                  {
                     if( !setPrimaryObjScore )
                     {
                        theSequencer.clearAttemptObjMeasure(mActivityID, mPRIMARY_OBJ_ID);
                     }
                  }
               }

               // May need to get the current valid requests
               mResponse.mValidRequests = new ADLValidRequests();
               theSequencer.getValidRequests(mResponse.mValidRequests);

               mLogger.info("Sequencer is initialized and statuses have been " + "set");
               mLogger.info("Now re-serialize the file");

               FileOutputStream fo = new FileOutputStream(actFile);

               ObjectOutputStream outFile = new ObjectOutputStream(fo);

               ISeqActivityTree theTempTree = theSequencer.getActivityTree();

               theTempTree.clearSessionState();

               outFile.writeObject(theTempTree);
               outFile.close();
            }
         }

         // Persist the run-time data model
         FileOutputStream fo = new FileOutputStream(mSCOFile);
         ObjectOutputStream outFile = new ObjectOutputStream(fo);
         outFile.writeObject(mRequest.mActivityData);
         outFile.close();
         fo.close();
      }
      catch( Exception e )
      {

         mLogger.severe(e.toString());
         e.printStackTrace();

      }
   }

} // LMSCMIServlet
