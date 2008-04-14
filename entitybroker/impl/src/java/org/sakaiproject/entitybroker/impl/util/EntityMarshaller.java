/**
 * $Id$
 * $URL$
 * EntityTreeMarshaller.java - entity-broker - Apr 14, 2008 1:07:51 PM - azeckoski
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

import java.util.Iterator;

import com.thoughtworks.xstream.alias.ClassMapper;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.core.AbstractReferenceMarshaller;
import com.thoughtworks.xstream.core.ReferenceByXPathMarshallingStrategy;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.path.Path;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Extending the mapper to handle adding in the extra entity information
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
@SuppressWarnings({"deprecation","unchecked"})
public class EntityMarshaller extends AbstractReferenceMarshaller implements MarshallingContext {

   // START COPY from ReferenceByXPathMarshaller
   protected final int mode;

   public EntityMarshaller(HierarchicalStreamWriter writer, ConverterLookup converterLookup, Mapper mapper, int mode) {
      super(writer, converterLookup, mapper);
      this.mode = mode;
   }

   public EntityMarshaller(HierarchicalStreamWriter writer, ConverterLookup converterLookup, ClassMapper classMapper) {
      this(writer, converterLookup, classMapper, ReferenceEntityMarshaller.RELATIVE);
   }

   protected String createReference(Path currentPath, Object existingReferenceKey) {
      return (mode == ReferenceByXPathMarshallingStrategy.RELATIVE ? currentPath.relativeTo((Path)existingReferenceKey) : existingReferenceKey).toString();
   }

   protected Object createReferenceKey(Path currentPath) {
      return currentPath;
   }

   protected void fireValidReference(Object referenceKey) {
      // nothing to do
   }
   // END COPY

   @Override
   public void convert(Object item, Converter converter) {
      super.convert(item, converter);

      // add in the sakai entity values if they can be found and are not null
      Class<?> entityClass = (Class<?>) super.get(EntityXStream.EXTRA_DATA_CLASS);
      if (entityClass != null) {
         if (item.getClass().isAssignableFrom(entityClass)) {
            // this is a sakai entity so add the attributes
            super.writer.startNode(EntityXStream.SAKAI_ENTITY);
            super.writer.setValue("true");
            super.writer.endNode();
            for (Iterator<String> iterator = super.keys(); iterator.hasNext();) {
               String key = iterator.next();
               if (! EntityXStream.EXTRA_DATA_CLASS.equals(key)) {
                  Object o = super.get(key);
                  if (o != null) {
                     String value = o.toString();
                     if (! "".equals(value)) {
                        super.writer.startNode(EntityXStream.SAKAI_ENTITY_DASH + key);
                        super.writer.setValue(value);
                        super.writer.endNode();
                     }
                  }
               }
            }
         }
      }
   }

}
