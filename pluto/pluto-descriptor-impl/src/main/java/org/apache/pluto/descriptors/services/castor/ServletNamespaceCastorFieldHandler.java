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

import org.apache.pluto.descriptors.services.Constants;
import org.apache.pluto.descriptors.servlet.WebAppDD;
import org.exolab.castor.mapping.AbstractFieldHandler;

/**
 * This class manages the <code>xmlns</code> attribute on the &lt;web-app&gt; for
 * Servlet 2.4 and greater deployment descriptors.
 *
 * Servlet 2.3 does not have a <code>xmlns</code> attribute, for the &lt;web-app&gt;
 * while Servlet 2.4 and higher do.
 *
 * @since Mar 3, 2007
 * @version $Id: ServletNamespaceCastorFieldHandler.java 607452 2007-12-29 17:52:38Z esm $
 */
public class ServletNamespaceCastorFieldHandler extends AbstractFieldHandler
{

    public Object getValue(Object webAppDD) throws IllegalStateException
    {
        if (! ( webAppDD instanceof WebAppDD ) )
        {
            throw new ClassCastException( "Error: was expecting a " +
                    WebAppDD.class.getName() + " but received a " + webAppDD.getClass().getName() );
        }

        String servletVersion = ((WebAppDD)webAppDD).getServletVersion();

        if ( "2.3".equals(servletVersion) )
        {
            return null;
        }
        else if ( "2.4".equals(servletVersion) )
        {
            return Constants.WEB_XML_24_SCHEMA_NAMESPACE;
        }

        return servletVersion;
    }

    public Object newInstance(Object arg0) throws IllegalStateException
    {
        // Do nothing
        return null;
    }

    public Object newInstance(Object arg0, Object[] arg1)
            throws IllegalStateException
    {
        // Do nothing
        return null;
    }

    public void resetValue(Object arg0) throws IllegalStateException,
            IllegalArgumentException
    {
        // Do nothing
    }

    public void setValue(Object webAppDD, Object namespaceValue) throws IllegalStateException, IllegalArgumentException
    {
        if (! ( webAppDD instanceof WebAppDD ) )
        {
            throw new ClassCastException( "Error: was expecting a " +
                    WebAppDD.class.getName() + " but received a " + webAppDD.getClass().getName() );
        }
        if (! ( namespaceValue instanceof String) )
        {
            throw new ClassCastException( "Error: was expecting a " +
                    String.class.getName() + " but received a " + namespaceValue.getClass().getName() );
        }

        ((WebAppDD)webAppDD).setXmlNs((String)namespaceValue);
    }

}
