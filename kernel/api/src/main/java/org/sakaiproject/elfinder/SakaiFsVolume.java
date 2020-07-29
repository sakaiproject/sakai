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
