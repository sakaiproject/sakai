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
package org.sakaiproject.jcr.test;

import java.io.IOException;
import java.util.Calendar;
import java.util.StringTokenizer;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.test.AbstractJCRTest;

/**
 * Sets up test data required for level 1 property test cases.
 */
public class PropertyTestData extends AbstractJCRTest
{

	/** Path pointing to the test root */
	private static final String TEST_DATA_PATH = "testdata/property";

	/**
	 * Creates a test node at {@link #TEST_DATA_PATH} with a boolean, double,
	 * long, calendar and a path property.
	 */
	public void testFillInTestData() throws RepositoryException, IOException
	{
		if (superuser.getRootNode().hasNode(TEST_DATA_PATH))
		{
			// delete previous data
			superuser.getRootNode().getNode(TEST_DATA_PATH).remove();
			superuser.save();
		}
		// create nodes to testPath
		StringTokenizer names = new StringTokenizer(TEST_DATA_PATH, "/");
		Node dataRoot = superuser.getRootNode();
		while (names.hasMoreTokens())
		{
			String name = names.nextToken();
			if (!dataRoot.hasNode(name))
			{
				dataRoot = dataRoot.addNode(name, testNodeType);
			}
			else
			{
				dataRoot = dataRoot.getNode(name);
			}
		}

		dataRoot.setProperty("boolean", true);
		dataRoot.setProperty("double", Math.PI);
		dataRoot.setProperty("long", 90834953485278298l);
		Calendar c = Calendar.getInstance();
		c.set(2005, 6, 18, 17, 30);
		dataRoot.setProperty("calendar", c);
		dataRoot.setProperty("path", superuser.getValueFactory().createValue("/",
				PropertyType.PATH));
		dataRoot.setProperty("multi", new String[] { "one", "two", "three" });
		superuser.save();
	}
}
