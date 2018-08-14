package org.sakaiproject.sitestats.tool.wicket.providers.infinite;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;

/**
 * Interface to combine InfiteDataProvider and ISortStateLocator interfaces into a single interface
 * @author plukasew
 */
public interface SortableInfiniteDataProvider<T, S> extends InfiniteDataProvider<T>, ISortStateLocator<S>
{
	// empty
}
