/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.user.tool;

import lombok.Data;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.sakaiproject.entity.api.ResourceProperties;

/**
 * Model object to store a record about an imported user
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Data
public class ImportedUser {

	private String eid;
	private String firstName;
	private String lastName;
	private String email;
	private String password;
	private String type;
	private ResourceProperties properties;
	
}
