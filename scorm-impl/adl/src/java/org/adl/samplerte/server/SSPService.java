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
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.adl.api.ecmascript.APIErrorManager;
import org.ims.ssp.samplerte.server.bucket.Persistence;
import org.ims.ssp.samplerte.server.SSP_Operation;
import org.ims.ssp.samplerte.client.ServletProxy;
import org.ims.ssp.samplerte.server.SSP_ServletRequest;
import org.ims.ssp.samplerte.server.SSP_ServletResponse;
import org.ims.ssp.samplerte.util.SSP_DBHandler;


/**
* <strong>Filename:</strong> SSPService.java<br><br>
*
* <strong>Description:</strong><br>
* The SSPService class handles requests to retrieve and manipulate SSP data.
* 
* <strong>Design Issues:</strong><br>
* This implementation is intended to be used by the SCORM 2004 3rd EditionSample RTE<br>
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
public class SSPService 
{
   /**
    * This controls display of log messages to the java console.
    */
   private static boolean _Debug = false;
   
   /**
    * This string contains the name of the SampleRTEFiles directory.
    */
   private static String SRTEFILESDIR = "SCORM3rdSampleRTE10Files";
   
   /**
    * The int that needs passed into APIErrorManager to get the right set of
    * error codes, error messages, and error diagnostic information for SSP
    */
   private static final int SSPERRORMGR = 2;
   
   /**
    * This constructor creates a SSPService object.
    */
   SSPService() 
   {
      // Default constructor
   }
   
   /**
    * This method creates a bucket based on the information passed into it in
    * the iBucketProfile object.
    * 
    * @param iBucketProfile The bucketProfile object containing information on
    *        the bucket which is to be created.
    * 
    * @return String of "true" if the bucket was successfully added or "false" 
    *         if it was not successfully added.
    */
   public int addBucket(BucketProfile iBucketProfile)
   {
      int errorCode = 0;
      
      try
      {
         SSP_ServletRequest sspRequest = new SSP_ServletRequest();
         SSP_ServletResponse expectedResponse = new SSP_ServletResponse();
      
         sspRequest.mBucketID      = iBucketProfile.mBucketID;
         sspRequest.mStudentID     = iBucketProfile.mUserID;
         sspRequest.mCourseID      = iBucketProfile.mCourseID;
         sspRequest.mSCOID         = iBucketProfile.mSCOID;
         sspRequest.mAttemptID     = iBucketProfile.mAttemptID;
         sspRequest.mOperationType = SSP_Operation.ALLOCATE;
         sspRequest.mMinimumSize   = iBucketProfile.mMinimum;
         sspRequest.mRequestedSize = iBucketProfile.mRequested;
         sspRequest.mReducible     = iBucketProfile.mReducible;
         sspRequest.mBucketType    = iBucketProfile.mType;
         sspRequest.mPersistence   = iBucketProfile.mPersistence;
         sspRequest.mOffset        = null; 
         sspRequest.mSize          = null; 
         sspRequest.mValue         = null;
         sspRequest.mManagedBucketIndex = 0;
         
         URL servletLocation = new URL( "http://localhost:8080/adl/SSPServer" );
         ServletProxy proxy = new ServletProxy( servletLocation );
         expectedResponse = proxy.postLMSRequest( sspRequest );
         
         String errorString = "";
         String errorDesc = "";
         APIErrorManager erM = new APIErrorManager(SSPERRORMGR);

         errorCode = (expectedResponse.mStatusInfo.mErrorCode).intValue();

         if ( errorCode != 0 )
         {
            String bucket = File.separator + SRTEFILESDIR + 
               File.separator + iBucketProfile.mUserID + File.separator + 
               iBucketProfile.mBucketID;

            switch ( iBucketProfile.mPersistence )
            {
            case Persistence.LEARNER:
               break;
            case Persistence.COURSE:
               bucket += iBucketProfile.mCourseID;
               break;
            case Persistence.SESSION:
               bucket += iBucketProfile.mCourseID + iBucketProfile.mSCOID + 
                  iBucketProfile.mAttemptID;
               break;
            default:
               break;
            }
            // error in response
            errorString = "ERROR: " + expectedResponse.mStatusInfo.mErrorCode;
            errorDesc = "ERROR Description: " + erM.getErrorDiagnostic(
               Integer.toString(errorCode));
            if( _Debug )
            {
               System.out.println(errorString);
               System.out.println(errorDesc);
            }
            
            bucket += ".obj";

            File bucketFile = new File( bucket );
            bucketFile.delete();
         }
      }
      catch ( MalformedURLException murle )
      {
         murle.printStackTrace();
      }
      return errorCode;
   }
   
   /**
    * Provides functionality to allow a bucket to be deleted from the system. 
    * This not only removes the requested bucket from the database but it also
    * will remove the bucket's associated .obj file found in the 
    * SampleRTExxxFiles folder.
    * 
    * @param iActivityID
    *             Unique identifier of the bucket to be deleted. 
    * 
    * @return
    *       Success of deleting the bucket from the system. Returns a string
    *          value of either true or false.
    */
   public String deleteBucket(String iActivityID)
   {
      String result = "false";
      try
      {
         // Get some information from the database
         Connection conn = SSP_DBHandler.getConnection();
         PreparedStatement stmtSelectSSP_BucketAllocateTbl;

         String sqlSelectSSP_BucketAllocateTbl = 
         "SELECT * FROM SSP_BucketAllocateTbl" +
         " WHERE ActivityID = " + iActivityID;

         stmtSelectSSP_BucketAllocateTbl = 
         conn.prepareStatement(sqlSelectSSP_BucketAllocateTbl);

         ResultSet rsSSP_BucketAllocateTbl = 
         stmtSelectSSP_BucketAllocateTbl.executeQuery();

         rsSSP_BucketAllocateTbl.next();
         
         String scoID = rsSSP_BucketAllocateTbl.getString("SCOID");
         String bucketID = rsSSP_BucketAllocateTbl.getString("BucketID");
         String courseID = rsSSP_BucketAllocateTbl.getString("CourseID");
         String attemptID = rsSSP_BucketAllocateTbl.getString("AttemptID");
         String learnerID = rsSSP_BucketAllocateTbl.getString("LearnerID");
         int persistence = rsSSP_BucketAllocateTbl.getInt("Persistence");
         boolean reallFail = rsSSP_BucketAllocateTbl.
         getBoolean("ReallocateFailure");

         if ( reallFail )
         {
            String sqlDeleteSingleBucketEntry = 
            "DELETE FROM SSP_BucketAllocateTbl" + 
            " WHERE ActivityID = " + iActivityID;

            PreparedStatement stmtDelSingBuckEntry =
            conn.prepareStatement(sqlDeleteSingleBucketEntry);

            stmtDelSingBuckEntry.executeUpdate();
            stmtDelSingBuckEntry.close();
         }
         else
         {
            String bucket = File.separator + SRTEFILESDIR + 
               File.separator + learnerID + File.separator + bucketID;

            switch ( persistence )
            {
            case Persistence.LEARNER:
               break;
            case Persistence.COURSE:
               bucket += courseID;
               break;
            case Persistence.SESSION:
               bucket += courseID + scoID + attemptID;
               break;
            default:
               break;
            }

            bucket += ".obj";

            File bucketFile = new File( bucket );
            bucketFile.delete();

            String sqlDelAllOfThisID =
            "DELETE FROM SSP_BucketAllocateTbl WHERE BucketID = '" + 
            bucketID + "' AND Persistence = " + persistence;

            PreparedStatement stmtDelAllOfThisID =
            conn.prepareStatement(sqlDelAllOfThisID);

            stmtDelAllOfThisID.executeUpdate();
            stmtDelAllOfThisID.close();
         }

         stmtSelectSSP_BucketAllocateTbl.close();
         conn.close();
       
         result = "true";
      }
      catch ( SQLException sqlE )
      {
         sqlE.printStackTrace();
      }
      return result;
   }
   
   /**
    * Provides the ability to get a list of buckets associated with a user
    * 
    * @param iUserID
    *             The id of the user with which the buckets to be returned are
    *                associated
    * 
    * @return
    *       List of buckets for the user
    */
   public List getBuckets( String iUserID )
   {
      List bucketList = new ArrayList(0);
      Connection conn;
      PreparedStatement stmtSelectBucket;
      String sqlSelectBucket = "SELECT *" +
                               "FROM SSP_BucketAllocateTbl " +
                               "WHERE LearnerID = '" + iUserID + "'";
      try
      {
         conn = SSP_DBHandler.getConnection();
         stmtSelectBucket = conn.prepareStatement( sqlSelectBucket );

         ResultSet bucketRS = null;
         bucketRS = stmtSelectBucket.executeQuery();

         while ( bucketRS.next() )
         {
            BucketProfile bucketProfile = new BucketProfile();
            bucketProfile.mBucketID = bucketRS.getString( "BucketID" );
            bucketProfile.mMinimum = bucketRS.getString( "Min" );
            bucketProfile.mRequested = bucketRS.getString( "Requested" );
            bucketProfile.mCourseID = bucketRS.getString( "CourseID" );
            bucketProfile.mSCOID = bucketRS.getString( "SCOID" );
            bucketProfile.mActivityID = bucketRS.getString( "ActivityID" );
            bucketProfile.mPersistence = bucketRS.getInt( "Persistence" );
            bucketProfile.mAttemptID = bucketRS.getString("AttemptID");
            bucketProfile.mReallocationFailure = 
               bucketRS.getBoolean( "ReallocateFailure" );
            
            bucketList.add(bucketProfile);
         }
         bucketRS.close();
         stmtSelectBucket.close();
         conn.close();
      }
      catch(SQLException sqle)
      {
         sqle.printStackTrace();
      } 
      
      return bucketList;
   }
}