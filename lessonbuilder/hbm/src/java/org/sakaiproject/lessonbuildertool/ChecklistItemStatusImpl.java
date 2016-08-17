/**********************************************************************************
 * $URL: $
 * $Id: $
 * **********************************************************************************
 * <p>
 * Author: David P. Bauer, dbauer1@udayton.edu
 * <p>
 * Copyright (c) 2016, University of Dayton
 * <p>
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.opensource.org/licenses/ECL-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.lessonbuildertool;

import java.io.Serializable;

public class ChecklistItemStatusImpl implements ChecklistItemStatus, Serializable {

    private static final long serialVersionUID = 1L;

    private long checklistId; // ID of the Checklist to which the checklist item belongs
    private long checklistItemId; // ID of the Checklist Item to which this belongs
    private String owner; // The user associated with this checklist item
    private boolean done; // Has this item been completed?

    public ChecklistItemStatusImpl() {
    }

    public ChecklistItemStatusImpl(long checklistId, long checklistItemId, String owner) {
        this.checklistId = checklistId;
        this.checklistItemId = checklistItemId;
        this.owner = owner;
        this.done = false;
    }

    public ChecklistItemStatusImpl(long checklistId, long checklistItemId, String owner, boolean done) {
        this.checklistId = checklistId;
        this.checklistItemId = checklistItemId;
        this.owner = owner;
        this.done = done;
    }

    public long getChecklistId() {
        return checklistId;
    }

    public void setChecklistId(long checklistId) {
        this.checklistId = checklistId;
    }

    public long getChecklistItemId() {
        return checklistItemId;
    }

    public void setChecklistItemId(long checklistItemId) {
        this.checklistItemId = checklistItemId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}