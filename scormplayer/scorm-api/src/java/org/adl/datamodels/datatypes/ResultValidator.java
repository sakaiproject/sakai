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

import org.adl.datamodels.DMErrorCodes;
import org.adl.datamodels.DMTypeValidator;

/**
 * Provides support for Valid Vocabulary tokens and if the input is valid based
 * on a Vocablist, as defined in the SCORM 2004 RTE Book<br><br>
 * 
 * <strong>Filename:</strong> ResultValidator.java<br><br>
 * 
 * <strong>Description:</strong><br><br>
 * 
 * <strong>Design Issues:</strong><br><br>
 * 
 * <strong>Implementation Issues:</strong><br><br>
 * 
 * <strong>Known Problems:</strong><br><br>
 * 
 * <strong>Side Effects:</strong><br><br>
 * 
 * <strong>References:</strong><br>
 * <ul>
 *     <li>SCORM 2004
 * </ul>
 * 
 * @author ADL Technical Team
 */
public class ResultValidator extends DMTypeValidator implements Serializable {

	/**
	  * 
	  */
	private static final long serialVersionUID = 1L;

	/**
	    * Describes the set of valid vocabulary tokens
	    */
	private String[] mVocabList = null;

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	
	 Constructors
	
	-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

	/**
	 * Default constructor required for serialization.
	 */
	public ResultValidator() {
		// The default constructor does not define any explicit functionality   
	}

	/**
	 * Constructor required for vocabulary initialization.
	 * @param iVocab An array of vocabulary string values to used for 
	 * initialization
	 */
	public ResultValidator(String[] iVocab) {
		mVocabList = iVocab;
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	
	 Public Methods
	
	-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

	/**
	 * Compares two valid data model elements for equality.
	 * 
	 * @param iFirst The first value being compared.
	 * @param iSecond The second value being compared.
	 * 
	 * @return Returns <code>true</code> if the two values are equal, otherwise
	 *         <code>false</code>.
	 */
	public boolean compare(String iFirst, String iSecond) {
		boolean equal = true;
		boolean done = false;

		double val1 = Double.NaN;
		double val2 = Double.NaN;

		try {
			// Try to make both values into reals
			val1 = Double.parseDouble(iFirst);
			val2 = Double.parseDouble(iSecond);
		} catch (NumberFormatException nfe) {
			// At least one of these must be a strings... Compare
			equal = iFirst.equals(iSecond);
			done = true;
		}

		if (!done) {
			// Only allow 7 signifigant digits -- truncate after that...
			val1 = Math.floor(val1 * 1000000.0) / 1000000.0;
			val2 = Math.floor(val2 * 1000000.0) / 1000000.0;

			equal = Double.compare(val1, val2) == 0;
		}

		return equal;
	}

	/**
	 * Validates the provided string against a known format.
	 * 
	 * @param iValue The value being validated.
	 * 
	 * @return An abstract data model error code indicating the result of this
	 *         operation.
	 */
	@Override
	public int validate(String iValue) {
		// Assume the value is not valid
		int valid = DMErrorCodes.TYPE_MISMATCH;

		boolean done = false;

		// Check to see if constructor is null
		if (mVocabList == null || iValue == null) {
			valid = DMErrorCodes.UNKNOWN_EXCEPTION;
			done = true;
		}

		// See if this value is in the provided vocabulary
		if (!done) {
			for (String tmpVocab : mVocabList) {
				// Check to see if this element equals the input value
				if (tmpVocab.equals(iValue)) {
					valid = DMErrorCodes.NO_ERROR;
					done = true;

					// done
					break;
				}
			}

			// There is no matching vocabulary item, see if the value is a real
			if (!done) {
				try {
					Double.parseDouble(iValue);

					// The value is a valid real number
					valid = DMErrorCodes.NO_ERROR;
				} catch (NumberFormatException nfe) {
					// The value is invalid
					valid = DMErrorCodes.TYPE_MISMATCH;
				}
			}
		}

		return valid;
	}

} // end ResultValidator

