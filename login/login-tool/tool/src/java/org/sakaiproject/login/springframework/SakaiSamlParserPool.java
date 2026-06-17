/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.login.springframework;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;

import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.sakaiproject.util.Xml;
import org.springframework.beans.factory.InitializingBean;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SakaiSamlParserPool implements ParserPool, InitializingBean {

    private static final EntityResolver BLOCKING_ENTITY_RESOLVER = (publicId, systemId) -> {
        throw new SAXException("External XML entities are disabled");
    };

    private DocumentBuilderFactory builderFactory;
    private Schema schema;

    @Override
    public void afterPropertiesSet() throws Exception {
        initialize();
    }

    public synchronized void initialize() throws XMLParserException {
        try {
            builderFactory = Xml.createSecureDocumentBuilderFactory(schema);
        } catch (ParserConfigurationException e) {
            throw new XMLParserException("Unable to configure XML document builder factory", e);
        }
    }

    @Override
    public DocumentBuilder getBuilder() throws XMLParserException {
        DocumentBuilderFactory factory = getInitializedFactory();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(BLOCKING_ENTITY_RESOLVER);
            return builder;
        } catch (ParserConfigurationException e) {
            throw new XMLParserException("Unable to create XML document builder", e);
        }
    }

    @Override
    public void returnBuilder(DocumentBuilder builder) {
        // Builders are intentionally not pooled; construction is cheap for SAML login traffic.
    }

    @Override
    public Document newDocument() throws XMLParserException {
        Document document = getBuilder().newDocument();
        if (document == null) {
            throw new XMLParserException("DocumentBuilder returned a null Document");
        }
        return document;
    }

    @Override
    public Document parse(InputStream input) throws XMLParserException {
        try {
            Document document = getBuilder().parse(input);
            if (document == null) {
                throw new XMLParserException("DocumentBuilder parsed a null Document");
            }
            return document;
        } catch (SAXException e) {
            throw new XMLParserException("Invalid XML", e);
        } catch (IOException e) {
            throw new XMLParserException("Unable to read XML from input stream", e);
        }
    }

    @Override
    public Document parse(Reader input) throws XMLParserException {
        try {
            Document document = getBuilder().parse(new InputSource(input));
            if (document == null) {
                throw new XMLParserException("DocumentBuilder parsed a null Document");
            }
            return document;
        } catch (SAXException e) {
            throw new XMLParserException("Invalid XML", e);
        } catch (IOException e) {
            throw new XMLParserException("Unable to read XML from input stream", e);
        }
    }

    @Override
    public synchronized Schema getSchema() {
        return schema;
    }

    @Override
    public synchronized void setSchema(Schema newSchema) {
        schema = newSchema;
        builderFactory = null;
    }

    private synchronized DocumentBuilderFactory getInitializedFactory() throws XMLParserException {
        if (builderFactory == null) {
            initialize();
        }
        return builderFactory;
    }
}
