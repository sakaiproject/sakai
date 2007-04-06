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
import org.hsqldb.lib.HashMappedList;
import org.hsqldb.persist.Logger;

/**
 * Manages SEQUENCE objects for a Database instance. <p>
 *
 * @author fredt@users
 * @version 1.7.2
 * @since 1.7.2
 */
public class SequenceManager {

    HashMappedList sequenceMap;

    SequenceManager() {
        sequenceMap = new HashMappedList();
    }

    /**
     *  Drops a sequence with the specified name from this Database
     *
     * @param name of the sequence to drop
     * @throws HsqlException if a database access error occurs
     */
    void dropSequence(String name) throws HsqlException {

        boolean found = sequenceMap.containsKey(name);

        Trace.check(found, Trace.SEQUENCE_NOT_FOUND, name);
        sequenceMap.remove(name);
    }

    public NumberSequence getSequence(String name) {
        return (NumberSequence) sequenceMap.get(name);
    }

    NumberSequence createSequence(HsqlName hsqlname, long start,
                                  long increment,
                                  int type) throws HsqlException {

        Trace.check(!sequenceMap.containsKey(hsqlname.name),
                    Trace.SEQUENCE_ALREADY_EXISTS);

        NumberSequence sequence = new NumberSequence(hsqlname, start,
            increment, type);

        sequenceMap.put(hsqlname.name, sequence);

        return sequence;
    }

    String logSequences(Session c, Logger logger) throws HsqlException {

        for (int i = 0; i < sequenceMap.size(); i++) {
            NumberSequence seq = (NumberSequence) sequenceMap.get(i);

            if (seq.wasUsed()) {
                logger.writeSequenceStatement(c, seq);
                seq.resetWasUsed();
            }
        }

        return null;
    }
}
