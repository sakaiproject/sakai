/**
 * 
 */
package org.sakaiproject.sitestats.tool.wicket.models;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;


public class UserModel extends LoadableDetachableModel {
	private static final long		serialVersionUID	= 1L;
	private static Log				LOG					= LogFactory.getLog(UserModel.class);

	@SpringBean
	private transient SakaiFacade	facade;

	private String					id;

	public UserModel(User u) {
		this(u.getId());
	}

	public UserModel(String id) {
		InjectorHolder.getInjector().inject(this);
		if(id == null){
			throw new IllegalArgumentException();
		}
		this.id = id;
	}

	public String getId() {
		return id;
	}

	@Override
	protected Object load() {
		try{
			return facade.getUserDirectoryService().getUser(getId());
		}catch(UserNotDefinedException e){
			LOG.warn("UserModel: no user with id " + getId());
			return null;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this){
			return true;
		}else if(obj == null){
			return false;
		}else if(obj instanceof UserModel){
			UserModel other = (UserModel) obj;
			return other.getId() == this.getId();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

}