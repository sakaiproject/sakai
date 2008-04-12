/**
 * $Id$
 * $URL$
 * MapConverter.java - entity-broker - Apr 8, 2008 6:43:14 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.impl.util;

import java.util.HashMap;
import java.util.Map;

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
            || clazz.equals(Hashtable.class)) {
         can = true;
      }
      return can;
   }

   public void marshal(Object value, HierarchicalStreamWriter writer,
         MarshallingContext context) {
      Map m = (Map) value;
      for (Object key : m.keySet()) {
         writer.startNode(key.toString());
         context.convertAnother(m.get(key));
         writer.endNode();         
      }
   }

   public Object unmarshal(HierarchicalStreamReader reader,
         UnmarshallingContext context) {
      Map m = new HashMap<String, Object>();
      while (reader.hasMoreChildren()) {
         reader.moveDown();
         m.put(reader.getNodeName(), reader.getValue());
         reader.moveUp();
      }
      return m;
   }

}
