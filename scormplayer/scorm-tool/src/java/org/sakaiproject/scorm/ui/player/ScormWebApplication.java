/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.scorm.ui.player;

import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.ui.ContentPackageResourceReference;
import org.sakaiproject.scorm.ui.player.pages.ScormCompletionPage;
import org.sakaiproject.scorm.ui.player.pages.ScormPlayerPage;
import org.sakaiproject.wicket.protocol.http.SakaiWebApplication;

/**
 * @author bjones86
 */
public abstract class ScormWebApplication extends SakaiWebApplication
{
    @Getter @Setter
    private ScormResourceService resourceService;

    @Override
    public void init()
    {
        super.init();
        mountPage( "scormPlayerPage", ScormPlayerPage.class );
        mountPage( "scormCompletionPage", ScormCompletionPage.class );
        mountResource( "/contentpackages/resourceName/private/scorm/${resourceID}/${resourceName}", new ContentPackageResourceReference() );
    }
}
