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
