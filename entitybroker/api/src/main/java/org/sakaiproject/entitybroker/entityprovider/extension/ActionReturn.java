/**
 * $Id$
 * $URL$
 * ActionReturn.java - entity-broker - Jul 25, 2008 4:19:15 PM - azeckoski
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

package org.sakaiproject.entitybroker.entityprovider.extension;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;


/**
 * A special object used to return specialized results from a custom action execution,
 * includes fields to allow for handling of encoded binary data and to indicate
 * that entity action processing should continue as it would have if there
 * had been no custom action call (rather than exiting the standard chain)
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class ActionReturn {

    public static enum Header {
        EXPIRES ("Expires"),
        DATE ("Date"),
        ETAG ("ETag"),
        LAST_MODIFIED ("Last-Modified"),
        CACHE_CONTROL ("Cache-Control");

        private String value;
        Header(String value) { this.value = value; }
        @Override
        public String toString() {
            return value;
        }
    };

    /**
     * The encoding to use for the output when it is returned
     */
    public String encoding = Formats.UTF_8;
    /**
     * The MIME type to use for the output when it is returned
     */
    public String mimeType;
    /**
     * the data to output (should use a provided OutputStream), can be binary, leave this null if not used
     */
    public OutputStream output;
    /**
     * the output data in string form, leave this null if not used
     */
    public String outputString;
    /**
     * An entity object to return, leave as null if not used
     */
    public EntityData entityData;
    /**
     * A List of entities to return, leave as null if not used
     */
    public List<EntityData> entitiesList;
    /**
     * Indicates the format (from {@link Formats}) to return the entity data in if there is any,
     * if using an outputstream, use encoding and mimetype
     */
    public String format;    
    /**
     * Indicates the format (from {@link Formats}) to return the entity data in if there is any,
     * if using an outputstream, use encoding and mimetype
     */
    public void setFormat(String format) {
        this.format = format;
    }
    /**
     * Optional headers to include in the response (can use the header constants if desired: {@link Header}),
     * Example: <br/>
     * headers.put(Header.EXPIRES.toString(), "12378389233737"); <br/>
     * headers.put("myHeaderKey", "my Value to put in the header"); <br/>
     */
    public Map<String, String> headers;
    /**
     * Set the optional headers to include in the response (can use the header constants if desired: {@link Header}),
     * Example: <br/>
     * headers.put(Header.EXPIRES.toString(), "12378389233737"); <br/>
     * headers.put("myHeaderKey", "my Value to put in the header"); <br/>
     */
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    /**
     * The response code to send back from the processing of this action,
     * the default which also indicates the response code as determined by the system should be used is -1
     */
    public int responseCode = -1;
    /**
     * @param responseCode the response code to send back from the processing of this action,
     * the default which also indicates the response code as determined by the system should be used is -1
     */
    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }


    /**
     * Set the OutputStream to indicate it was used,
     * uses default encoding UTF-8 and type of text/xml
     * @param output an OutputStream of data to send as is
     */
    public ActionReturn(OutputStream output) {
        this.output = output;
    }

    /**
     * Set a string of data to return,
     * uses default encoding UTF-8 and type of text/xml
     * @param outputString a string to send as is
     */
    public ActionReturn(String outputString) {
        this.outputString = outputString;
    }

    /**
     * Create a return that is appropriate for sending binary data or a large chunk of text
     * @param encoding the encoding to use for the binary data
     * @param mimeType the mime type to use for the binary data
     * @param output the stream of binary data
     */
    public ActionReturn(String encoding, String mimeType, OutputStream output) {
        this.encoding = encoding;
        this.mimeType = mimeType;
        this.output = output;
    }

    /**
     * Create a return that is appropriate for sending back a string
     * @param encoding the encoding to use for the binary data
     * @param mimeType the mime type to use for the binary data
     * @param outputString the string value
     */
    public ActionReturn(String encoding, String mimeType, String outputString) {
        this.encoding = encoding;
        this.mimeType = mimeType;
        this.outputString = outputString;
    }

    /**
     * Special constructor which will ensure the data is output exactly as is without adding in the entity meta data
     * @param data the data to encode (any java objects including collections, POJOs, maps, etc.)
     */
    public ActionReturn(Object data) {
        this(data, null, null);
    }

    /**
     * Special constructor which will ensure the data is output exactly as is without adding in the entity meta data
     * @param data the data to encode (any java objects including collections, POJOs, maps, etc.)
     * @param headers (optional) headers to include in the response (can use the header constants if desired: {@link Header})
     */
    public ActionReturn(Object data, Map<String, String> headers) {
        this(data, headers, null);
    }

    /**
     * Special constructor which will ensure the data is output exactly as is without adding in the entity meta data
     * @param data the data to encode (any java objects including collections, POJOs, maps, etc.)
     * @param headers (optional) headers to include in the response (can use the header constants if desired: {@link Header})
     * @param format (optional) the format to return this data in (from {@link Formats}), e.g. Formats.XML
     */
    public ActionReturn(Object data, Map<String, String> headers, String format) {
        if (data != null && EntityData.class.isAssignableFrom(data.getClass())) {
            this.entityData = (EntityData) data;
        } else {
            this.entityData = new EntityData(data);
        }
        this.format = format;
        setHeaders(headers);
    }

    /**
     * Create a return that is appropriate for sending back an entity
     * @param entityData an entity object ({@link EntityData} object)
     * @param format (optional) the format to return this data in (from {@link Formats}), e.g. Formats.XML
     */
    public ActionReturn(EntityData entityData, String format) {
        this.entityData = entityData;
        this.format = format;
    }

    /**
     * Create a return that is appropriate for sending back a list of entities
     * @param entityData a List of entities ({@link EntityData}) (can be empty)
     * @param format (optional) the format to return this data in (from {@link Formats}), e.g. Formats.XML
     */
    public ActionReturn(List<EntityData> entitiesList, String format) {
        this.entitiesList = entitiesList;
        this.format = format;
    }

    @Override
    public String toString() {
        return "actionReturn: encode=" + this.encoding + ": format=" + this.format + ": mime=" + this.mimeType 
        + ": output=" + (this.output != null) + ": list=" + (this.entitiesList != null) 
        + ": data=" + (this.entityData != null) + ": headers=" + this.headers;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getMimeType() {
        return mimeType;
    }

    public OutputStream getOutput() {
        return output;
    }

    public String getOutputString() {
        return outputString;
    }

    public EntityData getEntityData() {
        return entityData;
    }

    public List<EntityData> getEntitiesList() {
        return entitiesList;
    }

    public String getFormat() {
        return format;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public int getResponseCode() {
        return responseCode;
    }
}
