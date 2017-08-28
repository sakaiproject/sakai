package org.sakaiproject.serialization;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.hibernate.HibernateCrudRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

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
            XmlMapper mapper = new XmlMapper();
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
            XmlMapper mapper = new XmlMapper();
            try {
                obj = mapper.readValue(xml, getDomainClass());
            } catch (IOException e) {
                log.warn("Could not deserialize xml", e);
                obj = null;
            }
        }
        return obj;
    }
}
