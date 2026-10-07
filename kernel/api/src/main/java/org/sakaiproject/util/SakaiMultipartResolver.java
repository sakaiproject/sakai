/*
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload2.core.DiskFileItem;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

/** Adapts files already parsed by Sakai's RequestFilter to Spring MVC. */
public class SakaiMultipartResolver implements MultipartResolver {
    private final long uploadMax;

    public SakaiMultipartResolver() {
        this(Long.MAX_VALUE);
    }

    public SakaiMultipartResolver(long uploadMax) {
        this.uploadMax = uploadMax;
    }

    @Override
    public boolean isMultipart(HttpServletRequest request) {
        return request.getAttribute(RequestFilter.ATTR_UPLOADS_DONE) != null;
    }

    @Override
    public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) {
        if (request.getAttribute("upload.exception") instanceof Exception exception) {
            throw new MultipartException("Sakai upload processing failed", exception);
        }
        MultiValueMap<String, MultipartFile> files = new LinkedMultiValueMap<>();
        long totalSize = 0;
        if (request.getAttribute(RequestFilter.ATTR_UPLOAD_FILES) instanceof DiskFileItem[] items) {
            for (DiskFileItem item : items) {
                totalSize += item.getSize();
                if (totalSize > uploadMax) {
                    throw new MaxUploadSizeExceededException(uploadMax);
                }
                files.add(item.getFieldName(), new UploadedFile(item));
            }
        }
        return new DefaultMultipartHttpServletRequest(request, files, Collections.emptyMap(), Collections.emptyMap());
    }

    @Override
    public void cleanupMultipart(MultipartHttpServletRequest request) {
        // RequestFilter owns the uploaded files and deletes them at the end of the request.
    }

    private static class UploadedFile implements MultipartFile {
        private final DiskFileItem item;

        private UploadedFile(DiskFileItem item) { this.item = item; }
        @Override public String getName() { return item.getFieldName(); }
        @Override public String getOriginalFilename() { return item.getName(); }
        @Override public String getContentType() { return item.getContentType(); }
        @Override public boolean isEmpty() { return item.getSize() == 0; }
        @Override public long getSize() { return item.getSize(); }
        @Override public byte[] getBytes() throws IOException { return item.get(); }
        @Override public InputStream getInputStream() throws IOException { return item.getInputStream(); }
        @Override public void transferTo(File destination) throws IOException { item.write(destination.toPath()); }
    }
}
