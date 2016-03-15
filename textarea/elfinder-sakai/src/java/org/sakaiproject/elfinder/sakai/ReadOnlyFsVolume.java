package org.sakaiproject.elfinder.sakai;

import cn.bluejoe.elfinder.service.FsItem;
import cn.bluejoe.elfinder.service.FsVolume;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A FsVolume that doesn't allow changes, this can be extended by volumes that aren't like normal filesystems.
 */
public abstract class ReadOnlyFsVolume  implements FsVolume {
    @Override
    public void createFile(FsItem fsi) throws IOException {
        throw new UnsupportedOperationException("Can't create files here.");
    }

    @Override
    public void createFolder(FsItem fsi) throws IOException {
        throw new UnsupportedOperationException("Can't create folders here.");
    }

    @Override
    public void deleteFile(FsItem fsi) throws IOException {
        throw new UnsupportedOperationException("Can't delete files here.");
    }

    @Override
    public void deleteFolder(FsItem fsi) throws IOException {
        throw new UnsupportedOperationException("Can't delete files here.");
    }

    @Override
    public void writeStream(FsItem fsi, InputStream is) throws IOException {
        throw new UnsupportedOperationException("Can't write to files here.");
    }

    @Override
    public void rename(FsItem src, FsItem dst) throws IOException {
        throw new UnsupportedOperationException("Can't rename here.");
    }

}
