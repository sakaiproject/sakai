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

package org.sakaiproject.content.metadata.mixins;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Jackson description for {@link org.sakaiproject.content.metadata.model.MetadataType}
 * <p/>
 * Ignore getters/setters/constructors, seek for every field (even private fields)
 * Store the current class in "@class"
 * Ignore default values
 *
 * @author Colin Hebert
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonAutoDetect(value = JsonMethod.FIELD,
		fieldVisibility = JsonAutoDetect.Visibility.ANY,
		creatorVisibility = JsonAutoDetect.Visibility.NONE,
		getterVisibility = JsonAutoDetect.Visibility.NONE,
		isGetterVisibility = JsonAutoDetect.Visibility.NONE,
		setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_DEFAULT)
public interface MetadataTypeMixin
{
}
