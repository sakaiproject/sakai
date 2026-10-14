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
package org.apache.myfaces.webapp.filter;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.fileupload2.core.FileItem;
import java.nio.charset.Charset;
import org.apache.commons.fileupload2.core.AbstractFileUpload;
import org.apache.commons.fileupload2.core.FileUploadByteCountLimitException;
import org.apache.commons.fileupload2.core.FileUploadSizeException;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.webapp.filter.servlet.ServletChacheFileSizeErrorsFileUpload;

/**
 * @since 1.1.7
 * @author Sylvain Vieujot (latest modification by $Author: lu4242 $)
 * @version $Revision: 1379874 $ $Date: 2012-09-01 17:13:08 -0500 (Sat, 01 Sep 2012) $
 */
public class MultipartRequestWrapper
        extends HttpServletRequestWrapper
{
    private static Log log = LogFactory.getLog(MultipartRequestWrapper.class);
    public static final String UPLOADED_FILES_ATTRIBUTE = "org.apache.myfaces.uploadedFiles";
    public static final String WWW_FORM_URLENCODED_TYPE = "application/x-www-form-urlencoded";

    HttpServletRequest request = null;
    HashMap parametersMap = null;
    JakartaServletFileUpload fileUpload = null;
    HashMap fileItems = null;
    long maxFileSize;
    long maxSize;
    int thresholdSize;
    String repositoryPath;
    boolean cacheFileSizeErrors;

    public MultipartRequestWrapper(HttpServletRequest request,
                                   int maxFileSize, int thresholdSize,
                                   String repositoryPath){
        super( request );
        this.request = request;
        this.maxFileSize = maxFileSize;
        this.thresholdSize = thresholdSize;
        this.repositoryPath = repositoryPath;
        //Default values
        this.maxSize = maxFileSize;
        this.cacheFileSizeErrors = false;
    }
    
    public MultipartRequestWrapper(HttpServletRequest request,
            int maxFileSize, int thresholdSize,
            String repositoryPath, int maxRequestSize, boolean cacheFileSizeErrors){
        super( request );
        this.request = request;
        this.maxFileSize = maxFileSize;
        this.maxSize = maxRequestSize;
        this.thresholdSize = thresholdSize;
        this.repositoryPath = repositoryPath;
        this.cacheFileSizeErrors = cacheFileSizeErrors;
    }
    
    public MultipartRequestWrapper(HttpServletRequest request,
                                   long maxFileSize, int thresholdSize,
                                   String repositoryPath){
        super( request );
        this.request = request;
        this.maxFileSize = maxFileSize;
        this.thresholdSize = thresholdSize;
        this.repositoryPath = repositoryPath;
        //Default values
        this.maxSize = maxFileSize;
        this.cacheFileSizeErrors = false;
    }
    
    public MultipartRequestWrapper(HttpServletRequest request,
            long maxFileSize, int thresholdSize,
            String repositoryPath, long maxRequestSize, boolean cacheFileSizeErrors){
        super( request );
        this.request = request;
        this.maxFileSize = maxFileSize;
        this.maxSize = maxRequestSize;
        this.thresholdSize = thresholdSize;
        this.repositoryPath = repositoryPath;
        this.cacheFileSizeErrors = cacheFileSizeErrors;
    }

    private void parseRequest() {
        if (cacheFileSizeErrors)
        {
            fileUpload = new ServletChacheFileSizeErrorsFileUpload();
        }
        else
        {
            fileUpload = new JakartaServletFileUpload();
        }
        //fileUpload.setFileItemFactory(new DefaultFileItemFactory()); //USE DiskFileItemFactory
        fileUpload.setMaxSize(maxSize);
        fileUpload.setMaxFileSize(maxFileSize);

        //fileUpload.setSizeThreshold(thresholdSize); //Pass thresholdSize as param for DiskFileItemFactory

        //if(repositoryPath != null && repositoryPath.trim().length()>0)
        //    fileUpload.setRepositoryPath(repositoryPath);
        
        if(repositoryPath != null && repositoryPath.trim().length()>0)
        {
            fileUpload.setFileItemFactory(DiskFileItemFactory.builder()
                    .setThreshold(thresholdSize)
                    .setFile(new File(repositoryPath))
                    .get());
        }
        else
        {
            fileUpload.setFileItemFactory(DiskFileItemFactory.builder()
                    .setThreshold(thresholdSize)
                    .setFile(new File(System.getProperty("java.io.tmpdir")))
                    .get());
        }

        String charset = request.getCharacterEncoding();
        if (charset != null)
        {
            fileUpload.setHeaderCharset(Charset.forName(charset));
        }


        List requestParameters = null;
        
        try
        {
            if (cacheFileSizeErrors)
            {
                requestParameters = ((ServletChacheFileSizeErrorsFileUpload) fileUpload).
                    parseRequestCatchingFileSizeErrors(request, fileUpload);
            }
            else
            {
                requestParameters = fileUpload.parseRequest(request);
            }
        }
        catch (FileUploadByteCountLimitException e)
        {
            // Since commons-fileupload does not allow to continue processing files
            // if this exception is thrown, we can't do anything else.
            // So, the current request is rejected and we can't restore state, so 
            // this request is dealt like a new request, but note that caching the params
            // below it is possible to detect if the current request has been aborted
            // or not.
            // Note that if cacheFileSizeErrors is true, this is not thrown, so the request
            // is not aborted unless other different error occur.
            request.setAttribute(
                    "org.apache.myfaces.custom.fileupload.exception","fileSizeLimitExceeded");
            request.setAttribute("org.apache.myfaces.custom.fileupload.maxSize",
                    new Integer((int)maxFileSize));
            
            if (log.isWarnEnabled())
                log.warn("FileSizeLimitExceededException while uploading file.", e);
            
            requestParameters = Collections.EMPTY_LIST;
        }
        catch (FileUploadSizeException e)
        {
            // This exception is thrown when the max request size has been reached.
            // In this case, the current request is rejected. The current 
            // request is dealt like a new request, but note that caching the params below
            // params it is possible to detect if the current request has been aborted
            // or not.
            request.setAttribute(
                "org.apache.myfaces.custom.fileupload.exception","sizeLimitExceeded");
            request.setAttribute("org.apache.myfaces.custom.fileupload.maxSize",
                new Integer((int)maxSize));
            
            if (log.isWarnEnabled())
                log.warn("SizeLimitExceededException while uploading file.", e);
            
            requestParameters = Collections.EMPTY_LIST;
        }
        catch(FileUploadException fue)
        {
            if (log.isErrorEnabled())
                log.error("Exception while uploading file.", fue);
            
            requestParameters = Collections.EMPTY_LIST;
        }        

        parametersMap = new HashMap( requestParameters.size() );
        fileItems = new HashMap();

        for (Iterator iter = requestParameters.iterator(); iter.hasNext(); ){
            FileItem fileItem = (FileItem) iter.next();

            if (fileItem.isFormField()) {
                String name = fileItem.getFieldName();

                // The following code avoids commons-fileupload charset problem.
                // After fixing commons-fileupload, this code should be
                //
                //     String value = fileItem.getString();
                //
                String value = null;
                // fileupload2 declares IOException on FileItem.get()/getString()
                try {
                    if ( charset == null) {
                        value = fileItem.getString();
                    } else {
                        try {
                            value = new String(fileItem.get(), charset);
                        } catch (UnsupportedEncodingException e){
                            value = fileItem.getString();
                        }
                    }
                } catch (IOException e) {
                    if (log.isWarnEnabled())
                        log.warn("Could not read value of field " + name, e);
                }

                addTextParameter(name, value);
            } else { // fileItem is a File
                if (fileItem.getName() != null) {
                    fileItems.put(fileItem.getFieldName(), fileItem);
                }
            }
        }

        //Add the query string paramters
        /* This code does only works if is true the assumption that query params
         * are the same from start of the request. But it is possible
         * use jsp:include and jsp:param to set query params after
         * the request is parsed.
        for (Iterator it = request.getParameterMap().entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry entry = (Map.Entry)it.next();

            Object value = entry.getValue();

            if(value instanceof String[])
            {
                String[] valuesArray = (String[])entry.getValue();
                for (int i = 0; i < valuesArray.length; i++)
                {
                    addTextParameter((String)entry.getKey(), valuesArray[i]);
                }
            }
            else if(value instanceof String)
            {
                String strValue = (String)entry.getValue();
                addTextParameter((String)entry.getKey(), strValue);
            }
            else if(value != null)
                throw new IllegalStateException("value of type : "+value.getClass()+" for key : "+
                        entry.getKey()+" cannot be handled.");

        }
        */
    }

    private void addTextParameter(String name, String value){
        if( ! parametersMap.containsKey( name ) ){
            String[] valuesArray = {value};
            parametersMap.put(name, valuesArray);
        }else{
            String[] storedValues = (String[])parametersMap.get( name );
            int lengthSrc = storedValues.length;
            String[] valuesArray = new String[lengthSrc+1];
            System.arraycopy(storedValues, 0, valuesArray, 0, lengthSrc);
            valuesArray[lengthSrc] = value;
            parametersMap.put(name, valuesArray);
        }
    }

    public Enumeration getParameterNames() {
        if( parametersMap == null ) parseRequest();

        //return Collections.enumeration( parametersMap.keySet() );
        HashSet mergedNames = new HashSet(request.getParameterMap().keySet());
        mergedNames.addAll(parametersMap.keySet());
        
        return Collections.enumeration( mergedNames );
    }

    public String getParameter(String name) {
        if( parametersMap == null ) parseRequest();

        String[] values = (String[])parametersMap.get( name );
        //if( values == null )
        //    return null;
        //return values[0];
        if (values != null)
        {
            return values[0];
        }
        else if (parametersMap.containsKey(name))
        {
            return null;
        }
        else
        {
            return request.getParameter(name);
        }
    }

    public String[] getParameterValues(String name) {
        if( parametersMap == null ) parseRequest();

        //return (String[])parametersMap.get( name );
        String[] values = (String[])parametersMap.get( name );
        if (values != null)
        {
            return values;
        }
        else if (parametersMap.containsKey(name))
        {
            return null;
        }
        else
        {
            return request.getParameterValues(name);
        }
    }

    public Map getParameterMap() {
        if( parametersMap == null ) parseRequest();

        //return parametersMap;
        HashMap mergedMap = new HashMap(request.getParameterMap());
        mergedMap.putAll(parametersMap);
        return mergedMap;
    }

    // Hook for the x:inputFileUpload tag.
    public FileItem getFileItem(String fieldName) {
        if( fileItems == null ) parseRequest();

        return (FileItem) fileItems.get( fieldName );
    }

    /**
     * Not used internally by MyFaces, but provides a way to handle the uploaded files
     * out of MyFaces.
     */
    public Map getFileItems(){
        if( fileItems == null ) parseRequest();
        return fileItems;
    }


    public Object getAttribute(String string) {
        if (string.equals(UPLOADED_FILES_ATTRIBUTE)) {
            return getFileItems();
        }
        return super.getAttribute(string);
    }
    
    public String getContentType()
    {
      return WWW_FORM_URLENCODED_TYPE;
    }     
}