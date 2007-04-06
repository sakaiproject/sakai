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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Base class for producing the Socket objects used by HSQLDB.
 *
 * @author unsaved@users
 * @author boucherb@users
 * @version 1.7.2
 * @since 1.7.2
 */
public class HsqlSocketFactory {

// ----------------------------- static members ---------------------------------
    private static HsqlSocketFactory plainImpl;
    private static HsqlSocketFactory sslImpl;

// ------------------------------ constructors ---------------------------------

    /**
     * External construction disabled.  New factory instances are retreived
     * through the newHsqlSocketFactory method instead.
     */
    protected HsqlSocketFactory() throws Exception {}

// ------------------------- factory builder method ----------------------------

    /**
     * Retrieves an HsqlSocketFactory whose subclass and attributes are
     * determined by the specified argument, tls.
     *
     * @param tls whether to retrieve a factory producing SSL sockets
     * @throws Exception if the new factory cannot be constructed or is
     *      of the wrong type
     * @return a new factory
     */
    public static HsqlSocketFactory getInstance(boolean tls)
    throws Exception {
        return tls ? getSSLImpl()
                   : getPlainImpl();
    }

// -------------------------- public instance methods --------------------------
    public void configureSocket(Socket socket) {

        // default: do nothing
    }

    /**
     * Returns a server socket bound to the specified port.
     * The socket is configured with the socket options
     * given to this factory.
     *
     * @return the ServerSocket
     * @param port the port to which to bind the ServerSocket
     * @throws Exception if a network error occurs
     */
    public ServerSocket createServerSocket(int port) throws Exception {
        return new ServerSocket(port);
    }

    /**
     * Returns a server socket bound to the specified port.
     * The socket is configured with the socket options
     * given to this factory.
     *
     * @return the ServerSocket
     * @param port the port to which to bind the ServerSocket
     * @throws Exception if a network error occurs
     */
    public ServerSocket createServerSocket(int port,
                                           String address) throws Exception {
        return new ServerSocket(port, 128, InetAddress.getByName(address));
    }

    /**
     * Creates a socket and connects it to the specified remote host at the
     * specified remote port. This socket is configured using the socket options
     * established for this factory.
     *
     * @return the socket
     * @param host the server host
     * @param port the server port
     * @throws Exception if a network error occurs
     */
    public Socket createSocket(String host, int port) throws Exception {
        return new Socket(host, port);
    }

    /**
     * Retrieves whether this factory produces secure sockets.
     *
     * @return true if this factory produces secure sockets
     */
    public boolean isSecure() {
        return false;
    }

// ------------------------ static utility methods -----------------------------
    private static HsqlSocketFactory getPlainImpl() throws Exception {

        synchronized (HsqlSocketFactory.class) {
            if (plainImpl == null) {
                plainImpl = new HsqlSocketFactory();
            }
        }

        return plainImpl;
    }

    private static HsqlSocketFactory getSSLImpl() throws Exception {

        synchronized (HsqlSocketFactory.class) {
            if (sslImpl == null) {
                sslImpl = newFactory("org.hsqldb.HsqlSocketFactorySecure");
            }
        }

        return sslImpl;
    }

    /**
     * Retrieves a new HsqlSocketFactory whose class
     * is determined by the implClass argument. The basic contract here
     * is that implementations constructed by this method should return
     * true upon calling isSecure() iff they actually create secure sockets.
     * There is no way to guarantee this directly here, so it is simply
     * trusted that an  implementation is secure if it returns true
     * for calls to isSecure();
     *
     * @return a new secure socket factory
     * @param implClass the fully qaulified name of the desired
     *      class to construct
     * @throws Exception if a new secure socket factory cannot
     *      be constructed
     */
    private static HsqlSocketFactory newFactory(String implClass)
    throws Exception {

        Class       clazz;
        Constructor ctor;
        Class[]     ctorParm;
        Object[]    ctorArg;
        Object      factory;

        clazz    = Class.forName(implClass);
        ctorParm = new Class[0];

        // protected constructor
        ctor    = clazz.getDeclaredConstructor(ctorParm);
        ctorArg = new Object[0];

        try {
            factory = ctor.newInstance(ctorArg);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();

            throw (t instanceof Exception) ? ((Exception) t)
                                           : new RuntimeException(
                                               t.toString());
        }

        return (HsqlSocketFactory) factory;
    }

// --
}
