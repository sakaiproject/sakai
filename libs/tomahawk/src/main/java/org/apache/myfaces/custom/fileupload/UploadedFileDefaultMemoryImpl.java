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

import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.DiskFileItem;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;


/**
 * @author Sylvain Vieujot (latest modification by $Author: lu4242 $)
 * @version $Revision: 782291 $ $Date: 2009-06-06 13:03:52 -0500 (Sat, 06 Jun 2009) $
 */
public class UploadedFileDefaultMemoryImpl extends UploadedFileDefaultImplBase
{
    private static final long serialVersionUID = -6006333070975059090L;
    private byte[] bytes;
    private StorageStrategy storageStrategy;
    private transient FileItem fileItem = null;

    public UploadedFileDefaultMemoryImpl(final FileItem fileItem) throws IOException
    {
        super(fileItem.getName(), fileItem.getContentType());
        int sizeInBytes = (int)fileItem.getSize();
        bytes = new byte[sizeInBytes];
        this.fileItem = fileItem;
        fileItem.getInputStream().read(bytes);
        this.storageStrategy = new DefaultMemoryStorageStrategy();
    }
    
    private class DefaultMemoryStorageStrategy 
        extends StorageStrategy implements Serializable
    {
        private static final long serialVersionUID = 3610866246514636068L;

        public void deleteFileContents()
        {
          // UploadedFileDefaultMemoryImpl.this.fileItem becomes null 
          // when the parent class is serialized and deserialized.
          // In this case, the instance contained by the original
          // object is garbage collected, so we don't have to 
          // worry about it.
          if (UploadedFileDefaultMemoryImpl.this.fileItem != null)
          {
              try
              {
                  UploadedFileDefaultMemoryImpl.this.fileItem.delete();
              }
              catch (java.io.IOException e)
              {
                  // fileupload2 declares IOException on delete(); nothing useful to do here
              }
          }
          bytes = null;
        }
    }

    /**
     * Answer the uploaded file contents.
     *
     * @return file contents
     */
    public byte[] getBytes()
    {
        return bytes;
    }


    /**
     * Answer the uploaded file contents input stream
     *
     * @return InputStream
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException
    {
        return new ByteArrayInputStream( bytes );
    }

    /**
     * Answer the size of this file.
     * @return long
     */
    public long getSize() {
        if( bytes == null )
            return 0;
        return bytes.length;
    }

    public StorageStrategy getStorageStrategy() {
      return storageStrategy;
    }    
}
