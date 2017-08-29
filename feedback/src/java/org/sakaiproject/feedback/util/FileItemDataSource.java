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

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Proxy to allow FileItems to be used as DataSources for attachments.
 */
public class FileItemDataSource implements DataSource {

    private FileItem fileItem;

    public FileItemDataSource(FileItem fileItem) {
        this.fileItem = fileItem;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return fileItem.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new IOException("getOutputStream() isn't supported");
    }

    @Override
    public String getContentType() {
        return fileItem.getContentType();
    }

    @Override
    public String getName() {
        String fileName = fileItem.getName();
        if (fileName != null) {
            fileName = FilenameUtils.getName(fileName);
        }
        return fileName;
    }
}
