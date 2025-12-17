/**
 * $Id$
 * $URL$
 * RequestStorageImpl.java - entity-broker - Jul 24, 2008 2:26:09 PM - azeckoski
 **************************************************************************
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
 */

package org.sakaiproject.entitybroker.util.request;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorageWrite;
import org.azeckoski.reflectutils.ReflectUtils;


/**
 * Impl for the request store,
 * will store values in the request itself and will maintain a map of all request values in a threadlocal
 * which should always be cleared at the end of the request
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@SuppressWarnings("unchecked")
public class RequestStorageImpl implements RequestStorageWrite {

    protected RequestStorageImpl() { }

    public RequestStorageImpl(RequestGetter requestGetter) {
        super();
        this.requestGetter = requestGetter;
    }

    private ThreadLocal<HashMap<String, Object>> requestStore = new ThreadLocal<HashMap<String,Object>>();
    protected HashMap<String, Object> getInternalMap() {
        if (requestStore.get() == null) {
            requestStore.set( new HashMap<String, Object>() );
        }
        return requestStore.get();
    }

    private RequestGetter requestGetter;
    public void setRequestGetter(RequestGetter requestGetter) {
        this.requestGetter = requestGetter;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage#inRequest()
     */
    public boolean inRequest() {
        boolean in = false;
        if (requestGetter.getRequest() != null) {
            in = true;
        }
        return in;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage#getStorageMapCopy()
     */
    public Map<String, Object> getStorageMapCopy() {
        return getStorageMapCopy(true, true, true, true);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage#getStoredValue(java.lang.String)
     */
    public Object getStoredValue(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key must be non-null");
        }
        return getRequestValue(key);
    }

    /**
     * Special version which allows getting only the parts that are desired
     * @param includeInternal include the internal request values
     * @param includeHeaders include the request headers
     * @param includeParams include the request parameters
     * @param includeAttributes include the request attributes
     * @return the map with the requested values
     */
    public Map<String, Object> getStorageMapCopy(boolean includeInternal, boolean includeHeaders, boolean includeParams, boolean includeAttributes) {
        HashMap<String, Object> m = new HashMap<String, Object>();
        m.putAll( getRequestValues(includeHeaders, includeParams, includeAttributes) ); // put in the request ones first
        if (includeInternal) {
            m.putAll( getInternalMap() );
        }
        return m;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage#getStoredValueAsType(java.lang.Class, java.lang.String)
     */
    public <T> T getStoredValueAsType(Class<T> type, String key) {
        if (type == null) {
            throw new IllegalArgumentException("type must be non-null");
        }
        Object value = getStoredValue(key);
        T togo = null;
        if (value != null) {
            if (value.getClass().isAssignableFrom(type)) {
                // type matches the requested one
                togo = (T) value;
            } else {
                togo = (T) ReflectUtils.getInstance().convert(value, type);
            }
        }
        return togo;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage#setStoredValue(java.lang.String, java.lang.Object)
     */
    public void setStoredValue(String key, Object value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("key and value must be non-null");
        }
        for (int i = 0; i < ReservedKeys.values().length; i++) {
            String rKey = ReservedKeys.values()[i].name();
            if (rKey.equals(key)) {
                throw new IllegalArgumentException("Cannot change the value of a reserved key: " + rKey);
            }
        }
        setRequestValue(key, value);
    }

    /**
     * Resets the request storage and purges all stored values (has no effect on the data in the request)
     */
    public void reset() {
        getInternalMap().clear();
        // only clear the known attribute values from the request -AZ
        HttpServletRequest request = requestGetter.getRequest();
        if (request != null) {
            for (String key : getInternalMap().keySet()) {
                request.removeAttribute(key);
            }
        }
    }

    /**
     * Allows user to set the value of a key directly, including reserved keys
     * @param key
     * @param value
     */
    public void setRequestValue(String key, Object value) {
        HttpServletRequest request = requestGetter.getRequest();
        if (request != null) {
            if (value instanceof String) {
                request.setAttribute(key, value);
            }
        }
        getInternalMap().put(key, value);
    }

    /**
     * Place all these params into the request storage
     * @param params map of string -> value
     */
    public void setRequestValues(Map<String, Object> params) {
        if (params != null && params.size() > 0) {
            for (Entry<String, Object> entry : params.entrySet()) {
                setRequestValue(entry.getKey(), entry.getValue());
            }
        }
    }

    protected Object getRequestValue(String key) {
        Object value = getInternalMap().get(key);
        if (value == null) {
            value = getAllRequestValues().get(key);
        }
        if (value == null) {
            // perhaps get one of the reserved values
            if (ReservedKeys._locale.name().equals(key)) {
                // _locale
                value = Locale.getDefault();
            } else if (ReservedKeys._requestEntityReference.name().equals(key)) {
                // _requestEntityReference
                value = "/describe"; // default if not known
            } else if (ReservedKeys._requestActive.name().equals(key)) {
                // _requestActive
                if (requestGetter.getRequest() == null) {
                    value = false;
                } else {
                    value = true;
                }
            } else if (ReservedKeys._requestOrigin.name().equals(key)) {
                // _requestOrigin
                value = RequestOrigin.INTERNAL.name();
            }
        }
        return value;
    }

    protected Map<String, Object> getAllRequestValues() {
        HttpServletRequest request = requestGetter.getRequest();
        return getRequestValues(request, true, true, true);
    }

    protected Map<String, Object> getRequestValues(boolean includeHeaders, boolean includeParams, boolean includeAttributes) {
        HttpServletRequest request = requestGetter.getRequest();
        return getRequestValues(request, includeHeaders, includeParams, includeAttributes);
    }

    @Override
    public String toString() {
        return "RS:getter="+(this.requestGetter != null)+":store=" + getInternalMap();
    }

    public static class EntryComparator implements Comparator<Entry<String, Object>>, Serializable {
        public final static long serialVersionUID = 1l;
        public int compare(Entry<String, Object> o1, Entry<String, Object> o2) {
            return o1.getKey().compareTo(o2.getKey());
        }
    }

    // STATIC

    public static Map<String, Object> getRequestValues(HttpServletRequest request, 
            boolean includeHeaders, boolean includeParams, boolean includeAttributes) {
        HashMap<String, Object> m = new HashMap<String, Object>();
        if (request != null) {
            if (includeHeaders) {
                Enumeration<String> headerEnum = request.getHeaderNames();
                if (headerEnum != null) {
                    for (Enumeration<String> e = headerEnum ; e.hasMoreElements() ;) {
                        String key = e.nextElement();
                        Object value = request.getHeader(key);
                        m.put(key, value);
                    }
                }
            }
            if (includeParams) {
                Map<String, String[]> pMap = request.getParameterMap();
                if (pMap != null) {
                    for (Entry<String, String[]> param : pMap.entrySet()) {
                        if (param.getValue().length > 1) {
                            m.put(param.getKey(), param.getValue());
                        } else {
                            String value = "";
                            if (param.getValue().length == 1) {
                                value = param.getValue()[0];
                            }
                            m.put(param.getKey(), value);
                        }
                    }
                }
            }
            if (includeAttributes) {
                Enumeration<String> aEnum = request.getAttributeNames();
                if (aEnum != null) {
                    for (Enumeration<String> e = aEnum ; e.hasMoreElements() ;) {
                        String key = e.nextElement();
                        Object value = request.getAttribute(key);
                        m.put(key, value);
                    }
                }
            }
            // encode a select set of values from the request
            m.put(ReservedKeys._locale.name(), request.getLocale());
            m.put("method", request.getMethod().toUpperCase());
            m.put("queryString", request.getQueryString() == null ? "" : request.getQueryString());
            m.put("pathInfo", request.getPathInfo() == null ? "" : request.getPathInfo());
        }
        return m;
    }

}
