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

package org.adl.sequencer.impl;

import org.adl.util.debug.DebugIndicator;

import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;

import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Document;

/**
 * 
 * <strong>Filename:</strong> ADLSeqParser.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * Provides the elements from an imsmanefest.xml file required to build an
 * activity tree for the course.<br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the 
 * SCORM 2004 3rd Edition Sample RTE.<br>
 * <br>
 * 
 * <strong>References:</strong><br>
 * <ul>
 *     <li>IMS SS Specification</li>
 *     <li>SCORM 2004 3rd Edition</li>
 * </ul>
 * 
 * @author ADL Technical Team
 */
public class ADLSeqParser extends DOMParser
{
   /**
    * This controls display of log messages to the java console
    */
   private static boolean _Debug = DebugIndicator.ON;

   /**
    * This controls display of log messages to the java console
    */
   private String mFileToParse = null;

   /**
    * Identifier of the course being parsed.  
    */
   private String mCourseID = null;

   /**
    * Identifier of the default organization.
    * This is the content aggregation to be sequenced.
    */
   private String mOrganizationID = null;

   /**
    * Declares if the global shared objectives used by the content structure
    * are global to the system.
    */
   private boolean mGlobalToSystem = true;

   /**
    * Contains the <code>&lt;sequencingCollection&gt;</code> XML fragment.
    */
   private Node mSequencingCollection = null;

   /**
    * The parsed XML document
    */
   private Document mDocument = null;

   /**
    * Initializes the current test case.
    * 
    * @param iFileName Name of the XML file containing the intended test script.
    */
   public ADLSeqParser(String iFileName)
   {

      if ( _Debug )
      {
         System.out.println("  :: ADLSeqParser   --> BEGIN - constructor");
         System.out.println("  ::--> " + iFileName);
      }

      mFileToParse = iFileName; 

      if ( _Debug )
      {
         System.out.println("  :: ADLSeqParser   --> END   - constructor");
      }
   }

   /**
    * Attempts to parse the associated imsmanifest.xml file and extract the
    * default <code>&lt;organization&gt;</code> element.  This element will be 
    * used to construct an activity tree for this course.
    * 
    * @return The default <code>&lt;organization&gt;</code> element of the CP, 
    * or <code>null</code> if the parse fails.
    */
   public Node findDefaultOrganization()
   {

      if ( _Debug )
      {
         System.out.println("  :: ADLSeqParser   --> BEGIN - " +
                            "findDefaultOrganization");
      }

      Node organization = null;

      // Parse the target XML document
      boolean result = parseFile();

      if ( result )
      {

         // Get the root node of the imsmanifest.xml file <manifest>
         Node root = mDocument.getDocumentElement();

         // Get and set the identifier of the course
         // This is a required attribute, so we don't have to test for null.
         mCourseID = getAttribute( root, "identifier" );

         // Get the children of the root node
         NodeList children = root.getChildNodes();

         boolean done = false;
         boolean foundFirstOrg = false;

         // Find the <organizations> node
         for ( int i = 0; i < children.getLength(); i++ )
         {
            Node curNode = children.item(i);

            // Make sure this is an "element node"
            if ( curNode.getNodeType() == Node.ELEMENT_NODE )
            {
               if ( curNode.getLocalName().equals("organizations") )
               {
                  if ( _Debug )
                  {
                     System.out.println("  ::--> Found the " +
                                        "<organizations> element");
                  }

                  // Get and set the identifier for the default organization
                  mOrganizationID = getAttribute(curNode, "default");

                  // Get the children of the <organizations> node
                  NodeList orgs = curNode.getChildNodes();

                  // Find the <oranization> nodes -- and match the default 
                  for ( int j = 0; j < orgs.getLength() && !done; j++ )
                  {
                     Node curOrg = orgs.item(j);

                     // Make sure this is an "element node"
                     if ( curOrg.getNodeType() == Node.ELEMENT_NODE )
                     {
                        if ( curOrg.getLocalName().equals("organization") )
                        {
                           if ( _Debug )
                           {
                              System.out.println("  ::--> Found an " +
                                                 "<organization> element");
                           }

                           // Compare this organization's ID to the default
                           String id = getAttribute(curOrg, "identifier");

                           if ( mOrganizationID != null )
                           {
                              if ( id.equals(mOrganizationID) )
                              {

                                 // Check the scope of the objectives   
                                 // for this organization 

                                 String temp =
                                 getAttribute(curOrg, 
                                              "objectivesGlobalToSystem");

                                 if ( temp != null )
                                 {
                                    mGlobalToSystem =
                                    (new Boolean(temp)).booleanValue();
                                 }

                                 // We found the default organization
                                 organization = curOrg;
                                 done = true;
                                 continue;
                              }
                           }
                           else
                           {
                              if ( !foundFirstOrg )
                              {
                                 mOrganizationID = id;
                                 organization = curOrg;
                                 done = true;
                                 continue;
                              }
                           }

                           if ( !foundFirstOrg )
                           {
                              foundFirstOrg = true;
                           }

                        }
                     }
                  }

                  // We are done looking at the <organizations> element
                  // Make sure we have found the default
                  if ( organization == null )
                  {
                     if ( _Debug )
                     {
                        System.out.println("  ::--> Default <organization> " +
                                           "not found, using the first one.");
                     }

                     // Use the first <organization> by default
                     organization = orgs.item(0);
                  }
               }
               else if ( curNode.getLocalName().equals("resources") )
               {
                  if ( _Debug )
                  {
                     System.out.println("  ::--> Found the " +
                                        "<resources> element");
                  }
               }
               else if ( curNode.getLocalName().equals("sequencingCollection") )
               {
                  if ( _Debug )
                  {
                     System.out.println("  ::--> Found the " +
                                        "<sequencingCollection> element");
                  }

                  mSequencingCollection = curNode;
               }
            }
         }
      }
      else
      {
         if ( _Debug )
         {
            System.out.println("  ::-->  ERROR: Parse failed");
         }
      }

      if ( _Debug )
      {
         System.out.println("  :: ADLSeqParser   --> END   - " +
                            "findDefaultOrganization");
      }

      return organization;
   }

   /**
    * Retrieves the identifier for this course.
    * 
    * @return The identifier (<code>String</code>) for this course, or
    *         <code>null</code> if the document has not been initialized.
    */
   public String getCourseID()
   {

      if ( _Debug )
      {
         System.out.println("  :: ADLSeqParser   --> BEGIN - getCourseID");
         System.out.println("  ::-->  " + mCourseID);
         System.out.println("  :: ADLSeqParser   --> END   - getCourseID");
      }

      return mCourseID;
   }

   /**
    * Retrieves the identifier for the default organization.
    * 
    * @return The identifier (<code>String</code>) for the default organization,
    *         or <code>null</code> if the document has not been initialized.
    */
   public String getOrganizationID()
   {

      if ( _Debug )
      {
         System.out.println("  :: ADLSeqParser   --> BEGIN - " +
                            "getOrganizationID");
         System.out.println("  ::-->  " + mOrganizationID);
         System.out.println("  :: ADLSeqParser   --> END   - " +
                            "getOrganizationID");
      }

      return mOrganizationID;
   }

   /**
    * Returns the scope ID for the objectives associated with this activity
    * tree.
    * 
    * @return The ID associated with the scope of this activity tree's
    *         objectives, or <code>null</code> if the objectives are global to
    *         the system.
    */
   public String getScopeID()
   {

      if ( _Debug )
      {
         System.out.println("  :: ADLSeqParser   --> BEGIN - " +
                            "getScopeID");
         System.out.println("  ::-->  " + mGlobalToSystem);
      }

      String scopeID = null;

      if ( !mGlobalToSystem )
      {
         scopeID = mCourseID + "__" + mOrganizationID;
      }

      if ( _Debug )
      {
         System.out.println("  ::-->  " + scopeID);
         System.out.println("  :: ADLSeqParser   --> END   - " +
                            "getScopeID");
      }

      return scopeID;
   }

   /**
    * Retrieves the <code>&lt;sequencingCollection&gt;</code> node, if one 
    * exists.
    * 
    * @return The XML fragment (<code>Node</code>) for the collection of
    *         reusable sequencing rules <code>null</code> if one does not exist.
    */
   public Node getSequencingCollection()
   {

      if ( _Debug )
      {
         System.out.println("  :: ADLSeqParser   --> BEGIN - " +
                            "getSequencingCollection");
         System.out.println("  ::-->  " + mSequencingCollection);
         System.out.println("  :: ADLSeqParser   --> END   - " + 
                            "getSequencingCollection");
      }

      return mSequencingCollection;
   }

   /**
    * Parses the imsmanifest XML file associated with this course. 
    * 
    * @return <code>true</code> if the XML file was successfully parsed,
    *         otherwise <code>false</code>.
    */
   private boolean parseFile()
   {

      if ( _Debug )
      {
         System.out.println("  :: ADLSeqParser   --> BEGIN - parseFile");
      }

      boolean result = false;

      InputSource instanceInputSource = openSourceFile();
      if ( instanceInputSource != null )
      {
         // Attempt to parse the XML source file
         try
         {
            if ( _Debug )
            {
               System.out.println("  ::--> Calling super.parse()");
            }

            super.parse(instanceInputSource);

            if ( _Debug )
            {
               System.out.println("  ::--> Parse complete");
            }
         }
         catch ( SAXException se )
         {
            if ( _Debug )
            {
               System.out.println("  ::--> ERROR: SAX exception");
               System.out.println(se);
            }
         }
         catch ( IOException ioe )
         {
            if ( _Debug )
            {
               System.out.println("  ::--> ERROR: IO exception");
               System.out.println(ioe);
            }
         }
         catch ( NullPointerException npe )
         {
            if ( _Debug )
            {
               System.out.println("  ::--> ERROR: Null pointer");
               System.out.println(npe);
            }
         }

         // Attempt to get the the root of XML document
         try
         {
            mDocument = getDocument();

            // If the document has no children, we are unsuccessful
            if ( mDocument.hasChildNodes() )
            {
               result = true;
            }
            else
            {
               if ( _Debug )
               {
                  System.out.println("  ::--> The document has no children.");
               }

            }
         }
         catch ( NullPointerException npe )
         {
            if ( _Debug )
            {
               System.out.println("  ::--> ERROR: Null pointer -- No Doc");
               System.out.println(npe);
            }
         }
      }

      if ( _Debug )
      {
         System.out.println("  ::-->  " + result);
         System.out.println("  :: ADLSeqParser   --> END   - parseFile");
      }

      return result;
   }

   /**
    * Attempts to open the XML source file associated with this test case
    * 
    * @return An <code>InputSource</code> object if the source file was
    *         successfully opened, otherwise <code>null</code>.
    */
   private InputSource openSourceFile()
   {

      if ( _Debug )
      {
         System.out.println("  :: ADLSeqParser   --> BEGIN - openSourceFile");
      }

      InputSource input = null;

      if ( mFileToParse != null )
      {

         try
         {
            File xmlFile = new File(mFileToParse);

            if ( xmlFile.isFile() )
            {
               String tmp = xmlFile.getAbsolutePath();

               if ( _Debug )
               {
                  System.out.println("  ::--> Found XML File: " + tmp);
               }

               // Create the input source
               FileReader fr = new FileReader( xmlFile );
               input = new InputSource(fr);
            }
         }
         catch ( NullPointerException npe )
         {
            if ( _Debug )
            {
               System.out.println("  ::--> ERROR: Null pointer");
               System.out.println(npe);
            }
         }
         catch ( SecurityException se )
         {
            if ( _Debug )
            {
               System.out.println("  ::--> ERROR: Security exception");
               System.out.println(se);
            }
         }
         catch ( FileNotFoundException fnfe )
         {
            if ( _Debug )
            {
               System.out.println("  ::--> ERROR: File not found");
               System.out.println(fnfe);
            }
         }
      }
      else
      {
         if ( _Debug )
         {
            System.out.println("  ::--> ERROR: No file to parse");
         }
      }

      if ( _Debug )
      {
         System.out.println("  :: ADLSeqParser   --> END   - openSourceFile");
      }

      return input;
   }


   /**
    * Attempts to find the indicated attribute of the target element.
    * 
    * @param iNode      The DOM node of the target element.
    * 
    * @param iAttribute The requested attribute.
    * 
    * @return The value of the requested attribute on the target element, or
    *         <code>null</code> if the attribute does not exist.
    */
   private String getAttribute(Node iNode, String iAttribute)
   {

      if ( _Debug )
      {
         System.out.println("  :: ADLSeqParser   --> BEGIN - getAttribute");
         System.out.println("  ::-->  " + iAttribute);
      }

      String value = null;

      // Extract the node's attribute list and check if the requested
      // attribute is contained in it.
      NamedNodeMap attrs = iNode.getAttributes();

      if ( attrs != null )
      {

         Attr currentAttrNode;
         String currentNodeName;

         // loop through the attributes and get their values assuming
         // that the multiplicity of each attribute is 1 and only 1.
         for ( int k = 0; k < attrs.getLength(); k++ )
         {
            currentAttrNode = (Attr)attrs.item(k);
            currentNodeName = currentAttrNode.getLocalName();

            // store the value of the attribute
            if ( currentNodeName.equalsIgnoreCase(iAttribute) )
            {
               value = currentAttrNode.getNodeValue();
            }
         }

         if (value == null)
         {
            if ( _Debug )
            {
               System.out.println("  ::-->  The attribute \"" +
                                  iAttribute + "\" does not exist.");
            }
         }
      }
      else
      {
         if ( _Debug )
         {
            System.out.println("  ::-->  This node has no attributes.");
         }
      }

      if ( _Debug )
      {
         System.out.println("  ::-->  " + value);
         System.out.println("  :: ADLSeqParser   --> END - getAttribute");
      }

      return value;
   }


} // End ADLSeqParser