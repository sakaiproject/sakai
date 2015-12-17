package org.sakaiproject.citation.api;

import java.util.List;

/**
 * Created by nickwilson on 9/29/15.
 */
public interface CitationValidator {

	boolean isValid(List<CitationCollectionOrder> citationCollectionOrders);
}
