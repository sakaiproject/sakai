/**
 * $Id$
 * $URL$
 * MapConverter.java - entity-broker - Apr 8, 2008 6:43:14 PM - azeckoski
 **************************************************************************
 * Copyright 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.impl.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.sakaiproject.entitybroker.util.map.ConcurrentOrderedMap;
import org.sakaiproject.entitybroker.util.map.OrderedMap;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * A custom converter to make the map output look nicer
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
@SuppressWarnings("unchecked")
public class MapConverter implements Converter {

   public boolean canConvert(Class clazz) {
      boolean can = false;
      if (clazz.equals(HashMap.class)
            || clazz.equals(Hashtable.class)
            || clazz.equals(OrderedMap.class)
            || clazz.equals(ConcurrentOrderedMap.class)) {
         can = true;
      }
      return can;
   }

   public void marshal(Object value, HierarchicalStreamWriter writer,
         MarshallingContext context) {
      Map m = (Map) value;
      for (Entry es : (Set<Entry>) m.entrySet()) {
         writer.startNode(es.getKey().toString());
         context.convertAnother(es.getValue());
         writer.endNode();         
      }
   }

   public Object unmarshal(HierarchicalStreamReader reader,
         UnmarshallingContext context) {
      Map m = new OrderedMap<String, Object>();
      while (reader.hasMoreChildren()) {
         reader.moveDown();
         m.put(reader.getNodeName(), reader.getValue());
         reader.moveUp();
      }
      return m;
   }

}
