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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

/** Parses uploads dispatched through the portal without relying on the portal servlet's Part configuration. */
@Slf4j
public class SakaiMultipartResolver implements MultipartResolver {
    private final long uploadMax;

    public SakaiMultipartResolver(long uploadMax) {
        long ceiling = Long.getLong(RequestFilter.SYSTEM_UPLOAD_CEILING,
                Long.getLong(RequestFilter.SYSTEM_UPLOAD_MAX, 1L)) * 1024L * 1024L;
        this.uploadMax = Math.min(uploadMax, ceiling);
    }

    @Override
    public boolean isMultipart(HttpServletRequest request) {
        return JakartaServletFileUpload.isMultipartContent(request);
    }

    @Override
    public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) {
        DiskFileItemFactory.Builder factory = DiskFileItemFactory.builder();
        String directory = System.getProperty(RequestFilter.SYSTEM_UPLOAD_DIR);
        if (directory != null) {
            factory.setPath(Path.of(directory));
        }
        JakartaServletFileUpload<DiskFileItem, DiskFileItemFactory> upload = new JakartaServletFileUpload<>(factory.get());
        Charset encoding = request.getCharacterEncoding() == null ? StandardCharsets.UTF_8
                : Charset.forName(request.getCharacterEncoding());
        upload.setHeaderCharset(encoding);
        upload.setMaxFileSize(uploadMax);
        upload.setMaxSize(uploadMax + 64L * 1024L);
        MultiValueMap<String, MultipartFile> files = new LinkedMultiValueMap<>();
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        Map<String, String> contentTypes = new HashMap<>();
        List<DiskFileItem> items = null;
        try {
            items = upload.parseRequest(request);
            for (DiskFileItem item : items) {
                if (item.isFormField()) {
                    parameters.add(item.getFieldName(), item.getString(encoding));
                    contentTypes.put(item.getFieldName(), item.getContentType());
                    item.delete();
                } else {
                    files.add(item.getFieldName(), new UploadedFile(item));
                }
            }
            Map<String, String[]> values = new HashMap<>();
            parameters.forEach((name, entries) -> values.put(name, entries.toArray(new String[0])));
            return new DefaultMultipartHttpServletRequest(request, files, values, contentTypes);
        } catch (IOException e) {
            if (items != null) {
                for (DiskFileItem item : items) {
                    delete(item);
                }
            }
            throw new MultipartException("Unable to parse uploaded form", e);
        }
    }

    @Override
    public void cleanupMultipart(MultipartHttpServletRequest request) {
        request.getMultiFileMap().values().forEach(files -> files.forEach(file -> {
            if (file instanceof UploadedFile uploaded) {
                delete(uploaded.item);
            }
        }));
    }

    private static void delete(DiskFileItem item) {
        try {
            item.delete();
        } catch (IOException e) {
            log.warn("Unable to delete temporary upload for field {}", item.getFieldName(), e);
        }
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
