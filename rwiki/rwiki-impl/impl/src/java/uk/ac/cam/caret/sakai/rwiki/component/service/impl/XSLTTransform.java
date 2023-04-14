/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.component.service.impl;

import java.util.Properties;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.sun.org.apache.xml.internal.serializer.Serializer;
import com.sun.org.apache.xml.internal.serializer.SerializerFactory;

/**
 * Manages a TraxTransform using templates to make it fast to get hold of.
 * 
 * @author ieb
 */
public class XSLTTransform {

	private ClassLoader classLoader;
	private SAXParserFactory parserFactory;
	private SAXTransformerFactory transformerFactory;
	private Templates templates;


	public XSLTTransform() {
		classLoader = this.getClass().getClassLoader();
		transformerFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance(
				"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl",
				classLoader);
		parserFactory = SAXParserFactory.newInstance(
				"com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl",
				classLoader);
	}

	/**
	 * Set the xslt template to use during the transform.
	 * 
	 * @param  xsltResource
	 *         an Input Source to the XSLT
	 * @throws Exception
	 *         typically an exception related to parsing the template
	 */
	public void setXslt(InputSource xsltResource) throws Exception {
		parserFactory.setNamespaceAware(true);
		TemplatesHandler th = transformerFactory.newTemplatesHandler();
		String systemId = xsltResource.getSystemId();
		th.setSystemId(systemId);
		SAXParser parser = parserFactory.newSAXParser();
		XMLReader xr = parser.getXMLReader();
		xr.setContentHandler(th);
		xr.parse(xsltResource);
		templates = th.getTemplates();
	}

	/**
	 * Get the content handler of the transform, this method can also be used to
	 * test if the transform is valid.
	 * 
	 * @return A TransformerHandler object that can process SAX ContentHandler events into a Result.
	 * @throws Exception
	 *         typically an exception because of a configuration error
	 */
	public TransformerHandler getContentHandler() throws Exception {
		return transformerFactory.newTransformerHandler(templates);
	}

	/**
	 * Get the specified serializer for the output method that is specified by the value of the
	 * property associated with the "method" key in the format.
	 *
	 * @param format
	 *        the output format, must set the method property minimally
	 * @return a serializer for the specified output method
	 */
	public Serializer getSerializer(Properties format) {
		Thread currentThread = Thread.currentThread();
		ClassLoader savedLoader = currentThread.getContextClassLoader();
		currentThread.setContextClassLoader(classLoader);
		Serializer serializer = SerializerFactory.getSerializer(format);
		currentThread.setContextClassLoader(savedLoader);
		return serializer;
	}
}
