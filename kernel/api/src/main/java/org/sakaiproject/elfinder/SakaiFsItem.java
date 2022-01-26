/**
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
package org.sakaiproject.elfinder;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
public class SakaiFsItem {
    @Getter @Setter private String id;
    @EqualsAndHashCode.Exclude @Getter private String title;
    @Getter private SakaiFsVolume volume;
    @Getter private FsType type;

    public SakaiFsItem(SakaiFsVolume volume, FsType type) {
        this(null, null, volume, type);
    }

    public SakaiFsItem(String id, SakaiFsVolume volume, FsType type) {
        this(id, null, volume, type);
    }

    public SakaiFsItem(String id, String title, SakaiFsVolume volume, FsType type) {
        this.id = id;
        this.title = title;
        this.volume = volume;
        this.type = type;
    }
}
