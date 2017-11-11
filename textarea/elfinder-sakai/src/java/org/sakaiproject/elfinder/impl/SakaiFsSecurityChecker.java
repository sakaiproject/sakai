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
package org.sakaiproject.elfinder.impl;

import cn.bluejoe.elfinder.service.FsItem;
import cn.bluejoe.elfinder.service.FsSecurityChecker;
import cn.bluejoe.elfinder.service.FsService;
import org.sakaiproject.elfinder.sakai.ReadOnlyFsVolume;
import org.sakaiproject.elfinder.sakai.SakaiFsService;
import org.sakaiproject.elfinder.sakai.SiteVolume;
import org.sakaiproject.site.api.Site;

import java.io.IOException;
import java.util.Random;

/**
 * This just deals with if a item is writable by basically delegating through to the volume.
 * @see SiteVolume#isWriteable(FsItem)
 */
public class SakaiFsSecurityChecker implements FsSecurityChecker {

    @Override
    public boolean isLocked(FsService fsService, FsItem fsi) throws IOException {
        return false;
    }

    @Override
    public boolean isReadable(FsService fsService, FsItem fsi) throws IOException {
        // Filtering should be done by the volumes.
        return true;
    }

    @Override
    public boolean isWritable(FsService fsService, FsItem fsi) throws IOException {
        if (fsi.getVolume() instanceof ReadOnlyFsVolume) {
            return false;
        } else {
            if (fsi.getVolume() instanceof SiteVolume) {
                SiteVolume siteVolume = (SiteVolume) fsi.getVolume();
                return siteVolume.isWriteable(fsi);
            }
           return true;
        }
    }
}
