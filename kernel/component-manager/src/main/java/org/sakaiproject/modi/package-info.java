/*
 * Copyright (c) 2003-2022 The Apereo Foundation
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
/**
 * MOdern Dependency Injection for Sakai components.
 * <p>
 * This package completes the transition to Spring managing the dependency injection for Sakai. The SpringCompMgr was
 * most of the way there, but left some confusing lifecycle pieces and a strange relationship with the
 * ApplicationContext. With modi, there is a top-level context available globally and web applications can use it as a
 * parent by declaring the context-class.
 */
package org.sakaiproject.modi;
