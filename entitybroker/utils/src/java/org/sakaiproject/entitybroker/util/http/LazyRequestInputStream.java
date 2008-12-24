/**
 * $Id$
 * $URL$
 * LazyRequestInputStream.java - entity-broker - Dec 24, 2008 12:04:27 PM - azeckoski
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

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;


/**
 * This provides lazy access to the {@link ServletInputStream} data in a request,
 * this avoids issues with attempting to read data which was is later requested from the reader since
 * it will not actually do any reads using the {@link ServletInputStream} but still can pretend to
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class LazyRequestInputStream extends ServletInputStream {

    public ServletInputStream stream;
    public HttpServletRequest request;
    private ServletInputStream getStream() {
        if (stream == null) {
            try {
                // will try to get the data one way or another
                try {
                    // try to use the inputStream first always
                    stream = request.getInputStream();
                } catch (IllegalStateException e) {
                    // must use the reader then
                    BufferedReader r = request.getReader();
                    stream = new EntityServletInputStream(r);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed getting request inputstream from HttpServletRequest: " + e.getMessage(), e);
            }
        }
        return stream;
    }

    /**
     * Construct a new lazy inputstream from the given request
     * @param request any http request, cannot be null
     */
    public LazyRequestInputStream(HttpServletRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }
        this.request = request;
    }

    @Override
    public int readLine(byte[] b, int off, int len) throws IOException {
        return getStream().readLine(b, off, len);
    }
    public int available() throws IOException {
        return getStream().available();
    }
    public void close() throws IOException {
        getStream().close();
    }
    public void mark(int readlimit) {
        getStream().mark(readlimit);
    }
    public boolean markSupported() {
        return getStream().markSupported();
    }
    public int read() throws IOException {
        return getStream().read();
    }
    public int read(byte[] b) throws IOException {
        return getStream().read(b);
    }
    public int read(byte[] b, int off, int len) throws IOException {
        return getStream().read(b, off, len);
    }
    public void reset() throws IOException {
        getStream().reset();
    }
    public long skip(long n) throws IOException {
        return getStream().skip(n);
    }
    public boolean equals(Object obj) {
        return getStream().equals(obj);
    }
    public int hashCode() {
        return getStream().hashCode();
    }
    public String toString() {
        return getStream().toString();
    }
}
