/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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
package org.sakaiproject.user.detail;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.CandidateDetailProvider;
import org.sakaiproject.user.api.User;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This takes some values that are reported back and maps them to other values.
 * This is mainly just a proxy, but it allows deployments to have data stored in one form but manipulate to another
 * value based on a simple map.
 */
public class MappingDetailProvider implements CandidateDetailProvider {

    private CandidateDetailProvider wrapped;
    private Map<String, String> map;

    public MappingDetailProvider(CandidateDetailProvider wrapped, Map<String, String> map) {
        this.wrapped = Objects.requireNonNull(wrapped);
        this.map = Objects.requireNonNull(map);
    }

    @Override
    public Optional<String> getCandidateID(User user, Site site) {
        return wrapped.getCandidateID(user, site);
    }

    @Override
    public boolean useInstitutionalAnonymousId(Site site) {
        return wrapped.useInstitutionalAnonymousId(site);
    }

    @Override
    public Optional<List<String>> getAdditionalNotes(User user, Site site) {
        // Map our known additional notes.
        return wrapped.getAdditionalNotes(user, site).map(
                l -> l.stream().map(v -> map.containsKey(v)?map.get(v):v).collect(Collectors.toList())
        );
    }

    @Override
    public boolean isAdditionalNotesEnabled(Site site) {
        return wrapped.isAdditionalNotesEnabled(site);
    }
	
	@Override
	public Optional<String> getInstitutionalNumericId(User user, Site site)
	{
		return wrapped.getInstitutionalNumericId(user, site);
	}
	
	@Override
	public Optional<String> getInstitutionalNumericIdIgnoringCandidatePermissions(User candidate, Site site)
	{
		return wrapped.getInstitutionalNumericIdIgnoringCandidatePermissions(candidate, site);
	}
	
	@Override
	public boolean isInstitutionalNumericIdEnabled(Site site)
	{
		return wrapped.isInstitutionalNumericIdEnabled(site);
	}

}

