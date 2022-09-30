/*
 * Copyright (c) 2021- Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.sakaiproject.plus.api.repository;

import org.sakaiproject.plus.api.model.Subject;
import org.sakaiproject.plus.api.model.Tenant;
import org.sakaiproject.springframework.data.SpringCrudRepository;

public interface SubjectRepository extends SpringCrudRepository<Subject, String> {

	public Subject findBySubjectAndTenant(String subject, Tenant tenant);

	public Subject findByEmailAndTenant(String email, Tenant tenant);

	public Subject findBySakaiUserIdAndSakaiSiteId(String userId, String siteId);

}
