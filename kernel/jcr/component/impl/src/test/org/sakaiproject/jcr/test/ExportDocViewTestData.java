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
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.test.AbstractJCRTest;

/**
 * <code>ExportDocViewTestData</code> creates nodes named jcr:xmltest with
 * property jcr:xmlcharacters for xml export test.
 */
public class ExportDocViewTestData extends AbstractJCRTest
{

	/**
	 * Path pointing to the test root
	 */
	private static final String TEST_DATA_PATH = "testdata/docViewTest";

	/**
	 * The encoding for the test resource
	 */
	private static final String ENCODING = "UTF-8";

	/**
	 * Resolved QName for jcr:encoding
	 */
	private String jcrEncoding;

	/**
	 * Resolved QName for jcr:mimeType
	 */
	private String jcrMimeType;

	/**
	 * Resolved QName for jcr:lastModified
	 */
	private String jcrLastModified;

	/**
	 * Resolved QName for nt:unstructured
	 */
	private String ntUnstructured;

	/**
	 * Resolved QName for jcr:xmltext
	 */
	private String xmlText;

	/**
	 * Resolved QName for jcr:xmlcharacters
	 */
	private String xmlCharacters;

	private String text = " The entity reference "
			+ "characters: <, ', ,&, >,  \" should be escaped in xml export. ";

	private String simpleText = "A text without any special character.";

	private String invalidXmlPropName = "Prop<>prop";

	private String validXmlPropName = "propName";

	/**
	 * Sets up the fixture for this test.
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		ntUnstructured = superuser.getNamespacePrefix(NS_NT_URI) + ":unstructured";
		xmlText = superuser.getNamespacePrefix(NS_JCR_URI) + ":xmltext";
		xmlCharacters = superuser.getNamespacePrefix(NS_JCR_URI) + ":xmlcharacters";

		jcrEncoding = superuser.getNamespacePrefix(NS_JCR_URI) + ":encoding";
		jcrMimeType = superuser.getNamespacePrefix(NS_JCR_URI) + ":mimeType";
		jcrLastModified = superuser.getNamespacePrefix(NS_JCR_URI) + ":lastModified";
	}

	/**
	 * Creates a node {@link #TEST_DATA_PATH} and three child nodes named
	 * jcr:xmltest with at least a property jcr:xmlcharacters.
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
				dataRoot = dataRoot.addNode(name, ntUnstructured);
			}
			else
			{
				dataRoot = dataRoot.getNode(name);
			}
		}

		fillInInvalidXmlNameNode(dataRoot);
		fillInXmlTextNodes(dataRoot);
		bigNode(dataRoot);

		fillInValidNameProp(dataRoot);
		fillInInvalidXMLNameProp(dataRoot);

		superuser.save();
	}

	/**
	 * Creates nodes jcr:xmltext with prop(s) jcr:xmlcharacters.
	 * 
	 * @param dataRoot
	 * @throws RepositoryException
	 */
	private void fillInXmlTextNodes(Node dataRoot) throws RepositoryException
	{
		// two nodes which should be serialized as xml text in docView export
		// separated with a space
		Node xmltext = dataRoot.addNode(xmlText);
		xmltext.setProperty(xmlCharacters, simpleText);

		dataRoot.addNode("some-element");

		Node xmltext2 = dataRoot.addNode(xmlText);
		xmltext2.setProperty(xmlCharacters, text);

		dataRoot.addNode("some-element");

		// a node which should be serialized as xml text
		Node xmltext4 = dataRoot.addNode(xmlText);
		xmltext4.setProperty(xmlCharacters, simpleText);
	}

	private void fillInValidNameProp(Node dataRoot) throws RepositoryException,
			IOException
	{
		Node node = dataRoot.addNode("validNames");
		validMultiNoBin(node);
		validMultiBin(node);
		validNoBin(node);
		validBin(node);
	}

	private void fillInInvalidXMLNameProp(Node dataRoot) throws RepositoryException,
			IOException
	{
		Node node = dataRoot.addNode("invalidNames");
		invalidMultiNoBin(node);
		invalidMultiBin(node);
		invalidNoBin(node);
		invalidBin(node);
	}

	/**
	 * Creates a node with invalid xml name which has a property also with
	 * invalid xml name.
	 * 
	 * @param dataRoot
	 * @throws RepositoryException
	 */
	private void fillInInvalidXmlNameNode(Node dataRoot) throws RepositoryException
	{
		Node invalidName = dataRoot.addNode("invalidXmlName");
		invalidName.setProperty(validXmlPropName, "some text");
	}

	// create nodes with following properties
	// binary & multival & invalidname
	// binary & multival & validname
	// binary & single & invalidname
	// binary & single & validname
	// notbinary & multival & invalidname
	// notbinary & multival & validname
	// notbinary & single & invalidname
	// notbinary & single & validname

	private void invalidMultiNoBin(Node dataRoot) throws RepositoryException
	{
		Node invalidName = dataRoot.addNode("invalidMultiNoBin");
		String[] values = { "multival text 1", "multival text 2", "multival text 3" };
		invalidName.setProperty(invalidXmlPropName, values);
	}

	private void invalidMultiBin(Node dataRoot) throws RepositoryException
	{
		Node resource = dataRoot.addNode("invalidMultiBin", ntUnstructured);
		resource.setProperty(jcrEncoding, ENCODING);
		resource.setProperty(jcrMimeType, "text/plain");
		String[] values = { "SGVsbG8gd8O2cmxkLg==", "SGVsbG8gd8O2cmxkLg==" };
		resource.setProperty(invalidXmlPropName, values, PropertyType.BINARY);
		resource.setProperty(jcrLastModified, Calendar.getInstance());
	}

	private void invalidNoBin(Node dataRoot) throws RepositoryException
	{
		Node invalidName = dataRoot.addNode("invalidNoBin");
		String value = "text 1";
		invalidName.setProperty(invalidXmlPropName, value);
	}

	private void invalidBin(Node dataRoot) throws RepositoryException, IOException
	{
		Node resource = dataRoot.addNode("invalidBin", ntUnstructured);
		resource.setProperty(jcrEncoding, ENCODING);
		resource.setProperty(jcrMimeType, "text/plain");
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(data, ENCODING);
		writer.write("Hello w\u00F6rld.");
		writer.close();
		resource.setProperty(invalidXmlPropName, new ByteArrayInputStream(data
				.toByteArray()));
		resource.setProperty(jcrLastModified, Calendar.getInstance());
	}

	private void validMultiNoBin(Node dataRoot) throws RepositoryException
	{
		Node validName = dataRoot.addNode("validMultiNoBin");
		String[] values = { "multival text 1", "multival text 2", "multival text 3" };
		validName.setProperty(validXmlPropName, values);
	}

	private void validMultiBin(Node dataRoot) throws RepositoryException
	{
		Node resource = dataRoot.addNode("validMultiBin", ntUnstructured);
		resource.setProperty(jcrEncoding, ENCODING);
		resource.setProperty(jcrMimeType, "text/plain");
		String[] values = { "SGVsbG8gd8O2cmxkLg==", "SGVsbG8gd8O2cmxkLg==" };
		resource.setProperty("jcrData", values, PropertyType.BINARY);
		resource.setProperty(jcrLastModified, Calendar.getInstance());
	}

	private void validNoBin(Node dataRoot) throws RepositoryException
	{
		Node validName = dataRoot.addNode("validNoBin");
		String value = "text 1";
		validName.setProperty(validXmlPropName, value);
	}

	private void validBin(Node dataRoot) throws RepositoryException, IOException
	{
		Node resource = dataRoot.addNode("validBin", ntUnstructured);
		resource.setProperty(jcrEncoding, ENCODING);
		resource.setProperty(jcrMimeType, "text/plain");
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(data, ENCODING);
		writer.write("Hello w\u00F6rld.");
		writer.close();
		resource.setProperty("jcrData", new ByteArrayInputStream(data.toByteArray()));
		resource.setProperty(jcrLastModified, Calendar.getInstance());
	}

	private void bigNode(Node dataRoot) throws RepositoryException
	{
		Node node = dataRoot.addNode("bigNode", ntUnstructured);

		String[] binVals = { "SGVsbG8gd8O2cmxkLg==", "SGVsbG8gd8O2cmxkLg==" };
		node.setProperty(validXmlPropName + "0", binVals, PropertyType.BINARY);
		String value = "text 1";
		node.setProperty(validXmlPropName + "1", value);
		String[] valuess = { "multival text 1", "multival text 2", "multival text 3" };
		node.setProperty(validXmlPropName + "2", valuess);
		String value2 = "text 1";
		node.setProperty(validXmlPropName + "3", value2);
	}
}