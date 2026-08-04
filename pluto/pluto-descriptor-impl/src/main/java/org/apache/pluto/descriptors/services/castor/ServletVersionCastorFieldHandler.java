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

import org.apache.pluto.descriptors.servlet.WebAppDD;
import org.exolab.castor.mapping.AbstractFieldHandler;

/**
 * This class manages the <code>version</code> attribute on the &lt;web-app&gt; for
 * Servlet 2.3 and greater deployment descriptors.
 *
 * Servlet 2.3 does not have a <code>version</code> attribute, for the &lt;web-app&gt;
 * while Servlet 2.4 and higher do.
 *
 * @since Mar 3, 2007
 * @version $Id: ServletVersionCastorFieldHandler.java 516329 2007-03-09 08:43:46Z cziegeler $
 */
public class ServletVersionCastorFieldHandler extends AbstractFieldHandler
{

    public Object getValue(Object webAppDD) throws IllegalStateException
    {
        if (! ( webAppDD instanceof WebAppDD ) )
        {
            throw new ClassCastException( "Error: was expecting a " +
                    WebAppDD.class.getName() + " but received a " + webAppDD.getClass().getName() );
        }

        String servletVersion = ((WebAppDD)webAppDD).getServletVersion();

        // if the servlet version is 2.3, we don't want to include it in the
        // XML output as a version attribute.  Only servlet 2.4 and higher
        // have a version attribute in their <web-app> element.

        if ( "2.3".equals(servletVersion) )
        {
            return null;
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

    public void setValue(Object webAppDD, Object servletVersionValue)
            throws IllegalStateException, IllegalArgumentException
    {
        if (! ( webAppDD instanceof WebAppDD ) )
        {
            throw new ClassCastException( "Error: was expecting a " +
                    WebAppDD.class.getName() + " but received a " + webAppDD.getClass().getName() );
        }
        if (! ( servletVersionValue instanceof String) )
        {
            throw new ClassCastException( "Error: was expecting a " +
                    String.class.getName() + " but received a " + servletVersionValue.getClass().getName() );
        }

        ((WebAppDD)webAppDD).setServletVersion((String)servletVersionValue);
    }

}
