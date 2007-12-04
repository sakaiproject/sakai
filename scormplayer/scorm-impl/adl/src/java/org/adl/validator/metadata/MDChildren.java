 /******************************************************************************
**
** Advanced Distributed Learning Co-Laboratory (ADL Co-Lab) grants you
** ("Licensee") a non-exclusive, royalty free, license to use, modify and
** redistribute this software in source and binary code form, provided that
** i) this copyright notice and license appear on all copies of the software;
** and ii) Licensee does not utilize the software in a manner which is
** disparaging to ADL Co-Lab.
**
** This software is provided "AS IS," without a warranty of any kind.  ALL
** EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
** ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
** OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED.  ADL Co-Lab AND ITS LICENSORS
** SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
** USING, MODIFYING OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES.  IN NO
** EVENT WILL ADL Co-Lab OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE,
** PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
** INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE
** THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE
** SOFTWARE, EVEN IF ADL Co-Lab HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH
** DAMAGES.
**
******************************************************************************/
package org.adl.validator.metadata;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import org.adl.util.Messages;
import org.adl.util.LogMessage;
import org.adl.util.MessageType;
import org.adl.logging.DetailedLogMessageCollection;



/**
 *
 * <strong>Description: </strong>The <code>MDDChildren</code> object determines
 *                    whether the test subject metadata elements have children
 *                    as allowed by the SCORM.  If children exist the must come
 *                    from a restricted list as defined in the Content 
 *                    Aggregation Model of the SCORM.<br><br>
 *
 *
 * @author ADL Technical Team<br><br>
 */                                  
public class MDChildren
{
   /**
   * Logger object used for debug logging<br>
   */
  private Logger mLogger;

   /**
    * This list will hold the valid String names of the children that the parent 
    * element could possibly have.
    */ 
   private List mChildren;

   /**
    * This will be the list of children the parent node currently has.
    */
   private NodeList mKids;

   /**
    * Boolean value returned by the checkChildren() method
    */
   private boolean mAllKidsValid;

   /**
    * This will hold the value of the parent Node passed in
    */
   private Node mParent;

   /**
   * Default Constructor.  Sets the attributes to their initial values.<br>
   */
   public MDChildren( )
   {
      mLogger = Logger.getLogger( "org.adl.util.debug.validator" );
      mChildren = new ArrayList( );
      mAllKidsValid = false;

   }// end constructor()


   /**
   * This method is called by the user to initiate the validation process.
   * Depending on the name of the element a different method will be invoked to
   * make sure that any child elments have the correct name(s).<br>
   *
   * @param parent name of the Node whos children are being checked<br>
   * 
   * @return boolean: describes if the checks passed or failed. A true value
   * implies that the children are legitimate (i.e. allowed children), false 
   * otherwise.<br>
   */
   public boolean checkChildren( Node parent )
   {
      mParent = parent;
      mKids = parent.getChildNodes( );

      if( parent.getLocalName().equals("lom") )
      {
         checkLom( );
      }
      else if ( parent.getLocalName().equals("general") )
      {
         checkGeneral( );
      }
      else if (parent.getLocalName().equals("identifier") )
      {
         checkIdentifier( );
      }
      else if ( parent.getLocalName().equals("lifeCycle") )
      {
         checkLifeCycle( );
      }
      else if (parent.getLocalName().equals("contribute") )
      {
         checkContribute( ); 
      }
      else if (parent.getLocalName().equals("date") )
      {
         checkDate( );
      }
      else if (parent.getLocalName().equals("metaMetadata") )
      {
         checkMetaMetadata( );
      }
      else if (parent.getLocalName().equals("technical") )
      {
         checkTechnical( );
      }
      else if (parent.getLocalName().equals("requirement") )
      {
         checkRequirement( );
      }
      else if (parent.getLocalName().equals("orComposite") )
      {
         checkOrComposite( );
      }
      else if (parent.getLocalName().equals("duration") )
      {
         checkDuration( );
      }
      else if (parent.getLocalName().equals("educational") )
      {
         checkEducational( );
      }
      else if (parent.getLocalName().equals("typicalLearningTime") )
      {
         checkTypicalLearningTime( );
      }
      else if (parent.getLocalName().equals("rights") )
      {
         checkRights( );
      }
      else if (parent.getLocalName().equals("relation") )
      {
         checkRelation( ) ;
      }
      else if (parent.getLocalName().equals("resource") )
      {
         checkResource( );
      }
      else if (parent.getLocalName().equals("annotation") )
      {
         checkAnnotation( );
      }
      else if (parent.getLocalName().equals("classification") )
      {
         checkClassification( );
      }
      else if (parent.getLocalName().equals("taxonPath") )
      {
         checkTaxonPath( );
      }
      else if (parent.getLocalName().equals("taxon") )
      {
         checkTaxon( );
      }
      else if (parent.getLocalName().equals("title") ||
               parent.getLocalName().equals("description") ||
               parent.getLocalName().equals("keyword") ||
               parent.getLocalName().equals("coverage") ||
               parent.getLocalName().equals("version") ||
               parent.getLocalName().equals("installationRemarks") ||
               parent.getLocalName().equals("otherPlatformRequirements") ||
               parent.getLocalName().equals("typicalAgeRange") ||

               ( parent.getLocalName().equals("entry") && 
                 parent.getParentNode().getLocalName().equals("taxon")) ||

               ( parent.getLocalName().equals("source") &&
                 parent.getParentNode().getLocalName().equals("taxonPath") )
              )
      {
         mChildren.add("string");                                     

         checkKids( );
      }
      else if (parent.getLocalName().equals("structure") ||
               parent.getLocalName().equals("aggregationLevel") ||
               parent.getLocalName().equals("status") ||
               parent.getLocalName().equals("role") ||
               parent.getLocalName().equals("type") ||
               parent.getLocalName().equals("name") ||
               parent.getLocalName().equals("interactivityType") ||
               parent.getLocalName().equals("learningResourceType") ||
               parent.getLocalName().equals("interactivityLevel") ||
               parent.getLocalName().equals("semanticDensity") ||
               parent.getLocalName().equals("intendedEndUserRole") ||
               parent.getLocalName().equals("context") ||
               parent.getLocalName().equals("difficulty") ||
               parent.getLocalName().equals("cost") ||
               parent.getLocalName().equals("copyrightAndOtherRestrictions") ||
               parent.getLocalName().equals("kind") ||
               parent.getLocalName().equals("purpose")
               ) 
      {
         mChildren.add("source");
         mChildren.add("value");

         checkKids( );
      }

      //  These are all leaf elements, they should NOT have children
      else if (parent.getLocalName().equals("catalog") ||
               parent.getLocalName().equals("entry") ||
               parent.getLocalName().equals("string") ||
               parent.getLocalName().equals("language") ||
               parent.getLocalName().equals("entity") ||
               parent.getLocalName().equals("dateTime") ||
               parent.getLocalName().equals("metadataSchema") ||
               parent.getLocalName().equals("format") ||
               parent.getLocalName().equals("size") ||
               parent.getLocalName().equals("location") ||
               parent.getLocalName().equals("minimumVersion") ||
               parent.getLocalName().equals("maximumVersion") ||
               parent.getLocalName().equals("id") ||
               parent.getLocalName().equals("source") ||
               parent.getLocalName().equals("value") ||
               parent.getLocalName().equals("entry")
               )
      {
         checkLeafForKids( );
      }
      
      // If the parent does not meet any of these if/else conditions then false
      //   will be returned
      return mAllKidsValid; 
   }// end checkChildren

   /**
    * This method populates the list with names of valid children for this node
    */
   private void checkLom ( )
   {
      mChildren.add("general");
      mChildren.add("lifeCycle");
      mChildren.add("metaMetadata");
      mChildren.add("technical");
      mChildren.add("educational");
      mChildren.add("rights");
      mChildren.add("relation");
      mChildren.add("annotation");
      mChildren.add("classification");

      checkKids( ); 
   }// end checkLom()

   /**
    * This method populates the list with names of valid children for this node
    */
   private void checkGeneral( )
   {
      mChildren.add("identifier");
      mChildren.add("title");
      mChildren.add("language");
      mChildren.add("description");
      mChildren.add("keyword");
      mChildren.add("coverage");
      mChildren.add("structure");
      mChildren.add("aggregationLevel");

      checkKids( );
   }

   /**
    * This method populates the list with names of valid children for this node
    */
   private void checkIdentifier( )
   {
      mChildren.add("catalog");
      mChildren.add("entry");

      checkKids( );
   }

   /**
    * This method populates the list with names of valid children for this node
    */
   private void checkLifeCycle( )
   {
      mChildren.add("version");
      mChildren.add("status");
      mChildren.add("contribute");

      checkKids( );
   }

   /**
    * This method populates the list with names of valid children for this node
    */
   private void checkContribute( )
   {
      mChildren.add("role");
      mChildren.add("entity");
      mChildren.add("date");

      checkKids( );
   }

   /**
    * This method populates the list with names of valid children for this node
    */
   private void checkDate( )
   {
      mChildren.add("dateTime");
      mChildren.add("description");

      checkKids( );
   }

   /**
    * This method populates the list with names of valid mChildren for this node
    */   
   private void checkMetaMetadata( )
   {
      mChildren.add("identifier");
      mChildren.add("contribute");
      mChildren.add("metadataSchema");
      mChildren.add("language");

      checkKids( );
   }

   /**
    * This method populates the list with names of valid children for this node
    */
   private void checkTechnical( )
   {
      mChildren.add("format");
      mChildren.add("size");
      mChildren.add("location");
      mChildren.add("requirement");
      mChildren.add("installationRemarks");
      mChildren.add("otherPlatformRequirements");
      mChildren.add("duration");

      checkKids( );
   }

   /**
    * This method populates the list with names of valid children for this node
    */
   private void checkRequirement( )
   {
      mChildren.add("orComposite");

      checkKids( );
   }

   /**
    * This method populates the list with names of valid children for this node
    */
   private void checkOrComposite( )
   {
      mChildren.add("type");
      mChildren.add("name");
      mChildren.add("minimumVersion");
      mChildren.add("maximumVersion");

      checkKids( );
   }

   /**
    * This method populates the list with names of valid children for this node
    */
   private void checkDuration( )
   {
      if ( mParent.getParentNode().getLocalName().equals("technical") )
      {
         mChildren.add("duration");
         mChildren.add("description");

         checkKids( );
      }
      else
      {
         checkLeafForKids( );
      }
   }
   
   /**
    * This method populates the list with names of valid children for this node
    */   
   private void checkEducational( )
   {
      mChildren.add("interactivityType");
      mChildren.add("learningResourceType");
      mChildren.add("interactivityLevel");
      mChildren.add("semanticDensity");
      mChildren.add("intendedEndUserRole");
      mChildren.add("context");
      mChildren.add("typicalAgeRange");
      mChildren.add("difficulty");
      mChildren.add("typicalLearningTime");
      mChildren.add("description");
      mChildren.add("language");

      checkKids( );   
   }

   /**
    * This method populates the list with names of valid children for this node
    */
   private void checkTypicalLearningTime( )
   {
      mChildren.add("duration");
      mChildren.add("description");

      checkKids( );
   }

   /**
    * This method populates the list with names of valid children for this node
    */
   private void checkRights( )
   {
      mChildren.add("cost");
      mChildren.add("copyrightAndOtherRestrictions");
      mChildren.add("description");

      checkKids( );
   }

   /**
    * This method populates the list with names of valid children for this node
    */
   private void checkRelation( )
   {
      mChildren.add("kind");
      mChildren.add("resource");

      checkKids( );
   }

   /**
    * This method populates the list with names of valid children for this node
    */
   private void checkResource( )
   {
      mChildren.add("identifier");
      mChildren.add("description");

      checkKids( );
   }

   /**
    * This method populates the list with names of valid children for this node
    */
   private void checkAnnotation( )
   {
      mChildren.add("entity");
      mChildren.add("date");
      mChildren.add("description");

      checkKids( );
   }

   /**
    * This method populates the list with names of valid children for this node
    */
   private void checkClassification( )
   {
      mChildren.add("purpose");
      mChildren.add("taxonPath");
      mChildren.add("description");
      mChildren.add("keyword");

      checkKids( );
   }

   /**
    * This method populates the list with names of valid children for this node
    */
   private void checkTaxonPath( )
   {
      mChildren.add("source");
      mChildren.add("taxon");

      checkKids( );
   }

   /**
    * This method populates the list with names of valid children for this node
    */
   private void checkTaxon( )
   {
      mChildren.add("id");
      mChildren.add("entry");

      checkKids( );
   }

   /**
   * This method checks the children of the node against a list of valid
   * mChildren. If a child is not valid then we check to see if they are 
   * validating against the IEEE LOM, if so then they fail. If they are using
   * extensions then we just move on.
   */
   private void checkKids( )
   {  
      String msgText = "";

      // Cycle through the children of the node passed in and check them against
      //   the list of allowed children for this node
      for ( int i = 0; i < mKids.getLength(); i++ )
      {
         // Iteration through the list of actual children and compare them
         //   against a list of valid children
         if ( mKids.item( i ).getLocalName( ) != null )
         {
            // If it isnt in the list its either an invalid child or an
            //   extension element
            if ( !mChildren.contains( mKids.item( i ).getLocalName( ) ) )
            {
               // If it isnt in the list see if its an extension
               if ( mKids.item( i ).getNamespaceURI().equals(
                                               "http://ltsc.ieee.org/xsd/LOM") )
               {
                  msgText = Messages.getString("MDValidator.323",
                                                mKids.item( i ).getLocalName( ),
                                                mParent.getLocalName() );
                  mLogger.info( "FAILED:" + msgText );
                  DetailedLogMessageCollection.getInstance().addMessage( 
                                   new LogMessage( MessageType.FAILED, msgText ) );

                  mAllKidsValid = false;
                  return;
               }
            }
         }
         else
         {
            msgText=Messages.getString( "MDValidator.323",
                                        mParent.getLocalName() );
            mLogger.info( "FAILED:" + msgText );
            DetailedLogMessageCollection.getInstance().addMessage( new LogMessage ( 
                                                MessageType.FAILED, msgText ) );
            mAllKidsValid = false;
         }
      }

      mAllKidsValid = true;

   }// end checkKids()


   /**
   * This method checks to make sure that none of the leaf elements have
   * children associated with them.  If they do then they fail
   */
   private void checkLeafForKids( )
   {  
      String msgText = "";

      // If this element is empty (no text, no nothing) it will return null
      //   if it doesnt return null it might have kids
      if ( mParent.getFirstChild() != null )
      {
         // this is a leaf element and should not have any children
         if( mParent.getFirstChild().getLocalName() == null )
         {
            mAllKidsValid = true;
         }
         else
         {
            msgText = msgText = Messages.getString( "MDValidator.323",
                                                mParent.getFirstChild().getLocalName(),
                                                mParent.getLocalName() );
            mLogger.info( "FAILED:" + msgText );
            DetailedLogMessageCollection.getInstance().addMessage( new LogMessage ( 
                                                MessageType.FAILED, msgText ) );
            mAllKidsValid = false;
         }
      }
      else
      {
         mAllKidsValid = true;
      }

   }// end checkLeafForKids()


}// end class MDChildren.java
