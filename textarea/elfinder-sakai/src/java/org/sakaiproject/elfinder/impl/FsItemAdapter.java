/*
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.elfinder.impl;

import org.sakaiproject.elfinder.SakaiFsItem;

import cn.bluejoe.elfinder.service.FsItem;
import cn.bluejoe.elfinder.service.FsVolume;
import lombok.Getter;

public class FsItemAdapter implements FsItem {

    @Getter private final SakaiFsItem sakaiFsItem;

    public FsItemAdapter(SakaiFsItem sakaiFsItem) {
        this.sakaiFsItem = sakaiFsItem;
    }

    @Override
    public FsVolume getVolume() {
        return (sakaiFsItem == null) ? null : new FsVolumeAdapter(sakaiFsItem.getVolume());
    }

    public String getId() {
        return sakaiFsItem.getId();
    }

    public void setId(String id) {
        sakaiFsItem.setId(id);
    }

    public String getTitle() {
        return sakaiFsItem.getTitle();
    }
}
