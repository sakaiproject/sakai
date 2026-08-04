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
package org.apache.pluto.util.assemble;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Resolves entities by parsing the path from the System ID and resolving
 * the file using {@link Class#getResourceAsStream}.
 */
public class ResourceEntityResolver implements EntityResolver
{
    public InputSource resolveEntity( String publicId, String systemId ) throws SAXException, IOException
    {
        if ( systemId.lastIndexOf( "/" ) == systemId.trim().length() )
        {
            return null;
        }

        String path;

        if ( systemId.indexOf( "/" ) > -1 )
        {
            path = systemId.substring( systemId.lastIndexOf( "/" ) + 1 );
        }
        else
        {
            path = systemId.trim();
        }

        InputStream in = this.getClass().getResourceAsStream( path );
        return ( in == null ) ? null : new InputSource( in );
    }
}
