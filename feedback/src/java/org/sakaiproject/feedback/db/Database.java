/********************************************************************************** 
 * $URL$ 
 * $Id$ 
 *********************************************************************************** 
 * 
 * Copyright (c) 2011 The Sakai Foundation 
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.osedu.org/licenses/ECL-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **********************************************************************************/ 

package org.sakaiproject.feedback.db;

import java.sql.Connection;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.db.api.SqlService;

@Slf4j
public class Database {

    public final static String DB_ERROR = "DB_ERROR";

    private SqlService sqlService;
    public void setSqlService(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    private String mysqlInsertReportSql
        = "INSERT INTO sakai_feedback (user_id, email, site_id, report_type, title, content) VALUES(?,?,?,?,?,?)";

    private String oracleInsertReportSql
        = "INSERT INTO sakai_feedback (id, user_id, email, site_id, report_type, title, content) VALUES(sakai_feedback_seq.nextval, ?,?,?,?,?,?)";

    public void init() {
        
        sqlService.ddl(this.getClass().getClassLoader(), "createtables");
        sqlService.ddl(this.getClass().getClassLoader(), "createcontactustool");
    }

    public void logReport(String userId, String email, String siteId, String type, String title, String content) throws SQLException {

        Connection conn = null;

        try {
            conn = sqlService.borrowConnection();
            String insertReportSql = mysqlInsertReportSql;
            
            //If the vendor is oracle, should use the Oracle syntax using the Sequence as new identifier
            if("oracle".equals(sqlService.getVendor())){
                insertReportSql = oracleInsertReportSql;
            }
            
            sqlService.dbWrite(conn, insertReportSql, new String[] {userId, email, siteId, type, title, content});
        } catch (SQLException sqlException){
            log.error("Failed to insert feedback report. Caught sql exception while generating report. '" + DB_ERROR + "' will be returned to the client.", sqlException);
            throw sqlException;
        } finally {
            if (conn != null) {
                conn.commit();
                sqlService.returnConnection(conn);
            }
        }
    }
}
