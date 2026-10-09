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
package org.apache.pluto.internal.impl;

import org.apache.pluto.util.StringManager;

import java.util.ResourceBundle;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

/**
 *
 * @version 1.0
 * @since Jan 9, 2006
 */
class CombinedPortletResourceBundle extends ResourceBundle {

    private static final StringManager EXCEPTIONS =
        StringManager.getManager(CombinedPortletResourceBundle.class.getPackage().getName());

    private final HashMap contents = new HashMap();

    public CombinedPortletResourceBundle(InlinePortletResourceBundle inlineBundle, ResourceBundle resourceBundle) {
       dump(inlineBundle);
       dump(resourceBundle);
    }

    protected Object handleGetObject(String key) {
        if(key == null) {
            throw new NullPointerException(EXCEPTIONS.getString("error.null"));
        }
        return contents.get(key);
    }

    public Enumeration getKeys() {
       return new Vector(contents.keySet()).elements();
    }

    private void dump(ResourceBundle bundle) {
        Enumeration e = bundle.getKeys();
        while(e.hasMoreElements()) {
            String value = e.nextElement().toString();
            contents.put(value, bundle.getObject(value));
        }
    }
}
