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

package org.adl.datamodels;

import java.util.NoSuchElementException;

import org.adl.datamodels.ieee.IValidatorFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <strong>Filename:</strong>DMInterface.java<br><br>
 *
 * <strong>Description:</strong><br>
 *
 * <strong>Design Issues:</strong> None <br><br>
 *
 * <strong>Implementation Issues:</strong> None <br><br>
 *
 * <strong>Known Problems:</strong> None <br><br>
 *
 * <strong>Side Effects:</strong> None <br><br>
 *
 * <strong>References:</strong><br>
 * <ul>
 *     <li>SCORM 1.3</li>
 * </ul>
 *
 * @author ADL Technical Team
 */
public class DMInterface {

	private static Log log = LogFactory.getLog(DMInterface.class);

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	
	Public Methods
	
	-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

	/**
	 * Processes an equals() against a known set of SCO run-time data
	 * (<code>SCODataManager</code>.
	 * 
	 * @param iRequest  A dot-notation binding of the desired data model element.
	 * 
	 * @param iValue    Indicates the value that will be compared.
	 * 
	 * @param ioSCOData An instance of the <code>SCODataManager</code> that
	 *                  contains the run-time data for the individual SCO.
	 * 
	 * @return An abstract data model error code indicating the result of this
	 *         operation.
	 */
	public static int processEquals(String iRequest, String iValue, IDataManager ioSCOData) {
		// Assume no processing errors
		int result = DMErrorCodes.NO_ERROR;

		DMRequest request = null;

		if (iRequest != null) {
			// Attempt to create a DMRequest using the provided value
			try {
				request = new DMRequest(iRequest, iValue, false);

				// Process the Equals() request
				result = ioSCOData.equals(request);
			} catch (NullPointerException npe) {
				result = DMErrorCodes.INVALID_REQUEST;
				log.debug("NullPointerException in processEquals -> result is INVALID_REQUEST.", npe);
			} catch (NumberFormatException nfe) {
				result = DMErrorCodes.INVALID_REQUEST;
				log.debug("NumberFormatException in processEquals -> result is INVALID_REQUEST.", nfe);
			} catch (NoSuchElementException nee) {
				result = DMErrorCodes.INVALID_REQUEST;
				log.debug("NoSuchElementException in processEquals -> result is INVALID_REQUEST.", nee);
			}
		} else {
			result = DMErrorCodes.ELEMENT_NOT_SPECIFIED;
		}

		return result;
	}

	/**
	 * Processes a GetValue() against a known set of SCO run-time data
	 * (<code>SCODataManager</code>.
	 * 
	 * @param iRequest       A dot-notation binding of the desired data model
	 *                       element.
	 * 
	 * @param iAdmin         Indicates if this GetValue is an admin acttion
	 * 
	 * @param iDefDelimiters Indicates if the value returned should include
	 *                       default delimiters.
	 * 
	 * @param ioSCOData      An instance of the <code>SCODataManager</code> that
	 *                       contains the run-time data for the individual SCO.
	 * 
	 * @param oInfo          Provides the value of this data model element.
	 *                       <b>Note: The caller of this function must provide an
	 *                       initialized (new) <code>DMProcessingInfo</code> to
	 *                       hold the return value.</b>
	 * 
	 * @return An abstract data model error code indicating the result of this
	 *         operation.
	 */
	public static int processGetValue(String iRequest, boolean iAdmin, boolean iDefDelimiters, IDataManager ioSCOData, DMProcessingInfo oInfo) {
		// Assume no processing errors
		int result = DMErrorCodes.NO_ERROR;

		DMRequest request = null;

		if (iRequest != null && !iRequest.isEmpty()) {
			// Attempt to create a DMRequest using the provided value
			try {
				request = new DMRequest(iRequest, iAdmin, iDefDelimiters);

				// Process the GetValue() request
				result = ioSCOData.getValue(request, oInfo);
			} catch (NullPointerException npe) {
				result = DMErrorCodes.INVALID_REQUEST;
				log.debug("NullPointerException in processGetValue -> result is INVALID_REQUEST.", npe);
			} catch (NumberFormatException nfe) {
				result = DMErrorCodes.INVALID_REQUEST;
				log.debug("NumberFormatException in processGetValue -> result is INVALID_REQUEST.", nfe);
			}
		} else {
			result = DMErrorCodes.ELEMENT_NOT_SPECIFIED;
		}

		return result;
	}

	/**
	 * Processes a GetValue() against a known set of SCO run-time data
	 * (<code>SCODataManager</code>.
	 * 
	 * @param iRequest       A dot-notation binding of the desired data model
	 *                       element.
	 * 
	 * @param iDefDelimiters Indicates if the value returned should include
	 *                       default delimiters.
	 * 
	 * @param ioSCOData      An instance of the <code>SCODataManager</code> that
	 *                       contains the run-time data for the individual SCO.
	 * 
	 * @param oInfo          Provides the value of this data model element.
	 *                       <b>Note: The caller of this function must provide an
	 *                       initialized (new) <code>DMProcessingInfo</code> to
	 *                       hold the return value.</b>
	 * 
	 * @return An abstract data model error code indicating the result of this
	 *         operation.
	 */
	public static int processGetValue(String iRequest, boolean iDefDelimiters, IDataManager ioSCOData, DMProcessingInfo oInfo) {
		// Delegate non-admin calls
		return processGetValue(iRequest, false, iDefDelimiters, ioSCOData, oInfo);
	}

	/**
	 * Processes a SetValue() against a known set of SCO run-time data
	 * (<code>SCODataManager</code>.
	 * 
	 * @param iRequest  A dot-notation binding of the desired data model element.
	 * @param iValue    Indicates the value that will be set.
	 * @param iAdmin    Indicates if this SetValue is an administrative action.
	 * @param ioSCOData An instance of the <code>SCODataManager</code> that
	 *                  contains the run-time data for the individual SCO.
	 * @param validatorFactory TODO
	 * 
	 * @return An abstract data model error code indicating the result of this
	 *         operation.
	 */
	public static int processSetValue(String iRequest, String iValue, boolean iAdmin, IDataManager ioSCOData, IValidatorFactory validatorFactory) {
		// Assume no processing errors
		int result = DMErrorCodes.NO_ERROR;

		DMRequest request = null;

		if (iRequest != null && !iRequest.isEmpty()) {

			if (iValue != null) {
				// Attempt to create a DMRequest using the provided value
				try {
					request = new DMRequest(iRequest, iValue, iAdmin);

					// Process the SetValue() request
					result = ioSCOData.setValue(request, validatorFactory);
				} catch (NullPointerException npe) {
					result = DMErrorCodes.INVALID_REQUEST;
					log.debug("NullPointerException in processSetValue -> result is INVALID_REQUEST.", npe);
				} catch (NumberFormatException nfe) {
					result = DMErrorCodes.INVALID_REQUEST;
					log.debug("NumberFormatException in processSetValue -> result is INVALID_REQUEST.", nfe);
				}
			} else {
				// No second parameter defined
				result = DMErrorCodes.GEN_ARGUMENT_ERROR;
			}
		} else {
			result = DMErrorCodes.ELEMENT_NOT_SPECIFIED;
		}

		return result;
	}

	/**
	 * Processes a validate() against a known set of SCO run-time data
	 * (<code>SCODataManager</code>.
	 * 
	 * @param iRequest  A dot-notation binding of the desired data model element.
	 * 
	 * @param iValue    Indicates the value that will be compared.
	 * 
	 * @param ioSCOData An instance of the <code>SCODataManager</code> that
	 *                  contains the run-time data for the individual SCO.
	 * 
	 * @return An abstract data model error code indicating the result of this
	 *         operation.
	 */
	public static int processValidate(String iRequest, String iValue, IDataManager ioSCOData) {
		// Assume no processing errors
		int result = DMErrorCodes.NO_ERROR;

		DMRequest request = null;

		if (iRequest != null) {
			// Attempt to create a DMRequest using the provided value
			try {
				request = new DMRequest(iRequest, iValue, false);

				// Process the Equals() request
				result = ioSCOData.validate(request);
			} catch (NullPointerException npe) {
				result = DMErrorCodes.INVALID_REQUEST;
				log.debug("NullPointerException in processValidate -> result is INVALID_REQUEST.", npe);
			} catch (NumberFormatException nfe) {
				result = DMErrorCodes.INVALID_REQUEST;
				log.debug("NumberFormatException in processValidate -> result is INVALID_REQUEST.", nfe);
			}
		} else {
			result = DMErrorCodes.ELEMENT_NOT_SPECIFIED;
		}

		return result;
	}

} // end DMInterface
