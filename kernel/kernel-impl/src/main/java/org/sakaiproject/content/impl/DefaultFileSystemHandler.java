package org.sakaiproject.content.impl;

import org.sakaiproject.content.api.FileSystemHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.springframework.util.FileCopyUtils;

/**
 * The default implementation of FileSystemHandler, targeting local disk.
 *
 * This class read and writes content files to local filesystem paths.
 */
public class DefaultFileSystemHandler implements FileSystemHandler {
    private boolean useIdForFilePath = false;

    /**
     * Default constructor.
     */
    public DefaultFileSystemHandler() {
    }

    /**
     * Whether to use the id for the file path.
     */
    public void setUseIdForFilePath(boolean useIdForFilePath){
        this.useIdForFilePath = useIdForFilePath;
    }

    /**
     * A Helper method to get the File object for the parameters.
     * This method will look at the property useIdForFilePath to see if the
     * id must be used in the file path.
     * 
     * @param id The id of the resource.
     * @param root The root of the storage.
     * @param filePath The path to save the file to.
     * @return The File object.
     */
    private File getFile(String id, String root, String filePath){
        if (useIdForFilePath) {
            return new File(root, id);
        } else {
            return new File(root, filePath);
        }
    }

    @Override
    public InputStream getInputStream(String id, String root, String filePath) throws IOException {
        return new FileInputStream(getFile(id, root, filePath));
    }

    @Override
    public long saveInputStream(String id, String root, String filePath, InputStream stream) throws IOException {
        // Do not create the files for resources with zero length bodies
        if ((stream == null)) {
            return 0L;
        }

        // form the file name
        File file = getFile(id, root, filePath);

        // delete the old
        if (file.exists()) {
            file.delete();
        }

        // add the new
        // make sure all directories are there
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }

        // write the file
        return FileCopyUtils.copy(stream, new FileOutputStream(file));
    }

    @Override
    public boolean delete(String id, String root, String filePath){
        File file = getFile(id, root, filePath);

        // delete
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

	@Override
	public URI getAssetDirectLink(String id, String root, String filePath) throws IOException {
		return null;
	}
}
