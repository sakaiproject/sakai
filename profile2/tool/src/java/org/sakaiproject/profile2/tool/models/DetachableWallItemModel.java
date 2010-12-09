package org.sakaiproject.profile2.tool.models;

import org.apache.wicket.model.LoadableDetachableModel;
import org.sakaiproject.profile2.model.WallItem;

public class DetachableWallItemModel extends LoadableDetachableModel<WallItem>{

	private static final long serialVersionUID = 1L;

	private WallItem wallItem;
	
	public DetachableWallItemModel(WallItem wallItem) {
		this.wallItem = wallItem;
	}
	
	@Override
	protected WallItem load() {
		return wallItem;
	}

}
