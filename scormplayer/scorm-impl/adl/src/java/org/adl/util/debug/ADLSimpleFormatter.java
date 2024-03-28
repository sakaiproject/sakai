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

package org.adl.util.debug;

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * <strong>Filename: </strong> <br>
 * ADLSimpleFormatter.java <br>
 * <br>
 * <strong>Description: </strong> <br>
 * A <code>ADLSimpleFormatter</code> extends Java's SimpleFormatter class and
 * overrides that class's format function. This is so we can modify the messages
 * that are output using Java's logging output messages. Specifically, we do not
 * want the date/timestamp written on each and every message written to the
 * Console. <br>
 * <br>
 * 
 * @author ADL Technical Team <br>
 */
public class ADLSimpleFormatter extends SimpleFormatter {
	/**
	 * A line separator used to separate messages sent to the log.
	 */
	private String mLineSeparator = java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<String>() {
		@Override
		public String run() {
			return System.getProperty("line.separator");
		}
	});

	/**
	 * Overrides SimpleFormatter format function. Writes the output without
	 * displaying the date/timestamp.
	 * 
	 * @param iRecord The log record that needs formatted.
	 * @return A string formatted for a logging message
	 */
	@Override
	public synchronized String format(LogRecord iRecord) {
		StringBuilder sb = new StringBuilder();

		if (iRecord.getSourceClassName() != null) {
			sb.append(iRecord.getSourceClassName());
		} else {
			sb.append(iRecord.getLoggerName());
		}
		if (iRecord.getSourceMethodName() != null) {
			sb.append(" ");
			sb.append(iRecord.getSourceMethodName());
		}
		sb.append(" ");

		String message = formatMessage(iRecord);
		sb.append(iRecord.getLevel().getLocalizedName());
		sb.append(": ");
		sb.append(message);
		sb.append(mLineSeparator);

		return sb.toString();
	}
}