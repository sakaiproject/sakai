/**
 * Copyright (c) 2023 Apereo Foundation
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

package org.sakaiproject.meetings.controller;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.meetings.exceptions.MeetingsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


/**
 * GlobalController
 * 
 * This controller is used to handle exceptions
 * 
 */
@ControllerAdvice
public class GlobalController {

	@ExceptionHandler(value = {MeetingsException.class, IdUnusedException.class})
	public ResponseEntity<Object> handleMeetingsError(HttpServletRequest req, Exception ex) {
		if (ex instanceof MeetingsException) {
			return ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ex.getMessage());
		}
		if (ex instanceof IdUnusedException) {
			return ResponseEntity
					.status(HttpStatus.NOT_FOUND)
					.body(ex.getMessage());
		}
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ex.getMessage());
	}
}
