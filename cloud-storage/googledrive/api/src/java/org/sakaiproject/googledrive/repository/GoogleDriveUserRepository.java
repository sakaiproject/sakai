package org.sakaiproject.googledrive.repository;

import org.sakaiproject.googledrive.model.GoogleDriveUser;
import org.sakaiproject.serialization.SerializableRepository;

public interface GoogleDriveUserRepository extends SerializableRepository<GoogleDriveUser, String> {
	public GoogleDriveUser findBySakaiId(String id);
}
