/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.serialization;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import com.ctc.wstx.api.WstxInputProperties;
import com.ctc.wstx.api.WstxOutputProperties;
import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.Getter;

@Getter
public class MapperFactory {

    private ObjectMapper jsonMapper;
    private XmlMapper wrappedXmlMapper;
    private XmlMapper xmlMapper;

    public void init() {

        jsonMapper = new ObjectMapper();
        jsonMapper.registerModules(new JavaTimeModule());

        XMLInputFactory inputFactory = new WstxInputFactory();
        inputFactory.setProperty(WstxInputProperties.P_MAX_ATTRIBUTE_SIZE, 32000);
        inputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

        XMLOutputFactory wrappedCdataOutputFactory = new WstxOutputFactory();
        wrappedCdataOutputFactory.setProperty(WstxOutputProperties.P_OUTPUT_CDATA_AS_TEXT, true);
        wrappedCdataOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);

        wrappedXmlMapper = createXmlMapper(new XmlFactory(inputFactory, wrappedCdataOutputFactory));

        XMLOutputFactory outputFactory = new WstxOutputFactory();
        outputFactory.setProperty(WstxOutputProperties.P_OUTPUT_CDATA_AS_TEXT, false);
        outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);

        xmlMapper = createXmlMapper(new XmlFactory(inputFactory, outputFactory));
    }

    private XmlMapper createXmlMapper(XmlFactory factory) {

        XmlMapper mapper = new XmlMapper(factory);
        mapper.registerModules(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(ToXmlGenerator.Feature.WRITE_XML_1_1, true);
        return mapper;
    }
}
