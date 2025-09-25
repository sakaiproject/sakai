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

package org.adl.sequencer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lombok.extern.slf4j.Slf4j;

/**
 * <strong>Filename:</strong> ADLSeqUtilities.java<br><br>
 * 
 * <strong>Description:</strong><br><br>
 * This class contains several static utiliy methods utilized by the
 * sequencing subsystem.<br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the SCORM 2004 3rd
 * Edition Sample RTE.<br>
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
@Slf4j
public class ADLSeqUtilities {

	/**
	 * Initializes one activity (<code>SeqActivity</code>) that will be added to
	 * an activity tree.
	 * 
	 * @param iNode   A node from the DOM tree of an element containing
	 *                sequencing information.
	 * 
	 * @param iColl   The collection of reusable sequencing information.
	 * 
	 * @return An initialized activity (<code>SeqActivity</code>), or <code>
	 *         null</code> if there was an error initializing the activity.
	 */
	private static SeqActivity buildActivityNode(Node iNode, Node iColl) {

        log.debug("  :: ADLSeqUtilities  --> BEGIN - buildActivityNode");

		SeqActivity act = new SeqActivity();

		boolean error = false;

		String tempVal;

		// Set the activity's ID -- this is a required attribute
		act.setID(ADLSeqUtilities.getAttribute(iNode, "identifier"));

		// Get the activity's resource ID -- if it exsits
		tempVal = ADLSeqUtilities.getAttribute(iNode, "identifierref");
		if (StringUtils.isNotBlank(tempVal)) {
			act.setResourceID(tempVal);
		}

		// Check if the activity is visible
		tempVal = ADLSeqUtilities.getAttribute(iNode, "isvisible");
		if (StringUtils.isNotBlank(tempVal)) {
			act.setIsVisible(Boolean.parseBoolean(tempVal));
		}

		// Get the children elements of this activity 
		NodeList children = iNode.getChildNodes();

		// Initalize this activity from the information in the DOM  
		for (int i = 0; i < children.getLength(); i++) {
			Node curNode = children.item(i);

			// Check to see if this is an element node.
			if (curNode.getNodeType() == Node.ELEMENT_NODE) {
				if (curNode.getLocalName().equals("item")) {

                    log.debug("  ::--> Found an <item> element");

					// Initialize the nested activity
					SeqActivity nestedAct = ADLSeqUtilities.buildActivityNode(curNode, iColl);

					// Make sure this activity was created successfully
					if (nestedAct != null) {
                        log.debug("  ::--> Adding child");

						act.addChild(nestedAct);
					} else {
						error = true;
					}
				} else if (curNode.getLocalName().equals("title")) {

                    log.debug("  ::--> Found the <title> element");

					act.setTitle(ADLSeqUtilities.getElementText(curNode, null));
				} else if (curNode.getLocalName().equals("sequencing")) {

                    log.debug("  ::--> Found the <sequencing> element");

					Node seqInfo = curNode;

					// Check to see if the sequencing information is referenced in 
					// the <sequencingCollection>
					tempVal = ADLSeqUtilities.getAttribute(curNode, "IDRef");
					if (tempVal != null) {
						// Combine local and global sequencing information
						// Get the referenced Global sequencing information
						String search = ".//*[local-name()='sequencing' and @ID='" + tempVal + "']";

						log.debug("  ::--> Looking for sequencing by ID --> {} (search={})", tempVal, search);

						Node seqGlobal = findSequencingById(iColl, tempVal);
						if (seqGlobal == null && iColl != null && iColl.getParentNode() != null) {
							// In some manifests, iColl might be the <manifest> or a sibling; search upward once more as a fallback
							seqGlobal = findSequencingById(iColl.getParentNode(), tempVal);
						}

						if (seqGlobal == null) {
							// search from the document root to cover cases where iColl is not the sequencing collection
							try {
								Node root = curNode.getOwnerDocument() != null ? curNode.getOwnerDocument().getDocumentElement() : null;
								if (root != null) {
 									log.debug("  ::--> Searching from document root for sequencing ID '{}'", tempVal);
									seqGlobal = findSequencingById(root, tempVal);
								}
							} catch (Exception ignore) {
								log.debug("  ::--> IGNORE: Unable to find sequencing ID '{}' from document root", tempVal);
							}
						}

						if (seqGlobal != null) {
 							log.debug("  ::--> FOUND");
						} else {
                            log.debug("  ::--> ERROR: Not Found");
							seqInfo = null;
							error = true;
						}

						if (!error) {

							// Clone the global node
							seqInfo = seqGlobal.cloneNode(true);

							// Loop through the local sequencing element
							NodeList seqChildren = curNode.getChildNodes();
							for (int j = 0; j < seqChildren.getLength(); j++) {

								Node curChild = seqChildren.item(j);

								// Check to see if this is an element node.
								if (curChild.getNodeType() == Node.ELEMENT_NODE) {
                                    log.debug("  ::--> Local definition");
                                    log.debug("  ::-->   {}", j);
                                    log.debug("  ::-->  <{}>", curChild.getLocalName());

                                    // Add this to the global sequencing info (clone/import to ensure same ownerDocument)
									try {
										Node nodeToAppend = curChild.cloneNode(true);
										if (seqInfo.getOwnerDocument() != nodeToAppend.getOwnerDocument()) {
											nodeToAppend = seqInfo.getOwnerDocument().importNode(nodeToAppend, true);
										}
										seqInfo.appendChild(nodeToAppend);
                                    } catch (org.w3c.dom.DOMException e) {
                                        log.debug("  ::--> ERROR: ");
                                        log.warn("could not append sequencing info", e);

                                        error = true;
                                        seqInfo = null;
                                    }
                                }
							}
						}
					}

					// If we have an node to look at, extract its sequencing info
					if (seqInfo != null) {
						// Record this activity's sequencing XML fragment
						//                  XMLSerializer serializer = new XMLSerializer();

						// -+- TODO -+-
						//                  serializer.setNewLine("CR-LF");
						//                  act.setXMLFragment(serializer.writeToString(seqInfo));

						// Extract the sequencing information for this activity
						error = !ADLSeqUtilities.extractSeqInfo(seqInfo, act);
                        log.debug("  ::--> Extracted Sequencing Info");
                    }
				}
			}
		}

		// Make sure this activity either has an associated resource or children
		if (act.getResourceID() == null && !act.hasChildren(true)) {
			// This is not a vaild activity -- ignore it
			error = true;
		}

		// If the activity failed to initialize, clear the variable
		if (error) {
			act = null;
		}

        log.debug("  ::--> error == {}", error);
        log.debug("  :: ADLSeqUtilities  --> END   - buildActivityNode");

        return act;
	}

	/**
	 * Initializes an activity tree (<code>SeqActivityTree</code>) from the
	 * contents of a content package.<br><br>
	 * 
	 * Currently, only this method exists. It accepts an &lt;organization&gt; 
	 * XML node -- the default &lt;organization&gt; element from the parsed CP 
	 * DOM.
	 * 
	 * This method constructs a completly initialized activity tree.  This
	 * implementation is not optimized and is known to have scalablity issues.
	 * <br><br>
	 * 
	 * NOTE: The constuction of an activity tree is not coupled to the
	 * implementation of the sequencer; the sequencer only requires that an
	 * activity tree exists.
	 * 
	 * @param iOrg The <code>Node</code> object corresponding to the
	 *              default <code>&lt;organization&gt;</code> element of the CP.
	 * 
	 * @param iColl The <code>Node</code> object corresponding to the set of
	 * reusable sequencing definitions.
	 *- 
	 * @return An initialized activity tree (<code>SeqActivityTree</code>), or
	 *         <code>null</code> if initialization fails.
	 */
	public static SeqActivityTree buildActivityTree(Node iOrg, Node iColl) {

        log.debug("  :: ADLSeqUtilities  --> BEGIN - buildActivityTree");

		SeqActivityTree tree = new SeqActivityTree();

		// Build and set the root of the activity tree
		SeqActivity root = ADLSeqUtilities.buildActivityNode(iOrg, iColl);

		// Make sure the root was created successfully
		if (root != null) {
			tree.setRoot(root);
			tree.setDepths();
			tree.setTreeCount();
		} else {
			// If any activity failed to initialize, the activity tree is invalid
			tree = null;
		}

        log.debug("  :: ADLSeqUtilities  --> END   - buildActivityTree");
        return tree;
	}

	/**
	 * This method resets the global objective information for a set of
	 * referenced global objectives.
	 * 
	 * @param iLearnerID  The identifier of the student being tracked.
	 * 
	 * @param iScopeID   The identifier of the objective's scope.
	 * 
	 * @param iObjList    A list of global objective IDs.
	 */
	public static void clearGlobalObjs(String iLearnerID, String iScopeID, List<String> iObjList) {

        log.debug("  :: ADLSeqUtilities  --> BEGIN - " + "clearGlobalObjs");
		log.debug("NOT IMPLEMENTED: ADLSeqUtilities:clearGlobalObjs");

		// Get a connection to the global objective DB
		/*Connection conn = LMSDBHandler.getConnection();

		if ( conn != null )
		{
		   if ( iLearnerID != null )
		   {
		      if ( iObjList != null )
		      {

		         try
		         {
		            PreparedStatement stmtClearRecord = null;

		            // Create the SQL string, convert it to a prepared statement. 
		            String sqlClearRecord = "UPDATE Objectives " +
		                                    "SET satisfied = ?, measure = ? " +
		                                    "WHERE objID = ? AND " +  
		                                    "learnerID = ? AND scopeID = ?";

		            stmtClearRecord = conn.prepareStatement(sqlClearRecord);

		            for ( int i = 0; i < iObjList.size(); i++ )
		            {

		               String objID = (String)iObjList.elementAt(i);

		               log.debug("  ::--> Attempting to clear record for --> {} [{}] // {}", iLearnerID, iScopeID, objID);

		               // Insert values into the prepared statement and execute 
		               // the query.
		               synchronized(stmtClearRecord)
		               {
		                  stmtClearRecord.setString(1, "unknown");
		                  stmtClearRecord.setString(2, "unknown");
		                  stmtClearRecord.setString(3, objID);
		                  stmtClearRecord.setString(4, iLearnerID);

		                  if ( iScopeID == null )
		                  {
		                     stmtClearRecord.setString(5, "");
		                  }
		                  else
		                  {
		                     stmtClearRecord.setString(5, iScopeID);
		                  }

		                  stmtClearRecord.executeUpdate();
		               }
		            }

		            // Close the prepared statement.
		            stmtClearRecord.close();
		         }
		         catch ( Exception e )
		         {
		            log.warn("  ::-->  ERROR: DB Failure", e);
		         }
		      }
		      else
		      {
		         log.debug("  ::--> ERROR: NULL objectives list");
		      }
		   }
		   else
		   {
		      log.debug("  ::--> ERROR: NULL learnerID");
		   }
		}
		else
		{
		   log.debug("  ::--> ERROR: NULL connection");
		}


		log.debug("  :: ADLSeqUtilities  --> END   - clearGlobalObjs");
		*/
	}

	/**
	 * Creates a status record associated with a given activity tree's root
	 * and a given learner.
	 * 
	 * @param iCourseID  The ID identifing the activity tree.
	 * 
	 * @param iLearnerID The ID identifing the student.
	 */
	public static void createCourseStatus(String iCourseID, String iLearnerID) {
        log.debug("""
                  :: ADLSeqUtilities  --> BEGIN - createCourseStatus
                  ::-->  {}
                  ::-->  {}
                """, 
                iCourseID, iLearnerID);
        log.debug("NOT IMPLEMENTED - ADLSeqUtilies:createCourseStatus");

		// Get a connection to the global objective DB
		/*Connection conn = LMSDBHandler.getConnection();

		if ( conn != null )
		{
		   if ( iLearnerID != null )
		   {
		      if ( iCourseID != null )
		      {
		         try
		         {
		            PreparedStatement stmtCheckRecord = null;

		            // Create the SQL string, convert it to a prepared stat.
		            String sqlCheckRecord = "SELECT * FROM CourseStatus WHERE " +
		                                    "courseID = ? AND " + 
		                                    "learnerID = ?";

		            stmtCheckRecord = conn.prepareStatement( sqlCheckRecord );
		            ResultSet objRS = null;

		            synchronized(stmtCheckRecord)
		            {
		               stmtCheckRecord.setString(1, iCourseID);
		               stmtCheckRecord.setString(2, iLearnerID);

		               objRS = stmtCheckRecord.executeQuery();
		            }

		            PreparedStatement stmtCreateRecord = null;

		            // the objective does not exist, add it
		            if ( !objRS.next() )
		            {

		               log.debug("  ::--> Creating course status --> {} --> {}", iCourseID, iLearnerID);

		               // Create the SQL string, 
		               //   convert it to a prepared statement
		               String sqlCreateRecord = "INSERT INTO CourseStatus " +
		                                        "(courseID, learnerID, " + 
		                                        "satisfied, measure, " +
		                                        "completed) " +
		                                        "VALUES (?, ?, ? ,?, ?)";

		               stmtCreateRecord =
		               conn.prepareStatement(sqlCreateRecord);

		               // Insert values into the prepared statement and 
		               // execute the query.
		               synchronized(stmtCreateRecord)
		               {
		                  stmtCreateRecord.setString(1, iCourseID);
		                  stmtCreateRecord.setString(2, iLearnerID);

		                  stmtCreateRecord.setString(3, "unknown");
		                  stmtCreateRecord.setString(4, "unknown");
		                  stmtCreateRecord.setString(5, "unknown");

		                  stmtCreateRecord.executeUpdate();
		               }

		               // Close the prepared statement
		               stmtCreateRecord.close();
		            }


		            // Close the result set and prepared statement
		            objRS.close();
		            stmtCheckRecord.close();
		         }
		         catch ( Exception e )
		         {
		            log.warn("  ::-->  ERROR: DB Failure", e);
		         }
		      }
		      else
		      {
		         log.debug("  ::--> ERROR: NULL Course ID");
		      }
		   }
		   else
		   {
		      log.debug("  ::--> ERROR: NULL Student ID");
		   }
		}
		else
		{
		   log.debug("  ::--> ERROR: NULL connection");
		}

		log.debug("  :: ADLSeqUtilities  --> END   - createCourseStatus");
		*/
	}

	/**
	 * This method ensures that global objective information exists for a set of
	 * referenced global objectives.
	 * 
	 * @param iLearnerID  The identifier of the student being tracked.
	 * 
	 * @param iScopeID    The identifier of the objective's scope.
	 * 
	 * @param iObjList    A list of global objective IDs.
	 */
	public static void createGlobalObjs(String iLearnerID, String iScopeID, List<String> iObjList) {
        log.debug("""
                          :: ADLSeqUtilities  --> BEGIN - createGlobalObjs
                          ::-->  {}
                          ::-->  {}
                        """,
                iLearnerID, iScopeID);
        log.debug("NOT IMPLEMENTED: ADLSeqUtilities:createGlobalObjs");

		// Get a connection to the global objective DB
		/*Connection conn = LMSDBHandler.getConnection();

		if ( conn != null )
		{
		   if ( iLearnerID != null )
		   {
		      if ( iObjList != null )
		      {
		         try
		         {
		            PreparedStatement stmtCheckRecord = null;

		            // Create the SQL string, convert it to a prepared stat.
		            String sqlCheckRecord = "SELECT * FROM Objectives WHERE " +
		                                    "objID = ? AND " + 
		                                    "learnerID = ? AND scopeID = ?";

		            stmtCheckRecord = conn.prepareStatement( sqlCheckRecord );
		            ResultSet objRS = null;

		            for ( int i = 0; i < iObjList.size(); i++ )
		            {

		               String objID = (String)iObjList.elementAt(i);

		               log.debug("  ::--> Checking for objective --> {} [{}] // {}", iLearnerID, iScopeID, objID);

		               synchronized(stmtCheckRecord)
		               {
		                  stmtCheckRecord.setString(1, objID);
		                  stmtCheckRecord.setString(2, iLearnerID);

		                  if ( iScopeID == null )
		                  {
		                     stmtCheckRecord.setString(3, "");     
		                  }
		                  else
		                  {
		                     stmtCheckRecord.setString(3, iScopeID);
		                  }

		                  objRS = stmtCheckRecord.executeQuery();
		               }

		               PreparedStatement stmtCreateRecord = null;

		               // the objective does not exist, add it
		               if ( !objRS.next() )
		               {

     		              log.debug("  ::--> Creating objective --> {} [{}] // {}", iLearnerID, iScopeID, objID);

		                  // Create the SQL string,
		                  //   convert it to a prepared statement
		                  String sqlCreateRecord = "INSERT INTO Objectives " +
		                                           "(objID, learnerID, " + 
		                                           "scopeID, " +
		                                           "satisfied, measure) " +
		                                           "VALUES (?, ?, ? ,?, ?)";

		                  stmtCreateRecord =
		                  conn.prepareStatement(sqlCreateRecord);

		                  // Insert values into the prepared statement and 
		                  // execute the query.
		                  synchronized(stmtCreateRecord)
		                  {
		                     stmtCreateRecord.setString(1, objID);
		                     stmtCreateRecord.setString(2, iLearnerID);

		                     if ( iScopeID == null )
		                     {
		                        stmtCreateRecord.setString(3, "");
		                     }
		                     else
		                     {
		                        stmtCreateRecord.setString(3, iScopeID);
		                     }

		                     stmtCreateRecord.setString(4, "unknown");
		                     stmtCreateRecord.setString(5, "unknown");

		                     stmtCreateRecord.executeUpdate();
		                  }

		                  // Close the prepared statement
		                  stmtCreateRecord.close();
		               }
		            }

		            // Close the result set and prepared statement
		            objRS.close();
		            stmtCheckRecord.close();
		         }
		         catch ( Exception e )
		         {
		            log.warn("  ::-->  ERROR: DB Failure", e);
		         }
		      }
		      else
		      {
		         log.debug("  ::--> ERROR: NULL Objective List");
		      }
		   }
		   else
		   {
		      log.debug("  ::--> ERROR: NULL StudentID");
		   }
		}
		else
		{
		   log.debug("  ::--> ERROR: NULL connection");
		}

		log.debug("  :: ADLSeqUtilities  --> END   - createGlobalObjs");
		*/
	}

	/**
	 * This method deletes a course associated with a learner from the DB
	 * 
	 * @param iCourseID  The ID identifing the activity tree.
	 *  
	 * @param iLearnerID  The identifier of the student being tracked.
	 * 
	 */
	public static void deleteCourseStatus(String iCourseID, String iLearnerID) {

        log.debug("  :: ADLSeqUtilities  --> BEGIN - deleteCourseStatus");
		log.debug("NOT IMPLEMENTED - ADLSeqUtilies:deleteCourseStatus");

		// Get a connection to the global objective DB
		/*Connection conn = LMSDBHandler.getConnection();

		if ( conn != null )
		{
		   if ( iLearnerID != null )
		   {
		      if ( iCourseID != null )
		      {
		         try
		         {
		            PreparedStatement stmtDeleteRecord = null;

		            // Create the SQL string, convert it to a prepared statement. 
		            String sqlDeleteRecord = "DELETE * FROM CourseStatus " +
		                                     "WHERE courseID = ? AND " + 
		                                     "learnerID = ?";

		            stmtDeleteRecord = conn.prepareStatement(sqlDeleteRecord);

		            // Insert values into the prepared statement and 
		            // execute the query.
		            synchronized(stmtDeleteRecord)
		            {
		               stmtDeleteRecord.setString(1, iCourseID);
		               stmtDeleteRecord.setString(2, iLearnerID);

		               stmtDeleteRecord.executeUpdate();
		            }

		            // Close the prepared statement
		            stmtDeleteRecord.close();
		         }
		         catch ( Exception e )
		         {
		            log.warn("  ::-->  ERROR: DB Failure", e);
		         }
		      }
		      else
		      {
		         log.debug("  ::--> ERROR: NULL course ID");
		      }
		   }
		   else
		   {
		      log.debug("  ::--> ERROR: NULL student ID");
		   }
		}
		else
		{
		   log.debug("  ::--> ERROR: NULL connection");
		}

		log.debug("  :: ADLSeqUtilities  --> END   - deleteCourseStatus");
		*/
	}

	/**
	 * This method deletes a set global objective information from the database
	 * 
	 * @param iLearnerID  The identifier of the student being tracked.
	 * 
	 * @param iScopeID   The identifier of the objective's scope.
	 * 
	 * @param iObjList    A list of global objective IDs.
	 */
	public static void deleteGlobalObjs(String iLearnerID, String iScopeID, List<String> iObjList) {

        log.debug("  :: ADLSeqUtilities  --> BEGIN - " + "deleteGlobalObjs");

		log.debug("NOT IMPLEMENTED - ADLSeqUtilies:deleteGlobalObjs");

		// Get a connection to the global objective DB
		/*Connection conn = LMSDBHandler.getConnection();

		if ( conn != null )
		{
		   if ( iLearnerID != null )
		   {
		      if ( iObjList != null )
		      {
		         try
		         {
		            PreparedStatement stmtDeleteRecord = null;

		            // Create the SQL string, convert it to a prepared statement. 
		            String sqlDeleteRecord = "DELETE FROM Objectives " +
		                                     "WHERE objID = ? AND " + 
		                                     "learnerID = ? AND scopeID = ?";

		            stmtDeleteRecord = conn.prepareStatement(sqlDeleteRecord);


		            for ( int i = 0; i < iObjList.size(); i++ )
		            {

		               String objID = (String)iObjList.elementAt(i);


		               log.debug("  ::--> Attempting to delete record for --> {} [{}] // {}", iLearnerID, iScopeID, objID);

		               // Insert values into the prepared statement and 
		               // execute the query.
		               synchronized(stmtDeleteRecord)
		               {
		                  stmtDeleteRecord.setString(1, objID);
		                  stmtDeleteRecord.setString(2, iLearnerID);

		                  if ( iScopeID == null )
		                  {
		                     stmtDeleteRecord.setString(3, "");
		                  }
		                  else
		                  {
		                     stmtDeleteRecord.setString(3, iScopeID);
		                  }

		                  stmtDeleteRecord.executeUpdate();
		               }
		            }

		            // Close the prepared statement
		            stmtDeleteRecord.close();
		         }
		         catch ( Exception e )
		         {
		            log.debug("  ::-->  ERROR: DB Failure", e);
		         }
		      }
		      else
		      {
		         log.debug("  ::--> ERROR: NULL objective list");
		      }
		   }
		   else
		   {
		      log.debug("  ::--> ERROR: NULL StudentID");
		   }
		}
		else
		{
		   log.debug("  ::--> ERROR: NULL connection");
		}

		   log.debug("  :: ADLSeqUtilities  --> END   - deleteGlobalObjs");
		*/
	}

	/**
	 * Displays the values of the <code>ADLTOC</code> objects that constitute a
	 * table of contents.  This method is used for diagnostic purposes.
	 * 
	 * @param iTOC   A List of <code>ADLTOC</code> objects describing the TOC.
	 */
	public static void dumpTOC(List<ADLTOC> iTOC) {
        if (log.isDebugEnabled()) {
            log.debug("  :: ADLSeqUtilities  --> BEGIN - dumpTOC");

            if (iTOC != null) {
                log.debug("  ::-->  {}", iTOC.size());

                ADLTOC temp;
                for (ADLTOC adltoc : iTOC) {
                    temp = adltoc;
                    temp.dumpState();
                }
            } else {
                log.debug("  ::--> NULL");
            }

            log.debug("  :: ADLSeqUtilities  --> END   - dumpTOC");
        }
    }

	/**
	 * Extracts the contents of the IMS SS <code>&lt;sequencing&gt;</code> 
	 * element and initializes the associated activity.
	 * 
	 * @param iNode The DOM node associated with the IMS SS 
	 *        <code>&lt;sequencing&gt;</code>) element.
	 * 
	 * @param ioAct The associated activity being initialized.
	 * 
	 * @return <code>true</code> if the sequencing information extracted
	 *         successfully, otherwise <code>false</code>.
	 */
	private static boolean extractSeqInfo(Node iNode, SeqActivity ioAct) {
        log.debug("  :: ADLSeqUtilities  --> BEGIN - " + "extractSeqInfo");

		boolean ok = true;
		String tempVal;

		// Get the children elements of <sequencing>
		NodeList children = iNode.getChildNodes();

		// Initalize this activity's sequencing information  
		for (int i = 0; i < children.getLength(); i++) {
			Node curNode = children.item(i);

			// Check to see if this is an element node.
			if (curNode.getNodeType() == Node.ELEMENT_NODE) {
				if (curNode.getLocalName().equals("controlMode")) {

                    log.debug("  ::--> Found the <controlMode> element");

					// Look for 'choice'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "choice");
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setControlModeChoice(Boolean.parseBoolean(tempVal));
					}

					// Look for 'choiceExit'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "choiceExit");
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setControlModeChoiceExit(Boolean.parseBoolean(tempVal));
					}

					// Look for 'flow'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "flow");
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setControlModeFlow(Boolean.parseBoolean(tempVal));
					}

					// Look for 'forwardOnly'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "forwardOnly");
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setControlForwardOnly(Boolean.parseBoolean(tempVal));
					}

					// Look for 'useCurrentAttemptObjectiveInfo'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "useCurrentAttemptObjectiveInfo");
					
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setUseCurObjective(Boolean.parseBoolean(tempVal));
					}

					// Look for 'useCurrentAttemptProgressInfo'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "useCurrentAttemptProgressInfo");
					
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setUseCurProgress(Boolean.parseBoolean(tempVal));
					}
				} else if (curNode.getLocalName().equals("sequencingRules")) {
                    log.debug("  ::--> Found the <sequencingRules> element");

					ok = ADLSeqUtilities.getSequencingRules(curNode, ioAct);
				} else if (curNode.getLocalName().equals("limitConditions")) {
                    log.debug("  ::--> Found the <limitConditions> " + "element");

					// Look for 'attemptLimit'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "attemptLimit");
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setAttemptLimit(Long.valueOf(tempVal));
					}

					// Look for 'attemptAbsoluteDurationLimit'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "attemptAbsoluteDurationLimit");
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setAttemptAbDur(tempVal);
					}

					// Look for 'attemptExperiencedDurationLimit'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "attemptExperiencedDurationLimit");
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setAttemptExDur(tempVal);
					}

					// Look for 'activityAbsoluteDurationLimit'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "activityAbsoluteDurationLimit");
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setActivityAbDur(tempVal);
					}

					// Look for 'activityExperiencedDurationLimit'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "activityExperiencedDurationLimit");
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setActivityExDur(tempVal);
					}

					// Look for 'beginTimeLimit'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "beginTimeLimit");
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setBeginTimeLimit(tempVal);
					}

					// Look for 'endTimeLimit'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "endTimeLimit");
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setEndTimeLimit(tempVal);
					}
				} else if (curNode.getLocalName().equals("auxiliaryResources")) {
                    log.debug("  ::--> Found the <auxiliaryResourcees> element");

					ok = ADLSeqUtilities.getAuxResources(curNode, ioAct);
				} else if (curNode.getLocalName().equals("rollupRules")) {
                    log.debug("  ::--> Found the <rollupRules> element");

					ok = ADLSeqUtilities.getRollupRules(curNode, ioAct);
				} else if (curNode.getLocalName().equals("objectives")) {
                    log.debug("  ::--> Found the <objectives> element");

					ok = ADLSeqUtilities.getObjectives(curNode, ioAct);
				} else if (curNode.getLocalName().equals("randomizationControls")) {
                    log.debug("  ::--> Found the <randomizationControls> element");

					// Look for 'randomizationTiming'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "randomizationTiming");
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setRandomTiming(tempVal);
					}

					// Look for 'selectCount'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "selectCount");
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setSelectCount(Integer.parseInt(tempVal));
					}

					// Look for 'reorderChildren'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "reorderChildren");
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setReorderChildren(Boolean.parseBoolean(tempVal));
					}

					// Look for 'selectionTiming'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "selectionTiming");
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setSelectionTiming(tempVal);
					}
				} else if (curNode.getLocalName().equals("deliveryControls")) {
                    log.debug("  ::--> Found the <deliveryControls> element");

					// Look for 'tracked'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "tracked");
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setIsTracked(Boolean.parseBoolean(tempVal));
					}

					// Look for 'completionSetByContent'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "completionSetByContent");
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setSetCompletion(Boolean.parseBoolean(tempVal));
					}

					// Look for 'objectiveSetByContent'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "objectiveSetByContent");
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setSetObjective(Boolean.parseBoolean(tempVal));
					}
				} else if (curNode.getLocalName().equals("constrainedChoiceConsiderations")) {
                    log.debug("  ::--> Found the " + "<constrainedChoiceConsiderations> element");

					// Look for 'preventActivation'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "preventActivation");
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setPreventActivation(Boolean.parseBoolean(tempVal));
					}

					// Look for 'constrainChoice'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "constrainChoice");
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setConstrainChoice(Boolean.parseBoolean(tempVal));
					}
				} else if (curNode.getLocalName().equals("rollupConsiderations")) {
                    log.debug("  ::--> Found the " + "<rollupConsiderations> element");

					// Look for 'requiredForSatisfied'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "requiredForSatisfied");
					
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setRequiredForSatisfied(tempVal);
					}

					// Look for 'requiredForNotSatisfied'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "requiredForNotSatisfied");
					
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setRequiredForNotSatisfied(tempVal);
					}

					// Look for 'requiredForCompleted'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "requiredForCompleted");
					
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setRequiredForCompleted(tempVal);
					}

					// Look for 'requiredForIncomplete'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "requiredForIncomplete");
					
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setRequiredForIncomplete(tempVal);
					}

					// Look for 'measureSatisfactionIfActive'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "measureSatisfactionIfActive");
					
					if (StringUtils.isNotBlank(tempVal)) {
						ioAct.setSatisfactionIfActive(Boolean.parseBoolean(tempVal));
					}
				}
			}
		}

        log.debug("  ::-->  {}", ok);
        log.debug("  :: ADLSeqUtilities  --> END   - extractSeqInfo");
        return ok;
	}

	/**
	 * Extracts the conditions assoicated with a sequencing rule.
	 * 
	 * @param iNode  The DOM node associated with one of the IMS SS <code>
	 *               &lt;ruleConditions&gt;</code> element.
	 * 
	 * @return The condition set (<code>SeqConditionSet</code>) assoicated with
	 *         the rule.
	 */
	private static SeqConditionSet extractSeqRuleConditions(Node iNode) {

        log.debug("  :: ADLSeqUtilities  --> BEGIN - extractSeqRuleConditions");

		String tempVal;
		SeqConditionSet condSet = new SeqConditionSet();

		List<SeqCondition> conditions = new ArrayList<>();

		// Look for 'conditionCombination'
		tempVal = ADLSeqUtilities.getAttribute(iNode, "conditionCombination");
		if (tempVal != null) {
			if (StringUtils.isNotBlank(tempVal)) {
				condSet.mCombination = tempVal;
			}
		} else {
			// Enforce Default
			condSet.mCombination = SeqConditionSet.COMBINATION_ALL;
		}

		NodeList condInfo = iNode.getChildNodes();

		for (int i = 0; i < condInfo.getLength(); i++) {

			Node curCond = condInfo.item(i);

			// Check to see if this is an element node.
			if (curCond.getNodeType() == Node.ELEMENT_NODE) {

				if (curCond.getLocalName().equals("ruleCondition")) {
                    log.debug("  ::--> Found a <Condition> element");

					SeqCondition cond = new SeqCondition();

					// Look for 'condition'
					tempVal = ADLSeqUtilities.getAttribute(curCond, "condition");
					if (StringUtils.isNotBlank(tempVal)) {
						cond.mCondition = tempVal;
					}

					// Look for 'referencedObjective'
					tempVal = ADLSeqUtilities.getAttribute(curCond, "referencedObjective");
					if (StringUtils.isNotBlank(tempVal)) {
						cond.mObjID = tempVal;
					}

					// Look for 'measureThreshold'
					tempVal = ADLSeqUtilities.getAttribute(curCond, "measureThreshold");
					if (StringUtils.isNotBlank(tempVal)) {
						cond.mThreshold = Double.parseDouble(tempVal);
					}

					// Look for 'operator'
					tempVal = ADLSeqUtilities.getAttribute(curCond, "operator");
					if (tempVal != null) {
                        cond.mNot = tempVal.equals("not");
					}

					conditions.add(cond);
				}
			}
		}

		if (!conditions.isEmpty()) {
			// Add the conditions to the condition set 
			condSet.mConditions = conditions;
		} else {
			condSet = null;
		}

        log.debug("  :: ADLSeqUtilities  --> END   - extractSeqRuleConditions");
		return condSet;
	}

	/**
	 * Attempts to find the indicated attribute of the target element.
	 *
	 * @param iNode      The DOM node of the target element.
	 *
	 * @param iAttribute The requested attribute.
	 *
	 * @return The value of the requested attribute on the target element,
	 *         <code>null</code> if the attribute does not exist.
	 */
	private static String getAttribute(Node iNode, String iAttribute) {

        log.debug("  :: ADLSeqUtilities  --> BEGIN - getAttribute");
        log.debug("  ::-->  {}", iAttribute);

        String value = null;

		// Extract the node's attribute list and check if the requested
		// attribute is contained in it.
		NamedNodeMap attrs = iNode.getAttributes();

		if (attrs != null) {
			// Attempt to get the requested attribute
			Node attr = attrs.getNamedItem(iAttribute);

			if (attr != null) {
				// Extract the attributes value
				value = attr.getNodeValue();
			} else {
                log.debug("  ::-->  The attribute [{}] does not exist.", iAttribute);
			}
		} else {
            log.debug("  ::-->  This node has no attributes.");
		}

        log.debug("  ::-->  {}", value);
        log.debug("  :: ADLSeqUtilities  --> END - getAttribute");

        return value;
	}

	/**
	 * Extracts the descriptions of auxiliary resoures associated with the       
	 * activity from the activiy's associated element in the DOM.
	 * 
	 * @param iNode The DOM node associated with the IMS SS <code>
	 *              &lt;dataMap&gt;</code>) element.
	 * 
	 * @param ioAct The associated activity being initialized
	 * 
	 * @return <code>true</code> if the sequencing information extracted
	 *         successfully, otherwise <code>false</code>.
	 */
	private static boolean getAuxResources(Node iNode, SeqActivity ioAct) {

        log.debug("  :: ADLSeqUtilities  --> BEGIN - getAuxResources");

		boolean ok = true;
		String tempVal;

		// List of auxiliary resources
		List<ADLAuxiliaryResource> auxRes = new ArrayList<>();

		// Get the children elements of <auxiliaryResources>
		NodeList children = iNode.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node curNode = children.item(i);

			// Check to see if this is an element node.
			if (curNode.getNodeType() == Node.ELEMENT_NODE) {
				if (curNode.getLocalName().equals("auxiliaryResource")) {
					// Build a new data mapping rule
                    log.debug("  ::--> Found a <auxiliaryResource> element");

					ADLAuxiliaryResource res = new ADLAuxiliaryResource();

					// Get the resource's purpose
					tempVal = ADLSeqUtilities.getAttribute(curNode, "purpose");
					if (tempVal != null) {
						res.mType = tempVal;
					}

					// Get the resource's ID
					tempVal = ADLSeqUtilities.getAttribute(curNode, "auxiliaryResourceID");
					if (tempVal != null) {
						res.mResourceID = tempVal;
					}

					// Add this datamap to the list associated with this activity
					auxRes.add(res);
				}
			}
		}

		// Add the set of auxiliary resources to the activity
		ioAct.setAuxResources(auxRes);
        log.debug("  :: ADLSeqUtilities  --> END   - getAuxResources");
		return ok;
	}

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	
	 Private Methods
	
	-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

	/**
	 * Attempts to find the indicated subelement of the current node and
	 * extact its value.
	 *
	 * @param iNode    The DOM node of the target element.
	 *
	 * @param iElement The requested subelement.
	 *
	 * @return The value of the requested subelement of target element, or
	 *         <code>null</code> if the subelement does not exist.
	 */
	private static String getElementText(Node iNode, String iElement) {

        log.debug("""
                :: ADLSeqUtilities  --> BEGIN - getElementText
                ::-->  {}
              """, iElement);

        StringBuilder value = new StringBuilder();
		Node curNode = null;
		NodeList children;

		if (iElement != null && iNode != null) {
			children = iNode.getChildNodes();

			// Locate the target subelement
			for (int i = 0; i < children.getLength(); i++) {
				curNode = children.item(i);

				// Check to see if this is an element node.
				if (curNode.getNodeType() == Node.ELEMENT_NODE) {
                    log.debug("""
                                    ::-->   {}
                                    ::-->  <{}>
                                  """,
                            i, curNode.getLocalName());

                    if (curNode.getLocalName().equals(iElement)) {
                        log.debug("  ::--> Found <{}>", iElement);
						break;
					}
				}
			}

			if (curNode != null) {
				String comp = curNode.getLocalName();

				if (comp != null) {
					// Make sure we found the subelement
					if (!comp.equals(iElement)) {
						curNode = null;
					}
				} else {
					curNode = null;
				}
			}
		} else {
			curNode = iNode;
		}

		if (curNode != null) {
            log.debug("  ::--> Looking at children");

			// Extract the element's text value.
			children = curNode.getChildNodes();

			// Cycle through all children of node to get the text
			if (children != null) {
				// There must be a value
				value = new StringBuilder();

				for (int i = 0; i < children.getLength(); i++) {
					curNode = children.item(i);

					// make sure we have a 'text' element
					if ((curNode.getNodeType() == Node.TEXT_NODE) || (curNode.getNodeType() == Node.CDATA_SECTION_NODE)) {
						value.append(curNode.getNodeValue());
					}
				}
			}
		}

        log.debug("""
                          ::-->  {}
                          :: ADLSeqUtilities  --> END   - getElementText");
                        """,
                value);
        return value.toString();
	}

	/**
	 * Retrieves the measure associated with the global objective and
	 * the student.
	 * 
	 * @param iObjID     The ID identifing the desired global objective.
	 * 
	 * @param iLearnerID The ID identifing the student.
	 * 
	 * @param iScopeID   The identifier of the objective's scope.
	 * 
	 * @return The score associated with the shared competency, or
	 *         <code>null</code> if either ID is invalid.
	 */
	public static String getGlobalObjMeasure(String iObjID, String iLearnerID, String iScopeID) {

        log.debug("  :: ADLSeqUtilities  --> BEGIN - getGlobalObjMeasure");
		log.debug("NOT IMPLEMENTED - ADLSeqUtilies:getGlobalObjMeasure");

		String measure = null;

		// Get a connection to the global objective DB
		/*Connection conn = LMSDBHandler.getConnection();

		if ( conn != null )
		{
		   if ( iObjID != null )
		   {
		      if ( iLearnerID != null )
		      {
		         try
		         {
		            PreparedStatement stmtSelectMeasure = null;
		            ResultSet objRS = null;

		            // Create the SQL string and convert it to 
		            // a prepared statement.
		            String sqlSelectMeasure = "SELECT measure FROM Objectives " + 
		                                      "WHERE objID = ? AND " +
		                                      "learnerID = ? AND scopeID = ?";

		            stmtSelectMeasure = conn.prepareStatement(sqlSelectMeasure);

		            // Insert values into the prepared statement and execute the 
		            // query
		            synchronized( stmtSelectMeasure)
		            {
		               stmtSelectMeasure.setString(1, iObjID);
		               stmtSelectMeasure.setString(2, iLearnerID);

		               if ( iScopeID == null )
		               {
		                  stmtSelectMeasure.setString(3, "");    
		               }
		               else
		               {
		                  stmtSelectMeasure.setString(3, iScopeID);
		               }

		               objRS = stmtSelectMeasure.executeQuery();
		            }
		            // Make sure a result set is returned
		            if ( objRS.next() )
		            {
		               measure = objRS.getString("measure");
		            }
		            else
		            {
		               log.debug("  ::--> No resultset");
		            }

		            // Close result set
		            objRS.close();

		            // Close the prepared statement 
		            stmtSelectMeasure.close();
		         }
		         catch ( Exception e )
		         {
		            log.debug("  ::-->  ERROR : DB Failure", e);
		         }
		      }
		      else
		      {
		         log.debug("  ::--> ERROR : NULL student ID");
		      }
		   }
		   else
		   {
		      log.debug("  ::--> ERROR : NULL objective ID");
		   }
		}
		else
		{
		   log.debug("  ::--> ERROR : NULL connection");
		}

		log.debug("  ::-->  " + measure);
		log.debug("  :: ADLSeqUtilities  --> END   - getGlobalObjMeasure");
		*/

		return measure;
	}

	/**
	 * Retrieves the statified status associated with the global objective and
	 * the student.
	 * 
	 * @param iObjID     The ID identifing the global shared objective.
	 * 
	 * @param iLearnerID The ID identifing the student.
	 * 
	 * @param iScopeID   The identifier of the objective's scope.
	 * 
	 * @return The satified status associated with the global objective,
	 *         or <code>null</code> if either ID is invalid.
	 */
	public static String getGlobalObjSatisfied(String iObjID, String iLearnerID, String iScopeID) {

        log.debug("""
                        :: ADLSeqUtilities  --> BEGIN - getGlobalObjSatisfied
                        ::--> {}
                        ::--> {}
                        ::--> {}
                        NOT IMPLEMENTED - ADLSeqUtilies:getGlobalObjSatisfied
                      """,
                iObjID, iLearnerID, iScopeID);

        String satisfiedStatus = null;


		// Get a connection to the global objective DB
		/*Connection conn = LMSDBHandler.getConnection();

		if ( conn != null )
		{
		   if ( iLearnerID != null )
		   {
		      if ( iObjID != null )
		      {

		         try
		         {
		            PreparedStatement stmtSelectSatisfied= null;
		            ResultSet objRS = null;

		            // Create the SQL string, convert it to a prepared statement.
		            String sqlSelectSatisfied = "SELECT satisfied FROM " + 
		                                        "Objectives WHERE " +
		                                        "objID = ? AND " + 
		                                        "learnerID = ? AND scopeID = ?";

		            stmtSelectSatisfied = 
		            conn.prepareStatement(sqlSelectSatisfied);

		            // Insert values into the prepared statement 
		            //  and execute the query.
		            synchronized(stmtSelectSatisfied)
		            {
		               stmtSelectSatisfied.setString(1, iObjID);
		               stmtSelectSatisfied.setString(2, iLearnerID);

		               if ( iScopeID == null )
		               {
		                  stmtSelectSatisfied.setString(3, ""); 
		               }
		               else
		               {
		                  stmtSelectSatisfied.setString(3, iScopeID);
		               }

		               objRS = stmtSelectSatisfied.executeQuery();
		            }

		            // Make sure a result set is returned
		            if ( objRS.next() )
		            {
		               satisfiedStatus = objRS.getString("satisfied");
		            }
		            else
		            {
		               log.debug("  ::--> No result set");
		               satisfiedStatus = null;
		            }

		            // Close result set
		            objRS.close();

		            // Close the prepared statement
		            stmtSelectSatisfied.close();
		         }
		         catch ( Exception e )
		         {
		            log.debug("  ::-->  ERROR : DB Failure", e);
		         }
		      }
		      else
		      {
		         log.debug("  ::--> ERROR : NULL comp ID");
		      }
		   }
		   else
		   {
		      log.debug("  ::--> ERROR : NULL learnerID");
		   }
		}
		else
		{
		   log.debug("  ::--> ERROR : NULL connection");
		}

		log.debug("  ::-->  {}", satisfiedStatus);
		log.debug("  :: ADLSeqUtilities  --> END   - getGlobalObjSatisfied");
		*/

		return satisfiedStatus;
	}

	/**
	 * Extracts the objective maps associated with a specific objective from the
	 * <code>&lt;objectives&gt;</code> element of the DOM.
	 *
	 * @param iNode The DOM node associated with an objective.=
	 *
	 * @return The set (<code>List</code>) of objective maps extracted
	 *         successfully, otherwise <code>null</code>.
	 */
	private static List<SeqObjectiveMap> getObjectiveMaps(Node iNode) {

        log.debug("  :: ADLSeqUtilities  --> BEGIN - getObjectiveMaps");

		String tempVal;
		List<SeqObjectiveMap> maps = new ArrayList<>();

		// Get the children elements of this objective
		NodeList children = iNode.getChildNodes();

		// Initalize this objective's objective maps
		for (int i = 0; i < children.getLength(); i++) {
			Node curNode = children.item(i);

			// Check to see if this is an element node.
			if (curNode.getNodeType() == Node.ELEMENT_NODE) {
				if (curNode.getLocalName().equals("mapInfo")) {
                    log.debug("  ::--> Found a <mapInfo> element");

					SeqObjectiveMap map = new SeqObjectiveMap();

					// Look for 'targetObjectiveID'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "targetObjectiveID");
					if (StringUtils.isNotBlank(tempVal)) {
                        map.mGlobalObjID = tempVal;
					}

					// Look for 'readSatisfiedStatus'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "readSatisfiedStatus");
                    if (StringUtils.isNotBlank(tempVal)) {
                        map.mReadStatus = Boolean.parseBoolean(tempVal);
					}

					// Look for 'readNormalizedMeasure'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "readNormalizedMeasure");
                    if (StringUtils.isNotBlank(tempVal)) {
                        map.mReadMeasure = Boolean.parseBoolean(tempVal);
					}

					// Look for 'writeSatisfiedStatus'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "writeSatisfiedStatus");
                    if (StringUtils.isNotBlank(tempVal)) {
                        map.mWriteStatus = Boolean.parseBoolean(tempVal);
					}

					// Look for 'writeNormalizedMeasure'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "writeNormalizedMeasure");
                    if (StringUtils.isNotBlank(tempVal)) {
                        map.mWriteMeasure = Boolean.parseBoolean(tempVal);
					}

					maps.add(map);
				}
			}
		}

		// Don't return an empty set.
		if (maps.isEmpty()) {
			maps = null;
		}

        log.debug("  :: ADLSeqUtilities  --> END   - getObjectiveMaps");
		return maps;
	}

	/**
	 * Extracts the objectives associated with the activity from the
	 * <code>&lt;objectives&gt;</code> element of the DOM.
	 *
	 * @param iNode The DOM node associated with the IMS SS <code>
	 *              &lt;objectives&gt;</code> element.
	 *
	 * @param ioAct The associated activity being initialized
	 *
	 * @return <code>true</code> if the sequencing information extracted
	 *         successfully, otherwise <code>false</code>.
	 */
	private static boolean getObjectives(Node iNode, SeqActivity ioAct) {

        log.debug("  :: ADLSeqUtilities  --> BEGIN - getObjectives");

		boolean ok = true;
		String tempVal = null;
		List<SeqObjective> objectives = new ArrayList<>();

		// Get the children elements of <objectives>
		NodeList children = iNode.getChildNodes();

		// Initalize this activity's objectives
		for (int i = 0; i < children.getLength(); i++) {
			Node curNode = children.item(i);

			// Check to see if this is an element node.
			if (curNode.getNodeType() == Node.ELEMENT_NODE) {
				if (curNode.getLocalName().equals("primaryObjective")) {
                    log.debug("  ::--> Found a <primaryObjective> element");

					SeqObjective obj = new SeqObjective();

					obj.mContributesToRollup = true;

					// Look for 'objectiveID'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "objectiveID");
					if (StringUtils.isNotBlank(tempVal)) {
						obj.mObjID = tempVal;
					}

					// Look for 'satisfiedByMeasure'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "satisfiedByMeasure");
					if (StringUtils.isNotBlank(tempVal)) {
						obj.mSatisfiedByMeasure = Boolean.parseBoolean(tempVal);
					}

					// Look for 'minNormalizedMeasure'
					tempVal = getElementText(curNode, "minNormalizedMeasure");
					if (StringUtils.isNotBlank(tempVal)) {
                        obj.mMinMeasure = Double.parseDouble(tempVal);
					}

					List<SeqObjectiveMap> maps = getObjectiveMaps(curNode);

					if (maps != null) {
						obj.mMaps = maps;
					}

					obj.mContributesToRollup = true;
					objectives.add(obj);
				} else if (curNode.getLocalName().equals("objective")) {
                    log.debug("  ::--> Found a <objective> element");

					SeqObjective obj = new SeqObjective();

					// Look for 'objectiveID'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "objectiveID");
					if (StringUtils.isNotBlank(tempVal)) {
						obj.mObjID = tempVal;
					}

					// Look for 'satisfiedByMeasure'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "satisfiedByMeasure");
                    if (StringUtils.isNotBlank(tempVal)) {
                        obj.mSatisfiedByMeasure = Boolean.parseBoolean(tempVal);
					}

					// Look for 'minNormalizedMeasure'
					tempVal = getElementText(curNode, "minNormalizedMeasure");
					if (StringUtils.isNotBlank(tempVal)) {
                        obj.mMinMeasure = Double.parseDouble(tempVal);
					}

					List<SeqObjectiveMap> maps = getObjectiveMaps(curNode);

					if (maps != null) {
						obj.mMaps = maps;
					}

					objectives.add(obj);
				}
			}
		}

		// Set the Activity's objectives
		ioAct.setObjectives(objectives);

        log.debug("  :: ADLSeqUtilities  --> END   - getObjectives");
		return ok;
	}

	/**
	 * Extracts the rollup rules associated with the activity from the
	 * <code>&lt;sequencingRules&gt;</code> element of the DOM.
	 *
	 * @param iNode The DOM node associated with the IMS SS <code>
	 *              &lt;sequencingRules&gt;</code> element.
	 *
	 * @param ioAct The associated activity being initialized
	 *
	 * @return <code>true</code> if the sequencing information extracted
	 *         successfully, otherwise <code>false</code>.
	 */
	private static boolean getRollupRules(Node iNode, SeqActivity ioAct) {

        log.debug("  :: ADLSeqUtilities  --> BEGIN - getRollupRules");

		boolean ok = true;
		String tempVal = null;
		List<SeqRollupRule> rollupRules = new ArrayList<>();

		// Look for 'rollupObjectiveSatisfied'
        tempVal = ADLSeqUtilities.getAttribute(iNode, "rollupObjectiveSatisfied");
        if (StringUtils.isNotBlank(tempVal)) {
            ioAct.setIsObjRolledUp(Boolean.parseBoolean(tempVal)); 
		}
		// Look for 'objectiveMeasureWeight'
		tempVal = ADLSeqUtilities.getAttribute(iNode, "objectiveMeasureWeight");
        if (StringUtils.isNotBlank(tempVal)) {
            ioAct.setObjMeasureWeight(Double.parseDouble(tempVal));
        }
		// Look for 'rollupProgressCompletion'
		tempVal = ADLSeqUtilities.getAttribute(iNode, "rollupProgressCompletion");
        if (StringUtils.isNotBlank(tempVal)) {
            ioAct.setIsProgressRolledUp(Boolean.parseBoolean(tempVal));
        }
		// Get the children elements of <rollupRules>
		NodeList children = iNode.getChildNodes();

		// Initalize this activity's rollup rules 
		for (int i = 0; i < children.getLength(); i++) {
			Node curNode = children.item(i);

			// Check to see if this is an element node.
			if (curNode.getNodeType() == Node.ELEMENT_NODE) {
				if (curNode.getLocalName().equals("rollupRule")) {
					// Extract all of the rollup Rules
                    log.debug("  ::--> Found a <rollupRule> element");

					SeqRollupRule rule = new SeqRollupRule();

					// Look for 'childActivitySet'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "childActivitySet");
                    if (StringUtils.isNotBlank(tempVal)) {
                        rule.mChildActivitySet = tempVal;
                    }

					// Look for 'minimumCount'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "minimumCount");
                    if (StringUtils.isNotBlank(tempVal)) {
                        rule.mMinCount = Long.parseLong(tempVal);
					}

					// Look for 'minimumPercent'
					tempVal = ADLSeqUtilities.getAttribute(curNode, "minimumPercent");
					if (StringUtils.isNotBlank(tempVal)) {
                        rule.mMinPercent = Double.parseDouble(tempVal);
					}

					rule.mConditions = new SeqConditionSet(true);
					List<SeqCondition> conditions = new ArrayList<>();

					NodeList ruleInfo = curNode.getChildNodes();

					// Initalize this rollup rule
					for (int j = 0; j < ruleInfo.getLength(); j++) {

						Node curRule = ruleInfo.item(j);

						// Check to see if this is an element node.
						if (curRule.getNodeType() == Node.ELEMENT_NODE) {
							if (curRule.getLocalName().equals("rollupConditions")) {

                                log.debug("  ::--> Found a <rollupConditions> element");

								// Look for 'conditionCombination'
								tempVal = ADLSeqUtilities.getAttribute(curRule, "conditionCombination");

                                if (StringUtils.isNotBlank(tempVal)) {
                                    rule.mConditions.mCombination = tempVal;
								} else {
									// Enforce Default
									rule.mConditions.mCombination = SeqConditionSet.COMBINATION_ANY;
								}

								NodeList conds = curRule.getChildNodes();

								for (int k = 0; k < conds.getLength(); k++) {
									Node con = conds.item(k);

									// Check to see if this is an element node.
									if (con.getNodeType() == Node.ELEMENT_NODE) {
										if (con.getLocalName().equals("rollupCondition")) {

                                            log.debug("  ::--> Found a <rollupCondition> element");

											SeqCondition cond = new SeqCondition();

											// Look for 'condition'
											tempVal = ADLSeqUtilities.getAttribute(con, "condition");
                                            if (StringUtils.isNotBlank(tempVal)) {
                                                cond.mCondition = tempVal;
											}

											// Look for 'operator'
											tempVal = ADLSeqUtilities.getAttribute(con, "operator");
											if (tempVal != null) {
                                                cond.mNot = tempVal.equals("not");
											}

											conditions.add(cond);
										}
									}
								}
							} else if (curRule.getLocalName().equals("rollupAction")) {

                                log.debug("  ::--> Found a <rollupAction> element");
								// Look for 'action'
								tempVal = ADLSeqUtilities.getAttribute(curRule, "action");
                                if (StringUtils.isNotBlank(tempVal)) {
                                    rule.setRollupAction(tempVal);
								}
							}
						}
					}

					// Add the conditions to the condition set for the rule
					rule.mConditions.mConditions = conditions;

					// Add the rule to the ruleset
					rollupRules.add(rule);
				}
			}
		}

		if (rollupRules != null) {
			ISeqRollupRuleset rules = new SeqRollupRuleset(rollupRules);

			// Set the Activity's rollup rules
			ioAct.setRollupRules(rules);
		}

        log.debug("  :: ADLSeqUtilities  --> END   - getRollupRules");
		return ok;
	}

	/**
	 * Extracts the sequencing rules associated with the activity from the
	 * <code>&lt;sequencingRules&gt;</code> element of the DOM.
	 * 
	 * @param iNode The DOM node associated with the IMS SS <code>
	 *              &lt;sequencingRules&gt;</code> element.
	 * 
	 * @param ioAct The associated activity being initialized
	 * 
	 * @return <code>true</code> if the sequencing information extracted
	 *         successfully, otherwise <code>false</code>.
	 */
	private static boolean getSequencingRules(Node iNode, SeqActivity ioAct) {

        log.debug("  :: ADLSeqUtilities  --> BEGIN - getSequencingRules");

		boolean ok = true;
		String tempVal = null;

		List<ISeqRule> preRules = new ArrayList<>();
		List<ISeqRule> exitRules = new ArrayList<>();
		List<ISeqRule> postRules = new ArrayList<>();

		// Get the children elements of <sequencingRules>
		NodeList children = iNode.getChildNodes();

		// Initalize this activity's sequencing rules 
		for (int i = 0; i < children.getLength(); i++) {
			Node curNode = children.item(i);

			// Check to see if this is an element node.
			if (curNode.getNodeType() == Node.ELEMENT_NODE) {
				if (curNode.getLocalName().equals("preConditionRule")) {
					// Extract all of the precondition rules
                    log.debug("  ::--> Found a <preConditionRule> element");

					SeqRule rule = new SeqRule();

					NodeList ruleInfo = curNode.getChildNodes();

					for (int j = 0; j < ruleInfo.getLength(); j++) {

						Node curRule = ruleInfo.item(j);

						// Check to see if this is an element node.
						if (curRule.getNodeType() == Node.ELEMENT_NODE) {
							if (curRule.getLocalName().equals("ruleConditions")) {

                                log.debug("  ::--> Found a <ruleConditions> element");

								// Extract the condition set
								rule.mConditions = extractSeqRuleConditions(curRule);

							} else if (curRule.getLocalName().equals("ruleAction")) {
                                log.debug("  ::--> Found a <ruleAction> element");

								// Look for 'action'
								tempVal = ADLSeqUtilities.getAttribute(curRule, "action");

								if (StringUtils.isNotBlank(tempVal)) {
									rule.mAction = tempVal;
								}
							}
						}
					}

					if (rule.mConditions != null && rule.mAction != null) {
						preRules.add(rule);
					} else {
                        log.debug("  ::--> ERROR : Invaild Pre SeqRule");
					}
				} else if (curNode.getLocalName().equals("exitConditionRule")) {
					// Extract all of the exit action rules
                    log.debug("  ::--> Found a <exitConditionRule> element");

					SeqRule rule = new SeqRule();

					NodeList ruleInfo = curNode.getChildNodes();

					for (int k = 0; k < ruleInfo.getLength(); k++) {

						Node curRule = ruleInfo.item(k);

						// Check to see if this is an element node.
						if (curRule.getNodeType() == Node.ELEMENT_NODE) {
							if (curRule.getLocalName().equals("ruleConditions")) {
                                log.debug("  ::--> Found a <ruleConditions> element");

								// Extract the condition set
								rule.mConditions = extractSeqRuleConditions(curRule);

							} else if (curRule.getLocalName().equals("ruleAction")) {
                                log.debug("  ::--> Found a <ruleAction> element");

								// Look for 'action'
								tempVal = ADLSeqUtilities.getAttribute(curRule, "action");

								if (tempVal != null) {
									rule.mAction = tempVal;
								}
							}
						}
					}

					if (rule.mConditions != null && rule.mAction != null) {
						exitRules.add(rule);
					} else {
                        log.debug("  ::--> ERROR : Invaild Exit SeqRule");
					}
				} else if (curNode.getLocalName().equals("postConditionRule")) {
					// Extract all of the post condition action rules
                    log.debug("  ::--> Found a <postConditionRule> element");

					SeqRule rule = new SeqRule();

					NodeList ruleInfo = curNode.getChildNodes();

					for (int j = 0; j < ruleInfo.getLength(); j++) {

						Node curRule = ruleInfo.item(j);

						// Check to see if this is an element node.
						if (curRule.getNodeType() == Node.ELEMENT_NODE) {
							if (curRule.getLocalName().equals("ruleConditions")) {
                                log.debug("  ::--> Found a <ruleConditions> element");

								// Extract the condition set
								rule.mConditions = extractSeqRuleConditions(curRule);

							} else if (curRule.getLocalName().equals("ruleAction")) {
                                log.debug("  ::--> Found a <ruleAction> element");

								// Look for 'action'
								tempVal = ADLSeqUtilities.getAttribute(curRule, "action");

								if (tempVal != null) {
									rule.mAction = tempVal;
								}
							}
						}
					}

					if (rule.mConditions != null && rule.mAction != null) {
						postRules.add(rule);
					} else {
                        log.debug("  ::--> ERROR : Invaild Post SeqRule");
					}
				}
			}
		}

		if (!preRules.isEmpty()) {
			ISeqRuleset rules = new SeqRuleset(preRules);

			ioAct.setPreSeqRules(rules);
		}

		if (!exitRules.isEmpty()) {
			ISeqRuleset rules = new SeqRuleset(exitRules);

			ioAct.setExitSeqRules(rules);
		}

		if (!postRules.isEmpty()) {
			ISeqRuleset rules = new SeqRuleset(postRules);

			ioAct.setPostSeqRules(rules);
		}

        log.debug("  :: ADLSeqUtilities  --> END   - getSequencingRules");
		return ok;
	}


	/**
	 * Sets the status associated with a given activity tree's root and a given
	 * learner.
	 * 
	 * @param iCourseID  The ID identifing the activity tree.
	 * 
	 * @param iLearnerID The ID identifing the student.
	 * 
	 * @param iSatisfied The course's satisfied status.
	 * 
	 * @param iMeasure   The course's measure.
	 * 
	 * @param iCompleted The course's completion status.
	 * 
	 * @return <code>true</code> if the set was successful; if an error occured
	 *         <code>false</code>.
	 */
	public static boolean setCourseStatus(String iCourseID, String iLearnerID, String iSatisfied, String iMeasure, String iCompleted) {

        log.debug("""
                          :: ADLSeqUtilities  --> BEGIN - setCourseStatus
                          ::-->  {}
                          ::-->  {}
                          ::-->  {}
                          ::-->  {}
                          ::-->  {}
                        """,
                iCourseID, iLearnerID, iSatisfied, iMeasure, iCompleted);
        boolean success = true;

		// Validate vocabulary
		if (!(iSatisfied.equals("unknown") || iSatisfied.equals("satisfied") || iSatisfied.equals("notSatisfied"))) {

			success = false;

            log.debug("""
                              ::-->  Invalid value: {}
                              ::-->  {}
                              :: ADLSeqUtilities  --> END   - setCourseStatus
                            """,
                    iSatisfied, success);
            return success;
		}

		// Validate vocabulary
		if (!(iCompleted.equals("unknown") || iCompleted.equals("completed") || iCompleted.equals("incomplete"))) {

			success = false;

            log.debug("""
                              ::-->  Invalid value: {}
                              ::-->  {}
                              :: ADLSeqUtilities  --> END   - setCourseStatus
                            """,
                    iCompleted, success);

            return success;
		}

		// Validate measure range
		try {
			double measure = Double.parseDouble(iMeasure);

			if (measure < -1.0 || measure > 1.0) {
				success = false;
			}
		} catch (Exception e) {
			success = false;
		}

		if (!success) {

            log.debug("""
                              ::-->  Invalid value: {}
                              ::-->  {}
                              :: ADLSeqUtilities  --> END   - setCourseStatus
                            """,
                    iMeasure, success);
			return success;
		}

		log.debug("NOT IMPLEMENTED - ADLSeqUtilies:setCourseStatus");

		// Get a connection to the global objective DB
		/*Connection conn = LMSDBHandler.getConnection();

		if ( conn != null )
		{
		   if ( iLearnerID != null )
		   {
		      if ( iCourseID != null )
		      {
		         try
		         {

		            PreparedStatement stmtUpdateSatisfied = null;

		            // Create the SQL string
		            String sqlUpdateSatisfied = "UPDATE CourseStatus " + 
		                                        "SET " +
		                                        "satisfied = ? ," +
		                                        "measure = ? ," +
		                                        "completed = ? " +
		                                        "WHERE courseID = ? AND " +
		                                        "learnerID = ?";

		            stmtUpdateSatisfied = 
		            conn.prepareStatement(sqlUpdateSatisfied);

		            // Execute the query
		            synchronized(stmtUpdateSatisfied)
		            {
		               stmtUpdateSatisfied.setString(1, iSatisfied);
		               stmtUpdateSatisfied.setString(2, iMeasure);
		               stmtUpdateSatisfied.setString(3, iCompleted);
		               stmtUpdateSatisfied.setString(4, iCourseID);
		               stmtUpdateSatisfied.setString(5, iLearnerID);

		               stmtUpdateSatisfied.executeUpdate();
		            }

		            // Close the prepared statement
		            stmtUpdateSatisfied.close();
		         }
		         catch ( Exception e )
		         {
		            log.warn("  ::--> ERROR: DB Failure", e);
		            success = false;
		         }
		      }
		      else
		      {
		         log.debug("  ::--> ERROR: NULL course ID");
		         success = false;
		      }
		   }
		   else
		   {
		      log.debug("  ::--> ERROR: NULL learner ID");
		      success = false;
		   }
		}
		else
		{
		   log.debug("  ::--> ERROR: NULL connection");
		   success = false;
		}

		log.debug("  ::--> " + success);
		log.debug("  :: ADLSeqUtilities  --> END   - setCourseStatus");
		*/

		return success;
	}

	/**
	 * Sets the measure associated with the global objective and the student.
	 * 
	 * @param iObjID     The ID identifing the desired global objective.
	 * 
	 * @param iLearnerID The ID identifing the student.
	 * 
	 * @param iScopeID   The identifier of the objective's scope.
	 * 
	 * @param iMeasure   The desired measure.
	 * 
	 * @return <code>true</code> if the set was successful; if an error occured
	 *         <code>false</code>.     
	 */
	public static boolean setGlobalObjMeasure(String iObjID, String iLearnerID, String iScopeID, String iMeasure) {

        log.debug("""
                        :: ADLSeqUtilities  --> BEGIN - setGlobalObjMeasure
                        ::--> {}
                        ::--> {}
                        ::--> {}
                        ::--> {}
                        """,
                iObjID, iLearnerID, iScopeID, iMeasure);
		boolean goodMeasure = true;
		boolean success = true;

		// Validate score
		if (!iMeasure.equals("unknown")) {
			try {
                double range = Double.parseDouble(iMeasure);

				if (range < -1.0 || range > 1.0) {
                    log.debug("  ::--> Invalid range:  {}", iMeasure);

					// The measure is out of range -- ignore
					goodMeasure = false;
				}
			} catch (NumberFormatException e) {
                log.debug("  ::--> Invalid value:  {}", iMeasure);

				// Invalid format or 'Unknown'
				goodMeasure = false;
			}

			if (!goodMeasure) {
				success = false;

                log.debug("""
                          ::--> {}
                          :: ADLSeqUtilities  --> END   - getGlobalObjMeasure
                        """, success);
                return success;
			}
		}

		log.debug("NOT IMPLEMENTED - ADLSeqUtilies:setGlobalObjMeasure");

		// Get a connection to the global objective DB
		/*Connection conn = LMSDBHandler.getConnection();

		if ( conn != null )
		{
		   if ( iObjID != null )
		   {
		      if ( iLearnerID != null )
		      {
		         try
		         {
		            PreparedStatement stmtUpdateMeasure = null;

		            // Create the SQL string and covert it to a prepared statement
		            String sqlUpdateMeasure = "UPDATE Objectives SET " + 
		                                      "measure = ? " +
		                                      "WHERE objID = ? AND " + 
		                                      "learnerID = ? AND scopeID = ?";

		            stmtUpdateMeasure = conn.prepareStatement(sqlUpdateMeasure);

		            // Insert values into the prepared statement and execute the 
		            // update query
		            synchronized( stmtUpdateMeasure )
		            {
		               stmtUpdateMeasure.setString(1, iMeasure);
		               stmtUpdateMeasure.setString(2, iObjID);
		               stmtUpdateMeasure.setString(3, iLearnerID);

		               if ( iScopeID == null )
		               {
		                  stmtUpdateMeasure.setString(4, "");
		               }
		               else
		               {
		                  stmtUpdateMeasure.setString(4, iScopeID);
		               }

		               stmtUpdateMeasure.executeUpdate();
		            }

		            // Close the prepared statement
		            stmtUpdateMeasure.close();
		         }
		         catch ( Exception e )
		         {
		            log.debug("  ::-->  ERROR: DB Failure", e);
		            success = false;
		         }
		      }
		      else
		      {
		            log.debug("  ::--> ERROR: NULL learnerID");
		         success = false;
		      }
		   }
		   else
		   {
		         log.debug("  ::--> ERROR: NULL obj ID");
		      success = false;
		   }
		}
		else
		{
		      log.debug("  ::--> ERROR: NULL connection");

		   success = false;
		}

		   log.debug("  ::--> " + success);
		   log.debug("  :: ADLSeqUtilities  --> END   - getGlobalObjMeasure");
		*/

		return success;
	}

	/**
	 * Sets the satisfied status associated with a global objective and student.
	 * 
	 * @param iObjID     The ID identifing the global objective information.
	 * 
	 * @param iLearnerID The ID identifing the student.
	 * 
	 * @param iScopeID   The identifier of the objective's scope.
	 * 
	 * @param iSatisfied The desired satisfied status.
	 * 
	 * @return <code>true</code> if the set was successful; if an error occured
	 *         <code>false</code>.
	 */
	public static boolean setGlobalObjSatisfied(String iObjID, String iLearnerID, String iScopeID, String iSatisfied) {

        log.debug("""
                          :: ADLSeqUtilities  --> BEGIN - setGlobalObjSatisfied
                          ::--> {}
                          ::--> {}
                          ::--> {}
                          ::--> {}
                        """,
                iObjID, iLearnerID, iScopeID, iSatisfied);
        boolean success = true;

		// Validate vocabulary
		if (!(iSatisfied.equals("unknown") || iSatisfied.equals("satisfied") || iSatisfied.equals("notSatisfied"))) {

			success = false;

            log.debug("""
                              ::--> Invalid value: {}
                              ::-->  {}
                              :: ADLSeqUtilities  --> END   - setGlobalObjSatisfied
                            """,
                    iSatisfied, success);
            return success;
		}

		log.warn("NOT IMPLEMENTED - ADLSeqUtilies:setGlobalObjSatisfied");
		return success;
	}

	/**
	 * Finds the first <sequencing> element (any namespace) under the given root
	 * whose ID attribute matches the provided id.
	 * This avoids XPath to prevent Transformer/DTM issues in some XML parsers.
	 */
	private static Node findSequencingById(Node root, String id) {
		if (root == null || id == null) {
			return null;
		}

		if (root.getNodeType() == Node.ELEMENT_NODE) {
			String local = root.getLocalName();
			if ("sequencing".equals(local)) {
				String attr = ADLSeqUtilities.getAttribute(root, "ID");
				if (id.equals(attr)) {
					return root;
				}
			}
		}

		NodeList list = root.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node found = findSequencingById(list.item(i), id);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

}
