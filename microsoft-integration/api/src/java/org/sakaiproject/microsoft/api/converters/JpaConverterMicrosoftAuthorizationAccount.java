package org.sakaiproject.microsoft.api.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.microsoft.api.data.MicrosoftAuthorizationAccount;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter(autoApply = true)
public class JpaConverterMicrosoftAuthorizationAccount implements AttributeConverter<MicrosoftAuthorizationAccount, String> {

	private final static ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String convertToDatabaseColumn(MicrosoftAuthorizationAccount meta) {
		try {
			return objectMapper.writeValueAsString(meta);
		} catch (Exception ex) {
			log.error("Error serializing "+meta.getClass(), ex);
		}
		return null;
	}

	@Override
	public MicrosoftAuthorizationAccount convertToEntityAttribute(String dbData) {
		try {
			if(StringUtils.isNotBlank(dbData)) {
				return objectMapper.readValue(dbData, MicrosoftAuthorizationAccount.class);
			}
		} catch (Exception ex) {
			log.error("Error deserializing MicrosoftAuthorizationAccount");
		}
		return null;
	}
}