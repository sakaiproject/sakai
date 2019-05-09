package org.sakaiproject.sitestats.tool.wicket.components.paging.infinite;

import java.util.Collection;
import java.util.Iterator;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.IModel;

/**
 * Abstract base class for the view used by SakaiInfinitePagingDataTable
 * @author plukasew
 * @param <T>
 */
public abstract class AbstractInfinitePagingView<T> extends RefreshingView<T>
{
	@Getter private long itemsPerPage = Long.MAX_VALUE;
	@Getter @Setter protected long offset = 0;
	protected boolean hasNextPage = false;
	@Getter protected int rowCount = 0;

	public AbstractInfinitePagingView(String id, IModel<? extends Collection<? extends T>> model)
	{
		super(id, model);
	}

	public AbstractInfinitePagingView(String id)
	{
		super(id);
	}

	@Override
	protected Iterator<IModel<T>> getItemModels()
	{
		return getItemModels(offset, itemsPerPage);
	}

	protected abstract Iterator<IModel<T>> getItemModels(long offset, long count);

	public final void setItemsPerPage(long value)
	{
		if (value < 1)
		{
			throw new IllegalArgumentException("Items per page must be greater than 0");
		}

		if (itemsPerPage != value && isVersioned())
		{
			addStateChange();
		}

		itemsPerPage = value;
	}

	public boolean hasPrevPage()
	{
		return offset > 0;
	}

	public boolean hasNextPage()
	{
		return hasNextPage;
	}

	public void nextPage()
	{
		if (hasNextPage())
		{
			offset += itemsPerPage;
		}
	}

	public void prevPage()
	{
		if (hasPrevPage())
		{
			offset -= itemsPerPage;
			if (offset < 0)
			{
				offset = 0;
			}
		}
	}
}
