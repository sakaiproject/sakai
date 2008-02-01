package org.sakaiproject.scorm.dao.hibernate;

import java.io.Serializable;

import org.sakaiproject.scorm.dao.api.ContentPackageManifestDao;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class ContentPackageManifestDaoImpl extends HibernateDaoSupport implements ContentPackageManifestDao {

	public ContentPackageManifest load(Serializable id) {
		return (ContentPackageManifest)getHibernateTemplate().load(ContentPackageManifest.class, id);
	}

	public Serializable save(ContentPackageManifest manifest) {
		return getHibernateTemplate().save(manifest);
	}

}
