/**********************************************************************************
 *
 * Copyright (c) 2006, 2008, 2013 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.api;

import java.util.Arrays;
import java.util.List;

public interface FileConversionService {

    public static final List<String> DEFAULT_TYPES =
        Arrays.asList(ContentHostingService.DOCX_MIMETYPE
            , ContentHostingService.DOC_MIMETYPE
            , ContentHostingService.ODT_MIMETYPE
            , ContentHostingService.ODP_MIMETYPE
            , ContentHostingService.PPT_MIMETYPE
            , ContentHostingService.PPTX_MIMETYPE);

    /**
     * Can this service convert from this type to PDF? If the service is not enabled, this
     * will always return false. Otherwise it'll check against the configured list of supported
     * types.
     *
     * @param The full mimetype of the type we're enquiring about
     */
    boolean canConvert(String fromType);

    /**
     * Queue the conversion. The conversion won't happen right away, it's all threaded, so
     * there's be a delay before the worker pool picks it up.
     *
     * @param The content ref we want to convert
     */
    void submit(String ref);

    /**
     * Start the queue processing threads. If the service isn't enabled, nothing will happen.
     */
    void startIfEnabled();
}
