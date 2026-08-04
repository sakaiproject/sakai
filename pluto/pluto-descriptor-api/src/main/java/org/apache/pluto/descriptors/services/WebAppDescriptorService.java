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
package org.apache.pluto.descriptors.services;

import org.apache.pluto.descriptors.servlet.WebAppDD;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * WebApplication configuration as contained
 * within the web.xml Deployment Descriptor.
 *
 * @version $Id: WebAppDescriptorService.java 157038 2005-03-11 03:44:40Z ddewolf $
 * @since Feb 28, 2005
 */
public interface WebAppDescriptorService {

    /**
     * Retrieve the WebApp deployment descriptor
     * (web.xml).
     * @return Object representation of the descriptor.
     * @throws IOException if an IO error occurs.
     */
    WebAppDD read(InputStream in) throws IOException;

    /**
     * Write the WebApp deployment descriptor
     * (web.xml).
     * @param dd
     * @param out output stream to which the descriptor should be written
     * @throws IOException if an IO error occurs.
     */
    void write(WebAppDD dd, OutputStream out) throws IOException;
}
