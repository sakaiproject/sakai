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

import java.util.StringTokenizer;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.test.AbstractJCRTest;

/**
 * Sets up test data required for level 1 query test cases.
 */
public class QueryTestData extends AbstractJCRTest
{

	/** Path pointing to the test root */
	private static final String TEST_DATA_PATH = "testdata/query";

	/**
	 * Creates four nodes under {@link #TEST_DATA_PATH}. Three nodes with name
	 * {@link #nodeName1} and a fourth with name {@link #nodeName2}. Each node
	 * has a String property named {@link #propertyName1} with some content set.
	 */
	public void testFillInSearchData() throws RepositoryException
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

		Node n1 = dataRoot.addNode(nodeName1);
		log.println("Adding node: " + n1.getPath());
		Node n2 = dataRoot.addNode(nodeName1);
		log.println("Adding node: " + n2.getPath());
		Node n3 = dataRoot.addNode(nodeName1);
		log.println("Adding node: " + n3.getPath());
		Node n4 = dataRoot.addNode(nodeName2);
		log.println("Adding node: " + n4.getPath());

		n1.setProperty(propertyName1, "You can have it good, cheap, or fast. Any two.");
		n2.setProperty(propertyName1, "foo bar");
		n3.setProperty(propertyName1, "Hello world!");
		n4.setProperty(propertyName1, "Apache Jackrabbit");
		superuser.save();
	}
}
