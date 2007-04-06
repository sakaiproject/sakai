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

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Principal;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.security.cert.X509Certificate;

import org.hsqldb.lib.StringConverter;

/**
 * The default secure socket factory implementation.
 *
 * @author unsaved@users
 * @author boucherb@users
 * @version 1.7.2
 * @since 1.7.2
 */
public final class HsqlSocketFactorySecure extends HsqlSocketFactory
implements HandshakeCompletedListener {

// --------------------------------- members -----------------------------------

    /** The underlying socket factory implementation. */
    protected Object socketFactory;

    /** The underlying server socket factory implementation. */
    protected Object serverSocketFactory;

    /**
     * Monitor object to guard against conncurrent modification
     * of the underlying socket factory implementation member.
     */
    protected final Object socket_factory_mutex = new Object();

    /**
     * Monitor object to guard against concurrent modification of
     * the underlying server socket factory implementation member.
     */
    protected final Object server_socket_factory_mutex = new Object();

// ------------------------------ constructors ---------------------------------

    /**
     * External construction disabled.  New factory instances are retreived
     * through the newHsqlSocketFactory method instead.
     */
    protected HsqlSocketFactorySecure() throws Exception {

        super();

        Provider p;
        String   cls;

        if (Security.getProvider("SunJSSE") == null) {
            try {
                p = (Provider) Class.forName(
                    "com.sun.net.ssl.internal.ssl.Provider").newInstance();

                Security.addProvider(p);
            } catch (Exception e) {}
        }
    }

// ----------------------------- subclass overrides ----------------------------
    public void configureSocket(Socket socket) {

        SSLSocket s;

        super.configureSocket(socket);

        s = (SSLSocket) socket;

        s.addHandshakeCompletedListener(this);
    }

    /**
     * Creates a secure server socket bound to the specified port.
     * The socket is configured with the socket options
     * given to this factory.
     *
     * @return the secure ServerSocket
     * @param port the port to which to bind the secure ServerSocket
     * @throws Exception if a network or security provider error occurs
     */
    public ServerSocket createServerSocket(int port) throws Exception {

        SSLServerSocket ss;

        ss = (SSLServerSocket) getServerSocketFactoryImpl()
            .createServerSocket(port);

        if (Trace.TRACE) {
            Trace.printSystemOut("[" + this + "]: createServerSocket()");
            Trace.printSystemOut("capabilities for " + ss + ":");
            Trace.printSystemOut("----------------------------");
            dump("supported cipher suites", ss.getSupportedCipherSuites());
            dump("enabled cipher suites", ss.getEnabledCipherSuites());
        }

        return ss;
    }

    /**
     * Creates a secure server socket bound to the specified port.
     * The socket is configured with the socket options
     * given to this factory.
     *
     * @return the secure ServerSocket
     * @param port the port to which to bind the secure ServerSocket
     * @throws Exception if a network or security provider error occurs
     */
    public ServerSocket createServerSocket(int port,
                                           String address) throws Exception {

        SSLServerSocket ss;
        InetAddress     addr;

        addr = InetAddress.getByName(address);
        ss = (SSLServerSocket) getServerSocketFactoryImpl()
            .createServerSocket(port, 128, addr);

        if (Trace.TRACE) {
            Trace.printSystemOut("[" + this + "]: createServerSocket()");
            Trace.printSystemOut("capabilities for " + ss + ":");
            Trace.printSystemOut("----------------------------");
            dump("supported cipher suites", ss.getSupportedCipherSuites());
            dump("enabled cipher suites", ss.getEnabledCipherSuites());
        }

        return ss;
    }

    private static void dump(String title, String[] as) {

        Trace.printSystemOut(title);
        Trace.printSystemOut("----------------------------");

        for (int i = 0; i < as.length; i++) {
            Trace.printSystemOut(String.valueOf(as[i]));
        }

        Trace.printSystemOut("----------------------------");
    }

    /**
     * Creates a secure Socket and connects it to the specified remote host
     * at the specified remote port. This socket is configured using the
     * socket options established for this factory.
     *
     * @return the socket
     * @param host the server host
     * @param port the server port
     * @throws Exception if a network or security provider error occurs
     */
    public Socket createSocket(String host, int port) throws Exception {

        SSLSocket socket;

        socket = (SSLSocket) getSocketFactoryImpl().createSocket(host, port);

        socket.addHandshakeCompletedListener(this);
        socket.startHandshake();

// unsaved@users
// For https protocol, the protocol handler should do this verification
// (Sun's implementation does), but if we do not use the Protocol
// handler (which is only available in Java >= 1.4), then we need to do
// the verification: hostname == cert CN
//
// boucherb@users 20030503:
// CHEKME/TODO:
//
// Stricter verify?  Either require SunJSSE (assume its trust manager properly
// verifies whole chain), or implement our own TrustManager layer?
//
// What about v1/v3 and signing checks (re: man-in-the-middle attack),
// CRL check, basic constraints? notBefore? notAfter?
//
// Reference:  http://www.securitytracker.com/alerts/2002/Aug/1005030.html
//
// That is, we can't guarantee that installed/prefered provider trust manager
// implementations verify the whole chain properly and there are still
// v1 certs out there (i.e. have no basic constraints, etc.), meaning that
// we should check for and reject any intermediate certs that are not v3+
// (cannot be checked for basic constraints).  Only root and intermediate
// certs found in the trust store should be allowed to be v1 (since we must
// be trusing them for them to be there).  All other intermediate signers,
// however, should be required to be v3+, otherwise anybody with any kind
// of cert issued somehow via a trust chain from the root can pose as an
// intermediate signing CA and hence leave things open to man-in-the-middle
// style attack.  Also, we should really check CRLs, just in case
// it turns out that trust chain has been breached and thus issuer has revoked
// on some cert(s).  Of course, this really begs the question, as it is not
// guaranteed that all CAs in trust store have valid, working CRL URL
//
// So what to do?
//
// Maybe best to leave this all up to DBA?
        verify(host, socket.getSession());

        return socket;
    }

    /**
     * Retrieves whether this factory produces secure sockets.
     *
     * @return true iff this factory creates secure sockets
     */
    public boolean isSecure() {
        return true;
    }

// ----------------------- internal implementation -----------------------------

    /**
     * Retrieves the underlying javax.net.ssl.SSLServerSocketFactory.
     *
     * @throws Exception if there is a problem retrieving the
     *      underlying factory
     * @return the underlying javax.net.ssl.SSLServerSocketFactory
     */
    protected SSLServerSocketFactory getServerSocketFactoryImpl()
    throws Exception {

        Object factory;

        synchronized (server_socket_factory_mutex) {
            factory = serverSocketFactory;

            if (factory == null) {
                factory             = SSLServerSocketFactory.getDefault();
                serverSocketFactory = factory;
            }
        }

        return (SSLServerSocketFactory) factory;
    }

    /**
     * Retrieves the underlying javax.net.ssl.SSLSocketFactory.
     *
     * @throws Exception if there is a problem retrieving the
     *      underlying factory
     * @return the underlying javax.net.ssl.SSLSocketFactory
     */
    protected SSLSocketFactory getSocketFactoryImpl() throws Exception {

        Object factory;

        synchronized (socket_factory_mutex) {
            factory = socketFactory;

            if (factory == null) {
                factory       = SSLSocketFactory.getDefault();
                socketFactory = factory;
            }
        }

        return (SSLSocketFactory) factory;
    }

    /**
     * Verifyies the certificate chain presented by the server to which
     * a secure Socket has just connected.  Specifically, the provided host
     * name is checked against the Common Name of the server certificate;
     * additional checks may or may not be performed.
     *
     * @param host the requested host name
     * @param session SSLSession used on the connection to host
     * @throws Exception if the certificate chain cannot be verified
     */
    protected void verify(String host, SSLSession session) throws Exception {

        X509Certificate[] chain;
        X509Certificate   certificate;
        Principal         principal;
        PublicKey         publicKey;
        String            DN;
        String            CN;
        int               start;
        int               end;
        String            emsg;

        chain       = session.getPeerCertificateChain();
        certificate = chain[0];
        principal   = certificate.getSubjectDN();
        DN          = String.valueOf(principal);
        start       = DN.indexOf("CN=");

        if (start < 0) {
            throw new UnknownHostException(
                Trace.getMessage(Trace.HsqlSocketFactorySecure_verify));
        }

        start += 3;
        end   = DN.indexOf(',', start);
        CN    = DN.substring(start, (end > -1) ? end
                                               : DN.length());

        if (CN.length() < 1) {
            throw new UnknownHostException(
                Trace.getMessage(Trace.HsqlSocketFactorySecure_verify2));
        }

        if (!CN.equalsIgnoreCase(host)) {

            // TLS_HOSTNAME_MISMATCH
            throw new UnknownHostException(
                Trace.getMessage(
                    Trace.HsqlSocketFactorySecure_verify3, true,
                    new Object[] {
                CN, host
            }));
        }
    }

    public void handshakeCompleted(HandshakeCompletedEvent evt) {

        SSLSession session;
        String     sessionId;
        SSLSocket  socket;

        if (Trace.TRACE) {
            socket  = evt.getSocket();
            session = evt.getSession();

            Trace.printSystemOut("SSL handshake completed:");
            Trace.printSystemOut(
                "------------------------------------------------");
            Trace.printSystemOut("socket:      : " + socket);
            Trace.printSystemOut("cipher suite : "
                                 + session.getCipherSuite());

            sessionId = StringConverter.byteToHex(session.getId());

            Trace.printSystemOut("session id   : " + sessionId);
            Trace.printSystemOut(
                "------------------------------------------------");
        }
    }
}
