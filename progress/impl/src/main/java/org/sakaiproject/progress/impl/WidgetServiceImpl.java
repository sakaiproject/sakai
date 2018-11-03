package org.sakaiproject.widget.impl;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.widget.api.WidgetService;
import org.sakaiproject.widget.api.WidgetServiceException;
import org.sakaiproject.widget.api.persistence.WidgetRepository;
import org.sakaiproject.widget.model.Widget;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

/**
 * This is main service for widgets, all the buisiness logic about widgets should
 * exist here. The service should try to have unit tests that on every method in
 * its api. The service will be used by the tool and possibly from other services.
 */
@Slf4j
@Transactional
public class WidgetServiceImpl implements WidgetService {

    @Setter private WidgetRepository widgetRepository;

    public void init() {
        if (ServerConfigurationService.getBoolean("auto.ddl", false)) {
            initDB(ServerConfigurationService.getString("vendor@org.sakaiproject.db.api.SqlService"));
        }

    }

    @Override
    public Set<Widget> getWidgetsForSiteWithStatus(String site, Widget.STATUS... statuses) {
        if (StringUtils.isBlank(site)) {
            return Collections.emptySet();
        }
        return new HashSet<>(widgetRepository.getWidgetsForSiteWithStatus(site, statuses));
    }

    @Override
    public void addWidget(Widget widget) {
        Objects.requireNonNull(widget);

        if (widget.getId() != null) {
            throw new IllegalArgumentException("Can't persist a new widget with a non null id.");
        }
        if (widget.getContext() == null) {
            throw new IllegalArgumentException("Widget must have a context.");
        }

        widget.setStatus(Widget.STATUS.UNLOCKED);
        Instant now = Instant.now();
        widget.setDateCreated(now);
        widget.setDateModified(now);

        // Save the widget and verify
        Widget saved = widgetRepository.save(widget);
        if (saved == null || StringUtils.isBlank(saved.getId())) {
            throw new IllegalArgumentException("Failed to persist the widget: " + widget.toString());
        }
    }

    @Override
    public void updateWidget(Widget widget) {
        Objects.requireNonNull(widget);
        widget.setDateModified(Instant.now());
        widgetRepository.update(widget);
    }

    @Override
    public Optional<Widget> getWidget(String id) {
        if (StringUtils.isBlank(id)) return Optional.empty();
        return Optional.ofNullable(widgetRepository.findOne(id));
    }

    @Override
    public void deleteWidget(String id) {
        widgetRepository.delete(id);
    }
    
    private void initDB(String dbType) {
        String initFile = "db/init/" + dbType + ".sql";
        InputStream is = WidgetServiceImpl.class.getClassLoader().getResourceAsStream(initFile);

        if (is == null) {
            throw new WidgetServiceException("Failed to find database init file: " + initFile);
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
                throw new WidgetServiceException("Failed to read migration file: " + initFile, e);
            } finally {
                SqlService.returnConnection(db);

                try {
                    initInput.close();
                } catch (IOException e) {}
            }
        } catch (SQLException e) {
            throw new WidgetServiceException("Database migration failed", e);
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
