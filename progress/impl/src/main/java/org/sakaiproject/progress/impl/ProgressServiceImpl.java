package org.sakaiproject.progress.impl;

import org.sakaiproject.progress.api.ProgressService;
import org.sakaiproject.progress.api.ProgressServiceException;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.db.cover.SqlService;
import lombok.extern.slf4j.Slf4j;


/**
 * Implementation of the persistence layer for getting and setting configurations
 * TODO: Write this
 */
@Slf4j
public class ProgressServiceImpl implements ProgressService {

    public void init(){
        if (ServerConfigurationService.getBoolean("auto.ddl", false)) {
            initDB(ServerConfigurationService.getString("vendor@org.sakaiproject.db.api.SqlService"));
        }
    }

    @Override
    public void getConfig() {

    }

    @Override
    public void setConfig() {

    }
    
    private void initDB(String dbType) {
        String initFile = "db/init/" + dbType + ".sql";
        InputStream is = ProgressServiceImpl.class.getClassLoader().getResourceAsStream(initFile);

        if (is == null) {
            throw new ProgressServiceException("Failed to find database init file: " + initFile);
        }

        InputStreamReader initInput = new InputStreamReader(is);

        try {
            Connection db = SqlService.borrowConnection();

            try {
                for (String sql : parseInitFile(initInput)) {
                    try {
                    	log.warn("Executing SQL statement: " + sql);
                        PreparedStatement ps = db.prepareStatement(sql);
                        ps.execute();
                        ps.close();
                    } catch (SQLException e) {
                        log.warn("runDBI: " + e + "(sql: " + sql + ")");
                    }
                }
            } catch (IOException e) {
                throw new ProgressServiceException("Failed to read migration file: " + initFile, e);
            } finally {
                SqlService.returnConnection(db);

                try {
                    initInput.close();
                } catch (IOException e) {}
            }
        } catch (SQLException e) {
            throw new ProgressServiceException("Database migration failed", e);
        }
    }
    
    private String[] parseInitFile(InputStreamReader migrationInput) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[4096];

        int len;
        while ((len = migrationInput.read(buf)) > 0) {
            sb.append(buf, 0, len);
        }

        return sb.toString().replace("\n", " ").split(";\\s*");
    }
}
