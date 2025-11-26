/*
 * Copyright (c) 2003-2025 The Apereo Foundation
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
