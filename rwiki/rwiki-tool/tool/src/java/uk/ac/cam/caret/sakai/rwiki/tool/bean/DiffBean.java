/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
package uk.ac.cam.caret.sakai.rwiki.tool.bean;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;

/**
 * DiffBean is a helper bean which given an <code>RWikiObject</code> and some
 * strings representing the chosen left and right revision numbers will
 * determine which revisions have been chosen and create a
 * <code>GenericDiffBean</code> from the contents.
 * 
 * @see GenericDiffBean
 * @see uk.ac.cam.caret.sakai.rwiki.service.api.api.model.RWikiObject
 * @version $Id$
 * @author andrew
 */
//FIXME: Tool

public class DiffBean {

    /**
     * RWikiHistoryObject representing the left revision (i.e. the old revision)
     */
    private RWikiObject left;

    /**
     * RWikiHistoryObject representing the right revision (i.e. the new
     * revision)
     */
    private RWikiObject right;

    /**
     * The actual RWikiObject to which both left and right belong
     */
    private RWikiObject rwo;
    
    /**
     * 
     */
    private RWikiObjectService rwikiObjectService;

    /**
     * The revision number of <code>right</code>
     */
    private int rightVersionNumber;

    /**
     * The revision number of <code>left</code>
     */
    private int leftVersionNumber;

    /**
     * A Generic Diff Bean which will be given the contents of the revisions
     */
    private GenericDiffBean db;

    /**
     * Creates a DiffBean for the given RWikiObject, setting the left and right
     * revision numbers as appropriate
     * 
     * @param rwikiObject
     *            The RWikiObject to get revisions from
     * @param left
     *            String representation of the left revision number. Must be
     *            either null, empty or a number.
     * @param right
     *            String representation of the right revision number. Must be
     *            either null, empty or a number.
     */
    public DiffBean(RWikiObject rwikiObject, RWikiObjectService rwikiObjectService,  String left, String right) {
        this.rwo = rwikiObject;
        this.rwikiObjectService = rwikiObjectService;
        
        this.leftVersionNumber = rwikiObject.getRevision().intValue() - 1;
        this.rightVersionNumber = rwikiObject.getRevision().intValue();
        if (this.leftVersionNumber < 0)
            this.leftVersionNumber = 0;

        // Must be in this order... setRightVersionNumber sets the default
        // value for the left revision.
        this.setRightVersionNumber(right);
        this.setLeftVersionNumber(left);
    }

    private void setLeftVersionNumber(String versionString) {
        if (versionString != null && !versionString.equals("")) {
            leftVersionNumber = Integer.parseInt(versionString);
        }

        try {
        		if ( rwo.getRevision().intValue() == leftVersionNumber ) {
        			left = rwo;
        		} else {
        			left = rwikiObjectService.getRWikiHistoryObject(rwo,leftVersionNumber);
        		}
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid version number for left revision");
        }

    }

    private void setRightVersionNumber(String versionString) {
        if (versionString != null && !versionString.equals("")) {
            rightVersionNumber = Integer.parseInt(versionString);

            try {
            		if ( rwo.getRevision().intValue() == rightVersionNumber ) {
            			right = rwo;
            		} else 
            			right = rwikiObjectService.getRWikiHistoryObject(rwo,rightVersionNumber);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Invalid version number for right revision");
            }
        } else {
            right = rwikiObjectService.getRWikiObject(rwo);
        }

        // Finally set the default value for leftVersion
        leftVersionNumber = rightVersionNumber > 0 ? rightVersionNumber - 1 : 0;
    }

    /**
     * Get the selected left revision number.
     * 
     * @return the left revision number.
     */
    public int getLeftVersionNumber() {
        return leftVersionNumber;
    }

    /**
     * Get the selected right revision number.
     * 
     * @return the right revision number.
     */
    public int getRightVersionNumber() {
        return rightVersionNumber;
    }

    /**
     * @return Returns the RWikiObject this diffBean is related to.
     */
    public RWikiObject getRwikiObject() {
        return rwo;
    }

    /**
     * Gets the selected left revision
     * 
     * @return left revision
     */
    public RWikiObject getLeft() {
        return left;
    }

    /**
     * Gets the selected right revision
     * 
     * @return right revision
     */
    public RWikiObject getRight() {
        return right;
    }

    /**
     * Creates a <code>GenericDiffBean</code> using the contents of the left
     * and right revisions
     * 
     * @return a GenericDiffBean
     */
    public GenericDiffBean getGenericDiffBean() {
        if (db == null) {
            db = new GenericDiffBean(left.getContent(), right.getContent());
        }
        return db;
    }
}
