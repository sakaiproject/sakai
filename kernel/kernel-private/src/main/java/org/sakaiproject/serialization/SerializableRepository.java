package org.sakaiproject.serialization;

import java.io.Serializable;

import org.sakaiproject.hibernate.CrudRepository;

/**
 * Created by enietzel on 3/6/17.
 */
public interface SerializableRepository<T, ID extends Serializable> {

    /**
     * Serialize object to JSON
     * @param t
     * @return String
     */
    String toJSON(T t);

    /**
     * Deserialize object from JSON
     * @param json
     * @return T
     */
    T fromJSON(String json);

    /**
     * Serialize object to XML
     * @param t
     * @return String
     */
    String toXML(T t);

    /**
     * Deserialize object from XML
     * @param xml
     * @return T
     */
    T fromXML(String xml);
}
