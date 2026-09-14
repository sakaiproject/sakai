/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing the License.
 **********************************************************************************/

package org.sakaiproject.sitestats.impl.view;

import java.util.function.BooleanSupplier;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class WidgetHighlightSpec {

	private final String id;
	private final String titleKey;
	private final BooleanSupplier available;
	private final WidgetHighlightFactory factory;

	boolean isAvailable() {
		return available.getAsBoolean();
	}
}
