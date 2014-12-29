/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.tool.models;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple model to back a simple single text field. To be used only by the Profile2 tool.
 * 
 * <p>DO NOT USE THIS YOURSELF.</p>
 *
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 */
@Data
@NoArgsConstructor
public class StringModel implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String string;
	
}
