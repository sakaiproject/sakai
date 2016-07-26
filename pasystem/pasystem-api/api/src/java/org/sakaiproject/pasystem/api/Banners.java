/**********************************************************************************
 *
 * Copyright (c) 2015 The Sakai Foundation
 *
 * Original developers:
 *
 *   New York University
 *   Payten Giles
 *   Mark Triggs
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.pasystem.api;

import java.util.List;
import java.util.Optional;

/**
 * The interface for the banners sub-service.
 */
public interface Banners extends Acknowledger {

    /**
     * A list of banners that should be shown for a given server and user.
     */
    public List<Banner> getRelevantBanners(String serverId, String userId);

    public String createBanner(Banner banner);

    public void updateBanner(Banner banner);

    public void deleteBanner(String uuid);

    public List<Banner> getAll();

    /**
     * Forget all acknowledgements for the current user.
     */
    public void clearTemporaryDismissedForUser(String userId);

    public Optional<Banner> getForId(String uuid);
}
    
