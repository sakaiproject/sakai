/*
 * Copyright 2004-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.lucene.store.jdbc.handler;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.jdbc.JdbcDirectory;

/**
 * A file entry handler acts as a delegate to the {@link JdbcDirectory} for all "file" level
 * operations. Allows the {@link JdbcDirectory} to be abstracted from any specific implementation
 * details regarding a file entry, and have several different file entries for different files or
 * files groups.
 *
 * @author kimchy
 * @see org.apache.lucene.store.jdbc.JdbcDirectorySettings#registerFileEntrySettings(String, org.apache.lucene.store.jdbc.JdbcFileEntrySettings)
 */
public interface FileEntryHandler {

    /**
     * Called after the entry is created (during the {@link JdbcDirectory} initialization process.
     */
    void configure(JdbcDirectory jdbcDirectory);

    /**
     * Checks if the file exists for the given file name.
     *
     * @param name The name of the file
     * @return <code>true</code> of the file exists, <code>false</code> if it does not.
     * @throws IOException
     */
    boolean fileExists(final String name) throws IOException;

    /**
     * Returns the last modified date of the file.
     *
     * @param name The name of the file
     * @return The last modified date in millis.
     * @throws IOException
     */
    long fileModified(final String name) throws IOException;

    /**
     * Updates the last modified date of the file to the current time.
     *
     * @param name The name of the file
     * @throws IOException
     */
    void touchFile(final String name) throws IOException;

    /**
     * Deletes the given file name.
     *
     * @param name The name of the file to delete
     * @throws IOException
     */
    void deleteFile(final String name) throws IOException;

    /**
     * Deletes a list of files. It must be verified by the caller that
     * all files correlate to this file entry handler.
     *
     * @param names The list of file names to delete
     * @return List of files that were not deleted (or <code>null</code> if all were deleted)
     * @throws IOException
     */
    List deleteFiles(List names) throws IOException;

    /**
     * Renames the file entry from "from" to "to". The from entry is
     * the one that maps to the actual file entry handler.
     *
     * @param from The name to rename from
     * @param to The name to rename to
     * @throws IOException
     */
    void renameFile(final String from, final String to) throws IOException;

    /**
     * Returns the length of the file (in bytes).
     *
     * @param name The name of the file
     * @return The length of the file (in bytes)
     * @throws IOException
     */
    long fileLength(final String name) throws IOException;

    /**
     * Opens an <code>IndexInput</code> in order to read the file contents.
     *
     * @param name The name of the file
     * @return An <code>IndexInput</code> in order to read the file contents.
     * @throws IOException
     */
    IndexInput openInput(String name) throws IOException;

    /**
     * Creates an <code>IndexOutput</code> in order to write the file contents.
     *
     * @param name The name of the file
     * @return An <code>IndexOutput</code> to write the file contents
     * @throws IOException
     */
    IndexOutput createOutput(String name) throws IOException;

    /**
     * Closes the file entry handler.
     *
     * @throws IOException
     */
    void close() throws IOException;
}
