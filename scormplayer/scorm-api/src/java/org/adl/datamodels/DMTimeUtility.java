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

package org.adl.datamodels;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Defines run-time data model utilities.
 * <br><br>
 * 
 * <strong>Filename:</strong> DMTimeUtility.java<br><br>
 * 
 * <strong>Description:</strong>This file contains utility methods used
 * by the run-time datamodel.<br><br>
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
public class DMTimeUtility {
	private static final Pattern TIME_STRING_PATTERN = Pattern.compile("P(?:(\\d*)Y)?(?:(\\d*)M)?(?:(\\d*)D)?T?(?:(\\d*)H)?(?:(\\d*)M)?(?:(\\d*)(?:[.])?(\\d{0,2})?S)?");
	static final Log log = LogFactory.getLog(DMTimeUtility.class);

	/**
	 * This method calls the timeStringParse method to convert a time interval 
	 * string to integers for the year, month, day, hour, minute, second and 
	 * decimal portion of second.  The returned integers are added and then 
	 * converted back to a string which is returned.
	 *
	 * @param iTimeOne The String representation of a datamodel time interval.
	 * 
	 * @param iTimeTwo The String representation of a datamodel time interval.
	 * 
	 * @return A String representing the addition of the two input parameters.
	 * 
	 */
	public static String add(String iTimeOne, String iTimeTwo) {
		// Possible formats for input srings
		// P[yY][mM][dD][T[hH][mM][s[.s]S] 
		// P1Y3M2DT3H
		// PT3H5M

		// Regex
		// P(?:(\d+)Y)?(?:(\d+)M)?(?:(\d+)D)?T(?:(\d+)H)?(?:(\d+)M)?(?:([0-9.]+)S)? 

		String mTimeString = null;
		int multiple = 1;
		int[] mFirstTime = new int[7];
		int[] mSecondTime = new int[7];

		for (int i = 0; i < 7; i++) {
			mFirstTime[i] = 0;
			mSecondTime[i] = 0;
		}

		timeStringParse(iTimeOne, mFirstTime);
		timeStringParse(iTimeTwo, mSecondTime);

		// add first and second time arrays  
		for (int i = 0; i < 7; i++) {
			mFirstTime[i] += mSecondTime[i];
		}

		// adjust seconds, minutes, hours, and days if addition
		// results in too large a number
		if (mFirstTime[6] > 99) {
			multiple = mFirstTime[6] / 100;
			mFirstTime[6] = mFirstTime[6] % 100;
			mFirstTime[5] += multiple;
		}

		if (mFirstTime[5] > 59) {
			multiple = mFirstTime[5] / 60;
			mFirstTime[5] = mFirstTime[5] % 60;
			mFirstTime[4] += multiple;
		}
		if (mFirstTime[4] > 59) {
			multiple = mFirstTime[4] / 60;
			mFirstTime[4] = mFirstTime[4] % 60;
			mFirstTime[3] += multiple;
		}

		if (mFirstTime[3] > 23) {
			multiple = mFirstTime[3] / 24;
			mFirstTime[3] = mFirstTime[3] % 24;
			mFirstTime[2] += multiple;
		}

		boolean hasItems = false;
		for (int i = 0; i < mFirstTime.length; i++) {
			if (mFirstTime[i] != 0) {
				hasItems = true;
				break;
			}
		}

		// create the new timeInterval string
		mTimeString = "";
		if (hasItems) {
			mTimeString += "P";
		}
		if (mFirstTime[0] != 0) {
			Integer tempInt = mFirstTime[0];
			mTimeString += tempInt.toString();
			mTimeString += "Y";
		}
		if (mFirstTime[1] != 0) {
			Integer tempInt = mFirstTime[1];
			mTimeString += tempInt.toString();
			mTimeString += "M";
		}

		if (mFirstTime[2] != 0) {
			Integer tempInt = mFirstTime[2];
			mTimeString += tempInt.toString();
			mTimeString += "D";
		}

		if ((mFirstTime[3] != 0) || (mFirstTime[4] != 0) || (mFirstTime[5] != 0) || (mFirstTime[6] != 0)) {
			mTimeString += "T";
		}

		if (mFirstTime[3] != 0) {
			Integer tempInt = mFirstTime[3];
			mTimeString += tempInt.toString();
			mTimeString += "H";
		}

		if (mFirstTime[4] != 0) {
			Integer tempInt = mFirstTime[4];
			mTimeString += tempInt.toString();
			mTimeString += "M";
		}

		if (mFirstTime[5] != 0) {
			Integer tempInt = mFirstTime[5];
			mTimeString += tempInt.toString();
		}

		if (mFirstTime[6] != 0) {
			if (mFirstTime[5] == 0) {
				mTimeString += "0";
			}
			mTimeString += ".";
			if (mFirstTime[6] < 10) {
				mTimeString += "0";
			}
			Integer tempInt2 = mFirstTime[6];
			mTimeString += tempInt2.toString();
		}
		if ((mFirstTime[5] != 0) || (mFirstTime[6] != 0)) {
			mTimeString += "S";
		}

		return mTimeString;

	}

	/**
	 * This method takes the input String parameter which represents
	 * a datamodel time interval string and converts it to an array of integers.
	 * The array integers represent the years, months, days, hours, minutes, 
	 * seconds and decimal portions of seconds of the input time interval 
	 * string.  Any on of the time interval sections may be missing
	 * 
	 * @param iTime The String representation of a datamodel time interval.
	 * 
	 * @param ioArray An array of integers.
	 * 
	 */
	private static void timeStringParse(String iTime, int[] ioArray) {
		// P[yY][mM][dD][T[hH][mM][s[.s]S] 
		// P1Y3M2DT3H
		// PT3H5M

		Matcher matcher = TIME_STRING_PATTERN.matcher(iTime);
		if (matcher.matches()) {
			for (int i = 0; i < ioArray.length; i++) {
				if (i < matcher.groupCount()) {
					String value = matcher.group(i + 1);
					ioArray[i] = ((StringUtils.isNotEmpty(value) && StringUtils.isNumeric(value)) ? Integer.parseInt(value) : 0);
				}
			}
		}
	}

}
