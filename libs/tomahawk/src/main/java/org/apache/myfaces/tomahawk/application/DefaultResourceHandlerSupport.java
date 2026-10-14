/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.tomahawk.application;

import org.apache.myfaces.shared_tomahawk.resource.BaseResourceHandlerSupport;
import org.apache.myfaces.shared_tomahawk.resource.ResourceLoader;

/**
 * A ResourceHandlerSupport implementation for use with standard Java Servlet engines,
 * ie an engine that supports jakarta.servlet, and uses a standard web.xml file.
 * 
 * @author Leonardo Uribe (latest modification by $Author: bommel $)
 * @version $Revision: 915763 $ $Date: 2010-02-24 07:24:11 -0500 (Mié, 24 Feb 2010) $
 */
public class DefaultResourceHandlerSupport extends BaseResourceHandlerSupport
{

    public DefaultResourceHandlerSupport()
    {
    }

    @Override
    public ResourceLoader[] getResourceLoaders()
    {
        return null;
    }

}
