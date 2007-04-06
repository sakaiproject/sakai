package org.hsqldb.test;

// nbazin@users - enhancements to the original code
// fredt@users - 20050202 - corrected getRandomID(int) to return a randomly distributed value
/*
 *  This is a sample implementation of the Transaction Processing Performance
 *  Council Benchmark B coded in Java and ANSI SQL2.
 *
 *  This version is using one connection per thread to parallellize
 *  server operations.
 * @author Mark Matthews (mark@mysql.com)
 */
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Vector;

class JDBCBench {

    /* tpc bm b scaling rules */
    public static int tps       = 1;         /* the tps scaling factor: here it is 1 */
    public static int nbranches = 1;         /* number of branches in 1 tps db       */
    public static int ntellers  = 10;        /* number of tellers in  1 tps db       */
    public static int naccounts = 100000;    /* number of accounts in 1 tps db       */
    public static int nhistory = 864000;     /* number of history recs in 1 tps db   */
    public static final int TELLER              = 0;
    public static final int BRANCH              = 1;
    public static final int ACCOUNT             = 2;
    int                     failed_transactions = 0;
    int                     transaction_count   = 0;
    static int              n_clients           = 10;
    static int              n_txn_per_client    = 10;
    long                    start_time          = 0;
    static boolean          transactions        = true;
    static boolean          prepared_stmt       = false;
    static String           tableExtension      = "";
    static String           createExtension     = "";
    static String           ShutdownCommand     = "";
    static String           startupCommand      = "";
    static PrintStream      TabFile             = null;
    static boolean          verbose             = false;
    MemoryWatcherThread     MemoryWatcher;

    /* main program,    creates a 1-tps database:  i.e. 1 branch, 10 tellers,...
     *                    runs one TPC BM B transaction
     * example command line:
     * -driver  org.hsqldb.jdbcDriver -url jdbc:hsqldb:/hsql/jdbcbench/test -user sa -clients 20
     */
    public static void main(String[] Args) {

        String  DriverName         = "";
        String  DBUrl              = "";
        String  DBUser             = "";
        String  DBPassword         = "";
        boolean initialize_dataset = false;

        for (int i = 0; i < Args.length; i++) {
            if (Args[i].equals("-clients")) {
                if (i + 1 < Args.length) {
                    i++;

                    n_clients = Integer.parseInt(Args[i]);
                }
            } else if (Args[i].equals("-driver")) {
                if (i + 1 < Args.length) {
                    i++;

                    DriverName = Args[i];

                    if (DriverName.equals(
                            "org.enhydra.instantdb.jdbc.idbDriver")) {
                        ShutdownCommand = "SHUTDOWN";
                    }

                    if (DriverName.equals(
                            "com.borland.datastore.jdbc.DataStoreDriver")) {}

                    if (DriverName.equals("com.mckoi.JDBCDriver")) {
                        ShutdownCommand = "SHUTDOWN";
                    }

                    if (DriverName.equals("org.hsqldb.jdbcDriver")) {
                        tableExtension  = "CREATE CACHED TABLE ";
                        ShutdownCommand = "SHUTDOWN";
                        startupCommand  = "";
                    }
                }
            } else if (Args[i].equals("-url")) {
                if (i + 1 < Args.length) {
                    i++;

                    DBUrl = Args[i];
                }
            } else if (Args[i].equals("-user")) {
                if (i + 1 < Args.length) {
                    i++;

                    DBUser = Args[i];
                }
            } else if (Args[i].equals("-tabfile")) {
                if (i + 1 < Args.length) {
                    i++;

                    try {
                        FileOutputStream File = new FileOutputStream(Args[i]);

                        TabFile = new PrintStream(File);
                    } catch (Exception e) {
                        TabFile = null;
                    }
                }
            } else if (Args[i].equals("-password")) {
                if (i + 1 < Args.length) {
                    i++;

                    DBPassword = Args[i];
                }
            } else if (Args[i].equals("-tpc")) {
                if (i + 1 < Args.length) {
                    i++;

                    n_txn_per_client = Integer.parseInt(Args[i]);
                }
            } else if (Args[i].equals("-init")) {
                initialize_dataset = true;
            } else if (Args[i].equals("-tps")) {
                if (i + 1 < Args.length) {
                    i++;

                    tps = Integer.parseInt(Args[i]);
                }
            } else if (Args[i].equals("-v")) {
                verbose = true;
            }
        }

        if (DriverName.length() == 0 || DBUrl.length() == 0) {
            System.out.println(
                "usage: java JDBCBench -driver [driver_class_name] -url [url_to_db] -user [username] -password [password] [-v] [-init] [-tpc n] [-clients n]");
            System.out.println();
            System.out.println("-v          verbose error messages");
            System.out.println("-init       initialize the tables");
            System.out.println("-tpc        transactions per client");
            System.out.println("-clients    number of simultaneous clients");
            System.exit(-1);
        }

        System.out.println(
            "*********************************************************");
        System.out.println(
            "* JDBCBench v1.1                                        *");
        System.out.println(
            "*********************************************************");
        System.out.println();
        System.out.println("Driver: " + DriverName);
        System.out.println("URL:" + DBUrl);
        System.out.println();
        System.out.println("Scale factor value: " + tps);
        System.out.println("Number of clients: " + n_clients);
        System.out.println("Number of transactions per client: "
                           + n_txn_per_client);
        System.out.println();

        try {
            Class.forName(DriverName);

            JDBCBench Me = new JDBCBench(DBUrl, DBUser, DBPassword,
                                         initialize_dataset);
        } catch (Exception E) {
            System.out.println(E.getMessage());
            E.printStackTrace();
        }
    }

    public JDBCBench(String url, String user, String password, boolean init) {

        Vector      vClient  = new Vector();
        Thread      Client   = null;
        Enumeration e        = null;
        Connection  guardian = null;

        try {
            if (init) {
                System.out.println("Start: "
                                   + (new java.util.Date()).toString());
                System.out.print("Initializing dataset...");
                createDatabase(url, user, password);
                System.out.println("done.\n");
                System.out.println("Complete: "
                                   + (new java.util.Date()).toString());
            }

            guardian = connect(url, user, password);

            if (startupCommand.length() != 0) {
                Statement statement = guardian.createStatement();

                statement.execute(startupCommand);
                statement.close();
            }

            System.out.println("* Starting Benchmark Run *");

            MemoryWatcher = new MemoryWatcherThread();

            MemoryWatcher.start();

            transactions  = true;
            prepared_stmt = false;
            start_time    = System.currentTimeMillis();

            for (int i = 0; i < n_clients; i++) {
                Client = new ClientThread(n_txn_per_client, url, user,
                                          password);

                Client.start();
                vClient.addElement(Client);
            }

            /*
             ** Barrier to complete this test session
             */
            e = vClient.elements();

            while (e.hasMoreElements()) {
                Client = (Thread) e.nextElement();

                Client.join();
            }

            vClient.removeAllElements();
            reportDone();
            checkSums(guardian);

            // debug - allows stopping the test
            if (!transactions) {
                throw new Exception("end after one round");
            }

            transactions  = true;
            prepared_stmt = false;
            start_time    = System.currentTimeMillis();

            for (int i = 0; i < n_clients; i++) {
                Client = new ClientThread(n_txn_per_client, url, user,
                                          password);

                Client.start();
                vClient.addElement(Client);
            }

            /*
             ** Barrier to complete this test session
             */
            e = vClient.elements();

            while (e.hasMoreElements()) {
                Client = (Thread) e.nextElement();

                Client.join();
            }

            vClient.removeAllElements();
            reportDone();
            checkSums(guardian);

            transactions  = true;
            prepared_stmt = true;
            start_time    = System.currentTimeMillis();

            for (int i = 0; i < n_clients; i++) {
                Client = new ClientThread(n_txn_per_client, url, user,
                                          password);

                Client.start();
                vClient.addElement(Client);
            }

            /*
             ** Barrier to complete this test session
             */
            e = vClient.elements();

            while (e.hasMoreElements()) {
                Client = (Thread) e.nextElement();

                Client.join();
            }

            vClient.removeAllElements();
            reportDone();
            checkSums(guardian);

            transactions  = true;
            prepared_stmt = true;
            start_time    = System.currentTimeMillis();

            for (int i = 0; i < n_clients; i++) {
                Client = new ClientThread(n_txn_per_client, url, user,
                                          password);

                Client.start();
                vClient.addElement(Client);
            }

            /*
             ** Barrier to complete this test session
             */
            e = vClient.elements();

            while (e.hasMoreElements()) {
                Client = (Thread) e.nextElement();

                Client.join();
            }

            vClient.removeAllElements();
            reportDone();
            checkSums(guardian);
        } catch (Exception E) {
            System.out.println(E.getMessage());
            E.printStackTrace();
        } finally {
            MemoryWatcher.end();

            try {
                MemoryWatcher.join();

                if (ShutdownCommand.length() > 0) {
                    Statement Stmt = guardian.createStatement();

                    Stmt.execute(ShutdownCommand);
                    Stmt.close();
                    connectClose(guardian);
                }

                if (TabFile != null) {
                    TabFile.close();
                }
            } catch (Exception E1) {}

//            System.exit(0);
        }
    }

    public void reportDone() {

        long end_time = System.currentTimeMillis();
        double completion_time = ((double) end_time - (double) start_time)
                                 / 1000;

        if (TabFile != null) {
            TabFile.print(tps + ";" + n_clients + ";" + n_txn_per_client
                          + ";");
        }

        System.out.println("\n* Benchmark Report *");
        System.out.print("* Featuring ");

        if (prepared_stmt) {
            System.out.print("<prepared statements> ");

            if (TabFile != null) {
                TabFile.print("<prepared statements>;");
            }
        } else {
            System.out.print("<direct queries> ");

            if (TabFile != null) {
                TabFile.print("<direct queries>;");
            }
        }

        if (transactions) {
            System.out.print("<transactions> ");

            if (TabFile != null) {
                TabFile.print("<transactions>;");
            }
        } else {
            System.out.print("<auto-commit> ");

            if (TabFile != null) {
                TabFile.print("<auto-commit>;");
            }
        }

        System.out.println("\n--------------------");
        System.out.println("Time to execute " + transaction_count
                           + " transactions: " + completion_time
                           + " seconds.");
        System.out.println("Max/Min memory usage: " + MemoryWatcher.max
                           + " / " + MemoryWatcher.min + " kb");
        System.out.println(failed_transactions + " / " + transaction_count
                           + " failed to complete.");

        double rate = (transaction_count - failed_transactions)
                      / completion_time;

        System.out.println("Transaction rate: " + rate + " txn/sec.");

        if (TabFile != null) {
            TabFile.print(MemoryWatcher.max + ";" + MemoryWatcher.min + ";"
                          + failed_transactions + ";" + rate + "\n");
        }

        transaction_count   = 0;
        failed_transactions = 0;

        MemoryWatcher.reset();
    }

    public synchronized void incrementTransactionCount() {
        transaction_count++;
    }

    public synchronized void incrementFailedTransactionCount() {
        failed_transactions++;
    }

    void createDatabase(String url, String user,
                        String password) throws Exception {

        Connection Conn = connect(url, user, password);
        ;
        String     s    = Conn.getMetaData().getDatabaseProductName();

        System.out.println("DBMS: " + s);

        transactions = true;

        if (transactions) {
            try {
                Conn.setAutoCommit(false);
                System.out.println("In transaction mode");
            } catch (SQLException Etrxn) {
                transactions = false;
            }
        }

        try {
            int       accountsnb = 0;
            Statement Stmt       = Conn.createStatement();
            String    Query;

            Query = "SELECT count(*) ";
            Query += "FROM   accounts";

            ResultSet RS = Stmt.executeQuery(Query);

            Stmt.clearWarnings();

            while (RS.next()) {
                accountsnb = RS.getInt(1);
            }

            if (transactions) {
                Conn.commit();
            }

            Stmt.close();

            if (accountsnb == (naccounts * tps)) {
                System.out.println("Already initialized");
                connectClose(Conn);

                return;
            }
        } catch (Exception E) {}

        System.out.println("Drop old tables if they exist");

        try {
            Statement Stmt = Conn.createStatement();
            String    Query;

            Query = "DROP TABLE history";

            Stmt.execute(Query);
            Stmt.clearWarnings();

            Query = "DROP TABLE accounts";

            Stmt.execute(Query);
            Stmt.clearWarnings();

            Query = "DROP TABLE tellers";

            Stmt.execute(Query);
            Stmt.clearWarnings();

            Query = "DROP TABLE branches";

            Stmt.execute(Query);
            Stmt.clearWarnings();

            if (transactions) {
                Conn.commit();
            }

            Stmt.close();
        } catch (Exception E) {}

        System.out.println("Creates tables");

        try {
            Statement Stmt = Conn.createStatement();
            String    Query;

            if (tableExtension.length() > 0) {
                Query = tableExtension + " branches (";
            } else {
                Query = "CREATE TABLE branches (";
            }

            Query += "Bid         INTEGER NOT NULL PRIMARY KEY, ";
            Query += "Bbalance    INTEGER,";
            Query += "filler      CHAR(88))";    /* pad to 100 bytes */

            if (createExtension.length() > 0) {
                Query += createExtension;
            }

            Stmt.execute(Query);
            Stmt.clearWarnings();

            if (tableExtension.length() > 0) {
                Query = tableExtension + " tellers (";
            } else {
                Query = "CREATE TABLE tellers (";
            }

            Query += "Tid         INTEGER NOT NULL PRIMARY KEY,";
            Query += "Bid         INTEGER,";
            Query += "Tbalance    INTEGER,";
            Query += "filler      CHAR(84))";    /* pad to 100 bytes */

            if (createExtension.length() > 0) {
                Query += createExtension;
            }

            Stmt.execute(Query);
            Stmt.clearWarnings();

            if (tableExtension.length() > 0) {
                Query = tableExtension + " accounts (";
            } else {
                Query = "CREATE TABLE accounts (";
            }

            Query += "Aid         INTEGER NOT NULL PRIMARY KEY, ";
            Query += "Bid         INTEGER, ";
            Query += "Abalance    INTEGER, ";
            Query += "filler      CHAR(84))";    /* pad to 100 bytes */

            if (createExtension.length() > 0) {
                Query += createExtension;
            }

            Stmt.execute(Query);
            Stmt.clearWarnings();

            if (tableExtension.length() > 0) {
                Query = tableExtension + " history (";
            } else {
                Query = "CREATE TABLE history (";
            }

            Query += "Tid         INTEGER, ";
            Query += "Bid         INTEGER, ";
            Query += "Aid         INTEGER, ";
            Query += "delta       INTEGER, ";
            Query += "tstime        TIMESTAMP, ";
            Query += "filler      CHAR(22))";    /* pad to 50 bytes  */

            if (createExtension.length() > 0) {
                Query += createExtension;
            }

            Stmt.execute(Query);
            Stmt.clearWarnings();
            Stmt.execute("SET TABLE ACCOUNTS SOURCE \"ACCOUNTS.TXT\"");
            Stmt.execute("SET TABLE BRANCHES SOURCE \"BBRANCHES.TXT\"");
            Stmt.execute("SET TABLE TELLERS SOURCE \"TELLERS.TXT\"");
            Stmt.execute("SET TABLE HISTORY SOURCE \"HISTORY.TXT\"");

            if (transactions) {
                Conn.commit();
            }

            Stmt.close();
        } catch (Exception E) {
            System.out.println(
                "Delete elements in table in case Drop didn't work");
        }

        System.out.println(
            "Delete elements in table in case Drop didn't work");

        try {
            Statement Stmt = Conn.createStatement();
            String    Query;

            Query = "DELETE FROM history";

            Stmt.execute(Query);
            Stmt.clearWarnings();

            Query = "DELETE FROM accounts";

            Stmt.execute(Query);
            Stmt.clearWarnings();

            Query = "DELETE FROM tellers";

            Stmt.execute(Query);
            Stmt.clearWarnings();

            Query = "DELETE FROM branches";

            Stmt.execute(Query);
            Stmt.clearWarnings();

            if (transactions) {
                Conn.commit();
            }

            /* prime database using TPC BM B scaling rules.
             **  Note that for each branch and teller:
             **      branch_id = teller_id  / ntellers
             **      branch_id = account_id / naccounts
             */
            PreparedStatement pstmt = null;

            prepared_stmt = true;

            if (prepared_stmt) {
                try {
                    Query = "INSERT INTO branches(Bid,Bbalance) VALUES (?,0)";
                    pstmt = Conn.prepareStatement(Query);

                    System.out.println("Using prepared statements");
                } catch (SQLException Epstmt) {
                    pstmt         = null;
                    prepared_stmt = false;
                }
            }

            System.out.println("Insert data in branches table");

            for (int i = 0; i < nbranches * tps; i++) {
                if (prepared_stmt) {
                    pstmt.setInt(1, i);
                    pstmt.executeUpdate();
                    pstmt.clearWarnings();
                } else {
                    Query = "INSERT INTO branches(Bid,Bbalance) VALUES (" + i
                            + ",0)";

                    Stmt.executeUpdate(Query);
                }

                if ((i % 100 == 0) && (transactions)) {
                    Conn.commit();
                }
            }

            if (prepared_stmt) {
                pstmt.close();
            }

            if (transactions) {
                Conn.commit();
            }

            if (prepared_stmt) {
                Query =
                    "INSERT INTO tellers(Tid,Bid,Tbalance) VALUES (?,?,0)";
                pstmt = Conn.prepareStatement(Query);
            }

            System.out.println("Insert data in tellers table");

            for (int i = 0; i < ntellers * tps; i++) {
                if (prepared_stmt) {
                    pstmt.setInt(1, i);
                    pstmt.setInt(2, i / ntellers);
                    pstmt.executeUpdate();
                    pstmt.clearWarnings();
                } else {
                    Query = "INSERT INTO tellers(Tid,Bid,Tbalance) VALUES ("
                            + i + "," + i / ntellers + ",0)";

                    Stmt.executeUpdate(Query);
                }

                if ((i % 100 == 0) && (transactions)) {
                    Conn.commit();
                }
            }

            if (prepared_stmt) {
                pstmt.close();
            }

            if (transactions) {
                Conn.commit();
            }

            if (prepared_stmt) {
                Query =
                    "INSERT INTO accounts(Aid,Bid,Abalance) VALUES (?,?,0)";
                pstmt = Conn.prepareStatement(Query);
            }

            System.out.println("Insert data in accounts table");

            for (int i = 0; i < naccounts * tps; i++) {
                if (prepared_stmt) {
                    pstmt.setInt(1, i);
                    pstmt.setInt(2, i / naccounts);
                    pstmt.executeUpdate();
                    pstmt.clearWarnings();
                } else {
                    Query = "INSERT INTO accounts(Aid,Bid,Abalance) VALUES ("
                            + i + "," + i / naccounts + ",0)";

                    Stmt.executeUpdate(Query);
                }

                if ((i % 10000 == 0) && (transactions)) {
                    Conn.commit();
                }

                if ((i > 0) && ((i % 10000) == 0)) {
                    System.out.println("\t" + i + "\t records inserted");
                }
            }

            if (prepared_stmt) {
                pstmt.close();
            }

            if (transactions) {
                Conn.commit();
            }

            System.out.println("\t" + (naccounts * tps)
                               + "\t records inserted");

            // for tests
            Stmt.execute(ShutdownCommand);
            Stmt.close();
        } catch (Exception E) {
            System.out.println(E.getMessage());
            E.printStackTrace();
        }

        connectClose(Conn);
    }    /* end of CreateDatabase    */

    public static int getRandomInt(int lo, int hi) {

        int ret = 0;

        ret = (int) (Math.random() * (hi - lo + 1));
        ret += lo;

        return ret;
    }

    public static int getRandomID(int type) {

        int min = 0,
            max = 0;

        switch (type) {

            case TELLER :
                max = ntellers * tps - 1;
                break;

            case BRANCH :
                max = nbranches * tps - 1;
                break;

            case ACCOUNT :
                max = naccounts * tps - 1;
                break;
        }

        return (getRandomInt(min, max));
    }

    public static Connection connect(String DBUrl, String DBUser,
                                     String DBPassword) {

        try {
            Connection conn = DriverManager.getConnection(DBUrl, DBUser,
                DBPassword);

            return conn;
        } catch (Exception E) {
            System.out.println(E.getMessage());
            E.printStackTrace();
        }

        return null;
    }

    public static void connectClose(Connection c) {

        if (c == null) {
            return;
        }

        try {
            c.close();
        } catch (Exception E) {
            System.out.println(E.getMessage());
            E.printStackTrace();
        }
    }

    void checkSums(Connection conn) throws Exception {

        Statement st1 = null;
        ResultSet rs  = null;
        int       bbalancesum;
        int       tbalancesum;
        int       abalancesum;
        int       deltasum;

        try {
            st1 = conn.createStatement();
            rs  = st1.executeQuery("select sum(bbalance) from branches");

            rs.next();

            bbalancesum = rs.getInt(1);

            rs.close();

            rs = st1.executeQuery("select sum(tbalance) from tellers");

            rs.next();

            tbalancesum = rs.getInt(1);

            rs.close();

            rs = st1.executeQuery("select sum(abalance) from accounts");

            rs.next();

            abalancesum = rs.getInt(1);

            rs.close();

            rs = st1.executeQuery("select sum(delta) from history");

            rs.next();

            deltasum = rs.getInt(1);

            rs.close();

            rs = null;

            st1.close();

            st1 = null;

            if (abalancesum != bbalancesum || bbalancesum != tbalancesum
                    || tbalancesum != deltasum) {
                System.out.println("sums don't match!");
            } else {
                System.out.println("sums match!");
            }

            System.out.println(abalancesum + " " + bbalancesum + " "
                               + tbalancesum + " " + deltasum);
        } finally {
            if (st1 != null) {
                st1.close();
            }
        }
    }

    class ClientThread extends Thread {

        int               ntrans = 0;
        Connection        Conn;
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        PreparedStatement pstmt3 = null;
        PreparedStatement pstmt4 = null;
        PreparedStatement pstmt5 = null;

        public ClientThread(int number_of_txns, String url, String user,
                            String password) {

            ntrans = number_of_txns;
            Conn   = connect(url, user, password);

            if (Conn == null) {
                return;
            }

            try {
                if (transactions) {
                    Conn.setAutoCommit(false);
                }

                if (prepared_stmt) {
                    String Query;

                    Query  = "UPDATE accounts ";
                    Query  += "SET     Abalance = Abalance + ? ";
                    Query  += "WHERE   Aid = ?";
                    pstmt1 = Conn.prepareStatement(Query);
                    Query  = "SELECT Abalance ";
                    Query  += "FROM   accounts ";
                    Query  += "WHERE  Aid = ?";
                    pstmt2 = Conn.prepareStatement(Query);
                    Query  = "UPDATE tellers ";
                    Query  += "SET    Tbalance = Tbalance + ? ";
                    Query  += "WHERE  Tid = ?";
                    pstmt3 = Conn.prepareStatement(Query);
                    Query  = "UPDATE branches ";
                    Query  += "SET    Bbalance = Bbalance + ? ";
                    Query  += "WHERE  Bid = ?";
                    pstmt4 = Conn.prepareStatement(Query);
                    Query  = "INSERT INTO history(Tid, Bid, Aid, delta) ";
                    Query  += "VALUES (?,?,?,?)";
                    pstmt5 = Conn.prepareStatement(Query);
                }
            } catch (Exception E) {
                System.out.println(E.getMessage());
                E.printStackTrace();
            }
        }

        public void run() {

            while (ntrans-- > 0) {
                int account = JDBCBench.getRandomID(ACCOUNT);
                int branch  = JDBCBench.getRandomID(BRANCH);
                int teller  = JDBCBench.getRandomID(TELLER);
                int delta   = JDBCBench.getRandomInt(0, 1000);

                doOne(branch, teller, account, delta);
                incrementTransactionCount();
            }

            if (prepared_stmt) {
                try {
                    if (pstmt1 != null) {
                        pstmt1.close();
                    }

                    if (pstmt2 != null) {
                        pstmt2.close();
                    }

                    if (pstmt3 != null) {
                        pstmt3.close();
                    }

                    if (pstmt4 != null) {
                        pstmt4.close();
                    }

                    if (pstmt5 != null) {
                        pstmt5.close();
                    }
                } catch (Exception E) {
                    System.out.println(E.getMessage());
                    E.printStackTrace();
                }
            }

            connectClose(Conn);

            Conn = null;
        }

        /*
         **  doOne() - Executes a single TPC BM B transaction.
         */
        int doOne(int bid, int tid, int aid, int delta) {

            int aBalance = 0;

            if (Conn == null) {
                incrementFailedTransactionCount();

                return 0;
            }

            try {
                if (prepared_stmt) {
                    pstmt1.setInt(1, delta);
                    pstmt1.setInt(2, aid);
                    pstmt1.executeUpdate();
                    pstmt1.clearWarnings();
                    pstmt2.setInt(1, aid);

                    ResultSet RS = pstmt2.executeQuery();

                    pstmt2.clearWarnings();

                    while (RS.next()) {
                        aBalance = RS.getInt(1);
                    }

                    pstmt3.setInt(1, delta);
                    pstmt3.setInt(2, tid);
                    pstmt3.executeUpdate();
                    pstmt3.clearWarnings();
                    pstmt4.setInt(1, delta);
                    pstmt4.setInt(2, bid);
                    pstmt4.executeUpdate();
                    pstmt4.clearWarnings();
                    pstmt5.setInt(1, tid);
                    pstmt5.setInt(2, bid);
                    pstmt5.setInt(3, aid);
                    pstmt5.setInt(4, delta);
                    pstmt5.executeUpdate();
                    pstmt5.clearWarnings();
                } else {
                    Statement Stmt  = Conn.createStatement();
                    String    Query = "UPDATE accounts ";

                    Query += "SET     Abalance = Abalance + " + delta + " ";
                    Query += "WHERE   Aid = " + aid;

                    int res = Stmt.executeUpdate(Query);

                    Stmt.clearWarnings();

                    Query = "SELECT Abalance ";
                    Query += "FROM   accounts ";
                    Query += "WHERE  Aid = " + aid;

                    ResultSet RS = Stmt.executeQuery(Query);

                    Stmt.clearWarnings();

                    while (RS.next()) {
                        aBalance = RS.getInt(1);
                    }

                    Query = "UPDATE tellers ";
                    Query += "SET    Tbalance = Tbalance + " + delta + " ";
                    Query += "WHERE  Tid = " + tid;

                    Stmt.executeUpdate(Query);
                    Stmt.clearWarnings();

                    Query = "UPDATE branches ";
                    Query += "SET    Bbalance = Bbalance + " + delta + " ";
                    Query += "WHERE  Bid = " + bid;

                    Stmt.executeUpdate(Query);
                    Stmt.clearWarnings();

                    Query = "INSERT INTO history(Tid, Bid, Aid, delta) ";
                    Query += "VALUES (";
                    Query += tid + ",";
                    Query += bid + ",";
                    Query += aid + ",";
                    Query += delta + ")";

                    Stmt.executeUpdate(Query);
                    Stmt.clearWarnings();
                    Stmt.close();
                }

                if (transactions) {
                    Conn.commit();
                }

                return aBalance;
            } catch (Exception E) {
                if (verbose) {
                    System.out.println("Transaction failed: "
                                       + E.getMessage());
                    E.printStackTrace();
                }

                incrementFailedTransactionCount();

                if (transactions) {
                    try {
                        Conn.rollback();
                    } catch (SQLException E1) {}
                }
            }

            return 0;
        }    /* end of DoOne         */
    }    /* end of class ClientThread */

    class MemoryWatcherThread extends Thread {

        long    min          = 0;
        long    max          = 0;
        boolean keep_running = true;

        public MemoryWatcherThread() {

            this.reset();

            keep_running = true;
        }

        public void reset() {

            System.gc();

            long currentFree  = Runtime.getRuntime().freeMemory();
            long currentAlloc = Runtime.getRuntime().totalMemory();

            min = max = (currentAlloc - currentFree);
        }

        public void end() {
            keep_running = false;
        }

        public void run() {

            while (keep_running) {
                long currentFree  = Runtime.getRuntime().freeMemory();
                long currentAlloc = Runtime.getRuntime().totalMemory();
                long used         = currentAlloc - currentFree;

                if (used < min) {
                    min = used;
                }

                if (used > max) {
                    max = used;
                }

                try {
                    sleep(100);
                } catch (InterruptedException E) {}
            }
        }
    }    /* end of class MemoryWatcherThread */
}
