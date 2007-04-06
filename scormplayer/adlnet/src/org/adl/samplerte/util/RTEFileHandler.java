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
package org.adl.samplerte.util;

//Native java imports
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.adl.datamodels.DMFactory;
import org.adl.datamodels.DMInterface;
import org.adl.datamodels.SCODataManager;
import org.adl.util.debug.DebugIndicator;
import org.ims.ssp.samplerte.util.SSP_DBHandler;
import org.ims.ssp.samplerte.server.bucket.Persistence;

/**
 * <strong>Filename:</strong> RTEFilehandler.java<br><br>
 *
 * <strong>Description:</strong><br>
 * <code>RTEFilehandler</code> class is a utility class.  It contains methods 
 * to create a state file used to store datamodel information and to inititalize
 * datamodel elements based on elements in the imsmanifest.xml file.  It 
 * contains a method that queries teh database for initialized datamodel values 
 * and stores those values in String array.  In  addition, this class contains 
 * logic to delete any course files and temporary uploaded packages after a 
 * successful import.<br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the SCORM 2004 3rd Edition Sample 
 * Run-Time Environment Version 1.0.<br>
 * <br>
 * 
 * <strong>Implementation Issues:</strong><br><br>
 * 
 * <strong>Known Problems:</strong><br><br>
 * 
 * <strong>Side Effects:</strong><br><br>
 * 
 * @author ADL Technical Team
 */
public class RTEFileHandler
{
   /**
    * Debug indicator boolean
    */
   private static boolean _Debug = DebugIndicator.ON; 
   
   /**
    * The Sample RTE flat file root directory
    */
   private String mSampleRTERoot;

   

   /**
    * Default Constructor for the <code>RTEFileHandler</code>
    */
   public RTEFileHandler()
   {
      mSampleRTERoot = File.separator + "SCORM3rdSampleRTE10Files";
   }

   /**
    * This method is used to create a state file for an item.  It is called
    * by <code>LMSCMIServlet</code>when a item is entered for the first time.
    * 
    * @param iNumAttempt  Number of the current attempt on the item
    * @param iUserID  The Sample RTE's unique user identifier for
    *        a learner 
    * @param iUserName  The name of the user 
    * @param iCourseID  The unique course identifier
    * @param iItemID  The item's identifier
    * @param iDbID  The item's unique identifier (may be the same as the iItemID)
    * @param iSSPLocation The url of the ssp server
    */
   public void initializeStateFile(String iNumAttempt, String iUserID,
                                   String iUserName, String iCourseID,
                                   String iItemID, String iDbID ,
                                   String iSSPLocation)
   {
      try
      {
         if ( _Debug )
         {
            System.out.println("**** IN INITIALIZESTATEFILE****");
         }
         
         String userDir = mSampleRTERoot + File.separator + iUserID + 
                           File.separator + iCourseID;

         File scoDataDir = new File( userDir );

         // The course directory should not exist yet
         if ( !scoDataDir.isDirectory() )
         {                              
            if ( _Debug )
            {
               System.out.println("User directory does not exist");
            }
            scoDataDir.mkdirs();
         }
         else
         {
            if ( _Debug )
            {
               System.out.println("In RTEFileHandler user directory " + 
                                  "already exists.");
            }
         }

         if ( _Debug )
         {
            System.out.println("In RTEFileHandler - scoID is " + iItemID);
         }

         // Now create a SCODataManager object, initialize values, and 
         // serialize to file for SCO
         String scoDataPath = userDir + File.separator + iItemID + "__" 
                                  + iNumAttempt;
         SCODataManager scoData = new SCODataManager();
         
         //  Add a SCORM 2004 Data Model
         scoData.addDM(DMFactory.DM_SCORM_2004);

         //  Add a SCORM 2004 Nav Data Model
         scoData.addDM(DMFactory.DM_SCORM_NAV);

         //  Add a SSP Datamodel
         scoData.addDM(DMFactory.DM_SSP);

         initSCOData(scoData, iUserID, iUserName, iCourseID, iDbID);

         // SSP addition
         initSSPData(scoData, iCourseID, iItemID, 
                     iNumAttempt, iUserID, iSSPLocation);


         File scoDataFile = new File(scoDataPath);

         // Create a new file if and only if it doesn't already exist
         boolean mNoFileExists = scoDataFile.createNewFile();

         //new file was created
         if ( mNoFileExists )
         {
            // Write out the data to disk using serialization
            FileOutputStream fos = new FileOutputStream(scoDataFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(scoData);
            oos.close();
            fos.close();

            if ( _Debug )
            {
               System.out.println("RTEFileHandler created State file for: " 
                                   + iItemID);
            }
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
       
      }
   }


   /**
    * This method deletes the course files for a student/course when a 
    * registration is removed.
    * 
    * @param iDeleteCourseID  The courseId of course to delete course files
    * 
    * @param iUserID  The user identifier associated with the files to delete
    */                                                      
   public void deleteCourseFiles(String iDeleteCourseID, String iUserID)
   {
      try
      {
         String userDir = mSampleRTERoot + File.separator + iUserID + 
                          File.separator + iDeleteCourseID;

         if ( _Debug )
         {
            System.out.println("path  " + userDir);
         }

         File scoDataDir = new File(userDir);

         File scoFiles[] = scoDataDir.listFiles();

         for ( int i = 0; i < scoFiles.length; i++ )
         {
            scoFiles[i].delete();
         }

         scoDataDir.delete();
      }
      catch(Exception e)
      {            
         System.out.println("Error deleting files during un-registration");
         e.printStackTrace();
      }
   }

   /**
    * This function deletes the files in the SCORM3rdSampleRTE10Files\tempUploads 
    * directory.
    * 
    */
   public void deleteTempUloadFiles()
   {
      try
      {
         String tempDir = mSampleRTERoot + File.separator + "tempUploads";

         File tempUploadDir = new File(tempDir);

         File tempDirs[] = tempUploadDir.listFiles();

         for ( int i = 0; i < tempDirs.length; i++ )
         {
            File tempFiles[] = tempDirs[i].listFiles();

            for ( int j = 0; j < tempFiles.length; j++ )
            {
               tempFiles[j].delete();
            }
            tempDirs[i].delete();
         }
      }
      catch(Exception e)
      {
         
         System.out.println("Error deleting files in the " + 
                               "tempUploads directory");              
         e.printStackTrace();
      }

   }

   /**
    * This method is used to get initialized data model element values
    * from the database.  The initialized values are returned in a String 
    * array in the following order: cmi.scaled_passing_score, cmi.launch_data,
    * cmi.max_time_allowed, cmi.time_limit_action, 
    * cmi.completion_threshold
    * 
    * @param ioSCOData The manager of SCO Data   
    * @param iCourseID  The id of the course
    * @param iItemID    The item ID
    * @param iUserID   The user ID
    */
   public void initPersistedData(SCODataManager ioSCOData, String iCourseID, 
                                 String iItemID, String iUserID)
   {
      try
      {  
         
         // Get some information from the database
         Connection conn = LMSDatabaseHandler.getConnection();

         PreparedStatement stmtSelectItem;
         PreparedStatement stmtSelectComments;
         PreparedStatement stmtSelectUser;

         String sqlSelectItem = "SELECT * FROM ItemInfo WHERE CourseID " + 
                                "= ? AND ItemIdentifier = ?";
         String sqlSelectComments = "SELECT * FROM SCOComments WHERE " +
                                    "ActivityID = ?";
         String sqlSelectUser = "SELECT * FROM UserInfo WHERE UserID = ?";


         stmtSelectItem = conn.prepareStatement(sqlSelectItem);
         stmtSelectComments = conn.prepareStatement(sqlSelectComments);
         stmtSelectUser = conn.prepareStatement(sqlSelectUser);
         
         ResultSet rsItem = null;
         ResultSet rsUser = null;

         synchronized(stmtSelectItem)
         {
            stmtSelectItem.setString(1, iCourseID);
            stmtSelectItem.setString(2, iItemID);
            rsItem = stmtSelectItem.executeQuery();
         }

         synchronized(stmtSelectUser)
         {
            stmtSelectUser.setString(1, iUserID);
            rsUser = stmtSelectUser.executeQuery();
         }

         String masteryScore = new String();
         String dataFromLMS = new String();
         String maxTime = new String();
         String timeLimitAction = new String();
         String completionThreshold = new String();
         String audLev = new String();
         String audCap = new String();
         String delSpd = new String();
         String lang = new String();
         int activityID;

         // Get the learner preference values from the database
         if ( rsUser.next() )
         {
            audLev = rsUser.getString("AudioLevel");
            audCap = rsUser.getString("AudioCaptioning");
            delSpd = rsUser.getString("DeliverySpeed");
            lang = rsUser.getString("Language");
         }
         

         while( rsItem.next() )
         {
            String type = rsItem.getString("Type");

            if ( type.equals("sco") || type.equals("asset") )
            {
               masteryScore = rsItem.getString("MinNormalizedMeasure");
               dataFromLMS = rsItem.getString("DataFromLMS");
               maxTime = rsItem.getString("AttemptAbsoluteDurationLimit");
               timeLimitAction = rsItem.getString("TimeLimitAction");
               completionThreshold = rsItem.getString("CompletionThreshold");
               activityID = rsItem.getInt("ActivityID");

               ResultSet rsComments = null;

               // Get the comments associated with an activity if any
               synchronized(stmtSelectComments)
               {
                  stmtSelectComments.setInt(1, activityID);
                  rsComments = stmtSelectComments.executeQuery();
               }

               // Loop through the comments and initialize the SCO data\
               int idx = 0;
               while ( rsComments.next() )
               {
                  String cmt = rsComments.getString("Comment");
                  String elem = "cmi.comments_from_lms." + idx + ".comment";
                  
                  DMInterface.processSetValue(elem, cmt, true, ioSCOData);

                  String cmtDT = rsComments.getString("CommentDateTime");
                  elem = "cmi.comments_from_lms." + idx + ".timestamp";
                  
                  DMInterface.processSetValue(elem, cmtDT, true, ioSCOData); 

                  String cmtLoc = rsComments.getString("CommentLocation");
                  elem = "cmi.comments_from_lms." + idx + ".location";
                  
                  DMInterface.processSetValue(elem, cmtLoc, true, ioSCOData);
                  
                  idx++;
               }

            }
         }

         stmtSelectItem.close();
         stmtSelectComments.close();
         conn.close();


         String element = new String();

         // Initialize the cmi.credit value
         element = "cmi.credit";
         DMInterface.processSetValue(element, "credit", true, ioSCOData);

         // Initialize the mode 
         element = "cmi.mode";
         DMInterface.processSetValue(element, "normal", true, ioSCOData);

         // Initialize any launch data 
         if ( dataFromLMS != null && ! dataFromLMS.equals("") )
         {
            element = "cmi.launch_data";
            DMInterface.processSetValue(element, dataFromLMS, true, ioSCOData);
         }
         
         // Initialize the scaled passing score 
         if ( masteryScore != null && ! masteryScore.equals("") )
         {
            element = "cmi.scaled_passing_score";
            DMInterface.processSetValue(element, masteryScore, true, ioSCOData);
         }

         // Initialize the time limit action 
         if ( timeLimitAction != null && ! timeLimitAction.equals("") )
         {
            element = "cmi.time_limit_action";
            DMInterface.processSetValue(element, timeLimitAction, true, 
                                        ioSCOData);
         }

         // Initialize the completion_threshold
         if ( completionThreshold != null && ! completionThreshold.equals("") )
         {
            element = "cmi.completion_threshold";
            DMInterface.processSetValue(element, completionThreshold, 
                                        true, ioSCOData);
         }

         // Initialize the max time allowed 
         if ( maxTime != null && !  maxTime.equals("") )
         {
            element = "cmi.max_time_allowed";
            DMInterface.processSetValue(element, maxTime, true, ioSCOData);
         }

         // Initialize the learner preferences based on the SRTE 
         // learner profile information

         // audio_level
         element = "cmi.learner_preference.audio_level";
         DMInterface.processSetValue(element, audLev, true, ioSCOData);

         // audio_captioning
         element = "cmi.learner_preference.audio_captioning";
         DMInterface.processSetValue(element, audCap, true, ioSCOData);

         // delivery_speed
         element = "cmi.learner_preference.delivery_speed";
         DMInterface.processSetValue(element, delSpd, true, ioSCOData);
         
         // language
         element = "cmi.learner_preference.language";
         DMInterface.processSetValue(element, lang, true, ioSCOData);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }


    /**
    * initSCOData
    * 
    * @param ioSCOData  The SCODataManager whose values are initialized.
    * @param iUserID    The user ID
    * @param iUserName  The user name
    * @param iCourseID  The id of the course
    * @param iItemID    The item ID
    * 
    */
   private void initSCOData(SCODataManager ioSCOData, String iUserID,
                            String iUserName, String iCourseID, String iItemID)
   {
      try
      {
         // Get some information from the database
         Connection conn = LMSDatabaseHandler.getConnection();

         PreparedStatement stmtSelectItem;
         PreparedStatement stmtSelectComments;
         PreparedStatement stmtSelectUser;

         String sqlSelectItem = "SELECT * FROM ItemInfo WHERE CourseID " + 
                                "= ? AND ItemIdentifier = ?";
         String sqlSelectComments = "SELECT * FROM SCOComments WHERE " +
                                    "ActivityID = ?";
         String sqlSelectUser = "SELECT * FROM UserInfo WHERE UserID = ?";


         stmtSelectItem = conn.prepareStatement(sqlSelectItem);
         stmtSelectComments = conn.prepareStatement(sqlSelectComments);
         stmtSelectUser = conn.prepareStatement(sqlSelectUser);

         if ( _Debug )
         {
            System.out.println("about to call item in RTEFile");
            System.out.println("userID: " + iUserID);
            System.out.println("courseID: " + iCourseID);
            System.out.println("scoID: " + iItemID);
         }

         ResultSet rsItem = null;
         ResultSet rsUser = null;

         synchronized(stmtSelectItem)
         {
            stmtSelectItem.setString(1, iCourseID);
            stmtSelectItem.setString(2, iItemID);
            rsItem = stmtSelectItem.executeQuery();
         }

         synchronized(stmtSelectUser)
         {
            stmtSelectUser.setString(1, iUserID);
            rsUser = stmtSelectUser.executeQuery();
         }


         if ( _Debug )
         {
            System.out.println("call to itemRS is complete");
         }

         String masteryScore = new String();
         String dataFromLMS = new String();
         String maxTime = new String();
         String timeLimitAction = new String();
         String completionThreshold = new String();
         String audLev = new String();
         String audCap = new String();
         String delSpd = new String();
         String lang = new String();
         int activityID;

         // Get the learner preference values from the database
         if ( rsUser.next() )
         {
            audLev = rsUser.getString("AudioLevel");
            audCap = rsUser.getString("AudioCaptioning");
            delSpd = rsUser.getString("DeliverySpeed");
            lang = rsUser.getString("Language");
         }

         // loop through the result set until we find the correct record
         while ( rsItem.next() )
         {
            /* 
             * checks the item identifier from the record set against the
             * item id to make sure that the id is exactly the same (including
             * case sensitivity) since the SQL call will ignores case
             */
            if ( rsItem.getString("ItemIdentifier").equals(iItemID) )
            {
               String type = rsItem.getString("Type");

               if ( type.equals("sco") || type.equals("asset") )
               {
                  masteryScore = rsItem.getString("MinNormalizedMeasure");
                  dataFromLMS = rsItem.getString("DataFromLMS");
                  maxTime = rsItem.getString("AttemptAbsoluteDurationLimit");
                  timeLimitAction = rsItem.getString("TimeLimitAction");
                  completionThreshold = rsItem.getString("CompletionThreshold");
                  activityID = rsItem.getInt("ActivityID");

                  ResultSet rsComments = null;

                  // Get the comments associated with an activity if any
                  synchronized(stmtSelectComments)
                  {
                     stmtSelectComments.setInt(1, activityID);
                     rsComments = stmtSelectComments.executeQuery();
                  }

                  // Loop through the comments and initialize the SCO data\
                  int idx = 0;
                  while ( rsComments.next() )
                  {
                     String cmt = rsComments.getString("Comment");
                     String elem = "cmi.comments_from_lms." + idx + ".comment";
                     
                     DMInterface.processSetValue(elem, cmt, true, ioSCOData); 
                     String cmtDT = rsComments.getString("CommentDateTime");
                     elem = "cmi.comments_from_lms." + idx + ".timestamp";
                     
                     DMInterface.processSetValue(elem, cmtDT, true, ioSCOData);

                     String cmtLoc = rsComments.getString("CommentLocation");
                     elem = "cmi.comments_from_lms." + idx + ".location";
                     
                     DMInterface.processSetValue(elem, cmtLoc, true, ioSCOData);                                    

                     idx++;
                  }
               }
               // breaking out of the loop if we hit the right ID
               break;
            }
         }

         stmtSelectItem.close();
         conn.close();

         String element = new String();

         // Initialize the learner id
         element = "cmi.learner_id";
         DMInterface.processSetValue(element, iUserID, true, ioSCOData);

         // Initialize the learner name
         element = "cmi.learner_name";
         DMInterface.processSetValue(element, iUserName, true, ioSCOData);

         // Initialize the cmi.credit value
         element = "cmi.credit";
         DMInterface.processSetValue(element, "credit", true, ioSCOData);

         // Initialize the mode 
         element = "cmi.mode";
         DMInterface.processSetValue(element, "normal", true, ioSCOData);

         // Initialize any launch data 
         if ( dataFromLMS != null && ! dataFromLMS.equals("") )
         {
            element = "cmi.launch_data";
            DMInterface.processSetValue(element, dataFromLMS, true, ioSCOData);
         }
         
         // Initialize the scaled passing score 
         if ( masteryScore != null && ! masteryScore.equals("") )
         {
            element = "cmi.scaled_passing_score";
            DMInterface.processSetValue(element, masteryScore, true, ioSCOData);
         }

         // Initialize the time limit action 
         if ( timeLimitAction != null && ! timeLimitAction.equals("") )
         {
            element = "cmi.time_limit_action";
            DMInterface.processSetValue(element, timeLimitAction, true, 
                                        ioSCOData);
         }

         // Initialize the completion_threshold
         if ( completionThreshold != null && ! completionThreshold.equals("") )
         {
            element = "cmi.completion_threshold";
            DMInterface.processSetValue(element, completionThreshold, 
                                        true, ioSCOData);
         }

         // Initialize the max time allowed 
         if ( maxTime != null && !  maxTime.equals("") )
         {
            element = "cmi.max_time_allowed";
            DMInterface.processSetValue(element,  maxTime, true, ioSCOData);
         }

         // Initialize the learner preferences based on the SRTE 
         // learner profile information

         // audio_level
         element = "cmi.learner_preference.audio_level";
         DMInterface.processSetValue(element, audLev, true, ioSCOData);

         // audio_captioning
         element = "cmi.learner_preference.audio_captioning";
         DMInterface.processSetValue(element, audCap, true, ioSCOData);

         // delivery_speed
         element = "cmi.learner_preference.delivery_speed";
         DMInterface.processSetValue(element, delSpd, true, ioSCOData);
         
         // language
         element = "cmi.learner_preference.language";
         DMInterface.processSetValue(element, lang, true, ioSCOData);

      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }


   /**
    * This method is used to initialize certain data model elements.
    * This method is called by <code>initializeStateFiles</code> to initialize
    * data model information based on SSP specific information.
    *
    * @param ioSCOData  The SCODataManager whose values are initialized.
    * @param iCourseID The String representation of the course ID.
    * @param iScoID The String representation of the SCO ID.
    * @param iNumAttempt The String representation of the current attempt number.
    * @param iUserID The String representation of the course ID.
    * @param iSSPLocation The String representation of the course ID.
    */
   private void initSSPData( SCODataManager ioSCOData, String iCourseID,
                             String iScoID, String iNumAttempt, String iUserID,
                             String iSSPLocation )
   {
      String element = new String();
      int err;

      // Initialize the learner id
      element = "ssp.init.userid";
      err = DMInterface.processSetValue(element, iUserID, true,
                                        ioSCOData);

      // Initialize the course id
      element = "ssp.init.courseid";
      err = DMInterface.processSetValue(element, iCourseID, true,
                                        ioSCOData);

      // Initialize the attempt id
      element = "ssp.init.attemptnum";
      err = DMInterface.processSetValue(element, iNumAttempt, true,
                                        ioSCOData);

      // Initialize the attempt id
      element = "ssp.init.url";
      err = DMInterface.processSetValue(element, iSSPLocation, true,
                                        ioSCOData);

      // Initialize the sco id
      element = "ssp.init.scoID";
      err = DMInterface.processSetValue(element, iScoID, true,
                                        ioSCOData);


      try
      {
         // Get some information from the database
         Connection conn = SSP_DBHandler.getConnection();
         PreparedStatement stmtSelectSSP_BucketTbl;

         String sqlSelectSSP_BucketTbl = "SELECT * FROM SSP_BucketTbl" +
                                         " WHERE CourseID = ? AND ScoID = ?";
         stmtSelectSSP_BucketTbl = conn.prepareStatement(sqlSelectSSP_BucketTbl);

         if ( _Debug )
         {
            System.out.println("about to call SSP_BucketTbl in initSSPData");
            System.out.println("courseID: " + iCourseID);
            System.out.println("scoID: " + iScoID);
         }

         ResultSet rsSSP_BucketTbl = null;

         synchronized( stmtSelectSSP_BucketTbl )
         {
            stmtSelectSSP_BucketTbl.setString(1, iCourseID);
            stmtSelectSSP_BucketTbl.setString(2, iScoID);
            rsSSP_BucketTbl = stmtSelectSSP_BucketTbl.executeQuery();
         }

         if ( _Debug )
         {
            System.out.println("call to SSP_BucketTbl is complete");
         }

         String bucketID = new String();
         String bucketType = new String();
         String persistence = new String();
         String minSize = new String();
         String requestedSize = new String();
         String reducible = new String();

         String allocateString = new String();

         int errorCode = 0;

         // Get the values for the buckets defined in the manifest
         // from the database
         while ( rsSSP_BucketTbl.next() )
         {
            bucketID = rsSSP_BucketTbl.getString("BucketID");
            bucketType = rsSSP_BucketTbl.getString("BucketType");
            persistence = rsSSP_BucketTbl.getString("Persistence");
            minSize = rsSSP_BucketTbl.getString("Min");
            requestedSize = rsSSP_BucketTbl.getString("Requested");
            reducible = rsSSP_BucketTbl.getString("Reducible");

            // Allocate for each bucket listed in the Manifest
            // CHeck for MANDATORY BucketID
            if ( (bucketID == null) || (bucketID.equals("")) )
            {
               err = 351;
            }
            else
            {
               allocateString = "{bucketID=" + bucketID + "}";
            }

            // Check for MANDATORY requested
            if ( (requestedSize == null) || (requestedSize.equals("")) )
            {
               err = 351;
            }
            else
            {
               allocateString += "{requested=" + requestedSize + "}";
            }

            // Check for OPTIONAL type
            if ( (bucketType == null ) || (bucketType.equals("")) )
            {
               allocateString = allocateString;
            }
            else
            {
               allocateString += "{type=" + bucketType + "}";
            }

            // Check for OPTIONAL persistence
            if ( (persistence == null ) || (persistence.equals("")) )
            {
               allocateString = allocateString;
            }
            else
            {
               allocateString += "{persistence=" + persistence + "}";
            }

            // Check for OPTIONAL min
            if ( (minSize == null ) || (minSize.equals("")) )
            {
               allocateString = allocateString;
            }
            else
            {
               allocateString += "{minimum=" + minSize + "}";
            }

            // Check for OPTIONAL reducible
            if ( (reducible == null ) || (reducible.equals("")) )
            {
               allocateString = allocateString;
            }
            else
            {
               allocateString += "{reducible=" + reducible + "}";
            }

            if ( err != 351 )
            {
               err =
               DMInterface.processSetValue("ssp.allocate" , allocateString ,
                                           true, ioSCOData );
            }

         }

         stmtSelectSSP_BucketTbl.close();
         conn.close();

      }
      catch ( Exception e )
      {
         e.printStackTrace();
      }
   }


   /**
    * This method is used to deal with cleaning up buckets and the SSP
    * Database when a course is removed from the system for all learners.
    *
    * @param iCourseID - The ID of the course to be removed.
    */
   public void deleteCourseSSPData( String iCourseID )
   {

      try
      {
         // Get some information from the database
         Connection conn = SSP_DBHandler.getConnection();
         PreparedStatement stmtSelectSSP_BucketAllocateTbl;
         PreparedStatement stmtDeleteSSP_BucketAllocateTbl;

         String sqlSelectSSP_BucketAllocateTbl = 
            "SELECT * FROM SSP_BucketAllocateTbl" +
            " WHERE CourseID = ? AND Persistence <> ?";

         String sqlDeleteSSP_BucketAllocateTbl = 
            "DELETE FROM SSP_BucketAllocateTbl" +
            "  WHERE CourseID = ? AND Persistence <> ?";

         stmtSelectSSP_BucketAllocateTbl = 
            conn.prepareStatement(sqlSelectSSP_BucketAllocateTbl);

         stmtDeleteSSP_BucketAllocateTbl = 
            conn.prepareStatement(sqlDeleteSSP_BucketAllocateTbl);

         if ( _Debug )
         {
            System.out.println("about to call SSP_BucketAllocateTbl in " +
                               "deleteCourseSSPData");
            System.out.println("courseID: " + iCourseID);
         }

         ResultSet rsSSP_BucketAllocateTbl = null;

         synchronized( stmtSelectSSP_BucketAllocateTbl )
         {
            stmtSelectSSP_BucketAllocateTbl.setString(1, iCourseID);
            stmtSelectSSP_BucketAllocateTbl.setInt(2, Persistence.LEARNER);
            rsSSP_BucketAllocateTbl = 
               stmtSelectSSP_BucketAllocateTbl.executeQuery();
         }

         if ( _Debug )
         {
            System.out.println("call to SSP_BucketAllocateTbl is complete");
         }

         String bucketID = new String();
         String learnerID = new String();
         String attemptID = new String();
         int persistence = 0;

         String bucket = new String();

         // Get all of the buckets allocated for the course and remove the
         //  bucket files.
         while ( rsSSP_BucketAllocateTbl.next() )
         {
            bucketID = rsSSP_BucketAllocateTbl.getString("BucketID");
            learnerID = rsSSP_BucketAllocateTbl.getString("LearnerID");
            persistence = rsSSP_BucketAllocateTbl.getInt("Persistence");
            attemptID = rsSSP_BucketAllocateTbl.getString("AttemptID");

            bucket = mSampleRTERoot + File.separator + learnerID +
                     File.separator + bucketID + iCourseID;

            if ( persistence == Persistence.SESSION )
            {
               bucket = bucket + attemptID;
            }

            bucket = bucket + ".obj";

            if ( _Debug )
            {
               System.out.println("path  " + bucket);
            }

            File bucketFile = new File( bucket );
            bucketFile.delete();

         }


         // Now cleanup the SSP Allocate database to remove the course
         synchronized( stmtDeleteSSP_BucketAllocateTbl )
         {
            stmtDeleteSSP_BucketAllocateTbl.setString(1, iCourseID);
            stmtDeleteSSP_BucketAllocateTbl.setInt(2, Persistence.LEARNER);
            stmtDeleteSSP_BucketAllocateTbl.executeUpdate();
         }

         stmtSelectSSP_BucketAllocateTbl.close();
         stmtDeleteSSP_BucketAllocateTbl.close();
         conn.close();

      }
      catch ( Exception e )
      {
         e.printStackTrace();
      }
   }


   /**
    * This method is used to deal with cleaning up buckets and the SSP
    * Database when a course is removed from the system for a single
    * learner.
    *
    * @param iCourseID - The ID of the course to be removed.
    * @param iLearnerID - The ID of the learner enrolled in the course.
    */
   public void deleteCourseSSPData( String iCourseID, String iLearnerID )
   {

      try
      {
         // Get some information from the database
         Connection conn = SSP_DBHandler.getConnection();
         PreparedStatement stmtSelectSSP_BucketAllocateTbl;
         PreparedStatement stmtDeleteSSP_BucketAllocateTbl;

         String sqlSelectSSP_BucketAllocateTbl = 
            "SELECT * FROM SSP_BucketAllocateTbl" +
            " WHERE CourseID = ? AND LearnerID = ? AND Persistence <> ?";

         String sqlDeleteSSP_BucketAllocateTbl = 
            "DELETE FROM SSP_BucketAllocateTbl" +
            " WHERE CourseID = ? AND LearnerID = ? AND Persistence <> ?";

         stmtSelectSSP_BucketAllocateTbl = 
            conn.prepareStatement(sqlSelectSSP_BucketAllocateTbl);

         stmtDeleteSSP_BucketAllocateTbl = 
            conn.prepareStatement(sqlDeleteSSP_BucketAllocateTbl);

         if ( _Debug )
         {
            System.out.println("about to call SSP_BucketAllocateTbl in " +
                               "deleteCourseSSPData");

            System.out.println("courseID: " + iCourseID);
         }

         ResultSet rsSSP_BucketAllocateTbl = null;

         synchronized( stmtSelectSSP_BucketAllocateTbl )
         {
            stmtSelectSSP_BucketAllocateTbl.setString(1, iCourseID);
            stmtSelectSSP_BucketAllocateTbl.setString(2, iLearnerID);
            stmtSelectSSP_BucketAllocateTbl.setInt(3, Persistence.LEARNER);
            rsSSP_BucketAllocateTbl = 
               stmtSelectSSP_BucketAllocateTbl.executeQuery();
         }

         if ( _Debug )
         {
            System.out.println("call to SSP_BucketAllocateTbl is complete");
         }

         String bucketID = new String();
         String learnerID = new String();
         String attemptID = new String();
         int persistence = 0;

         String bucket = new String();

         // Get all of the buckets allocated for the course and remove the
         //  bucket files.
         while ( rsSSP_BucketAllocateTbl.next() )
         {
            bucketID = rsSSP_BucketAllocateTbl.getString("BucketID");
            persistence = rsSSP_BucketAllocateTbl.getInt("Persistence");
            attemptID = rsSSP_BucketAllocateTbl.getString("AttemptID");

            bucket = mSampleRTERoot + File.separator + iLearnerID +
                     File.separator + bucketID + iCourseID;

            if ( persistence == Persistence.SESSION )
            {
               bucket = bucket + attemptID;
            }

            bucket = bucket + ".obj";

            if ( _Debug )
            {
               System.out.println("path  " + bucket);
            }

            File bucketFile = new File( bucket );
            bucketFile.delete();

         }


         // Now cleanup the SSP Allocate database to remove the course
         synchronized( stmtDeleteSSP_BucketAllocateTbl )
         {
            stmtDeleteSSP_BucketAllocateTbl.setString(1, iCourseID);
            stmtDeleteSSP_BucketAllocateTbl.setString(2, iLearnerID);
            stmtDeleteSSP_BucketAllocateTbl.setInt(3, Persistence.LEARNER);
            stmtDeleteSSP_BucketAllocateTbl.executeUpdate();
         }

         stmtSelectSSP_BucketAllocateTbl.close();
         stmtDeleteSSP_BucketAllocateTbl.close();
         conn.close();

      }
      catch ( Exception e )
      {
         e.printStackTrace();
      }
   }



   /**
    * This method is used to deal with cleaning up buckets and the SSP
    * Database when a learner is removed from the system.
    *
    * @param iLearnerID - The ID of the learner to be removed.
    */
   public void deleteLearnerSSPData( String iLearnerID )
   {

      try
      {
         // Get some information from the database
         Connection conn = SSP_DBHandler.getConnection();
         PreparedStatement stmtSelectSSP_BucketAllocateTbl;
         PreparedStatement stmtDeleteSSP_BucketAllocateTbl;

         String sqlSelectSSP_BucketAllocateTbl = 
            "SELECT * FROM SSP_BucketAllocateTbl" +
            " WHERE LearnerID = ?";

         String sqlDeleteSSP_BucketAllocateTbl = 
            "DELETE FROM SSP_BucketAllocateTbl" +
            " WHERE LearnerID = ?";

         stmtSelectSSP_BucketAllocateTbl =
            conn.prepareStatement(sqlSelectSSP_BucketAllocateTbl);

         stmtDeleteSSP_BucketAllocateTbl = 
            conn.prepareStatement(sqlDeleteSSP_BucketAllocateTbl);

         if ( _Debug )
         {
            System.out.println("about to call SSP_BucketAllocateTbl in " +
                               "deleteLearnerSSPData");

            System.out.println("learnerID: " + iLearnerID);
         }

         ResultSet rsSSP_BucketAllocateTbl = null;

         synchronized( stmtSelectSSP_BucketAllocateTbl )
         {
            stmtSelectSSP_BucketAllocateTbl.setString(1, iLearnerID);
            rsSSP_BucketAllocateTbl = 
               stmtSelectSSP_BucketAllocateTbl.executeQuery();
         }

         if ( _Debug )
         {
            System.out.println("call to SSP_BucketAllocateTbl is complete");
         }

         String bucketID = new String();
         String courseID = new String();
         String attemptID = new String();
         int persistence = 0;

         String bucket = new String();

         // Get all of the buckets allocated for the learner and remove the
         //  bucket files.
         while ( rsSSP_BucketAllocateTbl.next() )
         {
            bucketID = rsSSP_BucketAllocateTbl.getString("BucketID");
            courseID = rsSSP_BucketAllocateTbl.getString("CourseID");
            persistence = rsSSP_BucketAllocateTbl.getInt("Persistence");
            attemptID = rsSSP_BucketAllocateTbl.getString("AttemptID");

            bucket = mSampleRTERoot + File.separator + iLearnerID +
                     File.separator + bucketID;

            if ( persistence == Persistence.COURSE )
            {
               bucket = bucket + courseID;
            }
            else if ( persistence == Persistence.SESSION )
            {
               bucket = bucket + courseID + attemptID;
            }

            bucket = bucket + ".obj";

            if ( _Debug )
            {
               System.out.println("path  " + bucket);
            }

            File bucketFile = new File( bucket );
            bucketFile.delete();

         }


         // Now cleanup the SSP Allocate database to remove the course
         synchronized( stmtDeleteSSP_BucketAllocateTbl )
         {
            stmtDeleteSSP_BucketAllocateTbl.setString(1, iLearnerID);
            stmtDeleteSSP_BucketAllocateTbl.executeUpdate();
         }

         stmtSelectSSP_BucketAllocateTbl.close();
         stmtDeleteSSP_BucketAllocateTbl.close();
         conn.close();

      }
      catch ( Exception e )
      {
         e.printStackTrace();
      }
   }


   /**
    * This method is used to deal with cleaning up buckets and the SSP
    * Database when an attempt is completed for a resource.
    *
    * @param iLearnerID  The ID of the learner.
    * @param iCourseID  The ID of the course.
    * @param iSCOID  The ID of the SCO
    */
   public void deleteAttemptSSPData( String iLearnerID, String iCourseID,
                                     String iSCOID )
   {

      try
      {
         // Get some information from the database
         Connection conn = SSP_DBHandler.getConnection();
         PreparedStatement stmtSelectSSP_BucketAllocateTbl;
         PreparedStatement stmtDeleteSSP_BucketAllocateTbl;

         String sqlSelectSSP_BucketAllocateTbl = 
            "SELECT * FROM SSP_BucketAllocateTbl" +
            " WHERE LearnerID = ? AND CourseID = ?" +
            " AND SCOID = ?" +
            " AND Persistence = ?";

         String sqlDeleteSSP_BucketAllocateTbl = 
            "DELETE FROM SSP_BucketAllocateTbl" +
            " WHERE LearnerID = ? AND CourseID = ?" +
            " AND SCOID = ?" +
            " AND Persistence = ?";

         stmtSelectSSP_BucketAllocateTbl = 
            conn.prepareStatement(sqlSelectSSP_BucketAllocateTbl);

         stmtDeleteSSP_BucketAllocateTbl = 
            conn.prepareStatement(sqlDeleteSSP_BucketAllocateTbl);

         if ( _Debug )
         {
            System.out.println("about to call SSP_BucketAllocateTbl in " +
                               "deleteLearnerSSPData");

            System.out.println("learnerID: " + iLearnerID);
         }

         ResultSet rsSSP_BucketAllocateTbl = null;

         synchronized( stmtSelectSSP_BucketAllocateTbl )
         {
            stmtSelectSSP_BucketAllocateTbl.setString(1, iLearnerID);
            stmtSelectSSP_BucketAllocateTbl.setString(2, iCourseID);
            stmtSelectSSP_BucketAllocateTbl.setString(3, iSCOID);
            stmtSelectSSP_BucketAllocateTbl.setInt(4, Persistence.SESSION);
            rsSSP_BucketAllocateTbl = 
               stmtSelectSSP_BucketAllocateTbl.executeQuery();
         }

         if ( _Debug )
         {
            System.out.println("call to SSP_BucketAllocateTbl is complete");
         }

         String bucketID = new String();
         String courseID = new String();
         String attemptID = new String();
         int persistence = 0;

         String bucket = new String();

         // Get all of the buckets allocated for the learner and remove the
         //  bucket files.
         while ( rsSSP_BucketAllocateTbl.next() )
         {
            bucketID = rsSSP_BucketAllocateTbl.getString("BucketID");
            persistence = rsSSP_BucketAllocateTbl.getInt("Persistence");
            attemptID = rsSSP_BucketAllocateTbl.getString("AttemptID");

            bucket = mSampleRTERoot + File.separator + iLearnerID +
                     File.separator + bucketID;

            bucket = bucket + iCourseID + iSCOID + attemptID;

            bucket = bucket + ".obj";

            if ( _Debug )
            {
               System.out.println("path  " + bucket);
            }

            File bucketFile = new File( bucket );
            bucketFile.delete();

         }


         // Now cleanup the SSP Allocate database to remove the course
         synchronized( stmtDeleteSSP_BucketAllocateTbl )
         {
            stmtDeleteSSP_BucketAllocateTbl.setString(1, iLearnerID);
            stmtDeleteSSP_BucketAllocateTbl.setString(2, iCourseID);
            stmtDeleteSSP_BucketAllocateTbl.setString(3, iSCOID);
            stmtDeleteSSP_BucketAllocateTbl.setInt(4, Persistence.SESSION);
            stmtDeleteSSP_BucketAllocateTbl.executeUpdate();
         }

         sqlDeleteSSP_BucketAllocateTbl = 
            "DELETE FROM SSP_BucketAllocateTbl" +
            " WHERE LearnerID = ? AND CourseID = ?" +
            " AND SCOID = ? AND" +
            " ReallocateFailure = ?";

         stmtDeleteSSP_BucketAllocateTbl = 
            conn.prepareStatement(sqlDeleteSSP_BucketAllocateTbl);

         synchronized( stmtDeleteSSP_BucketAllocateTbl )
         {
            stmtDeleteSSP_BucketAllocateTbl.setString(1, iLearnerID);
            stmtDeleteSSP_BucketAllocateTbl.setString(2, iCourseID);
            stmtDeleteSSP_BucketAllocateTbl.setString(3, iSCOID);
            stmtDeleteSSP_BucketAllocateTbl.setBoolean(4, true);
            stmtDeleteSSP_BucketAllocateTbl.executeUpdate();
         }


         stmtSelectSSP_BucketAllocateTbl.close();
         stmtDeleteSSP_BucketAllocateTbl.close();
         conn.close();

      }
      catch ( Exception e )
      {
         e.printStackTrace();
      }
   }


   /**
    * This method is used to deal with cleaning up buckets and the SSP
    * Database when a clear database is executed.
    *
    */
   public void deleteAllSSPData()
   {

      try
      {
         // Get some information from the database
         Connection conn = SSP_DBHandler.getConnection();
         PreparedStatement stmtSelectSSP_BucketAllocateTbl;
         PreparedStatement stmtDeleteSSP_BucketAllocateTbl;

         String sqlSelectSSP_BucketAllocateTbl = 
            "SELECT * FROM SSP_BucketAllocateTbl";

         String sqlDeleteSSP_BucketAllocateTbl = 
            "DELETE FROM SSP_BucketAllocateTbl";

         stmtSelectSSP_BucketAllocateTbl = 
            conn.prepareStatement(sqlSelectSSP_BucketAllocateTbl);

         stmtDeleteSSP_BucketAllocateTbl = 
            conn.prepareStatement(sqlDeleteSSP_BucketAllocateTbl);

         if ( _Debug )
         {
            System.out.println("about to call SSP_BucketAllocateTbl in " +
                               "deleteAllSSPData");
         }

         ResultSet rsSSP_BucketAllocateTbl = null;

         synchronized( stmtSelectSSP_BucketAllocateTbl )
         {
            rsSSP_BucketAllocateTbl = 
               stmtSelectSSP_BucketAllocateTbl.executeQuery();
         }

         if ( _Debug )
         {
            System.out.println("call to SSP_BucketAllocateTbl is complete");
         }

         String bucketID = new String();
         String courseID = new String();
         String learnerID = new String();
         String attemptID = new String();
         String scoID = new String();
         int persistence = 0;

         String bucket = new String();

         // Get all of the buckets allocated for the learner and remove the
         //  bucket files.
         while ( rsSSP_BucketAllocateTbl.next() )
         {
            bucketID = rsSSP_BucketAllocateTbl.getString("BucketID");
            courseID = rsSSP_BucketAllocateTbl.getString("CourseID");
            learnerID = rsSSP_BucketAllocateTbl.getString("LearnerID");
            persistence = rsSSP_BucketAllocateTbl.getInt("Persistence");
            attemptID = rsSSP_BucketAllocateTbl.getString("AttemptID");
            scoID = rsSSP_BucketAllocateTbl.getString("SCOID");

            bucket = mSampleRTERoot + File.separator + learnerID +
                     File.separator + bucketID;

            if ( persistence == Persistence.COURSE )
            {
               bucket = bucket + courseID;
            }
            else if ( persistence == Persistence.SESSION )
            {
               bucket = bucket + courseID + scoID + attemptID;
            }

            bucket += ".obj";

            if ( _Debug )
            {
               System.out.println("path  " + bucket);
            }

            File bucketFile = new File( bucket );
            bucketFile.delete();

         }


         // Now cleanup the SSP Allocate database to remove the course
         synchronized( stmtDeleteSSP_BucketAllocateTbl )
         {
            stmtDeleteSSP_BucketAllocateTbl.executeUpdate();
         }

         stmtSelectSSP_BucketAllocateTbl.close();
         stmtDeleteSSP_BucketAllocateTbl.close();
         conn.close();

      }
      catch ( Exception e )
      {
         e.printStackTrace();
      }
   }

}
