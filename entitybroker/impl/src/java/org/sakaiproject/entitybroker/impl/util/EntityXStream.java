/**
 * $Id$
 * $URL$
 * EntityXstream.java - entity-broker - Apr 13, 2008 11:01:41 AM - azeckoski
 **************************************************************************
 * Copyright 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
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

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.core.MapBackedDataHolder;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;


/**
 * Extended to allow adding in some custom fields to outgoing encoded objects
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EntityXStream extends XStream {

   public static String EXTRA_DATA_CLASS = "extra.data.class";
   public static String SAKAI_ENTITY = "sakaiEntity";
   public static String SAKAI_ENTITY_DASH = SAKAI_ENTITY + "-";

   protected HierarchicalStreamDriver visibleHSD;

   /**
    * This will let us get to the hierarchicalStreamDriver used since it is stupidly private
    */
   public EntityXStream(HierarchicalStreamDriver hierarchicalStreamDriver) {
      super(hierarchicalStreamDriver);
      super.setMarshallingStrategy( new ReferenceEntityMarshaller(ReferenceEntityMarshaller.RELATIVE) );
      visibleHSD = hierarchicalStreamDriver;
   }

   /**
    * Convert this object to XML and add in additional information in the data holder
    * 
    * @param obj
    * @param entityData any additional XML data to add in,
    * will only be added to the class type that is included with the key {@link #EXTRA_DATA_CLASS}
    * @return the string version of the xml
    */
   public String toXML(Object obj, Map<String, Object> entityData) {
      Writer out = new StringWriter();
      HierarchicalStreamWriter writer = visibleHSD.createWriter(out);
      marshal(obj, writer, makeDataHolder(entityData) );
      writer.flush();
      return out.toString();
   }

   protected DataHolder makeDataHolder(Map<String, Object> data) {
      DataHolder holder = null;
      if (data != null) {
         holder = new MapBackedDataHolder(data);
      }
      return holder;
   }

}
