/**********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.assignment.api.model;

import org.sakaiproject.assignment.api.AssignmentService;

import java.util.List;

/**
 * This is the interface for accessing assignment supplement item
 *
 * @author zqian
 */
public interface AssignmentSupplementItemService {

    /*************** attachment ********************/
    /**
     * new AssignmentSupplementItemAttachment object
     */
    public AssignmentSupplementItemAttachment newAttachment();

    /**
     * Save AssignmentSupplementItemAttachment object
     *
     * @param attachment
     * @return
     */
    public boolean saveAttachment(AssignmentSupplementItemAttachment attachment);

    /**
     * get the list of attachment ids for a AssignmentSupplementItemWithAttachment
     *
     * @param item
     * @return
     */
    public List<String> getAttachmentListForSupplementItem(AssignmentSupplementItemWithAttachment item);

    /**
     * reset the attachment list
     *
     * @param item
     * @return
     */
    public boolean cleanAttachment(AssignmentSupplementItemWithAttachment item);

    /**
     * remove the AssignmentSupplementItemAttachment object
     *
     * @param attachment
     * @return
     */
    public boolean removeAttachment(AssignmentSupplementItemAttachment attachment);

    /*************** model answer ******************/

    /**
     * new ModelAnswer object
     *
     * @return
     */
    public AssignmentModelAnswerItem newModelAnswer();

    /**
     * Save the ModelAnswer object
     *
     * @param mItem
     * @return
     */
    public boolean saveModelAnswer(AssignmentModelAnswerItem mItem);

    /**
     * Remove the ModelAnswer object
     *
     * @param mItem
     * @return
     */
    public boolean removeModelAnswer(AssignmentModelAnswerItem mItem);

    /**
     * Get the ModelAnswer object
     *
     * @param assignmentId
     * @return
     */
    public AssignmentModelAnswerItem getModelAnswer(String assignmentId);

    /******************* private note *******************/

    /**
     * new AssignmentNoteItem object
     */
    public AssignmentNoteItem newNoteItem();

    /**
     * Save the AssignmentNoteItem object
     *
     * @param nItem
     * @return
     */
    public boolean saveNoteItem(AssignmentNoteItem nItem);

    /**
     * Remove the AssignmentNoteItem object
     *
     * @param nItem
     * @return
     */
    public boolean removeNoteItem(AssignmentNoteItem nItem);

    /**
     * Get the AssignmentNoteItem object
     *
     * @param assignmentId
     * @return
     */
    public AssignmentNoteItem getNoteItem(String assignmentId);

    /******************* all purpose *******************/

    /**
     * new AssignmentAllPurposeItem object
     */
    public AssignmentAllPurposeItem newAllPurposeItem();

    /**
     * Save the AssignmentAllPurposeItem object
     *
     * @param aItem
     * @return
     */
    public boolean saveAllPurposeItem(AssignmentAllPurposeItem aItem);

    /**
     * Remove the AssignmentAllPurposeItem object
     *
     * @param aItem
     * @return
     */
    public boolean removeAllPurposeItem(AssignmentAllPurposeItem aItem);

    /**
     * reset the all purpose item access list
     *
     * @param aItem
     * @return
     */
    public boolean cleanAllPurposeItemAccess(AssignmentAllPurposeItem aItem);

    /**
     * Get the AssignmentAllPurposeItem object
     *
     * @param assignmentId
     * @return
     */
    public AssignmentAllPurposeItem getAllPurposeItem(String assignmentId);

    /**
     * new AssignmentAllPurposeItemAccess object
     */
    public AssignmentAllPurposeItemAccess newAllPurposeItemAccess();

    /**
     * save AssignmentAllPurposeItemAccess object
     */
    public boolean saveAllPurposeItemAccess(AssignmentAllPurposeItemAccess access);

    /**
     * remove the access record
     *
     * @param access
     * @return
     */
    public boolean removeAllPurposeItemAccess(AssignmentAllPurposeItemAccess access);

    /**
     * get the access list for this AllPurposeItem
     *
     * @param item
     * @return
     */
    public List<String> getAccessListForAllPurposeItem(AssignmentAllPurposeItem item);


    /**
     * Can the current user see the model answer or not
     *
     * @param a
     * @param s
     * @return
     */
    public boolean canViewModelAnswer(Assignment a, AssignmentSubmission s);

    /**
     * Can current user read the AssignmentNoteItem?
     *
     * @param a
     * @param context
     * @return
     */
    public boolean canReadNoteItem(Assignment a, String context);

    /**
     * Can the current user modify the AssignmentNoteItem?
     *
     * @param a
     * @return
     */
    public boolean canEditNoteItem(Assignment a);


    /**
     * Can the current user view the all purpose item?
     *
     * @param a
     * @return
     */
    public boolean canViewAllPurposeItem(Assignment a);

    void setAssignmentService(AssignmentService assignmentService);
}
