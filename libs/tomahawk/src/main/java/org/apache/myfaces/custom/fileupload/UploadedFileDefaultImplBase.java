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


/**
 * @author Sylvain Vieujot (latest modification by $Author: grantsmith $)
 * @version $Revision: 472638 $ $Date: 2006-11-08 15:54:13 -0500 (Wed, 08 Nov 2006) $
 */
public abstract class UploadedFileDefaultImplBase implements UploadedFile
{

    private String _name = null;
    private String _contentType = null;


    protected UploadedFileDefaultImplBase(String name, String contentType)
    {
        _name = name;
        _contentType = contentType;
    }

    /**
     * Answer the uploaded file contents.
     *
     * @return file contents
     */
    public abstract byte[] getBytes() throws IOException;


    /**
     * Answer the uploaded file contents input stream
     *
     * @return InputStream
     * @throws IOException
     */
    public abstract InputStream getInputStream() throws IOException;


    /**
     * @return Returns the _contentType.
     */
    public String getContentType()
    {
        return _contentType;
    }


    /**
     * @return Returns the _name.
     */
    public String getName()
    {
        return _name;
    }


    /**
     * Answer the size of this file.
     * @return long
     */
    public abstract long getSize();
}
