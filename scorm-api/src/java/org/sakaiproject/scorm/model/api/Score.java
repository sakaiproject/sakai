/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.scorm.model.api;

import java.io.Serializable;

public class Score implements Serializable {

	private static final long serialVersionUID = 1L;

	private double scaled;

	private double raw;

	private double min;

	private double max;

	private double scaledToPass;

	public double getMax() {
		return max;
	}

	public double getMin() {
		return min;
	}

	public double getRaw() {
		return raw;
	}

	public double getScaled() {
		return scaled;
	}

	public double getScaledToPass() {
		return scaledToPass;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public void setRaw(double raw) {
		this.raw = raw;
	}

	public void setScaled(double scaled) {
		this.scaled = scaled;
	}

	public void setScaledToPass(double scaledToPass) {
		this.scaledToPass = scaledToPass;
	}

}
