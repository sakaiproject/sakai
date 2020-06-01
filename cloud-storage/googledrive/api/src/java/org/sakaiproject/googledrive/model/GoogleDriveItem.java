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
package org.sakaiproject.googledrive.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
public class GoogleDriveItem {

    private String googleDriveItemId;
    private String name;    
    private Long size;
    private String downloadUrl;
    private String viewUrl;
    private String mimeType;
    private String parentId;
    private String icon;
    private String thumbnail;

    private boolean folder = false;
    public boolean isFolder() {
        return folder;
    }
    private boolean children = false;
    public boolean hasChildren() {
        return isFolder() && children;
    }

    private int depth = 0;
    private boolean expanded = false;

    @Override
    public boolean equals(Object obj) {
        boolean retVal = false;
        if (obj instanceof GoogleDriveItem){
            GoogleDriveItem gdi = (GoogleDriveItem) obj;
            return this.googleDriveItemId.equals(gdi.getGoogleDriveItemId());
        }
        return retVal;
    }
}
