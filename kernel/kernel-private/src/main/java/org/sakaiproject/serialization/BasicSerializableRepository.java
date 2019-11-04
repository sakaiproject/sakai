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

import java.io.IOException;
import java.io.Serializable;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.hibernate.HibernateCrudRepository;

import com.ctc.wstx.api.WstxInputProperties;
import com.ctc.wstx.api.WstxOutputProperties;
import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by enietzel on 3/3/17.
 */
@Slf4j
public abstract class BasicSerializableRepository<T, ID extends Serializable> extends HibernateCrudRepository<T, ID> implements SerializableRepository<T, ID> {

    @Override
    public String toJSON(T t) {
        String json = "";
        if (t != null) {
            sessionFactory.getCurrentSession().refresh(t);
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModules(new JavaTimeModule());
            try {
                json = mapper.writeValueAsString(t);
            } catch (JsonProcessingException e) {
                log.warn("Could not serialize to json", e);
                json = "";
            }
        }
        return json;
    }

    @Override
    public T fromJSON(String json) {
        T obj = null;
        if (StringUtils.isNotBlank(json)) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModules(new JavaTimeModule());
            try {
                obj = mapper.readValue(json, getDomainClass());
            } catch (IOException e) {
                log.warn("Could not deserialize json", e);
                obj = null;
            }
        }
        return obj;
    }

    @Override
    public String toXML(T t) {
        String xml = "";
        if (t != null) {
            sessionFactory.getCurrentSession().refresh(t);
            final XmlMapper mapper = createXMLMapper();
            try {
                xml = mapper.writeValueAsString(t);
            } catch (JsonProcessingException e) {
                log.warn("Could not serialize to xml", e);
                xml = "";
            }
        }
        return xml;
    }

    @Override
    public T fromXML(String xml) {
        T obj = null;
        if (StringUtils.isNotBlank(xml)) {
            final XmlMapper mapper = createXMLMapper();
            try {
                obj = mapper.readValue(xml, getDomainClass());
            } catch (IOException e) {
                log.warn("Could not deserialize xml", e);
                obj = null;
            }
        }
        return obj;
    }
    
    private XmlMapper createXMLMapper() {
        final XMLInputFactory ifactory = new WstxInputFactory();
        ifactory.setProperty(WstxInputProperties.P_MAX_ATTRIBUTE_SIZE, 32000);
        ifactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

        final XMLOutputFactory ofactory = new WstxOutputFactory();
        ofactory.setProperty(WstxOutputProperties.P_OUTPUT_CDATA_AS_TEXT, true);
        ofactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);

        final XmlFactory xf = new XmlFactory(ifactory, ofactory);

        final XmlMapper mapper = new XmlMapper(xf);
        mapper.registerModules(new JavaTimeModule());
        return mapper;
    }
}
