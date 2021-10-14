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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.sakaiproject.exception.PermissionException;

public interface SakaiFsVolume {
    void createFile(SakaiFsItem fsi) throws IOException;

    void createFolder(SakaiFsItem fsi) throws IOException;

    void deleteFile(SakaiFsItem fsi) throws IOException;

    void deleteFolder(SakaiFsItem fsi) throws IOException;

    boolean exists(SakaiFsItem newFile);

    SakaiFsItem fromPath(String relativePath);

    String getDimensions(SakaiFsItem fsi);

    long getLastModified(SakaiFsItem fsi);

    String getMimeType(SakaiFsItem fsi);

    String getName();

    String getName(SakaiFsItem fsi);

    SakaiFsItem getParent(SakaiFsItem fsi);

    String getPath(SakaiFsItem fsi) throws IOException;

    SakaiFsItem getRoot();

    long getSize(SakaiFsItem fsi) throws IOException;

    String getThumbnailFileName(SakaiFsItem fsi);

    boolean hasChildFolder(SakaiFsItem fsi);

    boolean isFolder(SakaiFsItem fsi);

    boolean isRoot(SakaiFsItem fsi);

    SakaiFsItem[] listChildren(SakaiFsItem fsi) throws PermissionException;

    InputStream openInputStream(SakaiFsItem fsi) throws IOException;

    void writeStream(SakaiFsItem f, InputStream is) throws IOException;

    void rename(SakaiFsItem src, SakaiFsItem dst) throws IOException;

    String getURL(SakaiFsItem f);

    void filterOptions(SakaiFsItem f, Map<String, Object> map);
}
