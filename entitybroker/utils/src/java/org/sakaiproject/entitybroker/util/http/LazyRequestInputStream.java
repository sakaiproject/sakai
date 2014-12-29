/**
 * $Id$
 * $URL$
 * LazyRequestInputStream.java - entity-broker - Dec 24, 2008 12:04:27 PM - azeckoski
 ***********************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

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
    @Override
    public boolean equals(Object obj) {
        if (stream == null) {
            return super.equals(obj);
        } else {
            return getStream().equals(obj);
        }
    }
    @Override
    public int hashCode() {
        if (stream == null) {
            return super.hashCode();
        } else {
            return getStream().hashCode();
        }
    }
    @Override
    public String toString() {
        if (stream == null) {
            return super.toString() + ":LazyStreamNotOpenYet";
        } else {
            return getStream().toString();
        }
    }
}
