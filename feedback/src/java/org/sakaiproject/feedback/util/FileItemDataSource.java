/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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
package org.sakaiproject.feedback.util;

import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.io.FilenameUtils;

import jakarta.activation.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

/**
 * Allows FileItems to be used as DataSources for attachments.
 * <p>
 * The content is copied when this is constructed. EmailService sends asynchronously, so the
 * message is usually written after the request has finished and RequestFilter has deleted the
 * upload's temp file. Streaming from the FileItem at that point fails with NoSuchFileException.
 */
public class FileItemDataSource implements DataSource {

    private final byte[] content;
    private final String contentType;
    private final String name;

    public FileItemDataSource(FileItem<?> fileItem) {
        try (InputStream in = fileItem.getInputStream()) {
            this.content = in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded file " + fileItem.getName(), e);
        }
        this.contentType = fileItem.getContentType();
        String fileName = fileItem.getName();
        this.name = fileName != null ? FilenameUtils.getName(fileName) : null;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new IOException("getOutputStream() isn't supported");
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getName() {
        return name;
    }
}
