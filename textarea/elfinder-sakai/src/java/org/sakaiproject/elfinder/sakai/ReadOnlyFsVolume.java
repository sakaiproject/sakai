/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.elfinder.sakai;

import cn.bluejoe.elfinder.service.FsItem;
import cn.bluejoe.elfinder.service.FsVolume;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;

/**
 * A FsVolume that doesn't allow changes, this can be extended by volumes that aren't like normal filesystems.
 */
public abstract class ReadOnlyFsVolume  implements FsVolume {
    @Override
    public void createFile(FsItem fsi) throws IOException {
        throw new UnsupportedOperationException("Can't create files here.");
    }

    @Override
    public void createFolder(FsItem fsi) throws IOException {
        throw new UnsupportedOperationException("Can't create folders here.");
    }

    @Override
    public void deleteFile(FsItem fsi) throws IOException {
        throw new UnsupportedOperationException("Can't delete files here.");
    }

    @Override
    public void deleteFolder(FsItem fsi) throws IOException {
        throw new UnsupportedOperationException("Can't delete files here.");
    }

    @Override
    public void writeStream(FsItem fsi, InputStream is) throws IOException {
        throw new UnsupportedOperationException("Can't write to files here.");
    }

    @Override
    public void rename(FsItem src, FsItem dst) throws IOException {
        throw new UnsupportedOperationException("Can't rename here.");
    }

    @Override
    public void filterOptions(FsItem item, Map<String, Object> map) {
        map.put("disabled", Arrays.asList(new String[]{"create", "rm", "duplicate", "rename", "mkfile", "mkdir", "search", "zipdl"}));
        // Disable chunked uploads.
        map.put("uploadMaxConn", "-1");
    }

}
