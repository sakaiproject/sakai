/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2007 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/

/**
 * 
 */
package org.sakaiproject.chat2.migration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.PropertyResourceBundle;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author chrismaurer
 *
 */
public class ChatDataMigration extends Task { 
   
   private String outputFile = "./migration/chat.sql";
   //private ChatService chatService = null;
   private SqlService sqlService = null;
   
   private static boolean debug = false;
   protected static Object compMgr;
   private Statement stmt;
   
   private ResourceLoader toolBundle;
   
   private static Log logger = LogFactory.getLog(ChatDataMigration.class);
   
   
   public void execute() throws BuildException {
      super.execute();
      printDebug("*******EXECUTE");
      try {
         oneTimeSetup();
         //localInit();
         testDataMigration();
      }
      catch (Exception e) {
         //printDebug(e.getMessage());
         this.log(e.getMessage());
         throw new BuildException(e);
      }
   }
   
   
   protected void setUp() throws Exception {
      printDebug("*******SETUP");
      
      //    Get the services we need for the tests
      sqlService = (SqlService) getService(SqlService.class.getName());
      printDebug("*******GOT SQL SERVICE");
   }

   protected void tearDown() throws Exception {
      printDebug("*******TEARDOWN");
      sqlService = null;      
   }

   public void testDataMigration() throws Exception {
      printDebug("*******BEGIN");
      setUp();
      load();
      tearDown();
      printDebug("*******DONE");
   }
   
   public void load() throws Exception {
      printDebug("*******outputDir: " + outputFile);

      Connection connection = null;
      
      PrintWriter sqlFile = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, false)), true);

      try {
         //Add a new column for us to keep track
         sqlFile.println(getMessageFromBundle("alter.channel"));
         
         sqlFile.println(getMessageFromBundle("alter.message"));

         connection = sqlService.borrowConnection();
         printDebug("*******BORROWED A CONNECTION");
         runChannelMigration(connection, sqlFile);
         
      }
      catch (Exception e) {
         printDebug(e.toString());
         this.log(e.getMessage());
         throw new Exception(e);         
      }
      finally {
         if (connection != null) {
            try {
               sqlService.returnConnection(connection);
            }
            catch (Exception e) {
               // can't do anything with this.
            }
         }
      }
   }
   
   protected void runChannelMigration(Connection con, PrintWriter output) {
      logger.info("runChannelMigration()");
      printDebug("*******GETTING CHANNELS");
      
      String sql = getMessageFromBundle("select.oldchannels");
      
      try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            try {
               while (rs.next()) {
                  /* 
                   * CHANNEL_ID                                               
                   * NEXT_ID     
                   * XML 
                   */
                  String oldId = rs.getString("CHANNEL_ID");
                  Object xml = rs.getObject("XML");
                  
                  printDebug("*******FOUND CHANNEL: " + oldId);
                  printDebug("*******FOUND CHANNEL: " + xml);
                  
                  Document doc = Xml.readDocumentFromString((String)xml);

                  // verify the root element
                  Element root = doc.getDocumentElement();
                  String context = root.getAttribute("context");
                  //String context = "test";
                  String title = root.getAttribute("id");
                  String newChannelId = IdManager.createUuid();
                  
                  //TODO Chat lookup the config params?
                  String outputSql = getMessageFromBundle("insert.channel", new Object[] {
                        newChannelId, context, null, title, "SelectMessagesByTime", 3, 0, oldId});
                  /* 
                   * CHANNEL_ID, 
                   * CONTEXT, 
                   * CREATION_DATE, 
                   * title, 
                   * description, 
                   * filterType, 
                   * filterParam, 
                   * contextDefaultChannel, 
                   * migratedChannelId
                   */
                  
                  output.println(outputSql);
                  
                  //Get the messages for each channel
                  runMessageMmigration(con, output, oldId);
                  
               }
           } finally {
               rs.close();
           }
        } catch (Exception e) {
            logger.error("error selecting data with this sql: " + sql);
            logger.error("", e);
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
            }
        }
        logger.info("Migration task fininshed: runChannelMigration()");
   }
   
   protected void runMessageMmigration(Connection con, PrintWriter output, String oldChannelId) {
      logger.info("runMessageMmigration()");
      printDebug("*******GETTING MESSAGES");
      
      String sql = getMessageFromBundle("select.oldmessages", new Object[] {oldChannelId});
      //String sql = "select c.channel_id, c.xml from chat_channel c";
      
      try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            try {
               while (rs.next()) {
                  /*
                   * CHANNEL_ID                                               
                   * MESSAGE_ID                            
                   * DRAFT     
                   * PUBVIEW     
                   * OWNER                                 
                   * MESSAGE_DATE          
                   * XML
                   * 
                   */
                  //String oldChannelId = rs.getString("CHANNEL_ID");
                  String oldMessageId = rs.getString("MESSAGE_ID");
                  String owner = rs.getString("OWNER");
                  Date messageDate = rs.getDate("MESSAGE_DATE");
                  Object xml = rs.getObject("XML");
                  
                  printDebug("*******FOUND MESSAGE: " + oldMessageId);
                  printDebug("*******FOUND MESSAGE: " + xml);
                  
                  Document doc = Xml.readDocumentFromString((String)xml);

                  // verify the root element
                  Element root = doc.getDocumentElement();
                  String body = root.getAttribute("body");
                  //String body = "test";

                  String newMessageId = IdManager.createUuid();
                  
                  String outputSql = getMessageFromBundle("insert.message", new Object[] {
                        newMessageId, oldChannelId, owner, messageDate, body, oldMessageId});
                  /*
                   * insert into CHAT2_MESSAGE (MESSAGE_ID, CHANNEL_ID, OWNER, MESSAGE_DATE, BODY) \
                        values ('{0}', '{1}', '{2}', '{3}', '{4}');
                  
                  */
                  output.println(outputSql);
               }
           } finally {
               rs.close();
           }
        } catch (Exception e) {
            logger.error("error selecting data with this sql: " + sql);
            logger.error("", e);
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
            }
        }
        logger.info("Migration task fininshed: runChannelMigration()");
      
   }
   
   protected static void printDebug(String message) {
      if (debug) {
         System.out.println(message);
      }
   }
   

   
   
   public String getOutputFile() {
      return outputFile;
   }
   public void setOutputFile(String outputFile) {
      this.outputFile = outputFile;
   }   
   public static boolean isDebug() {
      return debug;
   }
   public static void setDebug(boolean debug) {
      ChatDataMigration.debug = debug;
   }
   
   
   /**
    * Initialize the component manager once for all tests, and log in as admin.
    */
   protected static void oneTimeSetup() throws Exception {
      if(compMgr == null) {
         // Find the sakai home dir
         String tomcatHome = getTomcatHome();
         String sakaiHome = tomcatHome + File.separatorChar + "sakai" + File.separatorChar;
         String componentsDir = tomcatHome + "components/";
         
         // Set the system properties needed by the sakai component manager
         System.setProperty("sakai.home", sakaiHome);
         System.setProperty("sakai.components.root", componentsDir);

         logger.debug("Starting the component manager");

         // Add the sakai jars to the current classpath.  Note:  We are limited to using the sun jvm now
         URL[] sakaiUrls = getJarUrls(new String[] {tomcatHome + "common/endorsed/",
               tomcatHome + "common/lib/", tomcatHome + "shared/lib/"});
         //URLClassLoader appClassLoader = (URLClassLoader)Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader(ChatDataMigration.class.getClassLoader().getParent());
         URLClassLoader appClassLoader = (URLClassLoader)Thread.currentThread().getContextClassLoader();
         printDebug("*******THREAD CLASSLOADER: " + Thread.currentThread().getContextClassLoader());
         
         //URLClassLoader appClassLoader = (URLClassLoader)ChatDataMigration.class.getClassLoader().getParent();
         Method addMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] {URL.class});
         addMethod.setAccessible(true);
         for(int i=0; i<sakaiUrls.length; i++) {
            //printDebug("*******CLASSLOADING - " + sakaiUrls[i]);
            addMethod.invoke(appClassLoader, new Object[] {sakaiUrls[i]});
         }
         
         //URLClassLoader appClassLoader = URLClassLoader.newInstance(sakaiUrls);
         //compMgr = appClassLoader.loadClass("org.sakaiproject.component.cover.ComponentManager");
         
         //appClassLoader.
         /*
         Method addMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] {URL.class});
         addMethod.setAccessible(true);
         for(int i=0; i<sakaiUrls.length; i++) {
            addMethod.invoke(appClassLoader, new Object[] {sakaiUrls[i]});
         }
         */

         Class clazz = Class.forName("org.sakaiproject.component.cover.ComponentManager");
         compMgr = clazz.getDeclaredMethod("getInstance", (Class[])null).invoke((Object[])null, (Object[])null);

         logger.debug("Finished starting the component manager");
      }
   }
   
   /**
    * Close the component manager when the tests finish.
    */
   public static void oneTimeTearDown() {
      try {
      if(compMgr != null) {
         Method closeMethod = compMgr.getClass().getMethod("close", new Class[0]);
         closeMethod.invoke(compMgr, new Object[0]);
      }
      }
      catch (Exception e) {
         logger.error(e);
      }
   }

   /**
    * Fetches the "maven.tomcat.home" property from the maven build.properties
    * file located in the user's $HOME directory.
    * 
    * @return
    * @throws Exception
    */
   private static String getTomcatHome() throws Exception {
      printDebug("*******GET_TOMCAT_HOME");
      String testTomcatHome = System.getProperty("test.tomcat.home");
      if ( testTomcatHome != null && testTomcatHome.length() > 0 ) {
         logger.debug("Using tomcat home: " + testTomcatHome);
         return testTomcatHome;
      } else {
         String homeDir = System.getProperty("user.home");
         File file = new File(homeDir + File.separatorChar + "build.properties");
         FileInputStream fis = new FileInputStream(file);
         PropertyResourceBundle rb = new PropertyResourceBundle(fis);
         String tomcatHome = rb.getString("maven.tomcat.home");
         logger.debug("Tomcat home = " + tomcatHome);
         return tomcatHome;
      }
   }   
   
   /**
    * Builds an array of file URLs from a directory path.
    * 
    * @param dirPath
    * @return
    * @throws Exception
    */
   private static URL[] getJarUrls(String dirPath) throws Exception {
      File dir = new File(dirPath);
      File[] jars = dir.listFiles(new FileFilter() {
         public boolean accept(File pathname) {
            if(pathname.getName().startsWith("xml-apis")) {
               return false;
            }
            else if (pathname.getName().startsWith("osp-warehouse")) {
               return false;
            }
            return true;
         }
      });
      URL[] urls = new URL[jars.length];
      for(int i = 0; i < jars.length; i++) {
         urls[i] = jars[i].toURL();
         printDebug("*******JARS: " + urls[i]);
      }
      return urls;
   }
   

   private static URL[] getJarUrls(String[] dirPaths) throws Exception {
      List<URL> jarList = new ArrayList<URL>();
      
      // Add all of the tomcat jars
      for(int i=0; i<dirPaths.length; i++) {
         jarList.addAll(Arrays.asList(getJarUrls(dirPaths[i])));
      }

      URL[] urlArray = new URL[jarList.size()];
      jarList.toArray(urlArray);
      return urlArray;
   }
   
   
   
   /**
    * Convenience method to get a service bean from the Sakai component manager.
    * 
    * @param beanId The id of the service
    * 
    * @return The service, or null if the ID is not registered
    */
   protected static final Object getService(String beanId) {
      try {
         Method getMethod = compMgr.getClass().getMethod("get", new Class[] {String.class});
         return getMethod.invoke(compMgr, new Object[] {beanId});
      } catch (Exception e) {
         logger.error(e);
         return null;
      }
   }
   
   /**
    * Looks up the db vendor from the SqlService
    * @return The string for the db vendor (mysql, oracle, hsqldb, etc)
    */
   private String getDbPrefix() {
      return sqlService.getVendor();
   }
   
   /**
    * Looks up the sql statements defined in chat-sql.properties.  Appends the db
    * vendor key to the beginning of the message (oracle.select.channel, mysql.select.channel, etc)
    * @param key
    * @return
    */
   private String getMessageFromBundle(String key) {
      if (toolBundle == null)
         toolBundle = new ResourceLoader("chat-sql");
      
      return toolBundle.getString(getDbPrefix().concat("." + key));
   }
   
   private String getMessageFromBundle(String key, Object[] args) {
      return MessageFormat.format(getMessageFromBundle(key), args);
   }
   
   
   
   
}
