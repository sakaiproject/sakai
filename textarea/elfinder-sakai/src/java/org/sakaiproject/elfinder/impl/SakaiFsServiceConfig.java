package org.sakaiproject.elfinder.impl;

import cn.bluejoe.elfinder.service.FsServiceConfig;

/**
 * Simple implementation of the config. In the future this should hook up to ServerConfigurationService.
 */
public class SakaiFsServiceConfig implements FsServiceConfig {
    @Override
    public int getTmbWidth() {
        return 80;
    }
}
