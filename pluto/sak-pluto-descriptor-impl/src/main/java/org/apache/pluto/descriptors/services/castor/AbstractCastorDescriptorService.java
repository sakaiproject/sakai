/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pluto.descriptors.services.castor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.util.LocalConfiguration;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Abstract deployment descriptor support class.
 * This Base class provides support for reading
 * and writing deployment descriptors using Castor.
 *
 * @version $Id: AbstractCastorDescriptorService.java 156743 2005-03-10 05:50:30Z ddewolf $
 * @since Mar 5, 2005
 */
abstract class AbstractCastorDescriptorService {

    /**
     * Logger
     */
    private static final Log LOG = LogFactory.getLog(AbstractCastorDescriptorService.class);
    
    /**
     * The name of the system property that when set to the string
     * value "true" has Castor use JAXP instead of the parser specified
     * by the <code>org.exolab.castor.parser</code> property.
     * 
     * By using JAXP, the Pluto descriptor services no longer require
     * an XML parser in a shared classloader.
     * 
     * By default the value of this property is "true" For Pluto 1.2 and
     * higher.
     */
    private static final String JAXP_PROPERTY = "org.apache.pluto.useJaxp";
    
    /**
     * Default value of org.apache.pluto.useJaxp system property.
     * In Pluto 1.2.x it should be "true".  In Pluto 1.1.4 and up (but still
     * within the 1.1 line) it should be "false".
     */
    private static final boolean JAXP_DEFAULT = false;

    /**
     * Whether or not Castor should use JAXP.  If Castor is not using
     * JAXP, then default to the parser specified by 
     * <code>org.exolab.castor.parser</code>.
     */
    protected static final boolean USING_JAXP;
    
    static {
        final String useJaxpStr = System.getProperty(JAXP_PROPERTY);
        
        //No system property, try JDK version detection
        if (useJaxpStr == null) {
            final String javaSpecVersionStr = System.getProperty("java.specification.version");
            
            Double javaSpecVersion = null;
            try {
                javaSpecVersion = Double.valueOf(javaSpecVersionStr);
            }
            catch (NumberFormatException nfe) {
                //ignore, the null javaSpecVersion is handled correctly below
            }

            if (javaSpecVersion != null && javaSpecVersion.doubleValue() >= 1.5) {
                USING_JAXP = true;
            }
            else {
                USING_JAXP = JAXP_DEFAULT;
            }
        }
        else {
            USING_JAXP = Boolean.valueOf(useJaxpStr).booleanValue();
        }
    }
    
    /**
     * Read the and convert the descriptor into it's Object graph.
     * @return
     * @throws IOException
     */
    protected Object readInternal(InputStream is) throws IOException {
        Object object = null;
        try {
            // Use JAXP if we are instructed to do so.
            if (USING_JAXP) {
                LocalConfiguration castorConfig = LocalConfiguration.getInstance();
                // empty string means "use JAXP" for Castor
                castorConfig.getProperties().setProperty("org.exolab.castor.parser", "");
                castorConfig.getProperties().setProperty("org.exolab.castor.xml.serializer.factory", 
                        "org.exolab.castor.xml.XercesJDK5XMLSerializerFactory" );
            }
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Pluto descriptor service implementation using JAXP: [" + USING_JAXP + "]");                        
            }
            
            Mapping mapping = getCastorMapping();
            Unmarshaller unmarshaller = new Unmarshaller(mapping);
            unmarshaller.setEntityResolver(new EntityResolverImpl());
            unmarshaller.setIgnoreExtraElements(getIgnoreExtraElements());

            if(is!=null) {
                InputStreamReader in = new InputStreamReader(is);
                object = unmarshaller.unmarshal(in);
            }
        }
        catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            LOG.error(e.getMessage(), e);
            IOException ioe = new IOException(e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }
        finally {
            if(is != null) {
                is.close();
            }
        }
        return object;
    }

    /**
     * Write the object graph to it's descriptor.
     * @param object
     * @throws IOException
     */
    protected void writeInternal(Object object, OutputStream out) throws IOException {

        OutputStreamWriter writer =
            new OutputStreamWriter(out);

        try {
            // Construct the marshaller with a Writer instead of
            // a SAX DocumentHandler.  When you supply a document
            // handler, you can't set call marshaller.setDocType(String, String)

            // See Also:
            //  https://issues.apache.org/jira/browse/PLUTO-312
            //  http://castor.org/javadoc/org/exolab/castor/xml/Marshaller.html#setDoctype(java.lang.String,%20java.lang.String)
            Marshaller marshaller = new Marshaller(writer);
            marshaller.setMapping(getCastorMapping());
            
            // Use JAXP if we are instructed to do so.
            LocalConfiguration castorConfig = LocalConfiguration.getInstance();
            if (USING_JAXP) {                
                // empty string means "use JAXP" for Castor
                castorConfig.getProperties().setProperty("org.exolab.castor.parser", "" );                
                castorConfig.getProperties().setProperty("org.exolab.castor.xml.serializer.factory", 
                        "org.exolab.castor.xml.XercesJDK5XMLSerializerFactory" );
            }
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Pluto descriptor service implementation using JAXP: [" + USING_JAXP + "]");                        
            }
            
            castorConfig.getProperties().setProperty("org.exolab.castor.indent", "true");
            setCastorMarshallerOptions(marshaller, object);
            marshaller.marshal(object);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            IOException ioe = new IOException(e.getMessage());
            ioe.initCause(e);            
            throw ioe;
        }
        finally {
            writer.flush();
            writer.close();
        }
    }

    protected boolean getIgnoreExtraElements() {
        return false;
    }

    protected abstract Mapping getCastorMapping() throws IOException, MappingException;
    protected abstract String getPublicId();
    protected abstract String getDTDUri();

    /**
     * Subclasses should override this method if they need to set
     * options on the Castor marshaller, such as a doctype.
     *
     * @param marshaller the Castor Marshaller
     * @param beingMarshalled the Object being marshalled by Castor.
     */
    protected void setCastorMarshallerOptions(final Marshaller marshaller, final Object beingMarshalled) {

    }

}

