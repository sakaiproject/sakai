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

/**
 * The test result represents the result of one test method.
 *
 */
public class TestResult implements Serializable {

	/** The undefined return code. */
	public static final int UNDEFINED = -1;

	/** The warning return code. */
	public static final int WARNING = 0;

	/** The failed return code. */
	public static final int FAILED = 1;

	/** The passed return code. */
	public static final int PASSED = 2;


    // Private Member Variables ------------------------------------------------

    /** The name of the test. */
    private String name = null;

    /** The description of the test. */
    private String description = "[unknown]";

    /** The PLT number in the spec that is tested. */
    private String specPLT = "[unknown]";

    /** The return code of the test result: PASSED, WARNING, FAILED. */
    private int returnCode = UNDEFINED;

    /** The message of the test result. */
    private String resultMessage ="[unknown]";


    // Public Methods ----------------------------------------------------------

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
    	return description;
    }

    public void setDescription(String description) {
    	this.description = description;
    }

    public String getSpecPLT() {
    	return specPLT;
    }

    public void setSpecPLT(String specPLT) {
    	this.specPLT = specPLT;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public String getReturnCodeAsString() {
    	if (returnCode == WARNING) {
    		return "WARNING";
    	} else if (returnCode == FAILED) {
    		return "FAILED";
    	} else if (returnCode == PASSED) {
    		return "PASSED";
    	} else {
    		return "UNKNOWN RETURN CODE";
    	}
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public String getResultMessage() {
    	return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
    	this.resultMessage = resultMessage;
    }


    // Object Methods ----------------------------------------------------------

    /**
     * Override of toString() that prints out name and results values.
     * @see java.lang.Object#toString()
     */
    public String toString(){
    	StringBuffer buffer = new StringBuffer();
    	buffer.append(getClass().getName());
    	buffer.append("[name=").append(name);
    	buffer.append(";returnCode=").append(returnCode);
    	buffer.append(";resultMessage=").append(resultMessage).append("]");
    	return buffer.toString();
    }

}
