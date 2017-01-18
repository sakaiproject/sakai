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
** DAMAGES.**
******************************************************************************/
package org.adl.util;

/**
 * The MessageType data
 * structure serves as the classification system for the messages.
 * This data structure determines which  <br><br>
 * 
 * @author ADL Technical Team
 */
public class MessageType {
	// Public Data Members
	/**
	 * value used to represent informational messages in the test logs
	 */
	public static final int INFO = 0;

	/**
	 * value used to represent warning messages in the test logs
	 */
	public static final int WARNING = 1;

	/**
	 * value used to represent passed messages in the test logs
	 */
	public static final int PASSED = 2;

	/**
	 * value used to represent failed messages in the test logs
	 */
	public static final int FAILED = 3;

	/**
	 * value used to represent terminate messages in the test logs
	 */
	public static final int TERMINATE = 4;

	/**
	 * value used to represent conformant messages in the test logs
	 */
	public static final int CONFORMANT = 5;

	/**
	 * value used to send a new log message to the log writers to create a new 
	 * log
	 */
	public static final int NEWLOG = 7;

	/**
	 * value used to send an end log message to the log writers to close out
	 * the current log
	 */
	public static final int ENDLOG = 8;

	/**
	 * value used to represent "other" messages in the test logs
	 */
	public static final int OTHER = 9;

	/**
	 * value used to identify the message as not part of the test, instead this 
	 * message is the heading for the test logs
	 */
	public static final int HEADER = 10;

	/**
	 * value used to identify the message as not part of the test, instead this 
	 * message is the title for the test logs
	 */
	public static final int TITLE = 11;

	/**
	 * value used to identify the message as not part of the test, instead this 
	 * message is the sub title for the test logs
	 */
	public static final int SUBTITLE = 12;

	/**
	 * value used to represent informational messages in the heading of
	 * the test logs
	 */
	public static final int HEADINFO = 13;

	/**
	 * value used to represent warning messages in the heading of the test logs
	 */
	public static final int HEADWARN = 14;

	/**
	 * value used to identify the message as not part of the test, instead this 
	 * message is the title for the sub logs, such as md logs in the cp test
	 */
	public static final int SUBLOGTITLE = 15;

	/**
	 * value used to represent other xml messages being sent to the test logs
	 */
	public static final int XMLOTHER = 16;

	/**
	 * value used to identify a SCO detailed log link
	 */
	public static final int LINKSCO = 17;

	/**
	 * value used to identify a MD detailed log link
	 */
	public static final int LINKMD = 18;

	/**
	 * value used to identify a CP detailed log link
	 */
	public static final int LINKCP = 19;

	/**
	 * value used to identify a manifest detailed log link
	 */
	public static final int LINKMANIFEST = 20;

	/**
	 * value used to represent heading messages in the test sub logs, such as a 
	 * MD log in the CP test
	 */
	public static final int SUBLOGHEAD = 21;

	/**
	 * value used to represent informational messages in the test logs
	 */
	public static final String _INFO = "0";

	/**
	 * value used to represent warning messages in the test logs
	 */
	public static final String _WARNING = "1";

	/**
	 * value used to represent passed messages in the test logs
	 */
	public static final String _PASSED = "2";

	/**
	 * value used to represent failed messages in the test logs
	 */
	public static final String _FAILED = "3";

	/**
	 * value used to represent terminate messages in the test logs
	 */
	public static final String _TERMINATE = "4";

	/**
	 * value used to represent conformant messages in the test logs
	 */
	public static final String _CONFORMANT = "5";

	/**
	 * value used to represent "other" messages in the test logs
	 */
	public static final String _OTHER = "9";

	/**
	 * value used to identify the message as not part of the test, instead this 
	 * message is the heading for the test logs
	 */
	public static final String _HEADER = "10";

	/**
	 * value used to identify the message as not part of the test, instead this 
	 * message is the title for the test logs
	 */
	public static final String _TITLE = "11";

	/**
	 * value used to identify the message as not part of the test, instead this 
	 * message is the sub title for the test logs
	 */
	public static final String _SUBTITLE = "12";

	/**
	 * value used to represent informational messages in the heading of
	 * the test logs
	 */
	public static final String _HEADINFO = "13";

	/**
	 * value used to represent warning messages in the heading of the test logs
	 */
	public static final String _HEADWARN = "14";

	/**
	 * value used to identify the message as not part of the test, instead this 
	 * message is the title for the sub logs, such as md logs in the cp test
	 */
	public static final String _SUBLOGTITLE = "15";

	/**
	 * value used to represent other xml messages being sent to the test logs
	 */
	public static final String _XMLOTHER = "16";

	/**
	 * value used to identify a SCO detailed log link
	 */
	public static final String _LINKSCO = "17";

	/**
	 * value used to identify a MD detailed log link
	 */
	public static final String _LINKMD = "18";

	/**
	 * value used to identify a CP detailed log link
	 */
	public static final String _LINKCP = "19";

	/**
	 * value used to identify a manifest detailed log link
	 */
	public static final String _LINKMANIFEST = "20";

	/**
	 * value used to represent heading messages in the test sub logs, such as a 
	 * MD log in the CP test
	 */
	public static final String _SUBLOGHEAD = "21";

	public static int SSP = 6;

	public static String _SSP = "6";

}
