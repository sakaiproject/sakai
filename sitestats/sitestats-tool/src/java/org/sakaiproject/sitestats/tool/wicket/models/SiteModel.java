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
