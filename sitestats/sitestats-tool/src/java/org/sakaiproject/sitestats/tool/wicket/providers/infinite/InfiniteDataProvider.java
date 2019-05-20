/**
 * Copyright (c) 2006-2018 The Apereo Foundation
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
package org.sakaiproject.sitestats.tool.wicket.providers.infinite;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

/**
 * A data provider capable of providing paged results for a data set of unknown size.
 * @author plukasew
 */
public interface InfiniteDataProvider<T> extends IDetachable
{
	PagedInfiniteIterator<? extends T> iterator(long first, long count);

	IModel<T> model(T object);
}
