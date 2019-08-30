package org.sakaiproject.googledrive.repository;

import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;
import org.sakaiproject.serialization.BasicSerializableRepository;

import org.sakaiproject.googledrive.model.GoogleDriveUser;
import org.sakaiproject.googledrive.repository.GoogleDriveUserRepository;

/**
 * Created by bgarcia
 */
@Transactional(readOnly = true)
public class GoogleDriveUserRepositoryImpl extends BasicSerializableRepository<GoogleDriveUser, String> implements GoogleDriveUserRepository {

	@Override
	public GoogleDriveUser findBySakaiId(String sakaiId){
		return (GoogleDriveUser) startCriteriaQuery().add(Restrictions.eq("sakaiUserId", sakaiId)).uniqueResult();
	}

}
