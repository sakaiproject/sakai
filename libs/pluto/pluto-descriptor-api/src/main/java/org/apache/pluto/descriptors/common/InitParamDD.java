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
package org.apache.pluto.descriptors.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Initialization Parameter configuration.
 *
 * @version $Id: InitParamDD.java 157038 2005-03-11 03:44:40Z ddewolf $
 * @since Feb 28, 2005
 */
public class InitParamDD {

    /** The name of the parameter. */
    private String paramName;

    /** The value of the parameter. */
    private String paramValue;

    /** The description of the parameter. */
    private List descriptions = new ArrayList();

    /**
     * Default Constructor.
     */
    public InitParamDD() {

    }

    /**
     * Retrieve the name of the parameter.
     * @return
     */
    public String getParamName() {
        return paramName;
    }

    /**
     * Set the name of the parameter.
     * @param paramName
     */
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    /**
     * Get the name of the parameter.
     * @return
     */
    public String getParamValue() {
        return paramValue;
    }

    /**
     * Set the value of the parameter.
     * @param paramValue
     */
    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public List getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List descriptions) {
        this.descriptions = descriptions;
    }
}

