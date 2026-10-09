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
package org.sakaiproject.lessonbuildertool.resolver;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.sakaiproject.util.SakaiMultipartResolver;

import jakarta.servlet.http.HttpServletRequest;

public class MultiCommonsMultipartResolver extends SakaiMultipartResolver {

    @Override
    public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
        MultipartHttpServletRequest multipartRequest = super.resolveMultipart(request);
        // RSF consumes getFileMap(), so retain every file submitted under the same field name.
        Map<String, MultipartFile> files = new LinkedHashMap<>();
        for (List<MultipartFile> fieldFiles : multipartRequest.getMultiFileMap().values()) {
            for (MultipartFile file : fieldFiles) {
                files.put(Integer.toString(files.size()), file);
            }
        }
        return new DefaultMultipartHttpServletRequest(multipartRequest, multipartRequest.getMultiFileMap(),
                Collections.emptyMap(), Collections.emptyMap()) {
            @Override
            public Map<String, MultipartFile> getFileMap() {
                return files;
            }
        };
    }
}
