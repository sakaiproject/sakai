/**
 * $URL:$
 * $Id:$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
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
package org.sakaiproject.sitestats.tool.wicket.models;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;


public class SiteModel extends LoadableDetachableModel {
	private static final long		serialVersionUID	= 1L;
	private static Log				LOG					= LogFactory.getLog(SiteModel.class);

	@SpringBean
	private transient SakaiFacade	facade;

	private String					id;

	
	public SiteModel(Site site) {
		this(site.getId());
	}

	public SiteModel(String id) {
		InjectorHolder.getInjector().inject(this);
		if (id == null) {
            throw new IllegalArgumentException();
        }
		this.id = id;
	}

	@Override
	protected Object load() {
		try{
			return facade.getSiteService().getSite(id);
		}catch(IdUnusedException e){
			LOG.warn("SiteModel: no site with id "+id);
			return null;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this){
			return true;
		}else if(obj == null){
			return false;
		}else if(obj instanceof SiteModel){
			SiteModel other = (SiteModel) obj;
			return this.id != null && this.id.equals(other.id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

}
