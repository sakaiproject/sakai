/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.metadata.model;

/**
 * @author Matthew Buckett
 */
public class Duration {

	public enum Unit {
		YEAR, SEMESTER, STUDY_PERIOD, TERM, MONTH, WEEK, DAY, CLASS, HOUR, MINUTE, SECOND;

		/**
		 * @return The lowercase version of the name.
		 */
		public String toString() {
			return this.name().toLowerCase();
		}

		public static Unit parse(Object value) {
			try {
				if (value != null) {
					return Unit.valueOf(value.toString().toUpperCase());
				}
			} catch (IllegalArgumentException iae) {
				// Ignore
			}
			return null;
		}
	}

	private Integer firstCount;
	private Unit firstUnit;
	private Integer secondCount;
	private Unit secondUnit;

	public Integer getFirstCount() {
		return firstCount;
	}

	public void setFirstCount(Integer firstCount) {
		this.firstCount = firstCount;
	}

	public Unit getFirstUnit() {
		return firstUnit;
	}

	public void setFirstUnit(Unit firstUnit) {
		this.firstUnit = firstUnit;
	}

	public Integer getSecondCount() {
		return secondCount;
	}

	public void setSecondCount(Integer secondCount) {
		this.secondCount = secondCount;
	}

	public Unit getSecondUnit() {
		return secondUnit;
	}

	public void setSecondUnit(Unit secondUnit) {
		this.secondUnit = secondUnit;
	}

	boolean isEmpty() {
		return firstCount == null && firstUnit == null && secondCount == null && secondUnit == null;
	}
}
