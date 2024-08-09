/**
 * Copyright (c) 2024 The Apereo Foundation
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
package org.sakaiproject.microsoft.controller.auxiliar;

import lombok.Data;

import java.time.LocalDate;
import java.util.Calendar;

@Data
public class MainSessionBean {
	private static final Integer DEFAULT_PAGE_SIZE = 50;
	
	private String sortBy = "status";
	private String sortOrder = "ASC";
	private Integer pageNum = 0;
	private Integer pageSize = DEFAULT_PAGE_SIZE;
	private String search;
	private String siteProperty = "";
	private String fromDate = LocalDate.of(LocalDate.now().getMonthValue() >= 7 ? LocalDate.now().getYear() : LocalDate.now().getYear() - 1, Calendar.AUGUST, 15).toString();
	private String toDate = LocalDate.of(LocalDate.now().getMonthValue() >= 7 ? LocalDate.now().getYear() + 1 : LocalDate.now().getYear(), Calendar.AUGUST, 15).toString();
}
