/**
 * Copyright (c) 2003-2018 The Apereo Foundation
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
package org.sakaiproject.service.gradebook.shared;

import java.util.Collections;
import java.util.List;

/**
 * Immutable category score and list of gradebook items dropped (highest/lowest) from the score calculation
 * @author plukasew
 */
public final class CategoryScoreData
{
	public final double score;
	public final List<Long> droppedItems;

	public CategoryScoreData(double score, List<Long> droppedItems)
	{
		this.score = score;
		this.droppedItems = Collections.unmodifiableList(droppedItems);
	}
}
