/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
// TODO: check against 15608

package org.sakaiproject.content.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.FileSystemHandler;
import org.sakaiproject.content.api.Lock;
import org.sakaiproject.content.api.LockManager;
import org.sakaiproject.content.impl.serialize.impl.conversion.Type1BlobCollectionConversionHandler;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.api.serialize.EntityParseException;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.KernelConfigurationError;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.BaseDbBinarySingleStorage;
import org.sakaiproject.util.BaseDbDualSingleStorage;
import org.sakaiproject.util.BaseDbSingleStorage;
import org.sakaiproject.util.ByteStorageConversion;
import org.sakaiproject.util.DbSingleStorage;
import org.sakaiproject.util.EntityReaderAdapter;
import org.sakaiproject.util.SingleStorageUser;
import org.sakaiproject.util.Xml;

/**
 * <p>
 * DbContentService is an extension of the BaseContentService with a database implementation.
 * </p>
 * <p>
 * The sql scripts in src/sql/chef_content.sql must be run on the database.
 * </p>
 */
@Slf4j
public class DbContentService extends BaseContentService
{
    private static final Marker FATAL = MarkerFactory.getMarker("FATAL");

    /** Table name for collections. */
    protected String m_collectionTableName = "CONTENT_COLLECTION";

    /** Table name for resources. */
    protected String m_resourceTableName = "CONTENT_RESOURCE";

    /** Table name for resources. */
    protected String m_resourceBodyTableName = "CONTENT_RESOURCE_BODY_BINARY";

    /** Table name for entity-group relationships. */
    protected String m_groupTableName = "CONTENT_ENTITY_GROUPS";

    /** maximum items for 'select where in' sql statement (Oracle limitation) **/
    public static final int MAX_IN_QUERY = 1000;

    /**
     * If true, we do our locks in the remote database, otherwise we do them here.
     */
    protected boolean m_locksInDb = true;

    /** The extra field(s) to write to the database - collections. */
    protected static final String[] COLLECTION_FIELDS = {"IN_COLLECTION"};

    /**
     * The extra field(s) to write to the database - resources - when we are doing bodys in files without the context-query conversion.
     */
    protected static final String[] RESOURCE_FIELDS_FILE = {"IN_COLLECTION", "FILE_PATH"};

    /**
     * The extra field(s) to write to the database - resources - when we are doing bodys in files with the context-query conversion.
     */
    public static final String[] RESOURCE_FIELDS_FILE_CONTEXT = {"IN_COLLECTION", "CONTEXT", "FILE_SIZE", "RESOURCE_TYPE_ID", "FILE_PATH"};

    /**
     * The extra field(s) to write to the database - resources - when we are doing bodys the db without the context-query conversion.
     */
    protected static final String[] RESOURCE_FIELDS = {"IN_COLLECTION"};

    /**
     * The extra field(s) to write to the database - resources - when we are doing bodys the db with the context-query conversion.
     */
    protected static final String[] RESOURCE_FIELDS_CONTEXT = {"IN_COLLECTION", "CONTEXT", "FILE_SIZE", "RESOURCE_TYPE_ID"};

    /**
     * The ID that is used in the content_resource table to test UTF8
     */
    private static final String UTF8TESTID = "UTF8TEST";

    private static final String[] BASE_COLLECTION_IDS = new String[]{
        "/","/attachment/","/group-user/","/group/","/private/","/public/","/user/"   
    };

    /** Table name for resources delete. */
    protected String m_resourceDeleteTableName = "CONTENT_RESOURCE_DELETE";

    /** Table name for resources delete. Has to be less than 30 characters */
    protected String m_resourceBodyDeleteTableName = "CONTENT_RESOURCE_BB_DELETE";

    /** The chunk size used when streaming (100k). */
    protected static final int STREAM_BUFFER_SIZE = 102400;

    /** Property name used in sakai.properties to turn on/off Content Hosting Handler support */
    private static final String CHH_ENABLE_FLAG = "content.useCHH";

    /*************************************************************************************************************************************************
     * Constructors, Dependencies and their setter methods
     ************************************************************************************************************************************************/

    /**
     * The file system handler to use when files are not stored in the database.
     */
    private FileSystemHandler fileSystemHandler = new DefaultFileSystemHandler();

    /**
     * Get the file system handler to use when files are not stored in the database.
     * <p/>
     * This can be null if files are stored in the database.
     * <p/>
     * The Default is DefaultFileSystemHandler.
     */
    public FileSystemHandler getFileSystemHandler(){
        return fileSystemHandler;
    }

    /**
     * Set the file system handler to use when files are not stored in the database.
     * <p/>
     * This can be null if files are stored in the database.
     * <p/>
     * The Default is DefaultFileSystemHandler.
     */
    public void setFileSystemHandler(FileSystemHandler fileSystemHandler){
        this.fileSystemHandler = fileSystemHandler;
    }

    /** Dependency: LockManager */
    protected LockManager m_lockManager = null;

    /**
     * Dependency: LockManager
     * 
     * @param service
     *        The LockManager
     */
    public void setLockManager(LockManager lockManager)
    {
        m_lockManager = lockManager;
    }

    /** Dependency: SqlService */
    protected SqlService m_sqlService = null;

    /**
     * Dependency: SqlService.
     * 
     * @param service
     *        The SqlService.
     */
    public void setSqlService(SqlService service)
    {
        m_sqlService = service;
    }


    private SessionManager sessionManager;
    public void setSessionManager(SessionManager sessionManager) {
        super.setSessionManager(sessionManager);
        this.sessionManager = sessionManager;
    }

    private ThreadLocalManager threadLocalManager;
    public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
        super.setThreadLocalManager(threadLocalManager);
        this.threadLocalManager = threadLocalManager;
    }


    private TimeService timeService;
    public void setTimeService(TimeService timeService) {
        super.setTimeService(timeService);
        this.timeService = timeService;
    }


    /**
     * Configuration: set the table name for collections.
     * 
     * @param path
     *        The table name for collections.
     */
    public void setCollectionTableName(String name)
    {
        m_collectionTableName = name;
    }

    /**
     * Configuration: set the table name for resources.
     * 
     * @param path
     *        The table name for resources.
     */
    public void setResourceTableName(String name)
    {
        m_resourceTableName = name;
    }

    /**
     * Configuration: set the table name for resource body.
     * 
     * @param path
     *        The table name for resource body.
     */
    public void setResourceBodyTableName(String name)
    {
        m_resourceBodyTableName = name;
    }

    /**
     * Configuration: set the locks-in-db
     * 
     * @param value
     *        The locks-in-db value.
     */
    public void setLocksInDb(String value)
    {
        m_locksInDb = Boolean.valueOf(value).booleanValue();
    }

    /** Set if we are to run the to-file conversion. */
    protected boolean m_convertToFile = false;

    /**
     * Configuration: run the to-file conversion.
     * 
     * @param value
     *        The conversion desired value.
     */
    public void setConvertToFile(String value)
    {
        m_convertToFile = Boolean.valueOf(value).booleanValue();
    }

    /** Configuration: to run the ddl on init or not. */
    protected boolean m_autoDdl = false;

    /** Virtual Content Hosting Handler -- handler which resolves virtual entities to real ones. */
    private ContentHostingHandlerResolverImpl contentHostingHandlerResolver = null;

    /**
     * Configuration: to run the ddl on init or not.
     * 
     * @param value
     *        the auto ddl value.
     */
    public void setAutoDdl(String value)
    {
        m_autoDdl = Boolean.valueOf(value).booleanValue();
    }

    // htripath-start
    public void setResourceDeleteTableName(String name)
    {
        m_resourceDeleteTableName = name;
    }

    public void setResourceBodyDeleteTableName(String name)
    {
        m_resourceBodyDeleteTableName = name;
    }

    // htripath-end

    public void setEntityGroupTableName(String name)
    {
        m_groupTableName = name;
    }

    /** contains a map of the database dependent handlers. */
    protected Map<String, ContentServiceSql> databaseBeans;

    /** The db handler we are using. */
    protected ContentServiceSql contentServiceSql;

    private boolean addNewColumnsCompleted = false;

    public void setDatabaseBeans(Map databaseBeans)
    {
        this.databaseBeans = databaseBeans;
    }

    public ContentServiceSql getContentServiceSql()
    {
        return contentServiceSql;
    }

    /**
     * sets which bean containing database dependent code should be used depending on the database vendor.
     */
    public void setContentServiceSql(String vendor)
    {
        this.contentServiceSql = (databaseBeans.containsKey(vendor) ? databaseBeans.get(vendor) : databaseBeans.get("default"));
    }

    /*************************************************************************************************************************************************
     * Init and Destroy
     ************************************************************************************************************************************************/

    /**
     * Final initialization, once all dependencies are set.
     */
    public void init()
    {

        if (m_sqlService == null) {
            log.error("init(): no sqlService found");
            return;
        }

        try
        {
            // system property to manually set conversion completion status.
            filesizeColumnReady = m_serverConfigurationService.getBoolean("content.filesizeColumnReady", true);				

            setContentServiceSql(m_sqlService.getVendor());

            // if we are auto-creating our schema, check and create
            if (m_autoDdl)
            {
                m_sqlService.ddl(this.getClass().getClassLoader(), "sakai_content");

                // add the delete table
                m_sqlService.ddl(this.getClass().getClassLoader(), "sakai_content_delete");
            }

            // Check for the existence of the FILE_SIZE column
            filesizeColumnExists = filesizeColumnExists();

            if (!filesizeColumnExists)
            {
            	//See KNL-487 - DH
                //if the columns don't exist we need to exit - updat needs to be run
            	log.error("The filesize column doesn't exit. Please make sure you ran the 2.4-2.5 DB Conversion");
            	throw new Error("The filesize column doesn't exit. Please make sure you ran the 2.4-2.5 DB Conversion");
            }

            if (filesizeColumnExists && !readyToUseFilesizeColumn())
            {
                // if the convert flag is set to add CONTEXT and FILE_SIZE columns
                // start doing the conversion
                if (convertToContextQueryForCollectionSize)
                {
                    populateNewColumns();
                }
            }

            try
            {
                validateUTF8Db();
            }
            catch (Exception ex)
            {
                log.error(FATAL, "Check on Database Failed", ex);
                log.error(FATAL, "===========================================================");
                log.error(FATAL, "WARNING \n"
                        + "  The connection from this instance of Sakai to the database\n"
                        + "  has been tested and found to corrupt UTF-8 Data. \n"
                        + "  In order for Sakai to operate correctly you must ensure that your \n"
                        + "  database setup is correct for UTF-8 data. This includes both the \n"
                        + "  JDBC connection to the database and the underlying storage in the \n"
                        + "  database.\n"
                        + "  The test that was performed on your database create a table\n"
                        + "  wrote some data to that table and read it back again. On reading \n"
                        + "  that data back it found some form of corruption, reported above.\n"
                        + "\n"
                        + " More information on database setup for sakai can be found at \n"
                        + " https://confluence.sakaiproject.org/display/I18N/Common+UTF-8+Problems \n"
                        + "\n"
                        + " Sakai Startup will continue but you might want to address this issue ASAP.\n");
            }

            if ( migrateData ) {
                log.info("Migration of data to the Binary format will be performed by this node ");
            } else {
                log.info("Migration of data to the Binary format will NOT be performed by this node ");

            }

            // If CHH resolvers are turned off in sakai.properties, unset the resolver property.
            // This MUST happen before super.init() calls newStorage()
            // (since that's when obj refs to the contentHostingHandlerResovler are passed around).
            if (!m_serverConfigurationService.getBoolean(CHH_ENABLE_FLAG,false))
                this.contentHostingHandlerResolver = null;

            super.init();

            // convert to filesystem storage?
            if (m_convertToFile)
            {
                m_convertToFile = false;
                convertToFile();
            }

            //Check that there is a valid file system handler
            if (m_bodyPath != null && fileSystemHandler == null)
            {
                throw new IllegalStateException("There is no FileSystemHandler set for the ContentService!");
            }

            log.info("init(): tables: " + m_collectionTableName + " " + m_resourceTableName + " " + m_resourceBodyTableName + " "
                    + m_groupTableName + " locks-in-db: " + m_locksInDb + " bodyPath: " + m_bodyPath + " storage: " + m_storage);

        }
        catch (Exception t)
        {
            log.error("init(): ", t);
        }

        //testResourceByTypePaging();
    }

    /**
     * Runs tests of the getResourcesOfType() method. Steps are:<br/>
     * 1) Add 26 site-level resource collections ("/group/site_A/" through "/group/site_Z/")
     * and 676 resources of type "org.sakaiproject.content.mock.resource-type".
     * 2) Invoke the getResourcesOfType() method with page-size 64 and compare
     * the resource-id's with the resource-id's that would be returned if the 
     * method works correctly.
     * 3) Remove the mock resources and collections created in step 1.
     * A list of the resource-id's is created in step 1 and used in steps 2 and 3.
     * Verbose logging in step 2 is intended to help with troubleshooting.  It would 
     * help to reduce the amount of logging and target it better.  Verbose logging also
     * occurs in step 3 because the no realms are created for the collections in step 1,
     * resulting in informational messages when an attempt is made to remove the realms.  
     */
    protected void testResourceByTypePaging() 
    {
        // test 
        List<String> collectionIdList = new ArrayList<String>();
        List<String> resourceIdList = new ArrayList<String>();
        String collectionId = "/group/";
        String siteid = "site_";
        String fileid = "image_";
        String extension = "jpg";
        String resourceType = "org.sakaiproject.content.mock.resource-type";
        String contentType = "image/jpeg";
        byte[] content = new byte[(Byte.MAX_VALUE - Byte.MIN_VALUE) * 4];
        int index = 0;
        for(int i = 0; i < 4 && index < content.length; i++)
        {
            for(byte b = Byte.MIN_VALUE; b <= Byte.MAX_VALUE && index < content.length; b++)
            {
                content[index] = b;
                index++;
            }
        }

        try
        {
            //enableSecurityAdvisor();
            Session s = sessionManager.getCurrentSession();
            s.setUserId(UserDirectoryService.ADMIN_ID);

            for(char ch = 'A'; ch <= 'Z'; ch++)
            {
                try
                {
                    String name = siteid + ch;
                    ContentCollectionEdit collection = this.addCollection(collectionId, name);
                    ResourcePropertiesEdit props = collection.getPropertiesEdit();
                    props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
                    this.commitCollection(collection);
                    collectionIdList.add(collection.getId());

                    for(char ch1 = 'a'; ch1 <= 'z'; ch1++)
                    {
                        try
                        {
                            String resourceName = fileid + ch1;
                            ContentResourceEdit resource = this.addResource(collection.getId(), resourceName, extension, MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
                            ResourcePropertiesEdit properties = resource.getPropertiesEdit();
                            properties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, resourceName);
                            resource.setContent(content);
                            resource.setContentType(contentType);
                            resource.setResourceType(resourceType);
                            this.commitResource(resource);
                            resourceIdList.add(resource.getId());
                        }
                        catch(Exception e)
                        {
                            log.error("TEMPORARY LOG MESSAGE WITH STACK TRACE: Failed to create all resources; ch1 = " + ch1, e);
                        }
                    }
                }
                catch(Exception e)
                {
                    log.error("TEMPORARY LOG MESSAGE WITH STACK TRACE: Failed to create all collections; ch = " + ch, e);
                }
            }

            int successCount = 0;
            int failCount = 0;
            int pageSize = 64;
            for(int p = 0; p * pageSize < resourceIdList.size(); p++)
            {
                Collection<ContentResource> page = this.getResourcesOfType(resourceType, pageSize, p);
                int r = 0;
                for(ContentResource cr : page)
                {
                    if(p * pageSize + r >= resourceIdList.size())
                    {
                        log.info("TEMPORARY LOG MESSAGE: test failed ====> p = " + p + " r = " + r + " index out of range: p * pageSize + r = " + (p * pageSize + r) + " resourceIdList.size() = " + resourceIdList.size());
                        failCount++;
                    }
                    else if(cr.getId().equals(resourceIdList.get(p * pageSize + r)))
                    {
                        successCount++;
                    }
                    else
                    {
                        log.info("TEMPORARY LOG MESSAGE: test failed ====> p = " + p + " r = " + r + " resource-id doesn't match: cr.getId() = " + cr.getId() + " resourceIdList.get(p * pageSize + r) = resourceIdList.get(" + (p * pageSize + r) + ") = " + resourceIdList.get(p * pageSize + r));
                        failCount++;
                    }
                    r++;
                }
                log.info("TEMPORARY LOG MESSAGE: Testing getResourcesOfType() completed page " + p + " of " + (resourceIdList.size() / pageSize));
            }
            log.info("TEMPORARY LOG MESSAGE: Testing getResourcesOfType() SUCCEEDED: " + successCount + " FAILED: " + failCount);

            for(String resourceId : resourceIdList)
            {
                ContentResourceEdit edit = this.editResource(resourceId);
                this.removeResource(edit);
            }

            log.info("TEMPORARY LOG MESSAGE: Will delete 26 collections and 676 resources.  Some log messages will appear.  This block of code will be removed in trunk within a few days and the log messages will disappear.");
            for(String collId : collectionIdList)
            {
                ContentCollectionEdit edit = this.editCollection(collId);
                this.removeCollection(edit);
            }
        }
        catch(Exception e)
        {
            log.debug("TEMPORARY LOG MESSAGE WITH STACK TRACE: TEST FAILED ", e);
        }
    }

    protected long filesizeColumnCheckExpires = 0L;

    public boolean migrateData = true;

    protected static final long TWENTY_MINUTES = 20L * 60L * 1000L;

    protected boolean filesizeColumnExists() 
    {
        boolean ok = false;
        if(m_sqlService.getVendor().toLowerCase().contains("hsql"))
        {
            ok = m_autoDdl || addNewColumnsCompleted;
        }
        else
        {
            String sql = contentServiceSql.getFilesizeColumnExistsSql();
            List list = m_sqlService.dbRead(sql);
            ok = list != null && ! list.isEmpty();
        }		
        return ok;
    }

    public boolean readyToUseFilesizeColumn()
    {
        if (filesizeColumnExists && !filesizeColumnReady)
        {
            long now = System.currentTimeMillis();
            if(now > filesizeColumnCheckExpires)
            {
                // cached value has expired -- time to renew
                if(hasNullFilesizeValues())
                {
                    filesizeColumnCheckExpires = now + TWENTY_MINUTES;
                    log.debug("Conversion of the ContentHostingService database tables is needed to improve performance");
                }
                else
                {
                    String highlight = "\n====================================================\n====================================================\n";
                    log.info(highlight + "Conversion of the ContentHostingService database tables is complete.\nUsing new filesize column" + highlight);
                    filesizeColumnReady = true;
                }
            }
        }
        return filesizeColumnReady;
    }

    protected boolean hasNullFilesizeValues() 
    {
        String sql = contentServiceSql.getFilesizeExistsSql();
        List list = m_sqlService.dbRead(sql);
        return (list != null && !list.isEmpty());
    }

    /**
     *
     */
    protected int countQuery(String sql, String param) throws IdUnusedException
    {

        Object[] fields = new Object[1];
        fields[0] = param;

        List list = m_sqlService.dbRead(sql, fields, null);

        if (list != null)
        {
            Object val = null;
            int rv = 0;
            Iterator iter = list.iterator();
            if (iter.hasNext())
            {
                try
                {
                    val = iter.next();
                    rv = Integer.parseInt((String) val);
                }
                catch (Exception ignore)
                {
                    log.warn("Exception parsing integer from count query: " + val);
                }
            }
            return rv;
        }
        throw new IdUnusedException(param);
    }

    public int getCollectionSize(String id) throws IdUnusedException, TypeException, PermissionException
    {

        /*
         * Note: Content Hosting Handler This will only count local collection information For the moment its only used by setPriority
         */
        String wildcard;

        if (id.endsWith("/"))
        {
            wildcard = id + "%";
        }
        else
        {
            wildcard = id + "/%";
        }

        int fileCount = countQuery(contentServiceSql.getNumContentResources1Sql(), wildcard);
        int folderCount = countQuery(contentServiceSql.getNumContentResources2Sql(), wildcard);
        return fileCount + folderCount;
    }

    /*************************************************************************************************************************************************
     * UUID Support
     ************************************************************************************************************************************************/

    /**
     * For a given id, return its UUID (creating it if it does not already exist)
     */

    public String getUuid(String id)
    {

        /*
         * Note: Content Hosting Handler This will only operate on local resources The only thing that may not work is move.
         */
        String uuid = null;

        uuid = findUuid(id);

        if (uuid != null) return uuid;

        // UUID not found, so create one and store it

        IdManager uuidManager = (IdManager) ComponentManager.get(IdManager.class);
        uuid = uuidManager.createUuid();

        setUuidInternal(id, uuid);

        return uuid;
    }

    /**
     * @param id
     *        id of the resource to set the UUID for
     * @param uuid
     *        the new UUID of the resource
     * @throws IdInvalidException
     *         if the given resource already has a UUID set
     */
    public void setUuid(String id, String uuid) throws IdInvalidException
    {
        String existingUuid = findUuid(id);
        if (existingUuid != null)
        {
            throw new IdInvalidException(id);
        }

        setUuidInternal(id, uuid);
    }

    protected void setUuidInternal(String id, String uuid)
    {
        // get a connection for the updates
        Connection connection = null;

        try
        {
            connection = m_sqlService.borrowConnection();

            boolean wasCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            // set any existing one to null
            String sql = contentServiceSql.getUpdateContentResource1Sql();
            Object[] fields = new Object[2];
            fields[0] = null;
            fields[1] = uuid;
            m_sqlService.dbWrite(connection, sql, fields);

            sql = contentServiceSql.getUpdateContentResource2Sql();
            fields = new Object[2];
            fields[0] = uuid;
            fields[1] = id;
            m_sqlService.dbWrite(connection, sql, fields);

            connection.commit();
            connection.setAutoCommit(wasCommit);

        } catch (SQLException e) {
            log.warn("setUuid: failed: " + e);
        }
        finally {
            if (connection != null)
            {
                m_sqlService.returnConnection(connection);
            }
        }
    }

    /**
     * private utility method to search for UUID for given id
     */

    protected String findUuid(String id)
    {
        String sql = contentServiceSql.getResourceUuidSql();
        Object[] fields = new Object[1];
        fields[0] = id;

        String uuid = null;
        List result = m_sqlService.dbRead(sql, fields, null);

        if (result != null)
        {
            Iterator iter = result.iterator();
            if (iter.hasNext())
            {
                uuid = (String) iter.next();
            }
        }

        return uuid;
    }

    /**
     * For a given UUID, attempt to lookup and return the corresponding id (URI)
     */

    public String resolveUuid(String uuid)
    {

        String id = null;

        String sql = contentServiceSql.getResourceId1Sql();
        Object[] fields = new Object[1];
        fields[0] = uuid;

        List result = m_sqlService.dbRead(sql, fields, null);

        if (result != null)
        {
            Iterator iter = result.iterator();
            if (iter.hasNext())
            {
                id = (String) iter.next();
            }
        }

        return id;
    }

    /*************************************************************************************************************************************************
     * BaseContentService extensions
     ************************************************************************************************************************************************/

    /**
     * Construct a Storage object.
     * 
     * @return The new storage object.
     */
    protected Storage newStorage()
    {
        EntityReaderAdapter cera = new EntityReaderAdapter();
        CollectionStorageUser csu = new CollectionStorageUser();
        csu.setEntityReaderAdapter(cera);
        cera.setContainerEntryTagName("notdoublestorage");
        cera.setResourceEntryTagName("collection");
        cera.setSaxEntityReader(csu);
        cera.setStorageUser(csu);
        cera.setTarget(csu);

        EntityReaderAdapter rera = new EntityReaderAdapter();
        ResourceStorageUser rsu = new ResourceStorageUser();
        rsu.setEntityReaderAdapter(rera);
        rera.setContainerEntryTagName("notdoublestorage");
        rera.setResourceEntryTagName("resource");
        rera.setSaxEntityReader(rsu);
        rera.setStorageUser(rsu);
        rera.setTarget(rsu);


        Storage storage =  new DbStorage(csu, rsu, (m_bodyPath != null), contentHostingHandlerResolver);
        if ( contentHostingHandlerResolver != null ) {
            contentHostingHandlerResolver.setStorage(storage);
        }
        return storage;
    } // newStorage

    /*************************************************************************************************************************************************
     * Storage implementation
     ************************************************************************************************************************************************/
    protected class DbStorage implements Storage
    {

        /** A storage for collections. */
        protected DbSingleStorage m_collectionStore = null;

        /** A storage for resources. */
        protected DbSingleStorage m_resourceStore = null;

        /** htripath- Storage for resources delete.*/
        protected DbSingleStorage m_resourceDeleteStore = null;

        protected ContentHostingHandlerResolverImpl resolver = null;

        private ThreadLocal stackMarker = new ThreadLocal();

        private String m_collectionStorageFields;

        private String m_resourceStorageFields;

        /**
         * Construct.
         * 
         * @param collectionUser
         *        The StorageUser class to call back for creation of collection objects.
         * @param resourceUser
         *        The StorageUser class to call back for creation of resource objects.
         */
        public DbStorage(SingleStorageUser collectionUser, SingleStorageUser resourceUser, boolean bodyInFile, ContentHostingHandlerResolverImpl resolver)
        {
            this.resolver = resolver;
            if (resolver != null)
            {
                this.resolver.setResourceUser(resourceUser);
                this.resolver.setCollectionUser(collectionUser);
            }

            Connection connection = null;
            Statement statement = null;
            ResultSet rs = null;
            PreparedStatement updateStatement = null;
            PreparedStatement selectStatement = null;
            boolean binaryCollection = false;
            boolean xmlCollection = true;
            boolean binaryResource = false;
            boolean xmlResource = true;
            boolean binaryDelete = false;
            boolean xmlDelete = true;
            try {
                connection = m_sqlService.borrowConnection();
                statement = connection.createStatement();
                try {
                    statement.execute("select BINARY_ENTITY from CONTENT_COLLECTION where COLLECTION_ID = 'does-not-exist' " );
                    binaryCollection = true;
                } catch ( Exception ex ) {
                    binaryCollection = false;
                }
                try {
                    statement.execute("select XML from CONTENT_COLLECTION where COLLECTION_ID = 'does-not-exist' ");
                    xmlCollection = true;
                } catch ( Exception ex ) {
                    xmlCollection = false;
                }

                try {
                    statement.execute("select BINARY_ENTITY from CONTENT_RESOURCE where RESOURCE_ID = 'does-not-exist' " );
                    binaryResource = true;
                } catch ( Exception ex ) {
                    binaryResource = false;
                }
                try {
                    statement.execute("select XML from CONTENT_RESOURCE where RESOURCE_ID = 'does-not-exist' ");
                    xmlResource = true;
                } catch ( Exception ex ) {
                    xmlResource = false;
                }
                try {
                    statement.execute("select BINARY_ENTITY from CONTENT_RESOURCE_DELETE where RESOURCE_ID = 'does-not-exist' " );
                    binaryDelete = true;
                } catch ( Exception ex ) {
                    binaryDelete = false;
                }
                try {
                    statement.execute("select XML from CONTENT_RESOURCE_DELETE where RESOURCE_ID = 'does-not-exist' ");
                    xmlDelete= true;
                } catch ( Exception ex ) {
                    xmlDelete = false;
                }

                if ( migrateData && binaryCollection && xmlCollection ) {

                    // migrate the base XML entities
                    Type1BlobCollectionConversionHandler t1ch = new Type1BlobCollectionConversionHandler();

                    selectStatement = connection.prepareStatement("select XML from CONTENT_COLLECTION where BINARY_ENTITY IS NULL AND COLLECTION_ID = ? ");
                    updateStatement = connection.prepareStatement("update CONTENT_COLLECTION set XML = NULL, BINARY_ENTITY = ?  where COLLECTION_ID = ? ");
                    for ( String collectionid : BASE_COLLECTION_IDS ) {
                        selectStatement.clearParameters();
                        selectStatement.setString(1, collectionid);
                        rs = selectStatement.executeQuery();
                        if ( rs.next() ) {
                            String xml = rs.getString(1);
                            boolean bnull = rs.wasNull();
                            rs.close();
                            if ( !bnull && xml != null  ) {
                                updateStatement.clearParameters();
                                if ( t1ch.convertSource(collectionid, xml, updateStatement) ) {
                                    updateStatement.executeUpdate();
                                } else {
                                    log.info("XML Pase failed "+collectionid);												
                                }
                            }
                        } else {
                            rs.close();
                        }
                    }
                    connection.commit();

                }

                if ( !migrateData && binaryCollection ) {
                    rs = statement.executeQuery("select count(*) from CONTENT_COLLECTION where BINARY_ENTITY IS NOT NULL ");
                    int n = 0;
                    if ( rs.next() ) {
                        n = rs.getInt(1);
                    }
                    if ( n != 0 ) {
                        log.error(FATAL, "There are migrated content collection entries in the \n" +
                                "BINARY_ENTITY column  of CONTENT_COLLECTION you must ensure that this \n" +
                                "data is not required and set all entries to null before starting \n" +
                                "up with migrate data disabled. Failure to do this could loose \n" +
                                "updates since this database was upgraded \n");
                        log.error(FATAL, "STOP ============================================");
                        /*we need to close these here otherwise the system exit will lead them to being left open
                         * While this may be harmful is bad practice and prevents us identifying real issues
                         */
                        cleanup(connection, statement, rs, selectStatement, updateStatement);
                        System.exit(-10);
                    }
                }
                if ( !migrateData && binaryResource ) {
                    rs = statement.executeQuery("select count(*) from CONTENT_RESOURCE where BINARY_ENTITY IS NOT NULL ");
                    int n = 0;
                    if ( rs.next() ) {
                        n = rs.getInt(1);
                    }
                    if ( n != 0 ) {
                        log.error(FATAL, "There are migrated content collection entries in the \n" +
                                "BINARY_ENTITY column  of CONTENT_RESOURCE you must ensure that this \n" +
                                "data is not required and set all entries to null before starting \n" +
                                "up with migrate data disabled. Failure to do this could loose \n" +
                                "updates since this database was upgraded \n");
                        log.error(FATAL, "STOP ============================================");
                        /*we need to close these here otherwise the system exit will lead them to being left open
                         * While this may be harmful is bad practice and prevents us identifying real issues
                         */
                        cleanup(connection, statement, rs, selectStatement, updateStatement);
                        System.exit(-10);
                    }
                }
                if ( !migrateData && binaryResource ) {
                    rs = statement.executeQuery("select count(*) from CONTENT_RESOURCE_DELETE where BINARY_ENTITY IS NOT NULL ");
                    int n = 0;
                    if ( rs.next() ) {
                        n = rs.getInt(1);
                    }
                    if ( n != 0 ) {
                        log.error(FATAL, "There are migrated content collection entries in the \n" +
                                "BINARY_ENTITY column  of CONTENT_RESOURCE_DELETE you must ensure that this \n" +
                                "data is not required and set all entries to null before starting \n" +
                                "up with migrate data disabled. Failure to do this could loose \n" +
                                "updates since this database was upgraded \n");
                        log.error(FATAL, "STOP ============================================");
                        /*we need to close these here otherwise the system exit will lead them to being left open
                         * While this may be harmful is bad practice and prevents us identifying real issues
                         */
                        cleanup(connection, statement, rs, selectStatement, updateStatement);
                        throw new KernelConfigurationError("There are migrated content collection entries in the \n" +
                        		"BINARY_ENTITY column  of CONTENT_RESOURCE_DELETE you must ensure that this \n" +
                        		"data is not required and set all entries to null before starting \n" +
                        		"up with migrate data disabled. Failure to do this could loose \n" +
                        "updates since this database was upgraded");
                    }
                }

            } catch (SQLException e) {
                log.error("Unable to get database statement: {}", e.getMessage(), e);
            } finally {
                cleanup(connection, statement, rs, selectStatement, updateStatement);
            }

            if (migrateData && binaryCollection && xmlCollection) {
                // build the collection store - a single level store
                m_collectionStore = new BaseDbDualSingleStorage(m_collectionTableName, "COLLECTION_ID", COLLECTION_FIELDS, m_locksInDb, "collection",
                        collectionUser, m_sqlService);
                m_collectionStorageFields = BaseDbDualSingleStorage.STORAGE_FIELDS;

            } else if ( migrateData && binaryCollection) {
                // build the collection store - a single level store
                m_collectionStore = new BaseDbBinarySingleStorage(m_collectionTableName, "COLLECTION_ID", COLLECTION_FIELDS, m_locksInDb, "collection",
                        collectionUser, m_sqlService);
                m_collectionStorageFields = BaseDbBinarySingleStorage.STORAGE_FIELDS;

            } else {
                // build the collection store - a single level store
                m_collectionStore = new BaseDbSingleStorage(m_collectionTableName, "COLLECTION_ID", COLLECTION_FIELDS, m_locksInDb, "collection",
                        collectionUser, m_sqlService);
                m_collectionStorageFields = BaseDbSingleStorage.STORAGE_FIELDS;

            }

            if (  migrateData && binaryResource && xmlResource) {
                // build the resources store - a single level store
                m_resourceStore = new BaseDbDualSingleStorage(m_resourceTableName, "RESOURCE_ID", 
                        (bodyInFile ? RESOURCE_FIELDS_FILE_CONTEXT : RESOURCE_FIELDS_CONTEXT ),
                        m_locksInDb, "resource", resourceUser, m_sqlService);
                m_resourceStorageFields = BaseDbDualSingleStorage.STORAGE_FIELDS;

            } else if ( migrateData && binaryResource) {
                // build the resources store - a single level store
                m_resourceStore = new BaseDbBinarySingleStorage(m_resourceTableName, "RESOURCE_ID", 
                        (bodyInFile ? RESOURCE_FIELDS_FILE_CONTEXT : RESOURCE_FIELDS_CONTEXT ),
                        m_locksInDb, "resource", resourceUser, m_sqlService);
                m_resourceStorageFields = BaseDbBinarySingleStorage.STORAGE_FIELDS;

            } else {
                // build the resources store - a single level store
                m_resourceStore = new BaseDbSingleStorage(m_resourceTableName, "RESOURCE_ID", 
                        (bodyInFile ? RESOURCE_FIELDS_FILE_CONTEXT : RESOURCE_FIELDS_CONTEXT ),
                        m_locksInDb, "resource", resourceUser, m_sqlService);
                m_resourceStorageFields = BaseDbSingleStorage.STORAGE_FIELDS;

            }

            if ( migrateData && xmlDelete && binaryDelete ) {
                // htripath-build the resource for store of deleted record-single
                // level store
                m_resourceDeleteStore = new BaseDbDualSingleStorage(m_resourceDeleteTableName, "RESOURCE_ID", 
                        (bodyInFile ? RESOURCE_FIELDS_FILE_CONTEXT : RESOURCE_FIELDS_CONTEXT ),
                        m_locksInDb, "resource", resourceUser, m_sqlService, m_resourceStore); // support for SAK-12874

            } else if ( migrateData && binaryDelete) {
                // htripath-build the resource for store of deleted record-single
                // level store
                m_resourceDeleteStore = new BaseDbBinarySingleStorage(m_resourceDeleteTableName, "RESOURCE_ID", 
                        (bodyInFile ? RESOURCE_FIELDS_FILE_CONTEXT : RESOURCE_FIELDS_CONTEXT ),
                        m_locksInDb, "resource", resourceUser, m_sqlService, m_resourceStore); // support for SAK-12874

            } else {
                // htripath-build the resource for store of deleted record-single
                // level store
                m_resourceDeleteStore = new BaseDbSingleStorage(m_resourceDeleteTableName, "RESOURCE_ID", 
                        (bodyInFile ? RESOURCE_FIELDS_FILE_CONTEXT : RESOURCE_FIELDS_CONTEXT ),
                        m_locksInDb, "resource", resourceUser, m_sqlService, m_resourceStore); // support for SAK-12874

            }

        } // DbStorage

        /**
         * Cleanup the resultset, statements, and connection in the finally block or as needed
         * @param connection
         * @param statement
         * @param rs
         * @param selectStatement
         * @param updateStatement
         */
        private void cleanup(Connection connection, Statement statement, ResultSet rs,
                PreparedStatement selectStatement, PreparedStatement updateStatement) {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
                log.error("Failed to close resultset: " + ex, ex);
            }
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ex) {
                log.error("Failed to close statement: " + ex, ex);
            }
            try {
                if (selectStatement != null) {
                    selectStatement.close();
                }
            } catch (SQLException ex) {
                log.error("Failed to close selectStatement: " + ex, ex);
            }
            try {
                if (updateStatement != null) {
                    updateStatement.close();
                }
            } catch (SQLException ex) {
                log.error("Failed to close updateStatement: " + ex, ex);
            }
            m_sqlService.returnConnection(connection);
        }

        /**
         * Open and be ready to read / write.
         */
        public void open()
        {
            m_collectionStore.open();
            m_resourceStore.open();
            m_resourceDeleteStore.open();
        } // open

        /**
         * Close.
         */
        public void close()
        {
            m_collectionStore.close();
            m_resourceStore.close();
            m_resourceDeleteStore.close();
        } // close

        private class StackRef
        {
            protected int count = 0;
        }

        /**
         * increase the stack counter and return true if this is the top of the stack
         * 
         * @return
         */
        private boolean in()
        {
            StackRef r = (StackRef) stackMarker.get();
            if (r == null)
            {
                r = new StackRef();
                stackMarker.set(r);
            }
            r.count++;
            return r.count <= 1;// johnf@caret -- used to permit no self-recurses; now permits 0 or 2 (r.count == 1);
        }

        /**
         * decrement the stack counter on the thread
         */
        private void out()
        {
            StackRef r = (StackRef) stackMarker.get();
            if (r == null)
            {
                r = new StackRef();
                stackMarker.set(r);
            }
            r.count--;
            if (r.count < 0)
            {
                r.count = 0;
            }
        }

        /** Collections * */

        public boolean checkCollection(String id)
        {
            if (id == null || id.trim().length() == 0)
            {
                return false;
            }
            boolean goin = in();
            try
            {
                if (resolver != null && goin)
                {
                    return resolver.checkCollection(id);
                }
                else
                {
                    return m_collectionStore.checkResource(id);
                }
            }
            finally
            {
                out();
            }
        }

        public ContentCollection getCollection(String id)
        {
            if (id == null || id.trim().length() == 0)
            {
                return null;
            }
            boolean goin = in();
            try
            {
                if (resolver != null && goin)
                {
                    return resolver.getCollection(id);
                }
                else
                {
                    return (ContentCollection) m_collectionStore.getResource(id);
                }
            }
            finally
            {
                out();
            }

        }

        /**
         * Get a list of all getCollections within a collection.
         */
        public List<ContentCollectionEdit> getCollections(ContentCollection collection)
        {
            boolean goin = in();
            try
            {
                if (resolver != null && goin)
                {
                    return resolver.getCollections(collection);
                }
                else
                {
                    // limit to those whose reference path (based on id) matches
                    // the
                    // collection id
                    final String target = collection.getId();

                    /*
                     * // read all the records, then filter them to accept only those in this collection // Note: this is not desirable, as the read
                     * is linear to the database site -ggolden List rv = m_collectionStore.getSelectedResources( new Filter() { public boolean
                     * accept(Object o) { // o is a String, the collection id return StringUtil.referencePath((String) o).equals(target); } } );
                     */

                    List<ContentCollectionEdit> collections = (List<ContentCollectionEdit>) threadLocalManager.get("getCollections@" + target);
                    if (collections == null)
                    {
                        collections = m_collectionStore.getAllResourcesWhere("IN_COLLECTION", target);
                        if(collections != null && collections.size() > 0 && isSiteLevelDropbox(target))
                        {
                            Map<String,Long> updateTimes = getMostRecentUpdate(collection.getId());
                            Iterator it = collections.iterator();
                            while(it.hasNext())
                            {
                                BaseCollectionEdit dropbox = (BaseCollectionEdit) it.next();
                                Long update = updateTimes.get(dropbox.getId());
                                if(update != null)
                                {
                                    ResourcePropertiesEdit props = dropbox.getPropertiesEdit();
                                    Time time = timeService.newTime(update);
                                    props.addProperty(PROP_DROPBOX_CHANGE_TIMESTAMP, time.toString());
                                }
                            }
                        }
                        threadLocalManager.set("getCollections@" + target, collections);
                        cacheEntities(collections);
                    }
                    // read the records with a where clause to let the database
                    // select
                    // those in this collection
                    return collections;
                }
            }
            finally
            {
                out();
            }

        } // getCollections

        public ContentCollectionEdit putCollection(String id)
        {
            if (id == null || id.trim().length() == 0)
            {
                return null;
            }
            boolean goin = in();
            try
            {
                if (resolver != null && goin)
                {
                    return (ContentCollectionEdit) resolver.putCollection(id);
                }
                else
                {
                    return (ContentCollectionEdit) m_collectionStore.putResource(id, null);
                }
            }
            finally
            {
                out();
            }
        }

        public ContentCollectionEdit editCollection(String id)
        {
            if (id == null || id.trim().length() == 0)
            {
                return null;
            }
            boolean goin = in();
            try
            {
                if (resolver != null && goin)
                {
                    return (ContentCollectionEdit) resolver.editCollection(id);
                }
                else
                {
                    return (ContentCollectionEdit) m_collectionStore.editResource(id);
                }
            }
            finally
            {
                out();
            }
        }

        // protected String externalResourceDeleteFileName(ContentResource resource)
        // {
        // return m_bodyPath + "/delete/" + ((BaseResourceEdit) resource).m_filePath;
        // }

        // htripath -end

        public void cancelResource(ContentResourceEdit edit)
        {
            boolean goin = in();
            try
            {
                if (resolver != null && goin)
                {
                    resolver.cancelResource(edit);
                }
                else
                {
                    // clear the memory image of the body
                    byte[] body = ((BaseResourceEdit) edit).m_body;
                    ((BaseResourceEdit) edit).m_body = null;
                    m_resourceStore.cancelResource(edit);

                }
            }
            finally
            {
                out();
            }
        }

        public void commitCollection(ContentCollectionEdit edit)
        {
            boolean goin = in();
            try
            {
                if (resolver != null && goin)
                {
                    resolver.commitCollection(edit);
                }
                else
                {
                    if(isInsideIndividualDropbox(edit.getId()) || isIndividualDropbox(edit.getId()))
                    {
                        insertIndividualDropboxRecord(getIndividualDropboxId(edit.getId()));
                    }
                    m_collectionStore.commitResource(edit);
                }
            }
            finally
            {
                out();
            }
        }

        public void cancelCollection(ContentCollectionEdit edit)
        {
            boolean goin = in();
            try
            {
                if (resolver != null && goin)
                {
                    resolver.cancelCollection(edit);
                }
                else
                {
                    m_collectionStore.cancelResource(edit);
                }
            }
            finally
            {
                out();
            }

        }

        public void removeCollection(ContentCollectionEdit edit)
        {
            boolean goin = in();
            try
            {
                if (resolver != null && goin)
                {
                    resolver.removeCollection(edit);
                }
                else
                {
                    if(isInsideIndividualDropbox(edit.getId()))
                    {
                        insertIndividualDropboxRecord(getIndividualDropboxId(edit.getId()));
                    }
                    m_collectionStore.removeResource(edit);
                }
            }
            finally
            {
                out();
            }
        }

        /** Resources * */

        public boolean checkResource(String id)
        {
            if (id == null || id.trim().length() == 0)
            {
                return false;
            }
            boolean goin = in();
            try
            {
                if (resolver != null && goin)
                {
                    return resolver.checkResource(id);
                }
                else
                {
                    return m_resourceStore.checkResource(id);
                }
            }
            finally
            {
                out();
            }
        }

        public ContentResource getResource(String id)
        {
            if (id == null || id.trim().length() == 0)
            {
                return null;
            }
            boolean goin = in();
            try
            {
                if (resolver != null && goin)
                {
                    return (ContentResource) resolver.getResource(id);
                }
                else
                {
                    return (ContentResource) m_resourceStore.getResource(id);
                }
            }
            finally
            {
                out();
            }
        }

        public List<ContentResourceEdit> getResources(ContentCollection collection)
        {
            boolean goin = in();
            try
            {
                if (resolver != null && goin)
                {
                    return resolver.getResources(collection);
                }
                else
                {
                    // limit to those whose reference path (based on id) matches
                    // the
                    // collection id
                    final String target = collection.getId();

                    /*
                     * // read all the records, then filter them to accept only those in this collection // Note: this is not desirable, as the read
                     * is linear to the database site -ggolden List rv = m_resourceStore.getSelectedResources( new Filter() { public boolean
                     * accept(Object o) { // o is a String, the resource id return StringUtil.referencePath((String) o).equals(target); } } );
                     */

                    List<ContentResourceEdit> resources = (List<ContentResourceEdit>) threadLocalManager.get("getResources@" + target);
                    if (resources == null)
                    {
                        resources = m_resourceStore.getAllResourcesWhere("IN_COLLECTION", target);
                        threadLocalManager.set("getResources@" + target, resources);
                        cacheEntities(resources);
                    }
                    // read the records with a where clause to let the database
                    // select
                    // those in this collection
                    return resources;
                }
            }
            finally
            {
                out();
            }

        } // getResources

        public List getFlatResources(String collectionId)
        {
            List rv = null;
            boolean goin = in();
            try
            {
                if (resolver != null && goin)
                {
                    rv = resolver.getFlatResources(collectionId);
                }
                else
                {
                    rv = m_resourceStore.getAllResourcesWhereLike("IN_COLLECTION", collectionId + "%");
                }
                return rv;
            }
            finally
            {
                out();
            }
        }

        public ContentResourceEdit putResource(String id)
        {
            if (id == null || id.trim().length() == 0)
            {
                return null;
            }
            boolean goin = in();
            try
            {
                if (resolver != null && goin)
                {
                    return (ContentResourceEdit) resolver.putResource(id);
                }
                else
                {
                    if(isInsideIndividualDropbox(id))
                    {
                        insertIndividualDropboxRecord(getIndividualDropboxId(id));
                    }
                    return (ContentResourceEdit) m_resourceStore.putResource(id, null);
                }
            }
            finally
            {
                out();
            }
        }

        protected void insertIndividualDropboxRecord(String individualDropboxId) 
        {
            String sql = contentServiceSql.getInsertIndividualDropboxChangeSql();

            Object[] fields = null;
            if("oracle".equalsIgnoreCase(m_sqlService.getVendor()))
            {
                fields = new Object[6];
                fields[0] = individualDropboxId;
                fields[1] = individualDropboxId;
                fields[2] = isolateContainingId(individualDropboxId);
                fields[3] = Long.toString(timeService.newTime().getTime());
                fields[4] = isolateContainingId(individualDropboxId);
                fields[5] = Long.toString(timeService.newTime().getTime());
            }
            else if("hsqldb".equalsIgnoreCase(m_sqlService.getVendor()))
            {
                fields = new Object[3];
                fields[0] = individualDropboxId;
                fields[1] = isolateContainingId(individualDropboxId);
                fields[2] = Long.toString(timeService.newTime().getTime());
            }
            else
            {
                fields = new Object[5];
                fields[0] = individualDropboxId;
                fields[1] = isolateContainingId(individualDropboxId);
                fields[2] = Long.toString(timeService.newTime().getTime());
                fields[3] = isolateContainingId(individualDropboxId);
                fields[4] = Long.toString(timeService.newTime().getTime());
            }

            try
            {
                boolean ok = m_sqlService.dbWrite(sql, fields);
            }
            catch(Exception e)
            {
                log.error("sql == " + sql, e);
            }

        }

        protected void updateIndividualDropboxRecord(String individualDropboxId) 
        {
            String sql = contentServiceSql.getUpdateIndividualDropboxChangeSql();

            Object[] fields = new Object[3];
            fields[0] = isolateContainingId(individualDropboxId);
            fields[1] = Long.toString(timeService.newTime().getTime());
            fields[2] = individualDropboxId;

            boolean ok = m_sqlService.dbWrite(sql, fields);

        }

        public ContentResourceEdit editResource(String id)
        {
            if (id == null || id.trim().length() == 0)
            {
                return null;
            }
            boolean goin = in();
            try
            {
                if (resolver != null && goin)
                {
                    return (ContentResourceEdit) resolver.editResource(id);
                }
                else
                {
                    return (ContentResourceEdit) m_resourceStore.editResource(id);
                }
            }
            finally
            {
                out();
            }
        }

        public void commitResource(ContentResourceEdit edit) throws ServerOverloadException
        {
            // keep the body out of the XML

            boolean goin = in();
            try
            {
                String message = "failed to write file ";
                if (resolver != null && goin)
                {
                    resolver.commitResource(edit);
                }
                else
                {
                    BaseResourceEdit redit = (BaseResourceEdit) edit;
                    boolean ok = true;

                    /**
                     * https://jira.sakaiproject.org/browse/KNL-817
                     * If the reference copy flag is set then we do NOT actually do the content data copy (in the else below)
                     * Instead, we modify the in DB resource table to point at the new resource id
                     */
                    String referenceResourceId = redit.referenceCopy;
                    if (referenceResourceId != null) {
                        // special handling for reference commits
                        if (log.isDebugEnabled()) log.debug("Making resource ("+redit.getId()+") reference copy of DB resource ("+referenceResourceId+"), body/contentStream is ignored");
                        if (m_bodyPath == null) {
                            /* SPECIAL handling for a reference copy of a resource,
                             * for reference we just move the binary data location to point at the new one
                             */
                            // the DB write could fail so we do a count check first (still not a guarantee)
                            String sqlExists = "select count(*) from " + m_resourceBodyTableName + " where RESOURCE_ID=?";
                            @SuppressWarnings("unchecked")
                            List<String> sqlExistsResult = m_sqlService.dbRead(sqlExists, new Object[] { referenceResourceId }, null);
                            if (sqlExistsResult != null && Long.parseLong(sqlExistsResult.get(0)) == 1l) {
                                // the resource exists already so we proceed to redirect
                                String sql = "update "+m_resourceBodyTableName+" set RESOURCE_ID=? where RESOURCE_ID=?";
                                // this write could fail if we try to move it to a taken resource_id, no way to recover if it does
                                ok = m_sqlService.dbWrite(sql, new Object[] {redit.getId(), referenceResourceId});
                                if (log.isDebugEnabled()) log.debug("Moving RESOURCE_ID ("+redit.getId()+") to ("+referenceResourceId+") for DB stored content data ("+m_resourceBodyTableName+"), success="+ok);
                            } else {
                                ok = false;
                                if (log.isDebugEnabled()) log.debug("Moving RESOURCE_ID ("+redit.getId()+") to ("+referenceResourceId+") for DB stored content data ("+m_resourceBodyTableName+") failed because the referenceResourceId ("+referenceResourceId+") does not exist in the table");
                            }
                            if (!ok) {
                                // cannot recover so we will flip this over and do a normal content copy
                                log.warn("Moving RESOURCE_ID ("+redit.getId()+") to ("+referenceResourceId+") for DB stored content data ("+m_resourceBodyTableName+") failed... we will do a normal content copy as a fallback");
                                referenceResourceId = null;
                            }
                        }
                    }
                    if (referenceResourceId == null) {
                        // normal handling (write the resource content data)
                        if (log.isDebugEnabled()) log.debug("Normal resource ("+redit.getId()+") body/contentStream storage");
                        if (redit.m_body == null)
                        {
                            if (redit.m_contentStream == null)
                            {
                                // no body and no stream -- may result from edit in which body is not accessed or modified
                                log.debug("ContentResource committed with no change to contents (i.e. no body and no stream for content): "
                                        + edit.getReference());
                            }
                            else
                            {
                                message += "from stream ";
                                // if we have been configured to use an external file system
                                if (m_bodyPath != null)
                                {
                                    message += "to file";
                                    ok = putResourceBodyFilesystem(edit, redit.m_contentStream, m_bodyPath);
                                }

                                // otherwise use the database
                                else
                                {
                                    message += "to database";
                                    ok = putResourceBodyDb(edit, redit.m_contentStream, m_resourceBodyTableName);
                                }
                            }
                        }
                        else
                        {
                            message += "from byte-array ";
                            byte[] body = ((BaseResourceEdit) edit).m_body;
                            ((BaseResourceEdit) edit).m_body = null;

                            // update the resource body
                            if (body != null)
                            {
                                // if we have been configured to use an external file
                                // system
                                if (m_bodyPath != null)
                                {
                                    message += "to file";
                                    ok = putResourceBodyFilesystem(edit, new ByteArrayInputStream(body), m_bodyPath);
                                }

                                // otherwise use the database
                                else
                                {
                                    message += "to database";
                                    ok = putResourceBodyDb(edit, body, m_resourceBodyTableName);
                                }
                            }
                        }
                    }

                    if (!ok)
                    {
                        cancelResource(edit);
                        ServerOverloadException e = new ServerOverloadException(message);
                        // may be overkill, but let's make sure stack trace gets to log
                        log.error(message, e);
                        throw e;
                    }
                    if(isInsideIndividualDropbox(edit.getId()))
                    {
                        insertIndividualDropboxRecord(getIndividualDropboxId(edit.getId()));
                    }
                    m_resourceStore.commitResource(edit);
                }

            }
            finally
            {
                out();
            }
        }

       
       /** return deleted resource for the given  id */ 
       public ContentResourceEdit editDeletedResource(String id)
       {
           if (id == null || id.trim().length() == 0)
           {
               return null;
           }
           boolean goin = in();
           try
           {
               if (resolver != null && goin)
               {
                   return null; //return (ContentResourceEdit) resolver.editDeletedResource(id);
               }
               else
               {
                   return (ContentResourceEdit) m_resourceDeleteStore.editResource(id);
               }
           }
           finally
           {
               out();
           }
       }

       public void cancelDeletedResource(ContentResourceEdit edit)
       {
           boolean goin = in();
           try
           {
               if (resolver != null && goin)
               {
                   // We don't support deleted resources in resolver at the moment.
                   return;
               }
               else
               {
                   // clear the memory image of the body
                   ((BaseResourceEdit) edit).m_body = null;
                   m_resourceDeleteStore.cancelResource(edit);

               }
           }
           finally
           {
               out();
           }
       }

       /** return deleted resource for the given  id */ 
       public void removeDeletedResource(ContentResourceEdit edit)
       {
           // delete the body
           boolean goin = in();
           try
           {
               if (resolver != null && goin)
               {
                   resolver.removeResource(edit);
               }
               else
               {

                   // if we have been configured to use an external file system
                   if (m_bodyPath != null)
                   {
                       delResourceBodyFilesystem(m_bodyPathDeleted, edit);
                   }

                   // otherwise use the database
                   else
                   {
                       delResourceBodyDb(edit, m_resourceBodyDeleteTableName);
                   }

                   // clear the memory image of the body
                   byte[] body = ((BaseResourceEdit) edit).m_body;
                   ((BaseResourceEdit) edit).m_body = null;

                   m_resourceDeleteStore.removeResource(edit);

               }
           }
           finally
           {
               out();
           }

       }
       
	   /** return a list of deleted resource for the given collection id */ 
	   public List getDeletedResources(ContentCollection collection)
	   {
		   List rv = null;
		   boolean goin = in();
		   try
		   {
			   if (resolver != null && goin)
			   {
				   // rv = resolver.getDeletedResources(collectionId);
			   }
			   else
			   {
				   rv = m_resourceDeleteStore.getAllResourcesWhereLike("IN_COLLECTION", collection.getId() + "%");
			   }
			   return rv;
		   }
		   finally
		   {
			   out();
		   }
	   }


	   // htripath - start
	   /** Add resource to content_resouce_delete table for user deleted resources */
	   public ContentResourceEdit putDeleteResource(String id, String uuid, String userId)
	   {
		   boolean goin = in();
		   try
		   {
			   if (resolver != null && goin)
			   {
				   return (ContentResourceEdit) resolver.putDeleteResource(id, uuid, userId);
			   }
			   else
			   {
				   return (ContentResourceEdit) m_resourceDeleteStore.putDeleteResource(id, uuid, userId, null);
			   }
		   }
		   finally
		   {
			   out();
		   }
	   }

	   /**
		* update xml and store the body of file TODO storing of body content is not used now.
		*/
	   public void commitDeletedResource(ContentResourceEdit edit, String uuid) throws ServerOverloadException
	   {

		   if (m_bodyPathDeleted == null) { 
			   return;
		   }
		   boolean goin = in();
		   try
		   {
			   if (resolver != null && goin)
			   {
				   resolver.commitDeletedResource(edit, uuid);
			   }
			   else
			   {

                   String message = "failed to write file ";
                   BaseResourceEdit redit = (BaseResourceEdit) edit;

				   boolean ok = true;
				   if (redit.m_body == null)
				   {
					   if (redit.m_contentStream == null)
					   {
						   // no body and no stream -- may result from edit in which body is not accessed or modified
						   log.debug("ContentResource committed with no change to contents (i.e. no body and no stream for content): " + edit.getReference());
					   }
					   else
					   {
						   message += "from stream ";
						   // if we have been configured to use an external file system
						   if (m_bodyPath != null)
						   {
							   message += "to file";
							   ok = putResourceBodyFilesystem(edit, redit.m_contentStream, m_bodyPathDeleted);
						   }

						   // otherwise use the database
						   else
						   {
							   message += "to database";
							   ok = putResourceBodyDb(edit, redit.m_contentStream, m_resourceBodyDeleteTableName);
						   }
					   }
				   }
				   else
				   {
					   message += "from byte-array ";
					   byte[] body = ((BaseResourceEdit) edit).m_body;
					   ((BaseResourceEdit) edit).m_body = null;

					   // update the resource body
					   if (body != null)
					   {
						   // if we have been configured to use an external file
						   // system
						   if (m_bodyPath != null)
						   {
							   message += "to file";
							   ok = putResourceBodyFilesystem(edit, new ByteArrayInputStream(body), m_bodyPathDeleted);
						   }

						   // otherwise use the database
						   else
						   {
							   message += "to database";
							   ok = putResourceBodyDb(edit, body, m_resourceBodyDeleteTableName);
						   }
					   }
				   }
				   if (!ok)
				   {
					   cancelResource(edit);
					   ServerOverloadException e = new ServerOverloadException(message);
					   // may be overkill, but let's make sure stack trace gets to log
					   log.error(message, e);
					   throw e;
				   }

				   // update properties in xml and delete locks
				   m_resourceDeleteStore.commitDeleteResource(edit, uuid);
			   }
		   }
		   finally
		   {
			   out();
		   }

	   }

	   public void removeResource(ContentResourceEdit edit)
	   {
		   removeResource(edit, true);
	   }

	   public void removeResource(ContentResourceEdit edit, boolean removeContent)
	   {
		   // delete the body
		   boolean goin = in();
		   try
		   {
			   if (resolver != null && goin)
			   {
				   resolver.removeResource(edit);
			   }
			   else
			   {

				   if (m_bodyPath != null)
				   {
					   // if we have been configured to use an external file system
					   if (removeContent) {
						   log.info("Removing resource ("+edit.getId()+") content: "+m_bodyPath);
						   delResourceBodyFilesystem(m_bodyPath, edit);
					   } else {
						   log.info("Removing original resource reference ("+edit.getId()+") without removing the actual content: "+m_bodyPath);
					   }
				   }
				   else
				   {
					   // otherwise use the database
					   if (removeContent) {
						   delResourceBodyDb(edit, m_resourceBodyTableName);
						   log.info("Removing resource ("+edit.getId()+") DB content");
					   } else {
						   log.info("Removing original resource reference ("+edit.getId()+") without removing the actual DB content");
					   }
				   }

				   // clear the memory image of the body
				   ((BaseResourceEdit) edit).m_body = null;

				   if(isInsideIndividualDropbox(edit.getId()))
				   {
					   insertIndividualDropboxRecord(getIndividualDropboxId(edit.getId()));
				   }
				   m_resourceStore.removeResource(edit);

			   }
		   }
		   finally
		   {
			   out();
		   }

	   }

        /**
         * Read the resource's body.
         * 
         * @param resource
         *        The resource whose body is desired.
         * @return The resources's body content as a byte array.
         * @exception ServerOverloadException
         *            if the server is configured to save the resource body in the filesystem and an error occurs while accessing the server's
         *            filesystem, or
         *            if the server is configured to save the resource body in the database, and the resource cannot be read back from 
         *            the database.
         */
	   public byte[] getResourceBody(ContentResource resource) throws ServerOverloadException
	   {

		   long contentLength = ((BaseResourceEdit) resource).m_contentLength;
		   // If there is not supposed to be data in the file - simply return zero length byte array
		   if (contentLength == 0)
		   {
			   return new byte[0];
		   }

		   if (contentLength > Integer.MAX_VALUE) {
			   throw new ServerOverloadException("content too large to read from body to byte");
		   }

		   //This is guaranteed to not be too big from above
		   byte body[] = new byte[(int)contentLength];
		   byte buffer[] = new byte[512];
		   int totalBytes = 0;
		   int bytesRead = 0;
		   InputStream in = null;

		   try {
			   in = streamResourceBody(resource);
			   if (in == null) {
				   log.warn("Cannot retrieve body for resource " + resource.getId() +". Reset content to empty text.");
				   Arrays.fill(body, (byte)' '); // fill body with spaces
				   return body;
			   } else {
				   while ((bytesRead = in.read(buffer)) != -1 && totalBytes < contentLength) {
					   System.arraycopy(buffer, 0, body, totalBytes, bytesRead);
					   totalBytes += bytesRead;
				   }
			   }
		   } 
		   catch (IOException ioe) 
		   {
			   // If we have a non-zero body length and reading failed, it is an error worth of note
			   log.warn(": failed to read resource: " + resource.getId() + " len: " + contentLength + " : " + ioe);
			   throw new ServerOverloadException("failed to read resource");
			   // return null;
		   }
		   finally 
		   {
			   if (in != null) {
				   try { in.close(); } catch (IOException ignore) {
					   log.warn(": failed to close file stream: ");
				   }
			   }
		   }
		   return body;
	   }

       // the body is already in the resource for this version of storage
       public InputStream streamDeletedResourceBody(ContentResource resource) throws ServerOverloadException
       {

            boolean goin = in();
            try
            {
                if (resolver != null && goin)
                {
                    return resolver.streamResourceBody(resource);
                }
                else
                {
                    long length = ((BaseResourceEdit) resource).m_contentLength;
                    if (length <= 0)
                    {
                        if (length < 0)
                        {
                            log.warn("streamDeletedResourceBody(): negative content length: " + length + "  id: "
                                    + resource.getId());
                            return null;
                        }
                        return null;
                    }

                    // if we have been configured to use an external file system
                    if (m_bodyPath != null)
                    {
                        return streamResourceBodyFilesystem(m_bodyPathDeleted,resource);
                    }

                    // otherwise use the database
                    else
                    {
                        return streamResourceBodyDb(resource, m_resourceBodyDeleteTableName);
                    }
                }
            }
            finally
            {
                out();
            }

        }

        // the body is already in the resource for this version of storage
        public InputStream streamResourceBody(ContentResource resource) throws ServerOverloadException
        {
            boolean goin = in();
            try
            {
                if (resolver != null && goin)
                {
                    return resolver.streamResourceBody(resource);
                }
                else
                {
                    long length = ((BaseResourceEdit) resource).m_contentLength;
                    if (length <= 0)
                    {
                        if (length < 0)
                        {
                            log.warn("streamResourceBody(): negative content length: " + ((BaseResourceEdit) resource).m_contentLength + "  id: "
                                    + resource.getId());
                            return null;
                        }
                        return new ByteArrayInputStream(new byte[0]);
                    }

                    // if we have been configured to use an external file system
                    if (m_bodyPath != null)
                    {
                        return streamResourceBodyFilesystem(m_bodyPath,resource);
                    }

                    // otherwise use the database
                    else
                    {
                        return streamResourceBodyDb(resource, m_resourceBodyTableName);
                    }
                }
            }
            finally
            {
                out();
            }
        }
        
        /**
         * Return a URI containing a direct link to the asset.
         * 
         * @param rootFolder
         * @param resource
         * @return URI representing a direct link to the asset or null
         */
        public URI getDirectLink(ContentResource resource)
        {
        	try {
        		// SAK-30325 - HTML items not being BaseResourceEdits causes a
        		// ClassCastException here, which gets swallowed and turned into a 404.
        		// This is an ugly hack because of the necessary casting here (to get m_filePath).
        		// This is another case where the nested classes and fuzzy boundaries causes
        		// rather sloppy object orientation. A more complete treatment would reevaluate
        		// the interfaces, remove the Edits, and extract these classes and casts.
        		if (resource instanceof WrappedContentResource || !(resource instanceof BaseResourceEdit)) {
        			return null;
        		}
        		return fileSystemHandler.getAssetDirectLink(((BaseResourceEdit) resource).m_id, m_bodyPath, ((BaseResourceEdit) resource).m_filePath);
        	}
        	catch (IOException e) {
        		log.debug("No direct link available for resource: " + resource.getId());
        	}
        	
        	return null;
        }

        /**
         * Return an input stream.
         * 
         * @param resource the resource to resolve to a stream
         *        It is a non-fatal error for the file not to be readible as long as the resource's expected length is
         *        zero. In this case, a null stream is returned. Otherwise, attempt to prepare a stream for reading the
         *        file body and fail with an exception on error.
         */

        protected InputStream streamResourceBodyFilesystem(String rootFolder, ContentResource resource) throws ServerOverloadException
        {
            if (((BaseResourceEdit) resource).m_contentLength == 0)
            {
                // Zero-length files are not written, so don't bother checking the filesystem.
                return null;
            }

            try
            {
                return fileSystemHandler.getInputStream(((BaseResourceEdit) resource).m_id, rootFolder, ((BaseResourceEdit) resource).m_filePath);
            }
            catch (IOException e)
            {
                // If we have a non-zero body length and reading failed, it is an error worth of note
                log.error("Failed to read resource: " + resource.getId() + " len: " + ((BaseResourceEdit) resource).m_contentLength, e);
                throw new ServerOverloadException("Failed to read resource body", e);
            }
        }

        /**
         * When resources are stored, zero length bodys are not placed in the table hence this routine will return a null when the particular resource
		 * body is not found
		 * @param resourceBodyTableName The table to pull the resource body from.

         */
        protected InputStream streamResourceBodyDb(ContentResource resource, String resourceBodyTableName) throws ServerOverloadException
        {
            // get the resource from the db
            String sql = contentServiceSql.getBodySql(resourceBodyTableName);

            Object[] fields = new Object[1];
            fields[0] = resource.getId();

            // get the stream, set expectations that this could be big
            InputStream in = m_sqlService.dbReadBinary(sql, fields, true);

            return in;
        }

        /**
         * Write the resource body to the database table.
         * 
         * @param resource
         *        The resource whose body is being written.
         * @param body
         *        The body bytes to write. If there is no body or the body is zero bytes, no entry is inserted into the table.
         * @return true if the resource body is written successfully, false otherwise.
         */
        protected boolean putResourceBodyDb(ContentResourceEdit resource, byte[] body, String resourceBodyTableName)
        {
            if ((body == null) || (body.length == 0)) return true;

            if (log.isDebugEnabled()) log.debug("Making resource ("+resource.getId()+") copy of DB resource body");
            // delete the old
            String statement = contentServiceSql.getDeleteContentSql(resourceBodyTableName);

            Object[] fields = new Object[1];
            fields[0] = resource.getId();

            m_sqlService.dbWrite(statement, fields);

            // add the new
            statement = contentServiceSql.getInsertContentSql(resourceBodyTableName);

            boolean success = m_sqlService.dbWriteBinary(statement, fields, body, 0, body.length);
            if (log.isDebugEnabled()) log.debug("putResourceBodyDb: resource ("+resource.getId()+") put success="+success);
            return success;

            /*
             * %%% BLOB code // read the record's blob and update statement = "select body from " + m_resourceTableName + " where ( resource_id = '" +
             * Validator.escapeSql(resource.getId()) + "' ) for update"; Sql.dbReadBlobAndUpdate(statement, ((BaseResource)resource).m_body);
             */
        }

        /**
         * @param edit
         * @param stream
         * @return true if the resource body is written successfully, false otherwise.
         */
        protected boolean putResourceBodyDb(ContentResourceEdit edit, InputStream stream, String resourceBodyTableName)
        {
            // Do not create the files for resources with zero length bodies
            if ((stream == null)) return true;

            ByteArrayOutputStream bstream = new ByteArrayOutputStream();

            long byteCount = 0;

            // chunk
            byte[] chunk = new byte[STREAM_BUFFER_SIZE];
            int lenRead;
            try
            {
                while ((lenRead = stream.read(chunk)) != -1)
                {
                    bstream.write(chunk, 0, lenRead);
                    byteCount += lenRead;
                }

                edit.setContentLength(byteCount);
                ResourcePropertiesEdit props = edit.getPropertiesEdit();
                props.addProperty(ResourceProperties.PROP_CONTENT_LENGTH, Long.toString(byteCount));
                if (edit.getContentType() != null)
                {
                    props.addProperty(ResourceProperties.PROP_CONTENT_TYPE, edit.getContentType());
                }
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                log.error("IOException ", e);
            }
            finally
            {
                if (stream != null)
                {
                    try
                    {
                        stream.close();
                    }
                    catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        log.error("IOException ", e);
                    }
                }
            }

            if (byteCount > Integer.MAX_VALUE)
            {
                log.warn("Attempted to write file of size > 2G to database content store");
                return false;
            }

            boolean ok = true;
            if (bstream != null && bstream.size() > 0)
            {
                ok = putResourceBodyDb(edit, bstream.toByteArray(), resourceBodyTableName);
            }

            return ok;
        }

        /**
         * @param edit
         * @param stream
         * @return true if the resource body is written successfully, false otherwise.
         */
        private boolean putResourceBodyFilesystem(ContentResourceEdit resource, InputStream stream, String rootFolder)
        {
            try
            {
                long byteCount = fileSystemHandler.saveInputStream(((BaseResourceEdit) resource).m_id, rootFolder, ((BaseResourceEdit) resource).m_filePath, stream);
                resource.setContentLength(byteCount);
                ResourcePropertiesEdit props = resource.getPropertiesEdit();
                props.addProperty(ResourceProperties.PROP_CONTENT_LENGTH, Long.toString(byteCount));
                if (resource.getContentType() != null)
                {
                    props.addProperty(ResourceProperties.PROP_CONTENT_TYPE, resource.getContentType());
                }
                return true;
            }
            catch (IOException e)
            {
                log.error("IOException", e);
                return false;
            }
        }

        /**
         * Write the resource body to the external file system. The file name is the m_bodyPath with the resource id appended.
         * 
         * @param resource
         *        The resource whose body is being written.
         * @param body
         *        The body bytes to write. If there is no body or the body is zero bytes, no entry is inserted into the filesystem.
         * @return true if the resource body is written successfully, false otherwise.
         */
        protected boolean putResourceBodyFilesystem(ContentResourceEdit resource, byte[] body)
        {
            // Do not create the files for resources with zero length bodies
            if (body == null) return true;

			return putResourceBodyFilesystem(resource, new ByteArrayInputStream(body), m_bodyPath);

        }

        /*
         * Delete the resource body from the database table.
         * 
         * @param resource
         *        The resource whose body is being deleted.
		 * @param resourceBodyTableName The table in which the deleted resource bodies are stored.

         */
        protected void delResourceBodyDb(ContentResourceEdit resource, String resourceBodyTableName)
        {
			if (resource.getContentLength() > 0) {
				// delete the record
				String statement = contentServiceSql.getDeleteContentSql(resourceBodyTableName);

				Object[] fields = new Object[1];
				fields[0] = resource.getId();

				m_sqlService.dbWrite(statement, fields);
			}
        }

        /**
         * Delete the resource body from the external file system. The file name is the m_bodyPath with the resource id appended.
         * 
         * @param resource
         *        The resource whose body is being written.
         */
        protected void delResourceBodyFilesystem(String rootFolder, ContentResourceEdit resource)
        {
            fileSystemHandler.delete(((BaseResourceEdit) resource).m_id, rootFolder, ((BaseResourceEdit) resource).m_filePath);
        }

        public int getMemberCount(String collectionId)
        {
            if (collectionId == null || collectionId.trim().length() == 0)
            {
                return 0;
            }
            boolean goin = in();
            try
            {
                if (resolver != null && goin)
                {
                    return resolver.getMemberCount(collectionId);
                }
                else
                {

                    int fileCount = 0;
                    try
                    {
                        fileCount = countQuery(contentServiceSql.getNumContentResources3Sql(), collectionId);
                    }
                    catch (IdUnusedException e)
                    {
                        // ignore -- means this is not a collection or the collection contains no files, so zero is right answer
                    }
                    int folderCount = 0;
                    try
                    {
                        folderCount = countQuery(contentServiceSql.getNumContentResources4Sql(), collectionId);
                    }
                    catch (IdUnusedException e)
                    {
                        // ignore -- means this is not a collection or the collection contains no folders, so zero is right answer
                    }
                    ;
                    return fileCount + folderCount;
                }
            }
            finally
            {
                out();
            }
        }

        public Collection<String> getMemberCollectionIds(String collectionId)
        {
            List list = null;
            String sql = contentServiceSql.getCollectionIdSql(m_collectionTableName);
            Object[] fields = new Object[1];
            fields[0] = collectionId;

            list = m_sqlService.dbRead(sql, fields, null);

            return (Collection<String>) list;
        }

        public Collection<String> getMemberResourceIds(String collectionId)
        {
            List list = null;

            String sql = contentServiceSql.getResourceId3Sql(m_resourceTableName);
            Object[] fields = new Object[1];
            fields[0] = collectionId;

            list = m_sqlService.dbRead(sql, fields, null);
            return (Collection<String>) list;
        }

        public Collection<ContentResource> getResourcesOfType(String resourceType, int pageSize, int page) 
        {
            if(pageSize > MAXIMUM_PAGE_SIZE)
                pageSize = MAXIMUM_PAGE_SIZE;

            String key = "getResourcesOfType@" + resourceType + ":" + pageSize + ":" + page;
            List resources = (List) threadLocalManager.get(key);

            if (resources == null) {
                resources = this.m_resourceStore.getAllResourcesWhere("RESOURCE_TYPE_ID", resourceType, "RESOURCE_ID", page * pageSize, pageSize);
                if (resources != null) {
                    threadLocalManager.set(key, resources);
                }
            }

            return resources;
        }

        public Collection<ContentResource> getContextResourcesOfType(String resourceType, Set<String> contextIds) 
        {
            if ( resourceType == null || contextIds == null || contextIds.size() == 0 )
                return null;

            ArrayList resources = new ArrayList();
            StringBuilder sqlWhere = new StringBuilder("WHERE RESOURCE_TYPE_ID = '"+resourceType+"' AND CONTEXT IN (");
            int numContext = 0;

            for ( Iterator it=contextIds.iterator(); it.hasNext(); )
            {
                sqlWhere.append("'");
                sqlWhere.append( (String)it.next() );
                sqlWhere.append("'");
                if ( numContext+1<contextIds.size() )
                    sqlWhere.append(",");

                // run query if at end of context list (or at MAX_IN_QUERY)
                if ( (numContext % MAX_IN_QUERY == 0 && numContext > 0) ||
                        (numContext+1 == contextIds.size()) )
                {
                    sqlWhere.append(")");
                    resources.addAll( this.m_resourceStore.getSelectedResourcesWhere( sqlWhere.toString() ) );
                    sqlWhere = new StringBuilder("WHERE RESOURCE_TYPE_ID = '"+resourceType+"' AND CONTEXT IN (");
                }
                numContext++;
            }

            return resources;
        }

        public class EntityReader implements SqlReader
        {

            public Object readSqlResultRecord(ResultSet result) 
            {
                BaseResourceEdit edit = null;
                Object clob = null;
                try
                {
                    clob = result.getObject(1);
                    if(clob != null && clob instanceof byte[])
                    {
                        edit = new BaseResourceEdit();
                        resourceSerializer.parse(edit, (byte[]) clob);
                    }
                }
                catch(SQLException e)
                {
                    // ignore?
                    log.debug("SqlException unable to read entity");
                }
                catch(EntityParseException e)
                {
                    log.warn("EntityParseException unable to parse entity");
                }
                if(edit == null)
                {
                    try
                    {
                        String xml = result.getString(2);
                        if (xml == null)
                        {
                            log.warn("EntityReader: null xml : " );
                            return null;
                        }

                        // read the xml
                        Document doc = Xml.readDocumentFromString(xml);
                        if (doc == null)
                        {
                            log.warn("EntityReader: null xml doc : " );
                            return null;
                        }

                        // verify the root element
                        Element root = doc.getDocumentElement();
                        if (!root.getTagName().equals("resource"))
                        {
                            log.warn("EntityReader: XML root element not resource: " + root.getTagName());
                            return null;
                        }
                        edit = new BaseResourceEdit(root);

                    }
                    catch(SQLException e)
                    {
                        log.debug("SqlException problem with results");
                    }
                }
                return edit;
            }

        }

        /**
         * @return the m_collectionStorageFields
         */
        public String getCollectionStorageFields()
        {
            return m_collectionStorageFields;
        }


        /**
         * @return the m_resourceStorageFields
         */
        public String getResourceStorageFields()
        {
            return m_resourceStorageFields;
        }

    } // DbStorage

    @SuppressWarnings("unchecked")
    public Map<String, Long> getMostRecentUpdate(String id) 
    {
        Map<String, Long> map = new HashMap<String, Long>();

        String sql = contentServiceSql.getSiteDropboxChangeSql();

        Object[] fields = new Object[1];
        fields[0] = id;

        List<TimeEntry> list = m_sqlService.dbRead(sql, fields, new TimeReader());

        for(TimeEntry entry : list)
        {
            if(entry == null)
            {
                continue;
            }
            map.put(entry.getKey(), entry.getValue());
        }

        return map;
    }

    /** We allow these characters to go un-escaped into the file name. */
    static protected final String VALID_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.";


    /**
     * Return file system safe escaped name, that's also unique if the initial id is unique. * Use only the name, not the path part of the id
     * 
     * @param value
     *        The id to escape.
     * @return value escaped.
     */
    protected String escapeResourceName(String id)
    {
        if (id == null) return null;

        try
        {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < id.length(); i++)
            {
                char c = id.charAt(i);

                // if not valid, escape
                if (VALID_CHARS.indexOf(c) == -1)
                {
                    // the escape character
                    buf.append('_');

                    // the character value.
                    buf.append(Integer.toHexString(c));
                }
                else
                {
                    buf.append(c);
                }
            }

            String rv = buf.toString();
            return rv;
        }
        catch (Exception e)
        {
            log.error("escapeResourceName: ", e);
            return id;
        }
    }


    /**
     * Create a file system body binary for any content_resource record that has a null file_path.
     */
    protected void convertToFile()
    {
        log.info("convertToFile");

        //final Pattern contextPattern = Pattern.compile("\\A/group/(.+?)/");

        try
        {
            // get a connection for the updates
            final Connection connection = m_sqlService.borrowConnection();
            boolean wasCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            // get a connection for reading binary
            final Connection sourceConnection = m_sqlService.borrowConnection();

            final Counter count = new Counter();

            // read content_resource records that have null file path
            String sql = contentServiceSql.getResourceIdXmlSql();
            m_sqlService.dbRead(sql, null, new SqlReader()
            {
                public Object readSqlResultRecord(ResultSet result)
                {
                    String id = null;
                    BaseResourceEdit edit = null;

                    try
                    {
                        Object clob = result.getObject(3);
                        if(clob != null && clob instanceof byte[])
                        {
                            edit = new BaseResourceEdit();
                            resourceSerializer.parse(edit, (byte[]) clob);
                        }
                    }
                    catch(SQLException e)
                    {
                        // ignore?
                        log.debug("convertToFile(): SqlException unable to read entity");
                        edit = null;
                    }
                    catch(EntityParseException e)
                    {
                        log.warn("convertToFile(): EntityParseException unable to parse entity");
                        edit = null;
                    }
                    if(edit == null)
                    {
                        try
                        {
                            String xml = result.getString(2);
                            if (xml == null)
                            {
                                log.warn("convertToFile(): null xml : " );
                                return null;
                            }

                            // read the xml
                            Document doc = Xml.readDocumentFromString(xml);
                            if (doc == null)
                            {
                                log.warn("convertToFile(): null xml doc : " );
                                return null;
                            }

                            // verify the root element
                            Element root = doc.getDocumentElement();
                            if (!root.getTagName().equals("resource"))
                            {
                                log.warn("convertToFile(): XML root element not resource: " + root.getTagName());
                                return null;
                            }
                            edit = new BaseResourceEdit(root);

                        }
                        catch(SQLException e)
                        {
                            log.debug("convertToFile(): SqlException problem with results");
                        }
                    }

                    if(edit == null)
                    {
                        return null;
                    }

                    // zero length?
                    if (edit.getContentLength() == 0)
                    {
                        log.warn("convertToFile(): zero length body ");

                    }

                    id = edit.getId();

                    if(id == null)
                    {
                        return null;
                    }


                    // is resource body in db there?
                    String sql = contentServiceSql.getResourceId2Sql();
                    Object[] fields = new Object[1];
                    fields[0] = id;
                    List found = m_sqlService.dbRead(sourceConnection, sql, fields, null);
                    if ((found == null) || (found.size() == 0))
                    {
                        // not found
                        log.warn("convertToFile(): body not found in source : " + id);
                    }

                    // get the creation date (or modified date, or now)
                    Time created = null;
                    try
                    {
                        created = edit.getProperties().getTimeProperty(ResourceProperties.PROP_CREATION_DATE);
                    }
                    catch (Exception any)
                    {
                        try
                        {
                            created = edit.getProperties().getTimeProperty(ResourceProperties.PROP_MODIFIED_DATE);
                        }
                        catch (Exception e)
                        {
                            created = timeService.newTime();
                        }
                    }

                    // form the file name
                    edit.setFilePath(created);

                    try
                    {
                        // read the body from the source
                        sql = contentServiceSql.getBodySql(m_resourceBodyTableName);
                        InputStream stream = m_sqlService.dbReadBinary(sql, fields, true);

                        //byte[] body = new byte[edit.m_contentLength];
                        //m_sqlService.dbReadBinary(sourceConnection, sql, fields, body);

                        // write the body to the file
                        boolean ok = ((DbStorage) m_storage).putResourceBodyFilesystem(edit, stream, m_bodyPath);
                        if (!ok)
                        {
                            log.warn("convertToFile: body file failure : " + id + " file: " + edit.m_filePath);
                            return null;
                        }
                    }
                    catch (ServerOverloadException e)
                    {
                        log.debug("convertToFile(): ServerOverloadException moving resource body for " + id);
                        return null;
                    }

                    // write resource back to db, now with file path set

                    try
                    {
                        // regenerate the serialization
                        byte[] serialization = resourceSerializer.serialize(edit);

                        Matcher contextMatcher = contextPattern.matcher(id);
                        String context = null;
                        if(contextMatcher.find())
                        {
                            String root = contextMatcher.group(1);
                            context = contextMatcher.group(2);
                            if(! root.equals("group/"))
                            {
                                context = "~" + context;
                            }
                        }

                        // update the record
                        sql = contentServiceSql.getUpdateContentResource3Sql();
                        fields = new Object[6];
                        fields[0] = edit.m_filePath;
                        fields[1] = serialization;
                        fields[2] = context;
                        fields[3] = Long.valueOf(edit.m_contentLength);
                        fields[4] = edit.getResourceType();
                        fields[5] = id;

                        m_sqlService.dbWrite(connection, sql, fields);

                        count.value++;
                        if ((count.value % 1000) == 0)
                        {
                            connection.commit();
                            log.info(" ** converted: " + count.value);
                        }

                        return null;
                    }
                    catch (EntityParseException e)
                    {
                        log.debug("convertToFile(): EntityParseException for " + id);
                        return null;
                    }
                    catch (SQLException e) {
                        log.info(" ** exception converting : " + id + " : ", e);
                        return null;
                    }
                }
            });

            connection.commit();

            log.info("convertToFile: converted resources: " + count.value);

            m_sqlService.returnConnection(sourceConnection);

            connection.setAutoCommit(wasCommit);
            m_sqlService.returnConnection(connection);
        }
        catch (Exception t)
        {
            log.warn("convertToFile: failed: " + t);
        }

        log.info("convertToFile: done");
    }

    /**
     * <p>
     * Counter is is a counter that can be marked final.
     * </p>
     */
    public class Counter
    {
        public int value = 0;
    }

    public Collection<Lock> getLocks(String id)
    {
        return m_lockManager.getLocks(id);
    }

    public void lockObject(String id, String lockId, String subject, boolean system)
    {
        if (log.isDebugEnabled()) log.debug("lockObject has been called on: " + id);
        try
        {
            m_lockManager.lockObject(id, lockId, subject, system);
        }
        catch (Exception e)
        {
            log.warn("lockObject failed: " + e);
            return;
        }
        if (log.isDebugEnabled()) log.debug("lockObject succeeded");
    }

    public void removeLock(String id, String lockId)
    {
        m_lockManager.removeLock(id, lockId);
    }

    public boolean isLocked(String id)
    {
        return m_lockManager.isLocked(id);
    }

    public boolean containsLockedNode(String id)
    {
        throw new RuntimeException("containsLockedNode has not been implemented");
    }

    public void removeAllLocks(String id)
    {
        m_lockManager.removeAllLocks(id);
    }

    protected List getFlatResources(String parentId)
    {
        return m_storage.getFlatResources(parentId);
    }

    public ContentHostingHandlerResolverImpl getContentHostingHandlerResolver()
    {
        return contentHostingHandlerResolver;
    }

    public void setContentHostingHandlerResolver(ContentHostingHandlerResolverImpl contentHostingHandlerResolver)
    {
        this.contentHostingHandlerResolver = contentHostingHandlerResolver;
    }

    public boolean isContentHostingHandlersEnabled()
    {
        return (this.contentHostingHandlerResolver != null);
    }

    public class TimeEntry implements Map.Entry<String, Long>
    {
        protected String key;
        protected Long value;

        /**
         * @param key
         * @param value
         */
        public TimeEntry(String key, Long value) 
        {
            this.key = key;
            this.value = value;
        }
        public String getKey() 
        {
            return key;
        }

        public Long getValue() 
        {
            return value;
        }

        public void setKey(String arg0)
        {
            this.key = arg0;
        }

        public Long setValue(Long arg0) 
        {
            this.value = arg0;
            return this.value;
        }

    }

    public class TimeReader implements SqlReader
    {
        public Object readSqlResultRecord(ResultSet result) 
        {
            try
            {
                String individualDropboxId = result.getString(1);
                long time = result.getLong(2);

                return new TimeEntry(individualDropboxId, Long.valueOf(time));
            }
            catch(Exception e)
            {
                log.warn("TimeReader.readSqlResultRecord(): " + result.toString() + " exception: " + e);
            }
            return null;
        }

    }

    public void populateNewColumns()
    {
        String sql1 = contentServiceSql.getAccessResourceIdAndXmlSql(m_resourceTableName);
        m_sqlService.dbRead(sql1, null, new ContextAndFilesizeReader(m_resourceTableName));

        String sql2 = contentServiceSql.getAccessResourceIdAndXmlSql(m_resourceDeleteTableName);
        m_sqlService.dbRead(sql2, null, new ContextAndFilesizeReader(m_resourceDeleteTableName));
    }


    public class ContextAndFilesizeReader implements SqlReader
    {
        protected IdManager uuidManager = (IdManager) ComponentManager.get(IdManager.class);
        protected Pattern filesizePattern1 = Pattern.compile("\\scontent-length=\"(\\d+)\"\\s");
        protected Pattern filesizePattern2 = Pattern.compile("\\s*DAV:getcontentlength\\s+(\\d+)\\s*");
        protected Pattern typeidPattern1 = Pattern.compile("\\sresource-type=\"([0-9A-Za-z.]+)\"\\s");
        protected Pattern typeidPattern2 = Pattern.compile("\\s+%(.*)\\s+");
        protected String table;

        public ContextAndFilesizeReader(String table)
        {
            this.table = table;
        }

        public Object readSqlResultRecord(ResultSet result) 
        {
            try
            {
                boolean addingUuid = false;
                String resourceId = result.getString(1);
                String uuid = result.getString(2);
                String xml = result.getString(3);

                if(uuid == null)
                {
                    addingUuid = true;
                    uuid = uuidManager.createUuid();
                }
                String context = null;
                int filesize = 0;
                String resourceType = null;

                String sql = contentServiceSql.getContextFilesizeValuesSql(table, addingUuid);

                Matcher contextMatcher = contextPattern.matcher(resourceId);
                if(xml == null)
                {
                    ResultSetMetaData rsmd = result.getMetaData();
                    int numberOfColumns = rsmd.getColumnCount();

                    if(numberOfColumns > 3)
                    {
                        Blob binary_entity = result.getBlob(4);
                        // this is meant to match against the binary-entity value in cases where 
                        // xml is null (meaning xml may have already been converted). 
                        //						Matcher filesizeMatcher = filesizePattern2.matcher(xml);
                        //						if(filesizeMatcher.find())
                        //						{
                        //							try
                        //							{
                        //								filesize = Integer.parseInt(filesizeMatcher.group(1));
                        //							}
                        //							catch(Exception e)
                        //							{
                        //								// do nothing
                        //							}
                        //						}
                        //						Matcher typeidMatcher = typeidPattern2.matcher(xml);
                        //						if(typeidMatcher.find())
                        //						{
                        //							resourceType = typeidMatcher.group(1);
                        //						}
                    }
                    else
                    {
                        // Do nothing.  The binary-entity value is not available here.  
                        // Best to skip the record until we provide a query that gets 
                        // the binary-entity value in this context.
                    }
                }
                else
                {
                    Matcher filesizeMatcher = filesizePattern1.matcher(xml);
                    if(filesizeMatcher.find())
                    {
                        try
                        {
                            filesize = Integer.parseInt(filesizeMatcher.group(1));
                        }
                        catch(Exception e)
                        {
                            // do nothing
                        }
                    }
                    Matcher typeidMatcher = typeidPattern1.matcher(xml);
                    if(typeidMatcher.find())
                    {
                        resourceType = typeidMatcher.group(1);
                    }
                }

                if(contextMatcher.find())
                {
                    String root = contextMatcher.group(1);
                    context = contextMatcher.group(2);
                    if(! root.equals("group/"))
                    {
                        context = "~" + context;
                    }
                }

                log.info("adding new field values: resourceId == \"" + resourceId + "\" uuid == \"" + uuid + "\" context == \"" + context + "\" filesize == \"" + filesize + "\" addingUuid == " + addingUuid);

                if(addingUuid)
                {
                    // "update " + table + " set CONTEXT = ?, FILE_SIZE = ?, RESOURCE_TYPE_ID = ?, RESOURCE_UUID = ? where RESOURCE_ID = ?"
                    // update the record
                    Object [] fields = new Object[5];
                    fields[0] = context;
                    fields[1] = Integer.valueOf(filesize);
                    fields[2] = resourceType;
                    fields[3] = uuid;
                    fields[4] = resourceId;
                    m_sqlService.dbWrite(sql, fields);
                }
                else
                {
                    // "update " + table + " set CONTEXT = ?, FILE_SIZE = ?, RESOURCE_TYPE_ID = ? where RESOURCE_UUID = ?"
                    // update the record
                    Object [] fields = new Object[4];
                    fields[0] = context;
                    fields[1] = Integer.valueOf(filesize);
                    fields[2] = resourceType;
                    fields[3] = uuid;
                    m_sqlService.dbWrite(sql, fields);
                }
            }
            catch(Exception e)
            {
                log.error("ContextAndFilesizeReader.readSqlResultRecord() failed. result skipped", e);
            }

            return null;
        }
    }

    protected long getSizeForContext(String context) 
    {
        long size = 0L;

	String sql = contentServiceSql.getQuotaQuerySql();
	Object [] fields = new Object[] {context.startsWith(COLLECTION_DROPBOX)?context+"%":context};
	if (context.startsWith(COLLECTION_DROPBOX)) {
	    if (context.split(Entity.SEPARATOR).length == 4) {
		// User Folder
		sql = contentServiceSql.getDropBoxQuotaQuerySql();
	    } else {
		// Root Folder - KNL-1084, SAK-22169
		fields = new Object[] {context+"%",context,context};
		sql = contentServiceSql.getDropBoxRootQuotaQuerySql(); 
	    }
	}

        List list = m_sqlService.dbRead(sql, fields, null);
        if(list != null && ! list.isEmpty())
        {
            String result = (String) list.get(0);
            try
            {
                size = Float.valueOf(result).longValue();
            }
            catch(Exception e)
            {
                log.warn("getSizeForContext() unable to parse long from \"" + result + "\" for context \"" + context + "\"");
            }
        }

        return size;
    }

    /**
     * @throws Exception 
     * 
     */
    private void validateUTF8Db() throws Exception
    {
        Connection connection = m_sqlService.borrowConnection();
        try
        {
            testUTF8Transport(connection);
        }
        finally
        {
            m_sqlService.returnConnection(connection);
        }

    }	

    public void testUTF8Transport(Connection connection) throws Exception
    {
        /*
         * byte[] b = new byte[102400]; byte[] b2 = new byte[102400]; byte[] b3 =
         * new byte[102400]; char[] cin = new char[102400]; Random r = new
         * Random(); r.nextBytes(b);
         */
        byte[] bin = new byte[1024];
        char[] cin = new char[1024];
        byte[] bout = new byte[1024];

        {
            int i = 0;
            for (int bx = 0; i < bin.length; bx++)
            {
                bin[i++] = (byte) bx;
            }
        }
        ByteStorageConversion.toChar(bin, 0, cin, 0, cin.length);
        String sin = new String(cin);

        char[] cout = sin.toCharArray();
        ByteStorageConversion.toByte(cout, 0, bout, 0, cout.length);

        for (int i = 0; i < bin.length; i++)
        {
            if (bin[i] != bout[i])
            {
                throw new Exception("Internal Byte conversion failed at " + bin[i] + "=>"
                        + (int) cin[i] + "=>" + bout[i]);
            }
        }

        PreparedStatement statement = null;
        PreparedStatement statement2 = null;
        PreparedStatement statement3 = null;
        ResultSet rs = null;
        try
        {
            statement3 = connection
            .prepareStatement("delete from CONTENT_RESOURCE where  RESOURCE_ID =  ?");
            statement3.clearParameters();
            statement3.setString(1, UTF8TESTID);
            statement3.executeUpdate();

            statement = connection
            .prepareStatement("insert into CONTENT_RESOURCE ( RESOURCE_ID, XML ) values ( ?, ? )");
            statement.clearParameters();
            statement.setString(1, UTF8TESTID);
            statement.setString(2, sin);
            statement.executeUpdate();

            statement2 = connection
            .prepareStatement("select XML from CONTENT_RESOURCE where RESOURCE_ID = ? ");
            statement2.clearParameters();
            statement2.setString(1, UTF8TESTID);
            rs = statement2.executeQuery();
            String sout = null;
            if (rs.next())
            {
                sout = rs.getString(1);
            }
            rs.close();


            statement3.clearParameters();
            statement3.setString(1, UTF8TESTID);
            statement3.executeUpdate();


            if (sout != null) {
                cout = sout.toCharArray();
            }
            ByteStorageConversion.toByte(cout, 0, bout, 0, cout.length);

            if (sout != null) {
                if (sin.length() != sout.length())
                {
                    throw new Exception(
                            "UTF-8 Data was lost communicating with the database, please "
                            + "check connection string and default table types (Truncation/Expansion)");
                }
            }

            for (int i = 0; i < bin.length; i++)
            {
                if (bin[i] != bout[i])
                {
                    throw new Exception(
                            "UTF-8 Data was corrupted communicating with the database, "
                            + "please check connectionstring and default table types (Conversion)"
                            + "" + bin[i] + "=>" + (int) cin[i] + "=>" + bout[i]);
                }
            }


        }
        finally
        {
            try
            {
                rs.close();
            }
            catch (Exception ex)
            {

            }
            try
            {
                statement3.close();
            }
            catch (Exception ex)
            {

            }
            try
            {
                statement2.close();
            }
            catch (Exception ex)
            {

            }
            try
            {
                statement.close();
            }
            catch (Exception ex)
            {

            }
        }

    }

    /**
     * @return the migrateData
     */
    public boolean isMigrateData()
    {
        return migrateData;
    }

    /**
     * @param migrateData the migrateData to set
     */
    public void setMigrateData(boolean migrateData)
    {
        this.migrateData = migrateData;

    }

    /**
     *	 {@inheritDoc}
     */
    public Collection<ContentResource> getContextResourcesOfType(String resourceType, Set<String> contextids) 
    {
        return m_storage.getContextResourcesOfType(resourceType, contextids);
    }

    /**
     *	 {@inheritDoc}
     */
    public Collection<ContentResource> getResourcesOfType(String resourceType, int pageSize, int page) 
    {
        return  m_storage.getResourcesOfType(resourceType, pageSize, page);
    }

}
