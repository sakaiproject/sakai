/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.custom.transform;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URL;

import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.ValueBinding;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Transforms XML using and XSL stylesheet.
 * 
 * Used to transform XML (from either a String or URL) using either XSLT or
 * Velocity.
 *
 * @JSFComponent
 *   name = "t:xmlTransform"
 *   class = "org.apache.myfaces.custom.transform.XmlTransform"
 *   tagClass = "org.apache.myfaces.custom.transform.XmlTransformTag"
 * @since 1.1.7
 * @author Sean Schofield
 */
public abstract class AbstractXmlTransform extends UIComponentBase
{
    public static final String COMPONENT_TYPE = "org.apache.myfaces.tomahawk.XmlTransform";
    public static final String COMPONENT_FAMILY = "org.apache.myfaces.tomahawk.Transform";

    private Object contentStream;
    private Object styleStream;
    
    // see superclass for documentation
    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }

    public void encodeBegin(FacesContext context)
            throws IOException
    {
        InputStream xmlStream = (InputStream)getContentStream();
        String xml = getContent();
        String xmlLocation = getContentLocation();

        InputStream xslStream = (InputStream)getStyleStream();
        String xsl = getStylesheet();
        String xslLocation = getStylesheetLocation();

        if (context == null) throw new NullPointerException("context");
        if (!isRendered()) return;

        if (getContent() == null && getContentLocation() == null && getContentStream() == null)
            throw new NullPointerException("content/contentLocation/contentStream cannot all be null");

        //TODO - handle all cases
        if (xmlLocation != null)
        {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (loader == null)
            {
                loader = AbstractXmlTransform.class.getClassLoader();
            }

            URL url = loader.getResource(xmlLocation);
            xmlStream = new FileInputStream(new File(URI.create(url.toString())));
        }

        if (xslLocation != null)
        {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (loader == null)
            {
                loader = AbstractXmlTransform.class.getClassLoader();
            }

            URL url = loader.getResource(xslLocation);
            xslStream = new FileInputStream(new File(URI.create(url.toString())));
        }

        if (xml != null)
        {
            xmlStream = new ByteArrayInputStream(xml.getBytes());
        }

        if (xsl != null)
        {
            xslStream = new ByteArrayInputStream(xsl.getBytes());
        }

        if (xmlStream != null && xslStream != null)
        {
            transformContent(xmlStream, xslStream);
        }
    }

    /**
     * Transforms an XML string using the stylesheet string provided.
     *
     * @param content The XML to transform
     * @param stylesheet The stylesheet to use in the transformation
     * @throws IOException
     */
    private void transformContent(InputStream content, InputStream stylesheet)
        throws IOException
    {
        try
        {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer(new StreamSource(stylesheet));

            Writer responseWriter = FacesContext.getCurrentInstance().getResponseWriter();
            transformer.transform(new StreamSource(content), new StreamResult(responseWriter));
        }
        catch (TransformerException te)
        {
            throw new IOException("Error while transforming XML: " + te.getMessage());
        }
    }

    // component does not need to manage its own children (its not allowed to have any)
    public void encodeChildren(FacesContext context)
            throws IOException
    {}

    // nothing special to do here
    public void encodeEnd(FacesContext context)
            throws IOException
    {}

    /**
     * String containing the XML content to be transformed.
     * 
     * @JSFProperty
     */
    public abstract String getContent();

    /**
     * String containing the location of the XML content to be transformed.
     * 
     * @JSFProperty
     */
    public abstract String getContentLocation();

    /**
     * String containing the XSL information to use in the transformation.
     * 
     * @JSFProperty
     */
    public abstract String getStylesheet();
    
    public void setContentStream(Object contentStream)
    {
        this.contentStream = contentStream;
    }

    /**
     * Value binding expression referencing an InputStream from which the XML content is to be read.
     * 
     * @JSFProperty
     */
    public Object getContentStream()
    {
        if (contentStream != null) return contentStream;

        ValueBinding vb = getValueBinding("contentStream");
        return (vb != null) ? vb.getValue(getFacesContext()) : null;
    }
    

    /**
     * String containing the location of the XSL stylesheet to use in the transformation.
     * 
     * @JSFProperty
     */
    public abstract String getStylesheetLocation();
  
    public void setStyleStream(Object styleStream)
    {
        this.styleStream = styleStream;
    }

    /**
     * Value binding expression referencing an InputStream from which the XSL stylesheet is to be read.
     * 
     * @JSFProperty
     */
    public Object getStyleStream()
    {
        if (styleStream != null) return styleStream;

        ValueBinding vb = getValueBinding("styleStream");
        return (vb != null) ? vb.getValue(getFacesContext()) : null;
    }
    
}
