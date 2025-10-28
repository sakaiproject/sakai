package org.sakaiproject.scorm.service.api.launch;

/**
 * High-level status returned for SCORM launch interactions.
 */
public enum ScormLaunchState
{
    READY,
    DENIED,
    CHOICE_REQUIRED,
    ERROR
}
