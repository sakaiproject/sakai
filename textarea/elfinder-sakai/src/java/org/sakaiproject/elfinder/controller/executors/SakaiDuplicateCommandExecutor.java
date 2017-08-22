/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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
package org.sakaiproject.elfinder.controller.executors;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.sakaiproject.elfinder.sakai.SakaiFsService;

import cn.bluejoe.elfinder.controller.executor.AbstractJsonCommandExecutor;
import cn.bluejoe.elfinder.controller.executor.CommandExecutor;
import cn.bluejoe.elfinder.controller.executor.FsItemEx;
import cn.bluejoe.elfinder.service.FsItem;
import cn.bluejoe.elfinder.service.FsService;

public class SakaiDuplicateCommandExecutor extends AbstractJsonCommandExecutor implements CommandExecutor
{
	@Override
	public void execute(FsService fsService, HttpServletRequest request, ServletContext servletContext, JSONObject json)
			throws Exception
	{
		String[] targets = request.getParameterValues("targets[]");

		List<FsItemEx> added = new ArrayList<FsItemEx>();
		SakaiFsService sfsService = (SakaiFsService)fsService;
		
		for (String target : targets)
		{
			String newId = "";
			FsItem ftgt = sfsService.fromHash(target);
			FsItemEx ftgtex = new FsItemEx(ftgt, fsService);
			
			String tgtId = sfsService.asId(ftgt); 
			
			//if target is folder
			if(ftgtex.isFolder()) {
				//get folder id
				String folderId = ftgt.getVolume().getName(ftgt);
				folderId = folderId.replaceAll("\\(\\d+\\)$", "");
				
				//check for new, not existing, folder id
				int count = 1;
				FsItemEx dir = null;
				String newName = "";
				do {
					newName = String.format("%s(%d)/", folderId, count);
					dir = new FsItemEx(ftgtex.getParent(), newName);
					count++;
				} while(count < 999 && (dir != null && dir.exists()));
				
				//copy the folder (it will fail if folder is not empty)
				if(count < 999)
					newId = sfsService.getContent().copy(tgtId, ftgtex.getParent().getPath()+"/"+newName);
			}
			else //resources will rename on copy
				newId = sfsService.getContent().copy(tgtId, tgtId);
			
			try {
	    		int lastSlash = newId.lastIndexOf("/");

	    		if(lastSlash >= 0) {

		    		if ((lastSlash+1) == newId.length()) {
		    			lastSlash = newId.lastIndexOf("/", lastSlash-1);
		    		}
		    		newId = newId.substring(lastSlash+1);
	    		}
	    	} catch(Exception e) {}
			
			added.add(new FsItemEx(ftgtex.getParent(), newId));
		}

		json.put("added", files2JsonArray(request, added));
	}
}
