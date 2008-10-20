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
package org.adl.validator;

import org.adl.parsers.dom.ADLDOMParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

/**
 *
 * <strong>Filename: </strong>DOMRulesCreator.java<br><br>
 *
 * <strong>Description: </strong>The <code>DOMRulesCreator</code> will create
 * a DOM of the XML rules that are neccessary for each Application Profile
 * (ie.,  Content Package Validator - resource and content aggregation /
 * Metadata Validator - adlreg ) /
 * Sequence Validator - sequence<br><br>
 *
 * <strong>Design Issues: </strong>none<br><br>
 *
 * <strong>Implementation Issues: </strong>none<br><br>
 *
 * <strong>Known Problems: </strong>none<br><br>
 *
 * <strong>Side Effects: </strong>none<br><br>
 *
 * <strong>References: </strong>SCORM <br><br>
 *
 * @author ADL Technical Team
 */
public class DOMRulesCreator
{
   /**
    * The application profile type to read the rules for the following:
    * <ul>
    *    <li><strong>Metadata</strong>
    *       <ul>
    *          <li>adlreg</li>
    *       </ul>
    *    </li>
    *    <li><strong>Content Package</strong>
    *       <ul>
    *          <li>resource</li>
    *          <li>content aggregation</li>
    *       </ul>
    *    </li>
    *    <li><strong>Sequence</strong>
    *       <ul>
    *          <li>sequence</li>
    *       </ul>
    *    </li>
    * </ul>
    */
   private String mApplicationProfileType;

   /**
    * The validator type that this class is providing for the following:
    * <ul>
    *    <li>metadata</li>
    *    <li>contentpackage</li>
    *    <li>sequence</li>
    * </ul>
    */
   private String mValidatorType;

   /**
    * Logger object used for debug logging.
    */
   private static Log log = LogFactory.getLog(DOMRulesCreator.class);

   /**
    *
    * Constructor that sets the application profile and validator attributes
    * values.
    *
    * @param iApplicationProfileType Application Profile Rules to be retrieved.
    * 
    * @param iValidatorType Type of validator being used.  Valid values
    * include: contentpackage, metadata, sequence  
    */
   public DOMRulesCreator( String iApplicationProfileType,
                           String iValidatorType )
   {
      log.debug( "DOMRulesCreator()" );
      log.debug("      iApplicationProfileType coming in is " +
                           iApplicationProfileType );
      log.debug("      iValidatorType coming in is " +
                           iValidatorType );

      mApplicationProfileType = iApplicationProfileType;
      mValidatorType = iValidatorType;

      log.debug( "DOMRulesCreator()" );
   }

   /**
    *
    * Performs the reading in and parsing of the xml rules.
    *
    * @return Document DOM of the parsed xml rules
    */
   public Document provideRules()
   {
      log.debug( "provideRules()" );

      // create an ADLDOMParser object to parse the rules and provide a dom
      ADLDOMParser mParser = new ADLDOMParser();
      java.net.URL urlLocation = null;
      Document doc = null;

      // now we must determine which XML rules document it's location

      if( mValidatorType.equals("metadata") &&
         mApplicationProfileType.equals("adlreg") )
      {
         urlLocation = 
            DOMRulesCreator.class.
            getResource("metadata/rules/md_adlregRules.xml");
      
          log.debug( "adlreg fileLocation is" + urlLocation );
      }      
      else if ( mValidatorType.equals("contentpackage") &&
                mApplicationProfileType.equals("resource") )
      {
         urlLocation = 
            DOMRulesCreator.class.
               getResource("contentpackage/rules/cp_resourceRules.xml");

         log.debug( "resource fileLocation is" + urlLocation );
      }
      else if ( mValidatorType.equals("contentpackage") &&
                mApplicationProfileType.equals("contentaggregation") )
      {
         urlLocation = 
            DOMRulesCreator.class.
             getResource("contentpackage/rules/cp_contentaggregationRules.xml");

         log.debug( "contentaggregation fileLocation is" + urlLocation );
      }
      else if ( mValidatorType.equals("sequence") &&
                mApplicationProfileType.equals("sequence") )
      {
         urlLocation = 
            DOMRulesCreator.class.
               getResource("sequence/rules/sequenceRules.xml");

         log.debug( "sequence fileLocation is" + urlLocation );
      }
      else
      {
         log.error( "Error, ApplicationProfile and/or ValidatorType DNE" );
      }

      if ( urlLocation != null )
      {
         // parse XML rules document to provide a dom
         mParser.parseForWellformedness( urlLocation, false, false );

         if( mParser.getIsWellformed() )
         {
            doc = mParser.getDocument();
         }
      }
      log.debug( "provideRules()" );

      return doc;
    }
}
