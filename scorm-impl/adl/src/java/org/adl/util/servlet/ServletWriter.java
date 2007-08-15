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

package org.adl.util.servlet;

import java.net.*;
import java.io.*;
import org.adl.util.debug.DebugIndicator;


/**
 * Provides a means to 'POST' multiple serialized objects to a servlet.<br><br>
 *
 * <strong>Filename:</strong> ServletWriter<br><br>
 *
 * <strong>Description:</strong><br>
 * This class provides a method of posting multiple serialized objects to a
 * Java servlet and getting objects in return. This code was inspired by code
 * samples from the book 'Java Servlet Programming' by Jason Hunter and William
 * Crawford (O'Reilly & Associates. 1998).<br><br>
 *
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the SCORM 2004 3rd Edtion Sample
 * RTE. <br>
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
 *     <li>SCORM 2004 3rd Edition
 * </ul>
 *
 * @author ADL Technical Team
 */
public class ServletWriter
{

   /**
    * This controls display of log messages to the java console
    */
   private static boolean _Debug = DebugIndicator.ON;


   /**
    * Provides a means to 'POST' multiple serialized objects to a servlet.
    *
    * @param iServlet   The URL of the target servlet.
    *
    * @param iObjs      A list of objects to be serialized during the POST.
    *
    * @return A stream of serialized objects.
    * @exception Exception
    */
   static public ObjectInputStream postObjects(URL iServlet,
                                               Serializable iObjs[])
                                               throws Exception
   {

      if ( _Debug )
      {
         System.out.println("In ServletWriter::postObjects()");
      }

      URLConnection con = null;

      try
      {
         if ( _Debug )
         {
            System.out.println("Opening HTTP URL connection to " +
                               "servlet.");
         }

         con = iServlet.openConnection();
      }
      catch ( Exception e )
      {
         System.out.println("e = 1");

         if ( _Debug )
         {
            System.out.println("Exception caught in " +
                               "ServletWriter::postObjects()");
            e.printStackTrace();
         }

         System.out.println(e.getMessage());
         throw e;
      }


      if ( _Debug )
      {
         System.out.println("HTTP connection to servlet is open");
         System.out.println("configuring HTTP connection properties");
      }

      con.setDoInput(true);
      con.setDoOutput(true);
      con.setUseCaches(false);
      con.setRequestProperty("Content-Type","text/plain");
      con.setAllowUserInteraction(false);

      // Write the arguments as post data
      ObjectOutputStream out = null;

      try
      {
         if ( _Debug )
         {
            System.out.println("Creating new http output stream");
         }

         out = new ObjectOutputStream(con.getOutputStream());

         if ( _Debug )
         {
            System.out.println("Created new http output stream.");
            System.out.println("Writing command and data to servlet...");
         }

         int numObjects = iObjs.length;

         if ( _Debug )
         {
            System.out.println ("Num objects: " + numObjects);
         }

         for ( int i = 0; i < numObjects; i++ )
         {
            out.writeObject( iObjs[i]);

            if ( _Debug )
            {
               System.out.println("Just wrote a serialized object on " +
                                  "output stream... " +
                                  iObjs[i].getClass().getName());
            }
         }
      }
      catch ( Exception e )
      {
         if ( _Debug )
         {
            System.out.println("Exception caught in " +
                               "ServletWriter::postObjects()");
            System.out.println(e.getMessage());
         }

         e.printStackTrace();
         throw e;
      }

      try
      {
         if ( _Debug )
         {
            System.out.println("Flushing Object Output Stream.");
         }
         out.flush();
      }
      catch ( IOException ioe )
      {
         if ( _Debug )
         {
            System.out.println("Caught IOException when calling " +
                               "out.flush()");
            System.out.println(ioe.getMessage());
         }

         ioe.printStackTrace();
         throw ioe;
      }
      catch ( Exception e )
      {
         if ( _Debug )
         {
            System.out.println("Caught Exception when calling " +
                               "out.flush()" );
            System.out.println(e.getMessage());
         }

         e.printStackTrace();
         throw e;
      }

      try
      {
         if ( _Debug )
         {
            System.out.println("Closing object output stream.");
         }
         out.close();
      }
      catch ( IOException  ioe )
      {
         if ( _Debug )
         {
            System.out.println("Caught IOException when calling " +
                               "out.close()");
            System.out.println(ioe.getMessage());
         }

         ioe.printStackTrace();
         throw ioe;
      }
      catch ( Exception e )
      {
         if ( _Debug )
         {
            System.out.println("Caught Exception when calling " +
                               "out.close()");
            System.out.println(e.getMessage());
         }

         e.printStackTrace();
         throw e;
      }

      ObjectInputStream in;

      try
      {
         if ( _Debug )
         {
            System.out.println("Creating new http input stream.");
         }

         in = new ObjectInputStream(con.getInputStream());
      }
      catch ( Exception e )
      {
         if ( _Debug )
         {
            System.out.println("Exception caught in " +
                               "ServletWriter::postObjects()");
            System.out.println( e.getMessage() );
         }
         e.printStackTrace();
         throw e;
      }

      return in;
   }

} // ServletWriter
