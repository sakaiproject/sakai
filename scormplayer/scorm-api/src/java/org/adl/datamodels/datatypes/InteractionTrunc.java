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

import org.apache.commons.lang.StringUtils;

/**
 * <strong>Filename:</strong> InteractionTrunc.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * Provides support for the SCORM Data Model Interaction data types, as defined 
 * in the SCORM 2004.
 * 
 * @author ADL Technical Team
 */
public class InteractionTrunc {
	/**
	 * Truncates all parts of an interaction datatype to their SPMs
	 * 
	 * @param iValue The value being truncated
	 * @param iType  The type of the value being truncated
	 * 
	 * @return Returns the Truncated value
	 */
	public static String trunc(String iValue, int iType) {
		StringBuilder trunc = new StringBuilder();

		// SCORM defined separators
		String comma = "\\[,\\]";

		int idx = -1;

		// Swith on the interaction type
		switch (iType) {
		case InteractionValidator.MULTIPLE_CHOICE: {
			// Check for an empty set
			if (StringUtils.isBlank(iValue)) {
				// Value OK
				break;
			}

			String choices[] = iValue.split(comma);
			trunc = new StringBuilder();

			// Check to determine if each choice is within the SPM range  
			for (int i = 0; i < 36; i++) {
				if (choices[i].length() > 250) {
					trunc.append(choices[i].substring(0, 250));
				} else {
					trunc.append(choices[i]);
				}

				if (i != 35) {
					trunc.append("[,]");
				}
			}

			break;
		}
		case InteractionValidator.FILL_IN: {
			// Extract each part of the match_text
			String matchText[] = iValue.split(comma);
			trunc = new StringBuilder();

			for (int i = 0; i < 10; i++) {
				String matchString = null;
				String langString = null;

				// Look for the 'lang' delimiter
				if (matchText[i].startsWith("{lang=")) {
					// Find the closing '}'
					idx = matchText[i].indexOf('}');
					if (idx != -1) {
						matchString = matchText[i].substring(idx + 1);
						langString = matchText[i].substring(6, idx);
					} else {
						matchString = matchText[i];
					}
				} else {
					matchString = matchText[i];
				}

				if (StringUtils.length(langString) > 250) {
					trunc.append( "{lang=" ).append( langString.substring(0, 250) ).append("}");
				} else {
					trunc.append( "{lang=" ).append( langString ).append("}");
				}

				if (matchString.length() > 250) {
					trunc.append(matchString.substring(0, 250));
				} else {
					trunc.append(matchString);
				}

				if (i != 9) {
					trunc.append("[,]");
				}
			}

			break;
		}
		case InteractionValidator.LONG_FILL_IN: {
			if (iValue.length() > 4000) {
				trunc = new StringBuilder(iValue.substring(0, 4000));
			} else {
				trunc = new StringBuilder(iValue);
			}

			break;
		}
		case InteractionValidator.LIKERT: {
			if (iValue.length() > 250) {
				trunc = new StringBuilder(iValue.substring(0, 250));
			}

			break;
		}
		case InteractionValidator.MATCHING: {
			if (StringUtils.isBlank(iValue)) {
				// Value OK
				break;
			}

			String commas[] = iValue.split(comma);
			trunc = new StringBuilder();

			for (int i = 0; i < 36; i++) {
				idx = commas[i].indexOf("[.]");

				String target = commas[i].substring(0, idx);
				String source = commas[i].substring(idx + 3, commas[i].length());

				if (target.length() > 250) {
					trunc.append(target.substring(0, 250));
				} else {
					trunc.append(target);
				}

				trunc.append("[.]");

				if (source.length() > 250) {
					trunc.append(source.substring(0, 250));
				} else {
					trunc.append(source);
				}

				if (i != 35) {
					trunc.append("[,]");
				}
			}

			break;
		}
		case InteractionValidator.PERFORMANCE: {
			String commaCheck[] = iValue.split(comma);
			trunc = new StringBuilder();

			for (int i = 0; i < 125; i++) {
				idx = commaCheck[i].indexOf("[.]");

				String sn = commaCheck[i].substring(0, idx);
				String sa = commaCheck[i].substring(idx + 3, commaCheck[i].length());

				if (sn.length() > 250) {
					trunc.append(sn.substring(0, 250));
				} else {
					trunc.append(sn);
				}

				trunc.append("[.]");

				if (sa.length() > 250) {
					trunc.append(sa.substring(0, 250));
				} else {
					trunc.append(sa);
				}

				if (i != 124) {
					trunc.append("[,]");
				}
			}

			break;
		}
		case InteractionValidator.SEQUENCING: {
			String array[] = iValue.split(comma);
			trunc = new StringBuilder();

			for (int i = 0; i < 36; i++) {

				if (array[i].length() > 250) {
					trunc.append(array[i].substring(0, 250));
				} else {
					trunc = new StringBuilder(array[i]);
				}

				if (i != 35) {
					trunc.append("[,]");
				}
			}

			break;
		}
		case InteractionValidator.NUMERIC: {
			trunc = new StringBuilder(iValue);

			break;
		}
		default: {
			break;
		}
		}

		return trunc.toString();
	}

} // end InteractionTrunc
