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

package org.sakaiproject.content.metadata.logic;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.sakaiproject.content.metadata.mixins.ListMetadataTypeMixin;
import org.sakaiproject.content.metadata.mixins.MetadataTypeMixin;
import org.sakaiproject.content.metadata.model.ListMetadataType;
import org.sakaiproject.content.metadata.model.MetadataType;

/**
 * A simple parser that takes a JSON input stream and returns the MetadataTypes.
 * @author buckett
 *
 */
public class JsonMetadataParser implements MetadataParser {

	public List<MetadataType> parse(InputStream inputStream)
	{
		/**
		 *  FIXME: The ContextClassLoader is switched in order to work with {@link org.codehaus.jackson.map.jsontype.impl#typeFromId(String)}
		 *  The current ContextClassLoader is the one from the tool making the call (ie. ContentTool) so it doesn't contain the actual implementation of metadatatypes
		 *  The classloader is switched back later in the finally clause (as it HAS to be restored)
		 *
		 *  See JACKSON-350.
		 */
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try
		{
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.getDeserializationConfig().addMixInAnnotations(MetadataType.class, MetadataTypeMixin.class);
			objectMapper.getDeserializationConfig().addMixInAnnotations(ListMetadataType.class, ListMetadataTypeMixin.class);
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			return objectMapper.readValue(inputStream, new TypeReference<List<MetadataType>>() {});
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			Thread.currentThread().setContextClassLoader(cl);
		}
	}


}
