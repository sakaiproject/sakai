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

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.hibernate.HibernateCrudRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by enietzel on 3/3/17.
 */
@Slf4j
public abstract class BasicSerializableRepository<T, ID extends Serializable> extends HibernateCrudRepository<T, ID> implements SerializableRepository<T, ID> {

    // currently using a static since all entities should use these mappers
    protected static ObjectMapper xmlMapper = MapperFactory.createDefaultXmlMapper();
    protected static ObjectMapper jsonMapper = MapperFactory.createDefaultJsonMapper();

    @Override
    public String toJSON(T t) {
        String json = "";
        if (t != null) {
            sessionFactory.getCurrentSession().refresh(t);
            try {
                json = jsonMapper.writeValueAsString(t);
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
            try {
                obj = jsonMapper.readValue(json, getDomainClass());
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
            try {
                xml = xmlMapper.writeValueAsString(t);
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
            try {
                obj = xmlMapper.readValue(xml, getDomainClass());
            } catch (IOException e) {
                log.warn("Could not deserialize xml", e);
                obj = null;
            }
        }
        return obj;
    }
}
