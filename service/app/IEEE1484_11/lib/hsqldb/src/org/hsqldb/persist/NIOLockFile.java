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

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * A LockFile variant that capitalizes upon the
 * availability of {@link java.nio.channels.FileLock FileLock}.
 *
 * @author boucherb@users
 * @version 1.7.2
 * @since 1.7.2
 *
 */
final class NIOLockFile extends LockFile {

    // From the java.nio.channels.FileLock API docs:
    //
    // Some network filesystems do not implement file locks on regions
    // that extend past a certain position, often 2**30 or 2**31.
    // In general, great care should be taken when locking files that
    // reside on network filesystems.
    static final long MAX_NFS_LOCK_REGION = (1L << 30);
    static final long MIN_LOCK_REGION     = MAGIC.length + 8;

    /**
     * A <code>FileChannel</code> object obtained from the super
     * <code>raf</code> attribute. <p>
     *
     * The <code>fc</code> attribute is used to obtain this object's
     * {@link #fl FileLock} attribute.
     */
    private FileChannel fc;

    /**
     * The <code>FileLock</code> object used to lock this object's
     * lock file.
     */
    private FileLock fl;

    /**
     * Tries to obtain a valid NIO lock upon this object's lock file using
     * this object's {@link #fl FileLock} attribute.
     *
     * @return true if a valid lock is obtained, else false.
     * @throws Exception if an error occurs while attempting to obtain the lock
     *
     */
    protected boolean lockImpl() throws Exception {

        boolean isValid;

        if (fl != null && fl.isValid()) {
            return true;
        }

        trace("lockImpl(): fc = raf.getChannel()");

        fc = raf.getChannel();

        trace("lockImpl(): fl = fc.tryLock()");

        fl = null;

        try {
            fl = fc.tryLock(0, MIN_LOCK_REGION, false);

            trace("lockImpl(): fl = " + fl);
        } catch (Exception e) {

            // This will not work with a localized JVM
            /*
            if (-1 == e.toString().indexOf("No locks available")) {
                throw e;
            } else {
                trace(e.toString());
            }
            */
            trace(e.toString());
        }

// In an ideal world, maybe?:
//        try {
//            fl = fc.tryLock();
//
//            trace("lockImpl(): fl = " + fl);
//        } catch (Exception e) {
//            trace(e.toString());
//
//            try {
//               fl = fc.tryLock(0, MAX_NFS_LOCK_REGION, false);
//
//               trace("lockImpl(): fl = " + fl);
//               trace("Warning: possibly attempting to lock on NFS");
//            } catch (Exception e2) {
//                trace(e2.toString());
//
//                try {
//                    fl = fc.tryLock(0, MIN_LOCK_REGION, false);
//
//                    trace("lockImpl(): fl = " + fl);
//                    trace("Warning: backed off to min lock region");
//                    trace("Warning: lock file may be unusable on reuse");
//                } catch (Exception e3) {
//                      trace(e3.toString());
//                }
//            }
//        }
        trace("lockImpl(): f.deleteOnExit()");
        f.deleteOnExit();

        isValid = fl != null && fl.isValid();

        trace("lockImpl():isValid(): " + isValid);

        return isValid;
    }

    /**
     * Tries to release any valid lock held upon this object's lock file using
     * this object's {@link #fl FileLock} attribute.
     *
     * @return true if a valid lock is released, else false
     * @throws Exception if na error occurs while attempting to release the lock
     */
    protected boolean releaseImpl() throws Exception {

        // PRE: we know that this method is only called
        // if isLocked() is true.
        trace("releaseImpl(): fl = " + fl);

        if (fl != null) {
            trace("releaseImpl(): fl.release()");
            fl.release();
            trace("tryRelease(): fl = " + fl);

            fl = null;
        }

        trace("releaseImpl(): fc = " + fc);

        if (fc != null) {
            trace("releaseImpl(): fc.close()");
            fc.close();

            fc = null;
        }

        // CHECKME:
        // possibly overcomes some regarding full and
        // true release of FileLock and maybe related
        // NIO resources?
        // System.gc();
        return true;
    }

    /**
     * Retrieves whether this object's {@link #fl FileLock} attribute represents
     * a valid lock upon this object's lock file.
     *
     * @return true if this object's {@link #fl FileLock} attribute is valid,
     *      else false
     */
    public boolean isValid() {
        return super.isValid() && (fl != null && fl.isValid());
    }

    /**
     * Retrieves the String value: "fl =" + fl
     * @return the String value: "fl =" + fl
     */
    protected String toStringImpl() {
        return "fl =" + fl;
    }
}
