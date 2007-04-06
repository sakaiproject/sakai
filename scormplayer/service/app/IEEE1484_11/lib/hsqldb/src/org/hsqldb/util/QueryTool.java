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


package org.hsqldb.util;

import java.applet.Applet;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Event;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.Panel;
import java.awt.SystemColor;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import org.hsqldb.lib.java.JavaSystem;

/**
 * Simple demonstration applet
 *
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @version 1.7.0
 * @since Hypersonic SQL
 */
public class QueryTool extends Applet
implements WindowListener, ActionListener {

    static Properties pProperties = new Properties();
    boolean           bApplication;

    /**
     * You can start QueryTool without a browser and applet
     * using using this method. Type 'java QueryTool' to start it.
     * This is necessary if you want to use the standalone version
     * because appletviewer and internet browers do not allow the
     * applet to write to disk.
     */
    static Frame fMain;

    /**
     * Method declaration
     *
     *
     * @param arg
     */
    public static void main(String[] arg) {

        fMain = new Frame("Query Tool");

        QueryTool q = new QueryTool();

        q.bApplication = true;

        for (int i = 0; i < arg.length; i++) {
            String p = arg[i];

            if (p.equals("-?")) {
                printHelp();
            }

            if (p.charAt(0) == '-') {
                pProperties.put(p.substring(1), arg[i + 1]);

                i++;
            }
        }

        q.init();
        q.start();
        fMain.add("Center", q);

        MenuBar menu = new MenuBar();
        Menu    file = new Menu("File");

        file.add("Exit");
        file.addActionListener(q);
        menu.add(file);
        fMain.setMenuBar(menu);
        fMain.setSize(500, 400);
        fMain.show();
        fMain.addWindowListener(q);
    }

    Connection cConn;
    Statement  sStatement;

    /**
     * Initializes the window and the database and inserts some test data.
     */
    public void init() {

        initGUI();

        Properties p = pProperties;

        if (!bApplication) {

            // default for applets is in-memory (.)
            p.put("database", ".");

            try {

                // but it may be also a HTTP connection (http://)
                // try to use url as provided on the html page as parameter
                pProperties.put("database", getParameter("database"));
            } catch (Exception e) {}
        }

        String  driver   = p.getProperty("driver", "org.hsqldb.jdbcDriver");
        String  url      = p.getProperty("url", "jdbc:hsqldb:");
        String  database = p.getProperty("database", ".");
        String  user     = p.getProperty("user", "sa");
        String  password = p.getProperty("password", "");
        boolean test = p.getProperty("test", "true").equalsIgnoreCase("true");
        boolean log = p.getProperty("log", "true").equalsIgnoreCase("true");

        try {
            if (log) {
                trace("driver  =" + driver);
                trace("url     =" + url);
                trace("database=" + database);
                trace("user    =" + user);
                trace("password=" + password);
                trace("test    =" + test);
                trace("log     =" + log);
                JavaSystem.setLogToSystem(true);
            }

            // As described in the JDBC FAQ:
            // http://java.sun.com/products/jdbc/jdbc-frequent.html;
            // Why doesn't calling class.forName() load my JDBC driver?
            // There is a bug in the JDK 1.1.x that can cause Class.forName() to fail.
//            new org.hsqldb.jdbcDriver();
            Class.forName(driver).newInstance();

            cConn = DriverManager.getConnection(url + database, user,
                                                password);
        } catch (Exception e) {
            System.out.println("QueryTool.init: " + e.getMessage());
            e.printStackTrace();
        }

        sRecent = new String[iMaxRecent];
        iRecent = 0;

        try {
            sStatement = cConn.createStatement();
        } catch (SQLException e) {
            System.out.println("Exception: " + e);
        }

        if (test) {
            insertTestData();
        }

        txtCommand.requestFocus();
    }

    /**
     * Method declaration
     *
     *
     * @param s
     */
    void trace(String s) {
        System.out.println(s);
    }

    /**
     * This is function handles the events when a button is clicked or
     * when the used double-clicked on the listbox of recent commands.
     */
    public boolean action(Event evt, Object arg) {

        String s = arg.toString();

        if (s.equals("Execute")) {
            String   sCmd = txtCommand.getText();
            String[] g    = new String[1];

            try {
                sStatement.execute(sCmd);

                int r = sStatement.getUpdateCount();

                if (r == -1) {
                    formatResultSet(sStatement.getResultSet());
                } else {
                    g[0] = "update count";

                    gResult.setHead(g);

                    g[0] = String.valueOf(r);

                    gResult.addRow(g);
                }

                setRecent(txtCommand.getText());
            } catch (SQLException e) {
                g[0] = "SQL Error";

                gResult.setHead(g);

                g[0] = e.getMessage();

                gResult.addRow(g);
            }

            gResult.repaint();
            txtCommand.selectAll();
            txtCommand.requestFocus();
        } else if (s.equals("Script")) {
            String sScript = getScript();

            txtCommand.setText(sScript);
            txtCommand.selectAll();
            txtCommand.requestFocus();
        } else if (s.equals("Import")) {
            String sImport = getImport();

            txtCommand.setText(sImport);
            txtCommand.selectAll();
            txtCommand.requestFocus();
        } else if (s.equals("Exit")) {
            System.exit(0);
        } else {    // recent
            txtCommand.setText(s);
        }

        return true;
    }

    /**
     * Method declaration
     *
     *
     * @param r
     */
    void formatResultSet(ResultSet r) {

        try {
            ResultSetMetaData m   = r.getMetaData();
            int               col = m.getColumnCount();
            String[]          h   = new String[col];

            for (int i = 1; i <= col; i++) {
                h[i - 1] = m.getColumnLabel(i);
            }

            gResult.setHead(h);

            while (r.next()) {
                for (int i = 1; i <= col; i++) {
                    h[i - 1] = r.getString(i);

                    if (r.wasNull()) {
                        h[i - 1] = "(null)";
                    }
                }

                gResult.addRow(h);
            }
        } catch (SQLException e) {}
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    String getScript() {

        ResultSet rResult = null;

        try {
            rResult = sStatement.executeQuery("SCRIPT");

            StringBuffer a = new StringBuffer();

            while (rResult.next()) {
                a.append(rResult.getString(1));
                a.append('\n');
            }

            a.append('\n');

            return a.toString();
        } catch (SQLException e) {
            return "";
        } finally {
            if (rResult != null) {
                try {
                    rResult.close();
                } catch (Exception e) {}
            }
        }
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    String getImport() {

        StringBuffer   a        = new StringBuffer();
        String         filename = "import.sql";
        BufferedReader in       = null;

        try {
            in = new BufferedReader(new FileReader(filename));

            String line;

            while ((line = in.readLine()) != null) {
                a.append(line);
                a.append('\n');
            }

            a.append('\n');
            in.close();

            return a.toString();
        } catch (Exception e) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {}
            }

            return "";
        }
    }

    /**
     * Adds a String to the Listbox of recent commands.
     */
    private void setRecent(String s) {

        for (int i = 0; i < iMaxRecent; i++) {
            if (s.equals(sRecent[i])) {
                return;
            }
        }

        if (sRecent[iRecent] != null) {
            choRecent.remove(sRecent[iRecent]);
        }

        sRecent[iRecent] = s;
        iRecent          = (iRecent + 1) % iMaxRecent;

        choRecent.addItem(s);
    }

    String[]   sRecent;
    static int iMaxRecent = 24;
    int        iRecent;
    TextArea   txtCommand;
    Button     butExecute, butScript;
    Button     butImport;
    Choice     choRecent;
    Grid       gResult;

    /**
     * Create the graphical user interface. This is AWT code.
     */
    private void initGUI() {

        // all panels
        Panel pQuery       = new Panel();
        Panel pCommand     = new Panel();
        Panel pButton      = new Panel();
        Panel pRecent      = new Panel();
        Panel pResult      = new Panel();
        Panel pBorderWest  = new Panel();
        Panel pBorderEast  = new Panel();
        Panel pBorderSouth = new Panel();

        pQuery.setLayout(new BorderLayout());
        pCommand.setLayout(new BorderLayout());
        pButton.setLayout(new BorderLayout());
        pRecent.setLayout(new BorderLayout());
        pResult.setLayout(new BorderLayout());
        pBorderWest.setBackground(SystemColor.control);
        pBorderSouth.setBackground(SystemColor.control);
        pBorderEast.setBackground(SystemColor.control);

        // labels
        Label lblCommand = new Label(" Command", Label.LEFT);
        Label lblRecent  = new Label(" Recent", Label.LEFT);
        Label lblResult  = new Label(" Result", Label.LEFT);

        lblCommand.setBackground(SystemColor.control);
        lblRecent.setBackground(SystemColor.control);
        lblResult.setBackground(SystemColor.control);

        // buttons
        butExecute = new Button("Execute");
        butScript  = new Button("Script");
        butImport  = new Button("Import");

        pButton.add("South", butScript);
        pButton.add("Center", butExecute);
        pButton.add("North", butImport);

        // command - textarea
        Font fFont = new Font("Dialog", Font.PLAIN, 12);

        txtCommand = new TextArea(5, 40);

        txtCommand.setFont(fFont);

        // recent - choice
        choRecent = new Choice();

        // result - grid
        gResult = new Grid();

        // combine it
        setLayout(new BorderLayout());
        pRecent.add("Center", choRecent);
        pRecent.add("North", lblRecent);
        pCommand.add("North", lblCommand);
        pCommand.add("East", pButton);
        pCommand.add("Center", txtCommand);
        pCommand.add("South", pRecent);
        pResult.add("North", lblResult);
        pResult.add("Center", gResult);
        pQuery.add("North", pCommand);
        pQuery.add("Center", pResult);
        add("Center", pQuery);
        add("West", pBorderWest);
        add("East", pBorderEast);
        add("South", pBorderSouth);

        // fredt@users 20011210 - patch 450412 by elise@users
        doLayout();
    }

    static String[] sTestData = {
        "drop table Place if exists",
        "create table Place (Code integer,Name varchar(255))",
        "create index iCode on Place (Code)", "delete from place",
        "insert into Place values (4900,'Langenthal')",
        "insert into Place values (8000,'Zurich')",
        "insert into Place values (3000,'Berne')",
        "insert into Place values (1200,'Geneva')",
        "insert into Place values (6900,'Lugano')",
        "drop table Customer if exists",
        "create table Customer (Nr integer,Name varchar(255),Place integer)",
        "create index iNr on Customer (Nr)", "delete from Customer",
        "insert into Customer values (1,'Meier',3000)",
        "insert into Customer values (2,'Mueller',8000)",
        "insert into Customer values (3,'Devaux',1200)",
        "insert into Customer values (4,'Rossi',6900)",
        "insert into Customer values (5,'Rickli',3000)",
        "insert into Customer values (6,'Graf',3000)",
        "insert into Customer values (7,'Mueller',4900)",
        "insert into Customer values (8,'May',1200)",
        "insert into Customer values (9,'Berger',8000)",
        "insert into Customer values (10,'D''Ascoli',6900)",
        "insert into Customer values (11,'Padruz',1200)",
        "insert into Customer values (12,'Hug',4900)"
    };

    /**
     * Method declaration
     *
     */
    void insertTestData() {

        for (int i = 0; i < sTestData.length; i++) {
            try {
                sStatement.executeQuery(sTestData[i]);
            } catch (SQLException e) {
                System.out.println("Exception: " + e);
            }
        }

        setRecent("select * from place");
        setRecent("select * from Customer");
        setRecent("select * from Customer where place<>3000");
        setRecent("select * from place where code>3000 or code=1200");
        setRecent("select * from Customer where nr<=8\nand name<>'Mueller'");
        setRecent("update Customer set name='Russi'\nwhere name='Rossi'");
        setRecent("delete from Customer where place=8000");
        setRecent("insert into place values(3600,'Thun')");
        setRecent("drop index Customer.iNr");
        setRecent("select * from Customer where name like '%e%'");
        setRecent("select count(*),min(code),max(code),sum(code) from place");

        String s = "select * from Customer,place\n"
                   + "where Customer.place=place.code\n"
                   + "and place.name='Berne'";

        setRecent(s);
        txtCommand.setText(s);
        txtCommand.selectAll();
    }

    /**
     * Method declaration
     *
     */
    static void printHelp() {

        System.out.println(
            "Usage: java QueryTool [-options]\n" + "where options include:\n"
            + "    -driver <classname>  name of the driver class\n"
            + "    -url <name>          first part of the jdbc url\n"
            + "    -database <name>     second part of the jdbc url\n"
            + "    -user <name>         username used for connection\n"
            + "    -password <name>     password for this user\n"
            + "    -test <true/false>   insert test data\n"
            + "    -log <true/false>    write log to system out");
        System.exit(0);
    }

    /**
     * Method declaration
     *
     *
     * @param e
     */
    public void windowActivated(WindowEvent e) {}

    /**
     * Method declaration
     *
     *
     * @param e
     */
    public void windowDeactivated(WindowEvent e) {}

    /**
     * Method declaration
     *
     *
     * @param e
     */
    public void windowClosed(WindowEvent e) {}

    /**
     * Method declaration
     *
     *
     * @param ev
     */
    public void windowClosing(WindowEvent ev) {

        try {
            cConn.close();
        } catch (Exception e) {}

        if (fMain != null) {
            fMain.dispose();
        }

        System.exit(0);
    }

    /**
     * Method declaration
     *
     *
     * @param e
     */
    public void windowDeiconified(WindowEvent e) {}

    /**
     * Method declaration
     *
     *
     * @param e
     */
    public void windowIconified(WindowEvent e) {}

    /**
     * Method declaration
     *
     *
     * @param e
     */
    public void windowOpened(WindowEvent e) {}

    /**
     * Method declaration
     *
     *
     * @param ev
     */
    public void actionPerformed(ActionEvent ev) {

        String s = ev.getActionCommand();

        if (s != null && s.equals("Exit")) {
            windowClosing(null);
        }
    }
}
