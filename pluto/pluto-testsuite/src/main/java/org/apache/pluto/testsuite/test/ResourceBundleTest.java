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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.portlet.PortletConfig;
import javax.portlet.PortletRequest;

import org.apache.pluto.testsuite.TestResult;

/**
 * Tests basic attribute retrieval and storage functions within
 * the portlet request, session, and context objects.
 *
 */
public class ResourceBundleTest extends AbstractReflectivePortletTest {

	// Static Constant Definitions ---------------------------------------------

	/** Key for portlet title defined in portlet.xml/init-param. */
    private static final String TITLE_KEY = "javax.portlet.title";

	/** Key for portlet short title defined in portlet.xml/init-param. */
    private static final String SHORT_TITLE_KEY = "javax.portlet.short-title";

	/** Key for portlet keywords defined in portlet.xml/init-param. */
    private static final String KEYWORDS_KEY = "javax.portlet.keywords";

    /**
     * Parameter name for if resource bundle is declared in
     * <code>testsuite-config/init-param</code>.
     */
    private static final String BUNDLE_DECLARED_PARAM = "resource-bundle";

    /**
     * Parameter name for portlet title in
     * <code>testsuite-config/init-param</code>.
     */
    private static final String TITLE_PARAM = "title";

    /**
     * Parameter name for portlet short title in
     * <code>testsuite-config/init-param</code>.
     */
    private static final String SHORT_TITLE_PARAM = "short-title";

    /**
     * Parameter name for portlet keywords in
     * <code>testsuite-config/init-param</code>.
     */
    private static final String KEYWORDS_PARAM = "keywords";


    // Test Methods ------------------------------------------------------------

    protected TestResult checkResourceBundleExists(PortletConfig config,
                                                   PortletRequest request) {
        TestResult result = new TestResult();
        result.setDescription("Ensure the resource bundle is not null.");

        ResourceBundle bundle = config.getResourceBundle(request.getLocale());
        if (bundle != null) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	result.setReturnCode(TestResult.FAILED);
            result.setResultMessage("Unable to retrieve resource bundle "
            		+ "for locale: " + request.getLocale());
        }
        return result;
    }

    protected TestResult checkGetNames(PortletConfig config,
                                       PortletRequest request) {
        TestResult result = new TestResult();
        result.setDescription("Retrieve the property names and ensure that "
        		+ "the required keys are present.");

        List requiredKeys = new ArrayList();
        requiredKeys.add(TITLE_KEY);
        requiredKeys.add(SHORT_TITLE_KEY);
        requiredKeys.add(KEYWORDS_KEY);

        ResourceBundle bundle = config.getResourceBundle(request.getLocale());
        if (bundle == null) {
        	result.setReturnCode(TestResult.WARNING);
        	result.setResultMessage("A function upon which this test depends "
        			+ "failed to execute as expected. "
        			+ "Check the other test results in this test suite.");
        	return result;
        }

        for (Enumeration en = bundle.getKeys(); en.hasMoreElements(); ) {
            String key = (String) en.nextElement();
            requiredKeys.remove(key);
        }

        if (requiredKeys.isEmpty()) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	result.setReturnCode(TestResult.FAILED);
            StringBuffer buffer = new StringBuffer();
            for (Iterator it = requiredKeys.iterator(); it.hasNext(); ) {
            	buffer.append(it.next()).append(", ");
            }
            result.setResultMessage("Required keys [" + buffer.toString()
            		+ "] are missing in the resource bundle.");
        }
        return result;
    }

    protected TestResult checkGetGermanBundle(PortletConfig config,
                                              PortletRequest request) {
        return doGenericLocaleRequiredFields(config,
                                             request,
                                             Locale.GERMAN);
    }

    protected TestResult checkGetEnglishBundle(PortletConfig config,
                                               PortletRequest request) {
        return doGenericLocaleRequiredFields(config,
                                             request,
                                             Locale.ENGLISH);
    }

    /*
    protected TestResult checkGetDfltBundle(PortletConfig config,
                                            PortletRequest req) {
        return doGenericLocaleRequiredFields(config, req, new Locale("dflt"));
    }
    */


    // Private Methods ---------------------------------------------------------

    private TestResult doGenericLocaleRequiredFields(PortletConfig config,
                                                     PortletRequest request,
                                                     Locale locale) {
        TestResult result = new TestResult();
        result.setDescription("Retrieve the title and ensure it's set properly "
        		+ "under locale " + locale);

        // Retrieve title, short title and keywords from portlet resource bundle.
        ResourceBundle bundle = config.getResourceBundle(locale);
        if (bundle == null) {
        	result.setReturnCode(TestResult.WARNING);
        	result.setResultMessage("A function upon which this test depends "
        			+ "failed to execute as expected. "
        			+ "Check the other test results in this test suite.");
        	return result;
        }
        String title = bundle.getString(TITLE_KEY);
        String shortTitle = bundle.getString(SHORT_TITLE_KEY);
        String keywords = bundle.getString(KEYWORDS_KEY);

        // Retrieve expected title, short title and keywords from test config.
        String suffix = isBundleDeclared() ? ("_" + locale.getLanguage()) : "";
        Map initParams = getInitParameters();
        String expectedTitle = (String) initParams.get(
        		TITLE_PARAM + suffix);
        String expectedShortTitle = (String) initParams.get(
        		SHORT_TITLE_PARAM + suffix);
        String expectedKeywords = (String) initParams.get(
        		KEYWORDS_PARAM + suffix);

        // Assert that values retrieved from resource bundler are expected.
        boolean inconsistent = false;
        StringBuffer buffer = new StringBuffer();
        buffer.append("The following information is not correct: ");
        if (title == null || expectedTitle == null
        		|| !title.trim().equals(expectedTitle.trim())) {
        	inconsistent = true;
            buffer.append("Inconsistent title: '")
            		.append(title).append("' != '")
            		.append(expectedTitle).append("'; ");
        }
        if (shortTitle == null || expectedShortTitle == null
        		|| !shortTitle.trim().equals(expectedShortTitle.trim())) {
        	inconsistent = true;
            buffer.append("Inconsistent short title: '")
            		.append(shortTitle).append("' != '")
            		.append(expectedShortTitle).append("'; ");
        }
        if (keywords == null || expectedKeywords == null
        		|| !keywords.trim().equals(expectedKeywords.trim())) {
        	inconsistent = true;
            buffer.append("Inconsistent keywords: '")
            		.append(keywords).append("' != '")
            		.append(expectedKeywords).append("'; ");
        }

        if (!inconsistent) {
        	result.setReturnCode(TestResult.PASSED);
        } else {
        	result.setReturnCode(TestResult.FAILED);
            result.setResultMessage(buffer.toString());
        }
        return result;
    }

    private boolean isBundleDeclared() {
        String bundleDeclared = (String) getInitParameters().get(
        		BUNDLE_DECLARED_PARAM);
        if (Boolean.TRUE.toString().equalsIgnoreCase(bundleDeclared)) {
            return true;
        } else {
        	return false;
        }
    }





}
