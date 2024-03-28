/*
 * Copyright (c) 2003-2023 The Apereo Foundation
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
package org.sakaiproject.scorm.service.sakai.impl.mocks;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.elfinder.SakaiFsItem;
import org.sakaiproject.elfinder.SakaiFsItemFilter;
import org.sakaiproject.elfinder.SakaiFsService;
import org.sakaiproject.elfinder.SakaiFsVolume;
import org.sakaiproject.elfinder.ToolFsVolumeFactory;

/**
 *
 * @author bjones86
 */
public class MockSakaiFsService implements SakaiFsService
{
    @Override
    public SakaiFsItem[] find( SakaiFsItemFilter filter )
    {
        return new SakaiFsItem[]{};
    }

    @Override
    public SakaiFsItem fromHash( String hash ) throws IOException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ContentHostingService getContentHostingService()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String getHash( SakaiFsItem item ) throws IOException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public SakaiFsVolume getSiteVolume( String siteId )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Map<String, ToolFsVolumeFactory> getToolVolumes()
    {
        return Collections.emptyMap();
    }

    @Override
    public String getVolumeId( SakaiFsVolume volume )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public SakaiFsVolume[] getVolumes()
    {
        return new SakaiFsVolume[]{};
    }

    @Override
    public void registerToolVolume( ToolFsVolumeFactory volumeFactory )
    {
    }
}
