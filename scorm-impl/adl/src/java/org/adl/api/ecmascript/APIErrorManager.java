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

package org.adl.api.ecmascript;

import java.util.Hashtable;
import java.util.Enumeration;
import org.adl.datamodels.DMErrorCodes;
import org.adl.datamodels.ssp.SSP_DMErrorCodes;


/**
 * This class implements the error handling capabilities of the RTE API.<br><br>
 *
 * <strong>Filename:</strong> APIErrorManager<br><br>
 *
 * <strong>Description:</strong><br>
 * This class manages the error codes set by the API methods in the API Adapter
 * Applet.
 * <br><br>
 *
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the SCORM Sample RTE 1.3. <br>
 * <br>
 *
 * <strong>Implementation Issues:</strong><br>
 * For purposes of this example, this class uses a hardcoded array to store
 * the error mapping.<br><br>
 *
 * <strong>Known Problems:</strong><br><br>
 *
 * <strong>Side Effects:</strong><br><br>
 *
 * <strong>References:</strong><br>
 * <ul>
 *     <li>IMS SS Specification
 *     <li>SCORM 2004
 * </ul>
 *
 * @author ADL Technical Team
 */
public class APIErrorManager implements IErrorManager
{
   /**
    * The abstract error code from the last API method invocation.
    */
   private static int mCurrentErrorCode = 0;

   /**
    * Hashtable that holds all of the API Error Codes as Strings.  The
    * abstract error codes are used as the keys for the String error codes.
    */
   private Hashtable mErrorCodes;

   /**
    * Hashtable that holds all of the API Error Messages.  The abstract
    * error codes are used as the keys for the error messages.
    */
   private Hashtable mErrorMessages;

   /**
    * Hashtable that holds all of the API Error Diagnostics.  The abstract
    * error codes are used as the keys for the error diagnostics.
    */
   private Hashtable mErrorDiagnostics;

   /**
    * Hashtable that converts an error string to the abstract integer.  The
    * String representations of the SCORM error codes are used as the keys.
    */
   private Hashtable mAbstErrors;


   /**
    * Initializes this <code>mCurrentErrorCode</code> to 'No Error' and
    * initializes the Hashtables based on the API version.
    *
    * @param iAPIVersion - The API version that will use this error manager.
    */
   public APIErrorManager(int iAPIVersion)
   {

      mCurrentErrorCode = APIErrorCodes.NO_ERROR;
      // Intialize the Hashtable size - should be a prime number
      mErrorCodes = new Hashtable(211);
      mErrorMessages = new Hashtable(59);
      mErrorDiagnostics = new Hashtable(59);
      mAbstErrors = new Hashtable(59);

      if(iAPIVersion == SCORM_2004_API)
      {
         //  Initialize the SCORM 2004 RTE API Error Codes Hash Table
         mErrorCodes.put(new Integer(APIErrorCodes.NO_ERROR), new String("0"));
         mErrorCodes.put(new Integer(APIErrorCodes.GENERAL_EXCEPTION),
                         new String("101"));
         mErrorCodes.put(new Integer(APIErrorCodes.GENERAL_INIT_FAILURE),
                         new String("102"));
         mErrorCodes.put(new Integer(APIErrorCodes.ALREADY_INITIALIZED),
                         new String("103"));
         mErrorCodes.put(new Integer(APIErrorCodes.CONTENT_INSTANCE_TERMINATED),
                         new String("104"));
         mErrorCodes.put(new Integer(APIErrorCodes.GENERAL_TERMINATION_FAILURE),
                         new String("111"));
         mErrorCodes.put(new Integer(APIErrorCodes.TERMINATE_BEFORE_INIT),
                         new String("112"));
         mErrorCodes.put(new Integer(APIErrorCodes.TERMINATE_AFTER_TERMINATE),
                         new String("113"));
         mErrorCodes.put(new Integer(APIErrorCodes.GET_BEFORE_INIT),
                         new String("122"));
         mErrorCodes.put(new Integer(APIErrorCodes.GET_AFTER_TERMINATE),
                         new String("123"));
         mErrorCodes.put(new Integer(APIErrorCodes.SET_BEFORE_INIT),
                         new String("132"));
         mErrorCodes.put(new Integer(APIErrorCodes.SET_AFTER_TERMINATE),
                         new String("133"));
         mErrorCodes.put(new Integer(APIErrorCodes.COMMIT_BEFORE_INIT),
                         new String("142"));
         mErrorCodes.put(new Integer(APIErrorCodes.COMMIT_AFTER_TERMINATE),
                         new String("143"));
         mErrorCodes.put(new Integer(DMErrorCodes.GEN_ARGUMENT_ERROR),
                         new String("201"));
         mErrorCodes.put(new Integer(DMErrorCodes.GEN_GET_FAILURE),
                         new String("301"));
         mErrorCodes.put(new Integer(DMErrorCodes.GEN_SET_FAILURE),
                         new String("351"));
         mErrorCodes.put(new Integer(APIErrorCodes.GENERAL_COMMIT_FAILURE),
                         new String("391"));
         mErrorCodes.put(new Integer(DMErrorCodes.UNDEFINED_ELEMENT),
                         new String("401"));
         mErrorCodes.put(new Integer(DMErrorCodes.NOT_IMPLEMENTED),
                         new String("402"));
         mErrorCodes.put(new Integer(DMErrorCodes.NOT_INITIALIZED),
                         new String("403"));
         mErrorCodes.put(new Integer(DMErrorCodes.READ_ONLY),
                         new String("404"));
         mErrorCodes.put(new Integer(DMErrorCodes.WRITE_ONLY),
                         new String("405"));
         mErrorCodes.put(new Integer(DMErrorCodes.TYPE_MISMATCH),
                         new String("406"));
         mErrorCodes.put(new Integer(DMErrorCodes.VALUE_OUT_OF_RANGE),
                         new String("407"));
         mErrorCodes.put(new Integer(DMErrorCodes.DEP_NOT_ESTABLISHED),
                         new String("408"));
         mErrorCodes.put(new Integer(DMErrorCodes.DOES_NOT_HAVE_CHILDREN),
                         new String("301"));
         mErrorCodes.put(new Integer(DMErrorCodes.DOES_NOT_HAVE_COUNT),
                         new String("301"));
         mErrorCodes.put(new Integer(DMErrorCodes.DOES_NOT_HAVE_VERSION),
                         new String("301"));
         mErrorCodes.put(new Integer(DMErrorCodes.SET_OUT_OF_ORDER),
                         new String("351"));
         mErrorCodes.put(new Integer(DMErrorCodes.OUT_OF_RANGE),
                         new String("301"));
         mErrorCodes.put(new Integer(DMErrorCodes.ELEMENT_NOT_SPECIFIED),
                         new String("351"));
         mErrorCodes.put(new Integer(DMErrorCodes.NOT_UNIQUE),
                         new String("351"));
         mErrorCodes.put(new Integer(DMErrorCodes.MAX_EXCEEDED),
                         new String("351"));
         mErrorCodes.put(new Integer(DMErrorCodes.SET_KEYWORD),
                         new String("404"));
         mErrorCodes.put(new Integer(DMErrorCodes.INVALID_REQUEST),
                         new String("401"));
         mErrorCodes.put(new Integer(DMErrorCodes.INVALID_ARGUMENT),
                         new String("301"));
         mErrorCodes.put(new Integer(DMErrorCodes.OVERWRITE_ID),
                         new String("351"));

         // Initialize the SSP API Error Codes Hash Table
         mErrorCodes.put(new Integer(SSP_DMErrorCodes.NO_ERROR),
                         new String("0"));
         mErrorCodes.put(new Integer(SSP_DMErrorCodes.INVALID_SET_PARMS),
                         new String("351"));
         mErrorCodes.put(new Integer(SSP_DMErrorCodes.INVALID_GET_PARMS),
                         new String("301"));
         mErrorCodes.put(new Integer(SSP_DMErrorCodes.ALLOCATE_MIN_GREATER_MAX),
                         new String("406"));
         mErrorCodes.put(new Integer(SSP_DMErrorCodes.DATABASE_UPDATE_FAILURE),
                         new String("406"));
         mErrorCodes.put(new Integer(SSP_DMErrorCodes.TYPE_MISMATCH),
                         new String("406"));
         mErrorCodes.put(new Integer(SSP_DMErrorCodes.ALLOCATE_SPACE_EXCEEDED),
                         new String("351"));
         mErrorCodes.put(new Integer(SSP_DMErrorCodes.BUCKET_NOT_ALLOCATED_SET),
                         new String("351"));
         mErrorCodes.put(new Integer(SSP_DMErrorCodes.BUCKET_NOT_ALLOCATED_GET),
                         new String("301"));
         mErrorCodes.put(new Integer(SSP_DMErrorCodes.REQUESTED_SIZE_EXCEEDED_AVAIL),
                         new String("301"));
         mErrorCodes.put(new Integer(SSP_DMErrorCodes.BUCKET_SIZE_EXCEEDED_SET),
                         new String("351"));
         mErrorCodes.put(new Integer(SSP_DMErrorCodes.CREATE_BUCKET_PERSIST),
                         new String("351"));
         mErrorCodes.put(new Integer(SSP_DMErrorCodes.DATABASE_CREATE_FAILURE),
                         new String("351"));

         mErrorCodes.put(new Integer(SSP_DMErrorCodes.BUCKET_IMPROPERLY_DECLARED_SET),
                         new String("351"));
         mErrorCodes.put(new Integer(SSP_DMErrorCodes.BUCKET_IMPROPERLY_DECLARED_GET),
                         new String("301"));
         mErrorCodes.put(new Integer(SSP_DMErrorCodes.OFFSET_EXCEEDS_BUCKET_SIZE_SET),
                         new String("351"));
         mErrorCodes.put(new Integer(SSP_DMErrorCodes.OFFSET_EXCEEDS_BUCKET_SIZE_GET),
                         new String("301"));
         mErrorCodes.put(new Integer(SSP_DMErrorCodes.BUCKET_NOT_PACKED),
                         new String("351"));



         //  Initialize the SCORM 2004 RTE API Error Messages Hash Table
         mErrorMessages.put(new Integer(APIErrorCodes.NO_ERROR),
                            new String("No Error"));
         mErrorMessages.put(new Integer(APIErrorCodes.GENERAL_EXCEPTION),
                            new String("General Exception"));
         mErrorMessages.put(new Integer(APIErrorCodes.GENERAL_INIT_FAILURE),
                            new String("General Initialization Error"));
         mErrorMessages.put(new Integer(APIErrorCodes.ALREADY_INITIALIZED),
                            new String("Already Initialized"));
         mErrorMessages.put(new Integer(
                            APIErrorCodes.CONTENT_INSTANCE_TERMINATED),
                            new String("Content Instance Terminated"));
         mErrorMessages.put(new Integer(
                            APIErrorCodes.GENERAL_TERMINATION_FAILURE),
                            new String("General Termination Failure"));
         mErrorMessages.put(new Integer(APIErrorCodes.TERMINATE_BEFORE_INIT),
                            new String("Termination Before Initialization"));
         mErrorMessages.put(new Integer(
                            APIErrorCodes.TERMINATE_AFTER_TERMINATE),
                            new String("Termination After Termination"));
         mErrorMessages.put(new Integer(APIErrorCodes.GET_BEFORE_INIT),
                            new String("Retrieve Data Before Initialization"));
         mErrorMessages.put(new Integer(APIErrorCodes.GET_AFTER_TERMINATE),
                            new String("Retrieve Data After Termination"));
         mErrorMessages.put(new Integer(APIErrorCodes.SET_BEFORE_INIT),
                            new String("Store Data Before Initialization"));
         mErrorMessages.put(new Integer(APIErrorCodes.SET_AFTER_TERMINATE),
                            new String("Store Data After Termination"));
         mErrorMessages.put(new Integer(APIErrorCodes.COMMIT_BEFORE_INIT),
                            new String("Commit Before Initialization"));
         mErrorMessages.put(new Integer(APIErrorCodes.COMMIT_AFTER_TERMINATE),
                            new String("Commit After Termination"));
         mErrorMessages.put(new Integer(DMErrorCodes.GEN_ARGUMENT_ERROR),
                            new String("General Argument Error"));
         mErrorMessages.put(new Integer(DMErrorCodes.GEN_GET_FAILURE),
                            new String("General Get Failure"));
         mErrorMessages.put(new Integer(DMErrorCodes.GEN_SET_FAILURE),
                            new String("General Set Failure"));
         mErrorMessages.put(new Integer(APIErrorCodes.GENERAL_COMMIT_FAILURE),
                            new String("General Commit Failure"));
         mErrorMessages.put(new Integer(DMErrorCodes.UNDEFINED_ELEMENT),
                            new String("Undefined Data Model Element"));
         mErrorMessages.put(new Integer(DMErrorCodes.NOT_IMPLEMENTED),
                            new String("Unimplemented Data Model Element"));
         mErrorMessages.put(new Integer(DMErrorCodes.NOT_INITIALIZED),
                            new String("Data Model Element Value Not " +
                                        "Initialized"));
         mErrorMessages.put(new Integer(DMErrorCodes.READ_ONLY),
                            new String("Data Model Element Is Read Only"));
         mErrorMessages.put(new Integer(DMErrorCodes.WRITE_ONLY),
                            new String("Data Model Element Is Write Only"));
         mErrorMessages.put(new Integer(DMErrorCodes.TYPE_MISMATCH),
                            new String("Data Model Element Type Mismatch"));
         mErrorMessages.put(new Integer(DMErrorCodes.VALUE_OUT_OF_RANGE),
                            new String("Data Model Element Value Out Of " +
                                       "Range"));
         mErrorMessages.put(new Integer(DMErrorCodes.DEP_NOT_ESTABLISHED),
                            new String("Data Model Dependency Not " +
                                       "Established"));
         mErrorMessages.put(new Integer(DMErrorCodes.DOES_NOT_HAVE_CHILDREN),
                            new String("General Get Failure"));
         mErrorMessages.put(new Integer(DMErrorCodes.DOES_NOT_HAVE_COUNT),
                            new String("General Get Failure"));
         mErrorMessages.put(new Integer(DMErrorCodes.DOES_NOT_HAVE_VERSION),
                            new String("General Get Failure"));
         mErrorMessages.put(new Integer(DMErrorCodes.SET_OUT_OF_ORDER),
                            new String("General Set Failure"));
         mErrorMessages.put(new Integer(DMErrorCodes.OUT_OF_RANGE),
                            new String("General Get Failure"));
         mErrorMessages.put(new Integer(DMErrorCodes.ELEMENT_NOT_SPECIFIED),
                            new String("General Get Failure"));
         mErrorMessages.put(new Integer(DMErrorCodes.NOT_UNIQUE),
                            new String("General Set Failure"));
         mErrorMessages.put(new Integer(DMErrorCodes.MAX_EXCEEDED),
                            new String("General Set Failure"));
         mErrorMessages.put(new Integer(DMErrorCodes.SET_KEYWORD),
                            new String("Data Model Element Is Read Only"));
         mErrorMessages.put(new Integer(DMErrorCodes.INVALID_REQUEST),
                            new String("Undefined Data Model Element"));
         mErrorMessages.put(new Integer(DMErrorCodes.INVALID_ARGUMENT),
                            new String("General Get Failure"));
         mErrorMessages.put(new Integer(DMErrorCodes.OVERWRITE_ID ),
                            new String("General Set Failure"));

         // Initialize the SSP API Error Messages Hash Table
         mErrorMessages.put(new Integer(SSP_DMErrorCodes.NO_ERROR),
                         new String("No Error"));
         mErrorMessages.put(new Integer(SSP_DMErrorCodes.INVALID_SET_PARMS),
                         new String("General Set Failure"));
         mErrorMessages.put(new Integer(SSP_DMErrorCodes.INVALID_GET_PARMS),
                         new String("General Get Failure"));
         mErrorMessages.put(new Integer(SSP_DMErrorCodes.ALLOCATE_MIN_GREATER_MAX),
                         new String("Data Model Element Type Mismatch"));
         mErrorMessages.put(new Integer(SSP_DMErrorCodes.DATABASE_UPDATE_FAILURE),
                         new String("General Get Failure"));
         mErrorMessages.put(new Integer(SSP_DMErrorCodes.TYPE_MISMATCH),
                         new String("Type Mismatch"));
         mErrorMessages.put(new Integer(SSP_DMErrorCodes.ALLOCATE_SPACE_EXCEEDED),
                         new String("General Set Failure"));
         mErrorMessages.put(new Integer(SSP_DMErrorCodes.BUCKET_NOT_ALLOCATED_SET),
                         new String("General Set Failure"));
         mErrorMessages.put(new Integer(SSP_DMErrorCodes.BUCKET_NOT_ALLOCATED_GET),
                         new String("General Get Failure"));
         mErrorMessages.put(new Integer(SSP_DMErrorCodes.REQUESTED_SIZE_EXCEEDED_AVAIL),
                         new String("General Get Failure"));
         mErrorMessages.put(new Integer(SSP_DMErrorCodes.BUCKET_SIZE_EXCEEDED_SET),
                         new String("General Set Failure"));
         mErrorMessages.put(new Integer(SSP_DMErrorCodes.CREATE_BUCKET_PERSIST),
                         new String("General Set Failure"));
         mErrorMessages.put(new Integer(SSP_DMErrorCodes.DATABASE_CREATE_FAILURE),
                         new String("General Set Failure"));
         mErrorMessages.put(new Integer(SSP_DMErrorCodes.BUCKET_IMPROPERLY_DECLARED_SET),
                         new String("General Set Failure"));
         mErrorMessages.put(new Integer(SSP_DMErrorCodes.BUCKET_IMPROPERLY_DECLARED_GET),
                         new String("General Get Failure"));
         mErrorMessages.put(new Integer(SSP_DMErrorCodes.OFFSET_EXCEEDS_BUCKET_SIZE_SET),
                         new String("General Set Failure"));
         mErrorMessages.put(new Integer(SSP_DMErrorCodes.OFFSET_EXCEEDS_BUCKET_SIZE_GET),
                         new String("General Get Failure"));
         mErrorMessages.put(new Integer(SSP_DMErrorCodes.BUCKET_NOT_PACKED),
                         new String("General Set Failure"));



         //  Initialize the SCORM 2004 RTE API Error
         //  Diagnostics Hash Table
         mErrorDiagnostics.put(new Integer(APIErrorCodes.NO_ERROR),
                               new String("No Error"));
         mErrorDiagnostics.put(new Integer(APIErrorCodes.GENERAL_EXCEPTION),
                               new String("General Exception"));
         mErrorDiagnostics.put(new Integer(APIErrorCodes.GENERAL_INIT_FAILURE),
                               new String("General Initialization Error"));
         mErrorDiagnostics.put(new Integer(APIErrorCodes.ALREADY_INITIALIZED),
                               new String("Already Initialized"));
         mErrorDiagnostics.put(new Integer(
                               APIErrorCodes.CONTENT_INSTANCE_TERMINATED),
                               new String("Content Instance Terminated"));
         mErrorDiagnostics.put(new Integer(
                               APIErrorCodes.GENERAL_TERMINATION_FAILURE),
                               new String("General Termination Failure"));
         mErrorDiagnostics.put(new Integer(APIErrorCodes.TERMINATE_BEFORE_INIT),
                               new String("Termination Before Initialization"));
         mErrorDiagnostics.put(new Integer(
                               APIErrorCodes.TERMINATE_AFTER_TERMINATE),
                               new String("Termination After Termination"));
         mErrorDiagnostics.put(new Integer(APIErrorCodes.GET_BEFORE_INIT),
                               new String("Retrieve Data Before " +
                                          "Initialization"));
         mErrorDiagnostics.put(new Integer(APIErrorCodes.GET_AFTER_TERMINATE),
                               new String("Retrieve Data After Termination"));
         mErrorDiagnostics.put(new Integer(APIErrorCodes.SET_BEFORE_INIT),
                               new String("Store Data Before Initialization"));
         mErrorDiagnostics.put(new Integer(APIErrorCodes.SET_AFTER_TERMINATE),
                               new String("Store Data After Termination"));
         mErrorDiagnostics.put(new Integer(APIErrorCodes.COMMIT_BEFORE_INIT),
                               new String("Commit Before Initialization"));
         mErrorDiagnostics.put(new Integer(
                               APIErrorCodes.COMMIT_AFTER_TERMINATE),
                               new String("Commit After Termination"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.GEN_ARGUMENT_ERROR),
                               new String("General Argument Error"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.GEN_GET_FAILURE),
                               new String("General Get Failure"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.GEN_SET_FAILURE),
                               new String("General Set Failure"));
         mErrorDiagnostics.put(new Integer(
                               APIErrorCodes.GENERAL_COMMIT_FAILURE),
                               new String("General Commit Failure"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.UNDEFINED_ELEMENT),
                               new String("Undefined Data Model Element"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.NOT_IMPLEMENTED),
                               new String("Unimplemented Data Model Element"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.NOT_INITIALIZED),
                               new String("Data Model Element Value Not " +
                                        "Initialized"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.READ_ONLY),
                               new String("Data Model Element Is Read Only"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.WRITE_ONLY),
                               new String("Data Model Element Is Write Only"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.TYPE_MISMATCH),
                               new String("Data Model Element Type Mismatch"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.VALUE_OUT_OF_RANGE),
                               new String("Data Model Element Value Out Of " +
                                          "Range"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.DEP_NOT_ESTABLISHED),
                               new String("Data Model Dependency Not " +
                                          "Established"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.DOES_NOT_HAVE_CHILDREN),
                               new String("Data Model Element does not have " +
                                          "Children"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.DOES_NOT_HAVE_COUNT),
                               new String("Data Model Element does not have " +
                                          "Count"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.DOES_NOT_HAVE_VERSION),
                               new String("Data Model Element does not have " +
                                          "Version"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.SET_OUT_OF_ORDER),
                               new String("Data Model Array Set out of Order"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.OUT_OF_RANGE),
                               new String("Value Out of Range"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.ELEMENT_NOT_SPECIFIED),
                               new String("No Element Specified"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.NOT_UNIQUE),
                               new String("Value is not Unique"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.MAX_EXCEEDED),
                               new String("Error - Maximum Exceeded"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.SET_KEYWORD),
                               new String("Data Model Element Is a Keyword"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.INVALID_REQUEST),
                               new String("Request was Invalid"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.INVALID_ARGUMENT),
                               new String("Invalid Argument Error"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.OVERWRITE_ID ),
                               new String("Attempt to overwrite Objective ID"));

         // Initialize the SSP API Error Diagnostics Hash Table
         mErrorDiagnostics.put(new Integer(SSP_DMErrorCodes.NO_ERROR),
                         new String("No Error"));
         mErrorDiagnostics.put(new Integer(SSP_DMErrorCodes.INVALID_SET_PARMS),
                         new String("Invalid SSP parameters for set " +
                                    "operation"));
         mErrorDiagnostics.put(new Integer(SSP_DMErrorCodes.INVALID_GET_PARMS),
                         new String("Invalid SSP parameters for get " +
                                    "operation"));
         mErrorDiagnostics.put(new Integer(SSP_DMErrorCodes.ALLOCATE_MIN_GREATER_MAX),
                         new String("Data Model Element Type Mismatch - " +
                                    "minimum size greater than requested " +
                                    "size"));
         mErrorDiagnostics.put(new Integer(SSP_DMErrorCodes.DATABASE_UPDATE_FAILURE),
                         new String("SSP database update failure"));
         mErrorDiagnostics.put(new Integer(SSP_DMErrorCodes.TYPE_MISMATCH),
                         new String("Data model element type mismatch"));
         mErrorDiagnostics.put(new Integer(SSP_DMErrorCodes.ALLOCATE_SPACE_EXCEEDED),
                         new String("Bucket not allocated for ssp set " +
                                    "operation"));
         mErrorDiagnostics.put(new Integer(SSP_DMErrorCodes.BUCKET_NOT_ALLOCATED_SET),
                         new String("Bucket not allocated for ssp set " + 
                                    "operation"));
         mErrorDiagnostics.put(new Integer(SSP_DMErrorCodes.BUCKET_NOT_ALLOCATED_GET),
                         new String("Bucket not allocated for ssp get " +
                                    "operation"));
         mErrorDiagnostics.put(new Integer(SSP_DMErrorCodes.REQUESTED_SIZE_EXCEEDED_AVAIL),
                         new String("Requested data exceeds available data"));
         mErrorDiagnostics.put(new Integer(SSP_DMErrorCodes.BUCKET_SIZE_EXCEEDED_SET),
                         new String("Bucket size exceeded for ssp set " +
                                    "operation"));
         mErrorDiagnostics.put(new Integer(SSP_DMErrorCodes.CREATE_BUCKET_PERSIST),
                         new String("Bucket not able to be persisted"));
         mErrorDiagnostics.put(new Integer(SSP_DMErrorCodes.DATABASE_CREATE_FAILURE),
                         new String("SSP database create failure"));
         mErrorDiagnostics.put(new Integer(SSP_DMErrorCodes.BUCKET_IMPROPERLY_DECLARED_SET),
                         new String("The requested bucket was improperly " +
                                    "declared for ssp set operation"));
         mErrorDiagnostics.put(new Integer(SSP_DMErrorCodes.BUCKET_IMPROPERLY_DECLARED_GET),
                         new String("The requested bucket was improperly " +
                                    "declared for ssp get operation"));
         mErrorDiagnostics.put(new Integer(SSP_DMErrorCodes.OFFSET_EXCEEDS_BUCKET_SIZE_SET),
                         new String("The offset exceeds the bucket size for " +
                                    "ssp set operation"));
         mErrorDiagnostics.put(new Integer(SSP_DMErrorCodes.OFFSET_EXCEEDS_BUCKET_SIZE_GET),
                         new String("The offset exceeds the bucket size for " +
                                    "ssp get operation"));
         mErrorDiagnostics.put(new Integer(SSP_DMErrorCodes.BUCKET_NOT_PACKED),
                         new String("The bucket was not packed"));


         // Hash Table used to get the abstract error code from an error String
         mAbstErrors.put(new String("0"), new Integer(APIErrorCodes.NO_ERROR));
         mAbstErrors.put(new String("101"),
                         new Integer(APIErrorCodes.GENERAL_EXCEPTION));
         mAbstErrors.put(new String("102"),
                         new Integer(APIErrorCodes.GENERAL_INIT_FAILURE));
         mAbstErrors.put(new String("103"),
                         new Integer(APIErrorCodes.ALREADY_INITIALIZED));
         mAbstErrors.put(new String("104"),
                        new Integer(APIErrorCodes.CONTENT_INSTANCE_TERMINATED));
         mAbstErrors.put(new String("111"),
                        new Integer(APIErrorCodes.GENERAL_TERMINATION_FAILURE));
         mAbstErrors.put(new String("112"),
                         new Integer(APIErrorCodes.TERMINATE_BEFORE_INIT));
         mAbstErrors.put(new String("113"),
                         new Integer(APIErrorCodes.TERMINATE_AFTER_TERMINATE));
         mAbstErrors.put(new String("122"),
                         new Integer(APIErrorCodes.GET_BEFORE_INIT));
         mAbstErrors.put(new String("123"),
                         new Integer(APIErrorCodes.GET_AFTER_TERMINATE));
         mAbstErrors.put(new String("132"),
                         new Integer(APIErrorCodes.SET_BEFORE_INIT));
         mAbstErrors.put(new String("133"),
                         new Integer(APIErrorCodes.SET_AFTER_TERMINATE));
         mAbstErrors.put(new String("142"),
                         new Integer(APIErrorCodes.COMMIT_BEFORE_INIT));
         mAbstErrors.put(new String("143"),
                         new Integer(APIErrorCodes.COMMIT_AFTER_TERMINATE));
         mAbstErrors.put(new String("201"),
                         new Integer(DMErrorCodes.GEN_ARGUMENT_ERROR));
         mAbstErrors.put(new String("301"),
                         new Integer(DMErrorCodes.GEN_GET_FAILURE));
         mAbstErrors.put(new String("351"),
                         new Integer(DMErrorCodes.GEN_SET_FAILURE));
         mAbstErrors.put(new String("391"),
                         new Integer(APIErrorCodes.GENERAL_COMMIT_FAILURE));
         mAbstErrors.put(new String("401"),
                         new Integer(DMErrorCodes.UNDEFINED_ELEMENT));
         mAbstErrors.put(new String("402"),
                         new Integer(DMErrorCodes.NOT_IMPLEMENTED));
         mAbstErrors.put(new String("403"),
                         new Integer(DMErrorCodes.NOT_INITIALIZED));
         mAbstErrors.put(new String("404"),
                         new Integer(DMErrorCodes.READ_ONLY));
         mAbstErrors.put(new String("405"),
                         new Integer(DMErrorCodes.WRITE_ONLY));
         mAbstErrors.put(new String("406"),
                         new Integer(DMErrorCodes.TYPE_MISMATCH));
         mAbstErrors.put(new String("407"),
                         new Integer(DMErrorCodes.VALUE_OUT_OF_RANGE));
         mAbstErrors.put(new String("408"),
                         new Integer(DMErrorCodes.DEP_NOT_ESTABLISHED));

         // SSP abstract error mapping
         mAbstErrors.put(new String("0"), new Integer(APIErrorCodes.NO_ERROR));
         mAbstErrors.put(new String("10000"),
                         new Integer(SSP_DMErrorCodes.INVALID_SET_PARMS));
         mAbstErrors.put(new String("10001"),
                         new Integer(SSP_DMErrorCodes.INVALID_GET_PARMS));
         mAbstErrors.put(new String("10002"),
                         new Integer(SSP_DMErrorCodes.ALLOCATE_MIN_GREATER_MAX));
         mAbstErrors.put(new String("10003"),
                        new Integer(SSP_DMErrorCodes.DATABASE_UPDATE_FAILURE));
         mAbstErrors.put(new String("10004"),
                        new Integer(SSP_DMErrorCodes.TYPE_MISMATCH));
         mAbstErrors.put(new String("10005"),
                         new Integer(SSP_DMErrorCodes.ALLOCATE_SPACE_EXCEEDED));
         mAbstErrors.put(new String("10006"),
                         new Integer(SSP_DMErrorCodes.BUCKET_NOT_ALLOCATED_SET));
         mAbstErrors.put(new String("10007"),
                         new Integer(SSP_DMErrorCodes.BUCKET_NOT_ALLOCATED_GET));
         mAbstErrors.put(new String("10008"),
                         new Integer(SSP_DMErrorCodes.REQUESTED_SIZE_EXCEEDED_AVAIL));
         mAbstErrors.put(new String("10009"),
                         new Integer(SSP_DMErrorCodes.BUCKET_SIZE_EXCEEDED_SET));
         mAbstErrors.put(new String("10010"),
                         new Integer(SSP_DMErrorCodes.CREATE_BUCKET_PERSIST));
         mAbstErrors.put(new String("10011"),
                         new Integer(SSP_DMErrorCodes.DATABASE_CREATE_FAILURE));
         mAbstErrors.put(new String("10012"),
                         new Integer(SSP_DMErrorCodes.BUCKET_IMPROPERLY_DECLARED_SET));
         mAbstErrors.put(new String("10013"),
                         new Integer(SSP_DMErrorCodes.BUCKET_IMPROPERLY_DECLARED_GET));
         mAbstErrors.put(new String("10014"),
                         new Integer(SSP_DMErrorCodes.OFFSET_EXCEEDS_BUCKET_SIZE_SET));
         mAbstErrors.put(new String("10015"),
                         new Integer(SSP_DMErrorCodes.OFFSET_EXCEEDS_BUCKET_SIZE_GET));
         mAbstErrors.put(new String("10016"),
                         new Integer(SSP_DMErrorCodes.BUCKET_NOT_PACKED));
      }
      else if(iAPIVersion == SCORM_1_2_API)
      {
         //  Initialize the SCORM Version 1.2 API Error Codes Hash Table
         mErrorCodes.put(new Integer(APIErrorCodes.NO_ERROR), new String("0"));
         mErrorCodes.put(new Integer(APIErrorCodes.GENERAL_EXCEPTION),
                         new String("101"));
         mErrorCodes.put(new Integer(APIErrorCodes.GENERAL_INIT_FAILURE),
                         new String("101"));
         mErrorCodes.put(new Integer(APIErrorCodes.ALREADY_INITIALIZED),
                         new String("101"));
         mErrorCodes.put(new Integer(APIErrorCodes.CONTENT_INSTANCE_TERMINATED),
                         new String("101"));
         mErrorCodes.put(new Integer(APIErrorCodes.GENERAL_TERMINATION_FAILURE),
                         new String("101"));
         mErrorCodes.put(new Integer(APIErrorCodes.TERMINATE_BEFORE_INIT),
                         new String("301"));
         mErrorCodes.put(new Integer(APIErrorCodes.TERMINATE_AFTER_TERMINATE),
                         new String("101"));
         mErrorCodes.put(new Integer(APIErrorCodes.GET_BEFORE_INIT),
                         new String("301"));
         mErrorCodes.put(new Integer(APIErrorCodes.GET_AFTER_TERMINATE),
                         new String("101"));
         mErrorCodes.put(new Integer(APIErrorCodes.SET_BEFORE_INIT),
                         new String("301"));
         mErrorCodes.put(new Integer(APIErrorCodes.SET_AFTER_TERMINATE),
                         new String("101"));
         mErrorCodes.put(new Integer(APIErrorCodes.COMMIT_BEFORE_INIT),
                         new String("301"));
         mErrorCodes.put(new Integer(APIErrorCodes.COMMIT_AFTER_TERMINATE),
                         new String("101"));
         mErrorCodes.put(new Integer(DMErrorCodes.GEN_ARGUMENT_ERROR),
                         new String("201"));
         mErrorCodes.put(new Integer(DMErrorCodes.GEN_GET_FAILURE),
                         new String("101"));
         mErrorCodes.put(new Integer(DMErrorCodes.GEN_SET_FAILURE),
                         new String("101"));
         mErrorCodes.put(new Integer(APIErrorCodes.GENERAL_COMMIT_FAILURE),
                         new String("101"));
         mErrorCodes.put(new Integer(DMErrorCodes.UNDEFINED_ELEMENT),
                         new String("401"));
         mErrorCodes.put(new Integer(DMErrorCodes.NOT_IMPLEMENTED),
                         new String("401"));
         mErrorCodes.put(new Integer(DMErrorCodes.NOT_INITIALIZED),
                         new String("301"));
         mErrorCodes.put(new Integer(DMErrorCodes.READ_ONLY),
                         new String("403"));
         mErrorCodes.put(new Integer(DMErrorCodes.WRITE_ONLY),
                         new String("404"));
         mErrorCodes.put(new Integer(DMErrorCodes.TYPE_MISMATCH),
                         new String("405"));
         mErrorCodes.put(new Integer(DMErrorCodes.VALUE_OUT_OF_RANGE),
                         new String("405"));
         mErrorCodes.put(new Integer(DMErrorCodes.DEP_NOT_ESTABLISHED),
                         new String("405"));
         mErrorCodes.put(new Integer(DMErrorCodes.DOES_NOT_HAVE_CHILDREN),
                         new String("101"));
         mErrorCodes.put(new Integer(DMErrorCodes.DOES_NOT_HAVE_COUNT),
                         new String("101"));
         mErrorCodes.put(new Integer(DMErrorCodes.DOES_NOT_HAVE_VERSION),
                         new String("101"));
         mErrorCodes.put(new Integer(DMErrorCodes.SET_OUT_OF_ORDER),
                         new String("101"));
         mErrorCodes.put(new Integer(DMErrorCodes.OUT_OF_RANGE),
                         new String("101"));
         mErrorCodes.put(new Integer(DMErrorCodes.ELEMENT_NOT_SPECIFIED),
                         new String("101"));
         mErrorCodes.put(new Integer(DMErrorCodes.NOT_UNIQUE),
                         new String("101"));
         mErrorCodes.put(new Integer(DMErrorCodes.MAX_EXCEEDED),
                         new String("405"));
         mErrorCodes.put(new Integer(DMErrorCodes.SET_KEYWORD),
                         new String("402"));
         mErrorCodes.put(new Integer(DMErrorCodes.INVALID_REQUEST),
                         new String("401"));


         //  Initialize the SCORM Version 1.2 API Error Messages Hash Table
         mErrorMessages.put(new Integer(APIErrorCodes.NO_ERROR),
                            new String("No Error"));
         mErrorMessages.put(new Integer(APIErrorCodes.GENERAL_EXCEPTION),
                            new String("General Exception"));
         mErrorMessages.put(new Integer(APIErrorCodes.GENERAL_INIT_FAILURE),
                            new String("General Exception"));
         mErrorMessages.put(new Integer(APIErrorCodes.ALREADY_INITIALIZED),
                            new String("General Exception"));
         mErrorMessages.put(new Integer(
                                 APIErrorCodes.CONTENT_INSTANCE_TERMINATED),
                            new String("General Exception"));
         mErrorMessages.put(new Integer(
                                 APIErrorCodes.GENERAL_TERMINATION_FAILURE),
                            new String("General Exception"));
         mErrorMessages.put(new Integer(APIErrorCodes.TERMINATE_BEFORE_INIT),
                            new String("Not Initialized"));
         mErrorMessages.put(new Integer(
                                 APIErrorCodes.TERMINATE_AFTER_TERMINATE),
                            new String("General Exception"));
         mErrorMessages.put(new Integer(APIErrorCodes.GET_BEFORE_INIT),
                            new String("Not Initialized"));
         mErrorMessages.put(new Integer(APIErrorCodes.GET_AFTER_TERMINATE),
                            new String("General Exception"));
         mErrorMessages.put(new Integer(APIErrorCodes.SET_BEFORE_INIT),
                            new String("Not Initialized"));
         mErrorMessages.put(new Integer(APIErrorCodes.SET_AFTER_TERMINATE),
                            new String("General Exception"));
         mErrorMessages.put(new Integer(APIErrorCodes.COMMIT_BEFORE_INIT),
                            new String("Not Initialized"));
         mErrorMessages.put(new Integer(APIErrorCodes.COMMIT_AFTER_TERMINATE),
                            new String("General Exception"));
         mErrorMessages.put(new Integer(DMErrorCodes.GEN_ARGUMENT_ERROR),
                            new String("Invalid Argument Error"));
         mErrorMessages.put(new Integer(DMErrorCodes.GEN_GET_FAILURE),
                            new String("General Exception"));
         mErrorMessages.put(new Integer(DMErrorCodes.GEN_SET_FAILURE),
                            new String("General Exception"));
         mErrorMessages.put(new Integer(APIErrorCodes.GENERAL_COMMIT_FAILURE),
                            new String("General Exception"));
         mErrorMessages.put(new Integer(DMErrorCodes.UNDEFINED_ELEMENT),
                            new String("Not Implemented Error"));
         mErrorMessages.put(new Integer(DMErrorCodes.NOT_IMPLEMENTED),
                            new String("Not Implemented Error"));
         mErrorMessages.put(new Integer(DMErrorCodes.NOT_INITIALIZED),
                            new String("Not Initialized"));
         mErrorMessages.put(new Integer(DMErrorCodes.READ_ONLY),
                            new String("Element is Read Only"));
         mErrorMessages.put(new Integer(DMErrorCodes.WRITE_ONLY),
                            new String("Element is Write Only"));
         mErrorMessages.put(new Integer(DMErrorCodes.TYPE_MISMATCH),
                            new String("Incorrect Data Type"));
         mErrorMessages.put(new Integer(DMErrorCodes.VALUE_OUT_OF_RANGE),
                            new String("Incorrect Data Type"));
         mErrorMessages.put(new Integer(DMErrorCodes.DEP_NOT_ESTABLISHED),
                            new String("Incorrect Data Type"));
         mErrorMessages.put(new Integer(DMErrorCodes.DOES_NOT_HAVE_CHILDREN),
                            new String("General Exception"));
         mErrorMessages.put(new Integer(DMErrorCodes.DOES_NOT_HAVE_COUNT),
                            new String("General Exception"));
         mErrorMessages.put(new Integer(DMErrorCodes.DOES_NOT_HAVE_VERSION),
                            new String("General Exception"));
         mErrorMessages.put(new Integer(DMErrorCodes.SET_OUT_OF_ORDER),
                            new String("General Exception"));
         mErrorMessages.put(new Integer(DMErrorCodes.OUT_OF_RANGE),
                            new String("General Exception"));
         mErrorMessages.put(new Integer(DMErrorCodes.ELEMENT_NOT_SPECIFIED),
                            new String("General Exception"));
         mErrorMessages.put(new Integer(DMErrorCodes.NOT_UNIQUE),
                            new String("General Exception"));
         mErrorMessages.put(new Integer(DMErrorCodes.MAX_EXCEEDED),
                            new String("Incorrect Data Type"));
         mErrorMessages.put(new Integer(DMErrorCodes.SET_KEYWORD),
                            new String("Invalid Set Value.  Element is "+
                                       "a Keyword"));
         mErrorMessages.put(new Integer(DMErrorCodes.INVALID_REQUEST),
                            new String("Not Implemented Error"));

         //  Initialize the SCORM 1.2 API Error Diagnostics
         //  Diagnostics Hash Table
         mErrorDiagnostics.put(new Integer(APIErrorCodes.NO_ERROR),
                               new String("No Error"));
         mErrorDiagnostics.put(new Integer(APIErrorCodes.GENERAL_EXCEPTION),
                               new String("General Exception"));
         mErrorDiagnostics.put(new Integer(APIErrorCodes.GENERAL_INIT_FAILURE),
                               new String("General Initialization Error"));
         mErrorDiagnostics.put(new Integer(APIErrorCodes.ALREADY_INITIALIZED),
                               new String("Already Initialized"));
         mErrorDiagnostics.put(new Integer(
                               APIErrorCodes.CONTENT_INSTANCE_TERMINATED),
                               new String("Content Instance Terminated"));
         mErrorDiagnostics.put(new Integer(
                               APIErrorCodes.GENERAL_TERMINATION_FAILURE),
                               new String("General Termination Failure"));
         mErrorDiagnostics.put(new Integer(APIErrorCodes.TERMINATE_BEFORE_INIT),
                               new String("Termination Before Initialization"));
         mErrorDiagnostics.put(new Integer(
                               APIErrorCodes.TERMINATE_AFTER_TERMINATE),
                               new String("Termination After Termination"));
         mErrorDiagnostics.put(new Integer(APIErrorCodes.GET_BEFORE_INIT),
                               new String("Retrieve Data Before " +
                                          "Initialization"));
         mErrorDiagnostics.put(new Integer(APIErrorCodes.GET_AFTER_TERMINATE),
                               new String("Retrieve Data After Termination"));
         mErrorDiagnostics.put(new Integer(APIErrorCodes.SET_BEFORE_INIT),
                               new String("Store Data Before Initialization"));
         mErrorDiagnostics.put(new Integer(APIErrorCodes.SET_AFTER_TERMINATE),
                               new String("Store Data After Termination"));
         mErrorDiagnostics.put(new Integer(APIErrorCodes.COMMIT_BEFORE_INIT),
                               new String("Commit Before Initialization"));
         mErrorDiagnostics.put(new Integer(
                               APIErrorCodes.COMMIT_AFTER_TERMINATE),
                               new String("Commit After Termination"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.GEN_ARGUMENT_ERROR),
                               new String("General Argument Error"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.GEN_GET_FAILURE),
                               new String("General Get Failure"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.GEN_SET_FAILURE),
                               new String("General Set Failure"));
         mErrorDiagnostics.put(new Integer(
                               APIErrorCodes.GENERAL_COMMIT_FAILURE),
                               new String("General Commit Failure"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.UNDEFINED_ELEMENT),
                               new String("Undefined Data Model Element"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.NOT_IMPLEMENTED),
                               new String("Unimplemented Data Model Element"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.NOT_INITIALIZED),
                               new String("Data Model Element Value Not " +
                                        "Initialized"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.READ_ONLY),
                               new String("Data Model Element Is Read Only"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.WRITE_ONLY),
                               new String("Data Model Element Is Write Only"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.TYPE_MISMATCH),
                               new String("Data Model Element Type Mismatch"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.VALUE_OUT_OF_RANGE),
                               new String("Data Model Element Value Out Of " +
                                          "Range"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.DEP_NOT_ESTABLISHED),
                               new String("Data Model Dependency Not " +
                                          "Established"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.DOES_NOT_HAVE_CHILDREN),
                               new String("Data Model Element does not have " +
                                          "Children"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.DOES_NOT_HAVE_COUNT),
                               new String("Data Model Element does not have " +
                                          "Count"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.DOES_NOT_HAVE_VERSION),
                               new String("Data Model Element does not have " +
                                          "Version"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.SET_OUT_OF_ORDER),
                               new String("Data Model Array Set out of Order"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.OUT_OF_RANGE),
                               new String("Value Out of Range"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.ELEMENT_NOT_SPECIFIED),
                               new String("No Element Specified"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.NOT_UNIQUE),
                               new String("Value is not Unique"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.MAX_EXCEEDED),
                               new String("Error - Maximum Exceeded"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.SET_KEYWORD),
                               new String("Data Model Element Is a Keyword"));
         mErrorDiagnostics.put(new Integer(DMErrorCodes.INVALID_REQUEST),
                               new String("Request was Invalid"));

         //  Initialize the SCORM 1.2 API Error Codes
         mAbstErrors.put(new String("0"), new Integer(APIErrorCodes.NO_ERROR));
         mAbstErrors.put(new String("101"),
                         new Integer(APIErrorCodes.GENERAL_EXCEPTION));
         mAbstErrors.put(new String("201"),
                         new Integer(DMErrorCodes.GEN_ARGUMENT_ERROR));
         mAbstErrors.put(new String("202"),
                         new Integer(DMErrorCodes.DOES_NOT_HAVE_CHILDREN));
         mAbstErrors.put(new String("203"),
                         new Integer(DMErrorCodes.SET_OUT_OF_ORDER));
         mAbstErrors.put(new String("301"),
                         new Integer(DMErrorCodes.NOT_INITIALIZED));
         mAbstErrors.put(new String("401"),
                         new Integer(DMErrorCodes.NOT_IMPLEMENTED));
         mAbstErrors.put(new String("402"),
                         new Integer(DMErrorCodes.GEN_SET_FAILURE));
         mAbstErrors.put(new String("403"),
                         new Integer(DMErrorCodes.READ_ONLY));
         mAbstErrors.put(new String("404"),
                         new Integer(DMErrorCodes.WRITE_ONLY));
         mAbstErrors.put(new String("405"),
                         new Integer(DMErrorCodes.TYPE_MISMATCH));

      }
   }


   /**
    * Returns The current avaliable error code.
    *
    * @return The value of the current error code that was set by the most
    *         recent API call.
    */
   public String getCurrentErrorCode()
   {
      Integer errInt = new Integer(mCurrentErrorCode);
      String err = (String)mErrorCodes.get(errInt);

      if(err == null)
      {
         err = new String("0");
      }

      return err;
   }

   /**
    * Sets the error code (from the predefined list of codes).
    *
    * @param iCode  The error code being set.
    */
   public void setCurrentErrorCode(int iCode)
   {
      mCurrentErrorCode = iCode;
   }


   /**
    * Sets the current error code to No Error 
    * (<code>APIErrorCodes.NO_ERROR</code>)
    */
   public void clearCurrentErrorCode()
   {
      mCurrentErrorCode = APIErrorCodes.NO_ERROR;
   }

   /**
    * Returns the text associated with a given error code.
    *
    * @param iCode  The specified error code for which an error description
    *               is being requested.
    *
    * @return The text associated with the specfied error code.
    */
   public String getErrorDescription(String iCode)
   {
      String message = "";

      if ( (iCode != null) && (!iCode.equals("")) )
      {
         // Retrieves and returns the description of the provided error code
         Integer errInt = (Integer)mAbstErrors.get(iCode);

         if ( errInt != null )
         {
            message = (String)mErrorMessages.get(errInt);

            if(message == null)
            {
               message = "";
            }
         }
         else
         {
            message = "";
         }
      }

      return message;
   }

   /**
    * Returns the text associated with the current error code.
    *
    * @return The text associated with the specfied error code.
    */
   public String getErrorDescription()
   {
      // Retrieves and returns the description of the current error code
      Integer errInt = new Integer(mCurrentErrorCode);
      return (String)mErrorMessages.get(errInt);
   }

   /**
    * Returns the diagnostic text associated with an error code.
    *
    * @param iCode  The specified error code for which error diagnostic
    *               information is being requested.
    *
    * @return The diagnostic text associated with the specificed error code.
    */
   public String getErrorDiagnostic(String iCode)
   {
      String diagnostic = "";
      
      if ( (iCode != null) && (!iCode.equals("")) )
      {
         // Returns the diagnostic text of the provided error code
         Integer errInt =  (Integer)mAbstErrors.get(iCode);
         if(errInt != null)
         {
            String tempDiagnostic = (String)mErrorDiagnostics.get(errInt);

            if(tempDiagnostic != null)
            {
               diagnostic = tempDiagnostic;
            }
         }
      }
      else
      {
         // returns diagnostic text of previous error
         Integer errInt = new Integer(mCurrentErrorCode);
         String tempDiagnostic = (String)mErrorDiagnostics.get(errInt);
         
         if(tempDiagnostic != null)
         {
            diagnostic = tempDiagnostic;
         }
      }
      
      return diagnostic;
   }

   /**
    * Returns the diagnostic text associated with the current error code.
    *
    * @return The diagnostic text associated with the specificed error code.
    */
   public String getErrorDiagnostic()
   {
      // returns diagnostic text of previous error
      Integer errInt = new Integer(mCurrentErrorCode);
      return (String)mErrorDiagnostics.get(errInt);
   }

   /**
    * Determines whether or not the Error Code passed in 
    * (<code>iErrorCode</code>) is a valid and recognizable SCORM error code.
    *
    * @param iErrorCode The error code.
    * @return Indicates whether or not the error code is valid.
    */
   public boolean isValidErrorCode( String iErrorCode )
   {
      boolean result = false;
      Enumeration enumOfErrorCodes = mErrorCodes.elements();

      while ( !result && enumOfErrorCodes.hasMoreElements() )
      {
         String comp = (String)enumOfErrorCodes.nextElement();

         if ( comp.equals(iErrorCode) == true )

         {
            result = true;
         }
      }

      return result;
   }

} // APIErrorManager
