/* **************************************************************************
 * $OpenLDAP$
 *
 * Copyright (C) 1999, 2000, 2001 Novell, Inc. All Rights Reserved.
 *
 * THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND
 * TREATIES. USE, MODIFICATION, AND REDISTRIBUTION OF THIS WORK IS SUBJECT
 * TO VERSION 2.0.1 OF THE OPENLDAP PUBLIC LICENSE, A COPY OF WHICH IS
 * AVAILABLE AT HTTP://WWW.OPENLDAP.ORG/LICENSE.HTML OR IN THE FILE "LICENSE"
 * IN THE TOP-LEVEL DIRECTORY OF THE DISTRIBUTION. ANY USE OR EXPLOITATION
 * OF THIS WORK OTHER THAN AS AUTHORIZED IN VERSION 2.0.1 OF THE OPENLDAP
 * PUBLIC LICENSE, OR OTHER PRIOR WRITTEN CONSENT FROM NOVELL, COULD SUBJECT
 * THE PERPETRATOR TO CRIMINAL AND CIVIL LIABILITY.
 ******************************************************************************/

package edu.amc.sakai.user;

import com.novell.ldap.LDAPSocketFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;


/**
 * Represents a socket factory that creates secure socket connections to
 * LDAP servers using JSSE technology. Unlike the one in the JLDAP library this
 * actually has a connection connectTimeout that is sensible in production.
 * This modifies the standard one in the JLDAP library to specify a connect
 * timeout so that we don't wait forever to connect to a server.
 *
 * @see LDAPConnection#LDAPConnection(LDAPSocketFactory)
 * @see LDAPConnection#setSocketFactory
 */
public class LDAPJSSESecureSocketFactory
        implements LDAPSocketFactory, org.ietf.ldap.LDAPSocketFactory
{
    private SocketFactory factory;
    // The connectTimeout to make connections in.
    private int connectTimeout = 5000;

    /**
     * Constructs an LDAPSecureSocketFactory object using the default provider
     * for a JSSE SSLSocketFactory.
     *
     * <p>Setting the keystore for the default provider is specific to
     * the provider implementation.  For Sun's JSSE provider, the property
     * javax.net.ssl.truststore should be set to the path of a keystore that
     * holds the trusted root certificate of the directory server.</P>
     *
     * For information on creating keystores see the keytool documentation on
     * <a href="http://java.sun.com/j2se/1.4/docs/tooldocs/tools.html#security">
     * Java 2, security tools</a>.
     */
    public LDAPJSSESecureSocketFactory()
    {
        factory = SSLSocketFactory.getDefault();
        return;
    }

    /**
     * Constructs an LDAPSocketFactory object using the
     * SSLSocketFactory specified.
     *
     * For information on using the the SSLSocketFactory, see also
     * <a href="http://java.sun.com/j2se/1.4/docs/api/javax/net/ssl/SSLSocketFactory.html">
     * javax.net.ssl.SSLSocketFactory</a>
     */
    public LDAPJSSESecureSocketFactory(SSLSocketFactory factory)
    {
        this.factory = factory;
        return;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Returns the socket connected to the LDAP server with the specified
     * host name and port number.
     *
     * <p>The secure connection is established to the server when this
     * call returns.  This method is called by the constructor of LDAPConnection
     * </p>
     *
     * @param host The host name or a dotted string representing the IP address
     *             of the LDAP server to which you want to establish
     *             a connection.
     *<br><br>
     * @param port The port number on the specified LDAP server that you want to
     *             use for this connection. The default LDAP port for SSL
     *             connections is 636.
     *
     * @return A socket to the LDAP server using the specific host name and
     *         port number.
     *
     * @exception IOException A socket to the specified host and port
     *                          could not be created.
     *
     * @exception UnknownHostException The specified host could not be found.
     */
    public java.net.Socket createSocket(String host, int port)
            throws IOException, UnknownHostException
    {
        Socket socket = factory.createSocket();
        socket.connect(new InetSocketAddress(host, port), connectTimeout);
        return socket;
    }
}
