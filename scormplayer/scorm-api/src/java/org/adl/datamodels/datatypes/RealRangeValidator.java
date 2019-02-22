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
import java.util.Vector;

import org.adl.datamodels.DMDelimiter;
import org.adl.datamodels.DMErrorCodes;
import org.adl.datamodels.DMTypeValidator;

/**
 * Provides support for the Real data type within a specified range, as defined
 * in the SCORM 2004 RTE Book<br>
 * <br>
 * 
 * <strong>Filename:</strong> RealRangeValidator.java<br>
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
public class RealRangeValidator extends DMTypeValidator implements Serializable {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -8151319774431631583L;

	/**
	 * Describes the maximum value allowed in the range.
	 */
	private Double mMax = null;

	/**
	 * Describes the minimum value allowed in the range.
	 */
	private Double mMin = null;

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	
	 Constructors
	
	-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

	/**
	 * Default constructor required for serialization.
	 */
	public RealRangeValidator() {
		// The default constructor does not define any explicity functionality
	}

	/**
	 * Defines an real-valued range.
	 * 
	 * @param iMin
	 *            Defines the lower bound for this range.
	 * 
	 * @param iMax
	 *            Defines the upper bound for this range.
	 */
	public RealRangeValidator(Double iMin, Double iMax) {
		mMax = iMax;
		mMin = iMin;
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
		boolean done = false;

		double val1 = Double.NaN;
		double val2 = Double.NaN;

		try {
			val1 = Double.parseDouble(iFirst);
			val2 = Double.parseDouble(iSecond);
		} catch (NumberFormatException nfe) {
			equal = false;
			done = true;
		}

		if (!done) {
			val1 = Math.floor(val1 * 1000000.0) / 1000000.0;
			val2 = Math.floor(val2 * 1000000.0) / 1000000.0;

			equal = Double.compare(val1, val2) == 0;

			// If the floor at 7 digits didn't work, try rounding
			if (!equal) {
				val1 = Double.parseDouble(iFirst);
				val2 = Double.parseDouble(iSecond);

				val1 = Math.round(val1 * 1000000.0) / 1000000.0;
				val2 = Math.round(val2 * 1000000.0) / 1000000.0;

				equal = Double.compare(val1, val2) == 0;
			}
		}

		return equal;
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

		boolean done = false;

		if (iValue == null){
			// A null value can never be valid
			return DMErrorCodes.UNKNOWN_EXCEPTION;
		}

		try {
			double value = Double.parseDouble(iValue);

			if (mMin == null && mMax == null) {
				done = true;
			}

			// Just testing to see if the value is a real number
			if (done != true) {
				// Test with a min and no max; this defaults the max to infinity
				if (mMin != null && mMax == null) {
					if (value < mMin) {
						valid = DMErrorCodes.VALUE_OUT_OF_RANGE;
					}
				}
				// Test with a max and not min; this defaults the min to 0
				else if (mMin == null && mMax != null) {
					if (value < 0 || value > mMax) {
						valid = DMErrorCodes.VALUE_OUT_OF_RANGE;
					}
				}
				// Test with a max and a min
				else if (mMin != null && mMax != null) {
					if (value < mMin || value > mMax) {
						valid = DMErrorCodes.VALUE_OUT_OF_RANGE;
					}
				}
			}
		} catch (NumberFormatException nfe) {
			valid = DMErrorCodes.TYPE_MISMATCH;
		}

		return valid;
	}

} // end RealRangeValidator
