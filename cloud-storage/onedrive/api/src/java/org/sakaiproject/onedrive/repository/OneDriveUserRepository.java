package org.sakaiproject.onedrive.repository;

import org.sakaiproject.onedrive.model.OneDriveUser;
import org.sakaiproject.serialization.SerializableRepository;

public interface OneDriveUserRepository extends SerializableRepository<OneDriveUser, String> {
	public OneDriveUser findBySakaiId(String id);
}
