package org.sakaiproject.elfinder.sakai;

/**
 * This is a factory that tools need to implement which will be called by the service when a new
 * instance of the FsVolume is required for a site. This needs to be high performance as it will be called multiple
 * times in a request.
 */
public interface SiteVolumeFactory {

    String getPrefix();

    SiteVolume getVolume(SakaiFsService sakaiFsService, String siteId);

    String getToolId();

}
