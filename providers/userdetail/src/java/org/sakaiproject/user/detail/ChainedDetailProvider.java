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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.CandidateDetailProvider;
import org.sakaiproject.user.api.User;

/**
 * A candidate details provider that checks each of the providers in the chain until it gets a value.
 * For the boolean checks it does an OR and if any of the providers return true it will return true.
 */
@Slf4j
public class ChainedDetailProvider implements CandidateDetailProvider {

	private List<CandidateDetailProvider> providers;

	public void setProviders(List<CandidateDetailProvider> providers) {
		this.providers = providers;
	}

	public List<CandidateDetailProvider> getProviders() {
		if(providers == null)
			providers = new ArrayList<>();
		return providers;
	}
	public Optional<String> getCandidateID(User user, Site site) {
		if(site == null) {
			log.error("getCandidateID: Null site.");
			return Optional.empty();
		}
		for(CandidateDetailProvider provider : getProviders()) {
			String candidateID = provider.getCandidateID(user, site).orElse("");
			if(StringUtils.isNotBlank(candidateID)){
				return Optional.ofNullable(candidateID);
			}
		}
		return Optional.empty();
	}

	public boolean useInstitutionalAnonymousId(Site site) {
		for(CandidateDetailProvider provider : getProviders()) {
			if(provider.useInstitutionalAnonymousId(site)){
				return true;
			}
		}
		return false;
	}

	public Optional<List<String>> getAdditionalNotes(User user, Site site) {
		if(site == null) {
			log.error("getAdditionalNotes: Null site.");
			return Optional.empty();
		}
		for(CandidateDetailProvider provider : getProviders()) {
			Optional<List<String>> notes = provider.getAdditionalNotes(user, site);
			if(notes.isPresent()){
				return notes;
			}
		}
		return Optional.empty();
	}

	public boolean isAdditionalNotesEnabled(Site site) {
		for(CandidateDetailProvider provider : getProviders()) {
			if(provider.isAdditionalNotesEnabled(site)){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Optional<String> getInstitutionalNumericId(User user, Site site)
	{
		for (CandidateDetailProvider provider : getProviders())
		{
			String studentNumber = provider.getInstitutionalNumericId(user, site).orElse("");
			if (StringUtils.isNotBlank(studentNumber))
			{
				return Optional.of(studentNumber);
			}
		}
		
		return Optional.empty();
	}
	
	@Override
	public Optional<String> getInstitutionalNumericIdIgnoringCandidatePermissions(User candidate, Site site)
	{
		for (CandidateDetailProvider provider : getProviders())
		{
			String studentNumber = provider.getInstitutionalNumericIdIgnoringCandidatePermissions(candidate, site).orElse("");
			if (StringUtils.isNotBlank(studentNumber))
			{
				return Optional.of(studentNumber);
			}
		}
		
		return Optional.empty();
	}
	
	@Override
	public boolean isInstitutionalNumericIdEnabled(Site site)
	{
		for (CandidateDetailProvider provider : getProviders())
		{
			if (provider.isInstitutionalNumericIdEnabled(site))
			{
				return true;
			}
		}
		
		return false;
	}
}