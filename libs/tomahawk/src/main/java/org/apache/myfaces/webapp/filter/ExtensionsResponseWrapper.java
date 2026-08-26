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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import java.nio.charset.Charset;

/**
 * @author Sylvain Vieujot (latest modification by $Author: skitching $)
 * @version $Revision: 673833 $ $Date: 2008-07-03 16:58:05 -0500 (Thu, 03 Jul 2008) $
 */
public class ExtensionsResponseWrapper extends HttpServletResponseWrapper {
    private ByteArrayOutputStream stream = null;
    private PrintWriter printWriter = null;
    private String contentType;
    private HttpServletResponse delegate;
    
    private static final Log log = LogFactory.getLog(ExtensionsResponseWrapper.class);

    public ExtensionsResponseWrapper(HttpServletResponse response){
        super( response );
        this.delegate = response;
        stream = new ByteArrayOutputStream();
    }


    public byte[] getBytes() {
        return stream.toByteArray();
    }

    public String toString(){
        try{
            return stream.toString(getCharacterEncoding());
        }catch(UnsupportedEncodingException e){
            // an attempt to set an invalid character encoding would have caused this exception before
            throw new RuntimeException("Response accepted invalid character encoding " + getCharacterEncoding());
        }
    }

    /** This method is used by Tomcat.
     */
    public PrintWriter getWriter(){
        if( printWriter == null ){
            OutputStreamWriter streamWriter = new OutputStreamWriter(stream, Charset.forName(getCharacterEncoding()));
            printWriter = new PrintWriter(streamWriter, true);
            //printWriter = new PrintWriter(stream, true); // autoFlush is true
        }
        return printWriter;
    }

    /** This method is used by Jetty.
    */
    public ServletOutputStream getOutputStream(){
        return new MyServletOutputStream( stream );
    }

    public InputSource getInputSource(){
        ByteArrayInputStream bais = new ByteArrayInputStream( stream.toByteArray() );
        return new InputSource( bais );
    }

     /**
     *  Prevent content-length being set as the page might be modified.
     */
    public void setContentLength(int contentLength) {
        // noop
    }

    public void setContentType(String contentType) {
        super.setContentType(contentType);
        this.contentType = contentType;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void flushBuffer() throws IOException{
        stream.flush();
    }

    public void finishResponse() {
        try {
            if (printWriter != null) {
                printWriter.close();
            } else {
                if (stream != null) {
                    stream.close();
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }
    }

    public HttpServletResponse getDelegate() {
        return delegate;
    }

    /** Used in the <code>getOutputStream()</code> method.
     */
    private class MyServletOutputStream extends ServletOutputStream {
        private ByteArrayOutputStream outputStream;

        public MyServletOutputStream(ByteArrayOutputStream outputStream){
            this.outputStream = outputStream;
        }

        public void write(int b){
            outputStream.write( b );
        }

        public void write(byte[] bytes) throws IOException{
            outputStream.write( bytes );
        }

        public void write(byte[] bytes, int off, int len){
            outputStream.write(bytes, off, len);
        }
    
        // Servlet 3.1 async I/O; Tomahawk predates it and writes synchronously.
        public boolean isReady()
        {
            return true;
        }

        public void setWriteListener(jakarta.servlet.WriteListener writeListener)
        {
        }
}
}
