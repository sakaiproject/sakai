
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
package org.ims.ssp.samplerte.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.adl.util.debug.DebugIndicator;


/**
 * Provides database connection utility functions to the shared global
 * objective DB.<br><br>
 *
 * <strong>Filename:</strong> SSP_DBHandler.java<br><br>
 *
 * <strong>Description:</strong><br>
 * The <code>SSP_DBHandler</code> provides database connection and utility
 *
 * <br><br>
 *
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the 
 *    SCORM 2004 3rd Edition Sample RTE. <br>
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
 *     <li>IMS SSP Specification
 *     <li>SCORM 2004 3rd Edition
 * </ul>
 *
 * @author ADL Technical Team
 */
public class SSP_DBHandler
{

   /**
    * This controls display of log messages to the java console
    */
   private static boolean _Debug = DebugIndicator.ON;

   /**
    * Default constructor for the SSP_DBHandler class
    */
   public SSP_DBHandler()
   {
      //Default Constructor.
   }

   /**
    * Initializes the database connection.
    *
    * @return A connection to the DB or <code>null</code> if the connection can
    *         not be established.
    */
   public static Connection getConnection()
   {
      Connection conn = null;
      try
      {
         if ( _Debug )
         {
            System.out.println("  ::--> Connecting to the SSP DB");
         }

         String driverName = "sun.jdbc.odbc.JdbcOdbcDriver";
         String connectionURL = "jdbc:odbc:SCORM3RDSSP10";
         
         java.util.Properties prop = new java.util.Properties();
         prop.put("charSet", "utf-8");
         
         Class.forName(driverName).newInstance();
         conn = DriverManager.getConnection(connectionURL, prop);

         if ( _Debug )
         {
            System.out.println("  ::--> Connection successful");
         }
      }
      catch ( SQLException ex )
      {
         if ( _Debug )
         {
            System.out.println("  ::--> ERROR:  Could not connect to SSP DB");
            System.out.println("  ::-->  " + ex.getSQLState());
         }
         ex.printStackTrace();
      }
      catch ( Exception e )
      {
         if ( _Debug )
         {
            System.out.println("  ::--> ERROR:  Unexpected exception");
         }
         e.printStackTrace();
      }
 
      return conn;
   }

}  // SSP_DBHandler
