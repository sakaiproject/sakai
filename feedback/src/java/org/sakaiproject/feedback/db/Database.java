package org.sakaiproject.feedback.db;

import java.sql.Connection;
import java.sql.SQLException;

import org.sakaiproject.db.api.SqlService;
import org.apache.log4j.Logger;

public class Database {

	private Logger logger = Logger.getLogger(Database.class);

    public final static String DB_ERROR = "DB_ERROR";

    private SqlService sqlService;
    public void setSqlService(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    private String insertReportSql
        = "INSERT INTO sakai_feedback (user_id, email, site_id, report_type, title, content) VALUES(?,?,?,?,?,?)";

    public void init() {
        
        sqlService.ddl(this.getClass().getClassLoader(), "createtables");
        sqlService.ddl(this.getClass().getClassLoader(), "createcontactustool");
    }

    public void logReport(String userId, String email, String siteId, String type, String title, String content) throws SQLException {

        Connection conn = null;

        try {
            conn = sqlService.borrowConnection();
            sqlService.dbWrite(conn, insertReportSql, new String[] {userId, email, siteId, type, title, content});
        } catch (SQLException sqlException){
            logger.error("Failed to insert feedback report. Caught sql exception while generating report. '" + DB_ERROR + "' will be returned to the client.", sqlException);
            throw sqlException;
        } finally {
            if (conn != null) {
                sqlService.returnConnection(conn);
            }
        }
    }
}
