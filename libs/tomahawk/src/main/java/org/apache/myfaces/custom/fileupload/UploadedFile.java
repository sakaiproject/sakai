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
package org.apache.myfaces.custom.fileupload;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;



/**
 * @author Manfred Geiler (latest modification by $Author: mmarinschek $)
 * @version $Revision: 599022 $ $Date: 2007-11-28 10:14:17 -0500 (Wed, 28 Nov 2007) $
 */
public interface UploadedFile extends Serializable
{


    /**
     * Answer the uploaded file contents.
     *
     * @return file contents
     */
    byte[] getBytes() throws IOException;


    /**
     * Answer the uploaded file contents input stream
     *
     * @throws IOException
     * @return InputStream
     */
    InputStream getInputStream() throws IOException;


    /**
     * @return Returns the contentType.
     */
    String getContentType();



    /**
     * @return Returns the name.
     */
    String getName();


    /**
     * Returns the size of this file.
     * @return long
     */
    long getSize();
    
    /**Allows to get more information/interact more with the file, depending
     * on where it is stored
     * 
     * @return StorageStrategy the storage strategy of this file, 
     */
    StorageStrategy getStorageStrategy();
}
