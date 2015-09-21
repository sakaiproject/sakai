package org.sakaiproject.elfinder.controller.executors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import cn.bluejoe.elfinder.controller.executor.AbstractJsonCommandExecutor;
import cn.bluejoe.elfinder.controller.executor.CommandExecutor;
import cn.bluejoe.elfinder.controller.executor.FsItemEx;
import cn.bluejoe.elfinder.service.FsService;

public class SakaiRenameCommandExecutor extends AbstractJsonCommandExecutor implements CommandExecutor
{
	@Override
	public void execute(FsService fsService, HttpServletRequest request, ServletContext servletContext, JSONObject json)
			throws Exception
	{
		String target = request.getParameter("target");
		String current = request.getParameter("current");
		String name = request.getParameter("name");

		FsItemEx fsi = super.findItem(fsService, target);
		FsItemEx dst = new FsItemEx(fsi.getParent(), name);
		fsi.renameTo(dst);
		/*
 		* The difference between this and the original one is that it returns details of the original item as being added
 		* rather than the new item. The reason for this is that we don't actually have a new item, it's just the same
 		* item with a different display name.
 		*/
		json.put("added", new Object[] { getFsItemInfo(request, fsi) });
		json.put("removed", new String[] { target });
	}
}
