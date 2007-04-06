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
import org.hsqldb.lib.HsqlDeque;
import org.hsqldb.lib.StringConverter;
import org.hsqldb.lib.StringUtil;

// peterhudson@users 20020130 - patch 478657 by peterhudson - triggers support
// fredt@users 20020130 - patch 1.7.0 by fredt
// added new class as jdk 1.1 does not allow use of LinkedList
// fredt@users 20030727 - signature and other alterations
// fredt@users 20040430 - changes by mattshaw@users to allow termination of the
// trigger thread -

/**
 *  Represents an HSQLDB Trigger definition. <p>
 *
 *  Provides services regarding HSLDB Trigger execution and metadata. <p>
 *
 *  Development of the trigger implementation sponsored by Logicscope
 *  Realisations Ltd
 *
 * @author Peter Hudson - Logicscope Realisations Ltd
 * @version  1.7.0 (1.0.0.3)
 *      Revision History: 1.0.0.1 First release in hsqldb 1.61
 *      1.0.0.2 'nowait' support to prevent deadlock 1.0.0.3 multiple row
 *      queue for each trigger
 */
class TriggerDef extends Thread {

    /**
     *  member variables
     */
    static final int NUM_TRIGGER_OPS = 3;                          // {ins,del,upd}
    static final int NUM_TRIGS       = NUM_TRIGGER_OPS * 2 * 2;    // {b, a},{fer, fes}

    // other variables
    HsqlName name;
    String   when;
    String   operation;
    boolean  forEachRow;
    boolean  nowait;                                               // block or overwrite if queue full
    int      maxRowsQueued;                                        // max size of queue of pending triggers

    /**
     *  Retrieves the queue size assigned to trigger definitions when no
     *  queue size is explicitly declared. <p>
     *
     * @return the queue size assigned to trigger definitions when no
     *      queue size is explicitly declared
     */
    public static int getDefaultQueueSize() {
        return defaultQueueSize;
    }

    protected static int defaultQueueSize = 1024;
    Table                table;
    Trigger              trigger;
    String               triggerClassName;
    int                  vectorIndex;               // index into HsqlArrayList[]

    //protected boolean busy;               // firing trigger in progress
    protected HsqlDeque        pendingQueue;        // row triggers pending
    protected int              rowsQueued;          // rows in pendingQueue
    protected boolean          valid     = true;    // parsing valid
    protected volatile boolean keepGoing = true;

    /**
     *  Constructs a new TriggerDef object to represent an HSQLDB trigger
     *  declared in an SQL CREATE TRIGGER statement.
     *
     *  Changes in 1.7.2 allow the queue size to be specified as 0. A zero
     *  queue size causes the Trigger.fire() code to run in the main thread of
     *  execution (fully inside the enclosing transaction). Otherwise, the code
     *  is run in the Trigger's own thread.
     *  (fredt@users)
     *
     * @param  name The trigger object's HsqlName
     * @param  when the String representation of whether the trigger fires
     *      before or after the triggering event
     * @param  operation the String representation of the triggering operation;
     *      currently insert, update, or delete
     * @param  forEach indicates whether the trigger is fired for each row
     *      (true) or statement (false)
     * @param  table the Table object upon which the indicated operation
     *      fires the trigger
     * @param  triggerClassName the fully qualified named of the class implementing
     *      the org.hsqldb.Trigger (trigger body) interface
     * @param  noWait do not wait for available space on the pending queue; if
     *      the pending queue does not have fewer than nQueueSize queued items,
     *      then overwrite the current tail instead
     * @param  queueSize the length to which the pending queue may grow before
     *      further additions are either blocked or overwrite the tail entry,
     *      as determined by noWait
     * @throws HsqlException - Invalid input parameter
     */
    public TriggerDef(HsqlNameManager.HsqlName name, String when,
                      String operation, boolean forEach, Table table,
                      String triggerClassName, boolean noWait, int queueSize,
                      ClassLoader loader) throws HsqlException {

        this.name             = name;
        this.when             = when;
        this.operation        = operation;
        this.forEachRow       = forEach;
        this.nowait           = noWait;
        this.maxRowsQueued    = queueSize;
        this.table            = table;
        vectorIndex           = SqlToIndex();
        this.triggerClassName = triggerClassName;
        rowsQueued            = 0;
        pendingQueue          = new HsqlDeque();

        if (vectorIndex < 0) {
            throw Trace.error(Trace.UNEXPECTED_TOKEN,
                              Trace.CREATE_TRIGGER_COMMAND_1);
        }

        Class cl;

        try {
            cl = loader == null ? Class.forName(triggerClassName)
                                : loader.loadClass(triggerClassName);
        } catch (ClassNotFoundException e) {
            valid = false;
            cl    = DefaultTrigger.class;
        }

        try {

            // dynamically instantiate it
            trigger = (Trigger) cl.newInstance();
        } catch (Exception e) {
            valid = false;
            cl    = DefaultTrigger.class;
        }
    }

    /**
     *  Retrieves the SQL character sequence required to (re)create the
     *  trigger, as a StringBuffer
     *
     * @return the SQL character sequence required to (re)create the
     *  trigger
     */
    public StringBuffer getDDL() {

        StringBuffer a = new StringBuffer(256);

        a.append(Token.T_CREATE).append(' ');
        a.append(Token.T_TRIGGER).append(' ');
        a.append(name.statementName).append(' ');
        a.append(when).append(' ');
        a.append(operation).append(' ');
        a.append(Token.T_ON).append(' ');
        a.append(table.getName().statementName).append(' ');

        if (forEachRow) {
            a.append(Token.T_FOR).append(' ');
            a.append(Token.T_EACH).append(' ');
            a.append(Token.T_ROW).append(' ');
        }

        if (nowait) {
            a.append(Token.T_NOWAIT).append(' ');
        }

        if (maxRowsQueued != getDefaultQueueSize()) {
            a.append(Token.T_QUEUE).append(' ');
            a.append(maxRowsQueued).append(' ');
        }

        a.append(Token.T_CALL).append(' ');
        a.append(StringConverter.toQuotedString(triggerClassName, '"',
                false));

        return a;
    }

    /**
     *  SqlToIndex method declaration <P>
     *
     *  Given the SQL creating the trigger, say what the index to the
     *  HsqlArrayList[] is
     *
     * @return  index to the HsqlArrayList[]
     */
    public int SqlToIndex() {

        int indx;

        if (operation.equals(Token.T_INSERT)) {
            indx = Trigger.INSERT_AFTER;
        } else if (operation.equals(Token.T_DELETE)) {
            indx = Trigger.DELETE_AFTER;
        } else if (operation.equals(Token.T_UPDATE)) {
            indx = Trigger.UPDATE_AFTER;
        } else {
            return -1;
        }

        if (when.equals(Token.T_BEFORE)) {
            indx += NUM_TRIGGER_OPS;    // number of operations
        } else if (!when.equals(Token.T_AFTER)) {
            return -1;
        }

        if (forEachRow) {
            indx += 2 * NUM_TRIGGER_OPS;
        }

        return indx;
    }

    public static int indexToRight(int idx) {

        switch (idx) {

            case Trigger.DELETE_AFTER :
            case Trigger.DELETE_AFTER_ROW :
            case Trigger.DELETE_BEFORE :
            case Trigger.DELETE_BEFORE_ROW :
                return UserManager.DELETE;

            case Trigger.INSERT_AFTER :
            case Trigger.INSERT_AFTER_ROW :
            case Trigger.INSERT_BEFORE :
            case Trigger.INSERT_BEFORE_ROW :
                return UserManager.INSERT;

            case Trigger.UPDATE_AFTER :
            case Trigger.UPDATE_AFTER_ROW :
            case Trigger.UPDATE_BEFORE :
            case Trigger.UPDATE_BEFORE_ROW :
                return UserManager.UPDATE;

            default :
                return 0;
        }
    }

    /**
     *  run method declaration <P>
     *
     *  the trigger JSP is run in its own thread here. Its job is simply to
     *  wait until it is told by the main thread that it should fire the
     *  trigger.
     */
    public void run() {

        while (keepGoing) {
            TriggerData triggerData = popPair();

            if (triggerData != null) {
                if (triggerData.username != null) {
                    trigger.fire(this.vectorIndex, name.name,
                                 table.getName().name, triggerData.oldRow,
                                 triggerData.newRow);
                }
            }
        }
    }

    /**
     * start the thread if this is threaded
     */
    public synchronized void start() {

        if (maxRowsQueued != 0) {
            super.start();
        }
    }

    /**
     * signal the thread to stop
     */
    public synchronized void terminate() {

        keepGoing = false;

        notify();
    }

    /**
     *  pop2 method declaration <P>
     *
     *  The consumer (trigger) thread waits for an event to be queued <P>
     *
     *  <B>Note: </B> This push/pop pairing assumes a single producer thread
     *  and a single consumer thread _only_.
     *
     * @return  Description of the Return Value
     */
    synchronized TriggerData popPair() {

        if (rowsQueued == 0) {
            try {
                wait();    // this releases the lock monitor
            } catch (InterruptedException e) {

                /* ignore and resume */
            }
        }

        rowsQueued--;

        notify();    // notify push's wait

        if (pendingQueue.size() == 0) {
            return null;
        } else {
            return (TriggerData) pendingQueue.removeFirst();
        }
    }

    /**
     *  The main thread tells the trigger thread to fire by this call.
     *  If this Trigger is not threaded then the fire method is caled
     *  immediately and executed by the main thread. Otherwise, the row
     *  data objects are added to the queue to be used by the Trigger thread.
     *
     * @param  row1
     * @param  row2
     */
    synchronized void pushPair(Session session, Object[] row1,
                               Object[] row2) {

        if (maxRowsQueued == 0) {
            trigger.fire(vectorIndex, name.name, table.getName().name, row1,
                         row2);

            return;
        }

        if (rowsQueued >= maxRowsQueued) {
            if (nowait) {
                pendingQueue.removeLast();    // overwrite last
            } else {
                try {
                    wait();
                } catch (InterruptedException e) {

                    /* ignore and resume */
                }

                rowsQueued++;
            }
        } else {
            rowsQueued++;
        }

        pendingQueue.add(new TriggerData(session, row1, row2));
        notify();    // notify pop's wait
    }

    /**
     *  Method declaration
     *
     * @return
     */
    public boolean isBusy() {
        return rowsQueued != 0;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Class to store the data used to fire a trigger. The username attribute
     * is not used but it allows developers to change the signature of the
     * fire method of the Trigger class and pass the user name to the Trigger.
     */
    class TriggerData {

        public Object[] oldRow;
        public Object[] newRow;
        public String   username;

        public TriggerData(Session session, Object[] oldRow,
                           Object[] newRow) {

            this.oldRow   = oldRow;
            this.newRow   = newRow;
            this.username = session.getUsername();
        }
    }

    static class DefaultTrigger implements org.hsqldb.Trigger {

        public void fire(int i, String name, String table, Object[] row1,
                         Object[] row2) {
            throw new RuntimeException("Missing Trigger class!");
        }
    }
}
