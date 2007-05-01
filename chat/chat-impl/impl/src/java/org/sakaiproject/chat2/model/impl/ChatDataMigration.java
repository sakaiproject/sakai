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
package org.sakaiproject.chat2.model.impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



/**
 * @author chrismaurer
 *
 */
public class ChatDataMigration { 
   protected final transient Log logger = LogFactory.getLog(getClass());
   
   private boolean debug = false;
   private String outputFile = "/chat-migration.sql";
   
   private SqlService sqlService = null;
   
   private Statement stmt;
   
   private ResourceLoader toolBundle;
   
   private boolean performChatMigration = false;
   private boolean chatMigrationExecuteImmediate = true;
   
   
   /** init thread - so we don't wait in the actual init() call */
   public class ChatDataMigrationThread extends Thread
   {
      /**
       * construct and start the init activity
       */
      public ChatDataMigrationThread()
      {
         //m_ready = false;
         start();
      }

      /**
       * run the init
       */
      public void run()
      {
         try {
            load();
         } catch (Exception e) {
            logger.warn("Error with ChatDataMigrationThread.run()", e);
         }
      }
   }
   
   /**
    * Called on after the startup of the singleton.  This sets the global
    * list of functions which will have permission managed by sakai
    * @throws Exception
    */
   protected void init() throws Exception
   {
      logger.info("init()");
      
      try {         
         if (performChatMigration) {
            new ChatDataMigrationThread();
            //load();
         }         
      }
      catch (Exception e) {
         logger.warn("Error with ChatDataMigration.init()", e);
      }
      
   }
   
   /**
    * Destroy
    */
   public void destroy()
   {
      logger.info("destroy()");
   }
   
   public void load() throws Exception {
      printDebug("*******outputDir: " + outputFile);
      logger.info("Running Chat Migration; output file: " + outputFile + " immediate: " + chatMigrationExecuteImmediate);
      Connection connection = null;
      
      PrintWriter sqlFile = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, false)), true);

      try {
         
         sqlFile.println("--" + getDbPrefix());
         /*
         //Add a new columns for us to keep track
         String alterChannelTable = getMessageFromBundle("alter.channel");
         String alterMessageTable = getMessageFromBundle("alter.message");
         
         sqlFile.println(alterChannelTable);
         sqlFile.println(alterMessageTable);
         
         if (chatMigrationExecuteImmediate) {
            try {
               sqlService.dbWrite(null, alterChannelTable, null);
            }
            catch (Exception e) {
               logger.warn("Channel table has already been modified to accept migration data.");
            }
            try {
               sqlService.dbWrite(null, alterMessageTable, null);
            }
            catch (Exception e) {
               logger.warn("Message table has already been modified to accept migration data.");
            }
         }
         */
         
         sqlFile.println();
         
         connection = sqlService.borrowConnection();
         printDebug("*******BORROWED A CONNECTION");
         runChannelMigration(connection, sqlFile);
         
         runMessageMigration(connection, sqlFile);
         
      }
      catch (Exception e) {
         printDebug(e.toString());
         logger.error(e.getMessage());
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
      logger.info("Chat migration complete");
   }
   
   protected void runChannelMigration(Connection con, PrintWriter output) {
      logger.debug("runChannelMigration()");
      printDebug("*******GETTING CHANNELS");
      
      String sql = getMessageFromBundle("select.oldchannels");
      
      try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            try {
               while (rs.next()) {
                  /* 
                   * CHANNEL_ID
                   * XML
                   */
                  String oldId = rs.getString("CHANNEL_ID");
                  Object xml = rs.getObject("XML");
                  
                  printDebug("*******FOUND CHANNEL: " + oldId);
                  printDebug("*******FOUND CHANNEL: " + xml);
                  
                  Document doc = null;
                  try {
                     doc = Xml.readDocumentFromString((String)xml);
                  }
                  catch (ClassCastException cce) {
                     Clob xmlClob = (Clob) xml;
                     doc = Xml.readDocumentFromStream(xmlClob.getAsciiStream());
                  }

                  // verify the root element
                  Element root = doc.getDocumentElement();
                  String context = root.getAttribute("context");
                  String title = root.getAttribute("id");
                  String newChannelId = escapeSpecialChars(oldId);
                  
                  //TODO Chat lookup the config params?
                  String outputSql = getMessageFromBundle("insert.channel", new Object[]{
                        newChannelId, context, null, title, "", "SelectMessagesByTime", 3, 0, oldId, 1});
                  /* 
                   * CHANNEL_ID, 
                   * CONTEXT, 
                   * CREATION_DATE, 
                   * title, 
                   * description, 
                   * filterType, 
                   * filterParam, 
                   * contextDefaultChannel, 
                   * migratedChannelId,
                   * ENABLE_USER_OVERRIDE
                   */
                  
                  output.println(outputSql + ";");
                  if (chatMigrationExecuteImmediate) {
                     sqlService.dbWrite(null, outputSql, null);
                  }
                  
                  //Get the messages for each channel
                  //runMessageMigration(con, output, oldId, newChannelId);
                  
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
        logger.debug("Migration task fininshed: runChannelMigration()");
   }
   
   protected void runMessageMigration(Connection con, PrintWriter output) {
      logger.debug("runMessageMigration()");
      printDebug("*******GETTING MESSAGES");
      
      //String sql = getMessageFromBundle("select.oldmessages", new Object[]{oldChannelId});
      String sql = getMessageFromBundle("select.oldmessages");
      //String sql = "select c.channel_id, c.xml from chat_channel c";
      
      try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            try {
               while (rs.next()) {
                  /*
                   * MESSAGE_ID
                   * CHANNEL_ID                            
                   * XML
                   * OWNER                          
                   * MESSAGE_DATE
                   */
                  String oldMessageId = rs.getString("MESSAGE_ID");
                  String oldChannelId = rs.getString("CHANNEL_ID");
                  Object xml = rs.getObject("XML");
                  String owner = rs.getString("OWNER");
                  Date messageDate = rs.getTimestamp("MESSAGE_DATE");
                  
                  printDebug("*******FOUND MESSAGE: " + oldMessageId);
                  printDebug("*******FOUND MESSAGE: " + xml);
                  
                  Document doc = null;
                  try {
                     doc = Xml.readDocumentFromString((String)xml);
                  }
                  catch (ClassCastException cce) {
                     Clob xmlClob = (Clob) xml;
                     doc = Xml.readDocumentFromStream(xmlClob.getAsciiStream());
                  }

                  // verify the root element
                  Element root = doc.getDocumentElement();
                  String body = Xml.decodeAttribute(root, "body");
                  //String body = root.getAttribute("body");
                  //String body = "test";

                  String newMessageId = oldMessageId;
                  newMessageId = newMessageId.replaceAll("/", "_");
                  
                  String outputSql = getMessageFromBundle("insert.message", new Object[] {
                        newMessageId, escapeSpecialChars(oldChannelId), owner, messageDate, escapeSingleQuotes(body), oldMessageId});
                  /*
                   * insert into CHAT2_MESSAGE (MESSAGE_ID, CHANNEL_ID, OWNER, MESSAGE_DATE, BODY) \
                        values ('{0}', '{1}', '{2}', '{3}', '{4}');
                  
                  */
                  output.println(outputSql + ";");
                  if (chatMigrationExecuteImmediate) {
                     sqlService.dbWrite(null, outputSql, null);
                  }
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
        logger.debug("Migration task fininshed: runMessageMigration()");
      
   }
   
   /**
    * Escapes special characters that may be bad in sql statements
    * -- "/" is replaced with "_"
    * See also escapeSingleQuotes(String input)
    * @param input Original string to parse
    * @return A string with any special characters escaped
    */
   protected String escapeSpecialChars(String input) {
      String output = escapeSingleQuotes(input);
      output = output.replaceAll("/", "_");
      return output;
   }
   
   /**
    * Escapes single quotes
    * -- "'" is replaced with "''"
    * @param input Original string to parse
    * @return A string with any special characters escaped
    */
   protected String escapeSingleQuotes(String input) {
      String output = input.replaceAll("'", "''");
      return output;
   }
   
   
   protected void printDebug(String message) {
      if (debug) {
         //System.out.println(message);
         //logger.debug(message);
         logger.info("DEBUG: " + message);
      }
   }
   
   /**
    * Looks up the db vendor from the SqlService
    * @return The string for the db vendor (mysql, oracle, hsqldb, etc)
    */
   protected String getDbPrefix() {
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
   
   public boolean isChatMigrationExecuteImmediate() {
      return chatMigrationExecuteImmediate;
   }

   public void setChatMigrationExecuteImmediate(
         boolean chatMigrationExecuteImmediate) {
      this.chatMigrationExecuteImmediate = chatMigrationExecuteImmediate;
   }

   public boolean isPerformChatMigration() {
      return performChatMigration;
   }

   public void setPerformChatMigration(boolean performChatMigration) {
      this.performChatMigration = performChatMigration;
   }

   public boolean isDebug() {
      return debug;
   }

   public void setDebug(boolean debug) {
      this.debug = debug;
   }

   public String getOutputFile() {
      return outputFile;
   }

   public void setOutputFile(String outputFile) {
      this.outputFile = outputFile;
   }

   public SqlService getSqlService() {
      return sqlService;
   }

   public void setSqlService(SqlService sqlService) {
      this.sqlService = sqlService;
   }
   
}
