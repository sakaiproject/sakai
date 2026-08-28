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
package org.apache.myfaces.tomahawk.application.jsp;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * @since 1.1.7
 * @author Bruno Aranda (latest modification by $Author: lu4242 $)
 * @version $Revision: 691871 $ $Date: 2008-09-03 23:32:08 -0500 (Wed, 03 Sep 2008) $
 */
public class ViewResponseWrapper extends HttpServletResponseWrapper
{
    private PrintWriter _writer;
    private CharArrayWriter _charArrayWriter;
    private int _status = HttpServletResponse.SC_OK;
    private WrappedServletOutputStream _byteArrayWriter;

    public ViewResponseWrapper(HttpServletResponse httpServletResponse)
    {
        super(httpServletResponse);
    }

    @Override
    public void sendError(int status) throws IOException
    {
        super.sendError(status);
        _status = status;
    }

    @Override
    public void sendError(int status, String errorMessage) throws IOException
    {
        super.sendError(status, errorMessage);
        _status = status;
    }

    @Override
    public void setStatus(int status)
    {
        super.setStatus(status);
        _status = status;
    }

    // setStatus(int, String) was removed in Servlet 6.0; not an override any more.
    public void setStatus(int status, String errorMessage)
    {
        super.setStatus(status);
        _status = status;
    }

    public int getStatus()
    {
        return _status;
    }

    public void flushToWrappedResponse() throws IOException
    {
        if (_charArrayWriter != null)
        {
            _charArrayWriter.writeTo(getResponse().getWriter());
            _charArrayWriter.reset();
            _writer.flush();
        }
        else if (_byteArrayWriter != null)
        {
            getResponse().getOutputStream().write(_byteArrayWriter.toByteArray());
            _byteArrayWriter.reset();
            _byteArrayWriter.flush();
        }
    }

    public void flushToWriter(Writer writer,String encoding) throws IOException
    {
        if (_charArrayWriter != null)
        {
            _charArrayWriter.writeTo(writer);
            _charArrayWriter.reset();
            _writer.flush();
        }
        else if (_byteArrayWriter != null)
        {
            _byteArrayWriter.writeTo(writer,encoding);
            _byteArrayWriter.reset();
            _byteArrayWriter.flush();
        }
        writer.flush();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException
    {
        if (_charArrayWriter != null) throw new IllegalStateException();
        if (_byteArrayWriter == null)
        {
            _byteArrayWriter = new WrappedServletOutputStream();
        }
        return _byteArrayWriter;
    }

    @Override
    public PrintWriter getWriter() throws IOException
    {
        if (_byteArrayWriter != null) throw new IllegalStateException();
        if (_writer == null)
        {
            _charArrayWriter = new CharArrayWriter(4096);
            _writer = new PrintWriter(_charArrayWriter);
        }
        return _writer;
    }

    @Override
    public void reset()
    {
        if (_charArrayWriter != null)
        {
            _charArrayWriter.reset();
        }
    }

    @Override
    public String toString()
    {
        if (_charArrayWriter != null)
        {
            return _charArrayWriter.toString();
        }
        return null;
    }

    static class WrappedServletOutputStream extends ServletOutputStream
    {
        private WrappedByteArrayOutputStream _byteArrayOutputStream;


        public WrappedServletOutputStream()
        {
            _byteArrayOutputStream = new WrappedByteArrayOutputStream(1024);
        }

        public void write(int i) throws IOException
        {
            _byteArrayOutputStream.write(i);
        }

        public byte[] toByteArray()
        {
            return _byteArrayOutputStream.toByteArray();
        }
        
        /**
         * Write the data of this stream to the writer, using
         * the charset encoding supplied or if null the default charset.
         * 
         * @param out
         * @param encoding
         * @throws IOException
         */
        private void writeTo(Writer out, String encoding) throws IOException{
            //Get the charset based on the encoding or return the default if 
            //encoding == null
            Charset charset = (encoding == null) ? 
                    Charset.defaultCharset() : Charset.forName(encoding);
            CharsetDecoder decoder = charset.newDecoder();
            CharBuffer decodedBuffer = decoder.decode(
                    ByteBuffer.wrap(_byteArrayOutputStream.getInnerArray(),
                            0,_byteArrayOutputStream.getInnerCount()));
            if (decodedBuffer.hasArray()){
                out.write(decodedBuffer.array());
            }
        }

        public void reset()
        {
            _byteArrayOutputStream.reset();
        }
        
        /**
         * This Wrapper is used to provide additional methods to 
         * get the buf and count variables, to use it to decode
         * in WrappedServletOutputStream.writeTo and avoid buffer
         * duplication.
         */
        static class WrappedByteArrayOutputStream extends ByteArrayOutputStream{
            
            public WrappedByteArrayOutputStream(){
                super();
            }
            
            public WrappedByteArrayOutputStream(int size){
                super(size);                
            }
            
            private byte[] getInnerArray(){
                return buf; 
            }
            
            private int getInnerCount(){
                return count;
            }
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
