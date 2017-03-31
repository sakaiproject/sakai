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
