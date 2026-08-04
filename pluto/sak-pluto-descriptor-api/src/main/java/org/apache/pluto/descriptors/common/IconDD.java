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

/**
 * Resource Icon configuration.
 *
 * @version $Id: IconDD.java 157038 2005-03-11 03:44:40Z ddewolf $
 * @since Feb 28, 2005
 */
public class IconDD{

    /** The large icon uri. */
    private String largeIcon;

    /** The small icon uri. */
    private String smallIcon;

    /**
     * Default Constructor.
     */
    public IconDD() {

    }

    /**
     * Retrieve the large icon uri.
     * @return the uri to the large icon (relative to the context path).
     */
    public String getLargeIcon() {
        return largeIcon;
    }

    /**
     * Set the large icon uri.
     * @param largeIcon the relative path to the icon resource.
     */
    public void setLargeIcon(String largeIcon) {
        this.largeIcon = largeIcon;
    }

    /**
     * Retrieve the small icon uri.
     * @return the uri to the small icon (relative to the context path).
     */
    public String getSmallIcon() {
        return smallIcon;
    }

    /**
     * Set the small Icon uri.
     * @param smallIcon the relative path to the icon resource.
     */
    public void setSmallIcon(String smallIcon) {
        this.smallIcon = smallIcon;
    }

}

