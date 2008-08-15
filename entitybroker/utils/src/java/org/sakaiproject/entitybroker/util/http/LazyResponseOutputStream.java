/**
 * $Id$
 * $URL$
 * LazyOutputStream.java - entity-broker - Aug 15, 2008 7:52:58 PM - azeckoski
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

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

/**
 * This will get the response to get an outputstream from but will not actually get it from the response
 * until someone attempts to use the outputstream<br/>
 * This is horrible but required because of the way Sakai tool forwarding works<br/>
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class LazyResponseOutputStream extends OutputStream {

    /**
     * the output stream that will be used once it is first called
     */
    private OutputStream stream = null;
    private HttpServletResponse response = null;
    private OutputStream getStream() {
        if (stream == null) {
            try {
                stream = response.getOutputStream();
            } catch (IOException e) {
                throw new RuntimeException("Failed getting response output stream from HttpServletResponse", e);
            }
        }
        return stream;
    }

    /**
     * @param response the response that will be used to get the outputstream,
     * this will die if someone has tried to get the writer out of this
     */
    public LazyResponseOutputStream(HttpServletResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("response cannot be null");
        }
        this.response = response;
    }

    @Override
    public void write(byte[] b) throws IOException {
        getStream().write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        getStream().write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        getStream().write(b);
    }

    @Override
    public void close() throws IOException {
        getStream().close();
    }

    @Override
    public void flush() throws IOException {
        getStream().flush();
    }
}
