/**
 * Copyright (c) 2008-2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.tool.dataproviders;

import java.util.Iterator;

import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.profile2.logic.ProfileWallLogic;
import org.sakaiproject.profile2.model.WallItem;
import org.sakaiproject.profile2.tool.models.DetachableWallItemModel;

/**
 * Data provider for wall items.
 *
 * @author d.b.robinson@lancaster.ac.uk
 */
public class WallItemDataProvider implements IDataProvider<WallItem> {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileWallLogic")
	private ProfileWallLogic wallLogic;
	
	private String userUuid;
	
	public WallItemDataProvider(String userUuid) {
		
		// inject
		InjectorHolder.getInjector().inject(this);
		
		this.userUuid = userUuid;
	}
	
	@Override
	public Iterator<? extends WallItem> iterator(int first, int count) {

		return wallLogic.getWallItemsForUser(userUuid).subList(first,
				first + count).iterator();
	}

	@Override
	public IModel<WallItem> model(WallItem object) {
		return new DetachableWallItemModel(object);
	}

	@Override
	public int size() {	
		return wallLogic.getWallItemsCount(userUuid);
	}

	@Override
	public void detach() {
		// TODO Auto-generated method stub
		
	}

}
