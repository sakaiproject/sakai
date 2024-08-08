package org.sakaiproject.microsoft.api.converters;

import org.sakaiproject.microsoft.api.data.CreationStatus;
import org.sakaiproject.microsoft.api.data.SynchronizationStatus;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class JpaConverterCreationStatus implements AttributeConverter<CreationStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(CreationStatus status) {
        if (status == null) {
            return null;
        }
        return status.getCode();
    }

    @Override
    public CreationStatus convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }

        return CreationStatus.fromCode(code);
    }
}
