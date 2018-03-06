/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.content.impl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.MethodUtils;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import org.sakaiproject.content.api.FileSystemHandler;
import org.sakaiproject.content.impl.ContentServiceSqlDefault;

/**
 * This is a utility class to convert the storage from one FileSystem to
 * another.
 *
 * @author Jaques
 */
@Slf4j
public class StorageConverter {
    /**
     * The datasource for the database connections.
     */
    private DataSource dataSource;

    /**
     * The connection to the database.
     */
    private Connection connection;

    /**
     * The database connection driver.
     */
    private String connectionDriver;

    /**
     * The database connection URL.
     */
    private String connectionURL;

    /**
     * The database connection username.
     */
    private String connectionUsername;

    /**
     * The database connection password.
     */
    private String connectionPassword;

    /**
     * The sql to retrieve the content id's and paths.
     * The id field must be the first field.
     * The path field must be the second field.
     */
    private String contentSql = new ContentServiceSqlDefault().getResourceIdAndFilePath();

    /**
     * The root body path for the source resources.
     */
    private String sourceBodyPath;

    /**
     * The root body path for the destination resources.
     */
    private String destinationBodyPath;

    /**
     * The source file system handler.
     */
    private FileSystemHandler sourceFileSystemHandler;

    /**
     * The destination file system handler.
     */
    private FileSystemHandler destinationFileSystemHandler;

    /**
     * Whether to delete the resources from the source.
     */
    private boolean deleteFromSource = false;

    /**
     * Whether to ignore missing resources.
     */
    private boolean ignoreMissing = true;

    /**
     * Set the datasource for the database connections.
     * Either the datasource, connection or the connection details (driver, url,
     * username and password) must be set.
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Set the connection to the database.
     * Either the datasource, connection or the connection details (driver, url,
     * username and password) must be set.
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * Set the database connection driver.
     * Either the datasource, connection or the connection details (driver, url,
     * username and password) must be set.
     */
    public void setConnectionDriver(String connectionDriver) {
        this.connectionDriver = connectionDriver;
    }

    /**
     * Set the database connection URL.
     * Either the datasource, connection or the connection details (driver, url,
     * username and password) must be set.
     */
    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    /**
     * Set the database connection username.
     * Either the datasource, connection or the connection details (driver, url,
     * username and password) must be set.
     */
    public void setConnectionUsername(String connectionUsername) {
        this.connectionUsername = connectionUsername;
    }

    /**
     * Set the database connection password.
     * Either the datasource, connection or the connection details (driver, url,
     * username and password) must be set.
     */
    public void setConnectionPassword(String connectionPassword) {
        this.connectionPassword = connectionPassword;
    }

    /**
     * Set the sql to retrieve the content id's and paths.
     * The id field must be the first field.
     * The path field must be the second field.
     */
    public void setContentSql(String contentSql) {
        this.contentSql = contentSql;
    }

    /**
     * Set the root body path for the source resources.
     */
    public void setSourceBodyPath(String sourceBodyPath) {
        this.sourceBodyPath = sourceBodyPath;
    }

    /**
     * Set the root body path for the destination resources.
     */
    public void setDestinationBodyPath(String destinationBodyPath) {
        this.destinationBodyPath = destinationBodyPath;
    }

    /**
     * Set the source file system handler.
     */
    public void setSourceFileSystemHandler(FileSystemHandler source) {
        this.sourceFileSystemHandler = source;
    }

    /**
     * Set the destination file system handler.
     */
    public void setDestinationFileSystemHandler(FileSystemHandler destination) {
        this.destinationFileSystemHandler = destination;
    }

    /**
     * Set whether to delete the resources from the source.
     */
    public void setDeleteFromSource(boolean deleteFromSource) {
        this.deleteFromSource = deleteFromSource;
    }

    /**
     * Setup the datasource. THis method first look for a valid datasource,
     * then a connection and lastly will create a datasource from the
     * connection details.
     */
    private void setupDataSource() throws IllegalStateException {
        if (dataSource != null) {
            return;
        }
        if (connection != null) {
            dataSource = new SingleConnectionDataSource(connection, false);
            return;
        }
        try {
            Class.forName(connectionDriver);
            dataSource = new SimpleDriverDataSource(DriverManager.getDriver(connectionURL), connectionURL, connectionUsername, connectionPassword);
        } catch (Exception e) {
            throw new IllegalStateException("Either a valid datasource, connection or the connection details must be set!", e);
        }
    }

    /**
     * Transfer the resources from the source file system handler to the
     * destination.
     */
    public void convertStorage() {
        log.info("Start converting storage....");
        setupDataSource();
        if (sourceFileSystemHandler == null) {
            throw new IllegalStateException("The source FileSystemHandler must be set!");
        }
        if (destinationFileSystemHandler == null) {
            throw new IllegalStateException("The destination FileSystemHandler must be set!");
        }
        final AtomicInteger counter = new AtomicInteger(0);
        // read content_resource records that have null file path
        JdbcTemplate template = new JdbcTemplate(dataSource);
        template.query(contentSql, new RowCallbackHandler() {
            public void processRow(ResultSet resultSet) throws SQLException {
                counter.incrementAndGet();
                String id = resultSet.getString(1);
                String path = resultSet.getString(2);
                try {
                    InputStream input = sourceFileSystemHandler.getInputStream(id, sourceBodyPath, path);
                    if (input != null) {
                        destinationFileSystemHandler.saveInputStream(id, destinationBodyPath, path, input);
                    }
                    if (deleteFromSource) {
                        sourceFileSystemHandler.delete(id, sourceBodyPath, path);
                    }
                } catch (IOException e) {
                    if (ignoreMissing) {
                        log.info("Missing file: " + id);
                    } else {
                        log.error("Failed to read or write resources from or to the FileSystemHandlers", e);
                        throw new SQLException("Failed to read or write resources from or to the FileSystemHandlers", e);
                    }
                }
            }
        });
        log.info("Converted " + counter.get() + " records....");
        log.info("Finished converting storage....");
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException{
        log.info("Checking arguments...");
        if (args == null || args.length == 0 || args[0].contains("help")) {
            printHelp();
            return;
        }

        Properties p = readProperties(args);
        log.info("Properties: " + p);
        StorageConverter sc = new StorageConverter();
        FileSystemHandler sourceFSH = null;
        FileSystemHandler destinationFSH = null;

        try {
            log.info("Database connection...");
            sc.setConnectionDriver(p.getProperty("connectionDriver"));
            sc.setConnectionURL(p.getProperty("connectionURL"));
            sc.setConnectionUsername(p.getProperty("connectionUsername"));
            sc.setConnectionPassword(p.getProperty("connectionPassword"));
            log.info("Source FileSystemHandler...");
            sourceFSH = getFileSystemHandler(p, "sourceFileSystemHandler");
            sc.setSourceBodyPath(p.getProperty("sourceBodyPath"));
            sc.setSourceFileSystemHandler(sourceFSH);
            sc.setDeleteFromSource(Boolean.parseBoolean(p.getProperty("deleteFromSource")));
            log.info("Destination FileSystemHandler...");
            destinationFSH = getFileSystemHandler(p, "destinationFileSystemHandler");
            sc.setDestinationBodyPath(p.getProperty("destinationBodyPath"));
            sc.setDestinationFileSystemHandler(destinationFSH);

            if(p.containsKey("contentSql")){
                sc.setContentSql(p.getProperty("contentSql"));
            }

            log.info("Running convert...");
            sc.convertStorage();
            log.info("Done...");
        } finally {
            destroy(sourceFSH);
            destroy(destinationFSH);
        }
    }

    /**
     * Calls the objects destroy method is it exists.
     */
    private static void destroy(Object o) throws IllegalAccessException, InvocationTargetException {
        if (o == null) return;
        log.info("Destroying " + o + "...");
        try {
            log.info("Check if there is a destroy method...");
            MethodUtils.invokeExactMethod(o, "destroy", (Object[])null);
            log.info("destroy method invoked...");
        } catch (NoSuchMethodException e) {
            log.info("No destroy method...");
        }
    }

    /**
     * Creates the FileSystemHandler and set all its properties.
     * Will also call the init method if it exists.
     */
    private static FileSystemHandler getFileSystemHandler(Properties p, String fileSystemHandlerName) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException{
        String clazz = p.getProperty(fileSystemHandlerName);
        log.info("Building FileSystemHandler: " + clazz);
        Class<? extends FileSystemHandler> fshClass = Class.forName(clazz).asSubclass(FileSystemHandler.class);
        FileSystemHandler fsh = fshClass.newInstance();

        Enumeration<String> propertyNames = (Enumeration<String>) p.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String fullProperty = propertyNames.nextElement();
            if (fullProperty.startsWith(fileSystemHandlerName + ".")) {
                String property = fullProperty.substring(fullProperty.indexOf(".")+1);
                log.info("Setting property: " + property);
                BeanUtils.setProperty(fsh, property, p.getProperty(fullProperty));
            }
        }

        try {
            log.info("Check if there is a init method...");
            MethodUtils.invokeExactMethod(fsh, "init", (Object[])null);
            log.info("init method invoked...");
        } catch (NoSuchMethodException e) {
            log.info("No init method...");
        }
        log.info("Done with FileSystemHandler: " + clazz);
        return fsh;
    }

    /**
     * Read the properties file. Return null of the file is not found.
     */
    private static Properties readProperties(String[] args) throws IOException {
        Properties p = new Properties(){

            @Override
            public String getProperty(String key) {
                String prop = super.getProperty(key);
                log.info("- Property " + key + "='" + prop + "'");
                return prop;
            }

        };
        for(int i = 0; i < args.length; i++){
            if("-p".equals(args[i])){
                p.load(new FileInputStream(new File(args[++i])));
            }
            if(args[i].startsWith("-")){
                p.put(args[i].substring(1), args[++i]);
            }
        }
        return p;
    }

    private static void printHelp(){
        log.info("----------------------------------------------------------------------");
        log.info("StorageConverter Help");
        log.info("The StorageConverter needs properties to complete the conversion.");
        log.info("These properties can either be loaded in a properties file indicated with '-p' followed by the location of the properties file");
        log.info("or the properties specified in the arguments with a leading '-' followed by the values.");
        log.info("");
        log.info("Properties (mandatory):");
        log.info("- connectionDriver: The database connection driver class.");
        log.info("- connectionURL: The database connection URL.");
        log.info("- connectionUsername: The database connection username.");
        log.info("- connectionPassword: The database connection password.");
        log.info("- sourceFileSystemHandler: This is the full class name of the source FileSystemHandler.");
        log.info("- sourceFileSystemHandler.<some property>: You can set any property on the source FileSystemHandler by referensing their property names.");
        log.info("- sourceBodyPath: The path set in sakai.properties for the source.");
        log.info("- destinationFileSystemHandler: This is the full class name of the destination FileSystemHandler.");
        log.info("- destinationFileSystemHandler.<some property>: You can set any property on the destination FileSystemHandler by referensing their property names.");
        log.info("- destinationBodyPath: The path set in sakai.properties for the destination.");
        log.info("");
        log.info("Properties (optional):");
        log.info("- deleteFromSource: Whether to delete the source files. Default false.");
        log.info("- contentSql: The sql statement to retrieve the resource id's and paths. Default is new ContentServiceSqlDefault().getResourceIdAndFilePath()");
        log.info("----------------------------------------------------------------------");
    }
}
