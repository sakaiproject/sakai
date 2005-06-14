/**********************************************************************************
*
* $Header: /cvs/sakai2/gradebook/tool/src/test/org/sakaiproject/tool/gradebook/test/SpringEnabledTestCase.java,v 1.3 2005/05/26 18:04:55 josh.media.berkeley.edu Exp $
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

package org.sakaiproject.tool.gradebook.test;

import java.util.StringTokenizer;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * A JUnit TestCase extension that simplifies integration testing by making
 * available a Spring application context. This class short-circuits the 'set
 * all instance variables null before every test method' mechanism in favor of
 * loading Spring environment only once before executing any test methods.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public abstract class SpringEnabledTestCase extends TestCase {
    protected static final Log log = LogFactory.getLog(SpringEnabledTestCase.class);
    private static String[] contextFiles = null;
	private static ApplicationContext context = null;

	protected final static Object getBean(String bean) {
		return context.getBean(bean);
	}

	protected final static ApplicationContext getContext() {
		return context;
	}

	/**
	 * Alternate form to initialize/load context
	 * 
	 * @param xmlFiles
	 *            in comma delimited string
	 */
	protected final synchronized void initialize(String xmlFiles)
			throws Exception {
		String[] contextFiles = toStringArray(xmlFiles);
		initialize(contextFiles);
	}

	/**
	 * @param xmlFiles
	 *            context files that specify objects available in Spring
	 *            application environment.
	 */
	protected final synchronized void initialize(String[] xmlFiles)
			throws Exception {
		if (context == null || !same(xmlFiles, contextFiles)) {
			contextFiles = xmlFiles;
			context = new ClassPathXmlApplicationContext(xmlFiles);
			StringBuffer msg = new StringBuffer("<<< CONTEXT LOADED >>> [");
			for (int i = 0; i < xmlFiles.length; i++) {
				if (i > 0) {
					msg.append(" ");
				}
				msg.append(xmlFiles[i]);
			}
			msg.append("] IN class='");
			msg.append(this.getClass().getName());
			msg.append("'");
			System.out.println(msg.toString());
		}
	}

	private boolean same(String[] files1, String[] files2) {
		boolean ret = true;
		ret = files1 != null && files2 != null
				&& files1.length == files2.length;
		// true if both not null & same length arrays

		// loop stops if ret==false
		for (int i = 0; ret && i < files1.length; i++) {
			ret = files1[i].equals(files2[i]);
		}
		return ret;
	}

	private String[] toStringArray(String listing) {
		String[] ret = null;

		StringTokenizer t = new StringTokenizer(listing, ",");
		final int count = t.countTokens();
		ret = new String[count];

		for (int i = 0; i < count; i++) {
			ret[i] = t.nextToken().trim();
		}
		return ret;
	}
}
/**************************************************************************************************************************************************************************************************************************************************************
 * $Header: /cvs/sakai2/gradebook/tool/src/test/org/sakaiproject/tool/gradebook/test/SpringEnabledTestCase.java,v 1.3 2005/05/26 18:04:55 josh.media.berkeley.edu Exp $
 *************************************************************************************************************************************************************************************************************************************************************/
