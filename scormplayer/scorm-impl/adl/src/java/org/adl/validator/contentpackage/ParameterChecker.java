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
package org.adl.validator.contentpackage;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.adl.logging.DetailedLogMessageCollection;
import org.adl.util.LogMessage;
import org.adl.util.MessageType;
import org.adl.util.Messages;

/**
 * <strong>Filename: </strong>ParameterChecker.java<br>
 *
 * <strong>Description: </strong>The <code>ParameterChecker</code> checks to 
 * make sure the parameter attribute's value (of the &lt;item&gt; element) correctly 
 * adheres to the requirements defined by the IMS Content Packaging 
 * Specification V1.1.4 and SCORM 2004.  
 *
 * @author ADL Technical Team
 */
public class ParameterChecker
{

   /**
    * Regular expression representing all unreserved values
    */
   private String mUnreservedValues = "[\u0030-\u0039\u0061-\u007A\u0041-\u005A]"; 

   /**
    * Regular expression represending the following characters:
    *                       "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"
    */
   private String mMark = "[-_.!*'()]";

   /**
    * Regular expression representing how to encode reserved characters
    */
   private String mEscaped = "[%[[0-9a-fA-F](2)]]";

   /**
    * Regular expression to be compared to for  valid URIs
    */
   private String mPattern = "[" + mUnreservedValues + "|" + mMark + "|" + mEscaped +  "]*";

   /**
    * Logger object used for debug logging. 
    */
   private Logger mLogger;

   /**
    * Default Constructor 
    */
   public ParameterChecker()
   {
      mLogger = Logger.getLogger("org.adl.util.debug.validator"); 
   }

    /**
     * This method checks to make sure the parameter attribute's value (of the 
     * &lt;item&gt; element) correctly adheres to the requirements defined by the 
     * IMS Content Packaging Specification V1.1.4 and SCORM 2004.
     * 
     * The required syntax of the value shall be:
     * <ul> 
     *   <li>#&lt;parameter&gt;</li>
     *   <li>&lt;name&gt;=&lt;value&gt;(&&lt;name&gt;=&lt;value&gt;)*(#&lt;parameter&gt;)</li>
     *   <li>?&lt;name&gt;=&lt;value&gt;(&&lt;name&gt;=&lt;value&gt;)*(#&lt;parameter&gt;)</li>
     * </ul>
     * 
     * From RFC 2396:
     * 3.4. Query Component 
     * The query component is a string of information to be interpreted by the 
     * resource. 
     * 
     *       query         = \*uric 
     * 
     * Within a query component, the characters ";", "/", "?", ":", "@", "&", 
     * "=", "+", ",", and "$" are reserved.
     * 
     *      uric          = reserved | unreserved | escaped
     * 
     * reserved    = ";" | "/" | "?" | ":" | "@" | "&" | "=" | "+" | "$" | ","
     *    (reserved values must be escaped)
     * 
     * unreserved  = alphanum | mark 
     * 
     * alphanum = alpha | digit
     * alpha    = lowalpha | upalpha 
     * lowalpha = "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" | "j" | 
     *            "k" | "l" | "m" | "n" | "o" | "p" | "q" | "r" | "s" | "t" | 
     *            "u" | "v" | "w" | "x" | "y" | "z" 
     * upalpha  = "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" | "J" | 
     *            "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" | "S" | "T" | 
     *            "U" | "V" | "W" | "X" | "Y" | "Z" 
     * digit    = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
     * 
     * mark        = "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"
     * 
     * escaped     = "%" hex hex 
     * hex         = digit | "A" | "B" | "C" | "D" | "E" | "F" | "a" | "b" | 
     *               "c" | "d" | "e" | "f"
     * 
     * 
     *  @param iParameterString The parameter attribute's value 
     *  @return Returns <code>true</code> if the parameter attribute's value 
     *          is valid to the syntax described above, <code>false</code> 
     *          otherwise 
     */              
    public boolean checkParameters(String iParameterString)
    {  
       mLogger.info( "checkParameters(String): " + iParameterString );
       boolean result = true;

       if ( iParameterString.startsWith("#") )
       {
          mLogger.info("Parameter value starts with a #");
          result = checkPoundSyntax(iParameterString.substring(1));
       }
       else if ( iParameterString.startsWith("?") )
       {
          mLogger.info("Parameter value starts with a ?");
          result = checkQuestionMarkSyntax(iParameterString.substring(1));
       }
       else
       {

          mLogger.info("does not start with a special character");

          // CHECK TO SEE if the string has a crosshatch in it.  If so, validate
          // that is is of the proper syntax

          int crossHatchIndex = -1;
          crossHatchIndex = iParameterString.lastIndexOf("#");

          if ( crossHatchIndex != -1) 
          {
             String crossHatchString = iParameterString.substring( crossHatchIndex );
              // we have found a crosshatch, remove this from original string
              iParameterString = iParameterString.substring(0,crossHatchIndex);
              if ( !crossHatchString.equals("") ) 
              {
                 result = validateCrossHatch( crossHatchString ) && result;
              }
          }

          StringTokenizer tok = new StringTokenizer(iParameterString,"&");
          mLogger.info( "number of tokens = " + tok.countTokens() );

          if (tok.countTokens() == 1)
          {
             String tempToken = tok.nextToken();
             mLogger.info( "count tokens = 0" );

             result = performEqualSplit(tempToken);

          }
          else
          {
             while (tok.hasMoreTokens())
             {
                String temp = tok.nextToken();
                mLogger.info( "CALLING PERFORM SPLIT ON " + temp );

                if ( temp.indexOf('=') != -1 )
                {
                   result = performEqualSplit( temp ) && result;
                                   }
                else
                {
                    // must have ?<name>=<value>
                    result = false;
                    break;
                }
             }
          }
       }

       mLogger.info( "Leaving ParamChecker::checkParameterString(), returning: " 
                          + result );
       // print results to log
  
       if ( result) 
       {
           //  URI passes as valid syntax
          String msgText = Messages.getString("ParameterChecker.100", iParameterString); 
          mLogger.info( "PASSED: " + msgText ); 
              
          DetailedLogMessageCollection.getInstance().addMessage( 
                                  new LogMessage( MessageType.PASSED, msgText));
       }
       else
       { 
          // URI does not pass for valid syntax
          String msgText = Messages.getString("ParameterChecker.101", iParameterString ); 
          mLogger.info( "FAILED: " + msgText ); 
              
          DetailedLogMessageCollection.getInstance().addMessage( 
                                  new LogMessage( MessageType.FAILED, msgText));
       }
       return result;
    }   

    /**
     * This method validates a parameter string in the form of #&lt;parameter&gt;
     * 
     * @param iParameterString the URI being validated
     * 
     * @return boolean result describing if the URI was of valid syntax (true),
     * false implies otherwise 
     */
    private boolean checkPoundSyntax(String iParameterString)
    {
       mLogger.info( "|---checkPoundSyntax(): " + iParameterString );
       return iParameterString.matches(mPattern);
    }

    /**
     * This method validates a parameter string in the form of
     * ?&lt;name&gt;=&lt;value&gt;
     * 
     * @param iParameterString the URI being validated
     * 
     * @return boolean result describing if the URI was of valid syntax (true),
     * false implies otherwise
     */
    private boolean checkQuestionMarkSyntax(String iParameterString)
    {
       mLogger.info( "|---checkQuestionMarkSyntax(): " + iParameterString );
       boolean result = true;
       
       // CHECK TO SEE if the string has a crosshatch in it.  If so, validate
       // that is is of the proper syntax

       int crossHatchIndex = -1;
       crossHatchIndex = iParameterString.lastIndexOf("#");

       if ( crossHatchIndex != -1) 
       {
          String crossHatchString = iParameterString.substring(crossHatchIndex);
           // we have found a crosshatch, remove this from original string
           iParameterString = iParameterString.substring(0,crossHatchIndex);
           if ( !crossHatchString.equals("") ) 
           {
              result = validateCrossHatch( crossHatchString ) && result;
           }
       }

       // Check to see if there is multiple parameters separated by an '&'
       // symbol
       StringTokenizer tok = new StringTokenizer(iParameterString,"&");
       mLogger.info( "number of tokens = " + tok.countTokens() );

       if (tok.countTokens() == 1)
       {
          // There is only 1 set of tokens after the '&' token
          mLogger.info( "count tokens = 0" );
          if ( iParameterString.indexOf('=') == -1 )
          {
              // must have ?<name>=<value>
              result = false;
             //result = iParameterString.matches(mPattern);
          }
          else
          {
             result = performEqualSplit( iParameterString ) && result;
          }
       }
       else
       {
          mLogger.info( "more then one sets of tokens" );

          while (tok.hasMoreTokens())
          {
             // Get the first token
             String temp = tok.nextToken();

             // Split the token on the '=' sign, if there is one
             if ( temp.indexOf('=') != -1 )
             {
                 result = performEqualSplit(temp) && result;
             }
             else
             {
                // malformed query component - no '=' character found
                result = false;
                break;
             }
          }
       }

       mLogger.info( "|--Leaving checkQuestionMarkSyntax(): " + result );

       return result;
    }

    /**
     *  This method performs a string split on the "=" 
     * 
     * @param iToken the string value to be split
     * 
     * @return result describing if the URI was of valid syntax (true),
     * false implies otherwise
     */
    private boolean performEqualSplit( String iToken ) 
    {
       boolean result = true;

       String[] tempHolder = iToken.split("=");
       if ( tempHolder.length > 1 ) 
       {
          mLogger.info( "String[0]: " + tempHolder[0] );
          mLogger.info( "String[1]: " + tempHolder[1] );

          if ( tempHolder[0].equals("")) 
          {
             result = false;
          }
          else
          {
              result = (tempHolder[0]).matches(mPattern) && result;
          }
          if ( tempHolder[1].equals("")) 
          {
             result = false;
          }
          else
          {
             result = (tempHolder[1]).matches(mPattern) && result;       
          }
       }
       else
       {
          result = false;
       }

       return result;
    }

    /**
     * This method validates that if the ?&lt;name&gt;=&lt;value&gt; and 
     * &lt;name&gt;=&lt;value&gt; ends with #&lt;parameter&gt; that the sytax is
     * correct.
     * 
     * @param iCrossHatchString the value being checked for proper syntax
     * 
     * @return result describing if the proper syntax is used or not
     */
    private boolean validateCrossHatch( String iCrossHatchString )
    {
       boolean result = true;

       if ( iCrossHatchString.startsWith("#") )
       {
          mLogger.info( "crossHatchSub value starts with a #" );
          result = checkPoundSyntax( iCrossHatchString.substring(1));
       }
       else
       {
           // must start with #<parameter>
           result = false;
       }
       return result;
    }
}