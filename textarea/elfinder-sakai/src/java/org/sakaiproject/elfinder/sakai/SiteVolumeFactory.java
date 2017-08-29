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
package org.sakaiproject.elfinder.sakai;

/**
 * This is a factory that tools need to implement which will be called by the service when a new
 * instance of the FsVolume is required for a site. This needs to be high performance as it will be called multiple
 * times in a request.
 */
public interface SiteVolumeFactory {

    String getPrefix();

    SiteVolume getVolume(SakaiFsService sakaiFsService, String siteId);

    String getToolId();

}
