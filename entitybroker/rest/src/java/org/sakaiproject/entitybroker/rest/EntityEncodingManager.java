/**
 * $Id$
 * $URL$
 * EntityEncodingManager.java - entity-broker - Jul 23, 2008 3:25:32 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.rest;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import org.apache.commons.text.StringEscapeUtils;
import org.sakaiproject.entitybroker.EntityBrokerManager;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityFieldRequired;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Createable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Deleteable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.DepthLimitable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.InputTranslatable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.OutputFormattable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.OutputSerializable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Updateable;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityEncodingException;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.FormatUnsupportedException;
import org.sakaiproject.entitybroker.providers.EntityRequestHandler;
import org.sakaiproject.entitybroker.util.EntityDataUtils;
import org.sakaiproject.serialization.MapperFactory;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.util.ReflectionUtils;

import lombok.extern.slf4j.Slf4j;


/**
 * This handles the internal encoding (translation and formatting) of entity data,
 * this can be used by various parts of the EB system <br/>
 * this is for internal use only currently but may be exposed later
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Slf4j
public class EntityEncodingManager {

    public static final String ENTITY_REFERENCE = "entityReference";
    public static final String ENTITY_ID = "entityId";
    public static final String ENTITY_URL = "entityURL";
    public static final String ENTITY_TITLE = "entityTitle";
    public static final String ENTITY_PREFIX = "entityPrefix";
    public static final String COLLECTION = "_collection";
    public static final String BATCH_PREFIX = '/' + EntityRequestHandler.BATCH + '?' + EntityBatchHandler.REFS_PARAM_NAME + '=';

    public static final String[] HANDLED_INPUT_FORMATS = new String[] { Formats.XML, Formats.JSON, Formats.HTML };
    public static final String[] HANDLED_OUTPUT_FORMATS = new String[] { Formats.XML, Formats.JSON, Formats.JSONP, Formats.HTML, Formats.FORM };

    public static final String JSON_CALLBACK_PARAM = "jsonCallback";
    public static final String JSON_DEFAULT_CALLBACK = "jsonEntityFeed";
    private static final java.util.regex.Pattern JSONP_CALLBACK_PATTERN =
            java.util.regex.Pattern.compile("^[a-zA-Z_$][0-9A-Za-z_$]*(?:\\.[a-zA-Z_$][0-9A-Za-z_$]*)*$");

    private static final ObjectMapper JSON_MAPPER = MapperFactory.jsonBuilder()
            .ignoreUnknownProperties()
            .registerJdk8Module()
            .registerJavaTimeModule()
            .disableDateTimestamps()
            .disableFailOnEmptyBeans()
            .build();
    private static final ObjectWriter JSON_WRITER = JSON_MAPPER.writer();
    private static final ObjectWriter JSON_PRETTY_WRITER;

    static {
        // Configure pretty printer to match old reflectutils format: "key": "value" (space only after colon)
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
        printer.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
        // Remove space before colon, keep space after colon
        printer = printer.withSeparators(Separators.createDefaultInstance().withObjectFieldValueSpacing(Separators.Spacing.AFTER));
        JSON_PRETTY_WRITER = JSON_MAPPER.writer(printer);
    }
    private static final XmlMapper XML_MAPPER = MapperFactory.xmlBuilder()
            .registerJavaTimeModule()
            .disableDateTimestamps()
            .ignoreUnknownProperties()
            .enableOutputCDataAsText()
            .disableNamespaceAware()
            .enableRepairingNamespaces()
            .enableOutputXML11()
            .disableFailOnEmptyBeans()
            .build();
    static {
        XML_MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, false);
    }
    private static final ObjectWriter XML_WRITER = XML_MAPPER.writer()
            .withoutFeatures(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);

    protected static final String XML_HEADER_PREFIX = "<?";
    protected static final String XML_HEADER_SUFFIX = "?>";
    protected static final String XML_HEADER = XML_HEADER_PREFIX + "xml version=\"1.0\" encoding=\"UTF-8\" "+XML_HEADER_SUFFIX+"\n";
    protected static final String XHTML_HEADER = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" " +
    "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
    "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
    "<head>\n" +
    "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
    "  <title>{title}</title>\n" +
    "</head>\n" +
    "<body>\n";
    protected static final String XHTML_FOOTER = "</body>\n</html>\n";


    protected EntityEncodingManager() { }

    public EntityEncodingManager(EntityProviderManager entityProviderManager,
            EntityBrokerManager entityBrokerManager) {
        super();
        this.entityProviderManager = entityProviderManager;
        this.entityBrokerManager = entityBrokerManager;
    }

    private EntityProviderManager entityProviderManager;
    public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
        this.entityProviderManager = entityProviderManager;
    }

    private EntityBrokerManager entityBrokerManager;
    public void setEntityBrokerManager(EntityBrokerManager entityBrokerManager) {
        this.entityBrokerManager = entityBrokerManager;
    }


    /**
     * Format and output an entity or collection included or referred to by this entity ref object
     * into output according to the format string provided,
     * Should take into account the reference when determining what the entities are
     * and how to encode them
     * (This is basically a copy of the code in EntityHandlerImpl with stuff removed)
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
     * @param outputStream the output stream to place the formatted data in,
     * should be UTF-8 encoded if there is char data
     * @throws FormatUnsupportedException if you do not handle this format type (passes control to the internal handlers)
     * @throws EntityEncodingException if you cannot encode the received data into an entity
     * @throws IllegalArgumentException if any of the arguments are invalid
     * @throws IllegalStateException for all other failures
     */
    public void formatAndOutputEntity(EntityReference ref, String format, List<EntityData> entities, OutputStream outputStream, Map<String, Object> params) {
        if (ref == null || format == null || outputStream == null) {
            throw new IllegalArgumentException("ref, format, and output cannot be null");
        }
        String prefix = ref.getPrefix();
        Outputable outputable = (Outputable) entityProviderManager.getProviderByPrefixAndCapability(prefix, Outputable.class);
        if (outputable != null) {
            String[] outputFormats = outputable.getHandledOutputFormats();
            // check if the output formats are allowed
            if (outputFormats == null || contains(outputFormats, format) ) {
                boolean handled = false;

                // if the user wants to serialize their objects specially then allow them to translate them
                OutputSerializable serializable = entityProviderManager.getProviderByPrefixAndCapability(prefix, OutputSerializable.class);
                if (serializable != null) {
                    if (entities == null) {
                        // these will be EntityData
                        entities = entityBrokerManager.getEntitiesData(ref, new Search(), params);
                    }
                    if (! entities.isEmpty()) {
                        // find the type of the objects this providers deals in
                        Object sample = entityBrokerManager.getSampleEntityObject(prefix, null);
                        Class<?> entityType = Object.class;
                        if (sample != null) {
                            entityType = sample.getClass();
                        }
                        // now translate the objects to serialize form
                        for (EntityData entityData : entities) {
                            Object entity = entityData.getData();
                            // only translate if the entity is set
                            if (entity != null) {
                                // only translate if the type matches
                                if (entityType.isAssignableFrom(entity.getClass())) {
                                    try {
                                        entity = serializable.makeSerializableObject(ref, entity);
                                        entityData.setData(entity);
                                    } catch (Exception e) {
                                        throw new RuntimeException("Failure while attempting to serialize the object from ("+entity+") for ref("+ref+"): " + e.getMessage(), e);
                                    }
                                }
                            }
                        }
                    }
                }

                /* try to use the provider formatter if one available,
                 * if it decided not to handle it or none is available then control passes to internal
                 */
                try {
                    OutputFormattable formattable = entityProviderManager.getProviderByPrefixAndCapability(prefix, OutputFormattable.class);
                    if (formattable != null) {
                        // use provider's formatter
                        formattable.formatOutput(ref, format, entities, params, outputStream);
                        handled = true;
                    }
                } catch (FormatUnsupportedException e) {
                    // provider decided not to handle this format
                    handled = false;
                }
                if (!handled) {
                    // handle internally or fail
                    internalOutputFormatter(ref, format, entities, params, outputStream, null);
                }
            } else {
                // format type not handled
                throw new FormatUnsupportedException("Outputable restriction for " 
                        + prefix + " blocked handling this format ("+format+")",
                        ref+"", format);
            }
        }
    }

    /**
     * Translates the input data stream in the supplied format into an entity object for this reference
     * (This is basically a copy of the code in EntityHandlerImpl with stuff removed)
     * 
     * @param ref a globally unique reference to an entity, 
     * consists of the entity prefix and optional segments
     * @param format a string constant indicating the format (from {@link Formats}) 
     * of the input, (example: {@link #XML})
     * @param input a stream which contains the data to make up this entity,
     * you may assume this is UTF-8 encoded if you don't know anything else about it
     * @return an entity object of the type used for the given reference
     * @throws FormatUnsupportedException if you do not handle this format type (passes control to the internal handlers)
     * @throws EntityEncodingException if you cannot encode the received data into an entity
     * @throws IllegalArgumentException if any of the arguments are invalid
     * @throws IllegalStateException for all other failures
     */
    public Object translateInputToEntity(EntityReference ref, String format, InputStream inputStream, Map<String, Object> params) {
        if (ref == null || format == null || inputStream == null) {
            throw new IllegalArgumentException("ref, format, and inputStream cannot be null");
        }
        Object entity = null;
        String prefix = ref.getPrefix();
        Inputable inputable = (Inputable) entityProviderManager.getProviderByPrefixAndCapability(prefix, Inputable.class);
        if (inputable != null) {
            String[] inputFormats = inputable.getHandledInputFormats();
            if (inputFormats == null || contains(inputFormats, format) ) {
                boolean handled = false;
                /* try to use the provider translator if one available,
                 * if it decided not to handle it or none is available then control passes to internal
                 */
                try {
                    InputTranslatable translatable = (InputTranslatable) entityProviderManager.getProviderByPrefixAndCapability(prefix, InputTranslatable.class);
                    if (translatable != null) {
                        // use provider's translator
                        entity = translatable.translateFormattedData(ref, format, inputStream, params);
                        handled = true;
                    }
                } catch (FormatUnsupportedException e) {
                    // provider decided not to handle this format
                    handled = false;
                }
                if (!handled) {
                    // use internal translators or fail
                    entity = internalInputTranslator(ref, format, inputStream, null);
                }

                if (entity == null) {
                    // FAILURE input could not be translated into an entity object
                    throw new EntityEncodingException("Unable to translate entity ("+ref+") with format ("
                            +format+"), translated entity object was null", ref+"");
                }
            } else {
                // format type not handled
                throw new FormatUnsupportedException("Inputable restriction for " 
                        + prefix + " blocked handling this format ("+format+")",
                        ref+"", format);
            }
        }
        return entity;
    }

    /**
     * Will attempt to validate that string data is of a specific format
     * @param data a chunk of data to validate
     * @param format the format which the data is supposed encoded in
     * @return true if the data appears valid for the given format, false otherwise
     */
    public boolean validateFormat(String data, String format) {
        // note: this is a weak implementation for now -AZ
        boolean valid = false;
        if (data == null || format == null) {
            throw new IllegalArgumentException("Cannot validate format when the data ("+data+") OR the format ("+format+") are null");
        }
        data = data.trim();
        if (Formats.XML.equals(format)) {
            if (data.startsWith("<") && data.endsWith(">")) {
                valid = true;
            }
        } else if (Formats.JSON.equals(format)) {
            if (data.startsWith("{") && data.endsWith("}")) {
                valid = true;
            }
        } else if (Formats.HTML.equals(format)) {
            if (data.startsWith("<") && data.endsWith(">")) {
                valid = true;
            }
        } else {
            valid = true;
        }
        return valid;
    }


    // FUNCTIONAL CODE BELOW

    /**
     * Handled the internal encoding of data into an entity object
     * 
     * @param ref the entity reference
     * @param format the format which the input is encoded in
     * @param input the data being input
     * @return the entity object based on the data
     * @throws FormatUnsupportedException if you do not handle this format type (passes control to the internal handlers)
     * @throws EntityEncodingException if you cannot encode the received data into an entity
     * @throws IllegalArgumentException if any of the arguments are invalid
     * @throws IllegalStateException for all other failures
     */
    @SuppressWarnings("unchecked")
    public Object internalInputTranslator(EntityReference ref, String format, InputStream input, HttpServletRequest req) {
        Object entity = null;

        Inputable inputable = entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), Inputable.class);
        if (inputable != null) {
            // get a the current entity object or a sample
            Object current = entityBrokerManager.getSampleEntityObject(ref.getPrefix(), ref.getId());
            if (current != null) {
                if (Formats.HTML.equals(format) || format == null || "".equals(format)) {
                    // html req handled specially
                    if (req != null) {
                        Map<String, String[]> params = req.getParameterMap();
                        if (params != null && params.size() > 0) {
                            entity = current;
                            try {
                                populateBeanFromParams(entity, params);
                            } catch (RuntimeException e) {
                                throw new EntityEncodingException("Unable to populate bean for ref ("+ref+") from request: " + e.getMessage(), ref+"", e);
                            }
                        } else {
                            // no request params, bad request
                            throw new EntityException("No request params for html input request (there must be at least one) for reference: " + ref, 
                                    ref.toString(), HttpServletResponse.SC_BAD_REQUEST);
                        }
                    }
                } else {
                    // all other formats
                    if (input == null) {
                        // no request params, bad request
                        throw new EntityException("No input for input translation (input cannot be null) for reference: " + ref, 
                                ref.toString(), HttpServletResponse.SC_BAD_REQUEST);
                    } else {
                        String data = readInputStream(input);
                        Map<String, Object> decoded = null;
                        try {
                            decoded = decodeData(data, format);
                        } catch (IllegalArgumentException iae) {
                            throw new EntityEncodingException("No encoder available for the given format ("+format+"), ref=" + ref + ":" + iae.getMessage(), ref.toString(), iae);
                        } catch (UnsupportedOperationException uoe) {
                            throw new EntityEncodingException("Failure during internal input encoding of entity: " + ref + " to format ("+format+"):" + uoe.getMessage(), ref.toString(), uoe);
                        }
                        entity = current;
                        // handle the special case where the JSON was created by xstream or something else that puts the data inside an object with a "root"
                        if (decoded.size() == 1 && decoded.containsKey(ref.getPrefix())) {
                            Object o = decoded.get(ref.getPrefix());
                            if (o instanceof Map) {
                                decoded = (Map<String, Object>) o;
                            }
                        }
                        try {
                            populateBeanFromMap(entity, decoded);
                        } catch (RuntimeException e) {
                            throw new EntityEncodingException("Unable to populate bean for ref ("+ref+") from data: " + decoded + ":" + e.getMessage(), ref+"", e);
                        }
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
     * @throws FormatUnsupportedException if you do not handle this format type (passes control to the internal handlers)
     * @throws EntityEncodingException if you cannot encode the received data into an entity
     * @throws IllegalArgumentException if any of the arguments are invalid
     * @throws IllegalStateException for all other failures
     */
    public void internalOutputFormatter(EntityReference ref, String format, List<EntityData> entities, Map<String, Object> params, OutputStream output, EntityView view) {
        if (format == null) { format = Outputable.HTML; }
        String originalFormat = format;
        format = format.trim();
        if (!format.equals(originalFormat)) {
            log.debug("Trimmed format '{}' -> '{}' for ref {}", originalFormat, format, ref);
        }

        // check the format to see if we can handle it
        if (! contains(HANDLED_OUTPUT_FORMATS, format)) {
            throw new FormatUnsupportedException("Internal output formatter cannot handle format ("+format+") for ref ("+ref+")", ref+"", format);
        }

        if (view == null) {
            view = entityBrokerManager.makeEntityView(ref, null, null);
        }

        // get the entities if not supplied
        if (entities == null) {
            // these will be EntityData
            entities = entityBrokerManager.getEntitiesData(ref, new Search(), params);
        }
        if (entities.isEmpty()) {
            // just log this for now
            log.debug("EntityEncodingManager: No entities to format ("+format+") and output for ref (" + ref + ")");
        }

        // SAK-22738 - do not show form editing when batch processing is disabled
        String replacementEncoding = null;
        if (Formats.FORM.equals(format) 
                && !entityBrokerManager.getExternalIntegrationProvider().getConfigurationSetting(EntityBatchHandler.CONFIG_BATCH_ENABLE, EntityBatchHandler.CONFIG_BATCH_DEFAULT)) {
            String msg = "FORM editing is not enabled because the batch provider is disabled by sakai config: "+EntityBatchHandler.CONFIG_BATCH_ENABLE+"=false. Enable this config setting with "+EntityBatchHandler.CONFIG_BATCH_ENABLE+"=true to enable batch handling. See SAK-22619 for details.";
            replacementEncoding = "<div style=\"font-weight:bold;color:red;\">"+msg+"</div>";
        }

        String encoded = null;
        if (EntityView.VIEW_LIST.equals(view.getViewKey()) 
                || ref.getId() == null) {
            // encoding a collection of entities
            StringBuilder sb = new StringBuilder(40);

            // make header
            if (Formats.HTML.equals(format) 
                    || Formats.FORM.equals(format)) {
                sb.append("<h1>"+ref.getPrefix() + COLLECTION + "</h1>\n");
            } else if (Formats.JSON.equals(format) || Formats.JSONP.equals(format)) {
                sb.append("{\""+ENTITY_PREFIX+"\": \""+ref.getPrefix() + "\", \"" + ref.getPrefix() + COLLECTION + "\": [\n");
            } else if (Formats.XML.equals(format)) {
                sb.append("<" + ref.getPrefix() + COLLECTION + " " + ENTITY_PREFIX + "=\"" + ref.getPrefix() + "\">\n");
            } else { // general case
                sb.append(ref.getPrefix() + COLLECTION + "\n");
            }

            int encodedEntities = 0;
            if (replacementEncoding != null) {
                sb.append(replacementEncoding);
            } else {
                // loop through and encode items
                for (EntityData entity : entities) {
                    try {
                        String encode = encodeEntity(ref.getPrefix(), format, entity, view);
                        if (encode.length() > 3) {
                            if ((Formats.JSON.equals(format) || Formats.JSONP.equals(format)) 
                                    && encodedEntities > 0) {
                                sb.append(",");
                            }
                            sb.append(encode);
                            encodedEntities++;
                        }
                    } catch (RuntimeException e) {
                        throw new EntityEncodingException("Failure during internal output encoding of entity set on entity: " + ref, ref.toString(), e);
                    }
                }
            }

            // make footer
            if (Formats.HTML.equals(format)
                    || Formats.FORM.equals(format)) {
                sb.append("\n<b>Collection size:</b> "+encodedEntities+"\n");
            } else if (Formats.JSON.equals(format) || Formats.JSONP.equals(format)) {
                sb.append("\n]}");
            } else if (Formats.XML.equals(format)) {
                sb.append("</" + ref.getPrefix() + COLLECTION + ">");
            } else { // general case
                sb.append("\nSize: " + encodedEntities + "\n");
            }
            encoded = sb.toString();
        } else {
            // encoding a single entity
            EntityData toEncode = entities.get(0);
            if (replacementEncoding != null) {
                encoded = replacementEncoding;
            } else {
                if (toEncode == null) {
                    throw new EntityEncodingException("Failed to encode data for entity (" + ref 
                            + "), entity object to encode could not be found (null object in list)", ref.toString());
                } else {
                    try {
                        encoded = encodeEntity(ref.getPrefix(), format, toEncode, view);
                    } catch (RuntimeException e) {
                        throw new EntityEncodingException("Failure during internal output encoding of entity: " + ref, ref.toString(), e);
                    }
                }
            }
        }
        // add the HTML headers and footers
        if (Formats.FORM.equals(format)) {
            String title = view.getViewKey() + ":" + ref;
            encoded = XML_HEADER + XHTML_HEADER.replace("{title}", title) + encoded + XHTML_FOOTER;
        } else if (Formats.XML.equals(format)) {
            encoded = XML_HEADER + encoded;
        } else if (Formats.JSONP.equals(format)) {
            String callback = JSON_DEFAULT_CALLBACK;
            if (params != null && params.containsKey(JSON_CALLBACK_PARAM)) {
                callback = sanitizeJsonCallback(params.get(JSON_CALLBACK_PARAM));
            }
            encoded = callback + "(" + encoded + ")";
        }
        // put the encoded data into the stream
        try {
            byte[] b = encoded.getBytes(Formats.UTF_8);
            output.write(b);
        } catch (UnsupportedEncodingException e) {
            throw new EntityEncodingException("Failed to encode UTF-8: " + ref, ref.toString(), e);
        } catch (IOException e) {
            throw new EntityEncodingException("Failed to encode into output stream: " + ref, ref.toString(), e);
        }
    }

    /**
     * Encodes entity data
     * @param prefix the entity prefix related to this data
     * @param format the format to encode the data into
     * @param entityData (optional) entity data to encode
     * @param view (optional) used to generate links and determine the correct incoming view
     * @return the encoded entity or "" if encoding fails
     */
    public String encodeEntity(String prefix, String format, EntityData entityData, EntityView view) {
        if (prefix == null || format == null) {
            throw new IllegalArgumentException("prefix and format must not be null");
        }
        String originalFormat = format;
        format = format.trim();
        if (!format.equals(originalFormat)) {
            log.debug("Trimmed encodeEntity format '{}' -> '{}' for prefix {}", originalFormat, format, prefix);
        }
        if (entityData == null && ! Formats.FORM.equals(format)) {
            throw new IllegalArgumentException("entityData to encode must not be null for prefix ("+prefix+") and format ("+format+")");
        }
        String encoded = "";
        if (Formats.HTML.equals(format)) {
            // special handling for HTML
            StringBuilder sb = new StringBuilder(200);
            sb.append("  <div style='padding-left:1em;'>\n");
            if (entityData == null) {
                sb.append("NO DATA to encode");
            } else {
                sb.append("    <div style='font-weight:bold;'>"+StringEscapeUtils.escapeHtml4(entityData.getDisplayTitle())+"</div>\n");
                sb.append("    <table border='1'>\n");
                sb.append("      <caption style='font-weight:bold;'>Entity Data</caption>\n");
                if (! entityData.isDataOnly()) {
                    sb.append("      <tr><td>entityReference</td><td>"+StringEscapeUtils.escapeHtml4(entityData.getEntityReference())+"</td></tr>\n");
                    sb.append("      <tr><td>entityURL</td><td>"+StringEscapeUtils.escapeHtml4(entityData.getEntityURL())+"</td></tr>\n");
                    if (entityData.getEntityRef() != null) {
                        sb.append("      <tr><td>entityPrefix</td><td>"+StringEscapeUtils.escapeHtml4(entityData.getEntityRef().getPrefix())+"</td></tr>\n");
                        if (entityData.getEntityRef().getId() != null) {
                            sb.append("      <tr><td>entityID</td><td>"+StringEscapeUtils.escapeHtml4(entityData.getEntityRef().getId())+"</td></tr>\n");
                        }
                    }
                }
                if (entityData.getData() != null) {
                    sb.append("      <tr><td>entity-type</td><td>"+entityData.getData().getClass().getName()+"</td></tr>\n");
                    // dump entity data
                    sb.append("      <tr><td colspan='2'>Data:<br/>\n");
                    sb.append( encodeData(entityData.getData(), Formats.HTML, null, null) );
                    sb.append("      </td></tr>\n");
                } else {
                    sb.append("      <tr><td>entity-object</td><td><i>null</i></td></tr>\n");
                }
                sb.append("    </table>\n");
                Map<String, Object> props = entityData.getEntityProperties();
                if (!props.isEmpty()) {
                    sb.append("    <table border='1'>\n");
                    sb.append("      <caption style='font-weight:bold;'>Properties</caption>\n");
                    for (Entry<String, Object> entry : props.entrySet()) {
                        sb.append("      <tr><td>"+StringEscapeUtils.escapeHtml4(entry.getKey())+"</td><td>"+StringEscapeUtils.escapeHtml4(entry.getValue().toString())+"</td></tr>\n");
                    }
                    sb.append("    </table>\n");
                }
            }
            sb.append("  </div>\n");
            encoded = sb.toString();
        } else if (Formats.FORM.equals(format)) {
            // special handling for FORM type
            if (view == null) {
                throw new IllegalArgumentException("the view must be set for FORM handling and generation");
            }

            boolean handle = false;
            boolean createable = entityProviderManager.getProviderByPrefixAndCapability(prefix, Createable.class) != null;
            boolean updateable = entityProviderManager.getProviderByPrefixAndCapability(prefix, Updateable.class) != null;
            boolean deleteable = entityProviderManager.getProviderByPrefixAndCapability(prefix, Deleteable.class) != null;
            String viewKey = view.getViewKey();
            if (EntityView.VIEW_NEW.equals(viewKey) && createable) {
                handle = true;
            } else if (EntityView.VIEW_EDIT.equals(viewKey) && updateable) {
                handle = true;
            } else if (EntityView.VIEW_DELETE.equals(viewKey) && deleteable) {
                handle = true;
            } else if ( (EntityView.VIEW_LIST.equals(viewKey) || EntityView.VIEW_SHOW.equals(viewKey))
                    && (updateable || deleteable)) {
                // we handle these only if the stuff can be changed
                handle = true;
            }

            if (handle) {
                // fix up URL stuff first
                String prefixUrl = entityBrokerManager.getServletContext();
                if (EntityView.VIEW_LIST.equals(viewKey) 
                        && entityData != null
                        && entityData.getEntityId() != null) {
                    // SPECIAL CASE: if this is a list then the view refers to the space only so we have to copy it
                    view = view.copy();
                    view.setEntityReference( new EntityReference(prefix, entityData.getEntityId()) );
                }
                // now create the output data
                StringBuilder sb = new StringBuilder(300);
                String formName = prefix + "-" + (entityData != null ? entityData.getEntityRef().getId() : "xxx");
                sb.append("  <div style='font-weight:bold;'>");
                sb.append( StringEscapeUtils.escapeHtml4(entityData != null ? entityData.getDisplayTitle() : prefix) );
                if (createable 
                        && ! EntityView.VIEW_NEW.equals(viewKey)) {
                    // add the new link if this is not the create form
                    sb.append(" (<a href='" + prefixUrl + view.getEntityURL(EntityView.VIEW_NEW, Formats.FORM) + "'>NEW</a>) ");
                }
                if (deleteable
                        && ! EntityView.VIEW_NEW.equals(viewKey)) {
                    String formAction = makeFormViewUrl(prefixUrl, EntityView.VIEW_DELETE, view) + "&_method=DELETE";
                    sb.append("\n  <form name='"+formName+"-del' action='"+formAction+"' style='margin:0px; display:inline;' method='post'>\n");
                    sb.append("    <input type='submit' value='DEL' />\n");
                    sb.append("  </form>\n");
                }
                sb.append("</div>\n");

                if (! EntityView.VIEW_DELETE.equals(viewKey)) {
                    // only render all this stuff for non-delete forms
                    if ( (entityData == null 
                                || entityData.getData() == null)
                            && (EntityView.VIEW_LIST.equals(viewKey) 
                                    || EntityView.VIEW_SHOW.equals(viewKey)) ) {
                        // die if we have no data to encode and this is a SHOW/LIST view
                        throw new EntityEncodingException("Unable to find an entity to encode into the update form; prefix="+prefix+":"+view, prefix);
                    }
                    Object entity = (entityData != null ? entityData.getData() : null);
                    if (entity == null) {
                        // get the entity from the URL instead
                        String id = (view.getEntityReference() != null ? view.getEntityReference().getId() : null);
                        if (id == null) {
                            id = (entityData != null ? entityData.getEntityId() : null);
                        }
                        entity = entityBrokerManager.getSampleEntityObject(prefix, id);
                        if (entity == null) {
                            throw new EntityEncodingException("Unable to find entity data to create form from using prefix="+prefix+",id="+id, prefix);
                        }
                    }
                    Class<?> entityClass = entity.getClass();
                    String formAction;
                    if (EntityView.VIEW_NEW.equals(viewKey)) {
                        formAction = makeFormViewUrl(prefixUrl, EntityView.VIEW_NEW, view);
                    } else {
                        formAction = makeFormViewUrl(prefixUrl, EntityView.VIEW_EDIT, view) + "&_method=PUT";
                    }
                    sb.append("  <form name='"+formName+"-edit' action='"+formAction+"' style='margin:0px;' method='post'>\n");
                    sb.append("    <table border='1'>\n");
                    // get all the read and write fields from this object
                    PropertyMetadata propertyMetadata = analyzeProperties(entityClass);
                    Map<String, Object> fieldValues = getObjectValues(entity);
                    Map<String, Class<?>> readTypes = propertyMetadata.getReadableTypes();
                    Map<String, Class<?>> writeTypes = propertyMetadata.getWritableTypes();
                    Set<String> requiredFieldNames = propertyMetadata.getRequiredProperties();
                    // make sure no one tries to write the id field when not creating entities
                    String idFieldName = EntityDataUtils.getEntityIdField(entityClass);
                    if (idFieldName != null && ! EntityView.VIEW_NEW.equals(viewKey)) {
                        writeTypes.remove(idFieldName);
                    }
                    Map<String, Class<?>> entityTypes = new HashMap<String, Class<?>>(readTypes);
                    entityTypes.putAll(writeTypes);
                    ArrayList<String> keys = new ArrayList<String>(entityTypes.keySet());
                    Collections.sort(keys);
                    for (int i = 0; i < keys.size(); i++) {
                        String fieldName = keys.get(i);
                        Class<?> type = entityTypes.get(fieldName);
                        boolean read = true;
                        boolean write = false;
                        if (! readTypes.containsKey(fieldName)) {
                            // write only
                            read = false;
                            write = true;
                        } else if (! writeTypes.containsKey(fieldName)) {
                            // read only
                            write = false;
                        } else {
                            // read/write
                            write = true;
                        }
                        boolean required = requiredFieldNames.contains(fieldName);
                        // get the printable type names
                        String typeName = type.getName();
                        if (String.class.getName().equals(typeName)) {
                            typeName = "string";
                        } else if (Boolean.class.getName().equals(typeName)) {
                            typeName = "boolean";
                        } else if (Integer.class.getName().equals(typeName)) {
                            typeName = "int";
                        } else if (Long.class.getName().equals(typeName)) {
                            typeName = "long";
                        }
                        sb.append("      <tr><td>"+(i+1)+")&nbsp;</td>"
                                + "<td style='font-weight:bold;'>"+ fieldName +"</td>"
                                + "<td>"+ typeName +"</td><td>");
                        if (read && write) {
                            Object value = fieldValues.get(fieldName);
                            String sVal = "";
                            if (value != null) {
                                sVal = convertToString(value);
                            }
                            sb.append("<input type='text' name=\""+fieldName+"\" value=\""+StringEscapeUtils.escapeHtml4(sVal)+"\" />");
                        } else if (write) {
                            sb.append("<input type='text' name='"+fieldName+"' />");
                        } else if (read) {
                            Object value = fieldValues.get(fieldName);
                            String sVal = "";
                            if (value != null) {
                                sVal = convertToString(value);
                            }
                            sb.append(StringEscapeUtils.escapeHtml4(sVal));
                        }
                        if (required) {
                            sb.append(" <b style='color:red;'>*</b> ");
                        }
                        sb.append("</td></tr>\n");
                    }
                    sb.append("    </table>\n");
                    sb.append("    <input type='submit' value='SAVE' />\n");
                    sb.append("  </form>\n");
                }
                encoded = sb.toString();
            }
        } else {
            // encode the entity itself
            Object toEncode = entityData; // default to encoding the entity data object
            boolean dataOnly = entityData != null && entityData.isDataOnly();
            boolean beanEncodedDirectly = false;
            Map<String, Object> entityProps = new LinkedHashMap<String, Object>();
            if (entityData != null && entityData.getData() != null) {
                if (dataOnly) {
                    toEncode = entityData.getData();
                    // no meta data except properties if there are any
                    entityProps.putAll( entityData.getEntityProperties() );
                } else {
                    if (isBeanClass(entityData.getData().getClass())) {
                        // encode the bean directly if it is one
                        toEncode = entityData.getData();
                        beanEncodedDirectly = true;
                        // add in the extra props
                        entityProps.put(ENTITY_REFERENCE, entityData.getEntityReference());
                        entityProps.put(ENTITY_URL, entityData.getEntityURL());
                        if (entityData.getEntityRef().getId() != null) {
                            entityProps.put(ENTITY_ID, entityData.getEntityRef().getId());
                        }
                        if (entityData.isDisplayTitleSet()) {
                            entityProps.put(ENTITY_TITLE, entityData.getDisplayTitle());
                        }
                    }
                    entityProps.putAll(entityData.getEntityProperties());
                }
            }
            // do the encoding
            String encodingName = prefix;
            if ((beanEncodedDirectly || dataOnly) && (Formats.JSON.equals(format) || Formats.JSONP.equals(format))) {
                encodingName = null;
            }
            try {
                encoded = encodeData(toEncode, format, encodingName, entityProps);
            } catch (RuntimeException e) {
                log.warn("encodeEntity fallback for prefix={} format={} dataOnly={} beanEncodedDirectly={} entityClass={}",
                        prefix, format, dataOnly, beanEncodedDirectly, toEncode != null ? toEncode.getClass() : null, e);
                if (Formats.JSON.equals(format) || Formats.JSONP.equals(format)) {
                    try {
                        Object envelope = mergeProperties(toEncode, entityProps);
                        if (encodingName != null && !encodingName.isEmpty()) {
                            LinkedHashMap<String, Object> wrapper = new LinkedHashMap<String, Object>();
                            wrapper.put(encodingName, envelope);
                            envelope = wrapper;
                        }
                        encoded = JSON_PRETTY_WRITER.writeValueAsString(envelope);
                    } catch (JsonProcessingException jpe) {
                        throw new EntityEncodingException("Failure encoding data (" + toEncode + ") of type ("
                                + toEncode.getClass() + ")", prefix, jpe);
                    }
                } else {
                    throw e;
                }
            }
        }
        return encoded;
    }

    /**
     * @return the form view URLs which should be used with the forms
     */
    protected String makeFormViewUrl(String contextUrl, String viewKey, EntityView view) {
        if (viewKey == null || "".equals(viewKey)) {
            viewKey = EntityView.VIEW_SHOW;
        }
        return contextUrl + BATCH_PREFIX + contextUrl + view.getEntityURL(viewKey, null);
    }

    private static final String DATA_KEY = "data";

    /**
     * Encode data into a given format, can handle any java object,
     * note that unsupported formats will result in an exception
     * 
     * @param data the data to encode (can be a POJO or Map or pretty much any java object)
     * @param format the format to use for output (from {@link Formats})
     * @param name (optional) the name to use for the encoded data (e.g. root node for XML)
     * @param properties (optional) extra properties to add into the encoding, ignored if encoded object is not a map or bean
     * @return the encoded string
     * @throws UnsupportedOperationException if the data cannot be encoded
     */
    public String encodeData(Object data, String format, String name, Map<String, Object> properties) {
        if (format == null) {
            format = Formats.XML;
        }
        if (data == null) {
            return "";
        }
        Map<String, Object> safeProperties = properties == null ? Collections.<String, Object>emptyMap()
                : new LinkedHashMap<String, Object>(properties);
        // Check if data is EntityData before serialization (it will be converted to Map during prepareForSerialization)
        boolean isEntityData = data instanceof org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
        int depthLimit = determineDepthLimit(format, name);
        Object prepared = prepareForSerialization(data, depthLimit);
        try {
            if (Formats.JSON.equals(format) || Formats.JSONP.equals(format)) {
                return encodeJson(prepared, name, safeProperties, isEntityData);
            } else if (Formats.XML.equals(format)) {
                return encodeXml(prepared, name, safeProperties);
            } else if (Formats.HTML.equals(format)) {
                return encodeHtml(prepared, safeProperties);
            }
        } catch (JsonProcessingException e) {
            throw new UnsupportedOperationException("Failure encoding data (" + data + ") of type (" + data.getClass()
                    + "): " + e.getOriginalMessage(), e);
        }
        throw new UnsupportedOperationException("Unsupported format (" + format + ") for encoding data of type "
                + data.getClass());
    }
    
    private int determineDepthLimit(String format, String name) {
        int providerDepth = 0;
        if (name != null) {
            DepthLimitable provider = (DepthLimitable) entityProviderManager
                    .getProviderByPrefixAndCapability(name, DepthLimitable.class);
            if (provider != null) {
                providerDepth = provider.getMaxDepth();
            }
        }
        int jsonLimit = 0;
        if (Formats.JSON.equals(format) || Formats.JSONP.equals(format)) {
            if (entityBrokerManager != null) {
                jsonLimit = entityBrokerManager.getMaxJSONLevel();
            }
        }
        if (providerDepth <= 0) {
            return jsonLimit;
        }
        if (jsonLimit <= 0) {
            return providerDepth;
        }
        return Math.min(providerDepth, jsonLimit);
    }

    private Object prepareForSerialization(Object value, int depthLimit) {
        int maxDepth = depthLimit <= 0 ? -1 : depthLimit;
        return convertValueForSerialization(value, 0, maxDepth, new IdentityHashMap<Object, Boolean>());
    }

    private Object convertValueForSerialization(Object value, int currentDepth, int maxDepth,
            Map<Object, Boolean> visited) {
        if (value == null) {
            return null;
        }
        if (isSimpleValue(value)) {
            return value;
        }
        // Special handling for EntityData - if dataOnly=true, unwrap and return just the data
        if (value instanceof org.sakaiproject.entitybroker.entityprovider.extension.EntityData) {
            org.sakaiproject.entitybroker.entityprovider.extension.EntityData entityData =
                (org.sakaiproject.entitybroker.entityprovider.extension.EntityData) value;
            if (entityData.isDataOnly()) {
                return convertValueForSerialization(entityData.getData(), currentDepth, maxDepth, visited);
            }
        }
        if (maxDepth > -1 && currentDepth >= maxDepth) {
            return summarizeValue(value);
        }
        if (visited.containsKey(value)) {
            return null;
        }
        visited.put(value, Boolean.TRUE);
        try {
            if (value instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) value;
                Map<String, Object> converted = new LinkedHashMap<String, Object>(map.size());
                for (Entry<?, ?> entry : map.entrySet()) {
                    String key = entry.getKey() == null ? null : String.valueOf(entry.getKey());
                    converted.put(key, convertValueForSerialization(entry.getValue(), currentDepth + 1, maxDepth, visited));
                }
                return converted;
            } else if (value instanceof Collection) {
                Collection<?> collection = (Collection<?>) value;
                List<Object> converted = new ArrayList<Object>(collection.size());
                for (Object element : collection) {
                    converted.add(convertValueForSerialization(element, currentDepth + 1, maxDepth, visited));
                }
                return converted;
            } else if (value.getClass().isArray()) {
                int length = java.lang.reflect.Array.getLength(value);
                List<Object> converted = new ArrayList<Object>(length);
                for (int i = 0; i < length; i++) {
                    Object element = java.lang.reflect.Array.get(value, i);
                    converted.add(convertValueForSerialization(element, currentDepth + 1, maxDepth, visited));
                }
                return converted;
            } else if (isBeanClass(value.getClass())) {
                try {
                    Map<String, Object> beanValues = getObjectValues(value);
                    Map<String, Object> converted = new LinkedHashMap<String, Object>(beanValues.size());
                    for (Entry<String, Object> entry : beanValues.entrySet()) {
                        converted.put(entry.getKey(),
                                convertValueForSerialization(entry.getValue(), currentDepth + 1, maxDepth, visited));
                    }
                    return converted;
                } catch (RuntimeException ex) {
                    log.debug("Bean conversion failed for {}, deferring to serializer: {}", value.getClass(), ex.toString());
                    return value; // let Jackson handle it directly
                }
            } else {
                return summarizeValue(value);
            }
        } finally {
            visited.remove(value);
        }
    }

    private boolean isSimpleValue(Object value) {
        if (value == null) {
            return true;
        }
        Class<?> type = value.getClass();
        if (type.isPrimitive() || CharSequence.class.isAssignableFrom(type) || Number.class.isAssignableFrom(type)
                || Boolean.class.isAssignableFrom(type) || Character.class.isAssignableFrom(type)
                || Date.class.isAssignableFrom(type) || Temporal.class.isAssignableFrom(type)
                || Enum.class.isAssignableFrom(type)) {
            return true;
        }
        return false;
    }

    private Object summarizeValue(Object value) {
        if (value == null) {
            return null;
        }
        if (isSimpleValue(value)) {
            return value;
        }
        return Objects.toString(value, null);
    }

    @SuppressWarnings("unchecked")
    private Object mergeProperties(Object data, Map<String, Object> properties) {
        if (properties == null || properties.isEmpty()) {
            return data;
        }
        if (data instanceof Map) {
            Map<String, Object> merged = new LinkedHashMap<String, Object>((Map<String, Object>) data);
            for (Entry<String, Object> entry : properties.entrySet()) {
                merged.putIfAbsent(entry.getKey(), entry.getValue());
            }
            return merged;
        }
        LinkedHashMap<String, Object> merged = new LinkedHashMap<String, Object>(properties);
        merged.put(DATA_KEY, data);
        return merged;
    }

    private String stripXmlDeclaration(String xml) {
        if (xml == null || xml.isEmpty()) {
            return xml;
        }
        int offset = 0;
        int length = xml.length();
        while (offset < length && Character.isWhitespace(xml.charAt(offset))) {
            offset++;
        }
        if (offset < length && xml.startsWith("<?xml", offset)) {
            int endDecl = xml.indexOf("?>", offset);
            if (endDecl != -1) {
                offset = endDecl + 2;
                while (offset < length && Character.isWhitespace(xml.charAt(offset))) {
                    offset++;
                }
                return xml.substring(offset);
            }
        }
        return xml;
    }

    private String encodeJson(Object data, String name, Map<String, Object> properties, boolean isEntityData) throws JsonProcessingException {
        Object envelope = mergeProperties(data, properties);
        // Don't add name wrapper if data is EntityData (it already has entity metadata)
        if (name != null && !name.isEmpty() && !isEntityData) {
            LinkedHashMap<String, Object> wrapper = new LinkedHashMap<String, Object>();
            wrapper.put(name, envelope);
            envelope = wrapper;
        }
        return JSON_PRETTY_WRITER.writeValueAsString(envelope);
    }

    private String encodeXml(Object data, String name, Map<String, Object> properties) throws JsonProcessingException {
        Object envelope = mergeProperties(data, properties);
        ObjectWriter writer = XML_WRITER;
        if (name != null && !name.isEmpty()) {
            writer = writer.withRootName(name);
        }
        String xml = writer.writeValueAsString(envelope);
        return stripXmlDeclaration(xml);
    }

    private String encodeHtml(Object data, Map<String, Object> properties) throws JsonProcessingException {
        Object envelope = mergeProperties(data, properties);
        String rendered;
        if (isSimpleValue(envelope)) {
            rendered = Objects.toString(envelope, "");
        } else {
            rendered = JSON_PRETTY_WRITER.writeValueAsString(envelope);
        }
        return "<pre>" + StringEscapeUtils.escapeHtml4(rendered) + "</pre>";
    }

    private Map<String, Object> ensureDecodedMap(Object value) {
        Map<String, Object> normalized;
        if (value instanceof Map<?, ?> source) {
            normalized = new LinkedHashMap<>(source.size());
            for (Entry<?, ?> entry : source.entrySet()) {
                String key = entry.getKey() == null ? null : String.valueOf(entry.getKey());
                normalized.put(key, entry.getValue());
            }
        } else {
            normalized = new LinkedHashMap<>(1);
            normalized.put(DATA_KEY, value);
        }
        Map<String, Object> simplified = new LinkedHashMap<>(normalized.size());
        for (Entry<String, Object> entry : normalized.entrySet()) {
            simplified.put(entry.getKey(), simplifyDecodedValue(entry.getValue()));
        }
        Object converted = convertTypedMap(new LinkedHashMap<>(simplified));
        if (converted instanceof Map<?, ?> mapResult) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Entry<?, ?> entry : mapResult.entrySet()) {
                String key = entry.getKey() == null ? null : String.valueOf(entry.getKey());
                result.put(key, entry.getValue());
            }
            return result;
        }
        Map<String, Object> wrapper = new LinkedHashMap<>(1);
        wrapper.put(DATA_KEY, converted);
        return wrapper;
    }

    private Object simplifyDecodedValue(Object value) {
        if (value instanceof Map<?, ?> rawMap) {
            Map<String, Object> normalized = new LinkedHashMap<>(rawMap.size());
            for (Entry<?, ?> entry : rawMap.entrySet()) {
                String key = entry.getKey() == null ? null : String.valueOf(entry.getKey());
                normalized.put(key, simplifyDecodedValue(entry.getValue()));
            }
            return convertTypedMap(normalized);
        }
        if (value instanceof Collection<?> collection) {
            List<Object> list = new ArrayList<>(collection.size());
            for (Object element : collection) {
                list.add(simplifyDecodedValue(element));
            }
            return list;
        }
        return value;
    }

    private Object convertTypedMap(Map<String, Object> map) {
        String type = map.get("type") instanceof String ? (String) map.get("type") : null;
        if (type == null) {
            map.remove("");
            return map;
        }
        Map<String, Object> working = new LinkedHashMap<>(map);
        working.remove("type");
        Object classObj = working.remove("class");
        String className = classObj instanceof String ? (String) classObj : null;
        working.remove("size");
        working.remove("length");
        working.remove("component");
        Object text = working.remove("");
        switch (type) {
            case "number":
                return convertNumber(text, className);
            case "boolean":
                return text == null ? null : Boolean.valueOf(text.toString());
            case "string":
                return text == null ? null : text.toString();
            case "date":
                Object dateAttr = working.remove("date");
                return convertDate(text, dateAttr);
            case "array":
                return convertArrayValue(working);
            case "map":
                return removeMetadataKeys(working);
            default:
                return removeMetadataKeys(working);
        }
    }

    private Object convertNumber(Object text, String className) {
        if (text == null) {
            return null;
        }
        String value = text.toString();
        try {
            if (className != null) {
                switch (className) {
                    case "java.lang.Integer":
                    case "int":
                        return Integer.valueOf(value);
                    case "java.lang.Long":
                    case "long":
                        return Long.valueOf(value);
                    case "java.lang.Float":
                    case "float":
                        return Float.valueOf(value);
                    case "java.lang.Double":
                    case "double":
                        return Double.valueOf(value);
                    case "java.lang.Short":
                    case "short":
                        return Short.valueOf(value);
                    case "java.math.BigDecimal":
                        return new BigDecimal(value);
                    case "java.math.BigInteger":
                        return new BigInteger(value);
                    default:
                        break;
                }
            }
            if (value.contains(".")) {
                return Double.valueOf(value);
            }
            long longValue = Long.parseLong(value);
            if (longValue <= Integer.MAX_VALUE && longValue >= Integer.MIN_VALUE) {
                return (int) longValue;
            }
            return longValue;
        } catch (NumberFormatException e) {
            try {
                return new BigDecimal(value);
            } catch (NumberFormatException ex) {
                return value;
            }
        }
    }

    private Object convertDate(Object text, Object isoValue) {
        if (text != null) {
            String value = text.toString();
            try {
                return new Date(Long.parseLong(value));
            } catch (NumberFormatException e) {
                // fall through to ISO parsing
            }
        }
        if (isoValue instanceof String iso && !iso.isEmpty()) {
            try {
                return Date.from(OffsetDateTime.parse(iso).toInstant());
            } catch (DateTimeParseException e) {
                // ignore and fall through
            }
        }
        return isoValue != null ? isoValue : text;
    }

    private Object convertArrayValue(Map<String, Object> working) {
        List<Object> list = new ArrayList<>();
        for (Entry<String, Object> entry : working.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Collection<?>) {
                list.addAll((Collection<?>) value);
            } else if (value != null) {
                list.add(value);
            }
        }
        return list;
    }

    private Map<String, Object> removeMetadataKeys(Map<String, Object> map) {
        Map<String, Object> cleaned = new LinkedHashMap<>();
        for (Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            if (key == null || key.isEmpty()) {
                continue;
            }
            cleaned.put(key, entry.getValue());
        }
        return cleaned;
    }

    private String stripJsonp(String data) {
        String sanitized = data;
        if (sanitized.startsWith("/**/")) {
            sanitized = sanitized.substring(4);
        }
        int start = sanitized.indexOf('(');
        int end = sanitized.lastIndexOf(')');
        if (start >= 0 && end > start) {
            return sanitized.substring(start + 1, end);
        }
        return sanitized;
    }

    /**
     * Clean the JSONP callback parameter to make sure it is sensible
     * @param param The parameter for the callback, should be a String
     * @return The string version of the param or the default callback name
     */
    protected String sanitizeJsonCallback(Object param) {
        if (!(param instanceof String)) {
            return JSON_DEFAULT_CALLBACK;
        }
        String s = ((String) param).trim();
        if (s.length() == 0 || s.length() > 100) {
            return JSON_DEFAULT_CALLBACK;
        }
        if (!JSONP_CALLBACK_PATTERN.matcher(s).matches()) {
            return JSON_DEFAULT_CALLBACK;
        }
        return "/**/" + s;
    }

    /**
     * Decode a string of a specified format into a java map <br/>
     * Returned map can be fed into the {@link #populateBeanFromMap(Object, Map)} if you want to convert it
     * into a known object type <br/>
     * Types are likely to require conversion as guesses are made about the right formats,
     * use of the {@code convertValue(Object, Class)} helper method is recommended
     * 
     * @param data encoded data
     * @param format the format of the encoded data (from {@link Formats})
     * @return a map containing all the data derived from the encoded data
     * @throws UnsupportedOperationException if the data cannot be decoded
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> decodeData(String data, String format) {
        if (format == null) {
            format = Formats.XML;
        }
        Map<String, Object> decoded = new LinkedHashMap<String, Object>();
        if (data == null || "".equals(data)) {
            return decoded;
        }
        String trimmed = data.trim();
        try {
            if (Formats.JSONP.equals(format)) {
                trimmed = stripJsonp(trimmed);
                format = Formats.JSON;
            }
            if (Formats.JSON.equals(format)) {
                Object value = JSON_MAPPER.readValue(trimmed, Object.class);
                return ensureDecodedMap(value);
            } else if (Formats.XML.equals(format)) {
                Object value = XML_MAPPER.readValue(trimmed, Object.class);
                return ensureDecodedMap(value);
            } else if (Formats.HTML.equals(format)) {
                decoded.put(DATA_KEY, trimmed);
                return decoded;
            }
        } catch (IOException e) {
            throw new UnsupportedOperationException("Failure decoding data (" + data + ") for format (" + format + "): "
                    + e.getMessage(), e);
        }
        decoded.put(DATA_KEY, trimmed);
        return decoded;
    }

    private boolean contains(String[] array, String value) {
        if (array == null) {
            return false;
        }
        return Arrays.asList(array).contains(value);
    }

    private String readInputStream(InputStream input) {
        try {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read input stream", e);
        }
    }

    private void populateBeanFromParams(Object entity, Map<String, String[]> params) {
        if (entity == null || params == null) {
            return;
        }
        BeanWrapperImpl wrapper = new BeanWrapperImpl(entity);
        for (Entry<String, String[]> entry : params.entrySet()) {
            String propertyName = entry.getKey();
            String[] values = entry.getValue();
            Object value = null;
            if (values != null) {
                value = values.length == 1 ? values[0] : values;
            }
            applyValue(wrapper, entity, propertyName, value);
        }
    }

    @SuppressWarnings("unchecked")
    private void populateBeanFromMap(Object entity, Map<String, Object> values) {
        if (entity == null || values == null) {
            return;
        }
        BeanWrapperImpl wrapper = new BeanWrapperImpl(entity);
        for (Entry<String, Object> entry : values.entrySet()) {
            String propertyName = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                Class<?> propertyType = null;
                try {
                    PropertyDescriptor descriptor = wrapper.getPropertyDescriptor(propertyName);
                    propertyType = descriptor.getPropertyType();
                } catch (RuntimeException e) {
                    propertyType = null;
                }
                if (propertyType != null && !Map.class.isAssignableFrom(propertyType)) {
                    Object nested = null;
                    if (wrapper.isReadableProperty(propertyName)) {
                        nested = wrapper.getPropertyValue(propertyName);
                    }
                    if (nested == null) {
                        nested = instantiateBean(propertyType);
                        if (nested != null && wrapper.isWritableProperty(propertyName)) {
                            wrapper.setPropertyValue(propertyName, nested);
                        }
                    }
                    if (nested != null) {
                        populateBeanFromMap(nested, (Map<String, Object>) value);
                        continue;
                    }
                }
            }
            applyValue(wrapper, entity, propertyName, value);
        }
    }

    private void applyValue(BeanWrapper wrapper, Object target, String propertyName, Object value) {
        if (wrapper.isWritableProperty(propertyName)) {
            try {
                wrapper.setPropertyValue(propertyName, value);
                return;
            } catch (RuntimeException e) {
                throw e;
            }
        }
        setFieldValue(target, propertyName, value);
    }

    private void setFieldValue(Object target, String fieldName, Object value) {
        Field field = ReflectionUtils.findField(target.getClass(), fieldName);
        if (field == null) {
            return;
        }
        ReflectionUtils.makeAccessible(field);
        Object converted = convertValue(value, field.getType());
        ReflectionUtils.setField(field, target, converted);
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (targetType == null || value == null) {
            return value;
        }
        BeanWrapperImpl converter = new BeanWrapperImpl();
        return converter.convertIfNecessary(value, targetType);
    }

    private PropertyMetadata analyzeProperties(Class<?> type) {
        PropertyMetadata metadata = new PropertyMetadata();
        if (type == null) {
            return metadata;
        }
        try {
            for (PropertyDescriptor descriptor : Introspector.getBeanInfo(type).getPropertyDescriptors()) {
                String name = descriptor.getName();
                if ("class".equals(name)) {
                    continue;
                }
                Class<?> propertyType = descriptor.getPropertyType();
                Method readMethod = descriptor.getReadMethod();
                Method writeMethod = descriptor.getWriteMethod();
                if (readMethod != null) {
                    metadata.readableTypes.put(name, propertyType);
                }
                if (writeMethod != null) {
                    metadata.writableTypes.put(name, propertyType);
                }
                Field field = ReflectionUtils.findField(type, name);
                if (field != null && Modifier.isPublic(field.getModifiers())) {
                    metadata.readableTypes.putIfAbsent(name, field.getType());
                    metadata.writableTypes.putIfAbsent(name, field.getType());
                }
                if (isRequired(field, readMethod, writeMethod)) {
                    metadata.requiredProperties.add(name);
                }
            }
        } catch (IntrospectionException e) {
            throw new IllegalStateException("Failed to introspect " + type, e);
        }
        ReflectionUtils.doWithFields(type, field -> {
            String name = field.getName();
            if ("class".equals(name)) {
                return;
            }
            if (Modifier.isPublic(field.getModifiers())) {
                metadata.readableTypes.putIfAbsent(name, field.getType());
                metadata.writableTypes.putIfAbsent(name, field.getType());
            }
            if (field.isAnnotationPresent(EntityFieldRequired.class)) {
                metadata.requiredProperties.add(name);
            }
        }, field -> !Modifier.isStatic(field.getModifiers()));
        return metadata;
    }

    private Map<String, Object> getObjectValues(Object entity) {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        if (entity == null) {
            return values;
        }
        // Build a set of transient field names to exclude
        Set<String> transientFields = new HashSet<>();
        ReflectionUtils.doWithFields(entity.getClass(), field -> {
            if (Modifier.isTransient(field.getModifiers())) {
                transientFields.add(field.getName());
            }
        });

        BeanWrapperImpl wrapper = new BeanWrapperImpl(entity);
        for (PropertyDescriptor descriptor : wrapper.getPropertyDescriptors()) {
            String name = descriptor.getName();
            if ("class".equals(name)) {
                continue;
            }
            // Skip properties that have transient backing fields
            if (transientFields.contains(name)) {
                continue;
            }
            if (wrapper.isReadableProperty(name)) {
                values.put(name, wrapper.getPropertyValue(name));
            }
        }
        ReflectionUtils.doWithFields(entity.getClass(), field -> {
            if (!Modifier.isPublic(field.getModifiers())) {
                return;
            }
            // Skip transient fields
            if (Modifier.isTransient(field.getModifiers())) {
                return;
            }
            String name = field.getName();
            if (values.containsKey(name) || "class".equals(name)) {
                return;
            }
            ReflectionUtils.makeAccessible(field);
            values.put(name, ReflectionUtils.getField(field, entity));
        }, field -> !Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()));
        return values;
    }

    private boolean isBeanClass(Class<?> type) {
        if (type == null) {
            return false;
        }
        if (type.isPrimitive() || type.isArray()) {
            return false;
        }
        if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
            return false;
        }
        if (Number.class.isAssignableFrom(type) || CharSequence.class.isAssignableFrom(type)
                || Date.class.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type)
                || Enum.class.isAssignableFrom(type)) {
            return false;
        }
        try {
            type.getDeclaredConstructor();
            return true;
        } catch (NoSuchMethodException e) {
            // Fall through and try to detect readable properties or fields
        }
        try {
            for (PropertyDescriptor descriptor : Introspector.getBeanInfo(type).getPropertyDescriptors()) {
                if ("class".equals(descriptor.getName())) {
                    continue;
                }
                Method readMethod = descriptor.getReadMethod();
                if (readMethod != null && !Modifier.isStatic(readMethod.getModifiers())) {
                    return true;
                }
            }
        } catch (IntrospectionException ex) {
            log.debug("Failed to introspect {} while determining bean eligibility", type, ex);
        }
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            return true;
        }
        return false;
    }

    private boolean isRequired(Field field, Method readMethod, Method writeMethod) {
        if (field != null && field.isAnnotationPresent(EntityFieldRequired.class)) {
            return true;
        }
        if (readMethod != null && readMethod.isAnnotationPresent(EntityFieldRequired.class)) {
            return true;
        }
        if (writeMethod != null && writeMethod.isAnnotationPresent(EntityFieldRequired.class)) {
            return true;
        }
        return false;
    }

    private String convertToString(Object value) {
        if (value == null) {
            return "";
        }
        Object converted = convertValue(value, String.class);
        return Objects.toString(converted, "");
    }

    private Object instantiateBean(Class<?> type) {
        if (type == null) {
            return null;
        }
        if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
            return null;
        }
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static final class PropertyMetadata {
        private final Map<String, Class<?>> readableTypes = new LinkedHashMap<String, Class<?>>();
        private final Map<String, Class<?>> writableTypes = new LinkedHashMap<String, Class<?>>();
        private final Set<String> requiredProperties = new HashSet<String>();

        Map<String, Class<?>> getReadableTypes() {
            return readableTypes;
        }

        Map<String, Class<?>> getWritableTypes() {
            return writableTypes;
        }

        Set<String> getRequiredProperties() {
            return requiredProperties;
        }
    }

    /**
     * Using GSON is hopeless:
     * http://code.google.com/p/google-gson/issues/detail?id=45
     * 
     * Gson gson = getGson();
     * encoded = gson.toJson(data, new TypeToken<Map<String, Object>>() {}.getType());
     * decoded = gson.fromJson(data, new TypeToken<Map<String, Object>>() {}.getType());
     */
    //    protected SoftReference<Gson> gsonCoder = null;
    //    /**
    //     * Get the gson encoder in an efficient way to avoid recreating it over and over and over again
    //     * @return the gson encoder
    //     */
    //    protected Gson getGson() {
    //        Gson gson = gsonCoder == null ? null : gsonCoder.get();
    //        if (gson == null) {
    //            gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
    //            gsonCoder = new SoftReference<Gson>(gson);
    //        }
    //        return gson;
    //    }

}
