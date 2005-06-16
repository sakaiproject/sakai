/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.facades.standalone;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.ServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.facades.ContextManagement;

/**
 * An implementation of the ContextManagement facade to support demos and UI tests.
 */
public class ContextManagementStandaloneImpl implements ContextManagement {
	private static Log logger = LogFactory.getLog(ContextManagementStandaloneImpl.class);

	private static final String GRADEBOOK_UID_PARAM = "gradebookUid";

	public String getGradebookUid(Object request) {
		String gradebookUid = (String)((ServletRequest)request).getParameter(GRADEBOOK_UID_PARAM);
		if (gradebookUid != null) {
			try {
				gradebookUid = URLDecoder.decode(gradebookUid, "UTF-8");
			} catch (UnsupportedEncodingException ex) {
				logger.error("Unlikely exception thrown", ex);
			}
		}
		if (logger.isDebugEnabled()) logger.debug("getGradebookUid returning " + gradebookUid);
		return gradebookUid;
	}
}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Id$
 *************************************************************************************************************************************************************************************************************************************************************/
