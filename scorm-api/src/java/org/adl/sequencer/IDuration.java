/*
 * #%L
 * SCORM API
 * %%
 * Copyright (C) 2007 - 2016 Sakai Project
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.adl.sequencer;

public interface IDuration {

	/**
	 * Enumeration of possible relations between two <code>ADLDuration</code>
	 * objects.
	 * <br>Unknown
	 * <br><b>-999</b>
	 */
	public static final int UNKNOWN = -999;

	/**
	 * Enumeration of possible relations between two <code>ADLDuration</code>
	 * objects.
	 * <br>Less Than
	 * <br><b>-1</b>
	 */
	public static final int LT = -1;

	/**
	 * Enumeration of possible relations between two <code>ADLDuration</code>
	 * objects.
	 * <br>Less Than
	 * <br><b>0</b>
	 */
	public static final int EQ = 0;

	/**
	 * Enumeration of possible relations between two <code>ADLDuration</code>
	 * objects.
	 * <br>Greater Than
	 * <br><b>1</b>
	 */
	public static final int GT = 1;

	/**
	 * Enumeration of possible formats for duration information.
	 * <br>Seconds /w one tenth second accuracy
	 * <br><b>0</b>
	 */
	public static final int FORMAT_SECONDS = 0;

	/**
	 * Enumeration of possible formats for duration information.
	 * <br>XML Schema -- Duration Type
	 * <br><b>1</b>
	 */
	public static final int FORMAT_SCHEMA = 1;

	/**
	 * This method adds the duration value passed in (<code>iDur</code>) to the
	 * duration value being held by <code>mDuration</code>.
	 * 
	 * @param iDur The duration value to add.
	 */
	public void add(IDuration iDur);

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
	public int compare(IDuration iDur);

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
	public String format(int iFormat);

}