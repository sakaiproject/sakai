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

import java.util.Enumeration;
import java.util.Hashtable;

import org.adl.datamodels.DMErrorCodes;
import org.adl.datamodels.ssp.SSP_DMErrorCodes;

/**
 * This class implements the error handling capabilities of the RTE API.<br>
 * <br>
 * 
 * <strong>Filename:</strong> APIErrorManager<br>
 * <br>
 * 
 * <strong>Description:</strong><br>
 * This class manages the error codes set by the API methods in the API Adapter
 * Applet. <br>
 * <br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the SCORM Sample RTE 1.3. <br>
 * <br>
 * 
 * <strong>Implementation Issues:</strong><br>
 * For purposes of this example, this class uses a hardcoded array to store the
 * error mapping.<br>
 * <br>
 * 
 * <strong>Known Problems:</strong><br>
 * <br>
 * 
 * <strong>Side Effects:</strong><br>
 * <br>
 * 
 * <strong>References:</strong><br>
 * <ul>
 * <li>IMS SS Specification
 * <li>SCORM 2004
 * </ul>
 * 
 * @author ADL Technical Team
 */
public class APIErrorManager implements IErrorManager {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 5851228120434586106L;

	/**
	 * The abstract error code from the last API method invocation.
	 */
	private int mCurrentErrorCode = APIErrorCodes.NO_ERROR;

	/**
	 * Hashtable that holds all of the API Error Codes as Strings. The abstract
	 * error codes are used as the keys for the String error codes.
	 */
	private Hashtable<Integer, String> mErrorCodes;

	/**
	 * Hashtable that holds all of the API Error Messages. The abstract error
	 * codes are used as the keys for the error messages.
	 */
	private Hashtable<Integer, String> mErrorMessages;

	/**
	 * Hashtable that holds all of the API Error Diagnostics. The abstract error
	 * codes are used as the keys for the error diagnostics.
	 */
	private Hashtable<Integer, String> mErrorDiagnostics;

	/**
	 * Hashtable that converts an error string to the abstract integer. The
	 * String representations of the SCORM error codes are used as the keys.
	 */
	private Hashtable<String, Integer> mAbstErrors;

	/**
	 * Initializes this <code>mCurrentErrorCode</code> to 'No Error' and
	 * initializes the Hashtables based on the API version.
	 * 
	 * @param iAPIVersion
	 *            - The API version that will use this error manager.
	 */
	public APIErrorManager(int iAPIVersion) {

		mCurrentErrorCode = APIErrorCodes.NO_ERROR;
		// Intialize the Hashtable size - should be a prime number
		mErrorCodes = new Hashtable<>(211);
		mErrorMessages = new Hashtable<>(59);
		mErrorDiagnostics = new Hashtable<>(59);
		mAbstErrors = new Hashtable<>(59);

		if (iAPIVersion == SCORM_2004_API) {
			// Initialize the SCORM 2004 RTE API Error Codes Hash Table
			addErrorCode(APIErrorCodes.NO_ERROR, "0");
			addErrorCode(APIErrorCodes.GENERAL_EXCEPTION, "101");
			addErrorCode(APIErrorCodes.GENERAL_INIT_FAILURE, "102");
			addErrorCode(APIErrorCodes.ALREADY_INITIALIZED, "103");
			addErrorCode(APIErrorCodes.CONTENT_INSTANCE_TERMINATED, "104");
			addErrorCode(APIErrorCodes.GENERAL_TERMINATION_FAILURE, "111");
			addErrorCode(APIErrorCodes.TERMINATE_BEFORE_INIT, "112");
			addErrorCode(APIErrorCodes.TERMINATE_AFTER_TERMINATE, "113");
			addErrorCode(APIErrorCodes.GET_BEFORE_INIT, "122");
			addErrorCode(APIErrorCodes.GET_AFTER_TERMINATE, "123");
			addErrorCode(APIErrorCodes.SET_BEFORE_INIT, "132");
			addErrorCode(APIErrorCodes.SET_AFTER_TERMINATE, "133");
			addErrorCode(APIErrorCodes.COMMIT_BEFORE_INIT, "142");
			addErrorCode(APIErrorCodes.COMMIT_AFTER_TERMINATE, "143");
			addErrorCode(DMErrorCodes.GEN_ARGUMENT_ERROR, "201");
			addErrorCode(DMErrorCodes.GEN_GET_FAILURE, "301");
			addErrorCode(DMErrorCodes.GEN_SET_FAILURE, "351");
			addErrorCode(APIErrorCodes.GENERAL_COMMIT_FAILURE, "391");
			addErrorCode(DMErrorCodes.UNDEFINED_ELEMENT, "401");
			addErrorCode(DMErrorCodes.NOT_IMPLEMENTED, "402");
			addErrorCode(DMErrorCodes.NOT_INITIALIZED, "403");
			addErrorCode(DMErrorCodes.READ_ONLY, "404");
			addErrorCode(DMErrorCodes.WRITE_ONLY, "405");
			addErrorCode(DMErrorCodes.TYPE_MISMATCH, "406");
			addErrorCode(DMErrorCodes.VALUE_OUT_OF_RANGE, "407");
			addErrorCode(DMErrorCodes.DEP_NOT_ESTABLISHED, "408");
			addErrorCode(DMErrorCodes.DOES_NOT_HAVE_CHILDREN, "301");
			addErrorCode(DMErrorCodes.DOES_NOT_HAVE_COUNT, "301");
			addErrorCode(DMErrorCodes.DOES_NOT_HAVE_VERSION, "301");
			addErrorCode(DMErrorCodes.SET_OUT_OF_ORDER, "301");
			addErrorCode(DMErrorCodes.OUT_OF_RANGE, "301");
			addErrorCode(DMErrorCodes.ELEMENT_NOT_SPECIFIED, "351");
			addErrorCode(DMErrorCodes.NOT_UNIQUE, "351");
			addErrorCode(DMErrorCodes.MAX_EXCEEDED, "351");
			addErrorCode(DMErrorCodes.SET_KEYWORD, "404");
			addErrorCode(DMErrorCodes.INVALID_REQUEST, "401");
			addErrorCode(DMErrorCodes.INVALID_ARGUMENT, "301");
			addErrorCode(DMErrorCodes.OVERWRITE_ID, "351");

			// Initialize the SSP API Error Codes Hash Table
			addErrorCode(SSP_DMErrorCodes.NO_ERROR, "0");
			addErrorCode(SSP_DMErrorCodes.INVALID_SET_PARMS, "351");
			addErrorCode(SSP_DMErrorCodes.INVALID_GET_PARMS, "301");
			addErrorCode(SSP_DMErrorCodes.ALLOCATE_MIN_GREATER_MAX, "406");
			addErrorCode(SSP_DMErrorCodes.DATABASE_UPDATE_FAILURE, "406");
			addErrorCode(SSP_DMErrorCodes.TYPE_MISMATCH, "406");
			addErrorCode(SSP_DMErrorCodes.ALLOCATE_SPACE_EXCEEDED, "351");
			addErrorCode(SSP_DMErrorCodes.BUCKET_NOT_ALLOCATED_SET, "351");
			addErrorCode(SSP_DMErrorCodes.BUCKET_NOT_ALLOCATED_GET, "301");
			addErrorCode(SSP_DMErrorCodes.REQUESTED_SIZE_EXCEEDED_AVAIL, "301");
			addErrorCode(SSP_DMErrorCodes.BUCKET_SIZE_EXCEEDED_SET, "351");
			addErrorCode(SSP_DMErrorCodes.CREATE_BUCKET_PERSIST, "351");
			addErrorCode(SSP_DMErrorCodes.DATABASE_CREATE_FAILURE, "351");

			addErrorCode(SSP_DMErrorCodes.BUCKET_IMPROPERLY_DECLARED_SET, "351");
			addErrorCode(SSP_DMErrorCodes.BUCKET_IMPROPERLY_DECLARED_GET, "301");
			addErrorCode(SSP_DMErrorCodes.OFFSET_EXCEEDS_BUCKET_SIZE_SET, "351");
			addErrorCode(SSP_DMErrorCodes.OFFSET_EXCEEDS_BUCKET_SIZE_GET, "301");
			addErrorCode(SSP_DMErrorCodes.BUCKET_NOT_PACKED, "351");

			// Initialize the SCORM 2004 RTE API Error Messages Hash Table
			addErrorMessage(APIErrorCodes.NO_ERROR, "No Error");
			addErrorMessage(APIErrorCodes.GENERAL_EXCEPTION, "General Exception");
			addErrorMessage(APIErrorCodes.GENERAL_INIT_FAILURE, "General Initialization Error");
			addErrorMessage(APIErrorCodes.ALREADY_INITIALIZED, "Already Initialized");
			addErrorMessage(APIErrorCodes.CONTENT_INSTANCE_TERMINATED, "Content Instance Terminated");
			addErrorMessage(APIErrorCodes.GENERAL_TERMINATION_FAILURE, "General Termination Failure");
			addErrorMessage(APIErrorCodes.TERMINATE_BEFORE_INIT, "Termination Before Initialization");
			addErrorMessage(APIErrorCodes.TERMINATE_AFTER_TERMINATE, "Termination After Termination");
			addErrorMessage(APIErrorCodes.GET_BEFORE_INIT, "Retrieve Data Before Initialization");
			addErrorMessage(APIErrorCodes.GET_AFTER_TERMINATE, "Retrieve Data After Termination");
			addErrorMessage(APIErrorCodes.SET_BEFORE_INIT, "Store Data Before Initialization");
			addErrorMessage(APIErrorCodes.SET_AFTER_TERMINATE, "Store Data After Termination");
			addErrorMessage(APIErrorCodes.COMMIT_BEFORE_INIT, "Commit Before Initialization");
			addErrorMessage(APIErrorCodes.COMMIT_AFTER_TERMINATE, "Commit After Termination");
			addErrorMessage(DMErrorCodes.GEN_ARGUMENT_ERROR, "General Argument Error");
			addErrorMessage(DMErrorCodes.GEN_GET_FAILURE, "General Get Failure");
			addErrorMessage(DMErrorCodes.GEN_SET_FAILURE, "General Set Failure");
			addErrorMessage(APIErrorCodes.GENERAL_COMMIT_FAILURE, "General Commit Failure");
			addErrorMessage(DMErrorCodes.UNDEFINED_ELEMENT, "Undefined Data Model Element");
			addErrorMessage(DMErrorCodes.NOT_IMPLEMENTED, "Unimplemented Data Model Element");
			addErrorMessage(DMErrorCodes.NOT_INITIALIZED, "Data Model Element Value Not " + "Initialized");
			addErrorMessage(DMErrorCodes.READ_ONLY, "Data Model Element Is Read Only");
			addErrorMessage(DMErrorCodes.WRITE_ONLY, "Data Model Element Is Write Only");
			addErrorMessage(DMErrorCodes.TYPE_MISMATCH, "Data Model Element Type Mismatch");
			addErrorMessage(DMErrorCodes.VALUE_OUT_OF_RANGE, "Data Model Element Value Out Of " + "Range");
			addErrorMessage(DMErrorCodes.DEP_NOT_ESTABLISHED, "Data Model Dependency Not " + "Established");
			addErrorMessage(DMErrorCodes.DOES_NOT_HAVE_CHILDREN, "General Get Failure");
			addErrorMessage(DMErrorCodes.DOES_NOT_HAVE_COUNT, "General Get Failure");
			addErrorMessage(DMErrorCodes.DOES_NOT_HAVE_VERSION, "General Get Failure");
			addErrorMessage(DMErrorCodes.SET_OUT_OF_ORDER, "General Set Failure");
			addErrorMessage(DMErrorCodes.OUT_OF_RANGE, "General Get Failure");
			addErrorMessage(DMErrorCodes.ELEMENT_NOT_SPECIFIED, "General Get Failure");
			addErrorMessage(DMErrorCodes.NOT_UNIQUE, "General Set Failure");
			addErrorMessage(DMErrorCodes.MAX_EXCEEDED, "General Set Failure");
			addErrorMessage(DMErrorCodes.SET_KEYWORD, "Data Model Element Is Read Only");
			addErrorMessage(DMErrorCodes.INVALID_REQUEST, "Undefined Data Model Element");
			addErrorMessage(DMErrorCodes.INVALID_ARGUMENT, "General Get Failure");
			addErrorMessage(DMErrorCodes.OVERWRITE_ID, "General Set Failure");

			// Initialize the SSP API Error Messages Hash Table
			addErrorMessage(SSP_DMErrorCodes.NO_ERROR, "No Error");
			addErrorMessage(SSP_DMErrorCodes.INVALID_SET_PARMS, "General Set Failure");
			addErrorMessage(SSP_DMErrorCodes.INVALID_GET_PARMS, "General Get Failure");
			addErrorMessage(SSP_DMErrorCodes.ALLOCATE_MIN_GREATER_MAX, "Data Model Element Type Mismatch");
			addErrorMessage(SSP_DMErrorCodes.DATABASE_UPDATE_FAILURE, "General Get Failure");
			addErrorMessage(SSP_DMErrorCodes.TYPE_MISMATCH, "Type Mismatch");
			addErrorMessage(SSP_DMErrorCodes.ALLOCATE_SPACE_EXCEEDED, "General Set Failure");
			addErrorMessage(SSP_DMErrorCodes.BUCKET_NOT_ALLOCATED_SET, "General Set Failure");
			addErrorMessage(SSP_DMErrorCodes.BUCKET_NOT_ALLOCATED_GET, "General Get Failure");
			addErrorMessage(SSP_DMErrorCodes.REQUESTED_SIZE_EXCEEDED_AVAIL, "General Get Failure");
			addErrorMessage(SSP_DMErrorCodes.BUCKET_SIZE_EXCEEDED_SET, "General Set Failure");
			addErrorMessage(SSP_DMErrorCodes.CREATE_BUCKET_PERSIST, "General Set Failure");
			addErrorMessage(SSP_DMErrorCodes.DATABASE_CREATE_FAILURE, "General Set Failure");
			addErrorMessage(SSP_DMErrorCodes.BUCKET_IMPROPERLY_DECLARED_SET, "General Set Failure");
			addErrorMessage(SSP_DMErrorCodes.BUCKET_IMPROPERLY_DECLARED_GET, "General Get Failure");
			addErrorMessage(SSP_DMErrorCodes.OFFSET_EXCEEDS_BUCKET_SIZE_SET, "General Set Failure");
			addErrorMessage(SSP_DMErrorCodes.OFFSET_EXCEEDS_BUCKET_SIZE_GET, "General Get Failure");
			addErrorMessage(SSP_DMErrorCodes.BUCKET_NOT_PACKED, "General Set Failure");

			// Initialize the SCORM 2004 RTE API Error
			// Diagnostics Hash Table
			addErrorDiagnostics(APIErrorCodes.NO_ERROR, "No Error");
			addErrorDiagnostics(APIErrorCodes.GENERAL_EXCEPTION, "General Exception");
			addErrorDiagnostics(APIErrorCodes.GENERAL_INIT_FAILURE, "General Initialization Error");
			addErrorDiagnostics(APIErrorCodes.ALREADY_INITIALIZED, "Already Initialized");
			addErrorDiagnostics(APIErrorCodes.CONTENT_INSTANCE_TERMINATED, "Content Instance Terminated");
			addErrorDiagnostics(APIErrorCodes.GENERAL_TERMINATION_FAILURE, "General Termination Failure");
			addErrorDiagnostics(APIErrorCodes.TERMINATE_BEFORE_INIT, "Termination Before Initialization");
			addErrorDiagnostics(APIErrorCodes.TERMINATE_AFTER_TERMINATE, "Termination After Termination");
			addErrorDiagnostics(APIErrorCodes.GET_BEFORE_INIT, "Retrieve Data Before " + "Initialization");
			addErrorDiagnostics(APIErrorCodes.GET_AFTER_TERMINATE, "Retrieve Data After Termination");
			addErrorDiagnostics(APIErrorCodes.SET_BEFORE_INIT, "Store Data Before Initialization");
			addErrorDiagnostics(APIErrorCodes.SET_AFTER_TERMINATE, "Store Data After Termination");
			addErrorDiagnostics(APIErrorCodes.COMMIT_BEFORE_INIT, "Commit Before Initialization");
			addErrorDiagnostics(APIErrorCodes.COMMIT_AFTER_TERMINATE, "Commit After Termination");
			addErrorDiagnostics(DMErrorCodes.GEN_ARGUMENT_ERROR, "General Argument Error");
			addErrorDiagnostics(DMErrorCodes.GEN_GET_FAILURE, "General Get Failure");
			addErrorDiagnostics(DMErrorCodes.GEN_SET_FAILURE, "General Set Failure");
			addErrorDiagnostics(APIErrorCodes.GENERAL_COMMIT_FAILURE, "General Commit Failure");
			addErrorDiagnostics(DMErrorCodes.UNDEFINED_ELEMENT, "Undefined Data Model Element");
			addErrorDiagnostics(DMErrorCodes.NOT_IMPLEMENTED, "Unimplemented Data Model Element");
			addErrorDiagnostics(DMErrorCodes.NOT_INITIALIZED, "Data Model Element Value Not " + "Initialized");
			addErrorDiagnostics(DMErrorCodes.READ_ONLY, "Data Model Element Is Read Only");
			addErrorDiagnostics(DMErrorCodes.WRITE_ONLY, "Data Model Element Is Write Only");
			addErrorDiagnostics(DMErrorCodes.TYPE_MISMATCH, "Data Model Element Type Mismatch");
			addErrorDiagnostics(DMErrorCodes.VALUE_OUT_OF_RANGE, "Data Model Element Value Out Of " + "Range");
			addErrorDiagnostics(DMErrorCodes.DEP_NOT_ESTABLISHED, "Data Model Dependency Not " + "Established");
			addErrorDiagnostics(DMErrorCodes.DOES_NOT_HAVE_CHILDREN, "Data Model Element does not have " + "Children");
			addErrorDiagnostics(DMErrorCodes.DOES_NOT_HAVE_COUNT, "Data Model Element does not have " + "Count");
			addErrorDiagnostics(DMErrorCodes.DOES_NOT_HAVE_VERSION, "Data Model Element does not have " + "Version");
			addErrorDiagnostics(DMErrorCodes.SET_OUT_OF_ORDER, "Data Model Array Set out of Order");
			addErrorDiagnostics(DMErrorCodes.OUT_OF_RANGE, "Value Out of Range");
			addErrorDiagnostics(DMErrorCodes.ELEMENT_NOT_SPECIFIED, "No Element Specified");
			addErrorDiagnostics(DMErrorCodes.NOT_UNIQUE, "Value is not Unique");
			addErrorDiagnostics(DMErrorCodes.MAX_EXCEEDED, "Error - Maximum Exceeded");
			addErrorDiagnostics(DMErrorCodes.SET_KEYWORD, "Data Model Element Is a Keyword");
			addErrorDiagnostics(DMErrorCodes.INVALID_REQUEST, "Request was Invalid");
			addErrorDiagnostics(DMErrorCodes.INVALID_ARGUMENT, "Invalid Argument Error");
			addErrorDiagnostics(DMErrorCodes.OVERWRITE_ID, "Attempt to overwrite Objective ID");

			// Initialize the SSP API Error Diagnostics Hash Table
			addErrorDiagnostics(SSP_DMErrorCodes.NO_ERROR, "No Error");
			addErrorDiagnostics(SSP_DMErrorCodes.INVALID_SET_PARMS, "Invalid SSP parameters for set " + "operation");
			addErrorDiagnostics(SSP_DMErrorCodes.INVALID_GET_PARMS, "Invalid SSP parameters for get " + "operation");
			addErrorDiagnostics(SSP_DMErrorCodes.ALLOCATE_MIN_GREATER_MAX, "Data Model Element Type Mismatch - " + "minimum size greater than requested "
			        + "size");
			addErrorDiagnostics(SSP_DMErrorCodes.DATABASE_UPDATE_FAILURE, "SSP database update failure");
			addErrorDiagnostics(SSP_DMErrorCodes.TYPE_MISMATCH, "Data model element type mismatch");
			addErrorDiagnostics(SSP_DMErrorCodes.ALLOCATE_SPACE_EXCEEDED, "Bucket not allocated for ssp set " + "operation");
			addErrorDiagnostics(SSP_DMErrorCodes.BUCKET_NOT_ALLOCATED_SET, "Bucket not allocated for ssp set " + "operation");
			addErrorDiagnostics(SSP_DMErrorCodes.BUCKET_NOT_ALLOCATED_GET, "Bucket not allocated for ssp get " + "operation");
			addErrorDiagnostics(SSP_DMErrorCodes.REQUESTED_SIZE_EXCEEDED_AVAIL, "Requested data exceeds available data");
			addErrorDiagnostics(SSP_DMErrorCodes.BUCKET_SIZE_EXCEEDED_SET, "Bucket size exceeded for ssp set " + "operation");
			addErrorDiagnostics(SSP_DMErrorCodes.CREATE_BUCKET_PERSIST, "Bucket not able to be persisted");
			addErrorDiagnostics(SSP_DMErrorCodes.DATABASE_CREATE_FAILURE, "SSP database create failure");
			addErrorDiagnostics(SSP_DMErrorCodes.BUCKET_IMPROPERLY_DECLARED_SET, "The requested bucket was improperly " + "declared for ssp set operation");
			addErrorDiagnostics(SSP_DMErrorCodes.BUCKET_IMPROPERLY_DECLARED_GET, "The requested bucket was improperly " + "declared for ssp get operation");
			addErrorDiagnostics(SSP_DMErrorCodes.OFFSET_EXCEEDS_BUCKET_SIZE_SET, "The offset exceeds the bucket size for " + "ssp set operation");
			addErrorDiagnostics(SSP_DMErrorCodes.OFFSET_EXCEEDS_BUCKET_SIZE_GET, "The offset exceeds the bucket size for " + "ssp get operation");
			addErrorDiagnostics(SSP_DMErrorCodes.BUCKET_NOT_PACKED, "The bucket was not packed");

			// Hash Table used to get the abstract error code from an error
			// String
			addAbstErrors("0", APIErrorCodes.NO_ERROR);
			addAbstErrors("101", APIErrorCodes.GENERAL_EXCEPTION);
			addAbstErrors("102", APIErrorCodes.GENERAL_INIT_FAILURE);
			addAbstErrors("103", APIErrorCodes.ALREADY_INITIALIZED);
			addAbstErrors("104", APIErrorCodes.CONTENT_INSTANCE_TERMINATED);
			addAbstErrors("111", APIErrorCodes.GENERAL_TERMINATION_FAILURE);
			addAbstErrors("112", APIErrorCodes.TERMINATE_BEFORE_INIT);
			addAbstErrors("113", APIErrorCodes.TERMINATE_AFTER_TERMINATE);
			addAbstErrors("122", APIErrorCodes.GET_BEFORE_INIT);
			addAbstErrors("123", APIErrorCodes.GET_AFTER_TERMINATE);
			addAbstErrors("132", APIErrorCodes.SET_BEFORE_INIT);
			addAbstErrors("133", APIErrorCodes.SET_AFTER_TERMINATE);
			addAbstErrors("142", APIErrorCodes.COMMIT_BEFORE_INIT);
			addAbstErrors("143", APIErrorCodes.COMMIT_AFTER_TERMINATE);
			addAbstErrors("201", DMErrorCodes.GEN_ARGUMENT_ERROR);
			addAbstErrors("301", DMErrorCodes.GEN_GET_FAILURE);
			addAbstErrors("351", DMErrorCodes.GEN_SET_FAILURE);
			addAbstErrors("391", APIErrorCodes.GENERAL_COMMIT_FAILURE);
			addAbstErrors("401", DMErrorCodes.UNDEFINED_ELEMENT);
			addAbstErrors("402", DMErrorCodes.NOT_IMPLEMENTED);
			addAbstErrors("403", DMErrorCodes.NOT_INITIALIZED);
			addAbstErrors("404", DMErrorCodes.READ_ONLY);
			addAbstErrors("405", DMErrorCodes.WRITE_ONLY);
			addAbstErrors("406", DMErrorCodes.TYPE_MISMATCH);
			addAbstErrors("407", DMErrorCodes.VALUE_OUT_OF_RANGE);
			addAbstErrors("408", DMErrorCodes.DEP_NOT_ESTABLISHED);

			// SSP abstract error mapping
			addAbstErrors("0", APIErrorCodes.NO_ERROR);
			addAbstErrors("10000", SSP_DMErrorCodes.INVALID_SET_PARMS);
			addAbstErrors("10001", SSP_DMErrorCodes.INVALID_GET_PARMS);
			addAbstErrors("10002", SSP_DMErrorCodes.ALLOCATE_MIN_GREATER_MAX);
			addAbstErrors("10003", SSP_DMErrorCodes.DATABASE_UPDATE_FAILURE);
			addAbstErrors("10004", SSP_DMErrorCodes.TYPE_MISMATCH);
			addAbstErrors("10005", SSP_DMErrorCodes.ALLOCATE_SPACE_EXCEEDED);
			addAbstErrors("10006", SSP_DMErrorCodes.BUCKET_NOT_ALLOCATED_SET);
			addAbstErrors("10007", SSP_DMErrorCodes.BUCKET_NOT_ALLOCATED_GET);
			addAbstErrors("10008", SSP_DMErrorCodes.REQUESTED_SIZE_EXCEEDED_AVAIL);
			addAbstErrors("10009", SSP_DMErrorCodes.BUCKET_SIZE_EXCEEDED_SET);
			addAbstErrors("10010", SSP_DMErrorCodes.CREATE_BUCKET_PERSIST);
			addAbstErrors("10011", SSP_DMErrorCodes.DATABASE_CREATE_FAILURE);
			addAbstErrors("10012", SSP_DMErrorCodes.BUCKET_IMPROPERLY_DECLARED_SET);
			addAbstErrors("10013", SSP_DMErrorCodes.BUCKET_IMPROPERLY_DECLARED_GET);
			addAbstErrors("10014", SSP_DMErrorCodes.OFFSET_EXCEEDS_BUCKET_SIZE_SET);
			addAbstErrors("10015", SSP_DMErrorCodes.OFFSET_EXCEEDS_BUCKET_SIZE_GET);
			addAbstErrors("10016", SSP_DMErrorCodes.BUCKET_NOT_PACKED);
		} else if (iAPIVersion == SCORM_1_2_API) {
			// Initialize the SCORM Version 1.2 API Error Codes Hash Table
			addErrorCode(APIErrorCodes.NO_ERROR, "0");
			addErrorCode(APIErrorCodes.GENERAL_EXCEPTION, "101");
			addErrorCode(APIErrorCodes.GENERAL_INIT_FAILURE, "101");
			addErrorCode(APIErrorCodes.ALREADY_INITIALIZED, "101");
			addErrorCode(APIErrorCodes.CONTENT_INSTANCE_TERMINATED, "101");
			addErrorCode(APIErrorCodes.GENERAL_TERMINATION_FAILURE, "101");
			addErrorCode(APIErrorCodes.TERMINATE_BEFORE_INIT, "301");
			addErrorCode(APIErrorCodes.TERMINATE_AFTER_TERMINATE, "101");
			addErrorCode(APIErrorCodes.GET_BEFORE_INIT, "301");
			addErrorCode(APIErrorCodes.GET_AFTER_TERMINATE, "101");
			addErrorCode(APIErrorCodes.SET_BEFORE_INIT, "301");
			addErrorCode(APIErrorCodes.SET_AFTER_TERMINATE, "101");
			addErrorCode(APIErrorCodes.COMMIT_BEFORE_INIT, "301");
			addErrorCode(APIErrorCodes.COMMIT_AFTER_TERMINATE, "101");
			addErrorCode(DMErrorCodes.GEN_ARGUMENT_ERROR, "201");
			addErrorCode(DMErrorCodes.GEN_GET_FAILURE, "101");
			addErrorCode(DMErrorCodes.GEN_SET_FAILURE, "101");
			addErrorCode(APIErrorCodes.GENERAL_COMMIT_FAILURE, "101");
			addErrorCode(DMErrorCodes.UNDEFINED_ELEMENT, "401");
			addErrorCode(DMErrorCodes.NOT_IMPLEMENTED, "401");
			addErrorCode(DMErrorCodes.NOT_INITIALIZED, "301");
			addErrorCode(DMErrorCodes.READ_ONLY, "403");
			addErrorCode(DMErrorCodes.WRITE_ONLY, "404");
			addErrorCode(DMErrorCodes.TYPE_MISMATCH, "405");
			addErrorCode(DMErrorCodes.VALUE_OUT_OF_RANGE, "405");
			addErrorCode(DMErrorCodes.DEP_NOT_ESTABLISHED, "405");
			addErrorCode(DMErrorCodes.DOES_NOT_HAVE_CHILDREN, "101");
			addErrorCode(DMErrorCodes.DOES_NOT_HAVE_COUNT, "101");
			addErrorCode(DMErrorCodes.DOES_NOT_HAVE_VERSION, "101");
			addErrorCode(DMErrorCodes.SET_OUT_OF_ORDER, "101");
			addErrorCode(DMErrorCodes.OUT_OF_RANGE, "101");
			addErrorCode(DMErrorCodes.ELEMENT_NOT_SPECIFIED, "101");
			addErrorCode(DMErrorCodes.NOT_UNIQUE, "101");
			addErrorCode(DMErrorCodes.MAX_EXCEEDED, "405");
			addErrorCode(DMErrorCodes.SET_KEYWORD, "402");
			addErrorCode(DMErrorCodes.INVALID_REQUEST, "401");

			// Initialize the SCORM Version 1.2 API Error Messages Hash Table
			addErrorMessage(APIErrorCodes.NO_ERROR, "No Error");
			addErrorMessage(APIErrorCodes.GENERAL_EXCEPTION, "General Exception");
			addErrorMessage(APIErrorCodes.GENERAL_INIT_FAILURE, "General Exception");
			addErrorMessage(APIErrorCodes.ALREADY_INITIALIZED, "General Exception");
			addErrorMessage(APIErrorCodes.CONTENT_INSTANCE_TERMINATED, "General Exception");
			addErrorMessage(APIErrorCodes.GENERAL_TERMINATION_FAILURE, "General Exception");
			addErrorMessage(APIErrorCodes.TERMINATE_BEFORE_INIT, "Not Initialized");
			addErrorMessage(APIErrorCodes.TERMINATE_AFTER_TERMINATE, "General Exception");
			addErrorMessage(APIErrorCodes.GET_BEFORE_INIT, "Not Initialized");
			addErrorMessage(APIErrorCodes.GET_AFTER_TERMINATE, "General Exception");
			addErrorMessage(APIErrorCodes.SET_BEFORE_INIT, "Not Initialized");
			addErrorMessage(APIErrorCodes.SET_AFTER_TERMINATE, "General Exception");
			addErrorMessage(APIErrorCodes.COMMIT_BEFORE_INIT, "Not Initialized");
			addErrorMessage(APIErrorCodes.COMMIT_AFTER_TERMINATE, "General Exception");
			addErrorMessage(DMErrorCodes.GEN_ARGUMENT_ERROR, "Invalid Argument Error");
			addErrorMessage(DMErrorCodes.GEN_GET_FAILURE, "General Exception");
			addErrorMessage(DMErrorCodes.GEN_SET_FAILURE, "General Exception");
			addErrorMessage(APIErrorCodes.GENERAL_COMMIT_FAILURE, "General Exception");
			addErrorMessage(DMErrorCodes.UNDEFINED_ELEMENT, "Not Implemented Error");
			addErrorMessage(DMErrorCodes.NOT_IMPLEMENTED, "Not Implemented Error");
			addErrorMessage(DMErrorCodes.NOT_INITIALIZED, "Not Initialized");
			addErrorMessage(DMErrorCodes.READ_ONLY, "Element is Read Only");
			addErrorMessage(DMErrorCodes.WRITE_ONLY, "Element is Write Only");
			addErrorMessage(DMErrorCodes.TYPE_MISMATCH, "Incorrect Data Type");
			addErrorMessage(DMErrorCodes.VALUE_OUT_OF_RANGE, "Incorrect Data Type");
			addErrorMessage(DMErrorCodes.DEP_NOT_ESTABLISHED, "Incorrect Data Type");
			addErrorMessage(DMErrorCodes.DOES_NOT_HAVE_CHILDREN, "General Exception");
			addErrorMessage(DMErrorCodes.DOES_NOT_HAVE_COUNT, "General Exception");
			addErrorMessage(DMErrorCodes.DOES_NOT_HAVE_VERSION, "General Exception");
			addErrorMessage(DMErrorCodes.SET_OUT_OF_ORDER, "General Exception");
			addErrorMessage(DMErrorCodes.OUT_OF_RANGE, "General Exception");
			addErrorMessage(DMErrorCodes.ELEMENT_NOT_SPECIFIED, "General Exception");
			addErrorMessage(DMErrorCodes.NOT_UNIQUE, "General Exception");
			addErrorMessage(DMErrorCodes.MAX_EXCEEDED, "Incorrect Data Type");
			addErrorMessage(DMErrorCodes.SET_KEYWORD, "Invalid Set Value.  Element is " + "a Keyword");
			addErrorMessage(DMErrorCodes.INVALID_REQUEST, "Not Implemented Error");

			// Initialize the SCORM 1.2 API Error Diagnostics
			// Diagnostics Hash Table
			addErrorDiagnostics(APIErrorCodes.NO_ERROR, "No Error");
			addErrorDiagnostics(APIErrorCodes.GENERAL_EXCEPTION, "General Exception");
			addErrorDiagnostics(APIErrorCodes.GENERAL_INIT_FAILURE, "General Initialization Error");
			addErrorDiagnostics(APIErrorCodes.ALREADY_INITIALIZED, "Already Initialized");
			addErrorDiagnostics(APIErrorCodes.CONTENT_INSTANCE_TERMINATED, "Content Instance Terminated");
			addErrorDiagnostics(APIErrorCodes.GENERAL_TERMINATION_FAILURE, "General Termination Failure");
			addErrorDiagnostics(APIErrorCodes.TERMINATE_BEFORE_INIT, "Termination Before Initialization");
			addErrorDiagnostics(APIErrorCodes.TERMINATE_AFTER_TERMINATE, "Termination After Termination");
			addErrorDiagnostics(APIErrorCodes.GET_BEFORE_INIT, "Retrieve Data Before " + "Initialization");
			addErrorDiagnostics(APIErrorCodes.GET_AFTER_TERMINATE, "Retrieve Data After Termination");
			addErrorDiagnostics(APIErrorCodes.SET_BEFORE_INIT, "Store Data Before Initialization");
			addErrorDiagnostics(APIErrorCodes.SET_AFTER_TERMINATE, "Store Data After Termination");
			addErrorDiagnostics(APIErrorCodes.COMMIT_BEFORE_INIT, "Commit Before Initialization");
			addErrorDiagnostics(APIErrorCodes.COMMIT_AFTER_TERMINATE, "Commit After Termination");
			addErrorDiagnostics(DMErrorCodes.GEN_ARGUMENT_ERROR, "General Argument Error");
			addErrorDiagnostics(DMErrorCodes.GEN_GET_FAILURE, "General Get Failure");
			addErrorDiagnostics(DMErrorCodes.GEN_SET_FAILURE, "General Set Failure");
			addErrorDiagnostics(APIErrorCodes.GENERAL_COMMIT_FAILURE, "General Commit Failure");
			addErrorDiagnostics(DMErrorCodes.UNDEFINED_ELEMENT, "Undefined Data Model Element");
			addErrorDiagnostics(DMErrorCodes.NOT_IMPLEMENTED, "Unimplemented Data Model Element");
			addErrorDiagnostics(DMErrorCodes.NOT_INITIALIZED, "Data Model Element Value Not " + "Initialized");
			addErrorDiagnostics(DMErrorCodes.READ_ONLY, "Data Model Element Is Read Only");
			addErrorDiagnostics(DMErrorCodes.WRITE_ONLY, "Data Model Element Is Write Only");
			addErrorDiagnostics(DMErrorCodes.TYPE_MISMATCH, "Data Model Element Type Mismatch");
			addErrorDiagnostics(DMErrorCodes.VALUE_OUT_OF_RANGE, "Data Model Element Value Out Of " + "Range");
			addErrorDiagnostics(DMErrorCodes.DEP_NOT_ESTABLISHED, "Data Model Dependency Not " + "Established");
			addErrorDiagnostics(DMErrorCodes.DOES_NOT_HAVE_CHILDREN, "Data Model Element does not have " + "Children");
			addErrorDiagnostics(DMErrorCodes.DOES_NOT_HAVE_COUNT, "Data Model Element does not have " + "Count");
			addErrorDiagnostics(DMErrorCodes.DOES_NOT_HAVE_VERSION, "Data Model Element does not have " + "Version");
			addErrorDiagnostics(DMErrorCodes.SET_OUT_OF_ORDER, "Data Model Array Set out of Order");
			addErrorDiagnostics(DMErrorCodes.OUT_OF_RANGE, "Value Out of Range");
			addErrorDiagnostics(DMErrorCodes.ELEMENT_NOT_SPECIFIED, "No Element Specified");
			addErrorDiagnostics(DMErrorCodes.NOT_UNIQUE, "Value is not Unique");
			addErrorDiagnostics(DMErrorCodes.MAX_EXCEEDED, "Error - Maximum Exceeded");
			addErrorDiagnostics(DMErrorCodes.SET_KEYWORD, "Data Model Element Is a Keyword");
			addErrorDiagnostics(DMErrorCodes.INVALID_REQUEST, "Request was Invalid");

			// Initialize the SCORM 1.2 API Error Codes
			addAbstErrors("0", APIErrorCodes.NO_ERROR);
			addAbstErrors("101", APIErrorCodes.GENERAL_EXCEPTION);
			addAbstErrors("201", DMErrorCodes.GEN_ARGUMENT_ERROR);
			addAbstErrors("202", DMErrorCodes.DOES_NOT_HAVE_CHILDREN);
			addAbstErrors("203", DMErrorCodes.SET_OUT_OF_ORDER);
			addAbstErrors("301", DMErrorCodes.NOT_INITIALIZED);
			addAbstErrors("401", DMErrorCodes.NOT_IMPLEMENTED);
			addAbstErrors("402", DMErrorCodes.GEN_SET_FAILURE);
			addAbstErrors("403", DMErrorCodes.READ_ONLY);
			addAbstErrors("404", DMErrorCodes.WRITE_ONLY);
			addAbstErrors("405", DMErrorCodes.TYPE_MISMATCH);

		}
	}

	private void addAbstErrors(String apiCode, int internalCode) {
		mAbstErrors.put(apiCode, internalCode);
	}

	private void addErrorCode(int internalCode, String apiCode) {
		mErrorCodes.put(internalCode, apiCode);
	}

	private void addErrorDiagnostics(int internalCode, String apiCode) {
		mErrorDiagnostics.put(internalCode, apiCode);
	}

	private void addErrorMessage(int internalCode, String apiCode) {
		mErrorMessages.put(internalCode, apiCode);
	}

	/**
	 * Sets the current error code to No Error (
	 * <code>APIErrorCodes.NO_ERROR</code>)
	 */
	@Override
	public void clearCurrentErrorCode() {
		mCurrentErrorCode = APIErrorCodes.NO_ERROR;
	}

	/**
	 * Returns The current avaliable error code.
	 * 
	 * @return The value of the current error code that was set by the most
	 *         recent API call.
	 */
	@Override
	public String getCurrentErrorCode() {
		Integer errInt = mCurrentErrorCode;
		String err = mErrorCodes.get(errInt);

		if (err == null) {
			err = "0";
		}

		return err;
	}

	/**
	 * Returns the text associated with the current error code.
	 * 
	 * @return The text associated with the specfied error code.
	 */
	@Override
	public String getErrorDescription() {
		// Retrieves and returns the description of the current error code
		Integer errInt = mCurrentErrorCode;
		return mErrorMessages.get(errInt);
	}

	/**
	 * Returns the text associated with a given error code.
	 * 
	 * @param iCode
	 *            The specified error code for which an error description is
	 *            being requested.
	 * 
	 * @return The text associated with the specfied error code.
	 */
	@Override
	public String getErrorDescription(String iCode) {
		String message = "";

		if ((iCode != null) && (!iCode.isEmpty())) {
			// Retrieves and returns the description of the provided error code
			Integer errInt = mAbstErrors.get(iCode);

			if (errInt != null) {
				message = mErrorMessages.get(errInt);

				if (message == null) {
					message = "";
				}
			} else {
				message = "";
			}
		}

		return message;
	}

	/**
	 * Returns the diagnostic text associated with the current error code.
	 * 
	 * @return The diagnostic text associated with the specificed error code.
	 */
	@Override
	public String getErrorDiagnostic() {
		// returns diagnostic text of previous error
		Integer errInt = mCurrentErrorCode;
		return mErrorDiagnostics.get(errInt);
	}

	/**
	 * Returns the diagnostic text associated with an error code.
	 * 
	 * @param iCode
	 *            The specified error code for which error diagnostic
	 *            information is being requested.
	 * 
	 * @return The diagnostic text associated with the specificed error code.
	 */
	@Override
	public String getErrorDiagnostic(String iCode) {
		String diagnostic = "";

		if ((iCode != null) && (!iCode.isEmpty())) {
			// Returns the diagnostic text of the provided error code
			Integer errInt = mAbstErrors.get(iCode);
			if (errInt != null) {
				String tempDiagnostic = mErrorDiagnostics.get(errInt);

				if (tempDiagnostic != null) {
					diagnostic = tempDiagnostic;
				}
			}
		} else {
			// returns diagnostic text of previous error
			Integer errInt = mCurrentErrorCode;
			String tempDiagnostic = mErrorDiagnostics.get(errInt);

			if (tempDiagnostic != null) {
				diagnostic = tempDiagnostic;
			}
		}

		return diagnostic;
	}

	/**
	 * Determines whether or not the Error Code passed in (
	 * <code>iErrorCode</code>) is a valid and recognizable SCORM error code.
	 * 
	 * @param iErrorCode
	 *            The error code.
	 * @return Indicates whether or not the error code is valid.
	 */
	@Override
	public boolean isValidErrorCode(String iErrorCode) {
		boolean result = false;
		Enumeration<String> enumOfErrorCodes = mErrorCodes.elements();

		while (!result && enumOfErrorCodes.hasMoreElements()) {
			String comp = enumOfErrorCodes.nextElement();

			if (comp.equals(iErrorCode) == true)

			{
				result = true;
			}
		}

		return result;
	}

	/**
	 * Sets the error code (from the predefined list of codes).
	 * 
	 * @param iCode
	 *            The error code being set.
	 */
	@Override
	public void setCurrentErrorCode(int iCode) {
		mCurrentErrorCode = iCode;
	}

} // APIErrorManager
