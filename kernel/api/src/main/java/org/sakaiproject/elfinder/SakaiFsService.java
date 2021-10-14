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
import java.util.Map;

import org.sakaiproject.content.api.ContentHostingService;

/**
 * This interface matches that of elfinders FsService
 * By providing this interface we don't have include ElFinder jar and it serves as an
 * abstraction for Sakai services
 */
public interface SakaiFsService {
    void registerToolVolume(ToolFsVolumeFactory volumeFactory);

    Map<String, ToolFsVolumeFactory> getToolVolumes();

    SakaiFsItem fromHash(String hash) throws IOException;

    String getHash(SakaiFsItem item) throws IOException;

    String getVolumeId(SakaiFsVolume volume);

    SakaiFsVolume[] getVolumes();

    SakaiFsVolume getSiteVolume(String siteId);

    ContentHostingService getContentHostingService();

    /**
     * find files by name pattern, this provides a simple recursively iteration based method
     * lucene engines can be introduced to improve it!
     * This searches across all volumes.
     *
     * @param filter The filter to apply to select files.
     * @return A collection of files that match  the filter and gave the root as a parent.
     */
    // TODO: bad designs: FsItemEx should not used here top level interfaces should only know FsItem instead of FsItemEx
    SakaiFsItem[] find(SakaiFsItemFilter filter);
}
