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

import java.io.Serializable;

import org.sakaiproject.hibernate.CrudRepository;

/**
 * Created by enietzel on 3/6/17.
 */
public interface SerializableRepository<T, ID extends Serializable> extends CrudRepository<T, ID> {

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
