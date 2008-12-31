/**
 * $Id$
 * $URL$
 * EntityHttpServletResponse.java - entity-broker - Dec 24, 2008 4:01:15 PM - azeckoski
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.azeckoski.reflectutils.map.ArrayOrderedMap;


/**
 * This is here to allow us to receive response data back which will not mess up an existing response
 * object and to allow for mocking of responses,
 * based on and built from the example in spring framework
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@SuppressWarnings("unchecked")
public class EntityHttpServletResponse implements HttpServletResponse {

    /**
     * Create a default response that is valid for testing
     */
    public EntityHttpServletResponse() {
        this.setLocale( Locale.getDefault() );
        this.setStatus( HttpServletResponse.SC_OK );
    }

    /**
     * Create a servlet response using the various values and codes stored in the given one,
     * makes copies mostly
     * @param response any valid response, cannot be null
     */
    public EntityHttpServletResponse(HttpServletResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("response to copy cannot be null");
        }
        this.setBufferSize( response.getBufferSize() );
        if (response.getContentType() != null) {
            this.setContentType( response.getContentType() );
        }
        this.setLocale( response.getLocale() );
        this.setStatus( HttpServletResponse.SC_OK );
    }

    public static final int DEFAULT_SERVER_PORT = 80;
    private static final String CHARSET_PREFIX = "charset=";

    private boolean outputStreamAccessAllowed = true;
    private boolean writerAccessAllowed = true;
    private String characterEncoding = "UTF-8";
    private final ByteArrayOutputStream content = new ByteArrayOutputStream();
    private final ServletOutputStream outputStream = new EntityServletOutputStream(content);
    private PrintWriter writer;
    private int contentLength = 0;
    private String contentType;
    private int bufferSize = 4096;
    private boolean committed;
    private Locale locale = Locale.getDefault();
    ConcurrentHashMap<String, Vector<String>> headers = new ConcurrentHashMap<String, Vector<String>>();
    Vector<Cookie> cookies = new Vector<Cookie>();
    private int status = HttpServletResponse.SC_OK;
    private String errorMessage;
    private String redirectedUrl;
    private String forwardedUrl;
    private String includedUrl;

    
    // methods for testing only

    public void setForwardedUrl(String forwardedUrl) {
        this.forwardedUrl = forwardedUrl;
    }

    public String getForwardedUrl() {
        return this.forwardedUrl;
    }

    public void setIncludedUrl(String includedUrl) {
        this.includedUrl = includedUrl;
    }

    public String getIncludedUrl() {
        return this.includedUrl;
    }

    // Other methods

    /**
     * Set whether {@link #getOutputStream()} access is allowed.
     * <p>Default is <code>true</code>.
     */
    public void setOutputStreamAccessAllowed(boolean outputStreamAccessAllowed) {
        this.outputStreamAccessAllowed = outputStreamAccessAllowed;
    }

    /**
     * Return whether {@link #getOutputStream()} access is allowed.
     */
    public boolean isOutputStreamAccessAllowed() {
        return this.outputStreamAccessAllowed;
    }

    /**
     * Set whether {@link #getWriter()} access is allowed.
     * <p>Default is <code>true</code>.
     */
    public void setWriterAccessAllowed(boolean writerAccessAllowed) {
        this.writerAccessAllowed = writerAccessAllowed;
    }

    /**
     * Return whether {@link #getOutputStream()} access is allowed.
     */
    public boolean isWriterAccessAllowed() {
        return this.writerAccessAllowed;
    }

    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponse#getOutputStream()
     */
    public ServletOutputStream getOutputStream() {
        if (!this.outputStreamAccessAllowed) {
            throw new IllegalStateException("OutputStream access not allowed");
        }
        return this.outputStream;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponse#getWriter()
     */
    public PrintWriter getWriter() throws UnsupportedEncodingException {
        if (!this.writerAccessAllowed) {
            throw new IllegalStateException("Writer access not allowed");
        }
        if (this.writer == null) {
            Writer targetWriter = (this.characterEncoding != null ?
                    new OutputStreamWriter(this.content, this.characterEncoding) : new OutputStreamWriter(this.content));
            this.writer = new PrintWriter(targetWriter);
        }
        return this.writer;
    }

    /**
     * @return the content as a byte array
     */
    public byte[] getContentAsByteArray() {
        flushBuffer();
        return this.content.toByteArray();
    }

    /**
     * @return a string representing the content of this response
     */
    public String getContentAsString() {
        flushBuffer();
        try {
            return (this.characterEncoding != null) ?
                    this.content.toString(this.characterEncoding) : this.content.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failure during encoding of the string in this response: " + this.characterEncoding + ":" + e.getMessage(), e);
        }
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public int getContentLength() {
        return this.contentLength;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
        if (contentType != null) {
            int charsetIndex = contentType.toLowerCase().indexOf(CHARSET_PREFIX);
            if (charsetIndex != -1) {
                String encoding = contentType.substring(charsetIndex + CHARSET_PREFIX.length());
                setCharacterEncoding(encoding);
            }
        }
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public void flushBuffer() {
        setCommitted(true);
    }

    public void resetBuffer() {
        if (isCommitted()) {
            throw new IllegalStateException("Cannot reset buffer - response is already committed");
        }
        this.content.reset();
    }

    public void setCommitted(boolean committed) {
        this.committed = committed;
    }

    public boolean isCommitted() {
        return this.committed;
    }

    public void reset() {
        resetBuffer();
        this.characterEncoding = null;
        this.contentLength = 0;
        this.contentType = null;
        this.locale = null;
        this.cookies.clear();
        this.headers.clear();
        this.status = HttpServletResponse.SC_OK;
        this.errorMessage = null;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return this.locale;
    }


    //---------------------------------------------------------------------
    // HttpServletResponse interface
    //---------------------------------------------------------------------

    public void addCookie(Cookie cookie) {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie cannot be null");
        }
        this.cookies.add(cookie);
    }

    public Cookie[] getCookies() {
        return (Cookie[]) this.cookies.toArray(new Cookie[this.cookies.size()]);
    }

    public Cookie getCookie(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Cookie name cannot be null");
        }
        for (Iterator<Cookie> it = this.cookies.iterator(); it.hasNext();) {
            Cookie cookie = (Cookie) it.next();
            if (name.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }

    /**
     * The default implementation returns the given URL String as-is.
     * <p>Can be overridden in subclasses, appending a session id or the like.
     */
    public String encodeURL(String url) {
        return url;
    }

    /**
     * The default implementation delegates to {@link #encodeURL},
     * returning the given URL String as-is.
     * <p>Can be overridden in subclasses, appending a session id or the like
     * in a redirect-specific fashion. For general URL encoding rules,
     * override the common {@link #encodeURL} method instead, appyling
     * to redirect URLs as well as to general URLs.
     */
    public String encodeRedirectURL(String url) {
        return encodeURL(url);
    }

    public String encodeUrl(String url) {
        return encodeURL(url);
    }

    public String encodeRedirectUrl(String url) {
        return encodeRedirectURL(url);
    }

    public void sendError(int status, String errorMessage) throws IOException {
        if (isCommitted()) {
            throw new IllegalStateException("Cannot set error status - response is already committed");
        }
        this.status = status;
        this.errorMessage = errorMessage;
        setCommitted(true);
    }

    public void sendError(int status) throws IOException {
        if (isCommitted()) {
            throw new IllegalStateException("Cannot set error status - response is already committed");
        }
        this.status = status;
        setCommitted(true);
    }

    public void sendRedirect(String url) throws IOException {
        if (isCommitted()) {
            throw new IllegalStateException("Cannot send redirect - response is already committed");
        }
        if (url == null) {
            throw new IllegalArgumentException("url cannot be null");
        }
        this.redirectedUrl = url;
        setCommitted(true);
    }

    public String getRedirectedUrl() {
        return this.redirectedUrl;
    }

    public void setDateHeader(String name, long value) {
        setHeaderValue(name, value+"");
    }

    public void addDateHeader(String name, long value) {
        addHeaderValue(name, value+"");
    }

    public void setHeader(String name, String value) {
        setHeaderValue(name, value);
    }

    public void addHeader(String name, String value) {
        addHeaderValue(name, value);
    }

    public void setIntHeader(String name, int value) {
        setHeaderValue(name, value+"");
    }

    public void addIntHeader(String name, int value) {
        addHeaderValue(name, value+"");
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
     */
    public boolean containsHeader(String name) {
        boolean found = false;
        if (name != null) {
            found = this.headers.containsKey(name);
        }
        return found;
    }


    // HEADER handling methods

    /**
     * Return the names of all specified headers as a Set of Strings.
     * @return the <code>Set</code> of header name <code>Strings</code>, or an empty <code>Set</code> if none
     */
    public Set getHeaderNames() {
        return this.headers.keySet();
    }

    /**
     * Get all headers in this response
     * @return all headers as a map of string (header name) -> List(String) (header values)
     */
    public Map<String, Vector<String>> getActualHeaders() {
        return Collections.unmodifiableMap(this.headers);
    }

    /**
     * Get all headers in this response as a map of string (name) -> String[] (values)
     * @return all headers in this response as a map of string (name) -> String[] (values)
     */
    public Map<String, String[]> getHeaders() {
        Map<String, String[]> m = new ArrayOrderedMap<String, String[]>();
        if (this.headers != null && this.headers.size() > 0) {
            Set<String> keysSet = this.headers.keySet();
            ArrayList<String> keysList = new ArrayList<String>(keysSet);
            Collections.sort(keysList);
            for (String key : keysList) {
                Vector<String> values = this.headers.get(key);
                if (values != null && values.size() > 0) {
                    String[] value = values.toArray(new String[values.size()]);
                    m.put(key, value);
                }
            }
        }
        return m;
    }

    /**
     * Delete a header and all values by name
     * @param name the name key of the header
     */
    public void removeHeader(String name) {
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("name ("+name+") must not be null");
        }
        this.headers.remove(name);
    }

    public void clearHeaders() {
        this.headers.clear();
    }

    private void setHeaderValue(String name, String value) {
        doAddHeaderValue(name, value, true);
    }

    private void addHeaderValue(String name, String value) {
        doAddHeaderValue(name, value, false);
    }

    private void doAddHeaderValue(String name, String value, boolean replace) {
        if (name == null || "".equals(name)
                || value == null) {
            throw new IllegalArgumentException("name ("+name+") and value ("+value+") must not be null");
        }
        if (replace) {
            Vector<String> v = new Vector<String>();
            v.add(value);
            this.headers.put(name, v);
        } else {
            if (this.headers.containsKey(name)) {
                this.headers.get(name).add(value);
            } else {
                Vector<String> v = new Vector<String>();
                v.add(value);
                this.headers.put(name, v);
            }
        }
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setStatus(int status, String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public int getStatus() {
        return this.status;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

}
