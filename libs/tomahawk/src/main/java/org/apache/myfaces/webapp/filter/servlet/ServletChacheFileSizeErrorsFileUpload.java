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
package org.apache.myfaces.webapp.filter.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileItemFactory;
import org.apache.commons.fileupload2.core.FileItemHeaders;
import org.apache.commons.fileupload2.core.FileItemHeadersProvider;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.core.FileItemInput;
import java.io.OutputStream;
import org.apache.commons.fileupload2.core.AbstractFileUpload;
import org.apache.commons.fileupload2.core.FileUploadByteCountLimitException;
import java.io.OutputStream;
import org.apache.commons.fileupload2.core.AbstractFileUpload;
import org.apache.commons.fileupload2.core.FileUploadByteCountLimitException;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.core.RequestContext;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletRequestContext;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.io.IOUtils;

/**
 * Custom implementation of JakartaServletFileUpload intended to parse request but it
 * catch and swallow FileSizeLimitExceededExceptions in order to return as
 * many usable items as possible.
 * 
 * <p>
 * NOTE: This class should be used(instantiated) only by 
 * ServletMultipartRequestWrapper. By that reason, it could be changed
 * or removed in the future.
 * </p>
 * 
 * @since 1.1.9
 * @author Phillip Webb
 * @author Leonardo Uribe (latest modification by $Author: lu4242 $)
 * @version $Revision: 703744 $ $Date: 2008-10-11 17:28:20 -0500 (Sat, 11 Oct 2008) $
 *
 */
public class ServletChacheFileSizeErrorsFileUpload extends JakartaServletFileUpload
{
   
    public ServletChacheFileSizeErrorsFileUpload()
    {
        super();
    }
    
    public ServletChacheFileSizeErrorsFileUpload(FileItemFactory fileItemFactory)
    {
        super(fileItemFactory);
    }
        
    /**
     * Determine the length of an uploaded file as indicated by the header.
     * 
     * @param pHeaders
     * @return length or -1
     */
    private long getContentLength(FileItemHeaders pHeaders) {
        try {
            return Long.parseLong(pHeaders.getHeader(AbstractFileUpload.CONTENT_LENGTH));
        } catch (Exception e) {
            return -1;
        }
    }    

    /**
     * Similar to {@link JakartaServletFileUpload#parseRequest(RequestContext)} but will
     * catch and swallow FileSizeLimitExceededExceptions in order to return as
     * many usable items as possible.
     * 
     * @param fileUpload
     * @return List of {@link FileItem} excluding any that exceed max size.  
     * @throws FileUploadException
     */
    public List parseRequestCatchingFileSizeErrors(HttpServletRequest request, AbstractFileUpload fileUpload)
            throws FileUploadException
    {
        try
        {
            List items = new ArrayList();

            // Throws FileUploadSizeException if the request is longer than the max size
            // allowed (AbstractFileUpload.getMaxSize). If the request does not send proper
            // headers this check does nothing and we still have to check it again below.
            FileItemInputIterator iter = fileUpload
                    .getItemIterator(new JakartaServletRequestContext(request));

            FileItemFactory fac = fileUpload.getFileItemFactory();
            if (fac == null)
            {
                throw new NullPointerException(
                        "No FileItemFactory has been set.");
            }

            long maxFileSize = this.getMaxFileSize();
            long maxSize = this.getMaxSize();
            boolean checkMaxSize = false;

            if (maxFileSize == -1L)
            {
                //The max allowed file size should be approximate to the maxSize
                maxFileSize = maxSize;
            }
            if (maxSize != -1L)
            {
                checkMaxSize = true;
            }

            while (iter.hasNext())
            {
                final FileItemInput item = iter.next();
                // fileupload2 builds items through the factory's builder rather than
                // createItem(...); a null file name marks a plain form field.
                FileItem fileItem = (FileItem) fac.fileItemBuilder()
                        .setFieldName(item.getFieldName())
                        .setContentType(item.getContentType())
                        .setFileName(item.isFormField() ? null : item.getName())
                        .get();

                long allowedLimit = 0L;
                try
                {
                    if (maxFileSize != -1L || checkMaxSize)
                    {
                        if (checkMaxSize)
                        {
                            allowedLimit = maxSize > maxFileSize ? maxFileSize : maxSize;
                        }
                        else
                        {
                            //Just put the limit
                            allowedLimit = maxFileSize;
                        }

                        long contentLength = getContentLength(item.getHeaders());

                        //If we have a content length in the header we can use it
                        if (contentLength != -1L && contentLength > allowedLimit)
                        {
                            throw new FileUploadByteCountLimitException(
                                    "The field " + item.getFieldName()
                                            + " exceeds its maximum permitted "
                                            + " size of " + allowedLimit + " characters.",
                                    contentLength, allowedLimit,
                                    item.getFieldName(), item.getName());
                        }

                        //Otherwise we must limit the input as it arrives (NOTE: we cannot rely
                        //on commons upload to throw this exception as it will close the
                        //underlying stream
                        final InputStream itemInputStream = item.getInputStream();

                        InputStream limitedInputStream = new BoundedInputStream(
                                itemInputStream, allowedLimit)
                        {
                            // onMaxLength replaces 1.x's raiseError hook
                            protected void onMaxLength(long pSizeMax, long pCount)
                                    throws IOException
                            {
                                throw new FileUploadByteCountLimitException(
                                        "The field " + item.getFieldName()
                                                + " exceeds its maximum permitted "
                                                + " size of " + pSizeMax + " characters.",
                                        pCount, pSizeMax,
                                        item.getFieldName(), item.getName());
                            }
                        };

                        //Copy from the limited stream
                        long bytesCopied;
                        OutputStream fileItemOutput = fileItem.getOutputStream();
                        try
                        {
                            bytesCopied = IOUtils.copyLarge(limitedInputStream, fileItemOutput);
                        }
                        finally
                        {
                            IOUtils.closeQuietly(limitedInputStream);
                            IOUtils.closeQuietly(fileItemOutput);
                        }

                        // Decrement the bytesCopied values from maxSize, so the next file copied
                        // takes into account this value when allowedLimit var is calculated
                        maxSize -= bytesCopied;
                    }
                    else
                    {
                        //We can just copy the data
                        InputStream in = item.getInputStream();
                        OutputStream out = fileItem.getOutputStream();
                        try
                        {
                            IOUtils.copyLarge(in, out);
                        }
                        finally
                        {
                            IOUtils.closeQuietly(in);
                            IOUtils.closeQuietly(out);
                        }
                    }
                }
                catch (FileUploadByteCountLimitException se)
                {
                    // In fileupload2 FileUploadException extends IOException, so the
                    // FileUploadIOException tunnelling used by 1.x is no longer needed.
                    request.setAttribute(
                            "org.apache.myfaces.custom.fileupload.exception",
                            "fileSizeLimitExceeded");
                    String fieldName = fileItem.getFieldName();
                    request.setAttribute(
                            "org.apache.myfaces.custom.fileupload." + fieldName + ".maxSize",
                            Integer.valueOf((int) allowedLimit));
                }
                catch (IOException e)
                {
                    throw new FileUploadException("Processing of "
                            + AbstractFileUpload.MULTIPART_FORM_DATA
                            + " request failed. " + e.getMessage(), e);
                }
                if (fileItem instanceof FileItemHeadersProvider)
                {
                    final FileItemHeaders fih = item.getHeaders();
                    ((FileItemHeadersProvider) fileItem).setHeaders(fih);
                }
                if (fileItem != null)
                {
                    items.add(fileItem);
                }
            }
            return items;
        }
        catch (FileUploadException e)
        {
            throw e;
        }
        catch (IOException e)
        {
            throw new FileUploadException(e.getMessage(), e);
        }
    }
}
