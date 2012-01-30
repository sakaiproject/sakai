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
package org.adl.validator.contentpackage;

import java.util.logging.Logger;

/**
 *
 * <strong>Filename: </strong><br>XSDHandler.java<br><br>
 *
 * <strong>Description: </strong><br>The <CODE>XSDHandler </CODE>object handles
 * XSD manipulation, such as cononical compare of XSDs and required files check.
 * The XSDHandler class will produce a DOM of the provided XSD at the root of
 * the PIF as well as a DOM of the ADL owned XSDs.  The DOM's will then be
 * cononically compared .  The only difference allowed between the two DOM trees
 * is pointing to an external verses local copy of the ims_xml.xsd schema
 * imported by the IMS CP XSD.
 * 
 * @author ADL Technical Team
 */
public class XSDHandler {
	/**
	 * Logger object used for debug logging.
	 */
	@SuppressWarnings("unused")
	private Logger mLogger;

	/**
	 * The constructor.
	 */
	public XSDHandler(String iDirectory, String iApplicationProfile) {
		mLogger = Logger.getLogger("org.adl.util.debug.validator");
	}
}