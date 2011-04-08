package org.sakaiproject.dashboard.tool.dataproviders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.sakaiproject.dashboard.logic.ExternalLogic;
import org.sakaiproject.dashboard.logic.DashboardLogic;
import org.sakaiproject.dashboard.model.DashboardItem;
import org.sakaiproject.dashboard.tool.DashboardApplication;
import org.sakaiproject.dashboard.tool.models.DetachableItemModel;


/**
 * Implementation of IDataProvider that retrieves contacts from the contact database.
 * 
 * @author igor
 * 
 */
public class ItemDataProvider implements IDataProvider {
    
	private transient List<DashboardItem> list = new ArrayList<DashboardItem>();
	private transient List<String> friends = new ArrayList<String>();
	private transient ExternalLogic externalLogic;
	private transient DashboardLogic logic;

	
	public ItemDataProvider() {
		externalLogic = DashboardApplication.get().getExternalLogic();
		logic = DashboardApplication.get().getDashboardLogic();
		list = getItems();
	}
	
	private List<DashboardItem> getItems() {
		return logic.getAllVisibleItems(externalLogic.getCurrentLocationId(), externalLogic.getCurrentUserId());
	}
		
	public Iterator<DashboardItem> iterator(int first, int count) {
		try {
			List<DashboardItem> slice = list.subList(first, first + count);
			return slice.iterator();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Collections.EMPTY_LIST.iterator();
		}
	}

    public int size() {
    	if (list == null) {
			return 0;
    	}
		return list.size();
	}

    public IModel model(Object object) {
    	return new DetachableItemModel((DashboardItem)object);
    }

	public void detach() {
		// TODO Auto-generated method stub
	}
	
}