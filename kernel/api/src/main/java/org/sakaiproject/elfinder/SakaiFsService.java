package org.sakaiproject.elfinder;

import java.io.IOException;
import java.util.Map;

import org.sakaiproject.content.api.ContentHostingService;

/**
 * This interface matches that of elfinders FsService
 * By providing this interface we don't have include ElFinder jar and it serves as an
 * abstraction for Sakai services
 */
public interface SakaiFsService {
    void registerToolVolume(ToolFsVolumeFactory volumeFactory);

    Map<String, ToolFsVolumeFactory> getToolVolumes();

    SakaiFsItem fromHash(String hash) throws IOException;

    String getHash(SakaiFsItem item) throws IOException;

    String getVolumeId(SakaiFsVolume volume);

    SakaiFsVolume[] getVolumes();

    SakaiFsVolume getSiteVolume(String siteId);

    ContentHostingService getContentHostingService();

    /**
     * find files by name pattern, this provides a simple recursively iteration based method
     * lucene engines can be introduced to improve it!
     * This searches across all volumes.
     *
     * @param filter The filter to apply to select files.
     * @return A collection of files that match  the filter and gave the root as a parent.
     */
    // TODO: bad designs: FsItemEx should not used here top level interfaces should only know FsItem instead of FsItemEx
    SakaiFsItem[] find(SakaiFsItemFilter filter);
}
