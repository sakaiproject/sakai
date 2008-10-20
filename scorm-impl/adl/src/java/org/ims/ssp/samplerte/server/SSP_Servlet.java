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

package org.ims.ssp.samplerte.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.lang.ClassNotFoundException;
import java.lang.NullPointerException;
import java.io.UnsupportedEncodingException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.adl.datamodels.ssp.SSP_DMErrorCodes;

import org.ims.ssp.samplerte.server.SSP_ServletRequest;
import org.ims.ssp.samplerte.server.SSP_ServletResponse;
import org.ims.ssp.samplerte.server.bucket.ManagedBucket;
import org.ims.ssp.samplerte.server.bucket.StatusInfo;
import org.ims.ssp.samplerte.server.bucket.BucketCollectionManagerInterface;
import org.ims.ssp.samplerte.server.bucket.BucketManagerInterface;
import org.ims.ssp.samplerte.server.bucket.BucketState;
import org.ims.ssp.samplerte.server.bucket.BucketAllocation;
import org.ims.ssp.samplerte.server.bucket.Bucket;
import org.ims.ssp.samplerte.server.bucket.SuccessStatus;
import org.ims.ssp.samplerte.server.bucket.Persistence;

import org.ims.ssp.samplerte.util.SSP_DBHandler;
import org.adl.util.debug.DebugIndicator;

/**
 * Handles the server side processing of client side SSP datamodel requests.
 * <br><br>
 *
 * <strong>Filename:</strong> SSP_Servlet.java<br><br>
 *
 * <strong>Description:</strong><br>
 * 
 * Handles the server side processing of client side SSP datamodel requests.
 * <br><br>
 *
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the SCORM 2004 3rd Edition
 * Sample RTE. <br>
 * <br>
 *
 * <strong>Implementation Issues:</strong><br><br>
 *
 * <strong>Known Problems:</strong><br><br>
 *
 * <strong>Side Effects:</strong><br><br>
 *
 * <strong>References:</strong><br>
 * <ul>
 *     <li>IMS SSP Specification
 *     <li>SCORM 2004 3rd Edition
 * </ul>
 *
 * @author ADL Technical Team
 */
public class SSP_Servlet extends HttpServlet
implements BucketCollectionManagerInterface, BucketManagerInterface
{
   private static boolean _Debug = DebugIndicator.ON;
   
   /**
    * String containing the name of the SampleRTEFiles directory.
    */
   private static String SRTEFILESDIR = "SCORM3rdSampleRTE10Files";

   /**
    * This method is responsible for getting the servlet request sent from the
    * client, in this case the SSP Datamodel code.
    * 
    * @param iRequest
    * @param oResponse
    *
    * @exception ServletException
    * @exception IOException
    */
   public void doPost(HttpServletRequest iRequest,
                      HttpServletResponse oResponse)
   throws ServletException, IOException
   {
      try
      {
         boolean result = false;

         String returnValue = "";

         BucketAllocation bucketAllocation = new BucketAllocation();

         int operationType = -1;

         ObjectInputStream in = 
         new ObjectInputStream( iRequest.getInputStream() );

         ObjectOutputStream out = 
         new ObjectOutputStream(oResponse.getOutputStream());

         SSP_ServletResponse sspResponse = new SSP_ServletResponse();

         StatusInfo statusInfo = new StatusInfo();

         // Read the SSP_ServletRequest object
         SSP_ServletRequest request = (SSP_ServletRequest)in.readObject();

         bucketAllocation.setBucketID(request.mBucketID);
         bucketAllocation.setStudentID(request.mStudentID);
         bucketAllocation.setCourseID(request.mCourseID);
         bucketAllocation.setAttemptID(request.mAttemptID);
         operationType = request.mOperationType;
         bucketAllocation.setMinimum(request.mMinimumSize);
         bucketAllocation.setRequested(request.mRequestedSize);
         bucketAllocation.setReducible(request.mReducible);
         bucketAllocation.setBucketType(request.mBucketType);
         bucketAllocation.setPersistence(request.mPersistence);
         bucketAllocation.setOffset(request.mOffset);
         bucketAllocation.setSize(request.mSize);
         bucketAllocation.setValue(request.mValue);
         bucketAllocation.setManagedBucketIndex(request.mManagedBucketIndex);
         bucketAllocation.setSCOID(request.mSCOID);

         if ( _Debug )
         {
            System.out.println("@@@@@  DO POST  @@@@@");
            System.out.println("@@   mBucketID = "           
                               + bucketAllocation.getBucketID());
            System.out.println("@@   mStudentID = "          
                               + bucketAllocation.getStudentID());
            System.out.println("@@   mCourseID = "           
                               + bucketAllocation.getCourseID());
            System.out.println("@@   mSCOID = "              
                               + bucketAllocation.getSCOID());
            System.out.println("@@   mAttemptID = "          
                               + bucketAllocation.getAttemptID());
            System.out.println("@@   mOperationType = "      + operationType);
            System.out.println("@@   mManagedBucketIndex = " 
                               + bucketAllocation.getManagedBucketIndex());
            System.out.println("@@@@@  DO POST  @@@@@");
         }

         // Verify that all parameters are of correct type
         result = validateParameters(bucketAllocation, operationType);

         if ( result )
         {
            if ( _Debug )
            {
               System.out.println("the parameters ARE valid");
               System.out.println("mOperationType = " + operationType);
            }

            switch ( operationType )
            {
               case SSP_Operation.ALLOCATE:
               {
                  if ( _Debug )
                  {
                     System.out.println("have an allocate request");
                  }
                  statusInfo = allocate( bucketAllocation );

                  break;
               }

               case SSP_Operation.APPEND_DATA:
               {
                  if ( _Debug )
                  {
                     System.out.println("have an append data request");
                  }

                  byte[] data = null;

                  try
                  {
                     data = bucketAllocation.getValue().getBytes( Bucket.CHARSET );
                  }
                  catch ( UnsupportedEncodingException uee )
                  {
                     data = bucketAllocation.getValue().getBytes();

                     System.out.println( "UnsupportedEncodingException: " + 
                                         Bucket.CHARSET + " is not a " +
                                         "supported encoding.  The default " +
                                         "encoding is being used" );
                  }

                  statusInfo = appendData( bucketAllocation.getBucketID(), 
                                           data, bucketAllocation );

                  break;
               }

               case SSP_Operation.GET_DATA:
               {
                  if ( _Debug )
                  {
                     System.out.println("have a get data request");
                  }

                  byte[] data = null;

                  if ( (bucketAllocation.getOffset() == null) &&
                       (bucketAllocation.getSize() == null) )
                  {
                     statusInfo = getData( bucketAllocation.getBucketID(), 
                                           data, bucketAllocation );
                  }
                  else
                  {
                     statusInfo = getData( bucketAllocation.getBucketID(),
                                           bucketAllocation.getOffsetInt(),
                                           bucketAllocation.getSizeInt(),
                                           data, bucketAllocation );
                  }

                  // we use the mValue attribute since the out value (data)
                  // will most likely be null all the time.
                  sspResponse.mReturnValue = bucketAllocation.getValue();

                  if ( _Debug )
                  {
                     System.out.println("DATA = [" + 
                                        sspResponse.mReturnValue + "]");
                  }

                  break;
               }

               case SSP_Operation.GET_STATE:
               {
                  if ( _Debug )
                  {
                     System.out.println("have a get state request");
                  }

                  BucketState state = new BucketState();

                  statusInfo = getState( bucketAllocation.getBucketID(), state, 
                                         bucketAllocation );

                  sspResponse.mBucketState = state;

                  break;
               }

               case SSP_Operation.SET_DATA:
               {
                  if ( _Debug )
                  {
                     System.out.println("have an set data request");
                  }

                  byte[] data = null;

                  try
                  {
                     data = bucketAllocation.getValue().getBytes( Bucket.CHARSET );
                  }
                  catch ( UnsupportedEncodingException uee )
                  {
                     data = bucketAllocation.getValue().getBytes();

                     System.out.println( "UnsupportedEncodingException: " + 
                                         Bucket.CHARSET + " is not a " +
                                         "supported encoding.  The default " +
                                         "encoding is being used" );
                  }

                  if ( bucketAllocation.getOffset() == null )
                  {
                     statusInfo = setData( bucketAllocation.getBucketID(), 
                                           data, bucketAllocation );
                  }
                  else
                  {
                     statusInfo = setData( bucketAllocation.getBucketID(),
                                           bucketAllocation.getOffsetInt(), 
                                           data, bucketAllocation );
                  }

                  break;
               }

               default:
               {
                  sspResponse.mReturnValue = null;
               }
            }
         }
         else
         {
            switch ( operationType )
            {
               case SSP_Operation.ALLOCATE:
               {
                  statusInfo.mErrorCode = 
                     new Integer(SSP_DMErrorCodes.INVALID_SET_PARMS);

                  bucketAllocation.setAllocationStatus(SuccessStatus.FAILURE);

                  break;
               }

               case SSP_Operation.APPEND_DATA:
               case SSP_Operation.SET_DATA:
               {
                  statusInfo.mErrorCode = 
                     new Integer(SSP_DMErrorCodes.INVALID_SET_PARMS);

                  break;
               }

               case SSP_Operation.GET_DATA:
               case SSP_Operation.GET_STATE:
               {
                  sspResponse.mReturnValue = "";
                  statusInfo.mErrorCode = 
                     new Integer(SSP_DMErrorCodes.INVALID_GET_PARMS);

                  break;
               }

               default:
               {
                  sspResponse.mReturnValue = null;
               }
            }
         }
         sspResponse.mBucketID = request.mBucketID;
         sspResponse.mStudentID = request.mStudentID;
         sspResponse.mCourseID = request.mCourseID;
         sspResponse.mSCOID = request.mSCOID;
         sspResponse.mAttemptID = request.mAttemptID;
         sspResponse.mManagedBucketInfo =
         new ManagedBucket( request.mBucketID );
         sspResponse.mManagedBucketInfo.setSuccessStatus(
                                       bucketAllocation.getAllocationStatus() );

         sspResponse.mStatusInfo = statusInfo;

         if ( _Debug )
         {
            System.out.println("about to write the response");
         }

         out.writeObject(sspResponse);

         // Close the input and output streams
         in.close();
         out.close();
      }
      catch ( ClassNotFoundException cnfe )
      {
         cnfe.printStackTrace();
      }
      catch ( IOException ioe )
      {
         ioe.printStackTrace();
      }
   }
                     
   /**
    * This method validates the parameters based on the SSP SCORM App Profile.
    * 
    * @param iBucketAllocation The object containing the parameters to be 
    * validated
    * @param iOperationType Enumerated value representing the operation
    * the SCO requested
    * @return boolean of whether the parameters of the request are valid 
    * for that SSP Datamodel element
    */
   private boolean validateParameters( BucketAllocation iBucketAllocation, 
                                       int iOperationType )
   {
      boolean returnValue = true;

      if ( _Debug )
      {
         System.out.println("SSP_Servlet::validateParameters");
         System.out.println("mMinimumSize = " + iBucketAllocation.getMinimum());
         System.out.println("mRequestedSize = " + iBucketAllocation.getRequested());
         System.out.println("mReducible = " + iBucketAllocation.getReducible());
         System.out.println("mOffset = " + iBucketAllocation.getOffset());
         System.out.println("mSize = " + iBucketAllocation.getSize());
         System.out.println("mPersistence = " + iBucketAllocation.getPersistence());
      }

      try
      {
         // Check minimum size
         if ( iBucketAllocation.getMinimum() != null )
         {
            if ( _Debug )
            {
               System.out.println("Testing minimum size:");
            }

            try
            {
               iBucketAllocation.setMinimumSizeInt(
                  Integer.parseInt( iBucketAllocation.getMinimum()));
            }
            catch ( NullPointerException npe )
            {
               returnValue = false;
            }

            if ( iBucketAllocation.getMinimumSizeInt() < 0 )
            {
               returnValue = false;
            }
            else
            {
               // make sure it is an even number
               int modVal = iBucketAllocation.getMinimumSizeInt() % 2;

               if ( modVal != 0 )
               {
                  returnValue = false;
               }
            }

            if ( _Debug )
            {
               System.out.println("returnValue = " + returnValue);
            }
         }

         // Check requested size
         if ( iBucketAllocation.getRequested() != null )
         {
            if ( _Debug )
            {
               System.out.println("Testing requested size:");
            }

            try
            {
               iBucketAllocation.setRequestedSizeInt( 
                  Integer.parseInt( iBucketAllocation.getRequested()));
            }
            catch ( NullPointerException npe )
            {
               returnValue = false;
            }

            if ( iBucketAllocation.getRequestedSizeInt() < 0 )
            {
               returnValue = false;
            }
            else
            {
               // make sure it is an even number
               int modVal = iBucketAllocation.getRequestedSizeInt() % 2;

               if ( modVal != 0 )
               {
                  returnValue = false;
               }
            }

            if ( _Debug )
            {
               System.out.println("returnValue = " + returnValue);
            }
         }

         // Check reducible flag
         if ( iBucketAllocation.getReducible() != null )
         {
            if ( _Debug )
            {
               System.out.println("Testing reducible value:");
            }

            if ( iBucketAllocation.getReducible().equals( "true" ) )
            {
               iBucketAllocation.setReducibleBoolean(true);
            }
            else if ( iBucketAllocation.getReducible().equals( "false" ) )
            {
               iBucketAllocation.setReducibleBoolean(false);
            }
            else
            {
               returnValue = false;
            }

            if ( _Debug )
            {
               System.out.println("returnValue = " + returnValue);
            }
         }

         // Check offset value
         if ( iBucketAllocation.getOffset() != null )
         {
            if ( _Debug )
            {
               System.out.println("Testing offset value:");
            }

            try
            {
               iBucketAllocation.setOffsetInt(
                  Integer.parseInt( iBucketAllocation.getOffset()));
            }
            catch ( NullPointerException npe )
            {
               returnValue = false;
            }

            if ( iBucketAllocation.getOffsetInt() < 0 )
            {
               returnValue = false;
            }
            else
            {
               // make sure it is an even number
               int modVal = iBucketAllocation.getOffsetInt() % 2;

               if ( modVal != 0 )
               {
                  returnValue = false;
               }
            }

            if ( _Debug )
            {
               System.out.println("returnValue = " + returnValue);
            }
         }
         else
         {
            iBucketAllocation.setOffsetInt(0);
         }

         // Check size value
         if ( iBucketAllocation.getSize() != null )
         {
            if ( _Debug )
            {
               System.out.println("Testing size value:");
            }

            try
            {
               iBucketAllocation.setSizeInt(
                  Integer.parseInt( iBucketAllocation.getSize()));
            }
            catch ( NullPointerException npe )
            {
               returnValue = false;
            }

            if ( iBucketAllocation.getSizeInt() < 0 )
            {
               returnValue = false;
            }
            else
            {
               // make sure it is an even number
               int modVal = iBucketAllocation.getSizeInt() % 2;

               if ( modVal != 0 )
               {
                  returnValue = false;
               }
            }

            if ( _Debug )
            {
               System.out.println("returnValue = " + returnValue);
            }
         }

         if ( iOperationType == SSP_Operation.ALLOCATE )
         {
            if ( _Debug )
            {
               System.out.println("Testing Persistence:");
            }
            // check that persistence is one of the enumerated values
            if ( (iBucketAllocation.getPersistence() > 2) ||
                 (iBucketAllocation.getPersistence() < 0) )
            {
               returnValue = false;
               if ( _Debug )
               {
                  System.out.println("Persistence was not an enumerated value");
                  System.out.println("returnValue = " + returnValue);
               }
            }
         }
         else if ( iBucketAllocation.getPersistence() != -1 )
         {
            returnValue = false;

            if ( _Debug )
            {
               System.out.println("Persistence was not set");
               System.out.println("returnValue = " + returnValue);
            }
         }
      }
      catch ( NumberFormatException nf )
      {
         // One of the integer values is not of correct type
         returnValue = false;
      }

      if ( _Debug )
      {
         System.out.println("returnValue = " + returnValue);
         System.out.println("Exiting SSP_Servlet::validateParameters");
      }

      return returnValue;
   }

   /**
    * This method looks for the requested bucket.
    * @param iBucketAllocation The BucketAllocation object containing the 
    * parameters of the request.
    *
    * @return The BucketAllocation object.
    */
   private BucketAllocation checkForBucket( BucketAllocation iBucketAllocation )
   {
      if ( _Debug )
      {
         System.out.println("Entering SSP_Servlet::checkForBucket");
      }
      BucketAllocation returnBucketAllocation = new BucketAllocation();

      BucketAllocation ba = new BucketAllocation();
      BucketAllocation baf = new BucketAllocation();

      try
      {
         if ( iBucketAllocation.getPersistence() == Persistence.LEARNER )
         {
            // Check for locked learner bucket
            baf = retrieveDBRecord(iBucketAllocation.getStudentID(),
                                   iBucketAllocation.getBucketID(),
                                   null, null, -1,
                                   Persistence.LEARNER, true);

            // Check for good learner bucket
            ba = retrieveDBRecord(iBucketAllocation.getStudentID(),
                                  iBucketAllocation.getBucketID(),
                                  null, null, -1,
                                  Persistence.LEARNER, false);
         }

         else if ( iBucketAllocation.getPersistence() == Persistence.COURSE )
         {
            // Check for locked course bucket
            baf = retrieveDBRecord(iBucketAllocation.getStudentID(),
                                   iBucketAllocation.getBucketID(),
                                   iBucketAllocation.getCourseID(),
                                   null, -1,
                                   Persistence.COURSE, true);

            // Check for good course bucket
            ba = retrieveDBRecord(iBucketAllocation.getStudentID(),
                                  iBucketAllocation.getBucketID(),
                                  iBucketAllocation.getCourseID(),
                                  null, -1,
                                  Persistence.COURSE, false);
         }

         else
         {
            // Check for locked session bucket
            baf = retrieveDBRecord(iBucketAllocation.getStudentID(),
                                   iBucketAllocation.getBucketID(),
                                   iBucketAllocation.getCourseID(),
                                   iBucketAllocation.getSCOID(), -1,
                                   Persistence.SESSION, true);

            // Check for good session bucket
            ba = retrieveDBRecord(iBucketAllocation.getStudentID(),
                                  iBucketAllocation.getBucketID(),
                                  iBucketAllocation.getCourseID(),
                                  iBucketAllocation.getSCOID(), -1,
                                  Persistence.SESSION, false);
         }

         if ( baf.getBucketID() != null )
         {
            // entry found - the bucket is locked
            returnBucketAllocation = baf;
            returnBucketAllocation.setReallocateFailure(true);
         }
         else if ( ba.getBucketID() != null )
         {
            // entry found - the bucket is not locked
            returnBucketAllocation = ba;
         }
      }
      catch ( Exception e )
      {
         e.printStackTrace();
      }

      if ( _Debug )
      {
         System.out.println("Exiting SSP_Servlet::checkForBucket");
      }

      return returnBucketAllocation;
   }


   /**
    *
    * Creates a bucket.
    *
    * @param iDescription - Requirements for the bucket to be created.
    *
    * @return - Status or result information about the outcome of this call.
    */
   public StatusInfo allocate( BucketAllocation iDescription )
   {
      boolean okToProceed = true;
      boolean okToUpdateDB = true;

      StatusInfo status = new StatusInfo();

      // Check that only the valid parameters have been set
      if ( iDescription.getBucketID() == null ||
           iDescription.getRequested() == null || 
           iDescription.getOffset() != null ||
           iDescription.getSize() != null || iDescription.getValue() != null )
      {
         // We have a bad call
         okToProceed = false;

         status.mErrorCode = new Integer(SSP_DMErrorCodes.TYPE_MISMATCH);

         iDescription.setAllocationStatus(SuccessStatus.FAILURE);
      }

      // Check that requested space is not less than minimum space
      if ( okToProceed )
      {
         if ( iDescription.getRequestedSizeInt() < 
              iDescription.getMinimumSizeInt() )
         {
            okToProceed = false;

            status.mErrorCode = 
               new Integer(SSP_DMErrorCodes.ALLOCATE_MIN_GREATER_MAX);

            iDescription.setAllocationStatus(SuccessStatus.FAILURE);
         }
      }

      // Check the database to see if the bucket is already allocated
      if ( okToProceed )
      {
         BucketAllocation returnBucketAllocation = new BucketAllocation();
         returnBucketAllocation = checkForBucket( iDescription );

         // There was no bucket found in the DB - OK to try to allocate one
         if ( returnBucketAllocation.getBucketID() == null )
         {
            status = createBucket( iDescription );
         }
         else // There was a bucket in the DB - see if allocate parms match
         {
            okToUpdateDB = false;

            if ( iDescription.equals(returnBucketAllocation) )
            {
               // Bucket already allocated matching the parms - all OK
               iDescription.setAllocationStatus(
                  returnBucketAllocation.getAllocationStatus());

               status.mErrorCode = new Integer(SSP_DMErrorCodes.NO_ERROR);

               // Update the managed bucket index.
               boolean updateStatus = 
                  updateDBRecord(iDescription.getActivityID(), 
                                 iDescription.getManagedBucketIndex());

               if ( updateStatus == false )
               {
                  status.mErrorCode = 
                     new Integer(SSP_DMErrorCodes.DATABASE_UPDATE_FAILURE);
               }
            }
            else // Problem - bucket improperly declared
            {
               okToProceed = false;

               iDescription.setAllocationStatus(SuccessStatus.FAILURE);

               iDescription.setReallocateFailure( true );

               if ( returnBucketAllocation.getReallocateFailure() == false )
               {
                  boolean insertStatus = track( iDescription );
               }

               status.mErrorCode = new Integer(SSP_DMErrorCodes.TYPE_MISMATCH);
            }
         }
      }

      if ( iDescription.getAllocationStatus() == SuccessStatus.FAILURE &&
           okToUpdateDB )
      {
         iDescription.setReallocateFailure( true );
         boolean insertStatus = track( iDescription );
      }
      // Return result
      return status;
   }

   /**
    *
    * Retrieves the success status of the specified bucket.
    *
    * <b>Note:</b> This should never be called here in the servlet.
    *
    * @param iBucketID - The identifier of the bucket.
    * @param oSuccessStatus - The information requested.
    *
    * @return - Status or result information about the outcome of this call.
    */
   public StatusInfo getAllocationSuccess( String iBucketID,
                                           Integer oSuccessStatus )
   {
      // This should never be called here in the servlet.
      return null;
   }

   /**
    *
    * Retrieves an array of bucketIDs.
    *
    * <b>Note:</b> This should never be called here in the servlet.
    *
    * @param oBucketIDs - The array ov bucketIDs requested.
    *
    * @return - Status or result information about the outcome of this call.
    */
   public StatusInfo getBucketIDs( String[] oBucketIDs )
   {
      // This should never be called here in the servlet.

      return null;
   }

   /**
    *
    * Updates the bucket to put a block of data in the bucket, starting at the
    * end of the current data.
    *
    * @param ibucketID - The identifier of the bucket.
    * @param iData - The data to be stored in this bucket.
    *
    * @return - Status or result information about the outcome of this call.
    */
   public StatusInfo appendData( String ibucketID, byte[] iData,
                                 BucketAllocation iBucketAllocation )
   {
      boolean operationSuccess = false;
      boolean persisted = false;

      StatusInfo status = new StatusInfo();

      // Check that only the valid parameters have been set
      if ( iBucketAllocation.getBucketID() != null ||
           iBucketAllocation.getRequested() == null ||
           iBucketAllocation.getOffset() == null ||
           iBucketAllocation.getSize() == null )
      {
         Bucket bucket = retrieveBucket(iBucketAllocation);

         if ( bucket != null )
         {
            String incomingData;

            try
            {
               incomingData = new String( iData, Bucket.CHARSET );
            }
            catch ( UnsupportedEncodingException uee )
            {
               incomingData = new String( iData );
            }

            byte[] rawData = bucket.getData();

            String newData;

            try
            {
               newData = new String( rawData, bucket.CHARSET );
            }
            catch ( UnsupportedEncodingException uee )
            {
               newData = new String( rawData );
            }

            // append the incoming data to the new data
            newData += incomingData;

            // make sure we did not exceed the total bucket size
            int totalSpace = bucket.getTotalSpace() / 2;

            int newTotal = newData.length();

            if ( newTotal > totalSpace )
            {
               status.mErrorCode = 
                  new Integer(SSP_DMErrorCodes.BUCKET_SIZE_EXCEEDED_SET);
            }
            else
            {
               // store the result of this operation in the bucket.
               try
               {
                  bucket.setData( newData.getBytes( Bucket.CHARSET ) );

                  operationSuccess = true;
               }
               catch ( UnsupportedEncodingException uee )
               {
                  bucket.setData( newData.getBytes() );
               }

               // persist the result of the operation
               persisted = persistBucket( bucket, iBucketAllocation );
            }

            if ( operationSuccess && persisted )
            {
               status.mErrorCode = new Integer(SSP_DMErrorCodes.NO_ERROR);
            }
         }
         else
         {
            if ( iBucketAllocation.getAllocationStatus() == SuccessStatus.FAILURE )
            {
               status.mErrorCode = 
                  new Integer(SSP_DMErrorCodes.BUCKET_IMPROPERLY_DECLARED_SET);
            }
            else
            {
               status.mErrorCode = 
                  new Integer(SSP_DMErrorCodes.BUCKET_NOT_ALLOCATED_SET);
            }
         }
      }
      else
      {
         status.mErrorCode = new Integer(SSP_DMErrorCodes.TYPE_MISMATCH);
      }

      return status;
   }

   /**
    *
    * Retrieves all data currently stored in the bucket
    *
    * @param iBucketID - The identifier of the bucket.
    * @param oData - The data stored in this bucket.
    *
    * @return - Status or result information about the outcome of this call.
    */
   public StatusInfo getData( String iBucketID, byte[] oData, BucketAllocation iBucketAllocation )
   {
      StatusInfo status = new StatusInfo();

      // Check that only the valid parameters have been set
      if ( iBucketAllocation.getBucketID() == null || 
           iBucketAllocation.getRequested() != null || 
           iBucketAllocation.getOffset() != null ||
           iBucketAllocation.getSize() != null || 
           iBucketAllocation.getValue() == null )
      {
         Bucket bucket = retrieveBucket(iBucketAllocation);

         if ( bucket != null )
         {
            oData = bucket.getData();

            if ( oData == null )
            {
               iBucketAllocation.setValue("");
            }
            else
            {
               try
               {
                  iBucketAllocation.setValue(new String( oData, Bucket.CHARSET ));
               }
               catch ( UnsupportedEncodingException uee )
               {
                  iBucketAllocation.setValue(new String( oData ));
               }
            }

            status.mErrorCode = new Integer(SSP_DMErrorCodes.NO_ERROR);
         }
         else
         {
            if ( iBucketAllocation.getAllocationStatus() == SuccessStatus.FAILURE )
            {
               status.mErrorCode = 
                  new Integer(SSP_DMErrorCodes.BUCKET_IMPROPERLY_DECLARED_GET);
            }
            else
            {
               status.mErrorCode = 
                  new Integer(SSP_DMErrorCodes.BUCKET_NOT_ALLOCATED_GET);
            }
         }
      }
      else
      {
         status.mErrorCode = new Integer(SSP_DMErrorCodes.TYPE_MISMATCH);
      }

      return status;
   }

   /**
    *
    * Retrieves the specified amount of data starting at the specified offset
    * position.
    *
    * @param iBucketID - The identifier of the bucket.
    * @param iOffset - The position in the bucket to start reading.
    * @param iSize - The amount of data requested.
    * @param oData - The specified data stored in this bucket starting at the
    *                specified offset position.
    *
    * @return - Status or result information about the outcome of this call.
    */
   public StatusInfo getData( String iBucketID, int iOffset, int iSize,
                              byte[] oData, BucketAllocation iBucketAllocation )
   {
      StatusInfo status = new StatusInfo();

      int offset = -1;
      int size = -1;

      // Check that only the valid parameters have been set
      if ( iBucketAllocation.getBucketID() == null ||
           iBucketAllocation.getRequested() != null ||
           iBucketAllocation.getOffset() == null ||
           iBucketAllocation.getSize() == null ||
           iBucketAllocation.getValue() == null )
      {
         if ( iOffset != -1 )
         {
            offset = iOffset / 2;
         }

         if ( iSize != -1 )
         {
            size = iSize / 2;
         }

         Bucket bucket = retrieveBucket(iBucketAllocation);

         if ( bucket != null )
         {
            byte[] allDataRaw;
            String allData;

            if ( offset > (bucket.getUsed()/2) )
            {
               status.mErrorCode = 
                  new Integer(SSP_DMErrorCodes.OFFSET_EXCEEDS_BUCKET_SIZE_GET);
            }
            else
            {
               allDataRaw = bucket.getData();

               try
               {
                  allData = new String( allDataRaw, Bucket.CHARSET );
               }
               catch ( UnsupportedEncodingException uee )
               {
                  allData = new String( allDataRaw );
               }

               String reqData;

               if ( iSize > 0 )
               {
                  int total = offset + size;
                  int used = bucket.getUsed() / 2;

                  if ( total > used )
                  {
                     reqData = "";
                     status.mErrorCode = 
                        new Integer(SSP_DMErrorCodes.REQUESTED_SIZE_EXCEEDED_AVAIL);
                  }
                  else
                  {
                     reqData = allData.substring( offset, total );

                     status.mErrorCode = new Integer(SSP_DMErrorCodes.NO_ERROR);
                  }
               }
               else if ( iSize == -1 )
               {
                  reqData = allData.substring( offset );

                  status.mErrorCode = new Integer(SSP_DMErrorCodes.NO_ERROR);
               }
               else
               {
                  reqData = "";

                  status.mErrorCode = new Integer(SSP_DMErrorCodes.NO_ERROR);
               }

               try
               {
                  oData = reqData.getBytes( Bucket.CHARSET );
               }
               catch ( UnsupportedEncodingException uee )
               {
                  oData = reqData.getBytes();

                  System.out.println( "UnsupportedEncodingException: " + 
                                      Bucket.CHARSET + " is not a " +
                                      "supported encoding.  The default " +
                                      "encoding is being used" );
               }

               try
               {
                  iBucketAllocation.setValue(new String( oData, Bucket.CHARSET ));
               }
               catch ( UnsupportedEncodingException uee )
               {
                  iBucketAllocation.setValue(new String( oData ));
               }
            }
         }
         else
         {
            if ( iBucketAllocation.getAllocationStatus() == SuccessStatus.FAILURE )
            {
               status.mErrorCode = 
                  new Integer(SSP_DMErrorCodes.BUCKET_IMPROPERLY_DECLARED_GET);
            }
            else
            {
               status.mErrorCode = 
                  new Integer(SSP_DMErrorCodes.BUCKET_NOT_ALLOCATED_GET);
            }
         }
      }
      else
      {
         status.mErrorCode = new Integer(SSP_DMErrorCodes.TYPE_MISMATCH);
      }

      if ( oData == null )
      {
         iBucketAllocation.setValue("");
      }
      else
      {
         try
         {
            iBucketAllocation.setValue(new String( oData, Bucket.CHARSET ));
         }
         catch ( UnsupportedEncodingException uee )
         {
            iBucketAllocation.setValue(new String( oData ));
         }
      }

      return status;
   }

   /**
    *
    * Retrieves the current state of the bucket.
    *
    * @param iBucketID - The identifier of the bucket.
    * @param oState - The current state of the bucket.
    *
    * @return - Status or result information about the outcome of this call.
    */
   public StatusInfo getState( String iBucketID, BucketState oState, 
                               BucketAllocation iBucketAllocation )
   {
      StatusInfo status = new StatusInfo();

      // Check that only the valid parameters have been set
      if ( (iBucketAllocation.getBucketID() != null) &&
           (iBucketAllocation.getRequested() == null) &&
           (iBucketAllocation.getOffset() == null) &&
           (iBucketAllocation.getSize() == null) &&
           (iBucketAllocation.getValue() == null) )
      {
         Bucket bucket = retrieveBucket(iBucketAllocation);

         if ( bucket != null )
         {
            oState.mBucketType = bucket.getBucketType();
            oState.mTotalSpace = new Integer( bucket.getTotalSpace() );
            oState.mUsed = new Integer( bucket.getUsed() );

            status.mErrorCode = new Integer(SSP_DMErrorCodes.NO_ERROR);
         }
         else
         {
            if ( iBucketAllocation.getAllocationStatus() == SuccessStatus.FAILURE )
            {
               status.mErrorCode = 
                  new Integer(SSP_DMErrorCodes.BUCKET_IMPROPERLY_DECLARED_GET);
            }
            else
            {
               status.mErrorCode = 
                  new Integer(SSP_DMErrorCodes.BUCKET_NOT_ALLOCATED_GET);
            }
         }
      }
      else
      {
         status.mErrorCode = new Integer(SSP_DMErrorCodes.TYPE_MISMATCH);
      }

      return status;
   }

   /**
    *
    * Updates the bucket to put a block of data in the bucket, replacing all
    * data currently stored in the bucket
    *
    * @param iBucketID - The identifier of the bucket.
    * @param iData - The data to be stored in this bucket.
    *
    * @return - Status or result information about the outcome of this call.
    */
   public StatusInfo setData( String iBucketID, byte[] iData, 
                              BucketAllocation iBucketAllocation )
   {
      boolean operationSuccess = false;
      boolean persisted = false;

      StatusInfo status = new StatusInfo();

      // Check that only the valid parameters have been set
      if ( iBucketAllocation.getBucketID() != null ||
           iBucketAllocation.getRequested() == null ||
           iBucketAllocation.getOffset() == null ||
           iBucketAllocation.getSize() == null )
      {

         Bucket bucket = retrieveBucket(iBucketAllocation);

         if ( bucket != null )
         {
            String incomingData;

            try
            {
               incomingData = new String( iData, Bucket.CHARSET );
            }
            catch ( UnsupportedEncodingException uee )
            {
               incomingData = new String( iData );
            }

            // make sure will not exceed the total bucket size
            int totalSpace = bucket.getTotalSpace() / 2;

            int newTotal = incomingData.length();

            if ( newTotal > totalSpace )
            {
               status.mErrorCode = 
                  new Integer(SSP_DMErrorCodes.BUCKET_SIZE_EXCEEDED_SET);
            }
            else
            {
               // store the result of this operation in the bucket.
               try
               {
                  bucket.setData( incomingData.getBytes( Bucket.CHARSET ) );

                  operationSuccess = true;
               }
               catch ( UnsupportedEncodingException uee )
               {
                  bucket.setData( incomingData.getBytes() );
               }

               // persist the result of the operation
               persisted = persistBucket( bucket, iBucketAllocation );
            }

            if ( operationSuccess && persisted )
            {
               status.mErrorCode = new Integer(SSP_DMErrorCodes.NO_ERROR);
            }
         }
         else
         {
            if ( iBucketAllocation.getAllocationStatus() == SuccessStatus.FAILURE )
            {
               status.mErrorCode = 
                  new Integer(SSP_DMErrorCodes.BUCKET_IMPROPERLY_DECLARED_SET);
            }
            else
            {
               status.mErrorCode = 
                  new Integer(SSP_DMErrorCodes.BUCKET_NOT_ALLOCATED_SET);
            }
         }
      }
      else
      {
         status.mErrorCode = new Integer(SSP_DMErrorCodes.TYPE_MISMATCH);
      }

      return status;
   }

   /**
    *
    * Updates the bucket to put a block of data in the bucket, replacing any
    * data currently stored in the bucket starting at the specified offset
    * position.
    *
    * @param iBucketID - The identifier of the bucket.
    * @param iOffset - The position in the bucket to start reading.
    * @param iData - The data to be stored in this bucket.
    *
    * @return - Status or result information about the outcome of this call.
    */
   public StatusInfo setData( String iBucketID, int iOffset, byte[] iData,
                              BucketAllocation iBucketAllocation )
   {
      boolean operationSuccess = false;
      boolean persisted = false;
      int offset = iOffset / 2;

      StatusInfo status = new StatusInfo();

      // Check that only the valid parameters have been set
      if ( iBucketAllocation.getBucketID() != null ||
           iBucketAllocation.getRequested() == null ||
           iBucketAllocation.getOffset() != null )
      {
         Bucket bucket = retrieveBucket(iBucketAllocation);

         if ( bucket != null )
         {
            if ( offset > (bucket.getTotalSpace()/2) )
            {
               status.mErrorCode = 
                  new Integer(SSP_DMErrorCodes.OFFSET_EXCEEDS_BUCKET_SIZE_SET);
            }
            else if ( offset > (bucket.getUsed()/2) )
            {
               status.mErrorCode = 
                  new Integer(SSP_DMErrorCodes.BUCKET_NOT_PACKED);
            }
            else
            {
               String incomingData;

               try
               {
                  incomingData = new String( iData, Bucket.CHARSET );
               }
               catch ( UnsupportedEncodingException uee )
               {
                  incomingData = new String( iData );
               }

               byte[] rawData = bucket.getData();

               String oldData;

               try
               {
                  oldData = new String( rawData, bucket.CHARSET );
               }
               catch ( UnsupportedEncodingException uee )
               {
                  oldData = new String( rawData );
               }

               // insert the incoming data to the new data
               String newData = oldData.substring( 0, offset );

               newData += incomingData;

               int oldUsed = bucket.getUsed() / 2;
               int newUsed = offset + incomingData.length();

               if ( newUsed < oldUsed )
               {
                  newData += oldData.substring( newUsed );
               }

               // make sure we did not exceed the total bucket size
               int totalSpace = bucket.getTotalSpace() / 2;

               int newTotal = newData.length();

               if ( newTotal > totalSpace )
               {
                  status.mErrorCode = 
                     new Integer(SSP_DMErrorCodes.BUCKET_SIZE_EXCEEDED_SET);
               }
               else
               {
                  // store the result of this operation in the bucket.
                  try
                  {
                     bucket.setData( newData.getBytes( Bucket.CHARSET ) );

                     operationSuccess = true;
                  }
                  catch ( UnsupportedEncodingException uee )
                  {
                     bucket.setData( newData.getBytes() );
                  }

                  // persist the result of the operation
                  persisted = persistBucket( bucket, iBucketAllocation );
               }

               if ( operationSuccess && persisted )
               {
                  status.mErrorCode = new Integer(SSP_DMErrorCodes.NO_ERROR);
               }
            }
         }
         else
         {
            if ( iBucketAllocation.getAllocationStatus() == SuccessStatus.FAILURE )
            {
               status.mErrorCode = 
                  new Integer(SSP_DMErrorCodes.BUCKET_IMPROPERLY_DECLARED_SET);
            }
            else
            {
               status.mErrorCode = 
                  new Integer(SSP_DMErrorCodes.BUCKET_NOT_ALLOCATED_SET);
            }
         }
      }
      else
      {
         status.mErrorCode = new Integer(SSP_DMErrorCodes.TYPE_MISMATCH);
      }

      return status;
   }

   /**
    * This method creates a bucket based on the parameters passed from the 
    * request
    * @param iDescription BucketAllocation object containing the parameters of
    * the servlet request
    *
    * @return Status information of the created bucket
    */
   private StatusInfo createBucket( BucketAllocation iDescription )
   {
      if ( _Debug )
      {
         System.out.println("Entering SSP_Servlet::createBucket");
      }

      boolean created = false;
      boolean persisted = false;
      boolean tracked = false;

      StatusInfo status = new StatusInfo();

      Integer errorCode = null;
      String errorDesc = "";

      Bucket bucket = null;

      int requested = iDescription.getRequestedSizeInt();
      boolean reducible = iDescription.getReducibleBoolean();
      int minimum = iDescription.getMinimumSizeInt();
      int totalSpace = -1;

      // check if the size is greater than the size limit of this implementation
      if ( requested <= BucketAllocation.MAXIMUM_SIZE )
      {
         totalSpace = requested;

         // instantiate the bucket
         bucket = new Bucket( iDescription.getBucketID(),
                              iDescription.getBucketType(),
                              minimum, iDescription.getPersistence(),
                              reducible, requested, totalSpace );

         iDescription.setAllocationStatus(SuccessStatus.REQUESTED);
         created = true;

         if ( _Debug )
         {
            System.out.println("Requested size was accepted");
         }
      }
      // check if the minimum size greater than the size limit of this
      // implementation
      else if ( reducible &&
                ( minimum <= BucketAllocation.MAXIMUM_SIZE ) )
      {
         totalSpace = minimum;

         // instantiate the bucket
         bucket = new Bucket( iDescription.getBucketID(),
                              iDescription.getBucketType(),
                              minimum, iDescription.getPersistence(),
                              reducible, requested, totalSpace );

         iDescription.setAllocationStatus(SuccessStatus.MINIMUM);
         created = true;

         if ( _Debug )
         {
            System.out.println("Minimum size was used");
         }
      }
      else
      {
         errorCode = new Integer(SSP_DMErrorCodes.NO_ERROR);

         if ( _Debug )
         {
            System.out.println("Bucket was not created - could not allocate " +
                               "requested space and could not reduce.");
         }
      }

      if ( created )
      {
         // serialize the bucket
         if ( _Debug )
         {
            System.out.println("about to call persist of bucket");
         }

         persisted = persistBucket( bucket, iDescription );

         if ( ! persisted )
         {
            errorCode = new Integer(SSP_DMErrorCodes.CREATE_BUCKET_PERSIST);
         }

         if ( _Debug)
         {
            System.out.println("just persisted the bucket");
         }
      }


      if ( created && persisted )
      {
         // make DB entry
         if ( _Debug )
         {
            System.out.println("about to track the bucket");
         }

         tracked = track( iDescription );

         if ( ! tracked )
         {
            errorCode = new Integer(SSP_DMErrorCodes.DATABASE_CREATE_FAILURE);
         }

      }

      if ( created && persisted && tracked )
      {
         errorCode = new Integer(SSP_DMErrorCodes.NO_ERROR);
      }
      else
      {
         iDescription.setAllocationStatus(SuccessStatus.FAILURE);
      }

      status.mErrorCode = errorCode;

      return status;
   }

   /**
    * This method stores the bucket information.
    * 
    * @param iBucket
    * @param iBucketAllocation
    * @return boolean value - true if successful
    */
   private boolean persistBucket( Bucket iBucket, 
                                  BucketAllocation iBucketAllocation )
   {
      boolean result = false;

      String bucketFile = new String();

      bucketFile = File.separator + SRTEFILESDIR + File.separator +
                   iBucketAllocation.getStudentID() + File.separator + 
                   iBucket.getBucketID();

      if ( iBucket.getPersistence() == Persistence.COURSE )
      {
         bucketFile += iBucketAllocation.getCourseID();
      }
      else if ( iBucket.getPersistence() == Persistence.SESSION )
      {
         bucketFile += iBucketAllocation.getCourseID() + 
            iBucketAllocation.getSCOID() + iBucketAllocation.getAttemptID();
      }

      bucketFile += ".obj";

      try
      {
         FileOutputStream fo = new FileOutputStream(bucketFile);
         ObjectOutputStream out_file = new ObjectOutputStream(fo);
         out_file.writeObject(iBucket);
         out_file.close();
         fo.close();
         result = true;
      }
      catch ( Exception e )
      {
         result = false;
         e.printStackTrace();
      }

      return result;
   }

   /**
    * This method stores the bucket information within a database for tracking.
    * @param iDescription
    *
    * @return boolean - true if successful
    */
   private boolean track( BucketAllocation iDescription )
   {
      if ( _Debug )
      {
         System.out.println("Entering SSP_Servlet::track");
      }

      boolean result = true;

      ResultSet rsSSP_BucketTbl = null;

      try
      {
         if ( _Debug )
         {
            System.out.println("Getting connection to database");
         }

         // Insert bucket information in the database
         SSP_DBHandler myDatabaseHandler = new SSP_DBHandler();
         Connection conn = myDatabaseHandler.getConnection();

         PreparedStatement stmtInsertBucket;

         if ( iDescription.getReallocateFailure() == false )
         {
            String sqlInsertBucket
               = "INSERT INTO SSP_BucketAllocateTbl (CourseID, LearnerID, " +
                 "BucketID, BucketType, Persistence, Min, Requested, " +
                 "Reducible, Status, AttemptID, ManagedBucketIndex, SCOID) " +
                 "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            stmtInsertBucket = conn.prepareStatement(sqlInsertBucket);

            synchronized( stmtInsertBucket )
            {
               stmtInsertBucket.setString(1, iDescription.getCourseID());
               stmtInsertBucket.setString(2, iDescription.getStudentID());
               stmtInsertBucket.setString(3, iDescription.getBucketID());
               stmtInsertBucket.setString(4, iDescription.getBucketType());
               stmtInsertBucket.setInt(5, iDescription.getPersistence());
               stmtInsertBucket.setInt(6, iDescription.getMinimumSizeInt());
               stmtInsertBucket.setInt(7, iDescription.getRequestedSizeInt());
               stmtInsertBucket.setBoolean(8, iDescription.getReducibleBoolean());
               stmtInsertBucket.setInt(9, iDescription.getAllocationStatus());
               stmtInsertBucket.setString(10, iDescription.getAttemptID());
               stmtInsertBucket.setInt(11, iDescription.getManagedBucketIndex());
               stmtInsertBucket.setString(12, iDescription.getSCOID());
               stmtInsertBucket.executeUpdate();
            }
         }
         else
         {
            String sqlInsertBucket
               = "INSERT INTO SSP_BucketAllocateTbl (CourseID, LearnerID, " +
                 "BucketID, BucketType, Persistence, Min, Requested, " +
                 "Reducible, Status, AttemptID, ManagedBucketIndex," +
                 "SCOID, ReallocateFailure) " +
                 "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            stmtInsertBucket = conn.prepareStatement(sqlInsertBucket);

            synchronized( stmtInsertBucket )
            {
               stmtInsertBucket.setString(1, iDescription.getCourseID());
               stmtInsertBucket.setString(2, iDescription.getStudentID());
               stmtInsertBucket.setString(3, iDescription.getBucketID());
               stmtInsertBucket.setString(4, iDescription.getBucketType());
               stmtInsertBucket.setInt(5, iDescription.getPersistence());
               stmtInsertBucket.setInt(6, iDescription.getMinimumSizeInt());
               stmtInsertBucket.setInt(7, iDescription.getRequestedSizeInt());
               stmtInsertBucket.setBoolean(8, iDescription.getReducibleBoolean());
               stmtInsertBucket.setInt(9, iDescription.getAllocationStatus());
               stmtInsertBucket.setString(10, iDescription.getAttemptID());
               stmtInsertBucket.setInt(11, iDescription.getManagedBucketIndex());
               stmtInsertBucket.setString(12, iDescription.getSCOID());
               stmtInsertBucket.setBoolean(13, true);
               stmtInsertBucket.executeUpdate();
            }
         }

         // Close the statements
         stmtInsertBucket.close();

         conn.close();

      }
      catch ( Exception e )
      {
         result = false;
         e.printStackTrace();
      }

      if ( _Debug )
      {
         System.out.println("Exiting SSP_Servlet::track -- result = " + result);
      }

      return result;
   }

   /**
    * This method retrieves the bucket information within the database.
    * @param iBucketAllocation
    *
    * @return bucket 
    */
   private Bucket retrieveBucket( BucketAllocation iBucketAllocation )
   {
      Bucket result = null;
      ResultSet rsSSP_BucketTbl = null;

      BucketAllocation ba = new BucketAllocation();
      boolean okToProceed = true;

      try
      {
         if ( iBucketAllocation.getManagedBucketIndex() >= 0 )
         {
            ba = retrieveDBRecord(iBucketAllocation.getStudentID(),
                                  iBucketAllocation.getBucketID(), null, null,
                                  iBucketAllocation.getManagedBucketIndex(), -1,
                                  false);

            if ( ba.getBucketID() != null )
            {
               if ( ba.getAllocationStatus() != SuccessStatus.FAILURE )
               {
                  switch ( ba.getPersistence() )
                  {
                     case Persistence.SESSION:
                     {
                        result = readBucket( iBucketAllocation.getBucketID(),
                                             iBucketAllocation.getStudentID(),
                                             iBucketAllocation.getCourseID(),
                                             iBucketAllocation.getAttemptID(),
                                             iBucketAllocation.getSCOID() );
                        break;
                     }
                     case Persistence.COURSE:
                     {
                        result = readBucket( iBucketAllocation.getBucketID(),
                                             iBucketAllocation.getStudentID(),
                                             iBucketAllocation.getCourseID(),
                                             null, null );
                        break;
                     }
                     case Persistence.LEARNER:
                     {
                        result = readBucket( iBucketAllocation.getBucketID(),
                                             iBucketAllocation.getStudentID(),
                                             null, null, null );
                        break;
                     }
                  }
               }
            }
         }

         else // not a managed bucket
         {
            // Check for locked session bucket
            ba = retrieveDBRecord(iBucketAllocation.getStudentID(),
                                  iBucketAllocation.getBucketID(),
                                  iBucketAllocation.getCourseID(),
                                  iBucketAllocation.getSCOID(), -1,
                                  Persistence.SESSION, true);
            if ( ba.getBucketID() != null )
            {
               // entry found - the bucket is locked
               okToProceed = false;
               iBucketAllocation.setAllocationStatus(SuccessStatus.FAILURE);
            }

            // Check for good session bucket
            if ( okToProceed )
            {
               ba = retrieveDBRecord(iBucketAllocation.getStudentID(),
                                     iBucketAllocation.getBucketID(),
                                     iBucketAllocation.getCourseID(),
                                     iBucketAllocation.getSCOID(), -1,
                                     Persistence.SESSION, false);
               if ( ba.getBucketID() != null )
               {
                  result = readBucket( iBucketAllocation.getBucketID(),
                                       iBucketAllocation.getStudentID(),
                                       iBucketAllocation.getCourseID(),
                                       iBucketAllocation.getAttemptID(),
                                       iBucketAllocation.getSCOID() );
                  okToProceed = false;
               }
            }

            // Check for locked course bucket
            if ( okToProceed )
            {
               ba = retrieveDBRecord(iBucketAllocation.getStudentID(),
                                     iBucketAllocation.getBucketID(),
                                     iBucketAllocation.getCourseID(),
                                     iBucketAllocation.getSCOID(), -1,
                                     Persistence.COURSE, true);
               if ( ba.getBucketID() != null )
               {
                  // entry found - the bucket is locked
                  okToProceed = false;
                  iBucketAllocation.setAllocationStatus(SuccessStatus.FAILURE);
               }
            }

            // Check for good course bucket
            if ( okToProceed )
            {
               ba = retrieveDBRecord(iBucketAllocation.getStudentID(),
                                     iBucketAllocation.getBucketID(),
                                     iBucketAllocation.getCourseID(), null, -1,
                                     Persistence.COURSE, false);
               if ( ba.getBucketID() != null )
               {
                  result = readBucket( iBucketAllocation.getBucketID(),
                                       iBucketAllocation.getStudentID(),
                                       iBucketAllocation.getCourseID(), null,
                                       null );
                  okToProceed = false;
               }
            }

            // Check for locked learner bucket
            if ( okToProceed )
            {
               ba = retrieveDBRecord(iBucketAllocation.getStudentID(),
                                     iBucketAllocation.getBucketID(), null,
                                     iBucketAllocation.getSCOID(), -1,
                                     Persistence.LEARNER, true);
               if ( ba.getBucketID() != null )
               {
                  // entry found - the bucket is locked
                  okToProceed = false;
                  iBucketAllocation.setAllocationStatus(SuccessStatus.FAILURE);
               }
            }

            // Check for good learner bucket
            if ( okToProceed )
            {
               ba = retrieveDBRecord(iBucketAllocation.getStudentID(),
                                     iBucketAllocation.getBucketID(), null,
                                     null, -1, Persistence.LEARNER, false);
               if ( ba.getBucketID() != null )
               {
                  result = readBucket( iBucketAllocation.getBucketID(),
                                       iBucketAllocation.getStudentID(), null,
                                       null, null );
                  okToProceed = false;
               }
            }
         }
      }
      catch ( Exception e )
      {
         e.printStackTrace();
      }

      return result;
   }


   /**
    * This method reads in the requested bucket object from the persisted file.
    * 
    * @param iBucketID
    * @param iStudentID
    * @param iCourseID
    * @param iAttemptID
    * @param iSCOID
    *
    * @return Bucket
    */
   private Bucket readBucket( String iBucketID, String iStudentID,
                              String iCourseID, String iAttemptID,
                              String iSCOID )
   {
      Bucket bucket = null;
      String bucketFile = File.separator + SRTEFILESDIR;

      if ( (iBucketID != null) &&
           (iStudentID != null) )
      {
         bucketFile += File.separator + iStudentID + File.separator + iBucketID;

         if ( iCourseID != null )
         {
            bucketFile += iCourseID;
         }

         if ( iSCOID != null )
         {
            bucketFile += iSCOID;
         }

         if ( iAttemptID != null )
         {
            bucketFile += iAttemptID;
         }

         bucketFile += ".obj";

         if ( _Debug )
         {
            System.out.println("***  reading file - " + bucketFile);
         }

         try
         {
            FileInputStream fis = new FileInputStream( bucketFile );
            ObjectInputStream ois = new ObjectInputStream( fis );
            bucket = (Bucket)ois.readObject();
            ois.close();
            fis.close();
         }
         catch ( Exception exception )
         {
            System.out.println( "caught exception while accessing the " +
                                "serialized file" );
            bucket = null;
         }
      }

      return bucket;
   }

   /**
    *
    * Updates DB information for a bucket.
    *
    * @param iActivityID
    * @param iManagedBucketIndex
    *
    * @return - Status or result information about the outcome of this call.
    */
   public boolean updateDBRecord( int iActivityID, int iManagedBucketIndex )
   {
      boolean result = true;

      try
      {
         // Update some information in the database
         SSP_DBHandler myDatabaseHandler = new SSP_DBHandler();
         Connection conn = myDatabaseHandler.getConnection();
         PreparedStatement stmtUpdateSSP_BucketAllocateTbl;

         String sqlUpdateBucketIndex = "UPDATE SSP_BucketAllocateTbl SET " +
                                       "ManagedBucketIndex = ?" +
                                       " WHERE ActivityID = ?";

         stmtUpdateSSP_BucketAllocateTbl =
         conn.prepareStatement(sqlUpdateBucketIndex);


         synchronized( stmtUpdateSSP_BucketAllocateTbl )
         {
            stmtUpdateSSP_BucketAllocateTbl.setInt(1, iManagedBucketIndex);
            stmtUpdateSSP_BucketAllocateTbl.setInt(2, iActivityID);
            stmtUpdateSSP_BucketAllocateTbl.executeUpdate();
         }

      }
      catch ( Exception e )
      {
         result = false;
         e.printStackTrace();
      }

      // Return result
      return result;
   }


   /**
    *
    * Retrieve record from DB.
    *
    * @param iLearnerID
    * @param iBucketID
    * @param iCourseID
    * @param iSCOID
    * @param iManagedBucketIndex
    * @param iPersistence
    * @param iReallocateFailure
    *
    * @return - Status or result information about the outcome of this call.
    */
   private BucketAllocation retrieveDBRecord(String iLearnerID, 
                                             String iBucketID,
                                             String iCourseID, String iSCOID,
                                             int iManagedBucketIndex,
                                             int iPersistence, 
                                             boolean iReallocateFailure)
   {
      BucketAllocation result = new BucketAllocation();
      ResultSet rsSSP_BucketTbl = null;

      try
      {
         SSP_DBHandler databaseHandler = new SSP_DBHandler();
         Connection conn = databaseHandler.getConnection();
         Statement stmtSelectSSP_BucketTbl =
            conn.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE,
                                  ResultSet.CONCUR_READ_ONLY );

         String sqlSelectSSP_BucketTbl = "";

         if ( iLearnerID != null )
         {
            sqlSelectSSP_BucketTbl = "LearnerID = '" + iLearnerID + "'";
         }

         if ( iBucketID != null )
         {
            if ( sqlSelectSSP_BucketTbl != "" )
            {
               sqlSelectSSP_BucketTbl += " AND ";
            }
            sqlSelectSSP_BucketTbl += "BucketID = '" + iBucketID + "'";
         }

         if ( iCourseID != null )
         {
            if ( sqlSelectSSP_BucketTbl != "" )
            {
               sqlSelectSSP_BucketTbl += " AND ";
            }
            sqlSelectSSP_BucketTbl += "CourseID = '" + iCourseID + "'";
         }

         if ( iSCOID != null )
         {
            if ( sqlSelectSSP_BucketTbl != "" )
            {
               sqlSelectSSP_BucketTbl += " AND ";
            }
            sqlSelectSSP_BucketTbl += "SCOID = '" + iSCOID + "'";
         }

         if ( iManagedBucketIndex >= 0 )
         {
            if ( sqlSelectSSP_BucketTbl != "" )
            {
               sqlSelectSSP_BucketTbl += " AND ";
            }
            sqlSelectSSP_BucketTbl += "ManagedBucketIndex = " + 
                                      iManagedBucketIndex;
         }

         if ( sqlSelectSSP_BucketTbl != "" )
         {
            sqlSelectSSP_BucketTbl += " AND ";
         }
         sqlSelectSSP_BucketTbl += "ReallocateFailure = " + iReallocateFailure;

         if ( iPersistence != -1 )
         {
            if ( sqlSelectSSP_BucketTbl != "" )
            {
               sqlSelectSSP_BucketTbl += " AND ";
            }
            sqlSelectSSP_BucketTbl += "Persistence = " + iPersistence;
         }

         sqlSelectSSP_BucketTbl = "SELECT * FROM SSP_BucketAllocateTbl WHERE " +
                                  sqlSelectSSP_BucketTbl;

         if ( _Debug )
         {
            System.out.println("SQL stmt in retieve record: " + 
                               sqlSelectSSP_BucketTbl);
         }

         synchronized( stmtSelectSSP_BucketTbl )
         {
            rsSSP_BucketTbl = 
               stmtSelectSSP_BucketTbl.executeQuery( sqlSelectSSP_BucketTbl );
         }

         // determine how many records came back
         int bucketCount = 0;
         while ( rsSSP_BucketTbl.next() )
         {
            bucketCount++;
         }

         if ( bucketCount == 1 )
         {
            rsSSP_BucketTbl.first();

            result.setAllocationStatus(rsSSP_BucketTbl.getInt("Status"));
            result.setBucketID(iBucketID);
            result.setBucketType(rsSSP_BucketTbl.getString("BucketType"));
            result.setMinimumSizeInt(rsSSP_BucketTbl.getInt("Min"));
            result.setPersistence(rsSSP_BucketTbl.getInt("Persistence"));
            result.setReducibleBoolean(rsSSP_BucketTbl.getBoolean("Reducible"));
            result.setRequestedSizeInt(rsSSP_BucketTbl.getInt("Requested"));
            result.setSCOID(iSCOID);
            result.setActivityID(rsSSP_BucketTbl.getInt("ActivityID"));
         }

         stmtSelectSSP_BucketTbl.close();
         conn.close();
      }
      catch ( Exception e )
      {
         e.printStackTrace();
      }

      return result;
   }
}
