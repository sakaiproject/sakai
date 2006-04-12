/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2006 University of Cambridge
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.xml.serializer.DOMSerializer;
import org.apache.xml.serializer.Serializer;
import org.sakaiproject.service.framework.config.cover.ServerConfigurationService;
import org.sakaiproject.service.legacy.content.ContentResource;
import org.sakaiproject.service.legacy.content.cover.ContentHostingService;
import org.sakaiproject.service.legacy.entity.Reference;
import org.sakaiproject.service.legacy.resource.cover.EntityManager;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

public class BaseFOPSerializer implements Serializer {

	private static final Log logger = LogFactory
			.getLog(BaseFOPSerializer.class);

	private static final String configfile = "/uk/ac/cam/caret/sakai/rwiki/component/service/impl/fop.cfg.xml";

	private Properties outputFormat = null;

	private Writer writer = null;

	private OutputStream outputStream = null;

	private Fop fop = null;

	protected String mimeType = MimeConstants.MIME_PDF;

	/**
	 * 
	 * {@inheritDoc}
	 */
	public void setOutputStream(OutputStream arg0) {
		this.outputStream = arg0;
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	public OutputStream getOutputStream() {
		return outputStream;
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	public void setWriter(Writer arg0) {
		this.writer = arg0;
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	public Writer getWriter() {
		return writer;
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	public void setOutputFormat(Properties arg0) {
		this.outputFormat = arg0;
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	public Properties getOutputFormat() {
		return outputFormat;
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	public ContentHandler asContentHandler() throws IOException {
		if (fop == null) {
			try {
				DefaultConfigurationBuilder cfgBuild = new DefaultConfigurationBuilder();
				Configuration cfg = cfgBuild.build(getClass()
						.getResourceAsStream(configfile));
				FopFactory ff = FopFactory.newInstance();
				ff.setUserConfig(cfg);
				FOUserAgent userAgent = new FOUserAgent(ff);

				userAgent.setURIResolver(new URIResolver() {

					public Source resolve(String href, String base)
							throws TransformerException {
						Source source = null;
						try {
							logger.info("Resolving " + href + " from " + base);
							HttpServletRequest request = XSLTEntityHandler
									.getCurrentRequest();
							if (request != null && href.startsWith("/access")) {
								// going direct into the ContentHandler Service

								try {
									String path = href.substring("/access"
											.length());

									Reference ref = EntityManager
											.newReference(path);
									ContentResource resource = ContentHostingService
											.getResource(ref.getId());
									String headers = "Content-type: "
											+ resource.getContentType()
											+ "\nContent-length: "
											+ resource.getContentLength()
											+ "\n\n";
									byte[] output = new byte[headers.length()
											+ resource.getContentLength()];
									System.arraycopy(headers.getBytes(), 0,
											output, 0, headers.length());
									System.arraycopy(resource.getContent(), 0,
											output, headers.length(), resource
													.getContentLength());
									return new StreamSource(resource
											.streamContent());
								} catch (Exception ex) {
									URI uri = new URI(base);
									String content = uri.resolve(href)
											.toString();
									source = new StreamSource(content);
								}
							} else {
								URI uri = new URI(base);
								String content = uri.resolve(href).toString();
								source = new StreamSource(content);
							}
						} catch (Exception ex) {
							throw new TransformerException("Failed to get "
									+ href, ex);
						}
						return source;
					}

				});
				try {
					userAgent.setBaseURL(ServerConfigurationService
							.getString("serverUrl"));
				} catch (Throwable ex) {
				}

				fop = ff.newFop(mimeType, userAgent,outputStream);
			} catch (Exception e) {
				new IOException("Failed to create " + mimeType
						+ " Serializer: " + e.getMessage());
			}
		}
		DefaultHandler dh;
		try {

			dh = fop.getDefaultHandler();

		} catch (FOPException e) {
			throw new RuntimeException("Failed to get FOP Handler ", e);
		}

		return dh;
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	public DOMSerializer asDOMSerializer() throws IOException {
		return null;
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	public boolean reset() {
		fop = null;
		outputFormat = null;
		writer = null;
		outputStream = null;
		return false;
	}

}
