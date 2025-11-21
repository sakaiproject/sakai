/**
 * Copyright (c) 2024 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.microsoft.impl;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.sakaiproject.microsoft.api.MicrosoftConfigurationService;
import org.sakaiproject.microsoft.api.data.MicrosoftCredentials;
import org.sakaiproject.microsoft.api.data.MicrosoftUserIdentifier;
import org.sakaiproject.microsoft.api.data.SakaiSiteFilter;
import org.sakaiproject.microsoft.api.data.SakaiUserIdentifier;
import org.sakaiproject.microsoft.api.model.MicrosoftConfigItem;
import org.sakaiproject.microsoft.api.persistence.MicrosoftConfigRepository;
import org.springframework.transaction.annotation.Transactional;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@Slf4j
@Transactional
public class MicrosoftConfigurationServiceImpl implements MicrosoftConfigurationService {
	
	
	@Setter
	MicrosoftConfigRepository microsoftConfigRepository;

    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BIT = 128;
    private static final String SECRET_KEY = "0123456789abcdef";

	//------------------------------ CREDENTIALS -------------------------------------------------------
    @Override
    public MicrosoftCredentials getCredentials() {
        MicrosoftCredentials creds = microsoftConfigRepository.getCredentials();
        if (creds != null && creds.getSecret() != null) {
            creds.setSecret(decrypt(creds.getSecret()));
        }
        return creds;
    }
	
	public void saveCredentials(MicrosoftCredentials credentials) {
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftCredentials.KEY_AUTHORITY).value(credentials.getAuthority()).build());
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftCredentials.KEY_CLIENT_ID).value(credentials.getClientId()).build());
        saveOrUpdateSecretConfigItem(MicrosoftCredentials.KEY_SECRET, credentials.getSecret());
        saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftCredentials.KEY_SCOPE).value(credentials.getScope()).build());
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftCredentials.KEY_DELEGATED_SCOPE).value(credentials.getDelegatedScope()).build());
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftCredentials.KEY_EMAIL).value(credentials.getEmail()).build());
	}
	
	//------------------------------- MICROSOFT SYNCHRONIZATION ------------------------------------
	public Map<String, MicrosoftConfigItem> getDefaultSynchronizationConfigItems(){
		return microsoftConfigRepository.getDefaultSynchronizationConfigItems();
	}
	
	public Map<String, MicrosoftConfigItem> getAllSynchronizationConfigItems(){
		return microsoftConfigRepository.getAllSynchronizationConfigItems();
	}
	
	//------------------------------ ONEDRIVE -------------------------------------------------------
	public boolean isOneDriveEnabled() {
		return Boolean.valueOf(microsoftConfigRepository.getConfigItemValueByKey(MicrosoftConfigRepository.ONEDRIVE_ENABLED));
	}
	
	//------------------------------- MICROSOFT SYNCHRONIZATION - NEW SITE ------------------------------------
	public SakaiSiteFilter getNewSiteFilter() {
		return microsoftConfigRepository.getNewSiteFilter();
	}
	
	public void saveNewSiteFilter(SakaiSiteFilter filter) {
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftConfigRepository.NEW_SITE_TYPE).value(filter.getSiteType()).build());
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftConfigRepository.NEW_SITE_PUBLISHED).value(Boolean.toString(filter.isPublished())).build());
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftConfigRepository.NEW_SITE_PROPERTY).value(filter.getSiteProperty()).build());
	}
	
	public long getSyncDuration() {
		return microsoftConfigRepository.getSyncDuration();
	}
	
	public void saveSyncDuration(long syncDuration) {
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftConfigRepository.NEW_SITE_SYNC_DURATION).value(String.valueOf(syncDuration)).build());
	}
	
	//------------------------------- MICROSOFT SYNCHRONIZATION - JOB ------------------------------------
	public SakaiSiteFilter getJobSiteFilter() {
		return microsoftConfigRepository.getJobSiteFilter();
	}
	
	public void saveJobSiteFilter(SakaiSiteFilter filter) {
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftConfigRepository.JOB_SITE_TYPE).value(filter.getSiteType()).build());
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftConfigRepository.JOB_SITE_PUBLISHED).value(Boolean.toString(filter.isPublished())).build());
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftConfigRepository.JOB_SITE_PROPERTY).value(filter.getSiteProperty()).build());
	}
	
	//------------------------------- SAKAI - MICROSOFT USER MAPPING ------------------------------------
	public SakaiUserIdentifier getMappedSakaiUserId() {
		return microsoftConfigRepository.getMappedSakaiUserId();
	}
	
	public MicrosoftUserIdentifier getMappedMicrosoftUserId() {
		return microsoftConfigRepository.getMappedMicrosoftUserId();
	}
	public void saveMappedSakaiUserId(SakaiUserIdentifier identifier) {
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(SakaiUserIdentifier.KEY).value(identifier.getCode()).build());
	}
	public void saveMappedMicrosoftUserId(MicrosoftUserIdentifier identifier) {
		saveOrUpdateConfigItem(MicrosoftConfigItem.builder().key(MicrosoftUserIdentifier.KEY).value(identifier.getCode()).build());
	}
	
	//------------------------------ COLLABORATIVE DOCUMENTS -------------------------------------------------------
	public long getMaxUploadSize() {
		String strValue = microsoftConfigRepository.getConfigItemValueByKey(MicrosoftConfigRepository.MAX_UPLOAD_SIZE);
		try {
			return Long.valueOf(strValue);
		} catch(Exception e) {
			if(strValue != null) {
				log.warn("Invalid Long value in DB for mc_config_item -> "+MicrosoftConfigRepository.MAX_UPLOAD_SIZE);
			}
			return 0;
		}
	}
	
	//------------------------------------------- COMMON ------------------------------------------------
	public String getConfigItemValueByKey(String key) {
		return microsoftConfigRepository.getConfigItemValueByKey(key);
	}
	
	public void saveOrUpdateConfigItem(MicrosoftConfigItem item) {
		if (microsoftConfigRepository.exists(item.getKey())) {
			microsoftConfigRepository.merge(item);
		} else {
			microsoftConfigRepository.save(item);
		}
	}

    private void saveOrUpdateSecretConfigItem(String key, String value) {
        if(value == null) return;
        saveOrUpdateConfigItem(MicrosoftConfigItem.builder()
                .key(key)
                .value(encrypt(value))
                .build());
    }

    private String encrypt(String plainText) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGO);
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(encrypted, 0, combined, IV_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("Error encrypting Microsoft secret", e);
            return null;
        }
    }

    public static String decrypt(String cipherText) {
        if (cipherText == null) return null;
        try {
            byte[] decoded = Base64.getDecoder().decode(cipherText);

            byte[] iv = new byte[IV_LENGTH];
            byte[] encrypted = new byte[decoded.length - IV_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, IV_LENGTH);
            System.arraycopy(decoded, IV_LENGTH, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(ALGO);
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error decrypting Microsoft secret", e);
            return null;
        }
    }

    public void saveConfigItems(List<MicrosoftConfigItem> list){
		list.forEach(item -> saveOrUpdateConfigItem(item));
	}
}
