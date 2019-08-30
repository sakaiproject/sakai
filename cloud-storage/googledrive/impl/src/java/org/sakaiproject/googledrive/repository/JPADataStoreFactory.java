package org.sakaiproject.googledrive.repository;

import java.io.IOException;

import com.google.api.client.util.store.AbstractDataStoreFactory;

public class JPADataStoreFactory extends AbstractDataStoreFactory {

	private GoogleDriveUserRepository googledriveRepo;
	public JPADataStoreFactory(GoogleDriveUserRepository repository) {
		this.googledriveRepo = repository;
	}

	@Override
	protected JPADataStore createDataStore(String id) throws IOException {
		return new JPADataStore(this, id, googledriveRepo);
	}
}