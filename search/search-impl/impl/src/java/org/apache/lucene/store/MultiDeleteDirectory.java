package org.apache.lucene.store;

import java.io.IOException;
import java.util.List;

/**
 * Acts as a marker interface for directories that can delete multiple files at one go.
 * Basically here untill Lucene will add the method to the <code>Directory</code> class.
 * <p/>
 * Mainly used for performance reasons, especially for <code>JdbcDirectory</code>
 *
 * @author kimchy
 */
public interface MultiDeleteDirectory {

    /**
     * Deletes the given file names. Returns a list of the ones that could not be deleted.
     */
    List deleteFiles(final List names) throws IOException;
}
