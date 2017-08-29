/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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
package org.sakaiproject.elfinder.sakai.content;

import cn.bluejoe.elfinder.service.FsItem;
import cn.bluejoe.elfinder.service.FsVolume;

/**
 * Created by buckett on 08/07/15.
 */
public class ContentFsItem implements FsItem {

    private final FsVolume fsVolume;
    private String id;

    public ContentFsItem(FsVolume fsVolume, String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID can't be null");
        }
        this.fsVolume = fsVolume;
        this.id = id;
    }

    @Override
    public FsVolume getVolume() {
        return fsVolume;
    }

    public String getId() {
        return id;
    }

	public void setId(String id) {
		this.id = id;
	}
}
