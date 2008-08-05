/**
 * $Id$
 * $URL$
 * ReferenceEntityMarshaller.java - entity-broker - Apr 14, 2008 1:57:16 PM - azeckoski
 **************************************************************************
 * Copyright 2006 Sakai Foundation
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

import com.thoughtworks.xstream.MarshallingStrategy;
import com.thoughtworks.xstream.alias.ClassMapper;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.core.DefaultConverterLookup;
import com.thoughtworks.xstream.core.ReferenceByXPathUnmarshaller;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;


/**
 * Needed to allow the {@link EntityMarshaller} to work right
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
@SuppressWarnings({"deprecation","unchecked"})
public class ReferenceEntityMarshaller implements MarshallingStrategy {

   // COPIED FROM ReferenceByXPathMarshallingStrategy

   public static int RELATIVE = 0;
   public static int ABSOLUTE = 1;
   private final int mode;

   /**
    * @deprecated As of 1.2, use {@link #ReferenceByXPathMarshallingStrategy(int)}
    */
   public ReferenceEntityMarshaller() {
       this(RELATIVE);
   }

   public ReferenceEntityMarshaller(int mode) {
       this.mode = mode;
   }

   public Object unmarshal(Object root, HierarchicalStreamReader reader, DataHolder dataHolder, ConverterLookup converterLookup, Mapper mapper) {
       return new ReferenceByXPathUnmarshaller(root, reader, converterLookup, mapper).start(dataHolder);
   }

   public void marshal(HierarchicalStreamWriter writer, Object obj, ConverterLookup converterLookup, Mapper mapper, DataHolder dataHolder) {
       new EntityMarshaller(writer, converterLookup, mapper, mode).start(obj, dataHolder);
   }

   /**
    * @deprecated As of 1.2, use {@link #unmarshal(Object, HierarchicalStreamReader, DataHolder, ConverterLookup, Mapper)}
    */
   public Object unmarshal(Object root, HierarchicalStreamReader reader, DataHolder dataHolder, DefaultConverterLookup converterLookup, ClassMapper classMapper) {
       return new ReferenceByXPathUnmarshaller(root, reader, converterLookup, classMapper).start(dataHolder);
   }

   /**
    * @deprecated As of 1.2, use {@link #marshal(HierarchicalStreamWriter, Object, ConverterLookup, Mapper, DataHolder)}
    */
   public void marshal(HierarchicalStreamWriter writer, Object obj, DefaultConverterLookup converterLookup, ClassMapper classMapper, DataHolder dataHolder) {
       new EntityMarshaller(writer, converterLookup, classMapper).start(obj, dataHolder);
   }

}
