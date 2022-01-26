/*
 * Copyright (c) 2003-2020 The Apereo Foundation
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
