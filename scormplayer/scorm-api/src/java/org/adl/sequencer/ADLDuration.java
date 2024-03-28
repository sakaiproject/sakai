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
package org.adl.sequencer;

import java.io.Serializable;

import org.adl.util.debug.DebugIndicator;

/**
 * <strong>Filename:</strong> ADLDuration.java<br><br>
 *
 * <strong>Description:</strong>This class represents a duration.  The class
 * represents the duration as a <code>long</code>.  All durations are 
 * converted to a <code>long</code> upon creation.<br><br>
 * 
 * @author ADL Technical Team
 */
public class ADLDuration implements Serializable, IDuration {
	private static final long serialVersionUID = 1L;

	private long id;

	/**
	 * This controls display of log messages to the java console
	 */
	private static boolean _Debug = DebugIndicator.ON;

	/**
	 * The duration being tracked in milliseconds
	 */
	public long mDuration = 0;

	/**
	 * Default constructor for the <code>ADLDuration</code> class.  Sets the 
	 * duration to zero.
	 *
	 */
	public ADLDuration() {
		mDuration = 0;
	}

	/**
	 * Constructor for the <code>ADLDuration</code> class.  Based on the format 
	 * (<code>iFormat</code> to be used, this constructor takes the string 
	 * representation of the duration (<code>iValue</code>) and creates an
	 * <code>ADLDuration</code>.
	 * 
	 * @param iFormat Indicates the format for the duration
	 * @param iValue String value that holds the duration to be used
	 */
	public ADLDuration(int iFormat, String iValue) {

		String hours = null;
		String min = null;
		String sec = null;

		switch (iFormat) {

		case FORMAT_SECONDS: {
			double secs = 0.0;

			try {
				secs = (new Double(iValue));
			} catch (Exception e) {
				if (_Debug) {
					System.out.print("  Invalid Format ::  " + iFormat + " // " + iValue);
				}
			}

			mDuration = (long) (secs * 1000.0);

			break;

		}
		case FORMAT_SCHEMA: {
			int locStart = iValue.indexOf('T');
			int loc = 0;

			if (locStart != -1) {
				locStart++;

				loc = iValue.indexOf('H', locStart);

				if (loc != -1) {
					hours = iValue.substring(locStart, loc);
					mDuration = (Long.valueOf(hours)) * 3600;

					locStart = loc + 1;
				}

				loc = iValue.indexOf('M', locStart);
				if (loc != -1) {
					min = iValue.substring(locStart, loc);
					mDuration += (Long.valueOf(min)) * 60;

					locStart = loc + 1;
				}

				loc = iValue.indexOf('S', locStart);
				if (loc != -1) {
					sec = iValue.substring(locStart, loc);
					mDuration += (Long.valueOf(sec));
				}
			} else {
				if (_Debug) {
					System.out.println(" ERROR : Invalid format  --> " + iValue);
				}
			}

			break;

		}
		default: {
			// Do nothing
		}
		}
	}

	/**
	 * This method adds the duration value passed in (<code>iDur</code>) to the
	 * duration value being held by <code>mDuration</code>.
	 * 
	 * @param iDur The duration value to add.
	 */
	@Override
	public void add(IDuration durArg) {
		ADLDuration iDur = (ADLDuration) durArg;
		mDuration += iDur.mDuration;
	}

	/**
	 * This method compares to duration values.  The input duration value 
	 * (<code>iDur</code> is compared against the <code>mDuration</code> value.
	 * 
	 * @param iDur The duration value to compare.
	 * 
	 * @return Returns an integer value that represents the following:
	 * <ul>
	 *  <li> -1 if <code>mDuration</code> is less than <code>iDur</code></li>
	 *  <li> 0 if <code>mDuration</code> is equal to <code>iDur</code></li>
	 *  <li> 1 if <code>mDuration</code> is greater than <code>iDur</code></li>
	 *  <li> -999 if unknown</li>
	 * </ul>
	 */
	@Override
	public int compare(IDuration durArg) {
		ADLDuration iDur = (ADLDuration) durArg;
		int relation = IDuration.UNKNOWN;

		if (mDuration < iDur.mDuration) {
			relation = IDuration.LT;
		} else if (mDuration == iDur.mDuration) {
			relation = IDuration.EQ;
		} else if (mDuration > iDur.mDuration) {
			relation = IDuration.GT;
		}

		return relation;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		if (obj == null){
			return false;
		}
		if (getClass() != obj.getClass()){
			return false;
		}
		ADLDuration other = (ADLDuration) obj;
		if (id != other.id){
			return false;
		}
		return true;
	}

	/**
	    * This method formats the duration value according to the format type
	    * passed in (<code>iFormat</code>).
	    * 
	    * @param iFormat Indicates the format for which this method should convert
	    * the duration value to.
	    * 
	    * @return Returns a string representation of the duration, formatted
	    * accordingly.
	    */
	@Override
	public String format(int iFormat) {

		String out = null;

		long countHours = 0;
		long countMin = 0;
		long countSec = 0;

		long temp = 0;

		switch (iFormat) {

		case FORMAT_SECONDS: {
			double sec = mDuration / 1000.0;

			out = (new Double(sec)).toString();

			break;

		}
		case FORMAT_SCHEMA: {
			out = "";

			countHours = 0;
			countMin = 0;
			countSec = 0;

			temp = mDuration / 1000;

			if (temp >= 1000) {
				if (temp >= 3600) {
					countHours = temp / 3600;
					temp %= 3600;
				}

				if (temp > 60) {
					countMin = temp / 60;
					temp %= 60;
				}

				countSec = temp;
			}

			out = "PT";

			if (countHours > 0) {
				out += Long.toString(countHours, 10);
				out += "H";
			}

			if (countMin > 0) {
				out += Long.toString(countMin, 10);
				out += "M";
			}

			if (countSec > 0) {
				out += Long.toString(countSec, 10);
				out += "S";
			}

			break;

		}
		default: {
			// Do nothing
		}
		}

		return out;
	}

	public long getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}
} // end ADLDuration
