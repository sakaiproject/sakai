/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.data.dao.assessment;

import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTagIfc;

import java.io.IOException;

/**
 * A binding between an {@link ItemData} and a {@code TagService}-managed {@code Tag}
 */
public class ItemTag implements ItemTagIfc {

    private static final long serialVersionUID = 7526471155622776147L;
    private Long id;
    private ItemDataIfc item;
    private String tagId;
    private String tagLabel;
    private String tagCollectionId;
    private String tagCollectionName;

    public ItemTag() {
    }

    public ItemTag(ItemDataIfc item, String tagId, String tagLabel, String tagCollectionId, String tagCollectionName) {
        this.item = item;
        this.tagId = tagId;
        this.tagLabel = tagLabel;
        this.tagCollectionId = tagCollectionId;
        this.tagCollectionName = tagCollectionName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ItemDataIfc getItem() {
        return item;
    }

    public void setItem(ItemDataIfc item) {
        this.item = item;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getTagLabel() {
        return tagLabel;
    }

    public void setTagLabel(String tagLabel) {
        this.tagLabel = tagLabel;
    }

    public String getTagCollectionId() {
        return tagCollectionId;
    }

    public void setTagCollectionId(String tagCollectionId) {
        this.tagCollectionId = tagCollectionId;
    }

    public String getTagCollectionName() {
        return tagCollectionName;
    }

    public void setTagCollectionName(String tagCollectionName) {
        this.tagCollectionName = tagCollectionName;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
    }
}
