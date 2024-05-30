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
package org.sakaiproject.scorm.dao.sakai;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import lombok.extern.slf4j.Slf4j;

import org.adl.sequencer.ISeqActivityTree;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.scorm.dao.api.SeqActivityTreeDao;
import org.sakaiproject.tool.api.ToolManager;

@Slf4j
public abstract class SakaiActivityTreeDaoImpl implements SeqActivityTreeDao
{
	protected abstract ContentHostingService contentService();
	protected abstract ToolManager toolManager();

	@Override
	public ISeqActivityTree find(long contentPackageId, String learnerId)
	{
		Object obj = getObject(getKey(contentPackageId, learnerId));
		return (ISeqActivityTree) obj;
	}

	private String getKey(long contentPackageId, String learnerId)
	{
		return new StringBuilder(learnerId).append(":").append(contentPackageId).toString();
	}

	private Object getObject(String key)
	{
		Object object = null;

		try
		{
			ContentResource objectResource = contentService().getResource(key);
			byte[] bytes = objectResource.getContent();

			try (ByteArrayInputStream in = new ByteArrayInputStream(bytes))
			{
				ObjectInputStream ie = new ObjectInputStream(in);
				object = ie.readObject();
				ie.close();
			}
		}
		catch (Exception ioe)
		{
			log.error("Caught io exception reading manifest from file!", ioe);
		}

		return object;
	}

	private String putObject(String key, Object object)
	{
		ContentResource resource = null;

		String site = toolManager().getCurrentPlacement().getContext();
		String tool = "scorm";
		String type = "application/octet-stream";

		try
		{
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			try (ObjectOutputStream out = new ObjectOutputStream(byteOut))
			{
				out.writeObject(object);
			}

			resource = contentService().addAttachmentResource(key, site, tool, type, byteOut.toByteArray(), null);
			return resource.getId();
		}
		catch (Exception soe)
		{
			log.error("Caught an exception adding an attachment resource!", soe);
		}

		return null;
	}

	@Override
	public void save(ISeqActivityTree tree)
	{
		String key = getKey(tree.getContentPackageId(), tree.getLearnerID());
		putObject(key, tree);
	}
}
