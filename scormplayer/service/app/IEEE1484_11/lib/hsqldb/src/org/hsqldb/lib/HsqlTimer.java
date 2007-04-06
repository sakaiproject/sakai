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


package org.hsqldb.lib;

import java.util.Date;

/**
 * Provides facility for threads to schedule tasks for future execution in a
 * background thread.  Tasks may be scheduled for one-time execution, or for
 * repeated execution at regular intervals.  This class is a JDK 1.1 compatible
 * implementation required by HSQLDB because the java.util.Timer class is
 * available only in JDK 1.3+.
 *
 * @author boucherb@users
 * @version 1.7.2
 * @since 1.7.2
 */
public class HsqlTimer implements ObjectComparator {

    /** The priority queue for the scheduled tasks. */
    protected final TaskQueue taskQueue = new TaskQueue(16,
        (ObjectComparator) this);

    /** The inner runnable that executes tasks in the background thread. */
    protected final TaskRunner taskRunner = new TaskRunner();

    /** The background thread. */
    protected Thread taskRunnerThread;

    /** The factory that procduces the background threads. */
    protected ThreadFactory threadFactory;

    /**
     * Constructs a new HsqlTimer using the default thread factory
     * implementation.
     */
    public HsqlTimer() {
        this(null);
    }

    /**
     * Constructs a new HsqlTimer using the specified thread factory
     * implementation.
     *
     * @param tf the ThreadFactory used to produce the background threads.
     *      If null, the implementation supplied by HsqlThreadFactory will
     *      be used.
     */
    public HsqlTimer(ThreadFactory tf) {
        threadFactory = new HsqlThreadFactory(tf);
    }

    /**
     * ObjectComparator implemtation required to back priority queue
     * for scheduled tasks.
     *
     * @param a the first Task
     * @param b the second Task
     * @return 0 if equal, < 0 if a < b, > 0 if a > b
     */
    public int compare(Object a, Object b) {

        long awhen;
        long bwhen;

        awhen = ((Task) (a)).getNextScheduled();
        bwhen = ((Task) (b)).getNextScheduled();

        // must return an int, so (awhen - bwhen)
        // might not be that great... (:-(
        // under realistic use (scheduled times in this era ;-),
        // awhen - bwhen is fine
        // return (awhen < bwhen) ? -1 : awhen == bwhen ? 0: 1;
        return (int) (awhen - bwhen);
    }

    /**
     * Retrieves the background thread that is currently being used to
     * execute submitted tasks.  null is returned if there is no such thread.
     *
     * @return the current background thread or null
     */
    public synchronized Thread getThread() {
        return taskRunnerThread;
    }

    /**
     * (Re)starts background processing of the task queue.
     */
    public synchronized void restart() {

        if (taskRunnerThread == null) {
            taskRunnerThread = threadFactory.newThread(taskRunner);

            taskRunnerThread.setName("HSQLDB Timer @"
                                     + Integer.toHexString(this.hashCode()));
            taskRunnerThread.setDaemon(true);
            taskRunnerThread.start();
        } else {
            notify();
        }
    }

    /**
     * Causes the specified Runnable to be executed once in the background
     * after the specified delay.
     *
     * @param delay in milliseconds
     * @param r the Runnable to execute.
     * @return opaque reference to the internal task
     */
    public Object scheduleAfter(long delay, Runnable r) {
        return addTask(now() + delay, r, 0, false);
    }

    /**
     * Causes the specified Runnable to be executed once in the background
     * at the specified time.
     *
     * @param date time at which to execute the specified Runnable
     * @param r the Runnable to execute.
     * @return opaque reference to the internal task
     */
    public Object scheduleAt(Date date, Runnable r) {
        return addTask(date.getTime(), r, -1, false);
    }

    /**
     * Causes the specified Runnable to be executed periodically in the
     * background, starting at the specified time.
     *
     * @return opaque reference to the internal task
     * @param p the cycle period
     * @param relative if true, fixed rate sheduling else fixed period scheduling
     * @param date time at which to execute the specified Runnable
     * @param r the Runnable to execute
     */
    public Object schedulePeriodicallyAt(Date date, long p, Runnable r,
                                         boolean relative) {

        if (p <= 0) {
            throw new IllegalArgumentException();
        }

        return addTask(date.getTime(), r, p, relative);
    }

    /**
     * Causes the specified Runnable to be executed periodically in the
     * background, starting after the specified delay.
     *
     * @return opaque reference to the internal task
     * @param p the cycle period
     * @param relative if true, fixed rate sheduling else fixed period scheduling
     * @param delay in milliseconds
     * @param r the Runnable to execute.
     */
    public Object schedulePeriodicallyAfter(long delay, long p, Runnable r,
            boolean relative) {

        if (p <= 0) {
            throw new IllegalArgumentException();
        }

        return addTask(now() + delay, r, p, relative);
    }

    /**
     * Causes all pending tasks to be cancelled and then stops background
     * processing.
     */
    public synchronized void shutDown() {

        taskQueue.clear();

        if (taskRunnerThread != null) {
            taskRunnerThread.interrupt();
        }

        taskRunnerThread = null;
    }

    /**
     * Causes the task referenced by the supplied argument to be cancelled.
     * If the referenced task is currently executing, it will continue until
     * finished but will not be rescheduled.
     *
     * @param task a task reference
     * @exception ClassCastException if the task argument cannot be cast
     *      to the type of reference returned by a scheduleXXX method
     *      invocation.
     */
    public static void cancel(Object task) throws ClassCastException {

        if (task != null) {
            ((Task) task).cancel();

//            Trace.printSystemOut("HsqlTimer now() calls: " + nowCount);
        }
    }

    /**
     * Retrieves whether the specified argument references a cancelled task.
     *
     * @param task a task reference
     * @return true if referenced task is cancelled
     * @exception ClassCastException if the task argument cannot be cast
     *      to the type of reference returned by a scheduleXXX method
     *      invocation.
     */
    public static boolean isCancelled(Object task) throws ClassCastException {
        return task == null ? true
                            : ((Task) task).isCancelled();
    }

    /**
     * Retrieves whether the specified argument references a task scheduled
     * periodically using fixed rate scheduling.
     *
     * @param task a task reference
     * @return true if the task is scheduled at a fixed rate
     * @exception ClassCastException if the task argument cannot be cast
     *      to the type of reference returned by a scheduleXXX method
     *      invocation.
     */
    public static boolean isFixedRate(Object task) throws ClassCastException {

        return task == null ? false
                            : ((Task) task).relative
                              && ((Task) task).period > 0;
    }

    /**
     * Retrieves whether the specified argument references a task scheduled
     * periodically using fixed delay scheduling.
     *
     * @param task a task reference
     * @return if the task is scheduled using a fixed rate
     * @exception ClassCastException if the task argument cannot be cast
     *      to the type of reference returned by a scheduleXXX method
     *      invocation.
     */
    public static boolean isFixedDelay(Object task)
    throws ClassCastException {

        return task == null ? false
                            : !((Task) task).relative
                              && ((Task) task).period > 0;
    }

    /**
     * Retrieves whether the specified argument references a task scheduled
     * for periodic execution.
     *
     * @param task a task reference
     * @return true ifthe task is scheduled for periodic execution
     * @exception ClassCastException if the task argument cannot be cast
     *      to the type of reference returned by a scheduleXXX method
     *      invocation.
     */
    public static boolean isPeriodic(Object task) throws ClassCastException {
        return task == null ? false
                            : ((Task) task).period != 0;
    }

    /**
     * Retrieves the last time the referenced task was executed, as a
     * Date object. If the task has never been executed, null is returned.
     *
     * @param task a task reference
     * @return the last time the referenced task was executed
     * @exception ClassCastException if the task argument cannot be cast
     *      to the type of reference returned by a scheduleXXX method
     *      invocation.
     */
    public static Date getLastScheduled(Object task)
    throws ClassCastException {

        long last;

        last = task == null ? 0
                            : ((Task) task).getLastScheduled();

        return last == 0 ? null
                         : new Date(last);
    }

    /**
     * Resets the period for a task.
     *
     * @param task a task reference
     * @param period new period
     * @exception ClassCastException if the task argument cannot be cast
     *      to the type of reference returned by a scheduleXXX method
     *      invocation.
     */
    public static void setPeriod(Object task,
                                 long period) throws ClassCastException {

        if (task == null) {
            return;
        }

        ((Task) task).setPeriod(period);
    }

    /**
     * Retrieves the next time the referenced task is due to be executed, as a
     * Date object. If the task has been cancelled, null is returned.
     *
     * @param task a task reference
     * @return the next time the referenced task is due to be executed
     * @exception ClassCastException if the task argument cannot be cast
     *      to the type of reference returned by a scheduleXXX method
     *      invocation.
     */
    public static Date getNextScheduled(Object task)
    throws ClassCastException {
        return isCancelled(task) ? null
                                 : new Date(((Task) task).getNextScheduled());
    }

    /**
     * Adds to the task queue a new Task object encapsulating the supplied
     * Runnable and scheduling arguments.
     *
     * @param n the time of the first execution
     * @param r the Runnable to execute
     * @param p the periodicity
     * @param b if true, use fixed rate else use fixed period
     * @return a reference to the scheduled task
     */
    protected Task addTask(long n, Runnable r, long p, boolean b) {

        Task task;

        task = new Task(n, r, p, b);

        // sychronized
        taskQueue.add(task);

        // sychronized
        restart();

        return task;
    }

    /** Sets the background thread to null. */
    protected synchronized void clearThread() {
        taskRunnerThread = null;
    }

    /**
     * Retrieves the next task to execute, or null if the background thread
     * is interrupted.
     *
     * @return the next task to execute, or null
     */
    protected synchronized Task nextTask() {

        Task    task;
        long    now;
        long    last;
        long    next;
        long    late;
        long    period;
        boolean relative;

        try {
            while (!Thread.interrupted()) {
                task = (Task) (taskQueue.peek());

                if (task == null) {
                    wait();
                } else {
                    now  = now();
                    next = task.getNextScheduled();

                    if (next > now) {
                        wait(next - now);
                    } else {
                        task = (Task) taskQueue.remove();

                        if (task != null &&!task.isCancelled()) {
                            period = task.period;

                            if (period > 0) {
                                now = now();

                                if (task.relative) {
                                    late = now - next;

                                    if (late > 0) {
                                        period -= late;
                                    }
                                }

                                next = now + period;

                                task.setNextScheduled(next);
                                taskQueue.add(task);
                            }

                            return task;
                        }
                    }
                }
            }
        } catch (InterruptedException e) {

            // e.printStackTrace()
        }

        // interrupted
        return null;
    }

    static int nowCount = 0;

    /**
     * Convenience method replacing the longer incantation:
     * System.currentTimeMillis()
     *
     * @return System.currentTimeMillis()
     */
    private static long now() {

        nowCount++;

        return System.currentTimeMillis();
    }

    /**
     * The Runnable that the background thread uses to execute
     * scheduled tasks. <p>
     *
     * <b>Note:</b> Outer class could simply implement Runnable,
     * but using an inner class protects the public run method
     * from potential abuse.
     */
    protected class TaskRunner implements Runnable {

        public void run() {

            Task task;

            try {
                do {
                    task = HsqlTimer.this.nextTask();

                    if (task == null) {
                        break;
                    }

                    task.setLastScheduled(now());
                    task.runnable.run();
                } while (true);
            } finally {
                HsqlTimer.this.clearThread();
            }
        }
    }

    /**
     * A wrapper class used to schedule a Runnable object
     * for execution by the enclosing HsqlTimer's TaskRunner in a
     * background thread.
     */
    protected class Task {

        /** What to run */
        final Runnable runnable;

        /** The periodic interval, or 0 if one-shot */
        long period;

        /** The time this task was last executed, or 0 if never */
        private long last;

        /** The next time this task is scheduled to execute */
        private long next;

        /**
         * Whether to remove this task instead of running it
         * the next time it makes its way to the head of the
         * timer queue.
         */
        private boolean cancelled = false;

        /** protect the cancelled field under concurrent access */
        private Object cancel_mutex = new Object();

        /**
         * Whether periodic task is sheduled using fixed delay or fixed rate.
         *
         * When true, scheduling is fixed rate as opposed to fixed delay
         * and nextScheduled is calculated relative to when the task was
         * was last run rather than a fixed delay starting from
         * the current wall-clock time provided by
         * System.currentTimeMillis().  This helps tasks that must attempt
         * to maintain a fixed rate of execution under Java's approximate
         * wait() and Thread.sleep(millis).
         */
        final boolean relative;

        /**
         * Constructs a new Task object encapulating the specified Runnable
         * and scheduling arguments.
         *
         * @param n the next time to execute
         * @param r the Runnable to execute
         * @param p the periodicity of execution
         * @param b if true, use fixed rate scheduling else fixed period
         */
        Task(long n, Runnable r, long p, boolean b) {

            last     = 0;
            next     = n;
            runnable = r;
            period   = p;
            relative = b;
        }

        /** Sets this task's cancelled flag true. */
        void cancel() {

            synchronized (cancel_mutex) {
                cancelled = true;
            }
        }

        /**
         * Retrieves whether this task is cancelled.
         *
         * @return true if cancelled, else false
         */
        boolean isCancelled() {

            synchronized (cancel_mutex) {
                return cancelled;
            }
        }

        /**
         * Retrieves the instant in time just before this task was
         * last executed by the background thread. A value of zero
         * indicates that this task has never been executed.
         *
         * @return the last time this task was executed or zero if never
         */
        synchronized long getLastScheduled() {
            return last;
        }

        /**
         * Sets the time at which this task reports it was last executed.
         *
         * @param l the new value for the last executed attribute
         */
        synchronized void setLastScheduled(long l) {
            last = l;
        }

        /**
         * Retrieves the time at which this task is next scheduled for
         * execution.
         *
         * @return the time at which this task is next scheduled for
         *      execution
         */
        synchronized long getNextScheduled() {
            return next;
        }

        /**
         * Sets the new time at which this task is next scheduled for
         * execution.
         *
         * @param n the new time at which this task is next scheduled for
         *      execution
         */
        synchronized void setNextScheduled(long n) {
            next = n;
        }

        /**
         * Sets the period to wait.
         *
         * @param n the new period
         */
        synchronized void setPeriod(long n) {
            period = n;
        }
    }

    /**
     * Extends HsqlArrayHeap to allow all pending tasks to be cancelled when
     * the queue is cleared.  Currently, this is done for reporting purposes
     * only, as there is no public interface to reinsert Task objects after
     * they have been removed.
     */
    protected class TaskQueue extends HsqlArrayHeap {

        /**
         * Constructs a new TaskQueue with the specified initial capacity and
         * ObjectComparator.
         *
         * @param capacity the initial capacity of the queue
         * @param oc The ObjectComparator this queue uses to maintain its
         *      Heap invariant.
         */
        TaskQueue(int capacity, ObjectComparator oc) {
            super(capacity, oc);
        }

        /** Cancels all pending tasks and removes them from this queue. */
        public synchronized void clear() {

            for (int i = 0; i < count; i++) {
                ((Task) heap[i]).cancel();

                heap[i] = null;
            }

            count = 0;
        }
    }

// ---------------------------------- tests ------------------------------------
//    static class TestTask implements Runnable {
//        String      name;
//        HsqlTimer   timer;
//        Object      tid;
//        int         runs = 0;
//        long        last;
//        long        total = 0;
//        Thread      sleeper;
//
//        TestTask(String name) {
//            this.name = name;
//            try {
//                System.runFinalizersOnExit(true);
//            } catch (Exception e) {
//
//            }
//        }
//
//        public void run() {
//            System.out.println(this);
//            if (timer.getLastScheduled(tid) == null) {
//                System.out.println("no last sched.");
//            } else {
//                runs++;
//                if (runs == 1) {
//                    last = now();
//                } else {
//                    long now = now();
//                    total += (now - last);
//                    last = now;
//                    System.out.println("runs: " + (runs -1));
//                    System.out.println("totl: " + total);
//                    System.out.flush();
//                }
//            }
//            System.out.println("------------------");
//            if (runs == 10) {
//                timer.shutDown();
//                sleeper.interrupt();
//            }
//        }
//
//        public String toString() {
//            return name;
//        }
//
//        protected void printstats() {
//            System.out.println(this + " avg. latency: " + (total/(runs-1)));
//        }
//    }
//
//    public static void main(String[] args) {
//        HsqlTimer timer;
//        TestTask tt1;
//        TestTask tt2;
//        Thread   sleeper;
//
//        timer = new HsqlTimer();
//
//        // need this to run tests now, since
//        // taskRunnerThread is now daemon.
//        // Otherwise, timer thread exits
//        // immediately when this thread exits.
//        sleeper = new Thread() {
//            public void run() {
//                try {
//                    sleep(Long.MAX_VALUE);
//                } catch (Exception e) {
//                    // do nothing
//                }
//            }
//        };
//
//        sleeper.start();
//
//        Runnable r = new Runnable() {
//            long x;
//            int runs = 0;
//            public void run() {
//                for (int i = 0; i < 1000000; i++) {
//                    x = (long) ((x + 6) * (double)12) - 100;
//                }
//            }
//        };
//
//        tt2 = new TestTask("Task 2");
//        tt2.timer = timer;
//        tt2.sleeper = sleeper;
//        tt2.tid = (Task) timer.schedulePeriodicallyAfter(0, 500, tt2, false);
//
//        tt1 = new TestTask("Task 1");
//        tt1.timer = timer;
//        tt1.sleeper = sleeper;
//        tt1.tid = (Task) timer.schedulePeriodicallyAfter(0, 500, tt1, true);
//
//        timer.schedulePeriodicallyAfter(0,1, r, false);
//
//        try {
//            sleeper.join();
//        } catch (Exception e) {}
//
//        tt1.printstats();
//        tt2.printstats();
//
//    }
}
