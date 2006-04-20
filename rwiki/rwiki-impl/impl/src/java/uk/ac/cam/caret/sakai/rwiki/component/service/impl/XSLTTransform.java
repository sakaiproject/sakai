/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
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

package uk.ac.cam.caret.sakai.rwiki.component.service.impl;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Manages a TraxTransform using templates to make it fast to get hold of. This
 * class is NOT thread safe and should be cached in the Thread Local
 * 
 * @author ieb
 */
public class XSLTTransform
{

	private Templates templates = null;

	private SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory
			.newInstance();

	/**
	 * Set the xslt resource.
	 * 
	 * @param xsltresource
	 *        an Input Source to the XSLT
	 * @throws Exception
	 */
	public void setXslt(InputSource xsltresource) throws Exception
	{
		TemplatesHandler th = factory.newTemplatesHandler();
		String systemId = xsltresource.getSystemId();
		th.setSystemId(systemId);
		XMLReader xr = XMLReaderFactory
				.createXMLReader("org.apache.xerces.parsers.SAXParser");
		xr.setContentHandler(th);
		xr.parse(xsltresource);
		templates = th.getTemplates();
	}

	/**
	 * get the output content handler, configured with the writer, ready for
	 * pumping sax events into
	 * 
	 * @param out
	 *        the output stream
	 * @return a content handler configured to produce output
	 * @throws Exception
	 */
	public ContentHandler getOutputHandler(Writer out, Map outputProperties)
			throws Exception
	{
		TransformerHandler saxTH = factory.newTransformerHandler(templates);
		Result r = new StreamResult(out);
		if (outputProperties != null)
		{
			Transformer trans = saxTH.getTransformer();
			for (Iterator i = outputProperties.keySet().iterator(); i.hasNext();)
			{
				String name = (String) i.next();
				String value = (String) outputProperties.get(name);
				trans.setOutputProperty(name, value);

			}

			// String s = OutputKeys.INDENT;
		}
		saxTH.setResult(r);
		return saxTH;
	}

	public ContentHandler getOutputHandler(OutputStream out,
			final Map outputProperties) throws Exception
	{

		TransformerHandler saxTH = factory.newTransformerHandler(templates);
		Properties p = OutputPropertiesFactory
				.getDefaultMethodProperties("xml");
		if (outputProperties != null && outputProperties.size() > 0)
		{
			p.putAll(outputProperties);
		}

		Serializer serializer = null;

		String className = p
				.getProperty(OutputPropertiesFactory.S_KEY_CONTENT_HANDLER);

		if (null == className)
		{
			throw new IllegalArgumentException(
					"The output format must have a '"
							+ OutputPropertiesFactory.S_KEY_CONTENT_HANDLER
							+ "' property!");
		}

		serializer = (Serializer) Class.forName(className).newInstance();
		serializer.setOutputFormat(p);

		serializer.setOutputStream(out);

		Result r = new SAXResult(serializer.asContentHandler());

		saxTH.setResult(r);
		return saxTH;
	}

	/**
	 * Get the content handler of the transform, this method can also be used to
	 * test if the transform is valid.
	 * 
	 * @return
	 * @throws Exception
	 */
	public ContentHandler getContentHandler() throws Exception
	{
		TransformerHandler saxTH = factory.newTransformerHandler(templates);
		return saxTH;
	}
}
