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
package org.apache.pluto.descriptors.servlet;

/**
 * <B>TODO</B>: Document
 * @version $Id: EjbRefDD.java 156636 2005-03-09 12:16:31Z cziegeler $
 * @since Feb 28, 2005
 */
public class EjbRefDD {

    private String description;
    private String ejbRefName;
    private String ejbRefType;
    private String home;
    private String remote;
    private String ejbLink;
    private String runAs;

    public EjbRefDD() {

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEjbRefName() {
        return ejbRefName;
    }

    public void setEjbRefName(String ejbRefName) {
        this.ejbRefName = ejbRefName;
    }

    public String getEjbRefType() {
        return ejbRefType;
    }

    public void setEjbRefType(String ejbRefType) {
        this.ejbRefType = ejbRefType;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public String getEjbLink() {
        return ejbLink;
    }

    public void setEjbLink(String ejbLink) {
        this.ejbLink = ejbLink;
    }

    public String getRunAs() {
        return runAs;
    }

    public void setRunAs(String runAs) {
        this.runAs = runAs;
    }

}

