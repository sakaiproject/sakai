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
package edu.indiana.lib.twinpeaks.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Represent a single cookie
 */
@Slf4j
public class CookieData {
	/**
	 * Null (unset) cookie age
	 */
  public static final int NULL_AGE        = -1;
  /**
   * Expired cookie
   */
  public static final int EXPIRED_AGE     =  0;

  private String  key;
  private String  value;

  private String  path;
  private String  domain;
  private String  version;

  private String  expires;
  private int     maxAge;
  private boolean secure;


  private CookieData() {
  }

	/**
	 * Constructor
	 * @param url URL associated with this cookie
	 * @param key Cookie name
	 * @param value Cookie value
	 */
  public CookieData(URL url, String key, String value) {
    int slash     = url.getFile().lastIndexOf("/");
    /*
     * Save cookie name and content
     */
    this.key      = key;
    this.value    = value;
    /*
     * Domain defaults to hostname, path to the "directory" portion of
     * the request, minus all text from the rightmost "/" character to
     * the end of the string...
     */
    this.path     = slash < 1 ? "" : url.getFile().substring(0, slash);
    this.domain   = url.getHost();

    this.version  = null;
    this.expires  = null;
    this.maxAge   = NULL_AGE;

    this.secure   = false;
  }

  /**
   * Get cookie name
   * @return The cooke name
   */
  public String getName() {
    return this.key;
  }

  /**
   * Get cookie value (the cookie "text")
   * @return The value
   */
  public String getValue() {
    return this.value;
  }

  /**
   * Save the path
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Get the path
   * @return The cooke path attribute value (null if none)
   */
  public String getPath() {
    return this.path;
  }

  /**
   * Save the expiration date
   */
  public void setExpires(String expires) {
    this.expires = expires;
  }

  /**
   * Get the expiration date
   * @return The expires attribute value (null if none)
   */
  public String getExpires() {
    return this.expires;
  }

  /**
   * Save the domain
   */
  public void setDomain(String domain) {
    this.domain = domain;
  }

  /**
   * Get the domain
   * @return The domain attribute value (null if none)
   */
  public String getDomain() {
    return this.domain;
  }

  /**
   * Save the version
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Get the cookie version
   * @return The version (null if none)
   */
  public String getVersion() {
    return this.version;
  }

  /**
   * Set the maximum age for this cookie
   */
  public void setMaxAge(String maxAge) {
    try {
      this.maxAge = Integer.parseInt(maxAge);
    } catch (NumberFormatException ignore) { }
  }

  /**
   * Get the maximum age for this cookie
   */
  public int getMaxAge() {
    return this.maxAge;
  }

  /**
   * Save security setting (true if cookie to be sent only via HTTPS)
   */
  public void setSecure(boolean secure) {
    this.secure = secure;
  }

  public boolean getSecure() {
    return this.secure;
  }

  /**
   * Equal strings?
   * @param a String one
   * @param b Stringtwo
   * @return true if both are null, or String "equals" is true
   */
  private boolean stringEquals(String a, String b) {
    if ((a == null) && (b == null)) {
      return true;
    }

    if ((a == null) || (b == null)) {
      return false;
    }

    return a.equals(b);
  }

  /**
   * Equal cookies?
   * @param cookie for comparison
   * @return true if cookie name, path, and domain are all equal
   */
  public boolean equals(CookieData cookie) {
    if (!key.equals(cookie.getName())) {
      return false;
    }

    return stringEquals(path, cookie.getPath()) &&
           stringEquals(domain, cookie.getDomain());
  }
}