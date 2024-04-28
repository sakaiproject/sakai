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
package org.sakaiproject.microsoft.api.model;

import java.util.Map;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.microsoft.api.converters.JpaConverterMicrosoftAuthorizationAccount;
import org.sakaiproject.microsoft.api.data.MicrosoftAuthorizationAccount;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Entity
@Table(name = "mc_access_token")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder=true)
@Slf4j
public class MicrosoftAccessToken {
	private static final String KEY_REFRESH_TOKEN = "RefreshToken";
	private static final String KEY_SECRET = "secret";

    @Id
    private String sakaiUserId;

    private String microsoftUserId;

    @Lob
    private String accessToken;
    
    @Convert(converter = JpaConverterMicrosoftAuthorizationAccount.class)
    private MicrosoftAuthorizationAccount account;
    
    @Transient
    private String refreshToken = null;
    public String getRefreshToken() {
    	if(StringUtils.isNotBlank(accessToken) && refreshToken == null) {
    		try {
	    		ObjectMapper objectMapper = new ObjectMapper();
	    		Map<String, Object> map = objectMapper.readValue(accessToken, new TypeReference<Map<String,Object>>(){});
	    		Map<String, Object> refreshTokenMap = (Map<String, Object>)map.get(KEY_REFRESH_TOKEN);
	    		if(refreshTokenMap != null) {
	    			Map<String, String> firstMap = (Map<String, String>)refreshTokenMap.values()
	    					  .stream()
	    					  .findFirst()
	    					  .get();
	    			refreshToken = firstMap.get(KEY_SECRET);
	    		}
    		}catch(Exception e) {
    			log.warn("Error gettting refreshToken from serialized accessToken");
    		}
    	}
    	return refreshToken;
    }
}