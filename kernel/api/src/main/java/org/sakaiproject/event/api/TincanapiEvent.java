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
package org.sakaiproject.event.api;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table( name = "TINCANAPI_EVENT")
@NamedQuery( name = "getAllTincanapiEvent",
			 query = "from TincanapiEvent")

@Data
public class TincanapiEvent {

    @Id
    @Column(name = "EVENT")
    private String event;

    @Column(name = "VERB", length = 255, nullable = false)
    private String verb;

    @Column(name = "ORIGIN", length = 255, nullable = false)
    private String origin;

    @Column(name = "OBJECT", length = 255, nullable = false)
    private String object;
    
    @Column(name = "EVENT_SUPPLIER", length = 255)
    private String eventSupplier;
}