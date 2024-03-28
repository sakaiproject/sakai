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

package org.ims.ssp.samplerte.client;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URL;

import org.adl.util.debug.DebugIndicator;
import org.adl.util.servlet.ServletWriter;
import org.ims.ssp.samplerte.server.SSP_ServletRequest;
import org.ims.ssp.samplerte.server.SSP_ServletResponse;
import org.ims.ssp.samplerte.server.bucket.SuccessStatus;

/**
 * This class encapsulates communication between the API Adapter applet and
 * the <code>LMSCMIServlet</code>.<br><br>
 *
 * <strong>Filename:</strong> ServletProxy<br><br>
 *
 * <strong>Description:</strong><br><br>
 *
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the SCORM 2004 3rd Edition
 * Sample RTE. <br>
 * <br>
 *
 * <strong>Implementation Issues:</strong><br><br>
 *
 * <strong>Known Problems:</strong><br><br>
 *
 * <strong>Side Effects:</strong><br><br>
 *
 * <strong>References:</strong><br>
 * <ul>
 *     <li>SCORM 2004 3rd Edition
 * </ul>
 *
 * @author ADL Technical Team
 */
public class ServletProxy {
	/**
	 * This controls display of log messages to the java console
	 */
	private static boolean _Debug = DebugIndicator.ON;

	/**
	 * The URL of the target servlet.
	 */
	private URL mServletURL = null;

	/**
	 * Constructor
	 *
	 * @param iURL  The URL of the target servlet.
	 */
	public ServletProxy(URL iURL) {
		mServletURL = iURL;
	}

	/**
	 * Reads from the LMS server via the <code>SSP_Servlet</code>; the
	 * response containing the information requested.
	 *
	 * @param iRequest A <code>SSP_ServletRequest</code> object that
	 *                 provides all the data neccessary to POST a call to
	 *                 the <code>SSP_Servlet</code>.
	 *
	 * @return The <code>LMSCMIServletResponse</code> object provided by the
	 *         <code>LMSCMIServlet</code>.
	 */
	public SSP_ServletResponse postLMSRequest(SSP_ServletRequest iRequest) {

		if (_Debug) {
			System.out.println("In ServletProxy::postLMSRequest()");
		}

		SSP_ServletResponse response = new SSP_ServletResponse();

		try {
			Serializable[] data = { iRequest };

			if (_Debug) {
				System.out.println("Before postObjects()");
			}

			try (ObjectInputStream in = ServletWriter.postObjects(mServletURL, data))
			{
				if (_Debug) {
					System.out.println("Back In " + "ServletProxy::postLMSRequest()");
					System.out.println("Attempting to read servlet " + "response now...");
				}
				
				response = (SSP_ServletResponse) in.readObject();
			}
		} catch (Exception e) {
			if (_Debug) {
				System.out.println("Exception caught in " + "ServletProxy::postLMSRequest()");
				System.out.println(e.getMessage());
			}

			e.printStackTrace();
			response.mManagedBucketInfo.setSuccessStatus(SuccessStatus.FAILURE);
		}

		return response;
	}

} // ServletProxy
