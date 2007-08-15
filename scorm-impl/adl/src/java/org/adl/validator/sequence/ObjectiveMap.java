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
package org.adl.validator.sequence;

import java.util.Vector;
import java.util.logging.Logger;

import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

import org.adl.util.MessageType;
import org.adl.util.LogMessage;
import org.adl.util.Messages;
import org.adl.logging.DetailedLogMessageCollection;

/**
 *
 * <strong>Filename: </strong><br>ObjectiveMap.java<br><br>
 *
 * <strong>Description: </strong><br> A <CODE>ObjectiveMap</CODE> is a Data
 * Structure used to store objective information that is necessary for for the
 * validation and processing of read/write attributes to global objectives.<br>
 * <br>
 *
 * <strong>Side Effects: </strong><br>Populates the MessageCollection<br><br>
 *
 * @author ADL Technical Team
 */

public class ObjectiveMap
{
   /**
    * Logger object used for debug logging.
    */
   private Logger mLogger;

   /**
    * The identifier attribute of the <code>&lt;objective&gt;</code> or 
    * <code>&lt;primaryObjective&gt;</code>
    * element.
    */
   private String mObjectiveID;

   /**
    * The identifier attribute of the global <code>&lt;objective&gt;</code>.
    */
   private String mTargetObjectiveID;

   /**
    * Indicates that the satisfaction status for the local objective should
    * be retrieved from the global objective when undefined.
    */
   private String mReadSatisfiedStatus;

   /**
    * Indicates that the satisfaction status for the local objective should be
    * transferred to the global objective upon termination.
    */
   private String mWriteSatisfiedStatus;

   /**
    * Indicates that the normalized measure for the local objective should be
    * retrieved from the global objective when undefined.
    */
   private String mReadNormalizedMeasure;

   /**
    * Indicates that the normalized measure for the local objective should be
    * transferred to the global objective upon termination.
    */
   private String mWriteNormalizedMeasure;

   /**
    * The ObjectiveMap objects for all <code>&lt;objective&gt;</code> and 
    * <code>&lt;primaryObjective&gt;</code> children of the 
    * <code>&lt;objectives&gt;</code> element.
    */
   private Vector mObjectiveMaps;

   /**
    * The default constructor.
    */
   public ObjectiveMap()
   {
      mLogger = Logger.getLogger("org.adl.util.debug.validator"); 

      mObjectiveID                   = new String();
      mTargetObjectiveID             = new String();
      mReadSatisfiedStatus           = new String();
      mWriteSatisfiedStatus          = new String();
      mReadNormalizedMeasure         = new String();
      mWriteNormalizedMeasure        = new String();
      mObjectiveMaps                 = new Vector();
   }

   /**
    * Gives access to <code>identifier</code> attribute of the 
    * <code>&lt;objective&gt;</code> or <code>&lt;primaryObjective&gt;</code> 
    * element.
    *
    * @return Returns the <code>identifier</code> attribute of the objective or 
    * primaryObjective element.
    */
   public String getObjectiveID()
   {
      return mObjectiveID;
   }

   /**
    * Gives access to the <code>identifier</code> attribute of the global 
    * <code>&lt;objective&gt;</code>
    *
    * @return Returns the <code>identifier</code> attribute of the 
    * global objective.
    */
   public String getTargetObjectiveID()
   {
      return mTargetObjectiveID;
   }

   /**
    * The ObjectiveMap objects for all <code>&lt;objective&gt;</code> and 
    * <code>&lt;primaryObjective&gt;</code> children of the  
    * <code>&lt;objectives&gt;</code> element.
    *
    * @return Returns the vector containing all <code>&lt;objective&gt;</code>
    * and <code>&lt;primaryObjective&gt;</code> information in the 
    * <code>&lt;objectives&gt;</code>.
    */
   public Vector getObjectiveMaps()
   {
      return mObjectiveMaps;
   }

   /**
    * Gives access to whether or not the satisfaction status for the local
    * objective should be retrieved from the global objective when undefined
    *
    * @return - Whether or not the satisfaction status for the local objective
    * should be retrieved from the global objective when undefined. 
    * <ul>
    *    <li><code>true</code>: implies it should</li>
    *    <li><code>false</code>: implies otherwise</li>
    * </ul>
    */
   public String getWriteSatisfiedStatus()
   {
      return mWriteSatisfiedStatus;
   }

   /**
    * Gives access to whether or not the satisfaction status for the local
    * objective should be transferred to the global objective upon termination.
    *
    * @return - Whether or not the satisfaction status for the local objective
    * should be transferred to the global objective upon termination.
    */
   public String getReadSatisfiedStatus()
   {
      return mReadSatisfiedStatus;
   }

   /**
    * Gives access to whether or not the normalized measure for the local
    * objective should be retrieved from the global objective when undefined.
    *
    * @return - Whether or not the normalized measure for the local objective
    * should be retrieved from the global objective when undefined.
    */
   public String getReadNormalizedMeasure()
   {
      return mReadNormalizedMeasure;
   }


   /**
    * Gives access to whether or not the normalized measure for the local
    * objective should be retrieved from the global objective when undefined.
    *
    * @return - Whether or not the normalized measure for the local objective
    * should be retrieved from the global objective when undefined.
    */
   public String getWriteNormalizedMeasure()
   {
      return mWriteNormalizedMeasure;
   }

   /**
    * Sets the value of the <code>identifier</code> attribute of the 
    * <code>&lt;objective&gt;</code> or <code>&lt;primaryObjective&gt;</code> 
    * element.
    * 
    * @param iObjectiveID The objective identifier.
    */
   public void setObjectiveID( String iObjectiveID )
   {
      mObjectiveID = iObjectiveID;
   }

   /**
    * Sets the value of the <code>identifier</code> attribute of the global 
    * <code>&lt;objective&gt;</code>
    * 
    * @param iTargetObjectiveID The target objective identifier.
    */
   public void setTargetObjectiveID( String iTargetObjectiveID )
   {
      mTargetObjectiveID = iTargetObjectiveID;
   }


   /**
    * Sets whether or not the satisfaction status for the local
    * objective should be retrieved from the global objective when undefined
    * 
    * @param iWriteSatisfiedStatus The write satisfied status.
    */
   public void setWriteSatisfiedStatus( String iWriteSatisfiedStatus )
   {
      mWriteSatisfiedStatus = iWriteSatisfiedStatus;
   }

   /**
    * Sets whether or not the satisfaction status for the local
    * objective should be transferred to the global objective upon termination.
    * 
    * @param iReadSatisfiedStatus The read satisfied status.
    */
   public void setReadSatisfiedStatus( String iReadSatisfiedStatus )
   {
      mReadSatisfiedStatus = iReadSatisfiedStatus;
   }

   /**
    * Sets whether or not the normalized measure for the local
    * objective should be retrieved from the global objective when undefined.
    * 
    * @param iReadNormalizedMeasure The read normalized measure.
    */
   public void setReadNormalizedMeasure( String iReadNormalizedMeasure )
   {
      mReadNormalizedMeasure = iReadNormalizedMeasure;
   }

   /**
    * Sets whether or not the normalized measure for the local
    * objective should be retrieved from the global objective when undefined.
    * 
    * @param iWriteNormalizedMeasure The write normalized measure.
    */
   public void setWriteNormalizedMeasure( String iWriteNormalizedMeasure )
   {
      mWriteNormalizedMeasure = iWriteNormalizedMeasure;
   }

   /**
    *
    * This method populates the ObjectiveMap object by traversing down
    * the objectives node and storing all information necessary for the
    * validation reading and writing to global objectives.  Information stored
    * for the objectives element includes:
    * objective identifier,taget objective identifer, and flags determining
    * whether read or write normalized measure and satisfifed status are set.
    *
    * @param iNode - the objectives element node, to be used to traverse
    * and populate the ObjectiveMap object.
    */
   public void populateObjectiveMap( Node iNode )
   {
      mLogger.entering( "ObjectiveMap", "populateObjectiveMap" );  

      boolean addToVectorFlag = false;
      String mapObjectiveID = new String();

      if ( iNode.getLocalName().equals("objectives") ) 
      {
         NodeList objectivesChildren = iNode.getChildNodes();
         int length = objectivesChildren.getLength();

         for ( int i = 0; i < length; i++ )
         {
            Node currentChild = objectivesChildren.item(i);
            populateObjectiveMap(currentChild);
         }

      }

      String nodeName = iNode.getLocalName();

      if ( nodeName.equals("primaryObjective") || 
           nodeName.equals("objective")  ) 
      {
         //look for the attributes of this element
         NamedNodeMap attrList = iNode.getAttributes();

         int numAttr = attrList.getLength();
         mLogger.finer( "There are " + numAttr + " attributes of " +  
                         nodeName + " to test" ); 

         Attr currentAttrNode;
         String currentNodeName;
         String attributeValue = null;
         ObjectiveMap currentObjectiveMap = null;

         // loop thru attributes to find ObjectiveID value
         for ( int i = 0; i < numAttr; i++ )
         {
            currentAttrNode = (Attr)attrList.item(i);
            currentNodeName = currentAttrNode.getLocalName();

            if ( currentNodeName.equals("objectiveID") ) 
            {
               attributeValue = currentAttrNode.getValue();

               if ( mObjectiveID.equals("") ) 
               {
                  setObjectiveID( attributeValue );
               }
               else
               {
                  // need to indicate that we need to create a new object and
                  // store in vector atttibute
                  addToVectorFlag = true;
                  currentObjectiveMap = new ObjectiveMap();
                  currentObjectiveMap.setObjectiveID( attributeValue );
               }
               // store this id for the multiple mapInfo elements to have an
               // objectiveID
               mapObjectiveID = attributeValue;
            }
         }

         // find mapInfo children of objective/primaryObjective element
         NodeList children = iNode.getChildNodes();
         int length = children.getLength();

         // flag determining if we have to create new objects to hold multiple
         // mapInfo information.
         boolean createNewMapInfo = false;

         if (length == 0)
         {
            //Put in to prevent null pointer error that is thrown when testing
            //Photoshop_Competency.zip testcase.
            //Not sure if this is the correct fix or not...investigate later.
            if (currentObjectiveMap != null)
            {
               //No mapInfo children, but still need to add the objective
               mObjectiveMaps.add(currentObjectiveMap);
            }
         }
         else
         {

            for ( int j = 0; j < length; j++ )
            {
               Node currentChild = children.item(j);
               String currentChildName = currentChild.getLocalName();

               if ( currentChildName.equals("mapInfo") ) 
               {

                  //look for the attributes of the mapInfo element
                  NamedNodeMap mapAttrList = currentChild.getAttributes();

                  int numMapAttr = mapAttrList.getLength();
                  mLogger.finer( "There are " + numMapAttr + " attributes of " +  
                                  currentChildName + " to test" ); 

                  Attr currentMapAttrNode;
                  String currentMapAttrNodeName;

                  // read/write attributes initialized to default values
                  String readMeasure = "true"; 
                  String readStatus = "true"; 
                  String writeMeasure = "false"; 
                  String writeStatus = "false"; 
                  String targetID = new String();

                  // find the mapInfo attribute to populate the ObjectiveMap
                  for ( int k = 0; k < numMapAttr; k++ )
                  {
                     currentMapAttrNode = (Attr)mapAttrList.item(k);
                     currentMapAttrNodeName = currentMapAttrNode.getLocalName();

                     if ( currentMapAttrNodeName.equals("targetObjectiveID") ) 
                     {
                        targetID = currentMapAttrNode.getValue();
                     }
                     else if ( currentMapAttrNodeName.equals("readNormalizedMeasure") ) 
                     {
                        readMeasure = currentMapAttrNode.getValue();
                     }
                     else if (currentMapAttrNodeName.equals("writeNormalizedMeasure") ) 
                     {
                        writeMeasure = currentMapAttrNode.getValue();
                     }
                     else if ( currentMapAttrNodeName.equals("readSatisfiedStatus") ) 
                     {
                        readStatus = currentMapAttrNode.getValue();
                     }
                     else if (currentMapAttrNodeName.equals("writeSatisfiedStatus") ) 
                     {
                        writeStatus = currentMapAttrNode.getValue();
                     }
                  }

                  if ( createNewMapInfo )
                  {

                     // have to handle the creation of a new object when
                     // multiple mapInfo exist for a given objective
                     ObjectiveMap newCurrentObjectiveMap = new ObjectiveMap();
                     newCurrentObjectiveMap.setObjectiveID( mapObjectiveID );
                     newCurrentObjectiveMap.setTargetObjectiveID( targetID );
                     newCurrentObjectiveMap.setReadSatisfiedStatus( readStatus );
                     newCurrentObjectiveMap.setReadNormalizedMeasure( readMeasure );
                     newCurrentObjectiveMap.setWriteSatisfiedStatus( writeStatus );
                     newCurrentObjectiveMap.setWriteNormalizedMeasure( writeMeasure );

                     mObjectiveMaps.add(newCurrentObjectiveMap);
                  }
                  else if ( addToVectorFlag )
                  {
                     currentObjectiveMap.setTargetObjectiveID( targetID );
                     currentObjectiveMap.setReadSatisfiedStatus( readStatus );
                     currentObjectiveMap.setReadNormalizedMeasure( readMeasure );
                     currentObjectiveMap.setWriteSatisfiedStatus( writeStatus );
                     currentObjectiveMap.setWriteNormalizedMeasure( writeMeasure );

                     mObjectiveMaps.add(currentObjectiveMap);
                  }
                  else
                  {
                     setTargetObjectiveID( targetID );
                     setReadSatisfiedStatus( readStatus );
                     setReadNormalizedMeasure( readMeasure );
                     setWriteSatisfiedStatus( writeStatus );
                     setWriteNormalizedMeasure( writeMeasure );
                  }
                  createNewMapInfo = true;
               }
            }
         }
      }

      mLogger.exiting( "ObjectiveMap", "populateObjectiveMap" );  
   }


   /**
    * This method validates the populated ObjectiveMap objects to determine
    * if read/write to global/local objectives is valid.
    * 
    * @param iObjectiveMap The Objective Map that will be validated.
    *
    * @return - The boolean describing if the ObjectiveMap object(s) has been
    * found to be valid.
    */
   public boolean validateObjectiveMaps( ObjectiveMap iObjectiveMap)
   {
      boolean result = true;

      result = checkReadAttributes( iObjectiveMap, -1 ) && result;
      result = checkWriteAttributes( iObjectiveMap, -1 ) && result;

      return result;
   }

   /**
    * This method performs the validation of the objectives element and their
    * the read status to global objectives.
    * An error is thrown when an objectiveId is found that reads
    * information (readSatisfiedStatus, readSatisfiedMeasure) more than once
    * from the the same global objective.
    *
    * @param iObjectiveMap The objectiveMap objective being validated for
    * the read status.
    *
    * @param iMapLoc The index of the objectiveMap object in the
    * mObjectiveMaps vector.
    *
    * @return Whether or not validation of the read status passes.
    * <ul>
    *    <li><code>true</code>: implies it passes</li>
    *    <li><code>false</code>: implies otherwise</li>
    * </ul>
    */
   private boolean checkReadAttributes( ObjectiveMap iObjectiveMap, 
                                        int iMapLoc )
   {
      mLogger.entering( "ObjectiveMap", "checkReadAttributes" );  
      boolean result = true;
      String msgText = new String();
      boolean foundDuplicateReadStatus = false;
      boolean foundDuplicateReadMeasure = false;

      // loop through checking for matches to the mObjectiveID

      int mObjectiveMapsSize = mObjectiveMaps.size();
      for ( int i = 0; i < mObjectiveMapsSize; i++ )
      {
         // need to position i so that it skips over itself as a comparison
         if ( iMapLoc != -1 )
         {
            if ( i == iMapLoc )
            {
               if ( !( i >= ( mObjectiveMaps.size()-1 ) ) )
               {
                  i++;
               }
               else
               {
                  break;
               }
            }
         }
         try
         {
            ObjectiveMap currentObjectiveMap =
                                      (ObjectiveMap)mObjectiveMaps.elementAt(i);

            String objectiveID2 = currentObjectiveMap.getObjectiveID();

            if ( iObjectiveMap.getObjectiveID().equals( objectiveID2 ) )
            {
               // Found a match, need to validate that both do not read the 
               // same info
               if ( iObjectiveMap.getReadNormalizedMeasure().equalsIgnoreCase("true") 
                    &&
                    currentObjectiveMap.getReadNormalizedMeasure().equalsIgnoreCase("true") ) 
               {
                  result = false && result;
                  foundDuplicateReadMeasure = true;
               }

               if ( iObjectiveMap.getReadSatisfiedStatus().equalsIgnoreCase("true") 
                    &&
                   currentObjectiveMap.getReadSatisfiedStatus().equalsIgnoreCase("true") ) 
               {
                  result = false && result;
                  foundDuplicateReadStatus = true;

               }
            }
         }

         catch( ArrayIndexOutOfBoundsException iAIOBE )
         {
            mLogger.severe( "ArrayIndexOutOfBoundsException thrown" ); 
         }
         catch(NullPointerException iNPE)
         {
            mLogger.severe( "NullPointerException thrown" ); 
         }
      }
      // log error messages
      if ( foundDuplicateReadStatus )
      {
         msgText = Messages.getString("ObjectiveMap.34", iObjectiveMap.getObjectiveID() ); 

         mLogger.info( "FAILED: " + msgText ); 
         DetailedLogMessageCollection.getInstance().addMessage( new LogMessage(
                                                MessageType.FAILED, msgText ) );
      }

      if ( foundDuplicateReadMeasure)
      {
         msgText = Messages.getString("ObjectiveMap.38", iObjectiveMap.getObjectiveID() ); 

         mLogger.info( "FAILED: " + msgText ); 
         DetailedLogMessageCollection.getInstance().addMessage( new LogMessage(
                                                MessageType.FAILED, msgText ) );
      }

      // loop through the remaining ObjectiveMaps in the vector and recurse

      Vector maps = iObjectiveMap.getObjectiveMaps();
      
      for ( int i = 0; i < maps.size(); i++ )
      {

         try
         {
            ObjectiveMap nextObjectiveMap =
                                      (ObjectiveMap)maps.elementAt(i);

            result = checkReadAttributes( nextObjectiveMap, i ) && result;
         }
         catch( ArrayIndexOutOfBoundsException iAIOBE )
         {
            mLogger.severe( "ArrayIndexOutOfBoundsException thrown" ); 
         }
         catch(NullPointerException iNPE)
         {
            mLogger.severe( "NullPointerExceptionException thrown" ); 
         }

      }

      mLogger.exiting( "ObjectiveMap", "checkReadAttributes" );  
      return result;


   }

   /**
    * This method performs the validation of objectives and their write status.
    * An error is thrown when targetObjectiveIDs are equal and are found to be
    * write the same information (writeSatisfiedStatus, writeSatisfiedMeasure)
    * to the global objective.
    *
    * @param iObjectiveMap The objectiveMap objective being validated for
    * the read status.
    *
    * @param iMapLoc The index of the objectiveMap object in the
    * mObjectiveMaps vector.
    *
    * @return Whether or not validation of the read status passes.
    * <ul>
    *    <li><code>true</code>: implies it passes</li>
    *    <li><code>false</code>: implies otherwise</li>
    * </ul>
    */
   private boolean checkWriteAttributes( ObjectiveMap iObjectiveMap, 
                                         int iMapLoc )
   {

      mLogger.entering( "ObjectiveMap", "checkWriteAttributes" );  
      boolean result = true;
      String msgText = new String();

      // loop through checking for matches to the mObjectiveID

      int mObjectiveMapsSize = mObjectiveMaps.size();
      for ( int i = 0; i < mObjectiveMapsSize; i++ )
      {
         // need to position i so that it skips over itself as a comparison
         if ( iMapLoc != -1 )
         {
            if ( i == iMapLoc )
            {
               if ( !( i >= ( mObjectiveMaps.size()-1 ) ) )
               {
                  i++;
               }
               else
               {
                  break;
               }
            }
         }

         try
         {
            ObjectiveMap currentObjectiveMap =
                                      (ObjectiveMap)mObjectiveMaps.elementAt(i);

            String objectiveID2 = currentObjectiveMap.getTargetObjectiveID();


            msgText = "targetID1 is " + iObjectiveMap.getTargetObjectiveID(); 
            mLogger.info( "INFO: " + msgText ); 

            msgText = "targetID2 is " + currentObjectiveMap.getTargetObjectiveID(); 
            mLogger.info( "INFO: " + msgText ); 

            if ( iObjectiveMap.getTargetObjectiveID().equals( objectiveID2 ) )
            {
               // Found a match, need to validate that both do not read 
               // the same info
               if ( iObjectiveMap.getWriteNormalizedMeasure().equalsIgnoreCase("true") 
                    &&
                    currentObjectiveMap.getWriteNormalizedMeasure().equalsIgnoreCase("true") ) 
               {
                  result = false && result;

                  msgText = Messages.getString("ObjectiveMap.53", iObjectiveMap.getTargetObjectiveID() ); 

                  mLogger.info( "FAILED: " + msgText ); 
                  DetailedLogMessageCollection.getInstance().addMessage( new LogMessage(
                                                MessageType.FAILED, msgText ) );
               }

               if ( iObjectiveMap.getWriteSatisfiedStatus().equalsIgnoreCase("true") 
                    &&
                   currentObjectiveMap.getWriteSatisfiedStatus().equalsIgnoreCase("true") ) 
               {
                  result = false && result;

                  msgText = Messages.getString("ObjectiveMap.58", iObjectiveMap.getTargetObjectiveID() ); 

                  mLogger.info( "FAILED: " + msgText ); 
                  DetailedLogMessageCollection.getInstance().addMessage( new LogMessage(
                                                MessageType.FAILED, msgText ) );
               }
            }
         }
         catch( ArrayIndexOutOfBoundsException iAIOBE )
         {
            mLogger.severe( "ArrayIndexOutOfBoundsException thrown" ); 
         }
         catch(NullPointerException iNPE)
         {
            mLogger.severe( "NullPointerException thrown" ); 
         }
      }

      // loop through the remaining ObjectiveMaps in the vector and recurse

      Vector maps = iObjectiveMap.getObjectiveMaps();
      int mapsSize = maps.size();
      for ( int i = 0; i < mapsSize; i++ )
      {

         try
         {
            ObjectiveMap nextObjectiveMap =
                                      (ObjectiveMap)maps.elementAt(i);

            result = checkWriteAttributes( nextObjectiveMap, i ) && result;
         }
         catch( ArrayIndexOutOfBoundsException iAIOBE )
         {
            mLogger.severe( "ArrayIndexOutOfBoundsException thrown" ); 
         }
         catch(NullPointerException iNPE)
         {
            mLogger.severe( "NullPointerException thrown" ); 
         }

      }

      mLogger.exiting( "ObjectiveMap", "checkWriteAttributes" );  
      return result;

   }
}