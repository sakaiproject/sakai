/**
 * $Id$
 * $URL$
 * EntityXstream.java - entity-broker - Apr 13, 2008 11:01:41 AM - azeckoski
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
