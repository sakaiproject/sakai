/**
 * Copyright (c) 2003-2019 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.contentreview.service;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.sakaiproject.contentreview.exception.ContentReviewProviderException;
import org.sakaiproject.util.ResourceLoader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Generally in Sakai, i18n happens at the moment when the user is accessing content - we invoke ResourceLoaders to get formatted messages using the locale that is appropriate to the user.
 * Occasionally, we nest a few calls to ResourceLoader.getFormattedMessage(...), for instance the message:
 * "An error has occurred with the service. Error code: 42; cause: A Sakaiger ate the paper" might be coded as follows:
 *
 * example.properties:
 *     service.error=An error has occurred with the service. Error code: {0}; cause: {1}
 *     service.sakaiger.ate.paper=A Sakaiger ate the paper
 * ExampleContentReviewServiceImpl.java:
 *     message = rb.getFormattedMessage("service.error", 42, rb.getFormattedMessage("service.sakaiger.ate.paper"));
 *
 * We have an i18n use case in ContentReviewService that's slightly more complex:
 * We persist error messages at the time that they are encountered (usually in a quartz job using the admin session), and these messages have to be displayed to end users who are potentially using varying locales.
 * In order to accomplish this, we have to persist a model that represents any nesting of formatted messages, and all of their relevant arguments.
 * Then when users of varying locales access the last error of their originality report, we can interpret our persisted model, and serve a helpful error message in the language appropriate to each individual.
 *
 * This class creates XML models representing the keys and arguments of what would normally be point-in-time calls to rb.getFormattedMessage()
 */
public class I18nXmlUtility
{
	private static final String TAG_NAME_MESSAGE = "message";
	private static final String TAG_NAME_KEY = "key";
	private static final String TAG_NAME_ARG = "arg";

	private static DocumentBuilderFactory documentBuilderFactory = null;
	private static DocumentBuilder documentBuilder = null;

	/**
	 * Gets a DocumentBuilder instance
	 */
	public static DocumentBuilder getDocumentBuilder() {
		try {
			if (documentBuilderFactory == null) {
				documentBuilderFactory = DocumentBuilderFactory.newInstance();
			}
			if (documentBuilder == null) {
				documentBuilder = documentBuilderFactory.newDocumentBuilder();
			}

			return documentBuilder;
		}
		catch (Exception e) {
			throw new ContentReviewProviderException("Failed to produce an XML Document Builder", e);
		}
	}

	/**
	 * Creates a new XML Document instance
	 */
	public static Document createXmlDocument() {
		return getDocumentBuilder().newDocument();
	}

	/**
	 * Adds the specified element to the XML document and returns the document's contents as a String
	 */
	public static String addElementAndGetDocumentAsString(Document doc, Element el)
	{
		doc.appendChild(el);

		try {
			TransformerFactory tranFactory = TransformerFactory.newInstance();
			Transformer aTransformer = tranFactory.newTransformer();
			Source src = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			Result dest = new StreamResult(writer);
			aTransformer.transform(src, dest);
			String result = writer.getBuffer().toString();
			return result;
		}
		catch (Exception e) {
			throw new ContentReviewProviderException("Failed to transform the XML Document into a String");
		}
	}

	/**
	 * Creates XML that represents a call to ResourceLoader.getFormattedMessage().
	 * We define 'message' tags, which contain a 'key' tag followed by any number of 'arg' tags, or additional 'message' tags when nesting occurs.
	 *
	 * Consider the example:
	 * example.properties:
	 *     service.error=An error has occurred with the service. Error code: {0}; cause: {1}
	 *     service.sakaiger.ate.paper=A Sakaiger ate the paper
	 * ExampleContentReviewServiceImpl.java:
	 *     message = rb.getFormattedMessage("service.error", 42, rb.getFormattedMessage("service.sakaiger.ate.paper"));
	 *
	 * This would be translated to:
	 * createFormattedMessageXML(doc, "service.error", 42, createFormattedMessageXML(doc, "service.sakaiger.ate.paper"))
	 * and the returned node would appear as follows:
	 * <message>
	 *     <key>service.error</key>
	 *     <arg>42</arg>
	 *     <message>
	 *         <key>service.sakaiger.ate.paper</key>
	 *     </message>
	 * </message>
	 *
	 * Notice that nesting is achieved by passing an invocation of this method as an arg
	 *
	 * Throws a ContentReviewProviderException if document is null
	 *
	 * @param document the document that will be used to create XML
	 * @param key the formatted message key
	 * @param args the arguments to the formatted message. Elements are appended as is (assumed to be nested 'message' tags); everything else is embedded into 'arg' tags
	 * @return an XML element representing the i18n message key and arguments
	 */
	protected static Object createFormattedMessageXML(Document document, String key, Object... args) {
		if (document == null) {
			throw new ContentReviewProviderException("createFormattedMessageXML invoked with null document");
		}

		Element message = document.createElement(TAG_NAME_MESSAGE);

		Element eKey = document.createElement(TAG_NAME_KEY);
		eKey.setTextContent(key);

		message.appendChild(eKey);

		for (Object arg : args) {
			if (arg == null) {
				arg = "";
			}
			if (arg instanceof Element) {
				message.appendChild((Element)arg);
			}
			else {
				Element eArg = document.createElement(TAG_NAME_ARG);
				eArg.setTextContent(String.valueOf(arg));
				message.appendChild(eArg);
			}
		}

		return message;
	}

	/**
	 * Parses an XML node expectedly created via createFormattedMessageXML, and localizes it into a user presentable message using the specified ResourceLoader
	 */
	public static String getLocalizedMessage(ResourceLoader rb, Node node) {
		String nodeName = node.getNodeName();

		switch (nodeName) {
			case TAG_NAME_MESSAGE:
				Node keyNode = node.getFirstChild();
				if (!TAG_NAME_KEY.equals(keyNode.getNodeName())) {
					throw new ContentReviewProviderException("XML is malformed; first child of \"message\" tag expected \"key\", but got: " + keyNode.getNodeName());
				}
				String key = keyNode.getTextContent();

				List<String> args = new ArrayList<>();
				Node arg = keyNode.getNextSibling();
				while (arg != null) {
					args.add(getLocalizedMessage(rb, arg));
					arg = arg.getNextSibling();
				}
				String[] argsArray = args.toArray(new String[args.size()]);
				return rb.getFormattedMessage(key, argsArray);
			case TAG_NAME_ARG:
				return node.getTextContent();
			default:
				// Will throw exception below
				break;
		}
		throw new ContentReviewProviderException("XML is malformed; got unexpected tag: " + nodeName);
	}
}
