package org.sakaiproject.googledrive.repository;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import com.google.api.client.auth.oauth2.StoredCredential;
import org.sakaiproject.googledrive.model.GoogleDriveUser;
import com.google.api.client.util.store.AbstractDataStore;
import com.google.api.client.util.store.DataStore;

public class JPADataStore extends AbstractDataStore<StoredCredential> {
	private GoogleDriveUserRepository repository;
	private JPADataStoreFactory jpaDataStoreFactory;

	/**
	 * @param dataStoreFactory data store factory
	 * @param id			   data store ID
	 */
	protected JPADataStore(JPADataStoreFactory dataStoreFactory, String id, GoogleDriveUserRepository repository) {
		super(dataStoreFactory, id);
		this.repository = repository;
	}

	@Override
	public JPADataStoreFactory getDataStoreFactory() {
		return jpaDataStoreFactory;
	}

	@Override
	public int size() throws IOException {
		return (int) repository.count();
	}

	@Override
	public boolean isEmpty() throws IOException {
		return size() == 0;
	}

	@Override
	public boolean containsKey(String key) throws IOException {
		//not implemented
		return false;
	}

	@Override
	public boolean containsValue(StoredCredential value) throws IOException {
		//not implemented
		return false;
	}

	@Override
	public Set<String> keySet() throws IOException {
		//not implemented
		return null;
	}

	@Override
	public Collection<StoredCredential> values() throws IOException {
		//not implemented
		return null;
	}

	@Override
	public StoredCredential get(String userId) throws IOException {
		GoogleDriveUser gdu = repository.findBySakaiId(userId);
		if(gdu == null) {
			return null;
		}
		StoredCredential credential = new StoredCredential();
		credential.setRefreshToken(gdu.getRefreshToken());
		credential.setAccessToken(gdu.getToken());
		//credential.setExpirationTimeMilliseconds(googleCredential.getExpirationTimeMilliseconds());
		return credential;
	}

	@Override
	public DataStore<StoredCredential> set(String userId, StoredCredential cred) throws IOException {
		GoogleDriveUser gdu = repository.findBySakaiId(userId);
		if(gdu == null) {
			gdu = new GoogleDriveUser();
			gdu.setSakaiUserId(userId);
			gdu.setToken(cred.getAccessToken());
			gdu.setRefreshToken(cred.getRefreshToken());
			repository.save(gdu);
		} else {
			gdu.setToken(cred.getAccessToken());
			gdu.setRefreshToken(cred.getRefreshToken());
			repository.update(gdu);
		}
		return this;
	}

	@Override
	public DataStore<StoredCredential> clear() throws IOException {
		repository.deleteAll();
		return this;
	}

	@Override
	public DataStore<StoredCredential> delete(String userId) throws IOException {
		repository.delete(userId);
		return this;
	}
}