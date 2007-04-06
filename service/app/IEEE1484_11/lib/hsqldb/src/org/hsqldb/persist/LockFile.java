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


package org.hsqldb.persist;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;

import org.hsqldb.DatabaseManager;
import org.hsqldb.HsqlDateTime;
import org.hsqldb.HsqlException;
import org.hsqldb.Trace;
import org.hsqldb.lib.FileUtil;
import org.hsqldb.lib.HsqlTimer;
import org.hsqldb.lib.java.JavaSystem;

/**
 * The base HSQLDB cooperative file locking implementation and factory. <p>
 *
 * <hr>
 *
 * Here is the way this class operates: <p>
 *
 * <ol>
 * <li>A file with a well-known path relative to each database instance
 *     is used to implement cooperative locking of database files across
 *     process boundaries (database instances running in different JVM
 *     host processes) and class loader contexts (databases whose classes
 *     have been loaded by distinct class loaders such that their open
 *     database repositories are distinct and are inaccessible across
 *     the class loader context boundaries).<p>
 *
 * <li>A background thread periodically writes a timestamp to this object's
 *     lock file at {@link #HEARTBEAT_INTERVAL} millisecond intervals,
 *     acting as a heartbeat to indicate that a lock is still held.<p>
 *
 * <li>The generic lock attempt rules are: <p>
 *    <ul>
 *    <li>If a lock condition is already held by this object, do nothing and
 *        signify that the lock attempt was successful, else...<p>
 *
 *    <li>If no lock file exists, go ahead and create one, silently issue the
 *        {@link java.io.File#deleteOnExit File.deleteOnExit()} directive via
 *        refelective method invocation (in order to stay JDK 1.1 compliant),
 *        schedule a periodic heartbeat task and signify that the lock attempt
 *        was successful, else...<p>
 *
 *    <li>The lock file must already exist, so try to read its heartbeat
 *        timestamp. If the read fails, assume that a lock condition is held by
 *        another process or a database in an inaccessible class loader context
 *        and signify that the attempt failed, else if the read value
 *        is less than <code>HEARTBEAT_INTERVAL</code> milliseconds into the
 *        past or futuer, assume that a lock condition is held by another
 *        process or a database in an inaccessible class loader context and
 *        signify that the lock attempt failed, else assume that the file is
 *        not in use, schedule a periodic heartbeat task and signify that the
 *        lock attempt was successful.<p>
 *
 *    </ul>
 * <li>The generic release attempt rules are:<p>
 *    <ul>
 *    <li>If a lock condition is not currently held, do nothing and signify
 *        that the release attempt was successful, else...<p>
 *
 *    <li>A lock condition is currently held, so try to release it.  By
 *        default, releasing the lock condition consists of closing and
 *        nullifying any objects that have a file descriptor open on the
 *        lock file. If the release is successful, cancel the periodic
 *        heartbeat task and signify that the release succeeded, else signify
 *        that the release attempt failed.<p>
 *    </ul>
 * </ol> <p>
 *
 * In addition to the generic lock and release rules, the protected methods
 * {@link #lockImpl() lockImpl()} and {@link #releaseImpl() releaseImpl()}
 * are called during lock and release attempts, respectively.  This allows
 * transparent, JDK 1.1 compliant integration of extended strategies for
 * locking and releasing, based on subclassing and reflective construction
 * of such specializations in the factory method
 * {@link #newLockFile newLockFile()}, determined by information gathered
 * at run-time. <p>
 *
 * In particular, if it is available at runtime, then newLockFile() retrieves
 * instances of {@link org.hsqldb.NIOLockFile  NIOLockFile} to capitalize,
 * when possible, on the existence of the {@link java.nio.channels.FileLock
 * FileLock} class. If the <code>NIOLockFile</code> class does not exist at
 * run-time or the java.nio classes it uses are not supported under the
 * run-time JVM, then newLockFile() produces vanilla LockFile instances,
 * meaning that only purely cooperative locking takes place, as opposed to
 * possibly O/S-enforced file locking which, at least in theory, is made
 * available through the {@link java.nio.channels} package). However, it
 * must be noted that even if a JVM implementation provides the full
 * java.nio.channels package, it is not absolutely required to guarantee
 * that the underlying platform (the current operating system) provides
 * true process-wide file locking. <p>
 *
 * <b>Note:</b> <p>
 *
 * The <code>NIOLockFile</code> descendent exists because it has been determined
 * though experimenatation that <code>java.nio.channels.FileLock</code>
 * does not always exhibit the correct/desired behaviour under reflective
 * method invocation. That is, it has been discovered that under some operating
 * system/JVM combinations, after calling <code>FileLock.release()</code>
 * via a reflective method invocation, the lock is not released properly,
 * deletion of the lock file is not possible even from the owning object
 * (this) and it is impossible for other <code>LockFile</code> instances
 * or any other objects or processes to successfully obtain a lock
 * condition on the lock file, despite the fact that the <code>FileLock</code>
 * object reports that its lock is invalid (was released successfully).
 * Frustratingly, this condition appears to persist until full exit of the
 * JVM process in which the <code>FileLock.tryLock()</code> method was
 * reflectively invoked. <p>
 *
 * To solve this, the original <code>LockFile</code> class was split in two and
 * instead of reflective method invocation, reflection-based class
 * instantiation is now performed at the level of the <code>newLockFile()</code>
 * factory method. Similarly, the HSQLDB ANT build script detects the presence
 * or abscence of JDK 1.4 features such as java.nio and only attempts to build
 * and deploy <code>NIOLockFile</code> to the hsqldb.jar if such features are
 * reported present. </p>
 *
 * @author boucherb@users
 * @version 1.7.2
 * @since 1.7.2
 */
public class LockFile {

    /** Canonical reference to this object's lock file. */
    protected File f;

    /** Cached value of the lock file's canonical path. */
    private String cpath = null;

    /**
     * A RandomAccessFile constructed from this object's reference, f, to its
     * lock file. <p>
     *
     * This RandomAccessFile is used to periodically write out the heartbeat
     * timestamp to this object's lock file.
     */
    protected RandomAccessFile raf;

    /**
     * The period, in milliseconds, at which heartbeat timestamps are written
     * to this object's lock file.
     */
    public static final long HEARTBEAT_INTERVAL = 10000;

    /**
     * A magic value to place at the beginning of the lock file to
     * differentiate it from other files. The value is "HSQLLOCK".getBytes().
     */
    public static final byte[] MAGIC = "HSQLLOCK".getBytes();

    /** Indicates whether this object has a lock condition on its lock file. */
    protected boolean locked;

    /**
     * The timed scheduler with which to register this object's
     * heartbeat task.
     */
    protected static final HsqlTimer timer = DatabaseManager.getTimer();

    /**
     * An opaque reference to this object's heatbeat task.
     */
    private Object timerTask;

    /**
     * Attempts to read the hearbeat timestamp from this object's lock file
     * and compare it with the current time. <p>
     *
     * An exception is thrown if it must be presumned that another process has
     * locked the file, using the following rules: <p>
     *
     * <ol>
     * <li>If the file does not exist, this method returns immediately.
     *
     * <li>If an exception is raised reading the file, then an exeption is
     *     thrown.
     *
     * <li>If the read is successful and the timestamp read in is less than
     *     <code>HEARTBEAT_INTERVAL</code> milliseconds into the past or
     *     future, then an exception is thrown.
     *
     * <li>If no exception is thrown in 2.) or 3.), this method simply returns.
     * </ol>
     * @throws Exception if it must be presumed that another process
     *        or isolated class loader context currently has a
     *        lock condition on this object's lock file
     */
    private void checkHeartbeat() throws Exception {

        long   lastHeartbeat;
        String mn;
        String path;

        mn   = "checkHeartbeat(): ";
        path = "lock file [" + cpath + "]";

        trace(mn + "entered.");

        if (!f.exists()) {
            trace(mn + path + " does not exist. Check OK.");

            return;
        }

        if (f.length() != 16) {
            trace(mn + path + " length != 16; Check OK.");

            return;
        }

        try {
            lastHeartbeat = System.currentTimeMillis() - readHeartbeat();
        } catch (Exception e) {

            // e.printStackTrace();
            throw new Exception(
                Trace.getMessage(
                    Trace.LockFile_checkHeartbeat, true, new Object[] {
                e.toString(), cpath
            }));
        }

        trace(mn + path + " last heartbeat " + lastHeartbeat + " ms ago.");

        if (Math.abs(lastHeartbeat) < HEARTBEAT_INTERVAL) {
            throw new Exception(
                Trace.getMessage(
                    Trace.LockFile_checkHeartbeat2, true, new Object[] {
                mn, path
            }));
        }
    }

    /**
     * Closes this object's {@link #raf RandomAccessFile}.
     *
     * @throws Exception if an IOException occurs
     */
    private void closeRAF() throws Exception {

        String mn;

        mn = "closeRAF(): ";

        trace(mn + "entered.");

        if (raf == null) {
            trace(mn + "raf was null upon entry. Exiting immediately.");
        } else {
            trace(mn + "closing " + raf);
            raf.close();
            trace(mn + raf + " closed successfully. Setting raf null");

            raf = null;
        }
    }

    /**
     * Initializes this object with the specified <code>File</code>
     * object. <p>
     *
     * The file argument is a reference to this object's lock file. <p>
     *
     * This action has the effect of attempting to release any existing
     * lock condition and reinitializing all lock-related member attributes
     * @param file a reference to the file this object is to use as its
     *      lock file
     */
    private void setFile(File file) throws Exception {

        if (isLocked()) {
            try {
                tryRelease();
            } catch (Exception e) {
                trace(e);
            }
        }

        f      = FileUtil.canonicalFile(file);
        cpath  = f.getPath();
        raf    = null;
        locked = false;
    }

    /**
     * Provides any specialized locking actions for the
     * {@link #tryLock() tryLock()} method. <p>
     *
     * Descendents are free to provide additional functionality here,
     * using the following rules:
     *
     * <pre>
     * PRE:
     *
     * This method is only called if tryLock() thinks it needs to get a lock
     * condition, so it can be assumed the locked == false upon entry, raf is
     * a non-null instance that can be used to get a FileChannel if desired,
     * and the lock file is, at the very least, readable.  Further, this
     * object's heatbeat task is definitely cancelled and/or has not yet been
     * scheduled, so whatever timestamp is recorded in the lock file, if it
     * exists, is what was written by a previous locker, if any.  A timestamp
     * value in a preexisting file is only considered valid if the file is
     * of the correct length and its first eight bytes are
     * the value {@link #MAGIC MAGIC}.
     *
     * POST:
     *
     * This method must return false if any additional locking work fails,
     * else true.
     * </pre>
     *
     * The default implementation of this method reflectively (for JDK1.1
     * compliance) invokes f.deleteOnExit() in a silent manner and always
     * returns true. <p>
     *
     * @throws Exception if a situation is encountered that absolutely
     *        prevents the status of the lock condtion
     *        to be determined. (e.g. an IO exception
     *        occurs here)
     * @return <code>true</code> if no extended locking
     *        actions are taken or the actions succeed,
     *        else <code>false</code>.
     */
    protected boolean lockImpl() throws Exception {

        String mn;

        mn = "lockImpl(): ";

        trace(mn + "entered.");
        FileUtil.deleteOnExit(f);

        return true;
    }

    /**
     * Opens this object's {@link #raf RandomAccessFile}. <p>
     *
     * @throws Exception if an IOException occurs
     */
    private void openRAF() throws Exception {

        trace("openRAF(): entered.");

        raf = new RandomAccessFile(f, "rw");

        trace("openRAF(): got new 'rw' mode " + raf);
    }

    /**
     * Retrieves the last written hearbeat timestamp from
     * this object's lock file.  If this object's lock file
     * does not exist, <code>Long.MIN_VALUE</code> (the earliest
     * time representable as a long in Java) is retrieved. <p>
     *
     * @throws Exception if an error occurs while reading the hearbeat
     *      timestamp from this object's lock file.
     * @return the hearbeat timestamp from this object's lock file,
     *      as a <code>long</code> value or, if this object's lock
     *      file does not exist, Long.MIN_VALUE, the earliest time
     *      representable as a long in Java,
     */
    private long readHeartbeat() throws Exception {

        DataInputStream dis;
        long            heartbeat;

        heartbeat = Long.MIN_VALUE;

        String mn   = "readHeartbeat(): ";
        String path = "lock file [" + cpath + "]";

        trace(mn + "entered.");

        if (!f.exists()) {
            trace(mn + path + " does not exist. Return  '" + heartbeat + "'");

            return heartbeat;
        }

        dis = new DataInputStream(new FileInputStream(f));

        trace(mn + " got new " + dis);

        for (int i = 0; i < MAGIC.length; i++) {
            if (MAGIC[i] != dis.readByte()) {
                trace(mn + path + " is not lock file. Return '" + heartbeat
                      + "'");

                return heartbeat;
            }
        }

        heartbeat = dis.readLong();

        trace(mn + " read:  [" + HsqlDateTime.getTimestampString(heartbeat)
              + "]");
        dis.close();
        trace(mn + " closed " + dis);

        return heartbeat;
    }

    /**
     * Provides any specialized release actions for the tryRelease()
     * method. <p>
     *
     * @return true if there are no specialized release
     *        actions performed or they succeed,
     *        else false
     * @throws Exception if a situation is encountered that absolutely
     *        prevents the status of the lock condtion
     *        to be determined. (e.g. an IO exception
     *        occurs here).
     */
    protected boolean releaseImpl() throws Exception {

        trace("releaseImpl(): no action: returning true");

        return true;
    }

    /** Schedules the lock heartbeat task. */
    private void startHeartbeat() {

        Runnable r;

        trace("startHeartbeat(): entered.");

        if (timerTask == null || HsqlTimer.isCancelled(timerTask)) {
            r = new HeartbeatRunner();

            // now, periodic at HEARTBEAT_INTERVAL, running this, fixed rate
            timerTask = timer.schedulePeriodicallyAfter(0,
                    HEARTBEAT_INTERVAL, r, true);

            trace("startHeartbeat(): heartbeat task scheduled.");
        }

        trace("startHeartbeat(): exited.");
    }

    /** Cancels the lock heartbeat task. */
    private void stopHeartbeat() {

        String mn = "stopHeartbeat(): ";

        trace(mn + "entered");

        if (timerTask != null &&!HsqlTimer.isCancelled(timerTask)) {
            HsqlTimer.cancel(timerTask);

            timerTask = null;
        }

        trace(mn + "exited");
    }

    /**
     * Writes a magic value to this object's lock file that distiguishes
     * it as an HSQLDB lock file.
     *
     * @throws Exception if the magic value cannot be written to
     *      the lock file
     */
    private void writeMagic() throws Exception {

        String mn   = "writeMagic(): ";
        String path = "lock file [" + cpath + "]";

        trace(mn + "entered.");
        trace(mn + "raf.seek(0)");
        raf.seek(0);
        trace(mn + "raf.write(byte[])");
        raf.write(MAGIC);
        trace(mn + "wrote [\"HSQLLOCK\".getBytes()] to " + path);
    }

    /**
     * Writes the current hearbeat timestamp value to this
     * object's lock file. <p>
     *
     * @throws Exception if the current heartbeat timestamp value
     *      cannot be written
     */
    private void writeHeartbeat() throws Exception {

        long   time;
        String mn   = "writeHeartbeat(): ";
        String path = "lock file [" + cpath + "]";

        trace(mn + "entered.");

        time = System.currentTimeMillis();

        trace(mn + "raf.seek(" + MAGIC.length + ")");
        raf.seek(MAGIC.length);
        trace(mn + "raf.writeLong(" + time + ")");
        raf.writeLong(time);
        trace(mn + "wrote [" + time + "] to " + path);
    }

    /**
     * Retrieves a <code>LockFile</code> instance, initialized with a
     * <code>File</code> object whose path is the one specified by
     * the <code>path</code> argument. <p>
     *
     * @return a <code>LockFile</code> instance initialized with a
     *        <code>File</code> object whose path is the one specified
     *        by the <code>path</code> argument.
     * @param path the path of the <code>File</code> object with
     *        which the retrieved <code>LockFile</code>
     *        object is to be initialized
     */
    public static LockFile newLockFile(String path) throws Exception {

        File     f;
        LockFile lf;
        Class    c;

        c = null;

        try {
            Class.forName("java.nio.channels.FileLock");

            c  = Class.forName("org.hsqldb.persist.NIOLockFile");
            lf = (LockFile) c.newInstance();
        } catch (Exception e) {
            lf = new LockFile();
        }

        f = new File(path);

        FileUtil.makeParentDirectories(f);
        lf.setFile(f);

        return lf;
    }

    public static LockFile newLockFileLock(String path) throws HsqlException {

        LockFile lf = null;

        try {
            lf = LockFile.newLockFile(path + ".lck");
        } catch (Exception e) {
            throw Trace.error(Trace.FILE_IO_ERROR, e.toString());
        }

        boolean locked = false;
        String  msg    = "";

        try {
            locked = lf.tryLock();
        } catch (Exception e) {

            // e.printStackTrace();
            msg = e.toString();
        }

        if (!locked) {
            throw Trace.error(Trace.DATABASE_ALREADY_IN_USE, lf + ": " + msg);
        }

        return lf;
    }

    /**
     * Tests whether some other object is "equal to" this one.
     *
     * An object is considered equal to a <code>LockFile</code> object iff it
     * is not null, it is an instance of <code>LockFile</code> and either it's
     * the identical instance or it has the same lock file.  More  formally,
     * is is considered equal iff it is not null, it is an instance of
     * <code>LockFile</code>, and the expression: <p>
     *
     * <pre>
     * this == other ||
     * this.f == null ? other.f == null : this.f.equals(other.f);
     * </pre>
     *
     * yeilds true. <p>
     *
     * @param obj the reference object with which to compare.
     * @return <code>true</code> if this object is equal to
     *        the <code>obj</code> argument;
     *        <code>false</code> otherwise.
     * @see #hashCode
     */
    public boolean equals(Object obj) {

        // do faster tests first
        if (this == obj) {
            return true;
        } else if (obj instanceof LockFile) {
            LockFile that = (LockFile) obj;

            return (f == null) ? that.f == null
                               : f.equals(that.f);
        } else {
            return false;
        }
    }

    /**
     * Retrieves, as a String, the canonical path of this object's lock file.
     *
     * @return the canonical path of this object's lock file.
     */
    public String getCanonicalPath() {
        return cpath;
    }

    /**
     * Retrieves the hash code value for this object.
     *
     * The value is zero if the <code>File</code> object attribute
     * <code>f</code> is <code>null</code>, else it is the <code>hashCode</code>
     * of <code>f</code>. That is, two <code>LockFile</code>
     * objects have the same <code>hashCode</code> value if they have the
     * same lock file. <p>
     *
     * @return a hash code value for this object.
     * @see #equals(java.lang.Object)
     */
    public int hashCode() {
        return f == null ? 0
                         : f.hashCode();
    }

    /**
     * Retrieves whether this object has successfully obtained and is
     * still currently holding (has not yet released) a cooperative
     * lock condition on its lock file. <p>
     *
     * <b>Note:</b>  Due to the retrictions placed on the JVM by
     * platform-independence, it is very possible to successfully
     * obtain and hold a cooperative lock on a lock file and yet for
     * the lock to become invalid while held. <p>
     *
     * For instance, under JVMs with no <code>java.nio</code> package or
     * operating systems that cannot live up to the contracts set forth for
     * {@link java.nio.channels.FileLock FileLock}, it is quite possible
     * for another process or even an uncooperative bit of code running
     * in the same JVM to delete or overwrite the lock file while
     * this object holds a lock on it. <p>
     *
     * Because of this, the isValid() method is provided in the public
     * interface in order to allow clients to detect such situations. <p>
     *
     * @return true iff this object has successfully obtained
     *        and is currently holding (has not yet released)
     *        a lock on its lock file
     * @see #isValid
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Retrieves whether there is potentially already a cooperative lock,
     * operating system lock or some other situation preventing
     * a cooperative lock condition from being aquired, relative to the
     * specified path.
     *
     * @param path the path to test
     */
    public static boolean isLocked(String path) {

        FileInputStream fis = null;

        try {
            LockFile lf = LockFile.newLockFile(path);

            lf.checkHeartbeat();

            if (lf.f.exists() && lf.f.isFile()) {
                fis = new FileInputStream(lf.f);

                fis.read();
            }

            return false;
        } catch (Exception e) {}
        finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (java.io.IOException e) {}
            }
        }

        return true;
    }

    /**
     * Retrieves whether this object holds a valid lock on its lock file. <p>
     *
     * More formally, this method retrieves true iff: <p>
     *
     * <pre>
     * isLocked() &&
     * f != null &&
     * f.exists() &&
     * raf != null
     * </pre>
     *
     * @return true iff this object holds a valid lock on its
     *        lock file.
     */
    public boolean isValid() {
        return isLocked() && f != null && f.exists() && raf != null;
    }

    /**
     * For internal use only. <p>
     *
     * This Runnable class provides the implementation for the timed task
     * that periodically writes out a heartbeat timestamp to the lock file.<p>
     */
    protected class HeartbeatRunner implements Runnable {

        public void run() {

            try {
                trace("HeartbeatRunner.run(): writeHeartbeat()");
                writeHeartbeat();
            } catch (Throwable t) {
                trace("HeartbeatRunner.run(): caught Throwable: " + t);
            }
        }
    }

    /**
     * Retrieves a String representation of this object. <p>
     *
     * The String is of the form: <p>
     *
     * <pre>
     * super.toString() +
     * "[file=" + getAbsolutePath() +
     * ", exists=" + f.exists() +
     * ", locked=" + isLocked() +
     * ", valid=" + isValid() +
     * ", " + toStringImpl() +
     * "]";
     * </pre>
     * @return a String representation of this object.
     * @see #toStringImpl
     */
    public String toString() {

        return super.toString() + "[file =" + cpath + ", exists="
               + f.exists() + ", locked=" + isLocked() + ", valid="
               + isValid() + ", " + toStringImpl() + "]";
    }

    /**
     * Retrieves an implementation-specific tail value for the
     * toString() method. <p>
     *
     * The default implementation returns the empty string.
     * @return an implementation-specific tail value for the toString() method
     * @see #toString
     */
    protected String toStringImpl() {
        return "";
    }

    /**
     * Attempts, if not already held, to obtain a cooperative lock condition
     * on this object's lock file. <p>
     *
     * @throws Exception if an error occurs that absolutely prevents the lock
     *        status of the lock condition from being determined
     *        (e.g. an unhandled file I/O error).
     * @return <code>true</code> if this object already holds a lock or
     *        the lock was obtained successfully, else
     *        <code>false</code>
     */
    public boolean tryLock() throws Exception {

        String mn = "tryLock(): ";

        trace(mn + "entered.");

        if (locked) {
            trace(mn + " lock already held. Returning true immediately.");

            return true;
        }

        checkHeartbeat();

// Alternatively, we could give ourselves a second try,
// raising our chances of success in the rare case that the
// last locker terminiated abruptly just less than
// HEARTBEAT_INTERVAL ago.
//
//        try {
//            checkHeartbeat();
//        } catch (Exception e) {
//            try {
//                Thread.sleep(HEARTBEAT_INTERVAL);
//            } catch (Exception e2) {}
//            checkHeartbeat();
//        }
        openRAF();

        locked = lockImpl();

        if (locked) {
            writeMagic();
            startHeartbeat();

            try {

                // attempt to ensure that tryRelease() gets called if/when
                // the VM shuts down, just in case this object has not yet
                // been garbage-collected or explicitly released.
                JavaSystem.runFinalizers();
                trace(mn + "success for System.runFinalizersOnExit(true)");
            } catch (Exception e) {
                trace(mn + e.toString());
            }
        } else {
            try {
                releaseImpl();
                closeRAF();
            } catch (Exception e) {
                trace(mn + e.toString());
            }
        }

        trace(mn + "ran to completion.  Returning " + locked);

        return locked;
    }

    /**
     * Attempts to release any cooperative lock condition this object
     * may have on its lock file. <p>
     *
     * @throws Exception if an error occurs that absolutely prevents
     *       the status of the lock condition from
     *       being determined (e.g. an unhandled file
     *       I/O exception).
     * @return <code>true</code> if this object does not hold a
     *        lock or the lock is released successfully,
     *        else <code>false</code>.
     */
    public boolean tryRelease() throws Exception {

        String mn = "tryRelease(): ";
        String path;

        trace(mn + "entered.");

        boolean released = !locked;

        if (released) {
            trace(mn + "No lock held. Returning true immediately");

            return true;
        }

        try {
            released = releaseImpl();
        } catch (Exception e) {
            trace(mn + e);
        }

        if (!released) {
            trace(mn + "releaseImpl() failed. Returning false immediately.");

            return false;
        }

        trace(mn + "releaseImpl() succeeded.");
        stopHeartbeat();
        closeRAF();

        // without a small delay, the following delete may occasionally fail
        // and return false on some systems, when really it should succeed
        // and return true.
        trace(mn + "Starting Thread.sleep(100).");

        try {
            Thread.sleep(100);
        } catch (Exception e) {
            trace(mn + e.toString());
        }

        trace(mn + "Finished Thread.sleep(100).");

        path = "[" + cpath + "]";

        if (f.exists()) {
            trace(mn + path + " exists.");

            released = f.delete();

            trace(mn + path + (released ? ""
                                        : "not") + " deleted.");

            if (f.exists()) {
                trace(mn + " WARNING!: " + path + "still exists.");
            }
        }

        locked = !released;

        trace(mn + "ran to completion.  Returning " + released);

        return released;
    }

    /**
     * Prints tracing information and the value of the specified object
     *
     * @param o the value to print
     */
    protected void trace(Object o) {

        if (Trace.TRACE) {
            Trace.printSystemOut("[" + super.toString() + "]: " + o);
        }
    }

    /**
     * Attempts to release any lock condition this object may have on its
     * lock file. <p>
     *
     * @throws Throwable if this object encounters an unhandled exception
     *        trying to release the lock condition,
     *        if any, that it has on its lock file.
     */
    protected void finalize() throws Throwable {
        trace("finalize(): calling tryRelease()");
        tryRelease();
    }
}
