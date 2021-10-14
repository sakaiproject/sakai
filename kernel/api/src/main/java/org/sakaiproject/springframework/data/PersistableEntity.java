/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.springframework.data;

/**
 * If you are using the SpringCrudRepositoryImpl for your JPA persistence, you need
 * to implement this interface in your entity beans. This allows the repo code to work out
 * if an entity is new or not, based on its id. If you are using Lombok in your entity bean and
 * you have an attribute of "id", then you'll have the impl already and you just need to add 
 * <code>implements PersistableEntity</code> with the type of your id field. For an example, take
 * a look at Task and UserTask in the kernel api.
 */
public interface PersistableEntity<T> {
    T getId();
}
