package org.sakaiproject.progress.impl;

import lombok.Setter;
import lombok.Getter;

import org.sakaiproject.progress.api.ProgressService;
import org.sakaiproject.progress.api.ProgressServiceException;
import org.sakaiproject.progress.api.persistence.Configuration;
import org.sakaiproject.tool.gradebook.Gradebook;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.db.cover.SqlService;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.sakaiproject.progress.model.Progress;
import java.util.List;
import java.time.Instant;
import java.util.Objects;

/**
 * Implementation of the persistence layer for getting and setting configurations and progress entities. Uses JPA
 *
 */
@Slf4j
public class ProgressServiceImpl implements ProgressService {
	
	@Setter @Getter private Configuration configuration;

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


    public void addProgress(Progress progress){
        Objects.requireNonNull(progress);

        if(progress.getId() != null){
            throw new IllegalArgumentException("Can't persist a new progress with a non null id");
        }

        Instant now = Instant.now();
        progress.setDateCreated(now);
        progress.setDateEdited(now);

        EntityManagerFactory emFactory = Persistence.createEntityManagerFactory("progress");
        EntityManager em = emFactory.createEntityManager();
        em.getTransaction().begin();
        em.persist(progress);
        em.getTransaction().commit();
    }

    public void updateProgess(Progress progress){
        Objects.requireNonNull(progress);

        EntityManagerFactory emFactory = Persistence.createEntityManagerFactory("progress");
        EntityManager em = emFactory.createEntityManager();
        Progress updatedProgress = em.find(Progress.class, progress.getId());
        Instant now = Instant.now();
        progress.setDateEdited(now);

        em.getTransaction().begin();
        updatedProgress.update(progress);
        em.getTransaction().commit();
    }

    public Progress getProgress(String id){
        EntityManagerFactory emFactory = Persistence.createEntityManagerFactory("progress");
        EntityManager em = emFactory.createEntityManager();
        Progress progress = em.find(Progress.class, id);

        return progress;
    }

    public void deleteProgress(String id){
        EntityManagerFactory emFactory = Persistence.createEntityManagerFactory("progress");
        EntityManager em = emFactory.createEntityManager();
        Progress progress = em.find(Progress.class, id);

        em.getTransaction().begin();
        em.remove(progress);
        em.getTransaction().commit();
    }


    @Override
    public List<Progress> getAllProgress(){
        //TODO: Finish this method once it is understood how progress items are saved
        return null; //Returns null until method is implemented
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
