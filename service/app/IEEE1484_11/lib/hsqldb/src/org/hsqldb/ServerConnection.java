/* Copyright (c) 1995-2000, The Hypersonic SQL Group.
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
 * Neither the name of the Hypersonic SQL Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE HYPERSONIC SQL GROUP,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many individuals 
 * on behalf of the Hypersonic SQL Group.
 *
 *
 * For work added by the HSQL Development Group:
 *
 * Copyright (c) 2001-2005, The HSQL Development Group
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

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import org.hsqldb.lib.ArrayUtil;
import org.hsqldb.rowio.RowInputBinary;
import org.hsqldb.rowio.RowOutputBinary;

// fredt@users 20020215 - patch 461556 by paul-h@users - server factory
// fredt@users 20020424 - patch 1.7.0 by fredt - shutdown without exit
// fredt@users 20021002 - patch 1.7.1 by fredt - changed notification method
// fredt@users 20030618 - patch 1.7.2 by fredt - changed read/write methods

/**
 *  All ServerConnection objects are listed in a Set in server
 *  and removed by this class when closed.<p>
 *
 *  When the database or server is shutdown, the signalClose() method is called
 *  for all current ServerConnection instances. This will call the private
 *  close() method unless the ServerConnection thread itself has caused the
 *  shutdown. In this case, the keepAlive flag is set to false, allowing the
 *  thread to terminate once it has returned the result of the operation to
 *  the client.
 *  (fredt@users)<p>
 *
 * Rewritten in version HSQLDB 1.7.2, based on original Hypersonic code.
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @author fredt@users
 * @version 1.8.0
 * @since Hypersonic SQL
 */
class ServerConnection implements Runnable {

    boolean                      keepAlive;
    private String               user;
    private String               password;
    int                          dbID;
    private volatile Session     session;
    private Socket               socket;
    private Server               server;
    private DataInputStream      dataInput;
    private BufferedOutputStream dataOutput;
    private static int           mCurrentThread = 0;
    private int                  mThread;
    static final int             BUFFER_SIZE = 0x1000;
    final byte[]                 mainBuffer  = new byte[BUFFER_SIZE];
    RowOutputBinary              rowOut = new RowOutputBinary(BUFFER_SIZE);
    RowInputBinary               rowIn       = new RowInputBinary(rowOut);
    Thread                       runnerThread;

    /**
     * Creates a new ServerConnection to the specified Server on the
     * specified socket.
     *
     * @param socket the network socket on which Server communication
     *      takes place
     * @param server the Server instance to which the object
     *      represents a connection
     */
    ServerConnection(Socket socket, Server server) {

        this.socket = socket;
        this.server = server;

        synchronized (ServerConnection.class) {
            mThread = mCurrentThread++;
        }

        synchronized (server.serverConnSet) {
            server.serverConnSet.add(this);
        }
    }

    /**
     * Signals this object to close, including exiting the thread running
     * the request handling loop
     */
    void signalClose() {

        keepAlive = false;

        if (!Thread.currentThread().equals(runnerThread)) {
            close();
        }
    }

    /**
     * Closes this connection.
     */
    private void close() {

        if (session != null) {
            session.close();
        }

        session = null;

        // fredt@user - closing the socket is to stop this thread
        try {
            socket.close();
        } catch (IOException e) {}

        synchronized (server.serverConnSet) {
            server.serverConnSet.remove(this);
        }
    }

    /**
     * Initializes this connection.
     */
    private void init() {

        runnerThread = Thread.currentThread();
        keepAlive    = true;

        try {
            socket.setTcpNoDelay(true);

            dataInput  = new DataInputStream(socket.getInputStream());
            dataOutput = new BufferedOutputStream(socket.getOutputStream());

            Result resultIn = Result.read(rowIn, dataInput);
            Result resultOut;

            try {
                dbID     = server.getDBID(resultIn.subSubString);
                user     = resultIn.getMainString();
                password = resultIn.getSubString();

                if (!server.isSilent()) {
                    server.printWithThread(mThread
                                           + ":trying to connect user "
                                           + user);
                }

                session = DatabaseManager.newSession(dbID,
                                                     resultIn.getMainString(),
                                                     resultIn.getSubString());
                resultOut = new Result(ResultConstants.UPDATECOUNT);
                resultOut.databaseID = session.getDatabase().databaseID;
                resultOut.sessionID  = session.getId();
            } catch (HsqlException e) {
                session   = null;
                resultOut = new Result(e, null);
            } catch (RuntimeException e) {
                session   = null;
                resultOut = new Result(e, null);
            }

            Result.write(resultOut, rowOut, dataOutput);

            return;
        } catch (Exception e) {
            server.printWithThread(mThread + ":couldn't connect " + user);
        }

        close();
    }

    /**
     * Initializes this connection and runs the request handling
     * loop until closed.
     */
    public void run() {

        init();

        if (session != null) {
            try {
                while (keepAlive) {
                    Result resultIn = Result.read(rowIn, dataInput);

                    server.printRequest(mThread, resultIn);

                    Result resultOut;

                    if (resultIn.mode == ResultConstants.HSQLRESETSESSION) {
                        resultOut = resetSession();
                    } else {
                        resultOut = session.execute(resultIn);
                    }

                    Result.write(resultOut, rowOut, dataOutput);
                    rowOut.setBuffer(mainBuffer);
                    rowIn.resetRow(mainBuffer.length);
                }
            } catch (IOException e) {

                // fredt - is thrown when connection drops
                server.printWithThread(mThread + ":disconnected " + user);
            } catch (HsqlException e) {

                // fredt - is thrown while constructing the result
                server.printStackTrace(e);
            }

            close();
        }
    }

    /**
     * Used by pooled connections to close the existing SQL session and open
     * a new one.
     */
    private Result resetSession() {

        Result resultOut;

        if (!server.isSilent()) {
            server.printWithThread(mThread + ":trying to connect user "
                                   + user);
        }

        try {
            session.close();

            session = DatabaseManager.newSession(dbID, user, password);
            resultOut            = new Result(ResultConstants.UPDATECOUNT);
            resultOut.databaseID = session.getDatabase().databaseID;
            resultOut.sessionID  = session.getId();
        } catch (HsqlException e) {
            session   = null;
            resultOut = new Result(e, null);
        }

        return resultOut;
    }

    /**
     * Retrieves the thread name to be used  when
     * this object is the Runnable object of a Thread.
     *
     * @return the thread name to be used  when this object is the Runnable
     * object of a Thread.
     */
    String getConnectionThreadName() {
        return "HSQLDB Connection @" + Integer.toString(hashCode(), 16);
    }
}
