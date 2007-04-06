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

/*
* This class introduces iterator functionality to Result.
*
* @author fredt@users
* @version 1.7.2
* @since 1.7.2
*/
public class ResultBase {

    public Record    rRoot;
    protected Record rTail;
    protected int    iSize;

    public ResultBase() {}

    public ResultIterator iterator() {
        return new ResultIterator();
    }

    public class ResultIterator {

        boolean removed;
        int     counter;
        Record  current = rRoot;
        Record  last;

        public boolean hasNext() {
            return counter < iSize;
        }

        public boolean next() {

            if (hasNext()) {
                removed = false;

                if (counter != 0) {
                    last    = current;
                    current = current.next;
                }

                counter++;

                return true;
            } else {
                return false;
            }
        }

        public boolean previous() {
            return false;
        }

        public boolean absolute(int rows) {
            return false;
        }

        public boolean relative(int rows) {
            return false;
        }

        public boolean beforeFirst() {
            return false;
        }

        public boolean afterLast() {
            return false;
        }

        public boolean isBeforeFirst() {
            return false;
        }

        public boolean isAfterLast() {
            return false;
        }

        public void remove() {

            if (counter <= iSize && counter != 0 &&!removed) {
                removed = true;

                if (current == rTail) {
                    rTail = last;
                }

                if (current == rRoot) {
                    current = rRoot = rRoot.next;
                } else {
                    current      = last;
                    last         = null;
                    current.next = current.next.next;
                }

                iSize--;
                counter--;

                return;
            }
        }
    }
}
