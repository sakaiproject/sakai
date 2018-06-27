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
