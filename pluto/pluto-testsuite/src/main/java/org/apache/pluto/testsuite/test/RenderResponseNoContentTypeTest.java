/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pluto.testsuite.test;

import java.io.IOException;

import javax.portlet.PortletResponse;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.testsuite.TestResult;
import org.apache.pluto.testsuite.TestUtils;

/**
 * FIXME: separate the two check methods to two classes.
 *
 */
public class RenderResponseNoContentTypeTest
extends AbstractReflectivePortletTest {

	private static final Log LOG = LogFactory.getLog(RenderResponseNoContentTypeTest.class);

	/**
	 * Overwrite super implementation to return null. This test requires that
	 * content type of the render response is not set.
	 * @return <code>null</code>.
	 */
	public String getRenderResponseContentType() {
		return null;
	}

	// Test Methods ------------------------------------------------------------

	protected TestResult checkGetPortletOutputStream(PortletResponse response) {
		TestResult result = new TestResult();
		result.setDescription("Ensure If the getPortletOutputStream() method "
				+ "is called before the setContentType() method, it will throw "
				+ "an IllegalStateException.");
		result.setSpecPLT("12.3.1");

		RenderResponse renderResponse = (RenderResponse) response;
		ensureContentTypeNotSet(renderResponse, result);
		if (result.getReturnCode() == TestResult.WARNING) {
			return result;
		}

		try {
			renderResponse.getPortletOutputStream();
			result.setReturnCode(TestResult.FAILED);
			result.setResultMessage("Method getPortletOutputStream() didn't "
					+ "throw an IllegalStateException when content type is "
					+ "not set before.");
		} catch (IllegalStateException ex) {
			// We are expecting this exception!
			result.setReturnCode(TestResult.PASSED);
		} catch (IOException ex) {
			TestUtils.failOnException("Method getPortletOutputStream() throws "
					+ "an unexpected IOException", ex, result);
		}
		return result;
	}

	protected TestResult checkGetWriter(PortletResponse response) {
		TestResult result = new TestResult();
		result.setDescription("Ensure If the getWriter() method "
				+ "is called before the setContentType() method, it will throw "
				+ "an IllegalStateException.");
		result.setSpecPLT("12.3.1");

		RenderResponse renderResponse = (RenderResponse) response;
		ensureContentTypeNotSet(renderResponse, result);
		if (result.getReturnCode() == TestResult.WARNING) {
			return result;
		}

		try {
			renderResponse.getWriter();
			result.setReturnCode(TestResult.FAILED);
			result.setResultMessage("Method getWriter() didn't "
					+ "throw an IllegalStateException when content type is "
					+ "not set before.");
		} catch (IllegalStateException ex) {
			// We are expecting this exception!
			result.setReturnCode(TestResult.PASSED);
		} catch (IOException ex) {
			TestUtils.failOnException("Method getWriter() throws "
					+ "an unexpected IOException", ex, result);
		}
		return result;
	}


	// Private Methods ---------------------------------------------------------

	/**
	 * Ensures that the content type of the current render response is not set.
	 * If the content type is already set, this method sets the test result to
	 * WARNING with a warning message. Otherwise, this method does nothing.
	 * @param response  the render response to check.
	 * @param result  the test result.
	 */
	private void ensureContentTypeNotSet(RenderResponse response,
	                                     TestResult result) {
		String contentType = response.getContentType();
		if (contentType != null) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("Unable to run test: content type is already set ("
						+ contentType + ").");
			}
        	result.setReturnCode(TestResult.WARNING);
        	result.setResultMessage("The content type of the render response "
        			+ "is not as expected (" + contentType + " != null).");
		}
	}

}
