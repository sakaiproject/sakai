/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.bean.author;


import java.io.Serializable;

public class ItemTagBean  implements Serializable {

    private final static long serialVersionUID = 4216587136245498157L;

    private String itemTagId;
    private String tagLabel;
    private String tagCollectionName;

    public ItemTagBean() {
    }

    public ItemTagBean(String itemTagId, String tagLabel, String tagCollectionName) {
        this.itemTagId = itemTagId;
        this.tagLabel = tagLabel;
        this.tagCollectionName = tagCollectionName;
    }

    public String getItemTagId() {
        return itemTagId;
    }

    public void setItemTagId(String itemTagId) {
        this.itemTagId = itemTagId;
    }

    public String getTagLabel() {
        return tagLabel;
    }

    public void setTagLabel(String tagLabel) {
        this.tagLabel = tagLabel;
    }

    public String getTagCollectionName() {
        return tagCollectionName;
    }

    public void setTagCollectionName(String tagCollectionName) {
        this.tagCollectionName = tagCollectionName;
    }
}
