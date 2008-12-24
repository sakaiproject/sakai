/**
 * $Id$
 * $URL$
 * BufferedServletInputStream.java - entity-broker - Dec 24, 2008 11:14:58 AM - azeckoski
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
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.servlet.ServletInputStream;

/**
 * This is a class which allows us to produce and control {@link ServletInputStream}s,
 * normally there is no default implementation available for these for some stupid reason
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityServletInputStream extends ServletInputStream {

    private BufferedInputStream in;

    /**
     * Creates the SIS from an existing inputstream
     * @param in the IS to create the SIS from, can be buffered or not
     */
    public EntityServletInputStream(InputStream in) {
        if (in == null) {
            throw new IllegalArgumentException("inputstream cannot be null");
        }
        if (BufferedInputStream.class.isAssignableFrom(in.getClass())) {
            this.in = (BufferedInputStream) in;
        } else {
            this.in = new BufferedInputStream(in);
        }
    }

    /**
     * Creates the SIS from an existing string
     * @param str any string
     */
    public EntityServletInputStream(String str) {
        if (in == null) {
            throw new IllegalArgumentException("str cannot be null");
        }
        this.in = new BufferedInputStream( new ByteArrayInputStream( str.getBytes() ) );
    }

    /**
     * Creates the SIS from any existing reader,
     * note that this is not very efficient as it has to load the full reader into memory
     * @param reader any reader object, buffered or not
     */
    public EntityServletInputStream(Reader reader) {
        if (reader == null) {
            throw new IllegalArgumentException("reader cannot be null");
        }
        BufferedReader br = null;
        if (BufferedReader.class.isAssignableFrom(reader.getClass())) {
            br = (BufferedReader) reader;
        } else {
            br = new BufferedReader(reader);
        }
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read data from reader: " + e.getMessage(), e);
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String str = sb.toString();
        this.in = new BufferedInputStream( new ByteArrayInputStream( str.getBytes() ) );
    }


    @Override
    public int read() throws IOException {
        return in.read();
    }

    public int available() throws IOException {
        return in.available();
    }

    public void close() throws IOException {
        in.close();
    }

    public boolean equals(Object obj) {
        return in.equals(obj);
    }

    public int hashCode() {
        return in.hashCode();
    }

    public void mark(int readlimit) {
        in.mark(readlimit);
    }

    public boolean markSupported() {
        return in.markSupported();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    public int read(byte[] b) throws IOException {
        return in.read(b);
    }

    public void reset() throws IOException {
        in.reset();
    }

    public long skip(long n) throws IOException {
        return in.skip(n);
    }

    public String toString() {
        return in.toString();
    }

}
