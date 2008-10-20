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

import java.io.Serializable;
import java.util.Vector;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * <strong>Filename: </strong>RulesValidator.java<br><br>
 *
 * <strong>Description: </strong>The <code>RulesValidator</code>
 * object contains the information required for the SCORM Validation
 * Process performed by the SCORM Validator.  This object
 * will contain the DOM of the XML rules that are neccessary for
 * meeting conformance to each of the Application Profiles.<br><br>
 *
 * <strong>Design Issues: </strong><br><br>
 *
 * <strong>Implementation Issues: </strong><br><br>
 *
 * <strong>Known Problems: </strong><br><br>
 *
 * <strong>Side Effects: </strong><br><br>
 *
 * <strong>References: </strong><br><br>
 *
 * @author ADL Technical Team
 */
public class RulesValidator implements Serializable
{
   /**
   * The DOM of the application profile rules defined in xml.<br>
   */
   private Document mRulesDocument;

   /**
    * The validator type:
    *  <ul>
    *     <li><code>contentpackage</code></li>
    *     <li><code>metadata</code></li>
    *     <li><code>sequence</code></li>
    *  </ul>
    */
   private String mValidatorType;

   /**
    * Logger object used for debug logging.<br>
    */
   private static Log log = LogFactory.getLog(RulesValidator.class);


   /**
    * Default constructor - inializes the attributes for this class.<br>
    *
    * @param iValidatorType The validator that is currently being worked.
    * <ul>
    *    <li><code>contentpackage</code></li>
    *    <li><code>metadata</code></li>
    *    <li><code>sequence</code></li>
    * </ul>
    */
   public RulesValidator( String iValidatorType )
   {
      //mLogger = Logger.getLogger("org.adl.util.debug.validator");

      log.debug(
                        "RulesValidator()" );

      mRulesDocument = null;
      mValidatorType = iValidatorType;

      log.debug(
                       "RulesValidator()" );
   }

   /**
    * Creates a RulesManager to produce a DOM of the rules for the specified
    * Metadata/Content Package Application Profile.<br>
    *
    * @param iApplicationProfileType  The application profile rules document 
    * to be read in:
    *        <ul>
    *           <li><code>adlreg</code></li>
    *        </ul> 
    * 
    * @return boolean - a flag is returned describing if the rules have been 
    * read in successfully.  True implies that there were no problems and the 
    * XML rules exist as a DOM, false implies problems occured and application
    * profile checking cannot continue.  
    * 
    */
   public boolean readInRules( String iApplicationProfileType )
   {
     log.debug( "readInRules()" );
     log.debug( "      iApplicationProfileType coming in is " +
                            iApplicationProfileType );

     boolean result = true;

     // create an DOMRules object to provide the dom of rules
     DOMRulesCreator dom = new DOMRulesCreator( iApplicationProfileType,
                                                mValidatorType );

     Document rules = dom.provideRules();

     if ( rules != null )
     {
        mRulesDocument = rules;
        log.debug( "root element mRulesDoc = " +
                     ((Node)mRulesDocument.getDocumentElement()).getNodeName());
     }
     else
     {
        log.error( "Problem parsing XML rules" );
        result = false;
     }
     log.debug( "readInRules()" );
     return result;
   }

   /**
    * Retrieves the element node's value for the specified string name
    * at the specified path location.<br>
    *
    * @param iElementName Name of the element being searched for<br>
    *
    * @param iPath  Path of the element being searched for
    * (necessary for elements with same names but different values)<br>
    *
    * @param iRuleName Name of the rule being retrieved
    *                  <ul>
    *                     <li><code>min</code></li>
    *                     <li><code>max</code></li>
    *                     <li><code>spm</code></li>
    *                     <li><code>attrib</code></li>
    *                  </ul>
    *
    * @param iAttribAttribName Name of the attribute rule being retrieved.
    * <br><br>
    *
    * @return String: Value of specified rule for the specified element with the
    * specified path.<br>
    */
   public String getRuleValue( String iElementName,
                               String iPath,
                               String iRuleName,
                               String iAttribAttribName )
   {
      log.debug( "getRuleValue()" );
      log.debug( "    iElementName coming in is " + iElementName );
      log.debug( "    iPath coming in is " + iPath );
      log.debug( "    iRuleName coming in is " + iRuleName );
      log.debug( "    iAttribAttribName coming in is " + 
                      iAttribAttribName );

      String stringResult = new String("-1");
      Node ruleNode;

      if ( mRulesDocument != null )
      {
         log.debug( "mRulesDocument != null" );

         ruleNode = retrieveAttribRuleElement( iElementName, iPath, iRuleName,
                                               iAttribAttribName );

         if (ruleNode != null )
         {
            // retrieve value and return
            log.debug( "rule found, retrieving value" );

            stringResult = getTaggedData( ruleNode );
         }
         else
         {
            log.debug( "ruleNode is null - rull was not found" );
         }
      }
      else
      {
         log.error("Can not continue validation of rules, doc is null");
      }
         log.debug( "Returning the following value " + stringResult );
         log.debug( "getRuleValue()" );

      return stringResult;
   }

   /**
    * Retrieves the element node's value for the specified string name
    * at the specified path location.<br>
    *
    * @param iElementName Name of the element being searched for<br>
    *
    * @param iPath  Path of the element being searched for
    * (necessary for elements with same names but different values)<br>
    *
    * @param iRuleName Name of the rule being retrieved<br>
    *                  <ul>
    *                     <li><code>min</code></li>
    *                     <li><code>max</code></li>
    *                     <li><code>spm</code></li>
    *                     <li><code>attrib</code></li>
    *                  </ul>
    *
    * @return Specified rule value of the specified element with the
    * specified path.<br>
    */
   public String getRuleValue( String iElementName,
                               String iPath,
                               String iRuleName )
   {
      log.debug( "getRuleValue()" );

      log.debug( "    iElementName coming in is " + iElementName );
      log.debug( "    iPath coming in is " + iPath );
      log.debug( "    iRuleName coming in is " + iRuleName );

      String stringResult = new String("-1");
      Node ruleNode;

      if ( mRulesDocument != null )
      {
         log.debug( "mRulesDocument != null" );

         ruleNode = retrieveRuleElement( iElementName, iPath, iRuleName );
         if (ruleNode != null )
         {
            // retrieve value and return
            log.debug( "rule found, retrieving value" );

            stringResult = getTaggedData( ruleNode );
         }
         else
         {
            log.debug( "ruleNode is null - rull was not found" );
         }
      }
      else
      {
         log.error("Can not continue validation of rules, doc is null");
      }
         log.debug( "Returning the following value " + stringResult );
         log.debug( "getRuleValue()" );

      return stringResult;
   }

   /**
    *
    * Retrieves the vocabulary element for the specified element at the
    * at the specified path location.<br>
    *
    * @param iElementName Name of the element being searched for <br>
    *
    * @param iPath  Path of the element being searched for
    * (necessary for elements with same names but different values)<br>
    *
    * @return A Vector of vocabulary string values for the specified rule at
    * the specified element with the specified path.<br>
    *
    */
   public Vector getVocabRuleValues( String iElementName,
                                     String iPath )
   {
      log.debug( "getVocabRuleValues()" );

      log.debug( "    iElementName coming in is " + iElementName );
      log.debug( "    iPath coming in is " + iPath );

      Vector ruleNodeVector = new Vector();
      Vector vocabVector = new Vector();

      if ( mRulesDocument != null )
      {
         log.debug( "mRulesDocument != null" );

         ruleNodeVector = retrieveVocabRuleElements( iElementName, iPath,
                                                     "vocab" );

         int numVocabularies = ruleNodeVector.size();

         if ( numVocabularies != 0 )
         {
            // loop through vocabulary nodes to retrieve value and add them
            // to the string vector
            for ( int i = 0; i < numVocabularies; i++ )
            {
               vocabVector.add(getTaggedData(
                                (Node)(ruleNodeVector.elementAt(i)) ));
            }
         }
         else
         {
            log.debug( "ruleNode is null - rules not found" );
         }
      }
      else
      {
         log.error("Can not continue validation of rules, doc is null");
      }
        log.debug( "getVocabRuleValues()" );

     return vocabVector;
   }


   /**
    * Retrieves the vocabulary element for the specified element at the
    * at the specified path location.<br>
    *
    * @param iElementName Name of the element being searched for <br>
    *
    * @param iPath  Path of the element being searched for
    * (necessary for elements with same names but different values)<br>
    *
    * @param iAttribName Name of the attribute being tested<br>
    *
    * @return Vector:  Vector containing the vocabulary string values for the
    * specified rule at the specified element with the specified path.<br>
    */
   public Vector getAttribVocabRuleValues( String iElementName,
                                           String iPath,
                                           String iAttribName )
   {
      log.debug( "getAttribVocabRuleValues()" );
      log.debug( "    iElementName coming in is " + iElementName );
      log.debug( "    iPath coming in is " + iPath );
      log.debug( "    iAttribName coming in is " + iAttribName );

      Vector ruleNodeVector;
      Vector vocabVector = new Vector();

      if ( mRulesDocument != null )
      {
         log.debug( "mRulesDocument != null" );

         ruleNodeVector = retrieveAttribVocabRuleElements( iElementName,
                                                          iPath,
                                                          "vocab",
                                                          iAttribName );
         int numVocabularies = ruleNodeVector.size();

         if ( numVocabularies != 0 )
         {
            // loop through vocabulary nodes to retrieve value and add them
            // to the string vector
            for ( int i = 0; i < numVocabularies; i++ )
            {
               vocabVector.add(getTaggedData(
                                 (Node)(ruleNodeVector.elementAt(i)) ));
            }
         }
         else
         {
            log.debug( "ruleNode is null - rules not found" );
         }
      }
      else
      {
         log.error("Can not continue validation of rules, doc is null");
      }
      log.debug( "getAttribVocabRuleValues()" );

     return vocabVector;
   }

   /**
    * Retrieves the actual attrib rule node being searched for.
    *
    * @param iElementName Name of the element we are looking for rules for
    *
    * @param iPath Path of the element we are looking for
    *
    * @param iRuleName Rule we are looking for
    *
    * @param iAttribName Name of the attrib element we are looking for
    *
    * @return Node:  attrib's rule element being searched for
    */
   private Node retrieveAttribRuleElement( String iElementName,
                                           String iPath,
                                           String iRuleName,
                                           String iAttribName )
   {
      log.debug( "retrieveAttribRuleElement()" );
      Node resultNode = null;

      Node rootRulesNode = mRulesDocument.getDocumentElement();
      NodeList kids = rootRulesNode.getChildNodes();
      int numKids = kids.getLength();

      Node currentElementNode;
      String nodeName;
      String path;

      for ( int i = 0; i < numKids; i++ )
      {
         // traverse the element nodes and find the one with the correct
         // name and path attribute value
         currentElementNode = kids.item(i);
         nodeName = getAttribute( currentElementNode, "name" );
         log.debug( "currentElementNode's name is " + nodeName );

         path = getAttribute( currentElementNode, "path" );
         log.debug( "currentElementNode's path is " + path );

         if ( nodeName.equals( iElementName ) && path.equals( iPath ) )
         {
            // traverse the child nodes and find the one with the attribute
            // elements
            NodeList currentElementNodeKids = 
               currentElementNode.getChildNodes();
            int numCurrentNodeKids = currentElementNodeKids.getLength();
            log.debug( "numCurrentNodeKids is " + numCurrentNodeKids );

            Node attributeNode;
            String attributeNodeName;
            for (int j = 0; j < numCurrentNodeKids; j++ )
            {
               // traverse the attribute nodes and find the one with
               // the name that we want
               attributeNode = currentElementNodeKids.item(j);
               attributeNodeName = attributeNode.getNodeName();
               log.debug( "attributeNodeName is " + attributeNodeName );

               if ( attributeNodeName.equals( "attrib" ) )
               {
                  String attributeName = getAttribute( attributeNode, "name" );

                  if ( attributeName.equals(iAttribName) )
                  {
                     log.debug("Attribute rule found for " + 
                        attributeName);

                     // traverse the children of the specified attribute and
                     // get the specified rule
                     NodeList attributeNodeKids = attributeNode.getChildNodes();
                     int numAttributeNodeKids = attributeNodeKids.getLength();

                     Node ruleNode;
                     String ruleNodeName;

                     for ( int k = 0; k < numAttributeNodeKids; k++ )
                     {
                        ruleNode = attributeNodeKids.item(k);
                        ruleNodeName = ruleNode.getNodeName();
                        log.debug( "Found the " + ruleNodeName +
                                        " attribute");

                        if ( ruleNodeName.equals(iRuleName) )
                        {
                           log.debug( "Found the " + iRuleName + " rule");
                           resultNode = ruleNode;
                           break;
                        }
                     }

                     break;
                  }
               }
            }

            break;
         }
      }
      log.debug( "retrieveAttribRuleElement()" );

      return resultNode;
   }


   /**
    * Retrieves a Vector of the vocabulary rules for the attribute element
    * being searched for.
    *
    * @param iElementName Name of the element we are looking for rules for
    *
    * @param iPath Path of the element we are looking for
    *
    * @param iRuleName Rule being tested
    *
    * @param iAttribName Name of the attrib element we are looking for
    *
    * @return Vector: containing vocabulary for the attrib element being
    * searched for
    */
   private Vector retrieveAttribVocabRuleElements( String iElementName,
                                                   String iPath,
                                                   String iRuleName,
                                                   String iAttribName )
   {
      log.debug( "retrieveAttribVocabRuleElements()" );

      Vector resultVector = new Vector();

      Node rootRulesNode = mRulesDocument.getDocumentElement();
      NodeList kids = rootRulesNode.getChildNodes();
      int numKids = kids.getLength();

      Node currentElementNode;
      String nodeName;
      String path;

      for ( int i = 0; i < numKids; i++ )
      {
         // traverse the element nodes and find the one with the correct
         // name and path attribute value
         currentElementNode = kids.item(i);
         nodeName = getAttribute( currentElementNode, "name" );
         log.debug( "currentElementNode's name is " + nodeName );

         path = getAttribute( currentElementNode, "path" );
         log.debug( "currentElementNode's path is " + path );

         if ( nodeName.equals( iElementName ) && path.equals( iPath ) )
         {
            // traverse the child nodes and find the one with the attribute
            // elements
            NodeList currentElementNodeKids = 
               currentElementNode.getChildNodes();
            int numCurrentNodeKids = currentElementNodeKids.getLength();
            log.debug( "numCurrentNodeKids is " + numCurrentNodeKids );

            Node attributeNode;
            String attributeNodeName;
            for (int j = 0; j < numCurrentNodeKids; j++ )
            {
               // traverse the attribute nodes and find the one with
               // the name that we want
               attributeNode = currentElementNodeKids.item(j);
               attributeNodeName = attributeNode.getNodeName();
               log.debug( "attributeNodeName is " + attributeNodeName );

               if ( attributeNodeName.equals("attrib") )
               {
                  String attributeName = getAttribute( attributeNode, "name" );

                  if ( attributeName.equals(iAttribName) )
                  {
                     log.debug( "Attribute rule found for "
                                     + attributeName );

                     // traverse the children of the specified attribute and
                     // get the specified rule
                     NodeList attributeNodeKids = attributeNode.getChildNodes();
                     int numAttributeNodeKids = attributeNodeKids.getLength();

                     Node ruleNode;
                     String ruleNodeName;

                     for ( int k = 0; k < numAttributeNodeKids; k++ )
                     {
                        ruleNode = attributeNodeKids.item(k);
                        ruleNodeName = ruleNode.getNodeName();
                        log.debug( "Found the " + ruleNodeName +
                                        " attribute" );

                        if ( ruleNodeName.equals(iRuleName) &&
                             iRuleName.equals( "vocab" ) )
                        {
                           log.debug( "Found the " + iRuleName + " rule" );
                           resultVector.add( ruleNode );
                        }
                     }

                     break;
                  }
               }
            }

            break;
         }
      }
      log.debug( "retrieveAttribVocabRuleElements()" );

      return resultVector;
   }

   /**
    * Retrieves the actual rule node being searched for.
    *
    * @param iElementName Name of the element we are looking for rules for
    *
    * @param iPath Path of the element we are looking for
    *
    * @param iRuleName Rule we are looking for
    *
    * @return Node The node containing the element being searched for
    */
   private Node retrieveRuleElement( String iElementName,
                                     String iPath,
                                     String iRuleName )
   {
      log.debug( "retrieveRuleElement()" );

      Node resultNode = null;

      Node rootRulesNode = mRulesDocument.getDocumentElement();
      NodeList kids = rootRulesNode.getChildNodes();
      int numKids = kids.getLength();

      Node currentElementNode;
      String nodeName;
      String path;

      for ( int i = 0; i < numKids; i++ )
      {
         // traverse the element nodes and find the one with the correct
         // name and path attribute value
         currentElementNode = kids.item(i);
         nodeName = getAttribute( currentElementNode, "name" );
         log.debug( "currentElementNode's name is " + nodeName );

         path = getAttribute( currentElementNode, "path" );
         log.debug( "currentElementNode's path is " + path );

         if ( nodeName.equals( iElementName ) && path.equals( iPath ) )
         {
            // traverse the child nodes and find the one with the attribute
            // elements
            NodeList currentElementNodeKids = 
               currentElementNode.getChildNodes();
            int numCurrentNodeKids = currentElementNodeKids.getLength();
            log.debug( "numCurrentNodeKids is " + numCurrentNodeKids );

            Node ruleNode;
            String ruleNodeName;
            for (int j = 0; j < numCurrentNodeKids; j++ )
            {
               // traverse the attribute nodes and find the one with
               // the name that we want
               ruleNode = currentElementNodeKids.item(j);
               ruleNodeName = ruleNode.getNodeName();
               log.debug( "ruleNodeName is " + ruleNodeName );

               if ( iRuleName.equals( "min" ) || iRuleName.equals( "max" ) ||
                    iRuleName.equals( "spm" ) || iRuleName.equals( "datatype" ))
               {
                  if ( ruleNodeName.equals( iRuleName ) )
                  {
                     resultNode = ruleNode;
                     break;
                  }
               }
               else
               {
                  log.error("The rule searched for is not available");
               }
            }
            break;
         }
      }
      log.debug( "retrieveRuleElement()" );

      return resultNode;
   }

   /**
    * Retrieves a vector of the vocabulary rule node being searched for.
    *
    * @param iElementName Name of the element we are looking for rules for
    *
    * @param iPath Path of the element we are looking for
    *
    * @param iRuleName Rule we are looking for
    *
    * @return Vector Containing the valid vocabularies for the element being
    * tested
    */
   private Vector retrieveVocabRuleElements( String iElementName,
                                             String iPath,
                                             String iRuleName )
   {
      log.debug( "retrieveVocabRuleElements()" );

      Vector resultVector = new Vector();

      Node rootRulesNode = mRulesDocument.getDocumentElement();
      NodeList kids = rootRulesNode.getChildNodes();
      int numKids = kids.getLength();

      Node currentElementNode;
      String nodeName;
      String path;

      for ( int i = 0; i < numKids; i++ )
      {
         // traverse the element nodes and find the one with the correct
         // name and path attribute value
         currentElementNode = kids.item(i);
         nodeName = getAttribute( currentElementNode, "name" );
         log.debug( "currentElementNode's name is " + nodeName );

         path = getAttribute( currentElementNode, "path" );
         log.debug( "currentElementNode's path is " + path );

         if ( nodeName.equals( iElementName ) && path.equals( iPath ) )
         {
            // traverse the child nodes and find the one with the attribute
            // elements
            NodeList currentElementNodeKids = 
               currentElementNode.getChildNodes();
            int numCurrentNodeKids = currentElementNodeKids.getLength();
            log.debug( "numCurrentNodeKids is " + numCurrentNodeKids );

            Node ruleNode;
            String ruleNodeName;
            for (int j = 0; j < numCurrentNodeKids; j++ )
            {
               // traverse the child nodes and find the one with
               // the name that we want
               ruleNode = currentElementNodeKids.item(j);
               ruleNodeName = ruleNode.getNodeName();
               log.debug( "ruleNodeName is " + ruleNodeName );

               if ( iRuleName.equals("vocab") )
               {
                  if ( ruleNodeName.equals( iRuleName ) )
                  {
                     resultVector.add( ruleNode );
                  }
               }
            }
            break;
         }
      }
      log.debug( "retrieveVocabRuleElements()" );

      return resultVector;
   }

   /**
    * Retrieves the value of the desired attribute.
    *
    * @param iNode Node which contains the attributes.
    *
    * @param iAttribute Name of the attribute desired
    *
    * @return String: Value of the desired attribute
    */
   protected String getAttribute( Node iNode, String iAttribute)
   {
      String returnValue = new String();

      // grab attributes of the node
      Attr attrs[] = sortAttributes( iNode.getAttributes() );

      // now see if the asked for attribute exists and send
      // back the value
      Attr attribute;
      for ( int i = 0; i < attrs.length; i++ )
      {
         attribute = attrs[i];

         //if ( attribute.getName().equals( theAttribute ) )
         if ( attribute.getLocalName().equals( iAttribute ) )
         {
            returnValue = attribute.getValue();
            break;
         }
      }
      return returnValue;
   }

   /**
    * Sorts the elements attributes.
    *
    * @param iAttrs list of attributes to be sorted
    *
    * @return sorted array of attributes
    */
   protected Attr[] sortAttributes( NamedNodeMap iAttrs )
   {
      int len = (iAttrs != null) ? iAttrs.getLength() : 0;
      Attr array[] = new Attr[len];
      for ( int i = 0; i < len; i++ )
      {
         array[i] = (Attr)iAttrs.item(i);
      }
      for ( int i = 0; i < len - 1; i++ )
      {
         String name  = array[i].getLocalName();
         int    index = i;
         for ( int j = i + 1; j < len; j++ )
         {
            String curName = array[j].getLocalName();
            if ( curName.compareTo(name) < 0 )
            {
               name  = curName;
               index = j;
            }
         }
         if ( index != i )
         {
            Attr temp    = array[i];
            array[i]     = array[index];
            array[index] = temp;
         }
      }

      return array;
   }

   /**
    * Retrieves the text from a text node
    *
    * @param iNode   TEXT_NODE that contains the needed text
    *
    * @return String describing the text contained in the given node
    */
   public String getTaggedData( Node iNode )
   {
      log.debug( "getTaggedData()" );

      String value= new String();
      NodeList kids = iNode.getChildNodes();

      //cycle through all children of node to get the text
      if ( kids != null )
      {
         for ( int i = 0; i < kids.getLength(); i++ )
         {
            //make sure this is a "text node"
            if ( ( kids.item(i).getNodeType() == Node.TEXT_NODE ) ||
                 ( kids.item(i).getNodeType() == Node.CDATA_SECTION_NODE ) )
            {
               value = value + kids.item(i).getNodeValue().trim();
            }
         }
      } else
      {
            log.debug( "%%% no kids for value %%%" );

      }
      log.debug( "getTaggedData()" );

      return value;
   }

   /**
    * Retrieves the application profile that the xml rules describe.<br>
    *
    * @return String: The name of the application profile being tested<br>
    *
    */
   public String getApplicationProfile()
   {
      return getAttribute( mRulesDocument.getDocumentElement(), "appprof" );
   }

   /**
    * Retrieves the type of XML subject the xml rules describe.<br>
    *
    * @return String containing the type of the test subject<br>
    *    ( "metadata" || "imsmanifest" || "sequence" )<br>
    */
   public String getType()
   {
      return getAttribute( mRulesDocument.getDocumentElement(), "type" );
   }
}
