package org.sakaiproject.sitestats.tool.wicket.providers.infinite;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;

/**
 * Abstract base class for a sortable infinite data provider
 * @author plukasew
 * @param <T> model type
 * @param <S> sort type
 */
public abstract class AbstractSortableInfiniteDataProvider<T, S> implements SortableInfiniteDataProvider<T, S>
{
	private final SingleSortState<S> state = new SingleSortState<>();

	@Override
	public final ISortState<S> getSortState()
	{
		return state;
	}

	public SortParam<S> getSort()
	{
		return state.getSort();
	}

	/**
	 * Sets the current sort state
	 *
	 * @param param parameter containing new sorting information
	 */
	public void setSort(final SortParam<S> param)
	{
		state.setSort(param);
	}

	/**
	 * Sets the current sort state
	 *
	 * @param property sort property
	 * @param order sort order
	 */
	public void setSort(final S property, final SortOrder order)
	{
		state.setPropertySortOrder(property, order);
	}

	@Override
	public void detach()
	{
		// noop
	}
}
