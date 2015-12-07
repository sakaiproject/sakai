package edu.amc.sakai.user;

import com.novell.ldap.LDAPSocketFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This simple socket factory sets a connect timeout on any sockets it creates.
 * Otherwise attempting to connect to a down host can effectively hang until the
 * pool creation timeout is hit.
 */
public class LDAPSimpleSocketFactory implements LDAPSocketFactory {

    // The number of ms to wait for a socket connect to complete.
    private int connectTimeout = 5000;

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), connectTimeout);
        return socket;
    }
}
