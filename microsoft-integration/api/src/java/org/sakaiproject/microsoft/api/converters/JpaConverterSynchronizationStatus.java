package org.sakaiproject.microsoft.api.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.sakaiproject.microsoft.api.data.SynchronizationStatus;

@Converter(autoApply = true)
public class JpaConverterSynchronizationStatus implements AttributeConverter<SynchronizationStatus, Integer> {

	@Override
	public Integer convertToDatabaseColumn(SynchronizationStatus status) {
		if (status == null) {
			return null;
		}
		return status.getCode();
	}

	@Override
	public SynchronizationStatus convertToEntityAttribute(Integer code) {
		if (code == null) {
			return null;
		}

		return SynchronizationStatus.fromCode(code);
	}
}