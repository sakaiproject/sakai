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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.StringTokenizer;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.jackrabbit.test.AbstractJCRTest;

/**
 * Sets up test data required for level 1 node test cases.
 */
public class NodeTestData extends AbstractJCRTest
{

	/** Path pointing to the test root */
	private static final String TEST_DATA_PATH = "testdata/node";

	/** The encoding for the test resource */
	private static final String ENCODING = "UTF-8";

	/** Resolved QName for nt:resource */
	private String ntResource;

	/** Resolved QName for jcr:encoding */
	private String jcrEncoding;

	/** Resolved QName for jcr:mimeType */
	private String jcrMimeType;

	/** Resolved QName for jcr:data */
	private String jcrData;

	/** Resolved QName for jcr:lastModified */
	private String jcrLastModified;

	/**
	 * Sets up the fixture for this test.
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		ntResource = superuser.getNamespacePrefix(NS_NT_URI) + ":resource";
		jcrEncoding = superuser.getNamespacePrefix(NS_JCR_URI) + ":encoding";
		jcrMimeType = superuser.getNamespacePrefix(NS_JCR_URI) + ":mimeType";
		jcrData = superuser.getNamespacePrefix(NS_JCR_URI) + ":data";
		jcrLastModified = superuser.getNamespacePrefix(NS_JCR_URI) + ":lastModified";
	}

	/**
	 * Creates two nodes under {@link #TEST_DATA_PATH}: one of type nt:resource
	 * and a second node referencing the first.
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

		Node resource = dataRoot.addNode("myResource", ntResource);
		resource.setProperty(jcrEncoding, ENCODING);
		resource.setProperty(jcrMimeType, "text/plain");
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(data, ENCODING);
		writer.write("Hello w\u00F6rld.");
		writer.close();
		resource.setProperty(jcrData, new ByteArrayInputStream(data.toByteArray()));
		resource.setProperty(jcrLastModified, Calendar.getInstance());
		log.println("Adding node: " + resource.getPath());

		Node resReference = dataRoot.addNode("reference");
		resReference.setProperty("ref", resource);
		// make this node itself referenceable
		resReference.addMixin(mixReferenceable);
		log.println("Adding node: " + resReference.getPath());

		Node multiReference = dataRoot.addNode("multiReference");
		Value[] refs = new Value[2];
		refs[0] = superuser.getValueFactory().createValue(resource);
		refs[1] = superuser.getValueFactory().createValue(resReference);
		multiReference.setProperty("ref", refs);
		log.println("Adding node: " + multiReference.getPath());

		superuser.save();
	}
}
