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

import java.util.Comparator;

public class GoogleDriveItemComparator implements Comparator<GoogleDriveItem> {
    @Override
    public int compare(GoogleDriveItem o1, GoogleDriveItem o2) {
        String o1Name = o1.getGoogleDriveItemId();
        String o2Name = o2.getGoogleDriveItemId();
        if(!o1.isFolder()){
            o1Name = o1.getParentId() + o1Name;
        }
        if(!o2.isFolder()){
            o2Name = o2.getParentId() + o2Name;
        }
        return o1Name.compareTo(o2Name);
    }
}
