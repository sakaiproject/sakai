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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.DiskFileItem;


/**
 * @author Sylvain Vieujot (latest modification by $Author: lu4242 $)
 * @version $Revision: 782291 $ $Date: 2009-06-06 13:03:52 -0500 (Sat, 06 Jun 2009) $
 */
public class UploadedFileDefaultFileImpl extends UploadedFileDefaultImplBase
{
  private static final long serialVersionUID = -6401426361519246443L;
  private transient DiskFileItem fileItem = null;
  private StorageStrategy storageStrategy;

    public UploadedFileDefaultFileImpl(final FileItem fileItem) throws IOException
    {
        super(fileItem.getName(), fileItem.getContentType());
        this.fileItem = (DiskFileItem) fileItem;
        storageStrategy = new DefaultDiskStorageStrategy();
    }

    private class DefaultDiskStorageStrategy 
        extends DiskStorageStrategy implements Serializable
    {
        private static final long serialVersionUID = 5191237379179109587L;
        
        public DefaultDiskStorageStrategy()
        {
        }

        public File getTempFile()
        {
            if (UploadedFileDefaultFileImpl.this.fileItem != null)
            {
                // fileupload2 exposes a Path rather than a File
                return UploadedFileDefaultFileImpl.this.fileItem.getPath().toFile();
            }
            else
            {
                return null;
            }
        }

        public void deleteFileContents()
        {
            // UploadedFileDefaultFileImpl.this.fileItem becomes null 
            // when the parent class is serialized and deserialized.
            // In this case, the instance contained by the original
            // object is garbage collected, so we don't have to 
            // worry about it.
            if (UploadedFileDefaultFileImpl.this.fileItem != null)
            {
                try
                {
                    UploadedFileDefaultFileImpl.this.fileItem.delete();
                }
                catch (java.io.IOException e)
                {
                    // fileupload2 declares IOException on delete(); nothing useful to do here
                }
            }
        }
    }

    /**
     * Answer the uploaded file contents.
     *
     * @return file contents
     */
    public byte[] getBytes() throws IOException
    {
        byte[] bytes = new byte[(int)getSize()];
        if (fileItem != null) fileItem.getInputStream().read(bytes);
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
        return fileItem != null
               ? fileItem.getInputStream()
               : new ByteArrayInputStream(new byte[0]);
    }


    /**
     * Answer the size of this file.
     * @return long
     */
    public long getSize()
    {
        return fileItem != null ? fileItem.getSize() : 0;
    }


    public StorageStrategy getStorageStrategy() {
      return storageStrategy;
    }
}
