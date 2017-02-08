/**********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
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

package org.sakaiproject.rubrics.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * Rubric Criterion
 *
 * @author Brad Szabo (bszabo@unicon.net)
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Criterion implements Serializable {

	private long id;
	private String title;
	private String description;
    private Metadata metadata;
    private boolean optional;
    private List<Rating> ratings;

	@Data
	static class Metadata {
		private String created;
		private String modified;
	}
}
