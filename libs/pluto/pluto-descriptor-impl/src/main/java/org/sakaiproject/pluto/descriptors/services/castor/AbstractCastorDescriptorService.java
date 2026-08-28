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
package org.sakaiproject.pluto.descriptors.services.castor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.util.LocalConfiguration;
import org.exolab.castor.xml.Unmarshaller;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Abstract deployment descriptor support class.
 * Provides Castor-based <em>read</em> support for deployment descriptors.
 * Write/marshall support was removed: Sakai only loads descriptors
 * (see PortletDescriptorRegistry) and this tree is frozen for that use.
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
    private static final String JAXP_PROPERTY = "org.sakaiproject.pluto.useJaxp";
    
    /**
     * Default value of org.sakaiproject.pluto.useJaxp system property.
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
            configureCastorXml();
            
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

    protected boolean getIgnoreExtraElements() {
        return false;
    }

    /**
     * Configure Castor before creating an Unmarshaller.
     * Use the JDK JAXP parser; no Xerces serializer is required for read-only use.
     */
    private static void configureCastorXml() {
        if (!USING_JAXP) {
            return;
        }
        LocalConfiguration castorConfig = LocalConfiguration.getInstance();
        // empty string means "use JAXP" for Castor
        castorConfig.getProperties().setProperty("org.exolab.castor.parser", "");
    }

    protected abstract Mapping getCastorMapping() throws IOException, MappingException;

}
