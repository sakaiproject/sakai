/*******************************************************************************
**
** Advanced Distributed Learning Co-Laboratory (ADL Co-Lab) Hub grants you 
** ("Licensee") a non-exclusive, royalty free, license to use, modify and 
** redistribute this software in source and binary code form, provided that 
** i) this copyright notice and license appear on all copies of the software; 
** and ii) Licensee does not utilize the software in a manner which is 
** disparaging to ADL Co-Lab Hub.
**
** This software is provided "AS IS," without a warranty of any kind.  ALL 
** EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING 
** ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE 
** OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED.  ADL Co-Lab Hub AND ITS LICENSORS 
** SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF 
** USING, MODIFYING OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES.  IN NO 
** EVENT WILL ADL Co-Lab Hub OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, 
** PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, 
** INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE 
** THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE 
** SOFTWARE, EVEN IF ADL Co-Lab Hub HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH 
** DAMAGES.
**
*******************************************************************************/

package org.adl.samplerte.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.adl.util.debug.DebugIndicator;


/**
 * <strong>Filename:</strong> LMSDatabaseHandler.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * The <code>LMSDatabaseHandler</code> provides database connection and utility
 *  
 * <br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the 
 * SCORM 2004 3rd Edition Sample RTE 1.0. <br>
 * <br>
 * 
 * <strong>Implementation Issues:</strong><br><br>
 * 
 * <strong>Known Problems:</strong><br><br>
 * 
 * <strong>Side Effects:</strong><br><br>
 * 
 * <strong>References:</strong><br>
 * <ul>
 *     <li>IMS SS Specification</li>
 *     <li>SCORM 2004</li>
 * </ul>
 * 
 * @author ADL Technical Team
 */
public class LMSDatabaseHandler 
{

   /**
    * This controls display of log messages to the java console
    */
   private static boolean _Debug = DebugIndicator.ON; 

   /**
    * Default Constructor
    */
   public LMSDatabaseHandler()
   {
      /* default constructor*/
   }


   /**
    * Initializes the database connection.
    * 
    *
    * @return  Returns a database connection.
    */
   public static Connection getConnection() 
   {

      Connection conn = null; 

      try
      {
         if ( _Debug )
         {
            System.out.println("  ::--> Connecting to the DB");
         }

         String driverName = "sun.jdbc.odbc.JdbcOdbcDriver";
         String connectionURL = "jdbc:odbc:SCORM3RDSRTE10";
         
         java.util.Properties prop = new java.util.Properties();
         prop.put("charSet", "utf-8");
                  
         Class.forName(driverName).newInstance();
         conn = DriverManager.getConnection(connectionURL, prop);         

      }
      catch ( SQLException ex )
      {              
         System.out.println(" database handler sql exception " + 
                            ex.getSQLState());
         ex.printStackTrace();
      }
      catch ( Exception e )
      {               
         System.out.println("  LMSDatabaseHandler exception");
         e.printStackTrace();
      }

      return conn;
   }


   /**
    * Closes the database connection.
    * 
    * 
    *              
    
   public void CloseConnection()
   {   
     if ( conn != null )
      {
         try
         {
            if ( ! conn.isClosed() )
            {
               conn.close();
            }
         }
         catch ( SQLException ex )
         {
            System.out.println("  ::--> ERROR:  Could not close DB");
            ex.printStackTrace();
         }
      }

      conn = null;
   }  */
}
