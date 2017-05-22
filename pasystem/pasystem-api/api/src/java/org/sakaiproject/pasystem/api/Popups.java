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
 * The interface for the popups sub-service.
 */
public interface Popups extends Acknowledger {

    public String createCampaign(Popup popup,
                                 TemplateStream templateContent,
                                 Optional<List<String>> assignToEids);

    public void updateCampaign(Popup popup,
                               Optional<TemplateStream> templateInput,
                               Optional<List<String>> assignToEids);

    public List<Popup> getAll();

    public String getPopupContent(final String uuid);

    public Optional<Popup> getForId(final String uuid);

    public List<String> getAssigneeEids(final String uuid);

    public boolean deleteCampaign(final String uuid);
}
