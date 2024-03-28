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
package org.sakaiproject.scorm.adl.impl;

import lombok.extern.slf4j.Slf4j;

import org.adl.datamodels.IDataManager;
import org.adl.sequencer.ISeqActivityTree;
import org.adl.sequencer.ISequencer;
import org.adl.sequencer.SeqActivityTree;
import org.adl.sequencer.impl.ADLSequencer;

import org.sakaiproject.scorm.adl.ADLConsultant;
import org.sakaiproject.scorm.dao.api.ActivityTreeHolderDao;
import org.sakaiproject.scorm.dao.api.ContentPackageManifestDao;
import org.sakaiproject.scorm.dao.api.DataManagerDao;
import org.sakaiproject.scorm.model.api.ActivityTreeHolder;
import org.sakaiproject.scorm.model.api.ContentPackageManifest;
import org.sakaiproject.scorm.model.api.ScoBean;
import org.sakaiproject.scorm.model.api.SessionBean;

@Slf4j
public abstract class ADLConsultantImpl implements ADLConsultant
{
	protected abstract ActivityTreeHolderDao activityTreeHolderDao();
	protected abstract ContentPackageManifestDao contentPackageManifestDao();
	protected abstract DataManagerDao dataManagerDao();

	@Override
	public ISeqActivityTree getActivityTree(SessionBean sessionBean)
	{
		// First, we check to see if the tree is cached in the session bean 
		ActivityTreeHolder treeHolder = sessionBean.getTreeHolder();

		if (treeHolder == null)
		{
			// If not, we look to see if there's a modified version in the data store
			treeHolder = activityTreeHolderDao().find(sessionBean.getContentPackage().getContentPackageId(), sessionBean.getLearnerId());

			if (treeHolder == null)
			{
				// Finally, if all else fails, we look up the prototype version - this is the first time
				// the user has launched the content package
				ContentPackageManifest manifest = getManifest(sessionBean);
				if (manifest == null)
				{
					log.error("Could not find a valid manifest!");
				}
				else
				{
					ISeqActivityTree tree = manifest.getActTreePrototype();
					tree.setContentPackageId(sessionBean.getContentPackage().getContentPackageId());
					tree.setLearnerID(sessionBean.getLearnerId());

					treeHolder = new ActivityTreeHolder(sessionBean.getContentPackage().getContentPackageId(), sessionBean.getLearnerId());
					treeHolder.setSeqActivityTree((SeqActivityTree) tree);
				}
			}

			if (treeHolder != null)
			{
				sessionBean.setTreeHolder(treeHolder);
			}
		}

		if (treeHolder == null)
		{
			return null;
		}

		return treeHolder.getSeqActivityTree();
	}

	@Override
	public IDataManager getDataManager(SessionBean sessionBean, ScoBean scoBean)
	{
		IDataManager dataManager;

		if (scoBean.getDataManagerId() == null)
		{
			dataManager = dataManagerDao().find(sessionBean.getContentPackage().getContentPackageId(), sessionBean.getLearnerId(), sessionBean.getAttemptNumber(), scoBean.getScoId());
			scoBean.setDataManagerId(dataManager.getId());
		}
		else
		{
			dataManager = dataManagerDao().load(scoBean.getDataManagerId());
		}

		return dataManager;
	}

	@Override
	public ContentPackageManifest getManifest(SessionBean sessionBean)
	{
		// First, check to see if the manifest is cached in the session bean
		ContentPackageManifest manifest = sessionBean.getManifest();

		if (manifest == null)
		{
			//manifest = resourceService().getManifest(sessionBean.getContentPackage().getManifestResourceId());
			manifest = contentPackageManifestDao().load(sessionBean.getContentPackage().getManifestId());
			if (manifest != null)
			{
				sessionBean.setManifest(manifest);
			}
		}

		return manifest;
	}

	@Override
	public ISequencer getSequencer(ISeqActivityTree tree)
	{
		// Create the sequencer and set the tree
		ISequencer sequencer = new ADLSequencer();
		sequencer.setActivityTree(tree);
		return sequencer;
	}
}
