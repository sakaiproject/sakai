package org.sakaiproject.elfinder.impl;

import java.io.IOException;
import java.util.Arrays;

import org.sakaiproject.elfinder.SakaiFsItem;
import org.sakaiproject.elfinder.SakaiFsService;
import org.sakaiproject.elfinder.SakaiFsVolume;

import cn.bluejoe.elfinder.controller.executor.FsItemEx;
import cn.bluejoe.elfinder.service.FsItem;
import cn.bluejoe.elfinder.service.FsItemFilter;
import cn.bluejoe.elfinder.service.FsSecurityChecker;
import cn.bluejoe.elfinder.service.FsService;
import cn.bluejoe.elfinder.service.FsServiceConfig;
import cn.bluejoe.elfinder.service.FsVolume;
import lombok.Getter;
import lombok.Setter;

public class FsServiceAdapter implements FsService {

    @Setter private SakaiFsService sakaiFsService;
    @Getter @Setter private FsSecurityChecker securityChecker;
    @Setter private FsServiceConfig fsServiceConfig;

    @Override
    public FsItem fromHash(String hash) throws IOException {
        return new FsItemAdapter(sakaiFsService.fromHash(hash));
    }

    @Override
    public String getHash(FsItem fsi) throws IOException {
        SakaiFsItem sakaiFsItem = ((FsItemAdapter) fsi).getSakaiFsItem();
        return sakaiFsService.getHash(sakaiFsItem);
    }

    @Override
    public String getVolumeId(FsVolume fsVolume) {
        SakaiFsVolume volume = ((FsVolumeAdapter) fsVolume).getSakaiFsVolume();
        return sakaiFsService.getVolumeId(volume);
    }

    @Override
    public FsVolume[] getVolumes() {
        return Arrays.stream(sakaiFsService.getVolumes()).map(FsVolumeAdapter::new).toArray(FsVolumeAdapter[]::new);
    }

    @Override
    public FsServiceConfig getServiceConfig() {
        return fsServiceConfig;
    }

    @Override
    public FsItemEx[] find(FsItemFilter filter) {
        return new FsItemEx[0];
    }
}
