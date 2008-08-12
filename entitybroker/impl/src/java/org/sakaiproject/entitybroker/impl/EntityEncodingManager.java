/**
 * $Id$
 * $URL$
 * EntityEncodingManager.java - entity-broker - Jul 23, 2008 3:25:32 PM - azeckoski
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

package org.sakaiproject.entitybroker.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.capabilities.InputTranslatable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.OutputFormattable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EncodingException;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.impl.util.EntityXStream;
import org.sakaiproject.entitybroker.util.reflect.ReflectUtil;

import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.XppDomDriver;


/**
 * This handles the internal encoding (translation and formatting) of entity data,
 * this can be used by various parts of the EB system.
 * this is for internal use only currently but may be exposed later
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityEncodingManager {

   private static Log log = LogFactory.getLog(EntityEncodingManager.class);

   private static final String ENTITY_PREFIX = "entityPrefix";
   private static final String COLLECTION = "-collection";

   private EntityProviderManager entityProviderManager;
   public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
      this.entityProviderManager = entityProviderManager;
   }

   private EntityBrokerManager entityBrokerManager;
   public void setEntityBrokerManager(EntityBrokerManager entityBrokerManager) {
      this.entityBrokerManager = entityBrokerManager;
   }
   public EntityBrokerManager getEntityBrokerManager() {
      return entityBrokerManager;
   }

   
   /**
    * Format and output an entity or collection included or referred to by this entity ref object
    * into output according to the format string provided,
    * Should take into account the reference when determining what the entities are
    * and how to encode them
    * 
    * @param ref a globally unique reference to an entity, 
    * consists of the entity prefix and optional segments
    * @param format a string constant indicating the format (from {@link Formats}) 
    * for output, (example: {@link #XML})
    * @param entities (optional) a list of entities to create formatted output for,
    * if this is null then the entities should be retrieved based on the reference,
    * if this contains only a single item AND the ref refers to a single entity
    * then the entity should be extracted from the list and encoded without the indication
    * that it is a collection, for all other cases the encoding should include an indication that
    * this is a list of entities
    * @param output the output stream to place the formatted data in,
    * should be UTF-8 encoded if there is char data
    * @throws IllegalArgumentException if the entity does not support output formatting or any arguments are invalid
    * @throws EncodingException is there is failure encoding the output
    */
   public void formatAndOutputEntity(EntityReference ref, String format, List<EntityData> entities, OutputStream output, Map<String, Object> params) {
      if (ref == null || format == null || output == null) {
         throw new IllegalArgumentException("ref, format, and output cannot be null");
      }
      String prefix = ref.getPrefix();
      Outputable outputable = entityProviderManager.getProviderByPrefixAndCapability(prefix, Outputable.class);
      if (outputable != null) {
         String[] formats = outputable.getHandledOutputFormats();
         if ( ReflectUtil.contains(formats, format) ) {
            OutputFormattable formattable = entityProviderManager.getProviderByPrefixAndCapability(prefix, OutputFormattable.class);
            if (formattable == null) {
               // handle internally or fail
               internalOutputFormatter(ref, format, entities, output, null, params);
            } else {
               // use provider's formatter
               formattable.formatOutput(ref, format, entities, params, output);
            }
         } else {
            throw new IllegalArgumentException("This entity ("+ref+") is not outputable in this format ("+format+")," +
                  " only the following formats are supported: " + ReflectUtil.arrayToString(formats));
         }
      } else {
         throw new IllegalArgumentException("This entity ("+ref+") is not outputable");
      }
   }

   /**
    * Translates the input data stream in the supplied format into an entity object for this reference
    * 
    * @param ref a globally unique reference to an entity, 
    * consists of the entity prefix and optional segments
    * @param format a string constant indicating the format (from {@link Formats}) 
    * of the input, (example: {@link #XML})
    * @param input a stream which contains the data to make up this entity,
    * you may assume this is UTF-8 encoded if you don't know anything else about it
    * @return an entity object of the type used for the given reference
    * @throws IllegalArgumentException if the entity does not support input translation or any arguments are invalid
    * @throws EncodingException is there is failure encoding the input
    */
   public Object translateInputToEntity(EntityReference ref, String format, InputStream input, Map<String, Object> params) {
      if (ref == null || format == null || input == null) {
         throw new IllegalArgumentException("ref, format, and input cannot be null");
      }
      Object entity = null;
      String prefix = ref.getPrefix();
      Inputable inputable = entityProviderManager.getProviderByPrefixAndCapability(prefix, Inputable.class);
      if (inputable != null) {
         String[] formats = inputable.getHandledInputFormats();
         if ( ReflectUtil.contains(formats, format) ) {
            InputTranslatable translatable = entityProviderManager.getProviderByPrefixAndCapability(prefix, InputTranslatable.class);
            if (translatable == null) {
               // handle internally or fail
               entity = internalInputTranslator(ref, format, input, null);
            } else {
               // use provider's formatter
               entity = translatable.translateFormattedData(ref, format, input, params);
            }
         } else {
            throw new IllegalArgumentException("This entity ("+ref+") is not inputable in this format ("+format+")," +
                  " only the following formats are supported: " + ReflectUtil.arrayToString(formats));
         }
      } else {
         throw new IllegalArgumentException("This entity ("+ref+") is not inputable");
      }
      return entity;
   }

   /**
    * Handled the internal encoding of data into an entity object
    * 
    * @param ref the entity reference
    * @param format the format which the input is encoded in
    * @param input the data being input
    * @return the entity object based on the data
    * @throws EntityException if there is a failure in translation
    */
   @SuppressWarnings("unchecked")
   public Object internalInputTranslator(EntityReference ref, String format, InputStream input, HttpServletRequest req) {
      Object entity = null;

      // get the encoder to use
      EntityXStream encoder = getEncoderForFormat(format, false);

      Inputable inputable = (Inputable) entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), Inputable.class);
      if (inputable != null) {
         // get a the current entity object or a sample
         Object current = null;
         if (ref.getId() == null) {
            // get a sample
            current = inputable.getSampleEntity();
         } else {
            // get the current entity
            current = inputable.getEntity(ref);
         }

         if (current != null) {
            if (Formats.HTML.equals(format) || format == null || "".equals(format)) {
               // html req handled specially
               if (req != null) {
                  Map<String, String[]> params = req.getParameterMap();
                  if (params != null && params.size() > 0) {
                     entity = current;
                     try {
                        entityBrokerManager.getReflectUtil().populateFromParams(entity, params);
                     } catch (Exception e) {
                        throw new IllegalArgumentException("Unable to populate bean for ref ("+ref+") from request: " + e.getMessage(), e);
                     }
                  } else {
                     // no request params, bad request
                     throw new EntityException("No request params for html input request (there must be at least one) for reference: " + ref, 
                           ref.toString(), HttpServletResponse.SC_BAD_REQUEST);
                  }
               }
            } else if (encoder != null) {
               if (input == null) {
                  // no request params, bad request
                  throw new EntityException("No input for input translation (input cannot be null) for reference: " + ref, 
                        ref.toString(), HttpServletResponse.SC_BAD_REQUEST);
               } else {
                  encoder.alias(ref.getPrefix(), current.getClass());
                  // START classloader protection
                  ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
                  try {
                     Object classloaderIndicator = current;
                     ClassLoader newClassLoader = classloaderIndicator.getClass().getClassLoader();
                     encoder.setClassLoader(newClassLoader);
                     // translate using the encoder
                     entity = encoder.fromXML(input, current);
                     // END run in classloader
                  } catch (RuntimeException e) {
                     throw new EncodingException("Failure during internal input encoding of entity: " + ref, ref.toString(), e);
                  } finally {
                     encoder.setClassLoader(currentClassLoader);
                  }
                  // END classloader protection
               }
            }
         }
      } else {
         throw new IllegalArgumentException("This entity ("+ref+") does not allow input translation");
      }

      if (entity == null) {
         throw new EntityException("Unable to encode entity from input for reference: " + ref, ref.toString(), HttpServletResponse.SC_BAD_REQUEST);
      }
      return entity;
   }


   /**
    * Format entities for output based on the reference into a format,
    * use the provided list or get the entities
    * 
    * @param ref the entity reference for this, 
    * if this is a reference to a collection then this will be rendered as a collection of entities,
    * if a reference to a single entity then only the matching one from the collection will be used
    * @param format the format to use for the output data
    * @param entities (optional) if this is null then the entities will be fetched
    * @param output the outputstream to place the encoded data into
    * @param view (optional) 
    */
   public void internalOutputFormatter(EntityReference ref, String format, List<EntityData> entities, OutputStream output, EntityView view, Map<String, Object> params) {
      if (format == null) { format = Outputable.HTML; }
      if (view == null) {
         view = entityBrokerManager.makeEntityView(ref, null, null);
      }
      // create a scrap view from the current view, this should be more efficient
      EntityView workingView = view.copy();

      // get the entities if not supplied
      if (entities == null) {
         // these will be EntityData
         entities = entityBrokerManager.getEntitiesData(ref, new Search(), params);
      }
      if (entities.isEmpty()) {
         // just log this for now
         log.info("No entities to format ("+format+") and output for ref (" + ref + ")");
      }

      // get the encoder to use
      EntityXStream encoder = getEncoderForFormat(format, true);

      String encoded = null;
      if (EntityView.VIEW_LIST.equals(view.getViewKey()) 
            || ref.getId() == null) {
         // encoding a collection of entities
         if (encoder != null) {
            setEncoderDataAlias(ref.getPrefix(), entities, encoder);

            StringBuilder sb = new StringBuilder();
            // make header
            if (Formats.JSON.equals(format)) {
               sb.append("{\""+ENTITY_PREFIX+"\": \""+ref.getPrefix() + "\", \"" + ref.getPrefix() + COLLECTION + "\": [\n");
            } else { // assume XML
               sb.append("<" + ref.getPrefix() + COLLECTION + " " + ENTITY_PREFIX + "=\"" + ref.getPrefix() + "\">\n");
            }
            // loop through and encode items
            int encodedEntities = 0;
            for (EntityData entity : entities) {
               String encode = encodeEntity(ref, workingView, entity, encoder);
               if (encode.length() > 3) {
                  if (Formats.JSON.equals(format)) {
                     if (encodedEntities > 0) {
                        sb.append(",\n");
                     }
                     // special JSON cleanup (strips off the {"stuff": ... })
                     encode = encode.substring(encode.indexOf(':')+1, encode.length()-1);
                  } else {
                     if (encodedEntities > 0) {
                        sb.append("\n");
                     }
                  }
                  sb.append(encode);                     
                  encodedEntities++;
               }
            }
            // make footer
            if (Formats.JSON.equals(format)) {
               sb.append("\n]}");
            } else { // assume XML
               sb.append("\n</" + ref.getPrefix() + COLLECTION + ">");
            }
            encoded = sb.toString();
         } else {
            // just dump the whole thing to a string if there is no encoder
            EntityData ed = new EntityData(ref, null, entities);
            ed.setEntityURL( entityBrokerManager.makeFullURL(workingView.getEntityURL()) );
            encoded = encodeEntity(ref, workingView, ed, null);
         }
      } else {
         // encoding a single entity
         EntityData toEncode = entities.get(0);
         if (toEncode == null) {
            throw new EncodingException("Failed to encode data for entity (" + ref 
                  + "), entity object to encode could not be found (null object in list)", ref.toString());
         } else {
            if (encoder != null) {
               setEncoderDataAlias(ref.getPrefix(), entities, encoder);
            }
            try {
               encoded = encodeEntity(ref, workingView, toEncode, encoder);
            } catch (RuntimeException e) {
               throw new EncodingException("Failure during internal output encoding of entity: " + ref, ref.toString(), e);
            }
         }
      }
      // put the encoded data into the OS
      try {
         byte[] b = encoded.getBytes(Formats.UTF_8);
         output.write(b);
      } catch (UnsupportedEncodingException e) {
         throw new EncodingException("Failed to encode UTF-8: " + ref, ref.toString(), e);
      } catch (IOException e) {
         throw new EncodingException("Failed to encode into output stream: " + ref, ref.toString(), e);
      }
   }

   /**
    * Sets the encoded alias so that the data output is clear and easy to read by
    * making the prefix the text used in the outside XML,
    * this basically affects the way the encoded XML looks when it is dumped
    * and is Xstream specific
    */
   private void setEncoderDataAlias(String prefix, List<EntityData> entities, EntityXStream encoder) {
      Class<?> entityClass = null;
      if (! entities.isEmpty()) {
         EntityData ed = entities.get(0);
         if (ed != null) {
            Object obj = ed.getEntity();
            entityClass = obj.getClass();
         }
      }
      if (entityClass == null) {
         encoder.alias("entity-data", EntityData.class);
      } else {
         encoder.alias(prefix, entityClass);
      }
   }


   /**
    * stores the various xstream processors for handling the different types of data
    */
   private Map<String, EntityXStream> xstreams = new HashMap<String, EntityXStream>();
   /**
    * @param format
    * @param output if true then get the encode for output, if false then for input
    * @return the appropriate encoder for the format
    */
   public EntityXStream getEncoderForFormat(String format, boolean output) {
      EntityXStream encoder = null;
      if (Formats.JSON.equals(format)) {
         // http://jira.sakaiproject.org/jira/browse/SAK-13681
//       if (output) {
//       if (! xstreams.containsKey(format)) {
//       xstreams.put( format, new EntityXStream(new JsonHierarchicalStreamDriver()) );
//       }
//       } else {
//       format += "-IN";
//       if (! xstreams.containsKey(format)) {
//       xstreams.put( format, new EntityXStream(new JettisonMappedXmlDriver()) );
//       }
//       }
         if (! xstreams.containsKey(format)) {
            xstreams.put( format, new EntityXStream(new JettisonMappedXmlDriver()) );
         }
         encoder = xstreams.get(format);
      } else if (Formats.XML.equals(format)) {
         if (! xstreams.containsKey(format)) {
            xstreams.put( format, new EntityXStream(new XppDomDriver()) );
         }
         encoder = xstreams.get(format);
      } else if (Formats.TXT.equals(format)) {
         // TODO Add in plaintext encoder/decoder
      } else {
         encoder = null; // do a toString dump
      }
      return encoder;
   }

   /**
    * @param ref the entity reference
    * @param workingView this is a working view which can be changed around as needed to generate URLs,
    * always go to the EntityReference for original data
    * @param toEncode entity data to encode
    * @param encoder enhanced xstream encoder or null if no encoder available
    * @return the encoded entity or "" if encoding fails
    */
   public String encodeEntity(EntityReference ref, EntityView workingView, EntityData entityData, EntityXStream encoder) {
      String encoded = "";
      if (entityData == null) {
         throw new IllegalArgumentException("entity data to encode must not be null");
      }
      if (encoder != null) {
         // TODO encode the props from the entity data specially? (this may be the only data)
         // encode the entity itself
         Object toEncode = entityData.getEntity();
         if (toEncode == null) {
            // just use the entity data object
            toEncode = entityData;
         }
         // add in the extra props
         Map<String, Object> entityProps = new HashMap<String, Object>();
         Class<?> entityClass = toEncode.getClass();
         entityProps.put(EntityXStream.EXTRA_DATA_CLASS, entityClass);
         if (entityData.getEntityReference().getId() != null) {
            entityProps.put("ID", entityData.getEntityReference().getId());
         }
         entityProps.put("URL", entityData.getEntityURL());
         // encode the object
         encoded = encoder.toXML(toEncode, entityProps);
      } else {
         // just to string this and dump it out
         encoded = "<b>" + ref.getPrefix() + "</b>: " + entityData;
      }
      return encoded;
   }

}
