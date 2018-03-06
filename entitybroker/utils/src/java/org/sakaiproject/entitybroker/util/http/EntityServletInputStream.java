/**
 * $Id$
 * $URL$
 * BufferedServletInputStream.java - entity-broker - Dec 24, 2008 11:14:58 AM - azeckoski
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.servlet.ServletInputStream;

import lombok.extern.slf4j.Slf4j;

/**
 * This is a class which allows us to produce and control {@link ServletInputStream}s,
 * normally there is no default implementation available for these for some stupid reason
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Slf4j
public class EntityServletInputStream extends ServletInputStream {

    private BufferedInputStream in;

    /**
     * Creates the SIS from an existing inputstream
     * @param inputStream the IS to create the SIS from, can be buffered or not
     */
    public EntityServletInputStream(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputstream cannot be null");
        }
        if (BufferedInputStream.class.isAssignableFrom(inputStream.getClass())) {
            this.in = (BufferedInputStream) inputStream;
        } else {
            this.in = new BufferedInputStream(inputStream);
        }
    }

    /**
     * Creates the SIS from an existing string
     * @param str any string
     */
    public EntityServletInputStream(String str) {
        if (str == null) {
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
                log.error(e.getMessage(), e);
            }
        }
        String str = sb.toString();
        this.in = new BufferedInputStream( new ByteArrayInputStream( str.getBytes() ) );
    }


    @Override
    public int read() throws IOException {
        return in.read();
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }

    public void close() throws IOException {
        in.close();
    }

    @Override
    public boolean equals(Object obj) {
        return in.equals(obj);
    }

    @Override
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

    @Override
    public String toString() {
        return in.toString();
    }

}
