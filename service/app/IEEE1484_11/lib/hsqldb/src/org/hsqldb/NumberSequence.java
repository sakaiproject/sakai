/* Copyright (c) 2001-2005, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb;

import org.hsqldb.HsqlNameManager.HsqlName;

/**
 * Maintains a sequence of numbers.
 *
 * @author  fredt@users
 * @since 1.7.2
 * @version 1.7.2
 */
public class NumberSequence {

    HsqlName name;

    // original start value - used in CREATE and ALTER commands
    private long startValue;

    // present value
    private long currValue;

    // last value
    private long lastValue;
    private long increment;
    private int  dataType;

    /**
     * constructor with initial value and increment;
     */
    public NumberSequence(HsqlName name, long value, long increment,
                          int type) {

        this.name      = name;
        startValue     = currValue = lastValue = value;
        this.increment = increment;
        dataType       = type;
    }

    /**
     * principal getter for the next sequence value
     */
    synchronized long getValue() {

        long value = currValue;

        currValue += increment;

        return value;
    }

    /**
     * getter for a given value
     */
    synchronized long getValue(long value) {

        if (value >= currValue) {
            currValue = value;
            currValue += increment;

            return value;
        } else {
            return value;
        }
    }

    /** @todo fredt - check against max value of type */
    Object getValueObject() {

        long value = currValue;

        currValue += increment;

        Object result;

        if (dataType == Types.INTEGER) {
            result = new Integer((int) value);
        } else {
            result = new Long(value);
        }

        return result;
    }

    /**
     * reset to start value
     */
    void reset() {

        // no change if called before getValue() or called twice
        lastValue = currValue = startValue;
    }

    /**
     * get next value without incrementing
     */
    public long peek() {
        return currValue;
    }

    /**
     * true if one or more values were retreived since the last resetWasUsed
     */
    boolean wasUsed() {
        return lastValue != currValue;
    }

    /**
     * reset the wasUsed flag
     */
    void resetWasUsed() {
        lastValue = currValue;
    }

    /**
     * reset to new initial value
     */
    public void reset(long value) {
        startValue = currValue = lastValue = value;
    }

    void reset(long value, long increment) {

        reset(value);

        this.increment = increment;
    }

    int getType() {
        return dataType;
    }

    public HsqlName getName() {
        return name;
    }

    public String getSchemaName() {
        return name.schema.name;
    }

    long getIncrement() {
        return increment;
    }
}
