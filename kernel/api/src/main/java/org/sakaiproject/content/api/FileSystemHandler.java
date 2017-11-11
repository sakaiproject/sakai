/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
