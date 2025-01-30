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
package org.sakaiproject.scorm.dao.standalone;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import lombok.extern.slf4j.Slf4j;

import org.adl.sequencer.ISeqActivityTree;

import org.sakaiproject.scorm.dao.api.SeqActivityTreeDao;

@Slf4j
public class StandaloneActivityTreeDaoImpl implements SeqActivityTreeDao
{
	private final String storagePath = "trees";

	@Override
	public ISeqActivityTree find(long contentPackageId, String userId)
	{
		return (ISeqActivityTree) getObject(getKey(contentPackageId, userId));
	}

	private String getKey(long contentPackageId, String learnerId)
	{
		return new StringBuilder(learnerId).append(":").append(contentPackageId).toString();
	}

	public Object getObject(String key)
	{
		File objectFile = new File(storagePath, key);
		if (objectFile.exists())
		{
			try
			{
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(objectFile));
				Object obj = ois.readObject();
				return obj;
			}
			catch (Exception e)
			{
				log.error("Unable to unserialize the object for {}", key, e);
			}
		}

		return null;
	}

	public void putObject(String key, Object object)
	{
		File directory = new File(storagePath);
		File objectFile = new File(directory, key);
		if (!directory.exists())
		{
			if (!directory.mkdirs())
			{
				log.error("Unable to create directory {}", directory);
			}
		}

		FileOutputStream fos;
		try
		{
			fos = new FileOutputStream(objectFile);
			try (ObjectOutputStream oos = new ObjectOutputStream(fos))
			{
				oos.writeObject(object);
			}
			fos.close();
		}
		catch (Exception e)
		{
			log.error("Unable to serialize object to disk", e);
		}
	}

	@Override
	public void save(ISeqActivityTree tree)
	{
		putObject(getKey(tree.getContentPackageId(), tree.getLearnerID()), tree);
	}
}
