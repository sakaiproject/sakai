/**********************************************************************************
*
 * Copyright (c) 2003, 2004, 2007, 2008 The Sakai Foundation
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
*
**********************************************************************************/
package edu.indiana.lib.twinpeaks.net;

import edu.indiana.lib.twinpeaks.util.*;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Handle HTTP based search operations.  Send (POST or GET) a query to the
 * server, read up the response.
 *
 * The response text and HTTP details (status, character set, etc) are made
 * available to caller.
 */
@Slf4j
public class HttpTransaction {
  /**
   * Agent identification, HTTP form submission types
   */
  private final static String 	AGENT 			= "TwinPeaksAgent/1.0";
  public  final static String		METHOD_GET	= "GET";
  public  final static String		METHOD_POST = "POST";
	/*
	 * Character set constants
	 */
	private static final String		CHARSETEQ	= "charset=";
	public  static final String 	DEFAULTCS = HttpTransactionUtils.DEFAULTCS;

  private URL               url;
  private ParameterMap      parameters;
  private HttpURLConnection connection;

  private String            username;
  private String            password;

  private int               responseCode;
  private byte[]            responseRaw;
  private String            responseString;
  private CaseBlindHashMap  responseHeaders;
  private List              responseCookies;

	private String						inputCharacterSet;
	private String						defaultCharacterSet;

  private boolean           doPost;
  private boolean           doRedirects;
  private boolean           transactionDone;
  private boolean						preserveBaseUrlFile;

  /**
   * Default constructor
   */
  public HttpTransaction() {
    this.responseHeaders = new CaseBlindHashMap();
  }

  /**
   * Initialize.
   */
  public void initialize(URL url, List cookieList) {

    this.url              		= url;
    this.responseCookies  		= cookieList;
    this.parameters       		= null;
    this.doPost           		= true;
    this.doRedirects      		= false;
    this.preserveBaseUrlFile	= false;
    this.responseCode     		= 0;
    this.transactionDone  		= false;
    this.defaultCharacterSet	= DEFAULTCS;
    this.inputCharacterSet		= DEFAULTCS;
  }

  /**
   * Initialize
   */
  public void initialize(List cookieList) {
    initialize(null, cookieList);
  }

  /**
   * Set the transaction type
   * @param type (GET or POST)
   */
  public void setTransactionType(String type) {

    doPost = true;

    if (METHOD_GET.equalsIgnoreCase(type)) {
      doPost = false;
      return;
    }

    if (!METHOD_POST.equalsIgnoreCase(type)) {
      throw new IllegalArgumentException("Unsupported transaction: " + type);
    }
  }

  /**
   * Honor redirects?
   * @param follow Set as true to follow any redirects suggested
   */
  public void setFollowRedirects(boolean follow) {
    doRedirects = follow;
  }

  /**
   * Set up a name=value pair
   * @param name Parameter name
   * @param value Parameter value
   */
  public void setParameter(String name, String value) {
    addParameter(name, value);
  }

	/**
	 * Get a named parameter
   * @param name Parameter name
   * @return Parameter value
	 */
	public String getParameter(String name) {
		return parameters.getParameterMapValue(name);
	}

	/**
	 * Get parameter the name of first occurance of the supplied value
   * @param value Parameter value
   * @return Parameter name
	 */
	public String getParameterName(String value) {
		return parameters.getParameterMapName(value);
	}

  /**
   * Empty the parameter list
   */
  public void clearParameters() {

    if (parameters != null) {
      parameters.clear();
    }
  }

  /**
   * Initialize for a new transaction
   */
  private void reset() throws DomException {

    connection  		= null;
    responseString	= null;
    responseRaw 		= null;
    transactionDone = false;

    responseHeaders.clear();
  }

  /**
   * Get the response status
   * @return The HTTP response code
   */
  public int getResponseCode() {
    verifyServerResponseSeen();
    return responseCode;
  }

  /**
   * Get the URL text sent to the server
   * @return URL string
   */
  public String getUrl() {
    verifyServerResponseSeen();
    return connection.getURL().toString();
  }

  /**
   * Set the "preserve URL file" flag
   * @param state true to preserve the URL file portion (default is false)
   */
  public void setPreserveBaseUrlFile(boolean state) {
    preserveBaseUrlFile = state;
  }

  /**
   * Get the basic url specification for this connection
   * @return protocol://hostname[:port][/file]
   */
  public String getBaseUrlSpecification() throws MalformedURLException {
  	verifyServerResponseSeen();
		return HttpTransactionUtils.formatUrl(connection.getURL(), preserveBaseUrlFile);
  }

  /**
   * Get the unfiltered response as sent by the server (debug)
   * @return Response text as recieved
   */
  public byte[] getResponseBytes() {
    verifyServerResponseSeen();
    return responseRaw;
  }

  /**
   * Get the character-set-encoded String rendition of the server response
   * @return Response text as recieved
   */
  public String getResponseString() {
    verifyServerResponseSeen();
    return responseString;
  }

  /**
   * Get all HTTP response headers
   * @return CaseBlindHashMap of response-field/value pairs
   */
  public CaseBlindHashMap getResponseHeaders() {
    verifyServerResponseSeen();
    return responseHeaders;
  }

  /**
   * Get a named HTTP response
   * @return Response value
   */
  public String getResponseHeader(String key) {
    verifyServerResponseSeen();
    return (String) responseHeaders.get(key);
  }

  /**
   * Get all provided cookies
   * @return CaseBlindHashMap of response-field/value pairs
   */
  public List getResponseCookies() {
    verifyServerResponseSeen();
    return responseCookies;
  }

	/**
	 * Get the response document character set (supplied by the server)
	 * @return The character set (as a String, default to iso-8859-1)
	 */
	public String getResponseCharacterSet() {
		return getResponseCharacterSet(false);
	}

	/**
	 * Set the default character set
	 * @param cs Character set (utf-8, etc)
	 * Note: the character set defaults to DEFAULTCS (above) if not overridden
	 */
	public void setDefaultCharacterSet(String cs) {
		defaultCharacterSet = cs;
	}

	/**
	 * Get the default character set (use if none supplied by server)
	 * @return The character set (iso-8859-1, utf-8, etc)
	 */
	public String getDefaultCharacterSet() {
		return defaultCharacterSet;
	}

	/**
	 * Set the input character set
	 * @param cs Character set (utf-8, etc)
	 * Note: the character set defaults to DEFAULTCS (above) if not overridden
	 */
	public void setInputCharacterSet(String cs) {
		inputCharacterSet = cs;
	}

	/**
	 * Get the default character set (use if none supplied by server)
	 * @return The character set (iso-8859-1, utf-8, etc)
	 */
	public String getInputCharacterSet() {
		return inputCharacterSet;
	}

	/**
	 * Get the response document character set (supplied by the server)
	 * @param verify Validate server state (transaction complete)?
	 * @return The character set (as a String, default to iso-8859-1)
	 */
	private String getResponseCharacterSet(boolean verify) {
		String				contentType;
		StringBuilder	buffer;
		int						index;

		if (verify) {
			verifyServerResponseSeen();
		}

		contentType	= connection.getContentType();
    log.debug("ContentType = " + contentType);

		index = (contentType == null) ? -1 : contentType.toLowerCase().indexOf(CHARSETEQ);

		if (index == -1) {
			log.debug("return default character set: "
											 + getDefaultCharacterSet());
			return getDefaultCharacterSet();
		}

		buffer = new StringBuilder();
		for (int i = (index + CHARSETEQ.length()); i < contentType.length(); i++) {

			switch (contentType.charAt(i)) {
				case ' ':
				case '\t':
				case ';':
					break;

				default:
					buffer.append(contentType.charAt(i));
					break;
			}
		}
		log.debug("character set = "
											+ ((buffer.length() == 0) ? getDefaultCharacterSet()
																								: buffer.toString()));
		return (buffer.length() == 0) ? getDefaultCharacterSet() : buffer.toString();
	}

  /**
   * Add a <code>name=value</code> pair to the parameter list
   *
   * @param name  Parameter name
   * @param value Parameter content
   */
  private void addParameter(String name, String value) {

    if ((name != null) && (value != null)) {

      if (parameters == null) {
        parameters = new ParameterMap();
      }
      parameters.setParameterMapValue(name, value);
    }
  }

	/**
	 * Create a URL object from the provided url text.  If this is a GET operation
	 * and parameters have been set up, add them to the url text first.
	 * @param url URL text (eg http://xx/yy/zz)
	 */
  private URL addParametersAndCreateUrl(String url)
  																				throws MalformedURLException,
  																						   UnsupportedEncodingException {
	 	StringBuilder	urlBuffer	= new StringBuilder(url);

    if ((!doPost) && (parameters != null)) {
	    String  	separator 	= "?";
	    String		cs					= getInputCharacterSet();
	    Iterator	it;

			if (url.indexOf('?') != -1) {
				separator = "&";
			}

			it = parameters.getParameterMapIterator();
			while (parameters.nextParameterMapEntry(it)) {

        urlBuffer.append(HttpTransactionUtils.formatParameter
        										(parameters.getParameterNameFromIterator(),
        										 parameters.getParameterValueFromIterator(),
        										 separator, cs));

        if (separator.equals("?")) {
          separator = "&";
        }
      }
    }
    return new URL(urlBuffer.toString());
  }

  /**
   * POST provided parameters
   */
  private void postParameters() throws IOException {

    Writer writer = null;
    String cs			= getInputCharacterSet();

    connection.setDoOutput(true);
    try {
      writer = new OutputStreamWriter(connection.getOutputStream(), cs);

      if (parameters != null) {
        String    separator = "";
        Iterator	it;

				it = parameters.getParameterMapIterator();
				while (parameters.nextParameterMapEntry(it)) {

          writer.write(separator
          				+ 	 parameters.getParameterNameFromIterator()
          				+ 	 "="
          				+    URLEncoder.encode
          									(parameters.getParameterValueFromIterator(), cs));

          if (separator.equals("")) {
            separator = "&";
          }
        }
      }

    } finally {
      try { if (writer != null) writer.close(); } catch (Exception ignore) { }
    }
  }

  /**
   * Read the server response
   */
  private void readResponse()
               throws IOException, DomException, UnsupportedEncodingException {

    ByteArrayOutputStream   content = new ByteArrayOutputStream();
    BufferedInputStream     input   = null;

    byte[] buffer	= new byte[1024 * 8];
    int count;

    /*
     * Read the entire response
     */
    try {
      input = new BufferedInputStream(connection.getInputStream());

      while ((count = input.read(buffer, 0, buffer.length)) != -1) {
        content.write(buffer, 0, count);
      }

    } finally {
      try { if (input != null) input.close(); } catch (Exception ignore) { }
    }
    /*
     * Save the response text
     */
    responseString = content.toString(getResponseCharacterSet(false));
    responseRaw    = content.toByteArray();
    /*
     * Pick up the HTTP status, headers, cookies
     */
    responseCode = connection.getResponseCode();

    responseHeaders.clear();
    for (int i = 0; ; i++) {
      CookieData  cookie;
      String			key, value;

      key   = connection.getHeaderFieldKey(i);
      value = connection.getHeaderField(i);

      if ((key == null) && (value == null)) {
        break;
      }

      if (!"Set-Cookie".equalsIgnoreCase(key)) {
        responseHeaders.put(key, value);
        continue;
      }

      cookie = CookieUtils.parseCookie(url, value);
      CookieUtils.storeCookie(responseCookies, cookie);
      continue;
    }
  }

	/**
	 * Append a cookie attribute to the current cookie text
	 * @param sb Cookie text (StringBuilder)
	 * @param attribute Attribute name
	 * @param value Attribute value
	 */
  private void append(StringBuilder sb, String attribute, String value, boolean writeSeperator) {
    if (value != null) {

      sb.append(attribute);
      sb.append("=");
      sb.append(value);

      if (writeSeperator) {
      	sb.append( "; ");
      }
    }
  }

  /**
   * Set request (client-side) cookies
   */
  private String setRequestCookies() {
    List          cookieList;
    StringBuilder  cookieValues;
    Iterator      iterator;
    String        value;

    cookieValues  = new StringBuilder();
    cookieList    = CookieUtils.findCookiesForServer(responseCookies, url);
    iterator      = cookieList.iterator();

    while (iterator.hasNext()) {
      CookieData cookie = (CookieData) iterator.next();

      append(cookieValues, cookie.getName(), cookie.getValue(), iterator.hasNext());
    }
		return cookieValues.toString();
  }

  /**
   * Get an HttpURLConnection for this transaction
   */
  public HttpURLConnection getConnection() throws IOException {
    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
    /*
     * Set up the target URL (once)
     */
    return urlConnection;
  }

  /**
   * Perform one command transaction - add parameters and build the URL
   * @param url URL string to server-side resource
   * @return HTTP response code
   */
  public int doTransaction(String url) throws IOException, DomException {

  	this.url = addParametersAndCreateUrl(url);
    return doTransaction();
  }

  /**
   * Perform one command transaction
   * @param url URL for server-side
   * @return HTTP response code
   */
  public int doTransaction(URL url) throws IOException, DomException {
    this.url = url;
    return doTransaction();
  }

  /**
   * Perform one command transaction
   * @return HTTP response code
   */
  public int doTransaction() throws IOException, DomException {
  	String clientCookie;
    /*
     * Get connection, set transaction characteristics
     */
		log.debug("*** CONNECTING to URL: " + this.url.toString());

    reset();
    connection = getConnection();

    connection.setRequestProperty("User-Agent", AGENT);
    connection.setRequestProperty("Accept", "text/xml, text/html, text/*;q=0.5");
    connection.setRequestProperty("Accept-Charset", "iso-8859-1, utf-8, *;q=0.5");
		/*
		 * Send along any appropriate cookies
		 */
    clientCookie = setRequestCookies();
		if (clientCookie.length() > 0) {
			log.debug("Cookie: " + clientCookie);
    	connection.setRequestProperty("Cookie", clientCookie);
	  }
		/*
		 * Handle HTTP redirects as requested
		 */
    connection.setInstanceFollowRedirects(doRedirects);
    /*
     * POST or GET?
     *
     * Should GET build the URL from the parameter list?  We don't at present.
     */
    connection.setDoInput(true);
    if (doPost) {
      postParameters();
    }
		/*
		 * Get the server response, "close" the connection, return HTTP status
		 */
    readResponse();

    connection.disconnect();
    transactionDone = true;

    return getResponseCode();
  }

  /**
   * Verify we've recieved some sort of server response, even if invalid
   */
  private void verifyServerResponseSeen() {

    if (!transactionDone) {
      String message = "The server transaction is not yet complete";

      throw new IllegalStateException(message);
    }
  }
}
