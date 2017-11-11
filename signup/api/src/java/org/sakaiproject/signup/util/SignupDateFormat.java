/**
 * Copyright (c) 2007-2014 The Apereo Foundation
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
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.signup.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>
 * This class provides some static date formatting methods, which are commonly
 * used by Signup tool
 * </p>
 */
public class SignupDateFormat {

	public static SimpleDateFormat dateFormat = new SimpleDateFormat();

	/**
	 * display date as example: 11:30 AM
	 * 
	 * @param date
	 * @return a date string
	 */
	public static String format_h_mm_a(Date date) {
		if (date == null)
			return "";
		dateFormat.applyPattern("h:mm a");
		return dateFormat.format(date);
	}

	/**
	 * display a date as example: 01/22/08 11:30 AM
	 * 
	 * @param date
	 * @return a date string
	 */
	public static String format_date_h_mm_a(Date date) {
		if (date == null)
			return "";
		dateFormat.applyPattern("MM/dd/yy h:mm a");
		return dateFormat.format(date);
	}

	/**
	 * display a date as example: 01/22/08
	 * 
	 * @param date
	 * @return a date string
	 */
	public static String format_date_mm_dd_yy(Date date) {
		if (date == null)
			return "";
		dateFormat.applyPattern("MM/dd/yy");
		return dateFormat.format(date);
	}

}
