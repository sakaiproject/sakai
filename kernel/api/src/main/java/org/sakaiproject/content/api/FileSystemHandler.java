package org.sakaiproject.content.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * This is the api for reading and writing files to some file system.
 *
 */
public interface FileSystemHandler {

    /**
     * Retrieve a direct link to the asset.
     * 
     * @param id The id of the resource. Will not be null or empty.
     * @param root The root of the storage. Could be null or empty.
     * @param filePath The path to the file. Will not be null or empty.
     * @return A URI to retrieve the asset.
     * @throws IOException If the asset cannot be found.
     */
    public URI getAssetDirectLink(String id, String root, String filePath) throws IOException;

    /**
     * Retrieves an input stream from the file.
     * 
     * @param id The id of the resource. Will not be null or empty.
     * @param root The root of the storage. Could be null or empty.
     * @param filePath The path to the file. Will not be null or empty.
     * @return The valid input stream. Must not be null.
     * @throws IOException If the stream could not be created.
     */
    public InputStream getInputStream(String id, String root, String filePath) throws IOException;

    /**
     * Save the file from the input stream to the path and return the content size.
     * 
     * @param id The id of the resource. Will not be null or empty.
     * @param root The root of the storage. Could be null or empty.
     * @param filePath The path to save the file to. Will not be null or empty.
     * @param stream The stream to read the file from.
     * @return The content size.
     */
    public long saveInputStream(String id, String root, String filePath, InputStream stream) throws IOException;

    /**
     * Delete the file from the path.
     * 
     * @param id The id of the resource. Will not be null or empty.
     * @param root The root of the storage. Could be null or empty.
     * @param filePath The path to delete. Will not be null or empty.
     * @return If the path was deleted.
     */
    public boolean delete(String id, String root, String filePath);
}
