package org.sakaiproject.onedrive.repository;

import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;
import org.sakaiproject.serialization.BasicSerializableRepository;

import org.sakaiproject.onedrive.model.OneDriveUser;
import org.sakaiproject.onedrive.repository.OneDriveUserRepository;

/**
 * Created by bgarcia
 */
@Transactional(readOnly = true)
public class OneDriveUserRepositoryImpl extends BasicSerializableRepository<OneDriveUser, String> implements OneDriveUserRepository {

	@Override
	public OneDriveUser findBySakaiId(String sakaiId){
        return (OneDriveUser) startCriteriaQuery()
                .add(Restrictions.eq("sakaiUserId", sakaiId))
                .uniqueResult();
    }
}
