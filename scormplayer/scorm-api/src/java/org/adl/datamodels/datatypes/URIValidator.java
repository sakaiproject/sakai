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
 *******************************************************************************/

package org.adl.datamodels.datatypes;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;

import org.adl.datamodels.DMDelimiter;
import org.adl.datamodels.DMErrorCodes;
import org.adl.datamodels.DMTypeValidator;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides support for the URI data type, as defined in the SCORM 2004 RTE Book<br>
 * <br>
 * 
 * <strong>Filename:</strong> URIValidator.java<br>
 * <br>
 * 
 * <strong>Description:</strong><br>
 * <br>
 * 
 * <strong>Design Issues:</strong><br>
 * <br>
 * 
 * <strong>Implementation Issues:</strong><br>
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
 * <li>SCORM 2004
 * </ul>
 * 
 * @author ADL Technical Team
 */
public class URIValidator extends DMTypeValidator implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -7009966690641486705L;

	/**
	 * Describes the smallest permitted maximum allowed for a URI.
	 */
	private int mSPM = -1;

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	
	 Constructors
	
	-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

	/**
	 * Default constructor required for serialization.
	 */
	public URIValidator() {
		mType = "URI";
	}

	/**
	 * Describes the smallest permitted maximum allowed for the URI.
	 * 
	 * @param iSPM
	 *            Describes the SPM for the URI being validated
	 * 
	 * @param iType
	 *            Describes the human readable name for this type validator.
	 */
	public URIValidator(int iSPM, String iType) {
		mSPM = iSPM;
		mType = iType;
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	
	 Public Methods
	
	-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

	/**
	 * Compares two valid data model elements for equality.
	 * 
	 * @param iFirst
	 *            The first value being compared.
	 * 
	 * @param iSecond
	 *            The second value being compared.
	 * 
	 * @param iDelimiters
	 *            The common set of delimiters associated with the values being
	 *            compared.
	 * 
	 * @return Returns <code>true</code> if the two values are equal, otherwise
	 *         <code>false</code>.
	 */
	public boolean compare(String iFirst, String iSecond, Vector<DMDelimiter> iDelimiters) {
		boolean equal = true;

		if (StringUtils.isBlank( iFirst )) {
			// The first string is an invalid URI
			equal = false;
		} else {
			if (StringUtils.isBlank( iSecond )) {
				// The second string is an invalid URI
				equal = false;
			} else {

				try {
					// Try to create URIs from the provided strings
					URI uri1 = new URI(iFirst);
					URI uri2 = new URI(iSecond);

					equal = uri1.equals(uri2);
				} catch (URISyntaxException use) {
					// One of the stings is not a valid URI
					equal = false;
				}
			}
		}

		return equal;
	}

	/**
	 * Truncates the value to meet the DataType's SPM
	 * 
	 * @param iValue
	 *            The value to be truncated
	 * 
	 * @return Returns the value truncated at the DataType's SPM
	 */
	@Override
	public String trunc(String iValue) {
		String trunc = iValue;

		if ((mSPM > 0) && (iValue.length() > mSPM)) {
			trunc = trunc.substring(0, mSPM);
		}

		return trunc;
	}

	/**
	 * Validates the provided string against a known format.
	 * 
	 * @param iValue
	 *            The value being validated.
	 * 
	 * @return An abstract data model error code indicating the result of this
	 *         operation.
	 */
	@Override
	public int validate(String iValue) {
		// Assume the value is valid
		int valid = DMErrorCodes.NO_ERROR;

		if (iValue == null){
			// A null value can never be valid
			return DMErrorCodes.UNKNOWN_EXCEPTION;
		}

		// URIs are not allowed to be empty strings
		if (iValue.trim().isEmpty()) {
			valid = DMErrorCodes.TYPE_MISMATCH;
		} else {
			try {
				new URI(iValue);

				if (mSPM > -1) {
					if (iValue.length() > mSPM) {
						valid = DMErrorCodes.SPM_EXCEEDED;
					}
				}
			} catch (URISyntaxException use) {
				valid = DMErrorCodes.TYPE_MISMATCH;
			}
		}

		return valid;
	}

} // end URIValidatorValidator

