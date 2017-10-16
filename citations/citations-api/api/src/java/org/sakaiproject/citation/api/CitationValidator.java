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
package org.sakaiproject.citation.api;

import java.util.List;

/**
 * Created by nickwilson on 9/29/15.
 */
public interface CitationValidator {

	String getValidMessage(List<CitationCollectionOrder> citationCollectionOrderList, CitationCollectionOrder citationCollectionOrderTopNode, CitationCollection collection);

	String getAddSectionErrorMessage(CitationCollectionOrder citationCollectionOrder, CitationCollection collection);

	String getRemoveSectionErrorMessage(CitationCollection collection, int locationId);

	String getAddSubSectionErrorMessage(CitationCollectionOrder citationCollectionOrder, CitationCollection collection);

	String validateExistingDbStructure(CitationCollection collection);

	String getDragAndDropErrorMessage(List<CitationCollectionOrder> citationCollectionOrders, CitationCollection collection);

	String getUpdateSectionErrorMessage(CitationCollectionOrder citationCollectionOrder, CitationCollection collection);
}
