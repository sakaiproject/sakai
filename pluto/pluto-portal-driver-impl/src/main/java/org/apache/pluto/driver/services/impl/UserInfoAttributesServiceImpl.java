/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pluto.driver.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.portlet.PortletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.PortletWindow;
import org.apache.pluto.spi.optional.UserInfoAttributesService;
import org.apache.pluto.spi.optional.P3PAttributes;

/**
 * FIXME: Only include attributes that are defined in portlet.xml
 *
 * This is a default implementation of that gets user information attributes
 * from a properties file where the user information attribute name (as defined
 * in PLT.D of the JSR-168 spec) is prefixed by the user name (e.g. craig.user.name.given=Craig).
 *
 */
public class UserInfoAttributesServiceImpl implements UserInfoAttributesService {

	private static UserInfoAttributesServiceImpl instance = new UserInfoAttributesServiceImpl();

	private static final String USER_INFO_ATTR_FILE = "/user-info-attributes.properties";
	/** Logger. */
	private static final Log LOG = LogFactory.getLog(
			UserInfoAttributesServiceImpl.class);
	private static Properties props = new Properties();
	private static Map cache = new HashMap();

	private UserInfoAttributesServiceImpl(){

	}

	public static UserInfoAttributesServiceImpl getInstance() throws IOException {
		loadProperties();
		return instance;
	}

	public static UserInfoAttributesServiceImpl getInstance(Properties inprops) throws IOException {
		props = inprops;
		return instance;
	}

	/**
	 * Implementation of PLT.17.2 used to access user information attributes.
	 *
	 * @see UserInfoAttributesService#getUserInfo(javax.portlet.PortletRequest)
	 * @return As per the spec, return null if the user is not authenticated or an empty Map if there are
	 * no attributes in the properties file or a Map containing only those attributes found in the attribute
	 * data store (properties file).
	 */
	public Map getUserInfo(PortletRequest request)
			throws PortletContainerException {
		Map map = null;
		String user = request.getRemoteUser();
		if (user == null) {
			return null;
		}
		map = (Map)cache.get(user);
		if (map == null) {
			map = new HashMap();
			int len = P3PAttributes.ATTRIBUTE_ARRAY.length;
			StringBuffer prefix = new StringBuffer();
			prefix.append(user);
			prefix.append('.');
			StringBuffer name = null;
			for (int i = 0; i < len ; i++) {
				name = new StringBuffer();
				name.append(prefix);
				String attr = P3PAttributes.ATTRIBUTE_ARRAY[i];
				name.append(attr);
				String prop = props.getProperty(name.toString());
				//spec says that Map only attributes that have data
				if ( prop != null) {
					//TODO: convert user.bdate to milliseconds since January 1, 1970, 00:00:00 GMT.
					map.put(attr, prop);
				}
			}
			cache.put(user, map);
		}
		return map;
	}


    public Map getUserInfo(PortletRequest request, PortletWindow window) throws PortletContainerException {
        return getUserInfo(request);
    }

    private static void loadProperties() throws IOException {
	    //get the properties from prop file
		if (props.isEmpty()) {
		    InputStream stream = UserInfoAttributesServiceImpl.class.getResourceAsStream(USER_INFO_ATTR_FILE);
		    if (stream == null) {
		    	String msg = "The properties file '" + USER_INFO_ATTR_FILE +"' cannot be found." +
		    	" Please make sure this file exists and is in the classpath (i.e. WEB-INF/classes).";
		    	LOG.error(msg);
					throw new IOException(msg);
			}
			props.load(stream);
		}
	}



}
