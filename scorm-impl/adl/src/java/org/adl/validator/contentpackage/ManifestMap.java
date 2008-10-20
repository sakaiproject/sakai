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
package org.adl.validator.contentpackage;

// native java imports
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.adl.parsers.dom.DOMTreeUtility;
import org.adl.util.LogMessage;
import org.adl.util.MessageType;
import org.adl.logging.DetailedLogMessageCollection;
import org.adl.util.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * <strong>Filename: </strong><br>ManifestMap.java<br><br>
 *
 * <strong>Description: </strong><br> A <code>ManifestMap</code> is a Data
 * Structure used to store manifest information that is necessary for for the
 * validation and processing of (Sub) manifests.
 *
 * @author ADL Technical Team
 */

public class ManifestMap implements IManifestMap
{
   /**
    * Logger object used for debug logging.
    */
	private static Log log = LogFactory.getLog(ManifestMap.class);
			
   /**
    * The identifier attribute of the &lt;manifest&gt; element.
    */
   private String mManifestId;

   /**
    * The identifier attributes of all &lt;resource&gt; elements that belong to the
    * &lt;manifest&gt; element of mManifestId.
    */
   private Vector mResourceIds;

   /**
    * The identifier attributes of all &lt;item&gt; elements that belong to the
    * &lt;manifest&gt; element of mManifestId.
    */
   private Vector mItemIds;

   /**
    * The identifier reference values of all &lt;item&gt; elements that belong to the
    * &lt;manifest&gt; element of mManifestId.
    */
   private Vector mItemIdrefs;

   /**
    * The identifier reference values of all &lt;dependency&gt; elements that belong to
    * the &lt;manifest&gt; element of mManifestId.
    */
   private Vector mDependencyIdrefs;


   /**
    * The ManifestMap objects for all (Sub) manifest elements.
    */
   private Vector mManifestMaps;

   /**
    * The boolean describing if the manifest utilizes (Sub) manifest.
    */
   private boolean mDoSubmanifestExist;

   /**
    * A vector containing a list of Idrefs that reference subManifest elements.
    */
   private Vector mSubManifestIDrefs;

   /**
    * The identifier determining what type of (Sub) manifest is to be validated
    * for.  If an item identifierref points to a (Sub) manifest identifier value,
    * than the (Sub) manifest qualifies as "content aggregation" for this
    * attribute value - allowing 1 and only 1 organization element in the
    * (Sub) manifest.  If an item identfiierref value points to only a resource
    * identifier in a (Sub) manifest, the the (Sub) manifest qualifies as
    * "other" for this attribute - allowing 0 or 1 organization element in the
    * (Sub) manifest.
    */
   private String mApplicationProfile;


   /**
    * The default constructor.
    */
   public ManifestMap()
   {
      //mLogger = Logger.getLogger("org.adl.util.debug.validator"); 

      mManifestId                   = new String();
      mResourceIds                  = new Vector();
      mItemIds                      = new Vector();
      mItemIdrefs                   = new Vector();
      mManifestMaps                 = new Vector();
      mDoSubmanifestExist           = false;
      mApplicationProfile           = new String();
      mDependencyIdrefs             = new Vector();
      mSubManifestIDrefs            = new Vector();
   }


   /**
    * Gives access to the identifier value of the &lt;manifest&gt; element.
    *
    * @return - The identifier value of the &lt;manifest&gt; element.
    */
   public String getManifestId()
   {
      return mManifestId;
   }

   /**
    * Gives access to the identifier attributes of all &lt;resource&gt; elements that
    * belong to the &lt;manifest&gt; element of mManifestId.
    *
    * @return - The identifier attributes of all &lt;resource&gt; elements that
    * belong to the &lt;manifest&gt; element of mManifestId.
    */
   public Vector getResourceIds()
   {
      return mResourceIds;
   }

   /**
    * Gives access to the identifier attributes of all &lt;item&gt; elements that
    * belong to the &lt;manifest&gt; element of mManifestId.
    *
    * @return - The identifier attributes of all &lt;item&gt; elements that
    * belong to the &lt;manifest&gt; element of mManifestId.
    */
   public Vector getItemIds()
   {
      return mItemIds;
   }

   /**
    * Gives access to the identifier reference values of all &lt;item&gt; elements
    * that belong to the &lt;manifest&gt; element of mManifestId.
    *
    * @return - The identifier reference values of all &lt;item&gt; elements that
    * belong to the &lt;manifest&gt; element of mManifestId.
    */
   public Vector getItemIdrefs()
   {
      return mItemIdrefs;
   }

   /**
    * Gives access to the identifier reference values of all &lt;dependency&gt;
    * elements that belong to the &lt;manifest&gt; element of mManifestId.
    *
    * @return - The identifier reference values of all &lt;dependency&gt; elements
    * that belong to the &lt;manifest&gt; element of mManifestId.
    */
   public Vector getDependencyIdrefs()
   {
      return mDependencyIdrefs;
   }

   /**
    * Gives access to the ManifestMap objects for all (Sub) manifest elements.
    *
    * @return - The ManifestMap objects for all (Sub) manifest elements.
    */
   public Vector getManifestMaps()
   {
      return mManifestMaps;
   }

   /**
    * Gives access to the boolean describing if the manifest utilizes
    * (Sub) manifest.
    *
    * @return - The boolean describing if the manifest utilizes (Sub) manifest.
    * 
    */
   public boolean getDoSubmanifestExist()
   {
      return mDoSubmanifestExist;
   }

   /**
    * Gives access to the String describing which Application Profile the
    * (Sub) manifest adheres to.
    *
    * @return - The boolean describing if the manifest utilizes (Sub) manifest.
    */
   public String getApplicationProfile()
   {
      return mApplicationProfile;
   }

   /**
    * Gives access to the String describing which Application Profile the
    * (Sub) manifest adheres to.
    *
    * @param iApplicationProfile The indicator of the Application Profile
    */
   public void setApplicationProfile( String iApplicationProfile )
   {
      mApplicationProfile = iApplicationProfile;
   }

   /**
    * Gives access to the list of IDRefs that reference (Sub) manifest elements.
    *
    * @return - The ManifestMap objects for all (Sub) manifest elements.
    */
   public Vector getSubManifestIDrefs()
   {
      return mSubManifestIDrefs;
   }



   /**
    * This method populates the ManifestMap object by traversing down
    * the document node and storing all information necessary for the validation
    * of (Sub) manifests.  Information stored for each manifest element includes:
    * manifest identifiers,item identifers, item identifierrefs, and
    * resource identifiers
    *
    * @param iNode the node being checked. All checks will depend on the type of node
    * being evaluated
    * 
    * @return - The boolean describing if the ManifestMap object(s) has been
    * populated properly.
    */
   public boolean populateManifestMap( Node iNode )
   {
      // looks exactly like prunetree as we walk down the tree
      log.debug("populateManifestMap" );  

      boolean result = true;

      // is there anything to do?
      if ( iNode == null )
      {
         result = false;
         return result;
      }

      int type = iNode.getNodeType();

      switch ( type )
      {
         case Node.PROCESSING_INSTRUCTION_NODE:
         {
            break;
         }
         case Node.DOCUMENT_NODE:
         {
            Node rootNode = ((Document)iNode).getDocumentElement();

            result = populateManifestMap( rootNode ) && result;

            break;
         }
         case Node.ELEMENT_NODE:
         {
            String parentNodeName = iNode.getLocalName();

            if ( parentNodeName.equalsIgnoreCase("manifest") ) 
            {
               // We are dealing with an IMS <manifest> element, get the IMS
               // CP identifier for the <manifest> elememnt
               mManifestId =
                  DOMTreeUtility.getAttributeValue( iNode,
                                                    "identifier" ); 

               log.debug( "ManifestMap:populateManifestMap, " + 
                               "Just stored a Manifest Id value of " + 
                                mManifestId );

               // Recurse to populate mItemIdrefs and mItemIds

               // Find the <organization> elements

               Node orgsNode = DOMTreeUtility.getNode( iNode, "organizations" ); 

               if( orgsNode != null )
               {
                  Vector orgElems = DOMTreeUtility.getNodes( orgsNode, "organization" ); 

                  log.debug( "ManifestMap:populateManifestMap, " + 
                                  "Number of <organization> elements: " + 
                                   orgElems.size() );

                  if ( !orgElems.isEmpty() )
                  {
                     int orgElemsSize = orgElems.size();
                     for (int i = 0; i < orgElemsSize; i++ )
                     {
                        Vector itemElems = DOMTreeUtility.getNodes(
                                            (Node)orgElems.elementAt(i), "item" ); 

                        log.debug( "ManifestMap:populateManifestMap, " + 
                                        "Number of <item> elements: " + 
                                         itemElems.size() );

                        if ( !itemElems.isEmpty() )
                        {
                           int itemElemsSize = itemElems.size();
                           for (int j = 0; j < itemElemsSize; j++ )
                           {
                              result = populateManifestMap(
                                 (Node)(itemElems.elementAt(j)) ) && result;
                           }
                        }
                     }
                  }
               }

               //recurse to populate mResourceIds

               Node resourcesNode = DOMTreeUtility.getNode( iNode, "resources" ); 

               if( resourcesNode != null )
               {
                  Vector resourceElems = DOMTreeUtility.getNodes(
                                                  resourcesNode, "resource" ); 

                  log.debug( "ManifestMap:populateManifestMap, " + 
                               "Number of <resource> elements: " + 
                                resourceElems.size() );

                  int resourceElemsSize = resourceElems.size();
                  for (int k = 0; k < resourceElemsSize; k++ )
                  {
                     result = populateManifestMap(
                                 (Node)(resourceElems.elementAt(k)) ) && result;

                  }
               }

               //recurse to populate mManifestMaps

               //find the <manifest> elements (a.k.a sub-manifests)
               Vector subManifests =
                                   DOMTreeUtility.getNodes( iNode, "manifest" ); 

               log.debug( "ManifestMap:populateManifestMap, " + 
                               "Number of (Sub) manifest elements: " + 
                                subManifests.size() );

               if ( !subManifests.isEmpty() )
               {
                  mDoSubmanifestExist = true;
                  int subManifestSize = subManifests.size();
                  for (int l = 0; l < subManifestSize; l++ )
                  {
                     ManifestMap manifestMapObject = new ManifestMap();
                     result = manifestMapObject.populateManifestMap(
                                    (Node)subManifests.elementAt(l) ) && result;
                     mManifestMaps.add( manifestMapObject );
                  }

               }
            }
            else if ( parentNodeName.equalsIgnoreCase("item") ) 
            {
               //store item identifier value
               String itemId =
                        DOMTreeUtility.getAttributeValue( iNode, "identifier" );
               
               mItemIds.add( itemId );

               log.debug( "ManifestMap:populateManifestMap, " + 
                                  "Just stored an Item Id value of " + 
                                   itemId );

               //store item identifier reference value
               String itemIdref =
                     DOMTreeUtility.getAttributeValue( iNode, "identifierref" );
               
               mItemIdrefs.add( itemIdref );

               log.debug( "ManifestMap:populateManifestMap, " + 
                                  "Just stored an Item Idref value of " + 
                                   itemIdref );

               //recurse to populate all child item elements
               Vector items = DOMTreeUtility.getNodes( iNode, "item" ); 
               if ( !items.isEmpty() )
               {
                  int itemsSize = items.size();
                  for ( int z = 0; z < itemsSize; z++ )
                  {
                     result = populateManifestMap(
                        (Node)items.elementAt(z) ) && result;
                  }
               }
            }
            else if ( parentNodeName.equalsIgnoreCase("resource") ) 
            {
               //store resource identifier value
               String resourceId =
                        DOMTreeUtility.getAttributeValue( iNode, "identifier" ); 
               // convert to lower so case sensativity does not play a role
               
               mResourceIds.add( resourceId  );

               log.debug( "ManifestMap:populateManifestMap, " + 
                                  "Just stored a Resource Id value of " + 
                                   resourceId );

               // populate <dependency> element

               Vector dependencyElems = DOMTreeUtility.getNodes( iNode,
                                                                 "dependency" ); 

               int dependencyElemsSize= dependencyElems.size();

               for(int w=0; w < dependencyElemsSize; w++ )
               {
                  Node dependencyElem = (Node)dependencyElems.elementAt(w);

                  //store resource identifier value
                  String dependencyIdref =
                        DOMTreeUtility.getAttributeValue( dependencyElem,
                                                          "identifierref" ); 
                  
                  mDependencyIdrefs.add( dependencyIdref );

                  log.debug( "ManifestMap:populateManifestMap, " + 
                                     "Just stored a Dependency Idref value of " + 
                                      mDependencyIdrefs );
               }
            }

            break;
         }
         // handle entity reference nodes
         case Node.ENTITY_REFERENCE_NODE:
         {
            break;
         }

         // text
         case Node.COMMENT_NODE:
         {
            break;
         }

         case Node.CDATA_SECTION_NODE:
         {
            break;
         }

         case Node.TEXT_NODE:
         {
            break;
         }
      }

      log.debug("populateManifestMap" );  

      return result;
   }

   /**
    * This method drives the recursive validation of the referencing of
    * identifierref values.  It spans the validation of identifierrefs for
    * each identifierref value.
    *
    * @return - The Vector containing the identifierref value(s) that do not
    * reference valid identifers.
    *
    */
   public Vector checkAllIdReferences()
   {
     Vector resultVector = new Vector();
     String msgText = new String();
     String idrefValue = new String();
     boolean iItemdrefResult = false;

     if ( !mItemIdrefs.isEmpty() )
     {
        int mItemIdrefsSize = mItemIdrefs.size();
        for ( int i = 0; i < mItemIdrefsSize; i++ )
        {
           idrefValue = (String)mItemIdrefs.elementAt(i);

           if ( !idrefValue.equals("") ) 
           {
              msgText = Messages.getString("ManifestMap.40", idrefValue ); 
              log.debug( "INFO: " + msgText ); 
              DetailedLogMessageCollection.getInstance().addMessage( 
                                    new LogMessage( MessageType.INFO, msgText ) );

              iItemdrefResult = checkIdReference( idrefValue, false );

              // track all idref values whose reference was not valid

              if ( !iItemdrefResult )
              {
                 msgText = Messages.getString("ManifestMap.43", idrefValue ); 
                 log.debug( "FAILED: " + msgText ); 
                 DetailedLogMessageCollection.getInstance().addMessage( new LogMessage(
                    MessageType.FAILED, msgText ) );

                 resultVector.add( idrefValue );
              }
           }
        }
     }

     if ( !mDependencyIdrefs.isEmpty() )
     {
        int mDependencyIdrefsSize = mDependencyIdrefs.size();
        for ( int i = 0; i < mDependencyIdrefsSize; i++ )
        {
           idrefValue = (String)mDependencyIdrefs.elementAt(i);

           if ( !idrefValue.equals("") ) 
           {
              msgText = Messages.getString("ManifestMap.40", idrefValue ); 
              log.debug( "INFO: " + msgText ); 
              DetailedLogMessageCollection.getInstance().addMessage( new LogMessage( MessageType.INFO,
                                                              msgText ) );
              
              boolean iDependencydrefResult = checkIdReference( idrefValue, false );

              // track all idref values whose reference was not valid

              if ( !iDependencydrefResult )
              {
                 msgText = Messages.getString("ManifestMap.43", idrefValue ); 
                 log.debug( "FAILED: " + msgText ); 
                 DetailedLogMessageCollection.getInstance().addMessage( new LogMessage(
                    MessageType.FAILED, msgText ) );

                 resultVector.add( idrefValue );
              }
           }
        }
     }

     return resultVector;
   }

   /**
    * This method validates that the incoming identifierref value properly
    * references a valid identifier.  An error is thrown for identifierref
    * values that perform backwards or sideward referencing, or does not
    * reference an identifier value at all.
    *
    * @param iIdref the identifier reference being checked
    * 
    * @param iInSubManifest if true then we treat it differently
    * 
    * @return - The Vector containing the identifierref value(s) that do not
    * reference valid identifers.
    *
    */
   public boolean checkIdReference( String iIdref, boolean iInSubManifest )
   {
      boolean result = false;
      String msgText = new String(); 

      // loop through resourceIds and compare to incoming idref value
      if ( !mResourceIds.isEmpty() )
      {
         int mResourceIdsSize = mResourceIds.size();
         for ( int i = 0; i < mResourceIdsSize; i++ )
         {
            String resourceId = (String)mResourceIds.elementAt(i);
            msgText = "Comparing " + iIdref + " to " + resourceId;
            log.debug( msgText );

            if ( iIdref.equals( resourceId ) )
            {
               result = true;

               msgText = Messages.getString("ManifestMap.55", iIdref);
               log.debug( "PASSED: " + msgText ); 
               DetailedLogMessageCollection.getInstance().addMessage( new LogMessage(
                                                            MessageType.PASSED,
                                                            msgText ) );
               // set application profile to other only if it does not already
               // equal content aggregation.  Other triggers the need for
               // an additional check that will allow 0 or more orgs.
               String currentAppProfile = getApplicationProfile();

               if ( !currentAppProfile.equals("contentaggregation") && 
                    iInSubManifest )
               {
                  setApplicationProfile("other"); 
               }
               else
               {
                  setApplicationProfile("contentaggregation"); 
               }

               msgText = "IDRef " + iIdref + " points to a resource " +  
                          resourceId + " , app profile is " + 
                          getApplicationProfile() + " for " + getManifestId(); 
               log.debug(msgText);

               break;
            }
         }
      }
      // compare to manifestId of (Sub) manifest elements only if we didn't find
      // a match in the resources

      if ( !result ) 
      {
         msgText = iIdref + " did not match the resourceIds, " + 
                    " now checking ManifestMaps"; 
          log.debug( msgText );
        
          if ( !mManifestMaps.isEmpty() )
          {
             int mManifestMapsSize = mManifestMaps.size();
             for (int j = 0; j < mManifestMapsSize; j++ )
             {
                ManifestMap map = (ManifestMap)mManifestMaps.elementAt(j);
                String mapManifestId = map.getManifestId();
                    
                // first check to see if the idref references a manifest id
                if ( iIdref.equals( mapManifestId ) )
                {
                   result = true;
        
                   msgText = Messages.getString("ManifestMap.67", iIdref);
                   log.debug( "PASSED: " + msgText ); 
                   DetailedLogMessageCollection.getInstance().addMessage( new LogMessage(
                                                                 MessageType.PASSED,
                                                                 msgText ) );
        
                   // set application profile to be content aggregation, triggering
                   // the check that 1 and only 1 org may exist.
                   map.setApplicationProfile("contentaggregation"); 
        
                   msgText = "Idref " + iIdref + " points to a manifestId " +  
                                  mapManifestId + " app profile is " + 
                                  map.getApplicationProfile() + " for " + 
                                  map.getManifestId();
                   log.debug( msgText );
        
                   mSubManifestIDrefs.add( iIdref );
    
                   // this allows us to break out of the for loop once we have
                   // found a match
                   break;
        
                }
                // than check to see if it references a resource in the sub
                else
                {
                   if ( !result ) 
                   { 
                      //loop thru mapManifest to recuse with idref values
                       result = map.checkIdReference( iIdref, true );
    
                   }
                   else
                   {
                       break;
                   }
                }
                   
              }
          }
      }

      msgText = "Returning " + result + "from checkIdReference";
      log.debug( msgText );

      return result;
   }
}