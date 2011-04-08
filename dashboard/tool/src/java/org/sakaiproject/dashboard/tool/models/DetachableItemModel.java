package org.sakaiproject.dashboard.tool.models;

import org.apache.wicket.model.LoadableDetachableModel;
import org.sakaiproject.dashboard.logic.DashboardLogic;
import org.sakaiproject.dashboard.model.DashboardItem;
import org.sakaiproject.dashboard.tool.Locator;

public class DetachableItemModel extends LoadableDetachableModel{

	
	    private final long id;

	    protected DashboardLogic getDashboardLogic()
	    {
	    	return Locator.getDashboardLogic();
	    }

	    /**
	     * @param c
	     */
	    public DetachableItemModel(DashboardItem object)
	    {
	        this(object.getId());
	    }

	    /**
	     * @param id
	     */
	    public DetachableItemModel(long id)
	    {
	        if (id == 0)
	        {
	            throw new IllegalArgumentException();
	        }
	        this.id = id;
	    }

	    /**
	     * @see java.lang.Object#hashCode()
	     */
	    @Override
	    public int hashCode()
	    {
	        return Long.valueOf(id).hashCode();
	    }

	    /**
	     * used for dataview with ReuseIfModelsEqualStrategy item reuse strategy
	     * 
	     * @see org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy
	     * @see java.lang.Object#equals(java.lang.Object)
	     */
	    @Override
	    public boolean equals(final Object obj)
	    {
	        if (obj == this)
	        {
	            return true;
	        }
	        else if (obj == null)
	        {
	            return false;
	        }
	        else if (obj instanceof DetachableItemModel)
	        {
	        	DetachableItemModel other = (DetachableItemModel)obj;
	            return other.id == id;
	        }
	        return false;
	    }

	    /**
	     * @see org.apache.wicket.model.LoadableDetachableModel#load()
	     */
	    @Override
	    protected DashboardItem load()
	    {
	        // loads item from the database
	        return getDashboardLogic().getItemById(id);
	    }
	
	
	
	
	
}
