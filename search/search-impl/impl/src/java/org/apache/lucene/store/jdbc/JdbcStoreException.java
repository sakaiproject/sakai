/*
 * Copyright 2004-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.lucene.store.jdbc;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * A nestable checked Jdbc exception.
 *
 * @author kimchy
 */
public class JdbcStoreException extends IOException {

	private static final long serialVersionUID = 6238846660780283933L;

	/** Root cause of this nested exception */
    private Throwable cause;


    /**
     * Construct a <code>JdbcStoreException</code> with the specified detail message.
     * @param msg the detail message
     */
    public JdbcStoreException(String msg) {
        super(msg);
    }

    /**
     * Construct a <code>JdbcStoreException</code> with the specified detail message
     * and nested exception.
     * @param msg the detail message
     * @param ex the nested exception
     */
    public JdbcStoreException(String msg, Throwable ex) {
        super(msg);
        this.cause = ex;
    }

    /**
     * Return the nested cause, or <code>null</code> if none.
     */
    public Throwable getCause() {
        // Even if you cannot set the cause of this exception other than through
        // the constructor, we check for the cause being "this" here, as the cause
        // could still be set to "this" via reflection: for example, by a remoting
        // deserializer like Hessian's.
        return (this.cause == this ? null : this.cause);
    }

    /**
     * Return the detail message, including the message from the nested exception
     * if there is one.
     */
    public String getMessage() {
        if (getCause() == null) {
            return super.getMessage();
        }
        else {
            return super.getMessage() + "; nested exception is " + getCause().getClass().getName() +
                    ": " + getCause().getMessage();
        }
    }

    /**
     * Print the composite message and the embedded stack trace to the specified stream.
     * @param ps the print stream
     */
    public void printStackTrace(PrintStream ps) {
        if (getCause() == null) {
            super.printStackTrace(ps);
        }
        else {
            ps.println(this);
            getCause().printStackTrace(ps);
        }
    }

    /**
     * Print the composite message and the embedded stack trace to the specified print writer.
     * @param pw the print writer
     */
    public void printStackTrace(PrintWriter pw) {
        if (getCause() == null) {
            super.printStackTrace(pw);
        }
        else {
            pw.println(this);
            getCause().printStackTrace(pw);
        }
    }

    /**
     * Check whether this exception contains an exception of the given class:
     * either it is of the given class itself or it contains a nested cause
     * of the given class.
     * <p>Currently just traverses JdbcStoreException causes. Will use
     * the JDK 1.4 exception cause mechanism once requires JDK 1.4.
     * @param exClass the exception class to look for
     */
    public boolean contains(Class exClass) {
        if (exClass == null) {
            return false;
        }
        Throwable ex = this;
        while (ex != null) {
            if (exClass.isInstance(ex)) {
                return true;
            }
            if (ex instanceof JdbcStoreException) {
                // Cast is necessary on JDK 1.3, where Throwable does not
                // provide a "getCause" method itself.
                ex = ex.getCause();
            }
            else {
                ex = null;
            }
        }
        return false;
    }

}
