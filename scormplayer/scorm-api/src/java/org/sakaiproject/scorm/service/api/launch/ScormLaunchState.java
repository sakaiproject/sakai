package org.sakaiproject.scorm.service.api.launch;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * High-level status returned for SCORM launch interactions.
 * Enum names form part of the public REST contract and must remain stable.
 */
// Ensure string serialization for REST representations
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum ScormLaunchState
{
    READY,
    DENIED,
    CHOICE_REQUIRED,
    ERROR
}
