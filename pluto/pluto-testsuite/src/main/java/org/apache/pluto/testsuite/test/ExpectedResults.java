/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.pluto.testsuite.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.pluto.testsuite.InvalidConfigurationException;

/**
 * A Singleton which loads a properties file containing data expected by the
 * tests in the testsuite.
 */
public class ExpectedResults {

	/** The file name of properties holding expected results. */
	public static final String PROPERTY_FILENAME = "expectedResults.properties";

	/** The static singleton instance. */
	private static ExpectedResults instance;


	// Private Member Variables ------------------------------------------------

	/** The nested properties. */
	private final Properties properties;


	// Constructor -------------------------------------------------------------

	/**
	 * Private constructor that prevents external instantiation.
	 * @throws IOException  if fail to load properties from file.
	 */
	private ExpectedResults() throws IOException {
		properties = new Properties();
		InputStream in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(PROPERTY_FILENAME);
		if (in != null) {
			properties.load(in);
		} else {
			throw new IOException("Could not find " + PROPERTY_FILENAME);
		}
	}

	/**
	 * Returns the singleton expected results instance.
	 * @return the singleton expected results instance.
	 * @throws InvalidConfigurationException  if fail to read properties file.
	 */
	public static ExpectedResults getInstance()
	throws InvalidConfigurationException {
		if (instance == null) {
			try {
				instance = new ExpectedResults();
			} catch (IOException ex) {
				throw new InvalidConfigurationException("Error reading file "
						+ PROPERTY_FILENAME + ": " + ex.getMessage());
			}
		}
		return instance;
	}


	// Public Methods ----------------------------------------------------------

	public String getMajorVersion() {
		return properties.getProperty("expected.version.major");
	}

	public String getMinorVersion() {
		return properties.getProperty("expected.version.minor");
	}

	public String getServerInfo() {
		return properties.getProperty("expected.serverInfo");
	}

	public String getPortalInfo() {
		return properties.getProperty("expected.portalInfo");
	}

	public String getMappedSecurityRole() {
		return properties.getProperty("expected.security.role.mapped");
	}

	public String getUnmappedSecurityRole() {
        return properties.getProperty("expected.security.role");
	}

}


