/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.lessonbuildertool;

import java.io.Serializable;

/**
 * Composite ID for checklist item status.
 */
public class ChecklistItemStatusId implements Serializable {

    private long checklistId; // ID of the Checklist to which the checklist item belongs
    private long checklistItemId; // ID of the Checklist Item to which this belongs
    private String owner;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChecklistItemStatusId that = (ChecklistItemStatusId) o;

        if (checklistId != that.checklistId) return false;
        if (checklistItemId != that.checklistItemId) return false;
        return owner != null ? owner.equals(that.owner) : that.owner == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (checklistId ^ (checklistId >>> 32));
        result = 31 * result + (int) (checklistItemId ^ (checklistItemId >>> 32));
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        return result;
    }
}
