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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;


/**
 * Extended to allow adding in some custom fields to outgoing encoded objects
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EntityXstream extends XStream {

   protected HierarchicalStreamDriver visibleHSD;

   /**
    * This will let us get to the hierarchicalStreamDriver used since it is stupidly private
    */
   public EntityXstream(HierarchicalStreamDriver hierarchicalStreamDriver) {
      super(hierarchicalStreamDriver);
      visibleHSD = hierarchicalStreamDriver;
   }

   /**
    * Convert this object to XML and add in additional information in the data holder
    * 
    * @param obj
    * @param holder
    * @return the string version of the xml
    */
   public String toXml(Object obj, DataHolder holder) {
      Writer w = new StringWriter();
      HierarchicalStreamWriter writer = visibleHSD.createWriter(w);
      marshal(obj, writer, holder);
      writer.flush();
      return writer.toString();
   }

}
