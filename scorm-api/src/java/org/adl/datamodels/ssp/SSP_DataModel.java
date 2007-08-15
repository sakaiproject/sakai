/******************************************************************************
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
******************************************************************************/

package org.adl.datamodels.ssp;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import org.adl.datamodels.DMElement;
import org.adl.datamodels.DMErrorCodes;
import org.adl.datamodels.DMProcessingInfo;
import org.adl.datamodels.DMRequest;
import org.adl.datamodels.DataModel;
import org.adl.datamodels.RequestDelimiter;
import org.adl.datamodels.RequestToken;
import org.adl.util.debug.DebugIndicator;
//import org.ims.ssp.samplerte.client.ServletProxy;
//import org.ims.ssp.samplerte.server.SSP_Operation;
//import org.ims.ssp.samplerte.server.SSP_ServletRequest;
//import org.ims.ssp.samplerte.server.SSP_ServletResponse;
//import org.ims.ssp.samplerte.server.bucket.BucketState;
//import org.ims.ssp.samplerte.server.bucket.ManagedBucket;
//import org.ims.ssp.samplerte.server.bucket.StatusInfo;
//import org.ims.ssp.samplerte.server.bucket.SuccessStatus;
//import org.ims.ssp.samplerte.server.bucket.Persistence;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <br><br>
 *
 * <strong>Filename:</strong> SSP_DataModel.java<br><br>
 *
 * <strong>Description: </strong> This class implements the SCORM 2004 
 * Application Profile for SSP<br><br>
 *
 * @author ADL Technical Team
 */
public class SSP_DataModel extends AbstractSSPDataModel implements Serializable
{
	private static Log log = LogFactory.getLog(SSP_DataModel.class);

	/**
    * This controls display of log messages to the java console.
    */
   private static boolean _Debug = DebugIndicator.ON;

   /*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

    Constructors

    -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

   /**
    * Default constructor required for serialization support.
    */
   public SSP_DataModel()
   {
      mManaged = new Vector();

   }

   /*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

    Public Methods

    -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

   /**
    * Processes an equals() request against this data model. Compares two values
    * of the same data model element for equality.
    * 
    * @param iRequest The request (<code>DMRequest</code>) being processed.
    * 
    * @param iboolean The boolean representing a true or false value for something else. (Not used for this function -
    *                   we are merely overwriting the abstract method)
    * 
    * @return A data model error code indicating the result of this
    *         operation.
    */
   public int equals(DMRequest iRequest, boolean iboolean)
   {
      // Assume no processing errors
      int result = DMErrorCodes.NO_ERROR;

      return result;
   }

   /**
    * Describes this data model's binding string.
    *
    * @return This data model's binding string.
    */
   public String getDMBindingString()
   {
      return mBinding;
   }

   /**
    * Provides the request data model element.
    *
    * @param iElement Describes the requested element's dot-notation bound name.
    *
    * @return The <code>DMElement</code> corresponding to the requested element
    *         or <code>null</code> if the element does not exist in the data
    *         model.
    */
   public DMElement getDMElement(String iElement)
   {
      return null;
   }

   /**
    * Processes a GetValue() request against this data model.  Retrieves the 
    * value associated with the requested data model element in the current set 
    * of data model values.
    * 
    * @param iRequest The request (<code>DMRequest</code>) being processed.
    * 
    * @param oInfo   Provides the value returned by this request.
    * 
    * @return int    A data model error code indicating the result of this
    *                operation.
    */
   public int getValue(DMRequest iRequest, DMProcessingInfo oInfo)
   {
	   log.info("getValue(" + iRequest.toString() + ", " + oInfo.toString() + ")");
	      
      /*if( _Debug )
      {
         System.out.println("SSP_DataModel::getValue() -- entering");
      }

      // Assume no processing errors
      int result = DMErrorCodes.NO_ERROR;
      ServletProxy proxy = new ServletProxy(mURL);
      StatusInfo responseStatus = new StatusInfo();
      SSP_ServletRequest request = new SSP_ServletRequest();
      SSP_ServletResponse response = new SSP_ServletResponse();

      request.mStudentID = mLearnerID;
      request.mCourseID = mCourseID;
      request.mAttemptID = mAttemptNum;
      request.mSCOID = mSCOID;

      // Get the first specified element
      RequestToken tok = iRequest.getNextToken();

      // Check to see if there is a token
      if( tok != null )
      {
         // The call has an n reference. i.e. ssp.n.data
         // checking for an n value
         if( tok.getType() == RequestToken.TOKEN_INDEX )
         {
            // Get n index Value
            int idx = Integer.parseInt(tok.getValue());

            // check to see if index value is larger 
            // than the current number of buckets
            if( idx < mManaged.size() )
            {
               ManagedBucket tempBucket = (ManagedBucket)mManaged.get(idx);
               int tempBucketStatus = tempBucket.getSuccessStatus();
               String tempBucketID = tempBucket.getBucketID();
               tok = iRequest.getNextToken();
               String tempElem = tok.getValue();

               if( tempBucketStatus == SuccessStatus.FAILURE
                  && !( ( tempElem.equals("allocation_success") ) || ( tempElem.equals("id") ) ) )
               {
                  result = DMErrorCodes.GEN_GET_FAILURE;
               }
               else
               {
                  request.mBucketID = tempBucketID;
                  request.mManagedBucketIndex = idx;

                  if( tempElem.equals("id") )
                  {
                     oInfo.mValue = tempBucketID;
                  }
                  else if( tempElem.equals("allocation_success") )
                  {
                     int success = tempBucket.getSuccessStatus();

                     if( success == SuccessStatus.FAILURE )
                     {
                        oInfo.mValue = "failure";
                     }
                     else if( success == SuccessStatus.MINIMUM )
                     {
                        oInfo.mValue = "minimum";
                     }
                     else if( success == SuccessStatus.REQUESTED )
                     {
                        oInfo.mValue = "requested";
                     }
                     else
                     // Success Status returned doesn't match enumeration
                     {
                        result = DMErrorCodes.GEN_GET_FAILURE;
                     }
                  }
                  else if( tempElem.equals("bucket_state") )
                  {
                     request.mOperationType = SSP_Operation.GET_STATE;

                     if( _Debug )
                     {
                        printRequest(request);
                     }

                     response = proxy.postLMSRequest(request);

                     if( _Debug )
                     {
                        printResponse(response);
                     }

                     responseStatus = response.mStatusInfo;
                     result = ( responseStatus.mErrorCode ).intValue();

                     if( result == DMErrorCodes.NO_ERROR )
                     {
                        BucketState tempBucState = response.mBucketState;

                        if( tempBucState != null )
                        {
                           String totalSpace = tempBucState.mTotalSpace.toString();

                           String used = tempBucState.mUsed.toString();

                           // total space and used are required
                           if( totalSpace != null && used != null )
                           {
                              totalSpace = "{totalSpace=" + totalSpace + "}";
                              used = "{used=" + used + "}";
                              String type = tempBucState.mBucketType;

                              // type is optional
                              if( type == null || type.equals("") )
                              {
                                 oInfo.mValue = ( totalSpace + used );
                              }
                              else
                              {
                                 type = "{type=" + type + "}";
                                 oInfo.mValue = ( totalSpace + used + type );
                              }
                           }
                           else
                           // required delimiters weren't provided
                           {
                              result = DMErrorCodes.GEN_GET_FAILURE;
                           }
                        }
                        else
                        // BucketState was null
                        {
                           result = DMErrorCodes.GEN_GET_FAILURE;
                        }

                     }
                     else
                     // response from server returned an error code
                     {
                        result = DMErrorCodes.GEN_GET_FAILURE;
                     }
                  }
                  // GetValue on data
                  else if( tempElem.equals("data") )
                  {
                     tok = iRequest.getCurToken();
                     request.mOperationType = SSP_Operation.GET_DATA;
                     if( tok != null )
                     {
                        if( tok.getType() == RequestToken.TOKEN_ARGUMENT )
                        {
                           result = processDelimiters(iRequest, request);
                        }
                        else
                        {
                           result = DMErrorCodes.GEN_GET_FAILURE;
                        }
                     }

                     if( _Debug )
                     {
                        printRequest(request);
                     }

                     response = proxy.postLMSRequest(request);

                     if( _Debug )
                     {
                        printResponse(response);
                     }

                     responseStatus = response.mStatusInfo;
                     result = ( responseStatus.mErrorCode ).intValue();
                     if( result == DMErrorCodes.NO_ERROR )
                     {
                        oInfo.mValue = response.mReturnValue;
                     }
                  }
                  // Handle calls to get when the element is write only
                  else if( ( tempElem.equals("allocate") ) || ( tempElem.equals("appendData") ) )
                  {
                     result = DMErrorCodes.WRITE_ONLY;
                  }
                  else
                  {
                     result = DMErrorCodes.UNDEFINED_ELEMENT;
                  }
               }
            }
            else
            // index was larger than size of collection
            {
               result = DMErrorCodes.GEN_GET_FAILURE;
            }
         }

         // no index value
         // checking to see if token is an element
         else if( tok.getType() == RequestToken.TOKEN_ELEMENT )
         {
            String tempElem = tok.getValue();

            // GetValue on _count
            if( tempElem.equals("_count") )
            {
               if( iRequest.hasMoreTokens() )
               {
                  result = DMErrorCodes.TYPE_MISMATCH;
               }
               else
               {
                  oInfo.mValue = Integer.toString(mManaged.size());
               }
            }
            // GetValue on bucket_state
            else if( tempElem.equals("bucket_state") )
            {
               request.mOperationType = SSP_Operation.GET_STATE;

               // Processing to send off to servlet
               result = processDelimiters(iRequest, request);

               if( result == DMErrorCodes.NO_ERROR )
               {
                  if( _Debug )
                  {
                     printRequest(request);
                  }

                  response = proxy.postLMSRequest(request);
                  responseStatus = response.mStatusInfo;
                  result = ( responseStatus.mErrorCode ).intValue();
               }

               if( result == DMErrorCodes.NO_ERROR )
               {
                  BucketState tempBucState = response.mBucketState;

                  if( tempBucState != null )
                  {
                     String totalSpace = tempBucState.mTotalSpace.toString();

                     String used = tempBucState.mUsed.toString();

                     // total space and used are required
                     if( totalSpace != null && used != null )
                     {
                        totalSpace = "{totalSpace=" + totalSpace + "}";
                        used = "{used=" + used + "}";
                        String type = tempBucState.mBucketType;

                        // type is optional
                        if( type == null || type.equals("") )
                        {
                           oInfo.mValue = ( totalSpace + used );
                        }
                        else
                        {
                           type = "{type=" + type + "}";
                           oInfo.mValue = ( totalSpace + used + type );
                        }
                     }
                     else
                     // required delimiters weren't provided
                     {
                        result = DMErrorCodes.GEN_GET_FAILURE;
                     }
                  }
                  else
                  // BucketState was null
                  {
                     result = DMErrorCodes.GEN_GET_FAILURE;
                  }
               }
               else
               // response from server returned an error code
               {
                  result = DMErrorCodes.GEN_GET_FAILURE;
               }
            }
            // GetValue on data
            else if( tempElem.equals("data") )
            {
               request.mOperationType = SSP_Operation.GET_DATA;

               // Processing to send off to servlet
               result = processDelimiters(iRequest, request);

               if( result == DMErrorCodes.NO_ERROR )
               {
                  if( _Debug )
                  {
                     printRequest(request);
                  }

                  response = proxy.postLMSRequest(request);

                  if( _Debug )
                  {
                     printResponse(response);
                  }

                  responseStatus = response.mStatusInfo;
                  result = ( responseStatus.mErrorCode ).intValue();

                  if( result == DMErrorCodes.NO_ERROR )
                  {
                     oInfo.mValue = response.mReturnValue;
                  }
               }
               else
               {
                  if( tok.getType() == RequestToken.TOKEN_VALUE )
                  {
                     result = DMErrorCodes.WRITE_ONLY;
                  }
                  else
                  {
                     result = DMErrorCodes.GEN_GET_FAILURE;
                  }

               }
            }
            // Handle calls to get when the element is write only
            else if( ( tempElem.equals("allocate") ) || ( tempElem.equals("appendData") ) )
            {
               result = DMErrorCodes.WRITE_ONLY;
            }
            else
            {
               result = DMErrorCodes.UNDEFINED_ELEMENT;
            }
         }

         // invalid call
         // not an index or element, so error
         else
         {
            result = DMErrorCodes.UNDEFINED_ELEMENT;
         }
      }

      // token was null (ex. ssp. )
      else
      {
         result = DMErrorCodes.UNDEFINED_ELEMENT;
      }

      // return empty string if there is an error
      if( result != DMErrorCodes.NO_ERROR )
      {
         oInfo.mValue = "";
      }

      if( _Debug )
      {
         System.out.println("SSP_DataModel::getValue() -- returning: " + result);
         System.out.println("SSP_DataModel::getValue() -- exiting");
      }

      return result;*/
	   
	  return 0;

   } // end getvalue()

   /**
    * Performs data model specific initialization.
    *
    * @return A data model error code indicating the result of this
    *         operation.
    */
   public int initialize()
   {
      return DMErrorCodes.NO_ERROR;
   }

   /**
    * Processes a SetValue() request against this data model.  This includes a 
    * check for the validity of the value for the specified data model element.
    * 
    * @param iRequest The request (<code>DMRequest</code>) being processed.
    * 
    * @return A data model error code indicating the result of this
    *         operation.
    */
   public int setValue(DMRequest iRequest)
   {
	   
	   log.info("setValue(" + iRequest.toString() + ")");
	   
	   
	   
      /*if( _Debug )
      {
         System.out.println("SSP_DataModel::setValue -- entering");
      }

      // Assume no processing errors
      int result = DMErrorCodes.NO_ERROR;
      SSP_ServletRequest request = new SSP_ServletRequest();
      SSP_ServletResponse response = new SSP_ServletResponse();

      request.mStudentID = mLearnerID;
      request.mCourseID = mCourseID;
      request.mAttemptID = mAttemptNum;
      request.mSCOID = mSCOID;

      StatusInfo responseStatus = new StatusInfo();

      // Get the first specified element
      RequestToken tok = iRequest.getNextToken();

      // Check to see if the next token is a managed bucket
      if( tok != null )
      {
         // The call has an n reference. i.e. ssp.n.data
         // Checking for an n value
         if( tok.getType() == RequestToken.TOKEN_INDEX )
         {
            // Get n index Value
            int idx = Integer.parseInt(tok.getValue());

            // We have an n value now check to see if the n value is larger 
            // than the current number of Buckets
            if( idx < mManaged.size() )
            {
               ManagedBucket tempBucket = (ManagedBucket)mManaged.get(idx);

               int tempBucketStatus = tempBucket.getSuccessStatus();
               String tempBucketID = tempBucket.getBucketID();

               if( tempBucketStatus == SuccessStatus.FAILURE )
               {
                  result = DMErrorCodes.GEN_SET_FAILURE;
               }
               else
               {
                  request.mManagedBucketIndex = idx;
                  request.mBucketID = tempBucketID;

                  tok = iRequest.getNextToken();
                  String tempElem = tok.getValue();

                  // SetValue on data
                  if( tempElem.equals("data") )
                  {
                     request.mOperationType = SSP_Operation.SET_DATA;

                     tok = iRequest.getCurToken();
                     if( tok.getType() == RequestToken.TOKEN_VALUE )
                     {
                        request.mValue = tok.getValue();

                        if( tok.getDelimiterCount() > 0 )
                        {
                           result = processSetDelimiters(iRequest, request);
                        }

                        if( result == DMErrorCodes.NO_ERROR )
                        {
                           ServletProxy proxy = new ServletProxy(mURL);
                           if( _Debug )
                           {
                              printRequest(request);
                           }
                           response = proxy.postLMSRequest(request);
                           if( _Debug )
                           {
                              printResponse(response);
                           }

                           responseStatus = response.mStatusInfo;

                           result = ( responseStatus.mErrorCode ).intValue();
                        }
                     }
                     else
                     {
                        if( tok.getType() == RequestToken.TOKEN_ARGUMENT )
                        {
                           result = DMErrorCodes.READ_ONLY;//404
                        }
                        else
                        {
                           result = DMErrorCodes.UNDEFINED_ELEMENT;//401
                        }

                     }
                  }
                  // SetValue on append_data
                  else if( tempElem.equals("appendData") )
                  {
                     request.mOperationType = SSP_Operation.APPEND_DATA;

                     tok = iRequest.getCurToken();

                     if( tok.getType() == RequestToken.TOKEN_VALUE )
                     {
                        request.mValue = tok.getValue();

                        if( tok.getDelimiterCount() > 0 )
                        {
                           result = processSetDelimiters(iRequest, request);
                        }
                     }
                     else
                     // the next token should have been the value
                     {
                        result = DMErrorCodes.GEN_SET_FAILURE;
                     }

                     if( result == DMErrorCodes.NO_ERROR )
                     {
                        ServletProxy proxy = new ServletProxy(mURL);
                        if( _Debug )
                        {
                           printRequest(request);
                        }
                        response = proxy.postLMSRequest(request);
                        if( _Debug )
                        {
                           printResponse(response);
                        }
                        responseStatus = response.mStatusInfo;
                        result = ( responseStatus.mErrorCode ).intValue();
                     }
                  }
                  // Handle calls to set when the element is read only
                  else if( ( tempElem.equals("_count") ) || ( tempElem.equals("id") )
                     || ( tempElem.equals("allocation_success") ) || ( tempElem.equals("bucket_state") ) )
                  {
                     result = DMErrorCodes.READ_ONLY;
                  }
                  else
                  {
                     result = DMErrorCodes.UNDEFINED_ELEMENT;
                  }
               }
            } // idx greater than managed buckets
            else
            {
               result = DMErrorCodes.GEN_SET_FAILURE;
            }
         }
         // no n value 
         else if( tok.getType() == RequestToken.TOKEN_ELEMENT )
         {
            // Then look to see if special case
            // ***SPECIAL CASE *** - Internal Initialization
            if( ( tok.getValue().equals("init") ) && ( iRequest.isAdminRequest() ) )
            {
               tok = iRequest.getNextToken();
               String temp = tok.getValue();

               if( temp != null )
               {
                  if( temp.equals("userid") )
                  {
                     tok = iRequest.getNextToken();
                     mLearnerID = tok.getValue();
                  }
                  else if( temp.equals("courseid") )
                  {
                     tok = iRequest.getNextToken();
                     mCourseID = tok.getValue();
                  }
                  else if( temp.equals("attemptnum") )
                  {
                     tok = iRequest.getNextToken();
                     mAttemptNum = tok.getValue();
                  }
                  else if( temp.equals("scoID") )
                  {
                     tok = iRequest.getNextToken();
                     mSCOID = tok.getValue();
                  }
                  else if( temp.equals("url") )
                  {
                     tok = iRequest.getNextToken();

                     try
                     {
                        mURL = new URL(tok.getValue());
                     }
                     catch( MalformedURLException mfe )
                     {
                        result = DMErrorCodes.GEN_SET_FAILURE;
                     }
                  }
                  else
                  {
                     result = DMErrorCodes.NOT_IMPLEMENTED;
                  }
               }
               else
               {
                  result = DMErrorCodes.UNDEFINED_ELEMENT;
               }
            }
            else
            {
               // SetValue on allocate
               if( tok.getValue().equals("allocate") )
               {
                  request.mOperationType = SSP_Operation.ALLOCATE;
                  request.mPersistence = Persistence.LEARNER;
                  result = processDelimiters(iRequest, request);

                  // checking for min > requested size
                  if( request.mMinimumSize != null && request.mRequestedSize != null )
                  {
                     int tempMin = Integer.parseInt(request.mMinimumSize);
                     int tempReq = Integer.parseInt(request.mRequestedSize);

                     if( tempMin > tempReq )
                     {
                        result = DMErrorCodes.TYPE_MISMATCH;
                     }
                  }

                  if( result == DMErrorCodes.NO_ERROR )
                  {
                     boolean inFailedState = false;
                     String tempID = request.mBucketID;
                     int tempIdx = -1;

                     if( mManaged.size() > 0 )
                     {
                        // Check to see if bucket info is already listed
                        for( int i = 0; i < mManaged.size(); i++ )
                        {
                           ManagedBucket vecMB = (ManagedBucket)mManaged.get(i);
                           String vecbuckID = vecMB.getBucketID();

                           if( tempID.equals(vecbuckID) )
                           {
                              // Bucket already in vector
                              tempIdx = i;
                              int vecSuccess = vecMB.getSuccessStatus();
                              inFailedState = ( vecSuccess == SuccessStatus.FAILURE );
                              break;
                           }
                        } // end loop
                     }
                     if( !inFailedState )
                     {
                        request.mManagedBucketIndex = mManaged.size();
                        ServletProxy proxy = new ServletProxy(mURL);

                        if( _Debug )
                        {
                           printRequest(request);
                        }

                        response = proxy.postLMSRequest(request);

                        if( _Debug )
                        {
                           printResponse(response);
                        }

                        responseStatus = response.mStatusInfo;
                        result = responseStatus.mErrorCode.intValue();

                        if( tempIdx > -1 )
                        {
                           ManagedBucket vecMB = (ManagedBucket)mManaged.get(tempIdx);
                           ManagedBucket rMB = response.mManagedBucketInfo;
                           vecMB.setSuccessStatus(rMB.getSuccessStatus());
                        }
                        else
                        {
                           mManaged.add(response.mManagedBucketInfo);
                        }
                     } // bucket in failed state... just return gen set failure
                     else
                     {
                        result = DMErrorCodes.GEN_SET_FAILURE;
                     }
                  }// result from process delimiters had an error
               }
               // SetValue on data
               else if( tok.getValue().equals("data") )
               {
                  request.mOperationType = SSP_Operation.SET_DATA;

                  tok = iRequest.getCurToken();
                  if( tok.getType() == RequestToken.TOKEN_VALUE )
                  {
                     request.mValue = tok.getValue();

                     if( tok.getDelimiterCount() > 0 )
                     {
                        result = processSetDelimiters(iRequest, request);
                     }

                     if( result == DMErrorCodes.NO_ERROR )
                     {
                        ServletProxy proxy = new ServletProxy(mURL);

                        if( _Debug )
                        {
                           printRequest(request);
                        }
                        response = proxy.postLMSRequest(request);

                        if( _Debug )
                        {
                           printResponse(response);
                        }

                        responseStatus = response.mStatusInfo;

                        result = ( responseStatus.mErrorCode ).intValue();
                     }
                  }
                  else
                  {
                     if( tok.getType() == RequestToken.TOKEN_ARGUMENT )
                     {
                        result = DMErrorCodes.READ_ONLY;//404
                     }
                     else
                     {
                        result = DMErrorCodes.UNDEFINED_ELEMENT;//401
                     }

                  }
               }
               // SetValue on append_data
               else if( tok.getValue().equals("appendData") )
               {
                  request.mOperationType = SSP_Operation.APPEND_DATA;

                  tok = iRequest.getCurToken();

                  if( tok.getType() == RequestToken.TOKEN_VALUE )
                  {
                     request.mValue = tok.getValue();

                     if( tok.getDelimiterCount() > 0 )
                     {
                        result = processSetDelimiters(iRequest, request);
                     }
                  }
                  else
                  // the next token should have been the value
                  {
                     result = DMErrorCodes.GEN_SET_FAILURE;
                  }

                  if( result == DMErrorCodes.NO_ERROR )
                  {
                     ServletProxy proxy = new ServletProxy(mURL);

                     if( _Debug )
                     {
                        printRequest(request);
                     }

                     response = proxy.postLMSRequest(request);

                     if( _Debug )
                     {
                        printResponse(response);
                     }

                     responseStatus = response.mStatusInfo;
                     result = ( responseStatus.mErrorCode ).intValue();
                  }
               }
               // Handle calls to set when the element is read only
               else if( ( tok.getValue().equals("_count") ) || ( tok.getValue().equals("id") )
                  || ( tok.getValue().equals("allocation_success") ) || ( tok.getValue().equals("bucket_state") ) )
               {
                  result = DMErrorCodes.READ_ONLY;
               }
               else
               {
                  result = DMErrorCodes.UNDEFINED_ELEMENT;
               }
            }
         }
         // CASE C : invalid call
         else
         {
            result = DMErrorCodes.UNDEFINED_ELEMENT;
         }
      }
      else
      {
         result = DMErrorCodes.UNDEFINED_ELEMENT;
      }

      if( _Debug )
      {
         System.out.println("SSP_DataModel::setValue() -- result: " + result);
         System.out.println("SSP_DataModel::setValue() -- exiting");
      }

      return result;*/
	   
	   return 0;
   } // end setvalue()

   /**
    * Displays the contents of the entire data model.
    * NOTE:  This function has not been implemented.
    */
   public void showAllElements()
   {
      // At this time, there is no defined implementation for this method.
   }

   /**
    * Performs data model specific termination.  Called when the API processes
    * a Terminate request from a SCO.
    *
    * @return A data model error code indicating the result of this
    *         operation.
    */
   public int terminate()
   {
      return DMErrorCodes.NO_ERROR;
   }

   /**
    * Processes a validate() request against this data model.  Checks that
    * the value of the request is valid for the specified data model element.
    *
    * @param iRequest The request (<code>DMRequest</code>) being processed. 
    *                   (not used, needed to overwrite abstract method)
    *
    * @return A data model error code indicating the result of this
    *         operation.
    */
   public int validate(DMRequest iRequest)
   {
      // Assume no processing errors
      return DMErrorCodes.NO_ERROR;
   }

   /*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

    Private Methods

    -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

   // ProcessDelimiters loops through all delimiters for the certain element
   // to pass onto the Servlet_Request.
   /**
    * ProcessDelimiters loops through all delimiters for the certain element
    * to pass onto the Servlet_Request.
    * 
    * @param iDMRequest The DMRequest which contains the datamodel request (ex. {bucketId=foo})
    * 
    * @param iSSPServletRequest The operation request. (ex. ssp.allocate)
    * 
    * @return The DMErrorCode for this operation.
    * 
    * @see org.adl.datamodels.DMErrorCodes
    * @see org.adl.datamodels.DMRequest
    * @see org.ims.ssp.samplerte.server.SSP_ServletRequest
    */
   /*private int processDelimiters(DMRequest iDMRequest, SSP_ServletRequest iSSPServletRequest)
   {
      if( _Debug )
      {
         System.out.println("SSP_DataModel::processDelimiters() -- entering");
      }

      int error = DMErrorCodes.NO_ERROR;
      RequestToken tok = iDMRequest.getCurToken();

      // Retrieve the element type 
      int elementType = iSSPServletRequest.mOperationType;

      if( tok == null )
      {
         if( ( elementType == SSP_Operation.GET_DATA ) || ( elementType == SSP_Operation.GET_ALLOCATION_SUCCESS ) )
         {
            error = DMErrorCodes.GEN_GET_FAILURE;
         }
         else
         {
            error = DMErrorCodes.GEN_SET_FAILURE;
         }
      }
      else
      {
         // Loop through and find delimiters
         for( int i = 0; i < tok.getDelimiterCount(); i++ )
         {
            RequestDelimiter del = tok.getDelimiterAt(i);
            String delName = del.getName();

            if( _Debug )
            {
               System.out.println("SSP_DataModel::processDelimiters() -- " + "delimiter name = " + delName);
            }

            // Make sure there are no duplicates
            if( delName.equals("bucketID") )
            {
               if( iSSPServletRequest.mBucketID == null )
               {
                  if( del.getValue().equals("") || del.getValue() == null )
                  {
                     if( elementType == SSP_Operation.ALLOCATE )
                     {
                        error = DMErrorCodes.GEN_SET_FAILURE;
                        break;
                     }
                     error = DMErrorCodes.GEN_GET_FAILURE;
                     break;
                  }

                  iSSPServletRequest.mBucketID = del.getValue();
               }
               else
               {
                  // request.mBucketID was already set
                  // no duplicates allowed
                  error = DMErrorCodes.GEN_SET_FAILURE;
                  break;
               }
            }
            else if( ( delName.equals("requested") ) && ( elementType == SSP_Operation.ALLOCATE ) )
            {

               if( iSSPServletRequest.mRequestedSize == null )
               {
                  iSSPServletRequest.mRequestedSize = del.getValue();
               }
               else
               {
                  // request.mRequestedSize was already set
                  // no duplicates allowed
                  error = DMErrorCodes.GEN_SET_FAILURE;
                  break;
               }
            }
            else if( ( delName.equals("minimum") ) && ( elementType == SSP_Operation.ALLOCATE ) )
            {
               if( iSSPServletRequest.mMinimumSize == null )
               {
                  iSSPServletRequest.mMinimumSize = del.getValue();
               }
               else
               {
                  // request.mMinimumSize was already set
                  // no duplicates allowed 
                  error = DMErrorCodes.GEN_SET_FAILURE;
                  break;
               }

            }
            else if( ( delName.equals("reducible") ) && ( elementType == SSP_Operation.ALLOCATE ) )
            {

               if( iSSPServletRequest.mReducible == null )
               {
                  iSSPServletRequest.mReducible = del.getValue();
               }
               else
               {
                  // request.mReducible was already set
                  // no duplicates allowed
                  error = DMErrorCodes.GEN_SET_FAILURE;
                  break;
               }
            }
            else if( ( delName.equals("type") ) && ( elementType == SSP_Operation.ALLOCATE ) )
            {

               if( iSSPServletRequest.mBucketType == null )
               {
                  iSSPServletRequest.mBucketType = del.getValue();
               }
               else
               {
                  // request.mBucketType was already set
                  // no duplicates allowed
                  error = DMErrorCodes.GEN_SET_FAILURE;
                  break;
               }
            }
            else if( ( delName.equals("persistence") ) && ( elementType == SSP_Operation.ALLOCATE ) )
            {
               if( iSSPServletRequest.mPersistence == 0 )
               {
                  if( ( del.getValue() ).equals("learner") )
                  {
                     iSSPServletRequest.mPersistence = Persistence.LEARNER;
                  }
                  else if( ( del.getValue() ).equals("course") )
                  {
                     iSSPServletRequest.mPersistence = Persistence.COURSE;
                  }
                  else if( ( del.getValue() ).equals("session") )
                  {
                     iSSPServletRequest.mPersistence = Persistence.SESSION;
                  }
                  else
                  {
                     error = DMErrorCodes.TYPE_MISMATCH;
                     break;
                  }
               }
               else
               {
                  // request.mPersistence was already set
                  // no duplicates allowed
                  error = DMErrorCodes.GEN_SET_FAILURE;
                  break;
               }
            }
            else if( ( delName.equals("offset") )
               && ( ( elementType == SSP_Operation.GET_DATA ) || ( elementType == SSP_Operation.SET_DATA ) ) )
            {
               if( iSSPServletRequest.mOffset == null )
               {
                  iSSPServletRequest.mOffset = del.getValue();
               }
               else
               {
                  // request.mOffset was already set
                  // no duplicates allowed
                  if( elementType == SSP_Operation.GET_DATA )
                  {
                     error = DMErrorCodes.GEN_GET_FAILURE;
                     break;
                  }
                  error = DMErrorCodes.GEN_SET_FAILURE;
                  break;
               }
            }
            else if( ( delName.equals("size") ) && ( elementType == SSP_Operation.GET_DATA ) )
            {
               if( iSSPServletRequest.mSize == null )
               {
                  iSSPServletRequest.mSize = del.getValue();
               }
               else
               {
                  // request.mMinimumSize was already set
                  // no duplicates allowed
                  error = DMErrorCodes.GEN_SET_FAILURE;
                  break;
               }
            }
            else
            // not a ssp delimiter
            {
               if( ( elementType == SSP_Operation.GET_DATA ) || ( elementType == SSP_Operation.GET_ALLOCATION_SUCCESS ) )
               {
                  error = DMErrorCodes.GEN_GET_FAILURE;
                  break;
               }
               error = DMErrorCodes.GEN_SET_FAILURE;
               break;
            }
         }
         if( iSSPServletRequest.mBucketID != null )
         {
            if( ( elementType == SSP_Operation.ALLOCATE ) && ( iSSPServletRequest.mRequestedSize == null ) )
            {
               error = DMErrorCodes.GEN_SET_FAILURE;
            }
         }
         else
         {
            if( ( elementType == SSP_Operation.GET_DATA ) || ( elementType == SSP_Operation.GET_ALLOCATION_SUCCESS ) )
            {
               error = DMErrorCodes.GEN_GET_FAILURE;
            }
            else
            {
               error = DMErrorCodes.GEN_SET_FAILURE;
            }
         }
      }

      if( _Debug )
      {
         System.out.println("SSP_DataModel::processDelimiters() -- error: " + error);
         System.out.println("SSP_DataModel::processDelimiters() -- entering");
      }

      return error;
   }*/

   
   /**
    * Process Set Delimiters for appendData and setData calls This function 
    * adds functionality to accept non SSP delimiters and store them as part 
    * of the value.
    * 
    * @param iDMRequest The DMRequest which contains the datamodel request (ex. {bucketId=foo})
    * 
    * @param iSSPServletRequest The operation request. (ex. ssp.allocate)
    * 
    * @return The DMErrorCode for this operation.
    * 
    * @see org.adl.datamodels.DMErrorCodes
    * @see org.adl.datamodels.DMRequest
    * @see org.ims.ssp.samplerte.server.SSP_ServletRequest
    */
   /*private int processSetDelimiters(DMRequest iDMRequest, SSP_ServletRequest iSSPServletRequest)
   {
      if( _Debug )
      {
         System.out.println("SSP_DataModel::processSetDelimiters() -- " + "entering");
      }

      int error = DMErrorCodes.NO_ERROR;
      RequestToken tok = iDMRequest.getCurToken();
      String nonSSPDelims = "";
      boolean foundNonSSPDelim = false;

      // Retrieve the element type for the case stmt
      int elementType = iSSPServletRequest.mOperationType;

      if( elementType == SSP_Operation.SET_DATA )
      {
         for( int i = 0; i < tok.getDelimiterCount(); i++ )
         {
            RequestDelimiter del = tok.getDelimiterAt(i);
            String delName = del.getName();

            if( delName.equals("bucketID") )
            {
               if( iSSPServletRequest.mBucketID == null )
               {
                  if( del.getValue().equals("") || del.getValue() == null )
                  {
                     error = DMErrorCodes.GEN_SET_FAILURE;
                     break;
                  }
                  iSSPServletRequest.mBucketID = del.getValue();
               }
               else
               {
                  if( foundNonSSPDelim )
                  {
                     nonSSPDelims += "{" + delName + "=" + del.getValue() + "}";
                  }
                  else
                  {
                     error = DMErrorCodes.GEN_SET_FAILURE;
                     break;
                  }
               }
            }
            else if( delName.equals("offset") )
            {
               if( foundNonSSPDelim )
               {
                  nonSSPDelims += "{" + delName + "=" + del.getValue() + "}";
               }
               else
               {
                  if( iSSPServletRequest.mOffset == null )
                  {
                     iSSPServletRequest.mOffset = del.getValue();
                  }
                  else
                  {
                     error = DMErrorCodes.GEN_SET_FAILURE;
                     break;
                  }
               }
            }
            else if( iSSPServletRequest.mBucketID != null )
            {
               nonSSPDelims += "{" + delName + "=" + del.getValue() + "}";
               foundNonSSPDelim = true;
            }
            else
            {
               error = DMErrorCodes.GEN_SET_FAILURE;
               break;
            }
         }
      }
      else if( elementType == SSP_Operation.APPEND_DATA )
      {
         for( int i = 0; i < tok.getDelimiterCount(); i++ )
         {
            RequestDelimiter del = tok.getDelimiterAt(i);
            String delName = del.getName();

            if( delName.equals("bucketID") )
            {
               if( iSSPServletRequest.mBucketID == null )
               {
                  if( del.getValue().equals("") || del.getValue() == null )
                  {
                     error = DMErrorCodes.GEN_SET_FAILURE;
                     break;
                  }
                  iSSPServletRequest.mBucketID = del.getValue();
               }
               else
               {
                  if( foundNonSSPDelim )
                  {
                     nonSSPDelims += "{" + delName + "=" + del.getValue() + "}";
                  }
                  else
                  {
                     error = DMErrorCodes.GEN_SET_FAILURE;
                     break;
                  }
               }
            }
            else if( iSSPServletRequest.mBucketID != null )
            {
               nonSSPDelims += "{" + delName + "=" + del.getValue() + "}";
            }
            else
            {
               error = DMErrorCodes.GEN_SET_FAILURE;
               break;
            }
         }
      }
      else
      {
         // shouldn't get here
         error = DMErrorCodes.GEN_SET_FAILURE;
      }

      iSSPServletRequest.mValue = nonSSPDelims + iSSPServletRequest.mValue;

      if( _Debug )
      {
         System.out.println("SSP_DataModel::processSetDelimiters() -- error: " + error);
         System.out.println("SSP_DataModel::processSetDelimiters() -- exiting");
      }

      return error;
   }*/

   
   /**
    * Prints request information to the console
    * 
    * @param iRequest The ssp servlet request to be sent to the server.
    */
   /*private void printRequest(SSP_ServletRequest iRequest)
   {
      SSP_ServletRequest request = iRequest;

      System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
      System.out.println("      Request to be sent to server           ");
      System.out.println("                                             ");
      System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
      System.out.println("mBucketID = " + request.mBucketID);
      System.out.println("mStudentID = " + request.mStudentID);
      System.out.println("mCourseID = " + request.mCourseID);
      System.out.println("mSCOID = " + request.mSCOID);
      System.out.println("mAttemptID = " + request.mAttemptID);
      System.out.println("mOperationType = " + request.mOperationType);
      System.out.println("mMinimumSize = " + request.mMinimumSize);
      System.out.println("mRequestedSize = " + request.mRequestedSize);
      System.out.println("mReducible = " + request.mReducible);
      System.out.println("mBucketType = " + request.mBucketType);
      System.out.println("mPersistence = " + request.mPersistence);
      System.out.println("mOffset = " + request.mOffset);
      System.out.println("mSize = " + request.mSize);
      System.out.println("mValue = " + request.mValue);
      System.out.println("mManagedBucketIndex = " + request.mManagedBucketIndex);
      System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
      System.out.println("              End of request                 ");
      System.out.println("                                             ");
      System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
   }*/

   
   /**
    * Prints response information to the console
    * 
    * @param iResponse The ssp servlet response from the server.
    */
   /*private void printResponse(SSP_ServletResponse iResponse)
   {
      SSP_ServletResponse response = iResponse;

      ManagedBucket mb = response.mManagedBucketInfo;
      StatusInfo si = response.mStatusInfo;
      BucketState bs = response.mBucketState;

      System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
      System.out.println("       Response sent from server             ");
      System.out.println("                                             ");
      System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
      System.out.println("mBucketID = " + response.mBucketID);
      System.out.println("mStudentID = " + response.mStudentID);
      System.out.println("mCourseID = " + response.mCourseID);
      System.out.println("mAttemptID = " + response.mAttemptID);
      System.out.println("mReturnValue = " + response.mReturnValue);
      System.out.println("Managed Bucket success status = " + mb.getSuccessStatus());
      System.out.println("Status Info Error Code = " + si.mErrorCode.toString());
      if( si.mErrorDescription != null )
      {
         System.out.println("Status Info Error Description = " + si.mErrorDescription);
      }

      if( bs != null )
      {
         String bucType = new String(bs.mBucketType);
         String totSpace = ( bs.mTotalSpace ).toString();
         String used = ( bs.mUsed ).toString();
         System.out.println("Bucket State bucket type = " + bucType);
         System.out.println("Bucket State total space = " + totSpace);
         System.out.println("Bucket State used = " + used);
      }
      System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
      System.out.println("             End of response                 ");
      System.out.println("                                             ");
      System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

   }*/
} // end SSP_DATAMODEL
