/******************************************************************************
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
******************************************************************************/

package org.adl.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * <strong>Filename: </strong>Messages.java<br><br>
 *
 * <strong>Description: </strong> <br>
 * The <code>Messages</code> creates a resource bundle that contains all of the
 * messages for the logging of the ADL Conformance Test Suite.    <br>
 *
 * @author ADL Technical Team<br><br>
 */
public class Messages
{
   /**
    * The fully qualified name of the resource bundle.  This bundle contains
    * all of the messages used during the Content Test Suite logging.
    */
   private static final String BUNDLE_NAME = 
      "org.adl.util.messages";

   /**
    * The formal reference to the <code>ResourceBundle</code>
    */
   private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
      .getBundle( BUNDLE_NAME );

   /**
    * Default constructor.  No explicitly defined functionality for this 
    * constructor.
    */
   private Messages()
   {
      // No explicitly defined functionality
   }

   /**
    * Returns the CTS logging message associated with the given key. The method 
    * retrieves a parameterized message based on the input key and replaces the 
    * parameter with the input parameter. If the key is not found a default 
    * message based on the key will be returned.
    * 
    * @param iKey A unique identifier that identifies a message.
    * @return The message associated with the key.
    */
   public static String getString( String iKey )
   {
      try
      {
         return RESOURCE_BUNDLE.getString( iKey );
      }
      catch ( MissingResourceException e )
      {
         return '!' + iKey + '!';
      }
   }
   
   /**
    * Returns the CTS logging message associated with the given key. The method 
    * retrieves a parameterized message based on the input key and replaces the 
    * parameter with the input parameter. If the key is not found a default 
    * message based on the key will be returned.
    * 
    * @param iKey A unique identifier that identifies the parameterized message.
    * 
    * @param iParam A parameter to be used to replace the parameterized value
    * in the message.
    * 
    * @return The message associated with the key.
    */
   public static String getString( String iKey, String iParam )
   {
      try
      {
         String message = RESOURCE_BUNDLE.getString( iKey );
         iParam = mkRegxReady( iParam );      
         if ( iParam.length() >= 1000 )
         {
            iParam = "Original value removed due to size";
         }
         
         message = message.replaceAll( "p1",( iParam != null ) ? iParam : "" );

         return message;
      }
      catch ( MissingResourceException e )
      {
         return '!' + iKey + '!';
      }
   }
   
   /**
    * Returns the CTS logging message associated with the given key. The method 
    * retrieves a parameterized message based on the input key and replaces the 
    * parameter with the input parameter. If the key is not found a default 
    * message based on the key will be returned.
    * 
    * @param iKey A unique identifier that identifies the parameterized message.
    * 
    * @param iParam1 The first parameter to be used to replace the 
    * parameterized value in the message.
    * 
    * @param iParam2 The second parameter to be used to replace the 
    * parameterized value in the message.
    * 
    * @return The message associated with the key.
    */
   public static String getString( String iKey, String iParam1, String iParam2 )
   {
      try
      {
         String message = RESOURCE_BUNDLE.getString( iKey );
         iParam1 = mkRegxReady( iParam1 );
         iParam2 = mkRegxReady( iParam2 );
         
         // Truncates large params for logging purposes 
         if ( iParam1.length() >= 1000 )
         {
            iParam1 = "[ Original value removed due to size ]";
         }
         message = message.replaceAll( "p1",( iParam1 != null ) 
            ? iParam1 : "" );
         
         // Truncates large params for logging purposes          
         if ( iParam2.length() >= 1000 )
         {
            iParam2 = "[ Original value removed due to size ]";
         }
         message = message.replaceAll( "p2",( iParam2 != null ) 
            ? iParam2 : "" );
         return message;
      }
      catch ( MissingResourceException e )
      {
         return '!' + iKey + '!';
      }
   }
   
   /**
    * Returns the CTS logging message associated with the given key. The method 
    * retrieves a parameterized message based on the input key and replaces the 
    * parameter with the input parameter. If the key is not found a default 
    * message based on the key will be returned.
    * 
    * @param iKey A unique identifier that identifies the parameterized message.
    * 
    * @param iParam1 The first parameter to be used to replace the 
    * parameterized value in the message.
    * 
    * @param iParam2 The second parameter to be used to replace the 
    * parameterized value in the message.
    * 
    * @param iParam3 The second parameter to be used to replace the 
    * parameterized value in the message.
    * 
    * @return The message associated with the key.
    */
   public static String getString( String iKey, String iParam1, String iParam2,
                                   String iParam3 )
   {
      try
      {
         String message = RESOURCE_BUNDLE.getString( iKey );
         iParam1 = mkRegxReady( iParam1 );
         iParam2 = mkRegxReady( iParam2 );
         iParam3 = mkRegxReady( iParam3 );
         message = message.replaceAll( "p1",( iParam1 != null ) 
            ? iParam1 : ""  );
         message = message.replaceAll( "p2",( iParam2 != null ) 
            ? iParam2 : ""  );
         message = message.replaceAll( "p3",( iParam3 != null ) 
            ? iParam3 : ""  );
         return message;
      }
      catch ( MissingResourceException e )
      {
         return '!' + iKey + '!';
      }
   }
   
   /**
    * Returns the CTS logging message associated with the given key. The method 
    * retrieves a parameterized message based on the input key and replaces the 
    * parameter with the input parameter. If the key is not found a default 
    * message based on the key will be returned.
    * 
    * @param iKey A unique identifier that identifies the parameterized message.
    * 
    * @param iParam1 The first parameter to be used to replace the 
    * parameterized value in the message.
    * 
    * @param iParam2 An int parameter to be used to replace the parameterized 
    * value in the message.
    * 
    * @return The message associated with the key.
    */
   public static String getString( String iKey, String iParam1, int iParam2 )
   {
      try
      {
         String message = RESOURCE_BUNDLE.getString( iKey );
         String replace = Integer.toString( iParam2 );
         iParam1 = mkRegxReady( iParam1 );      
         message = message.replaceAll( "p1",( iParam1 != null ) 
            ? iParam1 : "" );
         message = message.replaceAll( "p2", ( replace != null ) 
            ? replace : ""  );
         return message;
      }
      catch ( MissingResourceException e )
      {
         return '!' + iKey + '!';
      }
   }
   
   /**
    * This method is used to format a string by searching for a specific string
    * and replacing the value. 
    * @param ioString The string that will be updated 
    * @param iOld The old string that will be used as the value to replace
    * @param iNew The new string that will replace the old string in the 
    *             returned string
    * @return Returns a new string with the replaced parameter. 
    */
   private static String replace( String ioString, String iOld, String iNew )
   {
      int startPos = 0;
      int indexPos = -1;
      String tempString;

      while ( ( indexPos = ioString.indexOf(iOld, startPos )) != -1 )
      {
         tempString = ioString.substring( 0, indexPos ) + iNew
            + ioString.substring( indexPos + iOld.length() );

         ioString = tempString;
         startPos = indexPos + iNew.length();
      }

      return ioString;
   }
   
   /**
    * This method looks for a given set of characters and replaces them with
    * a different set.
    * 
    * @param iParam The string that will be used during the replace procedure
    * 
    * @return Returns the newly replaced string.
    */
   private static String mkRegxReady(String iParam)
   {
      if ( iParam != null )
      {
         if ( iParam.indexOf("\\") != -1 )
         {
            iParam = replace(iParam, "\\", "\\\\");
         }
         if ( iParam.indexOf("$") != -1 )
         {
            iParam = replace(iParam, "$", "\\$");
         }
      }
      return iParam;
   }
   
}
