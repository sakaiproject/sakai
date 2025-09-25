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

package org.adl.util.servlet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;

import lombok.extern.slf4j.Slf4j;

/**
 * Provides a means to 'POST' multiple serialized objects to a servlet.<br><br>
 *
 * <strong>Filename:</strong> ServletWriter<br><br>
 *
 * <strong>Description:</strong><br>
 * This class provides a method of posting multiple serialized objects to a
 * Java servlet and getting objects in return. This code was inspired by code
 * samples from the book 'Java Servlet Programming' by Jason Hunter and William
 * Crawford (O'Reilly & Associates. 1998).<br><br>
 *
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the SCORM 2004 3rd Edtion Sample
 * RTE. <br>
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
@Slf4j
public class ServletWriter {

    /**
	 * Provides a means to 'POST' multiple serialized objects to a servlet.
	 *
	 * @param iServlet   The URL of the target servlet.
	 *
	 * @param iObjs      A list of objects to be serialized during the POST.
	 *
	 * @return A stream of serialized objects.
	 * @exception Exception
	 */
	static public ObjectInputStream postObjects(URL iServlet, Serializable iObjs[]) throws Exception {

        log.debug("In ServletWriter::postObjects()");

		URLConnection con = null;

		try {
            log.debug("Opening HTTP URL connection to servlet.");
			con = iServlet.openConnection();
		} catch (Exception e) {
			log.debug("e = 1");
            log.debug("Exception caught in ServletWriter::postObjects()", e);
			throw e;
		}

        log.debug("HTTP connection to servlet is open");
        log.debug("configuring HTTP connection properties");

        con.setDoInput(true);
		con.setDoOutput(true);
		con.setUseCaches(false);
		con.setRequestProperty("Content-Type", "text/plain");
		con.setAllowUserInteraction(false);

		// Write the arguments as post data
		ObjectOutputStream out = null;

		try {
            log.debug("Creating new http output stream");

			out = new ObjectOutputStream(con.getOutputStream());

            log.debug("Created new http output stream.");
            log.debug("Writing command and data to servlet...");

            int numObjects = iObjs.length;

            log.debug("Num objects: " + numObjects);

            for (Serializable iObj : iObjs) {
                out.writeObject(iObj);
                log.debug("Just wrote a serialized object on output stream... {}", iObj.getClass());
            }
		} catch (Exception e) {
            log.debug("Exception caught in ServletWriter::postObjects()", e);
			throw e;
		}

		try {
            log.debug("Flushing Object Output Stream.");
			out.flush();
		} catch (IOException ioe) {
            log.debug("Caught IOException when calling out.flush()", ioe);
			throw ioe;
		} catch (Exception e) {
            log.debug("Caught Exception when calling out.flush()", e);
            throw e;
		}

		try {
            log.debug("Closing object output stream.");
			out.close();
		} catch (IOException ioe) {
            log.debug("Caught IOException when calling out.close()", ioe);
			throw ioe;
		} catch (Exception e) {
            log.debug("Caught Exception when calling out.close()", e);
			throw e;
		}

		ObjectInputStream in;

		try {
            log.debug("Creating new http input stream.");
			in = new ObjectInputStream(con.getInputStream());
		} catch (Exception e) {
            log.debug("Exception caught in ServletWriter::postObjects()", e);
			throw e;
		}
		return in;
	}

}
