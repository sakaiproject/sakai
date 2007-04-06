package org.sakaiproject.scorm.service.app.dao;

import org.adl.datamodels.DMElement;
import org.springframework.orm.hibernate3.HibernateTemplate;

public class DMElementDAO {

	private HibernateTemplate hibernateTemplate;
	
	public long store(DMElement dmElement) {
		Long key = (Long)hibernateTemplate.save(dmElement);
		return key.longValue();
	}
	
	public DMElement get(long id) {
		return (DMElement)hibernateTemplate.get(DMElement.class, new Long(id));
	}
	
	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	
}
