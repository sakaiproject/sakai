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
package org.apache.pluto.driver.url;

/**
 * The portal URL parameter.
 * @version 1.0
 * @since Sep 30, 2004
 */
public class PortalURLParameter {


    private String window;
    private String name;
    private String[] values;

    public PortalURLParameter(String window, String name, String value) {
        this.window = window;
        this.name = name;
        this.values = new String[]{value};
    }

    public PortalURLParameter(String window, String name, String[] values) {
        this.window = window;
        this.name = name;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    public String getWindowId() {
        return window;
    }

}

