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
package org.apache.pluto.testsuite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * This class contains one or more test results.
 */
public class TestResults implements Serializable {


    private String name;

    private final ArrayList list = new ArrayList();

    private boolean failed = false;
    private boolean inQuestion = false;

    public TestResults(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void add(TestResult result) {
        if (result.getReturnCode() == TestResult.FAILED) {
            failed = true;
        } else if (result.getReturnCode() == TestResult.WARNING) {
            inQuestion = true;
        }
        list.add(result);
    }

    public boolean isFailed() {
        return failed;
    }

    public boolean isInQuestion() {
        return inQuestion;
    }

    public Collection getCollection() {
        return Collections.unmodifiableCollection(list);
    }

    /**
     * Override of toString() that prints out variable
     * names and values.
     *
     * @see java.lang.Object#toString()
     */
    public String toString(){
    	StringBuffer buffer = new StringBuffer();
    	buffer.append(getClass().getName());
    	buffer.append("[name=").append(name);
    	buffer.append(";failed=").append(failed);
    	buffer.append(";inQuestion=").append(inQuestion);
    	buffer.append(";results={").append(list).append("}]");
    	return buffer.toString();
    }
}
