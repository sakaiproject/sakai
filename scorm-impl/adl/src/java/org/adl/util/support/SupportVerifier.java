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

package org.adl.util.support;

import java.security.PrivilegedAction;
import java.security.AccessController;

import org.adl.util.MessageBox;
import org.adl.util.EnvironmentVariable;

/**
 * <strong>Filename:</strong>SupportVerifier.java<br><br>
 *
 * <strong>Description:</strong><br>
 * A <code>SupportVerifier</code> provides the ability to display support 
 * errors.  If the software was not tested for support, messages will be 
 * displayed in the following manner:
 * <ul>
 *    <li><strong>Operating System</strong> - If the user is running the software
 *     on an unsupported or untested Operating System, a warning is displayed 
 *     and the user is allowed to continue.</li>
 *    <li><strong>Java Version</strong> - If the user is running the software 
 *    with an unsupported or untested Java Version, an error is displayed and 
 *    the program is terminated.</li>
 * </ul>
 *
 * <strong>Design Issues:</strong><br><br>
 *
 * <strong>Implementation Issues:</strong><br><br>
 *
 * <strong>Known Problems:</strong><br><br>
 *
 * <strong>Side Effects:</strong><br>
 * If an unsupported Java version is detected, the program will terminate.
 * <br><br>
 *
 * <strong>References:</strong><br><br>
 *
 * @author ADL Technical Team
 */
public class SupportVerifier
{

   /**
    * Array of supported Java Versions.
    */
   private static final String[] mSupportedJRE = { "1.5.0_09",
                                                   "1.5.0_08",
                                                   "1.5.0_07",
                                                   "1.5.0_06",
                                                   "1.5.0_05",
                                                   "1.5.0_04",
                                                   "1.5.0_03",
                                                   "1.5.0_02",
                                                   "1.5.0_01",
                                                   "1.5.0",
                                                   "1.4.2_12",
                                                   "1.4.2_11",
                                                   "1.4.2_10",
                                                   "1.4.2_09",
                                                   "1.4.2_08",
                                                   "1.4.2_07",
                                                   "1.4.2_06",
                                                   "1.4.2_05",
                                                   "1.4.2_04",
                                                   "1.4.2_03",
                                                   "1.4.2_02",
                                                   "1.4.2_01",
                                                   "1.4.2"  };


   /**
    * Array of supported Operating Systems.
    */
   private static final String[] mSupportedOS = { "Windows XP",
                                                  "Windows 2000" };
   
   /**
    * Default constructor.  The default constructor does nothing explicitly
    *
    */
   public SupportVerifier()
   {
      // Does nothing explicitly
   }

   /**
    * This method is the default constructor of the <code>SupportVerifer</code>
    * class.  It controls all support verification sequences.
    */
   public void verifySupport()
   {
      verifyOSSupport();
      verifyJRESupport();
   }

   /**
    * This method handles all Java Version support verification sequences.
    */
   private void verifyJRESupport()
   {
      String jreVersion = System.getProperty("java.version");

      boolean jreSupported = false;
      int arrayLength = mSupportedJRE.length;

      for ( int i = 0; i < arrayLength; i++ )
      {
         if ( jreVersion.equals( mSupportedJRE[i] ) )
         {
            jreSupported = true;
            break;
         }
      }

      if ( ! jreSupported )
      {
         String title = new String("Environment Error");
         String messageText = "This software has NOT been tested with the " +
                       "installed Java Runtime Environment (JRE) Version:  " +
                       jreVersion + ".  \nThere is no guarantee that the software " +
                       "will function properly.  \nSee the Readme for " +
                       "detailed installation instructions and tested JREs " +
                       "prior to operating this software.";
         
         MessageBox mb = new MessageBox( MessageBox.WARNING, messageText,
                                         title );
      }
   }

   /**
    * This method handles all Operating System support verification sequences.
    */
   private void verifyOSSupport()
   {
      String osName = System.getProperty("os.name");
      
      boolean osSupported = false;
      int arrayLength = mSupportedOS.length;

      for ( int i = 0; i < arrayLength; i++ )
      {
         if ( osName.equalsIgnoreCase( mSupportedOS[i] ) )
         {
            osSupported = true;
            break;
         }
      }

      if ( ! osSupported )
      {
         String title = new String("Environment Warning");
         String messageText = "This software has NOT been tested with the Operating System: " 
             + osName + ".  \nThere is no guarantee that the software will function properly.  " +
             "\nSee the Readme for detailed installation instructions " +
             "and support information prior to operating the software.";
         MessageBox mb = new MessageBox( MessageBox.WARNING, messageText, title );
      }
   }

   /**
    * This method verifies that the environment variable was set up properly.
    * If not a messages is sent to the user.
    * 
    * @param iKey The environment variable that is being verified.
    */
   public void verifyEnvironmentVariable( String iKey )
   {
      String value = EnvironmentVariable.getValue( iKey );

      if ( value.equals("") )
      {
         String title = new String("Environment Error");
         String messageText = "The \"" + iKey + "\" Environment Variable could " +
                              "not be detected.  This Environment\n Variable " +
                              "must be set correctly for successful " +
                              "operation of this software.";
         MessageBox mb = new MessageBox( MessageBox.ERROR, messageText, title );
      }
   }


   /**
    * This method handles all Java Version support verification sequences.
    * 
    * @return A boolean that indicates whether or not the appropriate Java 
    * Run-Time Environment is being used.
    */
   public boolean verifyJRESupportBoolean()
   {
      String jreVersion = System.getProperty("java.version");

      boolean jreSupported = false;
      int arrayLength = mSupportedJRE.length;

      for ( int i = 0; i < arrayLength; i++ )
      {
         if ( jreVersion.equals( mSupportedJRE[i] ) )
         {
            jreSupported = true;
            break;
         }
      }

      return jreSupported;
   }

   /**
    * This method handles all Operating System support verification sequences.
    * 
    * @return A boolean that indicates whether or not the operating system
    * being used is one that is supported.
    */
   public boolean verifyOSSupportBoolean()
   {
      String osName = System.getProperty("os.name");

      boolean osSupported = false;
      int arrayLength = mSupportedOS.length;

      for ( int i = 0; i < arrayLength; i++ )
      {
         if ( osName.equalsIgnoreCase( mSupportedOS[i] ) )
         {
            osSupported = true;
            break;
         }
      }

      return osSupported;
   }

   /**
    * This method handles all Operating System support verification sequences.
    * 
    * @return A string representing the current Operating System that the 
    * system is running on.
    */
   public String getCurrentOS()
   {
      String osName = System.getProperty("os.name");
      PrivilegedGetSP psp = new PrivilegedGetSP("sun.os.patch.level");
      
      String patch = (AccessController.doPrivileged(psp)).toString();
      String abbreviatedPatch = patch.replaceAll("Service Pack", "SP");
      String os = osName + " - " + abbreviatedPatch;
      

      return os;
   }

   /**
    * This method handles all Java Version support verification sequences.
    * 
    * @return A string representing the current Java Run-Time Environment that
    * the system is using.
    */
   public String getCurrentJRE()
   {
      String jreVersion = System.getProperty("java.version");

      return jreVersion;
   }
   
   /**
    *
    * <strong>Description:</strong><br>This is a inner class that permits
    * the ability to retrieve information about the system.
    *
    */
   private class PrivilegedGetSP implements PrivilegedAction
   {
      /**
       * The string representation of the service pack
       */
      String mSPKey;
      
      /**
       * The value of the service pack
       */
      Object mSPValue;
      
      /**
       * Constructor for the inner class
       * 
       * @param iSPKey The service pack key
       */
      PrivilegedGetSP( String iSPKey )
      {
         mSPKey     = iSPKey;
      }

      /**
       * This run method grants privileged applet code access to write
       * to the summary log.  This allows the applet to work in Netscape 6.
       *
       * @return Object
       *
       */
      public Object run()
      {
         try
         {
            mSPValue = System.getProperty(mSPKey);
         }
         catch(Exception e)
         {
            e.printStackTrace();
         }

         return mSPValue;
      }
   }
}
