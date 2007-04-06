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

/**
 *
 * <strong>Filename:</strong><br> LogMessage.java<br><br>
 *
 * <strong>Description:</strong><br>
 * A <code>LogMessage</code> stores messages classified by the 
 * <code>MessageType</code> class.
 * 
 * @author ADL Technical Team
 */
public class LogMessage extends Object
{
   /**
    * This attribute holds the type of message classified by the
    * <code>MessageType</code> class. 
    *  6 = SSP <br>
    */
   private int mMessageType;

   /**
    * This attribute holds the actual text to be communicated by the message.
    */
   private String mMessageText = "";

   /**
    * This holds the name of the test file used.  It is used for links in the
    * Detailed log.  For example "Click here to view detailed MD test log" will
    * now read "Click here to view detailed MD test log for RA_1" or whatever 
    * the IdRef is for that particular
    */
   private String mTestID = "";

   /**
    * Default Constructor.  Initializes the attributes of this class.
    */
   private LogMessage()
   {
      mMessageType     = MessageType.INFO;
   }

   /**
    * This constructor initializes the message type and message text attributes
    * to the specified values.
    *
    * @param iMessageType - The type of message this is. Typically, this
    * should be "INFORMATION", "WARNING", or "ERROR", but this is up to the
    * client.
    *
    * @param iMessageText - The actual error message text.
    *
    */
   public LogMessage(int iMessageType, String iMessageText)
   {
      mMessageType = iMessageType;
      mMessageText = iMessageText;

      
   }

   /**
    * Creates a message object with a type, message and test ID
    * 
    * @param iMsgType - The type of message
    * @param iMsgTxt - The text of the message
    * @param iTestID - An identifier used to specifiy what is being tested
    */
   public LogMessage(int iMsgType, String iMsgTxt, String iTestID)
   {
      mTestID = iTestID; 
      mMessageType = iMsgType;
      mMessageText = iMsgTxt;

   }

     /**
    * This accessor returns the message type.
    *
    * @return int - The message type classified by the <code>MessageType</code>
    * class.
    */
   public int getMessageType()
   {
      return mMessageType;
   }

   /**
    * This accessor returns the message text.
    *
    * @return String - The message text.
    */
   public String getMessageText()
   {
      return mMessageText;
   }

   /**
    * Returns test id
    * 
    * @return the test id of this message
    */
   public String getTestID()
   {
       return mTestID;
   }

   /**
    * This method returns a representation of this message in a
    * predefined string form.
    *
    * Overloads the toString() method of the java.lang.Object class
    *
    * @return String - The message.
    */
   public String toString()
   {
      String  result = new String("");

      if ( mMessageType == MessageType.INFO )
      {
         result = "INFO";
      }
      else if ( mMessageType == MessageType.WARNING )
      {
         result = "WARNING";
      }
      else if ( mMessageType == MessageType.PASSED )
      {
         result = "PASSED";
      }
      else if ( mMessageType == MessageType.FAILED )
      {
         result = "FAILED";
      }
      else if ( mMessageType == MessageType.SSP )
      {
         result = "SSP";
      }
      else
      {
         result = "OTHER";
      }

      result = result + " : " + mMessageText;

      return result;
   }
}
