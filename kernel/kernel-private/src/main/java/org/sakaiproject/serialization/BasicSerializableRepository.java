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
public abstract class BasicSerializableRepository<T, ID extends Serializable> extends HibernateCrudRepository<T, ID> {

    public String toJSON(T t) {
        String json = "";
        if (t != null) {
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

    public T fromJSON(String text) {
        T obj = null;
        if (StringUtils.isNotBlank(text)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                obj = mapper.readValue(text, getDomainClass());
            } catch (IOException e) {
                log.warn("Could not deserialize json", e);
                obj = null;
            }
        }
        return obj;
    }

    public String toXML(T t) {
        String xml = "";
        if (t != null) {
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

    public T fromXML(String text) {
        T obj = null;
        if (StringUtils.isNotBlank(text)) {
            XmlMapper mapper = new XmlMapper();
            try {
                obj = mapper.readValue(text, getDomainClass());
            } catch (IOException e) {
                log.warn("Could not deserialize xml", e);
                obj = null;
            }
        }
        return obj;
    }
}
