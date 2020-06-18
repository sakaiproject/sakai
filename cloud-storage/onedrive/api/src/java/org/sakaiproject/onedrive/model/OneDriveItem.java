/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
package org.sakaiproject.onedrive.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class OneDriveItem {

    @JsonProperty("id")
    private String oneDriveItemId;

    private String name;
    
    private Integer size;
	
    @JsonProperty(value = "@microsoft.graph.downloadUrl")//this is always public
    //@JsonProperty(value = "webUrl")//this checks against onedrive permissions
    private String downloadUrl;
    
    private OneDriveFolder folder;
    private OneDriveFile file;

    @JsonProperty(value = "parentReference")
    private OneDriveParent parent;

    public boolean isFolder() {
        return folder != null;
    }
    public boolean hasChildren() {
        return isFolder() && folder.childCount != 0;
    }
	
    private int depth = 0;
    private boolean expanded = false;

    @Override
    public boolean equals(Object obj) {
        boolean retVal = false;
        if (obj instanceof OneDriveItem){
            OneDriveItem ptr = (OneDriveItem) obj;
            return this.oneDriveItemId.equals(ptr.getOneDriveItemId());
        }
        return retVal;
    }
}
