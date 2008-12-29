/**
 * $Id$
 * $URL$
 * EntityServletOutputStream.java - entity-broker - Dec 29, 2008 1:49:15 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.util.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.servlet.ServletOutputStream;


/**
 * This allows us to produce and control {@link ServletOutputStream}s,
 * normally there is not a default implementation available for these for some crazed reason
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityServletOutputStream extends ServletOutputStream {

    private BufferedOutputStream out;

    /**
     * Create a SOS with a default BOS as the storage mechanism for it
     */
    public EntityServletOutputStream() {
        this.out = new BufferedOutputStream( new ByteArrayOutputStream(1024) );
    }

    /**
     * Create the SOS from an existing outputstream
     * @param outputStream the OS to create the SOS from, can be buffered or not
     */
    public EntityServletOutputStream(OutputStream outputStream) {
        if (outputStream == null) {
            throw new IllegalArgumentException("outputStream cannot be null");
        }
        if (BufferedInputStream.class.isAssignableFrom(outputStream.getClass())) {
            this.out = (BufferedOutputStream) outputStream;
        } else {
            this.out = new BufferedOutputStream(outputStream);
        }
    }

    /**
     * Creates the SOS from an existing writer
     * @param writer any writer
     */
    public EntityServletOutputStream(Writer writer) {
        if (writer == null) {
            throw new IllegalArgumentException("writer cannot be null");
        }
        BufferedWriter bw = null;
        if (BufferedWriter.class.isAssignableFrom(writer.getClass())) {
            bw = (BufferedWriter) writer;
        } else {
            bw = new BufferedWriter(writer);
        }
        WriterOutputStream wos = new WriterOutputStream(bw, "UTF-8");
        this.out = new BufferedOutputStream( wos );
    }

    public void close() throws IOException {
        out.close();
    }

    public boolean equals(Object obj) {
        return out.equals(obj);
    }

    public void flush() throws IOException {
        out.flush();
    }

    public int hashCode() {
        return out.hashCode();
    }

    public String toString() {
        return out.toString();
    }

    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    public void write(byte[] b) throws IOException {
        out.write(b);
    }

    public void write(int b) throws IOException {
        out.write(b);
    }

    /** Wrap a Writer as an OutputStream.
     * When all you have is a Writer and only an OutputStream will do.
     * Try not to use this as it indicates that your design is a dogs
     * breakfast (JSP made me write it).
     * @version 1.0 Tue Feb 12 2002
     * @author Greg Wilkins (gregw)
     */
    public static class WriterOutputStream extends OutputStream
    {
        protected Writer _writer;
        protected String _encoding;
        private byte[] _buf=new byte[1];
        
        /* ------------------------------------------------------------ */
        public WriterOutputStream(Writer writer, String encoding)
        {
            _writer=writer;
            _encoding=encoding;
        }
        
        /* ------------------------------------------------------------ */
        public WriterOutputStream(Writer writer)
        {
            _writer=writer;
        }

        /* ------------------------------------------------------------ */
        public void close()
            throws IOException
        {
            _writer.close();
            _writer=null;
            _encoding=null;
        }
        
        /* ------------------------------------------------------------ */
        public void flush()
            throws IOException
        {
            _writer.flush();
        }
        
        /* ------------------------------------------------------------ */
        public void write(byte[] b) 
            throws IOException
        {
            if (_encoding==null)
                _writer.write(new String(b));
            else
                _writer.write(new String(b,_encoding));
        }
        
        /* ------------------------------------------------------------ */
        public void write(byte[] b, int off, int len)
            throws IOException
        {
            if (_encoding==null)
                _writer.write(new String(b,off,len));
            else
                _writer.write(new String(b,off,len,_encoding));
        }
        
        /* ------------------------------------------------------------ */
        public synchronized void write(int b)
            throws IOException
        {
            _buf[0]=(byte)b;
            write(_buf);
        }
    }

}
